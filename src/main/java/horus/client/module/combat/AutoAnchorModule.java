package horus.client.module.combat;

import horus.client.event.EventListener;
import horus.client.event.PlayerTickEvent;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.BooleanSetting;
import horus.client.setting.ModeSetting;
import horus.client.setting.NumberSetting;
import horus.client.storage.FriendStore;
import horus.client.ui.hud.NotificationHud;
import horus.client.util.EntityUtils;
import horus.client.util.InventoryUtils;
import horus.client.module.player.ServerHelperModule;

import java.util.Locale;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@ModuleInfo(
   name = "AutoAnchor",
   description = "Автоматически ставит, заряжает светокамнем и взрывает якоря возрождения у противников.",
   category = ModuleCategory.COMBAT
)
public final class AutoAnchorModule extends Module {
   private static AutoAnchorModule instance;

   private final ModeSetting modeSetting = this.addSetting(
      new ModeSetting("Режим", "Скорость выполнения комбо с якорем.", "Быстрый", "Быстрый", "Умный", "Задержка")
   );
   private final NumberSetting delaySetting = this.addSetting(
      new NumberSetting("Задержка шага", "Задержка между действиями (установка/зарядка/взрыв) в миллисекундах.")
         .withRange(0.0F, 300.0F)
         .withStep(10.0F)
         .withSuffix(" мс")
         .setValue(60.0F)
   );
   private final NumberSetting targetRangeSetting = this.addSetting(
      new NumberSetting("Дистанция до цели", "Максимальная дистанция обнаружения врага.")
         .withRange(3.0F, 7.0F)
         .withStep(0.1F)
         .withSuffix(" б.")
         .setValue(5.5F)
   );
   private final NumberSetting placeRangeSetting = this.addSetting(
      new NumberSetting("Радиус взаимодействия", "Максимальная дистанция взаимодействия с блоком якоря.")
         .withRange(3.0F, 5.0F)
         .withStep(0.1F)
         .withSuffix(" б.")
         .setValue(4.5F)
   );
   private final BooleanSetting avoidSelfSetting = this.addSetting(
      new BooleanSetting("Не взрывать себя", "Не детонировать якорь, если персонаж находится слишком близко к эпицентру взрыва.", true)
   );
   private final NumberSetting safeDistanceSetting = this.addSetting(
      new NumberSetting("Безопасная дистанция", "Минимальное расстояние от себя до якоря при детонации.")
         .withRange(2.0F, 4.5F)
         .withStep(0.1F)
         .withSuffix(" б.")
         .setValue(3.0F)
   );
   private final BooleanSetting avoidFriendsSetting = this.addSetting(
      new BooleanSetting("Не взрывать друзей", "Не детонировать якорь, если в радиусе взрыва находится друг.", true)
   );
   private final BooleanSetting rotationsSetting = this.addSetting(
      new BooleanSetting("Наведение", "Направлять прицел на якорь при установке и подрыве.", true)
   );
   private final BooleanSetting silentRotationsSetting = this.addSetting(
      new BooleanSetting("Тихие ротации", "Отправлять углы поворота только в сетевых пакетах без рывка камеры.", true)
   );
   private final BooleanSetting autoDisableSetting = this.addSetting(
      new BooleanSetting("Выключать без ресурсов", "Автоматически отключать модуль, если закончились якоря или светокамень.", false)
   );

   private boolean interacting;
   private long lastActionTime;
   private BlockPos currentAnchorPos;

   private final EventListener<PlayerTickEvent> tickListener = event -> this.onTick();

   private AutoAnchorModule() {
      this.delaySetting.withVisibility(() -> this.modeSetting.isSelected("Задержка"));
      this.safeDistanceSetting.withVisibility(() -> this.avoidSelfSetting.isChecked());
      this.silentRotationsSetting.withVisibility(() -> this.rotationsSetting.isChecked());
   }

   public static AutoAnchorModule getInstance() {
      if (instance == null) {
         instance = new AutoAnchorModule();
      }
      return instance;
   }

   public boolean isInteracting() {
      return this.interacting;
   }

   @Override
   protected void onEnable() {
      this.lastActionTime = 0L;
      this.currentAnchorPos = null;
      this.interacting = false;
   }

   @Override
   protected void onDisable() {
      this.lastActionTime = 0L;
      this.currentAnchorPos = null;
      this.interacting = false;
   }

   private void onTick() {
      if (mc.player == null || mc.world == null || mc.interactionManager == null) {
         return;
      }

      if (mc.currentScreen != null && mc.player.currentScreenHandler != mc.player.playerScreenHandler) {
         return;
      }

      // Anchors only explode outside the Nether (Overworld / End)
      if (mc.world.getRegistryKey() == World.NETHER) {
         return;
      }

      long delay = this.modeSetting.isSelected("Задержка")
         ? Math.round(this.delaySetting.getValue())
         : (this.modeSetting.isSelected("Умный") ? 50L : 0L);

      if (System.currentTimeMillis() - this.lastActionTime < delay) {
         return;
      }

      PlayerEntity target = this.findTarget();
      if (target == null) {
         this.currentAnchorPos = null;
         return;
      }

      // Step 1: Look for an existing Respawn Anchor near the target
      BlockPos existingAnchor = this.findExistingAnchorNear(target);
      if (existingAnchor != null) {
         this.currentAnchorPos = existingAnchor;
         BlockState state = mc.world.getBlockState(existingAnchor);
         int charges = state.get(RespawnAnchorBlock.CHARGES);

         if (charges == 0) {
            // Charge with glowstone
            this.chargeAnchor(existingAnchor);
         } else {
            // Detonate
            this.detonateAnchor(existingAnchor);
         }
         return;
      }

      // Step 2: No anchor found near target - place a new one
      this.placeAnchorNear(target);
   }

   private PlayerEntity findTarget() {
      // First check AttackAura target
      LivingEntity auraTarget = AttackAuraModule.getInstance().getTarget();
      if (auraTarget instanceof PlayerEntity player && this.isValidTarget(player)) {
         return player;
      }

      PlayerEntity nearest = null;
      double nearestDistSq = this.targetRangeSetting.getValue() * this.targetRangeSetting.getValue();

      for (PlayerEntity player : mc.world.getPlayers()) {
         if (!this.isValidTarget(player)) {
            continue;
         }

         double distSq = player.squaredDistanceTo(mc.player);
         if (distSq < nearestDistSq) {
            nearestDistSq = distSq;
            nearest = player;
         }
      }

      return nearest;
   }

   private boolean isValidTarget(PlayerEntity player) {
      if (player == null || player == mc.player || !player.isAlive() || player.isRemoved() || player.isSpectator()) {
         return false;
      }
      if (player.squaredDistanceTo(mc.player) > this.targetRangeSetting.getValue() * this.targetRangeSetting.getValue()) {
         return false;
      }
      return !this.isFriend(player);
   }

   private boolean isFriend(PlayerEntity player) {
      String name = player.getGameProfile().name().toLowerCase(Locale.ROOT);
      for (String friend : FriendStore.getFriendList()) {
         if (friend.equalsIgnoreCase(name)) {
            return true;
         }
      }
      return false;
   }

   private BlockPos findExistingAnchorNear(PlayerEntity target) {
      double maxReachSq = this.placeRangeSetting.getValue() * this.placeRangeSetting.getValue();
      Vec3d eyePos = mc.player.getEyePos();
      BlockPos bestPos = null;
      double bestDistSq = Double.MAX_VALUE;

      BlockPos targetPos = target.getBlockPos();
      for (BlockPos pos : BlockPos.iterate(targetPos.add(-2, -1, -2), targetPos.add(2, 2, 2))) {
         if (!mc.world.getBlockState(pos).isOf(Blocks.RESPAWN_ANCHOR)) {
            continue;
         }

         Vec3d center = Vec3d.ofCenter(pos);
         double distToPlayerSq = eyePos.squaredDistanceTo(center);
         if (distToPlayerSq > maxReachSq) {
            continue;
         }

         double distToTargetSq = pos.getSquaredDistance(target.getX(), target.getY(), target.getZ());
         if (distToTargetSq < bestDistSq) {
            bestDistSq = distToTargetSq;
            bestPos = pos.toImmutable();
         }
      }

      return bestPos;
   }

   private void chargeAnchor(BlockPos anchorPos) {
      // Check if player has glowstone
      boolean isOffhand = mc.player.getOffHandStack().isOf(Items.GLOWSTONE);
      int glowstoneSlot = isOffhand ? -1 : InventoryUtils.findInventorySlot(Items.GLOWSTONE);

      if (!isOffhand && glowstoneSlot == -1) {
         if (this.autoDisableSetting.isChecked()) {
            this.notifyEmpty("Закончился светокамень");
            this.setEnabled(false);
         }
         return;
      }

      Vec3d hitPos = Vec3d.ofCenter(anchorPos);
      BlockHitResult hitResult = new BlockHitResult(hitPos, Direction.UP, anchorPos, false);

      this.applyRotation(hitPos);

      if (isOffhand) {
         this.interacting = true;
         try {
            mc.interactionManager.interactBlock(mc.player, Hand.OFF_HAND, hitResult);
            mc.player.swingHand(Hand.OFF_HAND);
            this.lastActionTime = System.currentTimeMillis();
         } finally {
            this.interacting = false;
         }
      } else {
         int originalSlot = InventoryUtils.getSelectedSlot();
         boolean swappedFromInventory = glowstoneSlot >= 9;

         if (swappedFromInventory) {
            InventoryUtils.swapSlotWithHotbar(glowstoneSlot, originalSlot);
         } else {
            InventoryUtils.setSelectedSlot(glowstoneSlot, ServerHelperModule.getInstance().modeSetting.matchesOption("SpookyTime"));
         }

         this.interacting = true;
         try {
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
            mc.player.swingHand(Hand.MAIN_HAND);
            this.lastActionTime = System.currentTimeMillis();
         } finally {
            this.interacting = false;
            if (swappedFromInventory) {
               InventoryUtils.swapSlotWithHotbar(glowstoneSlot, originalSlot);
            } else {
               InventoryUtils.setSelectedSlot(originalSlot, ServerHelperModule.getInstance().modeSetting.matchesOption("SpookyTime"));
            }
         }
      }

      // In Fast mode, immediately detonate after charging
      if (this.modeSetting.isSelected("Быстрый")) {
         this.detonateAnchor(anchorPos);
      }
   }

   private void detonateAnchor(BlockPos anchorPos) {
      Vec3d anchorCenter = Vec3d.ofCenter(anchorPos);

      // Self-damage check
      if (this.avoidSelfSetting.isChecked()) {
         double selfDist = mc.player.getEyePos().distanceTo(anchorCenter);
         if (selfDist < this.safeDistanceSetting.getValue()) {
            return;
         }
      }

      // Friend check
      if (this.avoidFriendsSetting.isChecked() && this.isFriendNear(anchorPos)) {
         return;
      }

      // If holding glowstone in main hand, swap away so it doesn't just re-charge
      int originalSlot = InventoryUtils.getSelectedSlot();
      boolean holdingGlowstone = mc.player.getMainHandStack().isOf(Items.GLOWSTONE);
      int safeSlot = -1;

      if (holdingGlowstone) {
         for (int i = 0; i < 9; i++) {
            if (!mc.player.getInventory().getStack(i).isOf(Items.GLOWSTONE)) {
               safeSlot = i;
               break;
            }
         }
         if (safeSlot != -1) {
            InventoryUtils.setSelectedSlot(safeSlot, ServerHelperModule.getInstance().modeSetting.matchesOption("SpookyTime"));
         }
      }

      Vec3d hitPos = anchorCenter.add(0.0, 0.5, 0.0);
      BlockHitResult hitResult = new BlockHitResult(hitPos, Direction.UP, anchorPos, false);

      this.applyRotation(hitPos);

      this.interacting = true;
      try {
         mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
         mc.player.swingHand(Hand.MAIN_HAND);
         this.lastActionTime = System.currentTimeMillis();
      } finally {
         this.interacting = false;
         if (holdingGlowstone && safeSlot != -1) {
            InventoryUtils.setSelectedSlot(originalSlot, ServerHelperModule.getInstance().modeSetting.matchesOption("SpookyTime"));
         }
      }
   }

   private void placeAnchorNear(PlayerEntity target) {
      int anchorSlot = InventoryUtils.findInventorySlot(Items.RESPAWN_ANCHOR);
      int glowstoneSlot = InventoryUtils.findInventorySlot(Items.GLOWSTONE);
      boolean hasGlowstoneOffhand = mc.player.getOffHandStack().isOf(Items.GLOWSTONE);

      if (anchorSlot == -1 || (!hasGlowstoneOffhand && glowstoneSlot == -1)) {
         if (this.autoDisableSetting.isChecked()) {
            this.notifyEmpty("Закончились якоря или светокамень");
            this.setEnabled(false);
         }
         return;
      }

      PlacementCandidate best = this.findBestPlacement(target);
      if (best == null) {
         return;
      }

      int originalSlot = InventoryUtils.getSelectedSlot();
      boolean swappedFromInventory = anchorSlot >= 9;

      if (swappedFromInventory) {
         InventoryUtils.swapSlotWithHotbar(anchorSlot, originalSlot);
      } else {
         InventoryUtils.setSelectedSlot(anchorSlot, ServerHelperModule.getInstance().modeSetting.matchesOption("SpookyTime"));
      }

      this.applyRotation(best.hitVec);

      this.interacting = true;
      try {
         mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, best.hitResult);
         mc.player.swingHand(Hand.MAIN_HAND);
         this.lastActionTime = System.currentTimeMillis();
         this.currentAnchorPos = best.anchorPos;
      } finally {
         this.interacting = false;
         if (swappedFromInventory) {
            InventoryUtils.swapSlotWithHotbar(anchorSlot, originalSlot);
         } else {
            InventoryUtils.setSelectedSlot(originalSlot, ServerHelperModule.getInstance().modeSetting.matchesOption("SpookyTime"));
         }
      }

      // In Fast mode, charge immediately
      if (this.modeSetting.isSelected("Быстрый")) {
         this.chargeAnchor(best.anchorPos);
      }
   }

   private PlacementCandidate findBestPlacement(PlayerEntity target) {
      Vec3d eyePos = mc.player.getEyePos();
      double maxReachSq = this.placeRangeSetting.getValue() * this.placeRangeSetting.getValue();
      double safeDistSq = this.safeDistanceSetting.getValue() * this.safeDistanceSetting.getValue();
      BlockPos targetPos = target.getBlockPos();

      PlacementCandidate best = null;
      double bestTargetDistSq = Double.MAX_VALUE;

      for (int dx = -2; dx <= 2; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -2; dz <= 2; dz++) {
               BlockPos candidate = targetPos.add(dx, dy, dz);

               // Must be replaceable / air
               if (!mc.world.getBlockState(candidate).isReplaceable()) {
                  continue;
               }

               Vec3d candidateCenter = Vec3d.ofCenter(candidate);
               double distToPlayerSq = eyePos.squaredDistanceTo(candidateCenter);
               if (distToPlayerSq > maxReachSq) {
                  continue;
               }

               if (this.avoidSelfSetting.isChecked() && distToPlayerSq < safeDistSq) {
                  continue;
               }

               // Collision check: no living entities inside the anchor block
               Box box = new Box(candidate);
               if (!mc.world.getOtherEntities(null, box, e -> e.isAlive() && !(e instanceof EndCrystalEntity)).isEmpty()) {
                  continue;
               }

               // Find a neighboring solid block face to click against
               for (Direction dir : Direction.values()) {
                  BlockPos neighbor = candidate.offset(dir);
                  BlockState neighborState = mc.world.getBlockState(neighbor);
                  if (neighborState.isAir() || neighborState.isReplaceable()) {
                     continue;
                  }

                  Direction sideToClick = dir.getOpposite();
                  Vec3d hitVec = Vec3d.ofCenter(neighbor).add(
                     sideToClick.getOffsetX() * 0.5,
                     sideToClick.getOffsetY() * 0.5,
                     sideToClick.getOffsetZ() * 0.5
                  );

                  double clickDistSq = eyePos.squaredDistanceTo(hitVec);
                  if (clickDistSq > maxReachSq) {
                     continue;
                  }

                  double distToTargetSq = candidate.getSquaredDistance(target.getX(), target.getY(), target.getZ());
                  if (distToTargetSq < bestTargetDistSq) {
                     bestTargetDistSq = distToTargetSq;
                     BlockHitResult hitResult = new BlockHitResult(hitVec, sideToClick, neighbor, false);
                     best = new PlacementCandidate(candidate, hitVec, hitResult);
                  }
                  break;
               }
            }
         }
      }

      return best;
   }

   private boolean isFriendNear(BlockPos anchorPos) {
      Vec3d center = Vec3d.ofCenter(anchorPos);
      for (PlayerEntity player : mc.world.getPlayers()) {
         if (player == mc.player || !this.isFriend(player) || !player.isAlive()) {
            continue;
         }
         if (player.getEyePos().squaredDistanceTo(center) <= 5.0 * 5.0) {
            return true;
         }
      }
      return false;
   }

   private void applyRotation(Vec3d targetVec) {
      if (!this.rotationsSetting.isChecked() || mc.player == null) {
         return;
      }

      float[] rots = EntityUtils.calculateRotationsToVec(targetVec, mc.player.getEyePos());
      if (this.silentRotationsSetting.isChecked()) {
         if (mc.player.networkHandler != null) {
            mc.player.networkHandler.sendPacket(
               new PlayerMoveC2SPacket.LookAndOnGround(rots[0], rots[1], mc.player.isOnGround(), mc.player.horizontalCollision)
            );
         }
      } else {
         mc.player.setYaw(rots[0]);
         mc.player.setPitch(rots[1]);
      }
   }

   private void notifyEmpty(String message) {
      NotificationHud.notifyWithText(
         this.category.getIcon(),
         this.name,
         Text.literal(message).setStyle(Style.EMPTY.withColor(Formatting.RED)),
         true
      );
   }

   private static record PlacementCandidate(BlockPos anchorPos, Vec3d hitVec, BlockHitResult hitResult) {
   }
}

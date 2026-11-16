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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(
   name = "AutoExplosion",
   description = "Автоматически ставит, взрывает кристаллы Края и при необходимости устанавливает обсидиан.",
   category = ModuleCategory.COMBAT
)
public final class AutoExplosionModule extends Module {
   private static AutoExplosionModule autoExplosionModule;

   private final ModeSetting modeSetting = this.addSetting(
      new ModeSetting("Режим", "Скорость и поведение работы взрывов.", "Быстрый", "Быстрый", "Умный", "Задержка")
   );
   private final NumberSetting delaySetting = this.addSetting(
      new NumberSetting("Задержка", "Задержка между действиями (мс).")
         .withRange(0.0F, 300.0F)
         .withStep(10.0F)
         .withSuffix(" мс")
         .setValue(50.0F)
   );
   private final NumberSetting targetRangeSetting = this.addSetting(
      new NumberSetting("Дистанция до цели", "Максимальная дистанция обнаружения противника.")
         .withRange(3.0F, 10.0F)
         .withStep(0.2F)
         .withSuffix(" б.")
         .setValue(6.0F)
   );
   private final NumberSetting placeRangeSetting = this.addSetting(
      new NumberSetting("Радиус установки", "Максимальная дистанция установки кристалла.")
         .withRange(3.0F, 6.0F)
         .withStep(0.1F)
         .withSuffix(" б.")
         .setValue(4.5F)
   );
   private final NumberSetting breakRangeSetting = this.addSetting(
      new NumberSetting("Радиус подрыва", "Максимальная дистанция удара по кристаллу.")
         .withRange(3.0F, 6.0F)
         .withStep(0.1F)
         .withSuffix(" б.")
         .setValue(4.5F)
   );
   private final BooleanSetting avoidSelfSetting = this.addSetting(
      new BooleanSetting("Не взрывать себя", "Не атаковать кристалл, если персонаж находится слишком близко.", true)
   );
   private final NumberSetting safeDistanceSetting = this.addSetting(
      new NumberSetting("Безопасная дистанция", "Минимальное расстояние от себя до кристалла при детонации.")
         .withRange(1.5F, 4.5F)
         .withStep(0.1F)
         .withSuffix(" б.")
         .setValue(2.8F)
   );
   private final BooleanSetting avoidFriendsSetting = this.addSetting(
      new BooleanSetting("Не взрывать друзей", "Не детонировать кристаллы, если в радиусе взрыва находится друг.", true)
   );
   private final BooleanSetting avoidOresSetting = this.addSetting(
      new BooleanSetting("Не взрывать ресурсы", "Не взрывать кристаллы рядом с ценными рудами.", false)
   );
   private final BooleanSetting autoObsidianSetting = this.addSetting(
      new BooleanSetting("Авто-обсидиан", "Автоматически ставить обсидиан под противника, если нет базы.", true)
   );
   private final BooleanSetting rotationsSetting = this.addSetting(
      new BooleanSetting("Наведение", "Направлять прицел на кристалл и блок установки.", true)
   );
   private final BooleanSetting silentRotationsSetting = this.addSetting(
      new BooleanSetting("Тихие ротации", "Отправлять углы поворота только в сетевых пакетах без рывка камеры.", true)
   );
   private final BooleanSetting autoDisableSetting = this.addSetting(
      new BooleanSetting("Выключать без ресурсов", "Автоматически отключать модуль, если закончились кристаллы.", false)
   );

   private long lastActionAt;
   private final EventListener<PlayerTickEvent> tickListener = event -> this.tickAutoExplosion();

   private AutoExplosionModule() {
      this.delaySetting.withVisibility(() -> this.modeSetting.isSelected("Задержка"));
      this.safeDistanceSetting.withVisibility(() -> this.avoidSelfSetting.isChecked());
      this.silentRotationsSetting.withVisibility(() -> this.rotationsSetting.isChecked());
   }

   public static AutoExplosionModule getInstance() {
      if (autoExplosionModule == null) {
         autoExplosionModule = new AutoExplosionModule();
      }
      return autoExplosionModule;
   }

   @Override
   protected void onEnable() {
      this.lastActionAt = 0L;
   }

   @Override
   protected void onDisable() {
      this.lastActionAt = 0L;
   }

   private void tickAutoExplosion() {
      if (mc.player == null
         || mc.world == null
         || mc.interactionManager == null
         || mc.currentScreen != null
         || mc.player.currentScreenHandler != mc.player.playerScreenHandler) {
         return;
      }

      long delay = this.modeSetting.isSelected("Задержка")
         ? Math.round(this.delaySetting.getValue())
         : (this.modeSetting.isSelected("Умный") ? 50L : 0L);

      if (System.currentTimeMillis() - this.lastActionAt < delay) {
         return;
      }

      PlayerEntity target = this.findTarget();
      if (target == null) {
         return;
      }

      // Step 1: Check if an End Crystal already exists near target ready to detonate
      EndCrystalEntity crystal = this.findCrystalNear(target);
      if (crystal != null) {
         if (this.canDetonate(crystal)) {
            this.applyRotation(crystal.getEntityPos());
            mc.interactionManager.attackEntity(mc.player, crystal);
            mc.player.swingHand(Hand.MAIN_HAND);
            this.lastActionAt = System.currentTimeMillis();

            // In Fast mode, attempt to place next crystal immediately in the same tick
            if (!this.modeSetting.isSelected("Быстрый")) {
               return;
            }
         } else {
            return;
         }
      }

      // Step 2: Place crystal on best available obsidian/bedrock near target
      boolean placed = this.placeCrystalNear(target);
      if (!placed && this.autoObsidianSetting.isChecked()) {
         // Step 3: If no valid base found and AutoObsidian enabled, place obsidian
         this.placeObsidianNear(target);
      }
   }

   private PlayerEntity findTarget() {
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
      if (player == null
         || player == mc.player
         || !player.isAlive()
         || player.isRemoved()
         || player.isSpectator()
         || player.isCreative()) {
         return false;
      }

      if (this.avoidFriendsSetting.isChecked() && this.isFriend(player)) {
         return false;
      }

      double maxDistSq = this.targetRangeSetting.getValue() * this.targetRangeSetting.getValue();
      return player.squaredDistanceTo(mc.player) <= maxDistSq;
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

   private boolean isFriendNear(BlockPos pos) {
      Vec3d center = Vec3d.ofCenter(pos);
      for (PlayerEntity player : mc.world.getPlayers()) {
         if (player == mc.player || !player.isAlive() || !this.isFriend(player)) {
            continue;
         }
         if (player.getEyePos().squaredDistanceTo(center) <= 5.0 * 5.0) {
            return true;
         }
      }
      return false;
   }

   private EndCrystalEntity findCrystalNear(PlayerEntity target) {
      EndCrystalEntity bestCrystal = null;
      double bestDistToTarget = Double.MAX_VALUE;
      double breakReachSq = this.breakRangeSetting.getValue() * this.breakRangeSetting.getValue();
      Vec3d eyePos = mc.player.getEyePos();

      for (Entity entity : mc.world.getEntities()) {
         if (entity instanceof EndCrystalEntity crystal && crystal.isAlive()) {
            double distToEyeSq = eyePos.squaredDistanceTo(crystal.getEntityPos());
            if (distToEyeSq > breakReachSq) {
               continue;
            }

            double distToTargetSq = target.squaredDistanceTo(crystal.getEntityPos());
            if (distToTargetSq > 6.0 * 6.0) {
               continue;
            }

            if (distToTargetSq < bestDistToTarget) {
               bestDistToTarget = distToTargetSq;
               bestCrystal = crystal;
            }
         }
      }
      return bestCrystal;
   }

   private boolean canDetonate(EndCrystalEntity crystal) {
      Vec3d eyePos = mc.player.getEyePos();
      double distToSelfSq = eyePos.squaredDistanceTo(crystal.getEntityPos());
      double safeDistSq = this.safeDistanceSetting.getValue() * this.safeDistanceSetting.getValue();

      if (this.avoidSelfSetting.isChecked() && distToSelfSq < safeDistSq) {
         return false;
      }

      if (this.avoidFriendsSetting.isChecked() && this.isFriendNear(crystal.getBlockPos())) {
         return false;
      }

      if (this.avoidOresSetting.isChecked() && this.hasOreNear(crystal.getBlockPos())) {
         return false;
      }

      return true;
   }

   private boolean placeCrystalNear(PlayerEntity target) {
      boolean hasOffhand = mc.player.getOffHandStack().isOf(Items.END_CRYSTAL);
      int crystalSlot = hasOffhand ? -1 : InventoryUtils.findInventorySlot(Items.END_CRYSTAL);

      if (!hasOffhand && crystalSlot == -1) {
         if (this.autoDisableSetting.isChecked()) {
            this.notifyEmpty("Закончились кристаллы края");
            this.setEnabled(false);
         }
         return false;
      }

      PlacementCandidate best = this.findBestPlacement(target);
      if (best == null) {
         return false;
      }

      Hand hand = hasOffhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
      int originalSlot = InventoryUtils.getSelectedSlot();
      boolean swapped = !hasOffhand && crystalSlot >= 9;

      if (!hasOffhand) {
         if (swapped) {
            InventoryUtils.swapSlotWithHotbar(crystalSlot, originalSlot);
         } else {
            InventoryUtils.setSelectedSlot(crystalSlot, ServerHelperModule.getInstance().modeSetting.matchesOption("SpookyTime"));
         }
      }

      this.applyRotation(best.hitVec);

      try {
         mc.interactionManager.interactBlock(mc.player, hand, best.hitResult);
         mc.player.swingHand(hand);
         this.lastActionAt = System.currentTimeMillis();
         return true;
      } finally {
         if (!hasOffhand) {
            if (swapped) {
               InventoryUtils.swapSlotWithHotbar(crystalSlot, originalSlot);
            } else {
               InventoryUtils.setSelectedSlot(originalSlot, ServerHelperModule.getInstance().modeSetting.matchesOption("SpookyTime"));
            }
         }
      }
   }

   private PlacementCandidate findBestPlacement(PlayerEntity target) {
      Vec3d eyePos = mc.player.getEyePos();
      double maxReachSq = this.placeRangeSetting.getValue() * this.placeRangeSetting.getValue();
      double safeDistSq = this.safeDistanceSetting.getValue() * this.safeDistanceSetting.getValue();
      BlockPos targetPos = target.getBlockPos();

      BlockPos bestBase = null;
      double bestTargetDist = Double.MAX_VALUE;

      for (int dx = -3; dx <= 3; dx++) {
         for (int dy = -2; dy <= 2; dy++) {
            for (int dz = -3; dz <= 3; dz++) {
               BlockPos base = targetPos.add(dx, dy, dz);
               BlockState baseState = mc.world.getBlockState(base);
               if (!baseState.isOf(Blocks.OBSIDIAN) && !baseState.isOf(Blocks.BEDROCK)) {
                  continue;
               }

               Vec3d hitPos = Vec3d.ofCenter(base).add(0.0, 0.5, 0.0);
               double distToEyeSq = eyePos.squaredDistanceTo(hitPos);
               if (distToEyeSq > maxReachSq) {
                  continue;
               }

               Vec3d crystalPos = Vec3d.ofCenter(base.up());
               if (this.avoidSelfSetting.isChecked() && eyePos.squaredDistanceTo(crystalPos) < safeDistSq) {
                  continue;
               }

               if (this.avoidFriendsSetting.isChecked() && this.isFriendNear(base.up())) {
                  continue;
               }

               if (this.avoidOresSetting.isChecked() && this.hasOreNear(base.up())) {
                  continue;
               }

               if (!this.hasCrystalSpace(base)) {
                  continue;
               }

               double targetDistSq = target.squaredDistanceTo(crystalPos);
               if (targetDistSq < bestTargetDist) {
                  bestTargetDist = targetDistSq;
                  bestBase = base;
               }
            }
         }
      }

      if (bestBase == null) {
         return null;
      }

      Vec3d hitVec = Vec3d.ofCenter(bestBase).add(0.0, 0.5, 0.0);
      BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, bestBase, false);
      return new PlacementCandidate(bestBase, hitVec, hitResult);
   }

   private void placeObsidianNear(PlayerEntity target) {
      int obsidianSlot = InventoryUtils.findInventorySlot(Items.OBSIDIAN);
      if (obsidianSlot == -1) {
         return;
      }

      Vec3d eyePos = mc.player.getEyePos();
      double maxReachSq = this.placeRangeSetting.getValue() * this.placeRangeSetting.getValue();
      double safeDistSq = this.safeDistanceSetting.getValue() * this.safeDistanceSetting.getValue();
      BlockPos targetPos = target.getBlockPos();

      BlockPos bestPos = null;
      Direction bestSide = null;
      BlockPos bestNeighbor = null;
      double bestTargetDist = Double.MAX_VALUE;

      for (int dx = -2; dx <= 2; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -2; dz <= 2; dz++) {
               BlockPos pos = targetPos.add(dx, dy, dz);
               BlockState state = mc.world.getBlockState(pos);
               if (!state.isAir() && !state.isReplaceable()) {
                  continue;
               }

               // Must have air above for crystal
               BlockPos above = pos.up();
               BlockState aboveState = mc.world.getBlockState(above);
               if (!aboveState.isAir() && !aboveState.isReplaceable()) {
                  continue;
               }

               Vec3d center = Vec3d.ofCenter(pos);
               double distToEyeSq = eyePos.squaredDistanceTo(center);
               if (distToEyeSq > maxReachSq) {
                  continue;
               }
               if (this.avoidSelfSetting.isChecked() && distToEyeSq < safeDistSq) {
                  continue;
               }

               for (Direction dir : Direction.values()) {
                  BlockPos neighbor = pos.offset(dir);
                  BlockState nState = mc.world.getBlockState(neighbor);
                  if (nState.isAir() || nState.isReplaceable()) {
                     continue;
                  }

                  double tDist = pos.getSquaredDistance(target.getX(), target.getY(), target.getZ());
                  if (tDist < bestTargetDist) {
                     bestTargetDist = tDist;
                     bestPos = pos;
                     bestNeighbor = neighbor;
                     bestSide = dir.getOpposite();
                  }
                  break;
               }
            }
         }
      }

      if (bestPos != null && bestNeighbor != null && bestSide != null) {
         int originalSlot = InventoryUtils.getSelectedSlot();
         boolean swapped = obsidianSlot >= 9;
         if (swapped) {
            InventoryUtils.swapSlotWithHotbar(obsidianSlot, originalSlot);
         } else {
            InventoryUtils.setSelectedSlot(obsidianSlot, ServerHelperModule.getInstance().modeSetting.matchesOption("SpookyTime"));
         }

         Vec3d hitVec = Vec3d.ofCenter(bestNeighbor).add(
            bestSide.getOffsetX() * 0.5,
            bestSide.getOffsetY() * 0.5,
            bestSide.getOffsetZ() * 0.5
         );
         this.applyRotation(hitVec);

         try {
            BlockHitResult hitResult = new BlockHitResult(hitVec, bestSide, bestNeighbor, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
            mc.player.swingHand(Hand.MAIN_HAND);
            this.lastActionAt = System.currentTimeMillis();
         } finally {
            if (swapped) {
               InventoryUtils.swapSlotWithHotbar(obsidianSlot, originalSlot);
            } else {
               InventoryUtils.setSelectedSlot(originalSlot, ServerHelperModule.getInstance().modeSetting.matchesOption("SpookyTime"));
            }
         }
      }
   }

   private boolean hasCrystalSpace(BlockPos base) {
      BlockPos firstAir = base.up();
      BlockState state = mc.world.getBlockState(firstAir);
      if (!state.isAir() && !state.isReplaceable()) {
         return false;
      }

      Box placementBox = new Box(firstAir);
      return mc.world.getOtherEntities(null, placementBox, Entity::isAlive).isEmpty();
   }

   private boolean hasOreNear(BlockPos center) {
      for (BlockPos pos : BlockPos.iterate(center.add(-5, -5, -5), center.add(5, 5, 5))) {
         if (center.getSquaredDistance(pos) > 25.0) {
            continue;
         }

         String blockPath = Registries.BLOCK.getId(mc.world.getBlockState(pos).getBlock()).getPath();
         if (blockPath.endsWith("_ore") || blockPath.equals("ancient_debris")) {
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

   private static record PlacementCandidate(BlockPos basePos, Vec3d hitVec, BlockHitResult hitResult) {
   }
}

package horus.client.module.combat;

import horus.client.event.EventListener;
import horus.client.event.PlayerTickEvent;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.module.player.ServerHelperModule;
import horus.client.setting.BooleanSetting;
import horus.client.setting.ModeSetting;
import horus.client.setting.NumberSetting;
import horus.client.storage.FriendStore;
import horus.client.ui.hud.NotificationHud;
import horus.client.util.EntityUtils;
import horus.client.util.InventoryUtils;

import java.util.Locale;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

@ModuleInfo(
   name = "AutoCart",
   description = "Автоматически ставит рельсы, вагонетку с динамитом и подрывает её выстрелом из лука с защитой от самоподрыва.",
   category = ModuleCategory.COMBAT
)
public final class AutoCartModule extends Module {
   private static AutoCartModule instance;

   private static final Item[] RAILS = new Item[]{
      Items.RAIL,
      Items.POWERED_RAIL,
      Items.DETECTOR_RAIL,
      Items.ACTIVATOR_RAIL
   };

   private final ModeSetting modeSetting = this.addSetting(
      new ModeSetting("Режим", "Скорость выполнения комбо с вагонеткой.", "Быстрый", "Быстрый", "Умный", "Задержка")
   );
   private final NumberSetting delaySetting = this.addSetting(
      new NumberSetting("Задержка", "Задержка между действиями в миллисекундах.")
         .withRange(0.0F, 300.0F)
         .withStep(10.0F)
         .withSuffix(" мс")
         .setValue(40.0F)
   );
   private final NumberSetting targetRangeSetting = this.addSetting(
      new NumberSetting("Дистанция до цели", "Максимальная дистанция обнаружения противника.")
         .withRange(3.0F, 10.0F)
         .withStep(0.2F)
         .withSuffix(" б.")
         .setValue(6.0F)
   );
   private final NumberSetting placeRangeSetting = this.addSetting(
      new NumberSetting("Радиус установки", "Максимальная дистанция установки рельсов и вагонетки.")
         .withRange(3.0F, 5.0F)
         .withStep(0.1F)
         .withSuffix(" б.")
         .setValue(4.5F)
   );
   private final BooleanSetting avoidSelfSetting = this.addSetting(
      new BooleanSetting("Не взрывать себя", "Блокировать детонацию и установку, если взрыв нанесёт урон себе.", true)
   );
   private final NumberSetting safeDistanceSetting = this.addSetting(
      new NumberSetting("Безопасная дистанция", "Минимальное расстояние от себя до вагонетки при взрыве.")
         .withRange(2.5F, 5.5F)
         .withStep(0.1F)
         .withSuffix(" б.")
         .setValue(3.8F)
   );
   private final BooleanSetting wallCheckSetting = this.addSetting(
      new BooleanSetting("Учет стен", "Считать позицию безопаснее, если взрывная волна блокируется стеной.", true)
   );
   private final BooleanSetting avoidFriendsSetting = this.addSetting(
      new BooleanSetting("Не взрывать друзей", "Не взрывать вагонетку, если рядом находится друг.", true)
   );
   private final BooleanSetting onlyWithTotemSetting = this.addSetting(
      new BooleanSetting("Только с тотемом", "Атаковать только при наличии тотема в руках.", false)
   );
   private final NumberSetting minHealthSetting = this.addSetting(
      new NumberSetting("Мин. здоровье", "Минимальное здоровье для совершения подрыва.")
         .withRange(0.0F, 20.0F)
         .withStep(1.0F)
         .withSuffix(" HP")
         .setValue(0.0F)
   );
   private final NumberSetting bowPullTicksSetting = this.addSetting(
      new NumberSetting("Натяжение лука", "Количество тиков натяжения тетивы лука перед выстрелом.")
         .withRange(3.0F, 10.0F)
         .withStep(1.0F)
         .withSuffix(" т.")
         .setValue(3.0F)
   );
   private final BooleanSetting predictTargetSetting = this.addSetting(
      new BooleanSetting("Упреждение цели", "Рассчитывать положение рельсов по траектории движения врага.", true)
   );
   private final BooleanSetting rotationsSetting = this.addSetting(
      new BooleanSetting("Наведение", "Направлять прицел на рельсы, вагонетку и удерживать цель во время выстрела.", true)
   );
   private final BooleanSetting silentRotationsSetting = this.addSetting(
      new BooleanSetting("Тихие ротации", "Отправлять ротацию в сетевых пакетах без резкого движения камеры.", true)
   );
   private final BooleanSetting autoDisableSetting = this.addSetting(
      new BooleanSetting("Выключать без ресурсов", "Автоматически отключать модуль, если закончились предметы.", false)
   );

   private long lastActionAt;

   private boolean isDrawingBow;
   private int bowOriginalSlot = -1;
   private int bowSwappedSlot = -1;
   private Hand bowHand = Hand.MAIN_HAND;
   private Vec3d currentAimVec;
   private Entity currentAimEntity;

   private final EventListener<PlayerTickEvent> tickListener = event -> this.onTick();

   private AutoCartModule() {
      this.delaySetting.withVisibility(() -> this.modeSetting.isSelected("Задержка"));
      this.safeDistanceSetting.withVisibility(() -> this.avoidSelfSetting.isChecked());
      this.wallCheckSetting.withVisibility(() -> this.avoidSelfSetting.isChecked());
      this.silentRotationsSetting.withVisibility(() -> this.rotationsSetting.isChecked());
   }

   public static AutoCartModule getInstance() {
      if (instance == null) {
         instance = new AutoCartModule();
      }
      return instance;
   }

   @Override
   protected void onEnable() {
      this.lastActionAt = 0L;
      this.resetBow();
   }

   @Override
   protected void onDisable() {
      this.cancelBow();
      this.lastActionAt = 0L;
   }

   private void onTick() {
      if (mc.player == null
         || mc.world == null
         || mc.interactionManager == null
         || mc.currentScreen != null
         || mc.player.currentScreenHandler != mc.player.playerScreenHandler) {
         this.cancelBow();
         return;
      }

      if (this.isDrawingBow) {
         this.tickBow();
         return;
      }

      long delay = this.modeSetting.isSelected("Задержка")
         ? Math.round(this.delaySetting.getValue())
         : (this.modeSetting.isSelected("Умный") ? 50L : 0L);

      if (System.currentTimeMillis() - this.lastActionAt < delay) {
         return;
      }

      LivingEntity target = this.findTarget();
      if (target == null) {
         return;
      }

      TntMinecartEntity existingCart = this.findTntMinecartNear(target);
      if (existingCart != null) {
         if (this.startBowDraw(existingCart.getBoundingBox().getCenter(), existingCart)) {
            this.lastActionAt = System.currentTimeMillis();
         }
         return;
      }

      BlockPos railPos = this.findBestRailPos(target);
      if (railPos == null) {
         return;
      }

      boolean railAlreadyThere = AbstractRailBlock.isRail(mc.world.getBlockState(railPos));

      if (!railAlreadyThere) {
         if (!this.placeRail(railPos)) {
            return;
         }

         if (!this.modeSetting.isSelected("Быстрый")) {
            return;
         }
      }

      if (!this.placeTntMinecart(railPos)) {
         return;
      }

      Vec3d cartAimVec = Vec3d.ofCenter(railPos).add(0.0, 0.35, 0.0);
      this.startBowDraw(cartAimVec, null);
   }

   private boolean startBowDraw(Vec3d aimVec, Entity targetEntity) {
      if (mc.player == null || mc.interactionManager == null) {
         return false;
      }

      boolean offhandBow = mc.player.getOffHandStack().isOf(Items.BOW);
      int bowSlot = offhandBow ? -1 : InventoryUtils.findInventorySlot(Items.BOW);

      if (!offhandBow && bowSlot == -1) {
         if (this.autoDisableSetting.isChecked()) {
            this.notifyEmpty("Закончился лук");
            this.setEnabled(false);
         }
         return false;
      }

      if (!this.hasArrow()) {
         if (this.autoDisableSetting.isChecked()) {
            this.notifyEmpty("Закончились стрелы");
            this.setEnabled(false);
         }
         return false;
      }

      this.bowOriginalSlot = InventoryUtils.getSelectedSlot();
      this.bowHand = offhandBow ? Hand.OFF_HAND : Hand.MAIN_HAND;

      if (!offhandBow) {
         if (bowSlot >= 9) {
            InventoryUtils.swapSlotWithHotbar(bowSlot, this.bowOriginalSlot);
            this.bowSwappedSlot = bowSlot;
         } else {
            InventoryUtils.setSelectedSlot(bowSlot, ServerHelperModule.getInstance().modeSetting.matchesOption("SpookyTime"));
            this.bowSwappedSlot = -1;
         }
      }

      this.applyRotation(aimVec);
      mc.interactionManager.interactItem(mc.player, this.bowHand);

      this.isDrawingBow = true;
      this.currentAimVec = aimVec;
      this.currentAimEntity = targetEntity;
      return true;
   }

   private void tickBow() {
      if (mc.player == null || mc.interactionManager == null || !mc.player.isAlive()) {
         this.cancelBow();
         return;
      }

      if (!mc.player.isUsingItem()) {
         this.cancelBow();
         return;
      }

      Vec3d aim = (this.currentAimEntity != null && this.currentAimEntity.isAlive())
         ? this.currentAimEntity.getBoundingBox().getCenter()
         : this.currentAimVec;

      if (aim != null) {
         this.applyRotation(aim);
      }

      int requiredTicks = Math.round(this.bowPullTicksSetting.getValue());
      if (mc.player.getItemUseTime() >= requiredTicks) {
         if (aim != null) {
            this.applyRotation(aim);
         }

         mc.interactionManager.stopUsingItem(mc.player);
         this.finishBow();
         this.lastActionAt = System.currentTimeMillis();
      }
   }

   private void finishBow() {
      if (this.bowHand == Hand.MAIN_HAND && mc.player != null) {
         if (this.bowSwappedSlot != -1 && this.bowOriginalSlot != -1) {
            InventoryUtils.swapSlotWithHotbar(this.bowSwappedSlot, this.bowOriginalSlot);
         } else if (this.bowOriginalSlot != -1) {
            InventoryUtils.setSelectedSlot(this.bowOriginalSlot, ServerHelperModule.getInstance().modeSetting.matchesOption("SpookyTime"));
         }
      }
      this.resetBow();
   }

   private void cancelBow() {
      if (this.isDrawingBow && mc.player != null && mc.interactionManager != null) {
         if (mc.player.isUsingItem()) {
            mc.interactionManager.stopUsingItem(mc.player);
         }
         if (this.bowHand == Hand.MAIN_HAND) {
            if (this.bowSwappedSlot != -1 && this.bowOriginalSlot != -1) {
               InventoryUtils.swapSlotWithHotbar(this.bowSwappedSlot, this.bowOriginalSlot);
            } else if (this.bowOriginalSlot != -1) {
               InventoryUtils.setSelectedSlot(this.bowOriginalSlot, ServerHelperModule.getInstance().modeSetting.matchesOption("SpookyTime"));
            }
         }
      }
      this.resetBow();
   }

   private void resetBow() {
      this.isDrawingBow = false;
      this.bowOriginalSlot = -1;
      this.bowSwappedSlot = -1;
      this.bowHand = Hand.MAIN_HAND;
      this.currentAimVec = null;
      this.currentAimEntity = null;
   }

   private boolean placeRail(BlockPos railPos) {
      boolean hasOffhand = this.isRailItem(mc.player.getOffHandStack().getItem());
      int railSlot = hasOffhand ? -1 : this.findRailSlot();

      if (!hasOffhand && railSlot == -1) {
         if (this.autoDisableSetting.isChecked()) {
            this.notifyEmpty("Закончились рельсы");
            this.setEnabled(false);
         }
         return false;
      }

      BlockPos basePos = railPos.down();
      Vec3d hitVec = Vec3d.ofCenter(basePos).add(0.0, 0.5, 0.0);
      BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, basePos, false);

      this.applyRotation(hitVec);

      Hand hand = hasOffhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
      int originalSlot = InventoryUtils.getSelectedSlot();
      boolean swapped = !hasOffhand && railSlot >= 9;

      if (!hasOffhand) {
         if (swapped) {
            InventoryUtils.swapSlotWithHotbar(railSlot, originalSlot);
         } else {
            InventoryUtils.setSelectedSlot(railSlot, ServerHelperModule.getInstance().modeSetting.matchesOption("SpookyTime"));
         }
      }

      try {
         mc.interactionManager.interactBlock(mc.player, hand, hitResult);
         mc.player.swingHand(hand);
         this.lastActionAt = System.currentTimeMillis();
         return true;
      } finally {
         if (!hasOffhand) {
            if (swapped) {
               InventoryUtils.swapSlotWithHotbar(railSlot, originalSlot);
            } else {
               InventoryUtils.setSelectedSlot(originalSlot, ServerHelperModule.getInstance().modeSetting.matchesOption("SpookyTime"));
            }
         }
      }
   }

   private boolean placeTntMinecart(BlockPos railPos) {
      boolean hasOffhand = mc.player.getOffHandStack().isOf(Items.TNT_MINECART);
      int cartSlot = hasOffhand ? -1 : InventoryUtils.findInventorySlot(Items.TNT_MINECART);

      if (!hasOffhand && cartSlot == -1) {
         if (this.autoDisableSetting.isChecked()) {
            this.notifyEmpty("Закончились вагонетки с динамитом");
            this.setEnabled(false);
         }
         return false;
      }

      Vec3d hitVec = Vec3d.ofCenter(railPos).add(0.0, 0.1, 0.0);
      BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, railPos, false);

      this.applyRotation(hitVec);

      Hand hand = hasOffhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
      int originalSlot = InventoryUtils.getSelectedSlot();
      boolean swapped = !hasOffhand && cartSlot >= 9;

      if (!hasOffhand) {
         if (swapped) {
            InventoryUtils.swapSlotWithHotbar(cartSlot, originalSlot);
         } else {
            InventoryUtils.setSelectedSlot(cartSlot, ServerHelperModule.getInstance().modeSetting.matchesOption("SpookyTime"));
         }
      }

      try {
         mc.interactionManager.interactBlock(mc.player, hand, hitResult);
         mc.player.swingHand(hand);
         this.lastActionAt = System.currentTimeMillis();
         return true;
      } finally {
         if (!hasOffhand) {
            if (swapped) {
               InventoryUtils.swapSlotWithHotbar(cartSlot, originalSlot);
            } else {
               InventoryUtils.setSelectedSlot(originalSlot, ServerHelperModule.getInstance().modeSetting.matchesOption("SpookyTime"));
            }
         }
      }
   }

   private BlockPos findBestRailPos(LivingEntity target) {
      Vec3d eyePos = mc.player.getEyePos();
      double placeReachSq = this.placeRangeSetting.getValue() * this.placeRangeSetting.getValue();
      BlockPos targetPos = target.getBlockPos();

      Vec3d targetBase = target.getEntityPos();
      if (this.predictTargetSetting.isChecked()) {
         Vec3d vel = target.getVelocity();
         targetBase = targetBase.add(vel.x * 2.0, 0.0, vel.z * 2.0);
      }

      BlockPos bestPos = null;
      double bestScore = Double.MAX_VALUE;

      for (int dx = -2; dx <= 2; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -2; dz <= 2; dz++) {
               BlockPos pos = targetPos.add(dx, dy, dz);
               BlockState state = mc.world.getBlockState(pos);
               if (AbstractRailBlock.isRail(state)) {
                  Vec3d center = Vec3d.ofCenter(pos);
                  if (eyePos.squaredDistanceTo(center) <= placeReachSq && this.isSafeExplosion(center)) {
                     double distToTarget = center.squaredDistanceTo(targetBase);
                     if (distToTarget < bestScore) {
                        bestScore = distToTarget;
                        bestPos = pos;
                     }
                  }
               }
            }
         }
      }

      if (bestPos != null) {
         return bestPos;
      }

      bestScore = Double.MAX_VALUE;
      for (int dx = -2; dx <= 2; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -2; dz <= 2; dz++) {
               BlockPos pos = targetPos.add(dx, dy, dz);
               BlockPos basePos = pos.down();

               BlockState posState = mc.world.getBlockState(pos);
               if (!posState.isAir() && !posState.isReplaceable()) {
                  continue;
               }

               BlockState baseState = mc.world.getBlockState(basePos);
               if (!baseState.isSolid() || !Blocks.RAIL.getDefaultState().canPlaceAt(mc.world, pos)) {
                  continue;
               }

               Box box = new Box(pos);
               boolean blocked = false;
               for (Entity e : mc.world.getOtherEntities(mc.player, box)) {
                  if (e instanceof LivingEntity && e != target) {
                     blocked = true;
                     break;
                  }
               }
               if (blocked) {
                  continue;
               }

               Vec3d center = Vec3d.ofCenter(pos);
               Vec3d hitVec = Vec3d.ofCenter(basePos).add(0.0, 0.5, 0.0);
               if (eyePos.squaredDistanceTo(hitVec) > placeReachSq) {
                  continue;
               }

               if (!this.isSafeExplosion(center)) {
                  continue;
               }

               double distToTarget = center.squaredDistanceTo(targetBase);
               if (distToTarget < bestScore) {
                  bestScore = distToTarget;
                  bestPos = pos;
               }
            }
         }
      }

      return bestPos;
   }

   private TntMinecartEntity findTntMinecartNear(LivingEntity target) {
      TntMinecartEntity bestCart = null;
      double bestDistSq = Double.MAX_VALUE;
      Vec3d eyePos = mc.player.getEyePos();
      double maxShootDistSq = (this.placeRangeSetting.getValue() + 3.0) * (this.placeRangeSetting.getValue() + 3.0);

      for (Entity entity : mc.world.getEntities()) {
         if (entity instanceof TntMinecartEntity cart && cart.isAlive()) {
            Vec3d cartCenter = cart.getBoundingBox().getCenter();
            if (eyePos.squaredDistanceTo(cartCenter) > maxShootDistSq) {
               continue;
            }
            if (cart.squaredDistanceTo(target) > 5.0 * 5.0) {
               continue;
            }
            if (!this.isSafeExplosion(cartCenter)) {
               continue;
            }

            double distSq = cart.squaredDistanceTo(target);
            if (distSq < bestDistSq) {
               bestDistSq = distSq;
               bestCart = cart;
            }
         }
      }
      return bestCart;
   }

   private boolean isSafeExplosion(Vec3d explosionPos) {
      if (mc.player == null) {
         return false;
      }

      if (this.onlyWithTotemSetting.isChecked()) {
         boolean hasTotem = mc.player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING)
            || mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
         if (!hasTotem) {
            return false;
         }
      }

      float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
      if (health < this.minHealthSetting.getValue()) {
         return false;
      }

      if (this.avoidSelfSetting.isChecked()) {
         Vec3d eyePos = mc.player.getEyePos();
         Vec3d feetPos = mc.player.getEntityPos().add(0.0, 0.2, 0.0);

         double distEye = eyePos.distanceTo(explosionPos);
         double distFeet = feetPos.distanceTo(explosionPos);
         double safeDist = this.safeDistanceSetting.getValue();

         boolean hasCover = this.wallCheckSetting.isChecked() && this.hasBlastCover(eyePos, explosionPos);
         double requiredSafeDist = hasCover ? Math.max(2.0, safeDist - 1.6) : safeDist;

         if (distEye < requiredSafeDist || distFeet < requiredSafeDist) {
            return false;
         }
      }

      if (this.avoidFriendsSetting.isChecked() && this.isFriendNear(explosionPos)) {
         return false;
      }

      return true;
   }

   private boolean hasBlastCover(Vec3d start, Vec3d end) {
      if (mc.world == null) {
         return false;
      }
      RaycastContext context = new RaycastContext(
         start,
         end,
         RaycastContext.ShapeType.COLLIDER,
         RaycastContext.FluidHandling.NONE,
         mc.player
      );
      BlockHitResult hitResult = mc.world.raycast(context);
      return hitResult != null && hitResult.getType() == HitResult.Type.BLOCK;
   }

   private boolean isFriendNear(Vec3d pos) {
      for (PlayerEntity player : mc.world.getPlayers()) {
         if (player == mc.player || !player.isAlive() || !this.isFriend(player)) {
            continue;
         }
         if (player.getEntityPos().squaredDistanceTo(pos) <= 4.8 * 4.8) {
            return true;
         }
      }
      return false;
   }

   private LivingEntity findTarget() {
      LivingEntity auraTarget = AttackAuraModule.getInstance().getTarget();
      if (this.isValidTarget(auraTarget)) {
         return auraTarget;
      }

      LivingEntity nearest = null;
      double nearestDistSq = this.targetRangeSetting.getValue() * this.targetRangeSetting.getValue();

      for (Entity entity : mc.world.getEntities()) {
         if (entity instanceof LivingEntity living && this.isValidTarget(living)) {
            double distSq = living.squaredDistanceTo(mc.player);
            if (distSq < nearestDistSq) {
               nearestDistSq = distSq;
               nearest = living;
            }
         }
      }
      return nearest;
   }

   private boolean isValidTarget(LivingEntity entity) {
      if (entity == null
         || entity == mc.player
         || !entity.isAlive()
         || entity.isRemoved()
         || entity.isSpectator()) {
         return false;
      }

      if (entity instanceof PlayerEntity player) {
         if (player.isCreative()) {
            return false;
         }
         if (this.avoidFriendsSetting.isChecked() && this.isFriend(player)) {
            return false;
         }
      }

      double maxDistSq = this.targetRangeSetting.getValue() * this.targetRangeSetting.getValue();
      return entity.squaredDistanceTo(mc.player) <= maxDistSq;
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

   private int findRailSlot() {
      for (Item rail : RAILS) {
         int slot = InventoryUtils.findInventorySlot(rail);
         if (slot != -1) {
            return slot;
         }
      }
      return -1;
   }

   private boolean isRailItem(Item item) {
      for (Item rail : RAILS) {
         if (item == rail) {
            return true;
         }
      }
      return false;
   }

   private boolean hasArrow() {
      if (mc.player == null) {
         return false;
      }
      if (mc.player.getAbilities().creativeMode) {
         return true;
      }
      for (int i = 0; i < mc.player.getInventory().size(); i++) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (!stack.isEmpty() && (stack.isOf(Items.ARROW) || stack.isOf(Items.SPECTRAL_ARROW) || stack.isOf(Items.TIPPED_ARROW))) {
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
}

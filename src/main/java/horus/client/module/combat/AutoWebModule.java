package horus.client.module.combat;

import horus.client.event.EventListener;
import horus.client.event.PlayerTickEvent;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.util.EntityUtils;
import horus.client.util.InventoryUtils;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "AutoWeb", description = "Ставит паутину под цель AttackAura и возвращает предмет в руку.", category = ModuleCategory.COMBAT)
public final class AutoWebModule extends Module {
   private static AutoWebModule autoWebModule;
   private static final double PLACE_REACH = 4.5;
   private static final long PLACE_DELAY_MS = 150L;
   private long lastAttemptMillis;
   private final EventListener<PlayerTickEvent> playerTickListener = event -> this.tryPlaceWeb();

   private AutoWebModule() {
   }

   public static AutoWebModule getInstance() {
      if (autoWebModule == null) {
         autoWebModule = new AutoWebModule();
      }

      return autoWebModule;
   }

   private void tryPlaceWeb() {
      if (!AttackAuraModule.getInstance().isEnabled()
         || mc.player == null
         || mc.world == null
         || mc.interactionManager == null
         || mc.currentScreen != null
         || mc.player.currentScreenHandler != mc.player.playerScreenHandler
         || System.currentTimeMillis() - this.lastAttemptMillis < PLACE_DELAY_MS) {
         return;
      }

      LivingEntity target = AttackAuraModule.getInstance().getTarget();
      if (!(target instanceof PlayerEntity) || target == mc.player || !target.isAlive() || target.isRemoved()) {
         return;
      }

      if (EntityUtils.getDistanceToBox(target) > PLACE_REACH) {
         return;
      }

      BlockPos webPos = BlockPos.ofFloored(target.getX(), target.getY(), target.getZ());
      BlockPos supportPos = webPos.down();
      if (!mc.world.getBlockState(webPos).isReplaceable()
         || !mc.world.getBlockState(supportPos).isSideSolidFullSquare(mc.world, supportPos, Direction.UP)) {
         return;
      }

      int webSlot = InventoryUtils.findInventorySlot(Items.COBWEB);
      if (webSlot == -1) {
         return;
      }

      Vec3d hitPos = new Vec3d(webPos.getX() + 0.5, webPos.getY(), webPos.getZ() + 0.5);
      if (mc.player.getEyePos().squaredDistanceTo(hitPos) > PLACE_REACH * PLACE_REACH) {
         return;
      }

      BlockHitResult hitResult = new BlockHitResult(hitPos, Direction.UP, supportPos, false);
      boolean swappedWithHand = webSlot != mc.player.getInventory().getSelectedSlot();
      this.lastAttemptMillis = System.currentTimeMillis();
      if (swappedWithHand) {
         InventoryUtils.swapSlotWithHand(webSlot, Hand.MAIN_HAND);
      }

      try {
         mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
      } finally {
         if (swappedWithHand) {
            InventoryUtils.swapSlotWithHand(webSlot, Hand.MAIN_HAND);
         }
      }
   }
}

package horus.client.module.movement;

import horus.client.event.EventListener;
import horus.client.event.PlayerTickEvent;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.ModeSetting;
import horus.client.setting.NumberSetting;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/**
 * NoWeb — свободное движение в паутине с легитным обходом:
 * 1) замедление от паутины (slowMovement) отменяется миксином;
 * 2) чтобы анти-читы Sloth и ReallyWorld не флагали резкий дельта-рост,
 *    горизонтальная скорость в паутине ограничивается процентом от обычной
 *    ходьбы — для сервера это выглядит как человек, идущий в паутине.
 */
public final class NoWebModule extends Module {
   /** Максимальная горизонтальная скорость обычной ходьбы, блоков в тик. */
   private static final double WALK_SPEED = 0.216;

   private static NoWebModule noWebModule;

   private final ModeSetting bypassSetting = this.addSetting(
      new ModeSetting("Обход", "Под какой анти-чит калибровать скорость.", "ReallyWorld", "Sloth", "ReallyWorld")
   );
   private final NumberSetting speedSetting = this.addSetting(
      new NumberSetting("Скорость", "Процент от скорости обычной ходьбы внутри паутины.")
         .withRange(50.0F, 100.0F)
         .withStep(5.0F)
         .withSuffix("%")
         .setValue(90.0F)
   );

   private final EventListener<PlayerTickEvent> tickListener = event -> this.tickNoWeb();

   public static NoWebModule getInstance() {
      if (noWebModule == null) {
         noWebModule = new NoWebModule();
      }

      return noWebModule;
   }

   private NoWebModule() {
   }

   private void tickNoWeb() {
      PlayerEntity player = mc.player;
      if (player == null || mc.world == null || !this.isEnabled()) {
         return;
      }

      if (!this.isInWeb(player)) {
         return;
      }

      // ReallyWorld жёстче проверяет дельты в паутине — держим консервативнее
      double base = this.bypassSetting.isSelected("ReallyWorld") ? 0.85 : 0.95;
      double cap = base * (this.speedSetting.getValue() / 100.0) * WALK_SPEED;

      double vx = player.getVelocity().x;
      double vz = player.getVelocity().z;
      double horizontal = Math.hypot(vx, vz);

      if (horizontal > cap && horizontal > 0.0001) {
         double scale = cap / horizontal;
         player.setVelocity(vx * scale, player.getVelocity().y, vz * scale);
      }
   }

   private boolean isInWeb(PlayerEntity player) {
      Box box = player.getBoundingBox();
      BlockPos min = BlockPos.ofFloored(box.minX, box.minY, box.minZ);
      BlockPos max = BlockPos.ofFloored(box.maxX, box.maxY, box.maxZ);

      for (BlockPos pos : BlockPos.iterate(min, max)) {
         if (mc.world.getBlockState(pos).isOf(Blocks.COBWEB)) {
            return true;
         }
      }

      return false;
   }
}

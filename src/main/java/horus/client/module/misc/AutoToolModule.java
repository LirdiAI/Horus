package horus.client.module.misc;

import horus.client.event.EventListener;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.event.PlayerTickEvent;
import horus.client.util.InventoryUtils;
import horus.client.util.InventoryActionQueue;

import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;

@ModuleInfo(name = "AutoTool", description = "Сам берёт лучший инструмент из хотбара или инвентаря при копании блока.", category = ModuleCategory.MISC)
public final class AutoToolModule extends Module {
   private static AutoToolModule autoToolModule;
   private int intVal = -1;
   private int intVal2 = -1;
   private final EventListener<PlayerTickEvent> playerTickListener = playerTickEvent -> {
      if (mc.player != null && mc.world != null && mc.interactionManager != null && !mc.player.isCreative()) {
         if (!InventoryActionQueue.getInstance().hasPendingActions()) {
            if (mc.options.attackKey.isPressed()
               && mc.crosshairTarget != null
               && mc.crosshairTarget.getType() == Type.BLOCK
               && mc.crosshairTarget instanceof BlockHitResult blockHitResult) {
               BlockState blockState = mc.world.getBlockState(blockHitResult.getBlockPos());
               if (!blockState.isAir()) {
                  int local4769_4 = -1;
                  float miningSpeedMultiplier = mc.player.getInventory().getStack(InventoryUtils.getSelectedSlot()).getMiningSpeedMultiplier(blockState);

                  for (int index = 0; index < 36; index++) {
                     float miningSpeedMultiplier2 = mc.player.getInventory().getStack(index).getMiningSpeedMultiplier(blockState);
                     if (miningSpeedMultiplier2 > miningSpeedMultiplier + 0.01F) {
                        miningSpeedMultiplier = miningSpeedMultiplier2;
                        local4769_4 = index;
                     }
                  }

                  if (local4769_4 != -1 && local4769_4 != InventoryUtils.getSelectedSlot()) {
                     if (this.intVal == -1) {
                        this.intVal = InventoryUtils.getSelectedSlot();
                     }

                     if (local4769_4 < 9) {
                        InventoryUtils.setSelectedSlot(local4769_4, false);
                     } else if (this.intVal2 == -1) {
                        this.intVal2 = local4769_4;
                        InventoryActionQueue.getInstance().queueSlotSwap(local4769_4, this.intVal, 1);
                     }
                  }
               }
            } else if (this.intVal != -1 && !mc.options.attackKey.isPressed()) {
               if (this.intVal2 == -1) {
                  InventoryUtils.setSelectedSlot(this.intVal, false);
               } else {
                  InventoryActionQueue.getInstance().queueSlotSwap(this.intVal2, this.intVal, 1);
               }

               this.intVal = -1;
               this.intVal2 = -1;
            }
         }
      }
   };

   public static AutoToolModule getInstance() {
      if (autoToolModule == null) {
         autoToolModule = new AutoToolModule();
      }

      return autoToolModule;
   }

   @Override
   protected void onEnable() {
      this.intVal = -1;
      this.intVal2 = -1;
   }
}

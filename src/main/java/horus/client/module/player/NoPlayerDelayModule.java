package horus.client.module.player;

import horus.client.event.EventListener;
import horus.mixin.LivingEntityMixin;
import horus.mixin.MinecraftClientMixin3;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.MultiSelectSetting;
import horus.client.event.PlayerTickEvent;

import net.minecraft.item.BlockItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.GlassBottleItem;

@ModuleInfo(name = "NoPlayerDelay", description = "Убирает выбранные задержки действий игрока.", category = ModuleCategory.PLAYER)
public final class NoPlayerDelayModule extends Module {
   private static NoPlayerDelayModule noPlayerDelayModule;
   public MultiSelectSetting multiSelectSetting;
   private final EventListener<PlayerTickEvent> playerTickListener = playerTickEvent -> {
      if (mc.player != null) {
         if (this.multiSelectSetting.matchesOption("Прыжок")) {
            ((LivingEntityMixin)mc.player).setNoJumpDelay(0);
         }

         if (this.multiSelectSetting.matchesOption("Атаковать")) {
            // Сбрасываем кулдаун атаки — AttackAura бьёт даже при спаме прыжками в 2 блоках
            ((MinecraftClientMixin3)mc).setMissTime(0);
         }

         if (mc.player.getMainHandStack().getItem() instanceof BlockItem && this.multiSelectSetting.matchesOption("Ставка блоков")
            || mc.player.getMainHandStack().getItem() instanceof ExperienceBottleItem && this.multiSelectSetting.matchesOption("Пузырьки опыта")
            || mc.player.getMainHandStack().getItem() instanceof GlassBottleItem && this.multiSelectSetting.matchesOption("Бутыльки без воды")) {
            ((MinecraftClientMixin3)mc).setRightClickDelay(0);
         }
      }
   };

   private NoPlayerDelayModule() {
      this.multiSelectSetting = this.addSetting(
         new MultiSelectSetting("Задержки", "Где отключать задержку.", new String[]{"Ставка блоков", "Прыжок", "Атаковать", "Пузырьки опыта", "Бутыльки без воды"})
      );
   }

   public static NoPlayerDelayModule getInstance() {
      if (noPlayerDelayModule == null) {
         noPlayerDelayModule = new NoPlayerDelayModule();
      }

      return noPlayerDelayModule;
   }
}

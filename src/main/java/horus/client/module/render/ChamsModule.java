package horus.client.module.render;

import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.render.PlayerOutlineParams;
import horus.client.setting.BooleanSetting;
import horus.client.setting.MultiSelectSetting;
import horus.client.ui.UiTheme;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@ModuleInfo(name = "Chams", description = "Показывает выбранных существ сквозь стены вместе с экипировкой.", category = ModuleCategory.RENDER)
public final class ChamsModule extends Module implements horus.client.render.PlayerOutlineParams {
   private static ChamsModule chamsModule;
   public MultiSelectSetting multiSelectSetting = this.addSetting(
      new MultiSelectSetting("Кого показывать", "Кого видно сквозь стены.", new String[]{"Игроков", "Мобов", "Голых игроков", "Игроков в незерите"}, "Игроков")
   );
   public BooleanSetting outlineSetting = this.addSetting(new BooleanSetting("Обводка", "Шейдерная обводка игроков (как подсветка рук) вместо стандартного контура.", true));

   private ChamsModule() {
   }

   public static ChamsModule getInstance() {
      if (chamsModule == null) {
         chamsModule = new ChamsModule();
      }

      return chamsModule;
   }

   @Override
   public boolean outlineEnabled() {
      return this.isEnabled() && this.outlineSetting.isChecked();
   }

   @Override
   public int outlineColor() {
      return UiTheme.intVal7;
   }

   @Override
   public float fillValue() {
      return 0.0F;
   }

   @Override
   public float outlineValue() {
      return 0.8F;
   }

   @Override
   public float highlightValue() {
      return 0.55F;
   }

   public boolean matchesCriteria(LivingEntity livingEntity) {
      if (this.isEnabled() && livingEntity != mc.player && !(livingEntity instanceof ArmorStandEntity)) {
         if (!(livingEntity instanceof PlayerEntity playerEntity)) {
            return this.multiSelectSetting.matchesOption("Мобов");
         } else {
            if (this.multiSelectSetting.matchesOption("Игроков")) {
               return true;
            }

            int local4381_3 = 0;
            int local4381_4 = 0;

            for (EquipmentSlot equipmentSlot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
               ItemStack equippedStack = playerEntity.getEquippedStack(equipmentSlot);
               if (!equippedStack.isEmpty()) {
                  local4381_3++;
                  if (equippedStack.isOf(Items.NETHERITE_HELMET)
                     || equippedStack.isOf(Items.NETHERITE_CHESTPLATE)
                     || equippedStack.isOf(Items.NETHERITE_LEGGINGS)
                     || equippedStack.isOf(Items.NETHERITE_BOOTS)) {
                     local4381_4++;
                  }
               }
            }

            return this.multiSelectSetting.matchesOption("Голых игроков") && local4381_3 == 0 || this.multiSelectSetting.matchesOption("Игроков в незерите") && local4381_4 > 0;
         }
      } else {
         return false;
      }
   }
}

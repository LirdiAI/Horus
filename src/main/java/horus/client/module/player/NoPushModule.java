package horus.client.module.player;

import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.MultiSelectSetting;

@ModuleInfo(name = "NoPush", description = "Не даёт выбранным источникам толкать игрока.", category = ModuleCategory.PLAYER)
public final class NoPushModule extends Module {
   private static NoPushModule noPushModule;
   public final MultiSelectSetting multiSelectSetting = this.addSetting(
      new MultiSelectSetting("Толчки от", "Что не может толкать игрока.", new String[]{"Вода", "Лава", "Блоки", "Сущности", "Граница мира"}, "Вода", "Сущности")
   );

   public static NoPushModule getInstance() {
      if (noPushModule == null) {
         noPushModule = new NoPushModule();
      }

      return noPushModule;
   }

   private NoPushModule() {
   }

   public boolean matchesCriteria(String local5397_1) {
      return this.isEnabled() && this.multiSelectSetting.matchesOption(local5397_1);
   }
}

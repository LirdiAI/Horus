package horus.client.module.render;

import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.BooleanSetting;

@ModuleInfo(
   name = "DynamicIsland",
   description = "Когда играет музыка, ватермарка превращается в остров с обложкой трека, названием и исполнителями.",
   category = ModuleCategory.RENDER
)
public final class DynamicIslandModule extends Module {
   private static DynamicIslandModule dynamicIslandModule;
   public BooleanSetting textTrackSetting;

   private DynamicIslandModule() {
      this.textTrackSetting = this.addSetting(
         new BooleanSetting("Дорожка текста", "Вкл — плавная бегущая строка «Название — Исполнитель». Выкл — название белым и исполнители серым.", false)
      );
   }

   public static DynamicIslandModule getInstance() {
      if (dynamicIslandModule == null) {
         dynamicIslandModule = new DynamicIslandModule();
      }

      return dynamicIslandModule;
   }
}

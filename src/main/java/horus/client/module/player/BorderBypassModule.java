package horus.client.module.player;

import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;

@ModuleInfo(name = "BorderBypass", description = "Позволяет проходить за границу мира (синие полосы).", category = ModuleCategory.PLAYER)
public final class BorderBypassModule extends Module {
   private static BorderBypassModule borderBypassModule;

   public static BorderBypassModule getInstance() {
      if (borderBypassModule == null) {
         borderBypassModule = new BorderBypassModule();
      }

      return borderBypassModule;
   }

   private BorderBypassModule() {
   }
}

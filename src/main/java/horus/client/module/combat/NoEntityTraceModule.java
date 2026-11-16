package horus.client.module.combat;

import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;

@ModuleInfo(
   name = "NoEntityTrace",
   description = "Убирает хитбокс у всех сущностей в мире позволяя вам взаимодействовать через них.",
   category = ModuleCategory.COMBAT
)
public final class NoEntityTraceModule extends Module {
   private static NoEntityTraceModule noEntityTraceModule;

   private NoEntityTraceModule() {
   }

   public static NoEntityTraceModule getInstance() {
      if (noEntityTraceModule == null) {
         noEntityTraceModule = new NoEntityTraceModule();
      }

      return noEntityTraceModule;
   }
}

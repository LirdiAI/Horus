package horus.client.module.render;

import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.KeybindSetting;
import horus.client.setting.NumberSetting;

@ModuleInfo(name = "Zoom", description = "Приближает изображение при удержании бинда.", category = ModuleCategory.RENDER)
public final class ZoomModule extends Module {
   private static ZoomModule zoomModule;
   private final KeybindSetting zoomKey = this.addSetting(new KeybindSetting("Бинд зума", "Удерживай кнопку для приближения."));
   private final NumberSetting zoomStrength = this.addSetting(
      new NumberSetting("Сила зума", "Во сколько раз приближать. Колёсико меняет силу при удержании бинда.")
         .withRange(2.0F, 10.0F)
         .withStep(0.5F)
         .setValue(4.0F)
   );

   private ZoomModule() {
   }

   public static ZoomModule getInstance() {
      if (zoomModule == null) {
         zoomModule = new ZoomModule();
      }
      return zoomModule;
   }

   public float getFovMultiplier() {
      return this.isEnabled()
            && mc.player != null
            && this.zoomKey.isBound()
            && this.zoomKey.getBinding().isPressed(mc.getWindow().getHandle())
         ? 1.0F / this.zoomStrength.floatValue()
         : -1.0F;
   }

   public boolean adjustZoomScroll(double scrollAmount) {
      if (!this.isEnabled()
         || mc.player == null
         || mc.currentScreen != null
         || !this.zoomKey.isBound()
         || !this.zoomKey.getBinding().isPressed(mc.getWindow().getHandle())
         || scrollAmount == 0.0) {
         return false;
      }

      float direction = scrollAmount > 0.0 ? 1.0F : -1.0F;
      this.zoomStrength.setValue(this.zoomStrength.getValue() + direction * this.zoomStrength.getStep());
      return true;
   }
}

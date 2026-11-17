package horus.client.module.render;

import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.render.HandOutlineParams;
import horus.client.setting.BooleanSetting;
import horus.client.ui.UiTheme;

@ModuleInfo(name = "ItemOutline", description = "Красивая шейдерная обводка предмета в руке: живая аура духов обтекает предмет по контуру.", category = ModuleCategory.RENDER)
public final class ItemOutlineModule extends Module implements HandOutlineParams {
   private static ItemOutlineModule itemOutlineModule;
   public BooleanSetting animationSetting;

   private ItemOutlineModule() {
      this.animationSetting = this.addSetting(new BooleanSetting("Анимация", "Аура духов движется, дышит и переливается. Выкл — статичная обводка.", true));
   }

   public static ItemOutlineModule getInstance() {
      if (itemOutlineModule == null) {
         itemOutlineModule = new ItemOutlineModule();
      }

      return itemOutlineModule;
   }

   @Override
   public boolean outlineEnabled() {
      return this.isEnabled();
   }

   @Override
   public int outlineColor() {
      if (!this.animationSetting.isChecked()) {
         return UiTheme.intVal7;
      }

      return shiftHue(UiTheme.intVal7, 0.09 * Math.sin(System.currentTimeMillis() / 1000.0 * 0.7));
   }

   @Override
   public float fillValue() {
      return 0.0F;
   }

   @Override
   public float outlineValue() {
      if (!this.animationSetting.isChecked()) {
         return 0.8F;
      }

      double t = System.currentTimeMillis() / 1000.0;
      return 0.78F + 0.14F * (float) Math.sin(t * 1.7) + 0.05F * (float) Math.sin(t * 0.9 + 1.3);
   }

   @Override
   public float highlightValue() {
      if (!this.animationSetting.isChecked()) {
         return 0.55F;
      }

      double t = System.currentTimeMillis() / 1000.0;
      // «Дыхание» ауры: свечение медленно раздувается и опадает + лёгкая рябь
      return 0.42F + 0.30F * (float) (0.5 + 0.5 * Math.sin(t * 0.8)) + 0.12F * (float) Math.sin(t * 2.3 + 0.7);
   }

   private static int shiftHue(int rgb, double shift) {
      double red = (rgb >> 16 & 0xFF) / 255.0;
      double green = (rgb >> 8 & 0xFF) / 255.0;
      double blue = (rgb & 0xFF) / 255.0;
      double max = Math.max(red, Math.max(green, blue));
      double min = Math.min(red, Math.min(green, blue));
      if (max <= min) {
         return rgb;
      }

      double hue;
      if (max == red) {
         hue = (green - blue) / (max - min) / 6.0;
      } else if (max == green) {
         hue = (blue - red) / (max - min) / 6.0 + 1.0 / 3.0;
      } else {
         hue = (red - green) / (max - min) / 6.0 + 2.0 / 3.0;
      }

      double saturation = max <= 0.0 ? 0.0 : (max - min) / max;
      hue = (hue + shift + 1.0) % 1.0;
      double value = max;
      double h6 = hue * 6.0;
      int sextant = (int) h6;
      double fraction = h6 - sextant;
      double p = value * (1.0 - saturation);
      double q = value * (1.0 - fraction * saturation);
      double t2 = value * (1.0 - (1.0 - fraction) * saturation);

      double[] channels = switch (sextant % 6) {
         case 0 -> new double[]{value, t2, p};
         case 1 -> new double[]{q, value, p};
         case 2 -> new double[]{p, value, t2};
         case 3 -> new double[]{p, q, value};
         case 4 -> new double[]{t2, p, value};
         default -> new double[]{value, p, q};
      };

      int r2 = (int) Math.round(channels[0] * 255.0);
      int g2 = (int) Math.round(channels[1] * 255.0);
      int b2 = (int) Math.round(channels[2] * 255.0);
      return 0xFF000000 | r2 << 16 | g2 << 8 | b2;
   }
}

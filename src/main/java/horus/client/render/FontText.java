package horus.client.render;

import horus.client.render.FontFace;
import horus.client.util.FormattedText;
import horus.client.render.Render2D;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class FontText {
   public static void processData(
      FontFace fontFace,
      String local2050_1,
      float local2050_2,
      float local2050_3,
      float local2050_4,
      int local2050_5,
      float local2050_6,
      float local2050_7,
      float local2050_8
   ) {
      Render2D.processData2(
         fontFace.getResourceName(), local2050_1, local2050_2, local2050_3, local2050_4, local2050_5, local2050_6, local2050_7, local2050_8
      );
   }

   private FontText() {
   }

   public static void processData2(
      FontFace fontFace,
      String local2052_1,
      float local2052_2,
      float local2052_3,
      float local2052_4,
      int local2052_5,
      int local2052_6,
      float local2052_7
   ) {
      Render2D.processData(fontFace.getResourceName(), local2052_1, local2052_2 + local2052_7, local2052_3 + local2052_7, local2052_4, local2052_6);
      Render2D.processData(fontFace.getResourceName(), local2052_1, local2052_2, local2052_3, local2052_4, local2052_5);
   }

   public static float computeFloat(FontFace fontFace, String local2053_1, float local2053_2) {
      return Render2D.computeFloat2(fontFace.getResourceName(), local2053_1, local2053_2);
   }

   public static void processData3(FontFace fontFace, String local2054_1, float local2054_2, float local2054_3, float local2054_4, int local2054_5) {
      Render2D.processData(fontFace.getResourceName(), local2054_1, local2054_2, local2054_3, local2054_4, local2054_5);
   }

   public static void processData4(FontFace fontFace, String local2055_1, float local2055_2, float local2055_3, float local2055_4, int local2055_5) {
      float local2055_6 = Math.max(0.5F, local2055_4 / 16.0F);
      float local2055_7 = Math.round(local2055_6 * 2.0F) / 2.0F;
      int local2055_8 = computeInt(local2055_5);
      processData2(fontFace, local2055_1, local2055_2, local2055_3, local2055_4, local2055_5, local2055_8, local2055_7);
   }

   private static int computeInt(int local2056_0) {
      int local2056_1 = local2056_0 >>> 24 & 0xFF;
      int local2056_2 = Math.round(local2056_1 * 0.55F);
      return local2056_2 << 24;
   }

   public static void processData5(FontFace fontFace, Text newText, float local2057_2, float local2057_3, float local2057_4, int local2057_5) {
      StringBuilder stringBuilder = new StringBuilder();

      for (FormattedText.DataRecord local2057_8 : FormattedText.getArrayList2(newText, local2057_5)) {
         if (local2057_8.obfuscated()) {
            Render2D.processData54(
               fontFace.getResourceName(),
               local2057_8.text(),
               local2057_2 + computeFloat(fontFace, stringBuilder.toString(), local2057_4),
               local2057_3,
               local2057_4,
               local2057_8.color() & 16777215 | local2057_5 & 0xFF000000
            );
         } else {
            processData3(
               fontFace,
               local2057_8.text(),
               local2057_2 + computeFloat(fontFace, stringBuilder.toString(), local2057_4),
               local2057_3,
               local2057_4,
               local2057_8.color() & 16777215 | local2057_5 & 0xFF000000
            );
         }

         stringBuilder.append(local2057_8.text());
      }
   }

   public static void renderElement(
      DrawContext drawContext, FontFace fontFace, String local2058_2, float local2058_3, float local2058_4, float local2058_5, int local2058_6
   ) {
      Render2D.renderElement5(drawContext);
      processData3(fontFace, local2058_2, local2058_3, local2058_4, local2058_5, local2058_6);
      Render2D.updateState6();
   }

   public static void processData6(
      FontFace fontFace,
      String local2059_1,
      float local2059_2,
      float local2059_3,
      float local2059_4,
      int local2059_5,
      float local2059_6,
      float local2059_7,
      float local2059_8,
      float local2059_9,
      float local2059_10
   ) {
      Render2D.processData67(
         fontFace.getResourceName(),
         local2059_1,
         local2059_2,
         local2059_3,
         local2059_4,
         local2059_5,
         local2059_6,
         local2059_7,
         local2059_8,
         local2059_9,
         local2059_10
      );
   }

   public static void processData7(
      FontFace fontFace,
      String local2060_1,
      float local2060_2,
      float local2060_3,
      float local2060_4,
      int local2060_5,
      float local2060_6,
      float local2060_7,
      float local2060_8,
      boolean local2060_9,
      boolean local2060_10
   ) {
      Render2D.processData76(
         fontFace.getResourceName(),
         local2060_1,
         local2060_2,
         local2060_3,
         local2060_4,
         local2060_5,
         local2060_6,
         local2060_7,
         local2060_8,
         local2060_9,
         local2060_10
      );
   }

   public static void processData8(
      FontFace fontFace,
      String local2061_1,
      float local2061_2,
      float local2061_3,
      float local2061_4,
      int local2061_5,
      int local2061_6,
      int local2061_7,
      int local2061_8
   ) {
      Render2D.processData19(
         fontFace.getResourceName(), local2061_1, local2061_2, local2061_3, local2061_4, local2061_5, local2061_6, local2061_7, local2061_8
      );
   }

   public static void processData9(FontFace fontFace, String local2062_1, float local2062_2, float local2062_3, float local2062_4, int local2062_5) {
      float local2062_6 = computeFloat(fontFace, local2062_1, local2062_4);
      processData3(fontFace, local2062_1, local2062_2 - local2062_6 * 0.5F, local2062_3, local2062_4, local2062_5);
   }
}

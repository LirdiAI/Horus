package horus.client.ui;

import horus.client.render.ArcColorSpan;
import horus.client.render.FontText;
import horus.client.render.Outline360DrawData;

import horus.client.render.FontFace;
import horus.client.render.Render2D;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public final class UiTheme {
   public static int intVal = new Color(255, 255, 255, 255).getRGB();
   public static int intVal2 = new Color(255, 255, 255, 184).getRGB();
   public static int intVal3 = new Color(255, 255, 255, 122).getRGB();
   public static int intVal4 = new Color(255, 255, 255, 61).getRGB();
   public static int intVal5 = new Color(255, 255, 255, 26).getRGB();
   public static int intVal6 = new Color(255, 255, 255, 10).getRGB();
   public static int intVal7 = -1;
   public static int intVal8 = -1;
   public static int intVal9 = -1;
   public static int intVal10 = new Color(255, 255, 255, 255).getRGB();
   public static int intVal11 = new Color(15, 14, 17, 255).getRGB();
   public static int intVal12 = new Color(255, 255, 255, 255).getRGB();
   public static int intVal13 = new Color(19, 18, 22, 255).getRGB();
   public static int intVal14 = new Color(26, 25, 31, 255).getRGB();
   public static int intVal15 = new Color(21, 20, 23, 255).getRGB();
   public static int intVal16 = new Color(255, 255, 255, 20).getRGB();
   public static int intVal17 = new Color(255, 255, 255, 5).getRGB();
   public static int intVal18 = new Color(255, 255, 255, 20).getRGB();
   public static int intVal19 = new Color(255, 255, 255, 10).getRGB();
   public static int intVal20 = new Color(255, 255, 255, 5).getRGB();
   public static int intVal21 = new Color(255, 255, 255, 20).getRGB();
   public static final int intVal22 = new Color(196, 181, 253, 255).getRGB();
   public static final int intVal23 = new Color(231, 130, 137, 255).getRGB();
   public static int intVal24 = new Color(255, 255, 255, 8).getRGB();
   public static int intVal25 = new Color(255, 255, 255, 20).getRGB();
   public static int intVal26 = new Color(255, 255, 255, 6).getRGB();
   public static int intVal27 = new Color(255, 255, 255, 26).getRGB();
   public static int intVal28 = new Color(255, 255, 255, 178).getRGB();
   public static int intVal29 = new Color(255, 255, 255, 128).getRGB();
   public static int intVal30 = new Color(255, 255, 255, 102).getRGB();
   public static int intVal31 = new Color(255, 255, 255, 38).getRGB();
   public static int intVal32 = new Color(255, 255, 255, 8).getRGB();
   public static int intVal33 = new Color(255, 255, 255, 20).getRGB();
   public static int intVal34 = -1;
   public static int intVal35 = new Color(255, 81, 81, 255).getRGB();
   public static final String strVal = "assets/horus/textures/gui/mainmenu/background/menu.png";
   public static final String strVal2 = "assets/horus/textures/gui/mainmenu/background/menu_blur.png";
   public static final String strVal3 = "assets/horus/textures/gui/mainmenu/background/loading.png";
   public static final String strVal4 = "assets/horus/textures/gui/mainmenu/background/welcome.png";
   public static final String strVal5 = "assets/horus/textures/gui/mainmenu/icons/logo.png";
   public static final String strVal6 = "assets/horus/textures/gui/mainmenu/icons/arrow.png";
   public static final String strVal7 = "assets/horus/textures/gui/mainmenu/icons/person.png";
   public static final String strVal8 = "assets/horus/textures/gui/mainmenu/icons/telegram.png";
   public static final String strVal9 = "assets/horus/textures/gui/mainmenu/icons/discord.png";
   public static final String strVal10 = "assets/horus/textures/gui/mainmenu/icons/chevron.png";
   public static final String strVal11 = "assets/horus/textures/gui/mainmenu/icons/exit.png";
   public static final String strVal12 = "assets/horus/textures/gui/mainmenu/icons/settings.png";
   public static final String strVal13 = "assets/horus/textures/gui/mainmenu/icons/accounts.png";
   public static final String strVal14 = "assets/horus/textures/gui/mainmenu/icons/singleplayer.png";
   public static final String strVal15 = "assets/horus/textures/gui/mainmenu/icons/bell.png";
   public static final String strVal16 = "assets/horus/textures/gui/mainmenu/icons/pattern.png";
   public static final String strVal17 = "assets/horus/textures/gui/mainmenu/icons/search.png";
   public static final String strVal18 = "assets/horus/textures/gui/mainmenu/icons/back.png";
   public static final String strVal19 = "assets/horus/textures/gui/mainmenu/icons/dice.png";
   public static final String strVal20 = "assets/horus/textures/gui/mainmenu/icons/plus.png";
   public static final String strVal21 = "assets/horus/textures/gui/mainmenu/icons/user.png";
   public static final String strVal22 = "assets/horus/textures/gui/mainmenu/icons/delete.png";
   public static final String strVal23 = "assets/horus/textures/gui/mainmenu/icons/rename.png";
   public static final String strVal24 = "assets/horus/textures/gui/mainmenu/icons/grass.png";
   public static final String strVal25 = "assets/horus/textures/gui/mainmenu/icons/horus_serif.png";
   public static final String strVal26 = "assets/horus/textures/gui/mainmenu/icons/cos_menu.png";
   public static boolean flag;
   public static final String TEXT_ICONS = "icons";
   public static final String strVal27 = "α";
   public static final String strVal28 = "β";
   public static final String strVal29 = "γ";
   public static final String strVal30 = "δ";
   public static final String strVal31 = "ε";
   public static final String strVal32 = "b";
   public static final String strVal33 = "9";
   public static final String strVal34 = "B";
   public static final String strVal35 = "e";
   public static final String strVal36 = "d";
   public static final String strVal37 = "ζ";
   public static final String strVal38 = "E";
   public static final String strVal39 = "S";
   public static final String strVal40 = "W";
   public static final String strVal41 = "λ";
   public static final String strVal42 = "μ";
   public static final String strVal43 = "θ";
   public static final String strVal44 = "η";
   public static final String strVal45 = "N";
   public static final String strVal46 = "O";
   public static final String strVal47 = "P";
   public static final String strVal48 = "ι";
   public static final String strVal49 = "R";

   public static void processData(
      float local4499_0, float local4499_1, float local4499_2, float local4499_3, float local4499_4, int local4499_5, int local4499_6
   ) {
      Render2D.processData51(local4499_0, local4499_1, local4499_2, local4499_3, local4499_4, local4499_5);
      Render2D.processData86(local4499_0, local4499_1, local4499_2, local4499_3, local4499_4, 0.5F, local4499_6);
   }

   public static void processData2(float local4500_0, float local4500_1, float local4500_2, float local4500_3, int local4500_4) {
      long local4500_5 = System.currentTimeMillis();
      float local4500_7 = -((float)(local4500_5 % 1100L) / 1100.0F * 360.0F);
      float local4500_8 = (float)(local4500_5 % 1800L) / 1800.0F;
      local4500_8 = local4500_8 < 0.5F ? computeFloat2(local4500_8 * 2.0F) : 1.0F - computeFloat2((local4500_8 - 0.5F) * 2.0F);
      float local4500_9 = 55.0F + 230.0F * local4500_8;
      Render2D.processData42(
         new Outline360DrawData(
               local4500_0 - local4500_2,
               local4500_1 - local4500_2,
               local4500_2 * 2.0F,
               local4500_2 * 2.0F,
               local4500_2,
               local4500_3,
               Render2D.computeInt(computeInt(intVal, 0.051F)),
               List.of(ArcColorSpan.of(local4500_7, local4500_7 + local4500_9, Render2D.computeInt(local4500_4)))
            )
            .withBlendDegrees(Math.min(48.0F, local4500_9 * 0.42F))
      );
   }

   public static List<String> getList(FontFace fontFace, String local4501_1, float local4501_2, float local4501_3) {
      List<String> local4501_4 = new ArrayList<>();
      StringBuilder stringBuilder = new StringBuilder();

      for (String local4501_9 : local4501_1.split(" ")) {
         String local4501_10 = stringBuilder.length() == 0 ? local4501_9 : stringBuilder + " " + local4501_9;
         if (stringBuilder.length() != 0 && !(FontText.computeFloat(fontFace, local4501_10, local4501_2) <= local4501_3)) {
            local4501_4.add(stringBuilder.toString());
            stringBuilder = new StringBuilder(local4501_9);
         } else {
            stringBuilder = new StringBuilder(local4501_10);
         }
      }

      if (stringBuilder.length() > 0) {
         local4501_4.add(stringBuilder.toString());
      }

      return local4501_4;
   }

   public static float computeFloat(float local4502_0) {
      local4502_0 = Math.max(0.0F, Math.min(1.0F, local4502_0));
      return 1.0F + 2.70158F * (local4502_0 - 1.0F) * (local4502_0 - 1.0F) * (local4502_0 - 1.0F) + 1.70158F * (local4502_0 - 1.0F) * (local4502_0 - 1.0F);
   }

   public static float computeFloat2(float local4503_0) {
      local4503_0 = Math.max(0.0F, Math.min(1.0F, local4503_0));
      return 1.0F - (1.0F - local4503_0) * (1.0F - local4503_0);
   }

   public static void processData3(String local4504_0, float local4504_1, float local4504_2, float local4504_3, float local4504_4, int local4504_5) {
      Render2D.processData89(
         local4504_0, local4504_1 - local4504_3 / 2.0F, local4504_2 - local4504_4 / 2.0F, local4504_3, local4504_4, 0.0F, local4504_5
      );
   }

   public static int computeInt(int local4505_0, float local4505_1) {
      return Math.round((local4505_0 >>> 24 & 0xFF) * Math.max(0.0F, Math.min(1.0F, local4505_1))) << 24 | local4505_0 & 16777215;
   }

   private UiTheme() {
   }

   public static void processData4(long local4508_0, float local4508_2, float local4508_3) {
      float local4508_4 = Math.min(local4508_2 / 960.0F, local4508_3 / 540.0F);
      float local4508_5 = (local4508_3 - 540.0F * local4508_4) / 2.0F;
      long local4508_6 = System.currentTimeMillis();
      float local4508_8 = computeFloat(computeFloat3(local4508_0, 0L, 520L));
      float local4508_9 = local4508_2 / 2.0F;
      float local4508_10 = local4508_5 + 229.0F * local4508_4;
      Render2D.processData46(Math.min(1.0F, local4508_8 * 2.0F));
      processData2(local4508_9, local4508_10, 28.5F * local4508_4 * local4508_8, 2.7F * local4508_4 * Math.max(0.001F, local4508_8), intVal7);
      float local4508_11 = 1.0F + 0.045F * (float)Math.sin((float)(local4508_6 % 1600L) / 1600.0F * Math.PI * 2.0);
      processData3(
         "assets/horus/textures/gui/mainmenu/icons/logo.png",
         local4508_9,
         local4508_10,
         15.5F * local4508_4 * local4508_8 * local4508_11,
         12.0F * local4508_4 * local4508_8 * local4508_11,
         intVal7
      );
      Render2D.updateState();
      float local4508_12 = computeFloat2(computeFloat3(local4508_0, 140L, 480L));
      float local4508_13 = FontText.computeFloat(FontFace.SEMI, "Запускаем Horus...", 15.0F * local4508_4);
      Render2D.processData46(local4508_12);
      FontText.processData3(
         FontFace.SEMI,
         "Запускаем Horus" + ".".repeat(1 + (int)(local4508_6 / 450L % 3L)),
         local4508_9 - local4508_13 / 2.0F,
         local4508_5 + 283.0F * local4508_4 + (1.0F - local4508_12) * 10.0F * local4508_4,
         15.0F * local4508_4,
         intVal
      );
      Render2D.updateState();
      float local4508_14 = computeFloat2(computeFloat3(local4508_0, 240L, 480L));
      Render2D.processData46(local4508_14);
      float local4508_15 = local4508_5 + 313.0F * local4508_4 + (1.0F - local4508_14) * 10.0F * local4508_4;

      for (String local4508_17 : getList(
         FontFace.SEMI,
         "Загружаем необходимые компоненты и применяем ваши настройки. Ещё немного - и Horus будет готов к работе.",
         7.5F * local4508_4,
         190.0F * local4508_4
      )) {
         FontText.processData3(
            FontFace.SEMI,
            local4508_17,
            local4508_9 - FontText.computeFloat(FontFace.SEMI, local4508_17, 7.5F * local4508_4) / 2.0F,
            local4508_15,
            7.5F * local4508_4,
            intVal29
         );
         local4508_15 += 9.0F * local4508_4;
      }

      Render2D.updateState();
   }

   public static boolean matchesCriteria(float local4509_0, float local4509_1, float local4509_2, float local4509_3, float local4509_4, float local4509_5) {
      return local4509_0 >= local4509_2 && local4509_0 <= local4509_2 + local4509_4 && local4509_1 >= local4509_3 && local4509_1 <= local4509_3 + local4509_5;
   }

   public static void processData5(String local4510_0, String local4510_1, float local4510_2, float local4510_3, float local4510_4, int local4510_5) {
      float[] floatArray = Render2D.computeFloat(local4510_0, local4510_1, local4510_4);
      Render2D.processData73(
         local4510_0,
         local4510_1,
         local4510_2 - (floatArray[0] + floatArray[2]) / 2.0F,
         local4510_3 - (floatArray[1] + floatArray[3]) / 2.0F + 1.0F,
         local4510_4,
         local4510_5
      );
   }

   public static int computeInt2(int local4511_0, int local4511_1, float local4511_2) {
      float local4511_3 = Math.max(0.0F, Math.min(1.0F, local4511_2));
      return computeInt3(local4511_0 >>> 24 & 0xFF, local4511_1 >>> 24 & 0xFF, local4511_3) << 24
         | computeInt3(local4511_0 >>> 16 & 0xFF, local4511_1 >>> 16 & 0xFF, local4511_3) << 16
         | computeInt3(local4511_0 >>> 8 & 0xFF, local4511_1 >>> 8 & 0xFF, local4511_3) << 8
         | computeInt3(local4511_0 & 0xFF, local4511_1 & 0xFF, local4511_3);
   }

   public static void processData6(String local4512_0, float local4512_1, float local4512_2, float local4512_3) {
      float local4512_4 = Math.max(local4512_1 / 960.0F, local4512_2 / 540.0F);
      Render2D.processData89(
         local4512_0,
         (local4512_1 - 960.0F * local4512_4) / 2.0F,
         (local4512_2 - 540.0F * local4512_4) / 2.0F,
         960.0F * local4512_4,
         540.0F * local4512_4,
         0.0F,
         computeInt(intVal, local4512_3)
      );
   }

   private static int computeInt3(int local4513_0, int local4513_1, float local4513_2) {
      return Math.round(local4513_0 + (local4513_1 - local4513_0) * local4513_2);
   }

   public static void processData7(String local4514_0, float local4514_1, float local4514_2, float local4514_3, int local4514_4) {
      processData5("icons", local4514_0, local4514_1, local4514_2, local4514_3, local4514_4);
   }

   public static float computeFloat3(long local4515_0, long local4515_2, long local4515_4) {
      return Math.max(0.0F, Math.min(1.0F, (float)(System.currentTimeMillis() - local4515_0 - local4515_2) / (float)local4515_4));
   }
}

package horus.client.ui.hud;

import horus.client.render.FontText;
import horus.client.render.ThemeColors;
import horus.client.ui.UiTheme;

import horus.client.render.FontFace;
import horus.client.render.Render2D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;

public final class NotificationHud {
   private static final List<NotificationHud.ToastNotification> list = new ArrayList<>();
   private static NotificationHud.ToastNotification nested0449;

   public static void processData(boolean local4193_0) {
      long local4193_1 = System.currentTimeMillis();
      boolean local4193_3 = false;

      for (NotificationHud.ToastNotification local4193_5 : list) {
         if (local4193_5 != nested0449 && local4193_1 - local4193_5.timeMillis <= 3220L) {
            local4193_3 = true;
            break;
         }
      }

      if (local4193_0 && !local4193_3) {
         if (nested0449 == null || !list.contains(nested0449)) {
            NotificationHud.ToastNotification local4193_4 = new NotificationHud.ToastNotification();
            local4193_4.strVal = "F";
            local4193_4.strVal2 = "Example";
            local4193_4.strVal3 = "Notification!";
            local4193_4.flag = true;
            nested0449 = local4193_4;
            list.add(local4193_4);
            updateState();
         }
      } else if (nested0449 != null) {
         if (local4193_1 - nested0449.timeMillis < 3000L) {
            nested0449.timeMillis = local4193_1 - 3000L;
         }

         nested0449 = null;
      }
   }

   private static void updateState() {
      long local4194_0 = System.currentTimeMillis();
      list.removeIf(local4195_2 -> local4194_0 - local4195_2.timeMillis > 3220L);
      int local4194_2 = 0;

      for (NotificationHud.ToastNotification local4194_4 : list) {
         if (local4194_0 - local4194_4.timeMillis < 3000L) {
            local4194_2++;
         }
      }

      for (NotificationHud.ToastNotification local4194_4 : list) {
         if (local4194_2 <= 6) {
            return;
         }

         if (local4194_0 - local4194_4.timeMillis < 3000L) {
            local4194_4.timeMillis = local4194_0 - 3000L;
            local4194_2--;
         }
      }
   }

   public static void processData2(float local4196_0, float local4196_1) {
      long local4196_2 = System.currentTimeMillis();
      Iterator<NotificationHud.ToastNotification> local4196_4 = list.iterator();

      while (local4196_4.hasNext()) {
         if (local4196_2 - local4196_4.next().timeMillis > 3220L) {
            local4196_4.remove();
         }
      }

      if (!list.isEmpty()) {
         float[] floatArray = HudManager.computeFloat5("toasts", local4196_0 / 2.0F, local4196_1 * 0.62F);
         float local4196_6 = floatArray[1];
         float local4196_7 = local4196_6;
         float local4196_8 = 0.0F;

         for (int index = list.size() - 1; index >= 0; index--) {
            NotificationHud.ToastNotification local4196_10 = list.get(index);
            long local4196_11 = local4196_2 - local4196_10.timeMillis;
            float local4196_13 = ThemeColors.computeFloat((float)local4196_11 / 220.0F);
            float local4196_14 = Math.min(local4196_13, ThemeColors.computeFloat((float)(3220L - local4196_11) / 220.0F));
            if (!(local4196_14 < 0.01F)) {
               local4196_8 = Math.max(local4196_8, computeFloat(local4196_10));
               local4196_7 = computeFloat2(local4196_10, floatArray[0], local4196_7, local4196_13, local4196_11, local4196_14);
            }
         }

         if (local4196_7 - local4196_6 > 0.5F) {
            HudManager.processData6(
               "toasts", floatArray[0] - local4196_8 / 2.0F, local4196_6, local4196_8, local4196_7 - local4196_6, floatArray[0], local4196_6
            );
         }
      }
   }

   public static void processData3(String local4197_0, String local4197_1, String local4197_2, boolean local4197_3) {
      NotificationHud.ToastNotification local4197_4 = new NotificationHud.ToastNotification();
      local4197_4.strVal = local4197_0;
      local4197_4.strVal2 = local4197_1;
      local4197_4.strVal3 = local4197_2;
      local4197_4.flag = local4197_3;
      list.add(local4197_4);
      updateState();
   }

   public static void processData4(StatusEffectInstance statusEffectInstance, String local4198_1, String local4198_2, boolean local4198_3) {
      NotificationHud.ToastNotification local4198_4 = new NotificationHud.ToastNotification();
      local4198_4.statusEffectInstance2 = statusEffectInstance;
      local4198_4.strVal2 = local4198_1;
      local4198_4.strVal3 = local4198_2;
      local4198_4.flag = local4198_3;
      list.add(local4198_4);
      updateState();
   }

   private static float computeFloat(NotificationHud.ToastNotification local4199_0) {
      return 16.0F
         + FontText.computeFloat(FontFace.SEMI_BOLD, local4199_0.strVal2, 5.5F)
         + 1.0F
         + FontText.computeFloat(FontFace.SEMI_BOLD, local4199_0.strVal3, 5.5F)
         + 4.5F
         + 5.25F
         + 4.5F;
   }

   public static void notifyWithText(String local4201_0, String local4201_1, Text text, boolean local4201_3) {
      NotificationHud.ToastNotification local4201_4 = new NotificationHud.ToastNotification();
      local4201_4.strVal = local4201_0;
      local4201_4.strVal2 = local4201_1;
      local4201_4.strVal3 = text.getString();
      local4201_4.text2 = text;
      local4201_4.flag = local4201_3;
      list.add(local4201_4);
      updateState();
   }

   private static float computeFloat2(
      NotificationHud.ToastNotification local4202_0, float local4202_1, float local4202_2, float local4202_3, long local4202_4, float local4202_6
   ) {
      float local4202_7 = computeFloat(local4202_0);
      float local4202_8 = local4202_1 - local4202_7 / 2.0F;
      float local4202_9 = local4202_2 - 4.0F * (1.0F - local4202_3);
      float local4202_10 = local4202_9 + 7.5F;
      float local4202_11 = ThemeColors.computeFloat(1.0F - (float)local4202_4 / 3000.0F);
      Render2D.processData46(local4202_6);
      HudStyle.processData4(local4202_8, local4202_9, local4202_7, 15.0F, local4202_6, 5.5F);
      if (local4202_0.statusEffectInstance2 != null) {
         Render2D.processData69(local4202_0.statusEffectInstance2.getEffectType(), local4202_8 + 4.5F, local4202_10 - 3.5F, 7.0F);
      } else {
         ThemeColors.processData(
            local4202_0.strVal,
            local4202_8 + 4.5F + 3.5F,
            local4202_10,
            7.0F,
            local4202_0.flag ? ThemeColors.intVal9 : ThemeColors.intVal9 & 16777215 | 2046820352
         );
      }

      float local4202_12 = local4202_8 + 4.5F + 7.0F + 2.0F + 0.5F + 2.0F;
      float local4202_13 = local4202_10 - 2.75F;
      FontText.processData3(
         FontFace.SEMI_BOLD,
         local4202_0.strVal2,
         local4202_12,
         local4202_13,
         5.5F,
         local4202_0.flag ? HudStyle.computeInt2(1.0F) : HudStyle.computeInt2(0.6F)
      );
      float local4202_14 = local4202_12 + FontText.computeFloat(FontFace.SEMI_BOLD, local4202_0.strVal2, 5.5F) + 1.0F;
      int local4202_15 = local4202_0.flag ? HudStyle.computeInt4(1.0F) : HudStyle.computeInt2(1.0F);
      if (local4202_0.text2 != null) {
         FontText.processData5(FontFace.SEMI_BOLD, local4202_0.text2, local4202_14, local4202_13, 5.5F, local4202_15);
      } else {
         FontText.processData3(FontFace.SEMI_BOLD, local4202_0.strVal3, local4202_14, local4202_13, 5.5F, local4202_15);
      }

      ThemeColors.processData5(
         local4202_8 + local4202_7 - 4.5F - 2.625F,
         local4202_10,
         5.25F,
         local4202_11,
         UiTheme.computeInt2(
            ThemeColors.intVal4, local4202_0.flag ? ThemeColors.intVal9 : ThemeColors.intVal7, local4202_11
         )
      );
      Render2D.updateState();
      return local4202_2 + 18.5F * local4202_6;
   }

   private static final class ToastNotification {
      String strVal;
      StatusEffectInstance statusEffectInstance2;
      String strVal2;
      String strVal3;
      Text text2;
      boolean flag;
      long timeMillis = System.currentTimeMillis();
   }
}

package horus.client.ui;

import horus.client.render.FontText;

import horus.client.event.EventBus;
import horus.client.event.EventListener;
import horus.client.render.FontFace;
import horus.client.event.HudRenderEvent;
import horus.client.module.ModuleCategory;
import horus.client.render.Render2D;

public final class UiWarmup {
   private static final UiWarmup uiWarmup = new UiWarmup();
   private static volatile int intVal = 5;
   private static volatile boolean flag;
   private final EventListener<HudRenderEvent> hudRenderListener = hudRenderEvent -> {
      if (intVal > 0) {
         intVal--;
         if (!flag) {
            updateState();
         }

         try {
            this.updateState2();
         } catch (Throwable throwable) {
         }

         try {
            this.updateState4();
         } catch (Throwable throwable2) {
         }

         if (intVal <= 0) {
            EventBus.getInstance().unsubscribe(uiWarmup);
         }
      }
   };

   public static void updateState() {
      String local3048_0 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 .,:;!?-_()[]{}<>/\\|@#$%^&*+=\"'`~АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";

      for (FontFace fontFace : FontFace.values()) {
         try {
            FontText.computeFloat(
               fontFace,
               fontFace == FontFace.ICONS ? "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789αβγδεζηθικλμνξ" : local3048_0,
               8.0F
            );
         } catch (Throwable throwable) {
         }
      }

      flag = true;
   }

   private void updateState2() {
      Render2D.processData51(-4000.0F, 0.0F, 2.0F, 2.0F, 1.0F, 33554431);
      Render2D.processData37(-4000.0F, 0.0F, 2.0F, 2.0F, 1.0F, 33554431, 33554431, 33554431, 33554431);
      Render2D.processData86(-4000.0F, 0.0F, 2.0F, 2.0F, 1.0F, 0.5F, 33554431);
      Render2D.processData75(-4000.0F, 0.0F, 2.0F, 2.0F, 1.0F, 0.5F, 33554431);
      Render2D.processData55(-4000.0F, 1.0F, 1.0F, 33554431);
      Render2D.processData14(-4000.0F, 1.0F, 1.0F, 0.5F, 33554431);
      Render2D.processData70(-4000.0F, 0.0F, 2.0F, 1.0F, 0.25F, 2, 33554431);
      Render2D.processData80(-4000.0F, 0.0F, 4.0F, 4.0F, 2.0F, 2.0F, 2.0F, 2.0F, 3.0F, 1.0F, 0.0F, 0.0F, 1.0F, 33554431);
      Render2D.processData56(-4000.0F, 0.0F, 2.0F, 2.0F, 0.003921569F);
      Render2D.processData77(-4000.0F, 0.0F, 2.0F, 2.0F, 33554431, 0.003921569F);
      Render2D.processData25(-4000.0F, 0.0F, 2.0F, 2.0F, 1.0F, 8.0F);
      Render2D.processData60(-4000.0F, 0.0F, 2.0F, 2.0F, 1.0F, 8.0F, 1.0F, 33554431);
      Render2D.processData44(-4000.0F, 0.0F, 2.0F, 2.0F, 1.0F);
      Render2D.processData93(-4000.0F, 0.0F, 2.0F, 2.0F, 0.5F, 0.5F, 1.0F, 33554431);
      Render2D.processData28(-4000.0F, 0.0F, 2.0F, 2.0F, 0.5F, 0.5F, 1.0F, 0.5F, 33554431);
      Render2D.processData13(-4000.0F, 0.0F, 2.0F, 2.0F, 1.0F, 33554431, 0.5F);
      Render2D.processData91(-4000.0F, 1.0F, 33554431, 0.5F, 0.5F);
      Render2D.processData9(-4000.0F, 0.0F, 2.0F, 2.0F, 1.0F, 1.0F, 1.0F, 0.5F, 33554431, 33554431);
      Render2D.processData57(-4000.0F, 0.0F, 2.0F, 2.0F, 1.0F, 33554431, 33554431, "icons", "a", 1.0F, 2.0F, 2.0F, 0.0F);
      Render2D.processData23(FontFace.SEMI_BOLD.getResourceName(), "Ag", -4000.0F, 0.0F, 6.0F, 0.5F, 0.35F, 0.5F, 0.003921569F);

      for (FontFace fontFace : FontFace.values()) {
         FontText.processData3(fontFace, fontFace == FontFace.ICONS ? "aM" : "Ag", -4000.0F, 0.0F, 6.0F, 33554431);
      }
   }

   private UiWarmup() {
   }

   public static void updateState3() {
      intVal = 5;
      flag = false;
      EventBus.getInstance().unsubscribe(uiWarmup);
      EventBus.getInstance().subscribe(uiWarmup);
   }

   private void updateState4() {
      for (ModuleCategory moduleCategory : ModuleCategory.values()) {
         Render2D.processData89(ClickGuiStyle.formatText(moduleCategory), -4000.0F, 0.0F, 2.0F, 2.0F, 0.0F, 33554431);
      }

      for (String local3054_4 : new String[]{
         "assets/horus/textures/gui/clickgui/search.png",
         "assets/horus/textures/gui/clickgui/chev.png",
         "assets/horus/textures/gui/clickgui/chevright.png",
         "assets/horus/textures/gui/clickgui/check.png",
         "assets/horus/textures/gui/clickgui/picker.png",
         "assets/horus/textures/gui/clickgui/bindkey.png"
      }) {
         Render2D.processData89(local3054_4, -4000.0F, 0.0F, 2.0F, 2.0F, 0.0F, 33554431);
      }
   }

   public static void updateState5() {
      EventBus.getInstance().subscribe(uiWarmup);
   }
}

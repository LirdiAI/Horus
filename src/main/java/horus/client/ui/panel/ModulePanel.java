package horus.client.ui.panel;

import horus.client.ui.ClickGuiStyle;
import horus.client.render.RectDrawData;
import horus.client.ui.setting.SettingRenderContext;
import horus.client.ui.setting.SettingRendererRegistry;
import horus.client.ui.UiTheme;

import horus.client.module.Module;
import horus.client.render.Render2D;
import horus.client.setting.Setting;
import horus.client.setting.SettingGroup;

import java.util.List;

public final class ModulePanel {
   public static final float floatVal = 19.6F;

   private static void processData(SettingRenderContext settingRenderContext, Module newModule, float local5312_2, float local5312_3, float[] floatArray) {
      float local5312_5 = settingRenderContext.scale;
      float local5312_6 = settingRenderContext.computeFloat2("tgl:" + newModule.name, newModule.isEnabled());
      float local5312_7 = 10.74F * local5312_5;
      float local5312_8 = 6.64F * local5312_5;
      Render2D.processData72(
         new RectDrawData(
               local5312_2,
               local5312_3,
               local5312_7,
               local5312_8,
               local5312_8 / 2.0F,
               local5312_8 / 2.0F,
               local5312_8 / 2.0F,
               local5312_8 / 2.0F,
               Render2D.computeInt(UiTheme.computeInt2(ClickGuiStyle.intVal15, ClickGuiStyle.intVal17, local5312_6))
            )
            .withSmoothness(0.5F)
      );
      float local5312_9 = 4.88F * local5312_5;
      float local5312_10 = local5312_2 + 0.93F * local5312_5 + local5312_6 * (local5312_7 - local5312_9 - 1.86F * local5312_5);
      Render2D.processData30(
         local5312_10 + local5312_9 / 2.0F,
         local5312_3 + local5312_8 / 2.0F,
         local5312_9 / 2.0F,
         0.5F,
         Render2D.computeInt(UiTheme.computeInt2(ClickGuiStyle.intVal16, ClickGuiStyle.intVal, local5312_6))
      );
      settingRenderContext.processData7(
         local5312_2 - 1.0F * local5312_5,
         local5312_3 - 1.5F * local5312_5,
         local5312_7 + 2.0F * local5312_5,
         local5312_8 + 3.0F * local5312_5,
         floatArray,
         newModule::toggle
      );
   }

   private static float computeFloat(
      SettingRenderContext settingRenderContext,
      SettingGroup settingGroup,
      float local5314_2,
      float local5314_3,
      float local5314_4,
      float local5314_5,
      float[] floatArray
   ) {
      float local5314_7 = settingRenderContext.scale;
      if (!settingGroup.checkState()) {
         return computeFloat2(settingRenderContext, settingGroup.list, local5314_2, local5314_3, local5314_4, local5314_5, floatArray);
      }

      float local5314_8 = !settingGroup.list.isEmpty()
         ? settingRenderContext.computeFloat10(settingGroup.object, settingRenderContext.matchesCriteria(settingGroup.object, false))
         : 0.0F;
      settingRenderContext.processData15(
         settingGroup.strVal,
         local5314_3,
         local5314_4,
         5.5F * local5314_7,
         UiTheme.computeInt2(ClickGuiStyle.intVal13, ClickGuiStyle.intVal14, local5314_2),
         local5314_5 - 6.0F * local5314_7
      );
      Render2D.processData10(
         "assets/horus/textures/gui/clickgui/chev.png",
         local5314_3 + local5314_5 - 4.0F * local5314_7,
         local5314_4 + 0.6F * local5314_7,
         4.0F * local5314_7,
         0.0F,
         local5314_8 * 180.0F,
         local5314_3 + local5314_5 - 2.0F * local5314_7,
         local5314_4 + 2.6F * local5314_7,
         UiTheme.computeInt2(ClickGuiStyle.intVal8, ClickGuiStyle.intVal9, local5314_2)
      );
      settingRenderContext.processData7(
         local5314_3,
         local5314_4 - 1.5F * local5314_7,
         local5314_5,
         8.0F * local5314_7,
         floatArray,
         () -> settingRenderContext.processData5(settingGroup.object, false)
      );
      float local5314_9 = 6.0F * local5314_7;
      if (local5314_8 > 0.001F) {
         float[] floatArray2 = SettingRenderContext.computeFloat3(
            floatArray,
            local5314_3,
            local5314_4 + 6.0F * local5314_7,
            local5314_5,
            local5314_8 * (3.5F * local5314_7 + computeFloat5(settingRenderContext, settingGroup, local5314_5))
         );
         Render2D.renderElement2(settingRenderContext.drawContext, floatArray2[0], floatArray2[1], floatArray2[2], floatArray2[3]);
         Render2D.processData46(local5314_8);
         computeFloat2(
            settingRenderContext, settingGroup.list, local5314_2, local5314_3, local5314_4 + 6.0F * local5314_7 + 3.5F * local5314_7, local5314_5, floatArray2
         );
         Render2D.updateState();
         Render2D.renderElement(settingRenderContext.drawContext);
         local5314_9 = 6.0F * local5314_7 + local5314_8 * (3.5F * local5314_7 + computeFloat5(settingRenderContext, settingGroup, local5314_5));
      }

      return local5314_9;
   }

   private static float computeFloat2(
      SettingRenderContext settingRenderContext,
      List<Setting<?>> local5315_1,
      float local5315_2,
      float local5315_3,
      float local5315_4,
      float local5315_5,
      float[] floatArray
   ) {
      float local5315_7 = settingRenderContext.scale;
      float local5315_8 = local5315_4;
      boolean local5315_9 = true;

      for (Setting<?> local5315_11 : local5315_1) {
         float local5315_12 = settingRenderContext.computeFloat10(local5315_11, local5315_11.isVisible());
         if (!(local5315_12 < 0.001F)) {
            if (!local5315_9) {
               local5315_8 += 3.5F * local5315_7 * local5315_12;
            }

            local5315_9 = false;
            if (local5315_12 > 0.999F) {
               local5315_8 += SettingRendererRegistry.computeFloat(settingRenderContext, local5315_11, local5315_2, local5315_3, local5315_8, local5315_5, floatArray);
            } else {
               float local5315_13 = SettingRendererRegistry.computeFloat2(settingRenderContext, local5315_11, local5315_5);
               float[] floatArray2 = SettingRenderContext.computeFloat3(
                  floatArray, local5315_3 - 3.0F * local5315_7, local5315_8, local5315_5 + 6.0F * local5315_7, local5315_13 * local5315_12
               );
               Render2D.renderElement2(settingRenderContext.drawContext, floatArray2[0], floatArray2[1], floatArray2[2], floatArray2[3]);
               Render2D.processData46(local5315_12);
               SettingRendererRegistry.computeFloat(
                  settingRenderContext, local5315_11, local5315_2, local5315_3, local5315_8 - (1.0F - local5315_12) * 4.0F * local5315_7, local5315_5, floatArray2
               );
               Render2D.updateState();
               Render2D.renderElement(settingRenderContext.drawContext);
               local5315_8 += local5315_13 * local5315_12;
            }
         }
      }

      return local5315_8 - local5315_4;
   }

   private static float computeFloat3(SettingRenderContext settingRenderContext, SettingGroup settingGroup, float local5316_2) {
      float local5316_3 = settingRenderContext.scale;
      if (!settingGroup.checkState()) {
         return computeFloat5(settingRenderContext, settingGroup, local5316_2);
      }

      float local5316_4 = 6.0F * local5316_3;
      if (settingGroup.list.isEmpty()) {
         return local5316_4;
      }

      float local5316_5 = settingRenderContext.computeFloat10(settingGroup.object, settingRenderContext.matchesCriteria(settingGroup.object, false));
      return local5316_5 < 0.001F ? local5316_4 : local5316_4 + local5316_5 * (3.5F * local5316_3 + computeFloat5(settingRenderContext, settingGroup, local5316_2));
   }

   public static float computeFloat4(SettingRenderContext settingRenderContext, Module newModule, float local5319_2) {
      float local5319_3 = settingRenderContext.scale;
      float local5319_4 = 19.6F * local5319_3;
      if (!SettingGroup.matchesCriteria(SettingGroup.getList(newModule))) {
         return local5319_4;
      }

      float local5319_5 = settingRenderContext.computeFloat10(newModule, newModule.bindingActive);
      return local5319_5 < 0.001F
         ? local5319_4
         : local5319_4
            + local5319_5 * (computeFloat6(settingRenderContext, SettingGroup.getList(newModule), local5319_2 - 12.5F * local5319_3) + 5.1F * local5319_3);
   }

   private ModulePanel() {
   }

   public static void processData2(
      SettingRenderContext settingRenderContext, Module newModule, float local5322_2, float local5322_3, float local5322_4, float[] floatArray
   ) {
      float local5322_6 = settingRenderContext.scale;
      float local5322_7 = settingRenderContext.computeFloat2("tgl:" + newModule.name, newModule.isEnabled());
      List<SettingGroup> local5322_8 = SettingGroup.getList(newModule);
      boolean local5322_9 = SettingGroup.matchesCriteria(local5322_8);
      float local5322_10 = local5322_9 ? settingRenderContext.computeFloat10(newModule, newModule.bindingActive) : 0.0F;

      for (SettingGroup settingGroup : local5322_8) {
         for (Setting<?> local5322_14 : settingGroup.list) {
            if (settingRenderContext.object == local5322_14
               && (
                  !newModule.bindingActive || !local5322_14.isVisible() || settingGroup.checkState() && !settingRenderContext.matchesCriteria(settingGroup.object, false)
               )) {
               settingRenderContext.object = null;
            }
         }
      }

      float local5322_11 = computeFloat4(settingRenderContext, newModule, local5322_4);
      float local5322_12 = Math.max(local5322_7, local5322_10);
      local5322_12 = Math.max(
         local5322_12,
         0.35F
            * settingRenderContext.computeFloat2(
               "row:" + newModule.name,
               UiTheme.matchesCriteria(settingRenderContext.mouseX, settingRenderContext.mouseY, local5322_2, local5322_3, local5322_4, 19.6F * local5322_6)
                  && UiTheme.matchesCriteria(
                     settingRenderContext.mouseX, settingRenderContext.mouseY, floatArray[0], floatArray[1], floatArray[2], floatArray[3]
                  )
            )
      );
      if (local5322_12 > 0.005F) {
         Render2D.processData51(
            local5322_2,
            local5322_3,
            local5322_4,
            local5322_11,
            4.5F * local5322_6,
            UiTheme.computeInt(
               UiTheme.computeInt2(ClickGuiStyle.intVal6, ClickGuiStyle.intVal5, local5322_7), local5322_12
            )
         );
         Render2D.processData86(
            local5322_2,
            local5322_3,
            local5322_4,
            local5322_11,
            4.5F * local5322_6,
            0.5F,
            UiTheme.computeInt(ClickGuiStyle.intVal3, local5322_12)
         );
      }

      settingRenderContext.processData11(
         local5322_2,
         local5322_3,
         local5322_4,
         19.6F * local5322_6,
         floatArray,
         newModule::toggle,
         local5322_9 ? () -> newModule.bindingActive = !newModule.bindingActive : null,
         () -> settingRenderContext.module = settingRenderContext.module == newModule ? null : newModule
      );
      float local5322_13 = local5322_2 + local5322_4 - 16.1F * local5322_6;
      float local5322_14 = local5322_3 + 9.8F * local5322_6;
      if (newModule.description != null
         && !newModule.description.isEmpty()
         && UiTheme.matchesCriteria(settingRenderContext.mouseX, settingRenderContext.mouseY, floatArray[0], floatArray[1], floatArray[2], floatArray[3])) {
         settingRenderContext.processData17(
            newModule.description, local5322_2 + 3.0F * local5322_6, local5322_3 + 2.0F * local5322_6, local5322_4 * 0.6F, 15.6F * local5322_6
         );
      }

      float local5322_15 = (local5322_9 ? local5322_13 - 10.5F * local5322_6 : local5322_13 - 2.0F * local5322_6) - (local5322_2 + 6.3F * local5322_6);
      settingRenderContext.processData15(
         settingRenderContext.module == newModule ? "Нажми клавишу" : newModule.name,
         local5322_2 + 6.3F * local5322_6,
         local5322_3 + 5.8F * local5322_6,
         7.0F * local5322_6,
         settingRenderContext.module == newModule
            ? ClickGuiStyle.intVal
            : UiTheme.computeInt2(ClickGuiStyle.intVal7, ClickGuiStyle.intVal, local5322_7),
         local5322_15
      );
      if (local5322_9) {
         float local5322_16 = local5322_13 - 6.7F * local5322_6;
         Render2D.processData10(
            "assets/horus/textures/gui/clickgui/chev.png",
            local5322_16 - 2.25F * local5322_6,
            local5322_14 - 2.25F * local5322_6,
            4.5F * local5322_6,
            0.0F,
            local5322_10 * 180.0F,
            local5322_16,
            local5322_14,
            UiTheme.computeInt2(ClickGuiStyle.intVal8, ClickGuiStyle.intVal9, Math.max(local5322_7, local5322_10))
         );
         settingRenderContext.processData7(
            local5322_16 - 3.5F * local5322_6,
            local5322_14 - 3.5F * local5322_6,
            7.0F * local5322_6,
            7.0F * local5322_6,
            floatArray,
            () -> newModule.bindingActive = !newModule.bindingActive
         );
      }

      processData(settingRenderContext, newModule, local5322_13, local5322_14 - 3.32F * local5322_6, floatArray);
      if (local5322_10 > 0.001F) {
         float local5322_16 = local5322_3 + 19.0F * local5322_6;
         float[] floatArray2 = SettingRenderContext.computeFloat3(floatArray, local5322_2, local5322_16, local5322_4, local5322_11 - 19.0F * local5322_6);
         Render2D.renderElement2(settingRenderContext.drawContext, floatArray2[0], floatArray2[1], floatArray2[2], floatArray2[3]);
         Render2D.processData46(local5322_10);
         float local5322_18 = local5322_16;

         for (int index = 0; index < local5322_8.size(); index++) {
            local5322_18 += computeFloat(
               settingRenderContext,
               local5322_8.get(index),
               local5322_7,
               local5322_2 + 6.3F * local5322_6,
               local5322_18,
               local5322_4 - 12.5F * local5322_6,
               floatArray2
            );
            if (index < local5322_8.size() - 1) {
               local5322_18 += 3.5F * local5322_6;
            }
         }

         Render2D.updateState();
         Render2D.renderElement(settingRenderContext.drawContext);
      }
   }

   private static float computeFloat5(SettingRenderContext settingRenderContext, SettingGroup settingGroup, float local5323_2) {
      float local5323_3 = settingRenderContext.scale;
      float local5323_4 = 0.0F;
      boolean local5323_5 = true;

      for (Setting<?> local5323_7 : settingGroup.list) {
         float local5323_8 = settingRenderContext.computeFloat10(local5323_7, local5323_7.isVisible());
         if (!(local5323_8 < 0.001F)) {
            if (!local5323_5) {
               local5323_4 += 3.5F * local5323_3 * local5323_8;
            }

            local5323_4 += SettingRendererRegistry.computeFloat2(settingRenderContext, local5323_7, local5323_2) * local5323_8;
            local5323_5 = false;
         }
      }

      return local5323_4;
   }

   private static float computeFloat6(SettingRenderContext settingRenderContext, List<SettingGroup> local5324_1, float local5324_2) {
      float local5324_3 = settingRenderContext.scale;
      float local5324_4 = 0.0F;

      for (int index = 0; index < local5324_1.size(); index++) {
         local5324_4 += computeFloat3(settingRenderContext, local5324_1.get(index), local5324_2);
         if (index < local5324_1.size() - 1) {
            local5324_4 += 3.5F * local5324_3;
         }
      }

      return local5324_4;
   }
}

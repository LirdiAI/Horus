package horus.client.ui.panel;

import horus.client.ui.ClickGuiStyle;
import horus.client.render.FontText;
import horus.client.render.RoundedClipStack;
import horus.client.ui.setting.SettingRenderContext;
import horus.client.ui.SmoothScroll;

import horus.client.render.FontFace;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleManager;
import horus.client.render.Render2D;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ModuleCategoryPanel {
   private static final Map<Module, ModuleCategoryPanel.DataRecord> map = new IdentityHashMap<>();

   public static void processData(
      SettingRenderContext settingRenderContext,
      ModuleCategory moduleCategory,
      SmoothScroll smoothScroll,
      float local5347_3,
      float local5347_4,
      float local5347_5,
      float local5347_6,
      float local5347_7
   ) {
      float local5347_8 = settingRenderContext.scale;
      float local5347_9 = local5347_4 + 21.0F * local5347_8;
      Render2D.processData60(
         local5347_3 + 0.5F * local5347_8,
         local5347_4 + 0.5F * local5347_8,
         local5347_5 - 1.0F * local5347_8,
         local5347_6 - 1.0F * local5347_8,
         8.0F * local5347_8,
         30.0F,
         1.0F,
         -1
      );
      Render2D.processData51(local5347_3, local5347_4, local5347_5, local5347_6, 8.5F * local5347_8, ClickGuiStyle.intVal2);
      Render2D.processData86(local5347_3, local5347_4, local5347_5, local5347_6, 8.5F * local5347_8, 0.5F, ClickGuiStyle.intVal3);
      boolean searching = !settingRenderContext.strVal.isEmpty();
      FontText.processData3(
         FontFace.SF,
         searching ? "РЕЗУЛЬТАТЫ ПОИСКА".toUpperCase(Locale.ROOT) : moduleCategory.getDisplayName().toUpperCase(Locale.ROOT),
         local5347_3 + 11.0F * local5347_8,
         local5347_4 + 9.0F * local5347_8,
         6.3F * local5347_8,
         ClickGuiStyle.intVal4
      );
      Render2D.processData89(
         ClickGuiStyle.formatText(moduleCategory),
         local5347_3 + local5347_5 - 17.3F * local5347_8,
         local5347_4 + 9.4F * local5347_8,
         6.4F * local5347_8,
         6.4F * local5347_8,
         0.0F,
         ClickGuiStyle.intVal4
      );
      smoothScroll.update(local5347_7);
      float local5347_10 = local5347_4 + 26.5F * local5347_8 - smoothScroll.getPixelAlignedOffset();
      Render2D.renderElement2(settingRenderContext.drawContext, local5347_3, local5347_9, local5347_5, local5347_6 - 21.0F * local5347_8);
      RoundedClipStack.processData(
         local5347_3, local5347_9, local5347_5, local5347_6 - 21.0F * local5347_8, 0.0F, 0.0F, 8.5F * local5347_8, 8.5F * local5347_8
      );
      float[] floatArray = null;

      List<Module> moduleList = searching ? ModuleManager.getModules() : ModuleManager.getModulesByCategory(moduleCategory);

      for (Module module : moduleList) {
         ModuleCategoryPanel.DataRecord local5347_14 = getNested0484(module);
         float local5347_15 = settingRenderContext.computeFloat7(
            local5347_14.searchKey,
            !settingRenderContext.strVal.isEmpty()
                  && !local5347_14.lowerName.contains(settingRenderContext.strVal)
                  && !local5347_14.lowerDescription.contains(settingRenderContext.strVal)
               ? 0.0F
               : 1.0F
         );
         if (!(local5347_15 < 0.01F)) {
            if (local5347_15 < 0.999F) {
               Render2D.processData46(local5347_15 * local5347_15);
            }

            float local5347_16 = ModulePanel.computeFloat4(settingRenderContext, module, local5347_5 - 9.2F * local5347_8);
            if (local5347_10 + local5347_16 >= local5347_9 - 2.0F && local5347_10 <= local5347_9 + (local5347_6 - 21.0F * local5347_8) + 2.0F) {
               if (floatArray == null) {
                  floatArray = new float[]{local5347_3, local5347_9, local5347_5, local5347_6 - 21.0F * local5347_8};
               }

               ModulePanel.processData2(
                  settingRenderContext, module, local5347_3 + 4.6F * local5347_8, local5347_10, local5347_5 - 9.2F * local5347_8, floatArray
               );
            }

            if (local5347_15 < 0.999F) {
               Render2D.updateState();
            }

            local5347_10 += (local5347_16 + 1.5F * local5347_8) * local5347_15;
         }
      }

      RoundedClipStack.updateState2();
      Render2D.renderElement(settingRenderContext.drawContext);
      smoothScroll.maximumOffset = Math.max(0.0F, local5347_10 - local5347_4 + smoothScroll.getPixelAlignedOffset() - local5347_6 + 4.5F * local5347_8);
   }

   private ModuleCategoryPanel() {
   }

   private static ModuleCategoryPanel.DataRecord getNested0484(Module module) {
      ModuleCategoryPanel.DataRecord local5350_1 = map.get(module);
      if (local5350_1 == null || local5350_1.name != module.name || local5350_1.description != module.description) {
         String local5350_2 = module.description == null ? "" : module.description;
         local5350_1 = new ModuleCategoryPanel.DataRecord(
            module.name, module.description, module.name.toLowerCase(), local5350_2.toLowerCase(), "search." + module.name
         );
         map.put(module, local5350_1);
      }

      return local5350_1;
   }

   private record DataRecord(String name, String description, String lowerName, String lowerDescription, String searchKey) {
   }
}

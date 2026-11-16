package horus.client.ui.panel;

import horus.client.ui.AnimatedValue;
import horus.client.ui.ClickGuiStyle;
import horus.client.render.CornerRadii;
import horus.client.render.FontText;
import horus.client.render.ItemRenderCache;
import horus.client.util.PotionPreset;
import horus.client.render.RoundedClipStack;
import horus.client.ui.setting.SettingRenderContext;
import horus.client.render.ShapeDrawing;
import horus.client.ui.UiTheme;

import horus.client.render.FontFace;
import horus.client.util.ItemDrawOptions;
import horus.client.setting.KeybindSetting;
import horus.client.render.Render2D;
import horus.client.module.player.ServerHelperModule;
import horus.client.setting.TextSetting;

import java.util.ArrayList;
import net.minecraft.item.ItemStack;

public final class PotionPresetPanel {
   static final String strVal = "assets/horus/textures/gui/presets/logo.png";
   static final String strVal2 = "assets/horus/textures/gui/presets/chip.png";
   static final String strVal3 = "assets/horus/textures/gui/presets/search.png";
   static final String strVal4 = "assets/horus/textures/gui/presets/keyboard_dim.png";
   static final String strVal5 = "assets/horus/textures/gui/presets/keyboard_on.png";
   static final String strVal6 = "assets/horus/textures/gui/presets/toggle.png";
   static final String strVal7 = "assets/horus/textures/gui/presets/trash.png";
   static final String strVal8 = "assets/horus/textures/gui/presets/chip_small.png";
   static final String strVal9 = "assets/horus/textures/gui/presets/potion_add.png";
   static final String strVal10 = "assets/horus/textures/gui/presets/search_small.png";
   private static PotionPresetPanel potionPresetPanel;
   private AnimatedValue animatedValue = new AnimatedValue(300L).getAnimatedValue(0.0);
   private boolean flag;
   private long timeMillis;

   private void processData(SettingRenderContext settingRenderContext, float local1824_2, float local1824_3, boolean local1824_4, float local1824_5, float local1824_6) {
      PotionPreset potionPreset = PotionPreset.getInstance();
      float local1824_8 = (local1824_2 - 263.0F * settingRenderContext.scale) / 2.0F;
      float local1824_9 = (local1824_3 - 133.33F * settingRenderContext.scale) / 2.0F + local1824_6;
      long local1824_10 = System.currentTimeMillis();
      processData2(
         local1824_8, local1824_9, 166.0F * settingRenderContext.scale, 133.33F * settingRenderContext.scale, 5.7F * settingRenderContext.scale, settingRenderContext.scale
      );
      processData2(
         local1824_8 + 172.0F * settingRenderContext.scale,
         local1824_9,
         91.0F * settingRenderContext.scale,
         133.33F * settingRenderContext.scale,
         5.7F * settingRenderContext.scale,
         settingRenderContext.scale
      );
      if (local1824_4) {
         settingRenderContext.processData7(local1824_8, local1824_9, 166.0F * settingRenderContext.scale, 133.33F * settingRenderContext.scale, null, () -> {});
      }

      if (local1824_4) {
         settingRenderContext.processData7(
            local1824_8 + 172.0F * settingRenderContext.scale, local1824_9, 91.0F * settingRenderContext.scale, 133.33F * settingRenderContext.scale, null, () -> {}
         );
      }

      processData3(
         settingRenderContext,
         local1824_8,
         local1824_9,
         166.0F * settingRenderContext.scale,
         "Создание пресета",
         false,
         "Potion Combiner",
         "Выбрасывает указанные зелья из инвентаря по нажатию указанной вами клавиши",
         136.0F,
         151.33F
      );
      processData3(
         settingRenderContext,
         local1824_8 + 172.0F * settingRenderContext.scale,
         local1824_9,
         91.0F * settingRenderContext.scale,
         "Настройки пресетов",
         true,
         "Ваши пресеты",
         "Созданные вами ранее пресеты системы Potion Combiner",
         80.0F,
         75.0F
      );
      FontText.processData3(
         FontFace.SEMI,
         "Имя пресета",
         local1824_8 + 6.67F * settingRenderContext.scale,
         local1824_9 + 46.67F * settingRenderContext.scale,
         4.2F * settingRenderContext.scale,
         UiTheme.intVal
      );
      FontText.processData3(
         FontFace.SEMI,
         "Уникальное имя данного пресета",
         local1824_8 + 6.67F * settingRenderContext.scale,
         local1824_9 + 52.33F * settingRenderContext.scale,
         4.2F * settingRenderContext.scale,
         UiTheme.computeInt(UiTheme.intVal, 0.35F)
      );
      processData5(
         settingRenderContext,
         potionPreset.textSetting,
         local1824_8 + 6.67F * settingRenderContext.scale,
         local1824_9 + 60.0F * settingRenderContext.scale,
         151.33F * settingRenderContext.scale,
         9.0F * settingRenderContext.scale,
         local1824_4,
         "pc:name"
      );
      FontText.processData3(
         FontFace.SEMI,
         "Горячая клавиша",
         local1824_8 + 6.67F * settingRenderContext.scale,
         local1824_9 + 74.33F * settingRenderContext.scale,
         4.2F * settingRenderContext.scale,
         UiTheme.intVal
      );
      FontText.processData3(
         FontFace.SEMI,
         "Зелье выбрасываются при нажатии бинда",
         local1824_8 + 6.67F * settingRenderContext.scale,
         local1824_9 + 80.0F * settingRenderContext.scale,
         4.2F * settingRenderContext.scale,
         UiTheme.computeInt(UiTheme.intVal, 0.35F)
      );
      this.processData4(
         settingRenderContext,
         potionPreset.keybindSetting,
         local1824_8 + 134.0F * settingRenderContext.scale,
         local1824_9 + 76.0F * settingRenderContext.scale,
         30.0F,
         local1824_4,
         "pc:draftbind"
      );

      for (int index = 0; index < 7; index++) {
         float local1824_13 = local1824_8 + 6.67F * settingRenderContext.scale + index * 22.0F * settingRenderContext.scale;
         float local1824_14 = local1824_9 + 90.0F * settingRenderContext.scale;
         Render2D.processData51(
            local1824_13,
            local1824_14,
            19.67F * settingRenderContext.scale,
            19.67F * settingRenderContext.scale,
            3.33F * settingRenderContext.scale,
            UiTheme.computeInt(UiTheme.intVal7, 0.05F)
         );
         Render2D.processData86(
            local1824_13,
            local1824_14,
            19.67F * settingRenderContext.scale,
            19.67F * settingRenderContext.scale,
            3.33F * settingRenderContext.scale,
            0.35F * settingRenderContext.scale,
            UiTheme.computeInt(
               UiTheme.intVal7,
               index == 6
                  ? 0.19F
                  : 0.06F
                     + 0.13F
                        * settingRenderContext.computeFloat2(
                           "pc:slot:" + index,
                           local1824_4
                              && UiTheme.matchesCriteria(
                                 settingRenderContext.mouseX,
                                 settingRenderContext.mouseY,
                                 local1824_13,
                                 local1824_14,
                                 19.67F * settingRenderContext.scale,
                                 19.67F * settingRenderContext.scale
                              )
                        )
            )
         );
         if (index < 6 && !potionPreset.itemStackArray[index].isEmpty()) {
            ItemRenderCache.processData(
               potionPreset.itemStackArray[index],
               local1824_13 + 4.33F * settingRenderContext.scale,
               local1824_14 + 4.17F * settingRenderContext.scale,
               11.0F * settingRenderContext.scale,
               ItemDrawOptions.noDecorations(computeFloat2())
            );
         }

         if (index < 6 && potionPreset.strValArray[index] != null) {
            settingRenderContext.processData17(
               potionPreset.strValArray[index], local1824_13, local1824_14, 19.67F * settingRenderContext.scale, 19.67F * settingRenderContext.scale
            );
         }

         int index2 = index;
         if (local1824_4) {
            settingRenderContext.processData7(local1824_13, local1824_14, 19.67F * settingRenderContext.scale, 19.67F * settingRenderContext.scale, null, () -> {
               if (index2 < 6 && !potionPreset.itemStackArray[index2].isEmpty()) {
                  potionPreset.itemStackArray[index2] = ItemStack.EMPTY;
                  potionPreset.strValArray[index2] = null;
               } else {
                  potionPreset.intVal = index2 < 6 ? index2 : potionPreset.computeInt();
               }
            });
         }
      }

      float local1824_12 = settingRenderContext.computeFloat2(
         "pc:save",
         local1824_4
            && UiTheme.matchesCriteria(
               settingRenderContext.mouseX,
               settingRenderContext.mouseY,
               local1824_8 + 6.67F * settingRenderContext.scale,
               local1824_9 + 116.33F * settingRenderContext.scale,
               151.33F * settingRenderContext.scale,
               11.0F * settingRenderContext.scale
            )
      );
      Render2D.processData51(
         local1824_8 + 6.67F * settingRenderContext.scale,
         local1824_9 + 116.33F * settingRenderContext.scale,
         151.33F * settingRenderContext.scale,
         11.0F * settingRenderContext.scale,
         2.0F * settingRenderContext.scale,
         UiTheme.computeInt(UiTheme.intVal, 0.02F + 0.03F * local1824_12)
      );
      Render2D.processData86(
         local1824_8 + 6.67F * settingRenderContext.scale,
         local1824_9 + 116.33F * settingRenderContext.scale,
         151.33F * settingRenderContext.scale,
         11.0F * settingRenderContext.scale,
         2.0F * settingRenderContext.scale,
         0.35F * settingRenderContext.scale,
         UiTheme.computeInt(UiTheme.intVal7, 0.06F + 0.14F * local1824_12)
      );
      FontText.processData3(
         FontFace.SEMI_BOLD,
         "Сохранить пресет",
         local1824_8
            + 6.67F * settingRenderContext.scale
            + (
                  151.33F * settingRenderContext.scale
                     - FontText.computeFloat(FontFace.SEMI_BOLD, "Сохранить пресет", 4.2F * settingRenderContext.scale)
               )
               / 2.0F,
         local1824_9 + 119.33F * settingRenderContext.scale,
         4.2F * settingRenderContext.scale,
         UiTheme.computeInt(UiTheme.intVal, 1.0F - 0.45F * local1824_12)
      );
      if (local1824_4) {
         settingRenderContext.processData7(
            local1824_8 + 6.67F * settingRenderContext.scale,
            local1824_9 + 116.33F * settingRenderContext.scale,
            151.33F * settingRenderContext.scale,
            11.0F * settingRenderContext.scale,
            null,
            () -> potionPreset.updateState2()
         );
      }

      this.processData9(settingRenderContext, potionPreset, local1824_8 + 172.0F * settingRenderContext.scale, local1824_9, local1824_4, local1824_5);
      if (potionPreset.intVal < 0 && !(settingRenderContext.computeFloat10("pc:dropdown", potionPreset.intVal >= 0) > 0.005F)) {
         potionPreset.floatValArray = null;
      } else {
         this.processData8(
            settingRenderContext,
            potionPreset,
            local1824_8 - 80.0F * settingRenderContext.scale,
            local1824_9 + 27.33F * settingRenderContext.scale,
            local1824_4 && potionPreset.intVal >= 0,
            local1824_5,
            settingRenderContext.computeFloat10("pc:dropdown", potionPreset.intVal >= 0)
         );
      }
   }

   public static PotionPresetPanel getInstance() {
      if (potionPresetPanel == null) {
         potionPresetPanel = new PotionPresetPanel();
      }

      return potionPresetPanel;
   }

   public static void processData2(float local1827_0, float local1827_1, float local1827_2, float local1827_3, float local1827_4, float local1827_5) {
      ShapeDrawing.processData4(
         local1827_0, local1827_1, local1827_2, local1827_3, computeFloat2(), CornerRadii.uniform(local1827_4), ShapeDrawing.TypeOption.LOW, true
      );
      Render2D.processData51(local1827_0, local1827_1, local1827_2, local1827_3, local1827_4, ClickGuiStyle.intVal2);
      Render2D.processData86(
         local1827_0,
         local1827_1,
         local1827_2,
         local1827_3,
         local1827_4,
         0.35F * local1827_5,
         UiTheme.computeInt(UiTheme.intVal7, 0.06F)
      );
   }

   public boolean matchesCriteria(float local1828_1, float local1828_2, double local1828_3, float local1828_5) {
      if (PotionPreset.getInstance().floatValArray != null
         && PotionPreset.getInstance().intVal >= 0
         && UiTheme.matchesCriteria(
            local1828_1,
            local1828_2,
            PotionPreset.getInstance().floatValArray[0],
            PotionPreset.getInstance().floatValArray[1],
            PotionPreset.getInstance().floatValArray[2],
            PotionPreset.getInstance().floatValArray[3]
         )) {
         PotionPreset.getInstance().smoothScroll.targetOffset = SettingRenderContext.computeFloat(
            PotionPreset.getInstance().smoothScroll.targetOffset - (float)local1828_3 * 14.0F * local1828_5,
            0.0F,
            PotionPreset.getInstance().smoothScroll.maximumOffset
         );
      } else if (PotionPreset.getInstance().floatValArray2 != null
         && UiTheme.matchesCriteria(
            local1828_1,
            local1828_2,
            PotionPreset.getInstance().floatValArray2[0],
            PotionPreset.getInstance().floatValArray2[1],
            PotionPreset.getInstance().floatValArray2[2],
            PotionPreset.getInstance().floatValArray2[3]
         )) {
         PotionPreset.getInstance().smoothScroll2.targetOffset = SettingRenderContext.computeFloat(
            PotionPreset.getInstance().smoothScroll2.targetOffset - (float)local1828_3 * 14.0F * local1828_5,
            0.0F,
            PotionPreset.getInstance().smoothScroll2.maximumOffset
         );
      }

      return true;
   }

   public static void processData3(
      SettingRenderContext settingRenderContext,
      float local1829_1,
      float local1829_2,
      float local1829_3,
      String local1829_4,
      boolean local1829_5,
      String local1829_6,
      String local1829_7,
      float local1829_8,
      float local1829_9
   ) {
      FontText.processData3(
         FontFace.SEMI_BOLD,
         local1829_4,
         local1829_5 ? local1829_1 + 8.0F * settingRenderContext.scale : local1829_1 + 6.67F * settingRenderContext.scale,
         local1829_2 + 6.33F * settingRenderContext.scale,
         4.2F * settingRenderContext.scale,
         UiTheme.computeInt(UiTheme.intVal, 0.1F)
      );
      FontText.processData3(
         FontFace.SEMI,
         local1829_6,
         local1829_1 + (local1829_5 ? 8.0F : 6.67F) * settingRenderContext.scale,
         local1829_2 + 15.33F * settingRenderContext.scale,
         5.33F * settingRenderContext.scale,
         UiTheme.intVal
      );
      float local1829_10 = local1829_2 + 23.33F * settingRenderContext.scale;

      for (String local1829_12 : UiTheme.getList(
         FontFace.SEMI, local1829_7, 4.2F * settingRenderContext.scale, local1829_8 * settingRenderContext.scale
      )) {
         FontText.processData3(
            FontFace.SEMI,
            local1829_12,
            local1829_1 + (local1829_5 ? 8.0F : 6.67F) * settingRenderContext.scale,
            local1829_10,
            4.2F * settingRenderContext.scale,
            UiTheme.computeInt(UiTheme.intVal, 0.35F)
         );
         local1829_10 += 5.0F * settingRenderContext.scale;
      }

      Render2D.processData51(
         local1829_1 + (local1829_5 ? 8.0F : 6.67F) * settingRenderContext.scale,
         local1829_2 + 40.0F * settingRenderContext.scale,
         local1829_9 * settingRenderContext.scale,
         0.33F * settingRenderContext.scale,
         0.0F,
         UiTheme.computeInt(UiTheme.intVal, 0.03F)
      );
   }

   public void updateState() {
      this.flag = false;
      PotionPreset.getInstance().intVal = -1;
      PotionPreset.getInstance().updateState();
   }

   private void processData4(
      SettingRenderContext settingRenderContext,
      KeybindSetting newKeybindSetting,
      float local1831_3,
      float local1831_4,
      float local1831_5,
      boolean local1831_6,
      String local1831_7
   ) {
      boolean local1831_8 = settingRenderContext.keybindSetting == newKeybindSetting;
      Render2D.processData89(
         !newKeybindSetting.isBound() && !local1831_8
            ? "assets/horus/textures/gui/presets/keyboard_dim.png"
            : "assets/horus/textures/gui/presets/keyboard_on.png",
         local1831_3,
         local1831_4,
         3.33F * settingRenderContext.scale,
         3.33F * settingRenderContext.scale,
         0.0F,
         UiTheme.computeInt(UiTheme.intVal, 1.0F)
      );
      Render2D.renderElement2(
         settingRenderContext.drawContext, local1831_3, local1831_4 - 2.0F * settingRenderContext.scale, local1831_5 * settingRenderContext.scale, 8.0F * settingRenderContext.scale
      );
      FontText.processData3(
         FontFace.SEMI_BOLD,
         local1831_8 ? "..." : (newKeybindSetting.isBound() ? newKeybindSetting.getKeyName().toUpperCase() : "Press key"),
         local1831_3 + 5.67F * settingRenderContext.scale,
         local1831_4 - 0.33F * settingRenderContext.scale,
         4.0F * settingRenderContext.scale,
         !local1831_8 && !newKeybindSetting.isBound()
            ? UiTheme.computeInt(
               UiTheme.intVal,
               0.2F
                  + 0.3F
                     * settingRenderContext.computeFloat2(
                        local1831_7,
                        local1831_6
                           && UiTheme.matchesCriteria(
                              settingRenderContext.mouseX,
                              settingRenderContext.mouseY,
                              local1831_3 - 1.0F * settingRenderContext.scale,
                              local1831_4 - 2.0F * settingRenderContext.scale,
                              local1831_5 * settingRenderContext.scale,
                              8.0F * settingRenderContext.scale
                           )
                     )
            )
            : UiTheme.intVal7
      );
      Render2D.renderElement(settingRenderContext.drawContext);
      if (local1831_6) {
         settingRenderContext.processData6(
            local1831_3 - 1.0F * settingRenderContext.scale,
            local1831_4 - 2.0F * settingRenderContext.scale,
            local1831_5 * settingRenderContext.scale,
            8.0F * settingRenderContext.scale,
            null,
            () -> settingRenderContext.keybindSetting = newKeybindSetting,
            () -> newKeybindSetting.withKeyCode(-1)
         );
      }
   }

   public static void processData5(
      SettingRenderContext settingRenderContext,
      TextSetting newTextSetting,
      float local1833_2,
      float local1833_3,
      float local1833_4,
      float local1833_5,
      boolean local1833_6,
      String local1833_7
   ) {
      Render2D.processData51(
         local1833_2, local1833_3, local1833_4, local1833_5, 2.0F * settingRenderContext.scale, UiTheme.computeInt(UiTheme.intVal7, 0.05F)
      );
      Render2D.processData86(
         local1833_2,
         local1833_3,
         local1833_4,
         local1833_5,
         2.0F * settingRenderContext.scale,
         0.35F * settingRenderContext.scale,
         UiTheme.computeInt(
            UiTheme.intVal7, 0.06F + 0.13F * settingRenderContext.computeFloat2(local1833_7, settingRenderContext.textSetting == newTextSetting)
         )
      );
      Render2D.renderElement2(
         settingRenderContext.drawContext, local1833_2 + 2.5F * settingRenderContext.scale, local1833_3, local1833_4 - 5.5F * settingRenderContext.scale, local1833_5
      );
      FontText.processData3(
         FontFace.SEMI,
         (newTextSetting.getValue().isEmpty() && settingRenderContext.textSetting != newTextSetting ? newTextSetting.getPlaceholder() : newTextSetting.getValue())
            + (settingRenderContext.textSetting == newTextSetting ? SettingRenderContext.formatText() : ""),
         local1833_2 + 3.33F * settingRenderContext.scale,
         local1833_3 + local1833_5 / 2.0F - 1.67F * settingRenderContext.scale,
         3.33F * settingRenderContext.scale,
         newTextSetting.getValue().isEmpty() && settingRenderContext.textSetting != newTextSetting
            ? UiTheme.computeInt(UiTheme.intVal, 0.24F)
            : UiTheme.intVal
      );
      Render2D.renderElement(settingRenderContext.drawContext);
      if (local1833_6) {
         settingRenderContext.processData7(local1833_2, local1833_3, local1833_4, local1833_5, null, () -> {
            settingRenderContext.textSetting = newTextSetting;
            newTextSetting.moveToEnd(false);
         });
      }
   }

   public static void processData6(SettingRenderContext settingRenderContext, float local1835_1, float local1835_2) {
      Render2D.processData89(
         "assets/horus/textures/gui/presets/logo.png",
         local1835_1 / 2.0F
            - (8.0F + FontText.computeFloat(FontFace.BOLD, "Horus Client", 3.43F * settingRenderContext.scale) / settingRenderContext.scale)
               * settingRenderContext.scale
               / 2.0F,
         local1835_2 - 23.0F * settingRenderContext.scale,
         5.33F * settingRenderContext.scale,
         4.33F * settingRenderContext.scale,
         0.0F,
         UiTheme.computeInt(UiTheme.intVal, 1.0F)
      );
      FontText.processData3(
         FontFace.BOLD,
         "Horus Client",
         local1835_1 / 2.0F
            - (8.0F + FontText.computeFloat(FontFace.BOLD, "Horus Client", 3.43F * settingRenderContext.scale) / settingRenderContext.scale)
               * settingRenderContext.scale
               / 2.0F
            + 8.0F * settingRenderContext.scale,
         local1835_2 - 22.7F * settingRenderContext.scale,
         3.43F * settingRenderContext.scale,
         UiTheme.intVal7
      );
      FontText.processData3(
         FontFace.SEMI,
         "Beta 1.21.11",
         local1835_1 / 2.0F
            - (8.0F + FontText.computeFloat(FontFace.BOLD, "Horus Client", 3.43F * settingRenderContext.scale) / settingRenderContext.scale)
               * settingRenderContext.scale
               / 2.0F
            + 9.0F * settingRenderContext.scale,
         local1835_2 - 17.3F * settingRenderContext.scale,
         3.43F * settingRenderContext.scale,
         UiTheme.computeInt(UiTheme.intVal7, 0.4F)
      );
   }

   private PotionPresetPanel() {
   }

   public void updateState2() {
      if (!this.flag) {
         this.flag = true;
         this.timeMillis = System.currentTimeMillis();
      }
   }

   public float computeFloat() {
      this.animatedValue.processData(this.flag ? 1.0 : 0.0);
      return (float)this.animatedValue.computeDouble();
   }

   public void processData7(SettingRenderContext settingRenderContext, float local1841_2, float local1841_3, float local1841_4, float local1841_5) {
      if (!(local1841_4 < 0.005F)) {
         settingRenderContext.intVal = settingRenderContext.list.size();
         Render2D.processData51(0.0F, 0.0F, local1841_2, local1841_3, 0.0F, UiTheme.computeInt(ClickGuiStyle.intVal41, local1841_4));
         if (this.flag && local1841_4 > 0.6F) {
            settingRenderContext.processData7(0.0F, 0.0F, local1841_2, local1841_3, null, () -> this.updateState());
         }

         Render2D.processData46(local1841_4);
         this.processData(
            settingRenderContext, local1841_2, local1841_3, this.flag && local1841_4 > 0.6F, local1841_5, (1.0F - local1841_4) * 12.0F * settingRenderContext.scale
         );
         Render2D.updateState();
      }
   }

   public static float computeFloat2() {
      return (Render2D.computeInt(-16777216) >>> 24 & 0xFF) / 255.0F;
   }

   public boolean checkState() {
      return this.flag;
   }

   private void processData8(
      SettingRenderContext settingRenderContext,
      PotionPreset potionPreset,
      float local1846_3,
      float local1846_4,
      boolean local1846_5,
      float local1846_6,
      float local1846_7
   ) {
      local1846_5 = local1846_5 && local1846_7 > 0.6F;
      local1846_3 -= (1.0F - local1846_7) * 8.0F * settingRenderContext.scale;
      Render2D.processData46(local1846_7);
      if (local1846_5) {
         settingRenderContext.processData7(0.0F, 0.0F, 99999.0F, 99999.0F, null, () -> potionPreset.intVal = -1);
      }

      float local1846_8 = 74.0F * settingRenderContext.scale;
      float local1846_9 = 78.67F * settingRenderContext.scale;
      processData2(local1846_3, local1846_4, local1846_8, local1846_9, 3.0F * settingRenderContext.scale, settingRenderContext.scale);
      Render2D.processData51(
         local1846_3, local1846_4, local1846_8, local1846_9, 3.0F * settingRenderContext.scale, UiTheme.computeInt(UiTheme.intVal7, 0.05F)
      );
      if (local1846_5) {
         settingRenderContext.processData7(local1846_3, local1846_4, local1846_8, local1846_9, null, () -> {});
      }

      FontText.processData3(
         FontFace.SEMI_BOLD,
         "Ваши зелья",
         local1846_3 + 5.5F * settingRenderContext.scale,
         local1846_4 + 5.0F * settingRenderContext.scale,
         2.8F * settingRenderContext.scale,
         UiTheme.computeInt(UiTheme.intVal, 0.1F)
      );
      FontText.processData3(
         FontFace.SEMI,
         "Текущие зелья",
         local1846_3 + 5.5F * settingRenderContext.scale,
         local1846_4 + 10.0F * settingRenderContext.scale,
         4.2F * settingRenderContext.scale,
         UiTheme.intVal
      );
      processData5(
         settingRenderContext,
         potionPreset.textSetting2,
         local1846_3 + 5.5F * settingRenderContext.scale,
         local1846_4 + 18.33F * settingRenderContext.scale,
         63.0F * settingRenderContext.scale,
         9.0F * settingRenderContext.scale,
         local1846_5,
         "pc:psearch"
      );
      Render2D.processData51(
         local1846_3 + 5.5F * settingRenderContext.scale,
         local1846_4 + 32.33F * settingRenderContext.scale,
         63.0F * settingRenderContext.scale,
         0.33F * settingRenderContext.scale,
         0.0F,
         UiTheme.computeInt(UiTheme.intVal, 0.03F)
      );
      ArrayList<ServerHelperModule.SubContext> local1846_10 = potionPreset.getArrayList();
      float local1846_11 = local1846_4 + 36.33F * settingRenderContext.scale;
      float local1846_12 = local1846_9 - 40.0F * settingRenderContext.scale;
      potionPreset.floatValArray = new float[]{local1846_3, local1846_4, local1846_8, local1846_9};
      potionPreset.smoothScroll.maximumOffset = Math.max(0.0F, local1846_10.size() * 7.33F * settingRenderContext.scale - local1846_12);
      potionPreset.smoothScroll.update(local1846_6);
      if (local1846_10.isEmpty()) {
         FontText.processData3(
            FontFace.SEMI,
            "Зелий нет",
            local1846_3 + 12.0F * settingRenderContext.scale,
            local1846_11 + 2.0F * settingRenderContext.scale,
            3.67F * settingRenderContext.scale,
            UiTheme.computeInt(UiTheme.intVal, 0.2F)
         );
      }

      Render2D.renderElement2(
         settingRenderContext.drawContext, local1846_3 + 2.0F * settingRenderContext.scale, local1846_11, local1846_8 - 4.0F * settingRenderContext.scale, local1846_12
      );
      float[] floatArray = new float[]{local1846_3 + 2.0F * settingRenderContext.scale, local1846_11, local1846_8 - 4.0F * settingRenderContext.scale, local1846_12};

      for (int index = 0; index < local1846_10.size(); index++) {
         float local1846_15 = local1846_11 + index * 7.33F * settingRenderContext.scale - potionPreset.smoothScroll.getPixelAlignedOffset();
         if (!(local1846_15 + 6.0F * settingRenderContext.scale < local1846_11 - 2.0F) && !(local1846_15 > local1846_11 + local1846_12 + 2.0F)) {
            ServerHelperModule.SubContext local1846_16 = local1846_10.get(index);
            float local1846_17 = settingRenderContext.computeFloat2(
               "pc:drow:" + index,
               local1846_5
                  && UiTheme.matchesCriteria(
                     settingRenderContext.mouseX,
                     settingRenderContext.mouseY,
                     local1846_3 + 3.5F * settingRenderContext.scale,
                     local1846_15 - 1.0F * settingRenderContext.scale,
                     local1846_8 - 9.5F * settingRenderContext.scale,
                     7.33F * settingRenderContext.scale
                  )
                  && UiTheme.matchesCriteria(settingRenderContext.mouseX, settingRenderContext.mouseY, local1846_3, local1846_11, local1846_8, local1846_12)
            );
            if (local1846_17 > 0.005F) {
               Render2D.processData51(
                  local1846_3 + 3.5F * settingRenderContext.scale,
                  local1846_15 - 1.0F * settingRenderContext.scale,
                  local1846_8 - 9.5F * settingRenderContext.scale,
                  7.0F * settingRenderContext.scale,
                  2.0F * settingRenderContext.scale,
                  UiTheme.computeInt(UiTheme.intVal7, 0.08F * local1846_17)
               );
            }

            ItemRenderCache.processData(
               PotionPreset.getItemStack(local1846_16.getKeybindSetting().name),
               local1846_3 + 5.5F * settingRenderContext.scale,
               local1846_15,
               4.67F * settingRenderContext.scale,
               ItemDrawOptions.noDecorations(computeFloat2())
            );
            Render2D.renderElement2(
               settingRenderContext.drawContext,
               local1846_3 + 12.5F * settingRenderContext.scale,
               local1846_15 - 1.0F * settingRenderContext.scale,
               local1846_8 - 19.5F * settingRenderContext.scale,
               7.33F * settingRenderContext.scale
            );
            float var10002 = local1846_3 + 12.5F * settingRenderContext.scale;
            float var10003 = local1846_15 + 0.33F * settingRenderContext.scale;
            float var10004 = 3.67F * settingRenderContext.scale;
            FontText.processData3(
               FontFace.SEMI, local1846_16.getKeybindSetting().name, var10002, var10003, var10004, UiTheme.intVal
            );
            Render2D.renderElement(settingRenderContext.drawContext);
            if (local1846_5) {
               settingRenderContext.processData7(
                  local1846_3 + 3.5F * settingRenderContext.scale,
                  local1846_15 - 1.0F * settingRenderContext.scale,
                  local1846_8 - 9.5F * settingRenderContext.scale,
                  7.33F * settingRenderContext.scale,
                  floatArray,
                  () -> potionPreset.processData(
                     potionPreset.intVal < 0 ? potionPreset.computeInt() : potionPreset.intVal, local1846_16.getKeybindSetting().name
                  )
               );
            }
         }
      }

      Render2D.renderElement(settingRenderContext.drawContext);
      if (potionPreset.smoothScroll.maximumOffset > 0.0F) {
         Render2D.processData51(
            local1846_3 + 67.67F * settingRenderContext.scale,
            local1846_11 + (local1846_12 - 11.0F * settingRenderContext.scale) * (potionPreset.smoothScroll.getPixelAlignedOffset() / potionPreset.smoothScroll.maximumOffset),
            0.67F * settingRenderContext.scale,
            11.0F * settingRenderContext.scale,
            3.0F * settingRenderContext.scale,
            UiTheme.computeInt(UiTheme.intVal7, 0.35F)
         );
      }

      Render2D.updateState();
   }

   public boolean checkState2() {
      if (PotionPreset.getInstance().intVal >= 0) {
         PotionPreset.getInstance().intVal = -1;
         return true;
      }

      if (!this.flag) {
         return false;
      }

      this.flag = false;
      PotionPreset.getInstance().updateState();
      return true;
   }

   private void processData9(
      SettingRenderContext settingRenderContext, PotionPreset potionPreset, float local1849_3, float local1849_4, boolean local1849_5, float local1849_6
   ) {
      float local1849_7 = local1849_4 + 45.0F * settingRenderContext.scale;
      float local1849_8 = 133.33F * settingRenderContext.scale - 47.0F * settingRenderContext.scale;
      potionPreset.floatValArray2 = new float[]{local1849_3, local1849_7, 91.0F * settingRenderContext.scale, local1849_8};
      potionPreset.smoothScroll2.maximumOffset = Math.max(0.0F, potionPreset.list.size() * 16.0F * settingRenderContext.scale - local1849_8);
      potionPreset.smoothScroll2.update(local1849_6);
      if (potionPreset.list.isEmpty()) {
         FontText.processData3(
            FontFace.SEMI,
            "Пресетов пока нет",
            local1849_3
               + (91.0F * settingRenderContext.scale - FontText.computeFloat(FontFace.SEMI, "Пресетов пока нет", 4.0F * settingRenderContext.scale))
                  / 2.0F,
            local1849_7 + 8.0F * settingRenderContext.scale,
            4.0F * settingRenderContext.scale,
            UiTheme.computeInt(UiTheme.intVal, 0.2F)
         );
      }

      Render2D.renderElement2(
         settingRenderContext.drawContext, local1849_3 + 2.0F * settingRenderContext.scale, local1849_7, 89.0F * settingRenderContext.scale, local1849_8
      );
      RoundedClipStack.processData(
         local1849_3 + 2.0F * settingRenderContext.scale,
         local1849_7,
         89.0F * settingRenderContext.scale,
         local1849_8,
         2.0F * settingRenderContext.scale,
         2.0F * settingRenderContext.scale,
         2.0F * settingRenderContext.scale,
         2.0F * settingRenderContext.scale
      );
      float[] floatArray = new float[]{local1849_3 + 2.0F * settingRenderContext.scale, local1849_7, 89.0F * settingRenderContext.scale, local1849_8};

      for (int index = 0; index < potionPreset.list.size(); index++) {
         PotionPreset.SubContext local1849_11 = potionPreset.list.get(index);
         float local1849_12 = local1849_7 + index * 16.0F * settingRenderContext.scale - potionPreset.smoothScroll2.getPixelAlignedOffset();
         if (!(local1849_12 + 15.5F * settingRenderContext.scale < local1849_7 - 2.0F) && !(local1849_12 > local1849_7 + local1849_8 + 2.0F)) {
            FontText.processData3(
               FontFace.SEMI,
               local1849_11.strVal,
               local1849_3 + 8.0F * settingRenderContext.scale,
               local1849_12,
               3.8F * settingRenderContext.scale,
               UiTheme.computeInt(UiTheme.intVal, local1849_11.flag ? 1.0F : 0.5F)
            );
            float local1849_13 = local1849_12 + 5.5F * settingRenderContext.scale;
            float local1849_14 = local1849_11.list2.size() <= 1 ? 8.5F : Math.min(8.5F, 29.0F / (Math.min(5, local1849_11.list2.size()) - 1));

            for (int index2 = 0; index2 < Math.min(5, local1849_11.list2.size()); index2++) {
               ItemRenderCache.processData(
                  local1849_11.list2.get(index2).getItemStack2(),
                  local1849_3 + 8.0F * settingRenderContext.scale + index2 * local1849_14 * settingRenderContext.scale,
                  local1849_13 + 0.83F * settingRenderContext.scale,
                  7.0F * settingRenderContext.scale,
                  ItemDrawOptions.noDecorations(computeFloat2() * (local1849_11.flag ? 1.0F : 0.4F))
               );
            }

            this.processData4(
               settingRenderContext,
               local1849_11.keybindSetting,
               local1849_3 + (18.0F + (Math.min(5, local1849_11.list2.size()) - 1) * local1849_14) * settingRenderContext.scale,
               local1849_13 + 2.0F * settingRenderContext.scale,
               46.0F - (Math.min(5, local1849_11.list2.size()) - 1) * local1849_14,
               local1849_5,
               "pc:prb:" + index
            );
            float local1849_15 = local1849_3 + 64.67F * settingRenderContext.scale;
            float local1849_16 = local1849_3 + 74.67F * settingRenderContext.scale;
            Render2D.processData51(
               local1849_15,
               local1849_13,
               8.67F * settingRenderContext.scale,
               8.67F * settingRenderContext.scale,
               2.0F * settingRenderContext.scale,
               UiTheme.computeInt(UiTheme.intVal7, 0.05F)
            );
            Render2D.processData86(
               local1849_15,
               local1849_13,
               8.67F * settingRenderContext.scale,
               8.67F * settingRenderContext.scale,
               2.0F * settingRenderContext.scale,
               0.35F * settingRenderContext.scale,
               UiTheme.computeInt(
                  UiTheme.intVal7,
                  0.06F
                     + 0.13F
                        * settingRenderContext.computeFloat2(
                           "pc:tgh:" + index,
                           local1849_5
                              && UiTheme.matchesCriteria(
                                 settingRenderContext.mouseX,
                                 settingRenderContext.mouseY,
                                 local1849_15,
                                 local1849_13,
                                 8.67F * settingRenderContext.scale,
                                 8.67F * settingRenderContext.scale
                              )
                        )
               )
            );
            settingRenderContext.processData4(
               local1849_11.flag ? "O" : "N",
               local1849_15 + 4.33F * settingRenderContext.scale,
               local1849_13 + 4.33F * settingRenderContext.scale,
               4.5F * settingRenderContext.scale,
               local1849_11.flag
                  ? UiTheme.intVal7
                  : UiTheme.computeInt2(
                     UiTheme.intVal3,
                     UiTheme.intVal,
                     settingRenderContext.computeFloat2(
                        "pc:tgh:" + index,
                        local1849_5
                           && UiTheme.matchesCriteria(
                              settingRenderContext.mouseX,
                              settingRenderContext.mouseY,
                              local1849_15,
                              local1849_13,
                              8.67F * settingRenderContext.scale,
                              8.67F * settingRenderContext.scale
                           )
                     )
                  )
            );
            Render2D.processData51(
               local1849_16,
               local1849_13,
               8.67F * settingRenderContext.scale,
               8.67F * settingRenderContext.scale,
               2.0F * settingRenderContext.scale,
               UiTheme.computeInt(UiTheme.intVal7, 0.05F)
            );
            Render2D.processData86(
               local1849_16,
               local1849_13,
               8.67F * settingRenderContext.scale,
               8.67F * settingRenderContext.scale,
               2.0F * settingRenderContext.scale,
               0.35F * settingRenderContext.scale,
               UiTheme.computeInt(
                  UiTheme.intVal7,
                  0.06F
                     + 0.13F
                        * settingRenderContext.computeFloat2(
                           "pc:trh:" + index,
                           local1849_5
                              && UiTheme.matchesCriteria(
                                 settingRenderContext.mouseX,
                                 settingRenderContext.mouseY,
                                 local1849_16,
                                 local1849_13,
                                 8.67F * settingRenderContext.scale,
                                 8.67F * settingRenderContext.scale
                              )
                        )
               )
            );
            settingRenderContext.processData4(
               "P",
               local1849_16 + 4.33F * settingRenderContext.scale,
               local1849_13 + 4.33F * settingRenderContext.scale,
               4.5F * settingRenderContext.scale,
               UiTheme.computeInt2(
                  UiTheme.intVal3,
                  UiTheme.intVal,
                  settingRenderContext.computeFloat2(
                     "pc:trh:" + index,
                     local1849_5
                        && UiTheme.matchesCriteria(
                           settingRenderContext.mouseX,
                           settingRenderContext.mouseY,
                           local1849_16,
                           local1849_13,
                           8.67F * settingRenderContext.scale,
                           8.67F * settingRenderContext.scale
                        )
                  )
               )
            );
            int index3 = index;
            if (local1849_5) {
               settingRenderContext.processData7(local1849_15, local1849_13, 8.67F * settingRenderContext.scale, 8.67F * settingRenderContext.scale, floatArray, () -> {
                  local1849_11.flag = !local1849_11.flag;
                  potionPreset.updateState();
               });
            }

            if (local1849_5) {
               settingRenderContext.processData7(local1849_16, local1849_13, 8.67F * settingRenderContext.scale, 8.67F * settingRenderContext.scale, floatArray, () -> {
                  potionPreset.list.remove(index3);
                  potionPreset.updateState();
               });
            }
         }
      }

      RoundedClipStack.updateState2();
      Render2D.renderElement(settingRenderContext.drawContext);
   }
}

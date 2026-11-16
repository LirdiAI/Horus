package horus.client.ui.panel;

import horus.client.util.AuctionItemCatalog;
import horus.client.storage.AutoBuyItem;
import horus.client.ui.ClickGuiStyle;
import horus.client.render.FontText;
import horus.client.render.ItemRenderCache;
import horus.client.render.RoundedClipStack;
import horus.client.ui.setting.SettingRenderContext;
import horus.client.ui.UiTheme;

import horus.client.ui.AutoBuySettingsScreen;
import horus.client.render.FontFace;
import horus.client.util.ItemDrawOptions;
import horus.client.render.Render2D;
import horus.client.setting.TextSetting;

import java.util.ArrayList;

final class AutoBuyItemPanel {
   private static final String[] strValArray = new String[]{"500K", "250K", "100K", "50K", "0"};
   private static String strVal;

   static void processData(
      SettingRenderContext settingRenderContext,
      AutoBuySettingsScreen autoBuySettingsScreen,
      AutoBuyPanel autoBuyPanel,
      float local1519_3,
      float local1519_4,
      float local1519_5,
      float local1519_6,
      boolean local1519_7,
      long local1519_8
   ) {
      settingRenderContext.processData12(
         local1519_3, local1519_4, local1519_5, local1519_6, 4.0F * settingRenderContext.scale, ClickGuiStyle.intVal47, UiTheme.intVal17
      );
      boolean local1519_10 = autoBuySettingsScreen.strVal != null && autoBuySettingsScreen.map2.get(autoBuySettingsScreen.strVal) != null;
      if (local1519_10) {
         strVal = autoBuySettingsScreen.strVal;
      }

      FontText.processData3(
         FontFace.SEMI,
         "Цены",
         local1519_3 + 7.0F * settingRenderContext.scale,
         local1519_4 + 7.0F * settingRenderContext.scale,
         5.5F * settingRenderContext.scale,
         UiTheme.intVal
      );
      Render2D.processData46(settingRenderContext.computeFloat2("ab:pricehint", !local1519_10));
      FontText.processData3(
         FontFace.SEMI,
         "для всех предметов",
         local1519_3
            + 7.0F * settingRenderContext.scale
            + FontText.computeFloat(FontFace.SEMI, "Цены", 5.5F * settingRenderContext.scale)
            + 4.0F * settingRenderContext.scale,
         local1519_4 + 7.5F * settingRenderContext.scale,
         5.0F * settingRenderContext.scale,
         UiTheme.intVal3
      );
      Render2D.updateState();
      processData9(
         settingRenderContext,
         autoBuySettingsScreen,
         autoBuyPanel,
         local1519_3,
         local1519_4 - 1.5F * settingRenderContext.scale,
         local1519_5 + 1.0F * settingRenderContext.scale,
         local1519_7,
         local1519_10
      );
      float local1519_11 = AutoBuyPanel.computeFloat(local1519_8 - autoBuySettingsScreen.timeMillis2, 0.0F, 0.16F);
      float local1519_12 = 92.0F * settingRenderContext.scale;
      float local1519_13 = 13.0F * settingRenderContext.scale;
      float local1519_14 = local1519_3 + local1519_5 - 7.0F * settingRenderContext.scale - local1519_12;
      Render2D.processData46(local1519_11);
      FontText.processData3(
         FontFace.SEMI,
         "Покупка до",
         local1519_3 + 7.0F * settingRenderContext.scale,
         local1519_4 + 24.0F * settingRenderContext.scale + 3.75F * settingRenderContext.scale,
         5.5F * settingRenderContext.scale,
         UiTheme.intVal2
      );
      processData7(
         settingRenderContext,
         autoBuySettingsScreen,
         local1519_10 ? autoBuySettingsScreen.map2.get(autoBuySettingsScreen.strVal).nested03744 : autoBuySettingsScreen.nested0374,
         local1519_10 ? "item:buy:" + autoBuySettingsScreen.strVal : "buyMax",
         "$",
         null,
         local1519_14,
         local1519_4 + 24.0F * settingRenderContext.scale + (1.0F - local1519_11) * 2.0F * settingRenderContext.scale,
         local1519_12,
         local1519_13,
         local1519_7,
         local1519_8
      );
      FontText.processData3(
         FontFace.SEMI,
         "Продажа от",
         local1519_3 + 7.0F * settingRenderContext.scale,
         local1519_4 + 42.0F * settingRenderContext.scale + 3.75F * settingRenderContext.scale,
         5.5F * settingRenderContext.scale,
         UiTheme.intVal2
      );
      processData7(
         settingRenderContext,
         autoBuySettingsScreen,
         local1519_10 ? autoBuySettingsScreen.map2.get(autoBuySettingsScreen.strVal).nested03745 : autoBuySettingsScreen.nested03742,
         local1519_10 ? "item:sell:" + autoBuySettingsScreen.strVal : "sellMin",
         "$",
         null,
         local1519_14,
         local1519_4 + 42.0F * settingRenderContext.scale + (1.0F - local1519_11) * 2.0F * settingRenderContext.scale,
         local1519_12,
         local1519_13,
         local1519_7,
         local1519_8
      );
      Render2D.updateState();
      FontText.processData3(
         FontFace.SEMI,
         "Прочность от",
         local1519_3 + 7.0F * settingRenderContext.scale,
         local1519_4 + 60.0F * settingRenderContext.scale + 3.75F * settingRenderContext.scale,
         5.5F * settingRenderContext.scale,
         UiTheme.intVal2
      );
      processData7(
         settingRenderContext,
         autoBuySettingsScreen,
         autoBuySettingsScreen.nested03743,
         "durability",
         null,
         "%",
         local1519_14,
         local1519_4 + 60.0F * settingRenderContext.scale,
         local1519_12,
         local1519_13,
         local1519_7,
         local1519_8
      );
   }

   static void processData2(
      SettingRenderContext settingRenderContext,
      AutoBuySettingsScreen autoBuySettingsScreen,
      AutoBuyPanel autoBuyPanel,
      float local1520_3,
      float local1520_4,
      float local1520_5,
      float local1520_6,
      boolean local1520_7,
      long local1520_8
   ) {
      settingRenderContext.processData9(
         local1520_3, local1520_4, local1520_5, local1520_6, 4.0F * settingRenderContext.scale, ClickGuiStyle.intVal47, UiTheme.intVal17
      );
      FontText.processData3(
         FontFace.SEMI,
         "Pricing Configuration",
         local1520_3 + 8.0F * settingRenderContext.scale,
         local1520_4 + 8.0F * settingRenderContext.scale,
         5.0F * settingRenderContext.scale,
         UiTheme.intVal3
      );
      if (autoBuySettingsScreen.strVal != null && autoBuySettingsScreen.map2.get(autoBuySettingsScreen.strVal) != null) {
         strVal = autoBuySettingsScreen.strVal;
      }

      processData9(
         settingRenderContext,
         autoBuySettingsScreen,
         autoBuyPanel,
         local1520_3,
         local1520_4,
         local1520_5,
         local1520_7,
         autoBuySettingsScreen.strVal != null && autoBuySettingsScreen.map2.get(autoBuySettingsScreen.strVal) != null
      );
      processData10(
         settingRenderContext,
         "Auto Buy Speed",
         "Select scan frequency and execution speed",
         local1520_3 + 8.0F * settingRenderContext.scale,
         local1520_4 + 22.0F * settingRenderContext.scale,
         86.0F * settingRenderContext.scale
      );
      processData8(
         settingRenderContext,
         autoBuySettingsScreen,
         local1520_3 + 98.0F * settingRenderContext.scale,
         local1520_4 + 25.0F * settingRenderContext.scale,
         local1520_5 - 106.0F * settingRenderContext.scale,
         local1520_7
      );
      Render2D.processData46(AutoBuyPanel.computeFloat(local1520_8 - autoBuySettingsScreen.timeMillis2, 0.0F, 0.16F));
      processData10(
         settingRenderContext,
         "Buy Price (Max)",
         "Maximum price limit for automatic purchases",
         local1520_3 + 8.0F * settingRenderContext.scale,
         local1520_4
            + 48.0F * settingRenderContext.scale
            + (1.0F - AutoBuyPanel.computeFloat(local1520_8 - autoBuySettingsScreen.timeMillis2, 0.0F, 0.16F)) * 2.0F * settingRenderContext.scale,
         86.0F * settingRenderContext.scale
      );
      processData7(
         settingRenderContext,
         autoBuySettingsScreen,
         autoBuySettingsScreen.strVal != null && autoBuySettingsScreen.map2.get(autoBuySettingsScreen.strVal) != null
            ? autoBuySettingsScreen.map2.get(autoBuySettingsScreen.strVal).nested03744
            : autoBuySettingsScreen.nested0374,
         autoBuySettingsScreen.strVal != null && autoBuySettingsScreen.map2.get(autoBuySettingsScreen.strVal) != null ? "item:buy:" + autoBuySettingsScreen.strVal : "buyMax",
         "$",
         null,
         local1520_3 + 98.0F * settingRenderContext.scale,
         local1520_4
            + 51.0F * settingRenderContext.scale
            + (1.0F - AutoBuyPanel.computeFloat(local1520_8 - autoBuySettingsScreen.timeMillis2, 0.0F, 0.16F)) * 2.0F * settingRenderContext.scale,
         local1520_5 - 106.0F * settingRenderContext.scale,
         14.0F * settingRenderContext.scale,
         local1520_7,
         local1520_8
      );
      Render2D.updateState();
      Render2D.processData46(AutoBuyPanel.computeFloat(local1520_8 - autoBuySettingsScreen.timeMillis2, 0.04F, 0.16F));
      processData10(
         settingRenderContext,
         "Sell Price (Min)",
         "Minimum listing price for automatic reselling",
         local1520_3 + 8.0F * settingRenderContext.scale,
         local1520_4
            + 74.0F * settingRenderContext.scale
            + (1.0F - AutoBuyPanel.computeFloat(local1520_8 - autoBuySettingsScreen.timeMillis2, 0.04F, 0.16F)) * 2.0F * settingRenderContext.scale,
         86.0F * settingRenderContext.scale
      );
      processData7(
         settingRenderContext,
         autoBuySettingsScreen,
         autoBuySettingsScreen.strVal != null && autoBuySettingsScreen.map2.get(autoBuySettingsScreen.strVal) != null
            ? autoBuySettingsScreen.map2.get(autoBuySettingsScreen.strVal).nested03745
            : autoBuySettingsScreen.nested03742,
         autoBuySettingsScreen.strVal != null && autoBuySettingsScreen.map2.get(autoBuySettingsScreen.strVal) != null ? "item:sell:" + autoBuySettingsScreen.strVal : "sellMin",
         "$",
         null,
         local1520_3 + 98.0F * settingRenderContext.scale,
         local1520_4
            + 77.0F * settingRenderContext.scale
            + (1.0F - AutoBuyPanel.computeFloat(local1520_8 - autoBuySettingsScreen.timeMillis2, 0.04F, 0.16F)) * 2.0F * settingRenderContext.scale,
         local1520_5 - 106.0F * settingRenderContext.scale,
         14.0F * settingRenderContext.scale,
         local1520_7,
         local1520_8
      );
      Render2D.updateState();
      processData10(
         settingRenderContext,
         "Minimum Durability",
         "Items below this durability will be ignored",
         local1520_3 + 8.0F * settingRenderContext.scale,
         local1520_4 + 100.0F * settingRenderContext.scale,
         86.0F * settingRenderContext.scale
      );
      processData7(
         settingRenderContext,
         autoBuySettingsScreen,
         autoBuySettingsScreen.nested03743,
         "durability",
         null,
         "%",
         local1520_3 + 98.0F * settingRenderContext.scale,
         local1520_4 + 103.0F * settingRenderContext.scale,
         local1520_5 - 106.0F * settingRenderContext.scale,
         14.0F * settingRenderContext.scale,
         local1520_7,
         local1520_8
      );
   }

   static void processData3(
      SettingRenderContext settingRenderContext,
      AutoBuySettingsScreen autoBuySettingsScreen,
      float local1521_2,
      float local1521_3,
      float local1521_4,
      float local1521_5,
      boolean local1521_6
   ) {
      settingRenderContext.processData12(
         local1521_2,
         local1521_3,
         local1521_4,
         local1521_5,
         4.0F * settingRenderContext.scale,
         ClickGuiStyle.intVal47,
         UiTheme.computeInt2(
            UiTheme.intVal17,
            UiTheme.intVal18,
            settingRenderContext.computeFloat2("ab:searchf", settingRenderContext.textSetting == autoBuySettingsScreen.textSetting)
         )
      );
      settingRenderContext.processData4(
         "R",
         local1521_2 + 8.0F * settingRenderContext.scale + 2.75F * settingRenderContext.scale,
         local1521_3 + local1521_5 / 2.0F,
         5.5F * settingRenderContext.scale,
         UiTheme.computeInt2(
            UiTheme.intVal3,
            UiTheme.intVal2,
            settingRenderContext.computeFloat2("ab:searchf", settingRenderContext.textSetting == autoBuySettingsScreen.textSetting)
         )
      );
      FontText.processData3(
         FontFace.SEMI,
         (
               autoBuySettingsScreen.textSetting.getValue().isEmpty() && settingRenderContext.textSetting != autoBuySettingsScreen.textSetting
                  ? autoBuySettingsScreen.textSetting.getPlaceholder()
                  : autoBuySettingsScreen.textSetting.getValue()
            )
            + (settingRenderContext.textSetting == autoBuySettingsScreen.textSetting ? SettingRenderContext.formatText() : ""),
         local1521_2 + 8.0F * settingRenderContext.scale + 5.5F * settingRenderContext.scale + 4.0F * settingRenderContext.scale,
         local1521_3 + local1521_5 / 2.0F - 2.75F * settingRenderContext.scale,
         5.5F * settingRenderContext.scale,
         autoBuySettingsScreen.textSetting.getValue().isEmpty() ? UiTheme.intVal2 : UiTheme.intVal
      );
      if (local1521_6) {
         settingRenderContext.processData7(local1521_2, local1521_3, local1521_4, local1521_5, null, () -> {
            settingRenderContext.textSetting = autoBuySettingsScreen.textSetting;
            autoBuySettingsScreen.textSetting.moveToEnd(false);
         });
      }
   }

   static void processData4(
      SettingRenderContext settingRenderContext,
      AutoBuySettingsScreen autoBuySettingsScreen,
      float local1525_2,
      float local1525_3,
      float local1525_4,
      float local1525_5,
      boolean local1525_6,
      long local1525_7
   ) {
      settingRenderContext.processData12(
         local1525_2, local1525_3, local1525_4, local1525_5, 4.0F * settingRenderContext.scale, ClickGuiStyle.intVal47, UiTheme.intVal17
      );
      FontText.processData3(
         FontFace.SEMI,
         "Telegram",
         local1525_2 + 7.0F * settingRenderContext.scale,
         local1525_3 + 7.0F * settingRenderContext.scale,
         5.5F * settingRenderContext.scale,
         UiTheme.intVal
      );
      FontText.processData3(
         FontFace.SEMI,
         "уведомления о покупках",
         local1525_2
            + 7.0F * settingRenderContext.scale
            + FontText.computeFloat(FontFace.SEMI, "Telegram", 5.5F * settingRenderContext.scale)
            + 4.0F * settingRenderContext.scale,
         local1525_3 + 7.5F * settingRenderContext.scale,
         5.0F * settingRenderContext.scale,
         UiTheme.intVal3
      );
      float local1525_9 = 92.0F * settingRenderContext.scale;
      float local1525_10 = 13.0F * settingRenderContext.scale;
      float local1525_11 = local1525_2 + local1525_4 - 7.0F * settingRenderContext.scale - local1525_9;
      FontText.processData3(
         FontFace.SEMI,
         "Токен бота",
         local1525_2 + 7.0F * settingRenderContext.scale,
         local1525_3 + 20.0F * settingRenderContext.scale + 3.75F * settingRenderContext.scale,
         5.5F * settingRenderContext.scale,
         UiTheme.intVal2
      );
      processData7(
         settingRenderContext,
         autoBuySettingsScreen,
         autoBuySettingsScreen.textSetting2,
         "tgToken",
         null,
         null,
         local1525_11,
         local1525_3 + 20.0F * settingRenderContext.scale,
         local1525_9,
         local1525_10,
         local1525_6,
         local1525_7
      );
      FontText.processData3(
         FontFace.SEMI,
         "ID чата",
         local1525_2 + 7.0F * settingRenderContext.scale,
         local1525_3 + 36.0F * settingRenderContext.scale + 3.75F * settingRenderContext.scale,
         5.5F * settingRenderContext.scale,
         UiTheme.intVal2
      );
      processData7(
         settingRenderContext,
         autoBuySettingsScreen,
         autoBuySettingsScreen.textSetting3,
         "tgChat",
         null,
         null,
         local1525_11,
         local1525_3 + 36.0F * settingRenderContext.scale,
         local1525_9,
         local1525_10,
         local1525_6,
         local1525_7
      );
   }

   static void processData5(
      SettingRenderContext settingRenderContext,
      AutoBuySettingsScreen autoBuySettingsScreen,
      float local1529_2,
      float local1529_3,
      float local1529_4,
      float local1529_5,
      boolean local1529_6,
      long local1529_7
   ) {
      settingRenderContext.processData9(
         local1529_2, local1529_3, local1529_4, local1529_5, 4.0F * settingRenderContext.scale, ClickGuiStyle.intVal47, UiTheme.intVal17
      );
      FontText.processData3(
         FontFace.SEMI,
         "Bot API Configuration",
         local1529_2 + 8.0F * settingRenderContext.scale,
         local1529_3 + 8.0F * settingRenderContext.scale,
         5.0F * settingRenderContext.scale,
         UiTheme.intVal3
      );
      processData10(
         settingRenderContext,
         "Telegram Bot Token",
         "API token obtained from @BotFather",
         local1529_2 + 8.0F * settingRenderContext.scale,
         local1529_3 + 22.0F * settingRenderContext.scale,
         86.0F * settingRenderContext.scale
      );
      processData7(
         settingRenderContext,
         autoBuySettingsScreen,
         autoBuySettingsScreen.textSetting2,
         "tgToken",
         null,
         null,
         local1529_2 + 98.0F * settingRenderContext.scale,
         local1529_3 + 22.0F * settingRenderContext.scale,
         local1529_4 - 106.0F * settingRenderContext.scale,
         14.0F * settingRenderContext.scale,
         local1529_6,
         local1529_7
      );
      processData10(
         settingRenderContext,
         "Telegram Chat ID",
         "Target chat or channel ID for alerts",
         local1529_2 + 8.0F * settingRenderContext.scale,
         local1529_3 + 42.0F * settingRenderContext.scale,
         86.0F * settingRenderContext.scale
      );
      processData7(
         settingRenderContext,
         autoBuySettingsScreen,
         autoBuySettingsScreen.textSetting3,
         "tgChat",
         null,
         null,
         local1529_2 + 98.0F * settingRenderContext.scale,
         local1529_3 + 42.0F * settingRenderContext.scale,
         local1529_4 - 106.0F * settingRenderContext.scale,
         14.0F * settingRenderContext.scale,
         local1529_6,
         local1529_7
      );
   }

   static void processData6(
      SettingRenderContext settingRenderContext,
      AutoBuySettingsScreen autoBuySettingsScreen,
      float local1530_2,
      float local1530_3,
      float local1530_4,
      float local1530_5,
      float local1530_6,
      long local1530_7,
      long local1530_9
   ) {
      settingRenderContext.processData12(
         local1530_2, local1530_3, local1530_4, local1530_5, 4.0F * settingRenderContext.scale, ClickGuiStyle.intVal47, UiTheme.intVal17
      );
      FontText.processData3(
         FontFace.SEMI,
         "История покупок",
         local1530_2 + 7.0F * settingRenderContext.scale,
         local1530_3 + 7.0F * settingRenderContext.scale,
         5.5F * settingRenderContext.scale,
         UiTheme.intVal
      );
      float local1530_11 = local1530_3 + 22.0F * settingRenderContext.scale;
      float local1530_12 = local1530_3 + local1530_5 - 8.0F * settingRenderContext.scale - local1530_11;
      float local1530_13 = local1530_4 - 20.0F * settingRenderContext.scale;
      autoBuySettingsScreen.floatValArray4 = new float[]{local1530_2, local1530_11, local1530_4, local1530_12};
      autoBuySettingsScreen.smoothScroll2.maximumOffset = Math.max(
         0.0F, autoBuySettingsScreen.list.size() * 13.0F * settingRenderContext.scale - 6.0F * settingRenderContext.scale - local1530_12
      );
      autoBuySettingsScreen.smoothScroll2.update(local1530_6);
      if (autoBuySettingsScreen.list.isEmpty()) {
         FontText.processData3(
            FontFace.SEMI,
            "Покупок пока нет",
            local1530_2 + (local1530_4 - FontText.computeFloat(FontFace.SEMI, "Покупок пока нет", 5.0F * settingRenderContext.scale)) / 2.0F,
            local1530_11 + local1530_12 / 2.0F - 2.5F * settingRenderContext.scale,
            5.0F * settingRenderContext.scale,
            UiTheme.intVal4
         );
      }

      Render2D.renderElement2(
         settingRenderContext.drawContext,
         local1530_2 + 6.0F * settingRenderContext.scale,
         local1530_11 - 1.0F * settingRenderContext.scale,
         local1530_4 - 12.0F * settingRenderContext.scale,
         local1530_12 + 2.0F * settingRenderContext.scale
      );

      for (int index = 0; index < autoBuySettingsScreen.list.size(); index++) {
         float local1530_15 = local1530_11 + index * 13.0F * settingRenderContext.scale - autoBuySettingsScreen.smoothScroll2.getPixelAlignedOffset();
         if (!(local1530_15 + 7.0F * settingRenderContext.scale < local1530_11 - 2.0F) && !(local1530_15 > local1530_11 + local1530_12 + 2.0F)) {
            float local1530_16 = AutoBuyPanel.computeFloat(local1530_7 - local1530_9, 0.22F + Math.min(4, index) * 0.06F, 0.25F);
            if (!(local1530_16 < 0.005F)) {
               Render2D.processData46(local1530_16);
               ItemRenderCache.processData(
                  autoBuySettingsScreen.list.get(index).item().displayStack(),
                  local1530_2 + 8.0F * settingRenderContext.scale + (1.0F - local1530_16) * 4.0F * settingRenderContext.scale,
                  local1530_15 + 0.75F * settingRenderContext.scale,
                  5.5F * settingRenderContext.scale,
                  ItemDrawOptions.noDecorations(AutoBuyPanel.computeFloat2())
               );
               Render2D.renderElement2(
                  settingRenderContext.drawContext,
                  local1530_2 + 17.5F * settingRenderContext.scale,
                  local1530_15 - 1.0F * settingRenderContext.scale,
                  75.0F * settingRenderContext.scale,
                  9.0F * settingRenderContext.scale
               );
               FontText.processData3(
                  FontFace.SEMI,
                  autoBuySettingsScreen.list.get(index).item().name(),
                  local1530_2 + 17.5F * settingRenderContext.scale + (1.0F - local1530_16) * 4.0F * settingRenderContext.scale,
                  local1530_15,
                  5.5F * settingRenderContext.scale,
                  UiTheme.intVal
               );
               FontText.processData3(
                  FontFace.SEMI,
                  "x" + autoBuySettingsScreen.list.get(index).count(),
                  local1530_2
                     + 19.5F * settingRenderContext.scale
                     + (1.0F - local1530_16) * 4.0F * settingRenderContext.scale
                     + FontText.computeFloat(
                        FontFace.SEMI, autoBuySettingsScreen.list.get(index).item().name(), 5.5F * settingRenderContext.scale
                     ),
                  local1530_15 + 0.5F * settingRenderContext.scale,
                  5.0F * settingRenderContext.scale,
                  UiTheme.intVal2
               );
               Render2D.renderElement(settingRenderContext.drawContext);
               FontText.processData3(
                  FontFace.SEMI,
                  autoBuySettingsScreen.list.get(index).time(),
                  local1530_2 + 95.0F * settingRenderContext.scale,
                  local1530_15,
                  5.5F * settingRenderContext.scale,
                  UiTheme.intVal3
               );
               FontText.processData3(
                  FontFace.SEMI,
                  AutoBuySettingsScreen.formatText(autoBuySettingsScreen.list.get(index).priceCoins()),
                  local1530_2
                     + 8.0F * settingRenderContext.scale
                     + local1530_13
                     - FontText.computeFloat(
                        FontFace.SEMI,
                        AutoBuySettingsScreen.formatText(autoBuySettingsScreen.list.get(index).priceCoins()),
                        5.5F * settingRenderContext.scale
                     ),
                  local1530_15,
                  5.5F * settingRenderContext.scale,
                  UiTheme.intVal
               );
               FontText.processData3(
                  FontFace.SEMI,
                  "$",
                  local1530_2
                     + 8.0F * settingRenderContext.scale
                     + local1530_13
                     - FontText.computeFloat(
                        FontFace.SEMI,
                        AutoBuySettingsScreen.formatText(autoBuySettingsScreen.list.get(index).priceCoins()),
                        5.5F * settingRenderContext.scale
                     )
                     - FontText.computeFloat(FontFace.SEMI, "$", 5.5F * settingRenderContext.scale),
                  local1530_15,
                  5.5F * settingRenderContext.scale,
                  UiTheme.intVal7
               );
               Render2D.updateState();
            }
         }
      }

      Render2D.renderElement(settingRenderContext.drawContext);
      Render2D.processData51(
         local1530_2 + local1530_4 - 5.0F * settingRenderContext.scale,
         local1530_11,
         1.0F * settingRenderContext.scale,
         local1530_12,
         0.5F * settingRenderContext.scale,
         UiTheme.intVal17
      );
      if (autoBuySettingsScreen.smoothScroll2.maximumOffset > 0.0F) {
         Render2D.processData51(
            local1530_2 + local1530_4 - 5.0F * settingRenderContext.scale,
            local1530_11
               + (local1530_12 - Math.max(6.0F * settingRenderContext.scale, local1530_12 * (local1530_12 / (local1530_12 + autoBuySettingsScreen.smoothScroll2.maximumOffset))))
                  * (autoBuySettingsScreen.smoothScroll2.getPixelAlignedOffset() / autoBuySettingsScreen.smoothScroll2.maximumOffset),
            1.0F * settingRenderContext.scale,
            Math.max(6.0F * settingRenderContext.scale, local1530_12 * (local1530_12 / (local1530_12 + autoBuySettingsScreen.smoothScroll2.maximumOffset))),
            0.5F * settingRenderContext.scale,
            UiTheme.intVal5
         );
      }
   }

   private AutoBuyItemPanel() {
   }

   static void processData7(
      SettingRenderContext settingRenderContext,
      AutoBuySettingsScreen autoBuySettingsScreen,
      TextSetting newTextSetting,
      String local1533_3,
      String local1533_4,
      String local1533_5,
      float local1533_6,
      float local1533_7,
      float local1533_8,
      float local1533_9,
      boolean local1533_10,
      long local1533_11
   ) {
      Render2D.processData51(local1533_6, local1533_7, local1533_8, local1533_9, 2.0F * settingRenderContext.scale, ClickGuiStyle.intVal47);
      Render2D.processData86(
         local1533_6,
         local1533_7,
         local1533_8,
         local1533_9,
         2.0F * settingRenderContext.scale,
         0.5F,
         UiTheme.computeInt2(
            UiTheme.intVal17,
            UiTheme.intVal18,
            settingRenderContext.computeFloat2("ab:inf:" + local1533_3, settingRenderContext.textSetting == newTextSetting)
         )
      );
      Render2D.processData51(
         local1533_6 + local1533_8 - 14.5F * settingRenderContext.scale,
         local1533_7 + 1.0F * settingRenderContext.scale,
         0.5F * settingRenderContext.scale,
         local1533_9 - 2.0F * settingRenderContext.scale,
         0.0F,
         UiTheme.intVal17
      );
      String local1533_13 = settingRenderContext.textSetting == newTextSetting
         ? (local1533_4 == null ? "" : local1533_4)
            + (
               newTextSetting.getValue().isEmpty()
                  ? ""
                  : (
                     local1533_4 == null && local1533_5 == null
                        ? newTextSetting.getValue()
                        : AutoBuySettingsScreen.formatText(AutoBuySettingsScreen.computeLong(newTextSetting.getValue()))
                  )
            )
            + SettingRenderContext.formatText()
            + (local1533_5 == null ? "" : local1533_5)
         : (
            newTextSetting.getValue().isEmpty()
               ? newTextSetting.getPlaceholder()
               : (
                  local1533_4 == null && local1533_5 == null
                     ? newTextSetting.getValue()
                     : (local1533_4 == null ? "" : local1533_4)
                        + AutoBuySettingsScreen.formatText(AutoBuySettingsScreen.computeLong(newTextSetting.getValue()))
                        + (local1533_5 == null ? "" : local1533_5)
               )
         );
      Render2D.renderElement2(
         settingRenderContext.drawContext,
         local1533_6 + 5.0F * settingRenderContext.scale,
         local1533_7,
         local1533_8 - 14.5F * settingRenderContext.scale - 8.0F * settingRenderContext.scale,
         local1533_9
      );
      FontText.processData3(
         FontFace.SEMI,
         local1533_13,
         settingRenderContext.textSetting == newTextSetting
               && FontText.computeFloat(FontFace.SEMI, local1533_13, 5.0F * settingRenderContext.scale)
                  > local1533_8 - 14.5F * settingRenderContext.scale - 8.0F * settingRenderContext.scale
            ? local1533_6
               + local1533_8
               - 17.5F * settingRenderContext.scale
               - FontText.computeFloat(FontFace.SEMI, local1533_13, 5.0F * settingRenderContext.scale)
            : local1533_6 + 5.0F * settingRenderContext.scale,
         local1533_7 + local1533_9 / 2.0F - 2.5F * settingRenderContext.scale,
         5.0F * settingRenderContext.scale,
         newTextSetting.getValue().isEmpty() && settingRenderContext.textSetting != newTextSetting ? UiTheme.intVal3 : UiTheme.intVal
      );
      Render2D.renderElement(settingRenderContext.drawContext);
      float local1533_14 = autoBuySettingsScreen.map4.get(local1533_3) != null && (float)(local1533_11 - autoBuySettingsScreen.map4.get(local1533_3)) / 400.0F < 1.0F
         ? 1.0F - (float)(local1533_11 - autoBuySettingsScreen.map4.get(local1533_3)) / 400.0F
         : 0.0F;
      if (autoBuySettingsScreen.map4.get(local1533_3) != null && local1533_14 <= 0.0F) {
         autoBuySettingsScreen.map4.remove(local1533_3);
      }

      Render2D.processData51(
         local1533_6 + local1533_8 - 9.5F * settingRenderContext.scale,
         local1533_7 + local1533_9 / 2.0F - 2.5F * settingRenderContext.scale,
         5.0F * settingRenderContext.scale,
         5.0F * settingRenderContext.scale,
         1.5F * settingRenderContext.scale,
         UiTheme.computeInt2(
            UiTheme.computeInt2(
               UiTheme.intVal7,
               UiTheme.intVal9,
               settingRenderContext.computeFloat2(
                  "ab:int:" + local1533_3,
                  local1533_10
                     && UiTheme.matchesCriteria(
                        settingRenderContext.mouseX,
                        settingRenderContext.mouseY,
                        local1533_6 + local1533_8 - 14.0F * settingRenderContext.scale,
                        local1533_7,
                        14.0F * settingRenderContext.scale,
                        local1533_9
                     )
               )
            ),
            UiTheme.intVal,
            local1533_14
         )
      );
      Render2D.processData51(
         local1533_6 + local1533_8 - 9.5F * settingRenderContext.scale + 1.1F * settingRenderContext.scale,
         local1533_7 + local1533_9 / 2.0F - 2.5F * settingRenderContext.scale + 1.1F * settingRenderContext.scale,
         5.0F * settingRenderContext.scale - 2.2F * settingRenderContext.scale,
         5.0F * settingRenderContext.scale - 2.2F * settingRenderContext.scale,
         0.9F * settingRenderContext.scale,
         -1
      );
      Render2D.processData55(
         local1533_6 + local1533_8 - 7.0F * settingRenderContext.scale, local1533_7 + local1533_9 / 2.0F, 0.7F * settingRenderContext.scale, -3158318
      );
      if (local1533_10) {
         settingRenderContext.processData7(local1533_6, local1533_7, local1533_8 - 14.0F * settingRenderContext.scale, local1533_9, null, () -> {
            settingRenderContext.textSetting = newTextSetting;
            newTextSetting.moveToEnd(false);
         });
         settingRenderContext.processData7(
            local1533_6 + local1533_8 - 14.0F * settingRenderContext.scale, local1533_7, 14.0F * settingRenderContext.scale, local1533_9, null, () -> {
               if (settingRenderContext.textSetting == newTextSetting) {
                  settingRenderContext.textSetting = null;
               }

               autoBuySettingsScreen.map4.put(local1533_3, System.currentTimeMillis());
            }
         );
      }
   }

   private static void processData8(
      SettingRenderContext settingRenderContext, AutoBuySettingsScreen autoBuySettingsScreen, float local1534_2, float local1534_3, float local1534_4, boolean local1534_5
   ) {
      if (Math.max(0, Math.min(2, autoBuySettingsScreen.numberSetting.intValue())) != 2) {
         FontText.processData3(
            FontFace.SEMI,
            AutoBuySettingsScreen.strValArray[Math.max(0, Math.min(2, autoBuySettingsScreen.numberSetting.intValue()))],
            local1534_2,
            local1534_3,
            4.5F * settingRenderContext.scale,
            UiTheme.intVal2
         );
      }

      FontText.processData3(
         FontFace.SEMI,
         AutoBuySettingsScreen.strValArray[2],
         local1534_2 + local1534_4 - FontText.computeFloat(FontFace.SEMI, AutoBuySettingsScreen.strValArray[2], 4.5F * settingRenderContext.scale),
         local1534_3,
         4.5F * settingRenderContext.scale,
         UiTheme.intVal
      );
      Render2D.processData51(
         local1534_2,
         local1534_3 + 7.0F * settingRenderContext.scale,
         local1534_4,
         7.0F * settingRenderContext.scale,
         2.0F * settingRenderContext.scale,
         ClickGuiStyle.intVal48
      );
      Render2D.processData51(
         local1534_2,
         local1534_3 + 7.0F * settingRenderContext.scale + 0.15F,
         local1534_2
            + 1.0F * settingRenderContext.scale
            + settingRenderContext.computeFloat7("ab:speed", autoBuySettingsScreen.numberSetting.getFraction())
               * (local1534_4 - 2.0F * settingRenderContext.scale - 5.0F * settingRenderContext.scale)
            + 6.0F * settingRenderContext.scale
            - local1534_2,
         7.0F * settingRenderContext.scale,
         2.0F * settingRenderContext.scale,
         UiTheme.computeInt2(
            UiTheme.intVal7,
            UiTheme.computeInt2(UiTheme.intVal13, UiTheme.intVal7, 0.3F),
            settingRenderContext.computeFloat10(autoBuySettingsScreen.numberSetting, autoBuySettingsScreen.numberSetting.intValue() == 2)
         )
      );
      if (settingRenderContext.computeFloat10(autoBuySettingsScreen.numberSetting, autoBuySettingsScreen.numberSetting.intValue() == 2) > 0.005F) {
         Render2D.processData83(
            SettingRenderContext.computeFloat8(local1534_2),
            SettingRenderContext.computeFloat8((local1534_3 + 7.0F * settingRenderContext.scale) * settingRenderContext.scale),
            local1534_4,
            7.0F * settingRenderContext.scale,
            2.0F * settingRenderContext.scale,
            Math.max(2.0F, Math.round(3.0F * settingRenderContext.scale)) / 2.0F,
            settingRenderContext.computeFloat10(autoBuySettingsScreen.numberSetting, autoBuySettingsScreen.numberSetting.intValue() == 2),
            UiTheme.intVal7
         );
      }

      if (settingRenderContext.computeFloat10(autoBuySettingsScreen.numberSetting, autoBuySettingsScreen.numberSetting.intValue() == 2) > 0.005F) {
         Render2D.renderElement2(
            settingRenderContext.drawContext, local1534_2, local1534_3 + 7.0F * settingRenderContext.scale, local1534_4, 7.0F * settingRenderContext.scale
         );
         RoundedClipStack.processData(
            local1534_2,
            local1534_3 + 7.0F * settingRenderContext.scale,
            local1534_4,
            7.0F * settingRenderContext.scale,
            2.0F * settingRenderContext.scale,
            2.0F * settingRenderContext.scale,
            2.0F * settingRenderContext.scale,
            2.0F * settingRenderContext.scale
         );

         for (int index = 0; index * Math.max(2.0F, Math.round(3.0F * settingRenderContext.scale)) < local1534_4; index++) {
            for (int index2 = 0; index2 * Math.max(2.0F, Math.round(3.0F * settingRenderContext.scale)) < 7.0F * settingRenderContext.scale; index2++) {
               if ((index + index2 + (int)(System.currentTimeMillis() / 90L)) % 2 != 1) {
                  Render2D.processData51(
                     local1534_2 + index * Math.max(2.0F, Math.round(3.0F * settingRenderContext.scale)),
                     local1534_3 + 7.0F * settingRenderContext.scale + index2 * Math.max(2.0F, Math.round(3.0F * settingRenderContext.scale)),
                     Math.max(2.0F, Math.round(3.0F * settingRenderContext.scale)),
                     Math.max(2.0F, Math.round(3.0F * settingRenderContext.scale)),
                     0.0F,
                     UiTheme.computeInt(
                        UiTheme.intVal7,
                        settingRenderContext.computeFloat10(autoBuySettingsScreen.numberSetting, autoBuySettingsScreen.numberSetting.intValue() == 2)
                           * (0.28F + 0.34F * (float)Math.sin(System.currentTimeMillis() / 260.0 + index * 0.55F))
                     )
                  );
               }
            }
         }

         RoundedClipStack.updateState2();
         Render2D.renderElement(settingRenderContext.drawContext);
      }

      for (int index3 = 0; index3 < 3; index3++) {
         Render2D.processData51(
            local1534_2 + 1.5F * settingRenderContext.scale + index3 * (local1534_4 - 5.0F * settingRenderContext.scale) / 2.0F,
            local1534_3 + 10.5F * settingRenderContext.scale - 1.0F * settingRenderContext.scale + 0.25F,
            2.0F * settingRenderContext.scale,
            2.0F * settingRenderContext.scale,
            1.0F * settingRenderContext.scale,
            UiTheme.intVal4
         );
      }

      Render2D.processData51(
         local1534_2
            + 1.0F * settingRenderContext.scale
            + settingRenderContext.computeFloat7("ab:speed", autoBuySettingsScreen.numberSetting.getFraction()) * (local1534_4 - 7.0F * settingRenderContext.scale),
         local1534_3 + 8.0F * settingRenderContext.scale,
         5.0F * settingRenderContext.scale,
         5.0F * settingRenderContext.scale,
         1.0F * settingRenderContext.scale,
         UiTheme.intVal10
      );
      if (local1534_5) {
         settingRenderContext.map7.put(autoBuySettingsScreen.numberSetting, new float[]{local1534_2, local1534_4});
         settingRenderContext.processData7(
            local1534_2,
            local1534_3 + 5.0F * settingRenderContext.scale,
            local1534_4,
            11.0F * settingRenderContext.scale,
            null,
            () -> settingRenderContext.numberSetting = autoBuySettingsScreen.numberSetting
         );
      }
   }

   private static void processData9(
      SettingRenderContext settingRenderContext,
      AutoBuySettingsScreen autoBuySettingsScreen,
      AutoBuyPanel autoBuyPanel,
      float local1536_3,
      float local1536_4,
      float local1536_5,
      boolean local1536_6,
      boolean local1536_7
   ) {
      if (!(settingRenderContext.computeFloat2("ab:editmode", local1536_7) < 0.005F) && strVal != null && AuctionItemCatalog.getAutoBuyItem(strVal) != null) {
         float local1536_8 = 3.0F * settingRenderContext.scale
            + 5.5F * settingRenderContext.scale
            + 3.0F * settingRenderContext.scale
            + FontText.computeFloat(FontFace.SEMI, strVal, 5.0F * settingRenderContext.scale)
            + 4.0F * settingRenderContext.scale
            + 5.0F * settingRenderContext.scale
            + 3.0F * settingRenderContext.scale;
         float local1536_9 = local1536_3
            + local1536_5
            - 8.0F * settingRenderContext.scale
            - local1536_8
            + (1.0F - settingRenderContext.computeFloat2("ab:editmode", local1536_7)) * 8.0F * settingRenderContext.scale;
         float local1536_10 = local1536_4 + 8.0F * settingRenderContext.scale + 2.5F * settingRenderContext.scale - 5.0F * settingRenderContext.scale;
         Render2D.processData46(settingRenderContext.computeFloat2("ab:editmode", local1536_7));
         Render2D.processData51(
            local1536_9, local1536_10, local1536_8, 10.0F * settingRenderContext.scale, 2.0F * settingRenderContext.scale, ClickGuiStyle.intVal49
         );
         ItemRenderCache.processData(
            AuctionItemCatalog.getAutoBuyItem(strVal).displayStack(),
            local1536_9 + 3.0F * settingRenderContext.scale,
            local1536_10 + 2.25F * settingRenderContext.scale,
            5.5F * settingRenderContext.scale,
            ItemDrawOptions.noDecorations(AutoBuyPanel.computeFloat2())
         );
         FontText.processData3(
            FontFace.SEMI,
            strVal,
            local1536_9 + 11.5F * settingRenderContext.scale,
            local1536_10 + 2.0F * settingRenderContext.scale,
            5.0F * settingRenderContext.scale,
            UiTheme.intVal
         );
         settingRenderContext.processData4(
            "ε",
            local1536_9 + local1536_8 - 5.5F * settingRenderContext.scale,
            local1536_10 + 5.0F * settingRenderContext.scale,
            5.5F * settingRenderContext.scale,
            UiTheme.computeInt2(
               UiTheme.intVal3,
               UiTheme.intVal,
               settingRenderContext.computeFloat2(
                  "ab:editx",
                  local1536_6
                     && local1536_7
                     && UiTheme.matchesCriteria(
                        settingRenderContext.mouseX, settingRenderContext.mouseY, local1536_9, local1536_10, local1536_8, 10.0F * settingRenderContext.scale
                     )
               )
            )
         );
         Render2D.updateState();
         if (local1536_6 && local1536_7) {
            settingRenderContext.processData7(local1536_9, local1536_10, local1536_8, 10.0F * settingRenderContext.scale, null, () -> autoBuyPanel.updateState3());
         }
      }
   }

   static void processData10(SettingRenderContext settingRenderContext, String local1537_1, String local1537_2, float local1537_3, float local1537_4, float local1537_5) {
      FontText.processData3(
         FontFace.SEMI, local1537_1, local1537_3, local1537_4, 5.5F * settingRenderContext.scale, UiTheme.intVal
      );
      float local1537_6 = local1537_4 + 8.0F * settingRenderContext.scale;

      for (String local1537_8 : UiTheme.getList(FontFace.SEMI, local1537_2, 5.0F * settingRenderContext.scale, local1537_5)) {
         FontText.processData3(
            FontFace.SEMI, local1537_8, local1537_3, local1537_6, 5.0F * settingRenderContext.scale, UiTheme.intVal2
         );
         local1537_6 += 6.0F * settingRenderContext.scale;
      }
   }

   static void processData11(
      SettingRenderContext settingRenderContext,
      AutoBuySettingsScreen autoBuySettingsScreen,
      AutoBuyPanel autoBuyPanel,
      float local1538_3,
      float local1538_4,
      float local1538_5,
      float local1538_6,
      boolean local1538_7,
      float local1538_8,
      long local1538_9
   ) {
      float local1538_11 = 23.0F * settingRenderContext.scale;
      float local1538_12 = local1538_5 - 5.0F * settingRenderContext.scale;
      float local1538_13 = local1538_11 + (local1538_12 - 11.0F * local1538_11) / 10.0F;
      autoBuySettingsScreen.floatValArray3 = new float[]{local1538_3, local1538_4, local1538_5, local1538_6};
      ArrayList<AutoBuyItem> local1538_14 = autoBuySettingsScreen.getArrayList();
      autoBuySettingsScreen.smoothScroll.maximumOffset = Math.max(
         0.0F, (local1538_14.size() + 10) / 11 * local1538_13 - (local1538_12 - 11.0F * local1538_11) / 10.0F - local1538_6
      );
      autoBuySettingsScreen.smoothScroll.update(local1538_8);
      if (local1538_14.isEmpty()) {
         FontText.processData3(
            FontFace.SEMI,
            "Ничего не найдено",
            local1538_3 + (local1538_12 - FontText.computeFloat(FontFace.SEMI, "Ничего не найдено", 5.0F * settingRenderContext.scale)) / 2.0F,
            local1538_4 + local1538_6 / 2.0F - 2.5F * settingRenderContext.scale,
            5.0F * settingRenderContext.scale,
            UiTheme.intVal3
         );
      }

      float[] floatArray = new float[]{local1538_3, local1538_4, local1538_12, local1538_6};
      String local1538_16 = "";
      float local1538_17 = 0.0F;
      float local1538_18 = 0.0F;
      Render2D.renderElement2(
         settingRenderContext.drawContext,
         local1538_3 - 3.0F * settingRenderContext.scale,
         local1538_4 - 2.0F * settingRenderContext.scale,
         local1538_12 + 6.0F * settingRenderContext.scale,
         local1538_6 + 4.0F * settingRenderContext.scale
      );
      RoundedClipStack.processData(
         local1538_3 - 3.0F * settingRenderContext.scale,
         local1538_4 - 2.0F * settingRenderContext.scale,
         local1538_12 + 6.0F * settingRenderContext.scale,
         local1538_6 + 4.0F * settingRenderContext.scale,
         4.0F * settingRenderContext.scale,
         4.0F * settingRenderContext.scale,
         4.0F * settingRenderContext.scale,
         4.0F * settingRenderContext.scale
      );

      for (int index = 0; index < local1538_14.size(); index++) {
         float local1538_20 = local1538_3 + index % 11 * local1538_13;
         float local1538_21 = local1538_4 + index / 11 * local1538_13 - autoBuySettingsScreen.smoothScroll.getPixelAlignedOffset();
         if (!(local1538_21 + local1538_11 < local1538_4 - 2.0F) && !(local1538_21 > local1538_4 + local1538_6 + 2.0F)) {
            String local1538_22 = local1538_14.get(index).name();
            float local1538_23 = AutoBuyPanel.computeFloat(
               local1538_9 - autoBuySettingsScreen.timeMillis3, Math.min(0.5F, 0.05F + index / 11 * 0.045F + index % 11 * 0.012F), 0.25F
            );
            if (!(local1538_23 < 0.005F)) {
               float local1538_24 = local1538_21 + (1.0F - local1538_23) * 3.0F * settingRenderContext.scale;
               float local1538_25 = settingRenderContext.computeFloat2("ab:sel:" + local1538_22, autoBuySettingsScreen.matchesCriteria(local1538_22));
               float local1538_26 = settingRenderContext.computeFloat2(
                  "ab:hov:" + local1538_22,
                  local1538_7
                     && UiTheme.matchesCriteria(settingRenderContext.mouseX, settingRenderContext.mouseY, local1538_20, local1538_24, local1538_11, local1538_11)
                     && UiTheme.matchesCriteria(settingRenderContext.mouseX, settingRenderContext.mouseY, local1538_3, local1538_4, local1538_12, local1538_6)
               );
               Render2D.processData46(local1538_23);
               if (local1538_26 > 0.005F) {
                  Render2D.processData51(
                     local1538_20,
                     local1538_24,
                     local1538_11,
                     local1538_11,
                     4.0F * settingRenderContext.scale,
                     UiTheme.computeInt(ClickGuiStyle.intVal48, local1538_26)
                  );
               }

               if (local1538_25 > 0.005F) {
                  Render2D.processData51(
                     local1538_20,
                     local1538_24,
                     local1538_11,
                     local1538_11,
                     4.0F * settingRenderContext.scale,
                     UiTheme.computeInt(UiTheme.intVal7, 0.14F * local1538_25)
                  );
               }

               if (settingRenderContext.computeFloat2("ab:editc:" + local1538_22, local1538_22.equals(autoBuySettingsScreen.strVal)) > 0.005F) {
                  Render2D.processData86(
                     local1538_20,
                     local1538_24,
                     local1538_11,
                     local1538_11,
                     4.0F * settingRenderContext.scale,
                     0.5F,
                     UiTheme.computeInt(
                        UiTheme.intVal7, settingRenderContext.computeFloat2("ab:editc:" + local1538_22, local1538_22.equals(autoBuySettingsScreen.strVal))
                     )
                  );
               }

               if (autoBuySettingsScreen.map3.get(local1538_22) != null && (float)(local1538_9 - autoBuySettingsScreen.map3.get(local1538_22)) / 350.0F < 1.0F) {
                  Render2D.processData86(
                     local1538_20
                        - 2.5F
                           * settingRenderContext.scale
                           * (
                              (float)(local1538_9 - autoBuySettingsScreen.map3.get(local1538_22))
                                 / 350.0F
                                 * (2.0F - (float)(local1538_9 - autoBuySettingsScreen.map3.get(local1538_22)) / 350.0F)
                           ),
                     local1538_24
                        - 2.5F
                           * settingRenderContext.scale
                           * (
                              (float)(local1538_9 - autoBuySettingsScreen.map3.get(local1538_22))
                                 / 350.0F
                                 * (2.0F - (float)(local1538_9 - autoBuySettingsScreen.map3.get(local1538_22)) / 350.0F)
                           ),
                     local1538_11
                        + 5.0F
                           * settingRenderContext.scale
                           * (
                              (float)(local1538_9 - autoBuySettingsScreen.map3.get(local1538_22))
                                 / 350.0F
                                 * (2.0F - (float)(local1538_9 - autoBuySettingsScreen.map3.get(local1538_22)) / 350.0F)
                           ),
                     local1538_11
                        + 5.0F
                           * settingRenderContext.scale
                           * (
                              (float)(local1538_9 - autoBuySettingsScreen.map3.get(local1538_22))
                                 / 350.0F
                                 * (2.0F - (float)(local1538_9 - autoBuySettingsScreen.map3.get(local1538_22)) / 350.0F)
                           ),
                     4.0F * settingRenderContext.scale
                        + 2.5F
                           * settingRenderContext.scale
                           * (
                              (float)(local1538_9 - autoBuySettingsScreen.map3.get(local1538_22))
                                 / 350.0F
                                 * (2.0F - (float)(local1538_9 - autoBuySettingsScreen.map3.get(local1538_22)) / 350.0F)
                           ),
                     0.75F,
                     UiTheme.computeInt(
                        UiTheme.intVal7,
                        (
                              1.0F
                                 - (float)(local1538_9 - autoBuySettingsScreen.map3.get(local1538_22))
                                    / 350.0F
                                    * (2.0F - (float)(local1538_9 - autoBuySettingsScreen.map3.get(local1538_22)) / 350.0F)
                           )
                           * 0.9F
                     )
                  );
               } else if (autoBuySettingsScreen.map3.get(local1538_22) != null) {
                  autoBuySettingsScreen.map3.remove(local1538_22);
               }

               ItemRenderCache.processData(
                  local1538_14.get(index).displayStack(),
                  local1538_20 + local1538_11 / 4.0F,
                  local1538_24 + local1538_11 / 4.0F,
                  local1538_11 * 0.5F,
                  ItemDrawOptions.noDecorations(AutoBuyPanel.computeFloat2() * (0.48F + 0.52F * Math.max(local1538_25, local1538_26 * 0.55F)))
               );
               Render2D.updateState();
               if (local1538_7
                  && UiTheme.matchesCriteria(settingRenderContext.mouseX, settingRenderContext.mouseY, local1538_20, local1538_24, local1538_11, local1538_11)
                  && UiTheme.matchesCriteria(settingRenderContext.mouseX, settingRenderContext.mouseY, local1538_3, local1538_4, local1538_12, local1538_6)) {
                  local1538_16 = local1538_22;
                  local1538_17 = local1538_20 + local1538_11 / 2.0F;
                  local1538_18 = local1538_24;
               }

               if (local1538_7) {
                  settingRenderContext.processData6(
                     local1538_20,
                     local1538_24,
                     local1538_11,
                     local1538_11,
                     floatArray,
                     () -> autoBuySettingsScreen.processData2(local1538_22),
                     () -> autoBuyPanel.processData3(local1538_22)
                  );
               }
            }
         }
      }

      RoundedClipStack.updateState2();
      Render2D.renderElement(settingRenderContext.drawContext);
      if (!local1538_16.isEmpty()) {
         Render2D.processData51(
            Math.max(
               local1538_3,
               Math.min(
                  local1538_3
                     + local1538_12
                     - FontText.computeFloat(FontFace.SEMI, local1538_16, 5.0F * settingRenderContext.scale)
                     - 6.0F * settingRenderContext.scale,
                  local1538_17
                     - FontText.computeFloat(FontFace.SEMI, local1538_16, 5.0F * settingRenderContext.scale) / 2.0F
                     - 3.0F * settingRenderContext.scale
               )
            ),
            local1538_18 - 12.0F * settingRenderContext.scale < local1538_4
               ? local1538_18 + local1538_11 + 3.0F * settingRenderContext.scale
               : local1538_18 - 12.0F * settingRenderContext.scale,
            FontText.computeFloat(FontFace.SEMI, local1538_16, 5.0F * settingRenderContext.scale) + 6.0F * settingRenderContext.scale,
            10.0F * settingRenderContext.scale,
            3.0F * settingRenderContext.scale,
            ClickGuiStyle.intVal48
         );
         Render2D.processData86(
            Math.max(
               local1538_3,
               Math.min(
                  local1538_3
                     + local1538_12
                     - FontText.computeFloat(FontFace.SEMI, local1538_16, 5.0F * settingRenderContext.scale)
                     - 6.0F * settingRenderContext.scale,
                  local1538_17
                     - FontText.computeFloat(FontFace.SEMI, local1538_16, 5.0F * settingRenderContext.scale) / 2.0F
                     - 3.0F * settingRenderContext.scale
               )
            ),
            local1538_18 - 12.0F * settingRenderContext.scale < local1538_4
               ? local1538_18 + local1538_11 + 3.0F * settingRenderContext.scale
               : local1538_18 - 12.0F * settingRenderContext.scale,
            FontText.computeFloat(FontFace.SEMI, local1538_16, 5.0F * settingRenderContext.scale) + 6.0F * settingRenderContext.scale,
            10.0F * settingRenderContext.scale,
            3.0F * settingRenderContext.scale,
            0.5F,
            UiTheme.intVal17
         );
         FontText.processData3(
            FontFace.SEMI,
            local1538_16,
            Math.max(
                  local1538_3,
                  Math.min(
                     local1538_3
                        + local1538_12
                        - FontText.computeFloat(FontFace.SEMI, local1538_16, 5.0F * settingRenderContext.scale)
                        - 6.0F * settingRenderContext.scale,
                     local1538_17
                        - FontText.computeFloat(FontFace.SEMI, local1538_16, 5.0F * settingRenderContext.scale) / 2.0F
                        - 3.0F * settingRenderContext.scale
                  )
               )
               + 3.0F * settingRenderContext.scale,
            (
                  local1538_18 - 12.0F * settingRenderContext.scale < local1538_4
                     ? local1538_18 + local1538_11 + 3.0F * settingRenderContext.scale
                     : local1538_18 - 12.0F * settingRenderContext.scale
               )
               + 2.5F * settingRenderContext.scale,
            5.0F * settingRenderContext.scale,
            UiTheme.intVal
         );
      }

      if (autoBuySettingsScreen.smoothScroll.maximumOffset > 0.0F) {
         Render2D.processData51(
            local1538_3 + local1538_5 - 1.0F * settingRenderContext.scale,
            local1538_4
               + (local1538_6 - Math.max(8.0F * settingRenderContext.scale, local1538_6 * (local1538_6 / (local1538_6 + autoBuySettingsScreen.smoothScroll.maximumOffset))))
                  * (autoBuySettingsScreen.smoothScroll.getPixelAlignedOffset() / autoBuySettingsScreen.smoothScroll.maximumOffset),
            1.0F * settingRenderContext.scale,
            Math.max(8.0F * settingRenderContext.scale, local1538_6 * (local1538_6 / (local1538_6 + autoBuySettingsScreen.smoothScroll.maximumOffset))),
            0.5F * settingRenderContext.scale,
            UiTheme.intVal5
         );
      }
   }

   static void processData12(
      SettingRenderContext settingRenderContext,
      AutoBuySettingsScreen autoBuySettingsScreen,
      float local1539_2,
      float local1539_3,
      float local1539_4,
      float local1539_5,
      long local1539_6,
      long local1539_8,
      float local1539_10
   ) {
      settingRenderContext.processData9(
         local1539_2, local1539_3, local1539_4, local1539_5, 4.0F * settingRenderContext.scale, ClickGuiStyle.intVal47, UiTheme.intVal17
      );
      FontText.processData3(
         FontFace.SEMI,
         "Price Chart",
         local1539_2 + 8.0F * settingRenderContext.scale,
         local1539_3 + 8.0F * settingRenderContext.scale,
         5.0F * settingRenderContext.scale,
         UiTheme.intVal3
      );
      autoBuySettingsScreen.doubleVal = autoBuySettingsScreen.doubleVal + (autoBuySettingsScreen.timeMillis - autoBuySettingsScreen.doubleVal) * (1.0 - Math.exp(-local1539_10 * 8.0F));
      if (Math.abs(autoBuySettingsScreen.timeMillis - autoBuySettingsScreen.doubleVal) < 1.0) {
         autoBuySettingsScreen.doubleVal = autoBuySettingsScreen.timeMillis;
      }

      FontText.processData3(
         FontFace.SEMI,
         "Total sales: $" + AutoBuySettingsScreen.formatText(Math.round(autoBuySettingsScreen.doubleVal)),
         local1539_2
            + local1539_4
            - 8.0F * settingRenderContext.scale
            - FontText.computeFloat(
               FontFace.SEMI, "Total sales: $" + AutoBuySettingsScreen.formatText(Math.round(autoBuySettingsScreen.doubleVal)), 4.5F * settingRenderContext.scale
            ),
         local1539_3 + 8.5F * settingRenderContext.scale,
         4.5F * settingRenderContext.scale,
         UiTheme.intVal4
      );
      float local1539_11 = local1539_3 + 22.0F * settingRenderContext.scale;
      float local1539_12 = local1539_3 + local1539_5 - 8.0F * settingRenderContext.scale - local1539_11;
      float local1539_13 = local1539_2 + 28.0F * settingRenderContext.scale;
      float local1539_14 = local1539_2 + local1539_4 - 8.0F * settingRenderContext.scale - local1539_13;

      for (int index = 0; index < strValArray.length; index++) {
         FontText.processData3(
            FontFace.SEMI,
            strValArray[index],
            local1539_2
               + 20.0F * settingRenderContext.scale
               - FontText.computeFloat(FontFace.SEMI, strValArray[index], 4.5F * settingRenderContext.scale),
            local1539_11 + index * (local1539_12 - 5.0F * settingRenderContext.scale) / (strValArray.length - 1),
            4.5F * settingRenderContext.scale,
            UiTheme.intVal4
         );
         Render2D.processData51(
            local1539_13,
            local1539_11 + index * (local1539_12 - 5.0F * settingRenderContext.scale) / (strValArray.length - 1) + 2.25F * settingRenderContext.scale,
            local1539_14,
            0.5F * settingRenderContext.scale,
            0.25F * settingRenderContext.scale,
            UiTheme.intVal6
         );
      }

      for (int index2 = 0; index2 < autoBuySettingsScreen.floatValArray2.length; index2++) {
         float local1539_16 = Math.max(
            0.0F,
            settingRenderContext.computeFloat7("ab:bar:" + index2, autoBuySettingsScreen.floatValArray2[index2])
               * local1539_12
               * AutoBuyPanel.computeFloat(local1539_6 - local1539_8, 0.18F + index2 * 0.025F, 0.3F)
         );
         if (!(local1539_16 < 0.5F)) {
            Render2D.processData84(
               local1539_13 + index2 * (local1539_14 - 13.0F * settingRenderContext.scale) / (autoBuySettingsScreen.floatValArray2.length - 1),
               local1539_11 + local1539_12 - local1539_16,
               13.0F * settingRenderContext.scale,
               local1539_16,
               Math.min(2.0F * settingRenderContext.scale, local1539_16 / 2.0F),
               Math.min(2.0F * settingRenderContext.scale, local1539_16 / 2.0F),
               0.0F,
               0.0F,
               UiTheme.computeInt2(
                  UiTheme.intVal7,
                  UiTheme.intVal9,
                  settingRenderContext.computeFloat2(
                     "ab:barh:" + index2,
                     UiTheme.matchesCriteria(
                        settingRenderContext.mouseX,
                        settingRenderContext.mouseY,
                        local1539_13 + index2 * (local1539_14 - 13.0F * settingRenderContext.scale) / (autoBuySettingsScreen.floatValArray2.length - 1),
                        local1539_11,
                        13.0F * settingRenderContext.scale,
                        local1539_12
                     )
                  )
               )
            );
         }
      }
   }
}

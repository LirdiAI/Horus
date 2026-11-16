package horus.client.ui;



import horus.client.cosmetic.CosmeticCatalog;

import horus.client.cosmetic.CosmeticCategory;

import horus.client.cosmetic.CosmeticEntry;

import horus.client.cosmetic.CosmeticModel;

import horus.client.cosmetic.CosmeticPreviewAtlas;

import horus.client.cosmetic.CosmeticStore;

import horus.client.render.FontFace;

import horus.client.render.FontText;

import horus.client.ui.UiTheme;

import horus.client.cosmetic.CosmeticAccess;

import horus.client.ui.AnimatedValue;

import horus.client.render.Render2D;



import java.util.ArrayList;

import java.util.EnumMap;

import java.util.HashMap;

import java.util.List;

import java.util.Locale;

import net.minecraft.client.MinecraftClient;

import net.minecraft.client.gui.DrawContext;

import net.minecraft.client.gui.ScreenRect;

import org.lwjgl.glfw.GLFW;



public final class CosmeticsSelectionScreen {

   private final HashMap<String, AnimatedValue> map = new HashMap<>();

   private final HashMap<Integer, AnimatedValue> map2 = new HashMap<>();

   private final HashMap<Integer, AnimatedValue> map3 = new HashMap<>();

   private final EnumMap<CosmeticCategory, AnimatedValue> map4 = new EnumMap<>(CosmeticCategory.class);

   private final EnumMap<CosmeticCategory, AnimatedValue> map5 = new EnumMap<>(CosmeticCategory.class);

   private final EnumMap<CosmeticCategory, List<CosmeticEntry>> map6 = new EnumMap<>(CosmeticCategory.class);

   private final EnumMap<CosmeticCategory, String> map7 = new EnumMap<>(CosmeticCategory.class);

   private final HashMap<Integer, CosmeticsSelectionScreen.DataRecord> map8 = new HashMap<>();

   private final ArrayList<CosmeticsSelectionScreen.DataRecord2> list = new ArrayList<>();

   private final CosmeticsSelectionScreen.SubContext3 nested0538 = new CosmeticsSelectionScreen.SubContext3();

   private final CosmeticsSelectionScreen.SubContext3 nested05382 = new CosmeticsSelectionScreen.SubContext3();

   private final CosmeticsSelectionScreen.SubContext3 nested05383 = new CosmeticsSelectionScreen.SubContext3();

   private float cosClipX0;

   private float cosClipY0;

   private float cosClipX1;

   private float cosClipY1;

   private String strVal;

   private int intVal = -1;

   private float floatVal;

   private boolean flag;

   private String strVal2 = "";

   private float floatVal2;

   private float floatVal3;

   private float floatVal4;

   private long timeMillis = System.nanoTime() / 1000000L;

   private long timeMillis2;

   private Runnable runnable2 = () -> {};

   private float floatVal5;

   private float floatVal6;

   private float floatVal7;

   private float floatVal8;

   private float floatVal9;

   private float floatVal10 = 1.0F;

   private float floatVal11 = -1.0F;

   private float floatVal12;

   private float floatVal13;

   private CosmeticCategory cosmeticCategory3 = CosmeticCategory.HAT;

   private CosmeticCategory cosmeticCategory4 = CosmeticCategory.HAT;

   private long timeMillis3;

   private CosmeticEntry cosmeticEntry2;

   private CosmeticEntry cosmeticEntry3;

   private boolean flag2;

   private float floatVal14;



   private float computeFloat(String local7761_1, boolean local7761_2) {

      AnimatedValue animatedValue = this.map.computeIfAbsent(local7761_1, local7766_0 -> new AnimatedValue(140L));

      animatedValue.processData(local7761_2 ? 1.0 : 0.0);

      return (float)animatedValue.computeDouble();

   }



   public boolean matchesCriteria(int local7762_1) {

      if (!this.flag) {

         return false;

      }



      if (GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), 341) == 1 && local7762_1 == 86) {

         this.strVal2 = this.strVal2 + MinecraftClient.getInstance().keyboard.getClipboard().replaceAll("[\\r\\n\\t]", "");

         if (this.strVal2.length() > 24) {

            this.strVal2 = this.strVal2.substring(0, 24);

         }



         return true;

      } else if (local7762_1 == 259) {

         if (!this.strVal2.isEmpty()) {

            this.strVal2 = this.strVal2.substring(0, this.strVal2.length() - 1);

         }



         this.floatVal2 = 0.0F;

         return true;

      } else {

         if (local7762_1 != 257 && local7762_1 != 335 && local7762_1 != 256) {

            return false;

         }



         this.flag = false;

         return true;

      }

   }



   public void processData(Runnable runnable) {

      this.timeMillis2 = System.currentTimeMillis();

      this.timeMillis3 = this.timeMillis2;

      this.runnable2 = runnable;

      this.flag = false;

      this.flag2 = false;

   }



   private void updateState() {

      if (!this.strVal2.equals(this.strVal) || this.intVal != CosmeticCatalog.computeInt()) {

         boolean local7764_1 = !this.strVal2.equals(this.strVal);

         this.strVal = this.strVal2;

         this.intVal = CosmeticCatalog.computeInt();

         this.map6.clear();



         for (CosmeticCategory cosmeticCategory : CosmeticCategory.values()) {

            List<CosmeticEntry> local7764_6 = this.getList(cosmeticCategory);

            this.map6.put(cosmeticCategory, local7764_6);

            this.map7.put(cosmeticCategory, String.valueOf(local7764_6.size()));

         }



         if (local7764_1 && !this.strVal2.isBlank() && this.map6.get(this.cosmeticCategory3).isEmpty()) {

            for (CosmeticCategory cosmeticCategory2 : CosmeticCategory.values()) {

               if (!this.map6.get(cosmeticCategory2).isEmpty()) {

                  this.processData5(cosmeticCategory2);

                  break;

               }

            }

         }

      }

   }



   private void processData2(DrawContext drawContext, CosmeticEntry cosmeticEntry, float local7765_2, float local7765_3, float local7765_4, float local7765_5) {

      float local7765_6 = 158.0F * this.floatVal10;

      float local7765_7 = 29.0F * this.floatVal10;

      float local7765_8 = this.nested05383

         .computeFloat7(

            FontFace.SEMI_BOLD,

            cosmeticEntry == null ? "Ничего не надето" : this.formatText(cosmeticEntry),

            local7765_2 + 30.0F * this.floatVal10,

            local7765_3 + 14.5F * this.floatVal10,

            6.5F * this.floatVal10,

            cosmeticEntry == null ? UiTheme.intVal30 : UiTheme.intVal,

            4.0F * this.floatVal10,

            240L

         );

      FontText.processData3(

         FontFace.SEMI,

         cosmeticEntry == null ? "Выберите предмет" : "Надето",

         local7765_2 + 30.0F * this.floatVal10,

         local7765_3 + 5.5F * this.floatVal10,

         5.5F * this.floatVal10,

         UiTheme.intVal30

      );

      float local7765_9 = cosmeticEntry == null ? 0.0F : UiTheme.computeFloat(local7765_8);

      if (cosmeticEntry == null) {

         UiTheme.processData3(

            this.cosmeticCategory3.formatText4(),

            local7765_2 + 14.5F * this.floatVal10,

            local7765_3 + 14.5F * this.floatVal10,

            9.0F * this.floatVal10,

            9.0F * this.floatVal10,

            UiTheme.computeInt(UiTheme.intVal, 0.2F)

         );

      } else {

         CosmeticPreviewAtlas.AtlasEntry local7765_10 = CosmeticPreviewAtlas.getNested0499(cosmeticEntry);

         float local7765_11 = 18.0F * this.floatVal10 * Math.max(0.0F, local7765_9);

         if (local7765_10 != null && local7765_11 > 0.5F) {

            Render2D.processData39(

               local7765_10.atlasTexture(),

               local7765_2 + 14.5F * this.floatVal10 - local7765_11 / 2.0F,

               local7765_3 + 14.5F * this.floatVal10 - local7765_11 / 2.0F,

               local7765_11,

               local7765_11,

               0.0F,

               local7765_10.u0(),

               local7765_10.v0(),

               local7765_10.u1(),

               local7765_10.v1(),

               UiTheme.intVal

            );

         }



         float local7765_12 = this.computeFloat(

            "cosTakeOff",

            UiTheme.matchesCriteria(

               local7765_4,

               local7765_5,

               local7765_2 + local7765_6 - 22.0F * this.floatVal10,

               local7765_3 + 4.5F * this.floatVal10,

               18.0F * this.floatVal10,

               20.0F * this.floatVal10

            )

         );

         UiTheme.processData7(

            "c",

            local7765_2 + local7765_6 - 13.0F * this.floatVal10,

            local7765_3 + 14.5F * this.floatVal10,

            5.5F * this.floatVal10,

            UiTheme.computeInt2(UiTheme.intVal29, UiTheme.intVal23, local7765_12)

         );

         this.list

            .add(

               new CosmeticsSelectionScreen.DataRecord2(

                  local7765_2 + local7765_6 - 22.0F * this.floatVal10,

                  local7765_3 + 4.5F * this.floatVal10,

                  18.0F * this.floatVal10,

                  20.0F * this.floatVal10,

                  false,

                  () -> CosmeticStore.processData2(this.cosmeticCategory3)

               )

            );

      }

   }



   private void processData3(float local7767_1, float local7767_2, float local7767_3, float local7767_4) {

      float local7767_5 = this.computeFloat2(0);

      Render2D.processData46(local7767_5);

      float local7767_6 = local7767_1 - (1.0F - local7767_5) * 14.0F * this.floatVal10;

      float local7767_7 = this.computeFloat(

         "cosBack",

         UiTheme.matchesCriteria(

            local7767_3,

            local7767_4,

            local7767_6 - 4.0F * this.floatVal10,

            local7767_2 + 105.0F * this.floatVal10,

            18.0F * this.floatVal10,

            18.0F * this.floatVal10

         )

      );

      UiTheme.processData3(

         "assets/horus/textures/gui/mainmenu/icons/back.png",

         local7767_6 + 4.75F * this.floatVal10 - local7767_7 * 1.5F * this.floatVal10,

         local7767_2 + 113.75F * this.floatVal10,

         9.5F * this.floatVal10,

         9.5F * this.floatVal10,

         UiTheme.computeInt2(UiTheme.intVal29, UiTheme.intVal, local7767_7)

      );

      this.list

         .add(

            new CosmeticsSelectionScreen.DataRecord2(

               local7767_6 - 4.0F * this.floatVal10,

               local7767_2 + 105.0F * this.floatVal10,

               18.0F * this.floatVal10,

               18.0F * this.floatVal10,

               false,

               this.runnable2

            )

         );

      FontText.processData3(

         FontFace.SEMI,

         "Horus client  /  Главная  /  Косметика",

         local7767_6 + 15.5F * this.floatVal10,

         local7767_2 + 110.5F * this.floatVal10,

         6.5F * this.floatVal10,

         UiTheme.intVal29

      );

      FontText.processData3(

         FontFace.SEMI, "Косметика.", local7767_6, local7767_2 + 124.5F * this.floatVal10, 20.0F * this.floatVal10, UiTheme.intVal

      );

      FontText.processData3(

         FontFace.SEMI,

         "Собирайте образ - предметы применяются сразу",

         local7767_6,

         local7767_2 + 159.5F * this.floatVal10,

         7.5F * this.floatVal10,

         UiTheme.intVal29

      );

      Render2D.updateState();

   }



   private float computeFloat2(int local7768_1) {

      return UiTheme.computeFloat2(UiTheme.computeFloat3(this.timeMillis2, 40L + local7768_1 * 55L, 420L));

   }



   private void processData4(String local7770_1, float local7770_2, float local7770_3, float local7770_4, float local7770_5, boolean local7770_6) {

      FontText.processData3(FontFace.SEMI, local7770_1, local7770_2, local7770_3, 6.0F * this.floatVal10, UiTheme.intVal30);

      float local7770_7 = local7770_2 + 226.5F * this.floatVal10;

      if (local7770_6) {

         String local7770_8 = "Снять всё";

         float local7770_9 = FontText.computeFloat(FontFace.SEMI, local7770_8, 6.0F * this.floatVal10);

         float local7770_10 = this.computeFloat(

            "cosClear",

            UiTheme.matchesCriteria(

               local7770_4,

               local7770_5,

               local7770_2 + 226.5F * this.floatVal10 - local7770_9 - 9.0F * this.floatVal10,

               local7770_3 - 4.0F * this.floatVal10,

               local7770_9 + 9.0F * this.floatVal10,

               14.0F * this.floatVal10

            )

         );

         UiTheme.processData7(

            "c",

            local7770_2 + 226.5F * this.floatVal10 - local7770_9 - 5.5F * this.floatVal10,

            local7770_3 + 3.0F * this.floatVal10,

            5.0F * this.floatVal10,

            UiTheme.computeInt2(UiTheme.intVal29, UiTheme.intVal, local7770_10)

         );

         FontText.processData3(

            FontFace.SEMI,

            local7770_8,

            local7770_2 + 226.5F * this.floatVal10 - local7770_9,

            local7770_3,

            6.0F * this.floatVal10,

            UiTheme.computeInt2(UiTheme.intVal29, UiTheme.intVal, local7770_10)

         );

         this.list

            .add(

               new CosmeticsSelectionScreen.DataRecord2(

                  local7770_2 + 226.5F * this.floatVal10 - local7770_9 - 9.0F * this.floatVal10,

                  local7770_3 - 4.0F * this.floatVal10,

                  local7770_9 + 9.0F * this.floatVal10,

                  14.0F * this.floatVal10,

                  false,

                  CosmeticStore::updateState3

               )

            );

         local7770_7 -= local7770_9 + 11.0F * this.floatVal10;

      }



      float local7770_8 = local7770_2 + FontText.computeFloat(FontFace.SEMI, local7770_1, 6.0F * this.floatVal10) + 6.0F * this.floatVal10;

      if (local7770_7 - local7770_8 > 2.0F * this.floatVal10) {

         Render2D.processData51(

            local7770_8,

            local7770_3 + 3.0F * this.floatVal10,

            local7770_7 - local7770_8,

            0.5F * this.floatVal10,

            0.25F * this.floatVal10,

            UiTheme.computeInt(UiTheme.intVal, 0.12F)

         );

      }

   }



   public boolean matchesCriteria2(float local7771_1, float local7771_2) {

      if (!this.flag2) {

         return false;

      }



      float local7771_3 = this.floatVal6 - this.floatVal5;

      float local7771_4 = local7771_3 / this.floatVal10;

      float local7771_5 = Math.max(24.0F * this.floatVal10, local7771_3 * (local7771_4 / Math.max(local7771_4, this.floatVal)));

      this.floatVal2 = this.floatVal3 = computeFloat5((local7771_2 - this.floatVal14 - this.floatVal5) / Math.max(1.0F, local7771_3 - local7771_5), 0.0F, 1.0F)

         * this.floatVal4;

      return true;

   }



   private void processData5(CosmeticCategory cosmeticCategory) {

      if (cosmeticCategory != this.cosmeticCategory3) {

         this.cosmeticCategory4 = this.cosmeticCategory3;

         this.cosmeticCategory3 = cosmeticCategory;

         this.floatVal2 = this.floatVal3 = 0.0F;

         this.timeMillis3 = System.currentTimeMillis();

      }

   }



   private static float computeFloat3(float local7774_0, float local7774_1, float local7774_2) {

      local7774_0 += (local7774_1 - local7774_0) * (1.0F - (float)Math.exp(-local7774_2 * 22.0F));

      return Math.abs(local7774_1 - local7774_0) < 0.1F ? local7774_1 : local7774_0;

   }



   private void processData6(

      DrawContext drawContext,
      CosmeticEntry cosmeticEntry, float local7776_2, float local7776_3, float local7776_4, float local7776_5, boolean local7776_6, int local7776_7

   ) {

      float local7776_8 = Math.min(

         computeFloat5((local7776_3 + 80.0F * this.floatVal10 - this.floatVal5) / (14.0F * this.floatVal10), 0.0F, 1.0F),

         computeFloat5((this.floatVal6 - local7776_3) / (14.0F * this.floatVal10), 0.0F, 1.0F)

      );

      if (!(local7776_8 <= 0.01F)) {

         boolean local7776_9 = cosmeticEntry.beta() && !local7776_6;

         boolean local7776_10 = CosmeticStore.matchesCriteria(cosmeticEntry);

         boolean local7776_11 = !this.flag2

            && UiTheme.matchesCriteria(local7776_4, local7776_5, local7776_2, local7776_3, 56.0F * this.floatVal10, 80.0F * this.floatVal10)

            && local7776_5 >= this.floatVal5

            && local7776_5 <= this.floatVal6;

         if (local7776_11) {

            this.cosmeticEntry2 = cosmeticEntry;

         }



         float local7776_12 = this.computeFloat6(this.map2, cosmeticEntry.id(), local7776_11 && !local7776_9);

         float local7776_13 = this.computeFloat6(this.map3, cosmeticEntry.id(), local7776_10);

         float local7776_14 = UiTheme.computeFloat2(UiTheme.computeFloat3(this.timeMillis3, Math.min(Math.max(local7776_7, 0), 27) * 9L, 240L))

            * local7776_8;

         boolean local7776_15 = local7776_14 < 0.999F;

         if (local7776_15) {

            Render2D.processData46(local7776_14);

         }



         float local7776_16 = 38.0F * this.floatVal10;

         float local7776_17 = local7776_2 + (56.0F * this.floatVal10 - local7776_16) / 2.0F;

         float local7776_18 = local7776_3 + 5.0F * this.floatVal10;

         CosmeticPreviewAtlas.AtlasEntry local7776_19 = CosmeticPreviewAtlas.getNested0499(cosmeticEntry);

         if (local7776_19 != null) {

            Render2D.processData39(

               local7776_19.atlasTexture(),

               local7776_17,

               local7776_18,

               local7776_16,

               local7776_16,

               0.0F,

               local7776_19.u0(),

               local7776_19.v0(),

               local7776_19.u1(),

               local7776_19.v1(),

               UiTheme.computeInt(UiTheme.intVal, local7776_9 ? 0.5F : 1.0F)

            );

         } else {

            Render2D.processData51(

               local7776_17 + 8.0F * this.floatVal10,

               local7776_18 + 8.0F * this.floatVal10,

               local7776_16 - 16.0F * this.floatVal10,

               local7776_16 - 16.0F * this.floatVal10,

               5.0F * this.floatVal10,

               UiTheme.computeInt(

                  UiTheme.intVal, 0.03F + 0.025F * (float)Math.sin((float)(System.currentTimeMillis() % 1400L) / 1400.0F * Math.PI * 2.0)

               )

            );

         }



         CosmeticsSelectionScreen.DataRecord local7776_20 = this.getNested0536(cosmeticEntry);

         FontText.processData3(

            FontFace.SEMI,

            local7776_20.text(),

            local7776_2 + (56.0F * this.floatVal10 - local7776_20.width()) / 2.0F,

            local7776_3 + 47.0F * this.floatVal10,

            5.5F * this.floatVal10,

            local7776_9 ? UiTheme.computeInt(UiTheme.intVal, 0.42F) : UiTheme.intVal

         );

         float local7776_21 = local7776_2 + 4.0F * this.floatVal10;

         float local7776_22 = local7776_3 + 62.0F * this.floatVal10;

         float local7776_23 = 48.0F * this.floatVal10;

         float local7776_24 = 14.0F * this.floatVal10;

         if (local7776_9) {

            UiTheme.processData(

               local7776_21,

               local7776_22,

               local7776_23,

               local7776_24,

               5.0F * this.floatVal10,

               UiTheme.computeInt(UiTheme.intVal22, 0.07F),

               UiTheme.computeInt(UiTheme.intVal22, 0.2F)

            );

            float local7776_25 = FontText.computeFloat(FontFace.SEMI_BOLD, "Бета", 5.5F * this.floatVal10);

            float local7776_26 = local7776_2 + (56.0F * this.floatVal10 - local7776_25 - 8.0F * this.floatVal10) / 2.0F;

            UiTheme.processData7(

               "Z", local7776_26 + 2.5F * this.floatVal10, local7776_22 + 7.0F * this.floatVal10, 5.0F * this.floatVal10, UiTheme.intVal22

            );

            FontText.processData3(

               FontFace.SEMI_BOLD,

               "Бета",

               local7776_26 + 8.0F * this.floatVal10,

               local7776_22 + 4.5F * this.floatVal10,

               5.5F * this.floatVal10,

               UiTheme.intVal22

            );

         } else {

            UiTheme.processData(

               local7776_21,

               local7776_22,

               local7776_23,

               local7776_24,

               5.0F * this.floatVal10,

               UiTheme.computeInt2(

                  UiTheme.computeInt(UiTheme.intVal, 0.031F + 0.05F * local7776_12),

                  ((UiTheme.intVal7 & 0x00FFFFFF) >= 0xE0E0E0) ? UiTheme.computeInt(-1, 0.18F) : UiTheme.intVal7,

                  local7776_13

               ),

               UiTheme.computeInt2(

                  UiTheme.computeInt(UiTheme.intVal, 0.078F + 0.09F * local7776_12),

                  ((UiTheme.intVal7 & 0x00FFFFFF) >= 0xE0E0E0) ? UiTheme.computeInt(-1, 0.60F) : UiTheme.intVal34,

                  local7776_13

               )

            );

            FontText.processData3(

               FontFace.SEMI_BOLD,

               local7776_10 ? "Снять" : "Надеть",

               local7776_2 + (56.0F * this.floatVal10 - (local7776_10 ? this.floatVal13 : this.floatVal12)) / 2.0F,

               local7776_22 + 4.5F * this.floatVal10,

               5.5F * this.floatVal10,

               UiTheme.computeInt2(

                  UiTheme.computeInt2(UiTheme.intVal28, UiTheme.intVal, local7776_12),

                  UiTheme.intVal10,

                  local7776_13

               )

            );

            this.list

               .add(

                  new CosmeticsSelectionScreen.DataRecord2(

                     local7776_2, local7776_3, 56.0F * this.floatVal10, 80.0F * this.floatVal10, true, () -> CosmeticStore.processData(cosmeticEntry)

                  )

               );

         }



         if (local7776_15) {

            Render2D.updateState();

         }

      }

   }



   private void processData7(CosmeticCategory cosmeticCategory, float local7778_2, float local7778_3, float local7778_4, float local7778_5) {

      float local7778_6 = this.computeFloat4(

         this.map4,

         cosmeticCategory,

         UiTheme.matchesCriteria(local7778_4, local7778_5, local7778_2, local7778_3, 226.5F * this.floatVal10, 19.5F * this.floatVal10)

      );

      float local7778_7 = this.computeFloat4(this.map5, cosmeticCategory, cosmeticCategory == this.cosmeticCategory3);

      UiTheme.processData(

         local7778_2,

         local7778_3,

         226.5F * this.floatVal10,

         19.5F * this.floatVal10,

         6.0F * this.floatVal10,

         UiTheme.computeInt2(

            UiTheme.computeInt(UiTheme.intVal, 0.031F + 0.04F * local7778_6),

            UiTheme.computeInt(UiTheme.intVal7, 0.16F),

            local7778_7

         ),

         UiTheme.computeInt2(

            UiTheme.computeInt(UiTheme.intVal, 0.078F + 0.08F * local7778_6),

            UiTheme.computeInt(UiTheme.intVal34, 0.6F),

            local7778_7

         )

      );

      UiTheme.processData3(

         cosmeticCategory.formatText4(),

         local7778_2 + 11.0F * this.floatVal10,

         local7778_3 + 9.75F * this.floatVal10,

         7.0F * this.floatVal10,

         7.0F * this.floatVal10,

         UiTheme.computeInt2(

            UiTheme.computeInt2(UiTheme.intVal29, UiTheme.intVal, local7778_6),

            UiTheme.intVal8,

            local7778_7

         )

      );

      FontText.processData3(

         FontFace.SEMI,

         cosmeticCategory.formatText(),

         local7778_2 + 20.0F * this.floatVal10,

         local7778_3 + 6.25F * this.floatVal10,

         7.0F * this.floatVal10,

         UiTheme.computeInt2(UiTheme.intVal28, UiTheme.intVal, Math.max(local7778_6, local7778_7))

      );

      String orDefault = this.map7.getOrDefault(cosmeticCategory, "0");

      float local7778_9 = FontText.computeFloat(FontFace.SEMI, orDefault, 6.0F * this.floatVal10);

      FontText.processData3(

         FontFace.SEMI,

         orDefault,

         local7778_2 + 218.5F * this.floatVal10 - local7778_9,

         local7778_3 + 6.75F * this.floatVal10,

         6.0F * this.floatVal10,

         UiTheme.computeInt(UiTheme.intVal, 0.34F + 0.3F * local7778_7)

      );

      CosmeticEntry cosmeticEntry = CosmeticStore.getCosmeticEntry(cosmeticCategory);

      if (cosmeticEntry != null) {

         float local7778_11 = local7778_2 + 218.5F * this.floatVal10 - local7778_9 - 19.0F * this.floatVal10;

         CosmeticPreviewAtlas.AtlasEntry local7778_12 = CosmeticPreviewAtlas.getNested0499(cosmeticEntry);

         if (local7778_12 != null) {

            Render2D.processData39(

               local7778_12.atlasTexture(),

               local7778_11 + 1.0F * this.floatVal10,

               local7778_3 + 4.0F * this.floatVal10,

               11.5F * this.floatVal10,

               11.5F * this.floatVal10,

               0.0F,

               local7778_12.u0(),

               local7778_12.v0(),

               local7778_12.u1(),

               local7778_12.v1(),

               UiTheme.intVal

            );

         }

      }



      this.list

         .add(

            new CosmeticsSelectionScreen.DataRecord2(

               local7778_2, local7778_3, 226.5F * this.floatVal10, 19.5F * this.floatVal10, false, () -> this.processData5(cosmeticCategory)

            )

         );

   }



   private float computeFloat4(EnumMap<CosmeticCategory, AnimatedValue> local7779_1, CosmeticCategory cosmeticCategory, boolean local7779_3) {

      AnimatedValue animatedValue = local7779_1.computeIfAbsent(cosmeticCategory, cosmeticCategory2 -> new AnimatedValue(160L));

      animatedValue.processData(local7779_3 ? 1.0 : 0.0);

      return (float)animatedValue.computeDouble();

   }



   private String formatText(CosmeticEntry cosmeticEntry) {

      CosmeticsSelectionScreen.DataRecord local7780_2 = this.map8.get(-cosmeticEntry.id() - 1);

      if (local7780_2 != null) {

         return local7780_2.text();

      }



      String local7780_3 = cosmeticEntry.name();

      float local7780_4 = 100.0F * this.floatVal10;

      float local7780_5 = 6.5F * this.floatVal10;



      while (FontText.computeFloat(FontFace.SEMI_BOLD, local7780_3, local7780_5) > local7780_4 && local7780_3.length() > 3) {

         local7780_3 = local7780_3.substring(0, local7780_3.length() - 2) + "…";

      }



      this.map8.put(-cosmeticEntry.id() - 1, new CosmeticsSelectionScreen.DataRecord(local7780_3, 0.0F));

      return local7780_3;

   }



   private CosmeticsSelectionScreen.DataRecord getNested0536(CosmeticEntry cosmeticEntry) {

      CosmeticsSelectionScreen.DataRecord local7781_2 = this.map8.get(cosmeticEntry.id());

      if (local7781_2 != null) {

         return local7781_2;

      }



      String local7781_3 = cosmeticEntry.name().replaceFirst("^[^—]+ — ", "");

      float local7781_4 = 51.0F * this.floatVal10;

      float local7781_5 = 5.5F * this.floatVal10;



      while (FontText.computeFloat(FontFace.SEMI, local7781_3, local7781_5) > local7781_4 && local7781_3.length() > 3) {

         local7781_3 = local7781_3.substring(0, local7781_3.length() - 2) + "…";

      }



      CosmeticsSelectionScreen.DataRecord local7781_6 = new CosmeticsSelectionScreen.DataRecord(

         local7781_3, FontText.computeFloat(FontFace.SEMI, local7781_3, local7781_5)

      );

      this.map8.put(cosmeticEntry.id(), local7781_6);

      return local7781_6;

   }



   public boolean matchesCriteria3(float local7782_1, float local7782_2, float local7782_3) {

      if (!UiTheme.matchesCriteria(local7782_1, local7782_2, this.floatVal7, this.floatVal8, 592.0F * this.floatVal10, 396.0F * this.floatVal10)) {

         return false;

      }



      this.floatVal2 = computeFloat5(this.floatVal2 - local7782_3 * 87.0F, 0.0F, this.floatVal4);

      return true;

   }



   private void processData8(float local7783_1, float local7783_2, float local7783_3, float local7783_4) {

      float local7783_5 = this.computeFloat2(2);

      Render2D.processData46(local7783_5);

      float local7783_6 = local7783_1 - (1.0F - local7783_5) * 14.0F * this.floatVal10;

      float local7783_7 = local7783_2 + 214.0F * this.floatVal10;

      this.processData4("Образ", local7783_6, local7783_7, local7783_3, local7783_4, true);

      local7783_7 += 13.0F * this.floatVal10;

      boolean local7783_8 = false;



      for (CosmeticCategory cosmeticCategory : CosmeticCategory.values()) {

         if (cosmeticCategory == CosmeticCategory.SHIELD) {
            continue;
         }

         if (cosmeticCategory.checkState() && !local7783_8) {

            local7783_8 = true;

            local7783_7 += 5.0F * this.floatVal10;

            this.processData4("Оружие", local7783_6, local7783_7, local7783_3, local7783_4, false);

            local7783_7 += 13.0F * this.floatVal10;

         }



         this.processData7(cosmeticCategory, local7783_6, local7783_7, local7783_3, local7783_4);

         local7783_7 += 22.5F * this.floatVal10;

      }



      Render2D.updateState();

   }



   public boolean matchesCriteria4(int local7786_1) {

      if (this.flag && local7786_1 >= 32 && local7786_1 <= 65535) {

         if (this.strVal2.length() < 24) {

            this.strVal2 = this.strVal2 + (char)local7786_1;

         }



         this.floatVal2 = 0.0F;

         return true;

      } else {

         return false;

      }

   }



   public void renderElement(

      DrawContext drawContext,

      float local7787_2,

      float local7787_3,

      float local7787_4,

      float local7787_5,

      float local7787_6,

      float local7787_7,

      float local7787_8

   ) {

      this.floatVal10 = local7787_3;

      float local7787_9 = Math.min(0.05F, (float)(System.nanoTime() / 1000000L - this.timeMillis) / 1000.0F);

      this.timeMillis = System.nanoTime() / 1000000L;

      this.list.clear();

      this.cosmeticEntry3 = this.cosmeticEntry2;

      this.cosmeticEntry2 = null;

      if (this.floatVal11 != local7787_3) {

         this.map8.clear();

         this.floatVal11 = local7787_3;

         this.floatVal12 = FontText.computeFloat(FontFace.SEMI_BOLD, "Надеть", 5.5F * local7787_3);

         this.floatVal13 = FontText.computeFloat(FontFace.SEMI_BOLD, "Снять", 5.5F * local7787_3);

      }



      float local7787_10 = local7787_2 + 51.5F * local7787_3;

      this.updateState();

      this.processData3(local7787_10, local7787_4, local7787_7, local7787_8);

      this.processData9(local7787_10, local7787_4, local7787_7, local7787_8);

      this.processData8(local7787_10, local7787_4, local7787_7, local7787_8);

      this.renderElement2(drawContext, local7787_2, local7787_4, local7787_5, local7787_6, local7787_7, local7787_8, local7787_9);

   }



   public void updateState2() {

      this.flag = false;

      this.flag2 = false;

   }



   private static float computeFloat5(float local7790_0, float local7790_1, float local7790_2) {

      return Math.max(local7790_1, Math.min(local7790_2, local7790_0));

   }



   private float computeFloat6(HashMap<Integer, AnimatedValue> local7791_1, int local7791_2, boolean local7791_3) {

      AnimatedValue animatedValue = local7791_1.computeIfAbsent(local7791_2, local7785_0 -> new AnimatedValue(140L));

      animatedValue.processData(local7791_3 ? 1.0 : 0.0);

      return (float)animatedValue.computeDouble();

   }



   public boolean matchesCriteria5(float local7793_1, float local7793_2) {

      boolean local7793_3 = this.flag;

      this.flag = false;

      if (this.floatVal4 > 0.5F

         && UiTheme.matchesCriteria(

            local7793_1, local7793_2, this.floatVal9 - 4.0F * this.floatVal10, this.floatVal5, 10.0F * this.floatVal10, this.floatVal6 - this.floatVal5

         )) {

         float local7793_4 = this.floatVal6 - this.floatVal5;

         float local7793_5 = local7793_4 / this.floatVal10;

         float local7793_6 = Math.max(24.0F * this.floatVal10, local7793_4 * (local7793_5 / Math.max(local7793_5, this.floatVal)));

         float local7793_7 = this.floatVal5 + this.floatVal3 / this.floatVal4 * (local7793_4 - local7793_6);

         this.floatVal14 = local7793_2 >= local7793_7 && local7793_2 <= local7793_7 + local7793_6 ? local7793_2 - local7793_7 : local7793_6 / 2.0F;

         this.flag2 = true;

         this.matchesCriteria2(local7793_1, local7793_2);

         return true;

      }



      for (int index = this.list.size() - 1; index >= 0; index--) {

         CosmeticsSelectionScreen.DataRecord2 local7793_5 = this.list.get(index);

         if ((!local7793_5.clipped() || !(local7793_2 < this.floatVal5) && !(local7793_2 > this.floatVal6))

            && UiTheme.matchesCriteria(local7793_1, local7793_2, local7793_5.x(), local7793_5.y(), local7793_5.w(), local7793_5.h())) {

            local7793_5.action().run();

            return true;

         }

      }



      return local7793_3;

   }



   private static String formatText2(int local7794_0) {

      int local7794_1 = local7794_0 % 100;

      int local7794_2 = local7794_0 % 10;

      return local7794_1 >= 11 && local7794_1 <= 14

         ? "предметов"

         : (local7794_2 == 1 ? "предмет" : (local7794_2 >= 2 && local7794_2 <= 4 ? "предмета" : "предметов"));

   }



   private void renderElement2(

      DrawContext drawContext,

      float local7795_2,

      float local7795_3,

      float local7795_4,

      float local7795_5,

      float local7795_6,

      float local7795_7,

      float local7795_8

   ) {

      float local7795_9 = this.computeFloat2(3);

      Render2D.processData46(local7795_9);

      this.floatVal7 = local7795_2 + 344.0F * this.floatVal10 + (1.0F - local7795_9) * 14.0F * this.floatVal10;

      this.floatVal8 = local7795_3 + 92.0F * this.floatVal10;

      float local7795_10 = 592.0F * this.floatVal10;

      float local7795_11 = 396.0F * this.floatVal10;

      float local7795_12 = this.floatVal8 + 54.0F * this.floatVal10;

      float local7795_13 = this.floatVal7 + 16.0F * this.floatVal10;

      float local7795_14 = this.floatVal8 + 15.0F * this.floatVal10;

      List<CosmeticEntry> local7795_15 = this.map6.getOrDefault(this.cosmeticCategory3, List.of());

      CosmeticEntry cosmeticEntry = CosmeticStore.getCosmeticEntry(this.cosmeticCategory3);

      float local7795_17 = UiTheme.computeFloat2(UiTheme.computeFloat3(this.timeMillis3, 0L, 280L));

      if (local7795_17 < 0.999F && this.cosmeticCategory4 != this.cosmeticCategory3) {

         UiTheme.processData3(

            this.cosmeticCategory4.formatText4(),

            local7795_13 + 13.5F * this.floatVal10,

            local7795_14 + 13.5F * this.floatVal10,

            15.0F * this.floatVal10 * (1.0F - local7795_17 * 0.4F),

            15.0F * this.floatVal10 * (1.0F - local7795_17 * 0.4F),

            UiTheme.computeInt(UiTheme.intVal8, 1.0F - local7795_17)

         );

      }



      UiTheme.processData3(

         this.cosmeticCategory3.formatText4(),

         local7795_13 + 13.5F * this.floatVal10,

         local7795_14 + 13.5F * this.floatVal10,

         15.0F * this.floatVal10 * (0.6F + 0.4F * local7795_17),

         15.0F * this.floatVal10 * (0.6F + 0.4F * local7795_17),

         UiTheme.computeInt(UiTheme.intVal8, local7795_17)

      );

      this.nested0538

         .computeFloat7(

            FontFace.SEMI,

            this.cosmeticCategory3.formatText(),

            local7795_13 + 36.0F * this.floatVal10,

            local7795_14 + 0.5F * this.floatVal10,

            13.0F * this.floatVal10,

            UiTheme.intVal,

            7.0F * this.floatVal10,

            280L

         );

      String local7795_18 = this.cosmeticEntry3 != null

         ? this.cosmeticEntry3.name()

         : this.map7.getOrDefault(this.cosmeticCategory3, "0") + " " + formatText2(local7795_15.size()) + (this.strVal2.isBlank() ? "" : " по запросу");

      this.nested05382

         .computeFloat7(

            FontFace.SEMI,

            local7795_18,

            local7795_13 + 36.0F * this.floatVal10,

            local7795_14 + 17.0F * this.floatVal10,

            6.5F * this.floatVal10,

            this.cosmeticEntry3 != null ? UiTheme.computeInt(UiTheme.intVal, 0.85F) : UiTheme.intVal29,

            3.0F * this.floatVal10,

            170L

         );

      this.processData2(

         drawContext,

         cosmeticEntry,

         this.floatVal7 + local7795_10 - 16.0F * this.floatVal10 - 158.0F * this.floatVal10,

         local7795_14 - 1.0F * this.floatVal10,

         local7795_6,

         local7795_7

      );

      this.floatVal5 = local7795_12 + 8.0F * this.floatVal10;

      this.floatVal6 = this.floatVal8 + local7795_11 - 10.0F * this.floatVal10;

      this.cosClipX0 = local7795_13 - 4.0F * this.floatVal10;

      this.cosClipY0 = this.floatVal5;

      this.cosClipX1 = this.cosClipX0 + 568.0F * this.floatVal10;

      this.cosClipY1 = this.floatVal6;

      float local7795_19 = (this.floatVal6 - this.floatVal5) / this.floatVal10;

      float local7795_20 = 87.0F;

      int local7795_21 = (local7795_15.size() + 9 - 1) / 9;

      this.floatVal = Math.max(0.0F, local7795_21 * local7795_20 - 7.0F + 8.0F);

      this.floatVal4 = Math.max(0.0F, this.floatVal - local7795_19);

      this.floatVal2 = computeFloat5(this.floatVal2, 0.0F, this.floatVal4);

      this.floatVal3 = this.flag2 ? this.floatVal2 : computeFloat3(this.floatVal3, this.floatVal2, local7795_8);

      Render2D.renderElement2(

         drawContext, local7795_13 - 4.0F * this.floatVal10, this.floatVal5, 568.0F * this.floatVal10, this.floatVal6 - this.floatVal5

      );

      int local7795_22 = Math.max(0, (int)Math.floor((this.floatVal3 - 8.0F) / local7795_20));

      int local7795_23 = Math.min(local7795_21 - 1, (int)Math.ceil((this.floatVal3 + local7795_19) / local7795_20));

      boolean local7795_24 = CosmeticAccess.checkState();



      for (int index = local7795_22; index <= local7795_23; index++) {

         for (int index2 = 0; index2 < 9; index2++) {

            int local7795_27 = index * 9 + index2;

            if (local7795_27 >= local7795_15.size()) {

               break;

            }



            this.processData6(

               drawContext,
               local7795_15.get(local7795_27),

               local7795_13 + index2 * 63.0F * this.floatVal10,

               this.floatVal5 + (4.0F + index * local7795_20 - this.floatVal3) * this.floatVal10,

               local7795_6,

               local7795_7,

               local7795_24,

               local7795_27 - local7795_22 * 9

            );

         }

      }



      if (local7795_15.isEmpty()) {

         String local7795_25 = this.strVal2.isBlank() ? "Здесь пока пусто" : "Ничего не найдено";

         float local7795_26 = this.floatVal7 + local7795_10 / 2.0F;

         UiTheme.processData3(

            "assets/horus/textures/gui/mainmenu/icons/search.png",

            local7795_26,

            this.floatVal5 + 120.0F * this.floatVal10,

            14.0F * this.floatVal10,

            14.0F * this.floatVal10,

            UiTheme.computeInt(UiTheme.intVal, 0.22F)

         );

         FontText.processData3(

            FontFace.SEMI,

            local7795_25,

            local7795_26 - FontText.computeFloat(FontFace.SEMI, local7795_25, 7.5F * this.floatVal10) / 2.0F,

            this.floatVal5 + 137.0F * this.floatVal10,

            7.5F * this.floatVal10,

            UiTheme.intVal30

         );

      }



      Render2D.renderElement(drawContext);

      this.floatVal9 = this.floatVal7 + local7795_10 - 9.0F * this.floatVal10;

      if (this.floatVal4 > 0.5F) {

         float local7795_25 = this.floatVal6 - this.floatVal5;

         float local7795_26 = Math.max(24.0F * this.floatVal10, local7795_25 * (local7795_19 / Math.max(local7795_19, this.floatVal)));

         float local7795_27 = this.floatVal5 + this.floatVal3 / this.floatVal4 * (local7795_25 - local7795_26);

         float local7795_28 = this.computeFloat(

            "cosThumb",

            this.flag2

               || UiTheme.matchesCriteria(

                  local7795_6, local7795_7, this.floatVal9 - 3.0F * this.floatVal10, this.floatVal5, 8.0F * this.floatVal10, local7795_25

               )

         );

         Render2D.processData51(

            this.floatVal9,

            this.floatVal5,

            2.0F * this.floatVal10,

            local7795_25,

            1.0F * this.floatVal10,

            UiTheme.computeInt(UiTheme.intVal, 0.05F)

         );

         Render2D.processData51(

            this.floatVal9 - local7795_28 * 0.5F * this.floatVal10,

            local7795_27,

            (2.0F + local7795_28) * this.floatVal10,

            local7795_26,

            1.5F * this.floatVal10,

            UiTheme.computeInt2(UiTheme.intVal31, UiTheme.computeInt(UiTheme.intVal34, 0.9F), local7795_28)

         );

      }



      Render2D.updateState();

   }



   private List<CosmeticEntry> getList(CosmeticCategory cosmeticCategory) {

      List<CosmeticEntry> local7796_2 = CosmeticCatalog.getList(cosmeticCategory);

      if (this.strVal2.isBlank()) {

         return local7796_2;

      }



      String local7796_3 = this.strVal2.toLowerCase(Locale.ROOT);

      List<CosmeticEntry> local7796_4 = new ArrayList<>();



      for (CosmeticEntry cosmeticEntry : local7796_2) {

         if (cosmeticEntry.name().toLowerCase(Locale.ROOT).contains(local7796_3)) {

            local7796_4.add(cosmeticEntry);

         }

      }



      return local7796_4;

   }



   public boolean checkState() {

      boolean local7797_1 = this.flag2;

      this.flag2 = false;

      return local7797_1;

   }



   private void processData9(float local7798_1, float local7798_2, float local7798_3, float local7798_4) {

      float local7798_5 = this.computeFloat2(1);

      Render2D.processData46(local7798_5);

      float local7798_6 = local7798_1 - (1.0F - local7798_5) * 14.0F * this.floatVal10;

      float local7798_7 = local7798_2 + 184.5F * this.floatVal10;

      float local7798_8 = this.computeFloat("cosFieldFocus", this.flag);

      Render2D.processData51(

         local7798_6,

         local7798_7,

         226.5F * this.floatVal10,

         19.5F * this.floatVal10,

         6.0F * this.floatVal10,

         UiTheme.computeInt(UiTheme.intVal, 0.031F)

      );

      Render2D.processData86(

         local7798_6,

         local7798_7,

         226.5F * this.floatVal10,

         19.5F * this.floatVal10,

         6.0F * this.floatVal10,

         0.5F,

         UiTheme.computeInt2(UiTheme.intVal33, UiTheme.computeInt(UiTheme.intVal34, 0.8F), local7798_8)

      );

      UiTheme.processData3(

         "assets/horus/textures/gui/mainmenu/icons/search.png",

         local7798_6 + 8.5F * this.floatVal10,

         local7798_7 + 9.75F * this.floatVal10,

         6.5F * this.floatVal10,

         6.5F * this.floatVal10,

         UiTheme.computeInt2(UiTheme.intVal30, UiTheme.computeInt(UiTheme.intVal34, 0.95F), local7798_8)

      );

      String local7798_9 = this.strVal2;

      boolean local7798_10 = local7798_9.isEmpty() && !this.flag;

      FontText.processData3(

         FontFace.SEMI,

         local7798_10 ? "Найти предмет во всех категориях..." : local7798_9 + (this.flag && System.currentTimeMillis() % 1000L < 500L ? "|" : ""),

         local7798_6 + 19.0F * this.floatVal10,

         local7798_7 + 6.25F * this.floatVal10,

         7.0F * this.floatVal10,

         local7798_9.isEmpty() ? UiTheme.intVal30 : UiTheme.intVal

      );

      this.list

         .add(

            new CosmeticsSelectionScreen.DataRecord2(local7798_6, local7798_7, 226.5F * this.floatVal10, 19.5F * this.floatVal10, false, () -> this.flag = true)

         );

      float local7798_11 = this.computeFloat("cosSearchClearShown", !local7798_9.isEmpty());

      if (local7798_11 > 0.01F) {

         float local7798_12 = this.computeFloat(

            "cosSearchClear",

            UiTheme.matchesCriteria(

               local7798_3, local7798_4, local7798_6 + 207.0F * this.floatVal10, local7798_7, 19.5F * this.floatVal10, 19.5F * this.floatVal10

            )

         );

         UiTheme.processData7(

            "c",

            local7798_6 + 216.75F * this.floatVal10,

            local7798_7 + 9.75F * this.floatVal10,

            5.5F * this.floatVal10 * local7798_11,

            UiTheme.computeInt(

               UiTheme.computeInt2(UiTheme.intVal29, UiTheme.intVal, local7798_12), local7798_11

            )

         );

         if (!local7798_9.isEmpty()) {

            this.list

               .add(

                  new CosmeticsSelectionScreen.DataRecord2(

                     local7798_6 + 207.0F * this.floatVal10, local7798_7, 19.5F * this.floatVal10, 19.5F * this.floatVal10, false, () -> {

                        this.strVal2 = "";

                        this.floatVal2 = 0.0F;

                     }

                  )

               );

         }

      }



      Render2D.updateState();

   }



   private record DataRecord(String text, float width) {

   }



   private record DataRecord2(float x, float y, float w, float h, boolean clipped, Runnable action) {

   }



   private static final class SubContext3 {

      private String strVal3;

      private String strVal4;

      private long timeMillis4;



      private float computeFloat7(

         FontFace fontFace,

         String local7816_2,

         float local7816_3,

         float local7816_4,

         float local7816_5,

         int local7816_6,

         float local7816_7,

         long local7816_8

      ) {

         if (!local7816_2.equals(this.strVal3)) {

            this.strVal4 = this.strVal3;

            this.strVal3 = local7816_2;

            this.timeMillis4 = System.currentTimeMillis();

         }



         float local7816_10 = UiTheme.computeFloat2(UiTheme.computeFloat3(this.timeMillis4, 0L, local7816_8));

         if (local7816_10 < 0.999F && this.strVal4 != null) {

            Render2D.processData46(1.0F - local7816_10);

            FontText.processData3(fontFace, this.strVal4, local7816_3, local7816_4 - local7816_7 * local7816_10, local7816_5, local7816_6);

            Render2D.updateState();

         }



         Render2D.processData46(local7816_10);

         FontText.processData3(fontFace, this.strVal3, local7816_3, local7816_4 + local7816_7 * (1.0F - local7816_10), local7816_5, local7816_6);

         Render2D.updateState();

         return local7816_10;

      }

   }

}


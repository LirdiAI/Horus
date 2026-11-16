package horus.client.render;

import horus.client.HorusClient;
import horus.client.util.GuiScale;
import horus.client.render.MsdfGlyph;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.resource.Resource;
import org.joml.Matrix3x2f;

final class SmallTextAtlasBaker implements AutoCloseable {
   static final float floatVal = 20.0F;
   static final int intVal = 4;
   private static final SmallTextAtlasBaker.AtlasGlyphKey nested0496 = new SmallTextAtlasBaker.AtlasGlyphKey(null, 0.0F, 0.0F, 0.0F, 0.0F, 0, 0, 0, 0);
   private static final FontRenderContext fontRenderContext = new FontRenderContext(
      new AffineTransform(), RenderingHints.VALUE_TEXT_ANTIALIAS_ON, RenderingHints.VALUE_FRACTIONALMETRICS_OFF
   );
   private static final Font font3 = new Font("SansSerif", 0, 32);
   private static final ExecutorService executorService = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "horus-msdf-face-loader");
      thread.setDaemon(true);
      return thread;
   });
   private static final Map<Integer, Float> map = new ConcurrentHashMap<>();
   private final Map<FontAtlas, Optional<SmallTextAtlasBaker.SubContext2>> map2 = new ConcurrentHashMap<>();
   private final Map<FontAtlas, Optional<Font>> map3 = new ConcurrentHashMap<>();
   private final Set<FontAtlas> set = ConcurrentHashMap.newKeySet();
   private final Map<FontAtlas, Map<Long, SmallTextAtlasBaker.AtlasGlyphKey>> map4 = new HashMap<>();
   private final Map<Long, SmallTextAtlasBaker.AtlasGlyphKey> map5 = new HashMap<>();
   private final List<SmallTextAtlasBaker.SubContext> list = new ArrayList<>();
   private boolean flag;
   private long timeMillis;
   private long timeMillis2 = 2000000L;

   void updateState() {
      this.updateState2();
      this.map2.clear();
      this.map3.clear();
      this.set.clear();
      this.flag = false;
   }

   static boolean matchesCriteria(int local5863_0) {
      return local5863_0 > 32 && !Character.isWhitespace(local5863_0) && Character.isDefined(local5863_0) && font3.canDisplay(local5863_0);
   }

   private void updateState2() {
      for (SmallTextAtlasBaker.SubContext local5864_2 : this.list) {
         local5864_2.close();
      }

      this.list.clear();
      this.map4.clear();
      this.map5.clear();
   }

   private SmallTextAtlasBaker.AtlasGlyphKey getNested0496(
      FontAtlas fontAtlas, int local5866_2, MsdfGlyph msdfGlyph, float local5866_4, float local5866_5
   ) {
      Font font = this.getFont2(fontAtlas);
      if (font != null) {
         SmallTextAtlasBaker.AtlasGlyphKey local5866_7 = this.getNested04962(font, local5866_2, local5866_4, local5866_5);
         if (local5866_7 != null) {
            return local5866_7;
         }
      }

      return this.getNested04964(fontAtlas, msdfGlyph, local5866_4, local5866_5);
   }

   private SmallTextAtlasBaker.AtlasGlyphKey getNested04962(Font font, int local5867_2, float local5867_3, float local5867_4) {
      float local5867_5 = local5867_3 * GuiScale.computeFloat();
      int local5867_6 = 3;
      Font font2 = font.deriveFont(0, local5867_5 * local5867_6);
      GlyphVector glyphVector = font2.createGlyphVector(fontRenderContext, new String(Character.toChars(local5867_2)));
      if (glyphVector.getNumGlyphs() == 0) {
         return nested0496;
      }

      float local5867_9 = local5867_4 * local5867_6;
      Rectangle pixelBounds = glyphVector.getPixelBounds(fontRenderContext, local5867_9, 0.0F);
      if (pixelBounds.width > 0 && pixelBounds.height > 0) {
         int local5867_11 = Math.floorDiv(pixelBounds.x, local5867_6);
         int local5867_12 = Math.floorDiv(pixelBounds.y, local5867_6);
         int local5867_13 = -Math.floorDiv(-(pixelBounds.x + pixelBounds.width), local5867_6);
         int local5867_14 = -Math.floorDiv(-(pixelBounds.y + pixelBounds.height), local5867_6);
         int local5867_15 = local5867_13 - local5867_11 + 4;
         int local5867_16 = local5867_14 - local5867_12 + 4;
         if (local5867_15 <= 1024 && local5867_16 <= 1024) {
            int local5867_17 = local5867_15 * local5867_6;
            int local5867_18 = local5867_16 * local5867_6;
            BufferedImage bufferedImage = new BufferedImage(local5867_17, local5867_18, 2);
            Graphics2D graphics2D = bufferedImage.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            graphics2D.setColor(Color.WHITE);
            graphics2D.drawGlyphVector(glyphVector, (2 - local5867_11) * local5867_6 + local5867_9, (2 - local5867_12) * local5867_6);
            graphics2D.dispose();
            int[] rGB = bufferedImage.getRGB(0, 0, local5867_17, local5867_18, null, 0, local5867_17);
            int[] intArray = new int[local5867_15 * local5867_16];
            int local5867_23 = local5867_6 * local5867_6;

            for (int index = 0; index < local5867_16; index++) {
               for (int index2 = 0; index2 < local5867_15; index2++) {
                  int local5867_26 = 0;
                  int local5867_27 = index * local5867_6 * local5867_17 + index2 * local5867_6;

                  for (int index3 = 0; index3 < local5867_6; index3++) {
                     int local5867_29 = local5867_27 + index3 * local5867_17;

                     for (int index4 = 0; index4 < local5867_6; index4++) {
                        local5867_26 += rGB[local5867_29 + index4] >>> 24 & 0xFF;
                     }
                  }

                  intArray[index * local5867_15 + index2] = computeInt((float)local5867_26 / local5867_23);
               }
            }

            SmallTextAtlasBaker.DataRecord3 local5867_24 = this.getNested0498(local5867_15, local5867_16);
            if (local5867_24 == null) {
               return nested0496;
            }

            local5867_24.page.processData2(intArray, local5867_15, local5867_16, local5867_24.x, local5867_24.y);
            float local5867_25 = 9.765625E-4F;
            return new SmallTextAtlasBaker.AtlasGlyphKey(
               local5867_24.page,
               local5867_24.x * local5867_25,
               local5867_24.y * local5867_25,
               (local5867_24.x + local5867_15) * local5867_25,
               (local5867_24.y + local5867_16) * local5867_25,
               local5867_11 - 2,
               -local5867_12 + 2,
               local5867_15,
               local5867_16
            );
         } else {
            return nested0496;
         }
      } else {
         return nested0496;
      }
   }

   static boolean matchesCriteria2(Matrix3x2f matrix3x2f) {
      float local5868_1 = GuiScale.computeFloat();
      int local5868_2 = GuiScale.computeInt();
      return Math.abs(matrix3x2f.m01()) < 1.0E-4F
         && Math.abs(matrix3x2f.m10()) < 1.0E-4F
         && Math.abs(matrix3x2f.m00() * local5868_2 - local5868_1) < 0.001F
         && Math.abs(matrix3x2f.m11() * local5868_2 - local5868_1) < 0.001F;
   }

   SmallTextAtlasBaker.AtlasGlyphKey getNested04963(int local5869_1, float local5869_2, int local5869_3) {
      long local5869_4 = (long)local5869_1 << 16 | (long)Math.round(local5869_2 * 4.0F) << 2 | local5869_3;
      SmallTextAtlasBaker.AtlasGlyphKey local5869_6 = this.map5.get(local5869_4);
      if (local5869_6 == null) {
         long local5869_7 = System.nanoTime();
         local5869_6 = this.getNested04962(font3, local5869_1, local5869_2, local5869_3 / 4.0F);
         this.timeMillis2 = this.timeMillis2 - (System.nanoTime() - local5869_7);
         this.map5.put(local5869_4, local5869_6);
      }

      if (local5869_6.page() != null) {
         local5869_6.page().timeMillis3 = this.timeMillis;
      }

      return local5869_6;
   }

   void updateState3() {
      for (SmallTextAtlasBaker.SubContext local5870_2 : this.list) {
         local5870_2.updateState7();
      }
   }

   private SmallTextAtlasBaker.SubContext2 getNested0497(FontAtlas fontAtlas) {
      Optional<SmallTextAtlasBaker.SubContext2> local5872_2 = this.map2.get(fontAtlas);
      return local5872_2 == null ? null : local5872_2.orElse(null);
   }

   void updateState4() {
      this.timeMillis2 = 2000000L;
      this.timeMillis++;
   }

   private static int computeInt(float local5874_0) {
      double local5874_1 = computeFloat4(local5874_0 / 255.0F);
      return (int)Math.min(255L, Math.round(Math.pow(local5874_1, 0.72) * 255.0));
   }

   boolean matchesCriteria3(FontAtlas fontAtlas, int local5875_2, MsdfGlyph msdfGlyph, float local5875_4, int local5875_5) {
      Map<Long, SmallTextAtlasBaker.AtlasGlyphKey> local5875_6 = this.map4.computeIfAbsent(fontAtlas, fontAtlas2 -> new HashMap<>());
      long local5875_7 = (long)local5875_2 << 16 | (long)Math.round(local5875_4 * 4.0F) << 2 | local5875_5;
      if (local5875_6.containsKey(local5875_7)) {
         return true;
      }

      if (this.timeMillis2 <= 0L) {
         return false;
      }

      long local5875_9 = System.nanoTime();
      local5875_6.put(local5875_7, this.getNested0496(fontAtlas, local5875_2, msdfGlyph, local5875_4, local5875_5 / 4.0F));
      this.timeMillis2 = this.timeMillis2 - (System.nanoTime() - local5875_9);
      return true;
   }

   boolean checkState() {
      return this.timeMillis2 > 0L;
   }

   private static float computeFloat(
      SmallTextAtlasBaker.SubContext2 local5877_0,
      FontAtlas fontAtlas,
      MsdfGlyph msdfGlyph,
      float local5877_3,
      float local5877_4,
      float local5877_5,
      float local5877_6,
      float local5877_7,
      float local5877_8,
      float local5877_9
   ) {
      float local5877_10 = msdfGlyph.planeRight() - msdfGlyph.planeLeft();
      float local5877_11 = msdfGlyph.planeTop() - msdfGlyph.planeBottom();
      float local5877_12 = local5877_3 + (local5877_7 - msdfGlyph.planeLeft()) / local5877_10 * (local5877_4 - local5877_3);
      float local5877_13 = local5877_5 + (msdfGlyph.planeTop() - local5877_8) / local5877_11 * (local5877_6 - local5877_5);
      boolean local5877_14 = local5877_7 < msdfGlyph.planeLeft() + local5877_9
         || local5877_7 > msdfGlyph.planeRight() - local5877_9
         || local5877_8 < msdfGlyph.planeBottom() + local5877_9
         || local5877_8 > msdfGlyph.planeTop() - local5877_9;
      local5877_12 = computeFloat2(local5877_12, local5877_3 + 0.5F, local5877_4 - 0.5F);
      local5877_13 = computeFloat2(local5877_13, local5877_5 + 0.5F, local5877_6 - 0.5F);
      float local5877_15 = local5877_0.computeFloat8(local5877_12, local5877_13);
      float local5877_16 = (local5877_15 - 0.5F) * fontAtlas.computeFloat8();
      return local5877_14 ? Math.min(local5877_16, -0.01F) : local5877_16;
   }

   boolean matchesCriteria4(int local5878_1, float local5878_2, int local5878_3) {
      long local5878_4 = (long)local5878_1 << 16 | (long)Math.round(local5878_2 * 4.0F) << 2 | local5878_3;
      if (this.map5.containsKey(local5878_4)) {
         return true;
      }

      if (this.timeMillis2 <= 0L) {
         return false;
      }

      long local5878_6 = System.nanoTime();
      this.map5.put(local5878_4, this.getNested04962(font3, local5878_1, local5878_2, local5878_3 / 4.0F));
      this.timeMillis2 = this.timeMillis2 - (System.nanoTime() - local5878_6);
      return true;
   }

   private static float computeFloat2(float local5879_0, float local5879_1, float local5879_2) {
      return Math.max(local5879_1, Math.min(local5879_2, local5879_0));
   }

   private SmallTextAtlasBaker.AtlasGlyphKey getNested04964(FontAtlas fontAtlas, MsdfGlyph msdfGlyph, float local5880_3, float local5880_4) {
      SmallTextAtlasBaker.SubContext2 local5880_5 = this.getNested0497(fontAtlas);
      if (local5880_5 == null) {
         return nested0496;
      }

      float local5880_6 = msdfGlyph.planeRight() - msdfGlyph.planeLeft();
      float local5880_7 = msdfGlyph.planeTop() - msdfGlyph.planeBottom();
      if (!(local5880_6 <= 0.0F) && !(local5880_7 <= 0.0F)) {
         float local5880_8 = msdfGlyph.u0() * local5880_5.intVal10;
         float local5880_9 = msdfGlyph.u1() * local5880_5.intVal10;
         float local5880_10 = msdfGlyph.v0() * local5880_5.intVal11;
         float local5880_11 = msdfGlyph.v1() * local5880_5.intVal11;
         float local5880_12 = (local5880_9 - local5880_8) / local5880_6;
         if (local5880_12 <= 0.0F) {
            return nested0496;
         }

         float local5880_13 = local5880_3 * GuiScale.computeFloat();
         int local5880_14 = Math.round(msdfGlyph.planeLeft() * local5880_13 + local5880_4) - 2;
         int local5880_15 = Math.round(msdfGlyph.planeTop() * local5880_13) + 2;
         int local5880_16 = (int)Math.ceil(local5880_6 * local5880_13) + 4 + 2;
         int local5880_17 = (int)Math.ceil(local5880_7 * local5880_13) + 4 + 1;
         if (local5880_16 <= 1024 && local5880_17 <= 1024) {
            float local5880_18 = local5880_13 / local5880_12;
            float local5880_19 = 1.0F / local5880_12;
            int[] intArray = new int[local5880_16 * local5880_17];

            for (int index = 0; index < local5880_17; index++) {
               for (int index2 = 0; index2 < local5880_16; index2++) {
                  float local5880_23 = 0.0F;

                  for (int index3 = 0; index3 < 4; index3++) {
                     for (int index4 = 0; index4 < 4; index4++) {
                        float local5880_26 = (index4 + 0.5F) / 4.0F - 0.5F;
                        float local5880_27 = (index3 + 0.5F) / 4.0F - 0.5F;
                        float local5880_28 = (local5880_14 + index2 + 0.5F + local5880_26 - local5880_4) / local5880_13;
                        float local5880_29 = (local5880_15 - index - 0.5F - local5880_27) / local5880_13;
                        float local5880_30 = computeFloat(
                           local5880_5,
                           fontAtlas,
                           msdfGlyph,
                           local5880_8,
                           local5880_9,
                           local5880_10,
                           local5880_11,
                           local5880_28,
                           local5880_29,
                           local5880_19
                        );
                        float local5880_31 = local5880_30 * local5880_18;
                        local5880_23 += computeFloat4(local5880_31 * 4.0F + 0.5F);
                     }
                  }

                  intArray[index * local5880_16 + index2] = computeInt(local5880_23 / 16.0F * 255.0F);
               }
            }

            SmallTextAtlasBaker.DataRecord3 local5880_21 = this.getNested0498(local5880_16, local5880_17);
            if (local5880_21 == null) {
               return nested0496;
            }

            local5880_21.page.processData2(intArray, local5880_16, local5880_17, local5880_21.x, local5880_21.y);
            float local5880_22 = 9.765625E-4F;
            return new SmallTextAtlasBaker.AtlasGlyphKey(
               local5880_21.page,
               local5880_21.x * local5880_22,
               local5880_21.y * local5880_22,
               (local5880_21.x + local5880_16) * local5880_22,
               (local5880_21.y + local5880_17) * local5880_22,
               local5880_14,
               local5880_15,
               local5880_16,
               local5880_17
            );
         } else {
            return nested0496;
         }
      } else {
         return nested0496;
      }
   }

   @Override
   public void close() {
      this.updateState();
   }

   private static Font getFont(FontAtlas fontAtlas) {
      String local5882_1 = formatText(fontAtlas);
      if (local5882_1 == null) {
         return null;
      }

      try (InputStream resourceAsStream = SmallTextAtlasBaker.class.getResourceAsStream(local5882_1)) {
         if (resourceAsStream == null) {
            HorusClient.logger().warn("[MSDF] Inter OTF not found for small-text bake: {}", local5882_1);
            return null;
         } else {
            return Font.createFont(0, resourceAsStream).deriveFont(0, 1.0F);
         }
      } catch (Exception exception) {
         HorusClient.logger().warn("[MSDF] Failed to load Inter OTF {}", local5882_1, exception);
         return null;
      }
   }

   private void processData(FontAtlas fontAtlas) {
      if (this.set.add(fontAtlas)) {
         executorService.submit(
            () -> {
               try {
                  font3.canDisplay('A');
                  if (MinecraftClient.getInstance() == null) {
                     this.set.remove(fontAtlas);
                     return;
                  }

                  Optional<Font> local5865_2 = Optional.ofNullable(getFont(fontAtlas));
                  this.map2
                     .put(fontAtlas, local5865_2.isPresent() ? Optional.empty() : Optional.ofNullable(SmallTextAtlasBaker.SubContext2.getNested04972(fontAtlas)));
                  this.map3.put(fontAtlas, local5865_2);
               } catch (Throwable throwable) {
                  this.map2.putIfAbsent(fontAtlas, Optional.empty());
                  this.map3.putIfAbsent(fontAtlas, Optional.empty());
               }
            }
         );
      }
   }

   private static String formatText(FontAtlas fontAtlas) {
      String path = fontAtlas.getIdentifier().getPath();
      String local5885_2 = path.substring(path.lastIndexOf(47) + 1);
      if (local5885_2.endsWith(".png")) {
         local5885_2 = local5885_2.substring(0, local5885_2.length() - 4);
      }

      String local5885_3 = "/assets/horus/fonts/inter/";

      return switch (local5885_2.toLowerCase(Locale.ROOT)) {
         case "thin" -> local5885_3 + "Inter-Thin.otf";
         case "mini" -> local5885_3 + "Inter-Light.otf";
         case "semi" -> local5885_3 + "Inter-Medium.otf";
         case "semi_bold" -> local5885_3 + "Inter-SemiBold.otf";
         case "bold" -> local5885_3 + "Inter-Bold.otf";
         default -> null;
      };
   }

   private SmallTextAtlasBaker.SubContext getNested0495() {
      SmallTextAtlasBaker.SubContext local5887_1 = null;

      for (SmallTextAtlasBaker.SubContext local5887_3 : this.list) {
         if (local5887_3.timeMillis3 != this.timeMillis && (local5887_1 == null || local5887_3.timeMillis3 < local5887_1.timeMillis3)) {
            local5887_1 = local5887_3;
         }
      }

      if (local5887_1 == null) {
         return null;
      }

      SmallTextAtlasBaker.SubContext local5887_2 = local5887_1;

      for (Map<Long, SmallTextAtlasBaker.AtlasGlyphKey> local5887_4 : this.map4.values()) {
         local5887_4.values().removeIf(local5861_1 -> local5861_1.page() == local5887_2);
      }

      this.map5.values().removeIf(local5889_1 -> local5889_1.page() == local5887_2);
      local5887_2.updateState6();
      return local5887_2;
   }

   static float computeFloat3(int local5888_0) {
      return map.computeIfAbsent(
         local5888_0,
         local5893_0 -> font3.createGlyphVector(fontRenderContext, new String(Character.toChars(local5893_0))).getGlyphMetrics(0).getAdvance() / 32.0F
      );
   }

   private static float computeFloat4(float local5891_0) {
      return Math.max(0.0F, Math.min(1.0F, local5891_0));
   }

   private Font getFont2(FontAtlas fontAtlas) {
      Optional<Font> local5892_2 = this.map3.get(fontAtlas);
      return local5892_2 == null ? null : local5892_2.orElse(null);
   }

   void updateState5() {
      if (this.flag) {
         this.flag = false;
         this.updateState2();
      }
   }

   static float computeFloat5(float local5895_0) {
      return Math.max(0.5F, Math.round(local5895_0 * 4.0F) / 4.0F);
   }

   SmallTextAtlasBaker.AtlasGlyphKey getNested04965(FontAtlas fontAtlas, int local5896_2, MsdfGlyph msdfGlyph, float local5896_4, int local5896_5) {
      Map<Long, SmallTextAtlasBaker.AtlasGlyphKey> local5896_6 = this.map4.computeIfAbsent(fontAtlas, fontAtlas2 -> new HashMap<>());
      long local5896_7 = (long)local5896_2 << 16 | (long)Math.round(local5896_4 * 4.0F) << 2 | local5896_5;
      SmallTextAtlasBaker.AtlasGlyphKey local5896_9 = local5896_6.get(local5896_7);
      if (local5896_9 == null) {
         long local5896_10 = System.nanoTime();
         local5896_9 = this.getNested0496(fontAtlas, local5896_2, msdfGlyph, local5896_4, local5896_5 / 4.0F);
         this.timeMillis2 = this.timeMillis2 - (System.nanoTime() - local5896_10);
         local5896_6.put(local5896_7, local5896_9);
      }

      if (local5896_9.page() != null) {
         local5896_9.page().timeMillis3 = this.timeMillis;
      }

      return local5896_9;
   }

   private SmallTextAtlasBaker.DataRecord3 getNested0498(int local5897_1, int local5897_2) {
      for (SmallTextAtlasBaker.SubContext local5897_4 : this.list) {
         SmallTextAtlasBaker.DataRecord3 local5897_5 = local5897_4.getNested04982(local5897_1, local5897_2);
         if (local5897_5 != null) {
            return local5897_5;
         }
      }

      if (this.list.size() >= 16) {
         SmallTextAtlasBaker.SubContext local5897_3 = this.getNested0495();
         if (local5897_3 == null) {
            this.flag = true;
            HorusClient.logger().warn("[MSDF] Small-text atlas overflow ({} pages), reset scheduled", this.list.size() + 1);
            return null;
         } else {
            return local5897_3.getNested04982(local5897_1, local5897_2);
         }
      } else {
         SmallTextAtlasBaker.SubContext local5897_3 = new SmallTextAtlasBaker.SubContext();
         this.list.add(local5897_3);
         return local5897_3.getNested04982(local5897_1, local5897_2);
      }
   }

   boolean matchesCriteria5(FontAtlas fontAtlas) {
      Optional<Font> local5899_2 = this.map3.get(fontAtlas);
      Optional<SmallTextAtlasBaker.SubContext2> local5899_3 = this.map2.get(fontAtlas);
      if (local5899_2 != null && local5899_3 != null) {
         return local5899_2.isPresent() || local5899_3.isPresent();
      }

      this.processData(fontAtlas);
      return false;
   }

   static final class SubContext implements AutoCloseable {
      private static int intVal2;
      private final NativeImage nativeImage;
      private final NativeImageBackedTexture nativeImageBackedTexture;
      private final TextureSetup textureSetup;
      private int intVal3 = 1;
      private int intVal4 = 1;
      private int intVal5;
      private long timeMillis3;
      private int intVal6 = Integer.MAX_VALUE;
      private int intVal7 = Integer.MAX_VALUE;
      private int intVal8 = Integer.MIN_VALUE;
      private int intVal9 = Integer.MIN_VALUE;

      @Override
      public void close() {
         this.nativeImageBackedTexture.close();
      }

      private void processData2(int[] intArray, int local5902_2, int local5902_3, int local5902_4, int local5902_5) {
         for (int index = 0; index < local5902_3; index++) {
            for (int index2 = 0; index2 < local5902_2; index2++) {
               int local5902_8 = intArray[index * local5902_2 + index2];
               this.nativeImage.setColor(local5902_4 + index2, local5902_5 + index, local5902_8 << 24 | 16777215);
            }
         }

         this.intVal6 = Math.min(this.intVal6, local5902_4);
         this.intVal7 = Math.min(this.intVal7, local5902_5);
         this.intVal8 = Math.max(this.intVal8, local5902_4 + local5902_2);
         this.intVal9 = Math.max(this.intVal9, local5902_5 + local5902_3);
      }

      private SmallTextAtlasBaker.DataRecord3 getNested04982(int local5903_1, int local5903_2) {
         int local5903_3 = local5903_1 + 1;
         int local5903_4 = local5903_2 + 1;
         if (this.intVal3 + local5903_3 > 1024) {
            this.intVal3 = 1;
            this.intVal4 = this.intVal4 + this.intVal5;
            this.intVal5 = 0;
         }

         if (this.intVal4 + local5903_4 <= 1024 && local5903_3 <= 1024) {
            int local5903_5 = this.intVal3;
            int local5903_6 = this.intVal4;
            this.intVal3 += local5903_3;
            this.intVal5 = Math.max(this.intVal5, local5903_4);
            return new SmallTextAtlasBaker.DataRecord3(this, local5903_5, local5903_6);
         } else {
            return null;
         }
      }

      private void updateState6() {
         this.nativeImage.fillRect(0, 0, 1024, 1024, 0);
         this.intVal3 = 1;
         this.intVal4 = 1;
         this.intVal5 = 0;
         this.intVal6 = 0;
         this.intVal7 = 0;
         this.intVal8 = 1024;
         this.intVal9 = 1024;
      }

      SubContext() {
         this.nativeImage = new NativeImage(1024, 1024, true);
         this.nativeImageBackedTexture = new NativeImageBackedTexture(() -> "horus_msdf_small_atlas_" + intVal2++, this.nativeImage);
         this.textureSetup = TextureSetup.of(this.nativeImageBackedTexture.getGlTextureView(), RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
      }

      private void updateState7() {
         if (this.intVal8 > this.intVal6 && this.intVal9 > this.intVal7) {
            if (this.nativeImageBackedTexture.getGlTexture() != null) {
               int local5906_1 = Math.max(0, this.intVal6);
               int local5906_2 = Math.max(0, this.intVal7);
               int local5906_3 = Math.min(1024, this.intVal8) - local5906_1;
               int local5906_4 = Math.min(1024, this.intVal9) - local5906_2;
               if (local5906_3 > 0 && local5906_4 > 0) {
                  RenderSystem.getDevice()
                     .createCommandEncoder()
                     .writeToTexture(
                        this.nativeImageBackedTexture.getGlTexture(), this.nativeImage, 0, 0, local5906_1, local5906_2, local5906_3, local5906_4, local5906_1, local5906_2
                     );
               }

               this.intVal6 = Integer.MAX_VALUE;
               this.intVal7 = Integer.MAX_VALUE;
               this.intVal8 = Integer.MIN_VALUE;
               this.intVal9 = Integer.MIN_VALUE;
            }
         }
      }

      TextureSetup getTextureSetup() {
         return this.textureSetup;
      }
   }

   record AtlasGlyphKey(
      SmallTextAtlasBaker.SubContext page,
      float u0,
      float v0,
      float u1,
      float v1,
      int offsetX,
      int offsetY,
      int width,
      int height
   ) {
      boolean drawable() {
         return this.page != null && this.width > 0 && this.height > 0;
      }
   }

   private static final class SubContext2 {
      final int intVal10;
      final int intVal11;
      private final int[] intValArray;

      private float computeFloat6(int local5922_1, int local5922_2, int local5922_3) {
         local5922_1 = Math.max(0, Math.min(this.intVal10 - 1, local5922_1));
         local5922_2 = Math.max(0, Math.min(this.intVal11 - 1, local5922_2));
         return (this.intValArray[local5922_2 * this.intVal10 + local5922_1] >>> local5922_3 & 0xFF) / 255.0F;
      }

      static SmallTextAtlasBaker.SubContext2 getNested04972(FontAtlas fontAtlas) {
         MinecraftClient minecraftClient = MinecraftClient.getInstance();
         if (minecraftClient == null) {
            return null;
         }

         Optional<Resource> local5923_2 = minecraftClient.getResourceManager().getResource(fontAtlas.getIdentifier());
         if (local5923_2.isEmpty()) {
            HorusClient.logger().warn("[MSDF] Atlas PNG not readable for small-text bake: {}", fontAtlas.getIdentifier());
            return null;
         }

         try (InputStream inputStream = local5923_2.get().getInputStream()) {
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            if (bufferedImage == null) {
               return null;
            }

            int currentWidth = bufferedImage.getWidth();
            int currentHeight = bufferedImage.getHeight();
            return new SmallTextAtlasBaker.SubContext2(currentWidth, currentHeight, bufferedImage.getRGB(0, 0, currentWidth, currentHeight, null, 0, currentWidth));
         } catch (Exception exception) {
            HorusClient.logger().warn("[MSDF] Failed to load atlas PNG for small-text bake: {}", fontAtlas.getIdentifier(), exception);
            return null;
         }
      }

      private float computeFloat7(int local5924_1, int local5924_2, float local5924_3, float local5924_4, int local5924_5) {
         float local5924_6 = this.computeFloat6(local5924_1, local5924_2, local5924_5);
         float local5924_7 = this.computeFloat6(local5924_1 + 1, local5924_2, local5924_5);
         float local5924_8 = this.computeFloat6(local5924_1, local5924_2 + 1, local5924_5);
         float local5924_9 = this.computeFloat6(local5924_1 + 1, local5924_2 + 1, local5924_5);
         float local5924_10 = local5924_6 + (local5924_7 - local5924_6) * local5924_3;
         float local5924_11 = local5924_8 + (local5924_9 - local5924_8) * local5924_3;
         return local5924_10 + (local5924_11 - local5924_10) * local5924_4;
      }

      float computeFloat8(float local5925_1, float local5925_2) {
         local5925_1 -= 0.5F;
         local5925_2 -= 0.5F;
         int local5925_3 = (int)Math.floor(local5925_1);
         int local5925_4 = (int)Math.floor(local5925_2);
         float local5925_5 = local5925_1 - local5925_3;
         float local5925_6 = local5925_2 - local5925_4;
         float local5925_7 = this.computeFloat7(local5925_3, local5925_4, local5925_5, local5925_6, 16);
         float local5925_8 = this.computeFloat7(local5925_3, local5925_4, local5925_5, local5925_6, 8);
         float local5925_9 = this.computeFloat7(local5925_3, local5925_4, local5925_5, local5925_6, 0);
         return Math.max(Math.min(local5925_7, local5925_8), Math.min(Math.max(local5925_7, local5925_8), local5925_9));
      }

      private SubContext2(int local5926_1, int local5926_2, int[] intArray) {
         this.intVal10 = local5926_1;
         this.intVal11 = local5926_2;
         this.intValArray = intArray;
      }
   }

   private record DataRecord3(SmallTextAtlasBaker.SubContext page, int x, int y) {
   }
}

package horus.client.render;

import horus.client.render.FontTexture;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.text.Bidi;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class RasterGlyphCache implements AutoCloseable {
   private final SystemFontProvider systemFontProvider;
   private final float floatVal;
   private final int intVal;
   private final int intVal2;
   private final FontRenderContext fontRenderContext;
   private final Map<RasterGlyphCache.DataRecord, RasterGlyph> map = new HashMap<>();
   private final Map<Font, Font> map2 = new HashMap<>();
   private final Map<String, RasterTextLayout> map3 = new LinkedHashMap<String, RasterTextLayout>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, RasterTextLayout> local1516_1) {
         return this.size() > 1024;
      }
   };
   private final Map<String, Float> map4 = new LinkedHashMap<String, Float>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, Float> local1517_1) {
         return this.size() > 1024;
      }
   };
   private final List<FontTexture> list = new ArrayList<>();
   private final float floatVal2;
   private final float floatVal3;
   private final float floatVal4;

   public void updateState() {
      for (FontTexture fontTexture : this.list) {
         fontTexture.updateState();
      }
   }

   private FontTexture.DataRecord getNested0490(int local1476_1, int local1476_2) {
      for (FontTexture fontTexture : this.list) {
         FontTexture.DataRecord local1476_5 = fontTexture.getNested0490(local1476_1, local1476_2);
         if (local1476_5 != null) {
            return local1476_5;
         }
      }

      FontTexture fontTexture2 = new FontTexture(512);
      this.list.add(fontTexture2);
      return fontTexture2.getNested0490(local1476_1, local1476_2);
   }

   private RasterTextLayout getRasterTextLayout(String local1478_1) {
      Map<FontTexture, List<RasterGlyphQuad>> local1478_2 = new LinkedHashMap<>();
      float local1478_3 = 0.0F;
      float local1478_4 = 0.0F;
      float local1478_5 = 0.0F;
      float local1478_6 = this.floatVal2;
      int local1478_7 = 0;

      while (local1478_7 < local1478_1.length()) {
         int local1478_8 = local1478_1.codePointAt(local1478_7);
         if (local1478_8 == 10) {
            local1478_7 += Character.charCount(local1478_8);
            local1478_5 = Math.max(local1478_5, local1478_3);
            local1478_3 = 0.0F;
            local1478_4 += this.floatVal3;
         } else if (local1478_8 == 9) {
            local1478_7 += Character.charCount(local1478_8);
            local1478_3 += this.getRasterGlyph3(9).advance();
         } else if (Character.isISOControl(local1478_8)) {
            local1478_7 += Character.charCount(local1478_8);
         } else {
            Font font = this.getFont(local1478_8);
            int local1478_10 = local1478_7;
            local1478_7 += Character.charCount(local1478_8);

            while (local1478_7 < local1478_1.length()) {
               int local1478_11 = local1478_1.codePointAt(local1478_7);
               if (local1478_11 == 10 || local1478_11 == 9 || Character.isISOControl(local1478_11)) {
                  break;
               }

               Font font2 = this.getFont(local1478_11);
               if (!SystemFontProvider.formatText(font).equals(SystemFontProvider.formatText(font2))) {
                  break;
               }

               local1478_7 += Character.charCount(local1478_11);
            }

            RasterGlyphCache.DataRecord3 local1478_11 = this.getNested0369(font, local1478_1.substring(local1478_10, local1478_7));

            for (RasterGlyphCache.DataRecord2 local1478_13 : local1478_11.glyphs()) {
               RasterGlyph rasterGlyph = local1478_13.glyph();
               if (rasterGlyph.drawable()) {
                  float local1478_15 = local1478_3 + local1478_13.x() + rasterGlyph.xOffset();
                  float local1478_16 = local1478_6 + local1478_4 + local1478_13.y() + rasterGlyph.yOffset();
                  float local1478_17 = local1478_15 + rasterGlyph.width();
                  float local1478_18 = local1478_16 + rasterGlyph.height();
                  local1478_2.computeIfAbsent(rasterGlyph.page(), fontTexture -> new ArrayList<>())
                     .add(
                        new RasterGlyphQuad(
                           rasterGlyph.page(),
                           local1478_15,
                           local1478_16,
                           local1478_17,
                           local1478_18,
                           rasterGlyph.u0(),
                           rasterGlyph.v0(),
                           rasterGlyph.u1(),
                           rasterGlyph.v1()
                        )
                     );
               }
            }

            local1478_3 += local1478_11.advance();
         }
      }

      local1478_5 = Math.max(local1478_5, local1478_3);
      List<RasterTextLayout.DataRecord> local1478_7x = new ArrayList<>(local1478_2.size());

      for (Entry<FontTexture, List<RasterGlyphQuad>> local1478_9 : local1478_2.entrySet()) {
         local1478_7x.add(new RasterTextLayout.DataRecord(local1478_9.getKey(), local1478_9.getValue()));
      }

      return new RasterTextLayout(local1478_7x, local1478_5, local1478_4 + this.floatVal3);
   }

   private float computeFloat(Font font, String local1479_2) {
      if (local1479_2 != null && !local1479_2.isEmpty()) {
         Font font2 = this.getFont2(font);
         char[] charArray = local1479_2.toCharArray();
         int local1479_5 = Bidi.requiresBidi(charArray, 0, charArray.length) && new Bidi(local1479_2, -2).isRightToLeft() ? 1 : 0;
         GlyphVector glyphVector = font2.layoutGlyphVector(this.fontRenderContext, charArray, 0, charArray.length, local1479_5);
         int numGlyphs = glyphVector.getNumGlyphs();
         if (numGlyphs == 0) {
            return 0.0F;
         }

         float local1479_8 = Math.abs((float)glyphVector.getGlyphPosition(numGlyphs).getX() / this.intVal);
         return local1479_8 > 0.0F ? local1479_8 : (float)font2.getStringBounds(local1479_2, this.fontRenderContext).getWidth() / this.intVal;
      } else {
         return 0.0F;
      }
   }

   RasterGlyphCache.DataRecord3 getNested0369(Font font, String local1480_2) {
      if (local1480_2 != null && !local1480_2.isEmpty()) {
         Font font2 = this.getFont2(font);
         char[] charArray = local1480_2.toCharArray();
         int local1480_5 = Bidi.requiresBidi(charArray, 0, charArray.length) && new Bidi(local1480_2, -2).isRightToLeft() ? 1 : 0;
         GlyphVector glyphVector = font2.layoutGlyphVector(this.fontRenderContext, charArray, 0, charArray.length, local1480_5);
         int numGlyphs = glyphVector.getNumGlyphs();
         if (numGlyphs == 0) {
            return RasterGlyphCache.DataRecord3.EMPTY;
         }

         List<RasterGlyphCache.DataRecord2> local1480_8 = new ArrayList<>(numGlyphs);
         float local1480_9 = 0.0F;
         float local1480_10 = 0.0F;

         for (int index = 0; index < numGlyphs; index++) {
            Point2D glyphPosition = glyphVector.getGlyphPosition(index);
            Point2D glyphPosition2 = glyphVector.getGlyphPosition(index + 1);
            float local1480_14 = (float)glyphPosition.getX() / this.intVal;
            float local1480_15 = (float)glyphPosition.getY() / this.intVal;
            float local1480_16 = Math.abs((float)(glyphPosition2.getX() - glyphPosition.getX()) / this.intVal);
            RasterGlyph rasterGlyph = this.getRasterGlyph(font, glyphVector.getGlyphCode(index), local1480_16);
            if (rasterGlyph.drawable()) {
               local1480_9 = Math.min(local1480_9, local1480_14 + rasterGlyph.xOffset());
               local1480_10 = Math.max(local1480_10, local1480_14 + rasterGlyph.xOffset() + rasterGlyph.width());
            } else {
               local1480_10 = Math.max(local1480_10, local1480_14 + rasterGlyph.advance());
            }

            local1480_8.add(new RasterGlyphCache.DataRecord2(rasterGlyph, local1480_14, local1480_15));
         }

         float local1480_11 = Math.max(Math.abs((float)glyphVector.getGlyphPosition(numGlyphs).getX() / this.intVal), local1480_10 - local1480_9);
         float local1480_12 = local1480_9 < 0.0F ? -local1480_9 : 0.0F;
         if (local1480_12 > 0.0F) {
            List<RasterGlyphCache.DataRecord2> local1480_13 = new ArrayList<>(local1480_8.size());

            for (RasterGlyphCache.DataRecord2 local1480_15 : local1480_8) {
               local1480_13.add(new RasterGlyphCache.DataRecord2(local1480_15.glyph(), local1480_15.x() + local1480_12, local1480_15.y()));
            }

            local1480_8 = local1480_13;
            local1480_11 += local1480_12;
         }

         return new RasterGlyphCache.DataRecord3(local1480_8, local1480_11);
      } else {
         return RasterGlyphCache.DataRecord3.EMPTY;
      }
   }

   Font getFont(int local1481_1) {
      return this.systemFontProvider.getFont(local1481_1);
   }

   public float computeFloat2(String local1482_1) {
      return local1482_1 != null && !local1482_1.isEmpty() ? this.map4.computeIfAbsent(local1482_1, this::computeFloat4) : 0.0F;
   }

   float computeFloat3() {
      return this.floatVal2;
   }

   private float computeFloat4(String local1484_1) {
      float local1484_2 = 0.0F;
      float local1484_3 = 0.0F;
      int local1484_4 = 0;

      while (local1484_4 < local1484_1.length()) {
         int local1484_5 = local1484_1.codePointAt(local1484_4);
         if (local1484_5 == 10) {
            local1484_4 += Character.charCount(local1484_5);
            local1484_3 = Math.max(local1484_3, local1484_2);
            local1484_2 = 0.0F;
         } else if (local1484_5 == 9) {
            local1484_4 += Character.charCount(local1484_5);
            local1484_2 += this.floatVal4 * 4.0F;
         } else if (Character.isISOControl(local1484_5)) {
            local1484_4 += Character.charCount(local1484_5);
         } else {
            Font font = this.getFont(local1484_5);
            int local1484_7 = local1484_4;
            local1484_4 += Character.charCount(local1484_5);

            while (true) {
               if (local1484_4 < local1484_1.length()) {
                  int local1484_8 = local1484_1.codePointAt(local1484_4);
                  if (local1484_8 != 10 && local1484_8 != 9 && !Character.isISOControl(local1484_8)) {
                     Font font2 = this.getFont(local1484_8);
                     if (SystemFontProvider.formatText(font).equals(SystemFontProvider.formatText(font2))) {
                        local1484_4 += Character.charCount(local1484_8);
                        continue;
                     }
                  }
               }

               local1484_2 += this.computeFloat(font, local1484_1.substring(local1484_7, local1484_4));
               break;
            }
         }
      }

      return Math.max(local1484_3, local1484_2);
   }

   private RasterGlyph getRasterGlyph(Font font, int local1486_2, float local1486_3) {
      RasterGlyphCache.DataRecord local1486_4 = new RasterGlyphCache.DataRecord(SystemFontProvider.formatText(font), local1486_2);
      return this.map.computeIfAbsent(local1486_4, local1477_4 -> this.getRasterGlyph2(font, local1486_2, local1486_3));
   }

   @Override
   public void close() {
      this.map.clear();
      this.map2.clear();
      this.map3.clear();
      this.map4.clear();

      for (FontTexture fontTexture : this.list) {
         fontTexture.close();
      }

      this.list.clear();
   }

   float computeFloat5() {
      return this.floatVal3;
   }

   private Font getFont2(Font font) {
      return this.map2.computeIfAbsent(font, font2 -> font2.deriveFont(0, this.floatVal * this.intVal));
   }

   private RasterGlyph getRasterGlyph2(Font font, int local1491_2, float local1491_3) {
      Font font2 = this.getFont2(font);
      GlyphVector glyphVector = font2.createGlyphVector(this.fontRenderContext, new int[]{local1491_2});
      GlyphMetrics glyphMetrics = glyphVector.getGlyphMetrics(0);
      float local1491_7 = Math.max(local1491_3 > 0.0F ? local1491_3 : glyphMetrics.getAdvance() / this.intVal, 0.0F);
      Rectangle pixelBounds = glyphVector.getPixelBounds(this.fontRenderContext, 0.0F, 0.0F);
      if (pixelBounds.width > 0 && pixelBounds.height > 0) {
         int local1491_9 = pixelBounds.width + this.intVal2 * 2;
         int local1491_10 = pixelBounds.height + this.intVal2 * 2;
         if (local1491_9 > 0 && local1491_10 > 0 && local1491_9 <= 512 && local1491_10 <= 512) {
            BufferedImage bufferedImage = new BufferedImage(local1491_9, local1491_10, 2);
            Graphics2D graphics2D = bufferedImage.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            graphics2D.setColor(Color.WHITE);
            graphics2D.drawGlyphVector(glyphVector, this.intVal2 - pixelBounds.x, this.intVal2 - pixelBounds.y);
            graphics2D.dispose();
            FontTexture.DataRecord local1491_13 = this.getNested0490(local1491_9, local1491_10);
            if (local1491_13 == null) {
               return RasterGlyph.empty(local1491_7);
            }

            local1491_13.page().processData(bufferedImage, local1491_13.x(), local1491_13.y());
            float local1491_14 = (float)(pixelBounds.x - this.intVal2) / this.intVal;
            float local1491_15 = (float)(pixelBounds.y - this.intVal2) / this.intVal;
            float local1491_16 = (float)local1491_9 / this.intVal;
            float local1491_17 = (float)local1491_10 / this.intVal;
            return new RasterGlyph(
               local1491_13.page(),
               local1491_13.u0(),
               local1491_13.v0(),
               local1491_13.u1(),
               local1491_13.v1(),
               local1491_14,
               local1491_15,
               local1491_16,
               local1491_17,
               local1491_7
            );
         } else {
            return RasterGlyph.empty(local1491_7);
         }
      } else {
         return RasterGlyph.empty(local1491_7);
      }
   }

   public RasterTextLayout getRasterTextLayout2(String local1492_1) {
      return local1492_1 != null && !local1492_1.isEmpty() ? this.map3.computeIfAbsent(local1492_1, this::getRasterTextLayout) : RasterTextLayout.rasterTextLayout;
   }

   RasterGlyph getRasterGlyph3(int local1493_1) {
      if (local1493_1 == 9) {
         RasterGlyph rasterGlyph = this.getRasterGlyph3(32);
         return rasterGlyph.withAdvance(rasterGlyph.advance() * 4.0F);
      } else {
         Font font = this.systemFontProvider.getFont(local1493_1);
         Font font2 = this.getFont2(font);
         GlyphVector glyphVector = font2.createGlyphVector(this.fontRenderContext, new String(Character.toChars(local1493_1)));
         int currentGlyphCode = glyphVector.getGlyphCode(0);
         float local1493_6 = glyphVector.getGlyphMetrics(0).getAdvance() / this.intVal;
         return this.getRasterGlyph(font, currentGlyphCode, local1493_6);
      }
   }

   public RasterGlyphCache(SystemFontProvider initialSystemFontProvider, float local1494_2) {
      this.systemFontProvider = initialSystemFontProvider;
      this.floatVal = local1494_2;
      this.intVal = PixelSnap.computeInt(local1494_2);
      this.intVal2 = PixelSnap.computeInt2(this.intVal);
      this.fontRenderContext = new FontRenderContext(new AffineTransform(), RenderingHints.VALUE_TEXT_ANTIALIAS_ON, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
      Font font = initialSystemFontProvider.getFont3().deriveFont(0, local1494_2 * this.intVal);
      LineMetrics lineMetrics = font.getLineMetrics("Mg", this.fontRenderContext);
      this.floatVal2 = (float)Math.round(lineMetrics.getAscent()) / this.intVal;
      this.floatVal3 = (float)Math.round(Math.max(local1494_2 * this.intVal, lineMetrics.getHeight())) / this.intVal;
      this.floatVal4 = Math.max(font.createGlyphVector(this.fontRenderContext, " ").getGlyphMetrics(0).getAdvance() / this.intVal, local1494_2 * 0.25F);
   }

   private record DataRecord(String fontKey, int glyphCode) {
   }

   record DataRecord2(RasterGlyph glyph, float x, float y) {
   }

   record DataRecord3(List<RasterGlyphCache.DataRecord2> glyphs, float advance) {
      static final RasterGlyphCache.DataRecord3 EMPTY = new RasterGlyphCache.DataRecord3(List.of(), 0.0F);
   }
}

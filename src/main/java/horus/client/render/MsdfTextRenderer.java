package horus.client.render;

import horus.client.HorusClient;
import horus.mixin.DrawContextMixin;
import horus.client.util.GuiScale;
import horus.client.render.GuiLayerSerialAccess;
import horus.client.render.ScissorStack;
import horus.client.render.MsdfTextDrawData;
import horus.client.render.MsdfGlyph;
import horus.client.render.RoundedClipStack;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2f;

public final class MsdfTextRenderer implements AutoCloseable {
   private static volatile MsdfTextRenderer msdfTextRenderer;
   private static final VertexFormat vertexFormat = VertexFormat.builder()
      .add("Position", VertexFormatElement.POSITION)
      .add("UV0", VertexFormatElement.UV0)
      .add("Color", VertexFormatElement.COLOR)
      .add("LineWidth", VertexFormatElement.LINE_WIDTH)
      .build();
   private static final VertexFormat vertexFormat2 = VertexFormat.builder()
      .add("Position", VertexFormatElement.POSITION)
      .add("UV0", VertexFormatElement.UV0)
      .add("Color", VertexFormatElement.COLOR)
      .add("LineWidth", VertexFormatElement.LINE_WIDTH)
      .build();
   public static final RenderPipeline pipeline2 = RenderPipeline.builder()
      .withLocation(getIdentifier("pipeline/msdf_text"))
      .withVertexShader(getIdentifier("core/msdf_text"))
      .withFragmentShader(getIdentifier("core/msdf_text"))
      .withVertexFormat(vertexFormat, DrawMode.QUADS)
      .withBlend(BlendFunction.TRANSLUCENT)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withCull(false)
      .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
      .withUniform("Projection", UniformType.UNIFORM_BUFFER)
      .withSampler("Sampler0")
      .build();
   public static final RenderPipeline pipeline3 = RenderPipeline.builder()
      .withLocation(getIdentifier("pipeline/msdf_shimmer"))
      .withVertexShader(getIdentifier("core/msdf_shimmer"))
      .withFragmentShader(getIdentifier("core/msdf_shimmer"))
      .withVertexFormat(vertexFormat2, DrawMode.QUADS)
      .withBlend(BlendFunction.TRANSLUCENT)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withCull(false)
      .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
      .withUniform("Projection", UniformType.UNIFORM_BUFFER)
      .withSampler("Sampler0")
      .build();
   private final Map<MsdfTextRenderer.DataRecord3, MsdfTextRenderState> map = new LinkedHashMap<>(32);
   private final Map<MsdfTextRenderer.DataRecord4, RasterTextRenderState> map2 = new LinkedHashMap<>(32);
   private final Map<Identifier, MsdfTextRenderer.DataRecord5> map3 = new HashMap<>();
   private final SmallTextAtlasBaker smallTextAtlasBaker = new SmallTextAtlasBaker();
   private DrawContext drawContext2;
   private boolean flag;
   private int intVal;

   public float computeFloat(String local3659_1, String local3659_2, float local3659_3) {
      FontAtlas fontAtlas = FontRegistry.getAtlas(local3659_1);
      return fontAtlas == null ? 0.0F : fontAtlas.computeFloat7(local3659_2, local3659_3);
   }

   public void updateState() {
      this.smallTextAtlasBaker.updateState3();
      this.drawContext2 = null;
      this.map.clear();
      this.map2.clear();
   }

   public boolean checkState() {
      String[] stringArray = FontRegistry.getWarmupNames();
      if (this.intVal >= stringArray.length) {
         return false;
      }

      FontAtlas fontAtlas = FontRegistry.getAtlas(stringArray[this.intVal++]);
      if (fontAtlas != null) {
         this.smallTextAtlasBaker.matchesCriteria5(fontAtlas);
      }

      return true;
   }

   public static void updateState2() {
      MsdfTextRenderer currentMsdfTextRenderer = msdfTextRenderer;
      if (currentMsdfTextRenderer != null) {
         currentMsdfTextRenderer.close();
         msdfTextRenderer = null;
      }
   }

   public void updateState3() {
      this.smallTextAtlasBaker.updateState4();
   }

   private static int computeInt(float local3665_0) {
      return Math.round(computeFloat4(local3665_0) * 255.0F);
   }

   private int computeInt2(int local3666_1, float local3666_2, MsdfTextDrawData msdfTextDrawData) {
      if (!msdfTextDrawData.hasHorizontalFade()) {
         return local3666_1;
      }

      float local3666_4 = 1.0F;
      if (msdfTextDrawData.fadeLeftStrength() > 0.001F) {
         float local3666_5 = computeFloat7(computeFloat4((local3666_2 - msdfTextDrawData.fadeLeftX()) / msdfTextDrawData.fadeWidth()));
         local3666_4 = Math.min(local3666_4, 1.0F - msdfTextDrawData.fadeLeftStrength() * (1.0F - local3666_5));
      }

      if (msdfTextDrawData.fadeRightStrength() > 0.001F) {
         float local3666_5 = computeFloat7(computeFloat4((msdfTextDrawData.fadeRightX() - local3666_2) / msdfTextDrawData.fadeWidth()));
         local3666_4 = Math.min(local3666_4, 1.0F - msdfTextDrawData.fadeRightStrength() * (1.0F - local3666_5));
      }

      int local3666_5 = local3666_1 >>> 24 & 0xFF;
      return Math.round(local3666_5 * local3666_4) << 24 | local3666_1 & 16777215;
   }

   public void processData(MsdfTextDrawData msdfTextDrawData) {
      this.renderElement2(this.drawContext2, msdfTextDrawData, false, 0.0F, 0.0F, 0.0F, 0.0F);
   }

   private static int computeInt3(int local3668_0, float local3668_1) {
      if (local3668_1 >= 0.999F) {
         return local3668_0;
      }

      if (local3668_1 <= 0.001F) {
         return local3668_0 & 16777215;
      }

      int local3668_2 = Math.round((local3668_0 >>> 24 & 0xFF) * local3668_1);
      return local3668_2 << 24 | local3668_0 & 16777215;
   }

   public float[] computeFloat2(String local3669_1, String local3669_2, float local3669_3) {
      FontAtlas fontAtlas = FontRegistry.getAtlas(local3669_1);
      return fontAtlas == null ? new float[]{0.0F, 0.0F} : fontAtlas.computeFloat(local3669_2, local3669_3);
   }

   private boolean matchesCriteria(
      GuiRenderState guiRenderState,
      int local3671_2,
      Matrix3x2f matrix3x2f,
      ScreenRect screenRect,
      FontAtlas fontAtlas,
      MsdfTextDrawData msdfTextDrawData
   ) {
      float local3671_7 = msdfTextDrawData.size();
      float local3671_8 = SmallTextAtlasBaker.computeFloat5(local3671_7);
      float local3671_9 = GuiScale.computeFloat();
      int local3671_10 = GuiScale.computeInt();
      float local3671_11 = matrix3x2f.m20() * local3671_10;
      float local3671_12 = matrix3x2f.m21() * local3671_10;
      float local3671_13 = msdfTextDrawData.x();
      float local3671_14 = local3671_13;
      int local3671_15 = -1;
      String local3671_16 = msdfTextDrawData.text();
      int local3671_17 = 0;

      while (local3671_17 < local3671_16.length()) {
         int local3671_18 = local3671_16.codePointAt(local3671_17);
         local3671_17 += Character.charCount(local3671_18);
         if (local3671_18 != 10) {
            boolean local3671_19 = !fontAtlas.matchesCriteria(local3671_18) && SmallTextAtlasBaker.matchesCriteria(local3671_18);
            MsdfGlyph msdfGlyph = fontAtlas.getMsdfGlyph(local3671_18);
            if (local3671_15 != -1) {
               local3671_14 += fontAtlas.computeFloat2(local3671_15, local3671_18) * local3671_7;
            }

            if (msdfGlyph.drawable() || local3671_19) {
               float local3671_21 = local3671_14 * local3671_9 + local3671_11;
               int local3671_22 = Math.round((local3671_21 - (int)Math.floor(local3671_21)) * 4.0F);
               if (local3671_22 == 4) {
                  local3671_22 = 0;
               }

               if (local3671_19
                  ? !this.smallTextAtlasBaker.matchesCriteria4(local3671_18, local3671_8, local3671_22)
                  : !this.smallTextAtlasBaker.matchesCriteria3(fontAtlas, local3671_18, msdfGlyph, local3671_8, local3671_22)) {
                  return false;
               }
            }

            local3671_14 += (local3671_19 ? SmallTextAtlasBaker.computeFloat3(local3671_18) : msdfGlyph.advance()) * local3671_7;
            local3671_15 = local3671_18;
         } else {
            local3671_14 = local3671_13;
            local3671_15 = -1;
         }
      }

      float local3671_17x = msdfTextDrawData.y() + fontAtlas.computeFloat5(local3671_7);
      float local3671_18 = fontAtlas.computeFloat3(local3671_7);
      MsdfTextRenderer.DataRecord local3671_19 = MsdfTextRenderer.DataRecord.of(matrix3x2f);
      float local3671_20 = local3671_13;
      float local3671_21 = Math.round(local3671_17x * local3671_9 + local3671_12);
      int local3671_22 = 0;
      int local3671_23 = -1;
      String local3671_24 = msdfTextDrawData.text();
      int local3671_25 = 0;

      while (local3671_25 < local3671_24.length()) {
         int local3671_26 = local3671_24.codePointAt(local3671_25);
         local3671_25 += Character.charCount(local3671_26);
         if (local3671_26 == 10) {
            local3671_20 = local3671_13;
            local3671_21 = Math.round((local3671_17x + ++local3671_22 * local3671_18) * local3671_9 + local3671_12);
            local3671_23 = -1;
         } else {
            boolean local3671_27 = !fontAtlas.matchesCriteria(local3671_26) && SmallTextAtlasBaker.matchesCriteria(local3671_26);
            MsdfGlyph msdfGlyph2 = fontAtlas.getMsdfGlyph(local3671_26);
            if (local3671_23 != -1) {
               local3671_20 += fontAtlas.computeFloat2(local3671_23, local3671_26) * local3671_7;
            }

            if (msdfGlyph2.drawable() || local3671_27) {
               float local3671_29 = local3671_20 * local3671_9 + local3671_11;
               int local3671_30 = (int)Math.floor(local3671_29);
               int local3671_31 = Math.round((local3671_29 - local3671_30) * 4.0F);
               if (local3671_31 == 4) {
                  local3671_31 = 0;
                  local3671_30++;
               }

               SmallTextAtlasBaker.AtlasGlyphKey local3671_32 = local3671_27
                  ? this.smallTextAtlasBaker.getNested04963(local3671_26, local3671_8, local3671_31)
                  : this.smallTextAtlasBaker.getNested04965(fontAtlas, local3671_26, msdfGlyph2, local3671_8, local3671_31);
               if (local3671_32.drawable()) {
                  float local3671_33 = local3671_30;
                  float local3671_34 = (local3671_33 + local3671_32.offsetX() - local3671_11) / local3671_9;
                  float local3671_35 = (local3671_21 - local3671_32.offsetY() - local3671_12) / local3671_9;
                  float local3671_36 = local3671_34 + local3671_32.width() / local3671_9;
                  float local3671_37 = local3671_35 + local3671_32.height() / local3671_9;
                  MsdfTextRenderer.DataRecord4 local3671_38 = new MsdfTextRenderer.DataRecord4(
                     guiRenderState, local3671_2, local3671_32.page(), local3671_19, screenRect
                  );
                  RasterTextRenderState rasterTextRenderState = this.map2.get(local3671_38);
                  if (rasterTextRenderState == null) {
                     rasterTextRenderState = new RasterTextRenderState(matrix3x2f, local3671_32.page().getTextureSetup(), screenRect);
                     this.map2.put(local3671_38, rasterTextRenderState);
                     guiRenderState.addPreparedTextElement(rasterTextRenderState);
                  }

                  RasterTextRenderState rasterTextRenderState2 = rasterTextRenderState;
                  processData3(
                     local3671_34,
                     local3671_35,
                     local3671_36,
                     local3671_37,
                     local3671_32.u0(),
                     local3671_32.v0(),
                     local3671_32.u1(),
                     local3671_32.v1(),
                     (local3661_3, local3661_4, local3661_5, local3661_6, local3661_7, local3661_8, local3661_9, local3661_10, local3661_11, local3661_12) -> {
                        int local3661_13 = computeInt3(this.computeInt2(msdfTextDrawData.colorTopLeft(), local3661_3, msdfTextDrawData), local3661_11);
                        int local3661_14 = computeInt3(this.computeInt2(msdfTextDrawData.colorTopRight(), local3661_5, msdfTextDrawData), local3661_11);
                        int local3661_15 = computeInt3(this.computeInt2(msdfTextDrawData.colorBottomRight(), local3661_5, msdfTextDrawData), local3661_12);
                        int local3661_16 = computeInt3(this.computeInt2(msdfTextDrawData.colorBottomLeft(), local3661_3, msdfTextDrawData), local3661_12);
                        if ((local3661_13 | local3661_14 | local3661_15 | local3661_16) >>> 24 != 0) {
                           rasterTextRenderState2.add(
                              local3661_3,
                              local3661_4,
                              local3661_5,
                              local3661_6,
                              local3661_7,
                              local3661_8,
                              local3661_9,
                              local3661_10,
                              local3661_13,
                              local3661_14,
                              local3661_15,
                              local3661_16
                           );
                        }
                     }
                  );
               }
            }

            local3671_20 += (local3671_27 ? SmallTextAtlasBaker.computeFloat3(local3671_26) : msdfGlyph2.advance()) * local3671_7;
            local3671_23 = local3671_26;
         }
      }

      return true;
   }

   private TextureSetup getTextureSetup(Identifier identifier) {
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      if (minecraftClient == null) {
         return null;
      }

      TextureManager textureManager = minecraftClient.getTextureManager();
      if (textureManager == null) {
         return null;
      }

      AbstractTexture currentTexture = textureManager.getTexture(identifier);
      if (currentTexture != null && currentTexture.getGlTextureView() != null) {
         MsdfTextRenderer.DataRecord5 local3672_5 = this.map3.get(identifier);
         if (local3672_5 != null && local3672_5.texture() == currentTexture) {
            return local3672_5.setup();
         }

         TextureSetup textureSetup = TextureSetup.of(currentTexture.getGlTextureView(), RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
         this.map3.put(identifier, new MsdfTextRenderer.DataRecord5(currentTexture, textureSetup));
         return textureSetup;
      } else {
         return null;
      }
   }

   private static int computeInt4(float local3673_0, float local3673_1, float local3673_2, float local3673_3) {
      int local3673_4 = computeInt(local3673_3);
      int local3673_5 = computeInt((local3673_0 + 0.5F) / 2.0F);
      int local3673_6 = computeInt(local3673_1);
      int local3673_7 = computeInt(local3673_2);
      return local3673_4 << 24 | local3673_5 << 16 | local3673_6 << 8 | local3673_7;
   }

   private void processData2(
      MsdfTextRenderState msdfTextRenderState,
      GuiRenderState guiRenderState,
      int local3674_3,
      Matrix3x2f matrix3x2f,
      ScreenRect screenRect,
      FontAtlas fontAtlas,
      MsdfTextDrawData msdfTextDrawData,
      boolean local3674_8,
      int local3674_9
   ) {
      float local3674_10 = msdfTextDrawData.size();
      float local3674_11 = msdfTextDrawData.x();
      float local3674_12 = msdfTextDrawData.y() + fontAtlas.computeFloat5(local3674_10);
      float local3674_13 = fontAtlas.computeFloat3(local3674_10);
      float[] floatArray = this.computeFloat6(fontAtlas, msdfTextDrawData.text(), local3674_10);
      float local3674_15 = local3674_11;
      float local3674_16 = local3674_12;
      int local3674_17 = 0;
      int local3674_18 = -1;
      String local3674_19 = msdfTextDrawData.text();
      int local3674_20 = 0;

      while (local3674_20 < local3674_19.length()) {
         int local3674_21 = local3674_19.codePointAt(local3674_20);
         local3674_20 += Character.charCount(local3674_21);
         if (local3674_21 == 10) {
            local3674_15 = local3674_11;
            local3674_16 += local3674_13;
            local3674_17++;
            local3674_18 = -1;
         } else {
            MsdfGlyph msdfGlyph = fontAtlas.getMsdfGlyph(local3674_21);
            if (local3674_18 != -1) {
               local3674_15 += fontAtlas.computeFloat2(local3674_18, local3674_21) * local3674_10;
            }

            if (!fontAtlas.matchesCriteria(local3674_21) && SmallTextAtlasBaker.matchesCriteria(local3674_21)) {
               if (!this.flag && !local3674_8 && Math.abs(msdfTextDrawData.rotationDegrees()) < 0.001F) {
                  SmallTextAtlasBaker.AtlasGlyphKey local3674_23 = this.smallTextAtlasBaker.getNested04963(local3674_21, SmallTextAtlasBaker.computeFloat5(local3674_10), 0);
                  if (local3674_23.drawable()) {
                     float local3674_24 = local3674_15 + local3674_23.offsetX() / GuiScale.computeFloat();
                     float local3674_25 = local3674_16 - local3674_23.offsetY() / GuiScale.computeFloat();
                     MsdfTextRenderer.DataRecord4 local3674_26 = new MsdfTextRenderer.DataRecord4(
                        guiRenderState, local3674_3, local3674_23.page(), MsdfTextRenderer.DataRecord.of(matrix3x2f), screenRect
                     );
                     RasterTextRenderState rasterTextRenderState = this.map2.get(local3674_26);
                     if (rasterTextRenderState == null) {
                        rasterTextRenderState = new RasterTextRenderState(matrix3x2f, local3674_23.page().getTextureSetup(), screenRect);
                        this.map2.put(local3674_26, rasterTextRenderState);
                        guiRenderState.addPreparedTextElement(rasterTextRenderState);
                     }

                     RasterTextRenderState rasterTextRenderState2 = rasterTextRenderState;
                     processData3(
                        local3674_24,
                        local3674_25,
                        local3674_24 + local3674_23.width() / GuiScale.computeFloat(),
                        local3674_25 + local3674_23.height() / GuiScale.computeFloat(),
                        local3674_23.u0(),
                        local3674_23.v0(),
                        local3674_23.u1(),
                        local3674_23.v1(),
                        (local3677_3, local3677_4, local3677_5, local3677_6, local3677_7, local3677_8, local3677_9, local3677_10, local3677_11, local3677_12) -> {
                           int local3677_13 = computeInt3(this.computeInt2(msdfTextDrawData.colorTopLeft(), local3677_3, msdfTextDrawData), local3677_11);
                           int local3677_14 = computeInt3(this.computeInt2(msdfTextDrawData.colorTopRight(), local3677_5, msdfTextDrawData), local3677_11);
                           int local3677_15 = computeInt3(this.computeInt2(msdfTextDrawData.colorBottomRight(), local3677_5, msdfTextDrawData), local3677_12);
                           int local3677_16 = computeInt3(this.computeInt2(msdfTextDrawData.colorBottomLeft(), local3677_3, msdfTextDrawData), local3677_12);
                           if ((local3677_13 | local3677_14 | local3677_15 | local3677_16) >>> 24 != 0) {
                              rasterTextRenderState2.add(
                                 local3677_3,
                                 local3677_4,
                                 local3677_5,
                                 local3677_6,
                                 local3677_7,
                                 local3677_8,
                                 local3677_9,
                                 local3677_10,
                                 local3677_13,
                                 local3677_14,
                                 local3677_15,
                                 local3677_16
                              );
                           }
                        }
                     );
                  }
               }

               local3674_15 += SmallTextAtlasBaker.computeFloat3(local3674_21) * local3674_10;
               local3674_18 = local3674_21;
            } else {
               if (msdfGlyph.drawable()) {
                  float local3674_23 = local3674_15 + msdfGlyph.planeLeft() * local3674_10;
                  float local3674_24 = local3674_15 + msdfGlyph.planeRight() * local3674_10;
                  float local3674_25 = local3674_16 - msdfGlyph.planeTop() * local3674_10;
                  float local3674_26 = local3674_16 - msdfGlyph.planeBottom() * local3674_10;
                  float local3674_27 = local3674_17 < floatArray.length ? floatArray[local3674_17] : 1.0F;
                  float local3674_28 = local3674_8 ? computeFloat4((local3674_23 - local3674_11) / local3674_27) : 0.0F;
                  float local3674_29 = local3674_8 ? computeFloat4((local3674_24 - local3674_11) / local3674_27) : 0.0F;
                  if (local3674_8) {
                     int local3674_30 = msdfTextDrawData.colorTopLeft();
                     local3674_28 = computeFloat3(local3674_28, local3674_30);
                     local3674_29 = computeFloat3(local3674_29, local3674_30);
                  }

                  boolean local3674_30 = Math.abs(msdfTextDrawData.rotationDegrees()) >= 0.001F;
                  if (!local3674_8 && !local3674_30) {
                     MsdfTextRenderState msdfTextRenderState2 = msdfTextRenderState;
                     float local3674_32 = local3674_28;
                     float local3674_33 = local3674_29;
                     processData3(
                        local3674_23,
                        local3674_25,
                        local3674_24,
                        local3674_26,
                        msdfGlyph.u0(),
                        msdfGlyph.v0(),
                        msdfGlyph.u1(),
                        msdfGlyph.v1(),
                        (local3670_5, local3670_6, local3670_7, local3670_8, local3670_9, local3670_10, local3670_11, local3670_12, local3670_13, local3670_14) -> {
                           int local3670_15 = computeInt3(this.computeInt2(msdfTextDrawData.colorTopLeft(), local3670_5, msdfTextDrawData), local3670_13);
                           int local3670_16 = computeInt3(this.computeInt2(msdfTextDrawData.colorTopRight(), local3670_7, msdfTextDrawData), local3670_13);
                           int local3670_17 = computeInt3(this.computeInt2(msdfTextDrawData.colorBottomRight(), local3670_7, msdfTextDrawData), local3670_14);
                           int local3670_18 = computeInt3(this.computeInt2(msdfTextDrawData.colorBottomLeft(), local3670_5, msdfTextDrawData), local3670_14);
                           if ((local3670_15 | local3670_16 | local3670_17 | local3670_18) >>> 24 != 0) {
                              msdfTextRenderState2.addGlyph(
                                 local3670_5,
                                 local3670_6,
                                 local3670_7,
                                 local3670_8,
                                 local3670_9,
                                 local3670_10,
                                 local3670_11,
                                 local3670_12,
                                 local3670_15,
                                 local3670_16,
                                 local3670_17,
                                 local3670_18,
                                 local3674_32,
                                 local3674_33,
                                 0.0F,
                                 0.0F,
                                 0.0F
                              );
                           }
                        }
                     );
                  } else {
                     int local3674_31 = local3674_8
                        ? local3674_9
                        : RoundedClipStack.computeInt(this.computeInt2(msdfTextDrawData.colorTopLeft(), local3674_23, msdfTextDrawData), local3674_23, local3674_25);
                     int local3674_32 = local3674_8
                        ? local3674_9
                        : RoundedClipStack.computeInt(this.computeInt2(msdfTextDrawData.colorTopRight(), local3674_24, msdfTextDrawData), local3674_24, local3674_25);
                     int local3674_33 = local3674_8
                        ? local3674_9
                        : RoundedClipStack.computeInt(this.computeInt2(msdfTextDrawData.colorBottomRight(), local3674_24, msdfTextDrawData), local3674_24, local3674_26);
                     int local3674_34 = local3674_8
                        ? local3674_9
                        : RoundedClipStack.computeInt(this.computeInt2(msdfTextDrawData.colorBottomLeft(), local3674_23, msdfTextDrawData), local3674_23, local3674_26);
                     if (local3674_8 || (local3674_31 | local3674_32 | local3674_33 | local3674_34) >>> 24 != 0) {
                        msdfTextRenderState.addGlyph(
                           local3674_23,
                           local3674_25,
                           local3674_24,
                           local3674_26,
                           msdfGlyph.u0(),
                           msdfGlyph.v0(),
                           msdfGlyph.u1(),
                           msdfGlyph.v1(),
                           local3674_31,
                           local3674_32,
                           local3674_33,
                           local3674_34,
                           local3674_28,
                           local3674_29,
                           msdfTextDrawData.rotationDegrees(),
                           msdfTextDrawData.rotationOriginX(),
                           msdfTextDrawData.rotationOriginY()
                        );
                     }
                  }
               }

               local3674_15 += msdfGlyph.advance() * local3674_10;
               local3674_18 = local3674_21;
            }
         }
      }
   }

   private static float computeFloat3(float local3675_0, int local3675_1) {
      int local3675_2 = Math.round(computeFloat4(local3675_0) * 63.0F);
      int local3675_3 = Math.round((local3675_1 >>> 16 & 0xFF) / 255.0F * 63.0F);
      int local3675_4 = Math.round((local3675_1 >>> 8 & 0xFF) / 255.0F * 63.0F);
      int local3675_5 = Math.round((local3675_1 & 0xFF) / 255.0F * 63.0F);
      return local3675_2 + local3675_5 * 64 + local3675_4 * 4096 + local3675_3 * 262144;
   }

   @Override
   public void close() {
      this.updateState5();
   }

   private static float computeFloat4(float local3679_0) {
      return Math.max(0.0F, Math.min(1.0F, local3679_0));
   }

   public static void updateState4() {
      MsdfTextRenderer currentMsdfTextRenderer = msdfTextRenderer;
      if (currentMsdfTextRenderer != null) {
         currentMsdfTextRenderer.updateState5();
      } else {
         FontRegistry.clearCache();
      }
   }

   private static void processData3(
      float local3681_0,
      float local3681_1,
      float local3681_2,
      float local3681_3,
      float local3681_4,
      float local3681_5,
      float local3681_6,
      float local3681_7,
      MsdfTextRenderer.SubInterface2 local3681_8
   ) {
      if (!RoundedClipStack.checkState()) {
         local3681_8.processData6(local3681_0, local3681_1, local3681_2, local3681_3, local3681_4, local3681_5, local3681_6, local3681_7, 1.0F, 1.0F);
      } else {
         float local3681_9 = RoundedClipStack.computeFloat9();
         float local3681_10 = RoundedClipStack.computeFloat4();
         float local3681_11 = local3681_9 + RoundedClipStack.computeFloat6();
         float local3681_12 = local3681_10 + RoundedClipStack.computeFloat2();
         if (!(local3681_0 >= local3681_11)
            && !(local3681_2 <= local3681_9)
            && !(local3681_1 >= local3681_12)
            && !(local3681_3 <= local3681_10)
            && !(local3681_2 <= local3681_0)
            && !(local3681_3 <= local3681_1)) {
            float local3681_13 = local3681_2 - local3681_0;
            float local3681_14 = local3681_3 - local3681_1;
            float local3681_15 = Math.max(local3681_0, local3681_9);
            float local3681_16 = Math.min(local3681_2, local3681_11);
            float local3681_17 = Math.max(local3681_1, local3681_10);
            float local3681_18 = Math.min(local3681_3, local3681_12);
            float local3681_19 = local3681_4 + (local3681_6 - local3681_4) * (local3681_15 - local3681_0) / local3681_13;
            float local3681_20 = local3681_4 + (local3681_6 - local3681_4) * (local3681_16 - local3681_0) / local3681_13;
            float local3681_21 = local3681_10 + 1.0F;
            float local3681_22 = local3681_12 - 1.0F;
            float[] floatArray = new float[]{
               local3681_17,
               Math.min(Math.max(local3681_21, local3681_17), local3681_18),
               Math.min(Math.max(local3681_22, local3681_17), local3681_18),
               local3681_18
            };
            Arrays.sort(floatArray);

            for (int index = 0; index < 3; index++) {
               float local3681_25 = floatArray[index];
               float local3681_26 = floatArray[index + 1];
               if (!(local3681_26 - local3681_25 < 1.0E-4F)) {
                  float local3681_27 = local3681_5 + (local3681_7 - local3681_5) * (local3681_25 - local3681_1) / local3681_14;
                  float local3681_28 = local3681_5 + (local3681_7 - local3681_5) * (local3681_26 - local3681_1) / local3681_14;
                  local3681_8.processData6(
                     local3681_15,
                     local3681_25,
                     local3681_16,
                     local3681_26,
                     local3681_19,
                     local3681_27,
                     local3681_20,
                     local3681_28,
                     computeFloat5(local3681_25, local3681_10, local3681_12),
                     computeFloat5(local3681_26, local3681_10, local3681_12)
                  );
               }
            }
         }
      }
   }

   private static float computeFloat5(float local3682_0, float local3682_1, float local3682_2) {
      float local3682_3 = Math.min((local3682_0 - local3682_1) / 1.0F, (local3682_2 - local3682_0) / 1.0F);
      return Math.max(0.0F, Math.min(1.0F, local3682_3));
   }

   private void updateState5() {
      this.intVal = 0;
      this.drawContext2 = null;
      this.map.clear();
      this.map2.clear();
      this.map3.clear();
      this.smallTextAtlasBaker.updateState();
      FontRegistry.clearCache();
   }

   private float[] computeFloat6(FontAtlas fontAtlas, String local3684_2, float local3684_3) {
      int local3684_4 = 1;

      for (int index = 0; index < local3684_2.length(); index++) {
         if (local3684_2.charAt(index) == '\n') {
            local3684_4++;
         }
      }

      float[] floatArray = new float[local3684_4];
      int local3684_6 = 0;
      int local3684_7 = -1;
      int local3684_8 = 0;

      while (local3684_8 < local3684_2.length()) {
         int local3684_9 = local3684_2.codePointAt(local3684_8);
         local3684_8 += Character.charCount(local3684_9);
         if (local3684_9 == 10) {
            floatArray[local3684_6] = Math.max(floatArray[local3684_6], 1.0F);
            local3684_6++;
            local3684_7 = -1;
         } else {
            MsdfGlyph msdfGlyph = fontAtlas.getMsdfGlyph(local3684_9);
            if (local3684_7 != -1) {
               floatArray[local3684_6] += fontAtlas.computeFloat2(local3684_7, local3684_9) * local3684_3;
            }

            floatArray[local3684_6] += (
                  !fontAtlas.matchesCriteria(local3684_9) && SmallTextAtlasBaker.matchesCriteria(local3684_9)
                     ? SmallTextAtlasBaker.computeFloat3(local3684_9)
                     : msdfGlyph.advance()
               )
               * local3684_3;
            local3684_7 = local3684_9;
         }
      }

      for (int local3684_8x = 0; local3684_8x < floatArray.length; local3684_8x++) {
         floatArray[local3684_8x] = Math.max(floatArray[local3684_8x], 1.0F);
      }

      return floatArray;
   }

   private static Identifier getIdentifier(String local3685_0) {
      return Identifier.of("horus", local3685_0);
   }

   public void processData4(MsdfTextDrawData msdfTextDrawData, float local3686_2, float local3686_3, float local3686_4, float local3686_5) {
      this.renderElement2(this.drawContext2, msdfTextDrawData, true, local3686_2, local3686_3, local3686_4, local3686_5);
   }

   private MsdfTextRenderer() {
   }

   public void processData5(boolean local3688_1) {
      this.flag = local3688_1;
   }

   public void renderElement(DrawContext drawContext) {
      if (this.drawContext2 != drawContext) {
         this.map.clear();
         this.map2.clear();
      }

      this.drawContext2 = drawContext;
      this.smallTextAtlasBaker.updateState5();
   }

   public void updateState6() {
      this.map3.clear();
   }

   public boolean matchesCriteria2(String local3691_1, int local3691_2) {
      FontAtlas fontAtlas = FontRegistry.getAtlas(local3691_1);
      return fontAtlas != null && fontAtlas.matchesCriteria(local3691_2);
   }

   public static MsdfTextRenderer getMsdfTextRenderer() {
      MsdfTextRenderer currentMsdfTextRenderer = msdfTextRenderer;
      if (currentMsdfTextRenderer == null) {
         synchronized (MsdfTextRenderer.class) {
            currentMsdfTextRenderer = msdfTextRenderer;
            if (currentMsdfTextRenderer == null) {
               currentMsdfTextRenderer = new MsdfTextRenderer();
               msdfTextRenderer = currentMsdfTextRenderer;
            }
         }
      }

      return currentMsdfTextRenderer;
   }

   private void renderElement2(
      DrawContext drawContext, MsdfTextDrawData msdfTextDrawData, boolean local3693_3, float local3693_4, float local3693_5, float local3693_6, float local3693_7
   ) {
      if (drawContext != null && msdfTextDrawData != null && msdfTextDrawData.visible()) {
         FontAtlas fontAtlas = FontRegistry.getAtlas(msdfTextDrawData.fontName());
         if (fontAtlas != null) {
            try {
               Matrix3x2f matrix3x2f = GuiScale.renderElement(drawContext);
               GuiRenderState guiRenderState = ((DrawContextMixin)drawContext).getGuiRenderState();
               int currentLayerSerial = ((GuiLayerSerialAccess)guiRenderState).getLayerSerial();
               ScreenRect screenRect = ScissorStack.getScreenRect();
               boolean local3693_13 = !local3693_3 && Math.abs(msdfTextDrawData.rotationDegrees()) < 0.001F && msdfTextDrawData.size() <= 20.0F;
               if (local3693_13
                  && !this.flag
                  && SmallTextAtlasBaker.matchesCriteria2(matrix3x2f)
                  && this.smallTextAtlasBaker.matchesCriteria5(fontAtlas)
                  && this.matchesCriteria(guiRenderState, currentLayerSerial, matrix3x2f, screenRect, fontAtlas, msdfTextDrawData)) {
                  return;
               }

               TextureSetup textureSetup = this.getTextureSetup(fontAtlas.getIdentifier());
               if (textureSetup == null) {
                  return;
               }

               int local3693_15 = local3693_3 ? computeInt4(local3693_4, local3693_5, local3693_6, local3693_7) : 0;
               MsdfTextRenderer.DataRecord3 local3693_16 = new MsdfTextRenderer.DataRecord3(
                  guiRenderState, currentLayerSerial, fontAtlas.getIdentifier(), MsdfTextRenderer.DataRecord.of(matrix3x2f), screenRect, local3693_3, local3693_15
               );
               MsdfTextRenderState msdfTextRenderState = this.map.get(local3693_16);
               if (msdfTextRenderState == null) {
                  msdfTextRenderState = new MsdfTextRenderState(matrix3x2f, textureSetup, screenRect, local3693_3, fontAtlas.computeFloat8());
                  this.map.put(local3693_16, msdfTextRenderState);
                  guiRenderState.addPreparedTextElement(msdfTextRenderState);
               }

               this.processData2(msdfTextRenderState, guiRenderState, currentLayerSerial, matrix3x2f, screenRect, fontAtlas, msdfTextDrawData, local3693_3, local3693_15);
            } catch (RuntimeException exception) {
               HorusClient.logger().warn("[MSDF] Failed to submit text: {}", msdfTextDrawData.text(), exception);
            }
         }
      }
   }

   private static float computeFloat7(float local3694_0) {
      return local3694_0 * local3694_0 * (3.0F - 2.0F * local3694_0);
   }

   private record DataRecord(float m00, float m01, float m10, float m11, float m20, float m21) {
      static MsdfTextRenderer.DataRecord of(Matrix3x2f matrix3x2f) {
         return new MsdfTextRenderer.DataRecord(
            matrix3x2f.m00(), matrix3x2f.m01(), matrix3x2f.m10(), matrix3x2f.m11(), matrix3x2f.m20(), matrix3x2f.m21()
         );
      }
   }

   private interface SubInterface2 {
      void processData6(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10);
   }

   private record DataRecord3(
      GuiRenderState state, int layerSerial, Identifier atlas, MsdfTextRenderer.DataRecord pose, ScreenRect scissorArea, boolean shimmer, int shimmerParams
   ) {
   }

   private record DataRecord4(
      GuiRenderState state, int layerSerial, SmallTextAtlasBaker.SubContext page, MsdfTextRenderer.DataRecord pose, ScreenRect scissorArea
   ) {
   }

   private record DataRecord5(AbstractTexture texture, TextureSetup setup) {
   }
}

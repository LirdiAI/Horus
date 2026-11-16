package horus.client.render;

import horus.client.HorusClient;
import horus.mixin.DrawContextMixin;
import horus.client.util.GuiScale;
import horus.client.render.GuiLayerSerialAccess;
import horus.client.render.HalfIconDrawData;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2f;
import org.lwjgl.system.MemoryStack;

public final class RectHalfIconRenderer implements AutoCloseable {
   private static final VertexFormat vertexFormat = VertexFormat.builder()
      .add("Position", VertexFormatElement.POSITION)
      .add("UV0", VertexFormatElement.UV0)
      .add("Color", VertexFormatElement.COLOR)
      .add("LineWidth", VertexFormatElement.LINE_WIDTH)
      .build();
   private static volatile RectHalfIconRenderer rectHalfIconRenderer;
   public static final RenderPipeline pipeline2 = RenderPipeline.builder()
      .withLocation(getIdentifier("pipeline/rect_half_icon"))
      .withVertexShader(getIdentifier("core/rect_half_icon"))
      .withFragmentShader(getIdentifier("core/rect_half_icon"))
      .withVertexFormat(vertexFormat, DrawMode.QUADS)
      .withBlend(BlendFunction.TRANSLUCENT)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withCull(false)
      .withSampler("Sampler0")
      .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
      .withUniform("Projection", UniformType.UNIFORM_BUFFER)
      .withUniform("RectHalfIconParamsArray", UniformType.UNIFORM_BUFFER)
      .build();
   private final List<HalfIconDrawData> list = new ArrayList<>(32);
   private final Map<RectHalfIconRenderer.DataRecord, HalfIconRenderState> map = new LinkedHashMap<>(32);
   private DrawContext drawContext2;
   private GpuBuffer gpuBuffer2;
   private boolean flag = true;

   private static int computeInt(int local6502_0, float local6502_1) {
      int local6502_2 = Math.round((local6502_0 >>> 24 & 0xFF) * computeFloat2(local6502_1, 0.0F, 1.0F));
      return local6502_2 << 24 | local6502_0 & 16777215;
   }

   private static float computeFloat(long local6503_0, int local6503_2, int local6503_3, int local6503_4) {
      long local6503_5 = local6503_0 ^ local6503_2 * -7046029254386353131L ^ local6503_3 * -4658895280553007687L ^ local6503_4 * -7723592293110705685L;
      local6503_5 ^= local6503_5 >>> 30;
      local6503_5 *= -4658895280553007687L;
      local6503_5 ^= local6503_5 >>> 27;
      local6503_5 *= -7723592293110705685L;
      local6503_5 ^= local6503_5 >>> 31;
      return (float)(local6503_5 & 16777215L) / 1.6777215E7F * 2.0F - 1.0F;
   }

   public void updateState() {
      if (!this.list.isEmpty() && this.flag) {
         GpuBuffer gpuBuffer = this.getGpuBuffer2();
         if (gpuBuffer != null) {
            try {
               MemoryStack memoryStack = MemoryStack.stackPush();

               try {
                  ByteBuffer byteBuffer = this.getByteBuffer(memoryStack, this.list);
                  RenderSystem.getDevice().createCommandEncoder().writeToBuffer(gpuBuffer.slice(0L, byteBuffer.remaining()), byteBuffer);
                  this.flag = false;
               } catch (Throwable throwable) {
                  if (memoryStack != null) {
                     try {
                        memoryStack.close();
                     } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                     }
                  }

                  throw throwable;
               }

               if (memoryStack != null) {
                  memoryStack.close();
               }
            } catch (RuntimeException exception) {
               this.flag = true;
            }
         }
      }
   }

   private static float computeFloat2(float local6505_0, float local6505_1, float local6505_2) {
      return Math.max(local6505_1, Math.min(local6505_2, local6505_0));
   }

   private void renderElement(DrawContext drawContext, HalfIconDrawData halfIconDrawData) {
      if (drawContext != null && halfIconDrawData != null && halfIconDrawData.iconsVisible()) {
         try {
            HalfIconDrawData halfIconDrawData2 = this.getHalfIconDrawData(halfIconDrawData);
            TextAtlasRenderer.DataRecord2 local6506_4 = TextAtlasRenderer.getTextAtlasRenderer()
               .getNested0465(halfIconDrawData2.fontName(), halfIconDrawData2.icon(), halfIconDrawData2.iconSize());
            if (local6506_4.empty()) {
               return;
            }

            Matrix3x2f matrix3x2f = GuiScale.renderElement(drawContext);
            GuiRenderState guiRenderState = ((DrawContextMixin)drawContext).getGuiRenderState();
            int currentLayerSerial = ((GuiLayerSerialAccess)guiRenderState).getLayerSerial();
            RectHalfIconRenderer.DataRecord3 local6506_8 = RectHalfIconRenderer.DataRecord3.of(matrix3x2f);

            for (TextAtlasRenderer.DataRecord4 local6506_10 : local6506_4.pages()) {
               if (!local6506_10.glyphs().isEmpty()) {
                  List<HalfIconRenderState.DataRecord> local6506_11 = this.getList(halfIconDrawData2, local6506_4, local6506_10);
                  if (!local6506_11.isEmpty()) {
                     RectHalfIconRenderer.DataRecord local6506_12 = new RectHalfIconRenderer.DataRecord(
                        guiRenderState, currentLayerSerial, local6506_10.textureSetup(), local6506_8
                     );
                     HalfIconRenderState halfIconRenderState = this.map.get(local6506_12);
                     if (halfIconRenderState == null) {
                        halfIconRenderState = new HalfIconRenderState(matrix3x2f, local6506_10.textureSetup());
                        halfIconRenderState.add(halfIconDrawData2, local6506_11);
                        this.map.put(local6506_12, halfIconRenderState);
                        guiRenderState.addSimpleElement(halfIconRenderState);
                     } else {
                        halfIconRenderState.add(halfIconDrawData2, local6506_11);
                     }
                  }
               }
            }
         } catch (RuntimeException exception) {
            HorusClient.logger().warn("[HalfIconRectangle] Failed to submit icon rectangle", exception);
         }
      }
   }

   private List<HalfIconRenderState.DataRecord> getList(
      HalfIconDrawData halfIconDrawData, TextAtlasRenderer.DataRecord2 local6507_2, TextAtlasRenderer.DataRecord4 local6507_3
   ) {
      List<HalfIconRenderState.DataRecord> local6507_4 = new ArrayList<>(64);
      float local6507_5 = Math.max(halfIconDrawData.spacingX(), Math.max(local6507_2.width(), 1.0F));
      float local6507_6 = Math.max(halfIconDrawData.spacingY(), Math.max(local6507_2.height(), 1.0F));
      float local6507_7 = halfIconDrawData.x() + halfIconDrawData.paddingX() + local6507_2.width() * 0.5F;
      float local6507_8 = halfIconDrawData.y() + halfIconDrawData.paddingY() + local6507_2.height() * 0.5F;
      float local6507_9 = halfIconDrawData.x() + halfIconDrawData.width() - halfIconDrawData.paddingX() - local6507_2.width() * 0.5F;
      float local6507_10 = halfIconDrawData.y() + halfIconDrawData.height() - halfIconDrawData.paddingY() - local6507_2.height() * 0.5F;
      if (!(local6507_9 < local6507_7) && !(local6507_10 < local6507_8)) {
         int local6507_11 = Math.min(96, Math.max(1, (int)Math.floor((local6507_10 - local6507_8) / local6507_6) + 1));
         int local6507_12 = Math.min(96, Math.max(1, (int)Math.floor((local6507_9 - local6507_7) / local6507_5) + 1));

         for (int index = 0; index < local6507_11; index++) {
            for (int index2 = 0; index2 < local6507_12; index2++) {
               float local6507_15 = local6507_7
                  + index2 * local6507_5
                  + computeFloat(halfIconDrawData.seed(), index2, index, 1) * halfIconDrawData.jitterX();
               float local6507_16 = local6507_8
                  + index * local6507_6
                  + computeFloat(halfIconDrawData.seed(), index2, index, 2) * halfIconDrawData.jitterY();
               float local6507_17 = (float)Math.toRadians(computeFloat(halfIconDrawData.seed(), index2, index, 3) * halfIconDrawData.rotationMaxDegrees());
               float local6507_18 = (float)Math.sin(local6507_17);
               float local6507_19 = (float)Math.cos(local6507_17);
               float local6507_20 = local6507_15 - local6507_2.width() * 0.5F;
               float local6507_21 = local6507_16 - local6507_2.height() * 0.5F;
               int local6507_22 = computeInt(
                  halfIconDrawData.iconColor(), 1.0F - computeFloat2((local6507_16 - halfIconDrawData.y()) / Math.max(halfIconDrawData.height(), 1.0F), 0.0F, 1.0F) * 0.92F
               );

               for (TextAtlasRenderer.DataRecord3 local6507_24 : local6507_3.glyphs()) {
                  float local6507_25 = local6507_20 + local6507_24.x0();
                  float local6507_26 = local6507_21 + local6507_24.y0();
                  float local6507_27 = local6507_20 + local6507_24.x1();
                  float local6507_28 = local6507_21 + local6507_24.y1();
                  RectHalfIconRenderer.DataRecord2 local6507_29 = getNested0511(local6507_25, local6507_26, local6507_15, local6507_16, local6507_18, local6507_19);
                  RectHalfIconRenderer.DataRecord2 local6507_30 = getNested0511(local6507_25, local6507_28, local6507_15, local6507_16, local6507_18, local6507_19);
                  RectHalfIconRenderer.DataRecord2 local6507_31 = getNested0511(local6507_27, local6507_28, local6507_15, local6507_16, local6507_18, local6507_19);
                  RectHalfIconRenderer.DataRecord2 local6507_32 = getNested0511(local6507_27, local6507_26, local6507_15, local6507_16, local6507_18, local6507_19);
                  local6507_4.add(
                     new HalfIconRenderState.DataRecord(
                        local6507_29.x(),
                        local6507_29.y(),
                        local6507_24.u0(),
                        local6507_24.v0(),
                        local6507_30.x(),
                        local6507_30.y(),
                        local6507_24.u0(),
                        local6507_24.v1(),
                        local6507_31.x(),
                        local6507_31.y(),
                        local6507_24.u1(),
                        local6507_24.v1(),
                        local6507_32.x(),
                        local6507_32.y(),
                        local6507_24.u1(),
                        local6507_24.v0(),
                        local6507_22
                     )
                  );
               }
            }
         }

         return local6507_4;
      } else {
         return List.of();
      }
   }

   private ByteBuffer getByteBuffer(MemoryStack memoryStack, List<HalfIconDrawData> local6508_2) {
      ByteBuffer byteBuffer = memoryStack.calloc(Math.min(12288, local6508_2.size() * 3 * 4 * 4));

      for (int index = 0; index < local6508_2.size(); index++) {
         HalfIconDrawData halfIconDrawData = local6508_2.get(index);
         int local6508_6 = index * 3 * 4 * 4;
         byteBuffer.putFloat(local6508_6, halfIconDrawData.x());
         byteBuffer.putFloat(local6508_6 + 4, halfIconDrawData.y());
         byteBuffer.putFloat(local6508_6 + 8, halfIconDrawData.width());
         byteBuffer.putFloat(local6508_6 + 12, halfIconDrawData.height());
         byteBuffer.putFloat(local6508_6 + 16, halfIconDrawData.radiusTopLeft());
         byteBuffer.putFloat(local6508_6 + 20, halfIconDrawData.radiusTopRight());
         byteBuffer.putFloat(local6508_6 + 24, halfIconDrawData.radiusBottomRight());
         byteBuffer.putFloat(local6508_6 + 28, halfIconDrawData.radiusBottomLeft());
         byteBuffer.putFloat(local6508_6 + 32, halfIconDrawData.smoothness());
         byteBuffer.putFloat(local6508_6 + 36, 0.0F);
         byteBuffer.putFloat(local6508_6 + 40, 0.0F);
         byteBuffer.putFloat(local6508_6 + 44, 0.0F);
      }

      byteBuffer.position(0);
      return byteBuffer;
   }

   private static RectHalfIconRenderer.DataRecord2 getNested0511(
      float local6509_0, float local6509_1, float local6509_2, float local6509_3, float local6509_4, float local6509_5
   ) {
      float local6509_6 = local6509_0 - local6509_2;
      float local6509_7 = local6509_1 - local6509_3;
      return new RectHalfIconRenderer.DataRecord2(
         local6509_2 + local6509_6 * local6509_5 - local6509_7 * local6509_4, local6509_3 + local6509_6 * local6509_4 + local6509_7 * local6509_5
      );
   }

   private HalfIconDrawData getHalfIconDrawData(HalfIconDrawData halfIconDrawData) {
      float local6510_2 = Math.max(0.0F, Math.min(halfIconDrawData.width(), halfIconDrawData.height()) * 0.5F);
      float local6510_3 = Math.max(halfIconDrawData.iconSize(), 1.0F);
      return new HalfIconDrawData(
         halfIconDrawData.x(),
         halfIconDrawData.y(),
         halfIconDrawData.width(),
         halfIconDrawData.height(),
         computeFloat2(halfIconDrawData.radiusTopLeft(), 0.0F, local6510_2),
         computeFloat2(halfIconDrawData.radiusTopRight(), 0.0F, local6510_2),
         computeFloat2(halfIconDrawData.radiusBottomRight(), 0.0F, local6510_2),
         computeFloat2(halfIconDrawData.radiusBottomLeft(), 0.0F, local6510_2),
         halfIconDrawData.colorTopLeft(),
         halfIconDrawData.colorTopRight(),
         halfIconDrawData.colorBottomRight(),
         halfIconDrawData.colorBottomLeft(),
         Math.max(halfIconDrawData.smoothness(), 0.0F),
         halfIconDrawData.fontName(),
         halfIconDrawData.icon(),
         local6510_3,
         halfIconDrawData.iconColor(),
         Math.max(halfIconDrawData.spacingX(), local6510_3),
         Math.max(halfIconDrawData.spacingY(), local6510_3),
         Math.max(halfIconDrawData.paddingX(), 0.0F),
         Math.max(halfIconDrawData.paddingY(), 0.0F),
         Math.max(halfIconDrawData.jitterX(), 0.0F),
         Math.max(halfIconDrawData.jitterY(), 0.0F),
         computeFloat2(halfIconDrawData.rotationMaxDegrees(), 0.0F, 90.0F),
         halfIconDrawData.seed()
      );
   }

   private void updateState2() {
      if (this.gpuBuffer2 != null) {
         this.gpuBuffer2.close();
         this.gpuBuffer2 = null;
      }
   }

   public static RectHalfIconRenderer getRectHalfIconRenderer() {
      RectHalfIconRenderer currentRectHalfIconRenderer = rectHalfIconRenderer;
      if (currentRectHalfIconRenderer == null) {
         synchronized (RectHalfIconRenderer.class) {
            currentRectHalfIconRenderer = rectHalfIconRenderer;
            if (currentRectHalfIconRenderer == null) {
               currentRectHalfIconRenderer = new RectHalfIconRenderer();
               rectHalfIconRenderer = currentRectHalfIconRenderer;
            }
         }
      }

      return currentRectHalfIconRenderer;
   }

   private GpuBuffer getGpuBuffer() {
      if (!this.flag && this.gpuBuffer2 != null) {
         return this.gpuBuffer2;
      }

      this.updateState();
      if (!this.flag && this.gpuBuffer2 != null) {
         return this.gpuBuffer2;
      }

      this.updateState2();
      this.updateState();
      return this.gpuBuffer2;
   }

   int computeInt2(HalfIconDrawData halfIconDrawData) {
      int local6515_2 = this.list.size();
      if (local6515_2 == 256) {
         return -1;
      }

      this.list.add(halfIconDrawData);
      this.flag = true;
      return local6515_2;
   }

   public void processData(RenderPass renderPass) {
      if (renderPass != null && !this.list.isEmpty()) {
         GpuBuffer gpuBuffer = this.getGpuBuffer();
         if (gpuBuffer != null) {
            renderPass.setUniform("RectHalfIconParamsArray", gpuBuffer);
         }
      }
   }

   private static Identifier getIdentifier(String local6517_0) {
      return Identifier.of("horus", local6517_0);
   }

   public boolean matchesCriteria(RenderPipeline renderPipeline) {
      return renderPipeline == pipeline2;
   }

   public void processData2(HalfIconDrawData halfIconDrawData) {
      this.renderElement(this.drawContext2, halfIconDrawData);
   }

   public static void updateState3() {
      RectHalfIconRenderer currentRectHalfIconRenderer = rectHalfIconRenderer;
      if (currentRectHalfIconRenderer != null) {
         currentRectHalfIconRenderer.close();
         rectHalfIconRenderer = null;
      }
   }

   @Override
   public void close() {
      this.list.clear();
      this.map.clear();
      this.drawContext2 = null;
      this.updateState2();
   }

   private GpuBuffer getGpuBuffer2() {
      if (this.gpuBuffer2 != null && !this.gpuBuffer2.isClosed() && this.gpuBuffer2.size() >= 12288L) {
         return this.gpuBuffer2;
      }

      this.updateState2();

      try {
         this.gpuBuffer2 = RenderSystem.getDevice().createBuffer(() -> "horus_rect_half_icon_params", 136, 12288L);
         return this.gpuBuffer2;
      } catch (RuntimeException exception) {
         return null;
      }
   }

   private RectHalfIconRenderer() {
   }

   public void renderElement2(DrawContext drawContext, HalfIconDrawData halfIconDrawData) {
      this.renderElement3(drawContext);
      this.processData2(halfIconDrawData);
      this.updateState5();
   }

   public void updateState4() {
      this.list.clear();
      this.flag = false;
   }

   public void renderElement3(DrawContext drawContext) {
      if (this.drawContext2 != drawContext) {
         this.map.clear();
      }

      this.drawContext2 = drawContext;
   }

   public void updateState5() {
      this.drawContext2 = null;
      this.map.clear();
   }

   private record DataRecord(GuiRenderState state, int layerSerial, TextureSetup textureSetup, RectHalfIconRenderer.DataRecord3 pose) {
   }

   private record DataRecord2(float x, float y) {
   }

   private record DataRecord3(float m00, float m01, float m10, float m11, float m20, float m21) {
      static RectHalfIconRenderer.DataRecord3 of(Matrix3x2f matrix3x2f) {
         return new RectHalfIconRenderer.DataRecord3(
            matrix3x2f.m00(), matrix3x2f.m01(), matrix3x2f.m10(), matrix3x2f.m11(), matrix3x2f.m20(), matrix3x2f.m21()
         );
      }
   }
}

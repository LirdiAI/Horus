package horus.client.render;

import horus.mixin.DrawContextMixin;
import horus.client.util.GuiScale;
import horus.client.render.ThemePaletteBuffer;
import horus.client.render.GradientSplitBuffer;
import horus.client.render.GlowDrawBatch;
import horus.client.render.ScissorStack;
import horus.client.render.PaletteTransition;
import horus.client.render.GlowCompositeDrawData;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderSystem.ShapeIndexBuffer;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public final class GlowCompositeRenderer implements AutoCloseable {
   private static volatile GlowCompositeRenderer glowCompositeRenderer;
   public static final RenderPipeline pipeline2 = RenderPipeline.builder()
      .withLocation(getIdentifier("pipeline/glow_composite"))
      .withVertexShader(getIdentifier("ui/glow/glow_composite"))
      .withFragmentShader(getIdentifier("ui/glow/glow_composite"))
      .withVertexFormat(VertexFormats.POSITION_COLOR_LINE_WIDTH, DrawMode.QUADS)
      .withBlend(BlendFunction.LIGHTNING)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withCull(false)
      .withSampler("Sampler0")
      .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
      .withUniform("Projection", UniformType.UNIFORM_BUFFER)
      .withUniform("GlowParamsArray", UniformType.UNIFORM_BUFFER)
      .build();
   private static final RenderPipeline pipeline3 = RenderPipeline.builder()
      .withLocation(getIdentifier("pipeline/glow_shape"))
      .withVertexShader(getIdentifier("ui/glow/glow_shape"))
      .withFragmentShader(getIdentifier("ui/glow/glow_shape"))
      .withVertexFormat(VertexFormats.POSITION, DrawMode.QUADS)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withCull(false)
      .withUniform("GlowParamsArray", UniformType.UNIFORM_BUFFER)
      .withUniform("SplitParams", UniformType.UNIFORM_BUFFER)
      .build();
   private static final RenderPipeline pipeline4 = RenderPipeline.builder()
      .withLocation(getIdentifier("pipeline/glow_source"))
      .withVertexShader(getIdentifier("ui/glow/glow_source"))
      .withFragmentShader(getIdentifier("ui/glow/glow_shape"))
      .withVertexFormat(VertexFormats.POSITION, DrawMode.QUADS)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withCull(false)
      .withUniform("GlowParamsArray", UniformType.UNIFORM_BUFFER)
      .withUniform("SplitParams", UniformType.UNIFORM_BUFFER)
      .build();
   private static final RenderPipeline pipeline5 = RenderPipeline.builder()
      .withLocation(getIdentifier("pipeline/glow_blur_down"))
      .withVertexShader(getIdentifier("ui/glow/glow_blur"))
      .withFragmentShader(getIdentifier("ui/glow/glow_blur_down"))
      .withVertexFormat(VertexFormats.POSITION, DrawMode.QUADS)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withCull(false)
      .withSampler("Sampler0")
      .withUniform("KawaseParams", UniformType.UNIFORM_BUFFER)
      .build();
   private static final RenderPipeline pipeline6 = RenderPipeline.builder()
      .withLocation(getIdentifier("pipeline/glow_blur_up"))
      .withVertexShader(getIdentifier("ui/glow/glow_blur"))
      .withFragmentShader(getIdentifier("ui/glow/glow_blur_up"))
      .withVertexFormat(VertexFormats.POSITION, DrawMode.QUADS)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withCull(false)
      .withSampler("Sampler0")
      .withUniform("KawaseParams", UniformType.UNIFORM_BUFFER)
      .build();
   private static final RenderPipeline pipeline7 = RenderPipeline.builder()
      .withLocation(getIdentifier("pipeline/glow_cutout"))
      .withVertexShader(getIdentifier("ui/glow/glow_blur"))
      .withFragmentShader(getIdentifier("ui/glow/glow_cutout"))
      .withVertexFormat(VertexFormats.POSITION, DrawMode.QUADS)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withCull(false)
      .withSampler("Sampler0")
      .withSampler("Sampler1")
      .withUniform("KawaseParams", UniformType.UNIFORM_BUFFER)
      .build();
   private final List<SimpleFramebuffer[]> list = new ArrayList<>(224);
   private final List<SimpleFramebuffer[]> list2 = new ArrayList<>(224);
   private final List<SimpleFramebuffer> list3 = new ArrayList<>(224);
   private final List<SimpleFramebuffer> list4 = new ArrayList<>(224);
   private final List<SimpleFramebuffer> list5 = new ArrayList<>(224);
   private int intVal;
   private long timeMillis;
   private final List<GlowCompositeRenderer.DataRecord> list6 = new ArrayList<>(32);
   private final List<GlowCompositeDrawData> list7 = new ArrayList<>(32);
   private final List<GlowDrawBatch> list8 = new ArrayList<>(32);
   private int[] intValArray = new int[224];
   private int[] intValArray2 = new int[224];
   private int[] intValArray3 = new int[224];
   private int[] intValArray4 = new int[224];
   private DrawContext drawContext2;
   private GpuBuffer gpuBuffer4;
   private GpuBuffer gpuBuffer5;
   private GpuBuffer gpuBuffer6;
   private GpuBuffer gpuBuffer7;
   private GpuBuffer gpuBuffer8;
   private ByteBuffer byteBuffer4;
   private ByteBuffer byteBuffer5;
   private boolean flag = true;
   private boolean flag2;
   private final GlowDrawBatch glowDrawBatch = new GlowDrawBatch();

   public void updateState() {
      this.list7.clear();
      this.list8.clear();
      this.flag = false;
      this.flag2 = false;
   }

   private ByteBuffer getByteBuffer(int local2783_1) {
      if (this.byteBuffer4 == null || this.byteBuffer4.capacity() < local2783_1) {
         if (this.byteBuffer4 != null) {
            MemoryUtil.memFree(this.byteBuffer4);
         }

         this.byteBuffer4 = MemoryUtil.memAlloc(Math.max(local2783_1, 64 * VertexFormats.POSITION.getVertexSize()));
      }

      this.byteBuffer4.clear();
      return this.byteBuffer4;
   }

   public void renderElement(DrawContext drawContext) {
      this.drawContext2 = drawContext;
   }

   @Override
   public void close() {
      this.list7.clear();
      this.drawContext2 = null;
      this.intVal = 0;
      this.updateState2();
      this.updateState5();
      if (this.gpuBuffer6 != null) {
         this.gpuBuffer6.close();
         this.gpuBuffer6 = null;
      }

      if (this.gpuBuffer7 != null) {
         this.gpuBuffer7.close();
         this.gpuBuffer7 = null;
      }

      if (this.gpuBuffer8 != null) {
         this.gpuBuffer8.close();
         this.gpuBuffer8 = null;
      }

      if (this.byteBuffer4 != null) {
         MemoryUtil.memFree(this.byteBuffer4);
         this.byteBuffer4 = null;
      }

      if (this.byteBuffer5 != null) {
         MemoryUtil.memFree(this.byteBuffer5);
         this.byteBuffer5 = null;
      }

      for (SimpleFramebuffer simpleFramebuffer : this.list3) {
         if (simpleFramebuffer != null) {
            simpleFramebuffer.delete();
         }
      }

      for (SimpleFramebuffer simpleFramebuffer2 : this.list4) {
         if (simpleFramebuffer2 != null) {
            simpleFramebuffer2.delete();
         }
      }

      for (SimpleFramebuffer simpleFramebuffer3 : this.list5) {
         if (simpleFramebuffer3 != null) {
            simpleFramebuffer3.delete();
         }
      }

      this.list3.clear();
      this.list4.clear();
      this.list5.clear();
      processData5(this.list);
      processData5(this.list2);
   }

   private GpuBuffer getGpuBuffer() {
      if (this.gpuBuffer4 != null && !this.gpuBuffer4.isClosed() && this.gpuBuffer4.size() >= 17920L) {
         return this.gpuBuffer4;
      }

      this.updateState2();

      try {
         this.gpuBuffer4 = RenderSystem.getDevice().createBuffer(() -> "horus_glow_params", 136, 17920L);
         return this.gpuBuffer4;
      } catch (RuntimeException exception) {
         return null;
      }
   }

   private static void processData(ByteBuffer byteBuffer, int local2789_1, float local2789_2, float local2789_3) {
      byteBuffer.putFloat(local2789_1, local2789_2);
      byteBuffer.putFloat(local2789_1 + 4, local2789_3);
      byteBuffer.putFloat(local2789_1 + 8, 0.0F);
   }

   private SimpleFramebuffer getSimpleFramebuffer(int local2790_1, int local2790_2, int local2790_3) {
      while (this.list4.size() <= local2790_1) {
         this.list4.add(null);
      }

      SimpleFramebuffer simpleFramebuffer = this.getSimpleFramebuffer3(this.list4.get(local2790_1), "horus_glow_blur_" + local2790_1, local2790_2, local2790_3);
      this.list4.set(local2790_1, simpleFramebuffer);
      return simpleFramebuffer;
   }

   public void renderElement2(DrawContext drawContext, GlowCompositeDrawData glowCompositeDrawData) {
      this.renderElement(drawContext);
      this.processData13(glowCompositeDrawData);
      this.updateState6();
   }

   private SimpleFramebuffer getSimpleFramebuffer2(int local2792_1, int local2792_2, int local2792_3) {
      while (this.list3.size() <= local2792_1) {
         this.list3.add(null);
      }

      SimpleFramebuffer simpleFramebuffer = this.getSimpleFramebuffer3(this.list3.get(local2792_1), "horus_glow_shape_" + local2792_1, local2792_2, local2792_3);
      this.list3.set(local2792_1, simpleFramebuffer);
      return simpleFramebuffer;
   }

   private void renderElement3(DrawContext drawContext, GlowCompositeDrawData glowCompositeDrawData) {
      if (drawContext != null && glowCompositeDrawData != null && glowCompositeDrawData.visible()) {
         try {
            Matrix3x2f matrix3x2f = GuiScale.renderElement(drawContext);
            GlowDrawBatch currentGlowDrawBatch = new GlowDrawBatch();
            if (this.list6.size() < 224) {
               currentGlowDrawBatch.intVal = this.list6.size();
               this.list6.add(new GlowCompositeRenderer.DataRecord(glowCompositeDrawData, currentGlowDrawBatch, matrix3x2f));
            }

            ((DrawContextMixin)drawContext)
               .getGuiRenderState()
               .addSimpleElement(new GlowCompositeRenderState(matrix3x2f, glowCompositeDrawData, ScissorStack.getScreenRect(), currentGlowDrawBatch));
         } catch (RuntimeException exception) {
         }
      }
   }

   private int computeInt(int local2794_1) {
      int local2794_2 = this.list6.size();
      if (this.intValArray.length < local2794_2) {
         this.intValArray = new int[local2794_2];
         this.intValArray2 = new int[local2794_2];
         this.intValArray3 = new int[local2794_2];
         this.intValArray4 = new int[local2794_2];
      }

      float local2794_3 = 8.0F;

      for (int index = 0; index < local2794_2; index++) {
         local2794_3 = Math.max(local2794_3, this.list6.get(index).glow().effectivePad());
      }

      int local2794_4 = Math.max(8, Math.round(local2794_3 * 1.0F));
      int local2794_5 = local2794_4;
      int local2794_6 = local2794_4;
      int local2794_7 = 0;

      for (int index2 = 0; index2 < local2794_2; index2++) {
         GlowCompositeDrawData glowCompositeDrawData = this.list6.get(index2).glow();
         float local2794_10 = glowCompositeDrawData.effectivePad();
         int local2794_11 = Math.max(Math.round((glowCompositeDrawData.width() + local2794_10 * 2.0F) * 1.0F), 1);
         int local2794_12 = Math.max(Math.round((glowCompositeDrawData.height() + local2794_10 * 2.0F) * 1.0F), 1);
         if (local2794_5 + local2794_11 + local2794_4 > local2794_1) {
            local2794_5 = local2794_4;
            local2794_6 += local2794_7 + local2794_4;
            local2794_7 = 0;
         }

         if (local2794_11 + local2794_4 * 2 <= local2794_1 && local2794_6 + local2794_12 + local2794_4 <= 2048) {
            this.intValArray[index2] = local2794_5;
            this.intValArray2[index2] = local2794_6;
            this.intValArray3[index2] = local2794_11;
            this.intValArray4[index2] = local2794_12;
            local2794_5 += local2794_11 + local2794_4;
            local2794_7 = Math.max(local2794_7, local2794_12);
         } else {
            this.intValArray3[index2] = 0;
            this.intValArray4[index2] = 0;
         }
      }

      return local2794_6 + local2794_7 + local2794_4;
   }

   private void processData2(SimpleFramebuffer simpleFramebuffer, int local2795_2, int local2795_3) {
      int local2795_4 = this.list6.size();
      if (simpleFramebuffer != null && local2795_4 != 0) {
         int local2795_5 = Math.max(1, RenderSystem.getDevice().getUniformOffsetAlignment());
         int local2795_6 = (1248 + local2795_5 - 1) / local2795_5 * local2795_5;
         int local2795_7 = 4 * VertexFormats.POSITION.getVertexSize();
         ByteBuffer byteBuffer = this.getByteBuffer(local2795_4 * local2795_7);
         ByteBuffer byteBuffer2 = this.getByteBuffer3(local2795_4 * local2795_6);
         int local2795_10 = 0;

         for (int index = 0; index < local2795_4; index++) {
            GlowCompositeRenderer.DataRecord local2795_12 = this.list6.get(index);
            GlowDrawBatch currentGlowDrawBatch = local2795_12.capture();
            if (this.intValArray3[index] > 0 && this.intValArray4[index] > 0) {
               GlowCompositeDrawData glowCompositeDrawData = local2795_12.glow();
               int local2795_15 = this.intValArray[index];
               int local2795_16 = this.intValArray2[index];
               float local2795_17 = glowCompositeDrawData.effectivePad();
               float local2795_18 = Math.min((glowCompositeDrawData.width() + local2795_17 * 2.0F) * 1.0F, this.intValArray3[index]);
               float local2795_19 = Math.min((glowCompositeDrawData.height() + local2795_17 * 2.0F) * 1.0F, this.intValArray4[index]);
               currentGlowDrawBatch.floatVal = (float)local2795_15 / local2795_2;
               currentGlowDrawBatch.floatVal2 = (float)local2795_16 / local2795_3;
               currentGlowDrawBatch.floatVal3 = Math.max(local2795_18 / local2795_2, 1.0E-6F);
               currentGlowDrawBatch.floatVal4 = Math.max(local2795_19 / local2795_3, 1.0E-6F);
               float local2795_20 = (float)local2795_15 / local2795_2 * 2.0F - 1.0F;
               float local2795_21 = (float)local2795_16 / local2795_3 * 2.0F - 1.0F;
               float local2795_22 = (local2795_15 + local2795_18) / local2795_2 * 2.0F - 1.0F;
               float local2795_23 = (local2795_16 + local2795_19) / local2795_3 * 2.0F - 1.0F;
               int local2795_24 = local2795_10 * local2795_7;
               processData(byteBuffer, local2795_24, local2795_20, local2795_21);
               processData(byteBuffer, local2795_24 + 12, local2795_20, local2795_23);
               processData(byteBuffer, local2795_24 + 24, local2795_22, local2795_23);
               processData(byteBuffer, local2795_24 + 36, local2795_22, local2795_21);
               ByteBuffer byteBuffer3 = byteBuffer2.slice(local2795_10 * local2795_6, 1248).order(byteBuffer2.order());
               MemoryUtil.memSet(MemoryUtil.memAddress(byteBuffer3), 0, 1248L);
               processData10(byteBuffer3, 0, glowCompositeDrawData, 0.0F, 0.0F, 1.0F, 1.0F);
               byteBuffer3.putFloat(48, glowCompositeDrawData.splitIndex());
               processData9(byteBuffer3, glowCompositeDrawData);
               processData6(byteBuffer3);
               local2795_10++;
            } else {
               currentGlowDrawBatch.floatVal = 0.0F;
               currentGlowDrawBatch.floatVal2 = 0.0F;
               currentGlowDrawBatch.floatVal3 = 0.0F;
               currentGlowDrawBatch.floatVal4 = 0.0F;
            }
         }

         if (local2795_10 != 0) {
            int local2795_11 = local2795_10 * local2795_7;
            int local2795_12 = local2795_10 * local2795_6;

            try {
               CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
               GpuBuffer gpuBuffer = this.getGpuBuffer3(local2795_11);
               GpuBuffer gpuBuffer2 = this.getGpuBuffer5(local2795_12);
               byteBuffer.position(0).limit(local2795_11);
               commandEncoder.writeToBuffer(gpuBuffer.slice(0L, local2795_11), byteBuffer);
               byteBuffer2.position(0).limit(local2795_12);
               commandEncoder.writeToBuffer(gpuBuffer2.slice(0L, local2795_12), byteBuffer2);
               byteBuffer.clear();
               byteBuffer2.clear();
               ShapeIndexBuffer sequentialBuffer = RenderSystem.getSequentialBuffer(DrawMode.QUADS);
               GpuBuffer indexBuffer = sequentialBuffer.getIndexBuffer(6 * local2795_10);

               try (RenderPass renderPass = commandEncoder.createRenderPass(
                     () -> "horus_glow_source", simpleFramebuffer.getColorAttachmentView(), OptionalInt.empty(), null, OptionalDouble.empty()
                  )) {
                  renderPass.setPipeline(pipeline4);
                  GpuBuffer gpuBuffer3 = GradientSplitBuffer.getGpuBuffer();
                  if (gpuBuffer3 != null) {
                     renderPass.setUniform("SplitParams", gpuBuffer3);
                  }

                  renderPass.setVertexBuffer(0, gpuBuffer);
                  renderPass.setIndexBuffer(indexBuffer, sequentialBuffer.getIndexType());

                  for (int index2 = 0; index2 < local2795_10; index2++) {
                     renderPass.setUniform("GlowParamsArray", gpuBuffer2.slice((long)index2 * local2795_6, 1248L));
                     renderPass.drawIndexed(0, index2 * 6, 6, 1);
                  }
               }
            } catch (RuntimeException exception) {
               byteBuffer.clear();
               byteBuffer2.clear();
            }
         }
      }
   }

   private void updateState2() {
      if (this.gpuBuffer4 != null) {
         this.gpuBuffer4.close();
         this.gpuBuffer4 = null;
      }
   }

   private BuiltBuffer getBuiltBuffer(BufferAllocator bufferAllocator) {
      BufferBuilder bufferBuilder = new BufferBuilder(bufferAllocator, DrawMode.QUADS, VertexFormats.POSITION);
      bufferBuilder.vertex(-1.0F, -1.0F, 0.0F);
      bufferBuilder.vertex(-1.0F, 1.0F, 0.0F);
      bufferBuilder.vertex(1.0F, 1.0F, 0.0F);
      bufferBuilder.vertex(1.0F, -1.0F, 0.0F);
      return bufferBuilder.end();
   }

   private GpuBuffer getGpuBuffer2() {
      if (this.gpuBuffer5 != null && !this.gpuBuffer5.isClosed() && this.gpuBuffer5.size() >= 48L) {
         return this.gpuBuffer5;
      }

      this.updateState5();

      try {
         this.gpuBuffer5 = RenderSystem.getDevice().createBuffer(() -> "horus_glow_kawase_params", 136, 48L);
         return this.gpuBuffer5;
      } catch (RuntimeException exception) {
         return null;
      }
   }

   private static float computeFloat(float local2800_0, float local2800_1, float local2800_2) {
      return Math.max(local2800_1, Math.min(local2800_2, local2800_0));
   }

   private static Identifier getIdentifier(String local2801_0) {
      return Identifier.of("horus", local2801_0);
   }

   private GlowCompositeRenderer() {
   }

   int computeInt2(GlowCompositeDrawData glowCompositeDrawData, GlowDrawBatch newGlowDrawBatch) {
      int local2803_3 = this.list7.size();
      if (local2803_3 >= 224) {
         return -1;
      }

      this.list7.add(glowCompositeDrawData);
      this.list8.add(newGlowDrawBatch);
      this.flag = true;
      return local2803_3;
   }

   private ByteBuffer getByteBuffer2(MemoryStack memoryStack) {
      int local2806_2 = Math.max(1, this.list7.size());
      int local2806_3 = local2806_2 * 5 * 4 * 4;
      ByteBuffer byteBuffer = memoryStack.calloc(local2806_3);

      for (int index = 0; index < this.list7.size(); index++) {
         GlowCompositeDrawData glowCompositeDrawData = this.list7.get(index);
         GlowDrawBatch currentGlowDrawBatch = index < this.list8.size() ? this.list8.get(index) : null;
         float local2806_8 = currentGlowDrawBatch != null ? currentGlowDrawBatch.floatVal : 0.0F;
         float local2806_9 = currentGlowDrawBatch != null ? currentGlowDrawBatch.floatVal2 : 0.0F;
         float local2806_10 = currentGlowDrawBatch != null ? currentGlowDrawBatch.floatVal3 : 1.0F;
         float local2806_11 = currentGlowDrawBatch != null ? currentGlowDrawBatch.floatVal4 : 1.0F;
         processData10(byteBuffer, index, glowCompositeDrawData, local2806_8, local2806_9, local2806_10, local2806_11);
      }

      byteBuffer.position(0);
      return byteBuffer;
   }

   private void processData3(SimpleFramebuffer simpleFramebuffer) {
      if (simpleFramebuffer != null) {
         CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
         RenderPass renderPass = commandEncoder.createRenderPass(
            () -> "horus_glow_clear", simpleFramebuffer.getColorAttachmentView(), OptionalInt.of(0), null, OptionalDouble.empty()
         );
         if (renderPass != null) {
            renderPass.close();
         }
      }
   }

   public void updateState3() {
      if (!this.list6.isEmpty()) {
         MinecraftClient minecraftClient = MinecraftClient.getInstance();
         if (minecraftClient != null && minecraftClient.getWindow() != null) {
            int local2809_2 = this.computeInt(2048);
            int local2809_3 = 2048;
            int local2809_4 = Math.max(256, Math.min(2048, (local2809_2 + 256 - 1) / 256 * 256));
            long local2809_5 = System.nanoTime();
            if (local2809_4 >= this.intVal) {
               this.intVal = local2809_4;
               this.timeMillis = local2809_5;
            } else if (local2809_5 - this.timeMillis > 3000000000L) {
               this.intVal = local2809_4;
               this.timeMillis = local2809_5;
            }

            int local2809_7 = this.intVal;
            SimpleFramebuffer simpleFramebuffer = this.getSimpleFramebuffer2(0, local2809_3, local2809_7);
            SimpleFramebuffer simpleFramebuffer2 = this.getSimpleFramebuffer(0, local2809_3, local2809_7);
            SimpleFramebuffer simpleFramebuffer3 = this.getSimpleFramebuffer4(0, local2809_3, local2809_7);
            if (simpleFramebuffer != null && simpleFramebuffer2 != null && simpleFramebuffer3 != null) {
               GpuSampler gpuSampler = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
               this.processData3(simpleFramebuffer);
               float local2809_12 = 0.0F;

               for (int index = 0; index < this.list6.size(); index++) {
                  local2809_12 = Math.max(local2809_12, this.list6.get(index).glow().blurRadius());
               }

               this.processData2(simpleFramebuffer, local2809_3, local2809_7);
               int local2809_13 = 3;
               float local2809_14 = computeFloat(local2809_12 / 20.0F, 0.5F, 4.0F) * 1.0F;
               this.processData4(0, local2809_3, local2809_7, local2809_13);
               SimpleFramebuffer[] simpleFramebufferArray = this.list.get(0);
               SimpleFramebuffer[] simpleFramebufferArray2 = this.list2.get(0);
               GpuTextureView gpuTextureView = simpleFramebuffer.getColorAttachmentView();
               int local2809_18 = simpleFramebuffer.textureWidth;
               int local2809_19 = simpleFramebuffer.textureHeight;

               for (int index2 = 0; index2 < local2809_13; index2++) {
                  this.processData8(
                     pipeline5, gpuTextureView, local2809_18, local2809_19, simpleFramebufferArray[index2], local2809_14, 0.0F, 0.0F, 1.0F, 1.0F, gpuSampler
                  );
                  gpuTextureView = simpleFramebufferArray[index2].getColorAttachmentView();
                  local2809_18 = simpleFramebufferArray[index2].textureWidth;
                  local2809_19 = simpleFramebufferArray[index2].textureHeight;
               }

               for (int index3 = local2809_13 - 2; index3 >= 0; index3--) {
                  this.processData8(
                     pipeline6, gpuTextureView, local2809_18, local2809_19, simpleFramebufferArray2[index3], local2809_14, 0.0F, 0.0F, 1.0F, 1.0F, gpuSampler
                  );
                  gpuTextureView = simpleFramebufferArray2[index3].getColorAttachmentView();
                  local2809_18 = simpleFramebufferArray2[index3].textureWidth;
                  local2809_19 = simpleFramebufferArray2[index3].textureHeight;
               }

               this.processData8(pipeline6, gpuTextureView, local2809_18, local2809_19, simpleFramebuffer2, local2809_14, 0.0F, 0.0F, 1.0F, 1.0F, gpuSampler);
               this.processData12(simpleFramebuffer2, simpleFramebuffer, simpleFramebuffer3, gpuSampler);
               TextureSetup textureSetup = TextureSetup.of(simpleFramebuffer3.getColorAttachmentView(), gpuSampler);

               for (int index4 = 0; index4 < this.list6.size(); index4++) {
                  this.list6.get(index4).capture().textureSetup = textureSetup;
               }

               this.list6.clear();
            } else {
               this.list6.clear();
            }
         } else {
            this.list6.clear();
         }
      }
   }

   private GpuBuffer getGpuBuffer3(int local2812_1) {
      if (this.gpuBuffer7 == null || this.gpuBuffer7.isClosed() || this.gpuBuffer7.size() < local2812_1) {
         if (this.gpuBuffer7 != null) {
            this.gpuBuffer7.close();
         }

         int local2812_2 = Math.max(local2812_1, 64 * VertexFormats.POSITION.getVertexSize());
         local2812_2 = Integer.highestOneBit(local2812_2 - 1) << 1;
         this.gpuBuffer7 = RenderSystem.getDevice().createBuffer(() -> "horus_glow_src_quads", 40, local2812_2);
      }

      return this.gpuBuffer7;
   }

   public static void updateState4() {
      GlowCompositeRenderer currentGlowCompositeRenderer = glowCompositeRenderer;
      if (currentGlowCompositeRenderer != null) {
         currentGlowCompositeRenderer.close();
         glowCompositeRenderer = null;
      }
   }

   private void processData4(int local2814_1, int local2814_2, int local2814_3, int local2814_4) {
      while (this.list.size() <= local2814_1) {
         this.list.add(new SimpleFramebuffer[4]);
         this.list2.add(new SimpleFramebuffer[4]);
      }

      SimpleFramebuffer[] simpleFramebufferArray = this.list.get(local2814_1);
      SimpleFramebuffer[] simpleFramebufferArray2 = this.list2.get(local2814_1);
      int local2814_7 = Math.max(local2814_2, 1);
      int local2814_8 = Math.max(local2814_3, 1);

      for (int index = 0; index < local2814_4; index++) {
         simpleFramebufferArray[index] = this.getSimpleFramebuffer3(simpleFramebufferArray[index], "horus_glow_down_" + local2814_1 + "_" + index, local2814_7, local2814_8);
         if (index < local2814_4 - 1) {
            simpleFramebufferArray2[index] = this.getSimpleFramebuffer3(simpleFramebufferArray2[index], "horus_glow_up_" + local2814_1 + "_" + index, local2814_7, local2814_8);
         }

         local2814_7 = Math.max(local2814_7 / 2, 1);
         local2814_8 = Math.max(local2814_8 / 2, 1);
      }
   }

   private static void processData5(List<SimpleFramebuffer[]> local2816_0) {
      for (SimpleFramebuffer[] simpleFramebufferArray : local2816_0) {
         for (int index = 0; index < simpleFramebufferArray.length; index++) {
            if (simpleFramebufferArray[index] != null) {
               simpleFramebufferArray[index].delete();
               simpleFramebufferArray[index] = null;
            }
         }
      }

      local2816_0.clear();
   }

   private static void processData6(ByteBuffer byteBuffer) {
      int local2817_1 = 1120;
      int local2817_2 = Math.max(1, Math.min(ThemePaletteBuffer.computeInt3(), 6));
      int[] intArray = ThemePaletteBuffer.computeInt2();
      byteBuffer.putFloat(local2817_1, local2817_2);
      byteBuffer.putFloat(local2817_1 + 4, ThemePaletteBuffer.computeFloat4());
      byteBuffer.putFloat(local2817_1 + 8, ThemePaletteBuffer.computeFloat());
      byteBuffer.putFloat(local2817_1 + 12, PaletteTransition.getProgress());
      int local2817_4 = local2817_1 + 112;
      byteBuffer.putFloat(local2817_4, ThemePaletteBuffer.computeFloat3());
      byteBuffer.putFloat(local2817_4 + 4, ThemePaletteBuffer.computeFloat5());

      for (int index = 0; index < local2817_2 && index < intArray.length; index++) {
         int local2817_6 = intArray[index] & 16777215;
         int local2817_7 = local2817_1 + (1 + index) * 4 * 4;
         byteBuffer.putFloat(local2817_7, (local2817_6 >>> 16 & 0xFF) / 255.0F);
         byteBuffer.putFloat(local2817_7 + 4, (local2817_6 >>> 8 & 0xFF) / 255.0F);
         byteBuffer.putFloat(local2817_7 + 8, (local2817_6 & 0xFF) / 255.0F);
         byteBuffer.putFloat(local2817_7 + 12, 1.0F);
      }
   }

   private void processData7(int local2818_1, GlowCompositeDrawData glowCompositeDrawData, SimpleFramebuffer simpleFramebuffer) {
      GpuBuffer gpuBuffer = this.getGpuBuffer6();
      if (gpuBuffer != null && simpleFramebuffer != null) {
         GpuBuffer gpuBuffer2 = null;

         try {
            MemoryStack memoryStack = MemoryStack.stackPush();

            try {
               ByteBuffer byteBuffer = memoryStack.calloc(1248);
               processData10(byteBuffer, 0, glowCompositeDrawData, 0.0F, 0.0F, 1.0F, 1.0F);
               byteBuffer.putFloat(48, glowCompositeDrawData.splitIndex());
               processData9(byteBuffer, glowCompositeDrawData);
               processData6(byteBuffer);
               byteBuffer.position(0);
               gpuBuffer2 = RenderSystem.getDevice().createBuffer(() -> "horus_glow_shape_params_" + local2818_1, 136, byteBuffer);
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
            return;
         }

         try {
            CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
            ShapeIndexBuffer sequentialBuffer = RenderSystem.getSequentialBuffer(DrawMode.QUADS);
            GpuBuffer indexBuffer = sequentialBuffer.getIndexBuffer(6);

            try (RenderPass renderPass = commandEncoder.createRenderPass(
                  () -> "horus_glow_shape_" + local2818_1, simpleFramebuffer.getColorAttachmentView(), OptionalInt.of(0), null, OptionalDouble.empty()
               )) {
               renderPass.setPipeline(pipeline3);
               renderPass.setUniform("GlowParamsArray", gpuBuffer2);
               GpuBuffer gpuBuffer3 = GradientSplitBuffer.getGpuBuffer();
               if (gpuBuffer3 != null) {
                  renderPass.setUniform("SplitParams", gpuBuffer3);
               }

               renderPass.setVertexBuffer(0, gpuBuffer);
               renderPass.setIndexBuffer(indexBuffer, sequentialBuffer.getIndexType());
               renderPass.drawIndexed(0, 0, 6, 1);
            }
         } finally {
            gpuBuffer2.close();
         }
      }
   }

   private void updateState5() {
      if (this.gpuBuffer5 != null) {
         this.gpuBuffer5.close();
         this.gpuBuffer5 = null;
      }
   }

   public void updateState6() {
      this.drawContext2 = null;
   }

   private void processData8(
      RenderPipeline renderPipeline,
      GpuTextureView gpuTextureView,
      int local2822_3,
      int local2822_4,
      SimpleFramebuffer simpleFramebuffer,
      float local2822_6,
      float local2822_7,
      float local2822_8,
      float local2822_9,
      float local2822_10,
      GpuSampler gpuSampler
   ) {
      if (gpuTextureView != null && simpleFramebuffer != null) {
         GpuBuffer gpuBuffer = this.getGpuBuffer6();
         if (gpuBuffer != null) {
            GpuBuffer gpuBuffer2 = this.getGpuBuffer2();
            if (gpuBuffer2 != null) {
               CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
               ShapeIndexBuffer sequentialBuffer = RenderSystem.getSequentialBuffer(DrawMode.QUADS);
               GpuBuffer indexBuffer = sequentialBuffer.getIndexBuffer(6);
               MemoryStack memoryStack = MemoryStack.stackPush();

               try {
                  ByteBuffer byteBuffer = memoryStack.calloc(48);
                  byteBuffer.putFloat(0, local2822_7);
                  byteBuffer.putFloat(4, local2822_8);
                  byteBuffer.putFloat(8, local2822_9);
                  byteBuffer.putFloat(12, local2822_10);
                  byteBuffer.putFloat(16, local2822_6 / Math.max(local2822_3, 1));
                  byteBuffer.putFloat(20, local2822_6 / Math.max(local2822_4, 1));
                  byteBuffer.putFloat(32, 0.0F);
                  byteBuffer.putFloat(36, 0.0F);
                  byteBuffer.putFloat(40, 0.0F);
                  byteBuffer.putFloat(44, 0.0F);
                  byteBuffer.position(0);
                  commandEncoder.writeToBuffer(gpuBuffer2.slice(0L, 48L), byteBuffer);
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

               try (RenderPass local2822_17x = commandEncoder.createRenderPass(
                     () -> "horus_glow_kawase", simpleFramebuffer.getColorAttachmentView(), OptionalInt.of(0), null, OptionalDouble.empty()
                  )) {
                  local2822_17x.setPipeline(renderPipeline);
                  local2822_17x.bindTexture("Sampler0", gpuTextureView, gpuSampler);
                  local2822_17x.setUniform("KawaseParams", gpuBuffer2);
                  local2822_17x.setVertexBuffer(0, gpuBuffer);
                  local2822_17x.setIndexBuffer(indexBuffer, sequentialBuffer.getIndexType());
                  local2822_17x.drawIndexed(0, 0, 6, 1);
               }
            }
         }
      }
   }

   private static void processData9(ByteBuffer byteBuffer, GlowCompositeDrawData glowCompositeDrawData) {
      float[] floatArray = glowCompositeDrawData.spans();
      int local2823_3 = glowCompositeDrawData.spanCount();
      if (floatArray != null && local2823_3 > 0) {
         float local2823_4 = glowCompositeDrawData.effectivePad();
         float local2823_5 = Math.min(
            Math.min(glowCompositeDrawData.radiusTopLeft(), glowCompositeDrawData.radiusTopRight()), Math.min(glowCompositeDrawData.radiusBottomRight(), glowCompositeDrawData.radiusBottomLeft())
         );
         int local2823_6 = 80;
         byteBuffer.putFloat(local2823_6, Math.min(local2823_3, 64));
         byteBuffer.putFloat(local2823_6 + 4, local2823_5);
         byteBuffer.putFloat(local2823_6 + 8, glowCompositeDrawData.leftAligned());
         byteBuffer.putFloat(local2823_6 + 12, glowCompositeDrawData.bottomAnchored() > 0.5F ? 0.0F : 1.0F);
         int local2823_7 = local2823_6 + 16;
         int local2823_8 = Math.min(local2823_3, 64);
         float local2823_9 = glowCompositeDrawData.height() + local2823_4 * 2.0F;

         for (int index = 0; index < local2823_8; index++) {
            int local2823_11 = index * 4;
            float local2823_12 = floatArray[local2823_11] + local2823_4;
            float local2823_13 = floatArray[local2823_11 + 1] + local2823_4;
            float local2823_14 = floatArray[local2823_11 + 2] + local2823_4;
            float local2823_15 = floatArray[local2823_11 + 3] + local2823_4;
            float local2823_16 = local2823_9 - local2823_15;
            float local2823_17 = local2823_9 - local2823_14;
            int local2823_18 = local2823_7 + index * 4 * 4;
            byteBuffer.putFloat(local2823_18, local2823_12);
            byteBuffer.putFloat(local2823_18 + 4, local2823_13);
            byteBuffer.putFloat(local2823_18 + 8, local2823_16);
            byteBuffer.putFloat(local2823_18 + 12, local2823_17);
         }
      }
   }

   public void updateState7() {
      if (!this.list7.isEmpty() && this.flag) {
         GpuBuffer gpuBuffer = this.getGpuBuffer();
         if (gpuBuffer != null) {
            try {
               MemoryStack memoryStack = MemoryStack.stackPush();

               try {
                  ByteBuffer byteBuffer = this.getByteBuffer2(memoryStack);
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

   public static GlowCompositeRenderer getGlowCompositeRenderer() {
      GlowCompositeRenderer currentGlowCompositeRenderer = glowCompositeRenderer;
      if (currentGlowCompositeRenderer == null) {
         synchronized (GlowCompositeRenderer.class) {
            currentGlowCompositeRenderer = glowCompositeRenderer;
            if (currentGlowCompositeRenderer == null) {
               currentGlowCompositeRenderer = new GlowCompositeRenderer();
               glowCompositeRenderer = currentGlowCompositeRenderer;
            }
         }
      }

      return currentGlowCompositeRenderer;
   }

   public boolean matchesCriteria(RenderPipeline renderPipeline) {
      return renderPipeline == pipeline2;
   }

   private GpuBuffer getGpuBuffer4() {
      if (!this.flag && this.gpuBuffer4 != null) {
         return this.gpuBuffer4;
      }

      this.updateState7();
      if (!this.flag && this.gpuBuffer4 != null) {
         return this.gpuBuffer4;
      }

      this.updateState2();
      MemoryStack memoryStack = MemoryStack.stackPush();

      GpuBuffer gpuBuffer;
      try {
         ByteBuffer byteBuffer = this.getByteBuffer2(memoryStack);
         this.gpuBuffer4 = RenderSystem.getDevice().createBuffer(() -> "horus_glow_params", 128, byteBuffer);
         this.flag = false;
         gpuBuffer = this.gpuBuffer4;
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

      return gpuBuffer;
   }

   private static void processData10(
      ByteBuffer byteBuffer, int local2828_1, GlowCompositeDrawData glowCompositeDrawData, float local2828_3, float local2828_4, float local2828_5, float local2828_6
   ) {
      int local2828_7 = local2828_1 * 5 * 4 * 4;
      byteBuffer.putFloat(local2828_7, glowCompositeDrawData.radiusTopLeft());
      byteBuffer.putFloat(local2828_7 + 4, glowCompositeDrawData.radiusTopRight());
      byteBuffer.putFloat(local2828_7 + 8, glowCompositeDrawData.radiusBottomRight());
      byteBuffer.putFloat(local2828_7 + 12, glowCompositeDrawData.radiusBottomLeft());
      byteBuffer.putFloat(local2828_7 + 16, glowCompositeDrawData.width());
      byteBuffer.putFloat(local2828_7 + 20, glowCompositeDrawData.height());
      byteBuffer.putFloat(local2828_7 + 24, glowCompositeDrawData.effectivePad());
      byteBuffer.putFloat(local2828_7 + 28, 2.0F);
      int local2828_8 = glowCompositeDrawData.color();
      byteBuffer.putFloat(local2828_7 + 32, (local2828_8 >>> 16 & 0xFF) / 255.0F);
      byteBuffer.putFloat(local2828_7 + 36, (local2828_8 >>> 8 & 0xFF) / 255.0F);
      byteBuffer.putFloat(local2828_7 + 40, (local2828_8 & 0xFF) / 255.0F);
      byteBuffer.putFloat(local2828_7 + 44, glowCompositeDrawData.intensity() * glowCompositeDrawData.alpha());
      byteBuffer.putFloat(local2828_7 + 48, local2828_3);
      byteBuffer.putFloat(local2828_7 + 52, local2828_4);
      byteBuffer.putFloat(local2828_7 + 56, local2828_5);
      byteBuffer.putFloat(local2828_7 + 60, local2828_6);
      int local2828_9 = glowCompositeDrawData.secondColor();
      byteBuffer.putFloat(local2828_7 + 64, (local2828_9 >>> 16 & 0xFF) / 255.0F);
      byteBuffer.putFloat(local2828_7 + 68, (local2828_9 >>> 8 & 0xFF) / 255.0F);
      byteBuffer.putFloat(local2828_7 + 72, (local2828_9 & 0xFF) / 255.0F);
      byteBuffer.putFloat(local2828_7 + 76, glowCompositeDrawData.colorOffset());
   }

   public void processData11(RenderPass renderPass) {
      if (renderPass != null && !this.list7.isEmpty()) {
         GpuBuffer gpuBuffer = this.getGpuBuffer4();
         if (gpuBuffer != null) {
            renderPass.setUniform("GlowParamsArray", gpuBuffer);
         }
      }
   }

   private void processData12(SimpleFramebuffer simpleFramebuffer, SimpleFramebuffer simpleFramebuffer2, SimpleFramebuffer simpleFramebuffer3, GpuSampler gpuSampler) {
      if (simpleFramebuffer != null && simpleFramebuffer2 != null && simpleFramebuffer3 != null) {
         GpuBuffer gpuBuffer = this.getGpuBuffer6();
         GpuBuffer gpuBuffer2 = this.getGpuBuffer2();
         if (gpuBuffer != null && gpuBuffer2 != null) {
            CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
            ShapeIndexBuffer sequentialBuffer = RenderSystem.getSequentialBuffer(DrawMode.QUADS);
            GpuBuffer indexBuffer = sequentialBuffer.getIndexBuffer(6);
            MemoryStack memoryStack = MemoryStack.stackPush();

            try {
               ByteBuffer byteBuffer = memoryStack.calloc(48);
               byteBuffer.putFloat(0, 0.0F);
               byteBuffer.putFloat(4, 0.0F);
               byteBuffer.putFloat(8, 1.0F);
               byteBuffer.putFloat(12, 1.0F);
               byteBuffer.position(0);
               commandEncoder.writeToBuffer(gpuBuffer2.slice(0L, 48L), byteBuffer);
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

            try (RenderPass local2830_10x = commandEncoder.createRenderPass(
                  () -> "horus_glow_cutout", simpleFramebuffer3.getColorAttachmentView(), OptionalInt.of(0), null, OptionalDouble.empty()
               )) {
               local2830_10x.setPipeline(pipeline7);
               local2830_10x.bindTexture("Sampler0", simpleFramebuffer.getColorAttachmentView(), gpuSampler);
               local2830_10x.bindTexture("Sampler1", simpleFramebuffer2.getColorAttachmentView(), gpuSampler);
               local2830_10x.setUniform("KawaseParams", gpuBuffer2);
               local2830_10x.setVertexBuffer(0, gpuBuffer);
               local2830_10x.setIndexBuffer(indexBuffer, sequentialBuffer.getIndexType());
               local2830_10x.drawIndexed(0, 0, 6, 1);
            }
         }
      }
   }

   public void processData13(GlowCompositeDrawData glowCompositeDrawData) {
      this.renderElement3(this.drawContext2, glowCompositeDrawData);
   }

   private SimpleFramebuffer getSimpleFramebuffer3(SimpleFramebuffer simpleFramebuffer, String local2832_2, int local2832_3, int local2832_4) {
      if (simpleFramebuffer == null) {
         return new SimpleFramebuffer(local2832_2, local2832_3, local2832_4, false);
      }

      if (simpleFramebuffer.textureWidth != local2832_3 || simpleFramebuffer.textureHeight != local2832_4) {
         simpleFramebuffer.resize(local2832_3, local2832_4);
      }

      return simpleFramebuffer;
   }

   private GpuBuffer getGpuBuffer5(int local2834_1) {
      if (this.gpuBuffer8 == null || this.gpuBuffer8.isClosed() || this.gpuBuffer8.size() < local2834_1) {
         if (this.gpuBuffer8 != null) {
            this.gpuBuffer8.close();
         }

         int local2834_2 = Integer.highestOneBit(Math.max(local2834_1, 2) - 1) << 1;
         this.gpuBuffer8 = RenderSystem.getDevice().createBuffer(() -> "horus_glow_src_params", 136, local2834_2);
      }

      return this.gpuBuffer8;
   }

   private ByteBuffer getByteBuffer3(int local2835_1) {
      if (this.byteBuffer5 == null || this.byteBuffer5.capacity() < local2835_1) {
         if (this.byteBuffer5 != null) {
            MemoryUtil.memFree(this.byteBuffer5);
         }

         this.byteBuffer5 = MemoryUtil.memAlloc(local2835_1);
      }

      this.byteBuffer5.clear();
      return this.byteBuffer5;
   }

   private SimpleFramebuffer getSimpleFramebuffer4(int local2836_1, int local2836_2, int local2836_3) {
      while (this.list5.size() <= local2836_1) {
         this.list5.add(null);
      }

      SimpleFramebuffer simpleFramebuffer = this.getSimpleFramebuffer3(this.list5.get(local2836_1), "horus_glow_result_" + local2836_1, local2836_2, local2836_3);
      this.list5.set(local2836_1, simpleFramebuffer);
      return simpleFramebuffer;
   }

   private GpuBuffer getGpuBuffer6() {
      if (this.gpuBuffer6 != null) {
         return this.gpuBuffer6;
      }

      try (
         BufferAllocator bufferAllocator = new BufferAllocator(4 * VertexFormats.POSITION.getVertexSize());
         BuiltBuffer builtBuffer = this.getBuiltBuffer(bufferAllocator);
      ) {
         this.gpuBuffer6 = RenderSystem.getDevice().createBuffer(() -> "horus_glow_fullscreen_quad", 32, builtBuffer.getBuffer());
      }

      return this.gpuBuffer6;
   }

   private record DataRecord(GlowCompositeDrawData glow, GlowDrawBatch capture, Matrix3x2f pose) {
   }
}

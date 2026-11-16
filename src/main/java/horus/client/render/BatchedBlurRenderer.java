package horus.client.render;

import horus.mixin.DrawContextMixin;
import horus.client.util.GuiScale;
import horus.client.render.BlurDrawBatch;
import horus.client.render.ScissorStack;
import horus.client.render.GuiEffectsState;
import horus.client.render.BlurDrawData;

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
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2f;
import org.lwjgl.system.MemoryStack;

public final class BatchedBlurRenderer implements AutoCloseable {
   private static volatile BatchedBlurRenderer batchedBlurRenderer;
   private static float floatVal = 0.55F;
   private static float floatVal2 = 0.65F;
   private static float floatVal3 = 0.78F;
   private static float floatVal4;
   public static final RenderPipeline pipeline2 = RenderPipeline.builder()
      .withLocation(getIdentifier("pipeline/batched_blur"))
      .withVertexShader(getIdentifier("ui/batched_blur/batched_blur"))
      .withFragmentShader(getIdentifier("ui/batched_blur/batched_blur"))
      .withVertexFormat(VertexFormats.POSITION_COLOR_LINE_WIDTH, DrawMode.QUADS)
      .withBlend(BlendFunction.TRANSLUCENT)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withCull(false)
      .withSampler("Sampler0")
      .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
      .withUniform("Projection", UniformType.UNIFORM_BUFFER)
      .withUniform("BlurParamsArray", UniformType.UNIFORM_BUFFER)
      .build();
   public static final RenderPipeline pipeline3 = RenderPipeline.builder()
      .withLocation(getIdentifier("pipeline/kawase_down"))
      .withVertexShader(getIdentifier("ui/kawase/down"))
      .withFragmentShader(getIdentifier("ui/kawase/down"))
      .withVertexFormat(VertexFormats.POSITION, DrawMode.QUADS)
      .withBlend(BlendFunction.TRANSLUCENT)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withCull(false)
      .withSampler("Sampler0")
      .withUniform("KawaseParams", UniformType.UNIFORM_BUFFER)
      .build();
   public static final RenderPipeline pipeline4 = RenderPipeline.builder()
      .withLocation(getIdentifier("pipeline/kawase_up"))
      .withVertexShader(getIdentifier("ui/kawase/down"))
      .withFragmentShader(getIdentifier("ui/kawase/up"))
      .withVertexFormat(VertexFormats.POSITION, DrawMode.QUADS)
      .withBlend(BlendFunction.TRANSLUCENT)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withCull(false)
      .withSampler("Sampler0")
      .withUniform("KawaseParams", UniformType.UNIFORM_BUFFER)
      .build();
   public static final RenderPipeline pipeline5 = RenderPipeline.builder()
      .withLocation(getIdentifier("pipeline/gaussian_blur"))
      .withVertexShader(getIdentifier("ui/kawase/down"))
      .withFragmentShader(getIdentifier("ui/kawase/gaussian"))
      .withVertexFormat(VertexFormats.POSITION, DrawMode.QUADS)
      .withBlend(BlendFunction.TRANSLUCENT)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withCull(false)
      .withSampler("Sampler0")
      .withUniform("KawaseParams", UniformType.UNIFORM_BUFFER)
      .build();
   private final List<BatchedBlurRenderer.SubContext3> list = new ArrayList<>(32);
   private final List<BatchedBlurRenderer.DataRecord> list2 = new ArrayList<>(32);
   private DrawContext drawContext2;
   private final BatchedBlurRenderer.SubContext2 nested0527 = new BatchedBlurRenderer.SubContext2("horus_blur_result");
   private final BatchedBlurRenderer.SubContext2 nested05272 = new BatchedBlurRenderer.SubContext2("horus_blur_scratch");
   private final SimpleFramebuffer[] framebufferArray = new SimpleFramebuffer[4];
   private final SimpleFramebuffer[] framebufferArray2 = new SimpleFramebuffer[4];
   private SimpleFramebuffer framebuffer2;
   private long timeMillis;
   private long timeMillis2;
   private boolean flag;
   private boolean flag2;
   private boolean flag3 = true;
   private long timeMillis3;
   private long timeMillis4;
   private long timeMillis5;
   private long timeMillis6;
   private SimpleFramebuffer framebuffer3;
   private boolean flag4;
   private float floatVal5;
   private SimpleFramebuffer framebuffer4;
   private boolean flag5;
   private float floatVal6;
   private int intVal;
   private SimpleFramebuffer framebuffer5;
   private SimpleFramebuffer framebuffer6;
   private SimpleFramebuffer framebuffer7;
   private GpuBuffer gpuBuffer3;
   private GpuBuffer gpuBuffer4;
   private GpuBuffer gpuBuffer5;
   private boolean flag6;
   private boolean flag7;
   private boolean flag8;
   private boolean flag9 = true;
   private int intVal2;

   private void renderElement(DrawContext drawContext, BlurDrawData newBlurDrawData) {
      if (newBlurDrawData != null && newBlurDrawData.visible()) {
         if (drawContext != null) {
            try {
               BlurDrawData currentBlurDrawData = this.getBlurDrawData2(newBlurDrawData);
               Matrix3x2f matrix3x2f = GuiScale.renderElement(drawContext);
               ScreenRect screenRect = ScissorStack.getScreenRect();
               BlurDrawBatch currentBlurDrawBatch = new BlurDrawBatch();
               this.list.add(new BatchedBlurRenderer.SubContext3(currentBlurDrawData, matrix3x2f, screenRect, currentBlurDrawBatch, false));
               ((DrawContextMixin)drawContext).getGuiRenderState().addSimpleElement(new BlurRenderState(matrix3x2f, currentBlurDrawData, screenRect, currentBlurDrawBatch));
            } catch (RuntimeException exception) {
            }
         }
      }
   }

   public void processData(BlurDrawData newBlurDrawData) {
      DrawContext drawContext = this.drawContext2;
      if (newBlurDrawData != null && newBlurDrawData.visible() && drawContext != null) {
         try {
            BlurDrawData currentBlurDrawData = this.getBlurDrawData2(newBlurDrawData);
            Matrix3x2f matrix3x2f = GuiScale.renderElement(drawContext);
            ScreenRect screenRect = ScissorStack.getScreenRect();
            BlurDrawBatch currentBlurDrawBatch = new BlurDrawBatch();
            this.list.add(new BatchedBlurRenderer.SubContext3(currentBlurDrawData, matrix3x2f, screenRect, currentBlurDrawBatch, false, true));
            ((DrawContextMixin)drawContext).getGuiRenderState().addSimpleElement(new BlurRenderState(matrix3x2f, currentBlurDrawData, screenRect, currentBlurDrawBatch));
         } catch (RuntimeException exception) {
         }
      }
   }

   private static int computeInt(int local7555_0, int local7555_1, int local7555_2) {
      return Math.max(local7555_1, Math.min(local7555_2, local7555_0));
   }

   private void updateState() {
      if (this.gpuBuffer4 != null) {
         this.gpuBuffer4.close();
         this.gpuBuffer4 = null;
      }
   }

   private static int computeInt2(int local7557_0, int local7557_1) {
      return -Math.floorDiv(-local7557_0, local7557_1) * local7557_1;
   }

   private void processData2(
      int local7558_1, Framebuffer framebuffer, SimpleFramebuffer simpleFramebuffer, BatchedBlurRenderer.DataRecord5 local7558_4, float local7558_5
   ) {
      if (simpleFramebuffer != null && local7558_4 != null) {
         int local7558_6 = this.intVal2 > 0 ? this.intVal2 : this.computeInt5(local7558_5);
         this.flag8 = local7558_1 == 65 || local7558_1 == 66;
         this.processData4(simpleFramebuffer.textureWidth, simpleFramebuffer.textureHeight, local7558_6);
         SimpleFramebuffer[] simpleFramebufferArray = this.framebufferArray;
         SimpleFramebuffer[] simpleFramebufferArray2 = this.framebufferArray2;
         float local7558_9 = 1.0F;
         GpuSampler gpuSampler = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
         GpuTextureView gpuTextureView = framebuffer.getColorAttachmentView();
         int local7558_12 = framebuffer.textureWidth;
         int local7558_13 = framebuffer.textureHeight;
         float local7558_14 = (float)local7558_4.x() / Math.max(framebuffer.textureWidth, 1);
         float local7558_15 = (float)local7558_4.y() / Math.max(framebuffer.textureHeight, 1);
         float local7558_16 = (float)local7558_4.width() / Math.max(framebuffer.textureWidth, 1);
         float local7558_17 = (float)local7558_4.height() / Math.max(framebuffer.textureHeight, 1);

         for (int index = 0; index < local7558_6; index++) {
            SimpleFramebuffer simpleFramebuffer2 = simpleFramebufferArray[index];
            this.processData3(
               pipeline3,
               gpuTextureView,
               local7558_12,
               local7558_13,
               simpleFramebuffer2,
               local7558_9,
               local7558_14,
               local7558_15,
               local7558_16,
               local7558_17,
               gpuSampler
            );
            gpuTextureView = simpleFramebuffer2.getColorAttachmentView();
            local7558_12 = simpleFramebuffer2.textureWidth;
            local7558_13 = simpleFramebuffer2.textureHeight;
            local7558_14 = 0.0F;
            local7558_15 = 0.0F;
            local7558_16 = 1.0F;
            local7558_17 = 1.0F;
         }

         SimpleFramebuffer simpleFramebuffer3 = simpleFramebufferArray[local7558_6 - 1];
         SimpleFramebuffer simpleFramebuffer4 = this.framebuffer2;
         if (simpleFramebuffer3 != null && simpleFramebuffer4 != null && simpleFramebuffer4.getColorAttachmentView() != null) {
            float local7558_20 = local7558_5 * 0.5F * (Math.max(framebuffer.textureHeight, 1) / 1080.0F);
            float local7558_21 = local7558_20 * ((float)simpleFramebuffer3.textureHeight / Math.max(local7558_4.height(), 1));
            local7558_21 = computeFloat(local7558_21, 0.6F, 12.0F);
            int local7558_22 = Math.min(31, Math.max(2, (int)Math.ceil(local7558_21 * 3.0F)));
            boolean local7558_23 = local7558_1 == 66;
            this.processData15(
               simpleFramebuffer3.getColorAttachmentView(),
               simpleFramebuffer4.getColorAttachmentView(),
               1.0F / Math.max(simpleFramebuffer3.textureWidth, 1),
               0.0F,
               local7558_21,
               local7558_22,
               1.0F,
               1.0F,
               0.0F,
               gpuSampler
            );
            this.processData15(
               simpleFramebuffer4.getColorAttachmentView(),
               simpleFramebuffer3.getColorAttachmentView(),
               0.0F,
               1.0F / Math.max(simpleFramebuffer3.textureHeight, 1),
               local7558_21,
               local7558_22,
               local7558_23 ? 1.25F : 1.0F,
               local7558_23 ? 1.0F : 1.0F,
               local7558_23 ? 0.78F : 0.0F,
               gpuSampler
            );
            gpuTextureView = simpleFramebuffer3.getColorAttachmentView();
            local7558_12 = simpleFramebuffer3.textureWidth;
            local7558_13 = simpleFramebuffer3.textureHeight;
         }

         for (int index2 = local7558_6 - 2; index2 >= 0; index2--) {
            SimpleFramebuffer simpleFramebuffer5 = simpleFramebufferArray2[index2];
            this.processData3(pipeline4, gpuTextureView, local7558_12, local7558_13, simpleFramebuffer5, local7558_9, 0.0F, 0.0F, 1.0F, 1.0F, gpuSampler);
            gpuTextureView = simpleFramebuffer5.getColorAttachmentView();
            local7558_12 = simpleFramebuffer5.textureWidth;
            local7558_13 = simpleFramebuffer5.textureHeight;
         }

         this.processData3(pipeline4, gpuTextureView, local7558_12, local7558_13, simpleFramebuffer, local7558_9, 0.0F, 0.0F, 1.0F, 1.0F, gpuSampler);
      }
   }

   private SimpleFramebuffer getSimpleFramebuffer(int local7559_1, int local7559_2) {
      int local7559_3 = Math.max(Math.round(local7559_1 * 0.25F), 1);
      int local7559_4 = Math.max(Math.round(local7559_2 * 0.25F), 1);
      this.framebuffer6 = this.getSimpleFramebuffer2(this.framebuffer6, "horus_blur_stratum_heavy", local7559_3, local7559_4);
      return this.framebuffer6;
   }

   public void updateState2() {
      if (!this.list.isEmpty()) {
         MinecraftClient minecraftClient = MinecraftClient.getInstance();
         Framebuffer framebuffer = minecraftClient == null ? null : minecraftClient.getFramebuffer();
         if (framebuffer != null && framebuffer.getColorAttachment() != null) {
            this.intVal2 = this.computeInt5(0.0F);
            long local7560_3 = System.nanoTime();
            long local7560_5 = ++this.timeMillis;
            GpuSampler gpuSampler = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
            this.flag4 = false;
            this.flag5 = false;
            float local7560_8 = -1.0F;
            float local7560_9 = -1.0F;

            for (BatchedBlurRenderer.SubContext3 local7560_11 : this.list) {
               if (local7560_11.checkState()) {
                  if (local7560_11.computeInt6() == 0) {
                     local7560_8 = Math.max(local7560_8, local7560_11.getBlurDrawData().blurRadius());
                  } else {
                     local7560_9 = Math.max(local7560_9, local7560_11.getBlurDrawData().blurRadius());
                  }
               }
            }

            if (local7560_8 >= 0.0F) {
               float local7560_10 = this.computeFloat2(local7560_8, framebuffer.textureWidth, framebuffer.textureHeight);
               int local7560_11 = Math.max(Math.round(framebuffer.textureWidth * local7560_10), 1);
               int local7560_12 = Math.max(Math.round(framebuffer.textureHeight * local7560_10), 1);
               this.framebuffer3 = this.getSimpleFramebuffer2(this.framebuffer3, "horus_blur_global", local7560_11, local7560_12);
               this.timeMillis3 = local7560_3;
               if (this.framebuffer3 != null && this.framebuffer3.getColorAttachmentView() != null) {
                  this.flag4 = true;
                  this.floatVal5 = local7560_8;
                  TextureSetup textureSetup = TextureSetup.of(this.framebuffer3.getColorAttachmentView(), gpuSampler);

                  for (BatchedBlurRenderer.SubContext3 local7560_15 : this.list) {
                     if (local7560_15.checkState() && local7560_15.computeInt6() == 0) {
                        BlurDrawBatch currentBlurDrawBatch = local7560_15.getBlurDrawBatch();
                        currentBlurDrawBatch.floatVal = 0.0F;
                        currentBlurDrawBatch.floatVal2 = 0.0F;
                        currentBlurDrawBatch.floatVal3 = framebuffer.textureWidth;
                        currentBlurDrawBatch.floatVal4 = framebuffer.textureHeight;
                        currentBlurDrawBatch.textureSetup = textureSetup;
                     }
                  }

                  this.flag6 = true;
               }
            }

            if (local7560_9 >= 0.0F) {
               float local7560_10 = this.computeFloat2(local7560_9, framebuffer.textureWidth, framebuffer.textureHeight);
               int local7560_11 = Math.max(Math.round(framebuffer.textureWidth * local7560_10), 1);
               int local7560_12 = Math.max(Math.round(framebuffer.textureHeight * local7560_10), 1);
               this.framebuffer4 = this.getSimpleFramebuffer2(this.framebuffer4, "horus_blur_global2", local7560_11, local7560_12);
               this.timeMillis4 = local7560_3;
               if (this.framebuffer4 != null && this.framebuffer4.getColorAttachmentView() != null) {
                  this.flag5 = true;
                  this.floatVal6 = local7560_9;
                  TextureSetup textureSetup2 = TextureSetup.of(this.framebuffer4.getColorAttachmentView(), gpuSampler);

                  for (BatchedBlurRenderer.SubContext3 local7560_15 : this.list) {
                     if (local7560_15.checkState() && local7560_15.computeInt6() == 1) {
                        BlurDrawBatch currentBlurDrawBatch2 = local7560_15.getBlurDrawBatch();
                        currentBlurDrawBatch2.floatVal = 0.0F;
                        currentBlurDrawBatch2.floatVal2 = 0.0F;
                        currentBlurDrawBatch2.floatVal3 = framebuffer.textureWidth;
                        currentBlurDrawBatch2.floatVal4 = framebuffer.textureHeight;
                        currentBlurDrawBatch2.textureSetup = textureSetup2;
                     }
                  }

                  this.flag6 = true;
               }
            }

            boolean local7560_10 = false;

            for (BatchedBlurRenderer.SubContext3 local7560_12 : this.list) {
               if (local7560_12.checkState2()) {
                  local7560_10 = true;
                  break;
               }
            }

            if (local7560_10) {
               SimpleFramebuffer simpleFramebuffer = this.getSimpleFramebuffer(framebuffer.textureWidth, framebuffer.textureHeight);
               this.timeMillis6 = local7560_3;
               if (simpleFramebuffer != null && simpleFramebuffer.getColorAttachmentView() != null) {
                  this.flag7 = true;
                  TextureSetup textureSetup3 = TextureSetup.of(simpleFramebuffer.getColorAttachmentView(), gpuSampler);

                  for (BatchedBlurRenderer.SubContext3 local7560_14 : this.list) {
                     if (local7560_14.checkState2()) {
                        BlurDrawBatch currentBlurDrawBatch3 = local7560_14.getBlurDrawBatch();
                        currentBlurDrawBatch3.floatVal = 0.0F;
                        currentBlurDrawBatch3.floatVal2 = 0.0F;
                        currentBlurDrawBatch3.floatVal3 = framebuffer.textureWidth;
                        currentBlurDrawBatch3.floatVal4 = framebuffer.textureHeight;
                        currentBlurDrawBatch3.textureSetup = textureSetup3;
                     }
                  }

                  this.flag6 = true;
               }
            }

            for (int index = 0; index < this.list.size() && index < 64; index++) {
               BatchedBlurRenderer.SubContext3 local7560_12 = this.list.get(index);
               if (!local7560_12.checkState() && !local7560_12.checkState2()) {
                  BlurDrawBatch currentBlurDrawBatch4 = local7560_12.getBlurDrawBatch();
                  BatchedBlurRenderer.DataRecord5 local7560_14 = this.getNested0530(local7560_12, minecraftClient, framebuffer);
                  if (local7560_14 != null) {
                     float local7560_15 = this.computeFloat2(local7560_12.getBlurDrawData().blurRadius(), local7560_14.width(), local7560_14.height());
                     int local7560_16 = Math.max(Math.round(local7560_14.width() * local7560_15), 1);
                     int local7560_17 = Math.max(Math.round(local7560_14.height() * local7560_15), 1);
                     SimpleFramebuffer simpleFramebuffer2 = this.nested0527.getSimpleFramebuffer3(local7560_16, local7560_17, local7560_5, local7560_3);
                     if (simpleFramebuffer2 != null && simpleFramebuffer2.getColorAttachmentView() != null) {
                        local7560_12.processData18(simpleFramebuffer2);
                        local7560_12.processData19(local7560_14);
                        currentBlurDrawBatch4.floatVal = local7560_14.x();
                        currentBlurDrawBatch4.floatVal2 = local7560_14.y();
                        currentBlurDrawBatch4.floatVal3 = local7560_14.width();
                        currentBlurDrawBatch4.floatVal4 = local7560_14.height();
                        currentBlurDrawBatch4.textureSetup = TextureSetup.of(simpleFramebuffer2.getColorAttachmentView(), gpuSampler);
                        this.flag6 = true;
                     }
                  }
               }
            }
         } else {
            this.list.clear();
         }
      }
   }

   private GpuBuffer getGpuBuffer() {
      if (this.gpuBuffer4 != null && !this.gpuBuffer4.isClosed() && this.gpuBuffer4.size() >= 24576L) {
         return this.gpuBuffer4;
      }

      this.updateState();

      try {
         this.gpuBuffer4 = RenderSystem.getDevice().createBuffer(() -> "horus_blur_params", 136, 24576L);
         return this.gpuBuffer4;
      } catch (RuntimeException exception) {
         return null;
      }
   }

   private void processData3(
      RenderPipeline renderPipeline,
      GpuTextureView gpuTextureView,
      int local7562_3,
      int local7562_4,
      SimpleFramebuffer simpleFramebuffer,
      float local7562_6,
      float local7562_7,
      float local7562_8,
      float local7562_9,
      float local7562_10,
      GpuSampler gpuSampler
   ) {
      if (simpleFramebuffer != null) {
         this.processData7(
            renderPipeline,
            gpuTextureView,
            local7562_3,
            local7562_4,
            simpleFramebuffer.getColorAttachmentView(),
            local7562_6,
            local7562_7,
            local7562_8,
            local7562_9,
            local7562_10,
            gpuSampler,
            true,
            1.0F
         );
      }
   }

   private BlurDrawData getBlurDrawData2(BlurDrawData newBlurDrawData) {
      float local7563_2 = Math.max(0.0F, Math.min(newBlurDrawData.width(), newBlurDrawData.height()) * 0.5F);
      float local7563_3 = computeFloat(newBlurDrawData.radiusTopLeft(), 0.0F, local7563_2);
      float local7563_4 = computeFloat(newBlurDrawData.radiusTopRight(), 0.0F, local7563_2);
      float local7563_5 = computeFloat(newBlurDrawData.radiusBottomRight(), 0.0F, local7563_2);
      float local7563_6 = computeFloat(newBlurDrawData.radiusBottomLeft(), 0.0F, local7563_2);
      float local7563_7 = Math.max(0.0F, newBlurDrawData.smoothness());
      float local7563_8 = Math.max(0.0F, newBlurDrawData.blurRadius());
      return local7563_3 == newBlurDrawData.radiusTopLeft()
            && local7563_4 == newBlurDrawData.radiusTopRight()
            && local7563_5 == newBlurDrawData.radiusBottomRight()
            && local7563_6 == newBlurDrawData.radiusBottomLeft()
            && local7563_7 == newBlurDrawData.smoothness()
            && local7563_8 == newBlurDrawData.blurRadius()
         ? newBlurDrawData
         : new BlurDrawData(
            newBlurDrawData.x(),
            newBlurDrawData.y(),
            newBlurDrawData.width(),
            newBlurDrawData.height(),
            local7563_3,
            local7563_4,
            local7563_5,
            local7563_6,
            local7563_7,
            local7563_8,
            newBlurDrawData.colorTopLeft(),
            newBlurDrawData.colorTopRight(),
            newBlurDrawData.colorBottomRight(),
            newBlurDrawData.colorBottomLeft()
         );
   }

   public void updateState3() {
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      Framebuffer framebuffer = minecraftClient == null ? null : minecraftClient.getFramebuffer();
      if (framebuffer != null
         && framebuffer.getColorAttachmentView() != null
         && this.flag7
         && this.framebuffer6 != null
         && this.framebuffer6.getColorAttachmentView() != null) {
         if (this.flag3) {
            this.flag3 = false;
            this.processData2(
               66, framebuffer, this.framebuffer6, new BatchedBlurRenderer.DataRecord5(0, 0, framebuffer.textureWidth, framebuffer.textureHeight), 72.0F
            );
         }
      }
   }

   public void updateState4() {
      this.processData8(Integer.MAX_VALUE, 0);
   }

   public void updateState5() {
      if (!this.list2.isEmpty() && this.flag9) {
         GpuBuffer gpuBuffer = this.getGpuBuffer();
         if (gpuBuffer != null) {
            try {
               MemoryStack memoryStack = MemoryStack.stackPush();

               try {
                  ByteBuffer byteBuffer = this.getByteBuffer(memoryStack, this.list2);
                  RenderSystem.getDevice().createCommandEncoder().writeToBuffer(gpuBuffer.slice(0L, byteBuffer.remaining()), byteBuffer);
                  this.flag9 = false;
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
               this.flag9 = true;
            }
         }
      }
   }

   public void renderElement2(DrawContext drawContext, BlurDrawData newBlurDrawData) {
      this.renderElement3(drawContext);
      this.processData14(newBlurDrawData);
      this.updateState10();
   }

   private static float computeFloat(float local7569_0, float local7569_1, float local7569_2) {
      return Math.max(local7569_1, Math.min(local7569_2, local7569_0));
   }

   public void renderElement3(DrawContext drawContext) {
      this.drawContext2 = drawContext;
   }

   private BatchedBlurRenderer.DataRecord5 getNested0530(BatchedBlurRenderer.SubContext3 local7571_1, MinecraftClient minecraftClient, Framebuffer framebuffer) {
      Window window = minecraftClient.getWindow();
      int local7571_5 = Math.max(window.getScaledWidth(), 1);
      int local7571_6 = Math.max(window.getScaledHeight(), 1);
      float local7571_7 = (float)framebuffer.textureWidth / local7571_5;
      float local7571_8 = (float)framebuffer.textureHeight / local7571_6;
      BlurDrawData currentBlurDrawData = local7571_1.getBlurDrawData();
      float local7571_10 = GuiScale.computeFloat4(Math.max(16.0F, currentBlurDrawData.blurRadius() * 2.0F));
      ScreenRect screenRect = new ScreenRect(
            Math.round(currentBlurDrawData.x()), Math.round(currentBlurDrawData.y()), Math.round(currentBlurDrawData.width()), Math.round(currentBlurDrawData.height())
         )
         .transformEachVertex(local7571_1.getMatrix3x2f());
      if (local7571_1.getScreenRect() != null) {
         screenRect = local7571_1.getScreenRect().intersection(screenRect);
         if (screenRect == null) {
            return null;
         }
      }

      float local7571_12 = computeFloat(screenRect.getLeft() - local7571_10, 0.0F, local7571_5);
      float local7571_13 = computeFloat(screenRect.getTop() - local7571_10, 0.0F, local7571_6);
      float local7571_14 = computeFloat(screenRect.getRight() + local7571_10, local7571_12 + 1.0F, local7571_5);
      float local7571_15 = computeFloat(screenRect.getBottom() + local7571_10, local7571_13 + 1.0F, local7571_6);
      int local7571_16 = 64;
      int local7571_17 = computeInt(computeInt4((int)Math.floor(local7571_12 * local7571_7), local7571_16), 0, framebuffer.textureWidth - 1);
      int local7571_18 = computeInt(computeInt2((int)Math.ceil(local7571_14 * local7571_7), local7571_16), local7571_17 + 1, framebuffer.textureWidth);
      int local7571_19 = computeInt(computeInt4((int)Math.floor((local7571_6 - local7571_15) * local7571_8), local7571_16), 0, framebuffer.textureHeight - 1);
      int local7571_20 = computeInt(
         computeInt2((int)Math.ceil((local7571_6 - local7571_13) * local7571_8), local7571_16), local7571_19 + 1, framebuffer.textureHeight
      );
      int local7571_21 = Math.max(local7571_18 - local7571_17, 1);
      int local7571_22 = Math.max(local7571_20 - local7571_19, 1);
      return new BatchedBlurRenderer.DataRecord5(local7571_17, local7571_19, local7571_21, local7571_22);
   }

   private void processData4(int local7573_1, int local7573_2, int local7573_3) {
      long local7573_4 = ++this.timeMillis2;
      long local7573_6 = System.nanoTime();
      int local7573_8 = Math.max(local7573_1, 1);
      int local7573_9 = Math.max(local7573_2, 1);
      int local7573_10 = local7573_8;
      int local7573_11 = local7573_9;
      Arrays.fill(this.framebufferArray, null);
      Arrays.fill(this.framebufferArray2, null);

      for (int index = 0; index < local7573_3; index++) {
         this.framebufferArray[index] = this.nested05272.getSimpleFramebuffer3(local7573_8, local7573_9, local7573_4, local7573_6);
         if (index < local7573_3 - 1) {
            this.framebufferArray2[index] = this.nested05272.getSimpleFramebuffer3(local7573_8, local7573_9, local7573_4, local7573_6);
         }

         local7573_10 = local7573_8;
         local7573_11 = local7573_9;
         local7573_8 = Math.max(local7573_8 / 2, 1);
         local7573_9 = Math.max(local7573_9 / 2, 1);
      }

      this.framebuffer2 = this.nested05272.getSimpleFramebuffer3(local7573_10, local7573_11, local7573_4, local7573_6);
   }

   public void updateState6() {
      this.list2.clear();
      this.flag = false;
      this.flag2 = false;
      this.flag3 = true;
      this.processData13(System.nanoTime());
      this.flag6 = false;
      this.flag7 = false;
      this.flag5 = false;
      this.intVal = 0;
      this.flag9 = false;
      this.intVal2 = 0;
   }

   private GpuBuffer getGpuBuffer2() {
      if (this.gpuBuffer5 == null || this.gpuBuffer5.isClosed() || this.gpuBuffer5.size() < 64L) {
         this.updateState7();
         this.gpuBuffer5 = RenderSystem.getDevice().createBuffer(() -> "horus_kawase_params", 136, 64L);
      }

      return this.gpuBuffer5;
   }

   @Override
   public void close() {
      this.list.clear();
      this.list2.clear();
      this.drawContext2 = null;
      this.flag6 = false;
      this.updateState();
      this.updateState7();
      this.nested0527.updateState11();
      this.nested05272.updateState11();
      Arrays.fill(this.framebufferArray, null);
      Arrays.fill(this.framebufferArray2, null);
      this.framebuffer2 = null;
      if (this.framebuffer3 != null) {
         this.framebuffer3.delete();
         this.framebuffer3 = null;
      }

      if (this.framebuffer4 != null) {
         this.framebuffer4.delete();
         this.framebuffer4 = null;
      }

      if (this.framebuffer5 != null) {
         this.framebuffer5.delete();
         this.framebuffer5 = null;
      }

      if (this.framebuffer6 != null) {
         this.framebuffer6.delete();
         this.framebuffer6 = null;
      }

      if (this.framebuffer7 != null) {
         this.framebuffer7.delete();
         this.framebuffer7 = null;
      }

      this.flag4 = false;
      if (this.gpuBuffer3 != null) {
         this.gpuBuffer3.close();
         this.gpuBuffer3 = null;
      }
   }

   public void processData5(RenderPass renderPass) {
      if (renderPass != null && !this.list2.isEmpty()) {
         GpuBuffer gpuBuffer = this.getGpuBuffer4();
         if (gpuBuffer != null) {
            renderPass.setUniform("BlurParamsArray", gpuBuffer);
         }
      }
   }

   private GpuBuffer getGpuBuffer3() {
      if (this.gpuBuffer3 != null) {
         return this.gpuBuffer3;
      }

      try (
         BufferAllocator bufferAllocator = new BufferAllocator(4 * VertexFormats.POSITION.getVertexSize());
         BuiltBuffer builtBuffer = this.getBuiltBuffer(bufferAllocator);
      ) {
         this.gpuBuffer3 = RenderSystem.getDevice().createBuffer(() -> "horus_blur_fullscreen_quad", 32, builtBuffer.getBuffer());
         return this.gpuBuffer3;
      }
   }

   private void updateState7() {
      if (this.gpuBuffer5 != null) {
         this.gpuBuffer5.close();
         this.gpuBuffer5 = null;
      }
   }

   public static void processData6(float local7580_0) {
      floatVal4 = Math.max(0.0F, Math.min(1.0F, local7580_0));
   }

   private void processData7(
      RenderPipeline renderPipeline,
      GpuTextureView gpuTextureView,
      int local7581_3,
      int local7581_4,
      GpuTextureView gpuTextureView2,
      float local7581_6,
      float local7581_7,
      float local7581_8,
      float local7581_9,
      float local7581_10,
      GpuSampler gpuSampler,
      boolean local7581_12,
      float local7581_13
   ) {
      if (gpuTextureView != null && gpuTextureView2 != null) {
         GpuBuffer gpuBuffer = this.getGpuBuffer3();
         if (gpuBuffer != null) {
            CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
            ShapeIndexBuffer sequentialBuffer = RenderSystem.getSequentialBuffer(DrawMode.QUADS);
            GpuBuffer indexBuffer = sequentialBuffer.getIndexBuffer(6);
            MemoryStack memoryStack = MemoryStack.stackPush();

            label91: {
               try {
                  ByteBuffer byteBuffer = memoryStack.calloc(64);
                  byteBuffer.putFloat(0, local7581_7);
                  byteBuffer.putFloat(4, local7581_8);
                  byteBuffer.putFloat(8, local7581_9);
                  byteBuffer.putFloat(12, local7581_10);
                  byteBuffer.putFloat(16, local7581_6 / Math.max(local7581_3, 1));
                  byteBuffer.putFloat(20, local7581_6 / Math.max(local7581_4, 1));
                  float local7581_20 = floatVal4 * 0.97F;
                  byteBuffer.putFloat(32, this.flag8 ? local7581_20 : floatVal);
                  byteBuffer.putFloat(36, this.flag8 ? local7581_20 : floatVal2);
                  byteBuffer.putFloat(40, this.flag8 ? local7581_20 : floatVal3);
                  byteBuffer.putFloat(44, local7581_13);
                  byteBuffer.position(0);
                  GpuBuffer gpuBuffer2 = this.getGpuBuffer2();
                  if (gpuBuffer2 == null) {
                     break label91;
                  }

                  commandEncoder.writeToBuffer(gpuBuffer2.slice(0L, 64L), byteBuffer);

                  try (RenderPass renderPass = commandEncoder.createRenderPass(
                        () -> "horus_kawase_blur", gpuTextureView2, local7581_12 ? OptionalInt.of(0) : OptionalInt.empty(), null, OptionalDouble.empty()
                     )) {
                     renderPass.setPipeline(renderPipeline);
                     renderPass.bindTexture("Sampler0", gpuTextureView, gpuSampler);
                     renderPass.setUniform("KawaseParams", gpuBuffer2);
                     renderPass.setVertexBuffer(0, gpuBuffer);
                     renderPass.setIndexBuffer(indexBuffer, sequentialBuffer.getIndexType());
                     renderPass.drawIndexed(0, 0, 6, 1);
                  }
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

               return;
            }

            if (memoryStack != null) {
               memoryStack.close();
            }
         }
      }
   }

   public void processData8(int local7583_1, int local7583_2) {
      boolean local7583_3 = local7583_2 > local7583_1 && (GuiEffectsState.checkState2() || GuiEffectsState.checkState());
      this.flag3 = !local7583_3 || this.flag2;
      if (!this.flag6) {
         this.list.clear();
      } else {
         MinecraftClient minecraftClient = MinecraftClient.getInstance();
         Framebuffer framebuffer = minecraftClient == null ? null : minecraftClient.getFramebuffer();
         if (framebuffer != null && framebuffer.getColorAttachmentView() != null) {
            if (this.flag4 && this.framebuffer3 != null) {
               this.processData2(
                  64,
                  framebuffer,
                  this.framebuffer3,
                  new BatchedBlurRenderer.DataRecord5(0, 0, framebuffer.textureWidth, framebuffer.textureHeight),
                  this.floatVal5
               );
            }

            if (this.flag7 && (this.flag || !local7583_3) && this.framebuffer6 != null && this.framebuffer6.getColorAttachmentView() != null) {
               this.processData2(
                  66, framebuffer, this.framebuffer6, new BatchedBlurRenderer.DataRecord5(0, 0, framebuffer.textureWidth, framebuffer.textureHeight), 72.0F
               );
            }

            for (int index = 0; index < this.list.size() && index < 64; index++) {
               BatchedBlurRenderer.SubContext3 local7583_7 = this.list.get(index);
               if (!local7583_7.checkState() && !local7583_7.checkState2()) {
                  BatchedBlurRenderer.DataRecord5 local7583_8 = local7583_7.getNested05302();
                  SimpleFramebuffer simpleFramebuffer = local7583_7.getSimpleFramebuffer4();
                  if (local7583_8 != null && simpleFramebuffer != null) {
                     this.processData2(index, framebuffer, simpleFramebuffer, local7583_8, local7583_7.getBlurDrawData().blurRadius());
                  }
               }
            }

            this.list.clear();
         } else {
            this.list.clear();
         }
      }
   }

   public static BatchedBlurRenderer getBatchedBlurRenderer() {
      BatchedBlurRenderer currentBatchedBlurRenderer = batchedBlurRenderer;
      if (currentBatchedBlurRenderer == null) {
         synchronized (BatchedBlurRenderer.class) {
            currentBatchedBlurRenderer = batchedBlurRenderer;
            if (currentBatchedBlurRenderer == null) {
               currentBatchedBlurRenderer = new BatchedBlurRenderer();
               batchedBlurRenderer = currentBatchedBlurRenderer;
            }
         }
      }

      return currentBatchedBlurRenderer;
   }

   public static void processData9(float local7585_0, float local7585_1, float local7585_2) {
      if (Float.isFinite(local7585_0) && Float.isFinite(local7585_1) && Float.isFinite(local7585_2)) {
         floatVal = computeFloat(local7585_0, 0.0F, 1.0F);
         floatVal2 = computeFloat(local7585_1, 0.0F, 1.0F);
         floatVal3 = computeFloat(local7585_2, 0.0F, 1.0F);
      }
   }

   private BatchedBlurRenderer() {
   }

   public void processData10(BlurDrawData newBlurDrawData) {
      DrawContext drawContext = this.drawContext2;
      if (newBlurDrawData != null && newBlurDrawData.visible() && drawContext != null) {
         try {
            BlurDrawData currentBlurDrawData = this.getBlurDrawData2(newBlurDrawData);
            Matrix3x2f matrix3x2f = GuiScale.renderElement(drawContext);
            ScreenRect screenRect = ScissorStack.getScreenRect();
            BlurDrawBatch currentBlurDrawBatch = new BlurDrawBatch();
            BatchedBlurRenderer.SubContext3 local7587_7 = new BatchedBlurRenderer.SubContext3(currentBlurDrawData, matrix3x2f, screenRect, currentBlurDrawBatch, true);
            local7587_7.processData20(this.intVal);
            this.list.add(local7587_7);
            ((DrawContextMixin)drawContext).getGuiRenderState().addSimpleElement(new BlurRenderState(matrix3x2f, currentBlurDrawData, screenRect, currentBlurDrawBatch));
         } catch (RuntimeException exception) {
         }
      }
   }

   int computeInt3(BlurDrawData newBlurDrawData, BlurDrawBatch newBlurDrawBatch) {
      int local7588_3 = this.list2.size();
      if (local7588_3 == 512) {
         return -1;
      }

      this.list2.add(new BatchedBlurRenderer.DataRecord(newBlurDrawData, newBlurDrawBatch));
      this.flag9 = true;
      return local7588_3;
   }

   public static void processData11(int local7589_0) {
      getBatchedBlurRenderer().intVal = Math.max(0, Math.min(1, local7589_0));
   }

   private float computeFloat2(float local7590_1, int local7590_2, int local7590_3) {
      return local7590_1 >= 40.0F ? 0.25F : 0.5F;
   }

   public boolean matchesCriteria(RenderPipeline renderPipeline) {
      return renderPipeline == pipeline2;
   }

   private static int computeInt4(int local7592_0, int local7592_1) {
      return Math.floorDiv(local7592_0, local7592_1) * local7592_1;
   }

   private static Identifier getIdentifier(String local7594_0) {
      return Identifier.of("horus", local7594_0);
   }

   private BuiltBuffer getBuiltBuffer(BufferAllocator bufferAllocator) {
      BufferBuilder bufferBuilder = new BufferBuilder(bufferAllocator, DrawMode.QUADS, VertexFormats.POSITION);
      bufferBuilder.vertex(-1.0F, -1.0F, 0.0F);
      bufferBuilder.vertex(-1.0F, 1.0F, 0.0F);
      bufferBuilder.vertex(1.0F, 1.0F, 0.0F);
      bufferBuilder.vertex(1.0F, -1.0F, 0.0F);
      return bufferBuilder.end();
   }

   public void processData12(RenderPipeline renderPipeline, TextureSetup textureSetup, int local7596_3, int local7596_4) {
      if (this.flag7 && renderPipeline == pipeline2 && textureSetup != null && this.framebuffer6 != null) {
         GpuTextureView colorAttachmentView = this.framebuffer6.getColorAttachmentView();
         if (colorAttachmentView != null && textureSetup.texure0() == colorAttachmentView) {
            if (local7596_3 < local7596_4) {
               this.flag = true;
            } else {
               this.flag2 = true;
            }
         }
      }
   }

   private SimpleFramebuffer getSimpleFramebuffer2(SimpleFramebuffer simpleFramebuffer, String local7597_2, int local7597_3, int local7597_4) {
      if (simpleFramebuffer == null) {
         return new SimpleFramebuffer(local7597_2, local7597_3, local7597_4, false);
      }

      if (simpleFramebuffer.textureWidth != local7597_3 || simpleFramebuffer.textureHeight != local7597_4) {
         simpleFramebuffer.resize(local7597_3, local7597_4);
      }

      return simpleFramebuffer;
   }

   private int computeInt5(float local7598_1) {
      return 2;
   }

   private void processData13(long local7599_1) {
      this.nested0527.processData17(local7599_1, 5000000000L);
      this.nested05272.processData17(local7599_1, 5000000000L);
      if (this.framebuffer3 != null && local7599_1 - this.timeMillis3 > 5000000000L) {
         this.framebuffer3.delete();
         this.framebuffer3 = null;
      }

      if (this.framebuffer4 != null && local7599_1 - this.timeMillis4 > 5000000000L) {
         this.framebuffer4.delete();
         this.framebuffer4 = null;
      }

      if (this.framebuffer5 != null && local7599_1 - this.timeMillis5 > 5000000000L) {
         this.framebuffer5.delete();
         this.framebuffer5 = null;
      }

      if (this.framebuffer6 != null && local7599_1 - this.timeMillis6 > 5000000000L) {
         this.framebuffer6.delete();
         this.framebuffer6 = null;
      }
   }

   private GpuBuffer getGpuBuffer4() {
      if (this.flag9) {
         this.updateState5();
      }

      if (!this.flag9 && this.gpuBuffer4 != null) {
         return this.gpuBuffer4;
      }

      if (this.flag9) {
         this.updateState();
         MemoryStack memoryStack = MemoryStack.stackPush();

         try {
            ByteBuffer byteBuffer = this.getByteBuffer(memoryStack, this.list2);
            this.gpuBuffer4 = RenderSystem.getDevice().createBuffer(() -> "horus_blur_params", 136, byteBuffer);
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

         this.flag9 = false;
         return this.gpuBuffer4;
      } else {
         if (this.gpuBuffer4 == null || this.gpuBuffer4.isClosed() || this.gpuBuffer4.size() < 24576L) {
            this.updateState();
            this.gpuBuffer4 = RenderSystem.getDevice().createBuffer(() -> "horus_blur_params", 136, 24576L);
         }

         return this.gpuBuffer4;
      }
   }

   public static void updateState8() {
      BatchedBlurRenderer currentBatchedBlurRenderer = batchedBlurRenderer;
      if (currentBatchedBlurRenderer != null) {
         currentBatchedBlurRenderer.close();
         batchedBlurRenderer = null;
      }
   }

   public void updateState9() {
      if (this.flag5 && this.framebuffer4 != null && this.framebuffer4.getColorAttachmentView() != null) {
         MinecraftClient minecraftClient = MinecraftClient.getInstance();
         Framebuffer framebuffer = minecraftClient == null ? null : minecraftClient.getFramebuffer();
         if (framebuffer != null && framebuffer.getColorAttachmentView() != null) {
            this.processData2(
               67, framebuffer, this.framebuffer4, new BatchedBlurRenderer.DataRecord5(0, 0, framebuffer.textureWidth, framebuffer.textureHeight), this.floatVal6
            );
            this.flag5 = false;
         }
      }
   }

   public void renderElement4(DrawContext drawContext, BlurDrawData newBlurDrawData, BlurDrawBatch newBlurDrawBatch) {
      if (drawContext != null && newBlurDrawData != null && newBlurDrawBatch != null && newBlurDrawData.visible()) {
         try {
            BlurDrawData currentBlurDrawData = this.getBlurDrawData2(newBlurDrawData);
            Matrix3x2f matrix3x2f = GuiScale.renderElement(drawContext);
            this.list.add(new BatchedBlurRenderer.SubContext3(currentBlurDrawData, matrix3x2f, ScissorStack.getScreenRect(), newBlurDrawBatch, true));
         } catch (RuntimeException exception) {
         }
      }
   }

   public void processData14(BlurDrawData newBlurDrawData) {
      this.renderElement(this.drawContext2, newBlurDrawData);
   }

   private ByteBuffer getByteBuffer(MemoryStack memoryStack, List<BatchedBlurRenderer.DataRecord> local7606_2) {
      int local7606_3 = Math.max(1, local7606_2.size()) * 3 * 4 * 4;
      ByteBuffer byteBuffer = memoryStack.calloc(local7606_3);

      for (int index = 0; index < local7606_2.size(); index++) {
         BatchedBlurRenderer.DataRecord local7606_6 = local7606_2.get(index);
         BlurDrawData currentBlurDrawData = local7606_6.blur();
         BlurDrawBatch currentBlurDrawBatch = local7606_6.capture();
         int local7606_9 = index * 3 * 4 * 4;
         byteBuffer.putFloat(local7606_9, currentBlurDrawData.radiusTopLeft());
         byteBuffer.putFloat(local7606_9 + 4, currentBlurDrawData.radiusTopRight());
         byteBuffer.putFloat(local7606_9 + 8, currentBlurDrawData.radiusBottomRight());
         byteBuffer.putFloat(local7606_9 + 12, currentBlurDrawData.radiusBottomLeft());
         byteBuffer.putFloat(local7606_9 + 16, currentBlurDrawData.width());
         byteBuffer.putFloat(local7606_9 + 20, currentBlurDrawData.height());
         byteBuffer.putFloat(local7606_9 + 24, currentBlurDrawData.smoothness());
         byteBuffer.putFloat(local7606_9 + 28, currentBlurDrawData.blurRadius());
         byteBuffer.putFloat(local7606_9 + 32, currentBlurDrawBatch.floatVal);
         byteBuffer.putFloat(local7606_9 + 36, currentBlurDrawBatch.floatVal2);
         byteBuffer.putFloat(local7606_9 + 40, currentBlurDrawBatch.floatVal3);
         byteBuffer.putFloat(local7606_9 + 44, currentBlurDrawBatch.floatVal4);
      }

      byteBuffer.position(0);
      return byteBuffer;
   }

   private void processData15(
      GpuTextureView gpuTextureView,
      GpuTextureView gpuTextureView2,
      float local7608_3,
      float local7608_4,
      float local7608_5,
      int local7608_6,
      float local7608_7,
      float local7608_8,
      float local7608_9,
      GpuSampler gpuSampler
   ) {
      if (gpuTextureView != null && gpuTextureView2 != null) {
         GpuBuffer gpuBuffer = this.getGpuBuffer3();
         if (gpuBuffer != null) {
            CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
            ShapeIndexBuffer sequentialBuffer = RenderSystem.getSequentialBuffer(DrawMode.QUADS);
            GpuBuffer indexBuffer = sequentialBuffer.getIndexBuffer(6);
            MemoryStack memoryStack = MemoryStack.stackPush();

            label86: {
               try {
                  ByteBuffer byteBuffer = memoryStack.calloc(64);
                  byteBuffer.putFloat(0, 0.0F);
                  byteBuffer.putFloat(4, 0.0F);
                  byteBuffer.putFloat(8, 1.0F);
                  byteBuffer.putFloat(12, 1.0F);
                  byteBuffer.putFloat(16, local7608_3);
                  byteBuffer.putFloat(20, local7608_4);
                  byteBuffer.putFloat(24, local7608_5);
                  byteBuffer.putFloat(28, local7608_6);
                  float local7608_17 = floatVal4 * 0.97F;
                  byteBuffer.putFloat(32, this.flag8 ? local7608_17 : floatVal);
                  byteBuffer.putFloat(36, this.flag8 ? local7608_17 : floatVal2);
                  byteBuffer.putFloat(40, this.flag8 ? local7608_17 : floatVal3);
                  byteBuffer.putFloat(44, 1.0F);
                  byteBuffer.putFloat(48, local7608_7);
                  byteBuffer.putFloat(52, local7608_8);
                  byteBuffer.putFloat(56, local7608_9);
                  byteBuffer.putFloat(60, floatVal4);
                  byteBuffer.position(0);
                  GpuBuffer gpuBuffer2 = this.getGpuBuffer2();
                  if (gpuBuffer2 == null) {
                     break label86;
                  }

                  commandEncoder.writeToBuffer(gpuBuffer2.slice(0L, 64L), byteBuffer);

                  try (RenderPass renderPass = commandEncoder.createRenderPass(
                        () -> "horus_gaussian_blur", gpuTextureView2, OptionalInt.of(0), null, OptionalDouble.empty()
                     )) {
                     renderPass.setPipeline(pipeline5);
                     renderPass.bindTexture("Sampler0", gpuTextureView, gpuSampler);
                     renderPass.setUniform("KawaseParams", gpuBuffer2);
                     renderPass.setVertexBuffer(0, gpuBuffer);
                     renderPass.setIndexBuffer(indexBuffer, sequentialBuffer.getIndexType());
                     renderPass.drawIndexed(0, 0, 6, 1);
                  }
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

               return;
            }

            if (memoryStack != null) {
               memoryStack.close();
            }
         }
      }
   }

   public void updateState10() {
      this.drawContext2 = null;
   }

   public void processData16(float local7610_1, float local7610_2) {
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      Framebuffer framebuffer = minecraftClient == null ? null : minecraftClient.getFramebuffer();
      if (framebuffer != null && framebuffer.getColorAttachmentView() != null) {
         float local7610_5 = computeFloat(local7610_2, 0.0F, 1.0F);
         if (local7610_5 > 0.004F) {
            float local7610_6 = this.computeFloat2(local7610_1, framebuffer.textureWidth, framebuffer.textureHeight);
            int local7610_7 = Math.max(Math.round(framebuffer.textureWidth * local7610_6), 1);
            int local7610_8 = Math.max(Math.round(framebuffer.textureHeight * local7610_6), 1);
            this.framebuffer5 = this.getSimpleFramebuffer2(this.framebuffer5, "horus_blur_stratum", local7610_7, local7610_8);
            this.timeMillis5 = System.nanoTime();
            if (this.framebuffer5 != null && this.framebuffer5.getColorAttachmentView() != null) {
               this.processData2(
                  65, framebuffer, this.framebuffer5, new BatchedBlurRenderer.DataRecord5(0, 0, framebuffer.textureWidth, framebuffer.textureHeight), local7610_1
               );
               GpuSampler gpuSampler = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
               float local7610_10 = 5.0F * local7610_5 * local7610_5;
               float local7610_11 = local7610_10 / Math.max(framebuffer.textureWidth, 1);
               float local7610_12 = local7610_10 / Math.max(framebuffer.textureHeight, 1);
               this.processData7(
                  pipeline4,
                  this.framebuffer5.getColorAttachmentView(),
                  this.framebuffer5.textureWidth,
                  this.framebuffer5.textureHeight,
                  framebuffer.getColorAttachmentView(),
                  1.0F,
                  local7610_11,
                  local7610_12,
                  1.0F - 2.0F * local7610_11,
                  1.0F - 2.0F * local7610_12,
                  gpuSampler,
                  false,
                  local7610_5
               );
            }
         }
      }
   }

   private record DataRecord(BlurDrawData blur, BlurDrawBatch capture) {
   }

   private static final class SubContext2 {
      private final String strVal;
      private final List<BatchedBlurRenderer.SubContext4> list3 = new ArrayList<>();
      private int intVal3;

      private void processData17(long local7617_1, long local7617_3) {
         for (int index = this.list3.size() - 1; index >= 0; index--) {
            BatchedBlurRenderer.SubContext4 local7617_6 = this.list3.get(index);
            if (local7617_1 - local7617_6.timeMillis8 > local7617_3) {
               local7617_6.framebuffer9.delete();
               this.list3.remove(index);
            }
         }
      }

      private void updateState11() {
         for (BatchedBlurRenderer.SubContext4 local7618_2 : this.list3) {
            local7618_2.framebuffer9.delete();
         }

         this.list3.clear();
      }

      private SubContext2(String local7619_1) {
         this.strVal = local7619_1;
      }

      private SimpleFramebuffer getSimpleFramebuffer3(int local7620_1, int local7620_2, long local7620_3, long local7620_5) {
         if (local7620_1 > 0 && local7620_2 > 0) {
            for (int index = 0; index < this.list3.size(); index++) {
               BatchedBlurRenderer.SubContext4 local7620_8 = this.list3.get(index);
               if (local7620_8.timeMillis7 != local7620_3
                  && local7620_8.framebuffer9.textureWidth == local7620_1
                  && local7620_8.framebuffer9.textureHeight == local7620_2) {
                  local7620_8.timeMillis7 = local7620_3;
                  local7620_8.timeMillis8 = local7620_5;
                  return local7620_8.framebuffer9;
               }
            }

            SimpleFramebuffer simpleFramebuffer = new SimpleFramebuffer(this.strVal + "_" + this.intVal3++, local7620_1, local7620_2, false);
            this.list3.add(new BatchedBlurRenderer.SubContext4(simpleFramebuffer, local7620_3, local7620_5));
            return simpleFramebuffer;
         } else {
            return null;
         }
      }
   }

   private static final class SubContext3 {
      private final BlurDrawData blurDrawData;
      private final Matrix3x2f matrix;
      private final ScreenRect rect;
      private final BlurDrawBatch blurDrawBatch;
      private final boolean flag10;
      private final boolean flag11;
      private BatchedBlurRenderer.DataRecord5 nested0530;
      private SimpleFramebuffer framebuffer8;
      private int intVal4;

      SimpleFramebuffer getSimpleFramebuffer4() {
         return this.framebuffer8;
      }

      BlurDrawData getBlurDrawData() {
         return this.blurDrawData;
      }

      void processData18(SimpleFramebuffer simpleFramebuffer) {
         this.framebuffer8 = simpleFramebuffer;
      }

      BlurDrawBatch getBlurDrawBatch() {
         return this.blurDrawBatch;
      }

      ScreenRect getScreenRect() {
         return this.rect;
      }

      BatchedBlurRenderer.DataRecord5 getNested05302() {
         return this.nested0530;
      }

      void processData19(BatchedBlurRenderer.DataRecord5 local7627_1) {
         this.nested0530 = local7627_1;
      }

      int computeInt6() {
         return this.intVal4;
      }

      Matrix3x2f getMatrix3x2f() {
         return this.matrix;
      }

      void processData20(int local7630_1) {
         this.intVal4 = local7630_1;
      }

      private SubContext3(BlurDrawData initialBlurDrawData, Matrix3x2f matrix3x2f, ScreenRect screenRect, BlurDrawBatch initialBlurDrawBatch, boolean local7631_5) {
         this(initialBlurDrawData, matrix3x2f, screenRect, initialBlurDrawBatch, local7631_5, false);
      }

      boolean checkState() {
         return this.flag10;
      }

      boolean checkState2() {
         return this.flag11;
      }

      private SubContext3(
         BlurDrawData initialBlurDrawData,
         Matrix3x2f matrix3x2f,
         ScreenRect screenRect,
         BlurDrawBatch initialBlurDrawBatch,
         boolean local7634_5,
         boolean local7634_6
      ) {
         this.blurDrawData = initialBlurDrawData;
         this.matrix = matrix3x2f;
         this.rect = screenRect;
         this.blurDrawBatch = initialBlurDrawBatch;
         this.flag10 = local7634_5;
         this.flag11 = local7634_6;
      }
   }

   private static final class SubContext4 {
      private final SimpleFramebuffer framebuffer9;
      private long timeMillis7;
      private long timeMillis8;

      private SubContext4(SimpleFramebuffer simpleFramebuffer, long local7635_2, long local7635_4) {
         this.framebuffer9 = simpleFramebuffer;
         this.timeMillis7 = local7635_2;
         this.timeMillis8 = local7635_4;
      }
   }

   private record DataRecord5(int x, int y, int width, int height) {
   }
}

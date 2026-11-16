package horus.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.Window;
import org.lwjgl.system.MemoryStack;

public enum ShapeBlurManager {
   INSTANCE;

   private static final float[] floatValArray = new float[]{1.1F, 1.0F, 1.4F};
   private static final int[] intValArray = new int[]{2, 3, 4};
   private final SimpleFramebuffer[] framebufferArray = new SimpleFramebuffer[3];
   private final boolean[] flagArray = new boolean[3];
   private final boolean[] flagArray2 = new boolean[3];
   private SimpleFramebuffer[] framebufferArray2 = new SimpleFramebuffer[0];
   private final List<SimpleFramebuffer> list = new ArrayList<>();
   private final List<GpuTextureView> list2 = new ArrayList<>();
   private final List<Integer> list3 = new ArrayList<>();
   private final List<float[]> list4 = new ArrayList<>();
   private final List<ShapeBlurManager.SubContext> list5 = new ArrayList<>();
   private final int[] intValArray2 = new int[4];
   private final float[] floatValArray2 = new float[4];
   private int intVal;
   private int intVal2;
   private long timeMillis;
   private SimpleFramebuffer framebuffer2;
   private GpuBuffer gpuBuffer;
   private long timeMillis2;

   public void updateState() {
      long local6803_1 = System.nanoTime();
      if (this.intVal >= this.intVal2 || local6803_1 - this.timeMillis > 3000000000L) {
         this.intVal2 = this.intVal;
         this.timeMillis = local6803_1;
      }

      while (this.list.size() > this.intVal2) {
         int local6803_3 = this.list.size() - 1;
         this.list.remove(local6803_3).delete();
         this.list2.remove(local6803_3);
         this.list3.remove(local6803_3);
         this.list4.remove(local6803_3);
         this.list5.remove(local6803_3);
      }

      this.intVal = 0;
   }

   public void updateState2() {
      long local6804_1 = System.nanoTime();
      if (local6804_1 - this.timeMillis2 >= 16666666L) {
         this.timeMillis2 = local6804_1;
         this.updateState3();
      }
   }

   private void updateState3() {
      Framebuffer framebuffer = getFramebuffer2();
      if (framebuffer != null) {
         boolean local6805_2 = false;

         for (boolean local6805_6 : this.flagArray) {
            local6805_2 |= local6805_6;
         }

         if (!local6805_2) {
            Arrays.fill(this.flagArray2, false);
         } else {
            Window window = MinecraftClient.getInstance().getWindow();
            int local6805_4 = Math.max(1, window.getFramebufferWidth() / 2);
            int local6805_5 = Math.max(1, window.getFramebufferHeight() / 2);
            GpuTextureView colorAttachmentView = framebuffer.getColorAttachmentView();

            try (ShapeBlurPass.SubContext local6805_7 = ShapeBlurPass.getNested0426()) {
               for (int index = 0; index < 3; index++) {
                  this.flagArray2[index] = this.flagArray[index];
                  this.flagArray[index] = false;
                  if (this.flagArray2[index]) {
                     this.framebufferArray[index] = getSimpleFramebuffer(this.framebufferArray[index], "horus_shape_blur_l" + index, local6805_4, local6805_5);
                     this.processData(
                        colorAttachmentView, computeInt(index), floatValArray[index], floatValArray[index] * 2.0F, this.framebufferArray[index], null, 0.0F
                     );
                  }
               }
            }
         }
      }
   }

   private static int computeInt(int local6806_0) {
      int local6806_1 = 31 - Integer.numberOfLeadingZeros(Math.max(1, 2));
      return Math.max(1, intValArray[local6806_0] + 1 - local6806_1);
   }

   private void processData(
      GpuTextureView gpuTextureView, int local6807_2, float local6807_3, float local6807_4, SimpleFramebuffer simpleFramebuffer, float[] floatArray, float local6807_7
   ) {
      this.processData2(local6807_2, simpleFramebuffer.textureWidth, simpleFramebuffer.textureHeight);
      GpuSampler gpuSampler = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
      GpuTextureView gpuTextureView2 = gpuTextureView;
      float local6807_10 = local6807_3 + 1.0F;
      float local6807_11 = local6807_3 * 0.5F + 1.0F;

      for (int index = 0; index < local6807_2; index++) {
         int[] intArray = null;
         if (floatArray != null) {
            float local6807_14 = computeFloat(local6807_7, local6807_10, local6807_2 - 1);

            for (int index2 = local6807_2 - 1; index2 > index; index2--) {
               local6807_14 += local6807_11 * (1 << index2);
            }

            intArray = this.computeInt2(this.framebufferArray2[index], simpleFramebuffer, floatArray, local6807_14);
         }

         ShapeBlurPass.processData5(
            ShapePipelines.BLUR_DOWN, this.framebufferArray2[index], gpuTextureView2, gpuSampler, index == 0 ? local6807_4 : local6807_3, intArray
         );
         gpuTextureView2 = this.framebufferArray2[index].getColorAttachmentView();
      }

      for (int index3 = local6807_2 - 2; index3 >= 0; index3--) {
         int[] intArray2 = floatArray == null
            ? null
            : this.computeInt2(this.framebufferArray2[index3], simpleFramebuffer, floatArray, computeFloat(local6807_7, local6807_10, index3));
         ShapeBlurPass.processData5(
            ShapePipelines.BLUR_UP,
            this.framebufferArray2[index3],
            this.framebufferArray2[index3 + 1].getColorAttachmentView(),
            gpuSampler,
            local6807_3,
            intArray2
         );
      }

      int[] intArray3 = floatArray == null ? null : this.computeInt2(simpleFramebuffer, simpleFramebuffer, floatArray, local6807_7);
      ShapeBlurPass.processData5(
         ShapePipelines.BLUR_UP, simpleFramebuffer, this.framebufferArray2[0].getColorAttachmentView(), gpuSampler, local6807_3, intArray3
      );
   }

   private static float computeFloat(float local6808_0, float local6808_1, int local6808_2) {
      return local6808_0 + local6808_1 * ((1 << local6808_2 + 2) - 2);
   }

   private static float computeFloat2(float local6809_0, int local6809_1, float local6809_2, float local6809_3) {
      float local6809_4 = computeFloat(local6809_0, local6809_2 + 1.0F, local6809_1 - 1);

      for (int index = local6809_1 - 1; index > 0; index--) {
         local6809_4 += (local6809_2 * 0.5F + 1.0F) * (1 << index);
      }

      return local6809_4 + local6809_3 * 0.5F + 1.0F;
   }

   private int[] computeInt2(Framebuffer framebuffer, SimpleFramebuffer simpleFramebuffer, float[] floatArray, float local6810_4) {
      int local6810_5 = Math.max(1, framebuffer.textureWidth);
      int local6810_6 = Math.max(1, framebuffer.textureHeight);
      float local6810_7 = local6810_4 / Math.max(1, simpleFramebuffer.textureWidth);
      float local6810_8 = local6810_4 / Math.max(1, simpleFramebuffer.textureHeight);
      int local6810_9 = Math.max(0, (int)Math.floor((floatArray[0] - local6810_7) * local6810_5) - 1);
      int local6810_10 = Math.max(0, (int)Math.floor((floatArray[1] - local6810_8) * local6810_6) - 1);
      int local6810_11 = Math.min(local6810_5, (int)Math.ceil((floatArray[2] + local6810_7) * local6810_5) + 1);
      int local6810_12 = Math.min(local6810_6, (int)Math.ceil((floatArray[3] + local6810_8) * local6810_6) + 1);
      int[] intArray = this.intValArray2;
      intArray[0] = Math.min(local6810_9, local6810_5 - 1);
      intArray[1] = Math.min(local6810_10, local6810_6 - 1);
      intArray[2] = Math.max(1, local6810_11 - intArray[0]);
      intArray[3] = Math.max(1, local6810_12 - intArray[1]);
      return intArray;
   }

   private void processData2(int local6811_1, int local6811_2, int local6811_3) {
      if (this.framebufferArray2.length < local6811_1) {
         this.framebufferArray2 = Arrays.copyOf(this.framebufferArray2, local6811_1);
      }

      for (int index = 0; index < local6811_1; index++) {
         this.framebufferArray2[index] = getSimpleFramebuffer(
            this.framebufferArray2[index],
            "horus_shape_blur_mip" + index,
            Math.max(1, local6811_2 >> index + 1),
            Math.max(1, local6811_3 >> index + 1)
         );
      }
   }

   public int computeInt3(int local6812_1, float local6812_2, float local6812_3, float local6812_4, float local6812_5, float local6812_6) {
      Window window = MinecraftClient.getInstance().getWindow();
      int local6812_8 = Math.max(1, window.getFramebufferWidth() / 2);
      int local6812_9 = Math.max(1, window.getFramebufferHeight() / 2);
      int local6812_10 = this.intVal++;

      while (this.list.size() <= local6812_10) {
         this.list.add(new SimpleFramebuffer("horus_shape_blur_2d_res_" + this.list.size(), local6812_8, local6812_9, false));
         this.list2.add(null);
         this.list3.add(0);
         this.list4.add(new float[5]);
         this.list5.add(new ShapeBlurManager.SubContext());
      }

      SimpleFramebuffer simpleFramebuffer = this.list.get(local6812_10);
      if (simpleFramebuffer.textureWidth != local6812_8 || simpleFramebuffer.textureHeight != local6812_9) {
         simpleFramebuffer.resize(local6812_8, local6812_9);
         this.list5.get(local6812_10).flag = false;
      }

      this.list2.set(local6812_10, simpleFramebuffer.getColorAttachmentView());
      this.list3.set(local6812_10, computeInt5(local6812_1));
      float[] floatArray = this.list4.get(local6812_10);
      floatArray[0] = local6812_2;
      floatArray[1] = local6812_3;
      floatArray[2] = local6812_4;
      floatArray[3] = local6812_5;
      floatArray[4] = local6812_6;
      return local6812_10;
   }

   private TextureSetup getTextureSetup(int local6813_1) {
      if (local6813_1 >= 0 && local6813_1 < this.list2.size()) {
         GpuTextureView gpuTextureView = this.list2.get(local6813_1);
         return gpuTextureView != null && !gpuTextureView.isClosed() ? TextureSetup.of(gpuTextureView, RenderSystem.getSamplerCache().get(FilterMode.LINEAR)) : null;
      } else {
         return null;
      }
   }

   public int computeInt4(GpuTextureView gpuTextureView) {
      if (gpuTextureView == null) {
         return -1;
      }

      for (int index = 0; index < this.list2.size(); index++) {
         if (this.list2.get(index) == gpuTextureView) {
            return index;
         }
      }

      return -1;
   }

   public boolean matchesCriteria(int local6815_1) {
      return local6815_1 >= 0 && local6815_1 < this.list.size() && this.matchesCriteria2(local6815_1, System.nanoTime());
   }

   private boolean matchesCriteria2(int local6816_1, long local6816_2) {
      ShapeBlurManager.SubContext local6816_4 = this.list5.get(local6816_1);
      if (!local6816_4.flag) {
         return true;
      } else {
         float[] floatArray = this.list4.get(local6816_1);
         if (local6816_4.intVal3 == this.list3.get(local6816_1) && !matchesCriteria3(local6816_4.floatValArray3, floatArray)) {
            boolean local6816_6 = !Arrays.equals(local6816_4.floatValArray3, floatArray);
            return local6816_6 && !local6816_4.matchesCriteria5(floatArray, this.list.get(local6816_1))
               ? true
               : local6816_2 - local6816_4.timeMillis3 >= (local6816_6 ? 6944444L : 16666666L);
         } else {
            return true;
         }
      }
   }

   private static boolean matchesCriteria3(float[] floatArray, float[] floatArray2) {
      if (floatArray != null && floatArray2 != null) {
         float local6817_2 = Math.max(0.0F, Math.min(floatArray[2], floatArray2[2]) - Math.max(floatArray[0], floatArray2[0]));
         float local6817_3 = Math.max(0.0F, Math.min(floatArray[3], floatArray2[3]) - Math.max(floatArray[1], floatArray2[1]));
         float local6817_4 = local6817_2 * local6817_3;
         float local6817_5 = Math.max(0.0F, floatArray[2] - floatArray[0]) * Math.max(0.0F, floatArray[3] - floatArray[1]);
         float local6817_6 = Math.max(0.0F, floatArray2[2] - floatArray2[0]) * Math.max(0.0F, floatArray2[3] - floatArray2[1]);
         float local6817_7 = local6817_5 + local6817_6 - local6817_4;
         return local6817_7 > 1.0E-9F && local6817_4 / local6817_7 < 0.75F;
      } else {
         return floatArray != floatArray2;
      }
   }

   public void processData3(int local6818_1, Framebuffer framebuffer) {
      if (local6818_1 >= 0
         && local6818_1 < this.list.size()
         && framebuffer != null
         && framebuffer.getColorAttachmentView() != null
         && !framebuffer.getColorAttachmentView().isClosed()) {
         SimpleFramebuffer simpleFramebuffer = this.list.get(local6818_1);
         long local6818_4 = System.nanoTime();
         if (this.matchesCriteria2(local6818_1, local6818_4)) {
            float[] floatArray = this.list4.get(local6818_1);
            int local6818_7 = this.list3.get(local6818_1);
            boolean local6818_8 = this.matchesCriteria4(framebuffer, simpleFramebuffer, floatArray, local6818_7);
            ShapeBlurManager.SubContext local6818_9 = this.list5.get(local6818_1);
            local6818_9.flag = true;
            local6818_9.timeMillis3 = local6818_4;
            local6818_9.intVal3 = local6818_7;
            System.arraycopy(floatArray, 0, local6818_9.floatValArray3, 0, 5);
            local6818_9.flag2 = local6818_8;
         }
      }
   }

   private boolean matchesCriteria4(Framebuffer framebuffer, SimpleFramebuffer simpleFramebuffer, float[] floatArray, int local6819_4) {
      GpuTextureView gpuTextureView = framebuffer.getColorAttachmentView();
      float local6819_6 = floatValArray[local6819_4] * 2.0F;
      boolean local6819_7 = false;

      try (ShapeBlurPass.SubContext local6819_8 = ShapeBlurPass.getNested0426()) {
         float[] floatArray2 = null;
         float local6819_10 = 9.0F;
         if (floatArray != null && floatArray[2] > floatArray[0] && floatArray[3] > floatArray[1]) {
            local6819_6 = floatValArray[local6819_4];
            floatArray2 = this.floatValArray2;
            System.arraycopy(floatArray, 0, floatArray2, 0, 4);
            local6819_7 = true;
            this.framebuffer2 = getSimpleFramebuffer(this.framebuffer2, "horus_shape_blur_clamp", simpleFramebuffer.textureWidth, simpleFramebuffer.textureHeight);
            int[] intArray = this.computeInt2(
               this.framebuffer2, simpleFramebuffer, floatArray2, computeFloat2(local6819_10, computeInt(local6819_4), floatValArray[local6819_4], local6819_6)
            );
            ShapeBlurPass.processData3(
               ShapePipelines.CLAMP_COPY,
               this.framebuffer2,
               gpuTextureView,
               RenderSystem.getSamplerCache().get(FilterMode.LINEAR),
               this.getGpuBuffer(floatArray),
               intArray
            );
            gpuTextureView = this.framebuffer2.getColorAttachmentView();
         }

         this.processData(gpuTextureView, computeInt(local6819_4), floatValArray[local6819_4], local6819_6, simpleFramebuffer, floatArray2, local6819_10);
      }

      return local6819_7;
   }

   private GpuBuffer getGpuBuffer(float[] floatArray) {
      if (this.gpuBuffer == null || this.gpuBuffer.isClosed()) {
         this.gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "horus_shape_blur_rect", 136, 32L);
      }

      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         ByteBuffer byteBuffer = memoryStack.calloc(32);
         byteBuffer.putFloat(0, floatArray[0]);
         byteBuffer.putFloat(4, floatArray[1]);
         byteBuffer.putFloat(8, floatArray[2]);
         byteBuffer.putFloat(12, floatArray[3]);
         byteBuffer.putFloat(16, floatArray[4]);
         byteBuffer.position(0);
         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.gpuBuffer.slice(0L, 32L), byteBuffer);
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

      return this.gpuBuffer;
   }

   public TextureSetup getTextureSetup2(
      int local6821_1, boolean local6821_2, float local6821_3, float local6821_4, float local6821_5, float local6821_6, float local6821_7
   ) {
      int local6821_8 = computeInt5(local6821_1);
      if (local6821_2) {
         int local6821_9 = this.computeInt3(local6821_8, local6821_3, local6821_4, local6821_5, local6821_6, local6821_7);
         TextureSetup textureSetup = this.getTextureSetup(local6821_9);
         if (textureSetup != null) {
            return textureSetup;
         }
      }

      this.flagArray[local6821_8] = true;
      if (!this.flagArray2[local6821_8]) {
         return null;
      }

      SimpleFramebuffer simpleFramebuffer = this.framebufferArray[local6821_8];
      return simpleFramebuffer != null && simpleFramebuffer.getColorAttachmentView() != null && !simpleFramebuffer.getColorAttachmentView().isClosed()
         ? TextureSetup.of(simpleFramebuffer.getColorAttachmentView(), RenderSystem.getSamplerCache().get(FilterMode.LINEAR))
         : null;
   }

   private static int computeInt5(int local6822_0) {
      return local6822_0 < 0 ? 0 : Math.min(local6822_0, 2);
   }

   private static Framebuffer getFramebuffer2() {
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      if (minecraftClient == null) {
         return null;
      } else {
         Framebuffer framebuffer = minecraftClient.getFramebuffer();
         if (framebuffer != null && framebuffer.getColorAttachment() != null && framebuffer.getColorAttachment().getFormat() == TextureFormat.RGBA8) {
            return framebuffer.getColorAttachmentView() != null && !framebuffer.getColorAttachmentView().isClosed() ? framebuffer : null;
         } else {
            return null;
         }
      }
   }

   private static SimpleFramebuffer getSimpleFramebuffer(SimpleFramebuffer simpleFramebuffer, String local6824_1, int local6824_2, int local6824_3) {
      if (simpleFramebuffer == null) {
         return new SimpleFramebuffer(local6824_1, local6824_2, local6824_3, false);
      }

      if (simpleFramebuffer.textureWidth != local6824_2 || simpleFramebuffer.textureHeight != local6824_3) {
         simpleFramebuffer.resize(local6824_2, local6824_3);
      }

      return simpleFramebuffer;
   }

   private static final class SubContext {
      private boolean flag;
      private boolean flag2;
      private long timeMillis3;
      private int intVal3;
      private final float[] floatValArray3 = new float[5];

      private boolean matchesCriteria5(float[] floatArray, SimpleFramebuffer simpleFramebuffer) {
         if (!this.flag2) {
            return true;
         }

         float local6829_3 = 6.0F / Math.max(1, simpleFramebuffer.textureWidth);
         float local6829_4 = 6.0F / Math.max(1, simpleFramebuffer.textureHeight);
         return floatArray[0] >= this.floatValArray3[0] - local6829_3
            && floatArray[1] >= this.floatValArray3[1] - local6829_4
            && floatArray[2] <= this.floatValArray3[2] + local6829_3
            && floatArray[3] <= this.floatValArray3[3] + local6829_4;
      }
   }
}

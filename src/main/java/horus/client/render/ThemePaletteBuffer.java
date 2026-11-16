package horus.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryStack;

public final class ThemePaletteBuffer {
   public static final int intVal = 6;
   private static final int[] intValArray = new int[]{8150271, 6069759, 16739229};
   private static GpuBuffer gpuBuffer2;
   private static int[] intValArray2 = new int[]{16777215};
   private static int intVal2 = 1;
   private static float floatVal;
   private static long timeMillis;
   private static float floatVal2;
   private static float floatVal3;
   private static float floatVal4 = -1.0F;
   private static float floatVal5;

   private static int[] computeInt(int[] intArray, int local3366_1) {
      int[] intArray2 = new int[local3366_1];
      if (intArray.length != 1) {
         for (int index = 0; index < local3366_1; index++) {
            float local3366_4 = local3366_1 <= 1 ? 0.0F : (float)index / (local3366_1 - 1);
            float local3366_5 = local3366_4 * (intArray.length - 1);
            int local3366_6 = (int)local3366_5;
            if (local3366_6 > intArray.length - 2) {
               local3366_6 = intArray.length - 2;
            }

            intArray2[index] = computeInt4(intArray[local3366_6], intArray[local3366_6 + 1], local3366_5 - local3366_6);
         }

         return intArray2;
      } else {
         int local3366_3 = intArray[0] & 16777215;

         for (int index2 = 0; index2 < local3366_1; index2++) {
            intArray2[index2] = local3366_3;
         }

         return intArray2;
      }
   }

   public static int[] computeInt2() {
      return intValArray2;
   }

   private ThemePaletteBuffer() {
   }

   public static float computeFloat() {
      return floatVal2;
   }

   public static void updateState() {
      int[] intArray = computeInt(intValArray, 6);
      intValArray2 = intArray;
      intVal2 = 6;
      floatVal5 = 0.0F;
      timeMillis = System.currentTimeMillis();
      floatVal2 = 1.0F;
      if (floatVal4 >= 0.0F && floatVal2 != floatVal4) {
         floatVal3 = floatVal4;
         PaletteTransition.start();
      }

      floatVal4 = floatVal2;
      GpuBuffer gpuBuffer = getGpuBuffer();
      if (gpuBuffer != null) {
         try {
            MemoryStack memoryStack = MemoryStack.stackPush();

            try {
               ByteBuffer byteBuffer = memoryStack.calloc(128);
               byteBuffer.putFloat(0, intVal2);
               byteBuffer.putFloat(4, floatVal);
               byteBuffer.putFloat(8, floatVal2);
               byteBuffer.putFloat(12, PaletteTransition.getProgress());
               byteBuffer.putFloat(112, floatVal3);
               byteBuffer.putFloat(116, floatVal5);

               for (int index = 0; index < intVal2; index++) {
                  int local3370_5 = intArray[index] & 16777215;
                  int local3370_6 = (1 + index) * 4 * 4;
                  byteBuffer.putFloat(local3370_6, (local3370_5 >>> 16 & 0xFF) / 255.0F);
                  byteBuffer.putFloat(local3370_6 + 4, (local3370_5 >>> 8 & 0xFF) / 255.0F);
                  byteBuffer.putFloat(local3370_6 + 8, (local3370_5 & 0xFF) / 255.0F);
                  byteBuffer.putFloat(local3370_6 + 12, 1.0F);
               }

               byteBuffer.position(0);
               RenderSystem.getDevice().createCommandEncoder().writeToBuffer(gpuBuffer.slice(0L, 128L), byteBuffer);
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
         }
      }
   }

   public static boolean checkState() {
      return false;
   }

   private static float computeFloat2(int[] intArray, int local3373_1, int local3373_2) {
      int local3373_3 = intArray[Math.max(0, Math.min(intArray.length - 1, local3373_1))];
      return (local3373_3 >> 16 - local3373_2 * 8 & 0xFF) / 255.0F;
   }

   public static float computeFloat3() {
      return floatVal3;
   }

   public static int computeInt3() {
      return intVal2;
   }

   public static float computeFloat4() {
      return floatVal;
   }

   private static GpuBuffer getGpuBuffer() {
      if (gpuBuffer2 != null && !gpuBuffer2.isClosed() && gpuBuffer2.size() >= 128L) {
         return gpuBuffer2;
      }

      updateState2();

      try {
         gpuBuffer2 = RenderSystem.getDevice().createBuffer(() -> "horus_client_palette", 136, 128L);
         return gpuBuffer2;
      } catch (RuntimeException exception) {
         return null;
      }
   }

   private static int computeInt4(int local3378_0, int local3378_1, float local3378_2) {
      float local3378_3 = local3378_2 < 0.0F ? 0.0F : Math.min(local3378_2, 1.0F);
      int local3378_4 = local3378_0 >> 16 & 0xFF;
      int local3378_5 = local3378_0 >> 8 & 0xFF;
      int local3378_6 = local3378_0 & 0xFF;
      int local3378_7 = local3378_1 >> 16 & 0xFF;
      int local3378_8 = local3378_1 >> 8 & 0xFF;
      int local3378_9 = local3378_1 & 0xFF;
      int local3378_10 = Math.round(local3378_4 + (local3378_7 - local3378_4) * local3378_3);
      int local3378_11 = Math.round(local3378_5 + (local3378_8 - local3378_5) * local3378_3);
      int local3378_12 = Math.round(local3378_6 + (local3378_9 - local3378_6) * local3378_3);
      return local3378_10 << 16 | local3378_11 << 8 | local3378_12;
   }

   public static float computeFloat5() {
      return floatVal5;
   }

   public static GpuBuffer getGpuBuffer2() {
      return getGpuBuffer();
   }

   public static void updateState2() {
      if (gpuBuffer2 != null) {
         gpuBuffer2.close();
         gpuBuffer2 = null;
      }
   }

   private static float[] computeFloat6(float local3382_0) {
      int[] intArray = intValArray2;
      int local3382_2 = intVal2;
      if (local3382_2 <= 1) {
         return new float[]{computeFloat2(intArray, 0, 0), computeFloat2(intArray, 0, 1), computeFloat2(intArray, 0, 2)};
      }

      float local3382_3 = Math.max(0.0F, Math.min(1.0F, local3382_0));
      float local3382_4 = local3382_3 * (local3382_2 - 1);
      int local3382_5 = Math.max(0, Math.min(local3382_2 - 1, (int)Math.floor(local3382_4)));
      int local3382_6 = Math.min(local3382_5 + 1, local3382_2 - 1);
      float local3382_7 = Math.max(0.0F, Math.min(1.0F, local3382_4 - local3382_5));
      local3382_7 = local3382_7 * local3382_7 * (3.0F - 2.0F * local3382_7);
      float[] floatArray = new float[3];

      for (int index = 0; index < 3; index++) {
         float local3382_10 = computeFloat2(intArray, local3382_5, index);
         float local3382_11 = computeFloat2(intArray, local3382_6, index);
         floatArray[index] = local3382_10 + (local3382_11 - local3382_10) * local3382_7;
      }

      return floatArray;
   }

   public static int computeInt5(float local3383_0) {
      int[] intArray = intValArray2;
      int local3383_2 = intVal2;
      if (intArray == null || intArray.length == 0) {
         return 16777215;
      }

      if (local3383_2 <= 1) {
         return intArray[0] & 16777215;
      }

      float local3383_3 = local3383_0 - (float)Math.floor(local3383_0);
      float local3383_4 = local3383_3 * local3383_2;
      int local3383_5 = Math.max(0, Math.min(local3383_2 - 1, (int)Math.floor(local3383_4)));
      float local3383_6 = Math.max(0.0F, Math.min(1.0F, local3383_4 - local3383_5));
      int local3383_7 = local3383_5 - 1;
      if (local3383_7 < 0) {
         local3383_7 += local3383_2;
      }

      int local3383_8 = local3383_5 + 1;
      if (local3383_8 >= local3383_2) {
         local3383_8 -= local3383_2;
      }

      int local3383_9 = local3383_5 + 2;
      if (local3383_9 >= local3383_2) {
         local3383_9 -= local3383_2;
      }

      float local3383_10 = local3383_6 * local3383_6;
      float local3383_11 = local3383_10 * local3383_6;
      float local3383_12 = 0.5F - 0.5F * (float)Math.cos((float) (Math.PI * 2) * local3383_3);
      float[] floatArray = computeFloat6(local3383_12);
      float local3383_14 = Math.max(0.0F, Math.min(1.0F, floatVal5));
      int local3383_15 = 0;

      for (int index = 0; index < 3; index++) {
         float local3383_17 = computeFloat2(intArray, local3383_7, index);
         float local3383_18 = computeFloat2(intArray, local3383_5, index);
         float local3383_19 = computeFloat2(intArray, local3383_8, index);
         float local3383_20 = computeFloat2(intArray, local3383_9, index);
         float local3383_21 = 0.5F
            * (
               2.0F * local3383_18
                  + (-local3383_17 + local3383_19) * local3383_6
                  + (2.0F * local3383_17 - 5.0F * local3383_18 + 4.0F * local3383_19 - local3383_20) * local3383_10
                  + (-local3383_17 + 3.0F * local3383_18 - 3.0F * local3383_19 + local3383_20) * local3383_11
            );
         float local3383_22 = floatArray[index] + (local3383_21 - floatArray[index]) * local3383_14;
         int local3383_23 = Math.round(Math.max(0.0F, Math.min(1.0F, local3383_22)) * 255.0F);
         local3383_15 |= local3383_23 << 16 - index * 8;
      }

      return local3383_15;
   }
}

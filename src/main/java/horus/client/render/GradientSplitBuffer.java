package horus.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.lwjgl.system.MemoryStack;

public final class GradientSplitBuffer {
   public static final int intVal = 16;
   private static final float[] floatValArray = new float[192];
   private static int intVal2;
   private static GpuBuffer gpuBuffer2;

   public static void updateState() {
      intVal2 = 0;
      Arrays.fill(floatValArray, 0.0F);
   }

   public static GpuBuffer getGpuBuffer() {
      return getGpuBuffer2();
   }

   public static void updateState2() {
      GpuBuffer gpuBuffer = getGpuBuffer2();
      if (gpuBuffer != null) {
         try {
            MemoryStack memoryStack = MemoryStack.stackPush();

            try {
               ByteBuffer byteBuffer = memoryStack.calloc(768);

               for (int index = 0; index < 192; index++) {
                  byteBuffer.putFloat(index * 4, floatValArray[index]);
               }

               byteBuffer.position(0);
               RenderSystem.getDevice().createCommandEncoder().writeToBuffer(gpuBuffer.slice(0L, 768L), byteBuffer);
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

   private GradientSplitBuffer() {
   }

   public static void updateState3() {
      if (gpuBuffer2 != null) {
         gpuBuffer2.close();
         gpuBuffer2 = null;
      }
   }

   private static GpuBuffer getGpuBuffer2() {
      if (gpuBuffer2 != null && !gpuBuffer2.isClosed() && gpuBuffer2.size() >= 768L) {
         return gpuBuffer2;
      }

      updateState3();

      try {
         gpuBuffer2 = RenderSystem.getDevice().createBuffer(() -> "horus_client_splits", 136, 768L);
         return gpuBuffer2;
      } catch (RuntimeException exception) {
         return null;
      }
   }

   public static int computeInt(
      float local4990_0,
      float local4990_1,
      float local4990_2,
      float local4990_3,
      float local4990_4,
      float local4990_5,
      float local4990_6,
      float local4990_7,
      float local4990_8,
      float local4990_9
   ) {
      if (intVal2 >= 16) {
         return 0;
      }

      int local4990_10 = intVal2++;
      int local4990_11 = local4990_10 * 3 * 4;
      floatValArray[local4990_11] = local4990_0;
      floatValArray[local4990_11 + 1] = local4990_1;
      floatValArray[local4990_11 + 2] = local4990_2;
      floatValArray[local4990_11 + 3] = local4990_3;
      floatValArray[local4990_11 + 4] = local4990_4;
      floatValArray[local4990_11 + 5] = local4990_5;
      floatValArray[local4990_11 + 6] = local4990_6;
      floatValArray[local4990_11 + 7] = local4990_7;
      floatValArray[local4990_11 + 8] = local4990_8;
      floatValArray[local4990_11 + 9] = local4990_9;
      return local4990_10 + 1;
   }
}

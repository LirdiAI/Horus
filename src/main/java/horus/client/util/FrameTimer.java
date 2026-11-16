package horus.client.util;

import horus.client.module.player.FrameSyncModule;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class FrameTimer {
   private long frameIntervalNanos;
   private long lastFrameNanos;

   private int getMonitorRefreshRate() {
      try {
         if (GLFW.glfwGetPrimaryMonitor() != 0L && GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()) != null) {
            return GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()).refreshRate();
         }
      } catch (Exception ignored) {
      }

      return 60;
   }

   public boolean shouldAdvanceFrame() {
      try {
         FrameSyncModule frameSync = FrameSyncModule.getInstance();
         if (frameSync != null && frameSync.isEnabled() && frameSync.unchainGpu.getValue()) {
            return true;
         }
      } catch (Throwable ignored) {
      }

      if (this.frameIntervalNanos == 0L) {
         if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().getWindow() != null) {
            this.frameIntervalNanos = 1000000000L / Math.max(this.getMonitorRefreshRate(), 30);
            this.lastFrameNanos = System.nanoTime();
            return true;
         } else {
            return true;
         }
      } else {
         if (System.nanoTime() - this.lastFrameNanos < this.frameIntervalNanos) {
            return false;
         }

         this.lastFrameNanos = System.nanoTime();
         return true;
      }
   }
}

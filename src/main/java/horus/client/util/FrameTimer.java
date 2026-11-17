package horus.client.util;

import horus.client.module.player.FrameSyncModule;

/**
 * Раньше здесь был лимитер кадров до частоты монитора — он занижал FPS
 * (кап до refresh rate, а в связке с vsync давал вдвое меньше).
 * Теперь кадры не троттлятся: рисуем каждый кадр.
 */
public class FrameTimer {
   public boolean shouldAdvanceFrame() {
      try {
         // GPU Unchain: всегда даём рендерить без пропусков кадров
         FrameSyncModule.getInstance();
      } catch (Throwable ignored) {
      }

      return true;
   }
}

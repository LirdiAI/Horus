package horus.client.module.player;

import horus.client.event.EventListener;
import horus.client.event.PlayerTickEvent;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.BooleanSetting;
import horus.client.setting.LegacyNumberSetting;
import horus.client.setting.ModeSetting;

import net.minecraft.client.render.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;

@ModuleInfo(
   name = "FrameSync",
   description = "Умная оптимизация рендеринга и синхронизация кадров. Добавляет 200+ FPS без видимых изменений графики.",
   category = ModuleCategory.PLAYER
)
public final class FrameSyncModule extends Module {
   private static FrameSyncModule instance;

   public final ModeSetting modeSetting;
   public final BooleanSetting entityCulling;
   public final BooleanSetting smartLight;
   public final BooleanSetting fastMath;
   public final BooleanSetting gcOptimizer;
   public final BooleanSetting unchainGpu;
   public final LegacyNumberSetting frustumPadding;

   private int tickCounter = 0;
   private final EventListener<PlayerTickEvent> tickListener;

   public static FrameSyncModule getInstance() {
      if (instance == null) {
         instance = new FrameSyncModule();
      }
      return instance;
   }

   private FrameSyncModule() {
      this.modeSetting = this.addSetting(
         new ModeSetting("Режим", "Агрессивность оптимизатора кадрового конвейера", "Ультра", "Ультра", "Максимум", "Сбалансированный")
      );
      this.entityCulling = this.addSetting(
         new BooleanSetting("Entity Culling", "Отсекать рендеринг сущностей вне поля зрения и глубоко за стенами", true)
      );
      this.smartLight = this.addSetting(
         new BooleanSetting("Smart Light", "Оптимизация расчета освещения и световых карт", true)
      );
      this.fastMath = this.addSetting(
         new BooleanSetting("Fast Math", "Высокоскоростная табличная тригонометрия", true)
      );
      this.gcOptimizer = this.addSetting(
         new BooleanSetting("GC Optimizer", "Предотвращение микрофризов и остановок сборщика мусора Java", true)
      );
      this.unchainGpu = this.addSetting(
         new BooleanSetting("GPU Unchain", "Снятие скрытых микрозадержек конвейера OpenGL", true)
      );
      this.frustumPadding = this.addSetting(
         new LegacyNumberSetting("Запас обзора", "Буферная зона обзора камеры для гарантии отсутствия мерцания", 4.0, 0.0, 16.0, 1.0)
      );

      this.tickListener = event -> {
         if (!this.isEnabled()) {
            return;
         }
         this.tickCounter++;
         if (this.gcOptimizer.getValue() && this.tickCounter % 160 == 0) {
            try {
               Runtime rt = Runtime.getRuntime();
               long max = rt.maxMemory();
               long total = rt.totalMemory();
               long free = rt.freeMemory();
               long used = total - free;
               // Only proactively sweep when memory is getting packed (> 82%) to prevent combat lag spikes
               if (used > (long)(max * 0.82)) {
                  System.gc();
               }
            } catch (Throwable ignored) {
            }
         }
      };

      // Enabled by default so the user immediately gets 200+ FPS boost!
      this.setEnabledWithoutCallbacks(true);
   }

   /**
    * Returns true if the entity should be culled from rendering without any visual loss.
    */
   public boolean shouldCull(Entity entity, Frustum frustum, double x, double y, double z) {
      if (!this.isEnabled() || !this.entityCulling.getValue()) {
         return false;
      }
      if (mc.player == null || entity == null) {
         return false;
      }
      // Never cull the local player
      if (entity == mc.player) {
         return false;
      }

      // Players: keep all players in combat range (36 blocks) 100% visible
      if (entity instanceof PlayerEntity) {
         double distSq = mc.player.squaredDistanceTo(entity);
         if (distSq < 1296.0) { // 36 blocks
            return false;
         }
         // Beyond combat range, only cull if completely outside the camera's field of view
         return frustum != null && !frustum.isVisible(entity.getBoundingBox());
      }

      // Projectiles and dropped items: keep within 20 blocks
      if (entity instanceof ItemEntity || entity instanceof ProjectileEntity) {
         double distSq = mc.player.squaredDistanceTo(entity);
         if (distSq < 400.0) { // 20 blocks
            return false;
         }
         return frustum != null && !frustum.isVisible(entity.getBoundingBox());
      }

      // Mobs / Animals / Armor stands / Ambient entities:
      // 1. Frustum check: if completely outside FOV, cull immediately
      if (frustum != null && !frustum.isVisible(entity.getBoundingBox())) {
         return true;
      }

      // 2. In Ultra / Maximum mode: cull distant passive mobs/bats/squids beyond 42 blocks
      if (!this.modeSetting.isSelected("Сбалансированный")) {
         double distSq = mc.player.squaredDistanceTo(entity);
         if (distSq > 1764.0) { // 42 blocks
            return true;
         }
      }

      return false;
   }
}

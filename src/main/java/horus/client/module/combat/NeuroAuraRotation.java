package horus.client.module.combat;

import horus.client.MinecraftAccess;
import net.minecraft.entity.LivingEntity;

import java.security.SecureRandom;

/**
 * Neuro (Multi) rotation — ансамбль всех стратегий ротации.
 * Каждые 15-35 секунд (и при смене цели) Нейро переключается между движками
 * ReallyWorld / Mixer (MLSAC) / Sloth, как ансамбль нейросетей:
 * каждый движок имеет свой почерк обхода, а чередование почерков рвёт
 * накопление статистики у серверных анти-читов.
 * Между переключениями — короткая «задумчивость» (2-4 тика без доворота),
 * как у живого игрока, меняющего стиль наводки.
 */
public final class NeuroAuraRotation implements MinecraftAccess, AuraRotationStrategy {
   private static final SecureRandom RANDOM = new SecureRandom();
   private static final int MIN_SWITCH_TICKS = 300;
   private static final int MAX_SWITCH_TICKS = 700;

   private final AttackAuraModule aura;
   private final AuraRotationStrategy[] engines;
   private int currentEngine;
   private int switchTimer;
   private int hesitationTicks;
   private LivingEntity lastTarget;

   public NeuroAuraRotation(AttackAuraModule aura) {
      this.aura = aura;
      this.engines = new AuraRotationStrategy[]{
         new ReallyWorldAuraRotation(aura),
         new MixerAuraRotation(aura),
         new SlothAuraRotation(aura)
      };
      this.currentEngine = RANDOM.nextInt(this.engines.length);
      this.switchTimer = this.nextSwitchInterval();
   }

   @Override
   public void reset() {
      for (AuraRotationStrategy engine : this.engines) {
         engine.reset();
      }

      this.hesitationTicks = 0;
      this.lastTarget = null;
   }

   @Override
   public boolean isRotating(LivingEntity entity, float range) {
      if (entity == null || mc.player == null) {
         return false;
      }

      return this.engines[this.currentEngine].isRotating(entity, range);
   }

   @Override
   public void applyRotation(LivingEntity entity, float tickDelta) {
      this.engines[this.currentEngine].applyRotation(entity, tickDelta);
   }

   @Override
   public void update(LivingEntity entity, boolean canAttack, float range) {
      if (entity == null || mc.player == null || mc.world == null) {
         return;
      }

      // Смена цели — шанс сменить почерк наводки (как живой игрок, переключившийся на нового противника)
      if (this.lastTarget != null && entity != this.lastTarget && RANDOM.nextInt(3) == 0) {
         this.switchEngine(entity);
      }

      this.lastTarget = entity;

      if (this.hesitationTicks > 0) {
         this.hesitationTicks--;
         return;
      }

      if (--this.switchTimer <= 0) {
         this.switchEngine(entity);
         return;
      }

      this.engines[this.currentEngine].update(entity, canAttack, range);
   }

   private void switchEngine(LivingEntity entity) {
      int previous = this.currentEngine;

      // Всегда в другой движок — все стратегии работают по очереди
      do {
         this.currentEngine = RANDOM.nextInt(this.engines.length);
      } while (this.engines.length > 1 && this.currentEngine == previous);

      for (AuraRotationStrategy engine : this.engines) {
         engine.reset();
      }

      this.switchTimer = this.nextSwitchInterval();
      this.hesitationTicks = 2 + RANDOM.nextInt(3);
      this.lastTarget = entity;
   }

   private int nextSwitchInterval() {
      return MIN_SWITCH_TICKS + RANDOM.nextInt(MAX_SWITCH_TICKS - MIN_SWITCH_TICKS + 1);
   }
}

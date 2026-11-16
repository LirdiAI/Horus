package horus.client.module.combat;

import horus.client.event.EventListener;
import horus.client.event.PlayerTickEvent;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.BooleanSetting;
import horus.client.setting.ModeSetting;
import horus.client.setting.NumberSetting;
import horus.client.util.Stopwatch;
import net.minecraft.entity.LivingEntity;

@ModuleInfo(name = "TargetStrafe", description = "Кружит вокруг текущей цели киллауры.", category = ModuleCategory.COMBAT)
public final class TargetStrafeModule extends Module {
   private static TargetStrafeModule targetStrafeModule;
   public NumberSetting distanceSetting;
   public NumberSetting speedSetting;
   public ModeSetting directionSetting;
   public BooleanSetting autoJump;
   public BooleanSetting antiWall;
   public BooleanSetting onlyMoving;

   private double phase;
   private int currentDirection = 1;
   private final Stopwatch wallStopwatch = new Stopwatch();

   private final EventListener<PlayerTickEvent> playerTickListener = playerTickEvent -> {
      if (mc.player != null && mc.world != null) {
         AttackAuraModule aura = AttackAuraModule.getInstance();
         if (aura != null && aura.isEnabled()) {
            LivingEntity target = aura.getTarget();
            if (target != null && target.isAlive() && !target.isRemoved()) {
               if (this.onlyMoving.isChecked()
                  && !mc.options.forwardKey.isPressed()
                  && !mc.options.backKey.isPressed()
                  && !mc.options.leftKey.isPressed()
                  && !mc.options.rightKey.isPressed()) {
                  return;
               }

               if (this.antiWall.isChecked() && mc.player.horizontalCollision && this.wallStopwatch.hasExceededMillis(300L)) {
                  this.currentDirection = -this.currentDirection;
                  this.wallStopwatch.reset();
               }

               int dir = this.currentDirection;
               if (this.directionSetting.isSelected("По часовой")) {
                  dir = 1;
               } else if (this.directionSetting.isSelected("Против часовой")) {
                  dir = -1;
               }

               float speed = this.speedSetting.floatValue();
               this.phase += (speed * 0.2) * dir;
               if (this.phase > Math.PI * 2) {
                  this.phase -= Math.PI * 2;
               }
               if (this.phase < -Math.PI * 2) {
                  this.phase += Math.PI * 2;
               }

               double r = this.distanceSetting.floatValue();
               double targetX = target.getX() + Math.cos(this.phase) * r;
               double targetZ = target.getZ() + Math.sin(this.phase) * r;

               double deltaX = targetX - mc.player.getX();
               double deltaZ = targetZ - mc.player.getZ();
               double dist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

               if (dist > 0.05) {
                  double normX = deltaX / dist;
                  double normZ = deltaZ / dist;
                  double moveSpeed = Math.min(dist, speed);
                  mc.player.setVelocity(normX * moveSpeed, mc.player.getVelocity().y, normZ * moveSpeed);
               }

               if (this.autoJump.isChecked() && mc.player.isOnGround()) {
                  mc.player.jump();
               }
            }
         }
      }
   };

   private TargetStrafeModule() {
      this.distanceSetting = this.addSetting(
         new NumberSetting("Дистанция", "Радиус вращения вокруг цели.")
            .withRange(1.0F, 6.0F)
            .withStep(0.1F)
            .setValue(2.5F)
      );
      this.speedSetting = this.addSetting(
         new NumberSetting("Скорость", "Скорость перемещения по орбите.")
            .withRange(0.1F, 1.5F)
            .withStep(0.05F)
            .setValue(0.35F)
      );
      this.directionSetting = this.addSetting(
         new ModeSetting("Направление", "Направление вращения.", "Адаптивное", "По часовой", "Против часовой", "Адаптивное")
      );
      this.autoJump = this.addSetting(
         new BooleanSetting("Авто-прыжок", "Автоматически прыгает для нанесения критов и обхода замедления.", true)
      );
      this.antiWall = this.addSetting(
         new BooleanSetting("Обход стен", "Меняет направление при столкновении со стеной.", true)
      );
      this.onlyMoving = this.addSetting(
         new BooleanSetting("Только при движении", "Стрейфить только когда зажаты клавиши движения.", false)
      );
   }

   @Override
   protected void onDisable() {
      this.phase = 0.0;
   }

   public static TargetStrafeModule getInstance() {
      if (targetStrafeModule == null) {
         targetStrafeModule = new TargetStrafeModule();
      }
      return targetStrafeModule;
   }
}

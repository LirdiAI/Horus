package horus.client.module.movement;

import horus.client.event.EventListener;
import horus.client.event.PlayerTickEvent;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.BooleanSetting;
import horus.client.setting.LegacyNumberSetting;
import horus.client.util.ClientChat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

import java.util.function.Predicate;

@ModuleInfo(
   name = "ShulkerFly",
   description = "Полёт на шалкере в лодке: авто-посадка в лодку с шалкером и контроль левитации. Шалкер стреляет в тебя — ты паришь в лодке как на флае.",
   category = ModuleCategory.MOVEMENT
)
public final class ShulkerFlyModule extends Module {
   private static ShulkerFlyModule shulkerFlyModule;
   public BooleanSetting autoBoardSetting;
   public LegacyNumberSetting searchRangeSetting;

   private AbstractBoatEntity boat;
   private boolean flag;
   private boolean flag2;
   private boolean flag3;
   private int tickCounter;

   private final EventListener<PlayerTickEvent> playerTickListener = playerTickEvent -> {
      if (!this.isEnabled()) {
         return;
      }

      if (mc.player == null || mc.world == null) {
         return;
      }

      this.tickCounter++;

      boolean riding = mc.player.getVehicle() instanceof AbstractBoatEntity;
      boolean levitating = mc.player.hasStatusEffect(StatusEffects.LEVITATION);

      if (riding) {
         // Находимся в лодке — следим за левитацией
         if (levitating) {
            int secondsLeft = MathHelper.ceil(
               mc.player.getStatusEffect(StatusEffects.LEVITATION).getDuration() / 20.0F
            );

            if (secondsLeft <= 3 && !this.flag2) {
               this.flag2 = true;
               ClientChat.processData8("&eЛевитация заканчивается (&7" + secondsLeft + "с&e) — шалкер скоро выстрелит.");
            }

            this.flag3 = false;
         } else {
            this.flag2 = false;
            if (!this.flag3) {
               this.flag3 = true;
               ClientChat.processData8("&7Левитации нет — держись рядом с шалкером, он выстрелит и полёт продолжится.");
            }
         }

         return;
      }

      // Не в лодке — ищем лодку с шалкером
      if (this.tickCounter % 10 == 0) {
         this.boat = this.findShulkerBoat();
      }

      if (this.boat != null && this.boat.isRemoved()) {
         this.boat = null;
      }

      if (this.boat == null) {
         if (!this.flag) {
            this.flag = true;
            ClientChat.processData8("&7ShulkerFly: лодки с шалкером рядом нет. Нужна лодка, в которой сидит живой шалкер.");
         }

         return;
      }

      this.flag = false;
      double distance = MathHelper.sqrt((float) mc.player.squaredDistanceTo(this.boat));

      if (this.autoBoardSetting.isChecked() && distance <= 4.5 && this.tickCounter % 5 == 0) {
         mc.interactionManager.interactEntity(mc.player, this.boat, Hand.MAIN_HAND);
         ClientChat.processData8("&aСажусь в лодку с шалкером (&7" + String.format("%.1f", distance) + "м&a).");
      }
   };

   private AbstractBoatEntity findShulkerBoat() {
      double range = this.searchRangeSetting.getValue();
      Predicate<Entity> matcher = entity -> entity instanceof AbstractBoatEntity boatEntity
         && boatEntity.isAlive()
         && boatEntity.hasPassenger(passenger -> passenger instanceof ShulkerEntity);

      AbstractBoatEntity best = null;
      double bestDistance = Double.MAX_VALUE;

      for (Entity entity : mc.world.getEntities()) {
         if (matcher.test(entity)) {
            double distanceSq = mc.player.squaredDistanceTo(entity);
            if (distanceSq <= range * range && distanceSq < bestDistance) {
               best = (AbstractBoatEntity) entity;
               bestDistance = distanceSq;
            }
         }
      }

      return best;
   }

   public static ShulkerFlyModule getInstance() {
      if (shulkerFlyModule == null) {
         shulkerFlyModule = new ShulkerFlyModule();
      }

      return shulkerFlyModule;
   }

   private ShulkerFlyModule() {
      this.autoBoardSetting = this.addSetting(new BooleanSetting("Автопосадка", "Сам садиться в найденную лодку с шалкером.", true));
      this.searchRangeSetting = this.addSetting(new LegacyNumberSetting("Радиус поиска", "На каком расстоянии искать лодку с шалкером.", 32.0, 8.0, 64.0, 8.0));
   }
}

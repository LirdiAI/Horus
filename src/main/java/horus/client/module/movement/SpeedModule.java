package horus.client.module.movement;

import horus.client.event.EventListener;
import horus.client.event.PlayerTickEvent;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.BooleanSetting;
import horus.client.setting.ModeSetting;
import horus.client.setting.NumberSetting;
import horus.client.util.InventoryUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Speed", description = "Увеличивает скорость передвижения игрока.", category = ModuleCategory.MOVEMENT)
public final class SpeedModule extends Module {
   private static SpeedModule speedModule;
   public ModeSetting modeSetting;
   public NumberSetting speedMultiplier;
   public BooleanSetting autoJump;
   public NumberSetting rwBoost;
   public NumberSetting elytraSpeed;
   public BooleanSetting elytraLowHop;
   public BooleanSetting elytraAutoSwap;
   public NumberSetting grimDistance;
   public NumberSetting grimStrength;

   private int airTicks;

   private boolean isMoving() {
      return mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed()
         || mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed();
   }

   private boolean canSpeed() {
      return mc.player != null && mc.world != null
         && this.isMoving()
         && !mc.player.isSneaking()
         && !mc.player.isTouchingWater()
         && !mc.player.isInLava()
         && !mc.player.isClimbing()
         && !mc.player.getAbilities().flying
         && (this.modeSetting.isSelected("Elytra") || !mc.player.isGliding());
   }

   private final EventListener<PlayerTickEvent> playerTickListener = playerTickEvent -> {
      if (!this.canSpeed()) {
         this.airTicks = 0;
         return;
      }

      if (mc.player.isOnGround()) {
         this.airTicks = 0;
      } else {
         this.airTicks++;
      }

      if (this.modeSetting.isSelected("Vanilla")) {
         if (this.autoJump.isChecked() && mc.player.isOnGround()) {
            mc.player.jump();
         }
         Vec3d vel = mc.player.getVelocity();
         float factor = this.speedMultiplier.floatValue();
         mc.player.setVelocity(vel.x * factor, vel.y, vel.z * factor);
      } else if (this.modeSetting.isSelected("Bhop")) {
         if (mc.player.isOnGround()) {
            mc.player.jump();
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x * 1.35, 0.42, vel.z * 1.35);
         } else {
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x * 1.01, vel.y, vel.z * 1.01);
         }
      } else if (this.modeSetting.isSelected("ReallyWorld")) {
         if (mc.player.isOnGround()) {
            mc.player.jump();
            mc.player.setSprinting(true);
            double boost = this.rwBoost.floatValue();
            Vec3d look = Vec3d.fromPolar(0.0F, mc.player.getYaw()).multiply(boost);
            mc.player.addVelocity(look.x, 0.0, look.z);
         }
      } else if (this.modeSetting.isSelected("Elytra")) {
         boolean hasElytra = mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA);
         if (!hasElytra && this.elytraAutoSwap.isChecked()) {
            int elytraSlot = InventoryUtils.findInventorySlot(Items.ELYTRA);
            if (elytraSlot != -1 && mc.player.isOnGround()) {
               InventoryUtils.sendSlotClickPacket(InventoryUtils.getScreenSlotId(elytraSlot), 0, SlotActionType.QUICK_MOVE, false);
               hasElytra = true;
            }
         }

         if (hasElytra) {
            if (mc.player.isOnGround()) {
               if (this.autoJump.isChecked()) {
                  mc.player.jump();
               }
            } else {
               if (!mc.player.isGliding()) {
                  mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
               }

               Vec3d dir = Vec3d.fromPolar(0.0F, mc.player.getYaw()).multiply(this.elytraSpeed.floatValue());
               double yVel = mc.player.getVelocity().y;
               if (this.elytraLowHop.isChecked() && yVel > 0.05) {
                  yVel = -0.1;
               }
               mc.player.setVelocity(dir.x, yVel, dir.z);
            }
         }
      } else if (this.modeSetting.isSelected("Grim")) {
         if (this.autoJump.isChecked() && mc.player.isOnGround()) {
            mc.player.jump();
         }

         float expand = this.grimDistance.floatValue();
         Box box = mc.player.getBoundingBox().expand(expand);
         int collisions = 0;

         for (Entity entity : mc.world.getEntities()) {
            if (entity != mc.player
               && !(entity instanceof ArmorStandEntity)
               && (entity instanceof LivingEntity || entity instanceof AbstractBoatEntity)) {
               if (box.intersects(entity.getBoundingBox())) {
                  collisions++;
               }
            }
         }

         if (collisions > 0) {
            mc.player.setSprinting(true);
            double boost = this.grimStrength.floatValue() * Math.min(collisions, 3);
            Vec3d look = Vec3d.fromPolar(0.0F, mc.player.getYaw()).multiply(boost);
            mc.player.addVelocity(look.x, 0.0, look.z);
         }
      }
   };

   private SpeedModule() {
      this.modeSetting = this.addSetting(
         new ModeSetting("Режим", "Режим ускорения.", "Vanilla", "Vanilla", "Bhop", "ReallyWorld", "Elytra", "Grim")
      );
      this.speedMultiplier = this.addSetting(
         new NumberSetting("Скорость", "Множитель ванильной скорости.")
            .withRange(1.05F, 3.0F)
            .withStep(0.05F)
            .setValue(1.25F)
            .withVisibility(() -> this.modeSetting.isSelected("Vanilla"))
      );
      this.autoJump = this.addSetting(
         new BooleanSetting("Авто-прыжок", "Автоматически прыгать при беге.", true)
            .withVisibility(() -> this.modeSetting.isSelected("Vanilla") || this.modeSetting.isSelected("Elytra") || this.modeSetting.isSelected("Grim"))
      );
      this.rwBoost = this.addSetting(
         new NumberSetting("Буст RW", "Сила толчка на ReallyWorld.")
            .withRange(0.02F, 0.15F)
            .withStep(0.01F)
            .setValue(0.06F)
            .withVisibility(() -> this.modeSetting.isSelected("ReallyWorld"))
      );
      this.elytraSpeed = this.addSetting(
         new NumberSetting("Скорость элитры", "Горизонтальная скорость полёта на элитрах.")
            .withRange(0.2F, 2.5F)
            .withStep(0.05F)
            .setValue(0.85F)
            .withVisibility(() -> this.modeSetting.isSelected("Elytra"))
      );
      this.elytraLowHop = this.addSetting(
         new BooleanSetting("Низкий прыжок", "Быстрее приземляет для повторного толчка.", true)
            .withVisibility(() -> this.modeSetting.isSelected("Elytra"))
      );
      this.elytraAutoSwap = this.addSetting(
         new BooleanSetting("Авто-свап", "Автоматически надевает элитру из инвентаря.", true)
            .withVisibility(() -> this.modeSetting.isSelected("Elytra"))
      );
      this.grimDistance = this.addSetting(
         new NumberSetting("Дистанция Grim", "Радиус поиска сущностей для коллизии.")
            .withRange(0.1F, 1.5F)
            .withStep(0.05F)
            .setValue(0.5F)
            .withVisibility(() -> this.modeSetting.isSelected("Grim"))
      );
      this.grimStrength = this.addSetting(
         new NumberSetting("Сила Grim", "Сила ускорения за каждую найденную сущность.")
            .withRange(0.02F, 0.20F)
            .withStep(0.01F)
            .setValue(0.08F)
            .withVisibility(() -> this.modeSetting.isSelected("Grim"))
      );
   }

   @Override
   protected void onDisable() {
      this.airTicks = 0;
   }

   public static SpeedModule getInstance() {
      if (speedModule == null) {
         speedModule = new SpeedModule();
      }
      return speedModule;
   }
}

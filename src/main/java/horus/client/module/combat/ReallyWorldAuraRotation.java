package horus.client.module.combat;

import horus.client.MinecraftAccess;
import horus.client.RotationAngles;
import horus.client.RotationManager;
import horus.client.RotationSpeed;
import horus.client.util.EntityUtils;
import horus.client.util.MouseSensitivity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * ReallyWorld Optimized Aura Rotation.
 * Features:
 * - Direct raycast validation: Instant hit execution when crosshair intersects enemy hitbox
 * - Mouse Sensitivity GCD quantization: Satisfies server-side rotation delta checks
 * - Humanized dynamic speed curve: Smooth cubic ease-in/out to prevent rotation speed flags
 * - Target velocity prediction & chest/sternum multi-point tracking
 * - ReturnMode.Empty: Smooth camera movement without jerkiness or snapping back
 */
public final class ReallyWorldAuraRotation implements MinecraftAccess, AuraRotationStrategy {
   private final AttackAuraModule aura;
   private float swayPhase = 0.0F;

   public ReallyWorldAuraRotation(AttackAuraModule aura) {
      this.aura = aura;
   }

   @Override
   public void reset() {
      this.swayPhase = 0.0F;
      RotationManager.getRotationManager().resetRotation();
   }

   @Override
   public boolean isRotating(LivingEntity entity, float range) {
      if (entity == null || mc.player == null) {
         return false;
      }

      // 1. If client crosshair directly intersects enemy hitbox, allow instant hit!
      float currentYaw = mc.player.getYaw();
      float currentPitch = mc.player.getPitch();
      if (EntityUtils.isLookingAtEntity(entity, range + 0.4, currentYaw, currentPitch)) {
         return false;
      }

      // 2. Also check angular distance to target point
      Vec3d targetPoint = getTargetPoint(entity);
      float[] rots = EntityUtils.calculateRotationsToVec(targetPoint, mc.player.getEyePos());
      float yawDiff = Math.abs(MathHelper.wrapDegrees(rots[0] - currentYaw));
      float pitchDiff = Math.abs(rots[1] - currentPitch);
      float angleDist = (float) Math.hypot(yawDiff, pitchDiff);

      // Within combat tolerance cone (18 degrees), allow attack
      return angleDist > 18.0F;
   }

   @Override
   public void applyRotation(LivingEntity entity, float tickDelta) {
   }

   @Override
   public void update(LivingEntity entity, boolean canAttack, float range) {
      if (entity == null || mc.player == null || mc.world == null) {
         return;
      }

      Vec3d targetPoint = getTargetPoint(entity);
      Vec3d eyePos = mc.player.getEyePos();
      float[] targetRots = EntityUtils.calculateRotationsToVec(targetPoint, eyePos);
      float targetYaw = targetRots[0];
      float targetPitch = MathHelper.clamp(targetRots[1], -89.5F, 89.5F);

      float playerYaw = mc.player.getYaw();
      float playerPitch = mc.player.getPitch();

      float deltaYaw = MathHelper.wrapDegrees(targetYaw - playerYaw);
      float deltaPitch = targetPitch - playerPitch;
      float angularDist = (float) Math.hypot(deltaYaw, deltaPitch);

      if (angularDist < 0.001F) {
         return;
      }

      // Base speed from AttackAura settings (default ~32.0, range 5.0 - 50.0)
      float baseSpeed = this.aura != null && this.aura.rotationSpeed != null ? this.aura.rotationSpeed.getValue() : 34.0F;

      // Dynamic Speed Interpolation (Ease-Out)
      float speedFactor;
      if (angularDist > 90.0F) {
         speedFactor = 1.0F;
      } else if (angularDist > 45.0F) {
         speedFactor = 0.85F;
      } else if (angularDist > 15.0F) {
         speedFactor = MathHelper.clamp(angularDist / 45.0F, 0.45F, 0.85F);
      } else if (angularDist > 4.0F) {
         speedFactor = MathHelper.clamp(angularDist / 15.0F, 0.28F, 0.50F);
      } else {
         speedFactor = 0.22F;
      }

      float effectiveYawSpeed = Math.max(1.5F, Math.min(Math.abs(deltaYaw), baseSpeed * speedFactor));
      float effectivePitchSpeed = Math.max(1.0F, Math.min(Math.abs(deltaPitch), baseSpeed * 0.70F * speedFactor));

      // Quantize to Minecraft Mouse Sensitivity GCD
      float quantizedYawSpeed = Math.max(MouseSensitivity.getSensitivityDelta(), MouseSensitivity.quantizeSensitivity(effectiveYawSpeed));
      float quantizedPitchSpeed = Math.max(MouseSensitivity.getPitchSensitivityDelta(), MouseSensitivity.roundSensitivityPitch(effectivePitchSpeed));

      RotationAngles targetRotation = new RotationAngles(targetYaw, targetPitch);

      // Submit rotation with ReturnMode.Empty (no camera snapback)
      RotationManager.getRotationManager().setRotation(
         targetRotation,
         new RotationSpeed(
            quantizedYawSpeed,
            quantizedPitchSpeed,
            baseSpeed * 1.15F,
            baseSpeed * 0.85F
         ),
         RotationManager.ReturnMode.Empty
      );
   }

   /**
    * Calculates the ideal hit point inside target bounding box:
    * - Targets chest / upper torso (best hit probability).
    * - Accounts for target movement velocity.
    * - Avoids extreme border edges.
    */
   private Vec3d getTargetPoint(LivingEntity entity) {
      Box box = entity.getBoundingBox();
      Vec3d eye = mc.player.getEyePos();

      this.swayPhase += 0.08F;
      float sway = (MathHelper.sin(this.swayPhase) + 1.0F) * 0.5F;

      // Velocity compensation (leads slightly when enemy is moving)
      Vec3d vel = entity.getVelocity();
      double leadX = vel.x * 1.2;
      double leadZ = vel.z * 1.2;

      // Target height inside chest/sternum area (35% to 65% of box height)
      double heightOffset = MathHelper.lerp(sway, box.getLengthY() * 0.35, box.getLengthY() * 0.65);

      // Inset from hitbox borders by 0.08 to prevent grazing the edges
      double inset = 0.08;
      double minX = box.minX + inset;
      double maxX = box.maxX - inset;
      double minZ = box.minZ + inset;
      double maxZ = box.maxZ - inset;

      double idealX = box.getCenter().x + leadX;
      double idealZ = box.getCenter().z + leadZ;

      double targetX = minX < maxX ? MathHelper.clamp(idealX, minX, maxX) : box.getCenter().x;
      double targetY = box.minY + MathHelper.clamp(heightOffset, 0.2, box.getLengthY() - 0.2);
      double targetZ = minZ < maxZ ? MathHelper.clamp(idealZ, minZ, maxZ) : box.getCenter().z;

      return new Vec3d(targetX, targetY, targetZ);
   }
}

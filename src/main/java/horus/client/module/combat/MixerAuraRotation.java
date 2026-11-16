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
 * Mixer Rotation strategy tailored for MLSAC (Machine Learning Server Anti-Cheat).
 * Features:
 * - Dynamic hitbox multi-point mixing strictly inside target bounding box
 * - Natural ease-out velocity curve & mouse sensitivity GCD quantization
 * - Instant hit validation when crosshair intersects target hitbox
 * - ReturnMode.Empty to prevent camera fighting / snapping back
 */
public final class MixerAuraRotation implements MinecraftAccess, AuraRotationStrategy {
   private final AttackAuraModule aura;
   private float mixProgress = 0.0F;

   public MixerAuraRotation(AttackAuraModule aura) {
      this.aura = aura;
   }

   @Override
   public void reset() {
      this.mixProgress = 0.0F;
      RotationManager.getRotationManager().resetRotation();
   }

   @Override
   public boolean isRotating(LivingEntity entity, float range) {
      if (entity == null || mc.player == null) {
         return false;
      }

      // If player's crosshair is already looking directly at entity hitbox, NOT rotating (READY TO HIT!)
      if (EntityUtils.isLookingAtEntity(entity, range + 0.5, mc.player.getYaw(), mc.player.getPitch())) {
         return false;
      }

      // Check angular distance to target point
      Vec3d targetPoint = this.getMixedTargetPoint(entity);
      float[] rots = EntityUtils.calculateRotationsToVec(targetPoint, mc.player.getEyePos());
      float yawDiff = Math.abs(MathHelper.wrapDegrees(rots[0] - mc.player.getYaw()));
      float pitchDiff = Math.abs(rots[1] - mc.player.getPitch());
      float angularDistance = (float)Math.hypot(yawDiff, pitchDiff);

      // Within combat tolerance (12 degrees), allow attacks to register reliably
      return angularDistance > 12.0F;
   }

   @Override
   public void applyRotation(LivingEntity entity, float tickDelta) {
   }

   @Override
   public void update(LivingEntity entity, boolean canAttack, float range) {
      if (entity == null || mc.player == null || mc.world == null) {
         return;
      }

      // 1. Dynamic Mixed Hitbox Point strictly within bounding box
      Vec3d targetPoint = this.getMixedTargetPoint(entity);
      Vec3d eyePos = mc.player.getEyePos();
      float[] targetRots = EntityUtils.calculateRotationsToVec(targetPoint, eyePos);
      float targetYaw = targetRots[0];
      float targetPitch = targetRots[1];

      float playerYaw = mc.player.getYaw();
      float playerPitch = mc.player.getPitch();

      float deltaYaw = MathHelper.wrapDegrees(targetYaw - playerYaw);
      float deltaPitch = targetPitch - playerPitch;
      float angularDist = (float)Math.hypot(deltaYaw, deltaPitch);

      if (angularDist < 0.001F) {
         return;
      }

      // 2. Base speed from settings (default 32.0, range 5.0 - 50.0)
      float baseSpeed = this.aura != null && this.aura.rotationSpeed != null ? this.aura.rotationSpeed.getValue() : 35.0F;

      // 3. Humanized Dynamic Speed Curve:
      // When far away, accelerate smoothly; when close, decelerate smoothly to prevent overshooting
      float speedFactor;
      if (angularDist > 60.0F) {
         speedFactor = 1.0F;
      } else if (angularDist > 20.0F) {
         speedFactor = MathHelper.clamp(angularDist / 60.0F, 0.55F, 1.0F);
      } else if (angularDist > 5.0F) {
         speedFactor = MathHelper.clamp(angularDist / 20.0F, 0.35F, 0.70F);
      } else {
         speedFactor = 0.30F;
      }

      float effectiveYawSpeed = Math.max(2.0F, Math.min(Math.abs(deltaYaw), baseSpeed * speedFactor));
      float effectivePitchSpeed = Math.max(1.5F, Math.min(Math.abs(deltaPitch), baseSpeed * 0.75F * speedFactor));

      // 4. Quantize speeds to Minecraft sensitivity GCD (MLSAC bypass)
      float quantizedYawSpeed = Math.max(MouseSensitivity.getSensitivityDelta(), MouseSensitivity.quantizeSensitivity(effectiveYawSpeed));
      float quantizedPitchSpeed = Math.max(MouseSensitivity.getSensitivityDelta(), MouseSensitivity.quantizeSensitivity(effectivePitchSpeed));

      RotationAngles targetRotation = new RotationAngles(targetYaw, targetPitch);

      // 5. Submit to RotationManager with ReturnMode.Empty (prevents camera from snapping back!)
      RotationManager.getRotationManager().setRotation(
         targetRotation,
         new RotationSpeed(
            quantizedYawSpeed,
            quantizedPitchSpeed,
            baseSpeed * 1.2F,
            baseSpeed * 0.9F
         ),
         RotationManager.ReturnMode.Empty
      );
   }

   /**
    * Computes a mixed point strictly inside the target bounding box:
    * Blends smoothly between chest, abdomen, and head to defeat MLSAC static centroid detection.
    */
   private Vec3d getMixedTargetPoint(LivingEntity entity) {
      Box box = entity.getBoundingBox();
      Vec3d eye = mc.player.getEyePos();

      this.mixProgress += 0.05F;
      float sin = (MathHelper.sin(this.mixProgress) + 1.0F) * 0.5F; // 0.0 to 1.0

      // Mix between chest (0.35 box height) and upper torso / head (0.75 box height)
      double heightOffset = MathHelper.lerp(sin, box.getLengthY() * 0.35, box.getLengthY() * 0.75);

      // Clamp target coordinates strictly INSIDE entity bounding box (with 0.06 inset from edges)
      double minX = box.minX + 0.06;
      double maxX = box.maxX - 0.06;
      double minZ = box.minZ + 0.06;
      double maxZ = box.maxZ - 0.06;

      double targetX = minX < maxX ? MathHelper.clamp(eye.x, minX, maxX) : box.getCenter().x;
      double targetY = box.minY + MathHelper.clamp(heightOffset, 0.15, box.getLengthY() - 0.15);
      double targetZ = minZ < maxZ ? MathHelper.clamp(eye.z, minZ, maxZ) : box.getCenter().z;

      return new Vec3d(targetX, targetY, targetZ);
   }
}

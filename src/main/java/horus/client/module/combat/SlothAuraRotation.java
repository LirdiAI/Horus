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
 * Sloth Rotation strategy.
 * Specially designed for bypassing modern advanced heuristics (Sloth, Matrix, Grim, Vulcan):
 * - Direct Raycast Fast-Hit execution: allows instantaneous weapon strikes whenever the player crosshair intersects the target hitbox
 * - S-Curve Smootherstep & Non-linear humanized rotation speed curves
 * - Velocity lead prediction with dampening
 * - Continuous Lissajous / Sloth micro-sway to prevent static raycast / bone-lock flags
 * - Strict MouseSensitivity GCD quantization for 100% natural delta compliance
 * - ReturnMode.Empty to ensure fluid manual movement and zero snap-back
 */
public final class SlothAuraRotation implements MinecraftAccess, AuraRotationStrategy {
   private final AttackAuraModule aura;
   private float slothPhase = 0.0F;
   private float lastTargetYaw = 0.0F;
   private float lastTargetPitch = 0.0F;

   public SlothAuraRotation(AttackAuraModule aura) {
      this.aura = aura;
   }

   @Override
   public void reset() {
      this.slothPhase = 0.0F;
      this.lastTargetYaw = 0.0F;
      this.lastTargetPitch = 0.0F;
      RotationManager.getRotationManager().resetRotation();
   }

   @Override
   public boolean isRotating(LivingEntity entity, float range) {
      if (entity == null || mc.player == null) {
         return false;
      }

      // If player's crosshair is already looking directly at the entity hitbox, allow instant attack
      if (EntityUtils.isLookingAtEntity(entity, range + 0.45, mc.player.getYaw(), mc.player.getPitch())) {
         return false;
      }

      // Check angular distance to target point
      Vec3d targetPoint = this.getSlothTargetPoint(entity);
      float[] rots = EntityUtils.calculateRotationsToVec(targetPoint, mc.player.getEyePos());
      float yawDiff = Math.abs(MathHelper.wrapDegrees(rots[0] - mc.player.getYaw()));
      float pitchDiff = Math.abs(rots[1] - mc.player.getPitch());
      float angularDistance = (float)Math.hypot(yawDiff, pitchDiff);

      // Within combat registration tolerance, allow hits
      return angularDistance > 14.0F;
   }

   @Override
   public void applyRotation(LivingEntity entity, float tickDelta) {
      // Handled smoothly via RotationManager
   }

   @Override
   public void update(LivingEntity entity, boolean canAttack, float range) {
      if (entity == null || mc.player == null || mc.world == null) {
         return;
      }

      Vec3d targetPoint = this.getSlothTargetPoint(entity);
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

      // Configured rotation speed (default ~35.0, range 5.0 - 50.0)
      float baseSpeed = this.aura != null && this.aura.rotationSpeed != null ? this.aura.rotationSpeed.getValue() : 35.0F;

      // Sloth S-Curve Dynamic Scaling (Smootherstep: 6x^5 - 15x^4 + 10x^3)
      float normalizedDist = MathHelper.clamp(angularDist / 70.0F, 0.0F, 1.0F);
      float sCurve = normalizedDist * normalizedDist * normalizedDist * (normalizedDist * (normalizedDist * 6.0F - 15.0F) + 10.0F);
      float dynamicFactor = MathHelper.lerp(sCurve, 0.28F, 1.05F);

      float calculatedYawSpeed = Math.max(2.5F, Math.min(Math.abs(deltaYaw), baseSpeed * dynamicFactor));
      float calculatedPitchSpeed = Math.max(1.8F, Math.min(Math.abs(deltaPitch), baseSpeed * 0.75F * dynamicFactor));

      // Minecraft Mouse Sensitivity GCD Quantization (crucial for Sloth/Grim/Matrix bypass)
      float gcd = MouseSensitivity.getSensitivityDelta();
      float quantizedYawSpeed = Math.max(gcd, MouseSensitivity.quantizeSensitivity(calculatedYawSpeed));
      float quantizedPitchSpeed = Math.max(gcd, MouseSensitivity.quantizeSensitivity(calculatedPitchSpeed));

      RotationAngles targetRotation = new RotationAngles(targetYaw, targetPitch);

      RotationManager.getRotationManager().setRotation(
         targetRotation,
         new RotationSpeed(
            quantizedYawSpeed,
            quantizedPitchSpeed,
            baseSpeed * 1.25F,
            baseSpeed * 0.95F
         ),
         RotationManager.ReturnMode.Empty
      );

      this.lastTargetYaw = targetYaw;
      this.lastTargetPitch = targetPitch;
   }

   /**
    * Computes an optimized Sloth target point:
    * - Lead-prediction based on target momentum
    * - Lissajous micro-sway across chest/torso
    * - Strictly bounded inside the entity bounding box with safe insets
    */
   private Vec3d getSlothTargetPoint(LivingEntity entity) {
      Box box = entity.getBoundingBox();
      Vec3d eye = mc.player.getEyePos();

      this.slothPhase += 0.08F;

      // Lissajous curve micro-sway (torso/chest natural breathing & movement noise)
      float swayX = MathHelper.sin(this.slothPhase * 1.3F) * 0.08F;
      float swayY = MathHelper.cos(this.slothPhase * 0.9F) * 0.12F;
      float swayZ = MathHelper.sin(this.slothPhase * 1.1F) * 0.08F;

      // Predictive lead for moving targets
      Vec3d velocity = entity.getVelocity();
      double leadX = velocity.x * 0.85;
      double leadY = velocity.y * 0.65;
      double leadZ = velocity.z * 0.85;

      // Base target height: center around sternum / chest (55% of hitbox height)
      double baseHeight = box.getLengthY() * 0.55 + swayY + leadY;
      double targetY = box.minY + MathHelper.clamp(baseHeight, 0.20, box.getLengthY() - 0.20);

      // Horizontal target points clamped within bounding box
      double inset = 0.08;
      double minX = box.minX + inset;
      double maxX = box.maxX - inset;
      double minZ = box.minZ + inset;
      double maxZ = box.maxZ - inset;

      double targetX = minX < maxX ? MathHelper.clamp(eye.x + swayX + leadX, minX, maxX) : box.getCenter().x;
      double targetZ = minZ < maxZ ? MathHelper.clamp(eye.z + swayZ + leadZ, minZ, maxZ) : box.getCenter().z;

      return new Vec3d(targetX, targetY, targetZ);
   }
}

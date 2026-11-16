package horus.mixin;

import horus.client.module.render.ChamsModule;
import horus.client.MinecraftAccess;
import horus.client.render.EntityRenderRotations;
import horus.client.render.ChamsRenderStateAccess;
import horus.client.module.render.SeeInvisibleModule;
import ru.dreamix.fabricloader.VMBridge;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin implements MinecraftAccess {
   @Inject(
      method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
      at = @At("TAIL")
   )
   private void markChams(LivingEntity livingEntity, LivingEntityRenderState livingEntityRenderState, float local8305_3, CallbackInfo callbackInfo) {
      ((ChamsRenderStateAccess)livingEntityRenderState).horus$setChams(ChamsModule.getInstance().matchesCriteria(livingEntity));
   }



   @ModifyExpressionValue(
      method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F", ordinal = 0)
   )
   private float smoothHeadYaw(float local8307_1, LivingEntity livingEntity, LivingEntityRenderState livingEntityRenderState, float local8307_4) {
      return livingEntity == mc.player
         ? MathHelper.lerpAngleDegrees(local8307_4, EntityRenderRotations.previousHeadYaw, EntityRenderRotations.headYaw)
         : local8307_1;
   }

   @ModifyExpressionValue(
      method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getLerpedPitch(F)F")
   )
   private float smoothHeadPitch(float local8308_1, LivingEntity livingEntity, LivingEntityRenderState livingEntityRenderState, float local8308_4) {
      return livingEntity == mc.player ? MathHelper.lerp(local8308_4, EntityRenderRotations.previousPitch, EntityRenderRotations.pitch) : local8308_1;
   }

   @ModifyConstant(
      method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
      constant = @Constant(intValue = 654311423)
   )
   private int seeInvisibleOpacityTint(int local8309_1) {
      return !SeeInvisibleModule.getInstance().isEnabled()
         ? local8309_1
         : Math.max(1, (int)(SeeInvisibleModule.getInstance().legacyNumberSetting.getValue() * 255.0F)) << 24 | 16777215;
   }

   static {
      VMBridge.identifyClass(LivingEntityRendererMixin.class, "qXEUklpw");
   }
}

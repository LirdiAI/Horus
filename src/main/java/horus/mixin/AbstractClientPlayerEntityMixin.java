package horus.mixin;

import horus.client.MinecraftAccess;
import horus.client.module.render.NoRenderModule;
import horus.client.module.render.ZoomModule;
import horus.client.module.misc.YTMODEModule;
import ru.dreamix.fabricloader.VMBridge;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin implements MinecraftAccess {
   @Inject(method = "getFovMultiplier(ZF)F", at = @At("HEAD"), cancellable = true)
   private void overrideFovModifier(boolean local8060_1, float local8060_2, CallbackInfoReturnable<Float> local8060_3) {
      if (ZoomModule.getInstance().getFovMultiplier() > 0.0F) {
         local8060_3.setReturnValue(ZoomModule.getInstance().getFovMultiplier());
      } else if (NoRenderModule.getInstance().matchesCriteria2(" FOV")) {
         local8060_3.setReturnValue(1.0F);
      }
   }

   @Inject(method = "getSkin()Lnet/minecraft/entity/player/SkinTextures;", at = @At("HEAD"), cancellable = true)
   private void wearMySkinOnEveryone(CallbackInfoReturnable<SkinTextures> local8061_1) {
      if (YTMODEModule.getInstance().isEnabled() && mc.player != null && (Object)this != mc.player) {
         local8061_1.setReturnValue(mc.player.getSkin());
      }
   }

   static {
      VMBridge.identifyClass(AbstractClientPlayerEntityMixin.class, "NiqjLtOh");
   }
}

package horus.mixin;



import horus.client.module.render.ChamsModule;

import horus.client.module.render.TagsModule;



import net.minecraft.client.MinecraftClient;

import net.minecraft.client.network.AbstractClientPlayerEntity;

import net.minecraft.entity.Entity;

import net.minecraft.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.injection.At;

import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;



@Mixin(MinecraftClient.class)

public abstract class MinecraftClientMixin2 {

   @Inject(method = "hasOutline(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)

   private void forcePlayersGlowMask(Entity entity, CallbackInfoReturnable<Boolean> local8058_2) {

      if (ChamsModule.getInstance().isEnabled() && entity instanceof LivingEntity livingEntity && ChamsModule.getInstance().matchesCriteria(livingEntity)) {

         local8058_2.setReturnValue(true);

         return;

      }

      if (TagsModule.getInstance().checkState()) {

         local8058_2.setReturnValue(entity instanceof AbstractClientPlayerEntity abstractClientPlayerEntity && TagsModule.getInstance().matchesCriteria5(abstractClientPlayerEntity));

      }

   }

}


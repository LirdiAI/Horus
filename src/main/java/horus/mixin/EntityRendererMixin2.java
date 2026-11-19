package horus.mixin;

import horus.client.module.player.FrameSyncModule;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * FrameSync «Без теней»: не собираем теневые пятна под сущностями —
 * меньше отрисовки на земле и заметно больше FPS.
 */
@Mixin(EntityRenderer.class)
public class EntityRendererMixin2 {
   @Inject(
      method = "updateShadow(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/entity/state/EntityRenderState;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void skipEntityShadows(Entity entity, EntityRenderState entityRenderState, CallbackInfo callbackInfo) {
      if (FrameSyncModule.getInstance().shouldCullShadows()) {
         callbackInfo.cancel();
      }
   }
}

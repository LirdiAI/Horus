package horus.mixin;

import horus.client.render.WaveCapeRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeFeatureRenderer.class)
public abstract class CapeFeatureRendererMixin {
   @Inject(
      method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/PlayerEntityRenderState;FF)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void replaceWithWaveyCape(
      MatrixStack matrixStack,
      OrderedRenderCommandQueue orderedRenderCommandQueue,
      int light,
      PlayerEntityRenderState playerEntityRenderState,
      float yaw,
      float pitch,
      CallbackInfo callbackInfo
   ) {
      WaveCapeRenderer.render(matrixStack, orderedRenderCommandQueue, light, playerEntityRenderState, yaw, pitch);
      callbackInfo.cancel();
   }
}

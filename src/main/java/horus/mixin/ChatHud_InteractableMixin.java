package horus.mixin;

import horus.client.module.render.CustomChatModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.hud.ChatHud$Interactable")
public abstract class ChatHud_InteractableMixin {
   @Inject(method = "fill(IIIII)V", at = @At("HEAD"), cancellable = true, require = 0)
   private void cancelInteractableFill(int x1, int y1, int x2, int y2, int color, CallbackInfo ci) {
      if (CustomChatModule.getInstance().isEnabled()) {
         ci.cancel();
      }
   }
}

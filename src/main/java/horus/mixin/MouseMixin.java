package horus.mixin;

import horus.client.module.combat.ItemHelperModule;
import horus.client.module.player.ServerHelperModule;
import horus.client.module.render.ZoomModule;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
   @Inject(method = "onMouseButton(JLnet/minecraft/client/input/MouseInput;I)V", at = @At("HEAD"))
   private void onMouseButtonHook(long window, net.minecraft.client.input.MouseInput input, int action, CallbackInfo ci) {
      if (action == 0) {
         horus.client.module.render.CustomScoreboardModule.getInstance().resetDragState();
         horus.client.ui.hud.HudManager.processData5(0);
         horus.client.ui.hud.HudElementTypes.updateState2();
         horus.client.ui.hud.WatermarkHud.updateState();
      }
   }

   @Inject(method = "onMouseScroll(JDD)V", at = @At("HEAD"), cancellable = true)
   private void adjustZoomWithScroll(long window, double horizontal, double vertical, CallbackInfo callbackInfo) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client != null && client.currentScreen instanceof ChatScreen && client.getWindow() != null) {
         float scale = (float) client.getWindow().getScaledWidth() / (float) client.getWindow().getWidth();
         double mouseX = client.mouse.getX() * scale;
         double mouseY = client.mouse.getY() * scale;
         if (horus.client.ui.hud.HudManager.handleMouseScrolled(mouseX, mouseY, vertical)) {
            callbackInfo.cancel();
            return;
         }
      }

      if (ZoomModule.getInstance().adjustZoomScroll(vertical)) {
         callbackInfo.cancel();
      }
   }

   @Redirect(method = "onMouseScroll(JDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;setSelectedSlot(I)V"))
   private void skipBlockedScrollSlots(PlayerInventory playerInventory, int local8314_2) {
      playerInventory.setSelectedSlot(
         ItemHelperModule.getInstance().checkState2()
            ? playerInventory.getSelectedSlot()
            : ServerHelperModule.getInstance().computeInt2(playerInventory.getSelectedSlot(), local8314_2)
      );
   }
}

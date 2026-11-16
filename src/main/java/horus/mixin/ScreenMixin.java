package horus.mixin;

import horus.client.module.combat.ItemHelperModule;
import horus.client.module.render.ShulkerPreviewModule;
import horus.client.module.render.CustomScoreboardModule;
import horus.client.ui.FallenUiHelper;
import horus.client.ui.hud.HudElementTypes;
import horus.client.ui.hud.WatermarkHud;
import horus.client.ui.hud.HudManager;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class ScreenMixin {
   @Inject(method = "renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("HEAD"), cancellable = true)
   private void onRenderBackground(DrawContext drawContext, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (FallenUiHelper.isFallenScreen((Screen)(Object)this)) {
         FallenUiHelper.renderFallenBackground(drawContext, (Screen)(Object)this);
         ci.cancel();
      }
   }

   @Inject(method = "renderWithTooltip(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("TAIL"))
   private void renderShulkerToolip(DrawContext drawContext, int local8068_2, int local8068_3, float local8068_4, CallbackInfo callbackInfo) {
      if ((Object)this instanceof HandledScreen<?> local8068_6) {
         if (((HandledScreenMixin)local8068_6).hoveredSlot() != null && ((HandledScreenMixin)local8068_6).hoveredSlot().hasStack()) {
            ShulkerPreviewModule.getInstance().renderElement2(drawContext, ((HandledScreenMixin)local8068_6).hoveredSlot().getStack(), local8068_2, local8068_3);
         }
      }
   }

   @Inject(method = "mouseReleased(Lnet/minecraft/client/gui/Click;)Z", at = @At("HEAD"), require = 0)
   private void onScreenMouseReleased(Click click, CallbackInfoReturnable<Boolean> cir) {
      if (CustomScoreboardModule.getInstance().isEnabled()) {
         CustomScoreboardModule.getInstance().onMouseReleased(click.button());
      }
      HudElementTypes.updateState2();
      WatermarkHud.updateState();
      HudManager.processData5(click.button());
   }

}

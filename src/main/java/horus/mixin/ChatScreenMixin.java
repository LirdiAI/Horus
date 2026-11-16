package horus.mixin;

import horus.client.HorusClient;
import horus.client.ui.hud.HudElementTypes;
import horus.client.ui.hud.WatermarkHud;
import horus.client.ui.hud.HudManager;
import horus.client.module.render.CustomChatModule;
import horus.client.module.render.CustomScoreboardModule;
import ru.dreamix.fabricloader.VMBridge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
   @Shadow
   protected TextFieldWidget chatField;

   @Inject(method = "init()V", at = @At("TAIL"), require = 0)
   private void onChatInit(CallbackInfo ci) {
      if (CustomChatModule.getInstance().isEnabled()) {
         CustomChatModule.getInstance().resetAnimatedInput();
         this.chatField.setDrawsBackground(false);
         CustomChatModule.getInstance().updateChatFieldBounds(this.chatField);
      }
   }

   @Inject(method = "sendMessage(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
   private void dispatchClientCommand(String local8106_1, boolean local8106_2, CallbackInfo callbackInfo) {
      if (HorusClient.inst().commandDispatcher() != null && HorusClient.inst().commandDispatcher().matchesCriteria(local8106_1)) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.inGameHud != null && client.inGameHud.getChatHud() != null) {
            client.inGameHud.getChatHud().addToMessageHistory(local8106_1);
         }
         callbackInfo.cancel();
      }
   }

   @Inject(method = "mouseClicked(Lnet/minecraft/client/gui/Click;Z)Z", at = @At("HEAD"), cancellable = true)
   private void dragClick(Click click, boolean local8108_2, CallbackInfoReturnable<Boolean> local8108_3) {
      if (CustomChatModule.getInstance().isEnabled()) {
         if (CustomChatModule.getInstance().onMouseClicked(click.x(), click.y(), click.button(), text -> {
            if (this.chatField != null) {
               this.chatField.setText(text);
               this.chatField.setCursorToEnd(false);
               this.chatField.setFocused(true);
            }
         })) {
            if (this.chatField != null) {
               this.chatField.setFocused(true);
            }
            local8108_3.setReturnValue(true);
            return;
         }
      }
      if (CustomScoreboardModule.getInstance().isEnabled() && CustomScoreboardModule.getInstance().onMouseClicked(click.x(), click.y(), click.button())) {
         local8108_3.setReturnValue(true);
         return;
      }
      if (HudElementTypes.matchesCriteria(click.x(), click.y(), click.button())) {
         local8108_3.setReturnValue(true);
      } else if (WatermarkHud.matchesCriteria(click.x(), click.y(), click.button())) {
         local8108_3.setReturnValue(true);
      } else {
         if (HudManager.matchesCriteria3(click.x(), click.y(), click.button())) {
            local8108_3.setReturnValue(true);
         }
      }
   }

   @Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyInput;)Z", at = @At("HEAD"), cancellable = true)
   private void onChatKeyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
      if (CustomChatModule.getInstance().isEnabled()) {
         if (CustomChatModule.getInstance().handleKeyPressed(input.key(), input.modifiers())) {
            cir.setReturnValue(true);
         }
      }
   }

   @Inject(method = "removed()V", at = @At("HEAD"))
   private void dragRemoved(CallbackInfo callbackInfo) {
      CustomChatModule.getInstance().resetHoveredElements();
      CustomChatModule.getInstance().resetAnimatedInput();
      WatermarkHud.updateState();
      HudManager.updateState3();
      CustomScoreboardModule.getInstance().resetDragState();
   }

   @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("HEAD"))
   private void renderGlassInputFieldHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (CustomScoreboardModule.getInstance().isEnabled()) {
         CustomScoreboardModule.getInstance().updateMouseDrag(mouseX, mouseY);
      }
      if (CustomChatModule.getInstance().isEnabled() && this.chatField != null) {
         CustomChatModule.getInstance().updateChatFieldBounds(this.chatField);
         CustomChatModule.getInstance().renderInputFieldBackground(context, this.chatField);
      }
   }

   @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("TAIL"))
   private void renderGlassInputFieldTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (CustomChatModule.getInstance().isEnabled() && this.chatField != null) {
         int fieldX = this.chatField.getX() - 4;
         int fieldY = this.chatField.getY() - 3;
         TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

         CustomChatModule.getInstance().renderInputFieldForeground(
            context,
            textRenderer,
            this.chatField,
            mouseX,
            mouseY
         );
      }
   }

   @Redirect(
      method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"),
      require = 0
   )
   private void redirectInputFieldBackground(DrawContext context, int x1, int y1, int x2, int y2, int color) {
      if (CustomChatModule.getInstance().isEnabled()) {
         return;
      }
      context.fill(x1, y1, x2, y2, color);
   }

   static {
      VMBridge.identifyClass(ChatScreenMixin.class, "CVis2JDr");
   }
}

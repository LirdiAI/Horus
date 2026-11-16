package horus.mixin;

import horus.client.module.misc.AutoAcceptModule;
import horus.client.module.misc.AutoMineModule;
import horus.client.module.misc.AutoVillagerModule;
import horus.client.module.misc.AutoWardenModule;
import horus.client.module.render.CustomChatModule;
import horus.client.module.render.SmoothFeatureModule;
import horus.client.module.misc.StreamerModModule;
import horus.client.module.misc.YTMODEModule;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin2 {
   @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
   private void muteBaritoneChat(Text text, CallbackInfo callbackInfo) {
      if (AutoMineModule.getInstance().isEnabled() && AutoMineModule.getInstance().matchesCriteria5(text.getString())) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
   private void muteVillagerBaritoneChat(Text text, CallbackInfo callbackInfo) {
      if (AutoVillagerModule.checkState3() && AutoMineModule.getInstance().matchesCriteria5(text.getString())) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
   private void muteWardenBaritoneChat(Text text, CallbackInfo callbackInfo) {
      if (AutoWardenModule.checkState() && AutoMineModule.getInstance().matchesCriteria5(text.getString())) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
   private void muteServerGreetingChat(Text text, CallbackInfo callbackInfo) {
      if (YTMODEModule.getInstance().isEnabled() && YTMODEModule.getInstance().matchesCriteria2(text.getString())
         || StreamerModModule.getInstance().isEnabled() && StreamerModModule.getInstance().matchesCriteria2(text.getString())) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
   private void acceptChatInvites(Text text, CallbackInfo callbackInfo) {
      horus.client.module.render.TagsModule.recordChatPrefix(text.getString(), text);
      AutoAcceptModule.getInstance().processData(text.getString());
   }

   @org.spongepowered.asm.mixin.injection.ModifyVariable(
      method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
      at = @At("HEAD"),
      argsOnly = true
   )
   private Text colorizeChatLinks(Text text) {
      if (CustomChatModule.getInstance().isEnabled()) {
         return CustomChatModule.getInstance().formatIncomingMessage(text);
      }
      return text;
   }

   @Inject(
      method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void stackDuplicateChatSpam(Text text, MessageSignatureData messageSignatureData, MessageIndicator messageIndicator, CallbackInfo callbackInfo) {
      CustomChatModule.getInstance().trackIncomingTabMessage(text);
      if (CustomChatModule.getInstance().handleIncomingMessage((ChatHud)(Object)this, text)) {
         callbackInfo.cancel();
         return;
      }
      if (SmoothFeatureModule.getInstance().matchesCriteria((ChatHud)(Object)this, text)) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
      at = @At("TAIL")
   )
   private void slideChatOnNewLine(Text text, MessageSignatureData messageSignatureData, MessageIndicator messageIndicator, CallbackInfo callbackInfo) {
      CustomChatModule.getInstance().onNewMessageAdded();
      SmoothFeatureModule.getInstance().updateState();
   }

   @Inject(method = "addVisibleMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", at = @At("HEAD"), cancellable = true)
   private void filterChatTabVisibleMessage(net.minecraft.client.gui.hud.ChatHudLine message, CallbackInfo ci) {
      if (CustomChatModule.getInstance().isEnabled() && !CustomChatModule.getInstance().shouldDisplayInCurrentTab(message.content())) {
         ci.cancel();
      }
   }

   @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;IIIZZ)V", at = @At("HEAD"))
   private void beginChatSlideRender(
      DrawContext drawContext,
      TextRenderer textRenderer,
      int local8323_3,
      int local8323_4,
      int local8323_5,
      boolean local8323_6,
      boolean local8323_7,
      CallbackInfo callbackInfo
   ) {
      CustomChatModule.getInstance().renderGlassPanel((ChatHud)(Object)this, drawContext, local8323_3, local8323_6);
      CustomChatModule.getInstance().beginSmoothSlide(drawContext);
      SmoothFeatureModule.getInstance().renderElement4((ChatHud)(Object)this, drawContext);
   }

   @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;IIIZZ)V", at = @At("RETURN"))
   private void endChatSlideRender(
      DrawContext drawContext,
      TextRenderer textRenderer,
      int local8324_3,
      int local8324_4,
      int local8324_5,
      boolean local8324_6,
      boolean local8324_7,
      CallbackInfo callbackInfo
   ) {
      CustomChatModule.getInstance().endSmoothSlide(drawContext);
      SmoothFeatureModule.getInstance().renderElement3(drawContext);
   }
}

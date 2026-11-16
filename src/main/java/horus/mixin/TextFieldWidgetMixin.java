package horus.mixin;

import horus.client.module.render.CustomChatModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin {
   @Redirect(
      method = "renderWidget(Lnet/minecraft/client/gui/DrawContext;IIF)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"),
      require = 0
   )
   private void suppressCursorFill(DrawContext context, int x1, int y1, int x2, int y2, int color) {
      if (CustomChatModule.getInstance().isEnabled()
            && CustomChatModule.getInstance().animatedInput.isChecked()
            && MinecraftClient.getInstance().currentScreen instanceof ChatScreen) {
         return;
      }
      context.fill(x1, y1, x2, y2, color);
   }

   @Redirect(
      method = "renderWidget(Lnet/minecraft/client/gui/DrawContext;IIF)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)V"),
      require = 0
   )
   private void suppressCursorUnderscore(DrawContext context, TextRenderer textRenderer, String text, int x, int y, int color, boolean shadow) {
      if (CustomChatModule.getInstance().isEnabled()
            && CustomChatModule.getInstance().animatedInput.isChecked()
            && MinecraftClient.getInstance().currentScreen instanceof ChatScreen
            && "_".equals(text)) {
         return;
      }
      context.drawText(textRenderer, text, x, y, color, shadow);
   }
}

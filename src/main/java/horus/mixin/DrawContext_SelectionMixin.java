package horus.mixin;

import horus.client.module.render.CustomChatModule;
import horus.client.ui.ClickGuiStyle;
import horus.client.util.Colors;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContext_SelectionMixin {
   @Inject(method = "drawSelection", at = @At("HEAD"), cancellable = true)
   private void onDrawSelection(int x1, int y1, int x2, int y2, boolean invert, CallbackInfo ci) {
      int accent = CustomChatModule.getInstance().isEnabled()
         ? CustomChatModule.getInstance().getAccentColor()
         : ClickGuiStyle.intVal;
      int selectBg = Colors.computeInt7(accent, 120);
      int selectBorder = Colors.computeInt7(accent, 240);
      int minX = Math.min(x1, x2);
      int maxX = Math.max(x1, x2);
      int minY = Math.min(y1, y2);
      int maxY = Math.max(y1, y2);
      if (minX < maxX && minY < maxY) {
         DrawContext ctx = (DrawContext)(Object)this;
         ctx.fill(minX, minY, maxX, maxY, selectBg);
         ctx.fill(minX, minY, maxX, minY + 1, selectBorder);
         ctx.fill(minX, maxY - 1, maxX, maxY, selectBorder);
         ctx.fill(minX, minY, minX + 1, maxY, selectBorder);
         ctx.fill(maxX - 1, minY, maxX, maxY, selectBorder);
      }
      ci.cancel();
   }
}

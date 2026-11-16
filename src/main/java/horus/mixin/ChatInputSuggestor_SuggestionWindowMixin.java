package horus.mixin;

import horus.client.module.render.CustomChatModule;
import com.mojang.brigadier.suggestion.Suggestion;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.util.math.Rect2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatInputSuggestor.SuggestionWindow.class)
public abstract class ChatInputSuggestor_SuggestionWindowMixin {
   @Shadow
   private Rect2i area;

   @Shadow
   private String typedText;

   @Shadow
   private List<Suggestion> suggestions;

   @Shadow
   private int inWindowIndex;

   @Shadow
   private int selection;

   @Shadow
   public abstract void select(int index);

   @Shadow
   public abstract void complete();

   @Inject(method = "render", at = @At("HEAD"), cancellable = true)
   private void onRenderSuggestions(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
      if (CustomChatModule.getInstance().isEnabled()) {
         CustomChatModule.getInstance().renderSuggestionWindow(
            context,
            this.area,
            this.typedText,
            this.suggestions,
            this.inWindowIndex,
            this.selection,
            mouseX,
            mouseY,
            this::select
         );
         ci.cancel();
      }
   }

   @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
   private void onMouseClickedSuggestions(int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
      if (CustomChatModule.getInstance().isEnabled()) {
         if (CustomChatModule.getInstance().onSuggestionClicked(this.area, this.suggestions, this.inWindowIndex, mouseX, mouseY, idx -> {
            this.select(idx);
            this.complete();
         })) {
            cir.setReturnValue(true);
         }
      }
   }
}

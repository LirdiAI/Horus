package horus.mixin;

import horus.client.render.ChamsRenderStateAccess;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements ChamsRenderStateAccess {
   @Unique
   private boolean chams;

   @Override
   public boolean horus$chams() {
      return this.chams;
   }

   @Override
   public void horus$setChams(boolean newChams) {
      this.chams = newChams;
   }
}

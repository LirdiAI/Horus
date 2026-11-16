package horus.client.render;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public final class WaveCapeRenderer {
   public static class WaveCapeModel extends Model<PlayerEntityRenderState> {
      private final ModelPart[] segments = new ModelPart[16];

      public WaveCapeModel(ModelPart root) {
         super(root, RenderLayers::entitySolid);
         ModelPart cur = root.getChild("segment0");
         this.segments[0] = cur;
         for (int i = 1; i < 16; i++) {
            cur = cur.getChild("segment" + i);
            this.segments[i] = cur;
         }
      }

      public static TexturedModelData getTexturedModelData() {
         ModelData modelData = new ModelData();
         ModelPartData modelPartData = modelData.getRoot();

         // Segment 0 at shoulders
         ModelPartData parent = modelPartData.addChild(
            "segment0",
            ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, 0.0F, -1.0F, 10.0F, 1.0F, 1.0F, Dilation.NONE, 1.0F, 0.5F),
            ModelTransform.of(0.0F, 0.0F, 2.0F, 0.0F, 3.1415927F, 0.0F)
         );

         for (int i = 1; i < 16; i++) {
            parent = parent.addChild(
               "segment" + i,
               ModelPartBuilder.create().uv(0, i).cuboid(-5.0F, 0.0F, -1.0F, 10.0F, 1.0F, 1.0F, Dilation.NONE, 1.0F, 0.5F),
               ModelTransform.origin(0.0F, 1.0F, 0.0F)
            );
         }

         return TexturedModelData.of(modelData, 64, 64);
      }

      @Override
      public void setAngles(PlayerEntityRenderState state) {
         super.setAngles(state);

         // Base tilt from pitch and torso lean
         float basePitch = (6.0F + state.field_53536) * 0.017453292F;
         // Sideways sway from body roll / turn
         float rollAngle = (state.field_53538 / 2.0F) * 0.017453292F;
         // Running / movement speed
         float speed = Math.max(0.0F, state.field_53537);

         // Dynamic waving parameters: ripples flow downward from shoulders
         float waveFreq = 0.24F;
         float waveAmp = (speed > 3.0F ? (0.042F + speed * 0.0028F) : 0.012F);
         float sneakAngle = state.isInSneakingPose ? 0.024F : 0.0F;

         for (int i = 0; i < 16; i++) {
            ModelPart part = this.segments[i];
            float progress = (float) i / 15.0F;

            // Velocity bend curvature across the length of cloth
            float velBend = (speed / 36.0F) * 0.017453292F;
            // Realistic sine wave ripple propagating from segment to segment
            float ripple = (float) Math.sin((state.age * waveFreq) - (i * 0.36F)) * waveAmp;

            part.pitch = (i == 0 ? basePitch : 0.0F) + velBend + ripple + sneakAngle;
            part.roll = (i == 0 ? rollAngle * 0.45F : rollAngle * 0.045F);

            if (i == 0) {
               part.yaw = 3.1415927F - (state.field_53538 / 3.0F) * 0.017453292F;
            } else {
               part.yaw = 0.0F;
            }
         }
      }
   }

   private static final WaveCapeModel MODEL;

   static {
      TexturedModelData tmd = WaveCapeModel.getTexturedModelData();
      MODEL = new WaveCapeModel(tmd.createModel());
   }

   public static void render(
      MatrixStack matrixStack,
      OrderedRenderCommandQueue orderedRenderCommandQueue,
      int light,
      PlayerEntityRenderState playerEntityRenderState,
      float yaw,
      float pitch
   ) {
      if (playerEntityRenderState.invisible || !playerEntityRenderState.capeVisible) {
         return;
      }
      if (playerEntityRenderState.skinTextures == null || playerEntityRenderState.skinTextures.cape() == null) {
         return;
      }

      ItemStack chest = playerEntityRenderState.equippedChestStack;
      if (chest != null && !chest.isEmpty() && chest.isOf(Items.ELYTRA)) {
         return;
      }

      matrixStack.push();

      if (chest != null && !chest.isEmpty()) {
         matrixStack.translate(0.0F, -0.053125F, 0.06875F);
      }

      Identifier capeTexture = playerEntityRenderState.skinTextures.cape().texturePath();
      orderedRenderCommandQueue.submitModel(
         MODEL,
         playerEntityRenderState,
         matrixStack,
         RenderLayers.entitySolid(capeTexture),
         light,
         OverlayTexture.DEFAULT_UV,
         playerEntityRenderState.outlineColor,
         null
      );

      matrixStack.pop();
   }
}

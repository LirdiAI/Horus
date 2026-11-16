package horus.client.module.render;

import horus.client.module.combat.AimAssistModule;
import horus.client.module.combat.AttackAuraModule;
import horus.client.setting.BooleanSetting;
import horus.client.util.Colors;
import horus.client.HorusClient;
import horus.client.util.DepthSnapshot;
import horus.client.util.Easings;
import horus.client.event.EventListener;
import horus.client.setting.ModeSetting;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.render.WorldProjection;
import horus.client.util.EntityUtils;
import horus.client.ui.UiTheme;
import horus.client.util.RaycastUtils;
import horus.client.ui.AnimatedValue;
import horus.client.render.ThemeColors;
import horus.client.module.combat.TriggerBotModule;
import horus.client.event.WorldRenderEvent;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.BufferAllocator.CloseableBuffer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

@ModuleInfo(name = "TargetEsp", description = "Подсвечивает цель AttackAura, AimAssist или TrigerBot призраками и кольцом.", category = ModuleCategory.RENDER)
public final class TargetEspModule extends Module {
   private static TargetEspModule targetEspModule;
   private static final RenderPipeline pipeline2 = RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
      .withLocation(HorusClient.id("pipeline/target_ghosts"))
      .withVertexShader(Identifier.ofVanilla("core/position_tex_color"))
      .withFragmentShader(Identifier.ofVanilla("core/position_tex_color"))
      .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, DrawMode.QUADS)
      .withBlend(BlendFunction.LIGHTNING)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withDepthWrite(false)
      .withCull(false)
      .withSampler("Sampler0")
      .build();
   private static final RenderPipeline pipeline3 = RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
      .withLocation(HorusClient.id("pipeline/target_ghosts_depth"))
      .withVertexShader(Identifier.ofVanilla("core/position_tex_color"))
      .withFragmentShader(Identifier.ofVanilla("core/position_tex_color"))
      .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, DrawMode.QUADS)
      .withBlend(BlendFunction.LIGHTNING)
      .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
      .withDepthWrite(false)
      .withCull(false)
      .withSampler("Sampler0")
      .build();
   private static final RenderPipeline pipeline4 = RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
      .withLocation(HorusClient.id("pipeline/target_crystals"))
      .withVertexShader(Identifier.ofVanilla("core/position_color"))
      .withFragmentShader(Identifier.ofVanilla("core/position_color"))
      .withVertexFormat(VertexFormats.POSITION_COLOR, DrawMode.QUADS)
      .withBlend(BlendFunction.LIGHTNING)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withDepthWrite(false)
      .withCull(false)
      .build();
   private static final RenderPipeline pipeline5 = RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
      .withLocation(HorusClient.id("pipeline/target_crystals_depth"))
      .withVertexShader(Identifier.ofVanilla("core/position_color"))
      .withFragmentShader(Identifier.ofVanilla("core/position_color"))
      .withVertexFormat(VertexFormats.POSITION_COLOR, DrawMode.QUADS)
      .withBlend(BlendFunction.LIGHTNING)
      .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
      .withDepthWrite(false)
      .withCull(false)
      .build();
   private static final RenderPipeline pipeline6 = RenderPipeline.builder()
      .withLocation(HorusClient.id("pipeline/target_scan"))
      .withVertexShader(HorusClient.id("core/scan"))
      .withFragmentShader(HorusClient.id("core/scan"))
      .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, DrawMode.QUADS)
      .withBlend(BlendFunction.LIGHTNING)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withDepthWrite(false)
      .withCull(false)
      .withSampler("Sampler0")
      .withUniform("ScanParams", UniformType.UNIFORM_BUFFER)
      .build();
   private static final Vector4f vector4f = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f vector3f2 = new Vector3f();
   private static final Matrix4f matrix4f = new Matrix4f();
   private static final Vector3f[] vector3fArray = new Vector3f[]{
      new Vector3f(0.0F, 1.5F, 0.0F),
      new Vector3f(0.0F, -1.5F, 0.0F),
      new Vector3f(1.0F, 0.0F, 0.0F),
      new Vector3f(-1.0F, 0.0F, 0.0F),
      new Vector3f(0.0F, 0.0F, 1.0F),
      new Vector3f(0.0F, 0.0F, -1.0F)
   };
   private static final int[][] intValArray = new int[][]{{0, 2, 4}, {0, 4, 3}, {0, 3, 5}, {0, 5, 2}, {1, 4, 2}, {1, 3, 4}, {1, 5, 3}, {1, 2, 5}};
   private static final float[] floatValArray = new float[]{1.0F, 0.8F, 0.6F, 0.9F, 0.7F, 0.5F, 0.4F, 0.6F};
   public ModeSetting mode;
   public ModeSetting ghostStyle;
   public ModeSetting circleStyle;
   public BooleanSetting trident;
   public BooleanSetting projectiles;
   private final AnimatedValue animation = new AnimatedValue(350L);
   private LivingEntity livingEntity2;
   private float floatVal;
   private long timeMillis = System.currentTimeMillis();
   private float floatVal2;
   private float floatVal3;
   private long timeMillis2 = System.currentTimeMillis();
   private float floatVal4;
   private LivingEntity livingEntity3;
   private long timeMillis3;
   private long timeMillis4;
   private float[] floatValArray2 = new float[192];
   private float[] floatValArray3 = new float[192];
   private float[] floatValArray4 = new float[64];
   private int intVal;
   private ArrayList<float[]> list = new ArrayList<>();
   private BufferBuilder bufferBuilder5;
   private BufferBuilder bufferBuilder6;
   private Quaternionf quaternionf2;
   private Vector3f vector3f3 = new Vector3f();
   private float floatVal5;
   private float floatVal6;
   private float floatVal7;
   private int intVal2;
   private int intVal3;
   private final HashMap<Integer, float[]> map = new HashMap<>();
   private float floatVal8;
   private float floatVal9;
   private long timeMillis5;
   private final HashMap<Integer, Vec3d> map2 = new HashMap<>();
   private final AnimatedValue animation2 = new AnimatedValue(400L);
   private float floatVal10;
   private long timeMillis6;
   private final float[] floatValArray5 = new float[3];
   private long timeMillis7 = System.currentTimeMillis();
   private GpuBuffer gpuBuffer2;
   private final GpuBuffer[] gpuBufferArray = new GpuBuffer[8];
   private int intVal4;
   private final BufferAllocator[] bufferAllocatorArray = new BufferAllocator[2];
   private final HashMap<String, Identifier> map3 = new HashMap<>();
   private GpuBuffer gpuBuffer3;
   private boolean flag;
   private float floatVal11;
   private float floatVal12;
   private float floatVal13;
   private final Matrix4f matrix4f2 = new Matrix4f();
   private GpuBufferSlice gpuBufferSlice2;
   private ProjectionType projectionType;
   private final EventListener<WorldRenderEvent> worldRenderListener = worldRenderEvent -> {
      if (mc.player != null && mc.world != null) {
         this.intVal4 = 0;
         if (this.mode.isSelected("Цепи") && this.trident.isChecked()) {
            this.renderElement3(worldRenderEvent.getMatrixStack(), worldRenderEvent.computeFloat());
         }

         if (this.mode.isSelected("Молнии") && this.projectiles.isChecked()) {
            this.renderElement12(worldRenderEvent.getMatrixStack(), worldRenderEvent.computeFloat());
         }

         LivingEntity livingEntity = AttackAuraModule.getInstance().isEnabled() && AttackAuraModule.getInstance().getTarget() != null
            ? AttackAuraModule.getInstance().getTarget()
            : (
               AimAssistModule.getInstance().isEnabled() && AimAssistModule.getInstance().getTarget() != null
                  ? AimAssistModule.getInstance().getTarget()
                  : (TriggerBotModule.getInstance().isEnabled() ? TriggerBotModule.getInstance().getTarget() : null)
            );
         if (livingEntity != null && livingEntity != this.livingEntity2) {
            this.livingEntity2 = livingEntity;
            this.animation.getAnimatedValue(0.0);
            this.floatVal9 = 0.0F;
            this.timeMillis5 = System.currentTimeMillis();
            this.animation2.getAnimatedValue(0.0);
            this.timeMillis6 = System.currentTimeMillis();
         }

         this.animation.processData(livingEntity != null ? 1.0 : 0.0);
         if (this.livingEntity2 != null) {
            if (this.livingEntity2.isRemoved()) {
               this.livingEntity2 = null;
            } else {
               float local2980_3 = (float)this.animation.computeDouble();
               if (local2980_3 < 0.003921569F) {
                  if (livingEntity == null) {
                     this.livingEntity2 = null;
                  }

                  this.floatVal = 0.0F;
               } else {
                  float local2980_4 = this.livingEntity2.hurtTime / 10.0F;
                  if (this.mode.isSelected("Призраки")) {
                     if (TagsModule.getInstance().checkState()) {
                        this.flag = true;
                        this.floatVal11 = local2980_3;
                        this.floatVal12 = local2980_4;
                        this.floatVal13 = worldRenderEvent.computeFloat();
                        this.matrix4f2.set(worldRenderEvent.getMatrixStack().peek().getPositionMatrix());
                        this.gpuBufferSlice2 = RenderSystem.getProjectionMatrixBuffer();
                        this.projectionType = RenderSystem.getProjectionType();
                     } else if (this.ghostStyle.isSelected("Второй")) {
                        this.renderElement5(worldRenderEvent.getMatrixStack(), this.livingEntity2, worldRenderEvent.computeFloat(), local2980_3, local2980_4);
                     } else {
                        this.renderElement(worldRenderEvent.getMatrixStack(), this.livingEntity2, worldRenderEvent.computeFloat(), local2980_3, local2980_4);
                     }
                  }

                  if (this.mode.isSelected("Молнии")) {
                     this.renderElement4(worldRenderEvent.getMatrixStack(), this.livingEntity2, worldRenderEvent.computeFloat(), local2980_3, local2980_4);
                  }

                  if (this.mode.isSelected("Круг")) {
                     this.renderElement8(worldRenderEvent.getMatrixStack(), this.livingEntity2, worldRenderEvent.computeFloat(), local2980_3, local2980_4);
                  }

                  if (this.mode.isSelected("Кристаллы")) {
                     this.renderElement7(worldRenderEvent.getMatrixStack(), this.livingEntity2, worldRenderEvent.computeFloat(), local2980_3, local2980_4);
                  }

                  if (this.mode.isSelected("Ромб")) {
                     this.renderElement10(worldRenderEvent.getMatrixStack(), this.livingEntity2, worldRenderEvent.computeFloat(), local2980_3, local2980_4);
                  }

                  if (this.mode.isSelected("Цепи")) {
                     this.renderElement2(worldRenderEvent.getMatrixStack(), this.livingEntity2, worldRenderEvent.computeFloat(), local2980_3, local2980_4);
                  }

                  if (this.mode.isSelected("Череп")) {
                     this.renderElement11(worldRenderEvent.getMatrixStack(), this.livingEntity2, worldRenderEvent.computeFloat(), local2980_3, local2980_4);
                  }
               }
            }
         }
      }
   };

   private BufferAllocator getBufferAllocator(int local2977_1, int local2977_2) {
      BufferAllocator bufferAllocator = this.bufferAllocatorArray[local2977_1];
      if (bufferAllocator == null) {
         bufferAllocator = this.bufferAllocatorArray[local2977_1] = new BufferAllocator(local2977_2);
      }

      CloseableBuffer allocated = bufferAllocator.getAllocated();
      if (allocated != null) {
         allocated.close();
      }

      return bufferAllocator;
   }

   private void renderElement(MatrixStack matrixStack, LivingEntity entity, float local2978_3, float local2978_4, float local2978_5) {
      if (mc.getTextureManager().getTexture(Identifier.of("horus", "textures/particles/bloom.png")).getGlTextureView() != null
         && mc.getFramebuffer() != null
         && mc.getFramebuffer().getColorAttachmentView() != null) {
         Quaternionf rotation = mc.gameRenderer.getCamera().getRotation();
         double local2978_7 = MathHelper.lerp(local2978_3, entity.lastRenderX, entity.getX()) - mc.gameRenderer.getCamera().getCameraPos().x;
         double local2978_9 = MathHelper.lerp(local2978_3, entity.lastRenderY, entity.getY()) - mc.gameRenderer.getCamera().getCameraPos().y;
         double local2978_11 = MathHelper.lerp(local2978_3, entity.lastRenderZ, entity.getZ()) - mc.gameRenderer.getCamera().getCameraPos().z;
         this.floatVal9 = this.floatVal9 + 3.5F * (float)(System.currentTimeMillis() - this.timeMillis5) / 1000.0F;
         this.timeMillis5 = System.currentTimeMillis();
         int local2978_13 = 4;
         int local2978_14 = 15;
         float local2978_15 = 0.68F;
         float local2978_16 = 1.05F;
         float local2978_17 = 0.24F;
         float local2978_18 = 0.12F;
         float local2978_19 = 0.45F;
         float local2978_20 = -0.02F;
         float local2978_21 = 0.12F;
         float local2978_22 = 0.02F;
         float local2978_23 = 0.05F;
         float local2978_24 = (float)(0.5 * Math.asin(local2978_23 / 0.5F));
         int local2978_25 = 0;
         BufferAllocator bufferAllocator = this.getBufferAllocator(1, local2978_13 * local2978_14 * 4 * VertexFormats.POSITION_TEXTURE_COLOR.getVertexSize());
         BufferBuilder bufferBuilder = new BufferBuilder(bufferAllocator, DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         Vector3f vector3f = new Vector3f();

         for (int index = 0; index < local2978_13; index++) {
            float local2978_30 = index < 2 ? 1.0F : -1.0F;
            float local2978_31 = index % 2 == 0 ? -1.0F : 1.0F;
            float local2978_32 = index % 2 == 0 ? local2978_20 : -local2978_20;
            float local2978_33 = index % 2 == 0 ? local2978_19 : -local2978_19;

            for (int index2 = 0; index2 < local2978_14; index2++) {
               int local2978_35 = index % 2 == 0 ? index2 : local2978_14 - 1 - index2;
               float local2978_36 = this.floatVal9 + local2978_35 * local2978_24;
               float local2978_37 = 5.0F * MathHelper.lerp((float)local2978_35 / local2978_14, local2978_22, local2978_23);
               this.processData3(
                  bufferBuilder,
                  rotation,
                  vector3f,
                  local2978_7 + local2978_30 * (local2978_15 * MathHelper.sin(local2978_36 + local2978_21) + local2978_32),
                  local2978_9 + local2978_16 + local2978_17 * MathHelper.sin(this.floatVal9 + local2978_35 * local2978_18) + local2978_33,
                  local2978_11 + local2978_30 * local2978_31 * local2978_15 * MathHelper.cos(local2978_36 - local2978_21),
                  local2978_37,
                  0.0F,
                  Colors.computeInt6(this.computeInt3(index * 90, local2978_5), local2978_4)
               );
               local2978_25++;
            }
         }

         try (BuiltBuffer builtBuffer = bufferBuilder.end()) {
            this.renderElement9(
               matrixStack, "textures/particles/bloom.png", builtBuffer, local2978_25, RaycastUtils.matchesCriteria(mc.player, entity), false
            );
         }
      }
   }

   private float computeFloat(float local2979_1, float local2979_2, float local2979_3) {
      return MathHelper.clamp((local2979_3 - local2979_1) / (local2979_2 - local2979_1), 0.0F, 1.0F)
         * MathHelper.clamp((local2979_3 - local2979_1) / (local2979_2 - local2979_1), 0.0F, 1.0F)
         * (3.0F - 2.0F * MathHelper.clamp((local2979_3 - local2979_1) / (local2979_2 - local2979_1), 0.0F, 1.0F));
   }

   private void renderElement2(MatrixStack matrixStack, LivingEntity entity, float local2982_3, float local2982_4, float local2982_5) {
      this.floatVal10 = (this.floatVal10 + 75.0F * (float)(System.currentTimeMillis() - this.timeMillis6) / 1000.0F) % 720.0F;
      this.timeMillis6 = System.currentTimeMillis();
      this.animation2.processData(local2982_4 >= 0.95F ? 1.0 : 0.0);
      float local2982_6 = 20.0F * Math.min(1.0F + MathHelper.sin((float)Math.toRadians(this.floatVal10)), 1.0F);
      float local2982_7 = 20.0F * MathHelper.sin((float)Math.toRadians(this.floatVal10));
      float local2982_8 = entity.getWidth() * 1.5F;
      float local2982_9 = 1.0F;
      float local2982_10 = 22.5F;
      double local2982_11 = MathHelper.lerp(local2982_3, entity.lastRenderX, entity.getX()) - mc.gameRenderer.getCamera().getCameraPos().x;
      double local2982_13 = MathHelper.lerp(local2982_3, entity.lastRenderY, entity.getY())
         + entity.getHeight() * 0.5F
         - local2982_9 / 2.0F
         - mc.gameRenderer.getCamera().getCameraPos().y;
      double local2982_15 = MathHelper.lerp(local2982_3, entity.lastRenderZ, entity.getZ()) - mc.gameRenderer.getCamera().getCameraPos().z;
      int local2982_17 = 0;
      Vector3f vector3f = new Vector3f();
      BufferAllocator bufferAllocator = this.getBufferAllocator(0, 256 * VertexFormats.POSITION_TEXTURE_COLOR.getVertexSize());
      BufferAllocator bufferAllocator2 = this.getBufferAllocator(1, 256 * VertexFormats.POSITION_TEXTURE_COLOR.getVertexSize());
      BufferBuilder bufferBuilder = new BufferBuilder(bufferAllocator, DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      BufferBuilder bufferBuilder2 = new BufferBuilder(bufferAllocator2, DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

      for (int index = 0; index < 2; index++) {
         float local2982_24 = index == 0 ? local2982_4 : (float)this.animation2.computeDouble();
         if (!(local2982_24 < 0.003921569F)) {
            float local2982_25 = local2982_8 * (1.2F - 0.5F * local2982_24);
            float local2982_26 = (index == 0 ? local2982_6 : -local2982_6) / 100.0F;
            float local2982_27 = (index == 0 ? -local2982_7 : local2982_7) / 100.0F;
            Quaternionf quaternionf = new Quaternionf()
               .rotateZ((float)Math.toRadians(index == 0 ? local2982_6 : -local2982_6))
               .rotateX((float)Math.toRadians(index == 0 ? local2982_7 : -local2982_7));

            for (float local2982_29 = local2982_10; local2982_29 <= 720.0F; local2982_29 += local2982_10) {
               int local2982_30 = Colors.computeInt6(this.computeInt3(local2982_29 * 3.0F, local2982_5), local2982_24);
               float local2982_31 = local2982_26 + MathHelper.sin((float)Math.toRadians(local2982_29 - local2982_10 + this.floatVal10 * 0.5F)) * local2982_25;
               float local2982_32 = local2982_27 + MathHelper.cos((float)Math.toRadians(local2982_29 - local2982_10 + this.floatVal10 * 0.5F)) * local2982_25;
               float local2982_33 = local2982_26 + MathHelper.sin((float)Math.toRadians(local2982_29 + this.floatVal10 * 0.5F)) * local2982_25;
               float local2982_34 = local2982_27 + MathHelper.cos((float)Math.toRadians(local2982_29 + this.floatVal10 * 0.5F)) * local2982_25;
               quaternionf.transform(vector3f.set(local2982_31, 0.0F, local2982_32));
               bufferBuilder.vertex((float)(local2982_11 + vector3f.x), (float)(local2982_13 + vector3f.y), (float)(local2982_15 + vector3f.z))
                  .texture((local2982_29 - local2982_10) / 360.0F * 3.0F, 0.0F)
                  .color(local2982_30);
               quaternionf.transform(vector3f.set(local2982_33, 0.0F, local2982_34));
               bufferBuilder.vertex((float)(local2982_11 + vector3f.x), (float)(local2982_13 + vector3f.y), (float)(local2982_15 + vector3f.z))
                  .texture(local2982_29 / 360.0F * 3.0F, 0.0F)
                  .color(local2982_30);
               quaternionf.transform(vector3f.set(local2982_33, local2982_9, local2982_34));
               bufferBuilder.vertex((float)(local2982_11 + vector3f.x), (float)(local2982_13 + vector3f.y), (float)(local2982_15 + vector3f.z))
                  .texture(local2982_29 / 360.0F * 3.0F, 1.0F)
                  .color(local2982_30);
               quaternionf.transform(vector3f.set(local2982_31, local2982_9, local2982_32));
               bufferBuilder.vertex((float)(local2982_11 + vector3f.x), (float)(local2982_13 + vector3f.y), (float)(local2982_15 + vector3f.z))
                  .texture((local2982_29 - local2982_10) / 360.0F * 3.0F, 1.0F)
                  .color(local2982_30);
               quaternionf.transform(
                  vector3f.set(
                     MathHelper.lerp(0.87F, (local2982_31 + local2982_33) / 2.0F, local2982_31),
                     MathHelper.lerp(0.87F, local2982_9 / 2.0F, 0.0F),
                     MathHelper.lerp(0.87F, (local2982_32 + local2982_34) / 2.0F, local2982_32)
                  )
               );
               bufferBuilder2.vertex((float)(local2982_11 + vector3f.x), (float)(local2982_13 + vector3f.y), (float)(local2982_15 + vector3f.z))
                  .texture(0.0F, 0.0F)
                  .color(Colors.computeInt6(local2982_30, 0.05F));
               quaternionf.transform(
                  vector3f.set(
                     MathHelper.lerp(0.87F, (local2982_31 + local2982_33) / 2.0F, local2982_33),
                     MathHelper.lerp(0.87F, local2982_9 / 2.0F, 0.0F),
                     MathHelper.lerp(0.87F, (local2982_32 + local2982_34) / 2.0F, local2982_34)
                  )
               );
               bufferBuilder2.vertex((float)(local2982_11 + vector3f.x), (float)(local2982_13 + vector3f.y), (float)(local2982_15 + vector3f.z))
                  .texture(1.0F, 0.0F)
                  .color(Colors.computeInt6(local2982_30, 0.05F));
               quaternionf.transform(
                  vector3f.set(
                     MathHelper.lerp(0.87F, (local2982_31 + local2982_33) / 2.0F, local2982_33),
                     MathHelper.lerp(0.87F, local2982_9 / 2.0F, local2982_9),
                     MathHelper.lerp(0.87F, (local2982_32 + local2982_34) / 2.0F, local2982_34)
                  )
               );
               bufferBuilder2.vertex((float)(local2982_11 + vector3f.x), (float)(local2982_13 + vector3f.y), (float)(local2982_15 + vector3f.z))
                  .texture(1.0F, 1.0F)
                  .color(Colors.computeInt6(local2982_30, 0.05F));
               quaternionf.transform(
                  vector3f.set(
                     MathHelper.lerp(0.87F, (local2982_31 + local2982_33) / 2.0F, local2982_31),
                     MathHelper.lerp(0.87F, local2982_9 / 2.0F, local2982_9),
                     MathHelper.lerp(0.87F, (local2982_32 + local2982_34) / 2.0F, local2982_32)
                  )
               );
               bufferBuilder2.vertex((float)(local2982_11 + vector3f.x), (float)(local2982_13 + vector3f.y), (float)(local2982_15 + vector3f.z))
                  .texture(0.0F, 1.0F)
                  .color(Colors.computeInt6(local2982_30, 0.05F));
               local2982_17++;
            }
         }
      }

      try (BuiltBuffer builtBuffer = bufferBuilder2.end()) {
         this.renderElement9(
            matrixStack, "textures/particles/glow.png", builtBuffer, local2982_17, RaycastUtils.matchesCriteria(mc.player, entity), false
         );
      }

      try (BuiltBuffer builtBuffer2 = bufferBuilder.end()) {
         this.renderElement9(
            matrixStack, "textures/particles/chain.png", builtBuffer2, local2982_17, RaycastUtils.matchesCriteria(mc.player, entity), true
         );
      }
   }

   private int computeInt(
      BufferBuilder bufferBuilder,
      float local2983_2,
      float local2983_3,
      float local2983_4,
      LivingEntity livingEntity,
      float local2983_6,
      float local2983_7,
      float local2983_8,
      float local2983_9,
      float local2983_10,
      float local2983_11,
      int local2983_12,
      int local2983_13
   ) {
      int local2983_14 = 0;

      for (int index = 0; index < 360; index += 2) {
         float local2983_16 = MathHelper.cos((float)Math.toRadians(index));
         float local2983_17 = MathHelper.sin((float)Math.toRadians(index));
         float local2983_18 = MathHelper.cos((float)Math.toRadians(index + 2));
         float local2983_19 = MathHelper.sin((float)Math.toRadians(index + 2));
         bufferBuilder.vertex(
               local2983_2 + local2983_16 * (this.computeFloat3(livingEntity, local2983_8 / local2983_6, local2983_7) + local2983_10),
               local2983_3 + local2983_8,
               local2983_4 + local2983_17 * (this.computeFloat3(livingEntity, local2983_8 / local2983_6, local2983_7) + local2983_10)
            )
            .color(local2983_12);
         bufferBuilder.vertex(
               local2983_2 + local2983_18 * (this.computeFloat3(livingEntity, local2983_8 / local2983_6, local2983_7) + local2983_10),
               local2983_3 + local2983_8,
               local2983_4 + local2983_19 * (this.computeFloat3(livingEntity, local2983_8 / local2983_6, local2983_7) + local2983_10)
            )
            .color(local2983_12);
         bufferBuilder.vertex(
               local2983_2 + local2983_18 * (this.computeFloat3(livingEntity, local2983_9 / local2983_6, local2983_7) + local2983_11),
               local2983_3 + local2983_9,
               local2983_4 + local2983_19 * (this.computeFloat3(livingEntity, local2983_9 / local2983_6, local2983_7) + local2983_11)
            )
            .color(local2983_13);
         bufferBuilder.vertex(
               local2983_2 + local2983_16 * (this.computeFloat3(livingEntity, local2983_9 / local2983_6, local2983_7) + local2983_11),
               local2983_3 + local2983_9,
               local2983_4 + local2983_17 * (this.computeFloat3(livingEntity, local2983_9 / local2983_6, local2983_7) + local2983_11)
            )
            .color(local2983_13);
         local2983_14++;
      }

      return local2983_14;
   }

   private int computeInt2(float local2984_1, boolean local2984_2) {
      if (this.intVal < 2) {
         return 0;
      }

      for (int index = 0; index < this.intVal; index++) {
         float local2984_4 = this.floatValArray2[index * 3];
         float local2984_5 = this.floatValArray2[index * 3 + 1];
         float local2984_6 = this.floatValArray2[index * 3 + 2];
         float local2984_7 = 0.0F;
         float local2984_8 = 0.0F;
         float local2984_9 = 0.0F;
         float local2984_10 = 0.0F;
         float local2984_11 = 0.0F;
         float local2984_12 = 0.0F;
         boolean local2984_13 = index > 0;
         boolean local2984_14 = index < this.intVal - 1;
         if (local2984_13) {
            float local2984_15 = local2984_4 - this.floatValArray2[index * 3 - 3];
            float local2984_16 = local2984_5 - this.floatValArray2[index * 3 - 2];
            float local2984_17 = local2984_6 - this.floatValArray2[index * 3 - 1];
            local2984_7 = local2984_16 * local2984_6 - local2984_17 * local2984_5;
            local2984_8 = local2984_17 * local2984_4 - local2984_15 * local2984_6;
            local2984_9 = local2984_15 * local2984_5 - local2984_16 * local2984_4;
            float local2984_18 = MathHelper.sqrt(local2984_7 * local2984_7 + local2984_8 * local2984_8 + local2984_9 * local2984_9);
            if (local2984_18 < 1.0E-6F) {
               local2984_13 = false;
            } else {
               local2984_7 /= local2984_18;
               local2984_8 /= local2984_18;
               local2984_9 /= local2984_18;
            }
         }

         if (local2984_14) {
            float local2984_15 = this.floatValArray2[index * 3 + 3] - local2984_4;
            float local2984_16 = this.floatValArray2[index * 3 + 4] - local2984_5;
            float local2984_17 = this.floatValArray2[index * 3 + 5] - local2984_6;
            local2984_10 = local2984_16 * local2984_6 - local2984_17 * local2984_5;
            local2984_11 = local2984_17 * local2984_4 - local2984_15 * local2984_6;
            local2984_12 = local2984_15 * local2984_5 - local2984_16 * local2984_4;
            float local2984_18 = MathHelper.sqrt(local2984_10 * local2984_10 + local2984_11 * local2984_11 + local2984_12 * local2984_12);
            if (local2984_18 < 1.0E-6F) {
               local2984_14 = false;
            } else {
               local2984_10 /= local2984_18;
               local2984_11 /= local2984_18;
               local2984_12 /= local2984_18;
            }
         }

         float local2984_18 = 1.0F;
         float local2984_15;
         float local2984_16;
         float local2984_17;
         if (local2984_13 && local2984_14) {
            if (local2984_7 * local2984_10 + local2984_8 * local2984_11 + local2984_9 * local2984_12 < 0.0F) {
               local2984_10 = -local2984_10;
               local2984_11 = -local2984_11;
               local2984_12 = -local2984_12;
            }

            local2984_15 = local2984_7 + local2984_10;
            local2984_16 = local2984_8 + local2984_11;
            local2984_17 = local2984_9 + local2984_12;
            float local2984_19 = MathHelper.sqrt(local2984_15 * local2984_15 + local2984_16 * local2984_16 + local2984_17 * local2984_17);
            if (local2984_19 < 1.0E-6F) {
               local2984_15 = local2984_7;
               local2984_16 = local2984_8;
               local2984_17 = local2984_9;
            } else {
               local2984_15 /= local2984_19;
               local2984_16 /= local2984_19;
               local2984_17 /= local2984_19;
               local2984_18 = 1.0F / Math.max(local2984_15 * local2984_7 + local2984_16 * local2984_8 + local2984_17 * local2984_9, 0.35F);
            }
         } else if (local2984_13) {
            local2984_15 = local2984_7;
            local2984_16 = local2984_8;
            local2984_17 = local2984_9;
         } else if (local2984_14) {
            local2984_15 = local2984_10;
            local2984_16 = local2984_11;
            local2984_17 = local2984_12;
         } else {
            local2984_15 = 0.0F;
            local2984_16 = 0.0F;
            local2984_17 = 0.0F;
         }

         this.floatValArray3[index * 3] = local2984_15;
         this.floatValArray3[index * 3 + 1] = local2984_16;
         this.floatValArray3[index * 3 + 2] = local2984_17;
         this.floatValArray4[index] = local2984_18;
      }

      float local2984_3 = this.floatVal5 * local2984_1;
      float local2984_4 = this.floatVal7 * local2984_1;

      for (int index2 = 0; index2 < this.intVal - 1; index2++) {
         float local2984_6 = 1.0F;
         float local2984_7 = 1.0F;
         if (local2984_2) {
            float local2984_8 = MathHelper.clamp(((float)index2 / (this.intVal - 1) - 0.35F) / 0.65F, 0.0F, 1.0F);
            local2984_6 = 1.0F - 0.75F * local2984_8 * local2984_8 * (3.0F - 2.0F * local2984_8);
            local2984_8 = MathHelper.clamp(((float)(index2 + 1) / (this.intVal - 1) - 0.35F) / 0.65F, 0.0F, 1.0F);
            local2984_7 = 1.0F - 0.75F * local2984_8 * local2984_8 * (3.0F - 2.0F * local2984_8);
         }

         float local2984_9 = this.floatValArray3[index2 * 3] * local2984_3 * this.floatValArray4[index2] * local2984_6;
         float local2984_10 = this.floatValArray3[index2 * 3 + 1] * local2984_3 * this.floatValArray4[index2] * local2984_6;
         float local2984_11 = this.floatValArray3[index2 * 3 + 2] * local2984_3 * this.floatValArray4[index2] * local2984_6;
         float local2984_12 = this.floatValArray3[index2 * 3 + 3] * local2984_3 * this.floatValArray4[index2 + 1] * local2984_7;
         float local2984_13 = this.floatValArray3[index2 * 3 + 4] * local2984_3 * this.floatValArray4[index2 + 1] * local2984_7;
         float local2984_14 = this.floatValArray3[index2 * 3 + 5] * local2984_3 * this.floatValArray4[index2 + 1] * local2984_7;
         this.bufferBuilder5
            .vertex(
               this.floatValArray2[index2 * 3] - local2984_9,
               this.floatValArray2[index2 * 3 + 1] - local2984_10,
               this.floatValArray2[index2 * 3 + 2] - local2984_11
            )
            .color(this.intVal2);
         this.bufferBuilder5
            .vertex(
               this.floatValArray2[index2 * 3 + 3] - local2984_12,
               this.floatValArray2[index2 * 3 + 4] - local2984_13,
               this.floatValArray2[index2 * 3 + 5] - local2984_14
            )
            .color(this.intVal2);
         this.bufferBuilder5
            .vertex(
               this.floatValArray2[index2 * 3 + 3] + local2984_12,
               this.floatValArray2[index2 * 3 + 4] + local2984_13,
               this.floatValArray2[index2 * 3 + 5] + local2984_14
            )
            .color(this.intVal2);
         this.bufferBuilder5
            .vertex(
               this.floatValArray2[index2 * 3] + local2984_9,
               this.floatValArray2[index2 * 3 + 1] + local2984_10,
               this.floatValArray2[index2 * 3 + 2] + local2984_11
            )
            .color(this.intVal2);
         this.processData3(
            this.bufferBuilder6,
            this.quaternionf2,
            this.vector3f3,
            (this.floatValArray2[index2 * 3] + this.floatValArray2[index2 * 3 + 3]) * 0.5F,
            (this.floatValArray2[index2 * 3 + 1] + this.floatValArray2[index2 * 3 + 4]) * 0.5F,
            (this.floatValArray2[index2 * 3 + 2] + this.floatValArray2[index2 * 3 + 5]) * 0.5F,
            local2984_4 * (local2984_6 + local2984_7) * 0.5F,
            0.0F,
            this.intVal3
         );
      }

      return this.intVal - 1;
   }

   public void updateState() {
      if (this.flag) {
         this.flag = false;
         if (this.livingEntity2 != null && !this.livingEntity2.isRemoved()) {
            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(this.gpuBufferSlice2, this.projectionType);
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.multiplyPositionMatrix(this.matrix4f2);
            if (this.ghostStyle.isSelected("Второй")) {
               this.renderElement5(matrixStack, this.livingEntity2, this.floatVal13, this.floatVal11, this.floatVal12);
            } else {
               this.renderElement(matrixStack, this.livingEntity2, this.floatVal13, this.floatVal11, this.floatVal12);
            }

            RenderSystem.restoreProjectionMatrix();
         }
      }
   }

   private void renderElement3(MatrixStack matrixStack, float local2986_2) {
      for (Entity entity : mc.world.getEntities()) {
         if (entity instanceof PersistentProjectileEntity entity2
            && entity2.getOwner() == mc.player
            && !(entity2.getVelocity().lengthSquared() < 1.0E-5)) {
            double local2986_6 = MathHelper.lerp(local2986_2, mc.player.lastRenderX, mc.player.getX())
               - mc.gameRenderer.getCamera().getCameraPos().x;
            double local2986_8 = MathHelper.lerp(local2986_2, mc.player.lastRenderY, mc.player.getY())
               + mc.player.getStandingEyeHeight() * 0.75F
               - mc.gameRenderer.getCamera().getCameraPos().y;
            double local2986_10 = MathHelper.lerp(local2986_2, mc.player.lastRenderZ, mc.player.getZ())
               - mc.gameRenderer.getCamera().getCameraPos().z;
            double local2986_12 = MathHelper.lerp(local2986_2, entity2.lastRenderX, entity2.getX())
               - mc.gameRenderer.getCamera().getCameraPos().x;
            double local2986_14 = MathHelper.lerp(local2986_2, entity2.lastRenderY, entity2.getY())
               - mc.gameRenderer.getCamera().getCameraPos().y;
            double local2986_16 = MathHelper.lerp(local2986_2, entity2.lastRenderZ, entity2.getZ())
               - mc.gameRenderer.getCamera().getCameraPos().z;
            float local2986_18 = (float)Math.sqrt(
               (local2986_12 - local2986_6) * (local2986_12 - local2986_6)
                  + (local2986_14 - local2986_8) * (local2986_14 - local2986_8)
                  + (local2986_16 - local2986_10) * (local2986_16 - local2986_10)
            );
            float local2986_19 = 1.0F;
            float local2986_20 = local2986_18 * 0.06F;
            int local2986_21 = 16;
            float[] floatArray = new float[local2986_21 + 1];
            float[] floatArray2 = new float[local2986_21 + 1];
            float[] floatArray3 = new float[local2986_21 + 1];
            float[] floatArray4 = new float[local2986_21 + 1];
            float[] floatArray5 = new float[local2986_21 + 1];
            float[] floatArray6 = new float[local2986_21 + 1];
            BufferAllocator bufferAllocator = this.getBufferAllocator(1, local2986_21 * 4 * VertexFormats.POSITION_TEXTURE_COLOR.getVertexSize());
            BufferBuilder bufferBuilder = new BufferBuilder(bufferAllocator, DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            for (int index = 0; index <= local2986_21; index++) {
               floatArray[index] = (float)MathHelper.lerp((double)index / local2986_21, local2986_6, local2986_12);
               floatArray2[index] = (float)(
                  MathHelper.lerp((double)index / local2986_21, local2986_8, local2986_14)
                     - local2986_20 * 4.0F * ((float)index / local2986_21) * (1.0F - (float)index / local2986_21)
               );
               floatArray3[index] = (float)MathHelper.lerp((double)index / local2986_21, local2986_10, local2986_16);
            }

            for (int index2 = 0; index2 <= local2986_21; index2++) {
               float local2986_28 = floatArray[Math.min(index2 + 1, local2986_21)] - floatArray[Math.max(index2 - 1, 0)];
               float local2986_29 = floatArray2[Math.min(index2 + 1, local2986_21)] - floatArray2[Math.max(index2 - 1, 0)];
               float local2986_30 = floatArray3[Math.min(index2 + 1, local2986_21)] - floatArray3[Math.max(index2 - 1, 0)];
               floatArray4[index2] = local2986_29 * floatArray3[index2] - local2986_30 * floatArray2[index2];
               floatArray5[index2] = local2986_30 * floatArray[index2] - local2986_28 * floatArray3[index2];
               floatArray6[index2] = local2986_28 * floatArray2[index2] - local2986_29 * floatArray[index2];
               float local2986_31 = Math.max(
                  1.0E-5F,
                  (float)Math.sqrt(
                     floatArray4[index2] * floatArray4[index2]
                        + floatArray5[index2] * floatArray5[index2]
                        + floatArray6[index2] * floatArray6[index2]
                  )
               );
               floatArray4[index2] *= local2986_19 / 2.0F / local2986_31;
               floatArray5[index2] *= local2986_19 / 2.0F / local2986_31;
               floatArray6[index2] *= local2986_19 / 2.0F / local2986_31;
            }

            for (int index3 = 0; index3 < local2986_21; index3++) {
               bufferBuilder.vertex(
                     floatArray[index3] - floatArray4[index3],
                     floatArray2[index3] - floatArray5[index3],
                     floatArray3[index3] - floatArray6[index3]
                  )
                  .texture((float)index3 / local2986_21 * local2986_18 / (local2986_19 * 1.3333F), 0.0F)
                  .color(this.computeInt3(index3 * 24.0F, 0.0F));
               bufferBuilder.vertex(
                     floatArray[index3 + 1] - floatArray4[index3 + 1],
                     floatArray2[index3 + 1] - floatArray5[index3 + 1],
                     floatArray3[index3 + 1] - floatArray6[index3 + 1]
                  )
                  .texture((float)(index3 + 1) / local2986_21 * local2986_18 / (local2986_19 * 1.3333F), 0.0F)
                  .color(this.computeInt3((index3 + 1) * 24.0F, 0.0F));
               bufferBuilder.vertex(
                     floatArray[index3 + 1] + floatArray4[index3 + 1],
                     floatArray2[index3 + 1] + floatArray5[index3 + 1],
                     floatArray3[index3 + 1] + floatArray6[index3 + 1]
                  )
                  .texture((float)(index3 + 1) / local2986_21 * local2986_18 / (local2986_19 * 1.3333F), 1.0F)
                  .color(this.computeInt3((index3 + 1) * 24.0F, 0.0F));
               bufferBuilder.vertex(
                     floatArray[index3] + floatArray4[index3],
                     floatArray2[index3] + floatArray5[index3],
                     floatArray3[index3] + floatArray6[index3]
                  )
                  .texture((float)index3 / local2986_21 * local2986_18 / (local2986_19 * 1.3333F), 1.0F)
                  .color(this.computeInt3(index3 * 24.0F, 0.0F));
            }

            try (BuiltBuffer builtBuffer = bufferBuilder.end()) {
               this.renderElement9(
                  matrixStack, "textures/particles/chain.png", builtBuffer, local2986_21, RaycastUtils.matchesCriteria(mc.player, entity2), true
               );
            }
         }
      }
   }

   public TargetEspModule.DataRecord getNested0418(PlayerEntity playerEntity) {
      if (this.isEnabled()
         && this.mode.isSelected("Круг")
         && this.circleStyle.isSelected("Четвёртый")
         && this.livingEntity2 == playerEntity
         && !(this.animation.computeDouble() <= 0.001F)) {
         if (this.livingEntity3 != playerEntity || System.currentTimeMillis() - this.timeMillis4 > 250L) {
            this.livingEntity3 = playerEntity;
            this.timeMillis3 = System.currentTimeMillis();
         }

         this.timeMillis4 = System.currentTimeMillis();
         float local2987_2 = (float)((System.currentTimeMillis() - this.timeMillis3) % 1800L) / 1800.0F;
         return new TargetEspModule.DataRecord(
            this.computeInt3(0.0F, this.floatVal4),
            -49088,
            local2987_2,
            this.computeFloat(0.0F, 1.0F, MathHelper.clamp((float)this.animation.computeDouble(), 0.0F, 1.0F))
               * this.computeFloat(0.0F, 0.08F, local2987_2)
               * (1.0F - this.computeFloat(0.88F, 1.0F, local2987_2)),
            MathHelper.clamp(this.floatVal4, 0.0F, 1.0F),
            1.0F
         );
      } else {
         return null;
      }
   }

   private void renderElement4(MatrixStack matrixStack, LivingEntity entity, float local2989_3, float local2989_4, float local2989_5) {
      long local2989_6 = System.currentTimeMillis();
      this.floatVal = this.floatVal + (float)Math.min(local2989_6 - this.timeMillis, 100L) / 1000.0F;
      this.timeMillis = local2989_6;
      this.floatVal2 = entity.hurtTime > 0 ? entity.hurtTime / 10.0F : Math.max(0.0F, this.floatVal2 - 0.1F);
      int local2989_8 = (int)this.floatVal;
      float local2989_9 = this.floatVal - local2989_8;
      float height = entity.getHeight();
      float local2989_11 = entity.getWidth() * 0.75F + 0.16F;
      float local2989_12 = (float)(
         MathHelper.lerp(local2989_3, entity.lastRenderX, entity.getX()) - mc.gameRenderer.getCamera().getCameraPos().x
      );
      float local2989_13 = (float)(
         MathHelper.lerp(local2989_3, entity.lastRenderY, entity.getY()) - mc.gameRenderer.getCamera().getCameraPos().y
      );
      float local2989_14 = (float)(
         MathHelper.lerp(local2989_3, entity.lastRenderZ, entity.getZ()) - mc.gameRenderer.getCamera().getCameraPos().z
      );
      boolean local2989_15 = RaycastUtils.matchesCriteria(mc.player, entity);
      this.floatVal5 = height * 0.005F;
      this.floatVal7 = height * 0.09F;
      this.quaternionf2 = mc.gameRenderer.getCamera().getRotation();
      int local2989_16 = 0;
      int local2989_17 = 0;
      BufferAllocator bufferAllocator = this.getBufferAllocator(0, 2048 * VertexFormats.POSITION_COLOR.getVertexSize());
      BufferAllocator bufferAllocator2 = this.getBufferAllocator(1, 2048 * VertexFormats.POSITION_TEXTURE_COLOR.getVertexSize());
      this.bufferBuilder5 = new BufferBuilder(bufferAllocator, DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      this.bufferBuilder6 = new BufferBuilder(bufferAllocator2, DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

      for (int index = 0; index < 3; index++) {
         float local2989_21 = this.floatVal * 1.3F + index / 3.0F;
         int local2989_22 = (int)Math.floor(local2989_21);
         float local2989_23 = local2989_21 - local2989_22;
         float local2989_24 = MathHelper.clamp(local2989_23 / 0.08F, 0.0F, 1.0F)
            * (1.0F - MathHelper.clamp((local2989_23 - 0.42F) / 0.3F, 0.0F, 1.0F))
            * MathHelper.clamp(0.85F + 0.15F * this.computeFloat2(index, 900, local2989_8, local2989_9), 0.0F, 1.0F)
            * local2989_4;
         if (!(local2989_24 < 0.003921569F)) {
            this.intVal2 = Colors.computeInt6(
               Colors.computeInt7(
                  Colors.computeInt2(
                     Colors.computeInt2(-1, -1, (float)(Math.sin(System.currentTimeMillis() / 700.0 + Math.toRadians(44.0)) + 1.0) / 2.0F),
                     -49088,
                     local2989_5
                  ),
                  255
               ),
               local2989_24
            );
            this.intVal3 = Colors.computeInt6(this.intVal2, 0.13F);
            float local2989_25 = this.computeFloat4(index, 11, local2989_22) * (float) Math.PI;
            float local2989_26 = 0.6F + 0.5F * Math.abs(this.computeFloat4(index, 12, local2989_22));
            float local2989_27 = this.computeFloat4(index, 13, local2989_22) >= 0.0F ? 1.0F : -1.0F;
            float local2989_28 = height * (0.85F + 0.05F * this.computeFloat4(index, 14, local2989_22));
            float local2989_29 = height * 0.67F;
            float local2989_30 = local2989_11 * (1.0F + 0.12F * this.computeFloat4(index, 15, local2989_22));
            float local2989_31 = height * 0.16F * MathHelper.sin(this.floatVal * 1.7F + index * 2.1F);
            float local2989_32 = 0.0F;
            float local2989_33 = 0.0F;
            float local2989_34 = 0.0F;

            for (int index2 = 0; index2 <= 3; index2++) {
               float local2989_36 = index2 / 3.0F;
               float local2989_37 = local2989_25 + this.floatVal * 0.9F + local2989_27 * local2989_26 * (float) (Math.PI * 2) * local2989_36;
               float local2989_38 = local2989_12 + MathHelper.cos(local2989_37) * local2989_30;
               float local2989_39 = local2989_13 + local2989_28 - local2989_29 * local2989_36 + local2989_31;
               float local2989_40 = local2989_14 + MathHelper.sin(local2989_37) * local2989_30;
               if (index2 > 0) {
                  local2989_17 += this.computeInt4(
                     local2989_32,
                     local2989_33,
                     local2989_34,
                     local2989_38,
                     local2989_39,
                     local2989_40,
                     (index * 131 + index2) * 17 + local2989_22 * 101 | 1,
                     local2989_8,
                     local2989_9,
                     index2 == 3
                  );
               }

               local2989_32 = local2989_38;
               local2989_33 = local2989_39;
               local2989_34 = local2989_40;
            }
         }
      }

      local2989_16 = local2989_17;
      if (local2989_16 > 0) {
         try (BuiltBuffer builtBuffer = this.bufferBuilder6.end()) {
            this.renderElement9(matrixStack, "textures/particles/glow.png", builtBuffer, local2989_16, local2989_15, false);
         }
      }

      if (local2989_17 > 0) {
         try (BuiltBuffer builtBuffer2 = this.bufferBuilder5.end()) {
            this.renderElement9(matrixStack, null, builtBuffer2, local2989_17, local2989_15, false);
         }
      }
   }

   private int computeInt3(float local2990_1, float local2990_2) {
      return Colors.computeInt2(
         Colors.computeInt2(
            UiTheme.intVal7,
            UiTheme.intVal8,
            (float)(Math.sin(System.currentTimeMillis() / 700.0 + Math.toRadians(local2990_1)) + 1.0) / 2.0F
         ),
         -49088,
         local2990_2
      );
   }

   private void renderElement5(MatrixStack matrixStack, LivingEntity livingEntity, float local2991_3, float local2991_4, float local2991_5) {
      Quaternionf rotation = mc.gameRenderer.getCamera().getRotation();
      double local2991_7 = MathHelper.lerp(local2991_3, livingEntity.lastRenderX, livingEntity.getX()) - mc.gameRenderer.getCamera().getCameraPos().x;
      double local2991_9 = MathHelper.lerp(local2991_3, livingEntity.lastRenderY, livingEntity.getY()) - mc.gameRenderer.getCamera().getCameraPos().y;
      double local2991_11 = MathHelper.lerp(local2991_3, livingEntity.lastRenderZ, livingEntity.getZ()) - mc.gameRenderer.getCamera().getCameraPos().z;
      int local2991_13 = 4;
      int local2991_14 = 15;
      float width = livingEntity.getWidth();
      float local2991_16 = livingEntity.getHeight() / 2.0F + 0.2F;
      float local2991_17 = 0.7F;
      float local2991_18 = 0.2F;
      float local2991_19 = 0.6F;
      double local2991_20 = System.currentTimeMillis() / 1000.0;
      double local2991_22 = local2991_20 * 3.0 % (Math.PI * 2);
      double local2991_24 = Math.toRadians(60.0) / (local2991_14 - 1);
      int local2991_26 = 0;
      BufferAllocator bufferAllocator = this.getBufferAllocator(1, local2991_13 * local2991_14 * 4 * VertexFormats.POSITION_TEXTURE_COLOR.getVertexSize());
      BufferBuilder bufferBuilder = new BufferBuilder(bufferAllocator, DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      Vector3f vector3f = new Vector3f();

      for (int index = 0; index < local2991_13; index++) {
         double local2991_31 = local2991_20 + index * 15.0;
         double local2991_33 = index * (Math.PI / 2);

         for (int index2 = 0; index2 < local2991_14; index2++) {
            double local2991_36 = index2 * local2991_24;
            double local2991_38 = local2991_36 + local2991_22 + local2991_33;
            this.processData3(
               bufferBuilder,
               rotation,
               vector3f,
               local2991_7 + width * Math.cos(local2991_38),
               local2991_9 + local2991_16 + Math.sin(local2991_31 + local2991_36 + index) * local2991_17,
               local2991_11 + width * Math.sin(local2991_38),
               MathHelper.lerp((float)index2 / (local2991_14 - 1), local2991_18, local2991_19) / 2.0F,
               0.0F,
               Colors.computeInt6(this.computeInt3(index2 * 14, local2991_5), local2991_4)
            );
            local2991_26++;
         }
      }

      try (BuiltBuffer builtBuffer = bufferBuilder.end()) {
         this.renderElement9(matrixStack, "textures/particles/glow.png", builtBuffer, local2991_26, false, false);
      }
   }

   private void processData(
      float local2992_1,
      float local2992_2,
      float local2992_3,
      float local2992_4,
      float local2992_5,
      float local2992_6,
      int local2992_7,
      int local2992_8,
      int local2992_9,
      int local2992_10,
      float local2992_11
   ) {
      float local2992_12 = local2992_4 - local2992_1;
      float local2992_13 = local2992_5 - local2992_2;
      float local2992_14 = local2992_6 - local2992_3;
      float local2992_15 = MathHelper.sqrt(local2992_12 * local2992_12 + local2992_13 * local2992_13 + local2992_14 * local2992_14);
      if (local2992_7 > 0 && !(local2992_15 < 1.0E-4F)) {
         float local2992_16 = local2992_12 / local2992_15;
         float local2992_17 = local2992_13 / local2992_15;
         float local2992_18 = local2992_14 / local2992_15;
         float local2992_19 = Math.abs(local2992_17) < 0.9F ? 0.0F : 1.0F;
         float local2992_20 = Math.abs(local2992_17) < 0.9F ? 1.0F : 0.0F;
         float local2992_21 = -local2992_18 * local2992_20;
         float local2992_22 = local2992_18 * local2992_19;
         float local2992_23 = local2992_16 * local2992_20 - local2992_17 * local2992_19;
         float local2992_24 = MathHelper.sqrt(local2992_21 * local2992_21 + local2992_22 * local2992_22 + local2992_23 * local2992_23);
         if (local2992_24 < 1.0E-5F) {
            local2992_24 = 1.0F;
         }

         local2992_21 /= local2992_24;
         local2992_22 /= local2992_24;
         local2992_23 /= local2992_24;
         float local2992_25 = local2992_17 * local2992_23 - local2992_18 * local2992_22;
         float local2992_26 = local2992_18 * local2992_21 - local2992_16 * local2992_23;
         float local2992_27 = local2992_16 * local2992_22 - local2992_17 * local2992_21;
         float local2992_28 = this.computeFloat2(local2992_8, 0, local2992_10, local2992_11);
         float local2992_29 = this.computeFloat2(local2992_8, 1, local2992_10, local2992_11);
         float local2992_30 = local2992_15 * 0.3F;
         float local2992_31 = (local2992_1 + local2992_4) * 0.5F + (local2992_21 * local2992_28 + local2992_25 * local2992_29) * local2992_30;
         float local2992_32 = (local2992_2 + local2992_5) * 0.5F + (local2992_22 * local2992_28 + local2992_26 * local2992_29) * local2992_30;
         float local2992_33 = (local2992_3 + local2992_6) * 0.5F + (local2992_23 * local2992_28 + local2992_27 * local2992_29) * local2992_30;
         this.processData(
            local2992_1,
            local2992_2,
            local2992_3,
            local2992_31,
            local2992_32,
            local2992_33,
            local2992_7 - 1,
            local2992_8 * 2 + 7,
            local2992_9,
            local2992_10,
            local2992_11
         );
         this.processData(
            local2992_31,
            local2992_32,
            local2992_33,
            local2992_4,
            local2992_5,
            local2992_6,
            local2992_7 - 1,
            local2992_8 * 2 + 9,
            local2992_9,
            local2992_10,
            local2992_11
         );
         if (local2992_9 < 2 && local2992_7 >= 2 && this.computeFloat2(local2992_8, 2, local2992_10, local2992_11) > 0.45F) {
            float local2992_34 = this.computeFloat2(local2992_8, 3, local2992_10, local2992_11);
            float local2992_35 = this.computeFloat2(local2992_8, 4, local2992_10, local2992_11);
            float local2992_36 = local2992_16 * 0.4F + local2992_21 * local2992_34 + local2992_25 * local2992_35;
            float local2992_37 = local2992_17 * 0.4F + local2992_22 * local2992_34 + local2992_26 * local2992_35;
            float local2992_38 = local2992_18 * 0.4F + local2992_23 * local2992_34 + local2992_27 * local2992_35;
            float local2992_39 = MathHelper.sqrt(local2992_36 * local2992_36 + local2992_37 * local2992_37 + local2992_38 * local2992_38);
            if (local2992_39 < 1.0E-5F) {
               local2992_39 = 1.0F;
            }

            float local2992_40 = local2992_15 * 0.75F;
            this.list
               .add(
                  new float[]{
                     local2992_31,
                     local2992_32,
                     local2992_33,
                     local2992_31 + local2992_36 / local2992_39 * local2992_40,
                     local2992_32 + local2992_37 / local2992_39 * local2992_40,
                     local2992_33 + local2992_38 / local2992_39 * local2992_40,
                     Math.min(local2992_7 - 1, 3),
                     local2992_8 * 3 + 13,
                     local2992_9 + 1,
                     0.55F
                  }
               );
         }
      } else {
         this.processData2(local2992_4, local2992_5, local2992_6);
      }
   }

   private void renderElement6(MatrixStack matrixStack, LivingEntity livingEntity, float local2993_3, float local2993_4, float local2993_5) {
      if (mc.getFramebuffer() != null && mc.getFramebuffer().getColorAttachmentView() != null && DepthSnapshot.getGpuTextureView() != null) {
         float local2993_6 = (float)(
            MathHelper.lerp(local2993_3, livingEntity.lastRenderX, livingEntity.getX()) - mc.gameRenderer.getCamera().getCameraPos().x
         );
         float local2993_7 = (float)(
            MathHelper.lerp(local2993_3, livingEntity.lastRenderY, livingEntity.getY()) - mc.gameRenderer.getCamera().getCameraPos().y
         );
         float local2993_8 = (float)(
            MathHelper.lerp(local2993_3, livingEntity.lastRenderZ, livingEntity.getZ()) - mc.gameRenderer.getCamera().getCameraPos().z
         );
         float local2993_9 = livingEntity.getHeight() + 0.3F;
         float local2993_10 = livingEntity.getWidth() * 0.9F + 0.3F;
         float local2993_11 = livingEntity.getHeight() - 0.25F;
         float local2993_12 = (float)(System.currentTimeMillis() % 2200L) / 2200.0F * (float) (Math.PI * 2);
         float local2993_13 = 0.5F + 0.5F * MathHelper.sin(local2993_12);
         float local2993_14 = MathHelper.cos(local2993_12) >= 0.0F ? 1.0F : -1.0F;
         float local2993_15 = Math.abs(MathHelper.cos(local2993_12));
         float local2993_16 = local2993_7 + 0.12F + local2993_11 * local2993_13;
         int local2993_17 = this.computeInt3(0.0F, local2993_5);
         if (this.gpuBuffer3 == null || this.gpuBuffer3.isClosed()) {
            this.gpuBuffer3 = RenderSystem.getDevice().createBuffer(() -> "horus_target_scan_params", 136, 112L);
         }

         MemoryStack memoryStack = MemoryStack.stackPush();

         try {
            ByteBuffer byteBuffer = memoryStack.calloc(112);
            new Matrix4f(WorldProjection.getMatrix4f()).mul(matrixStack.peek().getPositionMatrix()).invert().get(0, byteBuffer);
            byteBuffer.putFloat(64, local2993_6);
            byteBuffer.putFloat(68, local2993_7);
            byteBuffer.putFloat(72, local2993_8);
            byteBuffer.putFloat(76, local2993_10);
            byteBuffer.putFloat(80, local2993_16);
            byteBuffer.putFloat(84, local2993_14);
            byteBuffer.putFloat(88, local2993_15);
            byteBuffer.putFloat(92, (float)(System.currentTimeMillis() % 100000L) / 1000.0F);
            byteBuffer.putFloat(96, local2993_7 + 0.06F);
            byteBuffer.putFloat(100, local2993_7 - 0.1F + local2993_9);
            byteBuffer.putFloat(104, local2993_4);
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.gpuBuffer3.slice(0L, 112L), byteBuffer);
         } catch (Throwable throwable) {
            if (memoryStack != null) {
               try {
                  memoryStack.close();
               } catch (Throwable throwable2) {
                  throwable.addSuppressed(throwable2);
               }
            }

            throw throwable;
         }

         if (memoryStack != null) {
            memoryStack.close();
         }

         try (BufferAllocator local2993_18x = new BufferAllocator(4 * VertexFormats.POSITION_TEXTURE_COLOR.getVertexSize())) {
            BufferBuilder bufferBuilder = new BufferBuilder(local2993_18x, DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(-1.0F, -1.0F, 0.0F).texture(0.0F, 0.0F).color(local2993_17);
            bufferBuilder.vertex(-1.0F, 1.0F, 0.0F).texture(0.0F, 1.0F).color(local2993_17);
            bufferBuilder.vertex(1.0F, 1.0F, 0.0F).texture(1.0F, 1.0F).color(local2993_17);
            bufferBuilder.vertex(1.0F, -1.0F, 0.0F).texture(1.0F, 0.0F).color(local2993_17);

            try (BuiltBuffer builtBuffer = bufferBuilder.end()) {
               if (this.gpuBuffer2 != null && !this.gpuBuffer2.isClosed() && this.gpuBuffer2.size() >= builtBuffer.getBuffer().remaining()) {
                  RenderSystem.getDevice()
                     .createCommandEncoder()
                     .writeToBuffer(this.gpuBuffer2.slice(0L, builtBuffer.getBuffer().remaining()), builtBuffer.getBuffer());
               } else {
                  if (this.gpuBuffer2 != null && !this.gpuBuffer2.isClosed()) {
                     this.gpuBuffer2.close();
                  }

                  this.gpuBuffer2 = RenderSystem.getDevice().createBuffer(() -> "horus_target_ghosts_vertices", 40, builtBuffer.getBuffer());
               }

               try (RenderPass renderPass = RenderSystem.getDevice()
                     .createCommandEncoder()
                     .createRenderPass(
                        () -> "horus_target_scan", mc.getFramebuffer().getColorAttachmentView(), OptionalInt.empty(), null, OptionalDouble.empty()
                     )) {
                  renderPass.setPipeline(pipeline6);
                  renderPass.bindTexture("Sampler0", DepthSnapshot.getGpuTextureView(), RenderSystem.getSamplerCache().get(FilterMode.NEAREST));
                  renderPass.setUniform("ScanParams", this.gpuBuffer3);
                  renderPass.setVertexBuffer(0, this.gpuBuffer2);
                  renderPass.setIndexBuffer(
                     RenderSystem.getSequentialBuffer(DrawMode.QUADS).getIndexBuffer(6), RenderSystem.getSequentialBuffer(DrawMode.QUADS).getIndexType()
                  );
                  renderPass.drawIndexed(0, 0, 6, 1);
               }
            }
         }
      }
   }

   private float computeFloat2(int local2996_1, int local2996_2, int local2996_3, float local2996_4) {
      return MathHelper.lerp(
         local2996_4 * local2996_4 * (3.0F - 2.0F * local2996_4),
         this.computeFloat4(local2996_1, local2996_2, local2996_3),
         this.computeFloat4(local2996_1, local2996_2, local2996_3 + 1)
      );
   }

   private void renderElement7(MatrixStack matrixStack, LivingEntity entity, float local3001_3, float local3001_4, float local3001_5) {
      long local3001_6 = System.currentTimeMillis();
      float local3001_8 = (1.2F - 0.5F * local3001_4) * entity.getWidth() * 1.5F;
      float local3001_9 = 0.1F;
      float local3001_10 = 1.0F;
      int local3001_11 = Colors.computeInt6(this.computeInt3(0.0F, local3001_5), local3001_4);
      matrixStack.push();
      matrixStack.translate(
         entity.getLerpedPos(local3001_3).x - mc.gameRenderer.getCamera().getCameraPos().x,
         entity.getLerpedPos(local3001_3).y - mc.gameRenderer.getCamera().getCameraPos().y,
         entity.getLerpedPos(local3001_3).z - mc.gameRenderer.getCamera().getCameraPos().z
      );
      BufferAllocator bufferAllocator = this.getBufferAllocator(0, 18 * intValArray.length * 4 * VertexFormats.POSITION_COLOR.getVertexSize());
      BufferBuilder bufferBuilder = new BufferBuilder(bufferAllocator, DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      MatrixStack matrixStack2 = new MatrixStack();
      int local3001_16 = 0;

      for (int index = 0; index < 360; index += 20) {
         float local3001_18 = (float)Math.toRadians(
            index + (float)((local3001_6 + (long)(7333.0F * (index / 360.0F))) % 7333L) / 7333.0F * 360.0F
         );
         float local3001_19 = MathHelper.sin(local3001_18) * local3001_8;
         float local3001_20 = MathHelper.cos(local3001_18) * local3001_8;
         float local3001_21 = 0.1F + entity.getHeight() * (float)Easings.EASE_IN_OUT_QUAD.apply(Math.abs(MathHelper.sin(index)));
         matrixStack2.push();
         matrixStack2.translate(local3001_19, local3001_21, local3001_20);
         matrixStack2.multiply(
            new Quaternionf()
               .rotationTo(
                  new Vector3f(0.0F, 1.0F, 0.0F), new Vector3f(-local3001_19, entity.getHeight() * 0.5F - local3001_21, -local3001_20).normalize()
               )
         );
         matrixStack2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(((float)(local3001_6 % 1200L) / 1200.0F * 360.0F + index * 13.7F) % 360.0F));
         matrixStack2.scale(local3001_9, local3001_9, local3001_9);

         for (int index2 = 0; index2 < intValArray.length; index2++) {
            for (int index3 = 0; index3 < 4; index3++) {
               Vector3f vector3f = vector3fArray[intValArray[index2][Math.min(index3, 2)]];
               bufferBuilder.vertex(matrixStack2.peek().getPositionMatrix(), vector3f.x, vector3f.y, vector3f.z)
                  .color(
                     Colors.computeInt2(
                        local3001_11, Colors.computeInt7(0, Colors.computeInt4(local3001_11)), 1.0F - floatValArray[index2]
                     )
                  );
            }

            local3001_16++;
         }

         matrixStack2.pop();
      }

      try (BuiltBuffer builtBuffer = bufferBuilder.end()) {
         this.renderElement9(matrixStack, null, builtBuffer, local3001_16, RaycastUtils.matchesCriteria(mc.player, entity), false);
      }

      bufferAllocator = this.getBufferAllocator(1, 72 * VertexFormats.POSITION_TEXTURE_COLOR.getVertexSize());
      bufferBuilder = new BufferBuilder(bufferAllocator, DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      Vector3f local3001_14x = new Vector3f();

      for (int index4 = 0; index4 < 360; index4 += 20) {
         float local3001_16x = (float)Math.toRadians(
            index4 + (float)((local3001_6 + (long)(7333.0F * (index4 / 360.0F))) % 7333L) / 7333.0F * 360.0F
         );
         this.processData3(
            bufferBuilder,
            mc.gameRenderer.getCamera().getRotation(),
            local3001_14x,
            MathHelper.sin(local3001_16x) * local3001_8,
            0.1F + entity.getHeight() * (float)Easings.EASE_IN_OUT_QUAD.apply(Math.abs(MathHelper.sin(index4))),
            MathHelper.cos(local3001_16x) * local3001_8,
            local3001_10 / 2.0F,
            0.0F,
            Colors.computeInt6(local3001_11, 0.2F)
         );
      }

      try (BuiltBuffer builtBuffer2 = bufferBuilder.end()) {
         this.renderElement9(matrixStack, "textures/particles/glow.png", builtBuffer2, 18, RaycastUtils.matchesCriteria(mc.player, entity), false);
      }

      matrixStack.pop();
   }

   private void renderElement8(MatrixStack matrixStack, LivingEntity livingEntity, float local3002_3, float local3002_4, float local3002_5) {
      long local3002_6 = System.currentTimeMillis();
      this.floatVal3 = this.floatVal3 + (float)Math.min(local3002_6 - this.timeMillis2, 100L) / 1000.0F;
      this.timeMillis2 = local3002_6;
      this.floatVal4 = livingEntity.hurtTime > 0 ? livingEntity.hurtTime / 10.0F : Math.max(0.0F, this.floatVal4 - 0.1F);
      if (!this.circleStyle.isSelected("Четвёртый")) {
         if (this.circleStyle.isSelected("Третий")) {
            float height = livingEntity.getHeight();
            float local3002_9 = (float)(System.currentTimeMillis() % 3600L) / 3600.0F * (float) (Math.PI * 2);
            float local3002_10 = 0.5F + 0.5F * MathHelper.cos(local3002_9);
            float local3002_11 = MathHelper.sin(local3002_9);
            float local3002_12 = this.computeFloat(0.0F, 1.0F, this.computeFloat(0.0F, 1.0F, MathHelper.clamp(local3002_4, 0.0F, 1.0F)));
            float local3002_13 = MathHelper.lerp(local3002_10, 0.035F, height - 0.035F);
            float local3002_14 = MathHelper.clamp(
               local3002_13 + local3002_11 * Math.min(0.55F, height * 0.55F) * this.computeFloat(0.0F, 1.0F, MathHelper.clamp(local3002_4, 0.0F, 1.0F)),
               0.02F,
               height - 0.02F
            );
            float local3002_15 = (float)(
               MathHelper.lerp(local3002_3, livingEntity.lastRenderX, livingEntity.getX()) - mc.gameRenderer.getCamera().getCameraPos().x
            );
            float local3002_16 = (float)(
               MathHelper.lerp(local3002_3, livingEntity.lastRenderZ, livingEntity.getZ()) - mc.gameRenderer.getCamera().getCameraPos().z
            );
            float local3002_17 = (float)(
               MathHelper.lerp(local3002_3, livingEntity.lastRenderY, livingEntity.getY()) - mc.gameRenderer.getCamera().getCameraPos().y
            );
            int local3002_18 = this.computeInt3(0.0F, this.floatVal4);
            int local3002_19 = Colors.computeInt2(local3002_18, -1, 0.72F * (1.0F - this.floatVal4));
            float local3002_20 = local3002_12 * (float)Math.sqrt(Math.abs(local3002_11));
            float local3002_21 = height * 0.019F;
            boolean local3002_22 = RaycastUtils.matchesCriteria(mc.player, livingEntity);
            float[] floatArray = new float[]{0.0F, 0.18F, 0.52F, 1.0F};
            int[] intArray = new int[]{125, 72, 24, 0};
            int local3002_25 = 0;
            BufferAllocator bufferAllocator = this.getBufferAllocator(0, 16384 * VertexFormats.POSITION_COLOR.getVertexSize());
            BufferBuilder bufferBuilder = new BufferBuilder(bufferAllocator, DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            for (int index = 0; index < floatArray.length - 1; index++) {
               local3002_25 += this.computeInt(
                  bufferBuilder,
                  local3002_15,
                  local3002_17,
                  local3002_16,
                  livingEntity,
                  height,
                  local3002_12,
                  MathHelper.lerp(floatArray[index], local3002_13, local3002_14),
                  MathHelper.lerp(floatArray[index + 1], local3002_13, local3002_14),
                  0.0F,
                  0.0F,
                  Colors.computeInt7(local3002_18, (int)(intArray[index] * local3002_20)),
                  Colors.computeInt7(local3002_18, (int)(intArray[index + 1] * local3002_20))
               );
            }

            local3002_25 += this.computeInt(
               bufferBuilder,
               local3002_15,
               local3002_17,
               local3002_16,
               livingEntity,
               height,
               local3002_12,
               Math.max(0.0F, local3002_13 - 0.075F),
               local3002_13,
               0.025F,
               0.035F,
               Colors.computeInt7(local3002_18, 0),
               Colors.computeInt7(local3002_18, (int)(72.0F * local3002_12))
            );
            local3002_25 += this.computeInt(
               bufferBuilder,
               local3002_15,
               local3002_17,
               local3002_16,
               livingEntity,
               height,
               local3002_12,
               local3002_13,
               Math.min(height, local3002_13 + 0.075F),
               0.035F,
               0.025F,
               Colors.computeInt7(local3002_18, (int)(72.0F * local3002_12)),
               Colors.computeInt7(local3002_18, 0)
            );
            local3002_25 += this.computeInt(
               bufferBuilder,
               local3002_15,
               local3002_17,
               local3002_16,
               livingEntity,
               height,
               local3002_12,
               local3002_13 - local3002_21,
               local3002_13 + local3002_21,
               0.045F,
               0.045F,
               Colors.computeInt7(local3002_19, (int)(245.0F * local3002_12)),
               Colors.computeInt7(local3002_19, (int)(245.0F * local3002_12))
            );

            for (int index2 = 1; index2 <= 5; index2++) {
               local3002_25 += this.computeInt(
                  bufferBuilder,
                  local3002_15,
                  local3002_17,
                  local3002_16,
                  livingEntity,
                  height,
                  local3002_12,
                  MathHelper.lerp(index2 / 6.0F, local3002_13, local3002_14) - local3002_21,
                  MathHelper.lerp(index2 / 6.0F, local3002_13, local3002_14) + local3002_21,
                  0.012F,
                  0.012F,
                  Colors.computeInt7(local3002_18, (int)((1.0F - index2 / 6.0F) * 34.0F * local3002_20)),
                  Colors.computeInt7(local3002_18, (int)((1.0F - index2 / 6.0F) * 34.0F * local3002_20))
               );
            }

            if (local3002_25 > 0) {
               try (BuiltBuffer builtBuffer = bufferBuilder.end()) {
                  this.renderElement9(matrixStack, null, builtBuffer, local3002_25, local3002_22, false);
               }
            }
         } else if (this.circleStyle.isSelected("Второй")) {
            float local3002_8 = (float)(System.currentTimeMillis() % 1750L);
            float local3002_9 = local3002_8 > 875.0F ? -1.0F : 1.0F;
            float local3002_10 = local3002_9 < 0.0F ? local3002_8 / 875.0F - 1.0F : 1.0F - local3002_8 / 875.0F;
            float local3002_11 = local3002_10 < 0.5F ? 2.0F * local3002_10 * local3002_10 : 1.0F - (float)Math.pow(-2.0F * local3002_10 + 2.0F, 2.0) / 2.0F;
            float local3002_12 = livingEntity.getHeight() + 0.15F;
            float local3002_13 = livingEntity.getWidth() * 0.8F;
            float local3002_14 = livingEntity.getHeight() * 0.006F;
            float local3002_15 = (float)(
               MathHelper.lerp(local3002_3, livingEntity.lastRenderX, livingEntity.getX()) - mc.gameRenderer.getCamera().getCameraPos().x
            );
            float local3002_16 = (float)(
               MathHelper.lerp(local3002_3, livingEntity.lastRenderZ, livingEntity.getZ()) - mc.gameRenderer.getCamera().getCameraPos().z
            );
            float local3002_17 = (float)(
                  MathHelper.lerp(local3002_3, livingEntity.lastRenderY, livingEntity.getY()) - mc.gameRenderer.getCamera().getCameraPos().y
               )
               + local3002_12 * local3002_11;
            float local3002_18 = local3002_12 * 0.8F * Math.min(local3002_11, 1.0F - local3002_11) * local3002_9;
            int local3002_19 = Colors.computeInt6(this.computeInt3(0.0F, this.floatVal4), local3002_4);
            int local3002_20 = Colors.computeInt6(local3002_19, 0.55F);
            int local3002_21 = Colors.computeInt7(local3002_19, 0);
            boolean local3002_22 = RaycastUtils.matchesCriteria(mc.player, livingEntity);
            int local3002_23 = 0;
            BufferAllocator bufferAllocator2 = this.getBufferAllocator(0, 8192 * VertexFormats.POSITION_COLOR.getVertexSize());
            BufferBuilder bufferBuilder2 = new BufferBuilder(bufferAllocator2, DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            for (int index3 = 0; index3 < 360; index3 += 3) {
               float local3002_27 = MathHelper.cos((float)Math.toRadians(index3));
               float local3002_28 = MathHelper.sin((float)Math.toRadians(index3));
               float local3002_29 = MathHelper.cos((float)Math.toRadians(index3 + 3));
               float local3002_30 = MathHelper.sin((float)Math.toRadians(index3 + 3));
               bufferBuilder2.vertex(
                     local3002_15 + local3002_27 * (local3002_13 - local3002_14), local3002_17, local3002_16 + local3002_28 * (local3002_13 - local3002_14)
                  )
                  .color(local3002_19);
               bufferBuilder2.vertex(
                     local3002_15 + local3002_29 * (local3002_13 - local3002_14), local3002_17, local3002_16 + local3002_30 * (local3002_13 - local3002_14)
                  )
                  .color(local3002_19);
               bufferBuilder2.vertex(
                     local3002_15 + local3002_29 * (local3002_13 + local3002_14), local3002_17, local3002_16 + local3002_30 * (local3002_13 + local3002_14)
                  )
                  .color(local3002_19);
               bufferBuilder2.vertex(
                     local3002_15 + local3002_27 * (local3002_13 + local3002_14), local3002_17, local3002_16 + local3002_28 * (local3002_13 + local3002_14)
                  )
                  .color(local3002_19);
               local3002_23++;
               bufferBuilder2.vertex(local3002_15 + local3002_27 * local3002_13, local3002_17, local3002_16 + local3002_28 * local3002_13).color(local3002_20);
               bufferBuilder2.vertex(local3002_15 + local3002_29 * local3002_13, local3002_17, local3002_16 + local3002_30 * local3002_13).color(local3002_20);
               bufferBuilder2.vertex(local3002_15 + local3002_29 * local3002_13, local3002_17 + local3002_18, local3002_16 + local3002_30 * local3002_13)
                  .color(local3002_21);
               bufferBuilder2.vertex(local3002_15 + local3002_27 * local3002_13, local3002_17 + local3002_18, local3002_16 + local3002_28 * local3002_13)
                  .color(local3002_21);
               local3002_23++;
            }

            if (local3002_23 > 0) {
               try (BuiltBuffer builtBuffer2 = bufferBuilder2.end()) {
                  this.renderElement9(matrixStack, null, builtBuffer2, local3002_23, local3002_22, false);
               }
            }
         } else {
            float local3002_8 = this.floatVal3 * 2.4F;
            float local3002_9 = 0.5F + 0.5F * MathHelper.sin(local3002_8);
            float local3002_10 = -MathHelper.cos(local3002_8);
            float local3002_11 = this.floatVal3 * 1.6F;
            float local3002_12 = (float)(
               MathHelper.lerp(local3002_3, livingEntity.lastRenderX, livingEntity.getX()) - mc.gameRenderer.getCamera().getCameraPos().x
            );
            float local3002_13 = (float)(
               MathHelper.lerp(local3002_3, livingEntity.lastRenderZ, livingEntity.getZ()) - mc.gameRenderer.getCamera().getCameraPos().z
            );
            float local3002_14 = (float)(
               MathHelper.lerp(local3002_3, livingEntity.lastRenderY, livingEntity.getY())
                  + (livingEntity.getHeight() + 0.1F) * local3002_9
                  - mc.gameRenderer.getCamera().getCameraPos().y
            );
            float local3002_15 = livingEntity.getWidth() * 0.9F + 1.0F * (1.0F - local3002_4);
            float local3002_16 = livingEntity.getHeight() * 0.006F;
            int local3002_17 = Colors.computeInt6(this.computeInt3(0.0F, this.floatVal4), local3002_4);
            int local3002_18 = Colors.computeInt6(local3002_17, 0.28F);
            int local3002_19 = Colors.computeInt6(local3002_17, 0.03F);
            int local3002_20 = Colors.computeInt6(local3002_17, 0.12F);
            boolean local3002_21 = RaycastUtils.matchesCriteria(mc.player, livingEntity);
            int local3002_22 = 0;
            int local3002_23 = 0;
            BufferAllocator bufferAllocator3 = this.getBufferAllocator(0, 8192 * VertexFormats.POSITION_COLOR.getVertexSize());
            BufferAllocator bufferAllocator4 = this.getBufferAllocator(1, 48 * VertexFormats.POSITION_TEXTURE_COLOR.getVertexSize());
            BufferBuilder bufferBuilder3 = new BufferBuilder(bufferAllocator3, DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            BufferBuilder bufferBuilder4 = new BufferBuilder(bufferAllocator4, DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            Vector3f vector3f = new Vector3f();

            for (int index4 = 0; index4 < 360; index4 += 3) {
               float local3002_30 = MathHelper.cos((float)Math.toRadians(index4));
               float local3002_31 = MathHelper.sin((float)Math.toRadians(index4));
               float local3002_32 = MathHelper.cos((float)Math.toRadians(index4 + 3));
               float local3002_33 = MathHelper.sin((float)Math.toRadians(index4 + 3));
               bufferBuilder3.vertex(
                     local3002_12 + local3002_30 * (local3002_15 - local3002_16), local3002_14, local3002_13 + local3002_31 * (local3002_15 - local3002_16)
                  )
                  .color(local3002_17);
               bufferBuilder3.vertex(
                     local3002_12 + local3002_32 * (local3002_15 - local3002_16), local3002_14, local3002_13 + local3002_33 * (local3002_15 - local3002_16)
                  )
                  .color(local3002_17);
               bufferBuilder3.vertex(
                     local3002_12 + local3002_32 * (local3002_15 + local3002_16), local3002_14, local3002_13 + local3002_33 * (local3002_15 + local3002_16)
                  )
                  .color(local3002_17);
               bufferBuilder3.vertex(
                     local3002_12 + local3002_30 * (local3002_15 + local3002_16), local3002_14, local3002_13 + local3002_31 * (local3002_15 + local3002_16)
                  )
                  .color(local3002_17);
               local3002_22++;
               float local3002_34 = (0.45F + 0.12F * MathHelper.sin((float)Math.toRadians(index4) * 3.0F - local3002_11)) * local3002_10;
               float local3002_35 = (0.45F + 0.12F * MathHelper.sin((float)Math.toRadians(index4 + 3) * 3.0F - local3002_11)) * local3002_10;
               bufferBuilder3.vertex(local3002_12 + local3002_30 * local3002_15, local3002_14, local3002_13 + local3002_31 * local3002_15).color(local3002_18);
               bufferBuilder3.vertex(local3002_12 + local3002_32 * local3002_15, local3002_14, local3002_13 + local3002_33 * local3002_15).color(local3002_18);
               bufferBuilder3.vertex(local3002_12 + local3002_32 * local3002_15, local3002_14 + local3002_35, local3002_13 + local3002_33 * local3002_15)
                  .color(local3002_19);
               bufferBuilder3.vertex(local3002_12 + local3002_30 * local3002_15, local3002_14 + local3002_34, local3002_13 + local3002_31 * local3002_15)
                  .color(local3002_19);
               local3002_22++;
            }

            for (int index5 = 0; index5 < 360; index5 += 7) {
               this.processData3(
                  bufferBuilder4,
                  mc.gameRenderer.getCamera().getRotation(),
                  vector3f,
                  local3002_12 + MathHelper.cos((float)Math.toRadians(index5)) * local3002_15,
                  local3002_14,
                  local3002_13 + MathHelper.sin((float)Math.toRadians(index5)) * local3002_15,
                  local3002_15 * 0.2F,
                  0.0F,
                  local3002_20
               );
               local3002_23++;
            }

            if (local3002_23 > 0) {
               try (BuiltBuffer builtBuffer3 = bufferBuilder4.end()) {
                  this.renderElement9(matrixStack, "textures/particles/glow.png", builtBuffer3, local3002_23, local3002_21, false);
               }
            }

            if (local3002_22 > 0) {
               try (BuiltBuffer builtBuffer4 = bufferBuilder3.end()) {
                  this.renderElement9(matrixStack, null, builtBuffer4, local3002_22, local3002_21, false);
               }
            }
         }
      }
   }

   private int computeInt4(
      float local3003_1,
      float local3003_2,
      float local3003_3,
      float local3003_4,
      float local3003_5,
      float local3003_6,
      int local3003_7,
      int local3003_8,
      float local3003_9,
      boolean local3003_10
   ) {
      this.list.clear();
      this.intVal = 0;
      this.processData2(local3003_1, local3003_2, local3003_3);
      this.processData(local3003_1, local3003_2, local3003_3, local3003_4, local3003_5, local3003_6, 4, local3003_7, 0, local3003_8, local3003_9);
      int local3003_11 = this.computeInt2(1.0F, local3003_10);

      for (int index = 0; index < this.list.size(); index++) {
         float[] floatArray = this.list.get(index);
         this.intVal = 0;
         this.processData2(floatArray[0], floatArray[1], floatArray[2]);
         this.processData(
            floatArray[0],
            floatArray[1],
            floatArray[2],
            floatArray[3],
            floatArray[4],
            floatArray[5],
            (int)floatArray[6],
            (int)floatArray[7],
            (int)floatArray[8],
            local3003_8,
            local3003_9
         );
         local3003_11 += this.computeInt2(floatArray[9], true);
      }

      this.list.clear();
      return local3003_11;
   }

   private float computeFloat3(LivingEntity livingEntity, float local3004_2, float local3004_3) {
      return (
            MathHelper.clamp(local3004_2, 0.0F, 1.0F) < 0.48F
               ? MathHelper.lerp(
                  this.computeFloat(0.24F, 0.46F, MathHelper.clamp(local3004_2, 0.0F, 1.0F)),
                  (livingEntity.getWidth() * 0.5F + 0.1F) * 1.02F,
                  (livingEntity.getWidth() * 0.5F + 0.1F) * 1.46F
               )
               : (
                  MathHelper.clamp(local3004_2, 0.0F, 1.0F) > 0.68F
                     ? MathHelper.lerp(
                        this.computeFloat(0.68F, 0.84F, MathHelper.clamp(local3004_2, 0.0F, 1.0F)),
                        (livingEntity.getWidth() * 0.5F + 0.1F) * 1.46F,
                        (livingEntity.getWidth() * 0.5F + 0.1F) * 1.16F
                     )
                     : (livingEntity.getWidth() * 0.5F + 0.1F) * 1.46F
               )
         )
         * (0.86F + 0.14F * this.computeFloat(0.0F, 1.0F, local3004_3));
   }

   private void processData2(float local3005_1, float local3005_2, float local3005_3) {
      if (this.intVal * 3 + 3 > this.floatValArray2.length) {
         this.floatValArray2 = Arrays.copyOf(this.floatValArray2, this.floatValArray2.length * 2);
         this.floatValArray3 = Arrays.copyOf(this.floatValArray3, this.floatValArray3.length * 2);
         this.floatValArray4 = Arrays.copyOf(this.floatValArray4, this.floatValArray4.length * 2);
      }

      this.floatValArray2[this.intVal * 3] = local3005_1;
      this.floatValArray2[this.intVal * 3 + 1] = local3005_2;
      this.floatValArray2[this.intVal * 3 + 2] = local3005_3;
      this.intVal++;
   }

   private void processData3(
      BufferBuilder bufferBuilder,
      Quaternionf quaternionf,
      Vector3f vector3f,
      double local3007_4,
      double local3007_6,
      double local3007_8,
      float local3007_10,
      float local3007_11,
      int local3007_12
   ) {
      quaternionf.transform(vector3f.set(-local3007_10, -local3007_10, -local3007_11));
      bufferBuilder.vertex((float)(local3007_4 + vector3f.x), (float)(local3007_6 + vector3f.y), (float)(local3007_8 + vector3f.z))
         .texture(0.0F, 0.0F)
         .color(local3007_12);
      quaternionf.transform(vector3f.set(-local3007_10, local3007_10, -local3007_11));
      bufferBuilder.vertex((float)(local3007_4 + vector3f.x), (float)(local3007_6 + vector3f.y), (float)(local3007_8 + vector3f.z))
         .texture(0.0F, 1.0F)
         .color(local3007_12);
      quaternionf.transform(vector3f.set(local3007_10, local3007_10, -local3007_11));
      bufferBuilder.vertex((float)(local3007_4 + vector3f.x), (float)(local3007_6 + vector3f.y), (float)(local3007_8 + vector3f.z))
         .texture(1.0F, 1.0F)
         .color(local3007_12);
      quaternionf.transform(vector3f.set(local3007_10, -local3007_10, -local3007_11));
      bufferBuilder.vertex((float)(local3007_4 + vector3f.x), (float)(local3007_6 + vector3f.y), (float)(local3007_8 + vector3f.z))
         .texture(1.0F, 0.0F)
         .color(local3007_12);
   }

   private void renderElement9(MatrixStack matrixStack, String local3010_2, BuiltBuffer builtBuffer, int local3010_4, boolean local3010_5, boolean local3010_6) {
      if (mc.getFramebuffer() != null && mc.getFramebuffer().getColorAttachmentView() != null) {
         Identifier identifier = local3010_2 == null
            ? null
            : this.map3.computeIfAbsent(local3010_2, local3015_0 -> Identifier.of("horus", local3015_0));
         if (identifier == null || mc.getTextureManager().getTexture(identifier).getGlTextureView() != null) {
            int local3010_8 = this.intVal4++ % this.gpuBufferArray.length;
            GpuBuffer gpuBuffer = this.gpuBufferArray[local3010_8];
            if (gpuBuffer != null && !gpuBuffer.isClosed() && gpuBuffer.size() >= builtBuffer.getBuffer().remaining()) {
               RenderSystem.getDevice()
                  .createCommandEncoder()
                  .writeToBuffer(gpuBuffer.slice(0L, builtBuffer.getBuffer().remaining()), builtBuffer.getBuffer());
            } else {
               if (gpuBuffer != null && !gpuBuffer.isClosed()) {
                  gpuBuffer.close();
               }

               gpuBuffer = this.gpuBufferArray[local3010_8] = RenderSystem.getDevice()
                  .createBuffer(() -> "horus_target_quads_vertices", 40, builtBuffer.getBuffer());
            }

            GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(matrixStack.peek().getPositionMatrix(), vector4f, vector3f2, matrix4f);

            try (RenderPass renderPass = RenderSystem.getDevice()
                  .createCommandEncoder()
                  .createRenderPass(
                     () -> "horus_target_quads",
                     mc.getFramebuffer().getColorAttachmentView(),
                     OptionalInt.empty(),
                     local3010_5 ? DepthSnapshot.getGpuTextureView() : null,
                     OptionalDouble.empty()
                  )) {
               renderPass.setPipeline(local3010_2 == null ? (local3010_5 ? pipeline5 : pipeline4) : (local3010_5 ? pipeline3 : pipeline2));
               if (identifier != null) {
                  renderPass.bindTexture(
                     "Sampler0",
                     mc.getTextureManager().getTexture(identifier).getGlTextureView(),
                     local3010_6 ? RenderSystem.getSamplerCache().getRepeated(FilterMode.LINEAR) : RenderSystem.getSamplerCache().get(FilterMode.LINEAR)
                  );
               }

               renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
               renderPass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
               renderPass.setVertexBuffer(0, gpuBuffer);
               renderPass.setIndexBuffer(
                  RenderSystem.getSequentialBuffer(DrawMode.QUADS).getIndexBuffer(local3010_4 * 6),
                  RenderSystem.getSequentialBuffer(DrawMode.QUADS).getIndexType()
               );
               renderPass.drawIndexed(0, 0, local3010_4 * 6, 1);
            }
         }
      }
   }

   private void renderElement10(MatrixStack matrixStack, LivingEntity livingEntity, float local3011_3, float local3011_4, float local3011_5) {
      this.floatVal8 = MathHelper.lerp(0.22F, this.floatVal8, livingEntity.hurtTime > 0 ? 0.0F : 1.0F);
      float local3011_6 = (2.0F - local3011_4 + 0.3F * this.floatVal8) / 2.0F;
      Quaternionf quaternionf = new Quaternionf(mc.gameRenderer.getCamera().getRotation())
         .rotateZ((float)Math.toRadians((float)(System.currentTimeMillis() % 3000L) / 3000.0F * 360.0F));
      matrixStack.push();
      matrixStack.translate(
         livingEntity.getLerpedPos(local3011_3).x - mc.gameRenderer.getCamera().getCameraPos().x,
         livingEntity.getLerpedPos(local3011_3).y + livingEntity.getHeight() * 0.5F - mc.gameRenderer.getCamera().getCameraPos().y,
         livingEntity.getLerpedPos(local3011_3).z - mc.gameRenderer.getCamera().getCameraPos().z
      );
      BufferAllocator bufferAllocator = this.getBufferAllocator(1, 8 * VertexFormats.POSITION_TEXTURE_COLOR.getVertexSize());
      BufferBuilder bufferBuilder = new BufferBuilder(bufferAllocator, DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      Vector3f vector3f = new Vector3f();

      for (float local3011_14 : new float[]{1.0F, 0.5F}) {
         quaternionf.transform(vector3f.set(-local3011_6, -local3011_6, 0.0F));
         bufferBuilder.vertex(vector3f.x, vector3f.y, vector3f.z)
            .texture(0.0F, 0.0F)
            .color(Colors.computeInt6(this.computeInt3(270.0F, local3011_5), local3011_4 * local3011_14));
         quaternionf.transform(vector3f.set(-local3011_6, local3011_6, 0.0F));
         bufferBuilder.vertex(vector3f.x, vector3f.y, vector3f.z)
            .texture(0.0F, 1.0F)
            .color(Colors.computeInt6(this.computeInt3(0.0F, local3011_5), local3011_4 * local3011_14));
         quaternionf.transform(vector3f.set(local3011_6, local3011_6, 0.0F));
         bufferBuilder.vertex(vector3f.x, vector3f.y, vector3f.z)
            .texture(1.0F, 1.0F)
            .color(Colors.computeInt6(this.computeInt3(180.0F, local3011_5), local3011_4 * local3011_14));
         quaternionf.transform(vector3f.set(local3011_6, -local3011_6, 0.0F));
         bufferBuilder.vertex(vector3f.x, vector3f.y, vector3f.z)
            .texture(1.0F, 0.0F)
            .color(Colors.computeInt6(this.computeInt3(90.0F, local3011_5), local3011_4 * local3011_14));
      }

      try (BuiltBuffer builtBuffer = bufferBuilder.end()) {
         this.renderElement9(matrixStack, "textures/particles/target.png", builtBuffer, 2, false, false);
      }

      matrixStack.pop();
   }

   @Override
   protected void onDisable() {
      this.livingEntity2 = null;
      this.floatVal9 = 0.0F;
      this.floatVal10 = 0.0F;
      this.animation2.getAnimatedValue(0.0);
      this.floatVal = 0.0F;
      this.timeMillis = System.currentTimeMillis();
      this.floatVal2 = 0.0F;
      this.floatVal3 = 0.0F;
      this.timeMillis2 = System.currentTimeMillis();
      this.floatVal4 = 0.0F;
      this.livingEntity3 = null;
      this.timeMillis3 = 0L;
      this.timeMillis4 = 0L;
      this.floatVal8 = 0.0F;
      Arrays.fill(this.floatValArray5, 0.0F);
      this.timeMillis7 = System.currentTimeMillis();
      if (this.gpuBuffer2 != null && !this.gpuBuffer2.isClosed()) {
         this.gpuBuffer2.close();
      }

      this.gpuBuffer2 = null;

      for (int index = 0; index < this.gpuBufferArray.length; index++) {
         if (this.gpuBufferArray[index] != null && !this.gpuBufferArray[index].isClosed()) {
            this.gpuBufferArray[index].close();
         }

         this.gpuBufferArray[index] = null;
      }

      this.intVal4 = 0;

      for (int index2 = 0; index2 < this.bufferAllocatorArray.length; index2++) {
         if (this.bufferAllocatorArray[index2] != null) {
            this.bufferAllocatorArray[index2].close();
         }

         this.bufferAllocatorArray[index2] = null;
      }
   }

   public static TargetEspModule getInstance() {
      if (targetEspModule == null) {
         targetEspModule = new TargetEspModule();
      }

      return targetEspModule;
   }

   private void renderElement11(MatrixStack matrixStack, LivingEntity livingEntity, float local3016_3, float local3016_4, float local3016_5) {
      long local3016_6 = System.currentTimeMillis();
      float local3016_8 = (float)Math.min(local3016_6 - this.timeMillis7, 100L) / 1000.0F;
      this.timeMillis7 = local3016_6;
      float local3016_9 = livingEntity instanceof PlayerEntity ? EntityUtils.getTotalHealth(livingEntity) : livingEntity.getHealth();
      int local3016_10 = local3016_9 < 8.0F ? 2 : (local3016_9 < 13.0F ? 1 : 0);

      for (int index = 0; index < this.floatValArray5.length; index++) {
         this.floatValArray5[index] = MathHelper.clamp(
            this.floatValArray5[index] + (index == local3016_10 ? local3016_8 : -local3016_8) / 0.25F, 0.0F, 1.0F
         );
      }

      float local3016_11 = 0.42F;
      double local3016_12 = MathHelper.lerp(local3016_3, livingEntity.lastRenderX, livingEntity.getX()) - mc.gameRenderer.getCamera().getCameraPos().x;
      double local3016_14 = MathHelper.lerp(local3016_3, livingEntity.lastRenderY, livingEntity.getY())
         + livingEntity.getHeight() * 0.55F
         - mc.gameRenderer.getCamera().getCameraPos().y;
      double local3016_16 = MathHelper.lerp(local3016_3, livingEntity.lastRenderZ, livingEntity.getZ()) - mc.gameRenderer.getCamera().getCameraPos().z;
      Quaternionf rotation = mc.gameRenderer.getCamera().getRotation();
      int local3016_19 = Colors.computeInt6(Colors.computeInt2(ThemeColors.intVal9, -49088, local3016_5), local3016_4);
      Vector3f vector3f = new Vector3f();
      BufferBuilder bufferBuilder = new BufferBuilder(
         this.getBufferAllocator(1, 4 * VertexFormats.POSITION_TEXTURE_COLOR.getVertexSize()), DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR
      );
      this.processData3(
         bufferBuilder,
         rotation,
         vector3f,
         local3016_12,
         local3016_14,
         local3016_16,
         local3016_11 * 1.6F,
         0.0F,
         Colors.computeInt6(local3016_19, 0.22F)
      );

      try (BuiltBuffer builtBuffer = bufferBuilder.end()) {
         this.renderElement9(matrixStack, "textures/particles/glow.png", builtBuffer, 1, false, false);
      }

      String[] stringArray = new String[]{"textures/img.png", "textures/img_1.png", "textures/img_2.png"};

      for (int index2 = 0; index2 < stringArray.length; index2++) {
         if (!(this.floatValArray5[index2] < 0.003921569F)) {
            int local3016_24 = Colors.computeInt6(local3016_19, this.floatValArray5[index2]);
            BufferBuilder bufferBuilder2 = new BufferBuilder(
               this.getBufferAllocator(1, 4 * VertexFormats.POSITION_TEXTURE_COLOR.getVertexSize()), DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR
            );
            rotation.transform(vector3f.set(-local3016_11, -local3016_11, 0.0F));
            bufferBuilder2.vertex((float)(local3016_12 + vector3f.x), (float)(local3016_14 + vector3f.y), (float)(local3016_16 + vector3f.z))
               .texture(0.0F, 1.0F)
               .color(local3016_24);
            rotation.transform(vector3f.set(-local3016_11, local3016_11, 0.0F));
            bufferBuilder2.vertex((float)(local3016_12 + vector3f.x), (float)(local3016_14 + vector3f.y), (float)(local3016_16 + vector3f.z))
               .texture(0.0F, 0.0F)
               .color(local3016_24);
            rotation.transform(vector3f.set(local3016_11, local3016_11, 0.0F));
            bufferBuilder2.vertex((float)(local3016_12 + vector3f.x), (float)(local3016_14 + vector3f.y), (float)(local3016_16 + vector3f.z))
               .texture(1.0F, 0.0F)
               .color(local3016_24);
            rotation.transform(vector3f.set(local3016_11, -local3016_11, 0.0F));
            bufferBuilder2.vertex((float)(local3016_12 + vector3f.x), (float)(local3016_14 + vector3f.y), (float)(local3016_16 + vector3f.z))
               .texture(1.0F, 1.0F)
               .color(local3016_24);

            try (BuiltBuffer builtBuffer2 = bufferBuilder2.end()) {
               this.renderElement9(matrixStack, stringArray[index2], builtBuffer2, 1, false, false);
            }
         }
      }
   }

   private float computeFloat4(int local3017_1, int local3017_2, int local3017_3) {
      int local3017_4 = local3017_1 * 668265261 + local3017_2 * 374761393 + local3017_3 * -1640531535;
      local3017_4 ^= local3017_4 >>> 15;
      local3017_4 *= -2048144789;
      local3017_4 ^= local3017_4 >>> 13;
      return (local3017_4 >>> 8) / 8388608.0F - 1.0F;
   }

   private TargetEspModule() {
      this.mode = this.addSetting(
         new ModeSetting("Мод", "Какой эффект рисовать на цели.", "Призраки", "Призраки", "Молнии", "Круг", "Кристаллы", "Ромб", "Цепи", "Череп")
      );
      this.ghostStyle = this.addSetting(new ModeSetting("Стиль призраков", "Какими рисовать призраков на цели.", "Второй", "Второй"));
      this.ghostStyle.withVisibility(() -> false);
      this.circleStyle = this.addSetting(new ModeSetting("Стиль круга", "Каким рисовать круг на цели.", "Второй", "Второй"));
      this.circleStyle.withVisibility(() -> false);
      this.trident = this.addSetting(
         new BooleanSetting("Трезубец", "Тянет цепь от руки к летящим трезубцу и стрелам.", true).withVisibility(() -> this.mode.isSelected("Цепи"))
      );
      this.projectiles = this.addSetting(
         new BooleanSetting("Снаряды", "Тянет молнию от руки к летящим трезубцу и стрелам.", true).withVisibility(() -> this.mode.isSelected("Молнии"))
      );
   }

   private void renderElement12(MatrixStack matrixStack, float local3019_2) {
      long local3019_3 = System.currentTimeMillis();
      float local3019_5 = (float)Math.min(local3019_3 - this.timeMillis, 100L) / 1000.0F;
      this.floatVal += local3019_5;
      this.timeMillis = local3019_3;
      this.map.keySet().removeIf(local2998_0 -> mc.world.getEntityById(local2998_0) == null);
      int local3019_6 = (int)this.floatVal;
      float local3019_7 = this.floatVal - local3019_6;
      float local3019_8 = this.floatVal * 1.7F - (int)(this.floatVal * 1.7F);
      float local3019_9 = (float)(
         MathHelper.lerp(local3019_2, mc.player.lastRenderX, mc.player.getX()) - mc.gameRenderer.getCamera().getCameraPos().x
      );
      float local3019_10 = (float)(
         MathHelper.lerp(local3019_2, mc.player.lastRenderY, mc.player.getY())
            + mc.player.getStandingEyeHeight() * 0.75F
            - mc.gameRenderer.getCamera().getCameraPos().y
      );
      float local3019_11 = (float)(
         MathHelper.lerp(local3019_2, mc.player.lastRenderZ, mc.player.getZ()) - mc.gameRenderer.getCamera().getCameraPos().z
      );
      this.floatVal5 = 0.0037F;
      this.floatVal7 = 0.047F;
      this.quaternionf2 = mc.gameRenderer.getCamera().getRotation();

      for (Entity entity : mc.world.getEntities()) {
         if (entity instanceof PersistentProjectileEntity entity2 && entity2.getOwner() == mc.player) {
            boolean local3019_15 = !entity2.isOnGround() && entity2.getVelocity().lengthSquared() > 1.0E-5;
            float[] floatArray = this.map.computeIfAbsent(entity2.getId(), local3009_0 -> new float[1]);
            floatArray[0] = MathHelper.clamp(floatArray[0] + (local3019_15 ? local3019_5 / 0.15F : -local3019_5 / 0.3F), 0.0F, 1.0F);
            if (!(floatArray[0] < 0.003921569F)) {
               float local3019_17 = (float)(
                  MathHelper.lerp(local3019_2, entity2.lastRenderX, entity2.getX()) - mc.gameRenderer.getCamera().getCameraPos().x
               );
               float local3019_18 = (float)(
                  MathHelper.lerp(local3019_2, entity2.lastRenderY, entity2.getY()) - mc.gameRenderer.getCamera().getCameraPos().y
               );
               float local3019_19 = (float)(
                  MathHelper.lerp(local3019_2, entity2.lastRenderZ, entity2.getZ()) - mc.gameRenderer.getCamera().getCameraPos().z
               );
               float local3019_20 = local3019_17 - local3019_9;
               float local3019_21 = local3019_18 - local3019_10;
               float local3019_22 = local3019_19 - local3019_11;
               float local3019_23 = MathHelper.sqrt(local3019_20 * local3019_20 + local3019_21 * local3019_21 + local3019_22 * local3019_22);
               if (!(local3019_23 < 1.0F)) {
                  float local3019_24 = local3019_20 / local3019_23;
                  float local3019_25 = local3019_21 / local3019_23;
                  float local3019_26 = local3019_22 / local3019_23;
                  float local3019_27 = Math.abs(local3019_25) < 0.9F ? 0.0F : 1.0F;
                  float local3019_28 = Math.abs(local3019_25) < 0.9F ? 1.0F : 0.0F;
                  float local3019_29 = -local3019_26 * local3019_28;
                  float local3019_30 = local3019_26 * local3019_27;
                  float local3019_31 = local3019_24 * local3019_28 - local3019_25 * local3019_27;
                  float local3019_32 = MathHelper.sqrt(local3019_29 * local3019_29 + local3019_30 * local3019_30 + local3019_31 * local3019_31);
                  if (local3019_32 < 1.0E-5F) {
                     local3019_32 = 1.0F;
                  }

                  local3019_29 /= local3019_32;
                  local3019_30 /= local3019_32;
                  local3019_31 /= local3019_32;
                  float local3019_33 = local3019_25 * local3019_31 - local3019_26 * local3019_30;
                  float local3019_34 = local3019_26 * local3019_29 - local3019_24 * local3019_31;
                  float local3019_35 = local3019_24 * local3019_30 - local3019_25 * local3019_29;
                  float local3019_36 = MathHelper.clamp(0.8F + 0.2F * this.computeFloat2(entity2.getId(), 900, local3019_6, local3019_7), 0.0F, 1.0F);
                  int local3019_37 = MathHelper.clamp((int)Math.ceil(local3019_23 / 2.2F), 2, 24);
                  int local3019_38 = 0;
                  BufferAllocator bufferAllocator = this.getBufferAllocator(0, 2048 * VertexFormats.POSITION_COLOR.getVertexSize());
                  BufferAllocator bufferAllocator2 = this.getBufferAllocator(1, 2048 * VertexFormats.POSITION_TEXTURE_COLOR.getVertexSize());
                  this.bufferBuilder5 = new BufferBuilder(bufferAllocator, DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                  this.bufferBuilder6 = new BufferBuilder(bufferAllocator2, DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                  float local3019_42 = local3019_9;
                  float local3019_43 = local3019_10;
                  float local3019_44 = local3019_11;

                  for (int index = 1; index <= local3019_37; index++) {
                     float local3019_46 = (float)index / local3019_37;
                     float local3019_47 = MathHelper.sin((float) Math.PI * local3019_46);
                     float local3019_48 = local3019_46 * 7.0F + this.floatVal * 4.0F;
                     float local3019_49 = Math.min(0.3F, local3019_23 * 0.06F) * local3019_47;
                     float local3019_50 = local3019_9
                        + local3019_20 * local3019_46
                        + (local3019_29 * MathHelper.sin(local3019_48) + local3019_33 * MathHelper.cos(local3019_48)) * local3019_49;
                     float local3019_51 = local3019_10
                        + local3019_21 * local3019_46
                        + (local3019_30 * MathHelper.sin(local3019_48) + local3019_34 * MathHelper.cos(local3019_48)) * local3019_49
                        - Math.min(0.6F, local3019_23 * 0.035F) * local3019_47;
                     float local3019_52 = local3019_11
                        + local3019_22 * local3019_46
                        + (local3019_31 * MathHelper.sin(local3019_48) + local3019_35 * MathHelper.cos(local3019_48)) * local3019_49;
                     float local3019_53 = Math.abs((index - 0.5F) / local3019_37 - local3019_8);
                     local3019_53 = Math.min(local3019_53, 1.0F - local3019_53);
                     float local3019_54 = Math.max(0.0F, 1.0F - local3019_53 / 0.22F);
                     this.intVal2 = Colors.computeInt6(
                        Colors.computeInt7(Colors.computeInt2(UiTheme.intVal7, -1, 0.4F + 0.6F * local3019_54), 255),
                        (0.5F + 0.5F * local3019_54 * local3019_54) * local3019_36 * floatArray[0]
                     );
                     this.intVal3 = Colors.computeInt6(this.intVal2, 0.13F);
                     local3019_38 += this.computeInt4(
                        local3019_42,
                        local3019_43,
                        local3019_44,
                        local3019_50,
                        local3019_51,
                        local3019_52,
                        (entity2.getId() * 131 + index) * 17 | 1,
                        local3019_6,
                        local3019_7,
                        index == local3019_37
                     );
                     local3019_42 = local3019_50;
                     local3019_43 = local3019_51;
                     local3019_44 = local3019_52;
                  }

                  int local3019_39 = local3019_38;
                  this.processData3(
                     this.bufferBuilder6,
                     this.quaternionf2,
                     this.vector3f3,
                     local3019_17,
                     local3019_18,
                     local3019_19,
                     0.067F + 0.02F * MathHelper.sin(this.floatVal * 9.0F + entity2.getId()),
                     0.0F,
                     Colors.computeInt6(
                        Colors.computeInt7(Colors.computeInt2(UiTheme.intVal7, -1, 0.7F), 255),
                        0.45F * local3019_36 * floatArray[0]
                     )
                  );
                  local3019_39++;
                  boolean local3019_45 = RaycastUtils.matchesCriteria(mc.player, entity2);

                  try (BuiltBuffer builtBuffer = this.bufferBuilder6.end()) {
                     this.renderElement9(matrixStack, "textures/particles/glow.png", builtBuffer, local3019_39, local3019_45, false);
                  }

                  if (local3019_38 > 0) {
                     try (BuiltBuffer builtBuffer2 = this.bufferBuilder5.end()) {
                        this.renderElement9(matrixStack, null, builtBuffer2, local3019_38, local3019_45, false);
                     }
                  }
               }
            }
         }
      }
   }

   public record DataRecord(int primaryColor, int secondaryColor, float phase, float alpha, float hit, float glow) {
   }
}

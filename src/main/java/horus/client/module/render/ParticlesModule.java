package horus.client.module.render;

import horus.client.setting.BooleanSetting;
import horus.client.util.Colors;
import horus.client.HorusClient;
import horus.client.util.DepthSnapshot;
import horus.client.event.EventListener;
import horus.client.setting.LegacyNumberSetting;
import horus.client.MinecraftAccess;
import horus.client.setting.ModeSetting;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.MultiSelectSetting;
import horus.client.event.PacketEvent;
import horus.client.event.PlayerTickEvent;
import horus.client.render.WorldProjection;
import horus.client.render.ThemePaletteBuffer;
import horus.client.setting.ColorSetting;
import horus.client.setting.SectionSetting;
import horus.client.event.WorldRenderEvent;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.BufferAllocator.CloseableBuffer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

@ModuleInfo(name = "Particles", description = "Частицы вокруг игрока: метеоры, фигуры и хит эффекты.", category = ModuleCategory.RENDER)
public final class ParticlesModule extends Module {
   private static ParticlesModule particlesModule;
   private static final Vector4f vector4f = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f vector3f5 = new Vector3f();
   private static final Matrix4f matrix4f = new Matrix4f();
   private static final RenderPipeline pipeline2 = RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
      .withLocation(HorusClient.id("pipeline/particles_glow"))
      .withVertexShader(Identifier.ofVanilla("core/position_tex_color"))
      .withFragmentShader(Identifier.ofVanilla("core/position_tex_color"))
      .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, DrawMode.QUADS)
      .withBlend(BlendFunction.LIGHTNING)
      .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
      .withDepthWrite(false)
      .withCull(false)
      .withSampler("Sampler0")
      .build();
   private static final RenderPipeline pipeline3 = RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
      .withLocation(HorusClient.id("pipeline/particles_wires"))
      .withVertexShader(Identifier.ofVanilla("core/position_color"))
      .withFragmentShader(Identifier.ofVanilla("core/position_color"))
      .withVertexFormat(VertexFormats.POSITION_COLOR, DrawMode.QUADS)
      .withBlend(BlendFunction.LIGHTNING)
      .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
      .withDepthWrite(false)
      .withCull(false)
      .build();
   private static final int[] intValArray = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 0, 2, 1, 3, 4, 6, 5, 7, 0, 4, 1, 5, 2, 6, 3, 7};
   private static final int[] intValArray2 = new int[]{0, 1, 1, 2, 2, 3, 3, 0, 0, 4, 1, 4, 2, 4, 3, 4};
   private static final int[] intValArray3 = new int[]{0, 2, 0, 3, 0, 4, 0, 5, 1, 2, 1, 3, 1, 4, 1, 5, 2, 5, 5, 3, 3, 4, 4, 2};
   private static final int[] intValArray4 = new int[]{0, 1, 0, 2, 0, 3, 1, 2, 2, 3, 3, 1};
   public MultiSelectSetting effects;
   public LegacyNumberSetting count;
   public LegacyNumberSetting lifetime;
   public LegacyNumberSetting size;
   public LegacyNumberSetting spawnMin;
   public LegacyNumberSetting spawnMax;
   public LegacyNumberSetting shapeHeight;
   public LegacyNumberSetting glow;
   public ModeSetting modeSetting;
   public MultiSelectSetting worldImages;
   public LegacyNumberSetting worldCount;
   public LegacyNumberSetting worldSize;
   public LegacyNumberSetting distanceMin;
   public LegacyNumberSetting distanceMax;
   public LegacyNumberSetting worldHeight;
   public BooleanSetting hitParticles;
   public MultiSelectSetting images;
   public LegacyNumberSetting count2;
   public LegacyNumberSetting hitSize;
   public BooleanSetting onlyCrits;
   public BooleanSetting customColor;
   public ColorSetting colorSetting;
   private final ArrayList<ParticlesModule.SubContext> list = new ArrayList<>();
   private final ArrayList<ParticlesModule.SubContext> list2 = new ArrayList<>();
   private final ArrayList<ParticlesModule.SubContext> list3 = new ArrayList<>();
   private final ArrayList<ParticlesModule.SubContext> list4 = new ArrayList<>();
   private final ArrayList<ParticlesModule.SubContext> list5 = new ArrayList<>();
   private final ArrayList<ParticlesModule.SubContext> list6 = new ArrayList<>();
   private final ArrayList<ParticlesModule.SubContext> list7 = new ArrayList<>();
   private final ArrayList<ParticlesModule.SubContext4> list8 = new ArrayList<>();
   private final ArrayList<ParticlesModule.SubContext4> list9 = new ArrayList<>();
   private final ConcurrentLinkedQueue<ParticlesModule.DataRecord5> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();
   private long timeMillis;
   private final HashMap<String, GpuBuffer> map = new HashMap<>();
   private GpuBuffer gpuBuffer2;
   private final Matrix3f matrix3f = new Matrix3f();
   private final Vector3f[] vector3fArray = new Vector3f[8];
   private final Vector3f vector3f6 = new Vector3f();
   private final Vector3f vector3f7 = new Vector3f();
   private float[] floatValArray;
   private float[] floatValArray2;
   private float[] floatValArray3;
   private float[] floatValArray4;
   private float[] floatValArray5;
   private boolean flag;
   private boolean flag2;
   private boolean flag3;
   private boolean flag4;
   private volatile boolean flag5;
   private BufferAllocator bufferAllocator;
   private final int[] intValArray5;
   private final ArrayList<ParticlesModule.SubContext4> list10;
   private final EventListener<PacketEvent> packetListener;
   private final EventListener<PlayerTickEvent> playerTickListener;
   private final EventListener<WorldRenderEvent> worldRenderListener;
   private static final String[] strValArray = new String[]{"Светлячок", "Звезда", "Сердце", "Снежинка", "Доллар"};

   private boolean checkState() {
      return mc.player != null
         && mc.player.fallDistance > 0.0
         && !mc.player.isOnGround()
         && !mc.player.isClimbing()
         && !mc.player.isTouchingWater()
         && !mc.player.hasVehicle()
         && !mc.player.isSprinting();
   }

   private void processData(
      BufferBuilder bufferBuilder,
      Quaternionf quaternionf,
      Vector3f vector3f,
      double local717_4,
      double local717_6,
      double local717_8,
      float local717_10,
      int local717_11
   ) {
      double local717_12 = local717_4 - mc.gameRenderer.getCamera().getCameraPos().x;
      double local717_14 = local717_6 - mc.gameRenderer.getCamera().getCameraPos().y;
      double local717_16 = local717_8 - mc.gameRenderer.getCamera().getCameraPos().z;
      quaternionf.transform(vector3f.set(-local717_10, -local717_10, 0.0F));
      bufferBuilder.vertex((float)(local717_12 + vector3f.x), (float)(local717_14 + vector3f.y), (float)(local717_16 + vector3f.z))
         .texture(0.0F, 0.0F)
         .color(local717_11);
      quaternionf.transform(vector3f.set(-local717_10, local717_10, 0.0F));
      bufferBuilder.vertex((float)(local717_12 + vector3f.x), (float)(local717_14 + vector3f.y), (float)(local717_16 + vector3f.z))
         .texture(0.0F, 1.0F)
         .color(local717_11);
      quaternionf.transform(vector3f.set(local717_10, local717_10, 0.0F));
      bufferBuilder.vertex((float)(local717_12 + vector3f.x), (float)(local717_14 + vector3f.y), (float)(local717_16 + vector3f.z))
         .texture(1.0F, 1.0F)
         .color(local717_11);
      quaternionf.transform(vector3f.set(local717_10, -local717_10, 0.0F));
      bufferBuilder.vertex((float)(local717_12 + vector3f.x), (float)(local717_14 + vector3f.y), (float)(local717_16 + vector3f.z))
         .texture(1.0F, 0.0F)
         .color(local717_11);
   }

   private static int computeInt(String local719_0) {
      for (int index = 0; index < strValArray.length; index++) {
         if (strValArray[index].equals(local719_0)) {
            return index;
         }
      }

      return -1;
   }

   private static void processData2(
      JsonArray jsonArray, int local721_1, float local721_2, float local721_3, float local721_4, float[][] floatArrayArray, boolean[] booleanArray
   ) {
      if (local721_1 >= 0 && local721_1 < jsonArray.size() && !booleanArray[local721_1]) {
         booleanArray[local721_1] = true;
         JsonObject asJsonObject = jsonArray.get(local721_1).getAsJsonObject();
         float local721_8 = local721_2;
         float local721_9 = local721_3;
         float local721_10 = local721_4;
         if (asJsonObject.has("translation")) {
            JsonArray asJsonArray = asJsonObject.getAsJsonArray("translation");
            local721_8 = local721_2 + asJsonArray.get(0).getAsFloat();
            local721_9 = local721_3 + asJsonArray.get(1).getAsFloat();
            local721_10 = local721_4 + asJsonArray.get(2).getAsFloat();
         }

         floatArrayArray[local721_1][0] = local721_8;
         floatArrayArray[local721_1][1] = local721_9;
         floatArrayArray[local721_1][2] = local721_10;
         if (asJsonObject.has("children")) {
            JsonArray asJsonArray2 = asJsonObject.getAsJsonArray("children");

            for (int index = 0; index < asJsonArray2.size(); index++) {
               processData2(jsonArray, asJsonArray2.get(index).getAsInt(), local721_8, local721_9, local721_10, floatArrayArray, booleanArray);
            }
         }
      }
   }

   private void processData3(long local723_1) {
      if (this.effects.isSelected("Кубы")
         || this.effects.isSelected("Пирамиды")
         || this.effects.isSelected("Тотем")
         || this.effects.isSelected("Полумесяц")
         || this.effects.isSelected("Сердце")
         || this.effects.isSelected("Звёзды")
         || this.effects.isSelected("Делюкс")) {
         Vec3d vec3d = mc.player.getEntityPos();
         double local723_4 = Math.min(this.spawnMin.getValue(), this.spawnMax.getValue());
         double local723_6 = Math.max(this.spawnMax.getValue(), local723_4 + 0.1);
         int local723_8 = Math.max(1, (int)this.count.getValue());
         if (this.effects.isSelected("Кубы")) {
            this.processData7(this.list, vec3d, local723_4, local723_6, local723_1, local723_8, 6.0F, 6.0F, 6.0F);
         } else {
            this.list.clear();
         }

         if (this.effects.isSelected("Пирамиды")) {
            this.processData7(this.list2, vec3d, local723_4, local723_6, local723_1, local723_8, 7.0F, 7.0F, 7.0F);
         } else {
            this.list2.clear();
         }

         if (this.effects.isSelected("Тотем")) {
            this.processData7(this.list3, vec3d, local723_4, local723_6, local723_1, local723_8, 5.0F, 8.0F, 5.0F);
         } else {
            this.list3.clear();
         }

         if (this.effects.isSelected("Полумесяц")) {
            this.processData7(this.list4, vec3d, local723_4, local723_6, local723_1, local723_8, 6.0F, 6.0F, 6.0F);
         } else {
            this.list4.clear();
         }

         if (this.effects.isSelected("Сердце")) {
            this.processData7(this.list5, vec3d, local723_4, local723_6, local723_1, local723_8, 6.0F, 6.0F, 6.0F);
         } else {
            this.list5.clear();
         }

         if (this.effects.isSelected("Звёзды")) {
            this.processData7(this.list6, vec3d, local723_4, local723_6, local723_1, local723_8, 6.0F, 6.0F, 6.0F);
         } else {
            this.list6.clear();
         }

         if (this.effects.isSelected("Делюкс")) {
            this.processData7(this.list7, vec3d, local723_4, local723_6, local723_1, local723_8, 6.0F, 6.0F, 6.0F);
         } else {
            this.list7.clear();
         }
      }
   }

   private ParticlesModule() {
      this.intValArray5 = new int[strValArray.length];
      this.list10 = new ArrayList<>();
      this.packetListener = packetEvent -> {
         if (packetEvent.isOutgoing() && packetEvent.getPacket() instanceof HandSwingC2SPacket && this.hitParticles.isChecked()) {
            if (mc.crosshairTarget instanceof EntityHitResult entityHitResult
               && entityHitResult.getEntity() instanceof LivingEntity livingEntity
               && entityHitResult.getEntity() != mc.player) {
               if (!this.onlyCrits.isChecked() || this.checkState()) {
                  this.concurrentLinkedQueue.add(new ParticlesModule.DataRecord5(livingEntity.getEntityPos(), livingEntity.getHeight()));
               }
            }
         }
      };
      this.playerTickListener = playerTickEvent -> {
         if (mc.player != null && mc.world != null) {
            long local757_2 = System.currentTimeMillis();
            this.processData9(local757_2);
            this.list8
               .removeIf(local724_2 -> local724_2.matchesCriteria2(local757_2) || WorldProjection.getVec3d2(local724_2.vec3d5, 1.0F, 1.0F) == null);
            if (this.effects.isSelected("Мир")) {
               this.list9
                  .removeIf(
                     local733_3 -> local733_3.matchesCriteria2(local757_2)
                        || local733_3.vec3d5.distanceTo(mc.player.getEntityPos()) > this.distanceMax.getValue() + 10.0
                        || WorldProjection.getVec3d2(local733_3.vec3d5, 1.0F, 1.0F) == null
                  );

               for (int index = 0;
                  index < (int)this.worldCount.getValue() && this.list9.size() < (int)this.worldCount.getValue();
                  index++
               ) {
                  this.processData8(local757_2);
               }
            } else {
               this.list9.clear();
            }

            for (ParticlesModule.SubContext4 local757_5 : this.list8) {
               local757_5.updateState2();
            }

            for (ParticlesModule.SubContext4 local757_5 : this.list9) {
               local757_5.updateState2();
            }

            this.updateState();
            this.processData6(this.list8);
            if (this.effects.isSelected("Мир")) {
               this.processData6(this.list9);
            }
         }
      };
      this.worldRenderListener = worldRenderEvent -> {
         if (mc.player != null && mc.world != null) {
            long local778_2 = System.currentTimeMillis();
            this.processData5(local778_2);
            this.processData3(local778_2);
            this.renderElement4(worldRenderEvent.getMatrixStack(), worldRenderEvent.computeFloat(), local778_2);
            this.renderElement5(this.list8, this.images, this.hitSize.getValue(), worldRenderEvent.getMatrixStack(), worldRenderEvent.computeFloat(), local778_2);
            if (this.effects.isSelected("Мир")) {
               this.renderElement5(this.list9, this.worldImages, this.worldSize.getValue(), worldRenderEvent.getMatrixStack(), worldRenderEvent.computeFloat(), local778_2);
            }

            this.renderElement2(worldRenderEvent.getMatrixStack(), local778_2);
         }
      };
      this.effects = this.addSetting(
         new MultiSelectSetting(
            "Эффекты", "Какие частицы живут вокруг игрока.", new String[]{"Мир", "Кубы", "Пирамиды", "Тотем", "Полумесяц", "Сердце", "Звёзды", "Делюкс"}, "Мир"
         )
      );
      this.count = this.addSetting(new LegacyNumberSetting("Количество", "Сколько частиц каждой фигуры держать вокруг.", 14.0, 1.0, 80.0, 1.0));
      this.lifetime = this.addSetting(new LegacyNumberSetting("Жизнь", "Время жизни частицы в миллисекундах.", 1600.0, 500.0, 5000.0, 100.0));
      this.size = this.addSetting(new LegacyNumberSetting("Размер", "Множитель размера фигур.", 0.45, 0.1, 1.5, 0.05));
      this.spawnMin = this.addSetting(new LegacyNumberSetting("Спавн от", "Ближе этой дистанции фигуры не спавнятся.", 4.0, 0.5, 80.0, 0.5));
      this.spawnMax = this.addSetting(new LegacyNumberSetting("Спавн до", "Дальше этой дистанции фигуры не спавнятся.", 10.0, 1.0, 80.0, 0.5));
      this.shapeHeight = this.addSetting(new LegacyNumberSetting("Высота фигур", "Вертикальный разброс спавна фигур вверх и вниз.", 6.0, 0.5, 40.0, 0.5));
      this.glow = this.addSetting(new LegacyNumberSetting("Свечение", "Сила свечения фигур и картинок.", 1.0, 0.0, 3.0, 0.1));
      this.addSetting(new SectionSetting("Мир"));
      this.modeSetting = this.addSetting(new ModeSetting("Спавн мира", "Где живут мировые частицы.", "Небо", "Небо", "Земля"));
      this.worldImages = this.addSetting(
         new MultiSelectSetting(
            "Картинки мира", "Какие картинки летают в мире.", new String[]{"Светлячок", "Звезда", "Сердце", "Снежинка", "Доллар"}, "Светлячок"
         )
      );
      this.worldCount = this.addSetting(new LegacyNumberSetting("Кол-во в мире", "Сколько частиц держать в мире.", 100.0, 10.0, 300.0, 10.0));
      this.worldSize = this.addSetting(new LegacyNumberSetting("Размер в мире", "Размер мировых картинок.", 0.1, 0.03, 0.5, 0.005));
      this.distanceMin = this.addSetting(new LegacyNumberSetting("Дистанция от", "Ближе этой дистанции мировые частицы не спавнятся.", 2.0, 1.0, 100.0, 1.0));
      this.distanceMax = this.addSetting(new LegacyNumberSetting("Дистанция до", "Дальше этой дистанции мировые частицы не спавнятся.", 16.0, 5.0, 120.0, 1.0));
      this.worldHeight = this.addSetting(new LegacyNumberSetting("Высота мира", "Насколько высоко над игроком спавнятся небесные частицы.", 8.0, 1.5, 40.0, 0.5));
      this.addSetting(new SectionSetting("Хит партиклы"));
      this.hitParticles = this.addSetting(new BooleanSetting("Хит партиклы", "Разлетающиеся картинки при ударе по цели.", false));
      this.images = this.addSetting(
         new MultiSelectSetting(
            "Картинки", "Какие картинки вылетают из цели.", new String[]{"Светлячок", "Звезда", "Сердце", "Снежинка", "Доллар"}, "Светлячок"
         )
      );
      this.count2 = this.addSetting(new LegacyNumberSetting("Кол-во", "Сколько картинок спавнить на удар.", 12.0, 1.0, 50.0, 1.0));
      this.hitSize = this.addSetting(new LegacyNumberSetting("Размер удара", "Размер картинок при ударе.", 0.1, 0.03, 0.5, 0.005));
      this.onlyCrits = this.addSetting(new BooleanSetting("Только криты", "Спавнить хит частицы только на критах.", false));
      this.addSetting(new SectionSetting("Цвет"));
      this.customColor = this.addSetting(new BooleanSetting("Свой цвет", "Красить частицы своим цветом вместо темы клиента.", false));
      this.colorSetting = this.addSetting(new ColorSetting("Цвет", "Кастомный цвет частиц."));
      this.glow.withVisibility(() -> this.effects.getSelectedCount() > 0 || this.hitParticles.isChecked());
      this.modeSetting.withVisibility(() -> this.effects.isSelected("Мир"));
      this.worldImages.withVisibility(() -> this.effects.isSelected("Мир"));
      this.worldCount.withVisibility(() -> this.effects.isSelected("Мир"));
      this.worldSize.withVisibility(() -> this.effects.isSelected("Мир"));
      this.distanceMin.withVisibility(() -> this.effects.isSelected("Мир"));
      this.distanceMax.withVisibility(() -> this.effects.isSelected("Мир"));
      this.worldHeight.withVisibility(() -> this.effects.isSelected("Мир") && this.modeSetting.isSelected("Небо"));
      this.images.withVisibility(() -> this.hitParticles.isChecked());
      this.count2.withVisibility(() -> this.hitParticles.isChecked());
      this.hitSize.withVisibility(() -> this.hitParticles.isChecked());
      this.onlyCrits.withVisibility(() -> this.hitParticles.isChecked());
      this.colorSetting.withVisibility(() -> this.customColor.isChecked());

      for (int index2 = 0; index2 < 8; index2++) {
         this.vector3fArray[index2] = new Vector3f();
      }
   }

   @Override
   protected void onEnable() {
      this.list.clear();
      this.list2.clear();
      this.list3.clear();
      this.list4.clear();
      this.list5.clear();
      this.list6.clear();
      this.list7.clear();
      this.list8.clear();
      this.list9.clear();
      this.concurrentLinkedQueue.clear();
      this.timeMillis = System.currentTimeMillis();
      if (!this.flag5) {
         this.flag5 = true;
         Thread thread = new Thread(() -> {
            this.computeFloat();
            this.computeFloat5();
            this.computeFloat6();
            this.computeFloat2();
            this.computeFloat3();
         }, "horus-particles-models");
         thread.setDaemon(true);
         thread.start();
      }
   }

   private ParticlesModule.SubContext getNested0353(
      Vec3d vec3d, double local727_2, double local727_4, long local727_6, float local727_8, float local727_9, float local727_10
   ) {
      ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
      double local727_12 = threadLocalRandom.nextDouble() * Math.PI * 2.0;
      double local727_14 = local727_2 + threadLocalRandom.nextDouble() * (local727_4 - local727_2);
      double local727_16 = threadLocalRandom.nextDouble() * Math.PI * 2.0;
      double local727_18 = Math.acos(2.0 * threadLocalRandom.nextDouble() - 1.0);
      double local727_20 = 1.6 + threadLocalRandom.nextDouble() * 1.8;
      return new ParticlesModule.SubContext(
         vec3d.add(
            Math.cos(local727_12) * local727_14,
            1.0 + threadLocalRandom.nextDouble(-this.shapeHeight.getValue(), this.shapeHeight.getValue()),
            Math.sin(local727_12) * local727_14
         ),
         local727_12,
         local727_6,
         this.computeLong(this.lifetime.getValue()),
         threadLocalRandom.nextInt(180),
         (threadLocalRandom.nextFloat() - 0.5F) * local727_8,
         (threadLocalRandom.nextFloat() - 0.5F) * local727_9,
         (threadLocalRandom.nextFloat() - 0.5F) * local727_10,
         Math.sin(local727_18) * Math.cos(local727_16) * local727_20,
         Math.cos(local727_18) * local727_20,
         Math.sin(local727_18) * Math.sin(local727_16) * local727_20
      );
   }

   private int computeInt2(BufferBuilder bufferBuilder, long local728_2) {
      float local728_4 = this.size.getValue() * 0.75F;
      int local728_5 = 0;

      for (ParticlesModule.SubContext local728_7 : this.list2) {
         float local728_8 = (float)(local728_2 - local728_7.timeMillis2) / (float)local728_7.timeMillis3;
         if (!(local728_8 >= 1.0F)) {
            Vec3d vec3d = local728_7.getVec3d(local728_8);
            if (WorldProjection.matchesCriteria2(vec3d.x, vec3d.y, vec3d.z, 2.0F)) {
               float local728_10 = local728_4 * (0.65F + local728_8 * 0.55F) * 0.5F;
               this.matrix3f
                  .identity()
                  .rotateY(local728_7.floatVal2 * local728_8)
                  .rotateX(local728_7.floatVal * local728_8)
                  .rotateZ(local728_7.floatVal3 * local728_8);
               this.matrix3f.transform(this.vector3fArray[0].set(-local728_10, -local728_10, -local728_10));
               this.matrix3f.transform(this.vector3fArray[1].set(local728_10, -local728_10, -local728_10));
               this.matrix3f.transform(this.vector3fArray[2].set(local728_10, -local728_10, local728_10));
               this.matrix3f.transform(this.vector3fArray[3].set(-local728_10, -local728_10, local728_10));
               this.matrix3f.transform(this.vector3fArray[4].set(0.0F, local728_10, 0.0F));
               int local728_11 = Colors.computeInt6(this.computeInt8(local728_7.intVal), this.computeFloat4(local728_8));

               for (int index = 0; index < intValArray2.length; index += 2) {
                  local728_5 += this.computeInt5(
                     bufferBuilder,
                     vec3d.x + this.vector3fArray[intValArray2[index]].x,
                     vec3d.y + this.vector3fArray[intValArray2[index]].y,
                     vec3d.z + this.vector3fArray[intValArray2[index]].z,
                     vec3d.x + this.vector3fArray[intValArray2[index + 1]].x,
                     vec3d.y + this.vector3fArray[intValArray2[index + 1]].y,
                     vec3d.z + this.vector3fArray[intValArray2[index + 1]].z,
                     local728_11
                  );
               }
            }
         }
      }

      return local728_5;
   }

   private static void processData4(
      HashMap<String, ParticlesModule.SubContext2> local729_0,
      float local729_1,
      float local729_2,
      float local729_3,
      float local729_4,
      float local729_5,
      float local729_6,
      float local729_7,
      float local729_8,
      float local729_9
   ) {
      int local729_10 = Math.round(local729_1 * 10000.0F);
      int local729_11 = Math.round(local729_2 * 10000.0F);
      int local729_12 = Math.round(local729_3 * 10000.0F);
      int local729_13 = Math.round(local729_4 * 10000.0F);
      int local729_14 = Math.round(local729_5 * 10000.0F);
      int local729_15 = Math.round(local729_6 * 10000.0F);
      boolean local729_16 = local729_10 != local729_13
         ? local729_10 > local729_13
         : (local729_11 != local729_14 ? local729_11 > local729_14 : local729_12 > local729_15);
      String local729_17 = local729_16
         ? local729_13 + "," + local729_14 + "," + local729_15 + "|" + local729_10 + "," + local729_11 + "," + local729_12
         : local729_10 + "," + local729_11 + "," + local729_12 + "|" + local729_13 + "," + local729_14 + "," + local729_15;
      ParticlesModule.SubContext2 local729_18 = local729_0.get(local729_17);
      if (local729_18 == null) {
         local729_0.put(
            local729_17,
            new ParticlesModule.SubContext2(local729_1, local729_2, local729_3, local729_4, local729_5, local729_6, local729_7, local729_8, local729_9)
         );
      } else {
         local729_18.intVal2++;
         if (Math.abs(local729_18.floatVal10 * local729_7 + local729_18.floatVal11 * local729_8 + local729_18.floatVal12 * local729_9) < 0.985F) {
            local729_18.flag6 = false;
         }
      }
   }

   private void renderElement(MatrixStack matrixStack, BufferBuilder bufferBuilder, int local730_3) {
      BuiltBuffer builtBuffer = bufferBuilder.endNullable();
      if (builtBuffer != null) {
         try (builtBuffer) {
            if (local730_3 != 0 && mc.getFramebuffer() != null && mc.getFramebuffer().getColorAttachmentView() != null) {
               if (this.gpuBuffer2 != null && !this.gpuBuffer2.isClosed() && this.gpuBuffer2.size() >= builtBuffer.getBuffer().remaining()) {
                  RenderSystem.getDevice()
                     .createCommandEncoder()
                     .writeToBuffer(this.gpuBuffer2.slice(0L, builtBuffer.getBuffer().remaining()), builtBuffer.getBuffer());
               } else {
                  if (this.gpuBuffer2 != null && !this.gpuBuffer2.isClosed()) {
                     this.gpuBuffer2.close();
                  }

                  this.gpuBuffer2 = RenderSystem.getDevice().createBuffer(() -> "horus_particles_wires", 40, builtBuffer.getBuffer());
               }

               GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(matrixStack.peek().getPositionMatrix(), vector4f, vector3f5, matrix4f);

               try (RenderPass renderPass = RenderSystem.getDevice()
                     .createCommandEncoder()
                     .createRenderPass(
                        () -> "horus_particles_wires",
                        mc.getFramebuffer().getColorAttachmentView(),
                        OptionalInt.empty(),
                        DepthSnapshot.getGpuTextureView(),
                        OptionalDouble.empty()
                     )) {
                  renderPass.setPipeline(pipeline3);
                  renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
                  renderPass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
                  renderPass.setVertexBuffer(0, this.gpuBuffer2);
                  renderPass.setIndexBuffer(
                     RenderSystem.getSequentialBuffer(DrawMode.QUADS).getIndexBuffer(local730_3 * 6),
                     RenderSystem.getSequentialBuffer(DrawMode.QUADS).getIndexType()
                  );
                  renderPass.drawIndexed(0, 0, local730_3 * 6, 1);
                  return;
               }
            }
         }
      }
   }

   private synchronized float[] computeFloat() {
      if (this.flag) {
         return this.floatValArray;
      }

      this.flag = true;
      this.floatValArray = this.computeFloat7("/assets/horus/models/particles/totem_undying.obj", -0.75F, false);
      return this.floatValArray;
   }

   private synchronized float[] computeFloat2() {
      if (this.flag4) {
         return this.floatValArray5;
      }

      this.flag4 = true;
      InputStream resourceAsStream = ParticlesModule.class.getResourceAsStream("/assets/horus/models/particles/horus.obj");
      if (resourceAsStream == null) {
         return null;
      }

      try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8))) {
         ArrayList<Vector3f> local736_3 = new ArrayList<>();
         HashMap<String, ParticlesModule.SubContext2> local736_4 = new HashMap<>();
         float local736_5 = Float.POSITIVE_INFINITY;
         float local736_6 = Float.POSITIVE_INFINITY;
         float local736_7 = Float.POSITIVE_INFINITY;
         float local736_8 = Float.NEGATIVE_INFINITY;
         float local736_9 = Float.NEGATIVE_INFINITY;
         float local736_10 = Float.NEGATIVE_INFINITY;

         String local736_11;
         while ((local736_11 = bufferedReader.readLine()) != null) {
            local736_11 = local736_11.trim();
            if (local736_11.startsWith("v ")) {
               String[] stringArray = local736_11.split("\\s+");
               Vector3f vector3f = new Vector3f(Float.parseFloat(stringArray[1]), -Float.parseFloat(stringArray[2]), Float.parseFloat(stringArray[3]));
               local736_3.add(vector3f);
               local736_5 = Math.min(local736_5, vector3f.x);
               local736_8 = Math.max(local736_8, vector3f.x);
               local736_6 = Math.min(local736_6, vector3f.y);
               local736_9 = Math.max(local736_9, vector3f.y);
               local736_7 = Math.min(local736_7, vector3f.z);
               local736_10 = Math.max(local736_10, vector3f.z);
            } else if (local736_11.startsWith("f ")) {
               String[] stringArray2 = local736_11.split("\\s+");
               Vector3f vector3f2 = local736_3.get(Integer.parseInt(stringArray2[1].split("/")[0]) - 1);
               Vector3f vector3f3 = local736_3.get(Integer.parseInt(stringArray2[2].split("/")[0]) - 1);
               Vector3f vector3f4 = local736_3.get(Integer.parseInt(stringArray2[3].split("/")[0]) - 1);
               float local736_16 = (vector3f3.y - vector3f2.y) * (vector3f4.z - vector3f2.z)
                  - (vector3f3.z - vector3f2.z) * (vector3f4.y - vector3f2.y);
               float local736_17 = (vector3f3.z - vector3f2.z) * (vector3f4.x - vector3f2.x)
                  - (vector3f3.x - vector3f2.x) * (vector3f4.z - vector3f2.z);
               float local736_18 = (vector3f3.x - vector3f2.x) * (vector3f4.y - vector3f2.y)
                  - (vector3f3.y - vector3f2.y) * (vector3f4.x - vector3f2.x);
               float local736_19 = (float)Math.sqrt(local736_16 * local736_16 + local736_17 * local736_17 + local736_18 * local736_18);
               if (!(local736_19 < 1.0E-8F)) {
                  processData4(
                     local736_4,
                     vector3f2.x,
                     vector3f2.y,
                     vector3f2.z,
                     vector3f3.x,
                     vector3f3.y,
                     vector3f3.z,
                     local736_16 / local736_19,
                     local736_17 / local736_19,
                     local736_18 / local736_19
                  );
                  processData4(
                     local736_4,
                     vector3f3.x,
                     vector3f3.y,
                     vector3f3.z,
                     vector3f4.x,
                     vector3f4.y,
                     vector3f4.z,
                     local736_16 / local736_19,
                     local736_17 / local736_19,
                     local736_18 / local736_19
                  );
                  processData4(
                     local736_4,
                     vector3f4.x,
                     vector3f4.y,
                     vector3f4.z,
                     vector3f2.x,
                     vector3f2.y,
                     vector3f2.z,
                     local736_16 / local736_19,
                     local736_17 / local736_19,
                     local736_18 / local736_19
                  );
               }
            }
         }

         float local736_12 = (local736_5 + local736_8) * 0.5F;
         float local736_13 = (local736_6 + local736_9) * 0.5F;
         float local736_14 = (local736_7 + local736_10) * 0.5F;
         float local736_15 = Math.max(local736_8 - local736_5, Math.max(local736_9 - local736_6, local736_10 - local736_7)) > 1.0E-5F
            ? 1.0F / Math.max(local736_8 - local736_5, Math.max(local736_9 - local736_6, local736_10 - local736_7))
            : 1.0F;
         ArrayList<Float> local736_16 = new ArrayList<>(local736_4.size() * 6);

         for (ParticlesModule.SubContext2 local736_18 : local736_4.values()) {
            if (local736_18.intVal2 == 1 || !local736_18.flag6) {
               local736_16.add((local736_18.floatVal4 - local736_12) * local736_15);
               local736_16.add((local736_18.floatVal5 - local736_13) * local736_15);
               local736_16.add((local736_18.floatVal6 - local736_14) * local736_15);
               local736_16.add((local736_18.floatVal7 - local736_12) * local736_15);
               local736_16.add((local736_18.floatVal8 - local736_13) * local736_15);
               local736_16.add((local736_18.floatVal9 - local736_14) * local736_15);
            }
         }

         this.floatValArray5 = new float[local736_16.size()];

         for (int index = 0; index < local736_16.size(); index++) {
            this.floatValArray5[index] = local736_16.get(index);
         }
      } catch (Exception exception) {
         this.floatValArray5 = null;
      }

      return this.floatValArray5;
   }

   private void renderElement2(MatrixStack matrixStack, long local737_2) {
      boolean local737_4 = this.effects.isSelected("Кубы") && !this.list.isEmpty()
         || this.effects.isSelected("Пирамиды") && !this.list2.isEmpty()
         || this.effects.isSelected("Тотем") && !this.list3.isEmpty()
         || this.effects.isSelected("Полумесяц") && !this.list4.isEmpty()
         || this.effects.isSelected("Сердце") && !this.list5.isEmpty()
         || this.effects.isSelected("Звёзды") && !this.list6.isEmpty()
         || this.effects.isSelected("Делюкс") && !this.list7.isEmpty();
      if (local737_4) {
         BufferBuilder bufferBuilder = new BufferBuilder(this.getBufferAllocator(), DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         int local737_6 = 0;
         if (this.effects.isSelected("Кубы")) {
            local737_6 += this.computeInt7(bufferBuilder, local737_2);
         }

         if (this.effects.isSelected("Пирамиды")) {
            local737_6 += this.computeInt2(bufferBuilder, local737_2);
         }

         if (this.effects.isSelected("Тотем")) {
            local737_6 += this.computeInt6(this.list3, this.computeFloat(), 0.78F, 0.75F, 0.35F, true, bufferBuilder, local737_2);
         }

         if (this.effects.isSelected("Полумесяц")) {
            local737_6 += this.computeInt6(this.list4, this.computeFloat3(), 0.72F, 0.7F, 0.45F, false, bufferBuilder, local737_2);
         }

         if (this.effects.isSelected("Сердце")) {
            local737_6 += this.computeInt6(this.list5, this.computeFloat6(), 0.7F, 0.7F, 0.45F, false, bufferBuilder, local737_2);
         }

         if (this.effects.isSelected("Звёзды")) {
            local737_6 += this.computeInt6(this.list6, this.computeFloat5(), 0.9F, 0.7F, 0.45F, false, bufferBuilder, local737_2);
         }

         if (this.effects.isSelected("Делюкс")) {
            local737_6 += this.computeInt6(this.list7, this.computeFloat2(), 0.9F, 0.7F, 0.45F, false, bufferBuilder, local737_2);
         }

         this.renderElement(matrixStack, bufferBuilder, local737_6);
      }
   }

   private void renderElement3(String local739_1, MatrixStack matrixStack, BufferBuilder bufferBuilder, int local739_4) {
      BuiltBuffer builtBuffer = bufferBuilder.endNullable();
      if (builtBuffer != null) {
         try (builtBuffer) {
            if (local739_4 != 0
               && mc.getTextureManager().getTexture(Identifier.of("horus", local739_1)).getGlTextureView() != null
               && mc.getFramebuffer() != null
               && mc.getFramebuffer().getColorAttachmentView() != null) {
               GpuBuffer gpuBuffer = this.map.get(local739_1);
               if (gpuBuffer != null && !gpuBuffer.isClosed() && gpuBuffer.size() >= builtBuffer.getBuffer().remaining()) {
                  RenderSystem.getDevice()
                     .createCommandEncoder()
                     .writeToBuffer(gpuBuffer.slice(0L, builtBuffer.getBuffer().remaining()), builtBuffer.getBuffer());
               } else {
                  if (gpuBuffer != null && !gpuBuffer.isClosed()) {
                     gpuBuffer.close();
                  }

                  gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "horus_particles_" + local739_1, 40, builtBuffer.getBuffer());
                  this.map.put(local739_1, gpuBuffer);
               }

               GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(matrixStack.peek().getPositionMatrix(), vector4f, vector3f5, matrix4f);

               try (RenderPass renderPass = RenderSystem.getDevice()
                     .createCommandEncoder()
                     .createRenderPass(
                        () -> "horus_particles",
                        mc.getFramebuffer().getColorAttachmentView(),
                        OptionalInt.empty(),
                        DepthSnapshot.getGpuTextureView(),
                        OptionalDouble.empty()
                     )) {
                  renderPass.setPipeline(pipeline2);
                  renderPass.bindTexture(
                     "Sampler0",
                     mc.getTextureManager().getTexture(Identifier.of("horus", local739_1)).getGlTextureView(),
                     RenderSystem.getSamplerCache().get(FilterMode.LINEAR)
                  );
                  renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
                  renderPass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
                  renderPass.setVertexBuffer(0, gpuBuffer);
                  renderPass.setIndexBuffer(
                     RenderSystem.getSequentialBuffer(DrawMode.QUADS).getIndexBuffer(local739_4 * 6),
                     RenderSystem.getSequentialBuffer(DrawMode.QUADS).getIndexType()
                  );
                  renderPass.drawIndexed(0, 0, local739_4 * 6, 1);
                  return;
               }
            }
         }
      }
   }

   private BufferAllocator getBufferAllocator() {
      if (this.bufferAllocator == null) {
         this.bufferAllocator = new BufferAllocator(65536);
      }

      CloseableBuffer allocated = this.bufferAllocator.getAllocated();
      if (allocated != null) {
         allocated.close();
      }

      return this.bufferAllocator;
   }

   private synchronized float[] computeFloat3() {
      if (this.floatValArray4 != null) {
         return this.floatValArray4;
      }

      float local744_1 = 0.5F;
      float local744_2 = 0.42F;
      float local744_3 = 0.3F;
      float local744_4 = 0.13F;
      float local744_5 = 0.022F;
      float local744_6 = (local744_1 * local744_1 - local744_2 * local744_2 + local744_3 * local744_3) / (2.0F * local744_3);
      float local744_7 = (float)Math.sqrt(Math.max(0.0F, local744_1 * local744_1 - local744_6 * local744_6));
      float local744_8 = (float)Math.atan2(local744_7, local744_6);
      float local744_9 = (float)Math.atan2(local744_7, local744_6 - local744_3);
      int local744_10 = 18;
      int local744_11 = 3;
      float local744_12 = (local744_1 - local744_6) * 0.5F;
      float local744_13 = (float) (Math.PI * 2) - 2.0F * local744_8;
      float local744_14 = (float) (Math.PI * 2) - 2.0F * local744_9;
      float[] floatArray = new float[local744_10 + 1];
      float[] floatArray2 = new float[local744_10 + 1];
      float[] floatArray3 = new float[local744_10 + 1];
      float[] floatArray4 = new float[local744_10 + 1];
      float[] floatArray5 = new float[local744_10 + 1];
      float[] floatArray6 = new float[local744_10 + 1];

      for (int index = 0; index <= local744_10; index++) {
         float local744_22 = local744_8 + local744_13 / local744_10 * index;
         floatArray[index] = (float)Math.cos(local744_22) * local744_1 + local744_12;
         floatArray2[index] = (float)Math.sin(local744_22) * local744_1;
         floatArray3[index] = local744_5 + (local744_4 - local744_5) * (float)Math.sin((local744_22 - local744_8) / local744_13 * Math.PI);
         float local744_23 = local744_9 + local744_14 / local744_10 * index;
         floatArray4[index] = (float)Math.cos(local744_23) * local744_2 + local744_3 + local744_12;
         floatArray5[index] = (float)Math.sin(local744_23) * local744_2;
         floatArray6[index] = local744_5 + (local744_4 - local744_5) * (float)Math.sin((local744_23 - local744_9) / local744_14 * Math.PI);
      }

      ArrayList<Float> local744_21 = new ArrayList<>(local744_10 * 28 + 64);

      for (int index2 = 0; index2 < local744_10; index2++) {
         local744_21.add(floatArray[index2]);
         local744_21.add(floatArray2[index2]);
         local744_21.add(floatArray3[index2]);
         local744_21.add(floatArray[index2 + 1]);
         local744_21.add(floatArray2[index2 + 1]);
         local744_21.add(floatArray3[index2 + 1]);
         local744_21.add(floatArray[index2]);
         local744_21.add(floatArray2[index2]);
         local744_21.add(-floatArray3[index2]);
         local744_21.add(floatArray[index2 + 1]);
         local744_21.add(floatArray2[index2 + 1]);
         local744_21.add(-floatArray3[index2 + 1]);
      }

      for (int index3 = 0; index3 < local744_10; index3++) {
         local744_21.add(floatArray4[index3]);
         local744_21.add(floatArray5[index3]);
         local744_21.add(floatArray6[index3]);
         local744_21.add(floatArray4[index3 + 1]);
         local744_21.add(floatArray5[index3 + 1]);
         local744_21.add(floatArray6[index3 + 1]);
         local744_21.add(floatArray4[index3]);
         local744_21.add(floatArray5[index3]);
         local744_21.add(-floatArray6[index3]);
         local744_21.add(floatArray4[index3 + 1]);
         local744_21.add(floatArray5[index3 + 1]);
         local744_21.add(-floatArray6[index3 + 1]);
      }

      for (int index4 = 0; index4 <= local744_10; index4 += local744_11) {
         local744_21.add(floatArray[index4]);
         local744_21.add(floatArray2[index4]);
         local744_21.add(floatArray3[index4]);
         local744_21.add(floatArray[index4]);
         local744_21.add(floatArray2[index4]);
         local744_21.add(-floatArray3[index4]);
      }

      for (int index5 = local744_11; index5 < local744_10; index5 += local744_11) {
         local744_21.add(floatArray4[index5]);
         local744_21.add(floatArray5[index5]);
         local744_21.add(floatArray6[index5]);
         local744_21.add(floatArray4[index5]);
         local744_21.add(floatArray5[index5]);
         local744_21.add(-floatArray6[index5]);
      }

      this.floatValArray4 = new float[local744_21.size()];

      for (int index6 = 0; index6 < local744_21.size(); index6++) {
         this.floatValArray4[index6] = local744_21.get(index6);
      }

      return this.floatValArray4;
   }

   private void updateState() {
      ArrayList<ParticlesModule.SubContext4> local745_1 = this.list10;
      local745_1.clear();
      local745_1.addAll(this.list8);
      if (this.effects.isSelected("Мир")) {
         local745_1.addAll(this.list9);
      }

      int local745_2 = 0;

      for (int index = local745_1.size(); local745_2 < index; local745_2++) {
         ParticlesModule.SubContext4 local745_4 = local745_1.get(local745_2);

         for (int index2 = local745_2 + 1; index2 < index; index2++) {
            ParticlesModule.SubContext4 local745_6 = local745_1.get(index2);
            double local745_7 = local745_4.floatVal19 + local745_6.floatVal19;
            double local745_9 = local745_6.vec3d5.x - local745_4.vec3d5.x;
            if (!(local745_9 >= local745_7) && !(-local745_9 >= local745_7)) {
               double local745_11 = local745_6.vec3d5.y - local745_4.vec3d5.y;
               if (!(local745_11 >= local745_7) && !(-local745_11 >= local745_7)) {
                  double local745_13 = local745_6.vec3d5.z - local745_4.vec3d5.z;
                  if (!(local745_13 >= local745_7) && !(-local745_13 >= local745_7)) {
                     double local745_15 = local745_9 * local745_9 + local745_11 * local745_11 + local745_13 * local745_13;
                     if (!(local745_15 >= local745_7 * local745_7) && !(local745_15 < 1.0E-8)) {
                        Vec3d vec3d = new Vec3d(local745_9, local745_11, local745_13);
                        double local745_18 = Math.sqrt(local745_15);
                        Vec3d vec3d2 = vec3d.multiply(1.0 / local745_18);
                        local745_4.vec3d5 = local745_4.vec3d5.subtract(vec3d2.multiply((local745_7 - local745_18) * 0.5));
                        local745_6.vec3d5 = local745_6.vec3d5.add(vec3d2.multiply((local745_7 - local745_18) * 0.5));
                        double local745_21 = local745_6.vec3d7.subtract(local745_4.vec3d7).dotProduct(vec3d2);
                        if (local745_21 < 0.0) {
                           local745_4.vec3d7 = local745_4.vec3d7.subtract(vec3d2.multiply(-local745_21 * 0.45));
                           local745_6.vec3d7 = local745_6.vec3d7.add(vec3d2.multiply(-local745_21 * 0.45));
                        }
                     }
                  }
               }
            }
         }
      }

      local745_1.clear();
   }

   private void renderElement4(MatrixStack matrixStack, float local747_2, long local747_3) {
      boolean local747_5 = this.glow.getValue() > 0.0F && this.hitParticles.isChecked() && !this.list8.isEmpty();
      boolean local747_6 = this.glow.getValue() > 0.0F && this.effects.isSelected("Мир") && !this.list9.isEmpty();
      boolean local747_7 = this.glow.getValue() > 0.0F
         && (
            this.effects.isSelected("Кубы") && !this.list.isEmpty()
               || this.effects.isSelected("Пирамиды") && !this.list2.isEmpty()
               || this.effects.isSelected("Тотем") && !this.list3.isEmpty()
               || this.effects.isSelected("Полумесяц") && !this.list4.isEmpty()
               || this.effects.isSelected("Сердце") && !this.list5.isEmpty()
               || this.effects.isSelected("Звёзды") && !this.list6.isEmpty()
         );
      if (local747_6 || local747_7 || local747_5) {
         Quaternionf quaternionf = new Quaternionf(mc.gameRenderer.getCamera().getRotation()).rotateY((float) Math.PI);
         Vector3f vector3f = new Vector3f();
         BufferBuilder bufferBuilder = new BufferBuilder(this.getBufferAllocator(), DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         int local747_11 = 0;
         if (local747_7) {
            if (this.effects.isSelected("Кубы")) {
               local747_11 += this.computeInt3(this.list, this.size.getValue() * 1.7F, false, bufferBuilder, quaternionf, vector3f, local747_3);
            }

            if (this.effects.isSelected("Пирамиды")) {
               local747_11 += this.computeInt3(this.list2, this.size.getValue() * 1.7F, false, bufferBuilder, quaternionf, vector3f, local747_3);
            }

            if (this.effects.isSelected("Тотем")) {
               local747_11 += this.computeInt3(this.list3, this.size.getValue() * 2.0F, true, bufferBuilder, quaternionf, vector3f, local747_3);
            }

            if (this.effects.isSelected("Полумесяц")) {
               local747_11 += this.computeInt3(this.list4, this.size.getValue() * 1.8F, false, bufferBuilder, quaternionf, vector3f, local747_3);
            }

            if (this.effects.isSelected("Сердце")) {
               local747_11 += this.computeInt3(this.list5, this.size.getValue() * 1.8F, false, bufferBuilder, quaternionf, vector3f, local747_3);
            }

            if (this.effects.isSelected("Звёзды")) {
               local747_11 += this.computeInt3(this.list6, this.size.getValue() * 1.8F, false, bufferBuilder, quaternionf, vector3f, local747_3);
            }
         }

         if (local747_5) {
            local747_11 += this.computeInt4(this.list8, this.hitSize.getValue() / 1.5F, local747_2, bufferBuilder, quaternionf, vector3f, local747_3);
         }

         if (local747_6) {
            local747_11 += this.computeInt4(this.list9, this.worldSize.getValue(), local747_2, bufferBuilder, quaternionf, vector3f, local747_3);
         }

         this.renderElement3("textures/particles/glowboost.png", matrixStack, bufferBuilder, local747_11);
      }
   }

   private int computeInt3(
      ArrayList<ParticlesModule.SubContext> local750_1,
      float local750_2,
      boolean local750_3,
      BufferBuilder bufferBuilder,
      Quaternionf quaternionf,
      Vector3f vector3f,
      long local750_7
   ) {
      int local750_9 = 0;

      for (ParticlesModule.SubContext local750_11 : local750_1) {
         float local750_12 = (float)(local750_7 - local750_11.timeMillis2) / (float)local750_11.timeMillis3;
         if (!(local750_12 >= 1.0F)) {
            Vec3d vec3d = local750_3 ? local750_11.getVec3d2(local750_12) : local750_11.getVec3d(local750_12);
            if (WorldProjection.matchesCriteria2(vec3d.x, vec3d.y, vec3d.z, 2.0F)) {
               int local750_14 = this.computeInt8(local750_11.intVal);
               float local750_15 = 0.85F + 0.15F * MathHelper.sin((float)local750_11.doubleVal * 3.0F + local750_12 * 6.0F);
               this.processData(
                  bufferBuilder,
                  quaternionf,
                  vector3f,
                  vec3d.x,
                  vec3d.y,
                  vec3d.z,
                  local750_2 * (0.95F + local750_12 * 0.25F) * local750_15,
                  Colors.computeInt6(local750_14, this.computeFloat4(local750_12) * 0.22F * this.glow.getValue())
               );
               this.processData(
                  bufferBuilder,
                  quaternionf,
                  vector3f,
                  vec3d.x,
                  vec3d.y,
                  vec3d.z,
                  local750_2 * 0.55F * (0.9F + local750_12 * 0.2F) * local750_15,
                  Colors.computeInt6(local750_14, this.computeFloat4(local750_12) * 0.55F * Math.min(1.0F, this.glow.getValue()))
               );
               local750_9 += 2;
            }
         }
      }

      return local750_9;
   }

   private boolean matchesCriteria(Vec3d vec3d) {
      if (mc.getCameraEntity() == null) {
         return false;
      } else {
         Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();
         HitResult hitResult = mc.world
            .raycast(new RaycastContext(cameraPos, vec3d, ShapeType.COLLIDER, FluidHandling.NONE, mc.getCameraEntity()));
         if (hitResult.getType() == Type.BLOCK && hitResult.getPos().squaredDistanceTo(cameraPos) < vec3d.squaredDistanceTo(cameraPos) - 0.001) {
            return false;
         } else {
            return mc.player.getBoundingBox().expand(0.15).raycast(cameraPos, vec3d).isPresent()
               ? false
               : mc.world
                  .getOtherEntities(
                     mc.getCameraEntity(),
                     mc.getCameraEntity().getBoundingBox().stretch(vec3d.subtract(cameraPos)).expand(0.3),
                     entity -> entity.getBoundingBox().expand(0.1).raycast(cameraPos, vec3d).isPresent()
                  )
                  .isEmpty();
         }
      }
   }

   public static ParticlesModule getInstance() {
      if (particlesModule == null) {
         particlesModule = new ParticlesModule();
      }

      return particlesModule;
   }

   private int computeInt4(
      ArrayList<ParticlesModule.SubContext4> local756_1,
      float local756_2,
      float local756_3,
      BufferBuilder bufferBuilder,
      Quaternionf quaternionf,
      Vector3f vector3f,
      long local756_7
   ) {
      int local756_9 = 0;

      for (ParticlesModule.SubContext4 local756_11 : local756_1) {
         float local756_12 = local756_11.computeFloat9(local756_7);
         if (!(local756_12 <= 0.0F)) {
            Vec3d vec3d = local756_11.vec3d6.lerp(local756_11.vec3d5, local756_3);
            if (WorldProjection.matchesCriteria2(vec3d.x, vec3d.y, vec3d.z, 2.0F)) {
               int local756_14 = this.computeInt8(local756_11.intVal5);
               float local756_15 = 0.85F + 0.15F * MathHelper.sin(local756_11.intVal5 + local756_11.computeFloat8(local756_7) * 6.0F);
               this.processData(
                  bufferBuilder,
                  quaternionf,
                  vector3f,
                  vec3d.x,
                  vec3d.y,
                  vec3d.z,
                  local756_2 * 1.9F * local756_15,
                  Colors.computeInt6(local756_14, local756_12 * 0.22F * this.glow.getValue())
               );
               this.processData(
                  bufferBuilder,
                  quaternionf,
                  vector3f,
                  vec3d.x,
                  vec3d.y,
                  vec3d.z,
                  local756_2 * 1.1F * local756_15,
                  Colors.computeInt6(local756_14, local756_12 * 0.55F * Math.min(1.0F, this.glow.getValue()))
               );
               local756_9 += 2;
            }
         }
      }

      return local756_9;
   }

   private float computeFloat4(float local758_1) {
      if (local758_1 < 0.15F) {
         return local758_1 / 0.15F;
      } else {
         return local758_1 > 0.75F ? Math.max(0.0F, (1.0F - local758_1) / 0.25F) : 1.0F;
      }
   }

   @Override
   protected void onDisable() {
      this.list.clear();
      this.list2.clear();
      this.list3.clear();
      this.list4.clear();
      this.list5.clear();
      this.list6.clear();
      this.list7.clear();
      this.list8.clear();
      this.list9.clear();
      this.concurrentLinkedQueue.clear();

      for (GpuBuffer gpuBuffer : this.map.values()) {
         if (!gpuBuffer.isClosed()) {
            gpuBuffer.close();
         }
      }

      this.map.clear();
      if (this.gpuBuffer2 != null && !this.gpuBuffer2.isClosed()) {
         this.gpuBuffer2.close();
      }

      this.gpuBuffer2 = null;
      if (this.bufferAllocator != null) {
         this.bufferAllocator.close();
         this.bufferAllocator = null;
      }

      this.list10.clear();
   }

   private synchronized float[] computeFloat5() {
      if (this.flag2) {
         return this.floatValArray2;
      }

      this.flag2 = true;
      this.floatValArray2 = this.computeFloat7("/assets/horus/models/particles/fire_nether_star.obj", 0.0F, true);
      return this.floatValArray2;
   }

   private int computeInt5(
      BufferBuilder bufferBuilder,
      double local764_2,
      double local764_4,
      double local764_6,
      double local764_8,
      double local764_10,
      double local764_12,
      int local764_14
   ) {
      float local764_15 = (float)(local764_2 - mc.gameRenderer.getCamera().getCameraPos().x);
      float local764_16 = (float)(local764_4 - mc.gameRenderer.getCamera().getCameraPos().y);
      float local764_17 = (float)(local764_6 - mc.gameRenderer.getCamera().getCameraPos().z);
      float local764_18 = (float)(local764_8 - mc.gameRenderer.getCamera().getCameraPos().x);
      float local764_19 = (float)(local764_10 - mc.gameRenderer.getCamera().getCameraPos().y);
      float local764_20 = (float)(local764_12 - mc.gameRenderer.getCamera().getCameraPos().z);
      float local764_21 = local764_18 - local764_15;
      float local764_22 = local764_19 - local764_16;
      float local764_23 = local764_20 - local764_17;
      float local764_24 = (local764_15 + local764_18) * 0.5F;
      float local764_25 = (local764_16 + local764_19) * 0.5F;
      float local764_26 = (local764_17 + local764_20) * 0.5F;
      float local764_27 = local764_22 * local764_26 - local764_23 * local764_25;
      float local764_28 = local764_23 * local764_24 - local764_21 * local764_26;
      float local764_29 = local764_21 * local764_25 - local764_22 * local764_24;
      float local764_30 = MathHelper.sqrt(local764_27 * local764_27 + local764_28 * local764_28 + local764_29 * local764_29);
      if (local764_30 < 1.0E-6F) {
         return 0;
      }

      int local764_31 = 0;
      if (this.glow.getValue() > 0.05F) {
         float local764_32 = 0.0256F / local764_30;
         int local764_33 = Colors.computeInt6(local764_14, 0.22F + 0.1F * this.glow.getValue());
         bufferBuilder.vertex(local764_15 - local764_27 * local764_32, local764_16 - local764_28 * local764_32, local764_17 - local764_29 * local764_32)
            .color(local764_33);
         bufferBuilder.vertex(local764_15 + local764_27 * local764_32, local764_16 + local764_28 * local764_32, local764_17 + local764_29 * local764_32)
            .color(local764_33);
         bufferBuilder.vertex(local764_18 + local764_27 * local764_32, local764_19 + local764_28 * local764_32, local764_20 + local764_29 * local764_32)
            .color(local764_33);
         bufferBuilder.vertex(local764_18 - local764_27 * local764_32, local764_19 - local764_28 * local764_32, local764_20 - local764_29 * local764_32)
            .color(local764_33);
         local764_31++;
      }

      float local764_32 = 0.008F / local764_30;
      bufferBuilder.vertex(local764_15 - local764_27 * local764_32, local764_16 - local764_28 * local764_32, local764_17 - local764_29 * local764_32)
         .color(local764_14);
      bufferBuilder.vertex(local764_15 + local764_27 * local764_32, local764_16 + local764_28 * local764_32, local764_17 + local764_29 * local764_32)
         .color(local764_14);
      bufferBuilder.vertex(local764_18 + local764_27 * local764_32, local764_19 + local764_28 * local764_32, local764_20 + local764_29 * local764_32)
         .color(local764_14);
      bufferBuilder.vertex(local764_18 - local764_27 * local764_32, local764_19 - local764_28 * local764_32, local764_20 - local764_29 * local764_32)
         .color(local764_14);
      return local764_31 + 1;
   }

   private long computeLong(float local767_1) {
      return Math.max(50L, Math.round(local767_1 * (0.75F + ThreadLocalRandom.current().nextFloat() * 0.5F)));
   }

   private void processData5(long local769_1) {
      this.list.removeIf(local772_2 -> local769_1 - local772_2.timeMillis2 > local772_2.timeMillis3);
      this.list2.removeIf(local774_2 -> local769_1 - local774_2.timeMillis2 > local774_2.timeMillis3);
      this.list3.removeIf(local740_2 -> local769_1 - local740_2.timeMillis2 > local740_2.timeMillis3);
      this.list4.removeIf(local732_2 -> local769_1 - local732_2.timeMillis2 > local732_2.timeMillis3);
      this.list5.removeIf(local749_2 -> local769_1 - local749_2.timeMillis2 > local749_2.timeMillis3);
      this.list6.removeIf(local759_2 -> local769_1 - local759_2.timeMillis2 > local759_2.timeMillis3);
      this.list7.removeIf(local752_2 -> local769_1 - local752_2.timeMillis2 > local752_2.timeMillis3);
   }

   private void processData6(ArrayList<ParticlesModule.SubContext4> local770_1) {
      Vec3d vec3d = mc.player.getEntityPos().subtract(mc.player.lastRenderX, mc.player.lastRenderY, mc.player.lastRenderZ);

      for (ParticlesModule.SubContext4 local770_4 : local770_1) {
         double local770_5 = MathHelper.clamp(local770_4.vec3d5.x, mc.player.getBoundingBox().minX, mc.player.getBoundingBox().maxX);
         double local770_7 = MathHelper.clamp(local770_4.vec3d5.y, mc.player.getBoundingBox().minY, mc.player.getBoundingBox().maxY);
         double local770_9 = MathHelper.clamp(local770_4.vec3d5.z, mc.player.getBoundingBox().minZ, mc.player.getBoundingBox().maxZ);
         Vec3d vec3d2 = local770_4.vec3d5.subtract(local770_5, local770_7, local770_9);
         double local770_12 = vec3d2.lengthSquared();
         if (!(local770_12 >= local770_4.floatVal19 * local770_4.floatVal19)) {
            double local770_14 = Math.sqrt(Math.max(local770_12, 1.0E-8));
            Vec3d vec3d3 = local770_14 < 1.0E-4
               ? local770_4.vec3d5.subtract(mc.player.getEntityPos())
               : vec3d2.multiply(1.0 / local770_14);
            vec3d3 = vec3d3.lengthSquared() < 1.0E-8 ? new Vec3d(0.0, 1.0, 0.0) : vec3d3.normalize();
            local770_4.vec3d5 = local770_4.vec3d5.add(vec3d3.multiply(local770_4.floatVal19 - local770_14 + 0.01));
            local770_4.vec3d7 = local770_4.vec3d7
               .add(vec3d3.multiply(0.035 + Math.min(vec3d.length() * 0.8, 0.25)))
               .add(vec3d.x * 0.4, Math.max(vec3d.y * 0.2, 0.0), vec3d.z * 0.4);
         }
      }
   }

   private int computeInt6(
      ArrayList<ParticlesModule.SubContext> local771_1,
      float[] floatArray,
      float local771_3,
      float local771_4,
      float local771_5,
      boolean local771_6,
      BufferBuilder bufferBuilder,
      long local771_8
   ) {
      if (floatArray != null && floatArray.length != 0) {
         float local771_10 = this.size.getValue() * local771_3;
         int local771_11 = 0;

         for (ParticlesModule.SubContext local771_13 : local771_1) {
            float local771_14 = (float)(local771_8 - local771_13.timeMillis2) / (float)local771_13.timeMillis3;
            if (!(local771_14 >= 1.0F)) {
               Vec3d vec3d = local771_6 ? local771_13.getVec3d2(local771_14) : local771_13.getVec3d(local771_14);
               if (WorldProjection.matchesCriteria2(vec3d.x, vec3d.y, vec3d.z, 2.0F)) {
                  float local771_16 = local771_10 * (local771_4 + local771_14 * local771_5);
                  this.matrix3f
                     .identity()
                     .rotateY(local771_13.floatVal2 * local771_14)
                     .rotateX(local771_13.floatVal * local771_14)
                     .rotateZ(local771_13.floatVal3 * local771_14);
                  int local771_17 = Colors.computeInt6(this.computeInt8(local771_13.intVal), this.computeFloat4(local771_14));

                  for (int index = 0; index < floatArray.length; index += 6) {
                     this.matrix3f
                        .transform(
                           this.vector3f6
                              .set(floatArray[index] * local771_16, floatArray[index + 1] * local771_16, floatArray[index + 2] * local771_16)
                        );
                     this.matrix3f
                        .transform(
                           this.vector3f7
                              .set(
                                 floatArray[index + 3] * local771_16,
                                 floatArray[index + 4] * local771_16,
                                 floatArray[index + 5] * local771_16
                              )
                        );
                     local771_11 += this.computeInt5(
                        bufferBuilder,
                        vec3d.x + this.vector3f6.x,
                        vec3d.y + this.vector3f6.y,
                        vec3d.z + this.vector3f6.z,
                        vec3d.x + this.vector3f7.x,
                        vec3d.y + this.vector3f7.y,
                        vec3d.z + this.vector3f7.z,
                        local771_17
                     );
                  }
               }
            }
         }

         return local771_11;
      } else {
         return 0;
      }
   }

   private void processData7(
      ArrayList<ParticlesModule.SubContext> local773_1,
      Vec3d vec3d,
      double local773_3,
      double local773_5,
      long local773_7,
      int local773_9,
      float local773_10,
      float local773_11,
      float local773_12
   ) {
      while (local773_1.size() > local773_9) {
         local773_1.remove(0);
      }

      while (local773_1.size() < local773_9) {
         local773_1.add(this.getNested0353(vec3d, local773_3, local773_5, local773_7, local773_10, local773_11, local773_12));
      }
   }

   private void processData8(long local775_1) {
      ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
      double local775_4 = Math.min(this.distanceMin.getValue(), this.distanceMax.getValue());
      double local775_6 = local775_4 + threadLocalRandom.nextDouble() * Math.max(0.1, this.distanceMax.getValue() - local775_4);
      double local775_8 = Math.toRadians(mc.player.getYaw() + threadLocalRandom.nextDouble(-80.0, 80.0));
      double local775_10 = mc.player.getX() - Math.sin(local775_8) * local775_6;
      double local775_12 = mc.player.getZ() + Math.cos(local775_8) * local775_6;
      if (this.modeSetting.isSelected("Земля")) {
         BlockPos blockPos = mc.world
            .getTopPosition(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, BlockPos.ofFloored(local775_10, mc.player.getY(), local775_12));
         Vec3d vec3d = new Vec3d(
            local775_10 + threadLocalRandom.nextDouble(), blockPos.getY() + threadLocalRandom.nextDouble(0.02, 0.12), local775_12 + threadLocalRandom.nextDouble()
         );
         if (WorldProjection.getVec3d2(vec3d, 1.0F, 1.0F) != null) {
            this.list9
               .add(
                  new ParticlesModule.SubContext4(
                     vec3d,
                     new Vec3d(threadLocalRandom.nextDouble(-0.02, 0.02), threadLocalRandom.nextDouble(0.02, 0.05), threadLocalRandom.nextDouble(-0.02, 0.02)),
                     local775_1,
                     2400L,
                     true,
                     threadLocalRandom.nextInt(360),
                     this.worldImages.getSelectedCount() == 0 ? "Светлячок" : this.worldImages.getSelectedOptions().get(threadLocalRandom.nextInt(this.worldImages.getSelectedCount())),
                     this.worldSize.getValue() * 0.42F
                  )
               );
         }
      } else {
         Vec3d vec3d2 = new Vec3d(
            local775_10, mc.player.getY() + threadLocalRandom.nextDouble(1.0, Math.max(1.5, this.worldHeight.getValue())), local775_12
         );
         if (WorldProjection.getVec3d2(vec3d2, 1.0F, 1.0F) != null) {
            this.list9
               .add(
                  new ParticlesModule.SubContext4(
                     vec3d2,
                     Vec3d.ZERO,
                     local775_1,
                     5000L,
                     false,
                     threadLocalRandom.nextInt(360),
                     this.worldImages.getSelectedCount() == 0 ? "Светлячок" : this.worldImages.getSelectedOptions().get(threadLocalRandom.nextInt(this.worldImages.getSelectedCount())),
                     this.worldSize.getValue() * 0.42F
                  )
               );
         }
      }
   }

   private int computeInt7(BufferBuilder bufferBuilder, long local777_2) {
      float local777_4 = this.size.getValue() * 0.6F;
      int local777_5 = 0;

      for (ParticlesModule.SubContext local777_7 : this.list) {
         float local777_8 = (float)(local777_2 - local777_7.timeMillis2) / (float)local777_7.timeMillis3;
         if (!(local777_8 >= 1.0F)) {
            Vec3d vec3d = local777_7.getVec3d(local777_8);
            if (WorldProjection.matchesCriteria2(vec3d.x, vec3d.y, vec3d.z, 2.0F)) {
               float local777_10 = local777_4 * (0.6F + local777_8 * 0.6F) * 0.5F;
               this.matrix3f
                  .identity()
                  .rotateY(local777_7.floatVal2 * local777_8)
                  .rotateX(local777_7.floatVal * local777_8)
                  .rotateZ(local777_7.floatVal3 * local777_8);

               for (int index = 0; index < 8; index++) {
                  this.matrix3f
                     .transform(
                        this.vector3fArray[index]
                           .set(
                              (index & 1) == 0 ? -local777_10 : local777_10,
                              (index & 2) == 0 ? -local777_10 : local777_10,
                              (index & 4) == 0 ? -local777_10 : local777_10
                           )
                     );
               }

               int local777_11 = Colors.computeInt6(this.computeInt8(local777_7.intVal), this.computeFloat4(local777_8));

               for (int index2 = 0; index2 < intValArray.length; index2 += 2) {
                  local777_5 += this.computeInt5(
                     bufferBuilder,
                     vec3d.x + this.vector3fArray[intValArray[index2]].x,
                     vec3d.y + this.vector3fArray[intValArray[index2]].y,
                     vec3d.z + this.vector3fArray[intValArray[index2]].z,
                     vec3d.x + this.vector3fArray[intValArray[index2 + 1]].x,
                     vec3d.y + this.vector3fArray[intValArray[index2 + 1]].y,
                     vec3d.z + this.vector3fArray[intValArray[index2 + 1]].z,
                     local777_11
                  );
               }
            }
         }
      }

      return local777_5;
   }

   private synchronized float[] computeFloat6() {
      if (this.flag3) {
         return this.floatValArray3;
      }

      this.flag3 = true;
      InputStream resourceAsStream = ParticlesModule.class.getResourceAsStream("/assets/horus/models/particles/heart.gltf");
      if (resourceAsStream == null) {
         return null;
      }

      try {
         JsonObject asJsonObject = JsonParser.parseString(new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
         String asString = asJsonObject.getAsJsonArray("buffers").get(0).getAsJsonObject().get("uri").getAsString();
         ByteBuffer byteBuffer = ByteBuffer.wrap(Base64.getDecoder().decode(asString.substring(asString.indexOf(44) + 1))).order(ByteOrder.LITTLE_ENDIAN);
         JsonArray asJsonArray = asJsonObject.getAsJsonArray("bufferViews");
         int[] intArray = new int[asJsonArray.size()];

         for (int index = 0; index < asJsonArray.size(); index++) {
            intArray[index] = asJsonArray.get(index).getAsJsonObject().has("byteOffset")
               ? asJsonArray.get(index).getAsJsonObject().get("byteOffset").getAsInt()
               : 0;
         }

         JsonArray asJsonArray2 = asJsonObject.getAsJsonArray("accessors");
         int[] intArray2 = new int[asJsonArray2.size()];
         int[] intArray3 = new int[asJsonArray2.size()];
         int[] intArray4 = new int[asJsonArray2.size()];
         int[] intArray5 = new int[asJsonArray2.size()];

         for (int index2 = 0; index2 < asJsonArray2.size(); index2++) {
            JsonObject asJsonObject2 = asJsonArray2.get(index2).getAsJsonObject();
            intArray2[index2] = asJsonObject2.get("bufferView").getAsInt();
            intArray3[index2] = asJsonObject2.get("componentType").getAsInt();
            intArray4[index2] = asJsonObject2.get("count").getAsInt();
            intArray5[index2] = asJsonObject2.has("byteOffset") ? asJsonObject2.get("byteOffset").getAsInt() : 0;
         }

         JsonArray asJsonArray3 = asJsonObject.getAsJsonArray("nodes");
         float[][] floatArrayArray = new float[asJsonArray3.size()][3];
         boolean[] booleanArray = new boolean[asJsonArray3.size()];
         JsonArray asJsonArray4 = asJsonObject.getAsJsonArray("scenes")
            .get(asJsonObject.has("scene") ? asJsonObject.get("scene").getAsInt() : 0)
            .getAsJsonObject()
            .getAsJsonArray("nodes");

         for (int index3 = 0; index3 < asJsonArray4.size(); index3++) {
            processData2(asJsonArray3, asJsonArray4.get(index3).getAsInt(), 0.0F, 0.0F, 0.0F, floatArrayArray, booleanArray);
         }

         JsonArray asJsonArray5 = asJsonObject.getAsJsonArray("meshes");
         int[] intArray6 = new int[asJsonArray5.size()];
         int[] intArray7 = new int[asJsonArray5.size()];

         for (int index4 = 0; index4 < asJsonArray5.size(); index4++) {
            JsonObject asJsonObject3 = asJsonArray5.get(index4).getAsJsonObject().getAsJsonArray("primitives").get(0).getAsJsonObject();
            intArray6[index4] = asJsonObject3.getAsJsonObject("attributes").get("POSITION").getAsInt();
            intArray7[index4] = asJsonObject3.get("indices").getAsInt();
         }

         HashMap<String, ParticlesModule.SubContext2> local779_19 = new HashMap<>();
         float local779_20 = Float.POSITIVE_INFINITY;
         float local779_21 = Float.POSITIVE_INFINITY;
         float local779_22 = Float.POSITIVE_INFINITY;
         float local779_23 = Float.NEGATIVE_INFINITY;
         float local779_24 = Float.NEGATIVE_INFINITY;
         float local779_25 = Float.NEGATIVE_INFINITY;

         for (int index5 = 0; index5 < asJsonArray3.size(); index5++) {
            JsonObject asJsonObject4 = asJsonArray3.get(index5).getAsJsonObject();
            if (asJsonObject4.has("mesh")) {
               int asInt = asJsonObject4.get("mesh").getAsInt();
               int local779_29 = intArray[intArray2[intArray6[asInt]]] + intArray5[intArray6[asInt]];
               float[] floatArray = new float[intArray4[intArray6[asInt]] * 3];

               for (int index6 = 0; index6 < intArray4[intArray6[asInt]]; index6++) {
                  floatArray[index6 * 3] = byteBuffer.getFloat(local779_29 + index6 * 12) + floatArrayArray[index5][0];
                  floatArray[index6 * 3 + 1] = byteBuffer.getFloat(local779_29 + index6 * 12 + 4) + floatArrayArray[index5][1];
                  floatArray[index6 * 3 + 2] = byteBuffer.getFloat(local779_29 + index6 * 12 + 8) + floatArrayArray[index5][2];
                  local779_20 = Math.min(local779_20, floatArray[index6 * 3]);
                  local779_23 = Math.max(local779_23, floatArray[index6 * 3]);
                  local779_21 = Math.min(local779_21, floatArray[index6 * 3 + 1]);
                  local779_24 = Math.max(local779_24, floatArray[index6 * 3 + 1]);
                  local779_22 = Math.min(local779_22, floatArray[index6 * 3 + 2]);
                  local779_25 = Math.max(local779_25, floatArray[index6 * 3 + 2]);
               }

               int local779_31 = intArray[intArray2[intArray7[asInt]]] + intArray5[intArray7[asInt]];
               int[] intArray8 = new int[intArray4[intArray7[asInt]]];

               for (int index7 = 0; index7 < intArray8.length; index7++) {
                  if (intArray3[intArray7[asInt]] == 5123) {
                     intArray8[index7] = byteBuffer.getShort(local779_31 + index7 * 2) & '\uffff';
                  } else if (intArray3[intArray7[asInt]] == 5125) {
                     intArray8[index7] = byteBuffer.getInt(local779_31 + index7 * 4);
                  } else {
                     intArray8[index7] = byteBuffer.get(local779_31 + index7) & 255;
                  }
               }

               for (int index8 = 0; index8 + 2 < intArray8.length; index8 += 3) {
                  float local779_34 = floatArray[intArray8[index8] * 3];
                  float local779_35 = floatArray[intArray8[index8] * 3 + 1];
                  float local779_36 = floatArray[intArray8[index8] * 3 + 2];
                  float local779_37 = floatArray[intArray8[index8 + 1] * 3];
                  float local779_38 = floatArray[intArray8[index8 + 1] * 3 + 1];
                  float local779_39 = floatArray[intArray8[index8 + 1] * 3 + 2];
                  float local779_40 = floatArray[intArray8[index8 + 2] * 3];
                  float local779_41 = floatArray[intArray8[index8 + 2] * 3 + 1];
                  float local779_42 = floatArray[intArray8[index8 + 2] * 3 + 2];
                  float local779_43 = (local779_38 - local779_35) * (local779_42 - local779_36) - (local779_39 - local779_36) * (local779_41 - local779_35);
                  float local779_44 = (local779_39 - local779_36) * (local779_40 - local779_34) - (local779_37 - local779_34) * (local779_42 - local779_36);
                  float local779_45 = (local779_37 - local779_34) * (local779_41 - local779_35) - (local779_38 - local779_35) * (local779_40 - local779_34);
                  float local779_46 = (float)Math.sqrt(local779_43 * local779_43 + local779_44 * local779_44 + local779_45 * local779_45);
                  if (!(local779_46 < 1.0E-8F)) {
                     processData4(
                        local779_19,
                        local779_34,
                        local779_35,
                        local779_36,
                        local779_37,
                        local779_38,
                        local779_39,
                        local779_43 / local779_46,
                        local779_44 / local779_46,
                        local779_45 / local779_46
                     );
                     processData4(
                        local779_19,
                        local779_37,
                        local779_38,
                        local779_39,
                        local779_40,
                        local779_41,
                        local779_42,
                        local779_43 / local779_46,
                        local779_44 / local779_46,
                        local779_45 / local779_46
                     );
                     processData4(
                        local779_19,
                        local779_40,
                        local779_41,
                        local779_42,
                        local779_34,
                        local779_35,
                        local779_36,
                        local779_43 / local779_46,
                        local779_44 / local779_46,
                        local779_45 / local779_46
                     );
                  }
               }
            }
         }

         float local779_26 = (local779_20 + local779_23) * 0.5F;
         float local779_27 = (local779_21 + local779_24) * 0.5F;
         float local779_28 = (local779_22 + local779_25) * 0.5F;
         float local779_29 = Math.max(local779_23 - local779_20, Math.max(local779_24 - local779_21, local779_25 - local779_22)) > 1.0E-5F
            ? 1.2F / Math.max(local779_23 - local779_20, Math.max(local779_24 - local779_21, local779_25 - local779_22))
            : 1.0F;
         ArrayList<Float> local779_30 = new ArrayList<>(local779_19.size() * 6);

         for (ParticlesModule.SubContext2 local779_32 : local779_19.values()) {
            if (local779_32.intVal2 == 1 || !local779_32.flag6) {
               local779_30.add((local779_32.floatVal4 - local779_26) * local779_29);
               local779_30.add((local779_32.floatVal5 - local779_27) * local779_29);
               local779_30.add((local779_32.floatVal6 - local779_28) * local779_29);
               local779_30.add((local779_32.floatVal7 - local779_26) * local779_29);
               local779_30.add((local779_32.floatVal8 - local779_27) * local779_29);
               local779_30.add((local779_32.floatVal9 - local779_28) * local779_29);
            }
         }

         this.floatValArray3 = new float[local779_30.size()];

         for (int index9 = 0; index9 < local779_30.size(); index9++) {
            this.floatValArray3[index9] = local779_30.get(index9);
         }
      } catch (Exception exception) {
         this.floatValArray3 = null;
      }

      return this.floatValArray3;
   }

   private void renderElement5(
      ArrayList<ParticlesModule.SubContext4> local780_1,
      MultiSelectSetting multiSelectSetting,
      float local780_3,
      MatrixStack matrixStack,
      float local780_5,
      long local780_6
   ) {
      if (!local780_1.isEmpty()) {
         Quaternionf quaternionf = new Quaternionf(mc.gameRenderer.getCamera().getRotation()).rotateY((float) Math.PI);
         Vector3f vector3f = new Vector3f();
         Arrays.fill(this.intValArray5, 0);
         int local780_10 = 0;
         int local780_11 = 0;

         for (int index = local780_1.size(); local780_11 < index; local780_11++) {
            int local780_13 = local780_1.get(local780_11).intVal6;
            if (local780_13 >= 0) {
               this.intValArray5[local780_13]++;
            } else {
               local780_10++;
            }
         }

         for (String local780_12 : multiSelectSetting.getOptions()) {
            int local780_13 = computeInt(local780_12);
            if (local780_13 >= 0 ? this.intValArray5[local780_13] != 0 : local780_10 != 0) {
               BufferBuilder bufferBuilder = new BufferBuilder(this.getBufferAllocator(), DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
               int local780_15 = 0;
               int local780_16 = 0;

               for (int index2 = local780_1.size(); local780_16 < index2; local780_16++) {
                  ParticlesModule.SubContext4 local780_18 = local780_1.get(local780_16);
                  if (local780_13 >= 0 ? local780_18.intVal6 == local780_13 : local780_18.strVal.equals(local780_12)) {
                     float local780_19 = local780_18.computeFloat9(local780_6);
                     if (!(local780_19 <= 0.0F)) {
                        Vec3d vec3d = local780_18.vec3d6.lerp(local780_18.vec3d5, local780_5);
                        if (WorldProjection.matchesCriteria2(vec3d.x, vec3d.y, vec3d.z, 2.0F)) {
                           this.processData(
                              bufferBuilder,
                              quaternionf,
                              vector3f,
                              vec3d.x,
                              vec3d.y,
                              vec3d.z,
                              local780_3 / 2.0F,
                              Colors.computeInt6(this.computeInt8(local780_18.intVal5), local780_19)
                           );
                           local780_15++;
                        }
                     }
                  }
               }

               this.renderElement3(this.formatText(local780_12), matrixStack, bufferBuilder, local780_15);
            }
         }
      }
   }

   private float[] computeFloat7(String local781_1, float local781_2, boolean local781_3) {
      InputStream resourceAsStream = ParticlesModule.class.getResourceAsStream(local781_1);
      if (resourceAsStream == null) {
         return null;
      }

      try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8))) {
         ArrayList<Vector3f> local781_6 = new ArrayList<>();
         HashMap<Long, ParticlesModule.SubContext3> local781_7 = new HashMap<>();
         float local781_8 = Float.POSITIVE_INFINITY;
         float local781_9 = Float.POSITIVE_INFINITY;
         float local781_10 = Float.POSITIVE_INFINITY;
         float local781_11 = Float.NEGATIVE_INFINITY;
         float local781_12 = Float.NEGATIVE_INFINITY;
         float local781_13 = Float.NEGATIVE_INFINITY;

         String local781_14;
         while ((local781_14 = bufferedReader.readLine()) != null) {
            local781_14 = local781_14.trim();
            if (local781_14.startsWith("v ")) {
               String[] stringArray = local781_14.split("\\s+");
               Vector3f vector3f = new Vector3f(
                  Float.parseFloat(stringArray[1]), Float.parseFloat(stringArray[2]) + local781_2, Float.parseFloat(stringArray[3])
               );
               local781_6.add(vector3f);
               local781_8 = Math.min(local781_8, vector3f.x);
               local781_11 = Math.max(local781_11, vector3f.x);
               local781_9 = Math.min(local781_9, vector3f.y);
               local781_12 = Math.max(local781_12, vector3f.y);
               local781_10 = Math.min(local781_10, vector3f.z);
               local781_13 = Math.max(local781_13, vector3f.z);
            } else if (local781_14.startsWith("f ")) {
               String[] stringArray2 = local781_14.split("\\s+");

               for (int index = 1; index < stringArray2.length; index++) {
                  int local781_17 = Integer.parseInt(stringArray2[index].split("/")[0]) - 1;
                  int local781_18 = Integer.parseInt(stringArray2[index == stringArray2.length - 1 ? 1 : index + 1].split("/")[0]) - 1;
                  if (local781_17 != local781_18) {
                     String[] stringArray3 = stringArray2[index].split("/");
                     int local781_20 = stringArray3.length > 2 && !stringArray3[2].isEmpty() ? Integer.parseInt(stringArray3[2]) - 1 : -1;
                     long local781_21 = (long)Math.min(local781_17, local781_18) << 32 | Math.max(local781_17, local781_18) & 4294967295L;
                     ParticlesModule.SubContext3 local781_23 = local781_7.get(local781_21);
                     if (local781_23 == null) {
                        local781_7.put(local781_21, new ParticlesModule.SubContext3(local781_6.get(local781_17), local781_6.get(local781_18), local781_20));
                     } else {
                        local781_23.intVal4++;
                        if (local781_23.intVal3 != local781_20) {
                           local781_23.flag7 = false;
                        }
                     }
                  }
               }
            }
         }

         float local781_15 = local781_3 ? (local781_8 + local781_11) * 0.5F : 0.0F;
         float local781_16 = local781_3 ? (local781_9 + local781_12) * 0.5F : 0.0F;
         float local781_17 = local781_3 ? (local781_10 + local781_13) * 0.5F : 0.0F;
         float local781_18 = local781_3 && Math.max(local781_11 - local781_8, Math.max(local781_12 - local781_9, local781_13 - local781_10)) > 1.0E-5F
            ? 1.0F / Math.max(local781_11 - local781_8, Math.max(local781_12 - local781_9, local781_13 - local781_10))
            : 1.0F;
         ArrayList<Float> local781_19 = new ArrayList<>();

         for (ParticlesModule.SubContext3 local781_21 : local781_7.values()) {
            if (local781_21.intVal4 == 1 || !local781_21.flag7) {
               local781_19.add((local781_21.floatVal13 - local781_15) * local781_18);
               local781_19.add((local781_21.floatVal14 - local781_16) * local781_18);
               local781_19.add((local781_21.floatVal15 - local781_17) * local781_18);
               local781_19.add((local781_21.floatVal16 - local781_15) * local781_18);
               local781_19.add((local781_21.floatVal17 - local781_16) * local781_18);
               local781_19.add((local781_21.floatVal18 - local781_17) * local781_18);
            }
         }

         float[] floatArray = new float[local781_19.size()];

         for (int index2 = 0; index2 < local781_19.size(); index2++) {
            floatArray[index2] = local781_19.get(index2);
         }

         return floatArray;
      } catch (Exception exception) {
         return null;
      }
   }

   private String formatText(String local782_1) {
      return switch (local782_1) {
         case "Звезда" -> "textures/particles/star.png";
         case "Сердце" -> "textures/particles/heart.png";
         case "Снежинка" -> "textures/particles/snowflake.png";
         case "Доллар" -> "textures/particles/dollar.png";
         default -> "textures/particles/firefly.png";
      };
   }

   private int computeInt8(int local784_1) {
      return this.customColor.isChecked()
         ? this.colorSetting.getValue()
         : 0xFF000000 | ThemePaletteBuffer.computeInt5((float)(System.currentTimeMillis() % 3600L) / 3600.0F + local784_1 / 360.0F);
   }

   private void processData9(long local787_1) {
      ParticlesModule.DataRecord5 local787_3;
      while ((local787_3 = this.concurrentLinkedQueue.poll()) != null) {
         if (this.hitParticles.isChecked() && this.images.getSelectedCount() != 0) {
            for (int index = 0; index < Math.max(1, (int)this.count2.getValue()); index++) {
               ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
               if (this.list8.size() < 200) {
                  this.list8
                     .add(
                        new ParticlesModule.SubContext4(
                           new Vec3d(
                              local787_3.feetPos().x, local787_3.feetPos().y + threadLocalRandom.nextDouble() * local787_3.heightBlocks(), local787_3.feetPos().z
                           ),
                           new Vec3d(threadLocalRandom.nextDouble(-2.0, 2.0) * 0.08, threadLocalRandom.nextDouble(0.05, 0.2) * 0.08, threadLocalRandom.nextDouble(-2.0, 2.0) * 0.08),
                           local787_1,
                           2200L,
                           false,
                           threadLocalRandom.nextInt(360),
                           this.images.getSelectedOptions().get(threadLocalRandom.nextInt(this.images.getSelectedCount())),
                           this.hitSize.getValue() * 0.42F
                        )
                     );
               }
            }
         }
      }
   }

   private static final class SubContext {
      final Vec3d vec3d4;
      final double doubleVal;
      final long timeMillis2;
      final long timeMillis3;
      final int intVal;
      final float floatVal;
      final float floatVal2;
      final float floatVal3;
      final double doubleVal2;
      final double doubleVal3;
      final double doubleVal4;

      Vec3d getVec3d(float local788_1) {
         return new Vec3d(
            this.vec3d4.x + this.doubleVal2 * local788_1,
            this.vec3d4.y + this.doubleVal3 * local788_1,
            this.vec3d4.z + this.doubleVal4 * local788_1
         );
      }

      Vec3d getVec3d2(float local789_1) {
         return new Vec3d(
            this.vec3d4.x + MathHelper.sin((float)this.doubleVal * 7.0F + local789_1 * 4.0F) * 0.6F,
            this.vec3d4.y + MathHelper.sin((float)this.doubleVal * 5.0F + local789_1 * 3.0F) * 0.4F + local789_1 * 0.6F,
            this.vec3d4.z + MathHelper.cos((float)this.doubleVal * 9.0F + local789_1 * 5.0F) * 0.6F
         );
      }

      SubContext(
         Vec3d vec3d,
         double local790_2,
         long local790_4,
         long local790_6,
         int local790_8,
         float local790_9,
         float local790_10,
         float local790_11,
         double local790_12,
         double local790_14,
         double local790_16
      ) {
         this.vec3d4 = vec3d;
         this.doubleVal = local790_2;
         this.timeMillis2 = local790_4;
         this.timeMillis3 = local790_6;
         this.intVal = local790_8;
         this.floatVal = local790_9;
         this.floatVal2 = local790_10;
         this.floatVal3 = local790_11;
         this.doubleVal2 = local790_12;
         this.doubleVal3 = local790_14;
         this.doubleVal4 = local790_16;
      }
   }

   private static final class SubContext2 {
      final float floatVal4;
      final float floatVal5;
      final float floatVal6;
      final float floatVal7;
      final float floatVal8;
      final float floatVal9;
      final float floatVal10;
      final float floatVal11;
      final float floatVal12;
      int intVal2 = 1;
      boolean flag6 = true;

      SubContext2(
         float local791_1,
         float local791_2,
         float local791_3,
         float local791_4,
         float local791_5,
         float local791_6,
         float local791_7,
         float local791_8,
         float local791_9
      ) {
         this.floatVal4 = local791_1;
         this.floatVal5 = local791_2;
         this.floatVal6 = local791_3;
         this.floatVal7 = local791_4;
         this.floatVal8 = local791_5;
         this.floatVal9 = local791_6;
         this.floatVal10 = local791_7;
         this.floatVal11 = local791_8;
         this.floatVal12 = local791_9;
      }
   }

   private static final class SubContext3 {
      final float floatVal13;
      final float floatVal14;
      final float floatVal15;
      final float floatVal16;
      final float floatVal17;
      final float floatVal18;
      final int intVal3;
      int intVal4 = 1;
      boolean flag7 = true;

      SubContext3(Vector3f vector3f, Vector3f vector3f2, int local792_3) {
         this.floatVal13 = vector3f.x;
         this.floatVal14 = vector3f.y;
         this.floatVal15 = vector3f.z;
         this.floatVal16 = vector3f2.x;
         this.floatVal17 = vector3f2.y;
         this.floatVal18 = vector3f2.z;
         this.intVal3 = local792_3;
      }
   }

   private static final class SubContext4 {
      Vec3d vec3d5;
      Vec3d vec3d6;
      Vec3d vec3d7;
      final long timeMillis4;
      final long timeMillis5;
      final boolean flag8;
      final int intVal5;
      final String strVal;
      final int intVal6;
      final float floatVal19;

      SubContext4(Vec3d vec3d, Vec3d vec3d2, long local793_3, long local793_5, boolean local793_7, int local793_8, String local793_9, float local793_10) {
         this.vec3d5 = vec3d;
         this.vec3d6 = vec3d;
         this.vec3d7 = vec3d2;
         this.timeMillis4 = local793_3;
         this.timeMillis5 = local793_5;
         this.flag8 = local793_7;
         this.intVal5 = local793_8;
         this.strVal = local793_9;
         this.intVal6 = ParticlesModule.computeInt(local793_9);
         this.floatVal19 = local793_10;
      }

      float computeFloat8(long local794_1) {
         return MathHelper.clamp((float)(local794_1 - this.timeMillis4) / (float)this.timeMillis5, 0.0F, 1.0F);
      }

      void updateState2() {
         this.vec3d6 = this.vec3d5;
         if (this.flag8) {
            this.vec3d7 = this.vec3d7.add(0.0, -0.0045, 0.0);
         }

         double local795_1 = this.vec3d5.x + this.vec3d7.x;
         double local795_3 = this.vec3d5.y + this.vec3d7.y;
         double local795_5 = this.vec3d5.z + this.vec3d7.z;
         if (!MinecraftAccess.mc
            .world
            .getBlockState(BlockPos.ofFloored(this.vec3d5.x, this.vec3d5.y, local795_5))
            .getCollisionShape(MinecraftAccess.mc.world, BlockPos.ofFloored(this.vec3d5.x, this.vec3d5.y, local795_5))
            .isEmpty()) {
            this.vec3d7 = new Vec3d(this.vec3d7.x, this.vec3d7.y, this.vec3d7.z * -0.8);
         }

         if (!MinecraftAccess.mc
            .world
            .getBlockState(BlockPos.ofFloored(this.vec3d5.x, local795_3, this.vec3d5.z))
            .getCollisionShape(MinecraftAccess.mc.world, BlockPos.ofFloored(this.vec3d5.x, local795_3, this.vec3d5.z))
            .isEmpty()) {
            if (this.flag8 && this.vec3d7.y < 0.0) {
               this.vec3d7 = this.vec3d7
                  .lerp(new Vec3d(this.vec3d7.x * 0.82, ThreadLocalRandom.current().nextDouble(0.012, 0.028), this.vec3d7.z * 0.82), 0.42);
            } else {
               this.vec3d7 = new Vec3d(this.vec3d7.x * 0.999, this.vec3d7.y * -0.7, this.vec3d7.z * 0.999);
            }
         }

         if (!MinecraftAccess.mc
            .world
            .getBlockState(BlockPos.ofFloored(local795_1, this.vec3d5.y, this.vec3d5.z))
            .getCollisionShape(MinecraftAccess.mc.world, BlockPos.ofFloored(local795_1, this.vec3d5.y, this.vec3d5.z))
            .isEmpty()) {
            this.vec3d7 = new Vec3d(this.vec3d7.x * -0.8, this.vec3d7.y, this.vec3d7.z);
         }

         if (this.flag8) {
            this.vec3d7 = this.vec3d7.multiply(0.996);
         } else {
            this.vec3d7 = new Vec3d(
               MathHelper.clamp(this.vec3d7.x + (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.005, -0.5, 0.5),
               MathHelper.clamp(this.vec3d7.y + (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.005, -0.5, 0.5),
               MathHelper.clamp(this.vec3d7.z + (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.005, -0.5, 0.5)
            );
         }

         this.vec3d5 = this.vec3d5.add(this.vec3d7);
         if (!this.flag8) {
            this.vec3d7 = new Vec3d(this.vec3d7.x / 0.999999, this.vec3d7.y - 5.0E-5, this.vec3d7.z / 0.999999);
         }
      }

      float computeFloat9(long local796_1) {
         long local796_3 = local796_1 - this.timeMillis4;
         if (local796_3 >= this.timeMillis5) {
            return 0.0F;
         } else if (local796_3 < 250L) {
            return (float)local796_3 / 250.0F;
         } else {
            return local796_3 >= this.timeMillis5 - 250L ? (float)(this.timeMillis5 - local796_3) / 250.0F : 1.0F;
         }
      }

      boolean matchesCriteria2(long local797_1) {
         return local797_1 - this.timeMillis4 >= this.timeMillis5;
      }
   }

   private record DataRecord5(Vec3d feetPos, float heightBlocks) {
   }
}

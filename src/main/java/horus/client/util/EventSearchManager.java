package horus.client.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import horus.client.HorusClient;
import horus.client.event.HudRenderEvent;
import horus.client.event.WorldRenderEvent;
import horus.client.module.player.ServerHelperModule;
import horus.client.render.FontFace;
import horus.client.render.FontText;
import horus.client.render.LineDrawData;
import horus.client.render.Render2D;
import horus.client.render.ShapeDrawing;
import horus.client.render.WorldProjection;
import horus.client.ui.hud.HudRenderContext;
import horus.client.ui.hud.HudStyle;
import horus.client.ui.hud.NotificationHud;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.BufferAllocator.CloseableBuffer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class EventSearchManager {
   private static EventSearchManager instance;

   private static final RenderPipeline boxPipeline = RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
      .withLocation(HorusClient.id("pipeline/event_head_esp"))
      .withVertexShader(Identifier.ofVanilla("core/position_color"))
      .withFragmentShader(Identifier.ofVanilla("core/position_color"))
      .withVertexFormat(VertexFormats.POSITION_COLOR, DrawMode.QUADS)
      .withBlend(BlendFunction.LIGHTNING)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withDepthWrite(false)
      .withCull(false)
      .build();

   private static final Vector4f vector4f = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f vector3f = new Vector3f();
   private static final Matrix4f matrix4f = new Matrix4f();

   private static final int[][] QUAD_INDICES = new int[][]{
      {0, 1, 5, 4}, // bottom
      {2, 3, 7, 6}, // top
      {0, 1, 3, 2}, // north
      {4, 5, 7, 6}, // south
      {0, 2, 6, 4}, // west
      {1, 3, 7, 5}  // east
   };

   private static final float[] FACE_SHADING = new float[]{0.5F, 1.0F, 0.75F, 0.75F, 0.6F, 0.6F};

   // State
   private String targetSkinHash = null;
   private String targetLabel = "Кастомная голова";
   private long lastAutoCaptureTime = 0L;
   private int scanTickCounter = 0;
   private boolean wasKeyPressed = false;

   private List<FoundHead> foundHeads = Collections.synchronizedList(new ArrayList<>());
   private GpuBuffer gpuBuffer;
   private BufferAllocator bufferAllocator;

   public record FoundHead(BlockPos pos, Vec3d center, Box boundingBox, double distSq) {}

   public static synchronized EventSearchManager getInstance() {
      if (instance == null) {
         instance = new EventSearchManager();
      }
      return instance;
   }

   private EventSearchManager() {}

   public String getTargetSkinHash() {
      return this.targetSkinHash;
   }

   public boolean hasTarget() {
      return this.targetSkinHash != null && !this.targetSkinHash.isEmpty();
   }

   public void resetTarget(boolean notify) {
      this.targetSkinHash = null;
      this.targetLabel = "Кастомная голова";
      this.foundHeads.clear();
      if (notify) {
         NotificationHud.notifyWithText(
            null,
            "EventSearch",
            Text.literal("Цель сброшена. Наведитесь на новую голову для захвата.").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)),
            true
         );
      }
   }

   public void setTarget(String skinHash, String label, boolean notify) {
      if (skinHash == null || skinHash.isEmpty()) {
         return;
      }
      this.targetSkinHash = skinHash;
      this.targetLabel = (label != null && !label.isBlank()) ? label : "Ивентовая голова";
      this.foundHeads.clear();
      this.scanWorld();

      if (notify) {
         String hashPreview = skinHash.length() > 8 ? skinHash.substring(0, 8) + "..." : skinHash;
         NotificationHud.notifyWithText(
            null,
            "EventSearch",
            Text.literal("Захвачена цель: ").setStyle(Style.EMPTY.withColor(Formatting.GREEN))
               .append(Text.literal(this.targetLabel + " (" + hashPreview + ")").setStyle(Style.EMPTY.withColor(Formatting.AQUA)))
               .append(Text.literal(" | Совпадений: " + this.foundHeads.size()).setStyle(Style.EMPTY.withColor(Formatting.WHITE))),
            true
         );
      }
   }

   public void onPlayerTick() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player == null || mc.world == null) {
         return;
      }

      ServerHelperModule helper = ServerHelperModule.getInstance();
      if (helper == null || !helper.isEnabled() || !helper.modeSetting.isSelected("ReallyWorld") || !helper.eventSearch.isChecked()) {
         return;
      }

      // Check Clear Target button
      if (helper.clearEventTarget.isChecked()) {
         helper.clearEventTarget.setValue(false);
         this.resetTarget(true);
         return;
      }

      // Check manual capture keybind
      boolean isKeyPressed = helper.eventSearchKey.getBinding().isPressed(mc.getWindow().getHandle());
      if (isKeyPressed && !this.wasKeyPressed) {
         this.captureTargetFromCrosshair(true);
      }
      this.wasKeyPressed = isKeyPressed;

      // Auto-capture when no target is set yet and crosshair is pointed at a skull
      if (!this.hasTarget() && helper.eventSearchAutoCapture.isChecked()) {
         long now = System.currentTimeMillis();
         if (now - this.lastAutoCaptureTime > 1500L) {
            if (this.captureTargetFromCrosshair(true)) {
               this.lastAutoCaptureTime = now;
            }
         }
      }

      // Periodic world scan
      this.scanTickCounter++;
      if (this.hasTarget() && this.scanTickCounter >= 8) {
         this.scanTickCounter = 0;
         this.scanWorld();
      }
   }

   public boolean captureTargetFromCrosshair(boolean notify) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player == null || mc.world == null) {
         return false;
      }

      // 1. Direct crosshair hit block
      HitResult hit = mc.crosshairTarget;
      if (hit instanceof BlockHitResult bhr && bhr.getType() == HitResult.Type.BLOCK) {
         BlockPos pos = bhr.getBlockPos();
         BlockEntity be = mc.world.getBlockEntity(pos);
         if (be instanceof SkullBlockEntity skull) {
            String hash = extractSkinHashFromProfile(skull.getOwner());
            if (hash != null) {
               this.setTarget(hash, "Блок головы", notify);
               return true;
            }
         }
      }

      // 2. Direct crosshair hit entity
      if (hit instanceof EntityHitResult ehr && ehr.getEntity() != null) {
         String hash = extractSkinHashFromEntity(ehr.getEntity());
         if (hash != null) {
            this.setTarget(hash, getEntityLabel(ehr.getEntity()), notify);
            return true;
         }
      }

      // 3. Extended raycast (up to 24 blocks from player's eyes)
      Vec3d eyePos = mc.player.getEyePos();
      Vec3d lookVec = mc.player.getRotationVec(1.0F);
      Vec3d reachEnd = eyePos.add(lookVec.multiply(24.0));

      BlockHitResult extendedBlockHit = mc.world.raycast(new RaycastContext(
         eyePos, reachEnd, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player
      ));
      if (extendedBlockHit.getType() == HitResult.Type.BLOCK) {
         BlockPos pos = extendedBlockHit.getBlockPos();
         BlockEntity be = mc.world.getBlockEntity(pos);
         if (be instanceof SkullBlockEntity skull) {
            String hash = extractSkinHashFromProfile(skull.getOwner());
            if (hash != null) {
               this.setTarget(hash, "Блок головы", notify);
               return true;
            }
         }
      }

      // Extended entity raycast
      Box traceBox = new Box(eyePos, reachEnd).expand(1.0);
      for (Entity entity : mc.world.getOtherEntities(mc.player, traceBox)) {
         Box b = entity.getBoundingBox().expand(0.35);
         Optional<Vec3d> rayHit = b.raycast(eyePos, reachEnd);
         if (rayHit.isPresent()) {
            String hash = extractSkinHashFromEntity(entity);
            if (hash != null) {
               this.setTarget(hash, getEntityLabel(entity), notify);
               return true;
            }
         }
      }

      // 4. Check main hand item as fallback
      ItemStack mainHand = mc.player.getMainHandStack();
      if (mainHand.isOf(Items.PLAYER_HEAD)) {
         String hash = extractSkinHashFromStack(mainHand);
         if (hash != null) {
            this.setTarget(hash, "Голова в руке", notify);
            return true;
         }
      }

      if (notify) {
         NotificationHud.notifyWithText(
            null,
            "EventSearch",
            Text.literal("Кастомная голова не найдена в прицеле! Наведитесь на голову.").setStyle(Style.EMPTY.withColor(Formatting.RED)),
            true
         );
      }
      return false;
   }

   public void scanWorld() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player == null || mc.world == null || !this.hasTarget()) {
         this.foundHeads.clear();
         return;
      }

      ServerHelperModule helper = ServerHelperModule.getInstance();
      float searchDist = (helper != null) ? helper.eventSearchDistance.getValue() : 96.0F;
      double maxDistSq = searchDist * searchDist;
      int chunkRadius = (int) Math.ceil(searchDist / 16.0F) + 1;
      int playerChunkX = mc.player.getChunkPos().x;
      int playerChunkZ = mc.player.getChunkPos().z;
      Vec3d playerPos = mc.player.getEyePos();

      List<FoundHead> list = new ArrayList<>();

      // 1. Scan loaded chunk block entities
      for (int cx = playerChunkX - chunkRadius; cx <= playerChunkX + chunkRadius; cx++) {
         for (int cz = playerChunkZ - chunkRadius; cz <= playerChunkZ + chunkRadius; cz++) {
            WorldChunk chunk = mc.world.getChunk(cx, cz);
            if (chunk == null) continue;

            for (BlockEntity be : chunk.getBlockEntities().values()) {
               if (be instanceof SkullBlockEntity skull) {
                  String hash = extractSkinHashFromProfile(skull.getOwner());
                  if (this.matchesTarget(hash)) {
                     BlockPos pos = skull.getPos();
                     Vec3d center = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5);
                     double dSq = center.squaredDistanceTo(playerPos);
                     if (dSq <= maxDistSq) {
                        Box box = new Box(pos.getX() + 0.25, pos.getY(), pos.getZ() + 0.25, pos.getX() + 0.75, pos.getY() + 0.5, pos.getZ() + 0.75);
                        list.add(new FoundHead(pos, center, box, dSq));
                     }
                  }
               }
            }
         }
      }

      // 2. Scan entities (ItemFrames, ArmorStands, Items)
      for (Entity entity : mc.world.getEntities()) {
         String hash = extractSkinHashFromEntity(entity);
         if (this.matchesTarget(hash)) {
            Vec3d center = entity.getBoundingBox().getCenter();
            double dSq = center.squaredDistanceTo(playerPos);
            if (dSq <= maxDistSq) {
               list.add(new FoundHead(entity.getBlockPos(), center, entity.getBoundingBox(), dSq));
            }
         }
      }

      // Sort nearest first
      list.sort((a, b) -> Double.compare(a.distSq(), b.distSq()));
      this.foundHeads = Collections.synchronizedList(list);
   }

   public boolean matchesTarget(String skinHash) {
      if (skinHash == null || this.targetSkinHash == null) {
         return false;
      }
      return this.targetSkinHash.equalsIgnoreCase(skinHash);
   }

   // ------------------ 2D HUD Rendering (Tracers + Badges) ------------------

   public void onHudRender(HudRenderEvent event) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player == null || mc.world == null) {
         return;
      }

      ServerHelperModule helper = ServerHelperModule.getInstance();
      if (helper == null || !helper.isEnabled() || !helper.modeSetting.isSelected("ReallyWorld") || !helper.eventSearch.isChecked()) {
         return;
      }

      HudRenderContext.renderElement(event.getDrawContext());
      int screenW = event.computeInt2();
      int screenH = event.computeInt();
      float guiScaleFactor = GuiScale.computeInt() / GuiScale.computeFloat();

      float screenCenterX = (screenW / 2.0F) * guiScaleFactor;
      float screenCenterY = (screenH / 2.0F) * guiScaleFactor;

      boolean showTracers = helper.eventSearchTracers.isChecked();
      List<FoundHead> currentList = new ArrayList<>(this.foundHeads);

      for (FoundHead head : currentList) {
         Vec3d projected = WorldProjection.getVec3d2(head.center(), screenW, screenH);
         if (projected == null) {
            continue;
         }

         float tagX = (float) projected.x * guiScaleFactor;
         float tagY = (float) projected.y * guiScaleFactor;
         double dist = Math.sqrt(head.distSq());

         // Draw tracer line from screen center to the head tag
         if (showTracers) {
            int tracerColor = 0xAA06B6D4; // Cyan neon tracer
            Render2D.processData27(screenCenterX, screenCenterY, tagX, tagY, 1.25F, tracerColor);
         }

         // Draw 2D ClickGUI Glass Badge
         String distStr = String.format(Locale.US, "%.1fм", dist);
         String label = "Ивент";
         float lW = FontText.computeFloat(FontFace.BOLD, label, 6.0F);
         float dW = FontText.computeFloat(FontFace.SEMI_BOLD, distStr, 6.0F);
         float badgeW = 6.0F + lW + 3.0F + dW + 6.0F;
         float badgeH = 13.0F;

         float badgeX = Math.round((tagX - badgeW / 2.0F) * 2.0F) / 2.0F;
         float badgeY = Math.round((tagY - badgeH / 2.0F) * 2.0F) / 2.0F;

         HudStyle.processData2(badgeX, badgeY, badgeW, badgeH, 1.0F, 4.0F, ShapeDrawing.TypeOption.LOW);
         FontText.processData3(FontFace.BOLD, label, badgeX + 5.0F, badgeY + badgeH / 2.0F - 3.5F, 6.0F, 0xFF22D3EE);
         FontText.processData3(FontFace.SEMI_BOLD, distStr, badgeX + 5.0F + lW + 3.0F, badgeY + badgeH / 2.0F - 3.5F, 6.0F, 0xFFFFFFFF);
      }

      // Draw Top Status Pill if target is active
      if (this.hasTarget()) {
         String statusText = "EventSearch: Найдено " + currentList.size() + " шт.";
         float sW = FontText.computeFloat(FontFace.SEMI_BOLD, statusText, 6.0F);
         float pillW = sW + 16.0F;
         float pillH = 13.0F;
         float pillX = screenCenterX - pillW / 2.0F;
         float pillY = 18.0F * guiScaleFactor;

         HudStyle.processData2(pillX, pillY, pillW, pillH, 0.85F, 4.0F, ShapeDrawing.TypeOption.LOW);
         int statusColor = currentList.isEmpty() ? 0xFFFBBF24 : 0xFF22D3EE;
         FontText.processData3(FontFace.SEMI_BOLD, statusText, pillX + 8.0F, pillY + pillH / 2.0F - 3.5F, 6.0F, statusColor);
      }
   }

   // ------------------ 3D ESP Rendering (Through-Wall Boxes) ------------------

   public void onWorldRender(WorldRenderEvent event) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player == null || mc.world == null || mc.getFramebuffer() == null || mc.getFramebuffer().getColorAttachmentView() == null) {
         return;
      }

      ServerHelperModule helper = ServerHelperModule.getInstance();
      if (helper == null || !helper.isEnabled() || !helper.modeSetting.isSelected("ReallyWorld") || !helper.eventSearch.isChecked() || !helper.eventSearchBoxes.isChecked()) {
         return;
      }

      List<FoundHead> currentList = new ArrayList<>(this.foundHeads);
      if (currentList.isEmpty()) {
         return;
      }

      float camX = (float) mc.gameRenderer.getCamera().getCameraPos().x;
      float camY = (float) mc.gameRenderer.getCamera().getCameraPos().y;
      float camZ = (float) mc.gameRenderer.getCamera().getCameraPos().z;

      if (this.bufferAllocator == null) {
         this.bufferAllocator = new BufferAllocator(8192 * VertexFormats.POSITION_COLOR.getVertexSize());
      }

      CloseableBuffer allocated = this.bufferAllocator.getAllocated();
      if (allocated != null) {
         allocated.close();
      }

      BufferBuilder bufferBuilder = new BufferBuilder(this.bufferAllocator, DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      int quadCount = 0;
      int baseColor = 0xFF06B6D4; // Neon Cyan
      int alpha = 85;

      for (FoundHead head : currentList) {
         Box b = head.boundingBox();
         float minX = (float) (b.minX - camX);
         float minY = (float) (b.minY - camY);
         float minZ = (float) (b.minZ - camZ);
         float maxX = (float) (b.maxX - camX);
         float maxY = (float) (b.maxY - camY);
         float maxZ = (float) (b.maxZ - camZ);

         for (int face = 0; face < 6; face++) {
            float shade = FACE_SHADING[face];
            int r = Math.min(255, (int) (((baseColor >> 16) & 0xFF) * shade));
            int g = Math.min(255, (int) (((baseColor >> 8) & 0xFF) * shade));
            int bl = Math.min(255, (int) ((baseColor & 0xFF) * shade));
            int faceColor = (alpha << 24) | (r << 16) | (g << 8) | bl;

            for (int idx : QUAD_INDICES[face]) {
               float vx = (idx & 4) == 0 ? minX : maxX;
               float vy = (idx & 2) == 0 ? minY : maxY;
               float vz = (idx & 1) == 0 ? minZ : maxZ;
               bufferBuilder.vertex(vx, vy, vz).color(faceColor);
            }
            quadCount++;
         }
      }

      if (quadCount == 0) {
         return;
      }

      try (BuiltBuffer builtBuffer = bufferBuilder.end()) {
         if (this.gpuBuffer != null && !this.gpuBuffer.isClosed() && this.gpuBuffer.size() >= builtBuffer.getBuffer().remaining()) {
            RenderSystem.getDevice()
               .createCommandEncoder()
               .writeToBuffer(this.gpuBuffer.slice(0L, builtBuffer.getBuffer().remaining()), builtBuffer.getBuffer());
         } else {
            if (this.gpuBuffer != null && !this.gpuBuffer.isClosed()) {
               this.gpuBuffer.close();
            }
            this.gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "horus_event_head_esp", 40, builtBuffer.getBuffer());
         }

         GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms()
            .write(event.getMatrixStack().peek().getPositionMatrix(), vector4f, vector3f, matrix4f);

         try (RenderPass renderPass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(
               () -> "horus_event_head_esp_pass", mc.getFramebuffer().getColorAttachmentView(), OptionalInt.empty(), null, OptionalDouble.empty()
            )) {
            renderPass.setPipeline(boxPipeline);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
            renderPass.setVertexBuffer(0, this.gpuBuffer);
            renderPass.setIndexBuffer(
               RenderSystem.getSequentialBuffer(DrawMode.QUADS).getIndexBuffer(quadCount * 6),
               RenderSystem.getSequentialBuffer(DrawMode.QUADS).getIndexType()
            );
            renderPass.drawIndexed(0, 0, quadCount * 6, 1);
         }
      }
   }

   // ------------------ Texture / Skin Extraction Helpers ------------------

   public static String extractSkinHashFromProfile(ProfileComponent profile) {
      if (profile == null) return null;
      GameProfile gp = profile.getGameProfile();
      if (gp == null) return null;

      if (gp.properties() != null) {
         for (Property prop : gp.properties().get("textures")) {
            if (prop != null && prop.value() != null && !prop.value().isEmpty()) {
               String hash = extractSkinHashFromRaw(prop.value());
               if (hash != null) return hash;
            }
         }
      }
      if (gp.id() != null) {
         return gp.id().toString();
      }
      if (gp.name() != null && !gp.name().isBlank()) {
         return gp.name();
      }
      return null;
   }

   public static String extractSkinHashFromStack(ItemStack stack) {
      if (stack == null || stack.isEmpty()) return null;
      ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);
      return extractSkinHashFromProfile(profile);
   }

   public static String extractSkinHashFromEntity(Entity entity) {
      if (entity == null) return null;
      if (entity instanceof ItemFrameEntity frame) {
         return extractSkinHashFromStack(frame.getHeldItemStack());
      } else if (entity instanceof ArmorStandEntity stand) {
         return extractSkinHashFromStack(stand.getEquippedStack(EquipmentSlot.HEAD));
      } else if (entity instanceof ItemEntity itemEntity) {
         return extractSkinHashFromStack(itemEntity.getStack());
      }
      return null;
   }

   public static String extractSkinHashFromRaw(String raw) {
      if (raw == null || raw.isBlank()) return null;

      // 1. Direct URL check
      if (raw.contains("/texture/")) {
         int idx = raw.indexOf("/texture/");
         String sub = raw.substring(idx + 9);
         int end = sub.indexOf('"');
         return end != -1 ? sub.substring(0, end) : sub;
      }

      // 2. Base64 decode to inspect JSON
      try {
         byte[] decodedBytes = Base64.getDecoder().decode(raw);
         String jsonStr = new String(decodedBytes, StandardCharsets.UTF_8);
         if (jsonStr.contains("/texture/")) {
            int idx = jsonStr.indexOf("/texture/");
            String sub = jsonStr.substring(idx + 9);
            int end = sub.indexOf('"');
            return end != -1 ? sub.substring(0, end) : sub;
         }
         try {
            JsonObject obj = JsonParser.parseString(jsonStr).getAsJsonObject();
            if (obj.has("textures")) {
               JsonObject textures = obj.getAsJsonObject("textures");
               if (textures.has("SKIN")) {
                  JsonObject skin = textures.getAsJsonObject("SKIN");
                  if (skin.has("url")) {
                     String url = skin.get("url").getAsString();
                     int idx = url.indexOf("/texture/");
                     return idx != -1 ? url.substring(idx + 9) : url;
                  }
               }
            }
         } catch (Exception ignored) {}
         return jsonStr;
      } catch (Exception ignored) {}

      return raw;
   }

   private static String getEntityLabel(Entity entity) {
      if (entity instanceof ItemFrameEntity) return "Рамка";
      if (entity instanceof ArmorStandEntity) return "Стойка";
      if (entity instanceof ItemEntity) return "Предмет";
      return "Сущность";
   }
}

package horus.client.module.misc;

import horus.client.util.ClientChat;
import horus.client.event.EventListener;
import horus.client.render.FontFace;
import horus.client.event.HudRenderEvent;
import horus.client.setting.KeybindSetting;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.event.PlayerTickEvent;
import horus.client.util.GuiScale;
import horus.client.util.SupabaseProfileManager;
import horus.client.render.WorldProjection;
import horus.client.render.FontText;
import horus.client.ui.hud.NotificationHud;
import horus.client.render.ShapeDrawing;
import horus.client.render.ThemeColors;
import horus.client.ui.hud.HudRenderContext;
import horus.client.ui.hud.HudStyle;

import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(
   name = "Party",
   description = "Пати с друзьями: видно участников на любом расстоянии и общие метки. Управление через .party",
   category = ModuleCategory.MISC
)
public final class PartyModule extends Module {
   private static PartyModule partyModule;
   public KeybindSetting waypointKey;
   public KeybindSetting playerWaypointKey;
   private static final Random RANDOM_SOURCE = new Random();
   private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).build();
   private final ExecutorService executorService = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "horus-party");
      thread.setDaemon(true);
      return thread;
   });
   private final String strVal = UUID.randomUUID().toString();
   private final AtomicBoolean atomicBoolean = new AtomicBoolean();
   private final HashMap<String, Vec3d> map = new HashMap<>();
   private final HashSet<String> set2 = new HashSet<>();
   private final HashSet<String> set3 = new HashSet<>();
   private volatile PartyModule.DataRecord2 nested0462;
   private String strVal2;
   private long timeMillis;
   private long timeMillis2;
   private boolean flag;
   private boolean flag2;
   private final EventListener<PlayerTickEvent> playerTickListener = playerTickEvent -> {
      if (mc.player != null && mc.world != null) {
         boolean local4677_2 = this.waypointKey.getBinding().isPressed(mc.getWindow().getHandle());
         if (local4677_2 && !this.flag && mc.currentScreen == null) {
            this.updateState3();
         }

         this.flag = local4677_2;
         boolean local4677_3 = this.playerWaypointKey.getBinding().isPressed(mc.getWindow().getHandle());
         if (local4677_3 && !this.flag2 && mc.currentScreen == null) {
            this.updateState5();
         }

         this.flag2 = local4677_3;
         if (this.strVal2 != null && System.currentTimeMillis() - this.timeMillis >= 500L && this.atomicBoolean.compareAndSet(false, true)) {
            this.timeMillis = System.currentTimeMillis();
            this.runAsync(this::apiBeat, this::processData3);
            this.atomicBoolean.set(false);
         }
      }
   };
   private final EventListener<HudRenderEvent> hudRenderListener = hudRenderEvent -> {
      PartyModule.DataRecord2 local4681_2 = this.nested0462;
      if (local4681_2 != null && mc.player != null && mc.world != null) {
         long local4681_3 = System.currentTimeMillis();
         float local4681_5 = Math.min(1.0F, (float)(local4681_3 - this.timeMillis2) / 1000.0F * 8.0F);
         this.timeMillis2 = local4681_3;
         HudRenderContext.renderElement(hudRenderEvent.getDrawContext());
         String local4681_6 = this.formatText();
         String local4681_7 = this.formatText3();
         String local4681_8 = mc.player.getGameProfile().name();

         for (PartyModule.DataRecord local4681_10 : local4681_2.members()) {
            if (!local4681_10.nick().equals(local4681_8) && local4681_10.host().equals(local4681_6) && local4681_10.dim().equals(local4681_7)) {
               AbstractClientPlayerEntity abstractClientPlayerEntity = this.getAbstractClientPlayerEntity(local4681_10.nick());
               Vec3d vec3d = abstractClientPlayerEntity != null ? abstractClientPlayerEntity.getLerpedPos(hudRenderEvent.computeFloat()) : local4681_10.position();
               Vec3d vec3d2 = this.map.get(local4681_10.nick());
               vec3d2 = vec3d2 != null && abstractClientPlayerEntity == null ? vec3d2.lerp(vec3d, local4681_5) : vec3d;
               this.map.put(local4681_10.nick(), vec3d2);
               String local4681_14 = (int)Math.ceil(
                     abstractClientPlayerEntity != null ? abstractClientPlayerEntity.getHealth() + abstractClientPlayerEntity.getAbsorptionAmount() : local4681_10.health()
                  )
                  + "hp";
               this.processData5(hudRenderEvent, vec3d2.add(0.0, 2.3, 0.0), local4681_10.nick(), local4681_14, HudStyle.computeInt4(1.0F), -1.0F);
            }
         }

         this.map.keySet().removeIf(local4676_1 -> local4681_2.members().stream().noneMatch(local4669_1 -> local4669_1.nick().equals(local4676_1)));

         for (PartyModule.DataRecord3 local4681_10 : local4681_2.marks()) {
            if (local4681_10.host().equals(local4681_6) && local4681_10.dim().equals(local4681_7)) {
               if (local4681_10.type().equals("player")) {
                  AbstractClientPlayerEntity abstractClientPlayerEntity2 = this.getAbstractClientPlayerEntity(local4681_10.target());
                  Vec3d vec3d3 = abstractClientPlayerEntity2 != null ? abstractClientPlayerEntity2.getLerpedPos(hudRenderEvent.computeFloat()) : local4681_10.position();
                  float local4681_13 = (float)mc.gameRenderer.getCamera().getCameraPos().distanceTo(vec3d3);
                  this.processData5(
                     hudRenderEvent,
                     vec3d3.add(0.0, 2.9, 0.0),
                     "Цель " + local4681_10.target(),
                     local4681_10.by(),
                     -43691,
                     Math.max(0.3F, 1.0F - local4681_13 * 0.008F)
                  );
               } else {
                  this.processData5(hudRenderEvent, local4681_10.position(), "Метка", local4681_10.by(), ThemeColors.intVal9, -1.0F);
               }
            }
         }
      }
   };

   @FunctionalInterface
   private interface ThrowingSupplier<T> {
      T get() throws Exception;
   }

   /** Выполняет операцию API в фоне, результат — на главном потоке. */
   /** Выполняет операцию API в фоне, результат — на главном потоке. */
   private void runAsync(ThrowingSupplier<JsonObject> operation, Consumer<JsonObject> callback) {
      this.executorService.execute(() -> {
         try {
            JsonObject result = operation.get();
            mc.execute(() -> callback.accept(result));
         } catch (Exception exception) {
            mc.execute(() -> ClientChat.processData8("&cПати: сервер недоступен"));
         }
      });
   }

   private String formatText() {
      IntegratedServer server = mc.getServer();
      if (server != null) {
         return "lan:" + (server.isRemote() ? server.getServerPort() : 0);
      } else if (mc.getNetworkHandler() == null
         || !(
            mc.getNetworkHandler().getConnection().getAddress() instanceof InetSocketAddress inetSocketAddress
               && inetSocketAddress.getAddress() != null
               && (inetSocketAddress.getAddress().isLoopbackAddress() || inetSocketAddress.getAddress().isSiteLocalAddress())
         )) {
         ServerInfo currentServerEntry = mc.getCurrentServerEntry();
         return currentServerEntry == null ? "" : currentServerEntry.address.trim().toLowerCase(Locale.ROOT).replaceFirst(":25565$", "").replaceFirst("\\.$", "");
      } else {
         return "lan:" + inetSocketAddress.getPort();
      }
   }

   public void processData2(String local4661_1) {
      if (this.checkState()) {
         this.runAsync(() -> this.apiKick(local4661_1), jsonObject2 -> {
            if (jsonObject2.get("ok").getAsBoolean()) {
               this.processData8(jsonObject2);
               ClientChat.processData8("&7" + local4661_1 + "&r исключён из пати.");
            } else {
               ClientChat.processData8("&cПати: " + this.formatText2(jsonObject2.get("error").getAsString()));
            }
         });
      }
   }

   private void processData3(JsonObject jsonObject) {
      if (this.strVal2 != null) {
         if (jsonObject.get("ok").getAsBoolean()) {
            this.processData8(jsonObject);
         } else {
            String asString = jsonObject.get("error").getAsString();
            if (asString.equals("not_member") || asString.equals("not_found")) {
               String local4668_3 = this.strVal2;
               this.updateState();
               ClientChat.processData8(
                  asString.equals("not_found") ? "Пати &7" + local4668_3 + "&r распущено." : "Тебя больше нет в пати &7" + local4668_3 + "&r."
               );
            }
         }
      }
   }

   private void updateState() {
      this.strVal2 = null;
      this.nested0462 = null;
      this.map.clear();
      this.set2.clear();
      this.set3.clear();
   }

   public void updateState2() {
      if (this.strVal2 == null) {
         ClientChat.processData8("Ты не в пати.");
      } else {
         String code = this.strVal2;
         this.updateState();
         this.runAsync(() -> this.apiLeaveById(code), jsonObject2 -> ClientChat.processData8("Ты вышел из пати."));
      }
   }

   public void processData4(String local4673_1) {
      if (mc.player != null) {
         if (!this.isEnabled()) {
            this.setEnabled(true);
         }

         if (this.strVal2 != null) {
            this.updateState2();
         }

         this.runAsync(() -> this.apiJoin(local4673_1), jsonObject2 -> {
            if (jsonObject2.get("ok").getAsBoolean()) {
               this.processData8(jsonObject2);
               ClientChat.processData8("Ты в пати &a" + this.strVal2 + "&r, владелец: &7" + jsonObject2.get("owner").getAsString());
            } else {
               ClientChat.processData8("&cПати: " + this.formatText2(jsonObject2.get("error").getAsString()));
            }
         });
      }
   }

   private void updateState3() {
      if (this.checkState()) {
         HitResult hitResult = mc.player.raycast(256.0, 1.0F, false);
         if (hitResult instanceof BlockHitResult blockHitResult && hitResult.getType() == Type.BLOCK) {
            Vec3d vec3d = blockHitResult.getBlockPos().toCenterPos().add(0.0, 0.5, 0.0);
            this.processData7("point", "", vec3d);
         } else {
            NotificationHud.processData3(this.category.getIcon(), "Метка", "Слишком далеко", true);
         }
      }
   }

   private void processData5(HudRenderEvent hudRenderEvent, Vec3d vec3d, String local4678_3, String local4678_4, int local4678_5, float local4678_6) {
      Vec3d vec3d2 = WorldProjection.getVec3d2(vec3d, hudRenderEvent.computeInt2(), hudRenderEvent.computeInt());
      if (vec3d2 != null) {
         float local4678_8 = GuiScale.computeInt() / GuiScale.computeFloat();
         float local4678_9 = (float)vec3d2.x * local4678_8;
         float local4678_10 = (float)vec3d2.y * local4678_8;
         float local4678_11 = (float)mc.player.getEntityPos().distanceTo(vec3d);
         String local4678_12 = String.format(Locale.US, "%.0fм", local4678_11);
         float local4678_13 = FontText.computeFloat(FontFace.SEMI_BOLD, local4678_3, 6.0F);
         float local4678_14 = FontText.computeFloat(FontFace.SEMI_BOLD, local4678_4, 6.0F);
         float local4678_15 = FontText.computeFloat(FontFace.SEMI_BOLD, local4678_12, 6.0F);
         float local4678_16 = 4.0F + local4678_13 + 3.0F + local4678_14 + 3.0F + local4678_15 + 4.0F;
         float local4678_17 = Math.round((local4678_9 - local4678_16 / 2.0F) * 2.0F) / 2.0F;
         float local4678_18 = Math.round((local4678_10 - 6.5F) * 2.0F) / 2.0F;
         HudRenderContext.processData3(local4678_9, local4678_10, local4678_6 > 0.0F ? local4678_6 : (float)Math.max(0.6, 1.1 - local4678_11 / 200.0));
         HudStyle.processData2(local4678_17, local4678_18, local4678_16, 13.0F, 1.0F, 4.5F, ShapeDrawing.TypeOption.LOW);
         float local4678_19 = local4678_18 + 6.5F - 3.5F;
         FontText.processData3(FontFace.SEMI_BOLD, local4678_3, local4678_17 + 4.0F, local4678_19, 6.0F, local4678_5);
         FontText.processData3(
            FontFace.SEMI_BOLD, local4678_4, local4678_17 + 4.0F + local4678_13 + 3.0F, local4678_19, 6.0F, HudStyle.computeInt2(1.0F)
         );
         FontText.processData3(
            FontFace.SEMI_BOLD,
            local4678_12,
            local4678_17 + 4.0F + local4678_13 + 3.0F + local4678_14 + 3.0F,
            local4678_19,
            6.0F,
            HudStyle.computeInt2(1.0F)
         );
         HudRenderContext.updateState();
      }
   }

   public void processData6(boolean local4682_1) {
      if (this.checkState()) {
         this.runAsync(() -> this.apiLock(local4682_1), jsonObject2 -> {
            if (jsonObject2.get("ok").getAsBoolean()) {
               this.processData8(jsonObject2);
               ClientChat.processData8(local4682_1 ? "Пати &cзакрыто&r, новые игроки не зайдут." : "Пати &aоткрыто&r для входа.");
            } else {
               ClientChat.processData8("&cПати: " + this.formatText2(jsonObject2.get("error").getAsString()));
            }
         });
      }
   }

   private void processData7(String local4683_1, String local4683_2, Vec3d vec3d) {
      this.runAsync(
         () -> this.apiMark(local4683_1, local4683_2, vec3d),
         jsonObject2 -> {
            if (jsonObject2.get("ok").getAsBoolean()) {
               this.processData8(jsonObject2);
               NotificationHud.processData3(
                  this.category.getIcon(), "Метка", local4683_1.equals("player") ? "Отмечен " + local4683_2 : "Метка поставлена", true
               );
            }
         }
      );
   }

   private void processData8(JsonObject jsonObject) {
      PartyModule.DataRecord2 local4684_2 = this.nested0462;
      ArrayList<PartyModule.DataRecord> local4684_3 = new ArrayList<>();

      for (JsonElement jsonElement : jsonObject.getAsJsonArray("members")) {
         JsonObject asJsonObject = jsonElement.getAsJsonObject();
         local4684_3.add(
            new PartyModule.DataRecord(
               asJsonObject.get("nick").getAsString(),
               asJsonObject.get("host").getAsString(),
               asJsonObject.get("dim").getAsString(),
               new Vec3d(asJsonObject.get("x").getAsDouble(), asJsonObject.get("y").getAsDouble(), asJsonObject.get("z").getAsDouble()),
               asJsonObject.get("hp").getAsFloat()
            )
         );
      }

      ArrayList<PartyModule.DataRecord3> local4684_4 = new ArrayList<>();

      for (JsonElement jsonElement2 : jsonObject.getAsJsonArray("marks")) {
         JsonObject asJsonObject2 = jsonElement2.getAsJsonObject();
         local4684_4.add(
            new PartyModule.DataRecord3(
               asJsonObject2.get("id").getAsString(),
               asJsonObject2.get("by").getAsString(),
               asJsonObject2.get("type").getAsString(),
               asJsonObject2.get("target").getAsString(),
               asJsonObject2.get("host").getAsString(),
               asJsonObject2.get("dim").getAsString(),
               new Vec3d(asJsonObject2.get("x").getAsDouble(), asJsonObject2.get("y").getAsDouble(), asJsonObject2.get("z").getAsDouble())
            )
         );
      }

      PartyModule.DataRecord2 local4684_5 = new PartyModule.DataRecord2(
         jsonObject.get("code").getAsString(), jsonObject.get("owner").getAsString(), jsonObject.get("locked").getAsBoolean(), local4684_3, local4684_4
      );
      this.strVal2 = local4684_5.code();
      this.nested0462 = local4684_5;
      if (local4684_2 != null) {
         String local4684_6 = mc.player == null ? "" : mc.player.getGameProfile().name();

         for (PartyModule.DataRecord local4684_8 : local4684_3) {
            if (local4684_2.members().stream().noneMatch(local4687_1 -> local4687_1.nick().equals(local4684_8.nick()))
               && this.set3.add(local4684_8.nick())) {
               ClientChat.processData8("&a+ &7" + local4684_8.nick() + "&r зашёл в пати.");
            }
         }

         for (PartyModule.DataRecord local4684_8 : local4684_2.members()) {
            if (local4684_3.stream().noneMatch(local4695_1 -> local4695_1.nick().equals(local4684_8.nick()))
               && this.set3.remove(local4684_8.nick())) {
               ClientChat.processData8("&c- &7" + local4684_8.nick() + "&r вышел из пати.");
            }
         }

         for (PartyModule.DataRecord3 local4684_8 : local4684_4) {
            if (this.set2.add(local4684_8.id()) && !local4684_8.by().equals(local4684_6)) {
               NotificationHud.processData3(
                  this.category.getIcon(),
                  local4684_8.by(),
                  local4684_8.type().equals("player") ? "отметил " + local4684_8.target() : "поставил метку",
                  true
               );
            }
         }

         this.set2.removeIf(local4679_1 -> local4684_4.stream().noneMatch(local4662_1 -> local4662_1.id().equals(local4679_1)));
      } else {
         for (PartyModule.DataRecord3 local4684_7 : local4684_4) {
            this.set2.add(local4684_7.id());
         }

         for (PartyModule.DataRecord local4684_7 : local4684_3) {
            this.set3.add(local4684_7.nick());
         }
      }
   }

   public void updateState4() {
      PartyModule.DataRecord2 local4685_1 = this.nested0462;
      if (local4685_1 == null) {
         ClientChat.processData8("Ты не в пати. &7.party create&r или &7.party join <код>");
      } else {
         StringBuilder stringBuilder = new StringBuilder();

         for (PartyModule.DataRecord local4685_4 : local4685_1.members()) {
            stringBuilder.append(stringBuilder.isEmpty() ? "" : "&r, ")
               .append(local4685_4.nick().equals(local4685_1.owner()) ? "&e" : "&7")
               .append(local4685_4.nick());
         }

         ClientChat.processData8("Пати &a" + local4685_1.code() + "&r " + (local4685_1.locked() ? "&c(закрыто)" : "&a(открыто)") + "&r: " + stringBuilder);
      }
   }

   private String formatText2(String local4686_1) {
      return switch (local4686_1) {
         case "not_found" -> "пати с таким кодом нет";
         case "locked" -> "пати закрыто";
         case "full" -> "пати заполнено";
         case "not_owner" -> "это может только создатель пати";
         case "not_member" -> "ты не в этом пати";
         default -> local4686_1;
      };
   }

   private AbstractClientPlayerEntity getAbstractClientPlayerEntity(String local4688_1) {
      for (AbstractClientPlayerEntity abstractClientPlayerEntity : mc.world.getPlayers()) {
         if (abstractClientPlayerEntity.getGameProfile().name().equalsIgnoreCase(local4688_1)) {
            return abstractClientPlayerEntity;
         }
      }

      return null;
   }

   public static PartyModule getInstance() {
      if (partyModule == null) {
         partyModule = new PartyModule();
      }

      return partyModule;
   }



   // ===== Supabase REST API =====

   private static final String REST_BASE = SupabaseProfileManager.SUPABASE_URL + "/rest/v1";
   private static final String API_KEY = SupabaseProfileManager.SUPABASE_KEY;
   private static final long STALE_MS = 6000L;

   private String myNick() {
      return mc.player == null ? "" : mc.player.getGameProfile().name();
   }

   private String nowIso() {
      return Instant.now().truncatedTo(ChronoUnit.MILLIS).toString();
   }

   private String http(String method, String path, String body, String prefer) throws Exception {
      HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(REST_BASE + path))
         .timeout(Duration.ofSeconds(6L))
         .header("apikey", API_KEY)
         .header("Authorization", "Bearer " + API_KEY)
         .header("Content-Type", "application/json");
      if (prefer != null) {
         builder.header("Prefer", prefer);
      }

      builder.method(method, BodyPublishers.ofString(body == null ? "" : body, StandardCharsets.UTF_8));
      HttpResponse<String> response = this.httpClient.send(builder.build(), BodyHandlers.ofString());
      int status = response.statusCode();
      if (status >= 200 && status < 300) {
         return response.body();
      }

      throw new IllegalStateException("HTTP " + status + " " + response.body());
   }

   private JsonArray httpArray(String path) throws Exception {
      String body = this.http("GET", path, null, null);
      return body == null || body.isBlank() ? new JsonArray() : JsonParser.parseString(body).getAsJsonArray();
   }

   private JsonObject firstRow(String path) throws Exception {
      JsonArray array = this.httpArray(path);
      return array.isEmpty() ? null : array.get(0).getAsJsonObject();
   }

   /** Собирает состояние пати (members/marks/owner/locked) в формате старого сервера. */
   private JsonObject fetchState(String code) throws Exception {
      JsonObject meta = this.firstRow("/party_meta?code=eq." + code + "&select=code,owner,locked");
      if (meta == null) {
         JsonObject error = new JsonObject();
         error.addProperty("ok", false);
         error.addProperty("error", "not_found");
         return error;
      }

      JsonObject result = new JsonObject();
      result.addProperty("ok", true);
      result.addProperty("code", meta.get("code").getAsString());
      result.addProperty("owner", meta.get("owner").getAsString());
      result.addProperty("locked", meta.get("locked").getAsBoolean());

      Instant cutoff = Instant.now().minusMillis(STALE_MS);
      JsonArray members = this.httpArray("/party_members?code=eq." + code + "&select=nick,host,dim,x,y,z,hp,last_seen");
      JsonArray freshMembers = new JsonArray();

      for (JsonElement element : members) {
         JsonObject member = element.getAsJsonObject();
         Instant seen = Instant.parse(member.get("last_seen").getAsString());
         if (seen.isBefore(cutoff)) {
            continue;
         }

         JsonObject copy = new JsonObject();
         copy.addProperty("nick", member.get("nick").getAsString());
         copy.addProperty("host", member.get("host").getAsString());
         copy.addProperty("dim", member.get("dim").getAsString());
         copy.addProperty("x", member.get("x").getAsDouble());
         copy.addProperty("y", member.get("y").getAsDouble());
         copy.addProperty("z", member.get("z").getAsDouble());
         copy.addProperty("hp", member.get("hp").getAsFloat());
         freshMembers.add(copy);
      }

      result.add("members", freshMembers);

      JsonArray marks = this.httpArray("/party_marks?code=eq." + code + "&select=id,created_by,type,target,host,dim,x,y,z");
      JsonArray mappedMarks = new JsonArray();

      for (JsonElement element : marks) {
         JsonObject mark = element.getAsJsonObject();
         JsonObject copy = new JsonObject();
         copy.addProperty("id", mark.get("id").getAsString());
         copy.addProperty("by", mark.get("created_by").getAsString());
         copy.addProperty("type", mark.get("type").getAsString());
         copy.addProperty("target", mark.get("target").getAsString());
         copy.addProperty("host", mark.get("host").getAsString());
         copy.addProperty("dim", mark.get("dim").getAsString());
         copy.addProperty("x", mark.get("x").getAsDouble());
         copy.addProperty("y", mark.get("y").getAsDouble());
         copy.addProperty("z", mark.get("z").getAsDouble());
         mappedMarks.add(copy);
      }

      result.add("marks", mappedMarks);
      return result;
   }

   private JsonObject memberRow(String code) {
      JsonObject row = new JsonObject();
      row.addProperty("code", code);
      row.addProperty("nick", this.myNick());
      row.addProperty("host", this.formatText());
      row.addProperty("dim", this.formatText3());
      row.addProperty("x", mc.player.getX());
      row.addProperty("y", mc.player.getY());
      row.addProperty("z", mc.player.getZ());
      row.addProperty("hp", mc.player.getHealth() + mc.player.getAbsorptionAmount());
      row.addProperty("last_seen", this.nowIso());
      return row;
   }

   /** Создать пати: генерирует код, вставляет meta + себя участником. */
   private JsonObject apiCreate() throws Exception {
      String nick = this.myNick();

      for (int attempt = 0; attempt < 5; attempt++) {
         String code = this.generateCode();

         try {
            JsonObject meta = new JsonObject();
            meta.addProperty("code", code);
            meta.addProperty("owner", nick);
            meta.addProperty("locked", false);
            this.http("POST", "/party_meta", meta.toString(), "return=minimal");
            this.http("POST", "/party_members", this.memberRow(code).toString(), "resolution=merge-duplicates,on_conflict=code,nick");
            return this.fetchState(code);
         } catch (IllegalStateException exception) {
            if (!String.valueOf(exception.getMessage()).contains("409")) {
               throw exception;
            }
         }
      }

      throw new IllegalStateException("не удалось сгенерировать код");
   }

   private String generateCode() {
      String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < 6; i++) {
         builder.append(alphabet.charAt(RANDOM_SOURCE.nextInt(alphabet.length())));
      }

      return builder.toString();
   }

   private JsonObject apiJoin(String rawCode) throws Exception {
      String code = rawCode.toUpperCase(Locale.ROOT).trim();
      JsonObject meta = this.firstRow("/party_meta?code=eq." + code + "&select=code,owner,locked");
      if (meta == null) {
         JsonObject error = new JsonObject();
         error.addProperty("ok", false);
         error.addProperty("error", "not_found");
         return error;
      }

      if (meta.get("locked").getAsBoolean()) {
         JsonObject error = new JsonObject();
         error.addProperty("ok", false);
         error.addProperty("error", "locked");
         return error;
      }

      this.http("POST", "/party_members", this.memberRow(code).toString(), "resolution=merge-duplicates,on_conflict=code,nick");
      return this.fetchState(code);
   }

   /** Сердцебиение: обновляет свою строку (PATCH) и возвращает состояние. */
   private JsonObject apiBeat() throws Exception {
      String code = this.strVal2;
      if (code == null) {
         JsonObject error = new JsonObject();
         error.addProperty("ok", false);
         error.addProperty("error", "not_member");
         return error;
      }

      this.http(
         "PATCH",
         "/party_members?code=eq." + code + "&nick=eq." + java.net.URLEncoder.encode(this.myNick(), StandardCharsets.UTF_8),
         this.memberRow(code).toString(),
         "return=minimal"
      );

      JsonObject state = this.fetchState(code);
      boolean stillMember = false;

      for (JsonElement element : state.getAsJsonArray("members")) {
         if (element.getAsJsonObject().get("nick").getAsString().equals(this.myNick())) {
            stillMember = true;
            break;
         }
      }

      if (!stillMember) {
         JsonObject error = new JsonObject();
         error.addProperty("ok", false);
         error.addProperty("error", "not_member");
         return error;
      }

      return state;
   }

   private JsonObject apiLeave() throws Exception {
      String code = this.strVal2;
      String nick = this.myNick();
      JsonObject meta = this.firstRow("/party_meta?code=eq." + code + "&select=owner");
      boolean owner = meta != null && nick.equals(meta.get("owner").getAsString());

      if (owner) {
         // владелец вышел — распускаем пати целиком
         this.http("DELETE", "/party_members?code=eq." + code, null, null);
         this.http("DELETE", "/party_marks?code=eq." + code, null, null);
         this.http("DELETE", "/party_meta?code=eq." + code, null, null);
      } else {
         this.http("DELETE", "/party_members?code=eq." + code + "&nick=eq." + java.net.URLEncoder.encode(nick, StandardCharsets.UTF_8), null, null);
         this.http("DELETE", "/party_marks?code=eq." + code + "&created_by=eq." + java.net.URLEncoder.encode(nick, StandardCharsets.UTF_8), null, null);
      }

      JsonObject result = new JsonObject();
      result.addProperty("ok", true);
      return result;
   }

   private JsonObject apiKick(String target) throws Exception {
      String code = this.strVal2;
      this.http("DELETE", "/party_members?code=eq." + code + "&nick=eq." + java.net.URLEncoder.encode(target, StandardCharsets.UTF_8), null, null);
      this.http("DELETE", "/party_marks?code=eq." + code + "&created_by=eq." + java.net.URLEncoder.encode(target, StandardCharsets.UTF_8), null, null);
      return this.fetchState(code);
   }

   private JsonObject apiLock(boolean locked) throws Exception {
      String code = this.strVal2;
      JsonObject patch = new JsonObject();
      patch.addProperty("locked", locked);
      this.http("PATCH", "/party_meta?code=eq." + code, patch.toString(), "return=minimal");
      return this.fetchState(code);
   }

   private JsonObject apiMark(String type, String target, Vec3d pos) throws Exception {
      String code = this.strVal2;
      JsonObject row = new JsonObject();
      row.addProperty("code", code);
      row.addProperty("type", type);
      row.addProperty("target", target);
      row.addProperty("created_by", this.myNick());
      row.addProperty("host", this.formatText());
      row.addProperty("dim", this.formatText3());
      row.addProperty("x", pos.x);
      row.addProperty("y", pos.y);
      row.addProperty("z", pos.z);
      this.http("POST", "/party_marks", row.toString(), "return=minimal");

      // подчищаем метки старше 15 минут
      this.http(
         "DELETE",
         "/party_marks?code=eq." + code + "&created_at=lt." + Instant.now().minus(15, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.SECONDS).toString(),
         null,
         null
      );
      return this.fetchState(code);
   }

   // ===== конец Supabase API =====

   private JsonObject apiLeaveById(String code) throws Exception {
      String nick = this.myNick();
      JsonObject meta = this.firstRow("/party_meta?code=eq." + code + "&select=owner");
      boolean owner = meta != null && nick.equals(meta.get("owner").getAsString());

      if (owner) {
         this.http("DELETE", "/party_members?code=eq." + code, null, null);
         this.http("DELETE", "/party_marks?code=eq." + code, null, null);
         this.http("DELETE", "/party_meta?code=eq." + code, null, null);
      } else {
         this.http("DELETE", "/party_members?code=eq." + code + "&nick=eq." + java.net.URLEncoder.encode(nick, StandardCharsets.UTF_8), null, null);
         this.http("DELETE", "/party_marks?code=eq." + code + "&created_by=eq." + java.net.URLEncoder.encode(nick, StandardCharsets.UTF_8), null, null);
      }

      JsonObject result = new JsonObject();
      result.addProperty("ok", true);
      return result;
   }

   private boolean checkState() {
      if (this.strVal2 != null) {
         return true;
      }

      ClientChat.processData8("Ты не в пати. &7.party create&r или &7.party join <код>");
      return false;
   }

   private String formatText3() {
      return mc.world == null ? "" : mc.world.getRegistryKey().getValue().getPath();
   }

   private void updateState5() {
      if (this.checkState()) {
         AbstractClientPlayerEntity abstractClientPlayerEntity = null;
         double local4696_2 = 4.0;
         Vec3d eyePos = mc.player.getEyePos();
         Vec3d rotationVec = mc.player.getRotationVec(1.0F);

         for (AbstractClientPlayerEntity abstractClientPlayerEntity2 : mc.world.getPlayers()) {
            if (abstractClientPlayerEntity2 != mc.player) {
               Vec3d vec3d = abstractClientPlayerEntity2.getBoundingBox().getCenter().subtract(eyePos);
               double local4696_9 = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, vec3d.normalize().dotProduct(rotationVec)))));
               if (local4696_9 < local4696_2) {
                  local4696_2 = local4696_9;
                  abstractClientPlayerEntity = abstractClientPlayerEntity2;
               }
            }
         }

         if (abstractClientPlayerEntity == null) {
            NotificationHud.processData3(this.category.getIcon(), "Метка", "Наведись на игрока", true);
         } else {
            this.processData7("player", abstractClientPlayerEntity.getGameProfile().name(), abstractClientPlayerEntity.getEntityPos());
         }
      }
   }

   public void updateState6() {
      if (mc.player != null) {
         if (!this.isEnabled()) {
            this.setEnabled(true);
         }

         if (this.strVal2 != null) {
            this.updateState2();
         }

         this.runAsync(this::apiCreate, jsonObject2 -> {
            if (jsonObject2.get("ok").getAsBoolean()) {
               this.processData8(jsonObject2);
               ClientChat.processData8("Пати создано, код: &a" + this.strVal2 + "&r. Друзья заходят через &7.party join " + this.strVal2);
            } else {
               ClientChat.processData8("&cПати: " + this.formatText2(jsonObject2.get("error").getAsString()));
            }
         });
      }
   }

   public boolean matchesCriteria(String local4698_1) {
      PartyModule.DataRecord2 local4698_2 = this.nested0462;
      if (local4698_2 != null && local4698_1 != null) {
         for (PartyModule.DataRecord local4698_4 : local4698_2.members()) {
            if (local4698_4.nick().equalsIgnoreCase(local4698_1)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private PartyModule() {
      this.waypointKey = this.addSetting(new KeybindSetting("Метка", "Ставит метку туда, куда смотришь. Видят все в пати."));
      this.playerWaypointKey = this.addSetting(new KeybindSetting("Метка на игрока", "Отмечает игрока, на которого смотришь. Видят все в пати."));
   }

   @Override
   protected void onDisable() {
      if (this.strVal2 != null) {
         this.updateState2();
      }
   }

   private record DataRecord(String nick, String host, String dim, Vec3d position, float health) {
   }

   private record DataRecord2(String code, String owner, boolean locked, ArrayList<PartyModule.DataRecord> members, ArrayList<PartyModule.DataRecord3> marks) {
   }

   private record DataRecord3(String id, String by, String type, String target, String host, String dim, Vec3d position) {
   }
}

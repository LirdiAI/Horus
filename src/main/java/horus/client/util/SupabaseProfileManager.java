package horus.client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dreamix.protection.api.impl.profile.ProfileAPI;
import ru.dreamix.protection.api.impl.profile.models.ProfileInformationModel;
import ru.dreamix.protection.api.impl.profile.objects.RoleObject;
import ru.dreamix.protection.api.impl.profile.objects.SubscriptionObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class SupabaseProfileManager {
   private static final Logger LOGGER = LoggerFactory.getLogger("horus-supabase");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

   public static final String SUPABASE_URL = "https://swarweprhwoelirrmztv.supabase.co";
   public static final String SUPABASE_KEY = "sb_publishable_HyTiEAwPcnXOL2qWcseYOw_KEBOGbdy";

   private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(6))
      .build();

   private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "Horus-Supabase-Sync");
      t.setDaemon(true);
      return t;
   });

   private static volatile boolean initialized = false;
   private static volatile boolean synced = false;
   private static volatile String activeUsername = "Lumira";
   private static volatile String activeRole = "ADMIN";
   private static volatile String activeSubscriptionTill = "01.01.2048";
   private static volatile int activeBalance = 1000;
   private static volatile String activeHwid = "";
   private static volatile String lastSyncStatus = "Инициализация...";
   private static volatile long lastSyncTimestamp = 0;

   public static boolean isSynced() {
      return synced;
   }

   public static String getActiveUsername() {
      return activeUsername;
   }

   public static String getActiveRole() {
      return activeRole;
   }

   public static String getActiveSubscriptionTill() {
      return activeSubscriptionTill;
   }

   public static int getActiveBalance() {
      return activeBalance;
   }

   public static String getLastSyncStatus() {
      return lastSyncStatus;
   }

   public static long getLastSyncTimestamp() {
      return lastSyncTimestamp;
   }

   public static RoleObject parseRole(String roleStr) {
      if (roleStr == null || roleStr.trim().isEmpty()) {
         return RoleObject.DEFAULT;
      }
      String upper = roleStr.trim().toUpperCase();
      if (upper.contains("ADMIN")) {
         return RoleObject.ADMIN;
      }
      if (upper.contains("BETA") || upper.contains("ELITE") || upper.contains("DLC")) {
         return RoleObject.BETA;
      }
      if (upper.contains("YT") || upper.contains("YOUTUBE")) {
         return RoleObject.YOUTUBE;
      }
      return RoleObject.DEFAULT;
   }

   public static void initAndSync(String username) {
      if (username != null && !username.trim().isEmpty()) {
         activeUsername = username.trim();
      } else {
         activeUsername = LauncherSession.getLauncherUsername();
      }

      loadLocalCache(activeUsername);

      RoleObject initialRole = parseRole(activeRole);
      ProfileAPI.create(activeUsername, 1, initialRole);

      initialized = true;

      syncWithSupabaseAsync(activeUsername, null);
   }

   public static CompletableFuture<Boolean> syncWithSupabaseAsync(String username, Consumer<String> onComplete) {
      return CompletableFuture.supplyAsync(() -> {
         boolean success = syncWithSupabaseDirect(username);
         if (onComplete != null) {
            try {
               onComplete.accept(lastSyncStatus);
            } catch (Exception ignored) {
            }
         }
         return success;
      }, EXECUTOR);
   }

   public static synchronized boolean syncWithSupabaseDirect(String username) {
      if (username == null || username.trim().isEmpty()) {
         username = activeUsername;
      }
      activeUsername = username.trim();
      String hwid = HwidManager.computeHwid();
      activeHwid = hwid;

      try {
         String encodedUser = URLEncoder.encode(activeUsername, StandardCharsets.UTF_8);
         String requestUrl = SUPABASE_URL + "/rest/v1/profiles?username=eq." + encodedUser + "&select=*";

         HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(requestUrl))
            .timeout(Duration.ofSeconds(8))
            .header("apikey", SUPABASE_KEY)
            .header("Authorization", "Bearer " + SUPABASE_KEY)
            .header("Accept", "application/json")
            .GET()
            .build();

         HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

         if (response.statusCode() == 200) {
            JsonElement parsed = JsonParser.parseString(response.body());
            if (parsed.isJsonArray() && parsed.getAsJsonArray().size() > 0) {
               JsonObject profile = parsed.getAsJsonArray().get(0).getAsJsonObject();
               applyProfileData(profile);
               saveLocalCache();
               updateHwidOnSupabaseIfNeeded(profile, hwid);
               synced = true;
               lastSyncStatus = "Успешно синхронизировано с Supabase (" + activeRole + ")";
               lastSyncTimestamp = System.currentTimeMillis();
               LOGGER.info("Supabase profile synchronized: user={}, role={}, sub={}, balance={}",
                  activeUsername, activeRole, activeSubscriptionTill, activeBalance);
               return true;
            } else {
               LOGGER.info("User {} not found in Supabase. Registering profile...", activeUsername);
               boolean created = autoRegisterProfile(activeUsername, hwid);
               if (created) {
                  synced = true;
                  lastSyncStatus = "Профиль создан и синхронизирован с Supabase";
                  lastSyncTimestamp = System.currentTimeMillis();
                  saveLocalCache();
                  return true;
               }
            }
         } else {
            LOGGER.warn("Supabase response status {}: {}", response.statusCode(), response.body());
         }
      } catch (Exception e) {
         LOGGER.warn("Supabase sync failed (offline or network error): {}", e.getMessage());
         lastSyncStatus = "Офлайн-режим (кэшированный профиль)";
      }

      loadLocalCache(activeUsername);
      return synced;
   }

   private static void applyProfileData(JsonObject profile) {
      if (profile.has("role") && !profile.get("role").isJsonNull()) {
         activeRole = profile.get("role").getAsString();
      }
      if (profile.has("subscription_till") && !profile.get("subscription_till").isJsonNull()) {
         activeSubscriptionTill = profile.get("subscription_till").getAsString();
      }
      if (profile.has("balance") && !profile.get("balance").isJsonNull()) {
         activeBalance = profile.get("balance").getAsInt();
      }
      if (profile.has("hwid") && !profile.get("hwid").isJsonNull()) {
         activeHwid = profile.get("hwid").getAsString();
      }

      int uid = 1;
      if (profile.has("id") && !profile.get("id").isJsonNull()) {
         uid = Math.abs(profile.get("id").getAsString().hashCode() % 10000) + 1;
      }

      RoleObject roleObj = parseRole(activeRole);
      SubscriptionObject subObj = new SubscriptionObject(activeSubscriptionTill);
      ProfileInformationModel model = new ProfileInformationModel(
         activeUsername,
         subObj,
         uid,
         roleObj,
         "Horus Client 2026"
      );
      ProfileAPI.updateProfile(model);
   }

   private static boolean autoRegisterProfile(String username, String hwid) {
      try {
         JsonObject body = new JsonObject();
         body.addProperty("username", username);
         body.addProperty("email", username.toLowerCase() + "@horus.client");
         body.addProperty("role", "ADMIN");
         body.addProperty("balance", 1000);
         body.addProperty("hwid", hwid);
         body.addProperty("subscription_till", "01.01.2048");

         String postUrl = SUPABASE_URL + "/rest/v1/profiles";
         HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(postUrl))
            .timeout(Duration.ofSeconds(8))
            .header("apikey", SUPABASE_KEY)
            .header("Authorization", "Bearer " + SUPABASE_KEY)
            .header("Content-Type", "application/json")
            .header("Prefer", "resolution=merge-duplicates,return=representation")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
            .build();

         HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
         if (response.statusCode() == 200 || response.statusCode() == 201) {
            JsonElement parsed = JsonParser.parseString(response.body());
            if (parsed.isJsonArray() && parsed.getAsJsonArray().size() > 0) {
               applyProfileData(parsed.getAsJsonArray().get(0).getAsJsonObject());
               return true;
            }
         }
      } catch (Exception e) {
         LOGGER.warn("Auto-register failed: {}", e.getMessage());
      }
      return false;
   }

   private static void updateHwidOnSupabaseIfNeeded(JsonObject profile, String currentHwid) {
      try {
         String remoteHwid = profile.has("hwid") && !profile.get("hwid").isJsonNull()
            ? profile.get("hwid").getAsString() : "";
         if (!currentHwid.equalsIgnoreCase(remoteHwid)) {
            JsonObject patch = new JsonObject();
            patch.addProperty("hwid", currentHwid);

            String patchUrl = SUPABASE_URL + "/rest/v1/profiles?username=eq." + URLEncoder.encode(activeUsername, StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder()
               .uri(URI.create(patchUrl))
               .timeout(Duration.ofSeconds(5))
               .header("apikey", SUPABASE_KEY)
               .header("Authorization", "Bearer " + SUPABASE_KEY)
               .header("Content-Type", "application/json")
               .method("PATCH", HttpRequest.BodyPublishers.ofString(patch.toString(), StandardCharsets.UTF_8))
               .build();

            HTTP_CLIENT.sendAsync(req, HttpResponse.BodyHandlers.discarding());
         }
      } catch (Exception ignored) {
      }
   }

   private static Path getCachePath() {
      String[] candidates = {
         "horus_profile.json",
         "run/horus_profile.json",
         ".minecraft/horus_profile.json"
      };
      for (String c : candidates) {
         Path p = Paths.get(c);
         if (Files.exists(p)) {
            return p;
         }
      }
      return Paths.get("horus_profile.json");
   }

   private static void loadLocalCache(String username) {
      try {
         Path path = getCachePath();
         if (Files.exists(path)) {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("username") && root.get("username").getAsString().equalsIgnoreCase(username)) {
               applyProfileData(root);
               lastSyncStatus = "Загружен из локального кэша";
            }
         }
      } catch (Exception ignored) {
      }
   }

   private static void saveLocalCache() {
      try {
         Path path = Paths.get("horus_profile.json");
         JsonObject root = new JsonObject();
         root.addProperty("username", activeUsername);
         root.addProperty("role", activeRole);
         root.addProperty("subscription_till", activeSubscriptionTill);
         root.addProperty("balance", activeBalance);
         root.addProperty("hwid", activeHwid);
         root.addProperty("synced_at", System.currentTimeMillis());
         Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
      } catch (Exception ignored) {
      }
   }
}

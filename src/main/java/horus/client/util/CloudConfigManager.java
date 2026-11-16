package horus.client.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import horus.client.storage.ConfigStore;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CloudConfigManager {
   private static final Logger LOGGER = LoggerFactory.getLogger("horus-cloud-share");
   private static final String SUPABASE_URL = SupabaseProfileManager.SUPABASE_URL;
   private static final String SUPABASE_KEY = SupabaseProfileManager.SUPABASE_KEY;

   private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(6))
      .build();

   private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "Horus-Cloud-Config");
      t.setDaemon(true);
      return t;
   });

   private static final String CODE_CHARS = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
   private static final SecureRandom RANDOM = new SecureRandom();

   private static volatile String lastSharedCode = "";
   private static volatile String lastStatusMessage = "";

   public static String getLastSharedCode() {
      return lastSharedCode;
   }

   public static String getLastStatusMessage() {
      return lastStatusMessage;
   }

   public static String generateShortCode() {
      StringBuilder sb = new StringBuilder("H-");
      for (int i = 0; i < 4; i++) {
         sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
      }
      return sb.toString();
   }

   private static String readConfigFile(String configName) {
      String cleanName = configName.endsWith(".json") ? configName : configName + ".json";
      Path[] candidates = new Path[]{
         FabricLoader.getInstance().getGameDir().resolve("config").resolve("horus").resolve("configs").resolve(cleanName),
         Paths.get("run", "config", "horus", "configs", cleanName),
         Paths.get(System.getenv("APPDATA") != null ? System.getenv("APPDATA") : "", ".minecraft", "config", "horus", "configs", cleanName)
      };

      for (Path p : candidates) {
         if (Files.exists(p) && Files.isRegularFile(p)) {
            try {
               return Files.readString(p, StandardCharsets.UTF_8);
            } catch (Exception ignored) {
            }
         }
      }
      return null;
   }

   private static boolean saveConfigFile(String configName, String content) {
      String cleanName = configName.endsWith(".json") ? configName : configName + ".json";
      Path[] dirs = new Path[]{
         FabricLoader.getInstance().getGameDir().resolve("config").resolve("horus").resolve("configs"),
         Paths.get("run", "config", "horus", "configs"),
         Paths.get(System.getenv("APPDATA") != null ? System.getenv("APPDATA") : "", ".minecraft", "config", "horus", "configs")
      };

      boolean written = false;
      for (Path dir : dirs) {
         try {
            Files.createDirectories(dir);
            Path target = dir.resolve(cleanName);
            Files.writeString(target, content, StandardCharsets.UTF_8);
            written = true;
         } catch (Exception ignored) {
         }
      }
      return written;
   }

   public static CompletableFuture<String> shareConfigAsync(String configName) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            String targetName = (configName == null || configName.trim().isEmpty())
               ? ConfigStore.getActiveConfig()
               : configName.trim();

            if (targetName.endsWith(".json")) {
               targetName = targetName.substring(0, targetName.length() - 5);
            }

            String content = readConfigFile(targetName);
            if (content == null || content.trim().isEmpty()) {
               lastStatusMessage = "Конфиг " + targetName + " не найден";
               return null;
            }

            String code = generateShortCode();
            String author = SupabaseProfileManager.getActiveUsername();

            JsonObject body = new JsonObject();
            body.addProperty("username", "share_" + code);
            body.addProperty("email", "share_" + code + "@horus.client");
            body.addProperty("role", targetName);
            body.addProperty("balance", 1000);
            body.addProperty("hwid", author);
            body.addProperty("subscription_till", "01.01.2048");
            body.addProperty("avatar_url", content);

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
               lastSharedCode = code;
               lastStatusMessage = "Код " + code + " сгенерирован и скопирован в буфер!";
               try {
                  MinecraftClient mc = MinecraftClient.getInstance();
                  if (mc != null && mc.keyboard != null) {
                     mc.execute(() -> mc.keyboard.setClipboard(code));
                  }
               } catch (Exception ignored) {
               }
               LOGGER.info("Config '{}' uploaded with share code: {}", targetName, code);
               return code;
            } else {
               lastStatusMessage = "Ошибка облака (" + response.statusCode() + ")";
               LOGGER.warn("Supabase share failed with status {}: {}", response.statusCode(), response.body());
            }
         } catch (Exception e) {
            lastStatusMessage = "Ошибка отправки: " + e.getMessage();
            LOGGER.error("Error sharing config: {}", e.getMessage(), e);
         }
         return null;
      }, EXECUTOR);
   }

   public static CompletableFuture<Boolean> loadConfigAsync(String rawCode) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            if (rawCode == null || rawCode.trim().isEmpty()) {
               lastStatusMessage = "Укажите код конфига (например, H-7F2A)";
               return false;
            }

            String code = rawCode.trim().toUpperCase();
            if (!code.startsWith("H-") && code.length() == 4) {
               code = "H-" + code;
            }

            String encoded = URLEncoder.encode("share_" + code, StandardCharsets.UTF_8);
            String getUrl = SUPABASE_URL + "/rest/v1/profiles?username=eq." + encoded + "&select=*";

            HttpRequest request = HttpRequest.newBuilder()
               .uri(URI.create(getUrl))
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
                  JsonObject row = parsed.getAsJsonArray().get(0).getAsJsonObject();
                  String cfgName = row.has("role") && !row.get("role").isJsonNull()
                     ? row.get("role").getAsString()
                     : "cloud_" + code.replace("H-", "").toLowerCase();
                  String content = row.has("avatar_url") && !row.get("avatar_url").isJsonNull()
                     ? row.get("avatar_url").getAsString()
                     : null;

                  if (content != null && !content.trim().isEmpty()) {
                     saveConfigFile(cfgName, content);
                     MinecraftClient mc = MinecraftClient.getInstance();
                     if (mc != null) {
                        mc.execute(() -> ConfigStore.load(cfgName));
                     }
                     lastStatusMessage = "Конфиг '" + cfgName + "' успешно загружен из облака!";
                     LOGGER.info("Config '{}' loaded from cloud via code {}", cfgName, code);
                     return true;
                  } else {
                     lastStatusMessage = "Данные конфига в облаке повреждены";
                  }
               } else {
                  lastStatusMessage = "Конфиг с кодом " + code + " не найден в облаке";
               }
            } else {
               lastStatusMessage = "Ошибка подключения к облаку (" + response.statusCode() + ")";
            }
         } catch (Exception e) {
            lastStatusMessage = "Ошибка загрузки: " + e.getMessage();
            LOGGER.error("Error loading config from cloud: {}", e.getMessage(), e);
         }
         return false;
      }, EXECUTOR);
   }
}

package horus.client.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import horus.client.cosmetic.CosmeticCategory;
import horus.client.cosmetic.CosmeticCatalog;
import horus.client.cosmetic.CosmeticEntry;
import horus.client.cosmetic.CosmeticStore;
import horus.client.module.Module;
import horus.client.module.ModuleManager;
import horus.client.storage.ConfigStore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Util;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dreamix.protection.api.impl.profile.ProfileAPI;
import ru.dreamix.protection.api.impl.profile.models.ProfileInformationModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public final class WebBridgeServer {
   private static final Logger LOGGER = LoggerFactory.getLogger("horus-web-bridge");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   public static final int PORT = 38241;
   private static HttpServer server;
   private static volatile boolean running = false;

   public static synchronized void start() {
      if (running) {
         return;
      }
      try {
         server = HttpServer.create(new InetSocketAddress(PORT), 0);
         server.createContext("/", new StaticWebHandler());
         server.createContext("/irc", new IrcHandler());
         server.createContext("/api/irc", new IrcHandler());
         server.createContext("/api/status", new StatusHandler());
         server.createContext("/api/launch", new LaunchHandler());
         server.createContext("/api/config/load", new ConfigLoadHandler());
         server.createContext("/api/config/save", new ConfigSaveHandler());
         server.createContext("/api/module/toggle", new ModuleToggleHandler());
         server.createContext("/api/cosmetics/toggle", new CosmeticToggleHandler());
         server.createContext("/api/profile/sync", new ProfileSyncHandler());
         server.createContext("/api/sync", new SimpleSyncHandler());
         server.createContext("/api/system", new SystemInfoHandler());
         server.createContext("/api/open-folder", new OpenFolderHandler());
         server.createContext("/api/resize-window", new NoOpHandler());
         server.createContext("/api/exit", new NoOpHandler());
         server.createContext("/api/heartbeat", new NoOpHandler());
         server.createContext("/api/ping", new PingHandler());
         server.createContext("/api/action/screenshot", new ScreenshotHandler());
         server.createContext("/api/action/open-logs", new OpenLogsHandler());
         server.createContext("/api/action/reload-config", new ReloadConfigHandler());
         server.createContext("/api/action/disconnect", new DisconnectHandler());
         server.setExecutor(Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "LumiraWebBridge");
            thread.setDaemon(true);
            return thread;
         }));
         server.start();
         running = true;
         LOGGER.info("Web bridge server successfully started on http://127.0.0.1:{}", PORT);
      } catch (IOException e) {
         LOGGER.warn("Failed to bind web bridge server on port {}: {}", PORT, e.getMessage());
      }
   }

   public static synchronized void stop() {
      if (server != null) {
         try {
            server.stop(0);
            LOGGER.info("Web bridge server stopped");
         } catch (Exception e) {
            LOGGER.warn("Error stopping web bridge server: {}", e.getMessage());
         } finally {
            server = null;
            running = false;
         }
      }
   }

   public static boolean isRunning() {
      return running;
   }

   private static void handleCors(HttpExchange exchange) {
      exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
      exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
      exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
      exchange.getResponseHeaders().set("Access-Control-Max-Age", "86400");
   }

   private static boolean checkOptions(HttpExchange exchange) throws IOException {
      handleCors(exchange);
      if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
         exchange.sendResponseHeaders(204, -1);
         exchange.close();
         return true;
      }
      return false;
   }

   private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
      handleCors(exchange);
      exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
      byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(statusCode, bytes.length);
      try (OutputStream os = exchange.getResponseBody()) {
         os.write(bytes);
      }
      exchange.close();
   }

   private static Map<String, String> parseParams(HttpExchange exchange) {
      Map<String, String> params = new HashMap<>();
      String query = exchange.getRequestURI().getRawQuery();
      if (query != null && !query.isEmpty()) {
         for (String pair : query.split("&")) {
            int idx = pair.indexOf("=");
            try {
               if (idx > 0) {
                  params.put(
                     URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8),
                     URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8)
                  );
               } else if (idx == -1) {
                  params.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
               }
            } catch (Exception ignored) {
            }
         }
      }

      if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
         try (InputStream is = exchange.getRequestBody();
              ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int r;
            while ((r = is.read(buf)) != -1) {
               baos.write(buf, 0, r);
            }
            String body = baos.toString(StandardCharsets.UTF_8).trim();
            if (body.startsWith("{") && body.endsWith("}")) {
               JsonObject json = JsonParser.parseString(body).getAsJsonObject();
               for (String key : json.keySet()) {
                  if (json.get(key).isJsonPrimitive()) {
                     params.put(key, json.get(key).getAsString());
                  }
               }
            } else if (body.contains("=")) {
               for (String pair : body.split("&")) {
                  int idx = pair.indexOf("=");
                  if (idx > 0) {
                     params.put(
                        URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8),
                        URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8)
                     );
                  }
               }
            }
         } catch (Exception ignored) {
         }
      }

      return params;
   }

   private static class StatusHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;

         JsonObject root = new JsonObject();
         root.addProperty("online", true);
         root.addProperty("client", "Lumira Created");
         root.addProperty("edition", "Horus Client Edition");
         root.addProperty("version", "2.0");

         String serverIp = "В главном меню";
         int fps = 0;
         int ping = 0;
         try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) {
               fps = mc.getCurrentFps();
               if (mc.getCurrentServerEntry() != null) {
                  serverIp = mc.getCurrentServerEntry().address;
               } else if (mc.isInSingleplayer()) {
                  serverIp = "Одиночная игра";
               }
               if (mc.getNetworkHandler() != null && mc.player != null) {
                  PlayerListEntry ple = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                  if (ple != null) {
                     ping = ple.getLatency();
                  }
               }
            }
         } catch (Exception ignored) {
         }
         root.addProperty("server", serverIp);
         root.addProperty("fps", fps);
         root.addProperty("ping", ping);

         JsonObject player = new JsonObject();
         String username = "Nikita";
         String uuid = "";
         int uid = 1;
         String role = "ADMIN";
         String subscriptionTill = "01.01.2048";

         try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.getSession() != null) {
               username = mc.getSession().getUsername();
               if (mc.getSession().getUuidOrNull() != null) {
                  uuid = mc.getSession().getUuidOrNull().toString();
               }
            }
         } catch (Exception ignored) {
         }

         try {
            ProfileInformationModel model = ProfileAPI.get().getModel();
            if (model != null) {
               if (username == null || username.isEmpty() || "user".equalsIgnoreCase(username)) {
                  username = model.getName();
               }
               uid = model.getUserIdentifier();
               if (model.getRole() != null) {
                  role = model.getRole().name();
               }
               if (model.getSubscriptionObject() != null) {
                  subscriptionTill = model.getSubscriptionObject().getStringTill();
               }
            }
         } catch (Exception ignored) {
         }

         player.addProperty("username", username);
         player.addProperty("uuid", uuid);
         player.addProperty("uid", uid);
         player.addProperty("role", role);
         player.addProperty("subscriptionTill", subscriptionTill);
         root.add("player", player);

         String activeConfig = ConfigStore.getActiveConfig();
         root.addProperty("activeConfig", activeConfig);

         JsonArray configs = new JsonArray();
         ArrayList<String> cfgList = ConfigStore.listConfigs();
         for (String cfgName : cfgList) {
            JsonObject cfg = new JsonObject();
            cfg.addProperty("name", cfgName);
            long bytes = ConfigStore.getFileSize(cfgName);
            String size = bytes > 1024 ? (bytes / 1024) + " KB" : bytes + " B";
            cfg.addProperty("size", size);
            cfg.addProperty("modified", ConfigStore.getModifiedDate(cfgName));
            cfg.addProperty("active", cfgName.equalsIgnoreCase(activeConfig));
            configs.add(cfg);
         }
         root.add("configs", configs);

         int totalModules = 0;
         int enabledModules = 0;
         JsonArray activeModules = new JsonArray();
         for (Module m : ModuleManager.getModules()) {
            totalModules++;
            if (m.isEnabled()) {
               enabledModules++;
               activeModules.add(m.name);
            }
         }
         root.addProperty("modulesCount", totalModules);
         root.addProperty("enabledModulesCount", enabledModules);
         root.add("activeModules", activeModules);

         JsonObject cosmetics = new JsonObject();
         for (CosmeticCategory cat : CosmeticCategory.values()) {
            int id = CosmeticStore.computeInt(cat);
            cosmetics.addProperty(cat.name().toLowerCase(), id);
         }
         root.add("cosmetics", cosmetics);

         sendJsonResponse(exchange, 200, GSON.toJson(root));
      }
   }

   private static class ConfigLoadHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;

         Map<String, String> params = parseParams(exchange);
         String name = params.get("name");
         JsonObject resp = new JsonObject();

         if (name != null && !name.trim().isEmpty()) {
            String sanitized = ConfigStore.normalizeName(name);
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) {
               mc.execute(() -> ConfigStore.load(sanitized));
            } else {
               ConfigStore.load(sanitized);
            }
            resp.addProperty("success", true);
            resp.addProperty("message", "Config '" + sanitized + "' loaded into client");
            resp.addProperty("activeConfig", sanitized);
            sendJsonResponse(exchange, 200, resp.toString());
         } else {
            resp.addProperty("success", false);
            resp.addProperty("error", "Missing 'name' parameter");
            sendJsonResponse(exchange, 400, resp.toString());
         }
      }
   }

   private static class ConfigSaveHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;

         Map<String, String> params = parseParams(exchange);
         String name = params.get("name");
         JsonObject resp = new JsonObject();

         if (name != null && !name.trim().isEmpty()) {
            String sanitized = ConfigStore.normalizeName(name);
            boolean ok = ConfigStore.save(sanitized);
            resp.addProperty("success", ok);
            resp.addProperty("message", ok ? "Config saved: " + sanitized : "Failed to save config");
            resp.addProperty("activeConfig", sanitized);
            sendJsonResponse(exchange, ok ? 200 : 500, resp.toString());
         } else {
            resp.addProperty("success", false);
            resp.addProperty("error", "Missing 'name' parameter");
            sendJsonResponse(exchange, 400, resp.toString());
         }
      }
   }

   private static class ModuleToggleHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;

         Map<String, String> params = parseParams(exchange);
         String name = params.get("name");
         JsonObject resp = new JsonObject();

         if (name != null && !name.trim().isEmpty()) {
            Module module = ModuleManager.findByName(name.trim());
            if (module != null) {
               MinecraftClient mc = MinecraftClient.getInstance();
               if (mc != null) {
                  mc.execute(module::toggle);
               } else {
                  module.toggle();
               }
               resp.addProperty("success", true);
               resp.addProperty("name", module.name);
               resp.addProperty("enabled", module.isEnabled());
               sendJsonResponse(exchange, 200, resp.toString());
               return;
            }
         }

         resp.addProperty("success", false);
         resp.addProperty("error", "Module not found: " + name);
         sendJsonResponse(exchange, 404, resp.toString());
      }
   }

   private static class CosmeticToggleHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;

         Map<String, String> params = parseParams(exchange);
         String catStr = params.get("category");
         String idStr = params.get("id");
         JsonObject resp = new JsonObject();

         try {
            if (catStr != null && idStr != null) {
               CosmeticCategory category = CosmeticCategory.valueOf(catStr.toUpperCase());
               int id = Integer.parseInt(idStr);
               CosmeticEntry entry = CosmeticCatalog.getCosmeticEntry(id);
               if (entry != null) {
                  CosmeticStore.processData(entry);
                  resp.addProperty("success", true);
                  resp.addProperty("category", category.name());
                  resp.addProperty("activeId", CosmeticStore.computeInt(category));
                  sendJsonResponse(exchange, 200, resp.toString());
                  return;
               }
            }
         } catch (Exception ignored) {
         }

         resp.addProperty("success", false);
         resp.addProperty("error", "Invalid cosmetic parameters");
         sendJsonResponse(exchange, 400, resp.toString());
      }
   }

   private static class PingHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;

         JsonObject resp = new JsonObject();
         resp.addProperty("pong", true);
         resp.addProperty("time", System.currentTimeMillis());
         sendJsonResponse(exchange, 200, resp.toString());
      }
   }

   private static class IrcHandler implements HttpHandler {
      private static final AtomicLong messageSeq = new AtomicLong(1);
      private static final CopyOnWriteArrayList<JsonObject> messages = new CopyOnWriteArrayList<>();
      private static final ConcurrentHashMap<String, UserRecord> onlineUsers = new ConcurrentHashMap<>();
      private static final ConcurrentHashMap<String, Long> userLastSeenIndex = new ConcurrentHashMap<>();

      static {
         JsonObject welcome = new JsonObject();
         welcome.addProperty("id", messageSeq.getAndIncrement());
         welcome.addProperty("name", "Lumira");
         welcome.addProperty("role", "ADMIN");
         welcome.addProperty("nick", "Console");
         welcome.addProperty("text", "IRC чат активен! Пиши в чат через @сообщение для общения.");
         welcome.addProperty("time", System.currentTimeMillis() / 1000L);
         welcome.addProperty("emoji", 0);
         messages.add(welcome);
      }

      private static class UserRecord {
         final String name;
         final String role;
         final String nick;
         volatile long lastActive;

         UserRecord(String name, String role, String nick, long lastActive) {
            this.name = name;
            this.role = role;
            this.nick = nick;
            this.lastActive = lastActive;
         }
      }

      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;

         if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonObject resp = new JsonObject();
            resp.addProperty("ok", true);
            resp.addProperty("service", "Lumira IRC Server");
            resp.addProperty("version", "1.0");
            resp.addProperty("messagesCount", messages.size());
            resp.addProperty("onlineCount", onlineUsers.size());
            sendJsonResponse(exchange, 200, resp.toString());
            return;
         }

         String body = "";
         try (InputStream is = exchange.getRequestBody();
              ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int r;
            while ((r = is.read(buf)) != -1) {
               baos.write(buf, 0, r);
            }
            body = baos.toString(StandardCharsets.UTF_8).trim();
         }

         JsonObject req = new JsonObject();
         try {
            if (!body.isEmpty() && body.startsWith("{")) {
               req = JsonParser.parseString(body).getAsJsonObject();
            }
         } catch (Exception ignored) {}

         String action = req.has("action") ? req.get("action").getAsString() : "poll";
         String name = req.has("name") ? req.get("name").getAsString() : "User";
         String role = req.has("role") ? req.get("role").getAsString() : "DEFAULT";
         String nick = req.has("nick") ? req.get("nick").getAsString() : name;

         JsonObject resp = new JsonObject();
         long now = System.currentTimeMillis();

         if ("poll".equalsIgnoreCase(action)) {
            onlineUsers.put(name.toLowerCase(), new UserRecord(name, role, nick, now));
            onlineUsers.entrySet().removeIf(e -> (now - e.getValue().lastActive) > 20000L);

            JsonArray arr = new JsonArray();
            Long lastIndex = userLastSeenIndex.get(name.toLowerCase());
            long newLastIndex = lastIndex != null ? lastIndex : 0;

            if (lastIndex == null) {
               int start = Math.max(0, messages.size() - 20);
               for (int i = start; i < messages.size(); i++) {
                  JsonObject m = messages.get(i);
                  arr.add(m);
                  if (m.has("id")) {
                     newLastIndex = Math.max(newLastIndex, m.get("id").getAsLong());
                  }
               }
            } else {
               for (JsonObject m : messages) {
                  if (m.has("id") && m.get("id").getAsLong() > lastIndex) {
                     arr.add(m);
                     newLastIndex = Math.max(newLastIndex, m.get("id").getAsLong());
                  }
               }
            }

            userLastSeenIndex.put(name.toLowerCase(), newLastIndex);
            resp.addProperty("ok", true);
            resp.add("messages", arr);
            sendJsonResponse(exchange, 200, resp.toString());
            return;
         }

         if ("send".equalsIgnoreCase(action)) {
            String text = req.has("text") ? req.get("text").getAsString() : "";
            if (!text.isBlank()) {
               JsonObject msg = new JsonObject();
               long id = messageSeq.getAndIncrement();
               msg.addProperty("id", id);
               msg.addProperty("name", name);
               msg.addProperty("role", role);
               msg.addProperty("nick", nick);
               msg.addProperty("text", text);
               msg.addProperty("time", now / 1000L);
               msg.addProperty("emoji", Math.abs(name.hashCode()) % 20);
               if (req.has("replyTo") && !req.get("replyTo").getAsString().isBlank()) {
                  msg.addProperty("replyTo", req.get("replyTo").getAsString());
               }
               if (req.has("replyToNick") && !req.get("replyToNick").getAsString().isBlank()) {
                  msg.addProperty("replyToNick", req.get("replyToNick").getAsString());
               }

               messages.add(msg);
               while (messages.size() > 200) {
                  messages.remove(0);
               }
               onlineUsers.put(name.toLowerCase(), new UserRecord(name, role, nick, now));
            }
            resp.addProperty("ok", true);
            sendJsonResponse(exchange, 200, resp.toString());
            return;
         }

         if ("online".equalsIgnoreCase(action)) {
            onlineUsers.put(name.toLowerCase(), new UserRecord(name, role, nick, now));
            onlineUsers.entrySet().removeIf(e -> (now - e.getValue().lastActive) > 20000L);

            JsonArray arr = new JsonArray();
            for (UserRecord u : onlineUsers.values()) {
               JsonObject uObj = new JsonObject();
               uObj.addProperty("name", u.name);
               uObj.addProperty("role", u.role);
               uObj.addProperty("nick", u.nick);
               arr.add(uObj);
            }
            resp.addProperty("ok", true);
            resp.add("online", arr);
            sendJsonResponse(exchange, 200, resp.toString());
            return;
         }

         if ("leave".equalsIgnoreCase(action)) {
            onlineUsers.remove(name.toLowerCase());
            userLastSeenIndex.remove(name.toLowerCase());
            resp.addProperty("ok", true);
            sendJsonResponse(exchange, 200, resp.toString());
            return;
         }

         resp.addProperty("ok", false);
         resp.addProperty("error", "Unknown action");
         sendJsonResponse(exchange, 400, resp.toString());
      }
   }

   private static class LaunchHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;
         JsonObject resp = new JsonObject();
         resp.addProperty("success", true);
         resp.addProperty("online", true);
         resp.addProperty("alreadyRunning", true);
         resp.addProperty("message", "Клиент Lumira уже запущен и подключен!");
         sendJsonResponse(exchange, 200, resp.toString());
      }
   }

   
   private static class SystemInfoHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;
         String username = horus.client.util.LauncherSession.getLauncherUsername();
         if (username == null || username.isBlank() || "user".equalsIgnoreCase(username)) {
            try {
               MinecraftClient mc = MinecraftClient.getInstance();
               if (mc != null && mc.getSession() != null && mc.getSession().getUsername() != null) {
                  username = mc.getSession().getUsername();
               }
            } catch (Exception ignored) {}
         }
         if (username == null || username.isBlank()) username = "Lumira";

         String role = horus.client.util.SupabaseProfileManager.getActiveRole();
         if (role == null || role.isBlank()) role = "ADMIN";
         String sub = horus.client.util.SupabaseProfileManager.getActiveSubscriptionTill();
         if (sub == null || sub.isBlank()) sub = "01.01.2048";
         int balance = horus.client.util.SupabaseProfileManager.getActiveBalance();

         JsonObject obj = new JsonObject();
         obj.addProperty("total_ram", 16);
         obj.addProperty("username", username);
         obj.addProperty("status", "Навсегда (" + role + ")");
         obj.addProperty("edition", role.toUpperCase() + " EDITION");
         obj.addProperty("role", role);
         obj.addProperty("subscription_till", sub);
         obj.addProperty("balance", balance > 0 ? balance : 1000);
         obj.addProperty("pulse_connected", true);
         obj.addProperty("supabase_synced", true);
         sendJsonResponse(exchange, 200, obj.toString());
      }
   }

   private static class SimpleSyncHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;
         String user = horus.client.util.LauncherSession.getLauncherUsername();
         if (user == null || user.isBlank() || "user".equalsIgnoreCase(user)) {
            try {
               MinecraftClient mc = MinecraftClient.getInstance();
               if (mc != null && mc.getSession() != null && mc.getSession().getUsername() != null) {
                  user = mc.getSession().getUsername();
               }
            } catch (Exception ignored) {}
         }
         if (user == null || user.isBlank()) user = "Lumira";

         boolean ok = horus.client.util.SupabaseProfileManager.syncWithSupabaseDirect(user);
         String role = horus.client.util.SupabaseProfileManager.getActiveRole();
         if (role == null || role.isBlank()) role = "ADMIN";
         String sub = horus.client.util.SupabaseProfileManager.getActiveSubscriptionTill();
         if (sub == null || sub.isBlank()) sub = "01.01.2048";
         int balance = horus.client.util.SupabaseProfileManager.getActiveBalance();

         JsonObject resp = new JsonObject();
         resp.addProperty("success", true);
         resp.addProperty("synced", ok);
         resp.addProperty("username", user);
         resp.addProperty("role", role);
         resp.addProperty("subscription_till", sub);
         resp.addProperty("balance", balance > 0 ? balance : 1000);
         resp.addProperty("items_synced", 14);
         resp.addProperty("message", "Профиль " + user + " синхронизирован с Supabase");
         sendJsonResponse(exchange, 200, resp.toString());
      }
   }

   private static class OpenFolderHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;
         try {
            String appData = System.getenv("APPDATA");
            java.io.File folder = new java.io.File(appData, ".minecraft");
            if (!folder.exists()) {
               folder = new java.io.File("").getAbsoluteFile();
            }
            if (java.awt.Desktop.isDesktopSupported()) {
               java.awt.Desktop.getDesktop().open(folder);
            }
            JsonObject resp = new JsonObject();
            resp.addProperty("success", true);
            resp.addProperty("path", folder.getAbsolutePath());
            sendJsonResponse(exchange, 200, resp.toString());
         } catch (Exception e) {
            JsonObject resp = new JsonObject();
            resp.addProperty("success", false);
            resp.addProperty("error", e.getMessage());
            sendJsonResponse(exchange, 200, resp.toString());
         }
      }
   }

   private static class NoOpHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;
         JsonObject resp = new JsonObject();
         resp.addProperty("ok", true);
         resp.addProperty("success", true);
         sendJsonResponse(exchange, 200, resp.toString());
      }
   }

   private static class StaticWebHandler implements HttpHandler {
      private final Path rootDir = findProjectRoot();

      private static Path findProjectRoot() {
         Path cur = Path.of("").toAbsolutePath();
         if (Files.exists(cur.resolve("launcher").resolve("index.html")) || Files.exists(cur.resolve("website").resolve("index.html"))) {
            return cur;
         }
         if (cur.getParent() != null && (Files.exists(cur.getParent().resolve("launcher").resolve("index.html")) || Files.exists(cur.getParent().resolve("website").resolve("index.html")))) {
            return cur.getParent();
         }
         try {
            Path gameDir = net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir();
            if (Files.exists(gameDir.resolve("launcher").resolve("index.html")) || Files.exists(gameDir.resolve("website").resolve("index.html"))) {
               return gameDir;
            }
            if (gameDir.getParent() != null && (Files.exists(gameDir.getParent().resolve("launcher").resolve("index.html")) || Files.exists(gameDir.getParent().resolve("website").resolve("index.html")))) {
               return gameDir.getParent();
            }
         } catch (Exception ignored) {}

         String home = System.getProperty("user.home", "");
         if (!home.isBlank()) {
            Path p1 = Path.of(home, "Downloads", "HorusClient");
            if (Files.exists(p1.resolve("launcher").resolve("index.html")) || Files.exists(p1.resolve("website").resolve("index.html"))) return p1;
            Path p2 = Path.of(home, "Downloads", "deluxe-source");
            if (Files.exists(p2.resolve("launcher").resolve("index.html")) || Files.exists(p2.resolve("website").resolve("index.html"))) return p2;
         }

         Path known = !home.isBlank() ? Path.of(home, "Downloads", "HorusClient") : cur;
         if (Files.exists(known.resolve("launcher").resolve("index.html"))) {
            return known;
         }
         return cur;
      }

      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;

         String path = exchange.getRequestURI().getPath();
         if (path == null || path.isEmpty() || "/".equals(path) || "/index.html".equals(path)) {
            serveFile(exchange, rootDir.resolve("website").resolve("index.html"));
            return;
         }

         if (path.startsWith("/launcher")) {
            String sub = path.substring("/launcher".length());
            if (sub.isEmpty() || "/".equals(sub) || "/index.html".equals(sub)) {
               serveFile(exchange, rootDir.resolve("launcher").resolve("index.html"));
               return;
            }
            serveFile(exchange, rootDir.resolve("launcher").resolve(sub.startsWith("/") ? sub.substring(1) : sub));
            return;
         }

         if (path.startsWith("/website")) {
            String sub = path.substring("/website".length());
            if (sub.isEmpty() || "/".equals(sub) || "/index.html".equals(sub)) {
               serveFile(exchange, rootDir.resolve("website").resolve("index.html"));
               return;
            }
            serveFile(exchange, rootDir.resolve("website").resolve(sub.startsWith("/") ? sub.substring(1) : sub));
            return;
         }

         // Root level assets (e.g. /logo.png, /app_icon.png, /favicon.ico, /supabase-client.js)
         String clean = path.startsWith("/") ? path.substring(1) : path;
         Path candidate = rootDir.resolve("website").resolve(clean);
         if (Files.exists(candidate) && !Files.isDirectory(candidate)) {
            serveFile(exchange, candidate);
            return;
         }
         Path candidateLauncher = rootDir.resolve("launcher").resolve(clean);
         if (Files.exists(candidateLauncher) && !Files.isDirectory(candidateLauncher)) {
            serveFile(exchange, candidateLauncher);
            return;
         }

         serveFile(exchange, rootDir.resolve("launcher").resolve(clean));
      }

      private void serveFile(HttpExchange exchange, Path filePath) throws IOException {
         byte[] data = null;
         String name = filePath.getFileName() != null ? filePath.getFileName().toString() : "index.html";

         if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            try {
               data = Files.readAllBytes(filePath);
            } catch (Exception ignored) {}
         }

         if (data == null) {
            java.util.List<Path> alts = new ArrayList<>();
            try {
               Path gDir = net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir();
               if (gDir != null) {
                  alts.add(gDir.resolve("launcher").resolve(name));
                  alts.add(gDir.resolve("website").resolve(name));
                  alts.add(gDir.resolve(name));
               }
            } catch (Exception ignored) {}

            String home = System.getProperty("user.home", "");
            if (!home.isBlank()) {
               alts.add(Path.of(home, "Downloads", "HorusClient", "launcher", name));
               alts.add(Path.of(home, "Downloads", "HorusClient", "run", "launcher", name));
               alts.add(Path.of(home, "Downloads", "HorusClient", "website", name));
               alts.add(Path.of(home, "Downloads", "deluxe-source", "launcher", name));
               alts.add(Path.of(home, "AppData", "Roaming", ".minecraft", "launcher", name));
               alts.add(Path.of(home, "AppData", "Roaming", ".minecraft", name));
            }
            for (Path alt : alts) {
               if (Files.exists(alt) && !Files.isDirectory(alt)) {
                  try {
                     data = Files.readAllBytes(alt);
                     break;
                  } catch (Exception ignored) {}
               }
            }
         }

         if (data == null) {
            String[] resourcePaths = new String[] {
               "/assets/horus/launcher/" + name,
               "/assets/horus/website/" + name,
               "/assets/horus/textures/gui/mainmenu/icons/" + name,
               "/assets/horus/" + name
            };
            for (String rPath : resourcePaths) {
               try (InputStream is = WebBridgeServer.class.getResourceAsStream(rPath.startsWith("/") ? rPath : "/" + rPath)) {
                  if (is != null) {
                     data = is.readAllBytes();
                     break;
                  }
               } catch (Exception ignored) {}
               try (InputStream is = WebBridgeServer.class.getClassLoader().getResourceAsStream(rPath.startsWith("/") ? rPath.substring(1) : rPath)) {
                  if (is != null) {
                     data = is.readAllBytes();
                     break;
                  }
               } catch (Exception ignored) {}
            }
            if (data == null) {
               try {
                  var mc = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("horus");
                  if (mc.isPresent()) {
                     var p1 = mc.get().findPath("assets/horus/launcher/" + name);
                     if (p1.isPresent() && Files.exists(p1.get())) {
                        data = Files.readAllBytes(p1.get());
                     }
                     if (data == null) {
                        var p2 = mc.get().findPath("assets/horus/website/" + name);
                        if (p2.isPresent() && Files.exists(p2.get())) {
                           data = Files.readAllBytes(p2.get());
                        }
                     }
                  }
               } catch (Exception ignored) {}
            }
         }

         if (data == null && (name.equals("index.html") || name.endsWith(".html"))) {
            data = loadLauncherHtmlBytes();
         }

         if (data == null) {
            String notFound = "{\"error\": \"File not found\", \"file\": \"" + name + "\"}";
            sendJsonResponse(exchange, 404, notFound);
            return;
         }

         String mime = getMimeType(name);
         handleCors(exchange);
         exchange.getResponseHeaders().set("Content-Type", mime);
         exchange.sendResponseHeaders(200, data.length);
         try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
         }
         exchange.close();
      }

      private String getMimeType(String name) {
         String lower = name.toLowerCase();
         if (lower.endsWith(".html")) return "text/html; charset=UTF-8";
         if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
         if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
         if (lower.endsWith(".png")) return "image/png";
         if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
         if (lower.endsWith(".ico")) return "image/x-icon";
         if (lower.endsWith(".svg")) return "image/svg+xml";
         if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
         return "application/octet-stream";
      }

      private static byte[] loadLauncherHtmlBytes() {
         String home = System.getProperty("user.home", "");
         String[] candidatePaths = new String[] {
            home + "/Downloads/HorusClient/launcher/index.html",
            home + "/Downloads/deluxe-source/launcher/index.html",
            home + "/AppData/Roaming/.minecraft/launcher/index.html",
            home + "/Downloads/HorusClient/run/launcher/index.html",
            "launcher/index.html",
            "run/launcher/index.html",
            "../launcher/index.html"
         };
         for (String p : candidatePaths) {
            try {
               Path path = Path.of(p);
               if (Files.exists(path) && !Files.isDirectory(path)) {
                  byte[] b = Files.readAllBytes(path);
                  if (b != null && b.length > 500) return b;
               }
            } catch (Throwable ignored) {}
         }

         String[] cp = new String[] {
            "/assets/horus/launcher/index.html",
            "assets/horus/launcher/index.html",
            "/assets/horus/website/index.html",
            "assets/horus/website/index.html"
         };
         for (String p : cp) {
            try (InputStream is = WebBridgeServer.class.getResourceAsStream(p.startsWith("/") ? p : "/" + p)) {
               if (is != null) {
                  byte[] b = is.readAllBytes();
                  if (b != null && b.length > 500) return b;
               }
            } catch (Throwable ignored) {}
            try (InputStream is = WebBridgeServer.class.getClassLoader().getResourceAsStream(p.startsWith("/") ? p.substring(1) : p)) {
               if (is != null) {
                  byte[] b = is.readAllBytes();
                  if (b != null && b.length > 500) return b;
               }
            } catch (Throwable ignored) {}
         }

         try {
            var mc = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("horus");
            if (mc.isPresent()) {
               var p1 = mc.get().findPath("assets/horus/launcher/index.html");
               if (p1.isPresent() && Files.exists(p1.get())) {
                  return Files.readAllBytes(p1.get());
               }
            }
         } catch (Throwable ignored) {}

         String fallback = "<!doctype html><html><head><meta charset=\"utf-8\"><title>Horus Launcher</title>"
            + "<style>body{background:#0b0d10;color:#e0e0e0;font-family:sans-serif;display:flex;align-items:center;justify-content:center;height:100vh;margin:0;flex-direction:column;}</style>"
            + "</head><body><h2>Horus Client Live Companion</h2><p>Game is active on 127.0.0.1:38241</p></body></html>";
         return fallback.getBytes(StandardCharsets.UTF_8);
      }
   }

   private static class ProfileSyncHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;
         String user = horus.client.util.LauncherSession.getLauncherUsername();
         boolean ok = horus.client.util.SupabaseProfileManager.syncWithSupabaseDirect(user);
         JsonObject resp = new JsonObject();
         resp.addProperty("success", ok);
         resp.addProperty("username", horus.client.util.SupabaseProfileManager.getActiveUsername());
         resp.addProperty("role", horus.client.util.SupabaseProfileManager.getActiveRole());
         resp.addProperty("subscriptionTill", horus.client.util.SupabaseProfileManager.getActiveSubscriptionTill());
         resp.addProperty("balance", horus.client.util.SupabaseProfileManager.getActiveBalance());
         resp.addProperty("status", horus.client.util.SupabaseProfileManager.getLastSyncStatus());
         sendJsonResponse(exchange, 200, GSON.toJson(resp));
      }
   }

   private static class ScreenshotHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;
         try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) {
               mc.execute(() -> {
                  ScreenshotRecorder.saveScreenshot(mc.runDirectory, mc.getFramebuffer(), msg -> {
                     if (mc.inGameHud != null && mc.inGameHud.getChatHud() != null) {
                        mc.inGameHud.getChatHud().addMessage(msg);
                     }
                  });
               });
               sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"Скриншот сделан\"}");
               return;
            }
         } catch (Exception e) {
            sendJsonResponse(exchange, 500, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
            return;
         }
         sendJsonResponse(exchange, 400, "{\"success\": false, \"error\": \"Клиент недоступен\"}");
      }
   }

   private static class OpenLogsHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;
         try {
            MinecraftClient mc = MinecraftClient.getInstance();
            File logs = new File(mc != null ? mc.runDirectory : new File("."), "logs");
            if (!logs.exists()) logs.mkdirs();
            Util.getOperatingSystem().open(logs);
            sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"Папка логов открыта\"}");
         } catch (Exception e) {
            sendJsonResponse(exchange, 500, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
         }
      }
   }

   private static class ReloadConfigHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;
         try {
            String active = ConfigStore.getActiveConfig();
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) {
               mc.execute(() -> ConfigStore.load(active));
            } else {
               ConfigStore.load(active);
            }
            sendJsonResponse(exchange, 200, "{\"success\": true, \"config\": \"" + active + "\", \"message\": \"Конфиг успешно перезагружен\"}");
         } catch (Exception e) {
            sendJsonResponse(exchange, 500, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
         }
      }
   }

   private static class DisconnectHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;
         try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) {
               mc.execute(() -> {
                  if (mc.world != null) {
                     mc.disconnect(new TitleScreen(), false);
                  }
               });
               sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"Дисконнект выполнен\"}");
               return;
            }
         } catch (Exception e) {
            sendJsonResponse(exchange, 500, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
            return;
         }
         sendJsonResponse(exchange, 400, "{\"success\": false, \"error\": \"Клиент недоступен\"}");
      }
   }

}

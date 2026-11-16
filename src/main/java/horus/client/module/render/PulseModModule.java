package horus.client.module.render;



import com.google.gson.JsonObject;



import com.google.gson.JsonParser;



import horus.client.event.ClientTickEvent;



import horus.client.event.EventListener;



import horus.client.module.Module;



import horus.client.module.ModuleCategory;



import horus.client.module.ModuleInfo;



import horus.client.setting.ActionSetting;



import horus.client.setting.SingleChoiceSetting;



import horus.client.setting.TextSetting;



import horus.client.ui.hud.NotificationHud;



import net.fabricmc.loader.api.FabricLoader;



import net.minecraft.client.MinecraftClient;



import net.minecraft.text.Text;



import net.minecraft.util.Formatting;



import net.minecraft.util.Identifier;



import net.minecraft.util.Util;



import java.net.URI;



import java.net.URLEncoder;



import java.net.http.HttpClient;



import java.net.http.HttpRequest;



import java.net.http.HttpResponse;



import java.nio.charset.StandardCharsets;



import java.nio.file.Files;



import java.nio.file.Path;



import java.nio.file.StandardOpenOption;



import java.time.Duration;



import java.util.HashMap;



import java.util.Map;



import java.util.UUID;



import java.util.concurrent.CompletableFuture;



import java.util.concurrent.Executors;



import java.util.concurrent.ScheduledExecutorService;



import java.util.concurrent.ScheduledFuture;



import java.util.concurrent.TimeUnit;



@ModuleInfo(



   name = "PulseMod",



   description = "Официальная интеграция с PulseVisuals. Подтверждение входа в аккаунт через сайт.",



   category = ModuleCategory.RENDER



)



public final class PulseModModule extends Module {



   private static PulseModModule instance;



   private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()



      .connectTimeout(Duration.ofSeconds(10))



      .build();



   private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {



      Thread t = new Thread(r, "PulseAuthPoller");



      t.setDaemon(true);



      return t;



   });



   private ScheduledFuture<?> pollingTask;



   private boolean linked = false;



   private String token = "";



   public final ActionSetting openSiteButton;



   public final TextSetting accountLoginSetting;



   public final ActionSetting websiteWardrobeButton;



   public final ActionSetting unlinkButton;



   private PulseModModule() {



      ActionSetting openBtn = new ActionSetting("Открыть сайт", "Открыть официальный сайт с подтверждением входа в аккаунт PulseVisuals.")



         .withButtonLabel("Войти через Pulse")



         .onClick(this::openSite);



      openBtn.withVisibility(() -> !this.linked);



      this.openSiteButton = this.addSetting(openBtn);



      TextSetting loginSet = new TextSetting("Логин PulseVisuals", "Логин вашего подтверждённого аккаунта на сайте pulsevisuals.pro")



         .withPlaceholder("Введите логин на сайте...");



      loginSet.withVisibility(() -> this.linked);



      loginSet.onChange(this::saveProfile);



      this.accountLoginSetting = this.addSetting(loginSet);



      ActionSetting wardrobeBtn = new ActionSetting("Гардероб на сайте", "Открыть страницу гардероба на сайте pulsevisuals.pro для смены косметики.")



         .withButtonLabel("Гардероб на сайте")



         .onClick(() -> openBrowser("https://pulsevisuals.pro/workshop?type=cosmetics"));



      wardrobeBtn.withVisibility(() -> this.linked);



      this.websiteWardrobeButton = this.addSetting(wardrobeBtn);



      ActionSetting unlinkBtn = new ActionSetting("Отвязать аккаунт", "Отвязать текущий аккаунт PulseVisuals.")



         .withButtonLabel("Отвязать аккаунт")



         .onClick(this::unlinkAccount);



      unlinkBtn.withVisibility(() -> this.linked);



      this.unlinkButton = this.addSetting(unlinkBtn);



      loadProfile();



   }



   public static synchronized PulseModModule getInstance() {



      if (instance == null) {



         instance = new PulseModModule();



      }



      return instance;



   }



   public boolean isLinked() {



      return this.linked;



   }



   public boolean isPulseActive() {



      return this.isEnabled() && this.linked;



   }



   public String getAccountLogin() {



      return this.accountLoginSetting != null ? this.accountLoginSetting.getValue().trim() : "";



   }



   public void openSite() {

      // Запускаем надежный прямой официальный Device Auth flow к работающему API PulseVisuals

      startDeviceAuthFlowDirect();

   }



   private void startPulseLoginWatcher() {



      if (pollingTask != null && !pollingTask.isDone()) {



         pollingTask.cancel(true);



      }



      final int[] count = {0};



      pollingTask = scheduler.scheduleAtFixedRate(() -> {



         count[0]++;



         if (count[0] > 120) {



            if (pollingTask != null) pollingTask.cancel(false);



            return;



         }



         try {



            String loadedToken = null;



            try {



               Class<?> siteLoginClass = Class.forName("ru.pulse.free.auth.SiteLogin");



               loadedToken = (String) siteLoginClass.getMethod("loadToken").invoke(null);



            } catch (Throwable t) {



               Path tokenFile = MinecraftClient.getInstance().runDirectory.toPath().resolve("pulse_cache").resolve("cosmetics_token.txt");



               if (Files.exists(tokenFile)) {



                  loadedToken = Files.readString(tokenFile, StandardCharsets.UTF_8).trim();



               }



            }



            if (loadedToken != null && !loadedToken.isBlank()) {



               if (pollingTask != null) pollingTask.cancel(false);



               String discoveredNick = null;



               try {



                  Class<?> clientClass = Class.forName("ru.pulse.free.net.CosmeticsClient");



                  Object client = clientClass.getMethod("getInstance").invoke(null);



                  Object account = clientClass.getMethod("account").invoke(client);



                  if (account != null) {



                     discoveredNick = (String) account.getClass().getMethod("nick").invoke(account);



                  }



               } catch (Throwable ignored) {}



               if (discoveredNick == null || discoveredNick.isBlank()) {



                  MinecraftClient mc = MinecraftClient.getInstance();



                  discoveredNick = (mc.player != null) ? mc.player.getName().getString() : "PulseUser";



               }



               final String fNick = discoveredNick;



               final String fToken = loadedToken;



               MinecraftClient.getInstance().execute(() -> onAuthConfirmed(fNick, fToken));



            }



         } catch (Exception ignored) {}



      }, 1, 1, TimeUnit.SECONDS);



   }



   private void startDeviceAuthFlowDirect() {



      String state = UUID.randomUUID().toString();



      CompletableFuture.runAsync(() -> {



         String[] candidateHosts = new String[] {

            "https://pulsevisuals.pro/api",

            "https://euapi.pulsevisuals.pro/api",

            "https://api.pulsevisuals.pro/api"

         };



         String workingBase = null;



         for (String base : candidateHosts) {



            try {



               String initUrl = base + "/auth/device/init?state=" + URLEncoder.encode(state, StandardCharsets.UTF_8) + "&scope=cosmetics";



               HttpRequest initReq = HttpRequest.newBuilder()



                  .uri(URI.create(initUrl))



                  .header("User-Agent", "PulseCosmetics/0.1.0")



                  .timeout(Duration.ofSeconds(4))



                  .GET()



                  .build();



               HttpResponse<String> resp = HTTP_CLIENT.send(initReq, HttpResponse.BodyHandlers.ofString());



               if (resp.statusCode() == 200) {



                  JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();



                  if (json.has("success") && json.get("success").getAsBoolean()) {



                     workingBase = base;



                     break;



                  }



               }



            } catch (Exception ignored) {}



         }



         if (workingBase == null) {



            MinecraftClient mc = MinecraftClient.getInstance();



            mc.execute(() -> {



               NotificationHud.notifyWithText(



                  this.category.getIcon(),



                  "PulseVisuals",



                  Text.literal("Ошибка связи с сервером Pulse").formatted(Formatting.RED),



                  false



               );



               if (mc.player != null) {



                  mc.player.sendMessage(



                     Text.literal("§c[PulseVisuals] Не удалось создать запрос на сервере. Проверьте интернет или повторите позже."),



                     false



                  );



               }



            });



            return;



         }



         final String apiBase = workingBase;



         String authWebUrl = "https://pulsevisuals.pro/auth/device?state=" + state;



         // Открываем браузер ТОЛЬКО ПОСЛЕ успешной инициализации на сервере!



         openBrowser(authWebUrl);



         MinecraftClient mc = MinecraftClient.getInstance();



         mc.execute(() -> {



            NotificationHud.notifyWithText(



               this.category.getIcon(),



               "PulseVisuals",



               Text.literal("Подтвердите вход в браузере").formatted(Formatting.AQUA),



               false



            );



            if (mc.player != null) {



               mc.player.sendMessage(



                  Text.literal("§9[PulseVisuals] §aПодтвердите вход в браузере: §b" + authWebUrl),



                  false



               );



            }



         });



         startPollingApi(apiBase, state);



      });



   }



   private void openBrowser(String url) {

      try {

         Util.getOperatingSystem().open(URI.create(url));

         return;

      } catch (Throwable ignored) {}

      try {

         if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {

            java.awt.Desktop.getDesktop().browse(URI.create(url));

            return;

         }

      } catch (Throwable ignored) {}

      try {

         new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();

         return;

      } catch (Throwable ignored) {}

      try {

         new ProcessBuilder("cmd.exe", "/c", "start", "", url).start();

      } catch (Throwable ignored) {}

   }







   private void startPollingApi(String apiBase, String state) {



      if (pollingTask != null && !pollingTask.isDone()) {



         pollingTask.cancel(true);



      }



      final int[] attempts = {0};



      String pollUrl = apiBase + "/auth/device/poll?state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);



      pollingTask = scheduler.scheduleAtFixedRate(() -> {



         attempts[0]++;



         if (attempts[0] > 130) {



            if (pollingTask != null) {



               pollingTask.cancel(false);



            }



            return;



         }



         try {



            HttpRequest pollReq = HttpRequest.newBuilder()



               .uri(URI.create(pollUrl))



               .header("User-Agent", "PulseCosmetics/0.1.0")



               .timeout(Duration.ofSeconds(6))



               .GET()



               .build();



            HttpResponse<String> resp = HTTP_CLIENT.send(pollReq, HttpResponse.BodyHandlers.ofString());



            if (resp.statusCode() == 200) {



               JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();



               if (json.has("status")) {



                  String st = json.get("status").getAsString();



                  if ("confirmed".equalsIgnoreCase(st)) {



                     if (pollingTask != null) {



                        pollingTask.cancel(false);



                     }



                     String receivedToken = json.has("token") && !json.get("token").isJsonNull() ? json.get("token").getAsString() : "";



                     String login = extractLogin(json, receivedToken);



                     MinecraftClient.getInstance().execute(() -> onAuthConfirmed(login, receivedToken));



                  } else if ("expired".equalsIgnoreCase(st) || "cancelled".equalsIgnoreCase(st) || "not_found".equalsIgnoreCase(st)) {



                     if (pollingTask != null) {



                        pollingTask.cancel(false);



                     }



                  }



               }



            }



         } catch (Exception ignored) {



         }



      }, 1500, 1500, TimeUnit.MILLISECONDS);



   }



   private String extractLogin(JsonObject json, String receivedToken) {



      if (json.has("login") && !json.get("login").isJsonNull()) {



         return json.get("login").getAsString();



      }



      if (json.has("username") && !json.get("username").isJsonNull()) {



         return json.get("username").getAsString();



      }



      if (json.has("name") && !json.get("name").isJsonNull()) {



         return json.get("name").getAsString();



      }



      if (json.has("user") && !json.get("user").isJsonNull()) {



         if (json.get("user").isJsonObject()) {



            JsonObject u = json.getAsJsonObject("user");



            if (u.has("login")) return u.get("login").getAsString();



            if (u.has("username")) return u.get("username").getAsString();



            if (u.has("name")) return u.get("name").getAsString();



         }



      }



      MinecraftClient mc = MinecraftClient.getInstance();



      return (mc.player != null) ? mc.player.getName().getString() : "PulseUser";



   }



   public void onAuthConfirmed(String login, String receivedToken) {



      this.linked = true;



      this.token = receivedToken;



      String currentVal = this.accountLoginSetting.getValue();



      if (currentVal == null || currentVal.isBlank() || currentVal.equalsIgnoreCase("PulseUser")) {



         String fallback = (login != null && !login.isBlank()) ? login : ((MinecraftClient.getInstance().player != null) ? MinecraftClient.getInstance().player.getName().getString() : "PulseUser");



         this.accountLoginSetting.setValue(fallback);



      }



      saveProfile();



      syncPulseModToken(this.token);



      syncProfileCache();



      MinecraftClient mc = MinecraftClient.getInstance();



      NotificationHud.notifyWithText(



         this.category.getIcon(),



         "PulseVisuals",



         Text.literal("Аккаунт успешно привязан: " + this.getAccountLogin()).formatted(Formatting.GREEN),



         false



      );



      if (mc.player != null) {



         mc.player.sendMessage(



            Text.literal("§9[PulseVisuals] §aАккаунт успешно привязан! Логин: §b" + this.getAccountLogin()),



            false



         );



      }



   }



   public void unlinkAccount() {



      if (pollingTask != null && !pollingTask.isDone()) {



         pollingTask.cancel(true);



      }



      this.linked = false;



      this.token = "";



      this.accountLoginSetting.setValue("");



      saveProfile();



      removePulseModToken();



      try {



         Class<?> pulseCosmeticsClass = Class.forName("ru.pulse.free.PulseCosmetics");



         pulseCosmeticsClass.getMethod("logout").invoke(null);



      } catch (Throwable ignored) {}



      try {



         Class<?> pulseClass = Class.forName("ru.pulse.Pulse");



         Class<?> userInfoClass = Class.forName("ru.pulse.utils.UserInfo");



         pulseClass.getMethod("setUserInfo", userInfoClass).invoke(null, (Object) null);



      } catch (Throwable ignored) {}



      NotificationHud.notifyWithText(



         this.category.getIcon(),



         "PulseVisuals",



         Text.literal("Аккаунт успешно отвязан").formatted(Formatting.YELLOW),



         false



      );



      MinecraftClient mc = MinecraftClient.getInstance();



      if (mc.player != null) {



         mc.player.sendMessage(



            Text.literal("§9[PulseVisuals] §eАккаунт был отвязан."),



            false



         );



      }



   }



   public Map<String, Integer> getActiveCosmetics() {



      Map<String, Integer> map = new HashMap<>();



      // Отображаем исключительно реальную косметику, надетую на сайте PulseVisuals



      try {



         Class<?> clientClass = Class.forName("ru.pulse.free.net.CosmeticsClient");



         Object client = clientClass.getMethod("getInstance").invoke(null);



         Object account = clientClass.getMethod("account").invoke(client);



         if (account != null) {



            Map<?, ?> remote = (Map<?, ?>) account.getClass().getMethod("cosmetics").invoke(account);



            if (remote != null && !remote.isEmpty()) {



               for (Map.Entry<?, ?> e : remote.entrySet()) {



                  if (e.getKey() instanceof String k && e.getValue() instanceof Integer v) {



                     // Исключаем плащи по требованию пользователя ("убери плащи")



                     if (!"cape".equalsIgnoreCase(k) && !"cloak".equalsIgnoreCase(k)) {



                        map.put(k, v);



                     }



                  }



               }



            }



         }



      } catch (Throwable ignored) {}



      return map;



   }



   private void syncPulseModToken(String tokenToSync) {



      if (tokenToSync == null || tokenToSync.isBlank()) return;



      try {



         Path runDir = MinecraftClient.getInstance().runDirectory.toPath();



         Path pulseCache = runDir.resolve("pulse_cache");



         Files.createDirectories(pulseCache);



         Files.writeString(pulseCache.resolve("cosmetics_token.txt"), tokenToSync.trim(), StandardCharsets.UTF_8);



         String appData = System.getenv("APPDATA");



         if (appData != null && !appData.isBlank()) {



            Path mcCache = Path.of(appData, ".minecraft", "pulse_cache");



            Files.createDirectories(mcCache);



            Files.writeString(mcCache.resolve("cosmetics_token.txt"), tokenToSync.trim(), StandardCharsets.UTF_8);



         }



         try {



            Class<?> clientClass = Class.forName("ru.pulse.free.net.CosmeticsClient");



            Object clientInst = clientClass.getMethod("getInstance").invoke(null);



            clientClass.getMethod("start", String.class).invoke(clientInst, tokenToSync.trim());



         } catch (Throwable ignored) {



         }



      } catch (Exception ignored) {



      }



   }



   private void removePulseModToken() {



      try {



         Path runDir = MinecraftClient.getInstance().runDirectory.toPath();



         Path tokenFile = runDir.resolve("pulse_cache").resolve("cosmetics_token.txt");



         Files.deleteIfExists(tokenFile);



         try {



            Class<?> clientClass = Class.forName("ru.pulse.free.net.CosmeticsClient");



            Object clientInst = clientClass.getMethod("getInstance").invoke(null);



            clientClass.getMethod("stop").invoke(clientInst);



         } catch (Throwable ignored) {



         }



      } catch (Exception ignored) {



      }



   }



   private Path getProfilePath() {



      return FabricLoader.getInstance().getConfigDir().resolve("horus").resolve("pulse_account.json");



   }



   private void loadProfile() {



      try {



         Path path = getProfilePath();



         if (Files.exists(path)) {



            String content = Files.readString(path, StandardCharsets.UTF_8);



            JsonObject obj = JsonParser.parseString(content).getAsJsonObject();



            if (obj.has("linked") && obj.get("linked").getAsBoolean()) {



               this.linked = true;



               if (obj.has("login")) {



                  this.accountLoginSetting.setValue(obj.get("login").getAsString());



               }



               if (obj.has("token")) {



                  this.token = obj.get("token").getAsString();



               }



            }



         }



         if (this.token == null || this.token.isBlank()) {



            try {



               Path runToken = MinecraftClient.getInstance().runDirectory.toPath().resolve("pulse_cache").resolve("cosmetics_token.txt");



               if (Files.exists(runToken)) {



                  this.token = Files.readString(runToken, StandardCharsets.UTF_8).trim();



               } else {



                  String appData = System.getenv("APPDATA");



                  if (appData != null && !appData.isBlank()) {



                     Path mcToken = Path.of(appData, ".minecraft", "pulse_cache", "cosmetics_token.txt");



                     if (Files.exists(mcToken)) {



                        this.token = Files.readString(mcToken, StandardCharsets.UTF_8).trim();



                     }



                  }



               }



               if (this.token != null && !this.token.isBlank()) {



                  this.linked = true;



                  saveProfile();



               }



            } catch (Exception ignored) {}



         }



         if (this.linked && this.isEnabled()) {



            syncPulseModToken(this.token);



         } else if (!this.linked) {



            removePulseModToken();



         }



      } catch (Exception ignored) {



      }



   }



   private void saveProfile() {



      try {



         Path path = getProfilePath();



         Files.createDirectories(path.getParent());



         JsonObject obj = new JsonObject();



         obj.addProperty("linked", this.linked);



         obj.addProperty("login", this.accountLoginSetting != null ? this.accountLoginSetting.getValue() : "");



         obj.addProperty("token", this.token);



         Files.writeString(path, obj.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);



      } catch (Exception ignored) {



      }



   }



   @Override



   protected void onEnable() {



      super.onEnable();



      if (this.linked && !this.token.isBlank()) {



         syncPulseModToken(this.token);



         syncProfileCache();



      }



   }



   public void syncProfileCache() {



      if (!this.isPulseActive()) return;



      MinecraftClient mc = MinecraftClient.getInstance();



      Map<String, Integer> cosmetics = getActiveCosmetics();



      // 1. Устанавливаем косметику в Pulse.setUserInfo для рендерера CosmeticFeatureRenderer



      try {



         Class<?> pulseClass = Class.forName("ru.pulse.Pulse");



         Class<?> userInfoClass = Class.forName("ru.pulse.utils.UserInfo");



         Object userInfo = userInfoClass.getConstructor(Map.class).newInstance(cosmetics);



         pulseClass.getMethod("setUserInfo", userInfoClass).invoke(null, userInfo);



      } catch (Throwable ignored) {}



      // 2. Убеждаемся что CosmeticTextureManager загрузил бандл и текстуры



      try {



         Class<?> ctmClass = Class.forName("ru.pulse.cosmetic.CosmeticTextureManager");



         Object ctm = ctmClass.getMethod("getInstance").invoke(null);



         java.lang.reflect.Field loadedField = ctmClass.getDeclaredField("loaded");



         loadedField.setAccessible(true);



         boolean loaded = (boolean) loadedField.get(ctm);



         if (!loaded) {



            ctmClass.getMethod("loadAll").invoke(ctm);



         }



      } catch (Throwable ignored) {}



      // 3. Регистрируем никнеймы игрока в ProfileCache с Tier 2 (фирменный логотип Pulse в Табе)



      try {



         Class<?> cacheClass = Class.forName("ru.pulse.free.net.ProfileCache");



         Object cacheInst = cacheClass.getMethod("getInstance").invoke(null);



         java.lang.reflect.Method putMethod = cacheClass.getMethod("put", String.class, int.class, Map.class);



         String sessionName = (mc.getSession() != null) ? mc.getSession().getUsername() : null;



         if (sessionName != null && !sessionName.isBlank()) {



            putMethod.invoke(cacheInst, sessionName, 2, cosmetics);



         }



         if (mc.player != null) {



            String entityName = mc.player.getName().getString();



            if (entityName != null && !entityName.isBlank()) {



               putMethod.invoke(cacheInst, entityName, 2, cosmetics);



            }



            String gameProfileName = mc.player.getGameProfile().name();



            if (gameProfileName != null && !gameProfileName.isBlank()) {



               putMethod.invoke(cacheInst, gameProfileName, 2, cosmetics);



            }



         }



         String login = this.getAccountLogin();



         if (login != null && !login.isBlank()) {



            putMethod.invoke(cacheInst, login, 2, cosmetics);



         }



      } catch (Throwable ignored) {}



      // 3.5. Поддерживаем активный коннект к Pulse WebSocket и актуализируем текущий сервер



      try {



         Class<?> clientClass = Class.forName("ru.pulse.free.net.CosmeticsClient");



         Object clientInst = clientClass.getMethod("getInstance").invoke(null);



         boolean isConn = false;



         try {



            java.lang.reflect.Field connField = clientClass.getDeclaredField("socket");



            connField.setAccessible(true);



            Object conn = connField.get(clientInst);



            if (conn != null) {



               isConn = (boolean) conn.getClass().getMethod("isConnected").invoke(conn);



            }



         } catch (Throwable ignored) {}



         if (!isConn && this.token != null && !this.token.isBlank()) {



            clientClass.getMethod("start", String.class).invoke(clientInst, this.token.trim());



         }



         if (mc.getCurrentServerEntry() != null && mc.player != null) {



            String srv = mc.getCurrentServerEntry().address;



            String nick = mc.player.getName().getString();



            clientClass.getMethod("updateServer", String.class, String.class).invoke(clientInst, srv, nick);



         }



      } catch (Throwable ignored) {}



      // 4. Устанавливаем Tier 2 в AccountState



      try {



         Class<?> clientClass = Class.forName("ru.pulse.free.net.CosmeticsClient");



         Object client = clientClass.getMethod("getInstance").invoke(null);



         Object account = clientClass.getMethod("account").invoke(client);



         if (account != null) {



            int currentTier = (int) account.getClass().getMethod("tier").invoke(account);



            if (currentTier == 0) {



               account.getClass().getMethod("setNick", String.class).invoke(account, (mc.player != null) ? mc.player.getName().getString() : "PulseUser");



               java.lang.reflect.Field tierField = account.getClass().getDeclaredField("tier");



               tierField.setAccessible(true);



               tierField.setInt(account, 2);



            }



         }



      } catch (Throwable ignored) {}



   }



   private int tickCounter = 0;



   public final EventListener<ClientTickEvent> clientTickListener = clientTickEvent -> {



      if (clientTickEvent.isPre() && this.isPulseActive()) {



         if (++this.tickCounter >= 20) {



            this.tickCounter = 0;



            this.syncProfileCache();



         }



      }



   };



   @Override



   protected void onDisable() {



      if (pollingTask != null && !pollingTask.isDone()) {



         pollingTask.cancel(true);



      }



      removePulseModToken();



      try {



         Class<?> pulseClass = Class.forName("ru.pulse.Pulse");



         Class<?> userInfoClass = Class.forName("ru.pulse.utils.UserInfo");



         pulseClass.getMethod("setUserInfo", userInfoClass).invoke(null, (Object) null);



      } catch (Throwable ignored) {}



      super.onDisable();



   }



}




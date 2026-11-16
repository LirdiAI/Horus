package horus.client.util;

import horus.client.ui.UiTheme;
import horus.client.util.ClientChat;
import horus.client.event.EventListener;
import horus.client.MinecraftAccess;
import horus.client.event.PlayerTickEvent;
import ru.dreamix.protection.api.impl.profile.ProfileAPI;
import ru.dreamix.protection.api.impl.profile.objects.RoleObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent.SuggestCommand;
import net.minecraft.text.HoverEvent.ShowText;
import net.minecraft.text.StyleSpriteSource.Font;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public final class IrcClient implements MinecraftAccess {
   public static final String DEFAULT_SERVER_URL = "http://127.0.0.1:38241/irc";
   private static IrcClient ircClient;
   private static final Style style = Style.EMPTY.withFont(new Font(Identifier.of("horus", "irc"))).withColor(16777215).withoutShadow();
   private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
   private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).build();
   private Path path;
   private String serverUrl = DEFAULT_SERVER_URL;
   private boolean flag;
   private volatile boolean flag2;
   private long timeMillis;
   private HashMap<String, String> map = new HashMap<>();
   private final Set<String> processedMessageKeys = Collections.newSetFromMap(
      new LinkedHashMap<String, Boolean>() {
         @Override
         protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
            return size() > 500;
         }
      }
   );

   private final EventListener<PlayerTickEvent> playerTickListener = playerTickEvent -> {
      if (this.flag && !this.flag2 && System.currentTimeMillis() - this.timeMillis >= 2000L) {
         this.flag2 = true;
         this.timeMillis = System.currentTimeMillis();
         this.getCompletableFuture(this.getJsonObject("poll")).whenComplete((jsonObject, throwable) -> {
            this.flag2 = false;
            if (jsonObject != null && jsonObject.has("messages")) {
               for (JsonElement jsonElement : jsonObject.getAsJsonArray("messages")) {
                  if (jsonElement != null && jsonElement.isJsonObject()) {
                     JsonObject msg = jsonElement.getAsJsonObject();
                     String key = (msg.has("id") ? msg.get("id").getAsString() : "") + ":" +
                                  (msg.has("name") ? msg.get("name").getAsString() : "") + ":" +
                                  (msg.has("time") ? msg.get("time").getAsLong() : "") + ":" +
                                  (msg.has("text") ? msg.get("text").getAsString() : "");
                     if (this.processedMessageKeys.add(key)) {
                        mc.execute(() -> this.processData3(msg));
                     }
                  }
               }
            }
         });
      }
   };

   private JsonObject getJsonObject(String local4430_1) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("action", local4430_1);
      String name = "User";
      String role = "DEFAULT";
      try {
         if (ProfileAPI.get() != null && ProfileAPI.get().getModel() != null) {
            name = ProfileAPI.get().getModel().getName();
            if (ProfileAPI.get().getModel().getRole() != null) {
               role = ProfileAPI.get().getModel().getRole().name();
            }
         }
      } catch (Exception ignored) {
      }
      String nick = (mc.getSession() != null && mc.getSession().getUsername() != null) ? mc.getSession().getUsername() : name;
      jsonObject.addProperty("name", name);
      jsonObject.addProperty("role", role);
      jsonObject.addProperty("nick", nick);
      return jsonObject;
   }

   public void processData(String local4431_1) {
      if (!local4431_1.isBlank()) {
         JsonObject jsonObject = this.getJsonObject("send");
         Matcher matcher = Pattern.compile("^([\\p{L}\\p{N}_.\\-]{1,24}),\\s*(.+)$").matcher(local4431_1);
         boolean local4431_4 = matcher.matches() && this.map.containsKey(matcher.group(1).toLowerCase(Locale.ROOT));
         jsonObject.addProperty("text", local4431_4 ? matcher.group(2) : local4431_1);
         if (local4431_4) {
            jsonObject.addProperty("replyTo", matcher.group(1));
            jsonObject.addProperty("replyToNick", this.map.get(matcher.group(1).toLowerCase(Locale.ROOT)));
         }

         this.getCompletableFuture(jsonObject).thenAccept(jsonObject2 -> {
            if (jsonObject2 == null) {
               ClientChat.processData8("&cIRC сейчас недоступен, попробуй позже.");
            } else if (!jsonObject2.has("ok") || !jsonObject2.get("ok").getAsBoolean()) {
               ClientChat.processData8("&c" + (jsonObject2.has("message") ? jsonObject2.get("message").getAsString() : "Сообщение не отправлено."));
            } else {
               this.timeMillis = 0L;
            }
         });
      }
   }

   private int computeInt(RoleObject roleObject) {
      return switch (roleObject.name()) {
         case "BETA" -> 11832575;
         case "YOUTUBE" -> 16734826;
         case "ADMIN" -> 16757575;
         default -> 13226457;
      };
   }

   private RoleObject getRoleObject(String local4434_1) {
      try {
         return RoleObject.valueOf(local4434_1);
      } catch (Exception exception) {
         return RoleObject.DEFAULT;
      }
   }

   public void updateState() {
      this.getCompletableFuture(this.getJsonObject("online"))
         .thenAccept(
            jsonObject -> {
               if (jsonObject != null && jsonObject.has("online")) {
                  MutableText text = Text.literal("В IRC сейчас " + jsonObject.getAsJsonArray("online").size() + ": ");
                  boolean local4439_3 = true;

                  for (JsonElement jsonElement : jsonObject.getAsJsonArray("online")) {
                     if (!local4439_3) {
                        text.append(Text.literal(", ").formatted(Formatting.DARK_GRAY));
                     }

                     local4439_3 = false;
                     RoleObject roleObject = this.getRoleObject(jsonElement.getAsJsonObject().get("role").getAsString());
                     text.append(Text.literal(String.valueOf((char)(57360 + roleObject.ordinal()))).setStyle(style)).append(" ");
                     text.append(
                        Text.literal(jsonElement.getAsJsonObject().get("name").getAsString()).setStyle(Style.EMPTY.withColor(this.computeInt(roleObject)))
                     );
                  }

                  ClientChat.processData5(text);
               } else {
                  ClientChat.processData8("&cIRC сейчас недоступен, попробуй позже.");
               }
            }
         );
   }

   public boolean checkState() {
      return this.flag;
   }

   public String getServerUrl() {
      return this.serverUrl;
   }

   public void setServerUrl(String newUrl) {
      if (newUrl != null && !newUrl.isBlank()) {
         String u = newUrl.trim();
         if (!u.startsWith("http://") && !u.startsWith("https://")) {
            u = "http://" + u;
         }
         this.serverUrl = u;
         this.saveConfig();
      }
   }

   private void saveConfig() {
      try {
         if (this.path != null) {
            Files.createDirectories(this.path.getParent());
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("enabled", this.flag);
            jsonObject.addProperty("url", this.serverUrl);
            Files.writeString(this.path, jsonObject.toString(), StandardCharsets.UTF_8);
         }
      } catch (Exception ignored) {
      }
   }

   private CompletableFuture<JsonObject> getCompletableFuture(JsonObject jsonObject) {
      try {
         URI uri = URI.create(this.serverUrl);
         return this.httpClient
            .sendAsync(
               HttpRequest.newBuilder(uri)
                  .timeout(Duration.ofSeconds(6L))
                  .header("Content-Type", "application/json")
                  .header("X-Horus-Key", "dlx_7Qm2vX9pK4tR8wZ1nB6s")
                  .POST(BodyPublishers.ofString(jsonObject.toString(), StandardCharsets.UTF_8))
                  .build(),
               BodyHandlers.ofString(StandardCharsets.UTF_8)
            )
            .thenApply(response -> {
               if (response != null && response.statusCode() == 200) {
                  return JsonParser.parseString(response.body()).getAsJsonObject();
               }
               return null;
            })
            .exceptionally(throwable -> null);
      } catch (Exception e) {
         return CompletableFuture.completedFuture(null);
      }
   }

   public void processData2(boolean local4440_1) {
      this.flag = local4440_1;
      this.timeMillis = 0L;
      if (!local4440_1) {
         this.getCompletableFuture(this.getJsonObject("leave"));
      }
      this.saveConfig();
   }

   private IrcClient() {
      this.path = FabricLoader.getInstance().getConfigDir().resolve("horus").resolve("irc.json");

      try {
         if (Files.exists(this.path)) {
            JsonObject jsonObject = JsonParser.parseString(Files.readString(this.path, StandardCharsets.UTF_8)).getAsJsonObject();
            if (jsonObject.has("enabled")) {
               this.flag = jsonObject.get("enabled").getAsBoolean();
            }
            if (jsonObject.has("url")) {
               String u = jsonObject.get("url").getAsString().trim();
               if (!u.isEmpty() && !u.contains("horusclient.ru")) {
                  this.serverUrl = u;
               }
            }
         }
      } catch (Exception ignored) {
      }
   }

   private void processData3(JsonObject jsonObject) {
      String asString = jsonObject.has("name") ? jsonObject.get("name").getAsString() : "User";
      String asString2 = jsonObject.has("text") ? jsonObject.get("text").getAsString() : "";
      if (asString2.isEmpty()) return;
      String asString3 = jsonObject.has("nick") ? jsonObject.get("nick").getAsString() : asString;
      String name = "User";
      try {
         if (ProfileAPI.get() != null && ProfileAPI.get().getModel() != null) {
            name = ProfileAPI.get().getModel().getName();
         }
      } catch (Exception ignored) {}
      String username = (mc.getSession() != null && mc.getSession().getUsername() != null) ? mc.getSession().getUsername() : "Player";
      String local4444_7 = jsonObject.has("replyTo") ? jsonObject.get("replyTo").getAsString() : "";
      String local4444_8 = jsonObject.has("replyToNick") ? jsonObject.get("replyToNick").getAsString() : "";
      RoleObject roleObject = this.getRoleObject(jsonObject.has("role") ? jsonObject.get("role").getAsString() : "DEFAULT");
      boolean local4444_10 = asString.equalsIgnoreCase(name) && asString3.equalsIgnoreCase(username);
      boolean local4444_11 = !local4444_10 && local4444_7.equalsIgnoreCase(name) && (local4444_8.isEmpty() || local4444_8.equalsIgnoreCase(username));
      boolean local4444_12 = local4444_11 || !local4444_10 && asString2.toLowerCase(Locale.ROOT).contains("@" + name.toLowerCase(Locale.ROOT));
      if (!local4444_10) {
         this.map.put(asString.toLowerCase(Locale.ROOT), asString3);
      }

      int emoji = jsonObject.has("emoji") ? jsonObject.get("emoji").getAsInt() : 0;
      long time = jsonObject.has("time") ? jsonObject.get("time").getAsLong() : (System.currentTimeMillis() / 1000L);

      Text text = Text.literal(roleObject.toString())
         .setStyle(Style.EMPTY.withColor(this.computeInt(roleObject)))
         .append(
            Text.literal("\nНик в игре: " + (asString3.isEmpty() ? "—" : asString3))
               .formatted(Formatting.GRAY)
         )
         .append(Text.literal("\nОтправлено: " + dateTimeFormatter.format(Instant.ofEpochSecond(time))).formatted(Formatting.GRAY));
      ClickEvent clickEvent = new SuggestCommand("@" + asString + ", ");
      MutableText text2 = Text.empty();
      text2.append(Text.literal("\ue000").setStyle(style.withColor(local4444_11 ? 16763709 : UiTheme.intVal7 & 16777215)));
      text2.append(Text.literal(" "));
      text2.append(Text.literal(String.valueOf((char)(57600 + Math.floorMod(emoji, 20)))).setStyle(style));
      text2.append(Text.literal(" "));
      HoverEvent hoverEvent = null;
      try {
         if (ProfileAPI.get() != null && ProfileAPI.get().getModel() != null && ProfileAPI.get().getModel().getRole() != null) {
            if (ProfileAPI.get().getModel().getRole().priorityHigherOrEquals(RoleObject.ADMIN)) {
               hoverEvent = new ShowText(text);
            }
         }
      } catch (Exception ignored) {}
      text2.append(Text.literal(String.valueOf((char)(57360 + roleObject.ordinal()))).setStyle(style.withHoverEvent(hoverEvent)));
      text2.append(Text.literal(" "));
      text2.append(
         Text.literal(asString).setStyle(Style.EMPTY.withColor(this.computeInt(roleObject)).withHoverEvent(hoverEvent).withClickEvent(clickEvent))
      );
      if (local4444_11) {
         text2.append(Text.literal(" "))
            .append(Text.literal("\ue014").setStyle(style.withHoverEvent(new ShowText(Text.literal(asString + " ответил тебе")))));
      } else if (!local4444_7.isEmpty()) {
         text2.append(Text.literal(" → ").formatted(Formatting.DARK_GRAY)).append(Text.literal(local4444_7).setStyle(Style.EMPTY.withColor(10134445)));
      }

      text2.append(Text.literal(" » ").formatted(Formatting.DARK_GRAY));
      text2.append(Text.literal(asString2).setStyle(Style.EMPTY.withColor(local4444_11 ? 16765788 : (local4444_12 ? 16769902 : 15593459))));
      text2.append(Text.literal(" "));
      text2.append(
         Text.literal("\ue001")
            .setStyle(style.withColor(9279397).withHoverEvent(new ShowText(Text.literal("Ответить " + asString))).withClickEvent(clickEvent))
      );
      ClientChat.processData4(text2);
      if (local4444_12) {
         SoundPlayer.processData("irc_mention");
         if (local4444_11) {
            mc.inGameHud
               .setOverlayMessage(
                  Text.empty()
                     .append(Text.literal("\ue014").setStyle(style))
                     .append(Text.literal(" " + asString).setStyle(Style.EMPTY.withColor(this.computeInt(roleObject))))
                     .append(Text.literal(" » ").formatted(Formatting.DARK_GRAY))
                     .append(
                        Text.literal(asString2.length() > 60 ? asString2.substring(0, 60) + "…" : asString2).setStyle(Style.EMPTY.withColor(16765788))
                     ),
                  false
               );
         }
      }
   }

   public static IrcClient getInstance() {
      if (ircClient == null) {
         ircClient = new IrcClient();
      }

      return ircClient;
   }
}

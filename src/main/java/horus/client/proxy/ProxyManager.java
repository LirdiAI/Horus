package horus.client.proxy;

import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ProxyManager {
   private static ProxyManager instance;

   public enum ProxyType {
      SOCKS5("SOCKS5"),
      SOCKS4("SOCKS4"),
      HTTP("HTTP");

      private final String displayName;

      ProxyType(String displayName) {
         this.displayName = displayName;
      }

      public String getDisplayName() {
         return this.displayName;
      }
   }

   public static class ProxyProfile {
      public String name;
      public String host;
      public int port;
      public ProxyType type;
      public String username;
      public String password;
      public int ping = -1;
      public String status = "Не проверен";

      public ProxyProfile(String name, String host, int port, ProxyType type, String username, String password) {
         this.name = name;
         this.host = host;
         this.port = port;
         this.type = type;
         this.username = username;
         this.password = password;
      }
   }

   private boolean enabled = false;
   private String host = "127.0.0.1";
   private int port = 1080;
   private ProxyType type = ProxyType.SOCKS5;
   private String username = "";
   private String password = "";
   private String statusMessage = "Отключено";
   private int currentPing = -1;
   private boolean failover = false;
   private int failoverStrikes;
   private final List<ProxyProfile> profiles = new ArrayList<>();

   public static ProxyManager getInstance() {
      if (instance == null) {
         instance = new ProxyManager();
         instance.loadConfig();
         instance.startWatchdog();
      }
      return instance;
   }

   private ProxyManager() {
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
      if (enabled) {
         applyProxy();
      } else {
         clearProxy();
      }
      saveConfig();
   }

   public void applyProxy() {
      try {
         if (!this.enabled || this.host == null || this.host.trim().isEmpty() || this.port <= 0) {
            clearProxy();
            return;
         }

         String cleanHost = this.host.trim();
         int cleanPort = this.port;

         if (this.type == ProxyType.HTTP) {
            System.setProperty("http.proxyHost", cleanHost);
            System.setProperty("http.proxyPort", String.valueOf(cleanPort));
            System.setProperty("https.proxyHost", cleanHost);
            System.setProperty("https.proxyPort", String.valueOf(cleanPort));
            System.clearProperty("socksProxyHost");
            System.clearProperty("socksProxyPort");
         } else {
            System.setProperty("socksProxyHost", cleanHost);
            System.setProperty("socksProxyPort", String.valueOf(cleanPort));
            System.setProperty("socksProxyVersion", this.type == ProxyType.SOCKS4 ? "4" : "5");
            System.clearProperty("http.proxyHost");
            System.clearProperty("http.proxyPort");
            System.clearProperty("https.proxyHost");
            System.clearProperty("https.proxyPort");
         }

         if (this.username != null && !this.username.trim().isEmpty()) {
            final String u = this.username.trim();
            final String p = this.password != null ? this.password : "";
            Authenticator.setDefault(new Authenticator() {
               @Override
               protected PasswordAuthentication getPasswordAuthentication() {
                  return new PasswordAuthentication(u, p.toCharArray());
               }
            });
         } else {
            Authenticator.setDefault(null);
         }

         this.statusMessage = "Активен (" + this.type.getDisplayName() + ")";
      } catch (Exception e) {
         this.statusMessage = "Ошибка: " + e.getMessage();
      }
   }

   public void clearProxy() {
      try {
         System.clearProperty("socksProxyHost");
         System.clearProperty("socksProxyPort");
         System.clearProperty("socksProxyVersion");
         System.clearProperty("http.proxyHost");
         System.clearProperty("http.proxyPort");
         System.clearProperty("https.proxyHost");
         System.clearProperty("https.proxyPort");
         Authenticator.setDefault(null);
         this.statusMessage = "Отключено";
      } catch (Exception ignored) {
      }
   }

   public boolean isFailover() {
      return this.failover;
   }

   public synchronized void setFailover(boolean failover) {
      this.failover = failover;
      this.failoverStrikes = 0;
      saveConfig();
   }

   private void startWatchdog() {
      Thread thread = new Thread(() -> {
         while (true) {
            try {
               Thread.sleep(10000L);
            } catch (InterruptedException exception) {
               return;
            }
            checkFailover();
         }
      }, "Horus-Proxy-Failover");
      thread.setDaemon(true);
      thread.start();
   }

   private void checkFailover() {
      String currentHost;
      int currentPort;
      ProxyType currentType;
      boolean active;
      synchronized (this) {
         active = this.enabled && this.failover && this.port > 0 && this.host != null && !this.host.isBlank() && !this.profiles.isEmpty();
         currentHost = this.host;
         currentPort = this.port;
         currentType = this.type;
      }

      if (!active) {
         this.failoverStrikes = 0;
         return;
      }

      long start = System.currentTimeMillis();
      boolean reachable;
      try (Socket socket = new Socket()) {
         socket.connect(new InetSocketAddress(currentHost.trim(), currentPort), 2500);
         reachable = true;
         this.currentPing = (int) (System.currentTimeMillis() - start);
      } catch (Exception exception) {
         reachable = false;
      }

      if (reachable) {
         this.failoverStrikes = 0;
         return;
      }

      this.failoverStrikes++;
      if (this.failoverStrikes < 2) {
         return;
      }

      synchronized (this) {
         ProxyProfile next = nextProfile(currentHost, currentPort, currentType);
         this.failoverStrikes = 0;
         if (next == null) {
            return;
         }
         activateProfile(next);
         this.statusMessage = "Автопереключение: " + next.host + ":" + next.port;
      }
   }

   private synchronized ProxyProfile nextProfile(String host, int port, ProxyType type) {
      if (this.profiles.isEmpty()) {
         return null;
      }
      int index = -1;
      for (int index2 = 0; index2 < this.profiles.size(); index2++) {
         if (isSameProfile(this.profiles.get(index2), host, port, type)) {
            index = index2;
            break;
         }
      }
      if (index < 0) {
         return this.profiles.get(0);
      }
      if (this.profiles.size() == 1) {
         return null;
      }
      return this.profiles.get((index + 1) % this.profiles.size());
   }

   public void testConnection(Consumer<String> callback) {
      new Thread(() -> {
         long start = System.currentTimeMillis();
         try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(this.host.trim(), this.port), 3000);
            int ping = (int)(System.currentTimeMillis() - start);
            this.currentPing = ping;
            this.statusMessage = "Доступен (" + ping + " ms)";
            if (callback != null) callback.accept(this.statusMessage);
         } catch (Exception e) {
            this.currentPing = -1;
            this.statusMessage = "Недоступен";
            if (callback != null) callback.accept("Недоступен: " + e.getMessage());
         }
      }, "Proxy-Check-Thread").start();
   }

   public void verifyAndSaveCurrentProfile(Consumer<String> callback) {
      final String targetHost = this.host == null ? "" : this.host.trim();
      final int targetPort = this.port;
      final ProxyType targetType = this.type;
      final String targetUsername = this.username == null ? "" : this.username;
      final String targetPassword = this.password == null ? "" : this.password;

      if (targetHost.isEmpty() || targetPort < 1 || targetPort > 65535) {
         if (callback != null) callback.accept("Укажите корректные хост и порт");
         return;
      }

      new Thread(() -> {
         long start = System.currentTimeMillis();
         try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(targetHost, targetPort), 3000);
            int ping = (int)(System.currentTimeMillis() - start);
            boolean added = this.saveProfile(targetHost, targetPort, targetType, targetUsername, targetPassword, ping);
            this.currentPing = ping;
            this.statusMessage = "Доступен (" + ping + " ms)";
            if (callback != null) callback.accept((added ? "Сервер сохранён" : "Сервер обновлён") + " (" + ping + " ms)");
         } catch (Exception e) {
            this.currentPing = -1;
            this.statusMessage = "Недоступен";
            if (callback != null) callback.accept("Не удалось сохранить: прокси недоступен");
         }
      }, "Proxy-Save-Check-Thread").start();
   }

   private synchronized boolean saveProfile(String host, int port, ProxyType type, String username, String password, int ping) {
      for (ProxyProfile profile : this.profiles) {
         if (profile.port == port && profile.host != null && profile.host.equalsIgnoreCase(host)) {
            profile.name = profileName(host, port);
            profile.type = type;
            profile.username = username;
            profile.password = password;
            profile.ping = ping;
            profile.status = "Доступен (" + ping + " ms)";
            this.saveConfig();
            return false;
         }
      }

      ProxyProfile profile = new ProxyProfile(profileName(host, port), host, port, type, username, password);
      profile.ping = ping;
      profile.status = "Доступен (" + ping + " ms)";
      this.profiles.add(profile);
      this.saveConfig();
      return true;
   }

   public synchronized void activateProfile(ProxyProfile profile) {
      if (profile == null) return;

      this.host = profile.host;
      this.port = profile.port;
      this.type = profile.type;
      this.username = profile.username == null ? "" : profile.username;
      this.password = profile.password == null ? "" : profile.password;
      this.currentPing = profile.ping;
      this.enabled = true;
      this.applyProxy();
      this.saveConfig();
   }

   public synchronized void removeProfile(String host, int port, ProxyType type) {
      if (this.profiles.removeIf(profile -> isSameProfile(profile, host, port, type))) {
         this.saveConfig();
      }
   }

   public synchronized void saveConfig() {
      try {
         Path path = Paths.get("horus/proxy.json");
         if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
         }
         StringBuilder sb = new StringBuilder();
         sb.append("{\n");
         sb.append("  \"enabled\": ").append(this.enabled).append(",\n");
         sb.append("  \"failover\": ").append(this.failover).append(",\n");
         sb.append("  \"type\": \"").append(this.type.name()).append("\",\n");
         sb.append("  \"host\": \"").append(escapeJson(this.host)).append("\",\n");
         sb.append("  \"port\": ").append(this.port).append(",\n");
         sb.append("  \"username\": \"").append(escapeJson(this.username)).append("\",\n");
         sb.append("  \"password\": \"").append(escapeJson(this.password)).append("\",\n");
         sb.append("  \"profiles\": [");
         for (int i = 0; i < this.profiles.size(); i++) {
            ProxyProfile profile = this.profiles.get(i);
            sb.append("\n    { \"host\": \"").append(escapeJson(profile.host)).append("\", ");
            sb.append("\"port\": ").append(profile.port).append(", ");
            sb.append("\"type\": \"").append(profile.type.name()).append("\", ");
            sb.append("\"username\": \"").append(escapeJson(profile.username)).append("\", ");
            sb.append("\"password\": \"").append(escapeJson(profile.password)).append("\", ");
            sb.append("\"ping\": ").append(profile.ping).append(" }");
            if (i < this.profiles.size() - 1) sb.append(",");
         }
         if (!this.profiles.isEmpty()) sb.append("\n  ");
         sb.append("]\n");
         sb.append("}\n");
         Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
      } catch (Exception ignored) {
      }
   }

   public synchronized void loadConfig() {
      try {
         Path path = Paths.get("horus/proxy.json");
         if (!Files.exists(path)) {
            path = Paths.get("run/horus/proxy.json");
         }
         if (Files.exists(path)) {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            if (json.contains("\"enabled\": true")) this.enabled = true;
            if (json.contains("\"enabled\": false")) this.enabled = false;
            if (json.contains("\"failover\": true")) this.failover = true;
            if (json.contains("\"failover\": false")) this.failover = false;

            for (ProxyType t : ProxyType.values()) {
               if (json.contains("\"type\": \"" + t.name() + "\"")) {
                  this.type = t;
                  break;
               }
            }

            extractString(json, "host", val -> this.host = val);
            extractString(json, "username", val -> this.username = val);
            extractString(json, "password", val -> this.password = val);

            int pIdx = json.indexOf("\"port\":");
            if (pIdx != -1) {
               int start = pIdx + 7;
               while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\t')) start++;
               int end = start;
               while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
               if (end > start) {
                  this.port = Integer.parseInt(json.substring(start, end));
               }
            }

            this.loadProfiles(json);

            if (this.enabled) {
               applyProxy();
            }
         }
      } catch (Exception ignored) {
      }
   }

   private void loadProfiles(String json) {
      this.profiles.clear();
      int profilesKey = json.indexOf("\"profiles\"");
      if (profilesKey < 0) return;

      int arrayStart = json.indexOf('[', profilesKey);
      int arrayEnd = json.indexOf(']', arrayStart);
      if (arrayStart < 0 || arrayEnd < 0) return;

      int cursor = arrayStart + 1;
      while (cursor < arrayEnd) {
         int objectStart = json.indexOf('{', cursor);
         if (objectStart < 0 || objectStart >= arrayEnd) break;
         int objectEnd = findJsonObjectEnd(json, objectStart);
         if (objectEnd < 0 || objectEnd > arrayEnd) break;

         String profileJson = json.substring(objectStart, objectEnd + 1);
         String profileHost = extractStringValue(profileJson, "host");
         int profilePort = extractIntValue(profileJson, "port", -1);
         String profileType = extractStringValue(profileJson, "type");
         ProxyType parsedType = parseType(profileType);
         if (profileHost != null && !profileHost.isBlank() && profilePort > 0 && profilePort <= 65535 && parsedType != null) {
            ProxyProfile profile = new ProxyProfile(
               profileName(profileHost, profilePort),
               profileHost,
               profilePort,
               parsedType,
               defaultString(extractStringValue(profileJson, "username")),
               defaultString(extractStringValue(profileJson, "password"))
            );
            profile.ping = extractIntValue(profileJson, "ping", -1);
            profile.status = profile.ping >= 0 ? "Доступен (" + profile.ping + " ms)" : "Не проверен";
            this.profiles.add(profile);
         }
         cursor = objectEnd + 1;
      }
   }

   private static ProxyType parseType(String typeName) {
      if (typeName == null) return null;
      try {
         return ProxyType.valueOf(typeName);
      } catch (IllegalArgumentException ignored) {
         return null;
      }
   }

   private static int findJsonObjectEnd(String json, int objectStart) {
      int depth = 0;
      boolean inString = false;
      boolean escaped = false;
      for (int i = objectStart; i < json.length(); i++) {
         char character = json.charAt(i);
         if (inString) {
            if (escaped) {
               escaped = false;
            } else if (character == '\\') {
               escaped = true;
            } else if (character == '"') {
               inString = false;
            }
         } else if (character == '"') {
            inString = true;
         } else if (character == '{') {
            depth++;
         } else if (character == '}' && --depth == 0) {
            return i;
         }
      }
      return -1;
   }

   private void extractString(String json, String key, Consumer<String> setter) {
      String value = extractStringValue(json, key);
      if (value != null) setter.accept(value);
   }

   private static String extractStringValue(String json, String key) {
      String pattern = "\"" + key + "\": \"";
      int idx = json.indexOf(pattern);
      if (idx == -1) return null;

      int start = idx + pattern.length();
      StringBuilder value = new StringBuilder();
      boolean escaped = false;
      for (int i = start; i < json.length(); i++) {
         char character = json.charAt(i);
         if (escaped) {
            switch (character) {
               case 'n' -> value.append('\n');
               case 'r' -> value.append('\r');
               case 't' -> value.append('\t');
               default -> value.append(character);
            }
            escaped = false;
         } else if (character == '\\') {
            escaped = true;
         } else if (character == '"') {
            return value.toString();
         } else {
            value.append(character);
         }
      }
      return null;
   }

   private static int extractIntValue(String json, String key, int fallback) {
      int keyIndex = json.indexOf("\"" + key + "\"");
      if (keyIndex < 0) return fallback;
      int colonIndex = json.indexOf(':', keyIndex);
      if (colonIndex < 0) return fallback;

      int start = colonIndex + 1;
      while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
      int end = start;
      if (end < json.length() && json.charAt(end) == '-') end++;
      while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
      try {
         return end > start ? Integer.parseInt(json.substring(start, end)) : fallback;
      } catch (NumberFormatException ignored) {
         return fallback;
      }
   }

   private static String escapeJson(String value) {
      if (value == null) return "";
      return value.replace("\\", "\\\\")
         .replace("\"", "\\\"")
         .replace("\n", "\\n")
         .replace("\r", "\\r")
         .replace("\t", "\\t");
   }

   private static String defaultString(String value) {
      return value == null ? "" : value;
   }

   private static String profileName(String host, int port) {
      return host + ":" + port;
   }

   private static boolean isSameProfile(ProxyProfile profile, String host, int port, ProxyType type) {
      return profile != null
         && profile.port == port
         && profile.type == type
         && profile.host != null
         && profile.host.equalsIgnoreCase(host);
   }

   // Getters and Setters
   public String getHost() { return host; }
   public void setHost(String host) { this.host = host; }
   public int getPort() { return port; }
   public void setPort(int port) { this.port = port; }
   public ProxyType getType() { return type; }
   public void setType(ProxyType type) { this.type = type; }
   public String getUsername() { return username; }
   public void setUsername(String username) { this.username = username; }
   public String getPassword() { return password; }
   public void setPassword(String password) { this.password = password; }
   public String getStatusMessage() { return statusMessage; }
   public int getCurrentPing() { return currentPing; }
   public synchronized List<ProxyProfile> getProfiles() {
      List<ProxyProfile> copy = new ArrayList<>();
      for (ProxyProfile profile : this.profiles) {
         ProxyProfile item = new ProxyProfile(profile.name, profile.host, profile.port, profile.type, profile.username, profile.password);
         item.ping = profile.ping;
         item.status = profile.status;
         copy.add(item);
      }
      return copy;
   }
}

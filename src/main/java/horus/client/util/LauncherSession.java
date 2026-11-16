package horus.client.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LauncherSession {
   private static String cachedUsername = null;
   private static final Pattern ACTIVE_PATTERN = Pattern.compile("\"active\"\\s*:\\s*\"([^\"]+)\"");

   public static String getLauncherUsername() {
      if (cachedUsername != null && !cachedUsername.isEmpty() && !cachedUsername.equalsIgnoreCase("user")) {
         return cachedUsername;
      }

      // 1. JVM property: -Dhorus.user=...
      String sysProp = System.getProperty("horus.user");
      if (isValidUser(sysProp)) {
         return cachedUsername = sysProp.trim();
      }

      // 2. Environment variable: HORUS_USER
      String envUser = System.getenv("HORUS_USER");
      if (isValidUser(envUser)) {
         return cachedUsername = envUser.trim();
      }

      // 3. Local session files: horus_session.txt in current, parent, or run/ directory
      String[] candidateFilePaths = {
         "horus_session.txt",
         "run/horus_session.txt",
         "../horus_session.txt",
         "launcher/horus_session.txt"
      };
      for (String relativePath : candidateFilePaths) {
         try {
            Path path = Paths.get(relativePath);
            if (Files.exists(path)) {
               String content = Files.readString(path, StandardCharsets.UTF_8).trim();
               if (isValidUser(content)) {
                  return cachedUsername = content;
               }
            }
         } catch (Exception ignored) {
         }
      }

      // 4. Launcher accounts.json in %LOCALAPPDATA%/HorusLauncher/accounts.json
      String localAppData = System.getenv("LOCALAPPDATA");
      if (localAppData != null) {
         try {
            Path accountsPath = Paths.get(localAppData, "HorusLauncher", "accounts.json");
            if (Files.exists(accountsPath)) {
               String json = Files.readString(accountsPath, StandardCharsets.UTF_8);
               Matcher matcher = ACTIVE_PATTERN.matcher(json);
               if (matcher.find()) {
                  String matched = matcher.group(1).trim();
                  if (isValidUser(matched)) {
                     return cachedUsername = matched;
                  }
               }
            }
         } catch (Exception ignored) {
         }
      }

      // 5. Fallback candidate json paths
      String[] candidateJsonPaths = {
         "launcher/accounts.json",
         "../launcher/accounts.json",
         "accounts.json"
      };
      for (String jsonPath : candidateJsonPaths) {
         try {
            Path path = Paths.get(jsonPath);
            if (Files.exists(path)) {
               String json = Files.readString(path, StandardCharsets.UTF_8);
               Matcher matcher = ACTIVE_PATTERN.matcher(json);
               if (matcher.find()) {
                  String matched = matcher.group(1).trim();
                  if (isValidUser(matched)) {
                     return cachedUsername = matched;
                  }
               }
            }
         } catch (Exception ignored) {
         }
      }

      return cachedUsername = "Lumira";
   }

   private static boolean isValidUser(String name) {
      return name != null && !name.trim().isEmpty() && !name.trim().equalsIgnoreCase("user");
   }

   public static void setUsername(String username) {
      if (isValidUser(username)) {
         cachedUsername = username.trim();
      }
   }
}

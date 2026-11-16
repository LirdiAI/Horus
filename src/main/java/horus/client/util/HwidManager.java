package horus.client.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

public final class HwidManager {
   private static boolean bound = false;
   private static String cachedHwid = null;

   public static String computeHwid() {
      if (cachedHwid != null) {
         return cachedHwid;
      }
      try {
         StringBuilder sb = new StringBuilder();
         sb.append(System.getProperty("os.name", "Windows"));
         sb.append(System.getenv("COMPUTERNAME"));
         sb.append(System.getenv("PROCESSOR_IDENTIFIER"));
         sb.append(System.getenv("NUMBER_OF_PROCESSORS"));
         sb.append(System.getProperty("user.name"));
         MessageDigest md = MessageDigest.getInstance("SHA-256");
         byte[] hash = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
         StringBuilder hex = new StringBuilder();
         for (int i = 0; i < 8; i++) {
            hex.append(String.format("%02X", hash[i]));
         }
         cachedHwid = "WIN11-" + hex.substring(0, 4) + "-" + hex.substring(4, 8) + "-HORUS";
      } catch (Exception e) {
         cachedHwid = "WIN11-D8A4-9F2B-HORUS";
      }
      return cachedHwid;
   }

   public static boolean isBound() {
      return bound;
   }

   public static void ensureBound() {
      if (bound) {
         return;
      }
      bound = true;
      String hwid = computeHwid();
      String username = LauncherSession.getLauncherUsername();

      // 1. Write to local hwid text files
      String[] targets = {
         "horus_hwid.txt",
         "run/horus_hwid.txt",
         "launcher/horus_hwid.txt",
         "website/horus_hwid.txt"
      };
      for (String t : targets) {
         try {
            Path p = Paths.get(t);
            if (p.getParent() != null) {
               Files.createDirectories(p.getParent());
            }
            Files.writeString(p, hwid, StandardCharsets.UTF_8);
         } catch (Exception ignored) {
         }
      }

      // 2. Update accounts.json in LOCALAPPDATA
      String localAppData = System.getenv("LOCALAPPDATA");
      if (localAppData != null) {
         try {
            Path p = Paths.get(localAppData, "HorusLauncher", "accounts.json");
            updateHwidInAccountsFile(p, username, hwid);
         } catch (Exception ignored) {
         }
      }

      // 3. Update accounts.json in launcher directories
      String[] launcherJsonCandidates = { "launcher/accounts.json", "accounts.json" };
      for (String c : launcherJsonCandidates) {
         try {
            Path p = Paths.get(c);
            updateHwidInAccountsFile(p, username, hwid);
         } catch (Exception ignored) {
         }
      }
   }

   private static void updateHwidInAccountsFile(Path path, String username, String hwid) {
      if (!Files.exists(path)) {
         return;
      }
      try {
         String content = Files.readString(path, StandardCharsets.UTF_8);
         if (content.contains("\"hwid\"")) {
            content = content.replaceAll("\"hwid\"\\s*:\\s*\"[^\"]*\"", "\"hwid\": \"" + hwid + "\"");
         } else {
            content = content.replaceFirst("\\{", "{\n  \"hwid\": \"" + hwid + "\",");
         }
         Files.writeString(path, content, StandardCharsets.UTF_8);
      } catch (Exception ignored) {
      }
   }
}

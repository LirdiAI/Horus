package horus.client.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public final class Lang {
   public enum Language { RU, EN }

   private static Language language = Language.RU;

   private Lang() {
   }

   static {
      load();
   }

   public static Language get() {
      return language;
   }

   public static void toggle() {
      language = language == Language.RU ? Language.EN : Language.RU;
      save();
   }

   public static String tr(String ru, String en) {
      return language == Language.RU ? ru : en;
   }

   private static void load() {
      for (Path path : new Path[]{Paths.get("horus/lang.json"), Paths.get("run/horus/lang.json")}) {
         if (Files.isRegularFile(path)) {
            try {
               String json = Files.readString(path, StandardCharsets.UTF_8);
               if (json.contains("\"language\": \"EN\"")) {
                  language = Language.EN;
               } else if (json.contains("\"language\": \"RU\"")) {
                  language = Language.RU;
               }
               return;
            } catch (Exception ignored) {
            }
         }
      }
   }

   private static void save() {
      try {
         Path path = Paths.get("horus/lang.json");
         if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
         }
         Files.writeString(
            path,
            "{\n  \"language\": \"" + language.name() + "\"\n}\n",
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
         );
      } catch (Exception ignored) {
      }
   }
}

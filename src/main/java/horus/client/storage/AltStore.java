package horus.client.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Хранилище мультиаккаунта: ники альтов + пароли + команда режима.
 * Файл: config/horus/multiaccount.json
 */
public final class AltStore {
   private static final Map<String, String> alts = new LinkedHashMap<>();
   private static String modeCommand = "";
   private static boolean loaded;
   private static Path path;

   private AltStore() {
   }

   private static void ensureLoaded() {
      if (loaded) {
         return;
      }

      loaded = true;
      path = FabricLoader.getInstance().getConfigDir().resolve("horus").resolve("multiaccount.json");

      try {
         if (Files.exists(path)) {
            JsonObject root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.has("modeCommand")) {
               modeCommand = root.get("modeCommand").getAsString();
            }

            if (root.has("alts")) {
               for (JsonElement element : root.get("alts").getAsJsonArray()) {
                  JsonObject alt = element.getAsJsonObject();
                  alts.put(alt.get("name").getAsString(), alt.has("password") ? alt.get("password").getAsString() : "");
               }
            }
         }
      } catch (Exception ignored) {
      }
   }

   private static void save() {
      ensureLoaded();

      try {
         Files.createDirectories(path.getParent());
         JsonObject root = new JsonObject();
         root.addProperty("modeCommand", modeCommand);
         JsonArray array = new JsonArray();

         for (Map.Entry<String, String> entry : alts.entrySet()) {
            JsonObject alt = new JsonObject();
            alt.addProperty("name", entry.getKey());
            alt.addProperty("password", entry.getValue());
            array.add(alt);
         }

         root.add("alts", array);
         Files.writeString(path, root.toString(), StandardCharsets.UTF_8);
      } catch (Exception ignored) {
      }
   }

   public static void addAlt(String name, String password) {
      ensureLoaded();
      if (name == null || name.isEmpty()) {
         return;
      }

      alts.put(name, password == null ? "" : password);
      save();
   }

   public static void removeAlt(String name) {
      ensureLoaded();
      if (alts.remove(name) != null) {
         save();
      }
   }

   public static List<String> getAltNames() {
      ensureLoaded();
      return new ArrayList<>(alts.keySet());
   }

   public static String getPassword(String name) {
      ensureLoaded();
      return alts.getOrDefault(name, "");
   }

   public static void setModeCommand(String command) {
      ensureLoaded();
      String value = command == null ? "" : command.trim();
      while (value.startsWith("/")) {
         value = value.substring(1);
      }

      if (!value.equals(modeCommand)) {
         modeCommand = value;
         save();
      }
   }

   public static String getModeCommand() {
      ensureLoaded();
      return modeCommand;
   }
}

package horus.client.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;

/** Local favorites for the main-menu server list; never modifies Minecraft's servers.dat. */
public final class ServerMenuStore {
   private static final Set<String> favorites = new HashSet<>();
   private static boolean loaded;
   private static Path path;

   private ServerMenuStore() {
   }

   private static String key(String address) {
      return address == null ? "" : address.trim().toLowerCase(Locale.ROOT);
   }

   private static void load() {
      if (loaded) return;
      loaded = true;
      path = FabricLoader.getInstance().getConfigDir().resolve("horus").resolve("server-favorites.json");
      try {
         if (!Files.exists(path)) return;
         JsonObject root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
         JsonArray entries = root.getAsJsonArray("favorites");
         if (entries == null) return;
         for (JsonElement entry : entries) {
            if (entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString()) {
               String value = key(entry.getAsString());
               if (!value.isEmpty()) favorites.add(value);
            }
         }
      } catch (Exception ignored) {
         // Broken local preferences must not prevent the server list from opening.
      }
   }

   public static synchronized boolean isFavorite(String address) {
      load();
      return favorites.contains(key(address));
   }

   public static synchronized void toggleFavorite(String address) {
      load();
      String value = key(address);
      if (value.isEmpty()) return;
      if (!favorites.remove(value)) favorites.add(value);
      try {
         Files.createDirectories(path.getParent());
         JsonObject root = new JsonObject();
         JsonArray entries = new JsonArray();
         favorites.stream().sorted().forEach(entries::add);
         root.add("favorites", entries);
         Files.writeString(path, root.toString(), StandardCharsets.UTF_8);
      } catch (Exception ignored) {
         // Still usable for this session if the config directory is read-only.
      }
   }
}

package horus.client.invmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class InvConfigManager {
   private static InvConfigManager instance;
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

   private final Path configsDir;
   private InvLayoutConfig activeConfig;

   private InvConfigManager() {
      File runDir = MinecraftClient.getInstance().runDirectory;
      if (runDir != null) {
         this.configsDir = runDir.toPath().resolve("horus").resolve("invmanager");
      } else {
         this.configsDir = Path.of("horus", "invmanager");
      }
      this.ensureDirExists();
      this.initializeDefaultPresets();

      // Load PvP or first available config, or new default
      List<String> configs = this.getAvailableConfigs();
      if (configs.contains("PvP")) {
         this.activeConfig = this.loadConfig("PvP");
      } else if (!configs.isEmpty()) {
         this.activeConfig = this.loadConfig(configs.get(0));
      } else {
         this.activeConfig = InvLayoutConfig.createDefaultPvpPreset();
      }
   }

   public static synchronized InvConfigManager getInstance() {
      if (instance == null) {
         instance = new InvConfigManager();
      }
      return instance;
   }

   private void ensureDirExists() {
      try {
         if (!Files.exists(this.configsDir)) {
            Files.createDirectories(this.configsDir);
         }
      } catch (Exception ignored) {}
   }

   private void initializeDefaultPresets() {
      try {
         Path pvpPath = this.getFilePath("PvP");
         if (!Files.exists(pvpPath)) {
            this.saveConfig(InvLayoutConfig.createDefaultPvpPreset());
         }

         Path totemPath = this.getFilePath("Totem Kit");
         if (!Files.exists(totemPath)) {
            this.saveConfig(InvLayoutConfig.createTotemKitPreset());
         }
      } catch (Exception ignored) {}
   }

   private Path getFilePath(String name) {
      String cleanName = sanitizeName(name);
      return this.configsDir.resolve(cleanName + ".json");
   }

   public static String sanitizeName(String name) {
      if (name == null || name.isBlank()) return "Default";
      return name.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
   }

   public List<String> getAvailableConfigs() {
      this.ensureDirExists();
      List<String> list = new ArrayList<>();
      try {
         File folder = this.configsDir.toFile();
         if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, fName) -> fName.toLowerCase().endsWith(".json"));
            if (files != null) {
               for (File f : files) {
                  String base = f.getName().substring(0, f.getName().length() - 5);
                  list.add(base);
               }
            }
         }
      } catch (Exception ignored) {}
      Collections.sort(list);
      return list;
   }

   public InvLayoutConfig loadConfig(String name) {
      this.ensureDirExists();
      Path path = this.getFilePath(name);
      if (!Files.exists(path)) {
         return new InvLayoutConfig(name);
      }

      try (FileReader reader = new FileReader(path.toFile(), StandardCharsets.UTF_8)) {
         JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
         String cfgName = json.has("name") ? json.get("name").getAsString() : name;
         InvLayoutConfig config = new InvLayoutConfig(cfgName);

         if (json.has("slots") && json.get("slots").isJsonObject()) {
            JsonObject slotsObj = json.getAsJsonObject("slots");
            for (Map.Entry<String, JsonElement> entry : slotsObj.entrySet()) {
               try {
                  int slotIndex = Integer.parseInt(entry.getKey());
                  String itemId = entry.getValue().getAsString();
                  config.setItem(slotIndex, itemId);
               } catch (Exception ignored) {}
            }
         }
         return config;
      } catch (Exception e) {
         return new InvLayoutConfig(name);
      }
   }

   public boolean saveConfig(InvLayoutConfig config) {
      if (config == null) return false;
      this.ensureDirExists();
      Path path = this.getFilePath(config.getName());

      try (FileWriter writer = new FileWriter(path.toFile(), StandardCharsets.UTF_8)) {
         JsonObject root = new JsonObject();
         root.addProperty("name", config.getName());

         JsonObject slotsObj = new JsonObject();
         for (Map.Entry<Integer, String> entry : config.getSlots().entrySet()) {
            slotsObj.addProperty(String.valueOf(entry.getKey()), entry.getValue());
         }
         root.add("slots", slotsObj);

         GSON.toJson(root, writer);
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   public boolean deleteConfig(String name) {
      try {
         Path path = this.getFilePath(name);
         if (Files.exists(path)) {
            Files.delete(path);
            return true;
         }
      } catch (Exception ignored) {}
      return false;
   }

   public InvLayoutConfig getActiveConfig() {
      if (this.activeConfig == null) {
         this.activeConfig = InvLayoutConfig.createDefaultPvpPreset();
      }
      return this.activeConfig;
   }

   public void setActiveConfig(InvLayoutConfig activeConfig) {
      this.activeConfig = activeConfig;
   }
}

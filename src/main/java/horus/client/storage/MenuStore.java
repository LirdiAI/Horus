package horus.client.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Настройки фона главного меню (из Студии фона).
 * Файл: config/horus/menubackground.json
 */
public final class MenuStore {
   private static final String[] BACKGROUNDS = {
      "assets/horus/textures/gui/mainmenu/backgrounds/mc-shader-night.png",
      "assets/horus/textures/gui/mainmenu/backgrounds/mc-cherry-sunset.png"
   };

   private static int backgroundIndex;
   private static int centerLayout; // 0 = Fallen reference, 1 = все карточки по центру, 2 = сетка 2x3
   private static String customPhoto; // путь к своему фото на диске (null = не задано)
   private static String photoName = "";
   private static boolean glossy = true; // стеклянный глянец кнопок
   private static int liveMode; // 0 = статика, 1 = дыхание, 2 = частицы, 3 = дымка
   private static boolean loaded;
   private static boolean saveFailed;
   public static boolean saveFailed() { return saveFailed; }
   private static Path path;

   private MenuStore() {
   }

   private static void ensureLoaded() {
      if (loaded) {
         return;
      }

      loaded = true;
      path = FabricLoader.getInstance().getConfigDir().resolve("horus").resolve("menubackground.json");

      try {
         if (Files.exists(path)) {
            JsonObject root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.has("bg")) {
               backgroundIndex = Math.max(0, Math.min(BACKGROUNDS.length - 1, root.get("bg").getAsInt()));
            }

            if (root.has("layout")) {
               centerLayout = Math.max(0, Math.min(2, root.get("layout").getAsInt()));
            }

            if (root.has("photo") && root.get("photo").isJsonPrimitive()) {
               customPhoto = root.get("photo").getAsString();
            }

            if (root.has("photoName") && root.get("photoName").isJsonPrimitive()) {
               photoName = root.get("photoName").getAsString();
            }

            if (root.has("gloss") && root.get("gloss").isJsonPrimitive()) {
               glossy = root.get("gloss").getAsBoolean();
            }

            if (root.has("live")) {
               liveMode = Math.max(0, Math.min(3, root.get("live").getAsInt()));
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
         root.addProperty("bg", backgroundIndex);
         root.addProperty("layout", centerLayout);
         root.addProperty("photo", customPhoto == null ? "" : customPhoto);
         root.addProperty("photoName", photoName == null ? "" : photoName);
         root.addProperty("gloss", glossy);
         root.addProperty("live", liveMode);
         Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
         Files.writeString(temporary, root.toString(), StandardCharsets.UTF_8);
         Files.move(temporary, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
         saveFailed = false;
      } catch (Exception exception) {
         saveFailed = true;
         horus.client.HorusClient.logger().warn("[MainMenu] Could not save appearance settings", exception);
      }
   }

   public static String currentBackground() {
      ensureLoaded();
      if (customPhoto != null && !customPhoto.isBlank()) {
         // если файл пропал с диска — сбрасываем на встроенные обои
         if (!java.nio.file.Files.isRegularFile(java.nio.file.Path.of(customPhoto))) {
            customPhoto = null;
            photoName = "";
            save();
            return BACKGROUNDS[backgroundIndex];
         }

         // динамическая текстура, регистрируемая MainMenuScreen по выбору пользователя
         return "horus:images/menu/custom_bg";
      }

      return BACKGROUNDS[backgroundIndex];
   }

   public static String backgroundAt(int index) {
      ensureLoaded();
      return BACKGROUNDS[Math.max(0, Math.min(BACKGROUNDS.length - 1, index))];
   }

   public static int backgroundCount() {
      return BACKGROUNDS.length;
   }

   public static int backgroundIndex() {
      ensureLoaded();
      return backgroundIndex;
   }

   public static void setBackgroundIndex(int index) {
      ensureLoaded();
      int value = Math.max(0, Math.min(BACKGROUNDS.length - 1, index));
      if (value != backgroundIndex) {
         backgroundIndex = value;
         save();
      }
   }

   public static int centerLayout() {
      ensureLoaded();
      return centerLayout;
   }

   public static int liveMode() {
      ensureLoaded();
      return liveMode;
   }

   public static void setLiveMode(int value) {
      ensureLoaded();
      int mode = Math.max(0, Math.min(3, value));
      if (mode != liveMode) {
         liveMode = mode;
         save();
      }
   }

   public static void setCenterLayout(int index) {
      ensureLoaded();
      int value = Math.max(0, Math.min(2, index));
      if (value != centerLayout) {
         centerLayout = value;
         save();
      }
   }

   public static String customPhoto() {
      ensureLoaded();
      return customPhoto;
   }

   public static String photoName() {
      ensureLoaded();
      return photoName;
   }

   public static void setCustomPhoto(String pathOnDisk, String displayName) {
      ensureLoaded();
      customPhoto = pathOnDisk;
      photoName = displayName == null ? "" : displayName;
      save();
   }

   public static void clearCustomPhoto() {
      ensureLoaded();
      if (customPhoto != null || !photoName.isEmpty()) {
         customPhoto = null;
         photoName = "";
         save();
      }
   }

   public static boolean glossy() {
      ensureLoaded();
      return glossy;
   }

   public static void setGlossy(boolean value) {
      ensureLoaded();
      if (value != glossy) {
         glossy = value;
         save();
      }
   }

   public static void toggleGlossy() {
      ensureLoaded();
      glossy = !glossy;
      save();
   }
}
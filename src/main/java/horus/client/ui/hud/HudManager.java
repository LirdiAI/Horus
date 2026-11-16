package horus.client.ui.hud;

import horus.client.ui.AnimatedValue;
import horus.client.render.ArcColorSpan;
import horus.client.storage.ConfigStore;
import horus.client.render.FontText;
import horus.client.util.GuiScale;
import horus.client.ui.UiTheme;

import horus.client.render.FontFace;
import horus.client.render.Render2D;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

public final class HudManager {
   private static final Map<String, float[]> map = new HashMap<>();
   private static final Map<String, float[]> map2 = new HashMap<>();
   private static final Map<String, HudManager.SubContext> map3 = new LinkedHashMap<>();
   private static final Map<String, float[]> map4 = new HashMap<>();
   private static final Map<String, float[]> map5 = new HashMap<>();
   private static final Map<String, Long> map6 = new HashMap<>();
   private static final Map<String, Float> map7 = new HashMap<>();
   private static final Map<String, float[]> map8 = new HashMap<>();
   private static final AnimatedValue animation = new AnimatedValue(160L).getAnimatedValue(0.0);
   private static final AnimatedValue animation2 = new AnimatedValue(160L).getAnimatedValue(0.0);
   private static final Map<String, AnimatedValue> map9 = new HashMap<>();
   private static final Map<String, AnimatedValue> map10 = new HashMap<>();
   private static final AnimatedValue animation3 = new AnimatedValue(160L).getAnimatedValue(0.0);
   private static float[] floatValArray;
   private static float[] floatValArray2;
   private static String strVal;
   private static final List<String> list = new ArrayList<>(
      List.of("watermark", "keybinds", "serverbinds", "potions", "cooldowns", "staffs", "target", "inventory", "Target", "arrows")
   );
   private static String strVal2;
   private static String strVal3;
   private static float floatVal;
   private static float floatVal2;
   private static float floatVal3;
   private static float floatVal4;
   private static float floatVal5;
   private static float floatVal6;
   private static float floatVal7;
   private static float floatVal8;
   private static float floatVal9;
   private static boolean flag;
   private static Path path;

   public static List<String> getList() {
      return list;
   }

   public static boolean matchesCriteria(String local5539_0) {
      return computeFloat(local5539_0)[3] > 0.5F;
   }

   public static String findSnapAnchor(float local5540_0, float local5540_1) {
      List<Entry<String, HudManager.SubContext>> local5540_2 = new ArrayList<>(map3.entrySet());

      for (int index = local5540_2.size() - 1; index >= 0; index--) {
         HudManager.SubContext value = local5540_2.get(index).getValue();
         if (local5540_0 >= value.floatVal10
            && local5540_0 <= value.floatVal10 + value.floatVal12
            && local5540_1 >= value.floatVal11
            && local5540_1 <= value.floatVal11 + value.floatVal13) {
            return local5540_2.get(index).getKey();
         }
      }

      return null;
   }

   public static boolean matchesCriteria2(String local5541_0) {
      return false;
   }

   private static float[] computeFloat(String local5542_0) {
      updateState();
      return map2.computeIfAbsent(local5542_0, local5557_0 -> new float[]{0.9F, 1.0F, 0.0F, 0.0F});
   }

   public static void processData(String local5543_0, boolean local5543_1) {
      computeFloat(local5543_0)[3] = local5543_1 ? 1.0F : 0.0F;
      if (local5543_1) {
         map7.put(local5543_0, map5.containsKey(local5543_0) ? map5.get(local5543_0)[0] : floatVal8 / 2.0F);
      } else {
         map7.remove(local5543_0);
      }

      saveHudConfig();
   }

   public static float computeFloat2() {
      return floatVal8;
   }

   public static float[] computeFloat3(String local5545_0) {
      HudManager.SubContext local5545_1 = map3.get(local5545_0);
      return local5545_1 == null ? null : new float[]{local5545_1.floatVal10, local5545_1.floatVal11, local5545_1.floatVal12, local5545_1.floatVal13};
   }

   public static float computeFloat4() {
      return floatVal9;
   }

   public static float[] computeFloat5(String local5547_0, float local5547_1, float local5547_2) {
      updateState();
      float[] floatArray = map.get(local5547_0);
      float local5547_4 = floatArray != null ? floatArray[0] : local5547_1;
      float local5547_5 = floatArray != null ? floatArray[1] : local5547_2;
      if (!(floatVal8 <= 0.0F) && !(floatVal9 <= 0.0F)) {
         float[] floatArray2 = map4.get(local5547_0);
         float local5547_7 = floatArray2 != null ? floatArray2[0] : 0.0F;
         float local5547_8 = floatArray2 != null ? floatArray2[1] : 0.0F;
         float local5547_9 = floatArray2 != null ? floatArray2[2] : 0.0F;
         float local5547_10 = floatArray2 != null ? floatArray2[3] : 0.0F;
         if (matchesCriteria(local5547_0) && floatArray2 != null && map7.containsKey(local5547_0)) {
            local5547_4 = map7.get(local5547_0) + local5547_9 - local5547_7 / 2.0F;
         }

         float local5547_11 = Math.max(5.0F, Math.min(Math.max(5.0F, floatVal8 - local5547_7 - 5.0F), local5547_4 - local5547_9));
         float local5547_12 = Math.max(5.0F, Math.min(Math.max(5.0F, floatVal9 - local5547_8 - 5.0F), local5547_5 - local5547_10));
         if (floatArray2 != null) {
            local5547_12 = computeFloat7(local5547_0, local5547_11, local5547_12, local5547_7, local5547_8);
         }

         return new float[]{local5547_11 + local5547_9, local5547_12 + local5547_10};
      } else {
         return new float[]{local5547_4, local5547_5};
      }
   }

   public static boolean handleMouseScrolled(double mouseX, double mouseY, double amount) {
      updateState();
      float scaleFactor = computeFloat10();
      float scaledX = (float)(mouseX * scaleFactor);
      float scaledY = (float)(mouseY * scaleFactor);
      String element = findSnapAnchor(scaledX, scaledY);
      if (element != null) {
         float currentScale = computeFloat9(element);
         float step = amount > 0 ? 0.05F : -0.05F;
         float newScale = Math.max(0.4F, Math.min(1.8F, currentScale + step));
         processData3(element, newScale);
         saveHudConfig();
         return true;
      }
      return false;
   }

   public static void processData2(String local5549_0, boolean local5549_1) {
      computeFloat(local5549_0)[2] = local5549_1 ? 1.0F : 0.0F;
      saveHudConfig();
   }

   public static void processData3(String local5550_0, float local5550_1) {
      computeFloat(local5550_0)[0] = Math.max(0.25F, Math.min(2.0F, local5550_1));
   }

   public static float[] computeFloat6(String local5552_0) {
      return map5.get(local5552_0);
   }

   private static void processData4(JsonObject jsonObject) {
      for (String local5553_2 : jsonObject.keySet()) {
         JsonObject asJsonObject = jsonObject.getAsJsonObject(local5553_2);
         if (asJsonObject.has("x") && asJsonObject.has("y")) {
            map.put(local5553_2, new float[]{asJsonObject.get("x").getAsFloat(), asJsonObject.get("y").getAsFloat()});
         }

         if (asJsonObject.has("scale") || asJsonObject.has("auto") || asJsonObject.has("hideF1") || asJsonObject.has("pin")) {
            float[] floatArray = map2.computeIfAbsent(local5553_2, local5548_0 -> new float[]{0.9F, 1.0F, 0.0F, 0.0F});
            if (asJsonObject.has("scale")) {
               floatArray[0] = asJsonObject.get("scale").getAsFloat();
            }

            if (asJsonObject.has("auto")) {
               floatArray[1] = asJsonObject.get("auto").getAsBoolean() ? 1.0F : 0.0F;
            }

            if (asJsonObject.has("hideF1")) {
               floatArray[2] = asJsonObject.get("hideF1").getAsBoolean() ? 1.0F : 0.0F;
            }

            if (asJsonObject.has("pin")) {
               floatArray[3] = asJsonObject.get("pin").getAsBoolean() ? 1.0F : 0.0F;
            }
         }
      }
   }

   public static void processData5(int local5554_0) {
      if (local5554_0 == 0 && strVal2 != null) {
         saveHudConfig();
         strVal2 = null;
      }

      if (local5554_0 == 0 && strVal3 != null) {
         saveHudConfig();
         strVal3 = null;
      }
   }

   private static float computeFloat7(String local5555_0, float local5555_1, float local5555_2, float local5555_3, float local5555_4) {
      for (Entry<String, float[]> local5555_6 : map8.entrySet()) {
         if (!local5555_6.getKey().equals(local5555_0) && (!matchesCriteria(local5555_0) || computeInt(local5555_6.getKey()) < computeInt(local5555_0))) {
            float[] value = local5555_6.getValue();
            if (local5555_1 < value[0] + value[2]
               && local5555_1 + local5555_3 > value[0]
               && local5555_2 < value[1] + value[3]
               && local5555_2 + local5555_4 > value[1]) {
               local5555_2 = value[1] + value[3] + 4.0F;
            }
         }
      }

      return Math.max(5.0F, Math.min(Math.max(5.0F, floatVal9 - local5555_4 - 5.0F), local5555_2));
   }

   private HudManager() {
   }

   public static void processData6(
      String local5558_0, float local5558_1, float local5558_2, float local5558_3, float local5558_4, float local5558_5, float local5558_6
   ) {
      float local5558_7 = computeFloat8(local5558_0);
      float local5558_8 = local5558_1 + local5558_3 / 2.0F;
      float local5558_9 = local5558_2 + local5558_4 / 2.0F;
      HudManager.SubContext local5558_10 = new HudManager.SubContext();
      local5558_10.floatVal10 = local5558_8 - local5558_3 * local5558_7 / 2.0F;
      local5558_10.floatVal11 = local5558_9 - local5558_4 * local5558_7 / 2.0F;
      local5558_10.floatVal12 = local5558_3 * local5558_7;
      local5558_10.floatVal13 = local5558_4 * local5558_7;
      local5558_10.floatVal14 = local5558_5;
      local5558_10.floatVal15 = local5558_6;
      map3.put(local5558_0, local5558_10);
      map4.put(
         local5558_0,
         new float[]{local5558_10.floatVal12, local5558_10.floatVal13, local5558_5 - local5558_10.floatVal10, local5558_6 - local5558_10.floatVal11}
      );
      map5.put(local5558_0, new float[]{local5558_8, local5558_9});
      if (matchesCriteria(local5558_0)) {
         map8.put(local5558_0, new float[]{local5558_10.floatVal10, local5558_10.floatVal11, local5558_10.floatVal12, local5558_10.floatVal13});
         map7.putIfAbsent(local5558_0, local5558_8);
      } else {
         map8.remove(local5558_0);
      }
   }

   public static boolean checkState() {
      return strVal2 != null;
   }

   public static boolean matchesCriteria3(double local5560_0, double local5560_2, int local5560_4) {
      if (local5560_4 != 0) {
         return false;
      }

      float local5560_5 = computeFloat10();
      float local5560_6 = (float)(local5560_0 * local5560_5);
      float local5560_7 = (float)(local5560_2 * local5560_5);
      String local5560_8 = formatText(local5560_6, local5560_7);
      if (local5560_8 != null) {
         HudManager.SubContext local5560_9 = map3.get(local5560_8);
         strVal3 = local5560_8;
         floatVal3 = local5560_6;
         floatVal4 = local5560_7;
         floatVal5 = Math.max(0.25F, Math.min(2.0F, computeFloat9(local5560_8)));
         floatVal6 = Math.max(1.0F, local5560_9.floatVal12 / computeFloat8(local5560_8) / 2.0F);
         floatVal7 = Math.max(1.0F, local5560_9.floatVal13 / computeFloat8(local5560_8) / 2.0F);
         if (list.remove(strVal3)) {
            list.add(strVal3);
         }

         return true;
      } else {
         String local5560_9 = findSnapAnchor(local5560_6, local5560_7);
         if (local5560_9 == null) {
            return false;
         }

         HudManager.SubContext local5560_10 = map3.get(local5560_9);
         strVal2 = local5560_9;
         floatVal = local5560_6 - local5560_10.floatVal14;
         floatVal2 = local5560_7 - local5560_10.floatVal15;
         if (list.remove(strVal2)) {
            list.add(strVal2);
         }

         return true;
      }
   }

   private static void updateState() {
      if (!flag) {
         flag = true;
         path = FabricLoader.getInstance().getConfigDir().resolve("horus").resolve("draggables.json");

         try {
            if (!Files.exists(path)) {
               return;
            }

            processData4(JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject());
         } catch (Exception exception) {
         }
      }
   }

   public static JsonObject getJsonObject() {
      updateState();
      JsonObject jsonObject = new JsonObject();
      Set<String> local5562_1 = new LinkedHashSet<>(map.keySet());
      local5562_1.addAll(map2.keySet());

      for (String local5562_3 : local5562_1) {
         JsonObject jsonObject2 = new JsonObject();
         float[] floatArray = map.get(local5562_3);
         if (floatArray != null) {
            jsonObject2.addProperty("x", floatArray[0]);
            jsonObject2.addProperty("y", floatArray[1]);
         }

         float[] floatArray2 = map2.get(local5562_3);
         if (floatArray2 != null) {
            jsonObject2.addProperty("scale", floatArray2[0]);
            jsonObject2.addProperty("auto", floatArray2[1] > 0.5F);
            jsonObject2.addProperty("hideF1", floatArray2[2] > 0.5F);
            jsonObject2.addProperty("pin", floatArray2.length > 3 && floatArray2[3] > 0.5F);
         }

         jsonObject.add(local5562_3, jsonObject2);
      }

      return jsonObject;
   }

   static void processData7(float local5563_0, float local5563_1, boolean local5563_2, float local5563_3, float local5563_4) {
      floatVal8 = local5563_3;
      floatVal9 = local5563_4;
      MinecraftClient client = MinecraftClient.getInstance();
      boolean isLeftDown = client != null && client.getWindow() != null
         && org.lwjgl.glfw.GLFW.glfwGetMouseButton(client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
      if (!local5563_2 || !isLeftDown) {
         if (strVal2 != null) {
            saveHudConfig();
            strVal2 = null;
         }

         if (strVal3 != null) {
            saveHudConfig();
            strVal3 = null;
         }

         map6.clear();
      } else {
         if (strVal3 != null) {
            processData3(strVal3, floatVal5 + ((local5563_0 - floatVal3) / floatVal6 + (local5563_1 - floatVal4) / floatVal7) / 2.0F);
         }

         if (strVal2 != null) {
            float[] floatArray = map4.get(strVal2);
            float local5563_6 = floatArray != null ? floatArray[2] : 0.0F;
            float local5563_7 = floatArray != null ? floatArray[3] : 0.0F;
            float local5563_8 = local5563_0 - floatVal;
            float local5563_9 = local5563_1 - floatVal2;
            if (checkState2() && floatArray != null) {
               if (Math.abs(local5563_8 - local5563_6 + floatArray[0] / 2.0F - floatVal8 / 2.0F) < 8.0F) {
                  local5563_8 = floatVal8 / 2.0F - floatArray[0] / 2.0F + local5563_6;
               }

               if (Math.abs(local5563_9 - local5563_7 + floatArray[1] / 2.0F - floatVal9 / 2.0F) < 8.0F) {
                  local5563_9 = floatVal9 / 2.0F - floatArray[1] / 2.0F + local5563_7;
               }
            }

            map.put(strVal2, new float[]{local5563_8, local5563_9});
            if (matchesCriteria(strVal2) && floatArray != null) {
               map7.put(strVal2, local5563_8 - local5563_6 + floatArray[0] / 2.0F);
            }
         }
      }
   }

   private static String formatText(float local5564_0, float local5564_1) {
      List<Entry<String, HudManager.SubContext>> local5564_2 = new ArrayList<>(map3.entrySet());

      for (int index = local5564_2.size() - 1; index >= 0; index--) {
         HudManager.SubContext value = local5564_2.get(index).getValue();
         float gripSize = Math.max(14.0F, Math.min(24.0F, Math.min(value.floatVal12, value.floatVal13) * 0.45F));
         if (local5564_0 >= value.floatVal10 + value.floatVal12 - gripSize
            && local5564_0 <= value.floatVal10 + value.floatVal12 + 6.0F
            && local5564_1 >= value.floatVal11 + value.floatVal13 - gripSize
            && local5564_1 <= value.floatVal11 + value.floatVal13 + 6.0F) {
            return local5564_2.get(index).getKey();
         }
      }

      return null;
   }

   public static float computeFloat8(String local5566_0) {
      float local5566_1 = Math.max(0.25F, Math.min(2.0F, computeFloat(local5566_0)[0]));
      AnimatedValue animatedValue = map9.computeIfAbsent(local5566_0, local5537_1 -> new AnimatedValue(140L).getAnimatedValue(local5566_1));
      animatedValue.processData(local5566_1);
      return (float)animatedValue.computeDouble();
   }

   private static int computeInt(String local5567_0) {
      int local5567_1 = list.indexOf(local5567_0);
      return local5567_1 < 0 ? Integer.MAX_VALUE : local5567_1;
   }

   public static boolean matchesCriteria4(String local5568_0) {
      return computeFloat(local5568_0)[2] > 0.5F;
   }

   private static void saveHudConfig() {
      updateState();

      try {
         ConfigStore.writeAsync(path, getJsonObject().toString());
      } catch (Exception exception) {
      }
   }

   public static float computeFloat9(String local5570_0) {
      return computeFloat(local5570_0)[0];
   }

   static void saveHudLayout() {
      map3.clear();
   }

   public static void updateState2() {
      saveHudConfig();
   }

   static void processData8(float local5573_0, float local5573_1, boolean local5573_2) {
      if (local5573_2) {
         animation.processData(checkState2() && strVal2 != null ? 1.0 : 0.0);
         float local5573_3 = (float)animation.computeDouble();
         if (local5573_3 > 0.01F && floatVal8 > 0.0F && floatVal9 > 0.0F) {
            Render2D.processData51(
               floatVal8 / 2.0F - 0.25F,
               floatVal9 / 2.0F * (1.0F - local5573_3),
               0.5F,
               floatVal9 * local5573_3,
               0.0F,
               UiTheme.computeInt(UiTheme.intVal, 0.14F * local5573_3)
            );
            Render2D.processData51(
               floatVal8 / 2.0F * (1.0F - local5573_3),
               floatVal9 / 2.0F - 0.25F,
               floatVal8 * local5573_3,
               0.5F,
               0.0F,
               UiTheme.computeInt(UiTheme.intVal, 0.14F * local5573_3)
            );
         }

         long local5573_4 = System.currentTimeMillis();

         for (Entry<String, HudManager.SubContext> local5573_7 : map3.entrySet()) {
            HudManager.SubContext value = local5573_7.getValue();
            boolean local5573_9 = local5573_7.getKey().equals(strVal2)
               || local5573_0 >= value.floatVal10
                  && local5573_0 <= value.floatVal10 + value.floatVal12
                  && local5573_1 >= value.floatVal11
                  && local5573_1 <= value.floatVal11 + value.floatVal13;
            if (!local5573_9) {
               map6.remove(local5573_7.getKey());
            } else {
               float local5573_10 = Math.min(
                  1.0F, Math.max(0.02F, (float)(local5573_4 - map6.computeIfAbsent(local5573_7.getKey(), local5575_2 -> local5573_4)) / 50.0F)
               );
               Render2D.processData75(
                  value.floatVal10 - 1.5F,
                  value.floatVal11 - 1.5F,
                  value.floatVal12 + 3.0F,
                  value.floatVal13 + 3.0F,
                  9.5F,
                  0.5F,
                  0,
                  ArcColorSpan.of(90.0F - local5573_10 * 360.0F, 90.0F, UiTheme.computeInt(UiTheme.intVal, 0.3F))
               );

            }
         }

         String local5573_6 = formatText(local5573_0, local5573_1);

         for (Entry<String, HudManager.SubContext> local5573_8 : map3.entrySet()) {
            HudManager.SubContext value2 = local5573_8.getValue();
            AnimatedValue animatedValue = map10.computeIfAbsent(local5573_8.getKey(), local5565_0 -> new AnimatedValue(160L).getAnimatedValue(0.0));
            animatedValue.processData(
               !local5573_8.getKey().equals(strVal3)
                     && (
                        !(local5573_0 >= value2.floatVal10 - 4.0F)
                           || !(local5573_0 <= value2.floatVal10 + value2.floatVal12 + 4.0F)
                           || !(local5573_1 >= value2.floatVal11 - 4.0F)
                           || !(local5573_1 <= value2.floatVal11 + value2.floatVal13 + 4.0F)
                     )
                  ? 0.0
                  : 1.0
            );
            float local5573_11 = (float)animatedValue.computeDouble();
            if (!(local5573_11 < 0.01F)) {
               float local5573_12 = local5573_11 * (!local5573_8.getKey().equals(strVal3) && !local5573_8.getKey().equals(local5573_6) ? 0.45F : 0.9F);
               float local5573_13 = value2.floatVal10 + value2.floatVal12 + 1.5F;
               float local5573_14 = value2.floatVal11 + value2.floatVal13 + 1.5F;

               for (int index = 0; index < 3; index++) {
                  for (int index2 = 0; index2 < 3; index2++) {
                     if (index + index2 >= 2) {
                        Render2D.processData51(
                           local5573_13 - 3.2F - (2 - index) * 2.6F,
                           local5573_14 - 3.2F - (2 - index2) * 2.6F,
                           1.5F,
                           1.5F,
                           0.75F,
                           UiTheme.computeInt(-1, local5573_12 * 0.9F)
                        );
                     }
                  }
               }
            }
         }

         animation3.processData(strVal3 != null && map3.containsKey(strVal3) ? 1.0 : 0.0);
         if (strVal3 != null && map3.containsKey(strVal3)) {
            floatValArray2 = computeFloat3(strVal3);
            strVal = strVal3;
         }

         float local5573_7 = (float)animation3.computeDouble();
         if (local5573_7 > 0.01F && floatValArray2 != null) {
            String local5573_8 = Math.round(computeFloat9(strVal) * 100.0F) + "%";
            float local5573_9 = FontText.computeFloat(FontFace.SEMI, local5573_8, 5.0F);
            float local5573_10 = floatValArray2[0] + floatValArray2[2] + 5.0F;
            if (local5573_10 + local5573_9 > floatVal8 - 2.0F) {
               local5573_10 = floatValArray2[0] + floatValArray2[2] - 10.0F - local5573_9;
            }

            FontText.processData3(
               FontFace.SEMI,
               local5573_8,
               local5573_10,
               floatValArray2[1] + floatValArray2[3] + 3.0F + (1.0F - local5573_7) * 2.0F,
               5.0F,
               UiTheme.computeInt(UiTheme.intVal, 0.8F * local5573_7)
            );
         }

         animation2.processData(strVal2 != null && map3.containsKey(strVal2) ? 1.0 : 0.0);
         if (strVal2 != null && map3.containsKey(strVal2)) {
            floatValArray = computeFloat3(strVal2);
         }

         float local5573_8 = (float)animation2.computeDouble();
         if (local5573_8 > 0.01F && floatValArray != null) {
            FontText.processData3(
               FontFace.SEMI,
               "Зажмите LCTRL: сетка | Колесико мыши: размер",
               floatValArray[0]
                  + floatValArray[2] / 2.0F
                  - FontText.computeFloat(FontFace.SEMI, "Зажмите LCTRL: сетка | Колесико мыши: размер", 5.0F) / 2.0F,
               floatValArray[1] + floatValArray[3] + 4.0F + (1.0F - local5573_8) * 3.0F,
               5.0F,
               UiTheme.computeInt(UiTheme.intVal, 0.7F * local5573_8)
            );
         }
      }
   }

   private static float computeFloat10() {
      return GuiScale.computeInt() / GuiScale.computeFloat();
   }

   public static boolean checkState2() {
      return MinecraftClient.getInstance().getWindow() != null && InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), 341);
   }

   public static void processData9(String local5577_0, float local5577_1, float local5577_2, float local5577_3, float local5577_4) {
      processData6(local5577_0, local5577_1, local5577_2, local5577_3, local5577_4, local5577_1, local5577_2);
   }

   public static boolean matchesCriteria5(String local5578_0) {
      updateState();
      return map.containsKey(local5578_0);
   }

   public static float[] computeFloat11(double local5579_0, double local5579_2) {
      float local5579_4 = computeFloat10();
      return new float[]{(float)(local5579_0 * local5579_4), (float)(local5579_2 * local5579_4)};
   }

   public static void processData10(JsonObject jsonObject) {
      updateState();
      map.clear();
      map2.clear();
      map7.clear();
      processData4(jsonObject);
      saveHudConfig();
   }

   public static void processData11(String local5581_0, boolean local5581_1) {
      computeFloat(local5581_0)[1] = local5581_1 ? 1.0F : 0.0F;
      saveHudConfig();
   }

   public static void updateState3() {
      if (strVal2 != null) {
         saveHudConfig();
         strVal2 = null;
      }

      if (strVal3 != null) {
         saveHudConfig();
         strVal3 = null;
      }

      map6.clear();
   }

   private static final class SubContext {
      float floatVal10;
      float floatVal11;
      float floatVal12;
      float floatVal13;
      float floatVal14;
      float floatVal15;
   }
}

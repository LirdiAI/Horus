package horus.client.module.misc;

import horus.client.util.ClientChat;
import horus.client.event.EventListener;
import horus.client.setting.LegacyNumberSetting;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.event.PacketEvent;
import horus.client.event.PlayerTickEvent;
import horus.client.util.InventoryUtils;
import horus.client.setting.NumericTextSetting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

@ModuleInfo(name = "AutoDeliver", description = "Автоматическая выдача валюты по заказам FunPay.", category = ModuleCategory.MISC)
public final class AutoDeliverModule extends Module {
   private static AutoDeliverModule autoDeliverModule;
   public NumericTextSetting goldenKey;
   public NumericTextSetting pricePer1M;
   public LegacyNumberSetting legacyNumberSetting;
   public NumericTextSetting anarchyDelivery;
   private static final Pattern pattern = Pattern.compile("data-app-data=\"([^\"]*)\"");
   private static final Pattern pattern2 = Pattern.compile("<form[^>]*action=\"([^\"]*)\"");
   private static final Pattern pattern3 = Pattern.compile("<input[^>]*>");
   private static final Pattern pattern4 = Pattern.compile("(\\d+)\\s*шт\\.");
   private static final Pattern pattern5 = Pattern.compile("Анархия-(\\d+)");
   private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).followRedirects(Redirect.NORMAL).build();
   private ScheduledExecutorService scheduledExecutorService;
   private String strVal;
   private String strVal2;
   private final HashMap<String, String> map = new HashMap<>();
   private String strVal3 = Integer.toHexString((int)(System.nanoTime() & 2147483647L));
   private ArrayList<AutoDeliverModule.DeliveryWorker> list;
   private ArrayList<AutoDeliverModule.DeliveryOrder> list2;
   private final ArrayList<String> list3 = new ArrayList<>();
   private final ConcurrentLinkedQueue<AutoDeliverModule.DeliveryConfig> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();
   private long timeMillis;
   private long timeMillis2;
   private boolean flag;
   private final EventListener<PlayerTickEvent> playerTickListener = playerTickEvent -> {
      if (mc.player != null && mc.world != null && System.currentTimeMillis() - this.timeMillis >= 300L) {
         AutoDeliverModule.DeliveryConfig local6233_2 = this.concurrentLinkedQueue.peek();
         if (local6233_2 != null) {
            this.flag = false;
            if (local6233_2.flag6) {
               if (mc.currentScreen != null
                  && mc.player.currentScreenHandler instanceof GenericContainerScreenHandler genericContainerScreenHandler
                  && mc.currentScreen.getTitle().getString().contains("Кикнуть")) {
                  Inventory inventory = genericContainerScreenHandler.getInventory();

                  for (int index = 0; index < inventory.size(); index++) {
                     if (inventory.getStack(index).getName().getString().contains("Подтвердить")) {
                        InventoryUtils.sendSlotClickPacket(index, 0, SlotActionType.PICKUP, true);
                        mc.player.closeHandledScreen();
                        this.concurrentLinkedQueue.poll();
                        this.timeMillis = System.currentTimeMillis();
                        if (this.scheduledExecutorService != null) {
                           this.scheduledExecutorService
                              .execute(
                                 () -> this.processData(
                                    local6233_2.strVal5,
                                    "✅ Готово, валюта у вас — приятной игры!\n\n\ud83d\ude4f Подтвердите заказ и оставьте отзыв, мне это очень поможет:\n\ud83d\udd17 https://funpay.com/orders/"
                                       + (local6233_2.strVal6.startsWith("#") ? local6233_2.strVal6.substring(1) : local6233_2.strVal6)
                                       + "/\n\n\ud83d\udc9b Спасибо за покупку!"
                                 )
                              );
                        }

                        return;
                     }
                  }
               }

               if (System.currentTimeMillis() - local6233_2.timeMillis5 >= 1500L) {
                  local6233_2.timeMillis5 = System.currentTimeMillis();
                  this.timeMillis = System.currentTimeMillis();
                  mc.player.networkHandler.sendChatCommand("clan kick " + local6233_2.strVal4);
               }
            } else if (this.computeInt2() != this.computeInt()) {
               if (System.currentTimeMillis() - this.timeMillis2 >= 5000L) {
                  this.timeMillis2 = System.currentTimeMillis();
                  this.timeMillis = System.currentTimeMillis();
                  mc.player.networkHandler.sendChatCommand(this.anarchyDelivery.getText().replace("/", ""));
               }
            } else if (!local6233_2.flag2) {
               if (mc.getNetworkHandler() != null && mc.getNetworkHandler().getPlayerListEntry(local6233_2.strVal4) != null) {
                  if (System.currentTimeMillis() - local6233_2.timeMillis3 >= 2500L) {
                     local6233_2.timeMillis3 = System.currentTimeMillis();
                     this.timeMillis = System.currentTimeMillis();
                     mc.player.networkHandler.sendChatCommand("clan invite " + local6233_2.strVal4);
                  }
               }
            } else if (!local6233_2.flag3) {
               local6233_2.flag3 = true;
               this.timeMillis = System.currentTimeMillis();
               mc.player.networkHandler.sendChatCommand("clan invest " + local6233_2.intVal * 1000000L);
            } else if (local6233_2.flag4) {
               if (!local6233_2.flag5) {
                  local6233_2.flag5 = true;
                  if (this.scheduledExecutorService != null) {
                     this.scheduledExecutorService
                        .execute(
                           () -> this.processData(
                              local6233_2.strVal5,
                              "\ud83d\udcb0 Права выданы, ваши "
                                 + local6233_2.intVal
                                 + " кк уже лежат в казне клана!\n\n\ud83d\udce5 Напишите в игровом чате: /clan withdraw all\n\ud83d\udcb5 Деньги упадут вам сразу же\n\n⚡ После вывода я выкину вас из клана — так и задумано"
                           )
                        );
                  }
               }
            } else {
               if (mc.currentScreen != null && mc.player.currentScreenHandler instanceof GenericContainerScreenHandler genericContainerScreenHandler2) {
                  Inventory inventory2 = genericContainerScreenHandler2.getInventory();
                  int local6233_5 = -1;
                  int local6233_6 = -1;

                  for (int index2 = 0; index2 < inventory2.size(); index2++) {
                     String string = inventory2.getStack(index2).getName().getString();
                     if (local6233_5 == -1 && string.toLowerCase().contains(local6233_2.strVal4.toLowerCase())) {
                        local6233_5 = index2;
                     }

                     if (local6233_6 == -1 && string.contains("Список участников")) {
                        local6233_6 = index2;
                     }
                  }

                  if (local6233_5 != -1) {
                     InventoryUtils.sendSlotClickPacket(local6233_5, 0, SlotActionType.QUICK_MOVE, true);
                     mc.player.closeHandledScreen();
                     local6233_2.flag4 = true;
                     this.timeMillis = System.currentTimeMillis();
                     return;
                  }

                  if (local6233_6 != -1) {
                     InventoryUtils.sendSlotClickPacket(local6233_6, 0, SlotActionType.PICKUP, true);
                     this.timeMillis = System.currentTimeMillis();
                     return;
                  }
               }

               if (System.currentTimeMillis() - local6233_2.timeMillis4 >= 1500L) {
                  local6233_2.timeMillis4 = System.currentTimeMillis();
                  this.timeMillis = System.currentTimeMillis();
                  mc.player.networkHandler.sendChatCommand("clan menu");
               }
            }
         } else if (!this.flag) {
            this.flag = true;
            this.timeMillis = System.currentTimeMillis();
            mc.player.networkHandler.sendChatCommand("hub");
         }
      }
   };
   private final EventListener<PacketEvent> packetListener = packetEvent -> {
      if (packetEvent.isIncoming() && packetEvent.getPacket() instanceof GameMessageS2CPacket gameMessageS2CPacket) {
         String var6 = gameMessageS2CPacket.content().getString().replaceAll("(?i)§[0-9a-fk-or]", "").toLowerCase();

         for (AutoDeliverModule.DeliveryConfig local6245_5 : this.concurrentLinkedQueue) {
            if (local6245_5.strVal4 != null && var6.contains(local6245_5.strVal4.toLowerCase())) {
               if (var6.contains("присоединился к клану")) {
                  local6245_5.flag2 = true;
               } else if (var6.contains("из казны клана")) {
                  local6245_5.flag6 = true;
               }
            }
         }
      }
   };

   private void processData(String local6218_1, String local6218_2) {
      if (local6218_1 != null && !local6218_1.isEmpty()) {
         try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("node", local6218_1);
            jsonObject.addProperty("last_message", -1);
            jsonObject.addProperty("content", local6218_2);
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.addProperty("action", "chat_message");
            jsonObject2.add("data", jsonObject);
            JsonObject jsonObject3 = new JsonObject();
            jsonObject3.addProperty("type", "chat_node");
            jsonObject3.addProperty("id", local6218_1);
            jsonObject3.addProperty("tag", "00000000");
            JsonObject jsonObject4 = new JsonObject();
            jsonObject4.addProperty("node", local6218_1);
            jsonObject4.addProperty("last_message", -1);
            jsonObject4.addProperty("content", "");
            jsonObject3.add("data", jsonObject4);
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(jsonObject3);
            if (this.matchesCriteria(this.getHttpResponse(jsonArray.toString(), jsonObject2.toString()))) {
               return;
            }

            this.strVal = null;
            if (this.checkState()) {
               this.getHttpResponse(jsonArray.toString(), jsonObject2.toString());
            }
         } catch (Exception exception) {
         }
      }
   }

   private boolean matchesCriteria(HttpResponse<String> local6219_1) {
      if (local6219_1.statusCode() != 200) {
         return false;
      }

      try {
         JsonObject asJsonObject = JsonParser.parseString(local6219_1.body()).getAsJsonObject();
         if (asJsonObject.has("error") && !asJsonObject.get("error").isJsonNull()) {
            return false;
         } else {
            return asJsonObject.has("response") && asJsonObject.get("response").isJsonObject()
               ? !asJsonObject.getAsJsonObject("response").has("error") || asJsonObject.getAsJsonObject("response").get("error").isJsonNull()
               : true;
         }
      } catch (Exception exception) {
         return false;
      }
   }

   private boolean matchesCriteria2(double local6220_1) {
      if (this.pricePer1M.getText().isBlank()) {
         return true;
      }

      try {
         double local6220_3 = Double.parseDouble(this.pricePer1M.getText().replace(',', '.').replaceAll("[^0-9.]", ""));
         return local6220_3 <= 0.0 || Math.abs(local6220_1 - local6220_3) <= local6220_3 * 0.25;
      } catch (Exception exception) {
         return true;
      }
   }

   private String formatText(String local6222_1, String local6222_2) {
      Matcher matcher = Pattern.compile(local6222_2 + "=\"([^\"]*)\"").matcher(local6222_1);
      return matcher.find() ? matcher.group(1) : "";
   }

   private String formatText2() {
      StringBuilder stringBuilder = new StringBuilder("golden_key=").append(this.goldenKey.getText());

      for (String local6223_3 : this.map.keySet()) {
         stringBuilder.append("; ").append(local6223_3).append('=').append(this.map.get(local6223_3));
      }

      return stringBuilder.toString();
   }

   @Override
   protected void onEnable() {
      if (this.goldenKey.getText().isBlank()) {
         ClientChat.processData8("Укажите Golden-Key в настройках AutoDeliver");
         this.toggle();
      } else {
         this.strVal = null;
         this.list = null;
         this.list2 = null;
         this.list3.clear();
         this.concurrentLinkedQueue.clear();
         this.flag = false;
         this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "horus-funpay-deliver");
            thread.setDaemon(true);
            return thread;
         });
         this.scheduledExecutorService.scheduleWithFixedDelay(this::updateState, 0L, 4L, TimeUnit.SECONDS);
      }
   }

   private boolean checkState() {
      if (this.strVal != null) {
         return true;
      }

      try {
         this.map.clear();
         HttpResponse<String> local6225_1 = this.httpClient
            .send(
               HttpRequest.newBuilder(URI.create("https://funpay.com/"))
                  .timeout(Duration.ofSeconds(5L))
                  .header("cookie", "golden_key=" + this.goldenKey.getText())
                  .GET()
                  .build(),
               BodyHandlers.ofString()
            );
         if (this.formatText4(local6225_1.body(), "user-link-name").isEmpty()) {
            return false;
         }

         Matcher matcher = pattern.matcher(local6225_1.body());
         if (!matcher.find()) {
            return false;
         }

         JsonObject asJsonObject = JsonParser.parseString(this.formatText3(matcher.group(1))).getAsJsonObject();
         this.strVal = asJsonObject.get("userId").getAsString();
         this.strVal2 = asJsonObject.get("csrf-token").getAsString();
         this.processData2(local6225_1);
         this.list = null;
         return true;
      } catch (Exception exception) {
         return false;
      }
   }

   private void updateState() {
      try {
         if (!this.checkState()) {
            return;
         }

         this.updateState2();
         this.updateState3();
      } catch (Exception exception) {
         this.strVal = null;
      }
   }

   @Override
   protected void onDisable() {
      if (this.scheduledExecutorService != null) {
         this.scheduledExecutorService.shutdownNow();
      }

      this.scheduledExecutorService = null;
      this.concurrentLinkedQueue.clear();
   }

   private ArrayList<AutoDeliverModule.DeliveryOrder> getArrayList() {
      ArrayList<AutoDeliverModule.DeliveryOrder> local6231_1 = new ArrayList<>();

      try {
         HttpResponse<String> local6231_2 = this.httpClient
            .send(
               HttpRequest.newBuilder(URI.create("https://funpay.com/orders/trade"))
                  .timeout(Duration.ofSeconds(5L))
                  .header("cookie", this.formatText2())
                  .header("accept", "*/*")
                  .header("x-requested-with", "XMLHttpRequest")
                  .POST(BodyPublishers.noBody())
                  .build(),
               BodyHandlers.ofString()
            );
         this.processData2(local6231_2);
         String local6231_3 = local6231_2.body();

         for (String local6231_5 : this.getArrayList3(local6231_3, "tc-item")) {
            try {
               String local6231_6 = local6231_5.contains("order-desc") ? local6231_5.substring(local6231_5.indexOf("order-desc")) : "";
               if (this.formatText4(local6231_5, "tc-status").equalsIgnoreCase("Оплачен") && this.formatText4(local6231_6, "text-muted").contains("Валюта")) {
                  String local6231_7 = this.formatText4(local6231_5, "order-desc");
                  int local6231_8 = 1;
                  Matcher matcher = pattern4.matcher(local6231_7);

                  while (matcher.find()) {
                     local6231_8 = Integer.parseInt(matcher.group(1));
                  }

                  double local6231_10 = Double.parseDouble(this.formatText4(local6231_5, "tc-price").replace(',', '.').replaceAll("[^0-9.]", ""));
                  if (this.matchesCriteria2(local6231_10 / local6231_8)) {
                     String local6231_12 = this.formatText4(local6231_5, "media-user-name");
                     if (!local6231_12.isEmpty()) {
                        local6231_1.add(new AutoDeliverModule.DeliveryOrder(this.formatText4(local6231_5, "tc-order"), local6231_12, local6231_10, local6231_8));
                     }
                  }
               }
            } catch (Exception exception) {
            }
         }

         return local6231_1;
      } catch (Exception exception2) {
         return null;
      }
   }

   private void processData2(HttpResponse<String> local6232_1) {
      for (String local6232_3 : local6232_1.headers().allValues("set-cookie")) {
         String local6232_4 = local6232_3.split(";", 2)[0].trim();
         if (local6232_4.indexOf(61) > 0 && !local6232_4.startsWith("golden_key=")) {
            this.map.put(local6232_4.substring(0, local6232_4.indexOf(61)), local6232_4.substring(local6232_4.indexOf(61) + 1));
         }
      }
   }

   private int computeInt() {
      String local6234_1 = this.anarchyDelivery.getText().replaceAll("\\D", "");
      return local6234_1.isEmpty() ? -1 : Integer.parseInt(local6234_1);
   }

   private ArrayList<AutoDeliverModule.DeliveryWorker> getArrayList2() throws Exception {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("type", "chat_bookmarks");
      jsonObject.addProperty("id", this.strVal);
      jsonObject.addProperty("tag", this.strVal3);
      jsonObject.addProperty("data", false);
      JsonArray jsonArray = new JsonArray();
      jsonArray.add(jsonObject);
      ArrayList<AutoDeliverModule.DeliveryWorker> local6235_3 = new ArrayList<>();

      for (JsonElement jsonElement : JsonParser.parseString(this.getHttpResponse(jsonArray.toString(), String.valueOf(false)).body())
         .getAsJsonObject()
         .getAsJsonArray("objects")) {
         JsonObject asJsonObject = jsonElement.getAsJsonObject();
         if ("chat_bookmarks".equalsIgnoreCase(asJsonObject.get("type").getAsString())) {
            this.strVal3 = asJsonObject.get("tag").getAsString();

            for (String local6235_8 : this.getArrayList3(asJsonObject.getAsJsonObject("data").get("html").getAsString(), "contact-item")) {
               AutoDeliverModule.DeliveryWorker local6235_9 = new AutoDeliverModule.DeliveryWorker();
               local6235_9.strVal7 = this.formatText(local6235_8, "data-id");
               local6235_9.timeMillis6 = this.formatText(local6235_8, "data-node-msg").isEmpty()
                  ? 0L
                  : Long.parseLong(this.formatText(local6235_8, "data-node-msg"));
               local6235_9.strVal8 = this.formatText4(local6235_8, "media-user-name");
               if (!local6235_9.strVal7.isEmpty() && !local6235_9.strVal8.isEmpty()) {
                  local6235_3.add(local6235_9);
               }
            }
         }
      }

      return local6235_3;
   }

   private ArrayList<String> getArrayList3(String local6236_1, String local6236_2) {
      ArrayList<String> local6236_3 = new ArrayList<>();
      Matcher matcher = this.getPattern(local6236_2).matcher(local6236_1);

      int local6236_5;
      for (local6236_5 = -1; matcher.find(); local6236_5 = Math.max(local6236_1.lastIndexOf(60, matcher.start()), 0)) {
         if (local6236_5 != -1) {
            local6236_3.add(local6236_1.substring(local6236_5, matcher.start()));
         }
      }

      if (local6236_5 != -1) {
         local6236_3.add(local6236_1.substring(local6236_5));
      }

      return local6236_3;
   }

   private String formatText3(String local6237_1) {
      return local6237_1.replace("&quot;", "\"")
         .replace("&#039;", "'")
         .replace("&apos;", "'")
         .replace("&lt;", "<")
         .replace("&gt;", ">")
         .replace("&nbsp;", " ")
         .replace(" ", " ")
         .replace("&amp;", "&");
   }

   private void processData3(String local6238_1) {
      try {
         HttpResponse<String> local6238_2 = this.httpClient
            .send(
               HttpRequest.newBuilder(URI.create("https://funpay.com/orders/" + (local6238_1.startsWith("#") ? local6238_1.substring(1) : local6238_1) + "/"))
                  .timeout(Duration.ofSeconds(5L))
                  .header("cookie", this.formatText2())
                  .GET()
                  .build(),
               BodyHandlers.ofString()
            );
         this.processData2(local6238_2);
         String local6238_3 = local6238_2.body();
         if (!local6238_3.contains("modal-refund")) {
            return;
         }

         String local6238_4 = local6238_3.substring(local6238_3.indexOf("modal-refund"));
         Matcher matcher = pattern2.matcher(local6238_4);
         if (!matcher.find()) {
            return;
         }

         String local6238_6 = local6238_4.contains("</form>") ? local6238_4.substring(0, local6238_4.indexOf("</form>")) : local6238_4;
         StringBuilder stringBuilder = new StringBuilder();
         Matcher matcher2 = pattern3.matcher(local6238_6);

         while (matcher2.find()) {
            if (matcher2.group().contains("type=\"hidden\"")) {
               stringBuilder.append(URLEncoder.encode(this.formatText(matcher2.group(), "name"), StandardCharsets.UTF_8))
                  .append('=')
                  .append(URLEncoder.encode(this.formatText(matcher2.group(), "value"), StandardCharsets.UTF_8))
                  .append('&');
            }
         }

         stringBuilder.append("csrf_token=").append(URLEncoder.encode(this.strVal2 == null ? "" : this.strVal2, StandardCharsets.UTF_8));
         this.httpClient
            .send(
               HttpRequest.newBuilder(URI.create(matcher.group(1).startsWith("http") ? matcher.group(1) : "https://funpay.com" + matcher.group(1)))
                  .timeout(Duration.ofSeconds(5L))
                  .header("cookie", this.formatText2())
                  .header("accept", "*/*")
                  .header("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                  .header("x-requested-with", "XMLHttpRequest")
                  .POST(BodyPublishers.ofString(stringBuilder.toString(), StandardCharsets.UTF_8))
                  .build(),
               BodyHandlers.ofString()
            );
      } catch (Exception exception) {
      }
   }

   private int computeInt2() {
      if (mc.world == null) {
         return -1;
      }

      ScoreboardObjective objectiveForSlot = mc.world.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
      if (objectiveForSlot == null) {
         return -1;
      }

      Matcher matcher = pattern5.matcher(objectiveForSlot.getDisplayName().getString().replaceAll("(?i)§[0-9a-fk-or]", ""));
      return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
   }

   private void updateState2() throws Exception {
      ArrayList<AutoDeliverModule.DeliveryWorker> local6241_1 = this.getArrayList2();
      if (!local6241_1.isEmpty()) {
         ArrayList<AutoDeliverModule.DeliveryWorker> local6241_2 = this.list;
         this.list = local6241_1;
         if (local6241_2 != null) {
            for (AutoDeliverModule.DeliveryWorker local6241_4 : local6241_1) {
               AutoDeliverModule.DeliveryWorker local6241_5 = local6241_2.stream()
                  .filter(local6228_1 -> local6228_1.strVal7.equals(local6241_4.strVal7))
                  .findFirst()
                  .orElse(null);
               long local6241_6 = local6241_5 != null ? local6241_5.timeMillis6 : 0L;
               if (local6241_5 != null) {
                  local6241_4.nested0507 = local6241_5.nested0507;
                  local6241_4.strVal9 = local6241_5.strVal9;
                  local6241_4.timeMillis7 = local6241_5.timeMillis7;
               }

               if (local6241_4.timeMillis6 > local6241_6) {
                  JsonObject jsonObject = new JsonObject();
                  jsonObject.addProperty("type", "chat_node");
                  jsonObject.addProperty("id", local6241_4.strVal7);
                  jsonObject.addProperty("tag", "00000000");
                  JsonObject jsonObject2 = new JsonObject();
                  jsonObject2.addProperty("node", local6241_4.strVal7);
                  jsonObject2.addProperty("last_message", -1);
                  jsonObject2.addProperty("content", "");
                  jsonObject.add("data", jsonObject2);
                  JsonArray jsonArray = new JsonArray();
                  jsonArray.add(jsonObject);

                  for (JsonElement jsonElement : JsonParser.parseString(this.getHttpResponse(jsonArray.toString(), String.valueOf(false)).body())
                     .getAsJsonObject()
                     .getAsJsonArray("objects")) {
                     JsonObject asJsonObject = jsonElement.getAsJsonObject();
                     if ("chat_node".equalsIgnoreCase(asJsonObject.get("type").getAsString())) {
                        for (JsonElement jsonElement2 : asJsonObject.getAsJsonObject("data").getAsJsonArray("messages")) {
                           JsonObject asJsonObject2 = jsonElement2.getAsJsonObject();
                           if (asJsonObject2.get("id").getAsLong() > local6241_6
                              && !asJsonObject2.get("author").getAsString().equals(this.strVal)
                              && !asJsonObject2.get("author").getAsString().equals("0")) {
                              String local6241_17 = this.formatText4(asJsonObject2.get("html").getAsString(), "chat-msg-text");
                              String local6241_18 = local6241_17.replaceAll("[^a-zA-Zа-яА-Я]", "").toLowerCase();
                              AutoDeliverModule.DeliveryOrder local6241_19 = this.getNested0505(local6241_4.strVal8);
                              if (local6241_19 == null
                                 || local6241_4.nested0507 != AutoDeliverModule.TypeOption.NICKNAME
                                 || !local6241_18.equals("отмена") && !local6241_18.equals("otmena")) {
                                 if (local6241_4.nested0507 == AutoDeliverModule.TypeOption.NICKNAME) {
                                    local6241_4.strVal9 = local6241_17.replaceAll("[^a-zA-Z0-9_]", "");
                                    local6241_4.nested0507 = AutoDeliverModule.TypeOption.CONFIRM_NICKNAME;
                                    this.processData(
                                       local6241_4.strVal7,
                                       "\ud83d\udd0e Проверьте данные перед выдачей\n\n\ud83d\udc64 Ник: "
                                          + local6241_4.strVal9
                                          + "\n\ud83d\udce6 Сумма: "
                                          + (local6241_19 != null ? local6241_19.units() : 0)
                                          + " кк\n\n✅ Всё верно — напишите «Да» или «+»\n✏️ Ошибся в нике — напишите «Нет» или «-»"
                                    );
                                 } else if (local6241_4.nested0507 == AutoDeliverModule.TypeOption.CONFIRM_NICKNAME) {
                                    if (!local6241_18.equals("да") && !local6241_18.equals("da") && !local6241_17.contains("+") && !local6241_17.contains("✓")) {
                                       if (local6241_18.equals("нет") || local6241_18.equals("net") || local6241_17.contains("-") || local6241_17.contains("✗")
                                          )
                                        {
                                          local6241_4.nested0507 = AutoDeliverModule.TypeOption.NICKNAME;
                                          this.processData(local6241_4.strVal7, "✏️ Хорошо, напишите ваш игровой ник ещё раз одним сообщением");
                                       }
                                    } else if (local6241_19 != null && local6241_19.units() > 0) {
                                       AutoDeliverModule.DeliveryConfig local6241_20 = new AutoDeliverModule.DeliveryConfig();
                                       local6241_20.strVal4 = local6241_4.strVal9;
                                       local6241_20.strVal5 = local6241_4.strVal7;
                                       local6241_20.strVal6 = local6241_19.id();
                                       local6241_20.intVal = local6241_19.units();
                                       this.concurrentLinkedQueue.add(local6241_20);
                                       this.processData(
                                          local6241_4.strVal7,
                                          "\ud83d\ude80 Данные приняты, начинаю выдачу!\n\n1️⃣ Зайдите на Анархию-"
                                             + this.computeInt()
                                             + "\n2️⃣ Примите приглашение в клан — оно уже летит вам\n3️⃣ Дождитесь моего сообщения и напишите /clan withdraw all\n\n\ud83d\udce6 К выдаче: "
                                             + local6241_19.units()
                                             + " кк"
                                       );
                                       local6241_4.nested0507 = AutoDeliverModule.TypeOption.DELIVERY;
                                       local6241_4.timeMillis7 = System.currentTimeMillis();
                                    } else {
                                       this.processData(local6241_4.strVal7, "⏳ Не вижу ваш оплаченный заказ, напишите мне через минуту — выдам сразу же!");
                                    }
                                 }
                              } else {
                                 local6241_4.nested0507 = null;
                                 this.processData3(local6241_19.id());
                                 this.processData(
                                    local6241_4.strVal7,
                                    "↩️ Заказ отменён, средства вернулись на ваш баланс FunPay\n\n\ud83d\udc9b Будем рады видеть вас снова!"
                                 );
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private AutoDeliverModule() {
      this.goldenKey = this.addSetting(new NumericTextSetting("Golden-Key", "Ключ сессии FunPay из cookie golden_key.", "", 64));
      this.pricePer1M = this.addSetting(
         new NumericTextSetting("Цена за 1кк", "Своя цена лота без комиссии FunPay, пусто - обслуживать любые заказы на валюту.", "", 16)
      );
      this.legacyNumberSetting = this.addSetting(
         new LegacyNumberSetting("Продавать при сумме от ляма", "Заказы меньше этой суммы бот возвращает покупателю.", 10.0, 1.0, 50.0, 1.0)
      );
      this.anarchyDelivery = this.addSetting(new NumericTextSetting("Анархия выдачи", "Куда бот летит выдавать и куда зовёт покупателя.", "an305", 16));
   }

   private Pattern getPattern(String local6247_1) {
      return Pattern.compile("class=\"[^\"]*(?<![-\\w])" + local6247_1 + "(?![-\\w])[^\"]*\"");
   }

   private String formatText4(String local6249_1, String local6249_2) {
      Matcher matcher = this.getPattern(local6249_2).matcher(local6249_1);
      if (!matcher.find()) {
         return "";
      }

      int local6249_4 = local6249_1.indexOf(62, matcher.end());

      while (local6249_4 != -1) {
         int local6249_5 = local6249_1.indexOf(60, local6249_4 + 1);
         String local6249_6 = this.formatText3(local6249_5 == -1 ? local6249_1.substring(local6249_4 + 1) : local6249_1.substring(local6249_4 + 1, local6249_5))
            .trim();
         if (!local6249_6.isEmpty()) {
            return local6249_6;
         }

         if (local6249_5 == -1) {
            return "";
         }

         local6249_4 = local6249_1.indexOf(62, local6249_5);
      }

      return "";
   }

   private HttpResponse<String> getHttpResponse(String local6251_1, String local6251_2) throws Exception {
      String local6251_3 = "objects="
         + URLEncoder.encode(local6251_1 == null ? "" : local6251_1, StandardCharsets.UTF_8)
         + "&request="
         + URLEncoder.encode(local6251_2 == null ? "" : local6251_2, StandardCharsets.UTF_8)
         + "&csrf_token="
         + URLEncoder.encode(this.strVal2 == null ? "" : this.strVal2, StandardCharsets.UTF_8);
      HttpResponse<String> local6251_4 = this.httpClient
         .send(
            HttpRequest.newBuilder(URI.create("https://funpay.com/runner/"))
               .timeout(Duration.ofSeconds(5L))
               .header("cookie", this.formatText2())
               .header("accept", "*/*")
               .header("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
               .header("x-requested-with", "XMLHttpRequest")
               .POST(BodyPublishers.ofString(local6251_3, StandardCharsets.UTF_8))
               .build(),
            BodyHandlers.ofString()
         );
      this.processData2(local6251_4);
      return local6251_4;
   }

   private void updateState3() {
      ArrayList<AutoDeliverModule.DeliveryOrder> local6253_1 = this.getArrayList();
      if (local6253_1 != null) {
         ArrayList<AutoDeliverModule.DeliveryOrder> local6253_2 = this.list2;
         this.list2 = local6253_1;
         if (local6253_2 != null) {
            if (this.list != null) {
               for (AutoDeliverModule.DeliveryOrder local6253_4 : local6253_2) {
                  if (!local6253_1.stream().anyMatch(local6242_1 -> local6242_1.id().equals(local6253_4.id()))) {
                     this.list
                        .stream()
                        .filter(local6230_1 -> local6230_1.strVal8.equalsIgnoreCase(local6253_4.buyerLogin()))
                        .filter(local6254_0 -> local6254_0.nested0507 != null)
                        .findFirst()
                        .ifPresent(local6240_0 -> local6240_0.nested0507 = null);
                  }
               }

               for (AutoDeliverModule.DeliveryOrder local6253_4 : local6253_1) {
                  if (!this.list3.contains(local6253_4.id())) {
                     this.list3.add(local6253_4.id());
                     this.list
                        .stream()
                        .filter(local6221_1 -> local6221_1.strVal8.equalsIgnoreCase(local6253_4.buyerLogin()))
                        .filter(local6250_0 -> local6250_0.nested0507 == null)
                        .findFirst()
                        .ifPresent(
                           local6227_2 -> {
                              if (local6253_4.units() < this.legacyNumberSetting.getValue()) {
                                 this.processData3(local6253_4.id());
                                 this.processData(
                                    local6227_2.strVal7,
                                    "❌ Минимальный заказ — "
                                       + this.legacyNumberSetting.intValue()
                                       + " кк, ваш меньше\n\n\ud83d\udcb8 Средства уже вернулись на ваш баланс FunPay, извините за неудобство!"
                                 );
                              } else {
                                 this.processData(
                                    local6227_2.strVal7,
                                    "\ud83d\udc4b Привет! На связи автовыдача валюты — horus client\n\n\ud83d\udce6 Ваш заказ: "
                                       + local6253_4.units()
                                       + " кк\n✏️ Напишите ваш игровой ник одним сообщением\n\nℹ️ Передумали? Напишите «Отмена» — сразу вернём средства"
                                 );
                                 local6227_2.nested0507 = AutoDeliverModule.TypeOption.NICKNAME;
                              }
                           }
                        );
                  }
               }
            }
         } else {
            for (AutoDeliverModule.DeliveryOrder local6253_4 : local6253_1) {
               this.list3.add(local6253_4.id());
            }
         }
      }
   }

   public static AutoDeliverModule getInstance() {
      if (autoDeliverModule == null) {
         autoDeliverModule = new AutoDeliverModule();
      }

      return autoDeliverModule;
   }

   private AutoDeliverModule.DeliveryOrder getNested0505(String local6257_1) {
      return this.list2 == null
         ? null
         : this.list2.stream().filter(local6248_1 -> local6248_1.buyerLogin().equalsIgnoreCase(local6257_1)).findFirst().orElse(null);
   }

   private static final class DeliveryConfig {
      private String strVal4;
      private String strVal5;
      private String strVal6;
      private int intVal;
      private long timeMillis3;
      private long timeMillis4;
      private long timeMillis5;
      private boolean flag2;
      private boolean flag3;
      private boolean flag4;
      private boolean flag5;
      private boolean flag6;
   }

   private record DeliveryOrder(String id, String buyerLogin, double price, int units) {
   }

   private static final class DeliveryWorker {
      private String strVal7;
      private String strVal8;
      private String strVal9;
      private long timeMillis6;
      private AutoDeliverModule.TypeOption nested0507;
      private long timeMillis7;
   }

   private enum TypeOption {
      NICKNAME,
      CONFIRM_NICKNAME,
      DELIVERY;
   }
}

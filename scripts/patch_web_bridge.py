path = r'C:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\web\WebBridgeServer.java'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Imports
imp_target = 'import net.minecraft.client.MinecraftClient;'
imp_replacement = '''import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Util;
import java.io.File;'''

content = content.replace(imp_target, imp_replacement, 1)

# 2. Add contexts in start()
ctx_target = '         server.createContext("/api/ping", new PingHandler());'
ctx_replacement = '''         server.createContext("/api/ping", new PingHandler());
         server.createContext("/api/action/screenshot", new ScreenshotHandler());
         server.createContext("/api/action/open-logs", new OpenLogsHandler());
         server.createContext("/api/action/reload-config", new ReloadConfigHandler());
         server.createContext("/api/action/disconnect", new DisconnectHandler());'''

content = content.replace(ctx_target, ctx_replacement, 1)

# 3. StatusHandler live server stats
status_target = '         root.addProperty("version", "2.0");'
status_replacement = '''         root.addProperty("version", "2.0");

         String serverIp = "В главном меню";
         int fps = 0;
         int ping = 0;
         try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) {
               fps = mc.getCurrentFps();
               if (mc.getCurrentServerEntry() != null) {
                  serverIp = mc.getCurrentServerEntry().address;
               } else if (mc.isInSingleplayer()) {
                  serverIp = "Одиночная игра";
               }
               if (mc.getNetworkHandler() != null && mc.player != null) {
                  PlayerListEntry ple = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                  if (ple != null) {
                     ping = ple.getLatency();
                  }
               }
            }
         } catch (Exception ignored) {
         }
         root.addProperty("server", serverIp);
         root.addProperty("fps", fps);
         root.addProperty("ping", ping);'''

content = content.replace(status_target, status_replacement, 1)

# 4. Action Handlers
handlers_code = '''   private static class ScreenshotHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;
         try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) {
               mc.execute(() -> {
                  ScreenshotRecorder.saveScreenshot(mc.runDirectory, mc.getFramebuffer(), msg -> {
                     if (mc.inGameHud != null && mc.inGameHud.getChatHud() != null) {
                        mc.inGameHud.getChatHud().addMessage(msg);
                     }
                  });
               });
               sendJsonResponse(exchange, 200, "{\\"success\\": true, \\"message\\": \\"Скриншот сделан\\"}");
               return;
            }
         } catch (Exception e) {
            sendJsonResponse(exchange, 500, "{\\"success\\": false, \\"error\\": \\"" + e.getMessage() + "\\"}");
            return;
         }
         sendJsonResponse(exchange, 400, "{\\"success\\": false, \\"error\\": \\"Клиент недоступен\\"}");
      }
   }

   private static class OpenLogsHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;
         try {
            MinecraftClient mc = MinecraftClient.getInstance();
            File logs = new File(mc != null ? mc.runDirectory : new File("."), "logs");
            if (!logs.exists()) logs.mkdirs();
            Util.getOperatingSystem().open(logs);
            sendJsonResponse(exchange, 200, "{\\"success\\": true, \\"message\\": \\"Папка логов открыта\\"}");
         } catch (Exception e) {
            sendJsonResponse(exchange, 500, "{\\"success\\": false, \\"error\\": \\"" + e.getMessage() + "\\"}");
         }
      }
   }

   private static class ReloadConfigHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;
         try {
            String active = ConfigStore.getActiveConfig();
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) {
               mc.execute(() -> ConfigStore.load(active));
            } else {
               ConfigStore.load(active);
            }
            sendJsonResponse(exchange, 200, "{\\"success\\": true, \\"config\\": \\"" + active + "\\", \\"message\\": \\"Конфиг успешно перезагружен\\"}");
         } catch (Exception e) {
            sendJsonResponse(exchange, 500, "{\\"success\\": false, \\"error\\": \\"" + e.getMessage() + "\\"}");
         }
      }
   }

   private static class DisconnectHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;
         try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) {
               mc.execute(() -> {
                  if (mc.world != null) {
                     mc.disconnect(new TitleScreen(), false);
                  }
               });
               sendJsonResponse(exchange, 200, "{\\"success\\": true, \\"message\\": \\"Дисконнект выполнен\\"}");
               return;
            }
         } catch (Exception e) {
            sendJsonResponse(exchange, 500, "{\\"success\\": false, \\"error\\": \\"" + e.getMessage() + "\\"}");
            return;
         }
         sendJsonResponse(exchange, 400, "{\\"success\\": false, \\"error\\": \\"Клиент недоступен\\"}");
      }
   }
'''

last_brace_idx = content.rfind('}')
content = content[:last_brace_idx] + handlers_code + '\n}\n'

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print("WebBridgeServer.java successfully updated!")

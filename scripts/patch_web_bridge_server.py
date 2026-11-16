# -*- coding: utf-8 -*-
from pathlib import Path

target_file = Path(r"C:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\web\WebBridgeServer.java")
with open(target_file, "r", encoding="utf-8") as f:
    text = f.read()

start_marker = "   private static class StaticWebHandler implements HttpHandler {"
end_marker = "      private String getMimeType(String name) {"

idx1 = text.find(start_marker)
idx2 = text.find(end_marker)

if idx1 == -1 or idx2 == -1:
    print(f"Error: markers not found (idx1={idx1}, idx2={idx2})")
    exit(1)

replacement = """   private static class StaticWebHandler implements HttpHandler {
      private final Path rootDir = findProjectRoot();

      private static Path findProjectRoot() {
         Path cur = Path.of("").toAbsolutePath();
         if (Files.exists(cur.resolve("launcher").resolve("index.html")) || Files.exists(cur.resolve("website").resolve("index.html"))) {
            return cur;
         }
         if (cur.getParent() != null && (Files.exists(cur.getParent().resolve("launcher").resolve("index.html")) || Files.exists(cur.getParent().resolve("website").resolve("index.html")))) {
            return cur.getParent();
         }
         try {
            Path gameDir = net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir();
            if (Files.exists(gameDir.resolve("launcher").resolve("index.html")) || Files.exists(gameDir.resolve("website").resolve("index.html"))) {
               return gameDir;
            }
            if (gameDir.getParent() != null && (Files.exists(gameDir.getParent().resolve("launcher").resolve("index.html")) || Files.exists(gameDir.getParent().resolve("website").resolve("index.html")))) {
               return gameDir.getParent();
            }
         } catch (Exception ignored) {}

         String home = System.getProperty("user.home", "");
         if (!home.isBlank()) {
            Path p1 = Path.of(home, "Downloads", "HorusClient");
            if (Files.exists(p1.resolve("launcher").resolve("index.html")) || Files.exists(p1.resolve("website").resolve("index.html"))) return p1;
            Path p2 = Path.of(home, "Downloads", "deluxe-source");
            if (Files.exists(p2.resolve("launcher").resolve("index.html")) || Files.exists(p2.resolve("website").resolve("index.html"))) return p2;
         }

         Path known = Path.of("C:\\\\Users\\\\Никита\\\\Downloads\\\\HorusClient");
         if (Files.exists(known.resolve("launcher").resolve("index.html"))) {
            return known;
         }
         return cur;
      }

      @Override
      public void handle(HttpExchange exchange) throws IOException {
         if (checkOptions(exchange)) return;

         String path = exchange.getRequestURI().getPath();
         if (path == null || path.isEmpty() || "/".equals(path) || "/index.html".equals(path)) {
            serveFile(exchange, rootDir.resolve("website").resolve("index.html"));
            return;
         }

         if (path.startsWith("/launcher")) {
            String sub = path.substring("/launcher".length());
            if (sub.isEmpty() || "/".equals(sub) || "/index.html".equals(sub)) {
               serveFile(exchange, rootDir.resolve("launcher").resolve("index.html"));
               return;
            }
            serveFile(exchange, rootDir.resolve("launcher").resolve(sub.startsWith("/") ? sub.substring(1) : sub));
            return;
         }

         if (path.startsWith("/website")) {
            String sub = path.substring("/website".length());
            if (sub.isEmpty() || "/".equals(sub) || "/index.html".equals(sub)) {
               serveFile(exchange, rootDir.resolve("website").resolve("index.html"));
               return;
            }
            serveFile(exchange, rootDir.resolve("website").resolve(sub.startsWith("/") ? sub.substring(1) : sub));
            return;
         }

         // Root level assets (e.g. /logo.png, /app_icon.png, /favicon.ico, /supabase-client.js)
         String clean = path.startsWith("/") ? path.substring(1) : path;
         Path candidate = rootDir.resolve("website").resolve(clean);
         if (Files.exists(candidate) && !Files.isDirectory(candidate)) {
            serveFile(exchange, candidate);
            return;
         }
         Path candidateLauncher = rootDir.resolve("launcher").resolve(clean);
         if (Files.exists(candidateLauncher) && !Files.isDirectory(candidateLauncher)) {
            serveFile(exchange, candidateLauncher);
            return;
         }

         serveFile(exchange, rootDir.resolve("launcher").resolve(clean));
      }

      private void serveFile(HttpExchange exchange, Path filePath) throws IOException {
         byte[] data = null;
         String name = filePath.getFileName() != null ? filePath.getFileName().toString() : "index.html";

         if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            try {
               data = Files.readAllBytes(filePath);
            } catch (Exception ignored) {}
         }

         if (data == null) {
            java.util.List<Path> alts = new ArrayList<>();
            String home = System.getProperty("user.home", "");
            if (!home.isBlank()) {
               alts.add(Path.of(home, "Downloads", "HorusClient", "launcher", name));
               alts.add(Path.of(home, "Downloads", "HorusClient", "run", "launcher", name));
               alts.add(Path.of(home, "Downloads", "HorusClient", "website", name));
               alts.add(Path.of(home, "Downloads", "deluxe-source", "launcher", name));
            }
            alts.add(Path.of("C:\\\\Users\\\\Никита\\\\Downloads\\\\HorusClient\\\\launcher", name));
            alts.add(Path.of("C:\\\\Users\\\\Никита\\\\Downloads\\\\HorusClient\\\\run\\\\launcher", name));
            alts.add(Path.of("C:\\\\Users\\\\Никита\\\\Downloads\\\\HorusClient\\\\website", name));

            for (Path alt : alts) {
               if (Files.exists(alt) && !Files.isDirectory(alt)) {
                  try {
                     data = Files.readAllBytes(alt);
                     break;
                  } catch (Exception ignored) {}
               }
            }
         }

         if (data == null) {
            String[] resourcePaths = new String[] {
               "/assets/horus/launcher/" + name,
               "/assets/horus/website/" + name,
               "/assets/horus/textures/gui/mainmenu/icons/" + name,
               "/assets/horus/" + name
            };
            for (String rPath : resourcePaths) {
               try (InputStream is = WebBridgeServer.class.getResourceAsStream(rPath)) {
                  if (is != null) {
                     data = is.readAllBytes();
                     break;
                  }
               } catch (Exception ignored) {}
            }
         }

         if (data == null) {
            String notFound = "{\\"error\\": \\"File not found\\", \\"file\\": \\"" + name + "\\"}";
            sendJsonResponse(exchange, 404, notFound);
            return;
         }

         String mime = getMimeType(name);
         handleCors(exchange);
         exchange.getResponseHeaders().set("Content-Type", mime);
         exchange.sendResponseHeaders(200, data.length);
         try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
         }
         exchange.close();
      }

"""

new_text = text[:idx1] + replacement + text[idx2:]
with open(target_file, "w", encoding="utf-8") as f:
    f.write(new_text)

print("WebBridgeServer.java patched successfully")

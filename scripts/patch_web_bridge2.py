from pathlib import Path

target_file = Path(r"C:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\web\WebBridgeServer.java")
text = target_file.read_text(encoding="utf-8")

old_serve = """      private void serveFile(HttpExchange exchange, Path filePath) throws IOException {
         if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            String notFound = "{\\"error\\": \\"File not found\\", \\"file\\": \\"" + filePath.getFileName() + "\\"}";
            sendJsonResponse(exchange, 404, notFound);
            return;
         }"""

new_serve = """      private void serveFile(HttpExchange exchange, Path filePath) throws IOException {
         if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            Path known = Path.of("C:\\\\Users\\\\Никита\\\\Downloads\\\\HorusClient");
            Path alt1 = known.resolve("launcher").resolve(filePath.getFileName().toString());
            Path alt2 = known.resolve("website").resolve(filePath.getFileName().toString());
            if (Files.exists(alt1) && !Files.isDirectory(alt1)) {
               filePath = alt1;
            } else if (Files.exists(alt2) && !Files.isDirectory(alt2)) {
               filePath = alt2;
            } else {
               String notFound = "{\\"error\\": \\"File not found\\", \\"file\\": \\"" + filePath.getFileName() + "\\"}";
               sendJsonResponse(exchange, 404, notFound);
               return;
            }
         }"""

if old_serve not in text:
    old_serve = old_serve.replace('\n', '\r\n')
    new_serve = new_serve.replace('\n', '\r\n')

if old_serve in text:
    text = text.replace(old_serve, new_serve, 1)
    target_file.write_text(text, encoding="utf-8")
    print("serveFile successfully patched!")
else:
    print("old_serve not found!")

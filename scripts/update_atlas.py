from pathlib import Path

target_file = Path(r"C:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\cosmetic\CosmeticPreviewAtlas.java")
with open(target_file, "r", encoding="utf-8") as f:
    text = f.read()

target1 = """      Path path = path3.resolve(local5947_0);
      Map<String, String> local5947_2 = getMap();
      if (local5947_2 == null) {
         if (Files.isRegularFile(path)) {
            return Files.readAllBytes(path);
         } else {
            throw new IOException("hosting unreachable and no cache for " + local5947_0);
         }
      } else {"""

replacement1 = """      Path path = path3.resolve(local5947_0);
      if (Files.isRegularFile(path)) {
         return Files.readAllBytes(path);
      }

      Map<String, String> local5947_2 = getMap();
      if (local5947_2 == null) {
         throw new IOException("hosting unreachable and no cache for " + local5947_0);
      } else {"""

assert target1 in text, "target1 not found"
text = text.replace(target1, replacement1, 1)

# Also shorten timeout in getMap
target2 = """            HttpRequest.newBuilder(URI.create("http://horusclient.ru/cosmetics/manifest.php")).timeout(Duration.ofSeconds(8L)).GET().build(),"""
replacement2 = """            HttpRequest.newBuilder(URI.create("http://horusclient.ru/cosmetics/manifest.php")).timeout(Duration.ofSeconds(2L)).GET().build(),"""

if target2 in text:
    text = text.replace(target2, replacement2, 1)

with open(target_file, "w", encoding="utf-8") as f:
    f.write(text)

print("Updated CosmeticPreviewAtlas.java successfully!")

from pathlib import Path

path = Path(r"C:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\ui\ProxyScreen.java")
with open(path, "r", encoding="utf-8") as f:
    text = f.read()

# 1. cardH
assert "float cardH = Math.min(screenH - 40.0F, 370.0F);" in text, "cardH not found"
text = text.replace("float cardH = Math.min(screenH - 40.0F, 370.0F);", "float cardH = Math.min(screenH - 40.0F, 280.0F);", 1)

# Normalize line endings
text_normalized = text.replace("\r\n", "\n")

# 2. Presets block
old_block = """      // Presets header
      FontText.processData3(FontFace.SEMI, "БЫСТРЫЕ ПРЕСЕТЫ:", rightColX, rCurY, 6.0F, 0x889CA3AF);
      rCurY += 10.0F;

      renderPresetButton("1. Local SOCKS5 (127.0.0.1:1080)", "127.0.0.1", 1080, ProxyManager.ProxyType.SOCKS5, rightColX, rCurY, rightColW, mx, my);
      rCurY += 26.0F;
      renderPresetButton("2. Tor SOCKS5 (127.0.0.1:9050)", "127.0.0.1", 9050, ProxyManager.ProxyType.SOCKS5, rightColX, rCurY, rightColW, mx, my);
      rCurY += 26.0F;
      renderPresetButton("3. HTTP Прокси (127.0.0.1:8080)", "127.0.0.1", 8080, ProxyManager.ProxyType.HTTP, rightColX, rCurY, rightColW, mx, my);
      rCurY += 34.0F;"""

old_block_norm = old_block.replace("\r\n", "\n")
assert old_block_norm in text_normalized, "old_block not found"
text_normalized = text_normalized.replace(old_block_norm, "")

# 3. renderPresetButton method
old_btn_method = """   private void renderPresetButton(String text, String host, int port, ProxyManager.ProxyType type, float x, float y, float w, float mx, float my) {
      boolean hov = mx >= x && mx <= x + w && my >= y && my <= y + 22.0F;
      Render2D.processData51(x, y, w, 22.0F, 6.0F, hov ? 0x26FFFFFF : 0x14FFFFFF);
      Render2D.processData86(x, y, w, 22.0F, 6.0F, 0.5F, 0x1AFFFFFF);
      FontText.processData3(FontFace.SEMI, text, x + 10.0F, y + 6.5F, 6.5F, hov ? 0xFFFFFFFF : 0xBBFFFFFF);
      this.clickAreas.add(new ClickArea(x, y, w, 22.0F, () -> {
         this.proxyManager.setHost(host);
         this.proxyManager.setPort(port);
         this.proxyManager.setType(type);
         if (this.proxyManager.isEnabled()) this.proxyManager.applyProxy();
         this.testResultText = "Загружен пресет: " + host + ":" + port;
      }));
   }"""

old_btn_norm = old_btn_method.replace("\r\n", "\n")
assert old_btn_norm in text_normalized, "old_btn_norm not found"
text_normalized = text_normalized.replace(old_btn_norm, "")

# 4. Add helpful info lines in right column
old_info = """      if (!this.testResultText.isEmpty()) {
         FontText.processData3(FontFace.SEMI, "Статус: " + this.testResultText, rightColX + 12.0F, rCurY + 54.0F, 6.5F, 0xFF93C5FD);
      } else {
         FontText.processData3(FontFace.SEMI, "Трафик: Minecraft TCP Sockets & Web", rightColX + 12.0F, rCurY + 54.0F, 6.5F, 0x77FFFFFF);
      }
      rCurY += 98.0F;"""

new_info = """      if (!this.testResultText.isEmpty()) {
         FontText.processData3(FontFace.SEMI, "Статус: " + this.testResultText, rightColX + 12.0F, rCurY + 54.0F, 6.5F, 0xFF93C5FD);
      } else {
         FontText.processData3(FontFace.SEMI, "Трафик: Minecraft TCP Sockets & Web", rightColX + 12.0F, rCurY + 54.0F, 6.5F, 0x77FFFFFF);
      }
      FontText.processData3(FontFace.SEMI, "Поддерживаются SOCKS5, SOCKS4 и HTTP.", rightColX + 12.0F, rCurY + 68.0F, 5.5F, 0x66FFFFFF);
      FontText.processData3(FontFace.SEMI, "Для авторизации укажите логин и пароль.", rightColX + 12.0F, rCurY + 78.0F, 5.5F, 0x66FFFFFF);
      rCurY += 98.0F;"""

old_info_norm = old_info.replace("\r\n", "\n")
new_info_norm = new_info.replace("\r\n", "\n")
assert old_info_norm in text_normalized, "old_info_norm not found"
text_normalized = text_normalized.replace(old_info_norm, new_info_norm)

with open(path, "w", encoding="utf-8") as f:
    f.write(text_normalized)

print("ProxyScreen.java updated successfully!")

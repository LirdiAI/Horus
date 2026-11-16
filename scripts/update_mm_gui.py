import pathlib

p = pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\ui\MainMenuScreen.java")
text = p.read_text(encoding="utf-8")

# 1. Add import for ClickGuiScreen if needed
if "import horus.client.ui.ClickGuiScreen;" not in text:
    text = text.replace("import horus.client.ui.ProxyScreen;", "import horus.client.ui.ProxyScreen;\nimport horus.client.ui.ClickGuiScreen;")

# 2. Add top-right FrameSync badge in drawChrome
fs_badge_code = '''      // Top-right FrameSync & ClickGUI pill
      float fsW = 125.0F * scale;
      float fsH = 18.0F * scale;
      float fsX = screenW - fsW - 10.0F * scale;
      float fsY = 10.0F * scale;
      boolean fsHov = interactive && UiTheme.matchesCriteria(mx, my, fsX, fsY, fsW, fsH);
      float fsHover = this.hoverPC("frameSyncBadge", fsHov);

      int fsBg = UiTheme.computeInt2(COLOR_BTN_DARK, COLOR_BTN_DARK_HOVER, fsHover);
      int fsBorder = UiTheme.computeInt2(COLOR_BTN_ACCENT_BORDER, COLOR_BTN_ACCENT_BORDER_HOVER, fsHover);
      Render2D.processData51(fsX, fsY, fsW, fsH, 5.0F * scale, fsBg);
      Render2D.processData86(fsX, fsY, fsW, fsH, 5.0F * scale, 0.85F * scale, fsBorder);

      Render2D.processData51(fsX + 6.0F * scale, fsY + 6.5F * scale, 5.0F * scale, 5.0F * scale, 2.5F * scale, 0xFF10B981);
      FontText.processData3(FontFace.BOLD, "⚡ FrameSync", fsX + 15.0F * scale, fsY + 5.5F * scale, 6.5F * scale, 0xFFFFFFFF);
      FontText.processData3(FontFace.SEMI, "ON (+200 FPS)", fsX + 68.0F * scale, fsY + 5.5F * scale, 6.0F * scale, 0xFFD8B4FE);

      if (interactive) {
         this.list.add(new MainMenuScreen.DataRecord(fsX, fsY, fsW, fsH, () -> {
            this.transitionTo(() -> {
               ClickGuiScreen gui = ClickGuiScreen.instance();
               gui.setParentScreen(this);
               MinecraftClient.getInstance().setScreen(gui);
            });
         }));
      }

      // Bottom centered policy disclaimer'''

text = text.replace("      // Bottom centered policy disclaimer", fs_badge_code)

# 3. Add "Модули (RShift)" link in drawCenterMenu
old_links = '''      // Bottom text links: Options | Proxies | Exit (with smooth exit transitions)
      float linksY = centerY + 82.0F * scale;
      String proxyText = horus.client.proxy.ProxyManager.getInstance().isEnabled() ? "Proxies (ON)" : "Proxies";
      this.drawTextLink("linkOptions", "Options", centerX - 80.0F * scale, linksY, scale, mx, my, interactive,
              () -> this.transitionTo(() -> minecraftClient.setScreen(new OptionsScreen(this, minecraftClient.options))));
      this.drawTextLink("linkProxies", proxyText, centerX, linksY, scale, mx, my, interactive,
              () -> this.transitionTo(() -> minecraftClient.setScreen(new ProxyScreen(this))));
      this.drawTextLink("linkExit", "Exit", centerX + 80.0F * scale, linksY, scale, mx, my, interactive,
              () -> this.transitionTo(minecraftClient::scheduleStop));'''

new_links = '''      // Bottom text links: Модули (RShift) | Options | Proxies | Exit (with smooth exit transitions)
      float linksY = centerY + 82.0F * scale;
      String proxyText = horus.client.proxy.ProxyManager.getInstance().isEnabled() ? "Proxies (ON)" : "Proxies";
      this.drawTextLink("linkMods", "Модули (RShift)", centerX - 120.0F * scale, linksY, scale, mx, my, interactive,
              () -> this.transitionTo(() -> {
                 ClickGuiScreen gui = ClickGuiScreen.instance();
                 gui.setParentScreen(this);
                 minecraftClient.setScreen(gui);
              }));
      this.drawTextLink("linkOptions", "Options", centerX - 40.0F * scale, linksY, scale, mx, my, interactive,
              () -> this.transitionTo(() -> minecraftClient.setScreen(new OptionsScreen(this, minecraftClient.options))));
      this.drawTextLink("linkProxies", proxyText, centerX + 40.0F * scale, linksY, scale, mx, my, interactive,
              () -> this.transitionTo(() -> minecraftClient.setScreen(new ProxyScreen(this))));
      this.drawTextLink("linkExit", "Exit", centerX + 120.0F * scale, linksY, scale, mx, my, interactive,
              () -> this.transitionTo(minecraftClient::scheduleStop));'''

text = text.replace(old_links, new_links)

# 4. In keyPressed, allow opening ClickGuiScreen with Right Shift (344)
kp_target = '''   @Override
   public boolean keyPressed(KeyInput keyInput) {'''

kp_replacement = '''   @Override
   public boolean keyPressed(KeyInput keyInput) {
      if (!this.flag && !this.flag2 && (keyInput.key() == 344 || keyInput.key() == 290)) { // Right Shift or F11/custom
         this.transitionTo(() -> {
            ClickGuiScreen gui = ClickGuiScreen.instance();
            gui.setParentScreen(this);
            MinecraftClient.getInstance().setScreen(gui);
         });
         return true;
      }'''

text = text.replace(kp_target, kp_replacement)

p.write_text(text, encoding="utf-8")
print("MainMenuScreen.java successfully updated with ClickGUI and FrameSync access!")

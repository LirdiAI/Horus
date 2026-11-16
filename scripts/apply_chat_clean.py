from pathlib import Path

# 1. Update ChatScreenMixin.java
chat_mixin_path = Path(r"C:\Users\Никита\Downloads\HorusClient\src\main\java\horus\mixin\ChatScreenMixin.java")
with open(chat_mixin_path, "r", encoding="utf-8") as f:
    text = f.read()

target_tabs_call = """         CustomChatModule.getInstance().renderChatTabs(
            context,
            textRenderer,
            fieldX,
            fieldY,
            this.chatField.getWidth(),
            mouseX,
            mouseY
         );"""

if target_tabs_call in text:
    text = text.replace(target_tabs_call, "", 1)
    with open(chat_mixin_path, "w", encoding="utf-8") as f:
        f.write(text)
    print("ChatScreenMixin.java updated: renderChatTabs call removed.")
else:
    print("ChatScreenMixin.java target not found or already removed.")

# 2. Update CustomChatModule.java
module_path = Path(r"C:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\module\render\CustomChatModule.java")
with open(module_path, "r", encoding="utf-8") as f:
    mod_text = f.read()

# Replace settings to not add them to GUI
target_tabs_setting = """public final BooleanSetting tabs = this.addSetting(
      new BooleanSetting("Вкладки чатов", "Интерактивные вкладки: Все чаты, Личные [ЛС], Клан, Staff, Сервер.", true)
   );"""

repl_tabs_setting = """public final BooleanSetting tabs = new BooleanSetting("Вкладки чатов", "Интерактивные вкладки: Все чаты, Личные [ЛС], Клан, Staff, Сервер.", false);"""

if target_tabs_setting in mod_text:
    mod_text = mod_text.replace(target_tabs_setting, repl_tabs_setting, 1)

target_search_setting = """   public final BooleanSetting chatSearch = this.addSetting(
      new BooleanSetting("Поиск по чату", "Поиск сообщений по ключевым словам (кнопка 🔍 или Ctrl+F).", true)
   );"""

repl_search_setting = """   public final BooleanSetting chatSearch = new BooleanSetting("Поиск по чату", "Поиск сообщений по ключевым словам (кнопка 🔍 или Ctrl+F).", false);"""

if target_search_setting in mod_text:
    mod_text = mod_text.replace(target_search_setting, repl_search_setting, 1)

# Header height and divider removal
target_header = """      float headerH = focused ? 22.0F : 0.0F;
      float inputH = focused ? 20.0F : 0.0F;
      float messageAreaH = focused
         ? Math.max(maxLines * 9.0F * chatScale + 6.0F, 120.0F)
         : (visibleCount * 9.0F * chatScale + 8.0F);

      float targetHeight = headerH + messageAreaH + inputH;"""

repl_header = """      float headerH = 0.0F;
      float inputH = focused ? 20.0F : 0.0F;
      float messageAreaH = focused
         ? Math.max(maxLines * 9.0F * chatScale + 6.0F, 120.0F)
         : (visibleCount * 9.0F * chatScale + 8.0F);

      float targetHeight = messageAreaH + inputH;"""

if target_header in mod_text:
    mod_text = mod_text.replace(target_header, repl_header, 1)

# Remove top divider line
target_dividers = """         // 4. Subtle dividers when chat is open (top header divider & bottom input divider)
         if (focused) {
            int divColor = Colors.computeInt7(ClickGuiStyle.intVal11, Math.round(100 * this.animatedAlpha));
            drawClickGuiRect(panelX + 4.0F, panelY + headerH, panelW - 8.0F, 0.75F, 0.5F, divColor);
            drawClickGuiRect(panelX + 4.0F, bottomY - inputH, panelW - 8.0F, 0.75F, 0.5F, divColor);
         }"""

repl_dividers = """         // 4. Subtle divider when chat is open (bottom input divider)
         if (focused) {
            int divColor = Colors.computeInt7(ClickGuiStyle.intVal11, Math.round(100 * this.animatedAlpha));
            drawClickGuiRect(panelX + 4.0F, bottomY - inputH, panelW - 8.0F, 0.75F, 0.5F, divColor);
         }"""

if target_dividers in mod_text:
    mod_text = mod_text.replace(target_dividers, repl_dividers, 1)

# Remove badge
target_badge = """         // 5. Active Tab Badge when chat is closed
         if (!focused && this.tabs.isChecked() && this.currentTab != ChatTab.ALL && mc.textRenderer != null) {
            String badge = "• " + this.currentTab.getFullName();
            int badgeW = mc.textRenderer.getWidth(badge) + 8;
            float badgeX = panelX + panelW - badgeW - 6.0F;
            float badgeY = panelY + 4.0F;
            drawClickGuiPanel(badgeX, badgeY, badgeW, 11.0F, 4.0F, ClickGuiStyle.intVal46, ClickGuiStyle.intVal11, 0.5F);
         }
      } finally {
         flushShaders(began);
      }

      if (!focused && this.tabs.isChecked() && this.currentTab != ChatTab.ALL && mc.textRenderer != null) {
         String badge = "• " + this.currentTab.getFullName();
         int badgeW = mc.textRenderer.getWidth(badge) + 8;
         float badgeX = panelX + panelW - badgeW - 6.0F;
         float badgeY = panelY + 4.0F;
         drawContext.drawText(mc.textRenderer, badge, (int)badgeX + 4, (int)badgeY + 2, baseAccent, false);
      }"""

repl_badge = """      } finally {
         flushShaders(began);
      }"""

if target_badge in mod_text:
    mod_text = mod_text.replace(target_badge, repl_badge, 1)

# Replace renderChatTabs body with empty method
start_rct = "public void renderChatTabs(DrawContext context, TextRenderer textRenderer, int fieldX, int fieldY, int chatWidth, int mouseX, int mouseY) {"
end_rct = "   // --- Direct Click on Links in Chat (instant browser open) ---"

if start_rct in mod_text and end_rct in mod_text:
    p1 = mod_text.index(start_rct)
    p2 = mod_text.index(end_rct)
    mod_text = mod_text[:p1] + """public void renderChatTabs(DrawContext context, TextRenderer textRenderer, int fieldX, int fieldY, int chatWidth, int mouseX, int mouseY) {
      // Disabled: tabs and search removed
   }

""" + mod_text[p2:]
    print("renderChatTabs replaced with empty method.")

# Update shouldDisplayInCurrentTab
target_should_display = """   public boolean shouldDisplayInCurrentTab(Text message) {
      if (!this.isEnabled()) {
         return true;
      }
      if (message == null) return false;
      String raw = message.getString();
      if (raw == null) return false;
      String clean = Formatting.strip(raw);

      // 1. Check Search filter
      if (this.chatSearch.isChecked() && this.isSearchOpen && !this.searchQuery.isBlank()) {
         if (!clean.toLowerCase(java.util.Locale.ROOT).contains(this.searchQuery.toLowerCase(java.util.Locale.ROOT))) {
            return false;
         }
      }

      // 2. Check Muted Players filter
      if (!this.mutedPlayers.isEmpty()) {
         String cleanLower = clean.toLowerCase(java.util.Locale.ROOT);
         for (String muted : this.mutedPlayers) {
            if (cleanLower.contains(muted)) {
               return false;
            }
         }
      }

      if (!this.tabs.isChecked() || this.currentTab == ChatTab.ALL) {
         return true;
      }

      String trimmedClean = clean.trim();
      if (trimmedClean.isEmpty()) {
         return false;
      }

      switch (this.currentTab) {
         case PM:
            return isPrivateMessage(trimmedClean);
         case CLAN:
            return isClanMessage(trimmedClean);
         case STAFF:
            return isStaffMessage(trimmedClean);
         case SERVER:
            return isServerMessage(trimmedClean);
         default:
            return true;
      }
   }"""

repl_should_display = """   public boolean shouldDisplayInCurrentTab(Text message) {
      if (!this.isEnabled()) {
         return true;
      }
      if (message == null) return false;
      String raw = message.getString();
      if (raw == null) return false;

      // Check Muted Players filter
      if (!this.mutedPlayers.isEmpty()) {
         String clean = Formatting.strip(raw);
         String cleanLower = clean.toLowerCase(java.util.Locale.ROOT);
         for (String muted : this.mutedPlayers) {
            if (cleanLower.contains(muted)) {
               return false;
            }
         }
      }

      return true;
   }"""

if target_should_display in mod_text:
    mod_text = mod_text.replace(target_should_display, repl_should_display, 1)
    print("shouldDisplayInCurrentTab updated.")

# Update onMouseClicked
target_mouse = """      // Direct click on links in chat (without popup mini-menu)
      if (this.linkHighlight.isChecked() && button == 0) {
         String clickedUrl = getClickedUrlAt(mouseX, mouseY);
         if (clickedUrl != null) {
            openUrl(clickedUrl);
            NotificationHud.processData3(this.category.getIcon(), "Ссылка", "Открываем в браузере...", true);
            return true;
         }
      }

      // Check Search button & bar
      if (this.chatSearch.isChecked() && button == 0) {
         if (!this.isSearchOpen) {
            if (mouseX >= this.searchBtnX && mouseX <= this.searchBtnX + this.searchBtnW &&
                mouseY >= this.searchBtnY && mouseY <= this.searchBtnY + this.searchBtnH) {
               toggleSearch();
               return true;
            }
         } else {
            // Close search button
            if (mouseX >= this.searchCloseBtnX && mouseX <= this.searchCloseBtnX + this.searchCloseBtnSize &&
                mouseY >= this.searchCloseBtnY && mouseY <= this.searchCloseBtnY + this.searchCloseBtnSize) {
               closeSearch();
               return true;
            }
            // Search bar input focus
            if (mouseX >= this.searchBtnX && mouseX <= this.searchBtnX + this.searchBtnW &&
                mouseY >= this.searchBtnY && mouseY <= this.searchBtnY + this.searchBtnH) {
               this.searchFocused = true;
               return true;
            } else {
               this.searchFocused = false;
            }
         }
      }

      // Check Jump To Bottom Button
      if (this.jumpToBottom.isChecked() && mc.inGameHud != null && mc.inGameHud.getChatHud() != null && button == 0) {
         int scrollPos = ((ChatHudMixin)mc.inGameHud.getChatHud()).chatScrollbarPos();
         if (scrollPos > 0) {
            if (mouseX >= this.jumpBtnX && mouseX <= this.jumpBtnX + this.jumpBtnSize &&
                mouseY >= this.jumpBtnY && mouseY <= this.jumpBtnY + this.jumpBtnSize) {
               mc.inGameHud.getChatHud().resetScroll();
               this.unreadScrolledCount = 0;
               return true;
            }
         }
      }

      // Check Chat Tabs
      if (this.tabs.isChecked() && button == 0) {
         for (TabBounds bounds : this.tabBoundsList) {
            if (bounds.contains(mouseX, mouseY)) {
               setTab(bounds.tab);
               return true;
            }
         }
      }

      return false;"""

repl_mouse = """      // Direct click on links in chat (without popup mini-menu)
      if (this.linkHighlight.isChecked() && button == 0) {
         String clickedUrl = getClickedUrlAt(mouseX, mouseY);
         if (clickedUrl != null) {
            openUrl(clickedUrl);
            NotificationHud.processData3(this.category.getIcon(), "Ссылка", "Открываем в браузере...", true);
            return true;
         }
      }

      // Check Jump To Bottom Button
      if (this.jumpToBottom.isChecked() && mc.inGameHud != null && mc.inGameHud.getChatHud() != null && button == 0) {
         int scrollPos = ((ChatHudMixin)mc.inGameHud.getChatHud()).chatScrollbarPos();
         if (scrollPos > 0) {
            if (mouseX >= this.jumpBtnX && mouseX <= this.jumpBtnX + this.jumpBtnSize &&
                mouseY >= this.jumpBtnY && mouseY <= this.jumpBtnY + this.jumpBtnSize) {
               mc.inGameHud.getChatHud().resetScroll();
               this.unreadScrolledCount = 0;
               return true;
            }
         }
      }

      return false;"""

if target_mouse in mod_text:
    mod_text = mod_text.replace(target_mouse, repl_mouse, 1)
    print("onMouseClicked updated.")

# Update handleKeyPressed & handleCharTyped
target_keys = """      // Ctrl + F (key 70 = F, modifier 2 = Ctrl)
      if (this.chatSearch.isChecked() && (modifiers & 2) != 0 && key == 70) {
         toggleSearch();
         return true;
      }
      if (this.isSearchOpen && this.searchFocused) {
         if (key == 256) { // Escape
            closeSearch();
            return true;
         }
         if (key == 259) { // Backspace
            if (!this.searchQuery.isEmpty()) {
               this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
               refreshMessages();
            }
            return true;
         }
         if (key == 257) { // Enter
            this.searchFocused = false;
            return true;
         }
         return true;
      }
      return false;
   }

   public boolean handleCharTyped(String ch) {
      if (!this.isEnabled()) {
         return false;
      }
      if (this.isSearchOpen && this.searchFocused && ch != null && !ch.isEmpty()) {
         this.searchQuery += ch;
         refreshMessages();
         return true;
      }
      return false;
   }"""

repl_keys = """      return false;
   }

   public boolean handleCharTyped(String ch) {
      return false;
   }"""

if target_keys in mod_text:
    mod_text = mod_text.replace(target_keys, repl_keys, 1)
    print("handleKeyPressed and handleCharTyped updated.")

with open(module_path, "w", encoding="utf-8") as f:
    f.write(mod_text)

print("CustomChatModule.java updated successfully!")

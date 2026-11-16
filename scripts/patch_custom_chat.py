from pathlib import Path

module_path = Path(r"C:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\module\render\CustomChatModule.java")
with open(module_path, "r", encoding="utf-8") as f:
    text = f.read()

# 1. Update tabs setting definition
old_tabs = """   public final BooleanSetting tabs = this.addSetting(
      new BooleanSetting("Вкладки чатов", "Интерактивные вкладки: Все чаты, Личные [ЛС], Клан, Staff, Сервер.", true)
   );"""
new_tabs = """   public final BooleanSetting tabs = new BooleanSetting("Вкладки чатов", "Интерактивные вкладки.", false);"""
if old_tabs in text:
    text = text.replace(old_tabs, new_tabs, 1)
    print("tabs setting updated.")
else:
    print("old_tabs not found")

# 2. Update chatSearch setting definition
old_search = """   public final BooleanSetting chatSearch = this.addSetting(
      new BooleanSetting("Поиск по чату", "Поиск сообщений по ключевым словам (кнопка \U0001f50d или Ctrl+F).", true)
   );"""
new_search = """   public final BooleanSetting chatSearch = new BooleanSetting("Поиск по чату", "Поиск сообщений.", false);"""
if old_search in text:
    text = text.replace(old_search, new_search, 1)
    print("chatSearch setting updated.")
else:
    print("old_search not found")

# 3. Update headerH to 0.0F
old_header = "      float headerH = focused ? 22.0F : 0.0F;"
new_header = "      float headerH = 0.0F;"
if old_header in text:
    text = text.replace(old_header, new_header, 1)
    print("headerH updated to 0.0F.")
else:
    print("old_header not found")

# 4. Remove top divider line
old_dividers = """         // 4. Subtle dividers when chat is open (top header divider & bottom input divider)
         if (focused) {
            int divColor = Colors.computeInt7(ClickGuiStyle.intVal11, Math.round(100 * this.animatedAlpha));
            drawClickGuiRect(panelX + 4.0F, panelY + headerH, panelW - 8.0F, 0.75F, 0.5F, divColor);
            drawClickGuiRect(panelX + 4.0F, bottomY - inputH, panelW - 8.0F, 0.75F, 0.5F, divColor);
         }"""
new_dividers = """         // 4. Subtle divider when chat is open (bottom input divider)
         if (focused) {
            int divColor = Colors.computeInt7(ClickGuiStyle.intVal11, Math.round(100 * this.animatedAlpha));
            drawClickGuiRect(panelX + 4.0F, bottomY - inputH, panelW - 8.0F, 0.75F, 0.5F, divColor);
         }"""
if old_dividers in text:
    text = text.replace(old_dividers, new_dividers, 1)
    print("dividers updated.")
else:
    print("old_dividers not found")

# 5. Remove closed chat tab badge
old_badge = """         // 5. Active Tab Badge when chat is closed
         if (!focused && this.tabs.isChecked() && this.currentTab != ChatTab.ALL && mc.field_1772 != null) {
            String badge = "• " + this.currentTab.getFullName();
            int badgeW = mc.field_1772.method_1727(badge) + 8;
            float badgeX = panelX + panelW - badgeW - 6.0F;
            float badgeY = panelY + 4.0F;
            drawClickGuiPanel(badgeX, badgeY, badgeW, 11.0F, 4.0F, ClickGuiStyle.intVal46, ClickGuiStyle.intVal11, 0.5F);
         }
      } finally {
         flushShaders(began);
      }

      if (!focused && this.tabs.isChecked() && this.currentTab != ChatTab.ALL && mc.field_1772 != null) {
         String badge = "• " + this.currentTab.getFullName();
         int badgeW = mc.field_1772.method_1727(badge) + 8;
         float badgeX = panelX + panelW - badgeW - 6.0F;
         float badgeY = panelY + 4.0F;
         drawContext.method_51433(mc.field_1772, badge, (int)badgeX + 4, (int)badgeY + 2, baseAccent, false);
      }"""
new_badge = """      } finally {
         flushShaders(began);
      }"""
if old_badge in text:
    text = text.replace(old_badge, new_badge, 1)
    print("closed chat badge removed.")
else:
    print("old_badge not found")

# 6. Replace renderChatTabs with Jump To Bottom only
start_rct = "   public void renderChatTabs(class_332 context, class_327 textRenderer, int fieldX, int fieldY, int chatWidth, int mouseX, int mouseY) {"
end_rct = "   // --- Player & URL Hover Detection ---"

if start_rct in text and end_rct in text:
    p1 = text.index(start_rct)
    p2 = text.index(end_rct)
    new_rct = """   public void renderChatTabs(class_332 context, class_327 textRenderer, int fieldX, int fieldY, int chatWidth, int mouseX, int mouseY) {
      if (!this.isEnabled() || mc.field_1724 == null || textRenderer == null) {
         return;
      }

      this.tabBoundsList.clear();

      // Jump to bottom button when scrolled up
      if (this.jumpToBottom.isChecked() && mc.field_1755 != null && mc.field_1755.method_1743() != null) {
         int scrollPos = ((ChatHudMixin)mc.field_1755.method_1743()).chatScrollbarPos();
         if (scrollPos > 0) {
            float jumpSize = 22.0F;
            float jumpX = this.lastPanelX + this.lastPanelW - jumpSize - 8.0F;
            float jumpY = this.lastPanelY + this.lastPanelH - jumpSize - 26.0F;
            this.jumpBtnX = jumpX;
            this.jumpBtnY = jumpY;
            this.jumpBtnSize = jumpSize;
            boolean isHoverJump = (mouseX >= jumpX && mouseX <= jumpX + jumpSize && mouseY >= jumpY && mouseY <= jumpY + jumpSize);

            boolean began = beginShaders(context);
            try {
               int accent = ClickGuiStyle.intVal;
               if (this.glow.isChecked()) {
                  drawClickGuiGlow(jumpX, jumpY, jumpSize, jumpSize, jumpSize / 2.0F, Colors.computeInt7(accent, 110), 4.5F, 0.85F);
               }
               int jBody = isHoverJump ? ClickGuiStyle.intVal9 : ClickGuiStyle.intVal46;
               int jOut = isHoverJump ? ClickGuiStyle.intVal : ClickGuiStyle.intVal11;
               drawClickGuiPanel(jumpX, jumpY, jumpSize, jumpSize, jumpSize / 2.0F, jBody, jOut, 0.75F);
            } finally {
               flushShaders(began);
            }

            String jumpIcon = "↓";
            int jIconW = textRenderer.method_1727(jumpIcon);
            context.method_51433(textRenderer, jumpIcon, (int)(jumpX + (jumpSize - jIconW) / 2.0F), (int)(jumpY + (jumpSize - 8.0F) / 2.0F), isHoverJump ? 0xFFFFFFFF : ClickGuiStyle.intVal, false);
            if (this.unreadScrolledCount > 0) {
               String badge = String.valueOf(Math.min(99, this.unreadScrolledCount));
               int bW = textRenderer.method_1727(badge) + 6;
               float bX = jumpX + jumpSize - 4.0F;
               float bY = jumpY - 3.0F;
               boolean bBegan = beginShaders(context);
               try {
                  drawClickGuiPanel(bX, bY, bW, 10.0F, 3.0F, ClickGuiStyle.intVal, ClickGuiStyle.intVal, 0.65F);
               } finally {
                  flushShaders(bBegan);
               }
               context.method_51433(textRenderer, badge, (int)(bX + 3), (int)(bY + 1), 0xFFFFFFFF, false);
            }
         }
      }
   }

"""
    text = text[:p1] + new_rct + text[p2:]
    print("renderChatTabs replaced successfully.")
else:
    print("rct markers not found")

with open(module_path, "w", encoding="utf-8") as f:
    f.write(text)

print("Finished updating CustomChatModule.java!")

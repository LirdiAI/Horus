from pathlib import Path

module_path = Path(r"C:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\module\render\CustomChatModule.java")
with open(module_path, "r", encoding="utf-8") as f:
    text = f.read()

# 1. Update chatSearch setting definition
target_search = """   public final BooleanSetting chatSearch = this.addSetting(
      new BooleanSetting("Поиск по чату", "Поиск сообщений по ключевым словам (кнопка \U0001f50d или Ctrl+F).", true)
   );"""

repl_search = """   public final BooleanSetting chatSearch = new BooleanSetting("Поиск по чату", "Поиск сообщений по ключевым словам (кнопка \U0001f50d или Ctrl+F).", false);"""

if target_search in text:
    text = text.replace(target_search, repl_search, 1)
    print("chatSearch setting updated.")
else:
    # Try finding by line substring
    lines = text.splitlines(keepends=True)
    new_lines = []
    skip = False
    for i, line in enumerate(lines):
        if "public final BooleanSetting chatSearch" in line:
            new_lines.append('   public final BooleanSetting chatSearch = new BooleanSetting("Поиск по чату", "Поиск сообщений по ключевым словам.", false);\n')
            skip = True
        elif skip:
            if ");" in line:
                skip = False
        else:
            new_lines.append(line)
    text = "".join(new_lines)
    print("chatSearch setting updated via line loop.")

# 2. Update shouldDisplayInCurrentTab and trackIncomingTabMessage
start_sd = "   public boolean shouldDisplayInCurrentTab(Text text) {"
end_sd = "   // --- Direct Click on Links in Chat (instant browser open) ---"

# Let's inspect where shouldDisplayInCurrentTab starts and ends
if start_sd in text:
    p1 = text.index(start_sd)
    # Find start of renderChatTabs
    p2 = text.index("   // --- Rendering Chat Tabs in ChatScreen ---")
    sd_code = """   public boolean shouldDisplayInCurrentTab(Text text) {
      if (!this.isEnabled()) {
         return true;
      }
      if (text == null) return false;
      String raw = text.getString();
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
   }

   public void trackIncomingTabMessage(Text text) {
   }

"""
    text = text[:p1] + sd_code + text[p2:]
    print("shouldDisplayInCurrentTab and trackIncomingTabMessage replaced.")

# 3. Replace renderChatTabs with Jump-To-Bottom only
start_rct = "   public void renderChatTabs(DrawContext context, TextRenderer textRenderer, int fieldX, int fieldY, int chatWidth, int mouseX, int mouseY) {"
p_rct = text.index(start_rct)
p_after_rct = text.index("   // --- Player & URL Hover Detection ---")

new_rct_code = """   public void renderChatTabs(DrawContext context, TextRenderer textRenderer, int fieldX, int fieldY, int chatWidth, int mouseX, int mouseY) {
      if (!this.isEnabled() || mc.player == null || textRenderer == null) {
         return;
      }

      this.tabBoundsList.clear();

      // Jump to bottom button when scrolled up
      if (this.jumpToBottom.isChecked() && mc.inGameHud != null && mc.inGameHud.getChatHud() != null) {
         int scrollPos = ((ChatHudMixin)mc.inGameHud.getChatHud()).chatScrollbarPos();
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
               int jBody = isHoverJump ? ClickGuiStyle.intVal11 : ClickGuiStyle.intVal46;
               int jOutline = isHoverJump ? ClickGuiStyle.intVal : ClickGuiStyle.intVal3;
               drawClickGuiPanel(jumpX, jumpY, jumpSize, jumpSize, jumpSize / 2.0F, jBody, jOutline, 0.75F);
            } finally {
               flushShaders(began);
            }

            String jumpIcon = "↓";
            int jIconW = textRenderer.getWidth(jumpIcon);
            context.drawText(textRenderer, jumpIcon, (int)(jumpX + (jumpSize - jIconW) / 2.0F), (int)(jumpY + (jumpSize - 8.0F) / 2.0F), isHoverJump ? 0xFFFFFFFF : ClickGuiStyle.intVal, false);
            if (this.unreadScrolledCount > 0) {
               String badge = String.valueOf(Math.min(99, this.unreadScrolledCount));
               int bW = textRenderer.getWidth(badge) + 6;
               float bX = jumpX + jumpSize - 4.0F;
               float bY = jumpY - 3.0F;
               boolean bBegan = beginShaders(context);
               try {
                  drawClickGuiPanel(bX, bY, bW, 10.0F, 3.0F, ClickGuiStyle.intVal, ClickGuiStyle.intVal, 0.65F);
               } finally {
                  flushShaders(bBegan);
               }
               context.drawText(textRenderer, badge, (int)(bX + 3), (int)(bY + 1), 0xFFFFFFFF, false);
            }
         }
      }
   }

"""

text = text[:p_rct] + new_rct_code + text[p_after_rct:]
print("renderChatTabs replaced successfully.")

with open(module_path, "w", encoding="utf-8") as f:
    f.write(text)

print("CustomChatModule.java updated completely!")

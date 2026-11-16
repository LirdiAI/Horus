from pathlib import Path

path = Path(r"C:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\module\render\CustomChatModule.java")
with open(path, "r", encoding="utf-8") as f:
    text = f.read()

# 1. Tabs setting
old_tabs = 'public final BooleanSetting tabs = this.addSetting(new BooleanSetting("\\u0412\\u043a\\u043b\\u0430\\u0434\\u043a\\u0438 \\u0447\\u0430\\u0442\\u043e\\u0432", "\\u0418\\u043d\\u0442\\u0435\\u0440\\u0430\\u043a\\u0442\\u0438\\u0432\\u043d\\u044b\\u0435 \\u0432\\u043a\\u043b\\u0430\\u0434\\u043a\\u0438: \\u0412\\u0441\\u0435 \\u0447\\u0430\\u0442\\u044b, \\u041b\\u0438\\u0447\\u043d\\u044b\\u0435 [\\u041b\\u0421], \\u041a\\u043b\\u0430\\u043d, Staff, \\u0421\\u0435\\u0440\\u0432\\u0435\\u0440.", true));'
new_tabs = 'public final BooleanSetting tabs = new BooleanSetting("\\u0412\\u043a\\u043b\\u0430\\u0434\\u043a\\u0438 \\u0447\\u0430\\u0442\\u043e\\u0432", "\\u0412\\u043a\\u043b\\u0430\\u0434\\u043a\\u0438", false);'
assert old_tabs in text, "old_tabs not found"
text = text.replace(old_tabs, new_tabs, 1)

# 2. Search setting
old_search = 'public final BooleanSetting chatSearch = this.addSetting(new BooleanSetting("\\u041f\\u043e\\u0438\\u0441\\u043a \\u043f\\u043e \\u0447\\u0430\\u0442\\u0443", "\\u041f\\u043e\\u0438\\u0441\\u043a \\u0441\\u043e\\u043e\\u0431\\u0449\\u0435\\u043d\\u0438\\u0439 \\u043f\\u043e \\u043a\\u043b\\u044e\\u0447\\u0435\\u0432\\u044b\\u043c \\u0441\\u043b\\u043e\\u0432\\u0430\\u043c (\\u043a\\u043d\\u043e\\u043f\\u043a\\u0430 \\ud83d\\udd0d \\u0438\\u043b\\u0438 Ctrl+F).", true));'
new_search = 'public final BooleanSetting chatSearch = new BooleanSetting("\\u041f\\u043e\\u0438\\u0441\\u043a \\u043f\\u043e \\u0447\\u0430\\u0442\\u0443", "\\u041f\\u043e\\u0438\\u0441\\u043a", false);'
assert old_search in text, "old_search not found"
text = text.replace(old_search, new_search, 1)

# 3. headerH in renderGlassPanel
old_header = 'float headerH = focused ? 22.0f : 0.0f;'
new_header = 'float headerH = 0.0f;'
assert old_header in text, "old_header not found"
text = text.replace(old_header, new_header, 1)

# 4. Dividers in renderGlassPanel
old_dividers = '''            if (focused) {
                int divColor = Colors.computeInt7(ClickGuiStyle.intVal11, Math.round(100.0f * this.animatedAlpha));
                this.drawClickGuiRect(panelX + 4.0f, panelY + headerH, panelW - 8.0f, 0.75f, 0.5f, divColor);
                this.drawClickGuiRect(panelX + 4.0f, bottomY - inputH, panelW - 8.0f, 0.75f, 0.5f, divColor);
            }'''
new_dividers = '''            if (focused) {
                int divColor = Colors.computeInt7(ClickGuiStyle.intVal11, Math.round(100.0f * this.animatedAlpha));
                this.drawClickGuiRect(panelX + 4.0f, bottomY - inputH, panelW - 8.0f, 0.75f, 0.5f, divColor);
            }'''
assert old_dividers in text, "old_dividers not found"
text = text.replace(old_dividers, new_dividers, 1)

# 5. Active Tab Badge when chat is closed
old_badge = '''            if (!focused && this.tabs.isChecked() && this.currentTab != ChatTab.ALL && CustomChatModule.mc.textRenderer != null) {
                String badge = "\\u2022 " + this.currentTab.getFullName();
                int badgeW = CustomChatModule.mc.textRenderer.getWidth(badge) + 8;
                float badgeX = panelX + panelW - (float)badgeW - 6.0f;
                float badgeY = panelY + 4.0f;
                this.drawClickGuiPanel(badgeX, badgeY, badgeW, 11.0f, 4.0f, ClickGuiStyle.intVal46, ClickGuiStyle.intVal11, 0.5f);
            }
        }
        finally {
            this.flushShaders(began);
        }
        if (!focused && this.tabs.isChecked() && this.currentTab != ChatTab.ALL && CustomChatModule.mc.textRenderer != null) {
            String badge = "\\u2022 " + this.currentTab.getFullName();
            int badgeW = CustomChatModule.mc.textRenderer.getWidth(badge) + 8;
            float badgeX = panelX + panelW - (float)badgeW - 6.0f;
            float badgeY = panelY + 4.0f;
            drawContext.drawText(CustomChatModule.mc.textRenderer, badge, (int)badgeX + 4, (int)badgeY + 2, baseAccent, false);
        }'''
new_badge = '''        }
        finally {
            this.flushShaders(began);
        }'''
assert old_badge in text, "old_badge not found"
text = text.replace(old_badge, new_badge, 1)

# 6. Replace renderChatTabs with Jump Button only
start_marker = '    public void renderChatTabs(DrawContext context, TextRenderer textRenderer, int fieldX, int fieldY, int chatWidth, int mouseX, int mouseY) {'
end_marker = '    private boolean isNickChar(char c) {'
assert start_marker in text and end_marker in text, "renderChatTabs markers not found"

p1 = text.index(start_marker)
p2 = text.index(end_marker)

new_tabs_code = '''    public void renderChatTabs(DrawContext context, TextRenderer textRenderer, int fieldX, int fieldY, int chatWidth, int mouseX, int mouseY) {
        this.renderJumpButton(context, textRenderer, mouseX, mouseY);
    }

    public void renderJumpButton(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        if (!this.isEnabled() || CustomChatModule.mc.player == null || textRenderer == null) {
            return;
        }
        boolean showJumpBtn = false;
        float jumpX = 0.0f;
        float jumpY = 0.0f;
        float jumpSize = 0.0f;
        boolean isHoverJump = false;
        int scrollbarPos = 0;
        if (this.jumpToBottom.isChecked() && CustomChatModule.mc.inGameHud != null && CustomChatModule.mc.inGameHud.getChatHud() != null && (scrollbarPos = ((ChatHudMixin)CustomChatModule.mc.inGameHud.getChatHud()).chatScrollbarPos()) > 0) {
            showJumpBtn = true;
            jumpSize = 20.0f;
            jumpX = this.lastPanelX + this.lastPanelW - jumpSize - 8.0f;
            jumpY = (float)context.getScaledWindowHeight() - 24.0f - jumpSize - 6.0f;
            this.jumpBtnX = jumpX;
            this.jumpBtnY = jumpY;
            this.jumpBtnSize = jumpSize;
            isHoverJump = (float)mouseX >= jumpX && (float)mouseX <= jumpX + jumpSize && (float)mouseY >= jumpY && (float)mouseY <= jumpY + jumpSize;
        }
        if (showJumpBtn) {
            int accent = ClickGuiStyle.intVal;
            boolean began = this.beginShaders(context);
            try {
                if (this.glow.isChecked()) {
                    this.drawClickGuiGlow(jumpX, jumpY, jumpSize, jumpSize, jumpSize / 2.0f, Colors.computeInt7(accent, 110), 4.5f, 0.85f);
                }
                int jBody = isHoverJump ? ClickGuiStyle.intVal9 : ClickGuiStyle.intVal46;
                int jOut = isHoverJump ? ClickGuiStyle.intVal : ClickGuiStyle.intVal11;
                this.drawClickGuiPanel(jumpX, jumpY, jumpSize, jumpSize, jumpSize / 2.0f, jBody, jOut, 0.75f);
            }
            finally {
                this.flushShaders(began);
            }
            String jumpIcon = "\\u2193";
            int jIconW = textRenderer.getWidth(jumpIcon);
            context.drawText(textRenderer, jumpIcon, (int)(jumpX + (jumpSize - (float)jIconW) / 2.0f), (int)(jumpY + (jumpSize - 8.0f) / 2.0f), isHoverJump ? -1 : ClickGuiStyle.intVal, false);
            if (this.unreadScrolledCount > 0) {
                String badge = String.valueOf(Math.min(99, this.unreadScrolledCount));
                int bW = textRenderer.getWidth(badge) + 6;
                float bX = jumpX + jumpSize - 4.0f;
                float bY = jumpY - 3.0f;
                boolean bBegan = this.beginShaders(context);
                try {
                    this.drawClickGuiPanel(bX, bY, bW, 10.0f, 3.0f, ClickGuiStyle.intVal, ClickGuiStyle.intVal, 0.65f);
                }
                finally {
                    this.flushShaders(bBegan);
                }
                context.drawText(textRenderer, badge, (int)(bX + 3.0f), (int)(bY + 1.0f), -1, false);
            }
        }
    }

'''

text = text[:p1] + new_tabs_code + text[p2:]

# 7. Update onMouseClicked
old_mouse = '''        if (this.linkHighlight.isChecked() && button == 0 && (clickedUrl = this.getClickedUrlAt(mouseX, mouseY)) != null) {
            this.openUrl(clickedUrl);
            NotificationHud.processData3(this.category.getIcon(), "\\u0421\\u0441\\u044b\\u043b\\u043a\\u0430", "\\u041e\\u0442\\u043a\\u0440\\u044b\\u0432\\u0430\\u0435\\u043c \\u0432 \\u0431\\u0440\\u0430\\u0443\\u0437\\u0435\\u0440\\u0435...", true);
            return true;
        }
        if (this.chatSearch.isChecked() && button == 0) {
            if (!this.isSearchOpen) {
                if (mouseX >= (double)this.searchBtnX && mouseX <= (double)(this.searchBtnX + this.searchBtnW) && mouseY >= (double)this.searchBtnY && mouseY <= (double)(this.searchBtnY + this.searchBtnH)) {
                    this.toggleSearch();
                    return true;
                }
            } else {
                if (mouseX >= (double)this.searchCloseBtnX && mouseX <= (double)(this.searchCloseBtnX + this.searchCloseBtnSize) && mouseY >= (double)this.searchCloseBtnY && mouseY <= (double)(this.searchCloseBtnY + this.searchCloseBtnSize)) {
                    this.closeSearch();
                    return true;
                }
                if (mouseX >= (double)this.searchBtnX && mouseX <= (double)(this.searchBtnX + this.searchBtnW) && mouseY >= (double)this.searchBtnY && mouseY <= (double)(this.searchBtnY + this.searchBtnH)) {
                    this.searchFocused = true;
                    return true;
                }
                this.searchFocused = false;
            }
        }
        if (this.jumpToBottom.isChecked() && CustomChatModule.mc.inGameHud != null && CustomChatModule.mc.inGameHud.getChatHud() != null && button == 0 && (scrollPos = ((ChatHudMixin)CustomChatModule.mc.inGameHud.getChatHud()).chatScrollbarPos()) > 0 && mouseX >= (double)this.jumpBtnX && mouseX <= (double)(this.jumpBtnX + this.jumpBtnSize) && mouseY >= (double)this.jumpBtnY && mouseY <= (double)(this.jumpBtnY + this.jumpBtnSize)) {
            CustomChatModule.mc.inGameHud.getChatHud().resetScroll();
            this.unreadScrolledCount = 0;
            return true;
        }
        if (this.tabs.isChecked() && button == 0) {
            for (TabBounds bounds : this.tabBoundsList) {
                if (!bounds.contains(mouseX, mouseY)) continue;
                this.setTab(bounds.tab);
                return true;
            }
        }
        return false;'''

new_mouse = '''        if (this.linkHighlight.isChecked() && button == 0 && (clickedUrl = this.getClickedUrlAt(mouseX, mouseY)) != null) {
            this.openUrl(clickedUrl);
            NotificationHud.processData3(this.category.getIcon(), "\\u0421\\u0441\\u044b\\u043b\\u043a\\u0430", "\\u041e\\u0442\\u043a\\u0440\\u044b\\u0432\\u0430\\u0435\\u043c \\u0432 \\u0431\\u0440\\u0430\\u0443\\u0437\\u0435\\u0440\\u0435...", true);
            return true;
        }
        if (this.jumpToBottom.isChecked() && CustomChatModule.mc.inGameHud != null && CustomChatModule.mc.inGameHud.getChatHud() != null && button == 0 && (scrollPos = ((ChatHudMixin)CustomChatModule.mc.inGameHud.getChatHud()).chatScrollbarPos()) > 0 && mouseX >= (double)this.jumpBtnX && mouseX <= (double)(this.jumpBtnX + this.jumpBtnSize) && mouseY >= (double)this.jumpBtnY && mouseY <= (double)(this.jumpBtnY + this.jumpBtnSize)) {
            CustomChatModule.mc.inGameHud.getChatHud().resetScroll();
            this.unreadScrolledCount = 0;
            return true;
        }
        return false;'''
assert old_mouse in text, "old_mouse not found"
text = text.replace(old_mouse, new_mouse, 1)

# 8. Update handleKeyPressed and handleCharTyped
old_keys = '''    public boolean handleKeyPressed(int key, int modifiers) {
        if (!this.isEnabled()) {
            return false;
        }
        if (this.isPlayerPopupActive && key == 256) {
            this.closePlayerPopup();
            return true;
        }
        if (this.chatSearch.isChecked() && (modifiers & 2) != 0 && key == 70) {
            this.toggleSearch();
            return true;
        }
        if (this.isSearchOpen && this.searchFocused) {
            if (key == 256) {
                this.closeSearch();
                return true;
            }
            if (key == 259) {
                if (!this.searchQuery.isEmpty()) {
                    this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
                    this.refreshMessages();
                }
                return true;
            }
            if (key == 257) {
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
            this.searchQuery = this.searchQuery + ch;
            this.refreshMessages();
            return true;
        }
        return false;
    }'''

new_keys = '''    public boolean handleKeyPressed(int key, int modifiers) {
        if (!this.isEnabled()) {
            return false;
        }
        if (this.isPlayerPopupActive && key == 256) {
            this.closePlayerPopup();
            return true;
        }
        return false;
    }

    public boolean handleCharTyped(String ch) {
        return false;
    }'''
assert old_keys in text, "old_keys not found"
text = text.replace(old_keys, new_keys, 1)

# 9. Call renderJumpButton inside renderInputFieldForeground
target_render_end = '''        this.updateHoveredElements(mouseX, mouseY);
        this.renderPopups(context, textRenderer, mouseX, mouseY);
    }'''
repl_render_end = '''        this.renderJumpButton(context, textRenderer, mouseX, mouseY);
        this.updateHoveredElements(mouseX, mouseY);
        this.renderPopups(context, textRenderer, mouseX, mouseY);
    }'''
assert target_render_end in text, "target_render_end not found"
text = text.replace(target_render_end, repl_render_end, 1)

with open(path, "w", encoding="utf-8") as f:
    f.write(text)

print("CustomChatModule.java successfully updated without tabs and search!")

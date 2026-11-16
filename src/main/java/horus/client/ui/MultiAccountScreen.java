package horus.client.ui;

import horus.client.module.misc.MultiAccountModule;
import horus.client.multiplayer.AltBot;
import horus.client.render.FontFace;
import horus.client.render.FontText;
import horus.client.render.Render2D;
import horus.client.util.GuiScale;
import horus.client.util.Lang;
import horus.client.ui.hud.HudRenderContext;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Панель мультиаккаунта: добавить альт по нику, смотреть статусы,
 * писать в чат от всех, отключать альтов.
 */
public final class MultiAccountScreen extends Screen {
   private static final int NICK_FIELD = 0;
   private static final int CHAT_FIELD = 1;
   private static final int PASS_FIELD = 2;

   private final Screen parent;
   private final MultiAccountModule module = MultiAccountModule.getInstance();
   private final List<ClickArea> clickAreas = new ArrayList<>();
   private String nickText = "";
   private String chatText = "";
   private String passText = "";
   private final java.util.Set<String> expandedBots = new java.util.HashSet<>();
   private String chatTargetName = "";
   private int feedScroll;
   private float feedBoxX;
   private float feedBoxY;
   private float feedBoxW;
   private float feedBoxH;
   private int focusedField = -1;
   private final AnimatedValue openAnim = new AnimatedValue(260L);
   private final AnimatedValue closeAnim = new AnimatedValue(180L);
   private boolean closing = false;

   private record ClickArea(float x, float y, float w, float h, Runnable action) {}

   public MultiAccountScreen(Screen parent) {
      super(Text.literal("Horus — Мультиаккаунт"));
      this.parent = parent;
   }

   @Override
   protected void init() {
      super.init();
      this.closing = false;
      this.openAnim.getAnimatedValue(0.0);
      this.closeAnim.getAnimatedValue(0.0);
   }

   @Override
   public boolean shouldPause() {
      return false;
   }

   @Override
   public boolean shouldCloseOnEsc() {
      return true;
   }

   @Override
   public void close() {
      if (!this.closing) {
         this.closing = true;
         this.closeAnim.getAnimatedValue(0.0);
      }
   }

   private static float sf() {
      return GuiScale.computeInt() / GuiScale.computeFloat();
   }

   @Override
   public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
      float screenW = this.width * sf();
      float screenH = this.height * sf();
      float mx = mouseX * sf();
      float my = mouseY * sf();

      this.clickAreas.clear();
      if (!this.passText.isEmpty() && this.focusedField != PASS_FIELD) {
         this.module.passwordSetting.setValue(this.passText);
      }

      Render2D.renderElement5(drawContext);

      this.openAnim.processData(1.0);
      float oVal = UiTheme.computeFloat2((float)this.openAnim.computeDouble());

      if (this.closing) {
         this.closeAnim.processData(1.0);
         float cVal = UiTheme.computeFloat2((float)this.closeAnim.computeDouble());
         if (cVal >= 0.98F) {
            if (this.client != null) {
               this.client.setScreen(this.parent);
            }
            return;
         }
         oVal *= 1.0F - cVal;
      }

      if (oVal < 0.999F) {
         Render2D.processData46(Math.max(0.01F, oVal));
      }

      float cardW = Math.min(screenW - 40.0F, 460.0F);
      float cardH = Math.min(screenH - 60.0F, 470.0F);
      float cardX = (screenW - cardW) / 2.0F;
      float enterDrift = (1.0F - oVal) * 16.0F * sf();
      float cardY = (screenH - cardH) / 2.0F + enterDrift;

      Render2D.processData51(cardX, cardY, cardW, cardH, 14.0F, FallenUiHelper.COLOR_BTN_DARK);
      Render2D.processData86(cardX, cardY, cardW, cardH, 14.0F, 1.0F, FallenUiHelper.COLOR_BTN_BORDER);

      FontText.processData3(FontFace.SEMI, Lang.tr("HORUS CLIENT  /  МУЛЬТИАККАУНТ", "HORUS CLIENT  /  MULTIACCOUNT"), cardX + 24.0F, cardY + 18.0F, 6.0F, FallenUiHelper.COLOR_TEXT_MUTED);
      FontText.processData3(FontFace.SEMI, Lang.tr("Второй аккаунт на том же сервере", "Second account on the same server"), cardX + 24.0F, cardY + 30.0F, 12.0F, FallenUiHelper.COLOR_TEXT_PRIMARY);

      float chipW = 120.0F;
      float chipH = 22.0F;
      float chipX = cardX + cardW - chipW - 24.0F;
      float chipY = cardY + 22.0F;
      int botCount = this.module.getBots().size();
      boolean anyOnline = false;
      for (AltBot bot : this.module.getBots()) {
         if (bot.isOnServer()) {
            anyOnline = true;
            break;
         }
      }

      String chipStr = botCount == 0 ? "○ Альтов нет" : (anyOnline ? "● Альтов: " + botCount : "○ Альтов: " + botCount);
      int chipColor = anyOnline ? FallenUiHelper.COLOR_BTN_ACCENT_HOVER : FallenUiHelper.COLOR_TEXT_MUTED;
      int chipBorder = anyOnline ? FallenUiHelper.COLOR_BTN_ACCENT : FallenUiHelper.COLOR_BTN_BORDER;
      Render2D.processData51(chipX, chipY, chipW, chipH, 7.0F, anyOnline ? 0x2E8B5CF6 : 0xFF0B0D10);
      Render2D.processData86(chipX, chipY, chipW, chipH, 7.0F, 1.0F, chipBorder);
      FontText.processData3(FontFace.SEMI, chipStr, chipX + 10.0F, chipY + 6.0F, 6.5F, chipColor);

      Render2D.processData51(cardX + 24.0F, cardY + 56.0F, cardW - 48.0F, 1.0F, 0.0F, FallenUiHelper.COLOR_BTN_BORDER);

      float colX = cardX + 24.0F;
      float curY = cardY + 66.0F;

      // Поле ника
      this.renderInputField(Lang.tr("НИК АЛЬТА (ОФФЛАЙН-СЕРВЕРА)", "ALT NICKNAME (OFFLINE SERVER)"), this.nickText, colX, curY, 160.0F, NICK_FIELD, mx, my);
      this.renderInputField(Lang.tr("ПАРОЛЬ (/REGISTER /LOGIN)", "PASSWORD (/REGISTER /LOGIN)"), this.maskPass(this.passText), colX + 168.0F, curY, 82.0F, PASS_FIELD, mx, my);
      curY += 34.0F;

      // Кнопка "Зайти"
      boolean joinHover = mx >= colX && mx <= colX + 118.0F && my >= curY && my <= curY + 24.0F;
      int joinBg = joinHover ? FallenUiHelper.COLOR_BTN_ACCENT_HOVER : FallenUiHelper.COLOR_BTN_ACCENT;
      int joinBorder = joinHover ? FallenUiHelper.COLOR_BTN_BORDER_HOVER : FallenUiHelper.COLOR_BTN_ACCENT_BORDER;
      Render2D.processData51(colX, curY, 118.0F, 24.0F, 7.0F, joinBg);
      Render2D.processData86(colX, curY, 118.0F, 24.0F, 7.0F, 1.0F, joinBorder);
      FontText.processData3(FontFace.SEMI, Lang.tr("▷ Зайти на сервер", "▷ Join server"), colX + 12.0F, curY + 8.0F, 7.2F, 0xFFFFFFFF);
      this.clickAreas.add(new ClickArea(colX, curY, 118.0F, 24.0F, () -> {
         String nick = this.nickText.trim();
         if (AltBot.isNickValid(nick)) {
            this.module.addBot(nick, this.module.getPassword());
            this.nickText = "";
            this.focusedField = -1;
         } else {
            this.focusedField = NICK_FIELD;
         }
      }));

      boolean restoreHover = mx >= colX + 126.0F && mx <= colX + 250.0F && my >= curY && my <= curY + 24.0F;
      int restoreBg = restoreHover ? FallenUiHelper.COLOR_BTN_DARK_HOVER : 0xFF0B0D10;
      int restoreBorder = restoreHover ? FallenUiHelper.COLOR_BTN_BORDER_HOVER : FallenUiHelper.COLOR_BTN_BORDER;
      Render2D.processData51(colX + 126.0F, curY, 124.0F, 24.0F, 7.0F, restoreBg);
      Render2D.processData86(colX + 126.0F, curY, 124.0F, 24.0F, 7.0F, 1.0F, restoreBorder);
      FontText.processData3(FontFace.SEMI, Lang.tr("↺ Восстановить всех", "↺ Restore all"), colX + 140.0F, curY + 8.0F, 7.0F, restoreHover ? 0xFFFFFFFF : FallenUiHelper.COLOR_TEXT_PRIMARY);
      this.clickAreas.add(new ClickArea(colX + 126.0F, curY, 124.0F, 24.0F, () -> this.module.restoreSavedAlts()));
      curY += 34.0F;

      // Список альтов
      Render2D.processData51(colX, curY, cardW - 48.0F, 118.0F, 8.0F, 0xFF0B0D10);
      Render2D.processData86(colX, curY, cardW - 48.0F, 118.0F, 8.0F, 1.0F, FallenUiHelper.COLOR_BTN_BORDER);
      FontText.processData3(FontFace.SEMI, Lang.tr("АККАУНТЫ", "ACCOUNTS"), colX + 12.0F, curY + 7.0F, 6.0F, FallenUiHelper.COLOR_TEXT_MUTED);

      float rowY = curY + 20.0F;
      List<AltBot> bots = this.module.getBots();
      if (bots.isEmpty()) {
         FontText.processData3(FontFace.SEMI, Lang.tr("Нет альтов. Введи ник и нажми «Зайти на сервер».", "No alts. Type a nickname and press Join."), colX + 12.0F, rowY + 8.0F, 6.5F, FallenUiHelper.COLOR_TEXT_POLICY);
      } else {
         int shown = 0;
         for (AltBot bot : bots) {
            if (shown >= 4) {
               FontText.processData3(FontFace.SEMI, "... и ещё " + (bots.size() - shown), colX + 12.0F, rowY + 6.0F, 5.5F, FallenUiHelper.COLOR_TEXT_MUTED);
               break;
            }

            AltBot.Status st = bot.getStatus();
            boolean online = bot.isOnServer();
            boolean closed = st == AltBot.Status.CLOSED;
            String stStr;
            int stColor;
            switch (st) {
               case PLAYING -> {
                  stStr = Lang.tr("● на сервере", "● online");
                  stColor = FallenUiHelper.COLOR_BTN_ACCENT_HOVER;
               }
               case DEAD -> {
                  stStr = Lang.tr("● мёртв (сам возродится)", "● dead (auto-respawn)");
                  stColor = 0xFFFF8A9E;
               }
               case CLOSED -> {
                  stStr = "○ " + bot.getStatusText();
                  stColor = FallenUiHelper.COLOR_TEXT_MUTED;
               }
               default -> {
                  stStr = "○ " + bot.getStatusText();
                  stColor = FallenUiHelper.COLOR_TEXT_SECONDARY;
               }
            }

            String stats = online
               ? String.format(java.util.Locale.US, "ХП %.1f  Голод %d  Пинг %s", bot.getHealth() < 0.0F ? 0.0F : bot.getHealth(), Math.max(0, bot.getFood()), bot.getPing() < 0 ? Lang.tr("н/д", "n/a") : bot.getPing() + "мс")
               : "";
            boolean expanded = this.expandedBots.contains(bot.name);

            if (expanded) {
               String infoLine = Lang.tr("пароль: ", "password: ") + (bot.getPassword().isEmpty() ? "—" : bot.getPassword())
                  + Lang.tr("  ·  на сервере: ", "  ·  online: ") + AltBot.formatOnline(bot.getOnlineSeconds())
                  + Lang.tr("  ·  режим: ", "  ·  mode: ") + bot.getModeLabel();
               FontText.processData3(FontFace.SEMI, infoLine, colX + 12.0F, rowY + 21.0F, 5.5F, 0xFFC4B5FD);
               java.util.List<String> msgs = bot.getLastMessages();
               AltBot driveTgt = this.module.getDriveTarget();
               boolean driving = driveTgt == bot;
               float driveW = 120.0F;
               float driveX = colX + 12.0F;
               float driveY = rowY + 31.0F + Math.min(2, msgs.size()) * 10.0F + 2.0F;
               boolean driveHover = mx >= driveX && mx <= driveX + driveW && my >= driveY && my <= driveY + 18.0F;
               Render2D.processData51(driveX, driveY, driveW, 18.0F, 5.0F, driving
                  ? (driveHover ? FallenUiHelper.COLOR_BTN_ACCENT_HOVER : FallenUiHelper.COLOR_BTN_ACCENT)
                  : (driveHover ? 0xFF3A2B5C : 0xFF251B38));
               Render2D.processData86(driveX, driveY, driveW, 18.0F, 5.0F, 1.0F, driving ? FallenUiHelper.COLOR_BTN_ACCENT_BORDER : (driveHover ? FallenUiHelper.COLOR_BTN_ACCENT : FallenUiHelper.COLOR_BTN_BORDER));
               FontText.processData3(FontFace.SEMI, driving ? Lang.tr("✋ Встать (свой акк)", "✋ Stand up") : Lang.tr("🎮 Сесть за альт", "🎮 Drive alt"), driveX + 10.0F, driveY + 4.5F, 6.0F, driving ? 0xFFFFFFFF : 0xFFC4B5FD);
               this.clickAreas.add(new ClickArea(driveX, driveY, driveW, 18.0F, () -> this.module.setDriveTarget(driving ? null : bot)));

               java.util.List<String> unused = msgs;

               for (int mIdx = msgs.size() - 1; mIdx >= 0 && mIdx >= msgs.size() - 2; mIdx--) {
                  String m = msgs.get(mIdx);
                  if (m.length() > 60) {
                     m = m.substring(0, 60);
                  }

                  FontText.processData3(FontFace.SEMI, "› " + m, colX + 12.0F, rowY + 31.0F + (msgs.size() - 1 - mIdx) * 10.0F, 5.5F, FallenUiHelper.COLOR_TEXT_SECONDARY);
               }
            }

            float btnX = colX + (cardW - 48.0F) - 56.0F;
            float infoBtnX = btnX - 20.0F;
            boolean infoHover = mx >= infoBtnX && mx <= infoBtnX + 17.0F && my >= rowY && my <= rowY + 20.0F;
            Render2D.processData51(infoBtnX, rowY + 2.0F, 17.0F, 17.0F, 4.0F, infoHover ? 0xFF3A2B5C : 0xFF251B38);
            Render2D.processData86(infoBtnX, rowY + 2.0F, 17.0F, 17.0F, 4.0F, 1.0F, expanded ? FallenUiHelper.COLOR_BTN_ACCENT : FallenUiHelper.COLOR_BTN_BORDER);
            FontText.processData3(FontFace.SEMI, "i", infoBtnX + 6.0F, rowY + 5.5F, 6.5F, expanded ? 0xFFD8B4FE : FallenUiHelper.COLOR_TEXT_SECONDARY);
            this.clickAreas.add(new ClickArea(infoBtnX, rowY, 17.0F, 20.0F, () -> {
               if (!this.expandedBots.remove(bot.name)) {
                  this.expandedBots.add(bot.name);
               }
            }));

            boolean btnHover = mx >= btnX && mx <= btnX + 44.0F && my >= rowY && my <= rowY + 20.0F;
            if (closed) {
               Render2D.processData51(btnX, rowY + 2.0F, 44.0F, 17.0F, 4.0F, btnHover ? FallenUiHelper.COLOR_BTN_ACCENT_HOVER : FallenUiHelper.COLOR_BTN_ACCENT);
               FontText.processData3(FontFace.SEMI, Lang.tr("Ещё раз", "Retry"), btnX + 6.0F, rowY + 6.0F, 5.0F, 0xFFFFFFFF);
               this.clickAreas.add(new ClickArea(btnX, rowY, 44.0F, 20.0F, () -> {
                  if (this.module.getCurrentServerAddress() != null) {
                     bot.retry();
                  }
               }));
            } else {
               Render2D.processData51(btnX, rowY + 2.0F, 44.0F, 17.0F, 4.0F, btnHover ? 0xFF6D3340 : 0xFF34232A);
               FontText.processData3(FontFace.SEMI, "×", btnX + 19.0F, rowY + 6.0F, 6.0F, 0xFFFFD9DF);
               this.clickAreas.add(new ClickArea(btnX, rowY, 44.0F, 20.0F, () -> this.module.removeBot(bot)));
            }

            boolean isChatTarget = bot.name.equals(this.chatTargetName);
            FontText.processData3(FontFace.SEMI, (isChatTarget ? "▶ " : "") + bot.name, colX + 12.0F, rowY + 6.5F, 7.5F, isChatTarget ? FallenUiHelper.COLOR_BTN_ACCENT_HOVER : FallenUiHelper.COLOR_TEXT_PRIMARY);
            this.clickAreas.add(new ClickArea(colX + 6.0F, rowY, 84.0F, 20.0F, () -> {
               this.chatTargetName = isChatTarget ? "" : bot.name;
            }));
            FontText.processData3(FontFace.SEMI, stStr, colX + 96.0F, rowY + 7.5F, 6.0F, stColor);
            if (!stats.isEmpty()) {
               FontText.processData3(FontFace.SEMI, stats, colX + 190.0F, rowY + 7.5F, 6.0F, FallenUiHelper.COLOR_TEXT_SECONDARY);
            }

            rowY += expanded ? 62.0F : 23.0F;
            shown++;
         }
      }

      curY += 126.0F;

      // Лента чата альтов
      float feedH = 74.0F;
      Render2D.processData51(colX, curY, cardW - 48.0F, feedH, 8.0F, 0xFF0B0D10);
      Render2D.processData86(colX, curY, cardW - 48.0F, feedH, 8.0F, 1.0F, FallenUiHelper.COLOR_BTN_BORDER);
      String feedTitle = this.chatTargetName.isEmpty()
         ? Lang.tr("ЛЕНТА ЧАТА АЛЬТОВ (ВСЕ)", "ALT CHAT FEED (ALL)")
         : Lang.tr("ЛЕНТА ЧАТА: ", "CHAT FEED: ") + this.chatTargetName.toUpperCase(java.util.Locale.ROOT);
      FontText.processData3(FontFace.SEMI, feedTitle, colX + 12.0F, curY + 6.0F, 5.5F, FallenUiHelper.COLOR_TEXT_MUTED);

      // собираем ленту
      ArrayList<String[]> feed = new ArrayList<>();
      for (AltBot fb : this.module.getBots()) {
         if (!this.chatTargetName.isEmpty() && !fb.name.equals(this.chatTargetName)) {
            continue;
         }

         for (String m : fb.getLastMessages()) {
            feed.add(new String[]{fb.name, m});
         }
      }

      feed.sort((a, b) -> 0);
      int feedShown = 5;
      int feedStart = Math.max(0, feed.size() - feedShown - this.feedScroll);
      float feedTextY = curY + 17.0F;

      for (int fIdx = feedStart; fIdx < feed.size() && fIdx < feedStart + feedShown; fIdx++) {
         String[] line = feed.get(fIdx);
         String text = "‹" + line[0] + "› " + line[1];
         if (text.length() > 92) {
            text = text.substring(0, 92);
         }

         FontText.processData3(FontFace.SEMI, text, colX + 12.0F, feedTextY, 5.5F, FallenUiHelper.COLOR_TEXT_SECONDARY);
         feedTextY += 10.5F;
      }

      this.feedBoxX = colX;
      this.feedBoxY = curY;
      this.feedBoxW = cardW - 48.0F;
      this.feedBoxH = feedH;
      curY += feedH + 8.0F;

      // Чат
      String chatLabel = this.chatTargetName.isEmpty()
         ? Lang.tr("ЧАТ (ОТПРАВИТЬ ОТ ВСЕХ)", "CHAT (SEND FROM ALL)")
         : Lang.tr("ЧАТ → только ", "CHAT → only ") + this.chatTargetName;
      this.renderInputField(chatLabel, this.chatText, colX, curY, 250.0F, CHAT_FIELD, mx, my);

      boolean sendHover = mx >= colX + 258.0F && mx <= colX + 342.0F && my >= curY + 8.0F && my <= curY + 30.0F;
      int sendBg = sendHover ? FallenUiHelper.COLOR_BTN_ACCENT_HOVER : FallenUiHelper.COLOR_BTN_ACCENT;
      Render2D.processData51(colX + 258.0F, curY + 8.0F, 84.0F, 22.0F, 6.0F, sendBg);
      Render2D.processData86(colX + 258.0F, curY + 8.0F, 84.0F, 22.0F, 6.0F, 1.0F, FallenUiHelper.COLOR_BTN_ACCENT_BORDER);
      FontText.processData3(FontFace.SEMI, Lang.tr("Отправить", "Send"), colX + 278.0F, curY + 14.0F, 6.8F, 0xFFFFFFFF);
      this.clickAreas.add(new ClickArea(colX + 258.0F, curY + 8.0F, 84.0F, 22.0F, () -> {
         String msg = this.chatText.trim();
         if (!msg.isEmpty()) {
            if (this.chatTargetName.isEmpty()) {
               this.module.sendChatFromAll(msg);
            } else {
               for (AltBot b : this.module.getBots()) {
                  if (b.name.equals(this.chatTargetName)) {
                     this.module.sendChatToBot(b, msg);
                     break;
                  }
               }
            }

            this.chatText = "";
            this.focusedField = -1;
         }
      }));

      // Кнопка закрытия
      boolean closeHover = mx >= cardX + cardW - 44.0F && mx <= cardX + cardW - 20.0F && my >= cardY - 6.0F && my <= cardY + 18.0F;
      Render2D.processData51(cardX + cardW - 44.0F, cardY - 6.0F, 24.0F, 24.0F, 6.0F, closeHover ? 0xFF6D3340 : 0xFF34232A);
      FontText.processData3(FontFace.SEMI, "×", cardX + cardW - 35.0F, cardY - 1.0F, 8.0F, 0xFFFFD9DF);
      this.clickAreas.add(new ClickArea(cardX + cardW - 44.0F, cardY - 6.0F, 24.0F, 24.0F, this::close));

      Render2D.updateState();
      Render2D.updateState6();
   }

   private String maskPass(String pass) {
      if (pass == null || pass.isEmpty()) return "";
      return "•".repeat(Math.min(pass.length(), 12));
   }

   private void renderInputField(String label, String value, float x, float y, float w, int field, float mx, float my) {
      FontText.processData3(FontFace.SEMI, label, x, y, 5.5F, FallenUiHelper.COLOR_TEXT_MUTED);
      float boxY = y + 8.0F;
      float boxH = 22.0F;
      boolean isFoc = this.focusedField == field;
      boolean hov = mx >= x && mx <= x + w && my >= boxY && my <= boxY + boxH;

      int border = isFoc || hov ? FallenUiHelper.COLOR_BTN_BORDER_HOVER : FallenUiHelper.COLOR_BTN_BORDER;
      Render2D.processData51(x, boxY, w, boxH, 6.0F, 0xFF0B0D10);
      Render2D.processData86(x, boxY, w, boxH, 6.0F, 1.0F, border);

      String displayVal = value == null || value.isEmpty() ? (isFoc ? "" : "...") : value;
      if (isFoc && (System.currentTimeMillis() / 400) % 2 == 0) {
         displayVal = displayVal + "|";
      }

      FontText.processData3(FontFace.SEMI, displayVal, x + 8.0F, boxY + 6.5F, 7.0F, isFoc ? 0xFFFFFFFF : FallenUiHelper.COLOR_TEXT_PRIMARY);
      this.clickAreas.add(new ClickArea(x, boxY, w, boxH, () -> this.focusedField = field));
   }

   @Override
   public boolean mouseClicked(Click click, boolean bl) {
      if (click.button() == 0) {
         float mx = (float)click.x() * sf();
         float my = (float)click.y() * sf();

         boolean clickedAny = false;
         for (int i = this.clickAreas.size() - 1; i >= 0; i--) {
            ClickArea area = this.clickAreas.get(i);
            if (mx >= area.x && mx <= area.x + area.w && my >= area.y && my <= area.y + area.h) {
               area.action.run();
               clickedAny = true;
               break;
            }
         }

         if (!clickedAny) {
            this.focusedField = -1;
         }

         return true;
      }

      return super.mouseClicked(click, bl);
   }

   @Override
   public boolean keyPressed(KeyInput keyInput) {
      int key = keyInput.key();
      if (key == GLFW.GLFW_KEY_ESCAPE) {
         this.close();
         return true;
      }

      if (this.focusedField != -1) {
         if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (this.focusedField == NICK_FIELD && !this.nickText.isEmpty()) {
               this.nickText = this.nickText.substring(0, this.nickText.length() - 1);
            } else if (this.focusedField == CHAT_FIELD && !this.chatText.isEmpty()) {
               this.chatText = this.chatText.substring(0, this.chatText.length() - 1);
            } else if (this.focusedField == PASS_FIELD && !this.passText.isEmpty()) {
               this.passText = this.passText.substring(0, this.passText.length() - 1);
            }

            return true;
         }

         if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            if (this.focusedField == NICK_FIELD) {
               String nick = this.nickText.trim();
               if (AltBot.isNickValid(nick)) {
                  this.module.addBot(nick, this.module.getPassword());
                  this.nickText = "";
                  this.focusedField = -1;
               }

               return true;
            }

            if (this.focusedField == CHAT_FIELD) {
               String msg = this.chatText.trim();
               if (!msg.isEmpty()) {
                  if (this.chatTargetName.isEmpty()) {
                     this.module.sendChatFromAll(msg);
                  } else {
                     for (AltBot b : this.module.getBots()) {
                        if (b.name.equals(this.chatTargetName)) {
                           this.module.sendChatToBot(b, msg);
                           break;
                        }
                     }
                  }

                  this.chatText = "";
                  this.focusedField = -1;
               }

               return true;
            }
         }

         if (key == GLFW.GLFW_KEY_TAB) {
            this.focusedField = this.focusedField == NICK_FIELD ? PASS_FIELD : (this.focusedField == PASS_FIELD ? CHAT_FIELD : NICK_FIELD);
            return true;
         }

         if (key == GLFW.GLFW_KEY_V
            && (GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == 1
               || GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == 1)) {
            String clip = MinecraftClient.getInstance().keyboard.getClipboard();
            if (clip != null && !clip.isEmpty()) {
               if (this.focusedField == NICK_FIELD) {
                  this.nickText = this.nickText + clip.trim();
               } else if (this.focusedField == PASS_FIELD) {
                  this.passText = this.passText + clip.trim();
               } else {
                  this.chatText = this.chatText + clip;
               }
            }

            return true;
         }
      }

      return super.keyPressed(keyInput);
   }

   @Override
   public boolean charTyped(CharInput charInput) {
      if (this.focusedField != -1 && charInput.codepoint() >= 32) {
         char c = (char)charInput.codepoint();
         if (this.focusedField == NICK_FIELD) {
            if (this.nickText.length() < 16) {
               this.nickText = this.nickText + c;
            }
         } else if (this.focusedField == PASS_FIELD) {
            if (this.passText.length() < 24) {
               this.passText = this.passText + c;
            }
         } else if (this.chatText.length() < 200) {
            this.chatText = this.chatText + c;
         }

         return true;
      }

      return super.charTyped(charInput);
   }
}

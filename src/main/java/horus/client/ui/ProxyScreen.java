package horus.client.ui;

import horus.client.proxy.ProxyManager;
import horus.client.ui.AnimatedValue;
import horus.client.ui.UiTheme;
import horus.client.render.FontFace;
import horus.client.render.FontText;
import horus.client.render.Render2D;
import horus.client.util.GuiScale;
import horus.client.util.Lang;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import ru.dreamix.protection.api.impl.profile.ProfileAPI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ProxyScreen extends Screen {
   private final Screen parent;
   private final ProxyManager proxyManager = ProxyManager.getInstance();

   private enum FocusedField {
      NONE,
      HOST,
      PORT,
      USERNAME,
      PASSWORD
   }

   private FocusedField focused = FocusedField.NONE;
   private String testResultText = "";
   private final List<ClickArea> clickAreas = new ArrayList<>();
   private final Map<String, ProfileRow> profileRows = new LinkedHashMap<>();
   private int profileScroll;
   private int visibleProfileRows = 2;
   private float profileListTop;
   private float profileListBottom;
   private float profileListX;
   private float profileListWidth;
   private final AnimatedValue openAnim = new AnimatedValue(260L);
   private final AnimatedValue closeAnim = new AnimatedValue(180L);
   private final AnimatedValue serversSectionAnim = new AnimatedValue(200L);
   private boolean closing = false;

   private record ClickArea(float x, float y, float w, float h, Runnable action) {}

   private static final class ProfileRow {
      private ProxyManager.ProxyProfile profile;
      private final AnimatedValue animation = new AnimatedValue(200L).getAnimatedValue(0.0);
      private boolean removing;

      private ProfileRow(ProxyManager.ProxyProfile profile) {
         this.profile = profile;
      }
   }

   public ProxyScreen(Screen parent) {
      super(Text.literal("Fallen — Прокси"));
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
      this.syncProfileRows();

      Render2D.renderElement5(drawContext);

      this.openAnim.processData(1.0);
      float oVal = UiTheme.computeFloat2((float) this.openAnim.computeDouble());

      if (this.closing) {
         this.closeAnim.processData(1.0);
         float cVal = UiTheme.computeFloat2((float) this.closeAnim.computeDouble());
         if (cVal >= 0.98F) {
            if (this.client != null) {
               this.client.setScreen(this.parent);
            }
            return;
         }
         oVal *= (1.0F - cVal);
      }

      // Deep solid minimalist dark background (#0B0D10)
      Render2D.processData51(0.0F, 0.0F, screenW, screenH, 0.0F, FallenUiHelper.COLOR_BG);

      if (oVal < 0.999F) {
         Render2D.processData46(Math.max(0.01F, oVal));
      }

      // Top and bottom chrome dividers (#383E4C)
      Render2D.processData51(0.0F, 32.0F * sf(), screenW, 1.0F, 0.0F, FallenUiHelper.COLOR_BTN_BORDER);
      Render2D.processData51(0.0F, screenH - 32.0F * sf(), screenW, 1.0F, 0.0F, FallenUiHelper.COLOR_BTN_BORDER);

      // Top-Left User Chrome
      String username = "Lumira";
      try {
         if (ProfileAPI.get() != null && ProfileAPI.get().getModel() != null && ProfileAPI.get().getModel().getName() != null) {
            username = ProfileAPI.get().getModel().getName();
         } else if (this.client != null && this.client.getSession() != null) {
            username = this.client.getSession().getUsername();
         }
      } catch (Exception ignored) {}

      float avatarX = 12.0F * sf();
      float avatarY = 8.0F * sf();
      float avatarSize = 16.0F * sf();

      Render2D.processData51(avatarX, avatarY, avatarSize, avatarSize, 2.0F, 0xFFE9E4FF);
      Render2D.processData51(avatarX, avatarY, avatarSize, 4.0F * sf(), 1.0F, 0xFF3A2B5C);
      Render2D.processData51(avatarX + 2.0F * sf(), avatarY + 7.0F * sf(), 4.0F * sf(), 2.0F * sf(), 0.0F, 0xFFFFFFFF);
      Render2D.processData51(avatarX + 10.0F * sf(), avatarY + 7.0F * sf(), 4.0F * sf(), 2.0F * sf(), 0.0F, 0xFFFFFFFF);
      Render2D.processData51(avatarX + 4.0F * sf(), avatarY + 7.0F * sf(), 2.0F * sf(), 2.0F * sf(), 0.0F, 0xFF4F3E8C);
      Render2D.processData51(avatarX + 10.0F * sf(), avatarY + 7.0F * sf(), 2.0F * sf(), 2.0F * sf(), 0.0F, 0xFF4F3E8C);
      Render2D.processData51(avatarX + 6.0F * sf(), avatarY + 11.0F * sf(), 4.0F * sf(), 2.0F * sf(), 0.0F, 0xFF9D7BEA);

      FontText.processData3(FontFace.SEMI, "Logged in as", avatarX + avatarSize + 6.0F * sf(), avatarY + 1.5F * sf(), 5.5F * sf(), FallenUiHelper.COLOR_TEXT_MUTED);
      FontText.processData3(FontFace.SEMI, username, avatarX + avatarSize + 6.0F * sf(), avatarY + 8.5F * sf(), 7.5F * sf(), FallenUiHelper.COLOR_TEXT_PRIMARY);

      // Center modal card
      float cardW = Math.min(screenW - 40.0F, 580.0F);
      float cardH = Math.min(screenH - 70.0F, 300.0F);
      float cardX = (screenW - cardW) / 2.0F;
      float enterDrift = (1.0F - oVal) * 16.0F * sf();
      float cardY = (screenH - cardH) / 2.0F + enterDrift;

      // Card container (#14171D with #383E4C border, radius 14px)
      Render2D.processData51(cardX, cardY, cardW, cardH, 14.0F, FallenUiHelper.COLOR_BTN_DARK);
      Render2D.processData86(cardX, cardY, cardW, cardH, 14.0F, 1.0F, FallenUiHelper.COLOR_BTN_BORDER);

      // Header title & category
      FontText.processData3(FontFace.SEMI, Lang.tr("HORUS CLIENT  /  ПРОКСИ", "HORUS CLIENT  /  PROXY"), cardX + 24.0F, cardY + 18.0F, 6.0F, FallenUiHelper.COLOR_TEXT_MUTED);
      FontText.processData3(FontFace.SEMI, Lang.tr("Настройки соединения", "Connection settings"), cardX + 24.0F, cardY + 30.0F, 13.0F, FallenUiHelper.COLOR_TEXT_PRIMARY);

      // Status Chip (top right of card)
      boolean isEnabled = this.proxyManager.isEnabled();
      String statusStr = isEnabled
         ? "● " + this.proxyManager.getType().getDisplayName() + ": " + this.proxyManager.getHost() + ":" + this.proxyManager.getPort()
         : Lang.tr("○ Прокси отключён", "○ Proxy off");
      int statusColor = isEnabled ? FallenUiHelper.COLOR_BTN_ACCENT_HOVER : FallenUiHelper.COLOR_TEXT_MUTED;
      int statusBg = isEnabled ? 0x2E8B5CF6 : 0xFF0B0D10;
      int statusBorder = isEnabled ? FallenUiHelper.COLOR_BTN_ACCENT : FallenUiHelper.COLOR_BTN_BORDER;

      float chipW = 160.0F;
      float chipH = 22.0F;
      float chipX = cardX + cardW - chipW - 24.0F;
      float chipY = cardY + 22.0F;
      Render2D.processData51(chipX, chipY, chipW, chipH, 7.0F, statusBg);
      Render2D.processData86(chipX, chipY, chipW, chipH, 7.0F, 1.0F, statusBorder);
      FontText.processData3(FontFace.SEMI, statusStr, chipX + 8.0F, chipY + 6.0F, 6.5F, statusColor);

      // Card divider line
      Render2D.processData51(cardX + 24.0F, cardY + 56.0F, cardW - 48.0F, 1.0F, 0.0F, FallenUiHelper.COLOR_BTN_BORDER);

      // Left Column (Inputs & Controls)
      float colX = cardX + 24.0F;
      float curY = cardY + 68.0F;

      // 1. Toggle Button
      boolean hoverToggle = mx >= colX && mx <= colX + 245.0F && my >= curY && my <= curY + 28.0F;
      int toggleBg = isEnabled
         ? (hoverToggle ? FallenUiHelper.COLOR_BTN_ACCENT_HOVER : FallenUiHelper.COLOR_BTN_ACCENT)
         : (hoverToggle ? FallenUiHelper.COLOR_BTN_DARK_HOVER : 0xFF0B0D10);
      int toggleBorder = hoverToggle ? FallenUiHelper.COLOR_BTN_BORDER_HOVER : FallenUiHelper.COLOR_BTN_BORDER;
      int toggleTextColor = isEnabled ? 0xFFFFFFFF : (hoverToggle ? FallenUiHelper.COLOR_TEXT_PRIMARY : FallenUiHelper.COLOR_TEXT_SECONDARY);

      Render2D.processData51(colX, curY, 245.0F, 28.0F, 8.0F, toggleBg);
      Render2D.processData86(colX, curY, 245.0F, 28.0F, 8.0F, 1.0F, toggleBorder);
      String toggleText = isEnabled ? Lang.tr("✓ Прокси активен (нажмите, чтобы выкл)", "✓ Proxy active (click to disable)") : Lang.tr("▷ Включить прокси", "▷ Enable proxy");
      FontText.processData3(FontFace.SEMI, toggleText, colX + 16.0F, curY + 8.5F, 7.8F, toggleTextColor);
      this.clickAreas.add(new ClickArea(colX, curY, 245.0F, 28.0F, () -> {
         this.proxyManager.setEnabled(!this.proxyManager.isEnabled());
      }));
      curY += 36.0F;

      // 2. Type Selector Tabs: [ SOCKS5 ] [ SOCKS4 ] [ HTTP ]
      FontText.processData3(FontFace.SEMI, Lang.tr("ТИП ПРОКСИ:", "PROXY TYPE:"), colX, curY, 6.0F, FallenUiHelper.COLOR_TEXT_MUTED);
      curY += 10.0F;
      ProxyManager.ProxyType[] types = ProxyManager.ProxyType.values();
      float tabW = 77.0F;
      for (int i = 0; i < types.length; i++) {
         ProxyManager.ProxyType t = types[i];
         float tx = colX + i * (tabW + 7.0F);
         boolean isSel = this.proxyManager.getType() == t;
         boolean hov = mx >= tx && mx <= tx + tabW && my >= curY && my <= curY + 20.0F;
         int tBg = isSel ? FallenUiHelper.COLOR_BTN_ACCENT : (hov ? FallenUiHelper.COLOR_BTN_DARK_HOVER : 0xFF0B0D10);
         int tBorder = isSel || hov ? FallenUiHelper.COLOR_BTN_BORDER_HOVER : FallenUiHelper.COLOR_BTN_BORDER;
         int tColor = isSel ? 0xFFFFFFFF : (hov ? FallenUiHelper.COLOR_TEXT_PRIMARY : FallenUiHelper.COLOR_TEXT_SECONDARY);

         Render2D.processData51(tx, curY, tabW, 20.0F, 6.0F, tBg);
         Render2D.processData86(tx, curY, tabW, 20.0F, 6.0F, 1.0F, tBorder);
         FontText.processData3(FontFace.SEMI, t.getDisplayName(), tx + 16.0F, curY + 5.5F, 7.0F, tColor);
         this.clickAreas.add(new ClickArea(tx, curY, tabW, 20.0F, () -> {
            this.proxyManager.setType(t);
            if (this.proxyManager.isEnabled()) this.proxyManager.applyProxy();
         }));
      }
      curY += 28.0F;

      // 3. Host and Port Fields
      float hostW = 165.0F;
      float portW = 74.0F;
      renderInputField(Lang.tr("СЕРВЕР / IP ПРОКСИ", "SERVER / PROXY IP"), this.proxyManager.getHost(), colX, curY, hostW, FocusedField.HOST, mx, my);
      renderInputField(Lang.tr("ПОРТ", "PORT"), String.valueOf(this.proxyManager.getPort()), colX + hostW + 6.0F, curY, portW, FocusedField.PORT, mx, my);
      curY += 40.0F;

      // 4. Username and Password Fields
      float authW = 119.0F;
      renderInputField(Lang.tr("ЛОГИН (ОПЦИОНАЛЬНО)", "USERNAME (OPTIONAL)"), this.proxyManager.getUsername(), colX, curY, authW, FocusedField.USERNAME, mx, my);
      renderInputField(Lang.tr("ПАРОЛЬ (ОПЦИОНАЛЬНО)", "PASSWORD (OPTIONAL)"), maskPass(this.proxyManager.getPassword()), colX + authW + 7.0F, curY, authW, FocusedField.PASSWORD, mx, my);
      curY += 40.0F;

      // 5. Action Buttons: [ Проверить пинг ] [ Сохранить ]
      boolean hovTest = mx >= colX && mx <= colX + 130.0F && my >= curY && my <= curY + 24.0F;
      int testBg = hovTest ? FallenUiHelper.COLOR_BTN_DARK_HOVER : 0xFF0B0D10;
      int testBorder = hovTest ? FallenUiHelper.COLOR_BTN_BORDER_HOVER : FallenUiHelper.COLOR_BTN_BORDER;
      Render2D.processData51(colX, curY, 130.0F, 24.0F, 7.0F, testBg);
      Render2D.processData86(colX, curY, 130.0F, 24.0F, 7.0F, 1.0F, testBorder);
      int pingIconColor = hovTest ? 0xFFD8B4FE : FallenUiHelper.COLOR_BTN_ACCENT_HOVER;
      Render2D.processData89(
              "assets/horus/textures/gui/hud/hud_ping.png",
              colX + 10.0F,
              curY + 5.0F,
              14.0F,
              14.0F,
              0.0F,
              pingIconColor
      );
      FontText.processData3(FontFace.SEMI, Lang.tr("Проверить пинг", "Test ping"), colX + 30.0F, curY + 7.0F, 7.0F, hovTest ? 0xFFFFFFFF : FallenUiHelper.COLOR_TEXT_PRIMARY);
      this.clickAreas.add(new ClickArea(colX, curY, 130.0F, 24.0F, () -> {
         this.testResultText = Lang.tr("Проверка...", "Testing...");
         this.proxyManager.testConnection(res -> this.testResultText = res);
      }));

      boolean hovSave = mx >= colX + 138.0F && mx <= colX + 245.0F && my >= curY && my <= curY + 24.0F;
      int saveBg = hovSave ? FallenUiHelper.COLOR_BTN_ACCENT_HOVER : FallenUiHelper.COLOR_BTN_ACCENT;
      int saveBorder = hovSave ? FallenUiHelper.COLOR_BTN_BORDER_HOVER : FallenUiHelper.COLOR_BTN_BORDER;
      Render2D.processData51(colX + 138.0F, curY, 107.0F, 24.0F, 7.0F, saveBg);
      Render2D.processData86(colX + 138.0F, curY, 107.0F, 24.0F, 7.0F, 1.0F, saveBorder);
      FontText.processData3(FontFace.SEMI, Lang.tr("💾 Сохранить", "💾 Save"), colX + 158.0F, curY + 7.0F, 7.0F, 0xFFFFFFFF);
      this.clickAreas.add(new ClickArea(colX + 138.0F, curY, 107.0F, 24.0F, () -> {
         this.testResultText = Lang.tr("Проверка прокси...", "Checking proxy...");
         this.proxyManager.verifyAndSaveCurrentProfile(result -> MinecraftClient.getInstance().execute(() -> {
            this.testResultText = result;
            this.syncProfileRows();
         }));
      }));
      curY += 32.0F;

      // Failover toggle (auto-switch on dead proxy)
      boolean failoverOn = this.proxyManager.isFailover();
      boolean hovFail = mx >= colX && mx <= colX + 245.0F && my >= curY && my <= curY + 24.0F;
      int failBg = failoverOn
         ? (hovFail ? FallenUiHelper.COLOR_BTN_ACCENT_HOVER : FallenUiHelper.COLOR_BTN_ACCENT)
         : (hovFail ? FallenUiHelper.COLOR_BTN_DARK_HOVER : 0xFF0B0D10);
      int failBorder = hovFail ? FallenUiHelper.COLOR_BTN_BORDER_HOVER : FallenUiHelper.COLOR_BTN_BORDER;
      int failText = failoverOn ? 0xFFFFFFFF : (hovFail ? FallenUiHelper.COLOR_TEXT_PRIMARY : FallenUiHelper.COLOR_TEXT_SECONDARY);
      Render2D.processData51(colX, curY, 245.0F, 24.0F, 8.0F, failBg);
      Render2D.processData86(colX, curY, 245.0F, 24.0F, 8.0F, 1.0F, failBorder);
      String failLabel = Lang.tr("Автопереключение при обрыве: ", "Failover on disconnect: ")
         + (failoverOn ? Lang.tr("ВКЛ", "ON") : Lang.tr("ВЫКЛ", "OFF"));
      FontText.processData3(FontFace.SEMI, failLabel, colX + 16.0F, curY + 7.0F, 7.0F, failText);
      this.clickAreas.add(new ClickArea(colX, curY, 245.0F, 24.0F, () -> {
         this.proxyManager.setFailover(!this.proxyManager.isFailover());
         this.testResultText = Lang.tr("Автопереключение: ", "Failover: ") + (this.proxyManager.isFailover() ? Lang.tr("ВКЛ", "ON") : Lang.tr("ВЫКЛ", "OFF"));
      }));

      // Right Column (Presets & Info Card)
      float rightColX = cardX + 295.0F;
      float rightColW = cardW - 319.0F;
      float rCurY = cardY + 68.0F;

      // Status Info Box (#0B0D10 with #383E4C border)
      Render2D.processData51(rightColX, rCurY, rightColW, 90.0F, 8.0F, 0xFF0B0D10);
      Render2D.processData86(rightColX, rCurY, rightColW, 90.0F, 8.0F, 1.0F, FallenUiHelper.COLOR_BTN_BORDER);
      FontText.processData3(FontFace.SEMI, Lang.tr("ИНФОРМАЦИЯ О СОЕДИНЕНИИ", "CONNECTION INFO"), rightColX + 12.0F, rCurY + 10.0F, 6.0F, FallenUiHelper.COLOR_TEXT_MUTED);

      String pingDisplay = this.proxyManager.getCurrentPing() >= 0 ? this.proxyManager.getCurrentPing() + " ms" : Lang.tr("Не замерялся", "Not measured");
      FontText.processData3(FontFace.SEMI, Lang.tr("Задержка (Пинг): ", "Ping: ") + pingDisplay, rightColX + 12.0F, rCurY + 24.0F, 7.0F, FallenUiHelper.COLOR_TEXT_PRIMARY);
      FontText.processData3(FontFace.SEMI, Lang.tr("Маршрут: ", "Route: ") + (isEnabled ? Lang.tr("Через прокси-сервер", "Via proxy") : Lang.tr("Прямое соединение", "Direct connection")), rightColX + 12.0F, rCurY + 38.0F, 7.0F, isEnabled ? FallenUiHelper.COLOR_BTN_ACCENT_HOVER : FallenUiHelper.COLOR_TEXT_SECONDARY);

      if (!this.testResultText.isEmpty()) {
         FontText.processData3(FontFace.SEMI, "Статус: " + this.testResultText, rightColX + 12.0F, rCurY + 54.0F, 6.5F, 0xFFC4B5FD);
      } else {
         FontText.processData3(FontFace.SEMI, Lang.tr("Трафик: Minecraft TCP Sockets & Web", "Traffic: Minecraft TCP Sockets & Web"), rightColX + 12.0F, rCurY + 54.0F, 6.5F, FallenUiHelper.COLOR_TEXT_MUTED);
      }
      FontText.processData3(FontFace.SEMI, Lang.tr("Поддерживаются SOCKS5, SOCKS4 и HTTP.", "SOCKS5, SOCKS4 and HTTP are supported."), rightColX + 12.0F, rCurY + 68.0F, 5.5F, FallenUiHelper.COLOR_TEXT_POLICY);
      FontText.processData3(FontFace.SEMI, Lang.tr("Для авторизации укажите логин и пароль.", "Enter username and password for auth."), rightColX + 12.0F, rCurY + 78.0F, 5.5F, FallenUiHelper.COLOR_TEXT_POLICY);

      // Saved servers list (statically below the info box, animated appearance)
      List<ProfileRow> rowList = new ArrayList<>(this.profileRows.values());
      this.serversSectionAnim.processData(rowList.isEmpty() ? 0.0 : 1.0);
      float srvAnim = UiTheme.computeFloat2((float)this.serversSectionAnim.computeDouble());
      float srvY = rCurY + 90.0F + 10.0F;
      float srvAvailable = cardY + cardH - 46.0F - srvY;
      float srvNeeded = 20.0F + rowList.size() * 27.0F + 10.0F;
      float srvListH = Math.max(30.0F, Math.min(srvNeeded, Math.max(30.0F, srvAvailable))) * srvAnim;
      this.profileListX = rightColX;
      this.profileListTop = srvY;
      this.profileListBottom = srvY + srvListH;
      this.profileListWidth = rightColW;
      if (srvAnim > 0.01F) {
         if (srvAnim < 0.999F) {
            Render2D.processData46(srvAnim);
         }
         Render2D.processData51(rightColX, srvY, rightColW, srvListH, 8.0F, 0xFF0B0D10);
         Render2D.processData86(rightColX, srvY, rightColW, srvListH, 8.0F, 1.0F, FallenUiHelper.COLOR_BTN_BORDER);
         FontText.processData3(FontFace.SEMI, Lang.tr("СЕРВЕРА", "SERVERS"), rightColX + 12.0F, srvY + 7.0F, 6.0F, FallenUiHelper.COLOR_TEXT_MUTED);
         if (rowList.isEmpty()) {
            FontText.processData3(FontFace.SEMI, Lang.tr("Нет сохранённых серверов", "No saved servers"), rightColX + 12.0F, srvY + 24.0F, 6.0F, FallenUiHelper.COLOR_TEXT_POLICY);
         } else {
            this.renderProfileRows(rightColX, srvY + 20.0F, rightColW, srvListH - 24.0F, mx, my);
         }
         if (srvAnim < 0.999F) {
            Render2D.updateState();
         }
      }

      // Back Button (Bottom right)
      float btnBackW = rightColW;
      float btnBackH = 26.0F;
      boolean hovBack = mx >= rightColX && mx <= rightColX + btnBackW && my >= cardY + cardH - 42.0F && my <= cardY + cardH - 16.0F;
      int backBg = hovBack ? FallenUiHelper.COLOR_BTN_DARK_HOVER : 0xFF0B0D10;
      int backBorder = hovBack ? FallenUiHelper.COLOR_BTN_BORDER_HOVER : FallenUiHelper.COLOR_BTN_BORDER;

      Render2D.processData51(rightColX, cardY + cardH - 42.0F, btnBackW, btnBackH, 7.0F, backBg);
      Render2D.processData86(rightColX, cardY + cardH - 42.0F, btnBackW, btnBackH, 7.0F, 1.0F, backBorder);
      FontText.processData3(FontFace.SEMI, Lang.tr("← Вернуться в главное меню", "← Back to main menu"), rightColX + 28.0F, cardY + cardH - 34.0F, 7.5F, hovBack ? 0xFFFFFFFF : FallenUiHelper.COLOR_TEXT_PRIMARY);
      this.clickAreas.add(new ClickArea(rightColX, cardY + cardH - 42.0F, btnBackW, btnBackH, this::close));

      // Bottom centered policy disclaimer matching MainMenuScreen
      String line1 = "By logging into your account you agree to all of our policies,";
      String line2 = "including our Privacy Policy and Terms of Service";
      float policySize = 5.2F * sf();
      float pW1 = line1.length() * 0.52F * policySize;
      float pW2 = line2.length() * 0.52F * policySize;
      FontText.processData3(FontFace.SEMI, line1, (screenW - pW1) / 2.0F, screenH - 20.0F * sf(), policySize, FallenUiHelper.COLOR_TEXT_POLICY);
      FontText.processData3(FontFace.SEMI, line2, (screenW - pW2) / 2.0F, screenH - 12.0F * sf(), policySize, FallenUiHelper.COLOR_TEXT_POLICY);

      Render2D.updateState();
      Render2D.updateState6();
   }

   private void syncProfileRows() {
      List<ProxyManager.ProxyProfile> profiles = this.proxyManager.getProfiles();
      for (ProxyManager.ProxyProfile profile : profiles) {
         String key = profileKey(profile);
         ProfileRow row = this.profileRows.computeIfAbsent(key, ignored -> new ProfileRow(profile));
         row.profile = profile;
         if (!row.removing) {
            row.animation.processData(1.0);
         }
      }

      for (Map.Entry<String, ProfileRow> entry : this.profileRows.entrySet()) {
         boolean exists = profiles.stream().anyMatch(profile -> profileKey(profile).equals(entry.getKey()));
         if (!exists) {
            entry.getValue().removing = true;
            entry.getValue().animation.processData(0.0);
         }
      }
   }

   private static String profileKey(ProxyManager.ProxyProfile profile) {
      return profile.host.toLowerCase(java.util.Locale.ROOT) + ":" + profile.port + ":" + profile.type.name();
   }

   private void renderProfileRows(float x, float y, float width, float height, float mx, float my) {
      float rowHeight = 27.0F;
      int visibleRows = Math.max(1, (int)(height / rowHeight));
      this.visibleProfileRows = visibleRows;
      List<ProfileRow> rows = new ArrayList<>(this.profileRows.values());
      int maxScroll = Math.max(0, rows.size() - visibleRows);
      this.profileScroll = Math.max(0, Math.min(this.profileScroll, maxScroll));
      String activeKey = this.proxyManager.isEnabled()
         ? this.proxyManager.getHost().toLowerCase(java.util.Locale.ROOT) + ":" + this.proxyManager.getPort() + ":" + this.proxyManager.getType().name()
         : "";
      float contentY = y;

      for (int index = this.profileScroll; index < rows.size() && index < this.profileScroll + visibleRows; index++) {
         ProfileRow row = rows.get(index);
         row.animation.processData(row.removing ? 0.0 : 1.0);
         float animation = (float)row.animation.computeDouble();
         if (animation < 0.01F) {
            continue;
         }

         float rowY = contentY + (index - this.profileScroll) * rowHeight + (1.0F - animation) * 5.0F;
         boolean inside = mx >= x && mx <= x + width && my >= contentY + (index - this.profileScroll) * rowHeight
            && my <= contentY + (index - this.profileScroll + 1) * rowHeight;
         boolean active = profileKey(row.profile).equals(activeKey);
         float textAlpha = 0.45F + 0.55F * animation;
         int rowBg = active && !inside ? 0xFF1A1428 : (inside ? FallenUiHelper.COLOR_BTN_DARK_HOVER : 0xFF111318);
         Render2D.processData46(animation);
         Render2D.processData51(x + 6.0F, rowY, width - 12.0F, 23.0F, 5.0F, rowBg);
         Render2D.processData86(x + 6.0F, rowY, width - 12.0F, 23.0F, 5.0F, 1.0F, active ? 0xFFA855F7 : (inside ? FallenUiHelper.COLOR_BTN_BORDER_HOVER : 0xFF232733));
         FontText.processData3(
            FontFace.SEMI,
            (active ? "● " : "") + row.profile.host + ":" + row.profile.port,
            x + 13.0F,
            rowY + 7.0F,
            7.0F,
            UiTheme.computeInt(active ? 0xFFD8B4FE : FallenUiHelper.COLOR_TEXT_PRIMARY, textAlpha)
         );

         float buttonY = rowY + 3.0F;
         float deleteX = x + width - 49.0F;
         float connectX = deleteX - 54.0F;
         boolean connectHover = mx >= connectX && mx <= connectX + 50.0F && my >= rowY && my <= rowY + 23.0F;
         boolean deleteHover = mx >= deleteX && mx <= deleteX + 43.0F && my >= rowY && my <= rowY + 23.0F;
         Render2D.processData51(connectX, buttonY, 50.0F, 17.0F, 4.0F,
            connectHover ? FallenUiHelper.COLOR_BTN_ACCENT_HOVER : FallenUiHelper.COLOR_BTN_ACCENT);
         Render2D.processData51(deleteX, buttonY, 43.0F, 17.0F, 4.0F,
            deleteHover ? 0xFF6D3340 : 0xFF34232A);
         FontText.processData3(FontFace.SEMI, Lang.tr("Подкл.", "Use"), connectX + 5.0F, buttonY + 5.0F, 5.0F, 0xFFFFFFFF);
         FontText.processData3(FontFace.SEMI, Lang.tr("Удалить", "Delete"), deleteX + 3.0F, buttonY + 5.0F, 4.6F,
            UiTheme.computeInt(0xFFFFD9DF, textAlpha));

         if (!row.removing) {
            ProxyManager.ProxyProfile profile = row.profile;
            this.clickAreas.add(new ClickArea(connectX, rowY, 50.0F, 23.0F, () -> {
               this.proxyManager.activateProfile(profile);
               this.testResultText = Lang.tr("Подключено: ", "Connected: ") + profile.host + ":" + profile.port;
            }));
            this.clickAreas.add(new ClickArea(deleteX, rowY, 43.0F, 23.0F, () -> {
               row.removing = true;
               row.animation.processData(0.0);
               this.proxyManager.removeProfile(profile.host, profile.port, profile.type);
            }));
         }
         Render2D.updateState();
      }

      Iterator<Map.Entry<String, ProfileRow>> iterator = this.profileRows.entrySet().iterator();
      while (iterator.hasNext()) {
         Map.Entry<String, ProfileRow> entry = iterator.next();
         if (entry.getValue().removing && rowAnimation(entry.getValue()) < 0.01F) {
            iterator.remove();
         }
      }
   }

   private float rowAnimation(ProfileRow row) {
      row.animation.processData(0.0);
      return (float)row.animation.computeDouble();
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      float mx = (float)mouseX * sf();
      float my = (float)mouseY * sf();
      if (mx >= this.profileListX && mx <= this.profileListX + this.profileListWidth
         && my >= this.profileListTop && my <= this.profileListBottom) {
         int maxScroll = Math.max(0, this.profileRows.size() - this.visibleProfileRows);
         this.profileScroll = Math.max(0, Math.min(maxScroll, this.profileScroll - (int)Math.signum(verticalAmount)));
         return true;
      }
      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   private void renderInputField(String label, String value, float x, float y, float w, FocusedField field, float mx, float my) {
      FontText.processData3(FontFace.SEMI, label, x, y, 5.5F, FallenUiHelper.COLOR_TEXT_MUTED);
      float boxY = y + 8.0F;
      float boxH = 22.0F;
      boolean isFoc = this.focused == field;
      boolean hov = mx >= x && mx <= x + w && my >= boxY && my <= boxY + boxH;

      int bg = 0xFF0B0D10;
      int border = isFoc || hov ? FallenUiHelper.COLOR_BTN_BORDER_HOVER : FallenUiHelper.COLOR_BTN_BORDER;

      Render2D.processData51(x, boxY, w, boxH, 6.0F, bg);
      Render2D.processData86(x, boxY, w, boxH, 6.0F, 1.0F, border);

      String displayVal = value == null || value.isEmpty() ? (isFoc ? "" : "...") : value;
      if (isFoc && (System.currentTimeMillis() / 400) % 2 == 0) {
         displayVal += "|";
      }
      FontText.processData3(FontFace.SEMI, displayVal, x + 8.0F, boxY + 6.5F, 7.0F, isFoc ? 0xFFFFFFFF : FallenUiHelper.COLOR_TEXT_PRIMARY);

      this.clickAreas.add(new ClickArea(x, boxY, w, boxH, () -> this.focused = field));
   }

   private String maskPass(String pass) {
      if (pass == null || pass.isEmpty()) return "";
      return "•".repeat(Math.min(pass.length(), 16));
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
            this.focused = FocusedField.NONE;
         }
         return true;
      }
      return super.mouseClicked(click, bl);
   }

   @Override
   public boolean keyPressed(KeyInput keyInput) {
      int key = keyInput.key();
      if (key == GLFW.GLFW_KEY_ESCAPE) {
         close();
         return true;
      }

      if (key == GLFW.GLFW_KEY_TAB) {
         this.focused = switch (this.focused) {
            case HOST -> FocusedField.PORT;
            case PORT -> FocusedField.USERNAME;
            case USERNAME -> FocusedField.PASSWORD;
            default -> FocusedField.HOST;
         };
         return true;
      }

      if (this.focused != FocusedField.NONE) {
         if (key == GLFW.GLFW_KEY_BACKSPACE) {
            handleBackspace();
            return true;
         }
         if (key == GLFW.GLFW_KEY_V && (GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == 1
            || GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == 1)) {
            String clip = MinecraftClient.getInstance().keyboard.getClipboard();
            if (clip != null && !clip.isEmpty()) {
               handleAppend(clip.trim());
            }
            return true;
         }
      }

      return super.keyPressed(keyInput);
   }

   @Override
   public boolean charTyped(CharInput charInput) {
      if (this.focused != FocusedField.NONE && charInput.codepoint() >= 32) {
         char c = (char)charInput.codepoint();
         handleAppend(String.valueOf(c));
         return true;
      }
      return super.charTyped(charInput);
   }

   private void handleBackspace() {
      switch (this.focused) {
         case HOST -> {
            String s = this.proxyManager.getHost();
            if (s.length() > 0) this.proxyManager.setHost(s.substring(0, s.length() - 1));
         }
         case PORT -> {
            String s = String.valueOf(this.proxyManager.getPort());
            if (s.length() > 1) {
               try { this.proxyManager.setPort(Integer.parseInt(s.substring(0, s.length() - 1))); } catch (Exception ignored) {}
            } else {
               this.proxyManager.setPort(0);
            }
         }
         case USERNAME -> {
            String s = this.proxyManager.getUsername();
            if (s.length() > 0) this.proxyManager.setUsername(s.substring(0, s.length() - 1));
         }
         case PASSWORD -> {
            String s = this.proxyManager.getPassword();
            if (s.length() > 0) this.proxyManager.setPassword(s.substring(0, s.length() - 1));
         }
         default -> {}
      }
   }

   private void handleAppend(String text) {
      switch (this.focused) {
         case HOST -> this.proxyManager.setHost(this.proxyManager.getHost() + text);
         case PORT -> {
            String digits = text.replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
               String cur = this.proxyManager.getPort() <= 0 ? "" : String.valueOf(this.proxyManager.getPort());
               try {
                  int p = Integer.parseInt(cur + digits);
                  if (p <= 65535) this.proxyManager.setPort(p);
               } catch (Exception ignored) {}
            }
         }
         case USERNAME -> this.proxyManager.setUsername(this.proxyManager.getUsername() + text);
         case PASSWORD -> this.proxyManager.setPassword(this.proxyManager.getPassword() + text);
         default -> {}
      }
   }
}

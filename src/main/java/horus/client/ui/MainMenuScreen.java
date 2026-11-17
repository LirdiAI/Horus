package horus.client.ui;

import horus.client.HorusClient;
import horus.client.render.FontFace;
import horus.client.storage.MenuStore;
import horus.client.module.render.InterfaceModule;
import ru.dreamix.protection.api.impl.profile.ProfileAPI;
import horus.client.util.GuiScale;
import horus.client.util.Lang;
import horus.client.render.FontText;
import horus.client.ui.UiTheme;
import horus.client.ui.panel.AccountManagerPanel;
import horus.client.ui.AnimatedValue;
import horus.client.render.Render2D;
import horus.client.render.SoftGlowDrawData;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class MainMenuScreen extends Screen {
   private final AccountManagerPanel accountManagerPanel = new AccountManagerPanel();
   final MainMenuModals modals = new MainMenuModals(this);
   private String modalFocus;
   private boolean flag = false;
   private final AnimatedValue animation = new AnimatedValue(260L);
   private final Map<String, AnimatedValue> map = new HashMap<>();
   private final List<MainMenuScreen.DataRecord> list = new ArrayList<>();
   private final List<MainMenuScreen.DataRecord> modalList = new ArrayList<>();
   private boolean flag3;
   private long timeMillis = System.currentTimeMillis();

   // Smooth Screen Open and Exit Transitions
   private final AnimatedValue openAnimation = new AnimatedValue(280L);
   private final AnimatedValue exitAnimation = new AnimatedValue(180L);
   private Runnable pendingTransition = null;

   private int photoRequest;
   private boolean photoLoading;
   private String photoStatus = "";
   private NativeImageBackedTexture customBgTexture;
   private static final Identifier CUSTOM_BG_ID = Identifier.of("horus", "images/menu/custom_bg");
   // кадры анимированного GIF: список кадров + задержки (мс)
   private List<NativeImage> gifFrames;
   private int[] gifDelays;
   private long gifClockNanos;
   private int gifShownFrame;

   record GifAnimation(NativeImage[] frames, int[] delays) {
   }

   // Palette constants matching the Fallen / Horus Dark Minimalist UI
   private static final int COLOR_BG = 0xFF0B0D10;
   private static final int COLOR_BTN_DARK = 0xFF14171D;
   private static final int COLOR_BTN_DARK_HOVER = 0xFF1E2127;
   // Bright vibrant white-purple / electric violet accent
   private static final int COLOR_BTN_ACCENT = 0xFF8B5CF6;
   private static final int COLOR_BTN_ACCENT_HOVER = 0xFFD8B4FE;

   // Button outline colors: gray/purple at rest -> smooth glowing white on hover
   private static final int COLOR_BTN_BORDER = 0xFF383E4C;
   private static final int COLOR_BTN_BORDER_HOVER = 0xFFFFFFFF;
   private static final int COLOR_BTN_ACCENT_BORDER = 0xFFA855F7;
   private static final int COLOR_BTN_ACCENT_BORDER_HOVER = 0xFFFFFFFF;

   private static final int COLOR_TEXT_PRIMARY = 0xFFEBECEF;
   private static final int COLOR_TEXT_SECONDARY = 0xFF8E95A2;
   private static final int COLOR_TEXT_MUTED = 0xFF636875;
   private static final int COLOR_TEXT_POLICY = 0xFF3B404A;

   public MainMenuScreen(boolean welcomeTransition) {
      super(Text.literal("Fallen"));
      this.flag3 = welcomeTransition;
   }

   public MainMenuScreen() {
      this(false);
   }

   private String welcomeName() {
      return ProfileAPI.get() != null
              && ProfileAPI.get().getModel() != null
              && ProfileAPI.get().getModel().getName() != null
              && !ProfileAPI.get().getModel().getName().isBlank()
              ? ProfileAPI.get().getModel().getName()
              : "yougame";
   }

   @Override
   public boolean shouldCloseOnEsc() {
      return false;
   }

   @Override
   public boolean shouldPause() {
      return false;
   }

   @Override
   protected void init() {
      super.init();
      this.timeMillis = System.currentTimeMillis();
      this.pendingTransition = null;
      this.openAnimation.getAnimatedValue(0.0);
      this.exitAnimation.getAnimatedValue(0.0);
      this.flag = false;
      this.animation.getAnimatedValue(0.0);
      this.accountManagerPanel.updateState();
      if (!this.photoLoading && this.customBgTexture == null) this.loadCustomPhotoTexture();

      // переоткрыть модалку списка миров после ванильного экрана настроек мира
      MainMenuModals.Type reopenAfterEdit = this.modals.consumePendingReopen();
      if (reopenAfterEdit != null) {
         this.modals.open(reopenAfterEdit);
      }
   }

   @Override
   public void tick() {
      super.tick();
      this.modals.tick();
   }

   @Override
   public void removed() {
      this.modals.removed();
      ++this.photoRequest;
      this.photoLoading = false;
      this.releaseGifFrames();
      if (this.customBgTexture != null) {
         MinecraftClient.getInstance().getTextureManager().destroyTexture(CUSTOM_BG_ID);
         this.customBgTexture = null;
         horus.client.render.ImageRenderer.getImageRenderer().updateState5();
      }
      super.removed();
   }

   @Override
   public void renderBackground(DrawContext drawContext, int mouseX, int mouseY, float delta) {
   }

   @Override
   public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
      float screenW = this.width * spaceFactor();
      float screenH = this.height * spaceFactor();
      float scale = Math.min(screenW / 960.0F, screenH / 540.0F);
      float offsetY = (screenH - 540.0F * scale) / 2.0F;
      float mx = mouseX * spaceFactor();
      float my = mouseY * spaceFactor();

      this.list.clear();
      InterfaceModule.getInstance().updateState();
      UiTheme.flag = true;
      Render2D.renderElement5(drawContext);

      // Advance smooth entrance and exit animations
      this.openAnimation.processData(1.0);
      float openVal = (float) this.openAnimation.computeDouble();
      float openEase = UiTheme.computeFloat2(openVal);

      if (this.pendingTransition != null) {
         this.exitAnimation.processData(1.0);
         float exitVal = (float) this.exitAnimation.computeDouble();
         if (exitVal >= 0.98F) {
            Runnable act = this.pendingTransition;
            this.pendingTransition = null;
            act.run();
            return;
         }
      }
      float exitVal = this.pendingTransition != null ? (float) this.exitAnimation.computeDouble() : 0.0F;
      float exitEase = UiTheme.computeFloat2(exitVal);

      float transitionAlpha = Math.max(0.001F, Math.min(1.0F, openEase * (1.0F - exitEase)));
      float enterDriftY = (1.0F - openEase) * 20.0F * scale;
      float exitDriftY = exitEase * 16.0F * scale;

      // Account manager overlay animation
      this.animation.processData(this.flag ? 1.0 : 0.0);
      float altAnim = (float)this.animation.computeDouble();
      float altEase = UiTheme.computeFloat2(altAnim);
      float overlayAnim = altEase;

      // Фон: фото-обои из шейдер-мокапа + живые режимы + мягкое welcome-затемнение
      this.tickGif();
      int liveMode = MenuStore.liveMode();
      if (liveMode == 1) {
         // «Дыхание» — медленный зум/панорама обоев (Ken Burns)
         long now = System.currentTimeMillis();
         double phase = (now % 26000L) / 26000.0 * Math.PI * 2.0;
         double phase2 = (now % 34000L) / 34000.0 * Math.PI * 2.0;
         float zoom = 1.14F + 0.05F * (float) Math.sin(phase);
         float panX = 16.0F * (float) Math.sin(phase2);
         float panY = 12.0F * (float) Math.cos(phase2 * 0.8F);
         Render2D.processData89(
                 this.customBgTexture != null && MenuStore.customPhoto() != null && !MenuStore.customPhoto().isBlank()
                       ? MenuStore.currentBackground() : MenuStore.backgroundAt(MenuStore.backgroundIndex()),
                 -panX - (screenW * zoom - screenW) / 2.0F,
                 -panY - (screenH * zoom - screenH) / 2.0F,
                 screenW * zoom,
                 screenH * zoom,
                 0.0F,
                 0xFFFFFFFF
         );
      } else {
         Render2D.processData89(
                 this.customBgTexture != null && MenuStore.customPhoto() != null && !MenuStore.customPhoto().isBlank()
                       ? MenuStore.currentBackground() : MenuStore.backgroundAt(MenuStore.backgroundIndex()),
                 0.0F,
                 0.0F,
                 screenW,
                 screenH,
                 0.0F,
                 0xFFFFFFFF
         );
      }
      if (liveMode == 2) {
         this.drawLiveParticles(screenW, screenH, scale);
      } else if (liveMode == 3) {
         this.drawLiveAurora(screenW, screenH, scale);
      }
      if (this.flag3) {
         float welcomeFade = UiTheme.computeFloat2(UiTheme.computeFloat3(this.timeMillis, 100L, 600L));
         if (welcomeFade < 0.999F) {
            Render2D.processData51(0.0F, 0.0F, screenW, screenH, 0.0F, UiTheme.computeInt(COLOR_BG, 1.0F - welcomeFade));
         }
      }

      // Top-left "Logged in as" badge & bottom policy footer
      float chromeAlpha = transitionAlpha * (1.0F - overlayAnim);
      if (chromeAlpha > 0.001F) {
         if (chromeAlpha < 0.999F) {
            Render2D.processData46(chromeAlpha);
         }
         this.drawChrome(screenW, screenH, scale, mx, my, overlayAnim < 0.5F && this.pendingTransition == null && openEase > 0.5F);
         if (chromeAlpha < 0.999F) {
            Render2D.updateState();
         }
      }

      // Main centered menu stack with smooth entrance/exit slide & fade
      float centerAlpha = transitionAlpha * (1.0F - overlayAnim);
      if (centerAlpha > 0.001F) {
         if (centerAlpha < 0.999F) {
            Render2D.processData46(centerAlpha);
         }
         float centerX = screenW / 2.0F;
         float centerY = screenH / 2.0F - overlayAnim * 18.0F * scale + enterDriftY + exitDriftY;
         this.drawCenterMenu(centerX, centerY, scale, mx, my, overlayAnim);
         if (centerAlpha < 0.999F) {
            Render2D.updateState();
         }
      }

      // Account Manager overlay (Swap Accounts) with smooth slide and fade
      float altAlpha = transitionAlpha * altEase;
      if (altAlpha > 0.001F) {
         Render2D.processData46(altAlpha);
         float panelX = (screenW - 329.5F * scale) / 2.0F + 32.0F * scale * (1.0F - altEase) + enterDriftY * 0.4F;
         this.accountManagerPanel.renderElement(drawContext, panelX, scale, offsetY, mx, my);
         Render2D.updateState();
      }

      // Модальные окна меню (рисуем ДО закрытия контекстов рендера — updateState6 в конце кадра)
      this.modalList.clear();
      if (this.modals.isOpen() && this.pendingTransition == null) {
            this.modals.render(screenW, screenH, scale, mx, my, true);
         Render2D.updateState();
      }

      if (altEase > 0.5F) {
         float panelX = (screenW - 329.5F * scale) / 2.0F + 32.0F * scale * (1.0F - altEase);
         this.accountManagerPanel.renderElement3(drawContext, panelX, scale, offsetY);
      }

      // Закрываем контексты батч-рендереров в самом конце кадра,
      // иначе всё, что рисуется после — невидимо (модалки открываются, но не отрисовываются)
      Render2D.updateState6();
   }

   private void drawChrome(float screenW, float screenH, float scale, float mx, float my, boolean interactive) {
      // Top-left profile avatar & "Logged in as <user>"
      float badgeX = 10.0F * scale;
      float badgeY = 10.0F * scale;
      float avatarSize = 16.0F * scale;
      float badgeW = 95.0F * scale;
      float badgeH = 18.0F * scale;
      float profileHover = this.hoverPC("profileBadge", interactive && UiTheme.matchesCriteria(mx, my, badgeX, badgeY, badgeW, badgeH));

      // Avatar head base (Steve skin tone + hair + eyes)
      Render2D.processData51(badgeX, badgeY, avatarSize, avatarSize, 1.5F * scale, 0xFFE9E4FF);
      Render2D.processData51(badgeX, badgeY, avatarSize, 4.5F * scale, 1.0F * scale, 0xFF3A2B5C);
      Render2D.processData51(badgeX + 2.2F * scale, badgeY + 7.2F * scale, 3.6F * scale, 2.0F * scale, 0.0F, 0xFFFFFFFF);
      Render2D.processData51(badgeX + 10.2F * scale, badgeY + 7.2F * scale, 3.6F * scale, 2.0F * scale, 0.0F, 0xFFFFFFFF);
      Render2D.processData51(badgeX + 4.0F * scale, badgeY + 7.2F * scale, 1.8F * scale, 2.0F * scale, 0.0F, 0xFF4F3E8C);
      Render2D.processData51(badgeX + 10.2F * scale, badgeY + 7.2F * scale, 1.8F * scale, 2.0F * scale, 0.0F, 0xFF4F3E8C);
      Render2D.processData51(badgeX + 6.0F * scale, badgeY + 11.2F * scale, 4.0F * scale, 2.2F * scale, 0.0F, 0xFF9D7BEA);

      FontText.processData3(
              FontFace.SEMI,
              Lang.tr("Вы вошли как", "Logged in as"),
              badgeX + avatarSize + 5.5F * scale,
              badgeY + 1.5F * scale,
              5.5F * scale,
              COLOR_TEXT_MUTED
      );
      FontText.processData3(
              FontFace.SEMI,
              this.welcomeName(),
              badgeX + avatarSize + 5.5F * scale,
              badgeY + 8.5F * scale,
              7.5F * scale,
              UiTheme.computeInt2(COLOR_TEXT_PRIMARY, 0xFFFFFFFF, profileHover)
      );
      if (interactive) {
         this.list.add(new MainMenuScreen.DataRecord(badgeX, badgeY, badgeW, badgeH, this::toggleAlt));
      }

      // Bottom centered policy disclaimer
      String line1 = "By logging into your account you agree to all of our policies,";
      String line2 = "including our Privacy Policy and Terms of Service";
      float policySize = 5.2F * scale;
      this.drawCenteredString(line1, screenW / 2.0F, screenH - 19.5F * scale, policySize, COLOR_TEXT_POLICY);
      this.drawCenteredString(line2, screenW / 2.0F, screenH - 12.0F * scale, policySize, COLOR_TEXT_POLICY);

      // Часы реального времени — по центру сверху главного меню (крупные, «МСК» под цифрами)
      try {
         java.time.ZonedDateTime now = java.time.ZonedDateTime.now();
         String clock = now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
         String zoneId = java.time.ZoneId.systemDefault().getId();
         String zoneLabel = zoneId.equals("Europe/Moscow") || zoneId.equals("Europe/Kaliningrad") || zoneId.equals("Europe/Samara") ? "МСК" : zoneId;
         float clockSize = 15.0F * scale;
         float zoneSize = 5.5F * scale;
         float clockW = this.estimateTextWidth(clock, clockSize);
         float zoneW = this.estimateTextWidth(zoneLabel, zoneSize);
         FontText.processData3(FontFace.SEMI, clock, screenW / 2.0F - clockW / 2.0F, 4.0F * scale, clockSize, 0xFFF4F4F8);
         FontText.processData3(FontFace.SEMI, zoneLabel, screenW / 2.0F - zoneW / 2.0F, 4.0F * scale + clockSize + 1.5F * scale, zoneSize, 0xFF8C8FA3);
      } catch (Exception ignored) {
      }

   }

   // Кнопка «Оформление» в самом верху — открывает раздел настройки фона и глянца
   private void drawGlossToggle(float centerX, float scale, float mx, float my, boolean interactive) {
      String label = Lang.tr("✨ Оформление меню", "✨ Menu Appearance");
      float w = Math.max(110.0F * scale, this.estimateTextWidth(label, 5.8F * scale) + 34.0F * scale);
      float h = 19.0F * scale;
      float x = centerX - w / 2.0F;
      float y = 28.0F * scale;
      boolean hovered = interactive && UiTheme.matchesCriteria(mx, my, x, y, w, h);
      float hover = this.hoverPC("gloss_toggle", hovered);
      float lift = hover * 1.5F * scale;
      y -= lift;

      int bgColor = UiTheme.computeInt2(0x5914151C, 0x8C232331, hover);
      int borderColor = UiTheme.computeInt2(0x1AFFFFFF, 0x669B6DFF, hover);
      int textColor = UiTheme.computeInt2(COLOR_TEXT_SECONDARY, 0xFFFFFFFF, hover);

      Render2D.processData51(x, y, w, h, 6.0F * scale, bgColor);
      Render2D.processData86(x, y, w, h, 6.0F * scale, 0.9F * scale, borderColor);
      this.drawGlossOverlay(x, y, w, h, 6.0F * scale, scale);

      float fontSize = 5.8F * scale;
      FontText.processData3(FontFace.SEMI, label, x + w / 2.0F - this.estimateTextWidth(label, fontSize) / 2.0F, y + (h - fontSize) / 2.0F - 0.5F * scale, fontSize, textColor);

      if (interactive) {
         this.list.add(new MainMenuScreen.DataRecord(x, y, w, h, () -> this.modals.open(MainMenuModals.Type.STUDIO)));
      }
   }

   // Стеклянный глянец: белый градиент сверху вниз внутри кнопки
   private void drawGlossOverlay(float x, float y, float w, float h, float radius, float scale) {
      if (!MenuStore.glossy() || h < 8.0F * scale) {
         return;
      }

      float inset = 1.0F * scale;
      float gx = x + inset;
      float gy = y + inset;
      float gw = w - inset * 2.0F;
      float gh = h * 0.58F;
      float rTL = Math.max(0.0F, radius - inset);
      float rTR = rTL;

      Render2D.processData40(gx, gy, gw, gh, rTL, rTR, 0.0F, 0.0F, 0x2FFFFFFF, 0x2FFFFFFF, 0x00FFFFFF, 0x00FFFFFF);
   }

   void transitionTo(Runnable action) {
      if (this.pendingTransition == null) {
         this.pendingTransition = action;
         this.exitAnimation.getAnimatedValue(0.0);
      }
   }

   private void drawCenterMenu(float centerX, float centerY, float scale, float mx, float my, float overlayAnim) {
      MinecraftClient minecraftClient = this.client != null ? this.client : MinecraftClient.getInstance();
      boolean interactive = overlayAnim < 0.5F && this.pendingTransition == null && !this.modals.isOpen();

      // Кнопка глянца над всеми кнопками (в самом верху экрана)
      this.drawGlossToggle(centerX, scale, mx, my, interactive);

      // Раскладка центральных кнопок (переключается в верхнем доке):
      // 0 = Fallen reference, 1 = все 6 шейдер-карточек по центру, 2 = сетка 2×3
      int centerLayout = MenuStore.centerLayout();
      if (centerLayout != 0) {
         this.drawPillButton(centerX + 145.0F * scale, 10.0F * scale, 32.0F * scale, 18.0F * scale,
               5.0F * scale, scale, Lang.get() == Lang.Language.RU ? "EN" : "RU", false, mx, my, interactive, Lang::toggle);
      }
      if (centerLayout == 1) {
         this.drawCenterCards(centerX, centerY, scale, mx, my, interactive);
         return;
      }

      if (centerLayout == 2) {
         this.drawCenterGrid(centerX, centerY, scale, mx, my, interactive);
         return;
      }

      // Логотип, приветствие и подзаголовок
      this.drawMenuHead(centerX, centerY, scale);

      // Main pill button dimensions
      float btnW = 230.0F * scale;
      float btnH = 32.0F * scale;
      float btnX = centerX - btnW / 2.0F;
      float btnRadius = 8.5F * scale;

      // 1. Singleplayer (with smooth exit transition)
      this.drawPillButton(
              btnX,
              centerY - 46.0F * scale,
              btnW,
              btnH,
              btnRadius,
              scale,
              Lang.tr("Одиночная игра", "Singleplayer"),
              false,
              mx,
              my,
              interactive,
              () -> this.modals.open(MainMenuModals.Type.SINGLEPLAYER)
      );

      // 2. Multiplayer (with smooth exit transition)
      this.drawPillButton(
              btnX,
              centerY - 6.0F * scale,
              btnW,
              btnH,
              btnRadius,
              scale,
              Lang.tr("Сетевая игра", "Multiplayer"),
              false,
              mx,
              my,
              interactive,
              () -> this.modals.open(MainMenuModals.Type.MULTIPLAYER)
      );

      // 3. Swap Accounts (Accent White-Purple button with smooth slide)
      this.drawPillButton(
              btnX,
              centerY + 34.0F * scale,
              btnW,
              btnH,
              btnRadius,
              scale,
              Lang.tr("Сменить аккаунт", "Swap Accounts"),
              true,
              mx,
              my,
              interactive,
              this::toggleAlt
      );

      // Secondary row: Опции • Прокси • EN/RU • Выход (маленькие стеклянные кнопки)
      float linksY = centerY + 76.0F * scale;
      float subW = 54.0F * scale;
      float subH = 20.0F * scale;
      float subGap = 6.0F * scale;
      float subX = centerX - (subW * 4.0F + subGap * 3.0F) / 2.0F;
      horus.client.proxy.ProxyManager proxyManager = horus.client.proxy.ProxyManager.getInstance();
      boolean proxyEnabled = proxyManager.isEnabled();
      this.drawSubButton(
              "linkOptions",
              subX,
              linksY,
              subW,
              subH,
              scale,
              Lang.tr("Опции", "Options"),
              false,
              false,
              mx,
              my,
              interactive,
              () -> this.transitionTo(() -> minecraftClient.setScreen(new OptionsScreen(this, minecraftClient.options)))
      );
      this.drawSubButton(
              "linkProxies",
              subX + (subW + subGap),
              linksY,
              subW,
              subH,
              scale,
              Lang.tr("Прокси", "Proxy"),
              false,
              proxyEnabled,
              mx,
              my,
              interactive,
              () -> this.transitionTo(() -> minecraftClient.setScreen(new ProxyScreen(this)))
      );
      this.drawSubButton(
              "linkLang",
              subX + (subW + subGap) * 2.0F,
              linksY,
              subW,
              subH,
              scale,
              Lang.get() == Lang.Language.RU ? "EN" : "RU",
              false,
              false,
              mx,
              my,
              interactive,
              Lang::toggle
      );
      this.drawSubButton(
              "linkExit",
              subX + (subW + subGap) * 3.0F,
              linksY,
              subW,
              subH,
              scale,
              Lang.tr("Выход", "Exit"),
              true,
              false,
              mx,
              my,
              interactive,
              () -> this.transitionTo(minecraftClient::scheduleStop)
      );
   }

   private void drawMenuHead(float centerX, float centerY, float scale) {
      // Center logo above greeting
      float logoW = 14.5F * scale;
      float logoH = 14.5F * scale;
      Render2D.processData89(
              "assets/horus/textures/gui/mainmenu/icons/logo.png",
              centerX - logoW / 2.0F,
              centerY - 115.0F * scale,
              logoW,
              logoH,
              0.0F,
              0xFFFFFFFF
      );

      // Greeting & subtitle
      String greeting = Lang.tr("Доброе время суток, ", "Good time of day, ") + this.welcomeName();
      String subtitle = Lang.tr("Добро пожаловать в Horus — лучший Minecraft клиент.", "Welcome to Horus, the best Minecraft client.");
      this.drawCenteredString(
              greeting,
              centerX,
              centerY - 87.0F * scale,
              9.5F * scale,
              COLOR_TEXT_PRIMARY
      );
      this.drawCenteredString(
              subtitle,
              centerX,
              centerY - 74.0F * scale,
              6.2F * scale,
              COLOR_TEXT_MUTED
      );
   }


   // РАССТАНОВКА 2: все 6 шейдер-кнопок по центру (как «all_center_cards» в мокапе)
   private void drawCenterCards(float centerX, float centerY, float scale, float mx, float my, boolean interactive) {
      this.drawMenuHead(centerX, centerY, scale);

      MinecraftClient minecraftClient = this.client != null ? this.client : MinecraftClient.getInstance();
      String accountsLabel = Lang.tr("Аккаунты", "Accounts") + " (" + this.welcomeName() + ")";

      float btnW = 230.0F * scale;
      float btnH = 27.0F * scale;
      float gap = 7.5F * scale;
      float btnX = centerX - btnW / 2.0F;
      float y = centerY - 55.0F * scale;
      float radius = 8.0F * scale;
      float halfW = (btnW - gap) / 2.0F;

      this.drawPillButton(btnX, y, btnW, btnH, radius, scale, Lang.tr("Одиночная игра", "Singleplayer"), false, mx, my, interactive, () -> this.modals.open(MainMenuModals.Type.SINGLEPLAYER));
      y += btnH + gap;
      this.drawPillButton(btnX, y, btnW, btnH, radius, scale, Lang.tr("Сетевая игра", "Multiplayer"), false, mx, my, interactive, () -> this.modals.open(MainMenuModals.Type.MULTIPLAYER));
      y += btnH + gap;
      this.drawPillButton(btnX, y, btnW, btnH, radius, scale, accountsLabel, true, mx, my, interactive, this::toggleAlt);
      y += btnH + gap;

      this.drawPillButton(btnX, y, halfW, btnH, radius, scale, Lang.tr("Прокси", "Proxy"), false, mx, my, interactive, () -> this.transitionTo(() -> minecraftClient.setScreen(new ProxyScreen(this))));
      this.drawPillButton(btnX + halfW + gap, y, halfW, btnH, radius, scale, Lang.tr("Опции", "Options"), false, mx, my, interactive, () -> this.transitionTo(() -> minecraftClient.setScreen(new OptionsScreen(this, minecraftClient.options))));
      y += btnH + gap;

      this.drawCardButton(btnX, y, halfW, btnH, radius, scale, Lang.tr("Своё фото / Фон", "Custom Photo"), null, 0, 0, mx, my, interactive, () -> this.modals.open(MainMenuModals.Type.STUDIO));
      this.drawCardButton(btnX + halfW + gap, y, halfW, btnH, radius, scale, Lang.tr("Выход", "Exit"), null, 0xFFEF4444, 0xFFFF7B7B, mx, my, interactive, () -> this.transitionTo(minecraftClient::scheduleStop));
   }

   // РАССТАНОВКА 3: симметричная сетка 2×3 с подписями (как «compact_grid» в мокапе)
   private void drawCenterGrid(float centerX, float centerY, float scale, float mx, float my, boolean interactive) {
      this.drawMenuHead(centerX, centerY, scale);

      MinecraftClient minecraftClient = this.client != null ? this.client : MinecraftClient.getInstance();
      horus.client.proxy.ProxyManager proxyManager = horus.client.proxy.ProxyManager.getInstance();
      boolean proxyEnabled = proxyManager.isEnabled();

      float cardW = 111.0F * scale;
      float cardH = 40.0F * scale;
      float gap = 8.0F * scale;
      float radius = 7.0F * scale;
      float x0 = centerX - cardW - gap / 2.0F;
      float y0 = centerY - (cardH * 3.0F + gap * 2.0F) / 2.0F;

      this.drawCardButton(x0, y0, cardW, cardH, radius, scale, Lang.tr("Одиночная игра", "Singleplayer"), Lang.tr("Локальные миры", "Local worlds"), 0, 0, mx, my, interactive, () -> this.modals.open(MainMenuModals.Type.SINGLEPLAYER));
      this.drawCardButton(x0 + cardW + gap, y0, cardW, cardH, radius, scale, Lang.tr("Сетевая игра", "Multiplayer"), Lang.tr("Серверы", "Servers"), 0, 0, mx, my, interactive, () -> this.modals.open(MainMenuModals.Type.MULTIPLAYER));
      y0 += cardH + gap;

      this.drawCardButton(x0, y0, cardW, cardH, radius, scale, Lang.tr("Аккаунты", "Accounts"), this.welcomeName(), 0xFF8B5CF6, 0xFFB794FF, mx, my, interactive, this::toggleAlt);
      this.drawCardButton(x0 + cardW + gap, y0, cardW, cardH, radius, scale, Lang.tr("Прокси", "Proxy"), proxyEnabled ? Lang.tr("Включён", "On") : "SOCKS5 / HTTP", 0, 0, mx, my, interactive, () -> this.transitionTo(() -> minecraftClient.setScreen(new ProxyScreen(this))));
      y0 += cardH + gap;

      this.drawCardButton(x0, y0, cardW, cardH, radius, scale, Lang.tr("Опции", "Options"), Lang.tr("Настройки и Фото", "Settings & Photo"), 0, 0, mx, my, interactive, () -> this.transitionTo(() -> minecraftClient.setScreen(new OptionsScreen(this, minecraftClient.options))));
      this.drawCardButton(x0 + cardW + gap, y0, cardW, cardH, radius, scale, Lang.tr("Выход", "Exit"), Lang.tr("Закрыть клиент", "Close client"), 0xFFEF4444, 0xFFFF7B7B, mx, my, interactive, () -> this.transitionTo(minecraftClient::scheduleStop));
   }

   // Универсальная шейдер-карточка (крупная кнопка с подписью, как в мокапе)
   private void drawCardButton(
           float x,
           float y,
           float w,
           float h,
           float radius,
           float scale,
           String label,
           String sublabel,
           int accentColor,
           int accentHoverColor,
           float mx,
           float my,
           boolean interactive,
           Runnable action
   ) {
      boolean hovered = interactive && UiTheme.matchesCriteria(mx, my, x, y, w, h);
      float hover = this.hoverPC("card_" + label + "_" + Math.round(x) + "_" + Math.round(y), hovered);
      float lift = hover * 1.5F * scale;
      y -= lift;

      boolean accent = accentColor != 0;
      int bgColor = accent
              ? UiTheme.computeInt2(accentColor, accentHoverColor, hover)
              : UiTheme.computeInt2(0x5914151C, 0x8C232331, hover);
      int borderColor = accent
              ? UiTheme.computeInt2(accentColor, 0xFFFFFFFF, hover)
              : UiTheme.computeInt2(0x10FFFFFF, 0x669B6DFF, hover);

      Render2D.processData51(x, y, w, h, radius, bgColor);
      Render2D.processData86(x, y, w, h, radius, 0.85F * scale, borderColor);
      this.drawGlossOverlay(x, y, w, h, radius, scale);

      boolean hasSub = sublabel != null && !sublabel.isEmpty();
      float mainSize = hasSub ? 6.5F * scale : 6.8F * scale;
      int textMain = accent ? 0xFFFFFFFF : UiTheme.computeInt2(COLOR_TEXT_SECONDARY, 0xFFFFFFFF, hover);

      if (hasSub) {
         this.drawCenteredString(label, x + w / 2.0F, y + h * 0.24F, mainSize, textMain);
         this.drawCenteredString(sublabel, x + w / 2.0F, y + h * 0.55F, 5.2F * scale, accent ? 0xCCFFFFFF : COLOR_TEXT_MUTED);
      } else {
         this.drawCenteredString(label, x + w / 2.0F, y + (h - mainSize) / 2.0F - 0.5F * scale, mainSize, textMain);
      }

      if (interactive) {
         this.list.add(new MainMenuScreen.DataRecord(x - 3.0F * scale, y, w + 6.0F * scale, h + 4.0F * scale, action));
      }
   }

   // ===== своё фото на фоне меню =====

   void pickCustomPhoto() {
      if (this.photoLoading) return;
      this.photoLoading = true;
      long handle = MinecraftClient.getInstance().getWindow().getHandle();
      org.lwjgl.glfw.GLFW.glfwIconifyWindow(handle);
      CompletableFuture<File> future = new CompletableFuture<>();
      EventQueue.invokeLater(() -> {
         java.awt.Frame owner = null;
         FileDialog dialog = null;
         try {
            owner = new java.awt.Frame();
            owner.setUndecorated(true);
            owner.setBounds(0, 0, 1, 1);
            owner.setAlwaysOnTop(true);
            owner.setVisible(true);
            dialog = new FileDialog(owner, Lang.tr("Выберите PNG, JPG, BMP или GIF", "Choose PNG, JPG, BMP or GIF"), FileDialog.LOAD);
            dialog.setAlwaysOnTop(true);
            dialog.setVisible(true);
            String name = dialog.getFile();
            String directory = dialog.getDirectory();
            future.complete(name != null && directory != null ? new File(directory, name) : null);
         } catch (Exception exception) {
            future.completeExceptionally(exception);
         } finally {
            if (dialog != null) dialog.dispose();
            if (owner != null) owner.dispose();
         }
      });
      future.whenComplete((file, error) -> MinecraftClient.getInstance().execute(() -> {
         org.lwjgl.glfw.GLFW.glfwRestoreWindow(handle);
         this.photoLoading = false;
         if (error != null) {
            this.photoStatus = Lang.tr("Не удалось открыть выбор файла", "Could not open file picker");
            HorusClient.logger().warn("[MainMenu] File picker failed", error);
         } else if (MinecraftClient.getInstance().currentScreen == this) {
            this.applyCustomPhoto(file);
         }
      }));
   }

   private void applyCustomPhoto(File file) {
      if (file == null || !file.isFile()) {
         return;
      }
      int request = ++this.photoRequest;
      this.photoLoading = true;
      this.photoStatus = Lang.tr("Загрузка…", "Loading…");
      Path saved = FabricLoader.getInstance().getConfigDir().resolve("horus")
            .resolve("menu_custom_bg_" + java.util.UUID.randomUUID() + extensionOf(file.getName()));
      CompletableFuture.supplyAsync(() -> {
         try {
            Path configDir = FabricLoader.getInstance().getConfigDir().resolve("horus");
            Files.createDirectories(configDir);
            String ext = extensionOf(file.getName());
            // Decode before replacing the last working background.
            Object decoded = ".gif".equals(ext) ? this.readGifFrames(file.toPath()) : this.readNativeImage(file.toPath(), ext);
            try {
               Files.copy(file.toPath(), saved, StandardCopyOption.REPLACE_EXISTING);
               return decoded;
            } catch (Exception exception) {
               closePhoto(decoded);
               throw exception;
            }
         } catch (Exception exception) {
            HorusClient.logger().warn("[MainMenu] Failed to load custom photo: {}", file, exception);
            return null;
         }
      }).thenAccept(result -> MinecraftClient.getInstance().execute(() -> {
         if (request != this.photoRequest) {
            closePhoto(result);
            return;
         }
         this.photoLoading = false;
         if (result != null) {
            this.installPhoto(result);
            MenuStore.setCustomPhoto(saved.toString(), file.getName());
            this.photoStatus = Lang.tr("Фон установлен", "Background applied");
         } else {
            this.photoStatus = Lang.tr("Ошибка: выберите PNG, JPG, BMP или GIF", "Error: choose PNG, JPG, BMP or GIF");
         }
      }));
   }

   private static void closePhoto(Object photo) {
      if (photo instanceof GifAnimation animation) {
         for (NativeImage frame : animation.frames()) frame.close();
      } else if (photo instanceof NativeImage image) {
         image.close();
      }
   }

   private void installPhoto(Object photo) {
      this.releaseGifFrames();
      if (photo instanceof GifAnimation animation) {
         this.registerGifTexture(animation);
      } else {
         try (NativeImage image = (NativeImage) photo) {
            this.registerCustomPhotoTexture(image);
         }
      }
   }

   void clearCustomPhoto() {
      ++this.photoRequest;
      this.photoLoading = false;
      MenuStore.clearCustomPhoto();
      this.releaseGifFrames();
      if (this.customBgTexture != null) {
         MinecraftClient.getInstance().getTextureManager().destroyTexture(CUSTOM_BG_ID);
         this.customBgTexture = null;
         horus.client.render.ImageRenderer.getImageRenderer().updateState5();
      }
      this.photoStatus = "";
   }

   String photoStatus() { return this.photoStatus; }
   boolean photoLoading() { return this.photoLoading; }

   private static String extensionOf(String fileName) {
      String name = fileName.toLowerCase();
      return name.endsWith(".jpg") || name.endsWith(".jpeg") ? ".jpg" : name.endsWith(".gif") ? ".gif" : name.endsWith(".bmp") ? ".bmp" : ".png";
   }

   private void loadCustomPhotoTexture() {
      String custom = MenuStore.customPhoto();
      if (custom == null || custom.isBlank()) {
         return;
      }

      File file = new File(custom);
      if (!file.isFile()) {
         return;
      }

      int request = ++this.photoRequest;
      CompletableFuture.supplyAsync(() -> {
         try {
            String name = file.getName().toLowerCase();
            String ext = name.endsWith(".jpg") || name.endsWith(".jpeg") ? ".jpg" : name.endsWith(".gif") ? ".gif" : name.endsWith(".bmp") ? ".bmp" : ".png";
            return ".gif".equals(ext) ? this.readGifFrames(file.toPath()) : this.readNativeImage(file.toPath(), ext);
         } catch (Exception exception) {
            HorusClient.logger().warn("[MainMenu] Failed to restore custom photo: {}", file, exception);
            return null;
         }
      }).thenAccept(result -> MinecraftClient.getInstance().execute(() -> {
         if (request != this.photoRequest) {
            closePhoto(result);
            return;
         }
         if (result != null) {
            this.installPhoto(result);
         } else {
            this.photoStatus = Lang.tr("Не удалось восстановить фон", "Could not restore background");
         }
      }));
   }

   // ===== анимированный GIF =====

   private GifAnimation readGifFrames(Path path) throws Exception {
      ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();

      try (ImageInputStream stream = ImageIO.createImageInputStream(path.toFile())) {
         reader.setInput(stream, false, false);
         int count = reader.getNumImages(true);
         if (count <= 0) {
            throw new IllegalStateException("Empty GIF: " + path);
         }

         NativeImage[] frames = new NativeImage[count];
         int[] delays = new int[count];
         Node logical = reader.getStreamMetadata().getAsTree("javax_imageio_gif_stream_1.0").getFirstChild();
         while (logical != null && !"LogicalScreenDescriptor".equals(logical.getNodeName())) logical = logical.getNextSibling();
         int width = logical == null ? reader.getWidth(0) : Integer.parseInt(logical.getAttributes().getNamedItem("logicalScreenWidth").getNodeValue());
         int height = logical == null ? reader.getHeight(0) : Integer.parseInt(logical.getAttributes().getNamedItem("logicalScreenHeight").getNodeValue());
         if ((long) width * height * count > 64L * 1024 * 1024) {
            throw new IllegalArgumentException("GIF exceeds decoded memory limit");
         }
         BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
         try {
            for (int i = 0; i < count; i++) {
               IIOMetadata metadata = reader.getImageMetadata(i);
               Node node = metadata.getAsTree("javax_imageio_gif_image_1.0").getFirstChild();
               int left = 0, top = 0;
               String disposal = "none";
               while (node != null) {
                  NamedNodeMap attrs = node.getAttributes();
                  if ("ImageDescriptor".equals(node.getNodeName())) {
                     left = Integer.parseInt(attrs.getNamedItem("imageLeftPosition").getNodeValue());
                     top = Integer.parseInt(attrs.getNamedItem("imageTopPosition").getNodeValue());
                  } else if ("GraphicControlExtension".equals(node.getNodeName())) {
                     disposal = attrs.getNamedItem("disposalMethod").getNodeValue();
                  }
                  node = node.getNextSibling();
               }
               BufferedImage previous = null;
               if ("restoreToPrevious".equals(disposal)) {
                  previous = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                  java.awt.Graphics2D copy = previous.createGraphics();
                  copy.drawImage(canvas, 0, 0, null);
                  copy.dispose();
               }
               BufferedImage patch = reader.read(i);
               java.awt.Graphics2D graphics = canvas.createGraphics();
               graphics.drawImage(patch, left, top, null);
               graphics.dispose();
               frames[i] = this.toNativeImage(canvas);
               delays[i] = Math.max(20, this.readGifDelay(metadata));
               if ("restoreToBackgroundColor".equals(disposal)) {
                  graphics = canvas.createGraphics();
                  graphics.setComposite(java.awt.AlphaComposite.Clear);
                  graphics.fillRect(left, top, patch.getWidth(), patch.getHeight());
                  graphics.dispose();
               } else if (previous != null) {
                  canvas = previous;
               }
            }
            return new GifAnimation(frames, delays);
         } catch (Exception exception) {
            for (NativeImage frame : frames) if (frame != null) frame.close();
            throw exception;
         }
      } finally {
         reader.dispose();
      }
   }

   private int readGifDelay(IIOMetadata metadata) {
      if (metadata == null) {
         return 100;
      }

      try {
         Node root = metadata.getAsTree("javax_imageio_gif_image_1.0");
         Node child = root.getFirstChild();

         while (child != null) {
            if ("GraphicControlExtension".equals(child.getNodeName())) {
               NamedNodeMap attrs = child.getAttributes();
               Node delay = attrs.getNamedItem("delayTime");
               if (delay != null) {
                  // задержка в GIF хранится в сотых долях секунды
                  return Integer.parseInt(delay.getNodeValue()) * 10;
               }
            }

            child = child.getNextSibling();
         }
      } catch (Exception ignored) {
      }

      return 100;
   }

   private NativeImage toNativeImage(BufferedImage buffered) {
      NativeImage image = new NativeImage(buffered.getWidth(), buffered.getHeight(), false);

      for (int y = 0; y < buffered.getHeight(); y++) {
         for (int x = 0; x < buffered.getWidth(); x++) {
            image.setColorArgb(x, y, buffered.getRGB(x, y));
         }
      }

      return image;
   }

   private void registerGifTexture(GifAnimation animation) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client == null || client.getTextureManager() == null || animation.frames().length == 0) {
         return;
      }

      this.releaseGifFrames();
      this.registerCustomPhotoTexture(animation.frames()[0]);
      this.gifFrames = new ArrayList<>();

      for (NativeImage frame : animation.frames()) {
         this.gifFrames.add(frame);
      }

      this.gifDelays = animation.delays();
      this.gifClockNanos = System.nanoTime();
      this.gifShownFrame = 0;
   }

   private void releaseGifFrames() {
      if (this.gifFrames != null) {
         for (NativeImage frame : this.gifFrames) {
            try {
               frame.close();
            } catch (Exception ignored) {
            }
         }
      }

      this.gifFrames = null;
      this.gifDelays = null;
      this.gifShownFrame = -1;
   }

   // Живые обои: парящие светящиеся частицы поверх фона
   private void drawLiveParticles(float w, float h, float scale) {
      long now = System.currentTimeMillis();
      float t = (now % 60000L) / 1000.0F;
      float span = h + 140.0F * scale;

      for (int i = 0; i < 34; i++) {
         float r1 = frac(i * 12.9898F);
         float r2 = frac(i * 78.233F);
         float r3 = frac(i * 39.425F);
         float speed = (8.0F + r1 * 16.0F) * scale;
         float size = (2.0F + r2 * 4.5F) * scale;
         float sway = (14.0F + r3 * 26.0F) * scale * (float) Math.sin(t * (0.3F + r3 * 0.4F) + i * 1.3F);
         float y = span - ((t * speed + r2 * span) % span) - 50.0F * scale;
         float x = r1 * w + sway;
         float alpha = 0.12F + 0.13F * (float) Math.sin(t * 0.8F + i * 1.7F);
         alpha = Math.max(0.05F, alpha);
         int color = i % 3 == 0 ? 0xFFFFFFFF : 0xFFB08CFF;
         Render2D.processData58(new SoftGlowDrawData(x - size, y - size, size, size, size / 2.0F, UiTheme.computeInt(color, alpha), size * 2.4F).withIntensity(0.85F));
      }
   }

   // Живые обои: медленно плывущая фиолетовая дымка поверх фона
   private void drawLiveAurora(float w, float h, float scale) {
      long now = System.currentTimeMillis();
      float t = (now % 90000L) / 1000.0F;
      float[][] blobs = {{0.25F, 0.3F, 0.06F, 300.0F}, {0.7F, 0.6F, -0.045F, 360.0F}, {0.5F, 0.85F, 0.035F, 260.0F}};

      for (int i = 0; i < blobs.length; i++) {
         float cx = w * blobs[i][0] + w * blobs[i][2] * (float) Math.sin(t * (0.25F + i * 0.08F));
         float cy = h * blobs[i][1] + h * 0.08F * (float) Math.cos(t * (0.2F + i * 0.07F) + i);
         float radius = blobs[i][3] * scale;
         int color = i == 1 ? 0xFF8F7BFF : 0xFFB08CFF;
         float alpha = 0.10F + 0.05F * (float) Math.sin(t * 0.5F + i * 2.1F);
         Render2D.processData58(new SoftGlowDrawData(cx - radius, cy - radius, radius * 2.0F, radius * 2.0F, radius, UiTheme.computeInt(color, Math.max(0.04F, alpha)), radius * 0.9F).withIntensity(0.9F));
      }
   }

   private static float frac(float value) {
      return (float) Math.sin(value) * 43758.5453F % 1.0F;
   }

   // подмена кадра текстуры по таймингам GIF (вызывается каждый кадр рендера)
   private void tickGif() {
      if (this.gifFrames == null || this.gifDelays == null || this.gifFrames.size() < 2 || this.customBgTexture == null) {
         return;
      }

      long elapsedMs = (System.nanoTime() - this.gifClockNanos) / 1000000L;
      long total = 0;

      for (int delay : this.gifDelays) {
         total += delay;
      }

      long position = elapsedMs % total;
      int frame = 0;

      while (position >= this.gifDelays[frame] && frame < this.gifDelays.length - 1) {
         position -= this.gifDelays[frame];
         frame++;
      }

      if (frame != this.gifShownFrame) {
         this.gifShownFrame = frame;
         this.customBgTexture.getImage().copyFrom(this.gifFrames.get(frame));
         this.customBgTexture.upload();
      }
   }

   private NativeImage readNativeImage(Path path, String ext) throws Exception {
      if (".png".equals(ext)) {
         try (BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(path))) {
            return NativeImage.read(stream);
         }
      }

      // JPG/GIF/BMP читаем через ImageIO и переносим пиксели в NativeImage
      BufferedImage buffered = ImageIO.read(path.toFile());
      if (buffered == null) {
         throw new IllegalStateException("Unsupported image format: " + path);
      }

      NativeImage image = new NativeImage(buffered.getWidth(), buffered.getHeight(), false);
      for (int y = 0; y < buffered.getHeight(); y++) {
         for (int x = 0; x < buffered.getWidth(); x++) {
            image.setColorArgb(x, y, buffered.getRGB(x, y));
         }
      }

      return image;
   }

   private void registerCustomPhotoTexture(NativeImage image) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client == null || client.getTextureManager() == null) {
         return;
      }

      // Texture owns a copy; decoded GIF frames retain independent ownership.
      NativeImage copy = new NativeImage(image.getWidth(), image.getHeight(), false);
      copy.copyFrom(image);
      NativeImageBackedTexture texture = new NativeImageBackedTexture(CUSTOM_BG_ID::toString, copy);
      client.getTextureManager().registerTexture(CUSTOM_BG_ID, texture);
      this.customBgTexture = texture;
      horus.client.render.ImageRenderer.getImageRenderer().updateState5();
   }

   private void drawPillButton(
           float x,
           float y,
           float w,
           float h,
           float radius,
           float scale,
           String label,
           boolean accent,
           float mx,
           float my,
           boolean interactive,
           Runnable action
   ) {
      boolean hovered = interactive && UiTheme.matchesCriteria(mx, my, x, y, w, h);
      float hover = this.hoverPC("pill_" + label, hovered);
      int bgColor = accent
              ? UiTheme.computeInt2(COLOR_BTN_ACCENT, COLOR_BTN_ACCENT_HOVER, hover)
              : UiTheme.computeInt2(COLOR_BTN_DARK, COLOR_BTN_DARK_HOVER, hover);

      int borderColor = accent
              ? UiTheme.computeInt2(COLOR_BTN_ACCENT_BORDER, COLOR_BTN_ACCENT_BORDER_HOVER, hover)
              : UiTheme.computeInt2(COLOR_BTN_BORDER, COLOR_BTN_BORDER_HOVER, hover);

      int textColor = accent
              ? 0xFFFFFFFF
              : UiTheme.computeInt2(COLOR_TEXT_SECONDARY, COLOR_TEXT_PRIMARY, hover);

      Render2D.processData51(x, y, w, h, radius, bgColor);
      Render2D.processData86(x, y, w, h, radius, 0.85F * scale, borderColor);
      this.drawGlossOverlay(x, y, w, h, radius, scale);

      float fontSize = 6.8F * scale;
      this.drawCenteredString(label, x + w / 2.0F, y + (h - fontSize) / 2.0F - 0.5F * scale, fontSize, textColor);

      if (interactive) {
         this.list.add(new MainMenuScreen.DataRecord(x, y, w, h, action));
      }
   }

   private void drawSubButton(
           String id,
           float x,
           float y,
           float w,
           float h,
           float scale,
           String label,
           boolean danger,
           boolean dot,
           float mx,
           float my,
           boolean interactive,
           Runnable action
   ) {
      boolean hovered = interactive && UiTheme.matchesCriteria(mx, my, x, y, w, h);
      float hover = this.hoverPC("sub_" + id, hovered);
      float lift = hover * 1.5F * scale;
      y -= lift;

      int bgColor = UiTheme.computeInt2(0x5914151C, 0x8C232331, hover);
      int borderColor = UiTheme.computeInt2(0x10FFFFFF, danger ? 0x66F87171 : 0x669B6DFF, hover);
      int textColor = danger
              ? UiTheme.computeInt2(0xFF8C8FA3, 0xFFFFC9C9, hover)
              : UiTheme.computeInt2(0xFF8C8FA3, 0xFFFFFFFF, hover);

      Render2D.processData51(x, y, w, h, 6.0F * scale, bgColor);
      Render2D.processData86(x, y, w, h, 6.0F * scale, 0.8F * scale, borderColor);
      this.drawGlossOverlay(x, y, w, h, 6.0F * scale, scale);

      float fontSize = 5.8F * scale;
      float textWidth = this.estimateTextWidth(label, fontSize) + (dot ? 3.5F * scale : 0.0F);
      float textX = x + (w - textWidth) / 2.0F;
      if (dot) {
         float dotSize = 1.6F * scale;
         Render2D.processData51(textX, y + (h - dotSize) / 2.0F, dotSize, dotSize, dotSize / 2.0F, 0xFF34D399);
         textX += dotSize + 1.9F * scale;
      }

      FontText.processData3(FontFace.SEMI, label, textX, y + (h - fontSize) / 2.0F - 0.5F * scale, fontSize, textColor);

      if (interactive) {
         this.list.add(new MainMenuScreen.DataRecord(x - 3.0F * scale, y, w + 6.0F * scale, h + 4.0F * scale, action));
      }
   }

   private void drawTextLink(
           String id,
           String label,
           float centerX,
           float y,
           float scale,
           float mx,
           float my,
           boolean interactive,
           boolean accent,
           Runnable action
   ) {
      float fontSize = 6.5F * scale;
      float hitW = Math.max(44.0F * scale, this.estimateTextWidth(label, fontSize) + 14.0F * scale);
      float hitH = 16.0F * scale;
      float hitX = centerX - hitW / 2.0F;
      float hitY = y - 4.0F * scale;

      boolean hovered = interactive && UiTheme.matchesCriteria(mx, my, hitX, hitY, hitW, hitH);
      float hover = this.hoverPC(id, hovered);
      int color = accent
              ? UiTheme.computeInt2(COLOR_BTN_ACCENT_HOVER, 0xFFFFFFFF, hover)
              : UiTheme.computeInt2(COLOR_TEXT_MUTED, COLOR_TEXT_PRIMARY, hover);

      this.drawCenteredString(label, centerX, y - hover * 1.0F * scale, fontSize, color);
      if (interactive) {
         this.list.add(new MainMenuScreen.DataRecord(hitX, hitY, hitW, hitH, action));
      }
   }

   private void drawCenteredString(String text, float centerX, float y, float size, int color) {
      float width = this.estimateTextWidth(text, size);
      FontText.processData3(FontFace.SEMI, text, centerX - width / 2.0F, y, size, color);
   }

   private float estimateTextWidth(String text, float size) {
      if (text == null || text.isEmpty()) {
         return 0.0F;
      }
      float units = 0.0F;
      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (c == ' ') units += 0.30F;
         else if ("iIl1.,!|:;'".indexOf(c) >= 0) units += 0.28F;
         else if ("mwMWЩШЖЮФ".indexOf(c) >= 0) units += 0.78F;
         else if (Character.isUpperCase(c)) units += 0.62F;
         else units += 0.52F;
      }
      return units * size;
   }

   @Override
   public boolean mouseClicked(Click click, boolean doubled) {
      if (click.button() == 0) {
         if (this.pendingTransition != null) {
            return true;
         }
         float factor = spaceFactor();
         float mx = (float)click.x() * factor;
         float my = (float)click.y() * factor;

         // модальные окна поверх всего
         if (this.modals.isOpen()) {
            this.modalFocus = null;
            if (this.modals.inside(mx, my)) {
               this.modalFocus = null;
               for (int index = this.modalList.size() - 1; index >= 0; index--) {
                  MainMenuScreen.DataRecord area = this.modalList.get(index);
                  if (UiTheme.matchesCriteria(mx, my, area.x(), area.y(), area.w(), area.h())) {
                     this.modalFocus = null;
                     area.action().run();
                     return true;
                  }
               }
            } else {
               this.modals.close();
            }

            return true;
         }

         if (this.flag && this.animation.computeDouble() > 0.1) {
            if (this.accountManagerPanel.matchesCriteria(mx, my)) {
               return true;
            }
            // Smoothly close account manager panel when clicking outside
            this.toggleAlt();
            return true;
         }

         for (int index = this.list.size() - 1; index >= 0; index--) {
            if (UiTheme.matchesCriteria(
                    mx,
                    my,
                    this.list.get(index).x(),
                    this.list.get(index).y(),
                    this.list.get(index).w(),
                    this.list.get(index).h()
            )) {
               this.list.get(index).action().run();
               return true;
            }
         }
      }

      return super.mouseClicked(click, doubled);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      float factor = spaceFactor();
      if (this.modals.isOpen()) {
         this.modals.scroll((float)verticalAmount);
         return true;
      }

      if (this.flag && this.accountManagerPanel.matchesCriteria2((float)mouseX * factor, (float)mouseY * factor, (float)verticalAmount)) {
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
      }
   }

   @Override
   public boolean keyPressed(KeyInput keyInput) {
      if (this.modals.isOpen()) {
         if (keyInput.key() == 256) {
            this.modals.close();
            return true;
         }

         if (keyInput.key() == 259 && this.modals.backspace()) {
            return true;
         }

         return true;
      }

      if (!this.flag && (keyInput.key() == 344 || keyInput.key() == 290)) { // Right Shift or F11/custom
         this.transitionTo(() -> {
            ClickGuiScreen gui = ClickGuiScreen.instance();
            gui.setParentScreen(this);
            MinecraftClient.getInstance().setScreen(gui);
         });
         return true;
      }
      if (this.flag) {
         if (this.accountManagerPanel.matchesCriteria3(keyInput.key())) {
            return true;
         }

         if (keyInput.key() == 256) {
            this.toggleAlt();
            return true;
         }
      }


      return super.keyPressed(keyInput);
   }

   @Override
   public boolean charTyped(CharInput charInput) {
      if (this.modals.isOpen() && charInput.codepoint() >= 32) {
         this.modals.charTyped((char)charInput.codepoint());
         return true;
      }

      if (this.flag && this.accountManagerPanel.matchesCriteria4(charInput.codepoint())) {
         return true;
      } else {
         return super.charTyped(charInput);
      }
   }

   // ===== поддержка модалок =====

   void addModalArea(float x, float y, float w, float h, Runnable action) {
      if (this.modals.isOpen()) {
         this.modalList.add(new MainMenuScreen.DataRecord(x, y, w, h, action));
      }
   }

   void renderModalInput(String id, String value, float x, float y, float w, float h, float scale) {
      boolean focused = id.equals(this.modalFocus);
      Render2D.processData51(x, y, w, h, 5.0F * scale, 0x80000000);
      Render2D.processData86(x, y, w, h, 5.0F * scale, 0.8F * scale, focused ? 0xFF9B6DFF : 0x1FFFFFFF);
      String display = value == null || value.isEmpty()
              ? (focused ? "" : ("serverSearch".equals(id) ? Lang.tr("Поиск по названию или адресу", "Search name or address") : "...")) : value;
      if ("serverSearch".equals(id) && display.length() > 42) {
         display = "…" + display.substring(display.length() - 41);
      }
      if (focused && (System.currentTimeMillis() / 400) % 2 == 0) {
         display = display + "|";
      }

      FontText.processData3(FontFace.SEMI, display, x + 8.0F * scale, y + (h - 6.5F * scale) / 2.0F, 6.5F * scale,
              "serverSearch".equals(id) && (value == null || value.isEmpty()) && !focused ? 0xFF8C8FA3 : 0xFFF4F4F8);
   }

   String modalFocus() {
      return this.modalFocus;
   }

   void setModalFocus(String id) {
      this.modalFocus = id;
   }

   static String formatLastPlayed(long timeMillis) {
      long diff = System.currentTimeMillis() - timeMillis;
      long minutes = diff / 60000L;
      if (minutes < 1L) {
         return "сейчас";
      }

      if (minutes < 60L) {
         return minutes + " мин назад";
      }

      long hours = minutes / 60L;
      if (hours < 24L) {
         return hours + " ч назад";
      }

      return hours / 24L + " дн назад";
   }

   private float hoverPC(String key, boolean hovered) {
      AnimatedValue animatedValue = this.map.computeIfAbsent(key, k -> new AnimatedValue(240L));
      animatedValue.processData(hovered ? 1.0 : 0.0);
      return (float)animatedValue.computeDouble();
   }

   private void toggleAlt() {
      this.flag = !this.flag;
      if (this.flag) {
         this.accountManagerPanel.processData2(this::toggleAlt);
      } else {
         this.accountManagerPanel.updateState();
      }
   }


   private static float spaceFactor() {
      return GuiScale.computeInt() / GuiScale.computeFloat();
   }

   private record DataRecord(float x, float y, float w, float h, Runnable action) {
   }
}

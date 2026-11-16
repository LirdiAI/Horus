package horus.client.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import ru.dreamix.protection.api.impl.profile.ProfileAPI;

public final class FallenUiHelper {
   // Palette constants matching the Fallen / Horus Dark Minimalist UI
   public static final int COLOR_BG = 0xFF0B0D10;
   public static final int COLOR_BTN_DARK = 0xFF14171D;
   public static final int COLOR_BTN_DARK_HOVER = 0xFF1E2127;
   // Bright luminous neon white-purple palette
   public static final int COLOR_BTN_ACCENT = 0xFF8B5CF6;
   public static final int COLOR_BTN_ACCENT_HOVER = 0xFFD8B4FE;

   public static final int COLOR_BTN_BORDER = 0xFF383E4C;
   public static final int COLOR_BTN_BORDER_HOVER = 0xFFFFFFFF;
   public static final int COLOR_BTN_ACCENT_BORDER = 0xFFA855F7;
   public static final int COLOR_BTN_ACCENT_BORDER_HOVER = 0xFFFFFFFF;

   public static final int COLOR_TEXT_PRIMARY = 0xFFEBECEF;
   public static final int COLOR_TEXT_SECONDARY = 0xFF8E95A2;
   public static final int COLOR_TEXT_MUTED = 0xFF636875;
   public static final int COLOR_TEXT_POLICY = 0xFF3B404A;

   private FallenUiHelper() {}

   public static boolean isFallenScreen(Screen screen) {
      if (screen == null) return false;
      if (screen instanceof MainMenuScreen) return false;
      if (screen instanceof ProxyScreen) return false;
      if (screen instanceof ClickGuiScreen) return false;
      if (screen instanceof HandledScreen) return false;
      if (screen instanceof ChatScreen) return false;

      if (screen instanceof MultiplayerScreen
            || screen instanceof SelectWorldScreen
            || screen instanceof OptionsScreen
            || screen instanceof GameOptionsScreen) {
         return true;
      }

      String simpleName = screen.getClass().getSimpleName();
      if (simpleName.isEmpty() || simpleName.contains("Chat") || simpleName.contains("Hud") || simpleName.contains("InGame")) {
         return false;
      }
      return simpleName.contains("Options")
            || simpleName.contains("Server")
            || simpleName.contains("World")
            || simpleName.contains("Multiplayer")
            || simpleName.contains("Singleplayer")
            || simpleName.contains("Language")
            || simpleName.contains("Accessibility")
            || simpleName.contains("Controls")
            || simpleName.contains("Skin")
            || simpleName.contains("Sound")
            || simpleName.contains("Video")
            || simpleName.contains("Mouse")
            || simpleName.contains("Keybind")
            || simpleName.contains("Stats")
            || simpleName.contains("Pack")
            || simpleName.contains("DirectConnect")
            || simpleName.contains("AddServer")
            || simpleName.contains("Connect")
            || simpleName.contains("Sodium");
   }

   public static boolean isMenuOrSettingsActive() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc == null || mc.currentScreen == null) return false;
      return isFallenScreen(mc.currentScreen);
   }

   public static void drawRoundedRect(DrawContext context, int x, int y, int w, int h, int radius, int color) {
      if (w <= 0 || h <= 0) return;
      int r = Math.min(radius, Math.min(w, h) / 2);
      if (r <= 0) {
         context.fill(x, y, x + w, y + h, color);
         return;
      }

      context.fill(x + r, y, x + w - r, y + h, color);
      context.fill(x, y + r, x + r, y + h - r, color);
      context.fill(x + w - r, y + r, x + w, y + h - r, color);

      for (int i = 0; i < r; i++) {
         int dx = r - (int)Math.round(Math.sqrt((double)(r * r - (r - i) * (r - i))));
         context.fill(x + dx, y + i, x + r, y + i + 1, color);
         context.fill(x + w - r, y + i, x + w - dx, y + i + 1, color);
         context.fill(x + dx, y + h - 1 - i, x + r, y + h - i, color);
         context.fill(x + w - r, y + h - 1 - i, x + w - dx, y + h - i, color);
      }
   }

   public static void drawRectOutline(DrawContext context, int x, int y, int w, int h, int color) {
      if (w <= 0 || h <= 0) return;
      context.fill(x, y, x + w, y + 1, color);
      context.fill(x, y + h - 1, x + w, y + h, color);
      context.fill(x, y + 1, x + 1, y + h - 1, color);
      context.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
   }

   public static void drawRoundedOutline(DrawContext context, int x, int y, int w, int h, int radius, int color) {
      if (w <= 0 || h <= 0) return;
      int r = Math.min(radius, Math.min(w, h) / 2);
      if (r <= 0) {
         drawRectOutline(context, x, y, w, h, color);
         return;
      }

      context.fill(x + r, y, x + w - r, y + 1, color);
      context.fill(x + r, y + h - 1, x + w - r, y + h, color);
      context.fill(x, y + r, x + 1, y + h - r, color);
      context.fill(x + w - 1, y + r, x + w, y + h - r, color);

      for (int i = 0; i < r; i++) {
         int dx = r - (int)Math.round(Math.sqrt((double)(r * r - (r - i) * (r - i))));
         context.fill(x + dx, y + i, x + dx + 1, y + i + 1, color);
         context.fill(x + w - 1 - dx, y + i, x + w - dx, y + i + 1, color);
         context.fill(x + dx, y + h - 1 - i, x + dx + 1, y + h - i, color);
         context.fill(x + w - 1 - dx, y + h - 1 - i, x + w - dx, y + h - i, color);
      }
   }

   public static void renderFallenBackground(DrawContext context, Screen screen) {
      int w = screen.width;
      int h = screen.height;
      MinecraftClient mc = MinecraftClient.getInstance();
      boolean inGame = mc != null && mc.world != null;

      try {
         if (inGame && mc.options != null && mc.options.getMenuBackgroundBlurrinessValue() >= 1.0F) {
            context.applyBlur();
         }
      } catch (Throwable ignored) {}

      // Solid deep obsidian dark background in main menu, translucent obsidian in game
      int bg = inGame ? 0xD40B0D10 : COLOR_BG;
      context.fill(0, 0, w, h, bg);

      // Top Header Divider (sleek slate line #383E4C at y=32)
      context.fill(0, 32, w, 33, COLOR_BTN_BORDER);

      // Bottom Footer Divider (sleek slate line #383E4C at y=h-36)
      context.fill(0, h - 36, w, h - 35, COLOR_BTN_BORDER);
   }

   public static void drawPillButton(DrawContext context, ClickableWidget widget) {
      int x = widget.getX();
      int y = widget.getY();
      int w = widget.getWidth();
      int h = widget.getHeight();

      boolean hovered = widget.isHovered() || widget.isSelected();
      boolean active = widget.active;

      String text = widget.getMessage() != null ? widget.getMessage().getString().toLowerCase() : "";
      boolean isAccent = text.contains("done")
            || text.contains("готово")
            || text.contains("join")
            || text.contains("подключ")
            || text.contains("play")
            || text.contains("играть")
            || text.contains("create")
            || text.contains("создать")
            || text.contains("save")
            || text.contains("сохранить");

      int bgColor;
      int borderColor;

      if (!active) {
         bgColor = 0xFF0E1014;
         borderColor = 0xFF232730;
      } else if (isAccent) {
         bgColor = hovered ? COLOR_BTN_ACCENT_HOVER : COLOR_BTN_ACCENT;
         borderColor = hovered ? COLOR_BTN_BORDER_HOVER : COLOR_BTN_ACCENT_BORDER;
      } else {
         bgColor = hovered ? COLOR_BTN_DARK_HOVER : COLOR_BTN_DARK;
         borderColor = hovered ? COLOR_BTN_BORDER_HOVER : COLOR_BTN_BORDER;
      }

      int radius = Math.min(6, h / 3);
      drawRoundedRect(context, x, y, w, h, radius, bgColor);
      drawRoundedOutline(context, x, y, w, h, radius, borderColor);
   }

   public static void drawFallenSlider(DrawContext context, SliderWidget slider, double value) {
      int x = slider.getX();
      int y = slider.getY();
      int w = slider.getWidth();
      int h = slider.getHeight();

      boolean hovered = slider.isHovered() || slider.isSelected();
      boolean active = slider.active;

      int radius = Math.min(6, h / 3);
      int trackBg = 0xFF14171D;
      int trackBorder = hovered ? COLOR_BTN_BORDER_HOVER : COLOR_BTN_BORDER;

      // Draw track
      drawRoundedRect(context, x, y, w, h, radius, trackBg);
      drawRoundedOutline(context, x, y, w, h, radius, trackBorder);

      // Draw thumb
      int thumbW = 8;
      int thumbX = x + (int)(value * (double)(w - thumbW));
      int thumbBg = hovered ? COLOR_BTN_ACCENT_HOVER : COLOR_BTN_ACCENT;
      int thumbBorder = hovered ? COLOR_BTN_BORDER_HOVER : COLOR_BTN_BORDER;

      drawRoundedRect(context, thumbX, y, thumbW, h, Math.min(radius, thumbW / 2), thumbBg);
      drawRoundedOutline(context, thumbX, y, thumbW, h, Math.min(radius, thumbW / 2), thumbBorder);

      // Draw text
      MinecraftClient mc = MinecraftClient.getInstance();
      if (slider.getMessage() != null && mc != null && mc.textRenderer != null) {
         int textColor = active ? (hovered ? 0xFFFFFFFF : COLOR_TEXT_PRIMARY) : COLOR_TEXT_MUTED;
         context.drawCenteredTextWithShadow(mc.textRenderer, slider.getMessage(), x + w / 2, y + (h - 8) / 2, textColor);
      }
   }

   public static void drawFallenListBackground(DrawContext context, int x, int y, int w, int h) {
      // Dark transparent list surface instead of dirt texture
      context.fill(x, y, x + w, y + h, 0x880B0D10);
   }

   public static void drawFallenSeparators(DrawContext context, int x, int y, int w, int h) {
      // Sleek 1px slate divider lines instead of vanilla dirt gradients
      context.fill(x, y, x + w, y + 1, COLOR_BTN_BORDER);
      context.fill(x, y + h - 1, x + w, y + h, COLOR_BTN_BORDER);
   }

   public static void drawFallenSelectionHighlight(DrawContext context, int x, int y, int w, int h) {
      // Subtle accent glow card with white border for selected entry
      context.fill(x, y, x + w, y + h, 0x2E14171D);
      drawRectOutline(context, x, y, w, h, COLOR_BTN_BORDER_HOVER);
   }

   public static void drawFallenScrollbar(DrawContext context, int x, int y, int w, int h, int trackY, int trackH) {
      // Track
      context.fill(x, trackY, x + w, trackY + trackH, 0xFF14171D);
      drawRectOutline(context, x, trackY, w, trackH, COLOR_BTN_BORDER);

      // Thumb
      int radius = Math.min(4, w / 2);
      drawRoundedRect(context, x, y, w, h, radius, COLOR_BTN_ACCENT);
      drawRoundedOutline(context, x, y, w, h, radius, COLOR_BTN_BORDER_HOVER);
   }
}

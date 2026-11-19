package horus.client.ui.hud;

import horus.client.render.FontText;
import horus.client.util.GuiScale;
import horus.client.util.MusicWatcher;
import horus.client.render.ScissorStack;
import horus.client.module.render.DynamicIslandModule;
import horus.client.render.ThemeColors;
import horus.client.ui.UiTheme;
import horus.client.render.CornerRadii;
import horus.client.render.ShapeDrawing;

import horus.client.util.Animation;

import horus.mixin.BossBarHudMixin;
import horus.client.util.Easings;
import horus.client.render.FontFace;
import horus.client.module.render.InterfaceModule;
import ru.dreamix.protection.api.impl.profile.ProfileAPI;
import horus.client.render.ThemeColors;
import horus.client.render.Render2D;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class WatermarkHud {
   private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
   private static final List<String> FIXED_ORDER = List.of("user", "ping", "logo", "fps", "coords", "time");
   private static final List<WatermarkHud.SubContext> list = new ArrayList<>(
      List.of(
         new WatermarkHud.SubContext("user", "Логин"),
         new WatermarkHud.SubContext("ping", "Пинг"),
         new WatermarkHud.SubContext("logo", null),
         new WatermarkHud.SubContext("fps", "ФПС"),
         new WatermarkHud.SubContext("coords", "Координаты"),
         new WatermarkHud.SubContext("time", "Время")
      )
   );
   private static final Animation animator = new Animation();
   private static final Animation animator2 = new Animation();
   private static final Animation animator3 = new Animation();
   private static float floatVal;
   private static float floatVal2;
   private static float floatVal3;
   private static float floatVal4;
   private static WatermarkHud.SubContext nested0435;
   private static float floatVal5;
   private static final Animation islandAnimation = new Animation();
   private static final Animation panelAnimation = new Animation();
   private static boolean rightMouseFlag;
   private static boolean leftMouseFlag;
   private static float panelRectX;
   private static float panelRectY;
   private static float panelRectW;
   private static float panelRectH;

   public static boolean matchesCriteria(double local3929_0, double local3929_2, int local3929_4) {
      // Суб-элементы ватермарки зафиксированы: клик передается в HudManager для перемещения всей ватермарки
      return false;
   }

   public static void processData(MinecraftClient minecraftClient, float local3930_1, float local3930_2) {
      animator.update();
      animator.animateTo(1.0, 0.3, Easings.EASE_OUT_QUART);
      animator2.update();
      animator2.animateTo(minecraftClient.getCurrentFps(), 0.15, Easings.EASE_OUT_QUART);
      String name = ProfileAPI.get().getModel().getName();
      if (name == null || name.isEmpty() || name.equalsIgnoreCase("user")) {
         name = horus.client.util.LauncherSession.getLauncherUsername();
      }
      boolean local3930_4 = InterfaceModule.getInstance().checkState() && HudManager.computeFloat2() > 0.0F;
      list.sort(
         Comparator.comparingInt(
            local3932_1 -> {
               int idx = FIXED_ORDER.indexOf(local3932_1.strVal);
               return idx >= 0 ? idx : Integer.MAX_VALUE;
            }
         )
      );

      int local3930_5 = minecraftClient.player != null
            && minecraftClient.getNetworkHandler() != null
            && minecraftClient.getNetworkHandler().getPlayerListEntry(minecraftClient.player.getUuid()) != null
         ? minecraftClient.getNetworkHandler().getPlayerListEntry(minecraftClient.player.getUuid()).getLatency()
         : 0;

      for (WatermarkHud.SubContext local3930_7 : list) {
         local3930_7.flag = local3930_7.strVal2 == null || InterfaceModule.getInstance().components.matchesOption(local3930_7.strVal2);
         switch (local3930_7.strVal) {
            case "logo":
               local3930_7.processData3(null, null, 13.0F);
               break;
            case "avatar":
               local3930_7.processData3(null, null, 12.0F);
               break;
            case "user":
               local3930_7.processData3(name, null, 7.0F);
               break;
            case "fps":
               local3930_7.processData3(String.valueOf((int)animator2.floatValue()), "FPS", 7.0F);
               break;
            case "ping":
               local3930_7.processData3(String.valueOf(local3930_5), "ms", 7.0F);
               break;
            case "coords":
               local3930_7.processData3(
                  minecraftClient.player == null
                     ? "0 0 0"
                     : minecraftClient.player.getBlockX() + " " + minecraftClient.player.getBlockY() + " " + minecraftClient.player.getBlockZ(),
                  null,
                  7.0F
               );
               break;
            case "time":
               local3930_7.processData3(LocalTime.now().format(dateTimeFormatter), null, 7.0F);
         }

         local3930_7.animator6.update();
         local3930_7.animator6
            .animateTo(
               local3930_7.strVal.equals("fps")
                  ? computeFloat(animator2.floatValue(), 60.0F, 25.0F)
                  : (local3930_7.strVal.equals("ping") ? computeFloat(local3930_5, 120.0F, 300.0F) : 0.0),
               0.5,
               Easings.EASE_OUT_QUART
            );
         local3930_7.animator4.update();
         local3930_7.animator4.animateTo(local3930_7.computeFloat5(), 0.05, Easings.EASE_OUT_EXPO);
      }

      MusicWatcher.start();
      boolean islandMode = MusicWatcher.isPlaying() && DynamicIslandModule.getInstance().isEnabled();

      float local3930_6 = 0.0F;

      for (WatermarkHud.SubContext local3930_8 : list) {
         if (!(local3930_8.computeFloat3() < 1.0F)) {
            if (local3930_6 > 0.0F) {
               local3930_6 += 4.0F;
            }

            local3930_6 += local3930_8.computeFloat3();
         }
      }

      floatVal4 = Math.max(local3930_6 + 8.0F, 1.0F);
      if (islandMode) {
         floatVal4 = computeIslandWidth();
      }
      float local3930_7 = GuiScale.computeInt() / GuiScale.computeFloat();
      if (local3930_4) {
         local3930_1 = HudManager.computeFloat2() / 2.0F - floatVal4 / 2.0F;
         local3930_2 = 5.0F;
      } else {
         float[] floatArray = HudManager.computeFloat5("watermark", local3930_1, local3930_2);
         local3930_1 = floatArray[0];
         local3930_2 = floatArray[1];
      }

      int local3930_8 = minecraftClient.inGameHud != null ? ((BossBarHudMixin)minecraftClient.inGameHud.getBossBarHud()).getEvents().size() : 0;
      float targetValue = 0.0F;
      if (local3930_8 > 0) {
         float local3930_10 = (12.0F + (local3930_8 - 1) * 19.0F + 8.0F + 1.5F) * local3930_7;
         float local3930_11 = HudManager.computeFloat2() / 2.0F;
         if (local3930_1 < local3930_11 + 91.0F * local3930_7 && local3930_1 + floatVal4 > local3930_11 - 91.0F * local3930_7 && local3930_2 < local3930_10) {
            targetValue = local3930_10 - local3930_2;
         }
      }

      animator3.update();
      animator3.animateTo(targetValue, 0.2, Easings.EASE_OUT_QUAD);
      local3930_2 += animator3.floatValue();
      floatVal2 = local3930_1;
      floatVal3 = local3930_2;
      HudManager.processData9("watermark", local3930_1, local3930_2, floatVal4, 15.0F);
      floatVal = HudManager.computeFloat4() > 0.0F ? (local3930_2 + 15.0F) / HudManager.computeFloat4() : 0.0F;

      if (islandMode) {
         renderDynamicIsland(minecraftClient, local3930_1, local3930_2, local3930_7);
         return;
      }

      float local3930_10 = animator.floatValue();
      if (!(local3930_10 <= 0.001F)) {
         boolean local3930_11 = HudRenderContext.checkState2();
         float local3930_12 = (float)(minecraftClient.mouse.getScaledX(minecraftClient.getWindow()) * local3930_7);
         float local3930_13 = (float)(minecraftClient.mouse.getScaledY(minecraftClient.getWindow()) * local3930_7);
         if (nested0435 != null) {
            processData2(local3930_12);
         }

         float local3930_14 = local3930_1 + 4.0F;

         for (WatermarkHud.SubContext local3930_16 : list) {
            local3930_16.animator5.update();
            float value = local3930_16 == nested0435
               ? Math.max(local3930_1 + 4.0F, Math.min(local3930_12 - floatVal5, local3930_1 + floatVal4 - 4.0F - local3930_16.computeFloat3()))
               : local3930_14;
            if (!local3930_16.flag2) {
               local3930_16.flag2 = true;
               local3930_16.animator5.resetTo(value);
            } else if (local3930_16 == nested0435) {
               local3930_16.animator5.resetTo(value);
            } else {
               local3930_16.animator5.animateTo(value, 0.15, Easings.EASE_OUT_EXPO);
            }

            if (local3930_16.computeFloat3() >= 1.0F) {
               local3930_14 += local3930_16.computeFloat3() + 4.0F;
            }
         }

         float local3930_15 = Float.MAX_VALUE;
         float local3930_16 = -Float.MAX_VALUE;

         for (WatermarkHud.SubContext local3930_18 : list) {
            if (!(local3930_18.computeFloat3() < 1.0F)) {
               local3930_15 = Math.min(local3930_15, local3930_18.animator5.floatValue());
               local3930_16 = Math.max(local3930_16, local3930_18.animator5.floatValue() + local3930_18.computeFloat3());
            }
         }

         float local3930_17 = local3930_15 == Float.MAX_VALUE ? local3930_1 : local3930_15 - 4.0F;
         float local3930_18 = local3930_15 == Float.MAX_VALUE ? floatVal4 : local3930_16 - local3930_15 + 8.0F;
         HudStyle.processData4(local3930_17, local3930_2, local3930_18, 15.0F, local3930_10, 6.75F);
         WatermarkHud.SubContext local3930_19 = null;
         float local3930_20 = local3930_2 + 7.5F;

         for (WatermarkHud.SubContext local3930_22 : list) {
            local3930_22.animator7.update();
            local3930_22.animator7.animateTo(local3930_22 == local3930_19 ? 1.0 : 0.0, 0.2, Easings.EASE_OUT_QUART);
            if (local3930_22 != nested0435 && local3930_22.flag && local3930_22.computeFloat3() >= 1.0F) {
               local3930_22.processData4(local3930_2, local3930_20, local3930_10);
            }
         }

         if (nested0435 != null && nested0435.flag && nested0435.computeFloat3() >= 1.0F) {
            nested0435.processData4(local3930_2, local3930_20, local3930_10);
         }
      }
   }

   private static int computeInt(int local3931_0, float local3931_1) {
      int local3931_2 = UiTheme.computeInt2(local3931_0, -42406, local3931_1);
      return UiTheme.computeInt2(local3931_2, -55256, ThemeColors.computeFloat((local3931_1 - 0.7F) / 0.3F));
   }

   // ============ DynamicIsland ============

   private static float computeIslandWidth() {
      float titleW = FontText.computeFloat(FontFace.SEMI, MusicWatcher.getTitle(), 6.0F);
      float artistW = FontText.computeFloat(FontFace.SEMI, MusicWatcher.getArtist(), 6.0F);
      float width = 3.0F + 9.0F + 3.0F + titleW + 2.0F + artistW + 4.0F;
      return Math.max(64.0F, Math.min(width, 180.0F));
   }

   private static void renderDynamicIsland(MinecraftClient minecraftClient, float x, float y, float scale) {
      islandAnimation.update();
      islandAnimation.animateTo(1.0, 0.3, Easings.EASE_OUT_EXPO);
      float alpha = islandAnimation.floatValue();

      // Точно как ватермарка: высота 15, шрифт SEMI 6.0F, тот же фон
      float width = floatVal4;
      float height = 15.0F;
      float coverSize = 9.0F;
      float coverX = x + 3.0F;
      float coverY = y + height / 2.0F - coverSize / 2.0F;
      float cy = y + height / 2.0F;

      HudStyle.processData4(x, y, width, 15.0F, alpha, 6.75F);

      Identifier coverId = MusicWatcher.getCover();
      if (coverId != null) {
         Render2D.processData89(coverId.toString(), coverX, coverY, coverSize, coverSize, 2.5F, UiTheme.computeInt(-1, alpha));
      } else {
         Render2D.processData89("assets/horus/textures/gui/mainmenu/icons/logo.png", coverX, coverY, coverSize, coverSize, 2.5F, UiTheme.computeInt(-1, alpha));
      }

      // ПКМ по острову — переключение плавной текстовой дорожки
      float mouseX = (float)(minecraftClient.mouse.getScaledX(minecraftClient.getWindow()) * scale);
      float mouseY = (float)(minecraftClient.mouse.getScaledY(minecraftClient.getWindow()) * scale);
      boolean rightMouse = GLFW.glfwGetMouseButton(minecraftClient.getWindow().getHandle(), 1) == 1;
      if (rightMouse && !rightMouseFlag && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
         DynamicIslandModule.getInstance().textTrackSetting.toggleValue();
      }

      rightMouseFlag = rightMouse;

      float panelWidth = Math.max(width, 170.0F);
      float panelX = Math.max(5.0F, Math.min(x + width / 2.0F - panelWidth / 2.0F, HudManager.computeFloat2() - panelWidth - 5.0F));
      float panelY = y + 15.0F + 2.0F;
      float panelHeight = 52.0F;

      boolean hover = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
         || panelAnimation.floatValue() > 0.05F
            && mouseX >= panelRectX && mouseX <= panelRectX + panelRectW && mouseY >= panelRectY && mouseY <= panelRectY + panelRectH;
      panelAnimation.update();
      panelAnimation.animateTo(hover ? 1.0 : 0.0, 0.3, Easings.EASE_OUT_EXPO);
      float expanded = panelAnimation.floatValue();

      if (expanded > 0.01F) {
         renderIslandPanel(minecraftClient, panelX, panelY, panelWidth, panelHeight, mouseX, mouseY, alpha * expanded);
         panelRectX = panelX;
         panelRectY = panelY;
         panelRectW = panelWidth;
         panelRectH = panelHeight;
      }

      float textX = x + 3.0F + coverSize + 3.0F;
      float areaWidth = width - (textX - x) - 4.0F;

      if (DynamicIslandModule.getInstance().textTrackSetting.isChecked()) {
         // Плавная бегущая дорожка: название — исполнители
         String full = MusicWatcher.getTitle() + "  —  " + MusicWatcher.getArtist();
         float textWidth = FontText.computeFloat(FontFace.SEMI, full, 6.0F);
         int white = UiTheme.computeInt(-1, alpha);
         ScissorStack.processData2(textX, y + 1.0F, areaWidth, height - 2.0F);

         if (textWidth <= areaWidth) {
            FontText.processData3(FontFace.SEMI, full, textX, cy - 3.5F, 6.0F, white);
         } else {
            float total = textWidth + 14.0F;
            float offset = (float)((System.currentTimeMillis() / 30L) % (long)Math.max(1, Math.round(total)));
            FontText.processData3(FontFace.SEMI, full, textX - offset, cy - 3.5F, 6.0F, white);
            FontText.processData3(FontFace.SEMI, full, textX - offset + total, cy - 3.5F, 6.0F, white);
         }

         ScissorStack.updateState2();
      } else {
         // В стиле ватермарки: название белым, исполнители серым после него
         String trackName = trimToWidth(MusicWatcher.getTitle(), areaWidth - FontText.computeFloat(FontFace.SEMI, MusicWatcher.getArtist(), 6.0F) - 2.0F, 6.0F);
         FontText.processData3(FontFace.SEMI, trackName, textX, cy - 3.5F, 6.0F, UiTheme.computeInt(-1, alpha));
         float titleDrawn = FontText.computeFloat(FontFace.SEMI, trackName, 6.0F);
         String performers = trimToWidth(MusicWatcher.getArtist(), areaWidth - titleDrawn - 2.0F, 6.0F);
         FontText.processData3(FontFace.SEMI, performers, textX + titleDrawn + 2.0F, cy - 3.5F, 6.0F, UiTheme.computeInt(0xFF9AA0A6, alpha));
      }
   }

   private static void renderIslandPanel(MinecraftClient minecraftClient, float px, float py, float pw, float ph, float mouseX, float mouseY, float alpha) {
      if (alpha < 0.01F) {
         return;
      }

      String artist = MusicWatcher.getArtist();
      String track = MusicWatcher.getTitle();

      ShapeDrawing.processData5(px, py, pw, ph, 0.0F, 0xEE121319, alpha, CornerRadii.uniform(10.0F));

      // Исполнители сверху (серым)
      String artistLine = trimToWidth(artist, pw - 16.0F, 5.5F);
      FontText.processData3(FontFace.SEMI, artistLine, px + pw / 2.0F - FontText.computeFloat(FontFace.SEMI, artistLine, 5.5F) / 2.0F, py + 7.0F, 5.5F, UiTheme.computeInt(0xFF9AA0A6, alpha));

      // Название крупно (белым)
      String titleLine = trimToWidth(track, pw - 16.0F, 8.0F);
      FontText.processData3(FontFace.SEMI, titleLine, px + pw / 2.0F - FontText.computeFloat(FontFace.SEMI, titleLine, 8.0F) / 2.0F, py + 17.0F, 8.0F, UiTheme.computeInt(-1, alpha));

      float rowY = py + 39.0F;

      // Крупная обложка слева
      float coverSize = 24.0F;
      float coverX = px + 9.0F;
      float coverY = rowY - coverSize / 2.0F;
      Identifier coverId = MusicWatcher.getCover();
      if (coverId != null) {
         Render2D.processData89(coverId.toString(), coverX, coverY, coverSize, coverSize, 5.0F, UiTheme.computeInt(-1, alpha));
      } else {
         Render2D.processData89("assets/horus/textures/gui/mainmenu/icons/logo.png", coverX, coverY, coverSize, coverSize, 5.0F, UiTheme.computeInt(-1, alpha));
      }

      // Кнопки справа: ◀◀ / пауза / ▶▶
      float nextX = px + pw - 9.0F;
      float playX = nextX - 20.0F;
      float prevX = playX - 19.0F;
      float nextSize = 13.0F;
      float playSize = 17.0F;
      float prevSize = 13.0F;
      int darkCircle = UiTheme.computeInt(0xFF26272E, alpha);
      int whiteCircle = UiTheme.computeInt(-1, alpha);
      int darkGlyph = UiTheme.computeInt(0xFF17181D, alpha);
      int lightGlyph = UiTheme.computeInt(-1, alpha);

      ShapeDrawing.processData5(nextX - nextSize / 2.0F, rowY - nextSize / 2.0F, nextSize, nextSize, 0.0F, darkCircle, alpha, CornerRadii.uniform(nextSize / 2.0F));
      FontText.processData3(FontFace.SEMI, "▶", nextX - FontText.computeFloat(FontFace.SEMI, "▶", 5.0F) / 2.0F, rowY - 2.5F, 5.0F, lightGlyph);

      ShapeDrawing.processData5(prevX - prevSize / 2.0F, rowY - prevSize / 2.0F, prevSize, prevSize, 0.0F, darkCircle, alpha, CornerRadii.uniform(prevSize / 2.0F));
      FontText.processData3(FontFace.SEMI, "◀", prevX - FontText.computeFloat(FontFace.SEMI, "◀", 5.0F) / 2.0F, rowY - 2.5F, 5.0F, lightGlyph);

      ShapeDrawing.processData5(playX - playSize / 2.0F, rowY - playSize / 2.0F, playSize, playSize, 0.0F, whiteCircle, alpha, CornerRadii.uniform(playSize / 2.0F));
      if (MusicWatcher.isPlaying()) {
         float barW = 1.6F;
         float barH = 6.0F;
         ShapeDrawing.processData5(playX - 2.2F - barW / 2.0F, rowY - barH / 2.0F, barW, barH, 0.0F, darkGlyph, alpha, CornerRadii.uniform(0.6F));
         ShapeDrawing.processData5(playX + 2.2F - barW / 2.0F, rowY - barH / 2.0F, barW, barH, 0.0F, darkGlyph, alpha, CornerRadii.uniform(0.6F));
      } else {
         FontText.processData3(FontFace.SEMI, "▶", playX - FontText.computeFloat(FontFace.SEMI, "▶", 5.5F) / 2.0F, rowY - 2.75F, 5.5F, darkGlyph);
      }

      // Полоса прогресса: позиция — бар — остаток
      float posSeconds = MusicWatcher.getPlayPosition();
      float endSeconds = Math.max(1.0F, MusicWatcher.getDuration());
      String posText = formatTime(posSeconds);
      String endText = "-" + formatTime(endSeconds);
      float posTextX = px + 9.0F + coverSize + 7.0F;
      float endTextRight = prevX - prevSize / 2.0F - 6.0F;
      float endTextX = endTextRight - FontText.computeFloat(FontFace.SEMI, endText, 5.0F);
      float barStart = posTextX + FontText.computeFloat(FontFace.SEMI, posText, 5.0F) + 4.0F;
      float barEnd = endTextX - 4.0F;
      float barWidth = Math.max(8.0F, barEnd - barStart);

      FontText.processData3(FontFace.SEMI, posText, posTextX, rowY - 2.5F, 5.0F, UiTheme.computeInt(-1, alpha));
      FontText.processData3(FontFace.SEMI, endText, endTextX, rowY - 2.5F, 5.0F, UiTheme.computeInt(0xFF9AA0A6, alpha));

      float progress = Math.max(0.0F, Math.min(1.0F, posSeconds / endSeconds));
      ShapeDrawing.processData5(barStart, rowY - 1.25F, barWidth, 2.5F, 0.0F, UiTheme.computeInt(0xFF3A3B44, alpha), alpha, CornerRadii.uniform(1.0F));
      if (barWidth * progress > 0.5F) {
         ShapeDrawing.processData5(barStart, rowY - 1.25F, barWidth * progress, 2.5F, 0.0F, UiTheme.computeInt(0xFFB08CFF, alpha), alpha, CornerRadii.uniform(1.0F));
      }

      // Клики по кнопкам
      boolean leftMouse = GLFW.glfwGetMouseButton(minecraftClient.getWindow().getHandle(), 0) == 1;
      if (leftMouse && !leftMouseFlag && alpha > 0.6F) {
         if (mouseY >= rowY - 9.0F && mouseY <= rowY + 9.0F) {
            if (mouseX >= prevX - prevSize / 2.0F && mouseX <= prevX + prevSize / 2.0F) {
               MusicWatcher.sendCommand("PREV");
            } else if (mouseX >= playX - playSize / 2.0F && mouseX <= playX + playSize / 2.0F) {
               MusicWatcher.sendCommand(MusicWatcher.isPlaying() ? "PAUSE" : "PLAY");
            } else if (mouseX >= nextX - nextSize / 2.0F && mouseX <= nextX + nextSize / 2.0F) {
               MusicWatcher.sendCommand("NEXT");
            }
         }
      }

      leftMouseFlag = leftMouse;
   }

   private static String formatTime(float seconds) {
      int total = Math.max(0, Math.round(seconds));
      return total / 60 + ":" + String.format(java.util.Locale.US, "%02d", total % 60);
   }

   private static String trimToWidth(String text, float maxWidth, float fontSize) {
      if (text == null || text.isEmpty()) {
         return "";
      }

      if (FontText.computeFloat(FontFace.SEMI, text, fontSize) <= maxWidth) {
         return text;
      }

      for (int index = text.length(); index > 3; index--) {
         String candidate = text.substring(0, index) + "…";
         if (FontText.computeFloat(FontFace.SEMI, candidate, fontSize) <= maxWidth) {
            return candidate;
         }
      }

      return text.substring(0, 3) + "…";
   }

   public static float computeFloat2() {
      return floatVal;
   }

   private static WatermarkHud.SubContext getNested0435(float local3934_0, float local3934_1) {
      if (!(local3934_1 < floatVal3 + 3.5F) && !(local3934_1 > floatVal3 + 15.0F - 3.5F)) {
         for (WatermarkHud.SubContext local3934_3 : list) {
            float local3934_4 = local3934_3.computeFloat3();
            float local3934_5 = Math.min(local3934_4 * 0.25F, 3.0F);
            if (local3934_4 >= 1.0F
               && local3934_0 >= local3934_3.animator5.floatValue() + local3934_5
               && local3934_0 <= local3934_3.animator5.floatValue() + local3934_4 - local3934_5) {
               return local3934_3;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static void updateState() {
      if (nested0435 != null) {
         nested0435 = null;
         StringBuilder stringBuilder = new StringBuilder();

         for (WatermarkHud.SubContext local3936_2 : list) {
            stringBuilder.append(stringBuilder.isEmpty() ? "" : ",").append(local3936_2.strVal);
         }

         InterfaceModule.getInstance().textSetting.setValue(stringBuilder.toString());
      }
   }

   private static float computeFloat(float local3937_0, float local3937_1, float local3937_2) {
      return local3937_2 > local3937_1
         ? ThemeColors.computeFloat((local3937_0 - local3937_1) / (local3937_2 - local3937_1))
         : ThemeColors.computeFloat((local3937_1 - local3937_0) / (local3937_1 - local3937_2));
   }

   private static void processData2(float local3938_0) {
      float local3938_1 = local3938_0 - floatVal5 + nested0435.computeFloat3() / 2.0F;
      list.remove(nested0435);
      float local3938_2 = floatVal2 + 4.0F;
      int local3938_3 = 0;

      for (int index = 0; index < list.size(); index++) {
         float local3938_5 = list.get(index).computeFloat3();
         if (!(local3938_5 < 1.0F)) {
            if (local3938_1 > local3938_2 + local3938_5 / 2.0F) {
               local3938_3 = index + 1;
            }

            local3938_2 += local3938_5 + 4.0F;
         }
      }

      list.add(local3938_3, nested0435);
   }

   private static final class SubContext {
      final String strVal;
      final String strVal2;
      boolean flag = true;
      final Animation animator4 = new Animation();
      final Animation animator5 = new Animation();
      final Animation animator6 = new Animation();
      final Animation animator7 = new Animation();
      boolean flag2;
      long timeMillis;
      String strVal3;
      String strVal4;
      float floatVal6;

      void processData3(String local3939_1, String local3939_2, float local3939_3) {
         this.strVal3 = local3939_1;
         this.strVal4 = local3939_2;
         this.floatVal6 = local3939_3;
      }

      SubContext(String local3940_1, String local3940_2) {
         this.strVal = local3940_1;
         this.strVal2 = local3940_2;
      }

      float computeFloat3() {
         return Math.round(this.animator4.floatValue());
      }

      void processData4(float local3942_1, float local3942_2, float local3942_3) {
         float local3942_4 = Math.round((this.animator5.floatValue() + this.computeFloat4()) * 2.0F) / 2.0F;
         float local3942_5 = this.animator7.floatValue();
         if (local3942_5 > 0.001F) {
            Render2D.processData51(
               local3942_4 - 3.0F,
               local3942_1 + 1.0F,
               this.computeFloat3() + 6.0F,
               13.0F,
               5.75F,
               UiTheme.computeInt(-1, local3942_5 * 0.12F * local3942_3)
            );
         }

         float local3942_6 = this.animator6.floatValue();
         int local3942_7 = UiTheme.computeInt(WatermarkHud.computeInt(ThemeColors.intVal9, local3942_6), local3942_3);
         int local3942_8 = UiTheme.computeInt(WatermarkHud.computeInt(HudStyle.computeInt4(1.0F), local3942_6), local3942_3);
         float local3942_9 = local3942_2 - this.floatVal6 / 2.0F;
         switch (this.strVal) {
            case "logo":
               Render2D.processData89(
                  "assets/horus/textures/gui/mainmenu/icons/logo.png",
                  local3942_4,
                  local3942_9,
                  this.floatVal6,
                  this.floatVal6,
                  0.0F,
                  local3942_7
               );
               break;
            case "avatar":
               Render2D.processData89(
                  "assets/horus/textures/images/avatar.png",
                  local3942_4,
                  local3942_9,
                  this.floatVal6,
                  this.floatVal6,
                  this.floatVal6 / 2.0F,
                  UiTheme.computeInt(-1, local3942_3)
               );
               break;
            case "user":
               HudIcons.USER.processData(local3942_4, local3942_9, this.floatVal6, local3942_7);
               break;
            case "fps":
               HudIcons.FPS.processData(local3942_4, local3942_9, this.floatVal6, local3942_7);
               break;
            case "ping":
               HudIcons.PING.processData(local3942_4, local3942_9, this.floatVal6, local3942_7);
               break;
            case "coords":
               HudIcons.COORDS.processData(local3942_4, local3942_9, this.floatVal6, local3942_7);
               break;
            default:
               HudIcons.TIME.processData(local3942_4, local3942_9, this.floatVal6, local3942_7);
         }

         if (this.strVal3 != null && !this.strVal3.isEmpty()) {
            float local3942_10 = Math.round((local3942_4 + this.floatVal6 + 2.5F) * 2.0F) / 2.0F;
            FontText.processData3(FontFace.SEMI, this.strVal3, local3942_10, local3942_2 - 3.5F, 6.0F, local3942_8);
            if (this.strVal4 != null && !this.strVal4.isEmpty()) {
               float local3942_11 = Math.round((local3942_10 + FontText.computeFloat(FontFace.SEMI, this.strVal3, 6.0F) + 2.0F) * 2.0F)
                  / 2.0F;
               FontText.processData3(FontFace.SEMI, this.strVal4, local3942_11, local3942_2 - 3.5F, 6.0F, local3942_8);
            }
         }
      }

      float computeFloat4() {
         if (this.timeMillis == 0L) {
            return 0.0F;
         } else {
            long local3943_1 = System.currentTimeMillis() - this.timeMillis;
            if (local3943_1 >= 360L) {
               this.timeMillis = 0L;
               return 0.0F;
            } else {
               return (float)Math.sin((float)local3943_1 / 1000.0F * 70.0F) * 2.5F * (1.0F - (float)local3943_1 / 360.0F);
            }
         }
      }

      float computeFloat5() {
         if (!this.flag) {
            return 0.0F;
         }

         if (this.strVal3 != null && !this.strVal3.isEmpty()) {
            float local3944_1 = this.floatVal6 + 2.5F + FontText.computeFloat(FontFace.SEMI, this.strVal3, 6.0F);
            if (this.strVal4 != null && !this.strVal4.isEmpty()) {
               local3944_1 += 2.0F + FontText.computeFloat(FontFace.SEMI, this.strVal4, 6.0F);
            }

            return local3944_1;
         } else {
            return this.floatVal6;
         }
      }
   }
}

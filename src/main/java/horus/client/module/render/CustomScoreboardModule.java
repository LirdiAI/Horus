package horus.client.module.render;

import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.module.misc.InfoReplaceModule;
import horus.client.module.misc.StreamerModModule;
import horus.client.module.misc.YTMODEModule;
import horus.client.render.Render2D;
import horus.client.render.SoftGlowDrawData;
import horus.client.setting.BooleanSetting;
import horus.client.setting.ColorSetting;
import horus.client.setting.LegacyNumberSetting;
import horus.client.ui.ClickGuiStyle;
import horus.client.util.Animation;
import horus.client.util.ClientChat;
import horus.client.util.Colors;
import horus.client.util.Easings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ModuleInfo(
   name = "ScoreBoard",
   description = "Шейдерный скорборд в стиле ClickGUI: стеклянный блюр, свечение, бейджи, скрытие чисел и перестановка строк.",
   category = ModuleCategory.RENDER
)
public final class CustomScoreboardModule extends Module {
   private static CustomScoreboardModule instance;

   public static CustomScoreboardModule getInstance() {
      if (instance == null) {
         instance = new CustomScoreboardModule();
      }
      return instance;
   }

   public final BooleanSetting blur = this.addSetting(
      new BooleanSetting("Размытие (Блюр)", "Шейдерное размытие игрового мира (Kawase Blur) за панелью скорборда.", true)
   );
   public final BooleanSetting glow = this.addSetting(
      new BooleanSetting("Неоновое свечение", "Мягкое объемное свечение (Soft Glow) в тон темы по контуру панели.", true)
   );
   public final BooleanSetting reorderRows = this.addSetting(
      new BooleanSetting("Кастомный порядок строк", "Позволяет менять строки местами с зажатым Shift при открытом чате.", true)
   );
   public final BooleanSetting resetRowOrder = this.addSetting(
      new BooleanSetting("Сбросить порядок строк", "Сбросить кастомный порядок строк на стандартный.", false)
   );
   public final BooleanSetting hideScores = this.addSetting(
      new BooleanSetting("Скрыть числа", "Скрывать красные порядковые числа справа (оставлять только чистый текст).", false)
   );
   public final BooleanSetting scoresBadge = this.addSetting(
      new BooleanSetting("Бейджи для чисел", "Отображать числа в виде стильных полупрозрачных акцентных мини-бейджей.", true)
   );
   public final BooleanSetting topAccentLine = this.addSetting(
      new BooleanSetting("Акцентная линия", "Тонкая неоновая полоска в тон темы на верхней грани панели.", true)
   );
   public final BooleanSetting centerTitle = this.addSetting(
      new BooleanSetting("Центрировать заголовок", "Выравнивать текст заголовка по центру панели.", true)
   );
   public final BooleanSetting rowStriping = this.addSetting(
      new BooleanSetting("Чередование строк", "Легкое чередование фона строк для комфортной читаемости информации.", true)
   );
   public final LegacyNumberSetting roundedRadius = this.addSetting(
      new LegacyNumberSetting("Скругление углов", "Радиус скругления стеклянной карточки скорборда.", 7.0, 0.0, 16.0, 1.0)
   );
   public final BooleanSetting customColor = this.addSetting(
      new BooleanSetting("Кастомный цвет", "Использовать индивидуальный цвет свечения вместо цвета темы ClickGUI.", false)
   );
   public final ColorSetting color = this.addSetting(
      new ColorSetting("Цвет акцента", "Пользовательский цвет подсветки скорборда.", new Color(255, 255, 255, 255))
   );
   public final LegacyNumberSetting maxLines = this.addSetting(
      new LegacyNumberSetting("Количество строк", "Максимальное число отображаемых строк скорборда (при уменьшении лишние строки снизу скрываются).", 15.0, 1.0, 15.0, 1.0)
   );
   public final LegacyNumberSetting minWidth = this.addSetting(
      new LegacyNumberSetting("Минимальная ширина", "Минимальная ширина карточки скорборда.", 90.0, 60.0, 300.0, 5.0)
   );
   public final LegacyNumberSetting xOffset = this.addSetting(
      new LegacyNumberSetting("Смещение X", "Смещение панели скорборда по горизонтали.", 0.0, -800.0, 800.0, 1.0)
   );
   public final LegacyNumberSetting yOffset = this.addSetting(
      new LegacyNumberSetting("Смещение Y", "Смещение панели скорборда по вертикали.", 0.0, -800.0, 800.0, 1.0)
   );

   // Animation & Dragging state
   private final Animation fadeAnimation = new Animation();
   private float lastCardX = 0.0F;
   private float lastCardY = 0.0F;
   private float lastCardW = 0.0F;
   private float lastCardH = 0.0F;
   private boolean dragging = false;
   private float dragStartX = 0.0F;
   private float dragStartY = 0.0F;
   private float initialXOffset = 0.0F;
   private float initialYOffset = 0.0F;

   // Resizing state
   private boolean resizing = false;
   private boolean resizingWidth = false;
   private float resizeStartX = 0.0F;
   private float resizeStartY = 0.0F;
   private int initialMaxLines = 15;
   private float initialMinWidth = 90.0F;

   // Row Reordering state
   private final Map<String, List<String>> objectiveCustomOrders = new HashMap<>();
   private int draggedRowIndex = -1;
   private int lastRowCount = 0;
   private String currentObjectiveName = "";

   private static final Comparator<ScoreboardEntry> ENTRY_COMPARATOR =
      Comparator.comparing(ScoreboardEntry::value).reversed()
         .thenComparing(ScoreboardEntry::owner, String.CASE_INSENSITIVE_ORDER);

   private CustomScoreboardModule() {
      this.setEnabled(true);
      this.fadeAnimation.resetTo(0.0);
   }

   public static boolean isShiftDown() {
      if (mc == null || mc.getWindow() == null) return false;
      return InputUtil.isKeyPressed(mc.getWindow(), 340) || InputUtil.isKeyPressed(mc.getWindow(), 344);
   }

   public int getAccentColor() {
      if (this.customColor.isChecked()) {
         return 0xFF000000 | (this.color.getValue() & 0x00FFFFFF);
      }
      return 0xFF000000 | (ClickGuiStyle.intVal & 0x00FFFFFF);
   }

   private float sf() {
      if (mc == null || mc.getWindow() == null) return 1.0F;
      return (float) mc.getWindow().getScaleFactor() / 2.0F;
   }

   // --- Render2D Shader Drawing Helpers (Matching ClickGUI style) ---

   private void drawClickGuiPanel(float x, float y, float w, float h, float radius, int bodyColor, int outlineColor, float stroke) {
      float s = sf();
      if (this.blur.isChecked()) {
         Render2D.processData60(x * s + 0.5F * s, y * s + 0.5F * s, w * s - 1.0F * s, h * s - 1.0F * s, radius * s, 30.0F, 1.0F, -1);
      }
      Render2D.processData51(x * s, y * s, w * s, h * s, radius * s, bodyColor);
      if (outlineColor != 0 && stroke > 0.0F) {
         Render2D.processData86(x * s, y * s, w * s, h * s, radius * s, stroke * s, outlineColor);
      }
   }

   private void drawClickGuiGlow(float x, float y, float w, float h, float radius, int glowColor, float glowRadius, float intensity) {
      if (!this.glow.isChecked() || intensity <= 0.001F) return;
      float s = sf();
      Render2D.processData58(
         new SoftGlowDrawData(x * s, y * s, w * s, h * s, radius * s, glowColor, glowRadius * s).withIntensity(intensity)
      );
   }

   private void drawClickGuiRect(float x, float y, float w, float h, float radius, int color) {
      float s = sf();
      Render2D.processData51(x * s, y * s, w * s, h * s, radius * s, color);
   }

   private void drawClickGuiOutline(float x, float y, float w, float h, float radius, float stroke, int color) {
      float s = sf();
      Render2D.processData86(x * s, y * s, w * s, h * s, radius * s, stroke * s, color);
   }

   private boolean beginShaders(DrawContext context) {
      if (!Render2D.isRendering()) {
         Render2D.renderElement5(context);
         return true;
      }
      return false;
   }

   private void flushShaders(boolean began) {
      if (began && Render2D.isRendering()) {
         Render2D.updateState6();
      }
   }

   public static final class ProcessedRow {
      public final MutableText name;
      public final MutableText score;
      public final int nameWidth;
      public final int scoreWidth;
      public final String owner;

      public ProcessedRow(MutableText name, MutableText score, int nameWidth, int scoreWidth, String owner) {
         this.name = name;
         this.score = score;
         this.nameWidth = nameWidth;
         this.scoreWidth = scoreWidth;
         this.owner = owner;
      }
   }

   // --- Main Scoreboard Render Logic ---

   public void renderScoreboard(DrawContext drawContext, ScoreboardObjective objective) {
      if (!this.isEnabled() || mc.world == null || mc.player == null) {
         return;
      }

      if (this.dragging || this.resizing || this.draggedRowIndex >= 0) {
         if (mc.getWindow() != null) {
            boolean isLeftDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(
               mc.getWindow().getHandle(),
               org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT
            ) == org.lwjgl.glfw.GLFW.GLFW_PRESS;

            if (!isLeftDown || mc.currentScreen == null) {
               resetDragState();
            }
         } else {
            resetDragState();
         }
      }

      String objName = objective.getName();
      this.currentObjectiveName = objName;

      if (this.resetRowOrder.isChecked()) {
         this.objectiveCustomOrders.remove(objName);
         this.resetRowOrder.setValue(false);
      }

      Text titleText = objective.getDisplayName();
      if (StreamerModModule.getInstance().isEnabled()) {
         titleText = StreamerModModule.getInstance().getText(titleText);
      }

      Scoreboard scoreboard = objective.getScoreboard();
      NumberFormat numberFormat = objective.getNumberFormatOr(StyledNumberFormat.RED);
      Collection<ScoreboardEntry> allEntries = scoreboard.getScoreboardEntries(objective);

      List<ScoreboardEntry> filtered = allEntries.stream()
         .filter(entry -> !entry.hidden())
         .filter(entry -> !YTMODEModule.getInstance().isEnabled() || !YTMODEModule.getInstance().matchesCriteria(entry))
         .filter(entry -> !StreamerModModule.getInstance().isEnabled() || !StreamerModModule.getInstance().matchesCriteria(entry))
         .sorted(ENTRY_COMPARATOR)
         .limit(15)
         .toList();

      if (filtered.isEmpty()) {
         this.fadeAnimation.update();
         this.fadeAnimation.animateTo(0.0, 0.25, Easings.EASE_OUT_QUART);
         return;
      }

      TextRenderer tr = mc.textRenderer;
      List<ProcessedRow> naturalRows = new ArrayList<>(filtered.size());

      for (ScoreboardEntry entry : filtered) {
         Team team = scoreboard.getScoreHolderTeam(entry.owner());
         MutableText name = Team.decorateName(team, entry.name());
         if (InfoReplaceModule.getInstance().isEnabled()) {
            name = InfoReplaceModule.getInstance().getMutableText(name);
         }
         MutableText score = entry.formatted(numberFormat);
         int nw = tr.getWidth(name);
         int sw = tr.getWidth(score);
         naturalRows.add(new ProcessedRow(name, score, nw, sw, entry.owner()));
      }

      // Применяем кастомный порядок строк при включенной настройке
      List<ProcessedRow> orderedRows;
      if (this.reorderRows.isChecked()) {
         List<String> order = this.objectiveCustomOrders.get(objName);
         if (order == null || order.isEmpty()) {
            order = new ArrayList<>();
            for (ProcessedRow r : naturalRows) {
               order.add(r.owner);
            }
            this.objectiveCustomOrders.put(objName, order);
            orderedRows = naturalRows;
         } else {
            List<ProcessedRow> sortedRows = new ArrayList<>(naturalRows.size());
            Map<String, ProcessedRow> mapByOwner = new LinkedHashMap<>();
            for (ProcessedRow r : naturalRows) {
               mapByOwner.put(r.owner, r);
            }
            for (String key : order) {
               ProcessedRow r = mapByOwner.remove(key);
               if (r != null) {
                  sortedRows.add(r);
               }
            }
            for (ProcessedRow remaining : mapByOwner.values()) {
               sortedRows.add(remaining);
               order.add(remaining.owner);
            }
            order.removeIf(k -> naturalRows.stream().noneMatch(r -> r.owner.equals(k)));
            orderedRows = sortedRows;
         }
      } else {
         orderedRows = naturalRows;
      }

      // Лимит видимых строк: при уменьшении размера нижние строки исчезают
      int totalAvailableRows = orderedRows.size();
      int allowedLines = (int) Math.round(this.maxLines.getValue());
      int visibleCount = Math.max(1, Math.min(totalAvailableRows, allowedLines));

      List<ProcessedRow> rows = new ArrayList<>(orderedRows.subList(0, visibleCount));
      this.lastRowCount = rows.size();
      if (this.draggedRowIndex >= rows.size()) {
         this.draggedRowIndex = -1;
      }

      // Вычисляем ширину содержимого только по отображаемым строкам
      int maxNameWidth = 0;
      int maxScoreWidth = 0;
      for (ProcessedRow r : rows) {
         maxNameWidth = Math.max(maxNameWidth, r.nameWidth);
         maxScoreWidth = Math.max(maxScoreWidth, r.scoreWidth);
      }

      int titleWidth = tr.getWidth(titleText);
      boolean hideNums = this.hideScores.isChecked();
      boolean badges = this.scoresBadge.isChecked();

      int contentWidth = maxNameWidth;
      if (!hideNums && maxScoreWidth > 0) {
         contentWidth += (badges ? 14 : 8) + maxScoreWidth;
      }

      int padX = 8;
      int headerHeight = 16;
      int dividerHeight = 3;
      int rowHeight = 11;
      int padBottom = 5;

      int cardWidth = Math.max(contentWidth, titleWidth + (this.centerTitle.isChecked() ? 16 : 26)) + padX * 2;
      cardWidth = Math.max(cardWidth, (int) Math.round(this.minWidth.getValue()));

      int cardHeight = headerHeight + dividerHeight + rows.size() * rowHeight + padBottom;

      int screenW = drawContext.getScaledWindowWidth();
      int screenH = drawContext.getScaledWindowHeight();

      float targetBaseX = screenW - cardWidth - 8.0F + (float) this.xOffset.getValue();
      float targetBaseY = (screenH - cardHeight) / 2.0F + (float) this.yOffset.getValue();

      targetBaseX = Math.clamp(targetBaseX, 2.0F, Math.max(2.0F, screenW - cardWidth - 2.0F));
      targetBaseY = Math.clamp(targetBaseY, 2.0F, Math.max(2.0F, screenH - cardHeight - 2.0F));

      this.fadeAnimation.update();
      this.fadeAnimation.animateTo(1.0, 0.35, Easings.EASE_OUT_QUART);
      float alpha = this.fadeAnimation.floatValue();
      if (alpha <= 0.001F) return;

      float slideOffset = (1.0F - alpha) * 18.0F;
      float cardX = targetBaseX + slideOffset;
      float cardY = targetBaseY;

      this.lastCardX = cardX;
      this.lastCardY = cardY;
      this.lastCardW = cardWidth;
      this.lastCardH = cardHeight;

      // Mouse position for hover & dragging visual feedback
      double curMouseX = -1.0;
      double curMouseY = -1.0;
      if (mc.currentScreen != null && mc.getWindow() != null && mc.getWindow().getWidth() > 0 && mc.getWindow().getHeight() > 0) {
         curMouseX = mc.mouse.getX() * (double) screenW / (double) mc.getWindow().getWidth();
         curMouseY = mc.mouse.getY() * (double) screenH / (double) mc.getWindow().getHeight();
      }
      boolean inScreen = mc.currentScreen != null;
      boolean shiftActive = isShiftDown() && inScreen;
      boolean overResize = inScreen && isOverResizeHandle(curMouseX, curMouseY);
      int hoveredRow = shiftActive ? getRowIndexAt(curMouseX, curMouseY, rows.size(), false) : -1;

      int accent = getAccentColor();
      int bodyColor = Colors.computeInt7(0x091020, (int) (195 * alpha));
      int outlineColor = Colors.computeInt7(accent, (int) (125 * alpha));
      float radius = (float) this.roundedRadius.getValue();

      // Begin shaders batch
      boolean began = beginShaders(drawContext);

      // 1. Soft Glow
      if (this.glow.isChecked()) {
         drawClickGuiGlow(cardX, cardY, cardWidth, cardHeight, radius, accent, 18.0F, 0.55F * alpha);
      }

      // 2. Glass Body + Kawase Blur + Razor Outline
      drawClickGuiPanel(cardX, cardY, cardWidth, cardHeight, radius, bodyColor, outlineColor, 1.0F);

      // 3. Top Accent Line
      if (this.topAccentLine.isChecked()) {
         float topLineWidth = Math.max(24.0F, cardWidth * 0.45F);
         float topLineX = cardX + (cardWidth - topLineWidth) / 2.0F;
         int topBarColor = Colors.computeInt7(accent, (int) (240 * alpha));
         drawClickGuiRect(topLineX, cardY + 0.5F, topLineWidth, 1.5F, 0.75F, topBarColor);
         if (this.glow.isChecked()) {
            drawClickGuiGlow(topLineX, cardY + 0.5F, topLineWidth, 1.5F, 1.0F, accent, 6.0F, 0.4F * alpha);
         }
      }

      // 4. Subtle Title Header Bar background
      int headerBg = Colors.computeInt7(accent, (int) (22 * alpha));
      drawClickGuiRect(cardX + 2.0F, cardY + 2.0F, cardWidth - 4.0F, headerHeight - 1.0F, Math.max(2.0F, radius - 2.0F), headerBg);

      // 5. Hairline divider between header and rows
      float divY = cardY + headerHeight + 1.0F;
      int divColor = Colors.computeInt7(0xFFFFFF, (int) (25 * alpha));
      drawClickGuiRect(cardX + padX, divY, cardWidth - padX * 2, 0.75F, 0.35F, divColor);

      // 6. Optional zebra row striping or badge fills & row hover/drag highlights
      float currentY = divY + dividerHeight;
      for (int i = 0; i < rows.size(); i++) {
         ProcessedRow row = rows.get(i);
         float rowY = currentY + i * rowHeight;
         boolean isBeingDragged = (i == this.draggedRowIndex);
         boolean isHovered = (i == hoveredRow && this.draggedRowIndex == -1);

         if (isBeingDragged) {
            // Dragged row highlight: bright accent tint + crisp border
            int dragBg = Colors.computeInt7(accent, (int) (65 * alpha));
            int dragBorder = Colors.computeInt7(accent, (int) (220 * alpha));
            drawClickGuiRect(cardX + 3.0F, rowY, cardWidth - 6.0F, rowHeight, 3.0F, dragBg);
            drawClickGuiOutline(cardX + 3.0F, rowY, cardWidth - 6.0F, rowHeight, 3.0F, 0.75F, dragBorder);
         } else if (isHovered) {
            // Subtle hover pill when Shift is held
            int hoverBg = Colors.computeInt7(accent, (int) (30 * alpha));
            int hoverBorder = Colors.computeInt7(accent, (int) (120 * alpha));
            drawClickGuiRect(cardX + 3.0F, rowY, cardWidth - 6.0F, rowHeight, 3.0F, hoverBg);
            drawClickGuiOutline(cardX + 3.0F, rowY, cardWidth - 6.0F, rowHeight, 3.0F, 0.5F, hoverBorder);
         } else if (this.rowStriping.isChecked() && (i % 2 == 1)) {
            int stripeColor = Colors.computeInt7(0xFFFFFF, (int) (8 * alpha));
            drawClickGuiRect(cardX + 3.0F, rowY, cardWidth - 6.0F, rowHeight, 3.0F, stripeColor);
         }

         if (!hideNums && badges && row.scoreWidth > 0) {
            float badgeW = row.scoreWidth + 6.0F;
            float badgeH = 9.0F;
            float badgeX = cardX + cardWidth - padX - badgeW;
            float badgeY = rowY + (rowHeight - badgeH) / 2.0F;
            int badgeBg = isBeingDragged ? Colors.computeInt7(accent, (int) (60 * alpha)) : Colors.computeInt7(accent, (int) (32 * alpha));
            int badgeBorder = isBeingDragged ? Colors.computeInt7(accent, (int) (180 * alpha)) : Colors.computeInt7(accent, (int) (110 * alpha));
            drawClickGuiRect(badgeX, badgeY, badgeW, badgeH, 2.5F, badgeBg);
            drawClickGuiOutline(badgeX, badgeY, badgeW, badgeH, 2.5F, 0.75F, badgeBorder);
         }
      }

      // 7. Interactive Bottom Resize Grip & Bar (visible when a screen like Chat is open)
      if (inScreen) {
         float handleW = Math.min(36.0F, cardWidth - 16.0F);
         float handleX = cardX + (cardWidth - handleW) / 2.0F;
         float handleY = cardY + cardHeight - 2.0F;
         int handleColor = (overResize || this.resizing)
            ? Colors.computeInt7(accent, (int) (220 * alpha))
            : Colors.computeInt7(accent, (int) (80 * alpha));
         drawClickGuiRect(handleX, handleY, handleW, 1.5F, 0.75F, handleColor);

         // Corner dots grip
         float cornerX = cardX + cardWidth - 10.0F;
         float cornerY = cardY + cardHeight - 10.0F;
         int gripColor = (overResize || this.resizing)
            ? Colors.computeInt7(accent, (int) (240 * alpha))
            : Colors.computeInt7(0xFFFFFF, (int) (80 * alpha));
         drawClickGuiRect(cornerX + 4.0F, cornerY + 4.0F, 2.0F, 2.0F, 0.5F, gripColor);
         drawClickGuiRect(cornerX + 1.0F, cornerY + 4.0F, 2.0F, 2.0F, 0.5F, gripColor);
         drawClickGuiRect(cornerX + 4.0F, cornerY + 1.0F, 2.0F, 2.0F, 0.5F, gripColor);
      }

      flushShaders(began);

      // --- Text Rendering ---
      int titleColor = Colors.computeInt7(0xFFFFFF, (int) (255 * alpha));
      float titleY = cardY + 2.0F + (headerHeight - 1.0F - 9.0F) / 2.0F;
      if (this.centerTitle.isChecked()) {
         float titleX = cardX + (cardWidth - titleWidth) / 2.0F;
         drawContext.drawText(tr, titleText, (int) titleX, (int) titleY, titleColor, true);
      } else {
         float dotX = cardX + padX + 1.0F;
         float dotY = titleY + 3.5F;
         int dotColor = Colors.computeInt7(accent, (int) (240 * alpha));
         drawContext.fill((int) dotX, (int) dotY, (int) dotX + 3, (int) dotY + 3, dotColor);
         float titleX = cardX + padX + 8.0F;
         drawContext.drawText(tr, titleText, (int) titleX, (int) titleY, titleColor, true);
      }

      for (int i = 0; i < rows.size(); i++) {
         ProcessedRow row = rows.get(i);
         float rowY = currentY + i * rowHeight;
         float textY = rowY + (rowHeight - 9.0F) / 2.0F;
         float textX = cardX + padX;
         boolean isBeingDragged = (i == this.draggedRowIndex);
         boolean isHovered = (i == hoveredRow && this.draggedRowIndex == -1);

         // Drag handle indicator when Shift is active
         if (shiftActive && (isBeingDragged || isHovered)) {
            int handleColor = isBeingDragged ? 0xFFFFFFFF : Colors.computeInt7(accent, (int)(220 * alpha));
            drawContext.drawText(tr, "≡", (int) textX, (int) textY, handleColor, true);
            textX += 8.0F;
         }

         int rowTextColor = isBeingDragged ? 0xFFFFFFFF : titleColor;
         drawContext.drawText(tr, row.name, (int) textX, (int) textY, rowTextColor, true);

         if (!hideNums && row.scoreWidth > 0) {
            if (badges) {
               float badgeW = row.scoreWidth + 6.0F;
               float scoreX = cardX + cardWidth - padX - badgeW + 3.0F;
               int badgeTextColor = isBeingDragged ? 0xFFFFFFFF : Colors.computeInt7(0xFFFFFF, (int) (235 * alpha));
               drawContext.drawText(tr, row.score, (int) scoreX, (int) textY, badgeTextColor, false);
            } else {
               float scoreX = cardX + cardWidth - padX - row.scoreWidth;
               int scoreColor = isBeingDragged ? 0xFFFF7777 : Colors.computeInt7(0xFF5555, (int) (255 * alpha));
               drawContext.drawText(tr, row.score, (int) scoreX, (int) textY, scoreColor, true);
            }
         }
      }

      // Interactive hint pill below card
      String hint = null;
      if (inScreen) {
         if (this.resizing) {
            hint = "Размер: " + rows.size() + " " + getLinesWord(rows.size()) + " (из " + totalAvailableRows + ")";
         } else if (shiftActive && isMouseOver(curMouseX, curMouseY)) {
            hint = (this.draggedRowIndex >= 0) ? "Перемещение строки..." : "Shift + ЛКМ: Поменять строки | ПКМ: Сброс";
         } else if (overResize) {
            hint = "Потяните вниз/вверх для размера | ПКМ: Сброс";
         }
      }

      if (hint != null) {
         int hintW = tr.getWidth(hint) + 12;
         float hintH = 13.0F;
         float hintX = cardX + (cardWidth - hintW) / 2.0F;
         float hintY = cardY + cardHeight + 4.0F;

         boolean hintBegan = beginShaders(drawContext);
         try {
            int hintBg = Colors.computeInt7(0x091020, (int) (215 * alpha));
            int hintBorder = Colors.computeInt7(accent, (int) (160 * alpha));
            drawClickGuiPanel(hintX, hintY, hintW, hintH, 4.0F, hintBg, hintBorder, 0.75F);
         } finally {
            flushShaders(hintBegan);
         }

         int hintTextColor = Colors.computeInt7(0xFFFFFF, (int) (240 * alpha));
         drawContext.drawText(tr, hint, (int) (hintX + 6.0F), (int) (hintY + 2.5F), hintTextColor, true);
      }

      // If dragging or resizing while chat/screen is open, draw subtle accent border
      if ((this.dragging || this.resizing) && inScreen) {
         drawContext.fill((int) cardX, (int) cardY, (int) (cardX + cardWidth), (int) cardY + 1, accent);
         drawContext.fill((int) cardX, (int) (cardY + cardHeight - 1), (int) (cardX + cardWidth), (int) cardY + cardHeight, accent);
         drawContext.fill((int) cardX, (int) cardY, (int) cardX + 1, (int) (cardY + cardHeight), accent);
         drawContext.fill((int) (cardX + cardWidth - 1), (int) cardY, (int) (cardX + cardWidth), (int) (cardY + cardHeight), accent);
      }
   }

   // --- Mouse Interaction / Draggable in Chat Screen ---

   public boolean isMouseOver(double mouseX, double mouseY) {
      return mouseX >= this.lastCardX && mouseX <= this.lastCardX + this.lastCardW
         && mouseY >= this.lastCardY && mouseY <= this.lastCardY + this.lastCardH;
   }

   public boolean isOverResizeHandle(double mouseX, double mouseY) {
      if (this.lastCardW <= 0.0F || this.lastCardH <= 0.0F) return false;
      return mouseX >= this.lastCardX - 2.0F && mouseX <= this.lastCardX + this.lastCardW + 4.0F
         && mouseY >= this.lastCardY + this.lastCardH - 7.0F && mouseY <= this.lastCardY + this.lastCardH + 6.0F;
   }

   public int getRowIndexAt(double mouseX, double mouseY, int totalRows, boolean clamp) {
      if (totalRows <= 0) return -1;
      if (!clamp && (mouseX < this.lastCardX - 20.0F || mouseX > this.lastCardX + this.lastCardW + 20.0F)) {
         return -1;
      }
      float startY = this.lastCardY + 20.0F;
      float rowH = 11.0F;
      float relY = (float) mouseY - startY;
      if (relY < 0.0F) return clamp ? 0 : -1;
      int idx = (int) (relY / rowH);
      if (idx >= totalRows) return clamp ? totalRows - 1 : -1;
      return idx;
   }

   public boolean onMouseClicked(double mouseX, double mouseY, int button) {
      if (!this.isEnabled()) return false;

      boolean overResize = isOverResizeHandle(mouseX, mouseY);
      boolean overCard = isMouseOver(mouseX, mouseY) || overResize;
      if (!overCard) return false;

      boolean shift = isShiftDown();

      // Shift + Right Click: сбросить кастомный порядок строк
      if (shift && button == 1) {
         if (this.currentObjectiveName != null && !this.currentObjectiveName.isEmpty()) {
            this.objectiveCustomOrders.remove(this.currentObjectiveName);
            ClientChat.processData8("Порядок строк скорборда &7сброшен&r.");
            return true;
         }
      }

      // Shift + Left Click: перетаскивание отдельной строки
      if (shift && button == 0 && this.reorderRows.isChecked()) {
         int rowIdx = getRowIndexAt(mouseX, mouseY, this.lastRowCount, false);
         if (rowIdx >= 0) {
            this.draggedRowIndex = rowIdx;
            this.dragging = false;
            this.resizing = false;
            return true;
         }
      }

      // Без Shift: клик по нижней грани или правому нижнему углу (Resize)
      if (!shift && overResize) {
         if (button == 0) {
            this.resizing = true;
            this.dragging = false;
            this.draggedRowIndex = -1;
            this.resizeStartX = (float) mouseX;
            this.resizeStartY = (float) mouseY;
            this.initialMaxLines = (int) Math.round(this.maxLines.getValue());
            this.initialMinWidth = (float) this.minWidth.getValue();
            this.resizingWidth = (mouseX >= this.lastCardX + this.lastCardW - 14.0F);
            return true;
         } else if (button == 1) {
            this.maxLines.setValue(15.0F);
            this.minWidth.setValue(90.0F);
            ClientChat.processData8("Размер скорборда &7сброшен (все строки, ширина 90)&r.");
            return true;
         }
      }

      // Без Shift: перемещение всей панели скорборда (ЛКМ)
      if (!shift && button == 0) {
         this.dragging = true;
         this.resizing = false;
         this.draggedRowIndex = -1;
         this.dragStartX = (float) mouseX;
         this.dragStartY = (float) mouseY;
         this.initialXOffset = (float) this.xOffset.getValue();
         this.initialYOffset = (float) this.yOffset.getValue();
         return true;
      }

      // Без Shift: сброс позиции карточки по умолчанию (ПКМ)
      if (!shift && button == 1) {
         this.xOffset.setValue(0.0F);
         this.yOffset.setValue(0.0F);
         return true;
      }

      return false;
   }

   public void resetDragState() {
      this.dragging = false;
      this.resizing = false;
      this.draggedRowIndex = -1;
   }

   public void onMouseReleased(int button) {
      resetDragState();
   }

   public void updateMouseDrag(double mouseX, double mouseY) {
      if (!this.dragging && !this.resizing && this.draggedRowIndex < 0) {
         return;
      }

      if (mc.getWindow() != null) {
         boolean isLeftDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(
            mc.getWindow().getHandle(),
            org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT
         ) == org.lwjgl.glfw.GLFW.GLFW_PRESS;

         if (!isLeftDown || mc.currentScreen == null) {
            resetDragState();
            return;
         }
      } else {
         resetDragState();
         return;
      }

      // 1. Изменение размера скорборда (скрытие/появление строк)
      if (this.resizing) {
         float dy = (float) mouseY - this.resizeStartY;
         int lineDelta = Math.round(dy / 11.0F);
         int targetLines = Math.clamp(this.initialMaxLines + lineDelta, 1, 15);
         this.maxLines.setValue((float) targetLines);

         if (this.resizingWidth) {
            float dx = (float) mouseX - this.resizeStartX;
            float targetWidth = Math.clamp(this.initialMinWidth + dx, 60.0F, 300.0F);
            float snapped = Math.round(targetWidth / 5.0F) * 5.0F;
            this.minWidth.setValue(snapped);
         }
         return;
      }

      // 2. Перемещение строк скорборда с зажатым Shift
      if (this.draggedRowIndex >= 0 && this.reorderRows.isChecked()) {
         int targetIndex = getRowIndexAt(mouseX, mouseY, this.lastRowCount, true);
         if (targetIndex >= 0 && targetIndex != this.draggedRowIndex) {
            List<String> order = this.objectiveCustomOrders.get(this.currentObjectiveName);
            if (order != null && this.draggedRowIndex < order.size() && targetIndex < order.size()) {
               String item = order.remove(this.draggedRowIndex);
               order.add(targetIndex, item);
               this.draggedRowIndex = targetIndex;
            }
         }
         return;
      }

      // 3. Перемещение всей панели скорборда
      if (this.dragging) {
         float dx = (float) mouseX - this.dragStartX;
         float dy = (float) mouseY - this.dragStartY;
         this.xOffset.setValue((float) Math.clamp(this.initialXOffset + dx, -800.0, 800.0));
         this.yOffset.setValue((float) Math.clamp(this.initialYOffset + dy, -800.0, 800.0));
      }
   }

   private static String getLinesWord(int count) {
      int rem100 = count % 100;
      int rem10 = count % 10;
      if (rem100 >= 11 && rem100 <= 19) return "строк";
      if (rem10 == 1) return "строка";
      if (rem10 >= 2 && rem10 <= 4) return "строки";
      return "строк";
   }
}

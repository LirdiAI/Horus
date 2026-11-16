package horus.client.ui;

import horus.client.invmanager.InvConfigManager;
import horus.client.invmanager.InvLayoutConfig;
import horus.client.module.player.InvManagerModule;
import horus.client.render.FontFace;
import horus.client.render.FontText;
import horus.client.render.Render2D;
import horus.client.util.GuiScale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class InvManagerScreen extends Screen {
   private InvLayoutConfig activeLayout;
   private String configNameInput = "";
   private boolean isInputFocused = false;
   private int selectedSlot = -1;
   private String statusMessage = "Выберите слот или действие";

   private final List<ClickArea> clickAreas = new ArrayList<>();
   private final List<SlotBox> slotBoxes = new ArrayList<>();

   private record ClickArea(float x, float y, float w, float h, Runnable action) {}
   private record SlotBox(int slotIndex, float x, float y, float w, float h, String label) {}

   public InvManagerScreen() {
      super(Text.literal("Horus InvManager"));
      this.activeLayout = InvConfigManager.getInstance().getActiveConfig().copy();
      this.configNameInput = this.activeLayout.getName();
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
      if (this.client != null) {
         this.client.setScreen(null);
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
      this.slotBoxes.clear();

      Render2D.renderElement5(drawContext);

      // Dark semi-transparent background overlay
      Render2D.processData51(0.0F, 0.0F, screenW, screenH, 0.0F, 0xCC090B10);

      // Modal container dimensions
      float cardW = Math.min(screenW - 20.0F, 690.0F);
      float cardH = Math.min(screenH - 20.0F, 430.0F);
      float cardX = (screenW - cardW) / 2.0F;
      float cardY = (screenH - cardH) / 2.0F;

      // Card background & modern purple-tinted subtle border
      Render2D.processData51(cardX, cardY, cardW, cardH, 14.0F, 0xF20F121A);
      Render2D.processData86(cardX, cardY, cardW, cardH, 14.0F, 1.0F, 0x2AFFFFFF);

      // Header: Breadcrumb & Title
      FontText.processData3(FontFace.SEMI, "Horus Client  /  Раскладка инвентаря", cardX + 22.0F, cardY + 16.0F, 6.0F, 0x889CA3AF);
      FontText.processData3(FontFace.BOLD, "Менеджер инвентаря (InvManager)", cardX + 22.0F, cardY + 28.0F, 13.0F, 0xFFFFFFFF);

      // Header: Active Preset Pill Badge
      String badgeText = "Активен: " + this.activeLayout.getName();
      float badgeW = FontText.computeFloat(FontFace.SEMI, badgeText, 6.5F) + 16.0F;
      float badgeX = cardX + cardW - badgeW - 48.0F;
      float badgeY = cardY + 18.0F;
      Render2D.processData51(badgeX, badgeY, badgeW, 20.0F, 6.0F, 0x268B5CF6);
      Render2D.processData86(badgeX, badgeY, badgeW, 20.0F, 6.0F, 1.0F, 0xFF8B5CF6);
      FontText.processData3(FontFace.SEMI, badgeText, badgeX + 8.0F, badgeY + 5.0F, 6.5F, 0xFFC4B5FD);

      // Header: Close Cross Button [ × ]
      float closeX = cardX + cardW - 34.0F;
      float closeY = cardY + 17.0F;
      boolean closeHov = mx >= closeX && mx <= closeX + 22.0F && my >= closeY && my <= closeY + 22.0F;
      Render2D.processData51(closeX, closeY, 22.0F, 22.0F, 6.0F, closeHov ? 0x33EF4444 : 0x1AFFFFFF);
      Render2D.processData86(closeX, closeY, 22.0F, 22.0F, 6.0F, 0.5F, closeHov ? 0xFFEF4444 : 0x22FFFFFF);
      FontText.processData3(FontFace.BOLD, "✕", closeX + 7.0F, closeY + 5.0F, 8.5F, closeHov ? 0xFFEF4444 : 0x88FFFFFF);
      this.clickAreas.add(new ClickArea(closeX, closeY, 22.0F, 22.0F, this::close));

      // Divider line
      Render2D.processData51(cardX + 22.0F, cardY + 48.0F, cardW - 44.0F, 1.0F, 0.0F, 0x15FFFFFF);

      // ================= LEFT COLUMN: CONFIGS MINI-MENU =================
      float colX = cardX + 22.0F;
      float colW = 200.0F;
      float curY = cardY + 58.0F;

      FontText.processData3(FontFace.SEMI, "МИНИ-МЕНЮ КОНФИГОВ", colX, curY, 6.5F, 0x889CA3AF);
      curY += 12.0F;

      // Config Name Input Field
      float inputH = 26.0F;
      boolean inputHov = mx >= colX && mx <= colX + colW && my >= curY && my <= curY + inputH;
      int inputBorder = this.isInputFocused ? 0xFF8B5CF6 : (inputHov ? 0x44FFFFFF : 0x22FFFFFF);
      Render2D.processData51(colX, curY, colW, inputH, 6.0F, 0x18FFFFFF);
      Render2D.processData86(colX, curY, colW, inputH, 6.0F, 1.0F, inputBorder);

      String dispInput = this.configNameInput.isEmpty() ? (this.isInputFocused ? "" : "Имя конфига...") : this.configNameInput;
      if (this.isInputFocused && (System.currentTimeMillis() / 450) % 2 == 0) {
         dispInput += "|";
      }
      int inputTextColor = this.isInputFocused || !this.configNameInput.isEmpty() ? 0xFFFFFFFF : 0x66FFFFFF;
      FontText.processData3(FontFace.SEMI, dispInput, colX + 8.0F, curY + 7.5F, 7.0F, inputTextColor);
      this.clickAreas.add(new ClickArea(colX, curY, colW, inputH, () -> this.isInputFocused = true));
      curY += 32.0F;

      // Save Config Button
      float saveH = 24.0F;
      boolean saveHov = mx >= colX && mx <= colX + colW && my >= curY && my <= curY + saveH;
      Render2D.processData51(colX, curY, colW, saveH, 6.0F, saveHov ? 0x448B5CF6 : 0x268B5CF6);
      Render2D.processData86(colX, curY, colW, saveH, 6.0F, 1.0F, 0xFF8B5CF6);
      FontText.processData3(FontFace.SEMI, "💾 Сохранить раскладку", colX + 38.0F, curY + 6.5F, 7.0F, 0xFFEDE9FE);
      this.clickAreas.add(new ClickArea(colX, curY, colW, saveH, this::saveCurrentConfig));
      curY += 34.0F;

      // Saved Configs List
      FontText.processData3(FontFace.SEMI, "СОХРАНЕННЫЕ ПРЕСЕТЫ:", colX, curY, 6.0F, 0x889CA3AF);
      curY += 10.0F;

      List<String> configs = InvConfigManager.getInstance().getAvailableConfigs();
      int maxShown = Math.min(configs.size(), 5);
      for (int i = 0; i < maxShown; i++) {
         String cfgName = configs.get(i);
         boolean isCurrent = cfgName.equalsIgnoreCase(this.activeLayout.getName());
         float itemH = 22.0F;
         boolean itemHov = mx >= colX && mx <= colX + colW - 24.0F && my >= curY && my <= curY + itemH;

         int itemBg = isCurrent ? 0x338B5CF6 : (itemHov ? 0x20FFFFFF : 0x10FFFFFF);
         int itemBorder = isCurrent ? 0x888B5CF6 : 0x18FFFFFF;
         Render2D.processData51(colX, curY, colW, itemH, 5.0F, itemBg);
         Render2D.processData86(colX, curY, colW, itemH, 5.0F, 0.5F, itemBorder);

         String prefix = isCurrent ? "● " : "○ ";
         FontText.processData3(FontFace.SEMI, prefix + cfgName, colX + 8.0F, curY + 5.5F, 6.5F, isCurrent ? 0xFFC4B5FD : 0xDDFFFFFF);

         // Click card to load
         this.clickAreas.add(new ClickArea(colX, curY, colW - 24.0F, itemH, () -> this.loadConfig(cfgName)));

         // Delete button [ ✕ ]
         float delX = colX + colW - 20.0F;
         float delY = curY + 3.0F;
         boolean delHov = mx >= delX && mx <= delX + 16.0F && my >= delY && my <= delY + 16.0F;
         if (delHov) {
            Render2D.processData51(delX, delY, 16.0F, 16.0F, 4.0F, 0x33EF4444);
         }
         FontText.processData3(FontFace.BOLD, "✕", delX + 4.5F, delY + 3.0F, 6.5F, delHov ? 0xFFEF4444 : 0x55FFFFFF);
         this.clickAreas.add(new ClickArea(delX, delY, 16.0F, 16.0F, () -> this.deleteConfig(cfgName)));

         curY += itemH + 4.0F;
      }

      // Quick Utility Buttons
      float utilY = cardY + cardH - 64.0F;
      boolean copyHov = mx >= colX && mx <= colX + colW && my >= utilY && my <= utilY + 22.0F;
      Render2D.processData51(colX, utilY, colW, 22.0F, 5.0F, copyHov ? 0x24FFFFFF : 0x12FFFFFF);
      Render2D.processData86(colX, utilY, colW, 22.0F, 5.0F, 0.5F, 0x1AFFFFFF);
      FontText.processData3(FontFace.SEMI, "📋 Скопировать текущий инв.", colX + 14.0F, utilY + 5.5F, 6.5F, copyHov ? 0xFFFFFFFF : 0xCCFFFFFF);
      this.clickAreas.add(new ClickArea(colX, utilY, colW, 22.0F, this::copyLiveInventory));

      utilY += 26.0F;
      boolean clearHov = mx >= colX && mx <= colX + colW && my >= utilY && my <= utilY + 22.0F;
      Render2D.processData51(colX, utilY, colW, 22.0F, 5.0F, clearHov ? 0x33EF4444 : 0x12FFFFFF);
      Render2D.processData86(colX, utilY, colW, 22.0F, 5.0F, 0.5F, clearHov ? 0xFFEF4444 : 0x1AFFFFFF);
      FontText.processData3(FontFace.SEMI, "🗑 Очистить раскладку", colX + 34.0F, utilY + 5.5F, 6.5F, clearHov ? 0xFFEF4444 : 0x99FFFFFF);
      this.clickAreas.add(new ClickArea(colX, utilY, colW, 22.0F, this::clearLayout));

      // Vertical divider between left menu and right grid
      Render2D.processData51(cardX + 238.0F, cardY + 56.0F, 1.0F, cardH - 72.0F, 0.0F, 0x15FFFFFF);

      // ================= RIGHT COLUMN: INVENTORY LAYOUT GRID =================
      float rightX = cardX + 252.0F;
      float gridY = cardY + 58.0F;

      FontText.processData3(FontFace.SEMI, "РАСКЛАДКА ПРЕДМЕТОВ", rightX, gridY, 6.5F, 0x889CA3AF);
      FontText.processData3(FontFace.SEMI, "ЛКМ: выбрать и поменять местами | ПКМ: очистить слот", rightX + 120.0F, gridY + 1.0F, 5.5F, 0x66FFFFFF);
      gridY += 14.0F;

      // 1. Armor Slots (39: Helmet, 38: Chestplate, 37: Leggings, 36: Boots) and Offhand (40)
      float slotSize = 25.0F;
      float slotGap = 4.0F;

      // Armor slots
      int[] armorIndices = new int[]{39, 38, 37, 36};
      String[] armorLabels = new String[]{"Шлем", "Грудь", "Поножи", "Боты"};
      for (int i = 0; i < 4; i++) {
         float sx = rightX + i * (slotSize + slotGap);
         this.renderSlotBox(armorIndices[i], sx, gridY, slotSize, slotSize, armorLabels[i], mx, my);
      }

      // Offhand slot
      float offhandX = rightX + 4 * (slotSize + slotGap) + 18.0F;
      this.renderSlotBox(40, offhandX, gridY, slotSize, slotSize, "2-я рука", mx, my);

      gridY += slotSize + 16.0F;

      // 2. Main Inventory (27 slots: 9..35)
      FontText.processData3(FontFace.SEMI, "ОСНОВНОЙ ИНВЕНТАРЬ (27 слотов)", rightX, gridY, 5.5F, 0x66A0A8B6);
      gridY += 8.0F;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            int slotIdx = 9 + row * 9 + col;
            float sx = rightX + col * (slotSize + slotGap);
            float sy = gridY + row * (slotSize + slotGap);
            this.renderSlotBox(slotIdx, sx, sy, slotSize, slotSize, null, mx, my);
         }
      }
      gridY += 3 * (slotSize + slotGap) + 4.0F;

      // 3. Hotbar (9 slots: 0..8)
      FontText.processData3(FontFace.SEMI, "ХОТБАР (Слоты 1 - 9)", rightX, gridY, 5.5F, 0x66A0A8B6);
      gridY += 8.0F;

      float hotbarW = 9 * (slotSize + slotGap) - slotGap;
      Render2D.processData51(rightX - 3.0F, gridY - 2.0F, hotbarW + 6.0F, slotSize + 4.0F, 6.0F, 0x14FFFFFF);

      for (int col = 0; col < 9; col++) {
         int slotIdx = col;
         float sx = rightX + col * (slotSize + slotGap);
         this.renderSlotBox(slotIdx, sx, gridY, slotSize, slotSize, String.valueOf(col + 1), mx, my);
      }
      gridY += slotSize + 14.0F;

      // Status / Hovered Item Info Bar
      String infoText = this.statusMessage;
      SlotBox hoveredBox = this.findHoveredSlot(mx, my);
      if (hoveredBox != null) {
         String itemId = this.activeLayout.getItem(hoveredBox.slotIndex());
         if (!itemId.isBlank()) {
            ItemStack s = InvLayoutConfig.getItemStack(itemId);
            infoText = "Слот: " + (hoveredBox.label() != null ? hoveredBox.label() : ("#" + hoveredBox.slotIndex()))
               + " → " + s.getName().getString();
         } else {
            infoText = "Слот: " + (hoveredBox.label() != null ? hoveredBox.label() : ("#" + hoveredBox.slotIndex()))
               + " (Пусто)";
         }
      } else if (this.selectedSlot != -1) {
         infoText = "Выбран слот #" + this.selectedSlot + " (Кликните другой слот для обмена)";
      }
      FontText.processData3(FontFace.SEMI, infoText, rightX, gridY + 2.0F, 6.5F, 0xFFDDD6FE);

      // 4. Action Button: "⚡ РАЗЛОЖИТЬ ИНВЕНТАРЬ"
      float sortBtnW = Math.min(cardW - 274.0F, 280.0F);
      float sortBtnH = 30.0F;
      float sortBtnY = cardY + cardH - 46.0F;
      boolean sortHov = mx >= rightX && mx <= rightX + sortBtnW && my >= sortBtnY && my <= sortBtnY + sortBtnH;

      int sortBg = sortHov ? 0xFF8B5CF6 : 0xFF7C3AED;
      Render2D.processData51(rightX, sortBtnY, sortBtnW, sortBtnH, 7.0F, sortBg);
      Render2D.processData86(rightX, sortBtnY, sortBtnW, sortBtnH, 7.0F, 1.0F, 0x44FFFFFF);

      String sortBtnText = "⚡ РАЗЛОЖИТЬ ИНВЕНТАРЬ";
      float textW = FontText.computeFloat(FontFace.BOLD, sortBtnText, 7.5F);
      FontText.processData3(FontFace.BOLD, sortBtnText, rightX + (sortBtnW - textW) / 2.0F, sortBtnY + 8.5F, 7.5F, 0xFFFFFFFF);
      this.clickAreas.add(new ClickArea(rightX, sortBtnY, sortBtnW, sortBtnH, this::executeSort));

      // Flush 2D shader rendering before drawing Minecraft item icons
      Render2D.updateState6();

      // ================= DRAW ITEM ICONS IN SLOTS =================
      float s = 1.0F / sf();
      for (SlotBox box : this.slotBoxes) {
         String itemId = this.activeLayout.getItem(box.slotIndex());
         if (itemId == null || itemId.isBlank()) continue;

         ItemStack stack = InvLayoutConfig.getItemStack(itemId);
         if (stack.isEmpty()) continue;

         drawContext.getMatrices().pushMatrix();
         drawContext.getMatrices().scale(s, s);
         int itemX = (int)(box.x() + (box.w() - 16.0F) / 2.0F);
         int itemY = (int)(box.y() + (box.h() - 16.0F) / 2.0F);

         drawContext.drawItem(stack, itemX, itemY);
         drawContext.drawStackOverlay(this.textRenderer, stack, itemX, itemY);
         drawContext.getMatrices().popMatrix();
      }
   }

   private void renderSlotBox(int slotIndex, float x, float y, float w, float h, String label, float mx, float my) {
      this.slotBoxes.add(new SlotBox(slotIndex, x, y, w, h, label));

      boolean isSelected = this.selectedSlot == slotIndex;
      boolean isHov = mx >= x && mx <= x + w && my >= y && my <= y + h;

      int bg = isSelected ? 0x448B5CF6 : (isHov ? 0x2AFFFFFF : 0x16FFFFFF);
      int border = isSelected ? 0xFF8B5CF6 : (isHov ? 0x66FFFFFF : 0x1EFFFFFF);

      Render2D.processData51(x, y, w, h, 4.0F, bg);
      Render2D.processData86(x, y, w, h, 4.0F, isSelected ? 1.5F : 0.5F, border);

      if (label != null && !this.activeLayout.hasItem(slotIndex)) {
         float lw = FontText.computeFloat(FontFace.SEMI, label, 5.0F);
         FontText.processData3(FontFace.SEMI, label, x + (w - lw) / 2.0F, y + (h - 5.0F) / 2.0F, 5.0F, 0x44FFFFFF);
      }
   }

   private SlotBox findHoveredSlot(float mx, float my) {
      for (SlotBox box : this.slotBoxes) {
         if (mx >= box.x() && mx <= box.x() + box.w() && my >= box.y() && my <= box.y() + box.h()) {
            return box;
         }
      }
      return null;
   }

   @Override
   public boolean mouseClicked(Click click, boolean bl) {
      float mx = (float)click.x() * sf();
      float my = (float)click.y() * sf();

      if (click.button() == 0) { // Left click
         // 1. Check UI Click Areas
         for (int i = this.clickAreas.size() - 1; i >= 0; i--) {
            ClickArea area = this.clickAreas.get(i);
            if (mx >= area.x && mx <= area.x + area.w && my >= area.y && my <= area.y + area.h) {
               area.action.run();
               return true;
            }
         }
         this.isInputFocused = false;

         // 2. Check Slot Box clicks
         SlotBox clickedBox = this.findHoveredSlot(mx, my);
         if (clickedBox != null) {
            int slot = clickedBox.slotIndex();
            if (this.selectedSlot == -1) {
               this.selectedSlot = slot;
               this.statusMessage = "Выбран слот #" + slot;
            } else if (this.selectedSlot == slot) {
               this.selectedSlot = -1;
               this.statusMessage = "Выбор отменен";
            } else {
               // Swap items between selectedSlot and clicked slot
               String itemA = this.activeLayout.getItem(this.selectedSlot);
               String itemB = this.activeLayout.getItem(slot);
               this.activeLayout.setItem(this.selectedSlot, itemB);
               this.activeLayout.setItem(slot, itemA);
               this.statusMessage = "Слоты #" + this.selectedSlot + " и #" + slot + " поменяны местами";
               this.selectedSlot = -1;
            }
            return true;
         }
      } else if (click.button() == 1) { // Right click -> clear slot item
         SlotBox clickedBox = this.findHoveredSlot(mx, my);
         if (clickedBox != null) {
            this.activeLayout.setItem(clickedBox.slotIndex(), "");
            if (this.selectedSlot == clickedBox.slotIndex()) {
               this.selectedSlot = -1;
            }
            this.statusMessage = "Слот #" + clickedBox.slotIndex() + " очищен";
            return true;
         }
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

      if (this.isInputFocused) {
         if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (!this.configNameInput.isEmpty()) {
               this.configNameInput = this.configNameInput.substring(0, this.configNameInput.length() - 1);
            }
            return true;
         }
         if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            this.saveCurrentConfig();
            this.isInputFocused = false;
            return true;
         }
         if (key == GLFW.GLFW_KEY_V && (GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == 1
            || GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == 1)) {
            String clip = MinecraftClient.getInstance().keyboard.getClipboard();
            if (clip != null && !clip.isBlank()) {
               this.configNameInput = (this.configNameInput + clip.trim());
               if (this.configNameInput.length() > 20) {
                  this.configNameInput = this.configNameInput.substring(0, 20);
               }
            }
            return true;
         }
      }

      return super.keyPressed(keyInput);
   }

   @Override
   public boolean charTyped(CharInput charInput) {
      if (this.isInputFocused && charInput.codepoint() >= 32) {
         char c = (char)charInput.codepoint();
         if (this.configNameInput.length() < 20) {
            this.configNameInput += c;
         }
         return true;
      }
      return super.charTyped(charInput);
   }

   private void saveCurrentConfig() {
      String name = this.configNameInput.isBlank() ? this.activeLayout.getName() : this.configNameInput.trim();
      this.activeLayout.setName(name);
      boolean ok = InvConfigManager.getInstance().saveConfig(this.activeLayout);
      if (ok) {
         InvConfigManager.getInstance().setActiveConfig(this.activeLayout);
         this.statusMessage = "✓ Конфиг '" + name + "' успешно сохранен!";
      } else {
         this.statusMessage = "Ошибка при сохранении конфига";
      }
   }

   private void loadConfig(String name) {
      this.activeLayout = InvConfigManager.getInstance().loadConfig(name);
      this.configNameInput = name;
      InvConfigManager.getInstance().setActiveConfig(this.activeLayout);
      this.selectedSlot = -1;
      this.statusMessage = "Загружен конфиг: " + name;
   }

   private void deleteConfig(String name) {
      InvConfigManager.getInstance().deleteConfig(name);
      this.statusMessage = "Конфиг '" + name + "' удален";
   }

   private void copyLiveInventory() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null) {
         this.activeLayout = InvLayoutConfig.fromPlayerInventory(client.player.getInventory(), this.activeLayout.getName());
         this.selectedSlot = -1;
         this.statusMessage = "✓ Скопирован текущий инвентарь игрока!";
      }
   }

   private void clearLayout() {
      this.activeLayout.clear();
      this.selectedSlot = -1;
      this.statusMessage = "Раскладка полностью очищена";
   }

   private void executeSort() {
      InvConfigManager.getInstance().setActiveConfig(this.activeLayout);
      InvManagerModule.getInstance().startSorting(this.activeLayout);
      this.statusMessage = "✓ Запущена авто-раскладка инвентаря!";
   }
}

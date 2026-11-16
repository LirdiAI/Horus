package horus.client.module.misc;

import horus.client.event.EventListener;
import horus.client.event.PlayerTickEvent;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.ModeSetting;
import horus.client.setting.NumberSetting;
import horus.client.ui.hud.NotificationHud;
import horus.client.util.InventoryUtils;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

@ModuleInfo(name = "AutoJoiner", description = "Автоматически заходит на выбранный режим ReallyWorld через компас.", category = ModuleCategory.MISC)
public final class AutoJoinerModule extends Module {
   private enum JoinStage {
      WAIT_PLAYER,
      WAIT_COMPASS_MENU,
      WAIT_GRIFF_MENU,
      WAIT_JOIN
   }

   private static AutoJoinerModule autoJoinerModule;
   private final ModeSetting server = this.addSetting(
      new ModeSetting("Сервер", "Выбор сети для входа.", "ReallyWorld", "ReallyWorld")
   );
   private final NumberSetting griff = this.addSetting(
      new NumberSetting("Гриф", "Номер режима ReallyWorld для входа.")
         .withIntegerRange(1, 56)
         .setValue(1.0F)
   );
   private JoinStage stage = JoinStage.WAIT_PLAYER;
   private long stageStartedAt;
   private long noScreenSince;
   private long lastHeadClickAt;
   private final EventListener<PlayerTickEvent> tickListener = event -> this.tickJoin();

   private AutoJoinerModule() {
   }

   public static AutoJoinerModule getInstance() {
      if (autoJoinerModule == null) {
         autoJoinerModule = new AutoJoinerModule();
      }
      return autoJoinerModule;
   }

   @Override
   protected void onEnable() {
      this.stage = JoinStage.WAIT_PLAYER;
      this.stageStartedAt = System.currentTimeMillis();
      this.noScreenSince = 0L;
      this.lastHeadClickAt = 0L;
   }

   @Override
   protected void onDisable() {
      this.stage = JoinStage.WAIT_PLAYER;
      this.noScreenSince = 0L;
      this.lastHeadClickAt = 0L;
   }

   private void tickJoin() {
      if (mc.player == null || mc.world == null || mc.interactionManager == null) {
         return;
      }

      if (!this.server.isSelected("ReallyWorld")) {
         return;
      }

      switch (this.stage) {
         case WAIT_PLAYER -> this.openCompassMenu();
         case WAIT_COMPASS_MENU -> this.handleCompassMenu();
         case WAIT_GRIFF_MENU -> this.handleGriffMenu();
         case WAIT_JOIN -> this.waitForJoin();
      }
   }

   private void openCompassMenu() {
      int compassSlot = InventoryUtils.findHotbarSlot(Items.COMPASS);
      if (compassSlot == -1) {
         this.fail("В хотбаре нет компаса");
         return;
      }

      int oldSlot = InventoryUtils.getSelectedSlot();
      try {
         InventoryUtils.setSelectedSlot(compassSlot, true);
         mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
      } finally {
         InventoryUtils.setSelectedSlot(oldSlot, true);
      }

      this.advance(JoinStage.WAIT_COMPASS_MENU);
   }

   private void handleCompassMenu() {
      if (this.isTimedOut()) {
         this.fail("Не открылось меню компаса");
         return;
      }

      GenericContainerScreenHandler handler = this.getContainerHandler();
      if (handler == null) {
         return;
      }

      Slot dynamicSlot = this.findSlot(handler, this::isDynamicItem);
      if (dynamicSlot != null) {
         InventoryUtils.sendSlotClickPacket(dynamicSlot.id, 0, SlotActionType.PICKUP, false);
         this.advance(JoinStage.WAIT_GRIFF_MENU);
      }
   }

   private void handleGriffMenu() {
      if (this.isTimedOut()) {
         this.fail("Не найден список режимов ReallyWorld");
         return;
      }

      GenericContainerScreenHandler handler = this.getContainerHandler();
      if (handler == null) {
         return;
      }

      String target = "гриф #" + this.griff.intValue();
      Slot griffSlot = this.findSlot(
         handler,
         stack -> stack.isOf(Items.PLAYER_HEAD)
            && Formatting.strip(stack.getName().getString()).toLowerCase(java.util.Locale.ROOT).startsWith(target)
      );
      if (griffSlot != null) {
         InventoryUtils.sendSlotClickPacket(griffSlot.id, 0, SlotActionType.PICKUP, false);
         this.advance(JoinStage.WAIT_JOIN);
         this.noScreenSince = 0L;
         this.lastHeadClickAt = System.currentTimeMillis();
      }
   }

   private void waitForJoin() {
      GenericContainerScreenHandler handler = this.getContainerHandler();
      if (handler != null) {
         Slot griffSlot = this.findSelectedGriffSlot(handler);
         long now = System.currentTimeMillis();
         if (griffSlot != null && now - this.lastHeadClickAt >= 1000L) {
            InventoryUtils.sendSlotClickPacket(griffSlot.id, 0, SlotActionType.PICKUP, false);
            this.lastHeadClickAt = now;
         }
         this.noScreenSince = 0L;
         return;
      }

      if (mc.currentScreen == null) {
         long now = System.currentTimeMillis();
         if (this.noScreenSince == 0L) {
            this.noScreenSince = now;
         } else if (now - this.noScreenSince >= 2500L) {
            this.notify("Подключение к Грифу #" + this.griff.intValue(), true);
            this.setEnabled(false);
         }
      } else {
         this.noScreenSince = 0L;
      }
   }

   private GenericContainerScreenHandler getContainerHandler() {
      if (!(mc.currentScreen instanceof HandledScreen<?>)
         || !(mc.player.currentScreenHandler instanceof GenericContainerScreenHandler handler)) {
         return null;
      }
      return handler;
   }

   private Slot findSlot(GenericContainerScreenHandler handler, java.util.function.Predicate<ItemStack> predicate) {
      int inventorySize = handler.getInventory().size();
      for (Slot slot : handler.slots) {
         if (slot.id < inventorySize && predicate.test(slot.getStack())) {
            return slot;
         }
      }
      return null;
   }

   private Slot findSelectedGriffSlot(GenericContainerScreenHandler handler) {
      String target = "гриф #" + this.griff.intValue();
      return this.findSlot(
         handler,
         stack -> stack.isOf(Items.PLAYER_HEAD)
            && Formatting.strip(stack.getName().getString()).toLowerCase(java.util.Locale.ROOT).startsWith(target)
      );
   }

   private boolean isDynamicItem(ItemStack stack) {
      return stack.isOf(Items.TNT);
   }

   private boolean isTimedOut() {
      return System.currentTimeMillis() - this.stageStartedAt >= 15000L;
   }

   private void advance(JoinStage nextStage) {
      this.stage = nextStage;
      this.stageStartedAt = System.currentTimeMillis();
   }

   private void fail(String message) {
      this.notify(message, false);
      this.setEnabled(false);
   }

   private void notify(String message, boolean success) {
      NotificationHud.notifyWithText(
         this.category.getIcon(),
         "AutoJoiner",
         Text.literal(message).setStyle(Style.EMPTY.withColor(success ? Formatting.GREEN : Formatting.RED)),
         true
      );
   }
}

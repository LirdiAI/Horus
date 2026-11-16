package horus.client.module.player;

import horus.client.event.EventListener;
import horus.client.event.PlayerTickEvent;
import horus.client.invmanager.InvConfigManager;
import horus.client.invmanager.InvLayoutConfig;
import horus.client.invmanager.InvSortEngine;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.ActionSetting;
import horus.client.setting.KeybindSetting;
import horus.client.setting.NumberSetting;
import horus.client.ui.InvManagerScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

@ModuleInfo(
   name = "InvManager",
   description = "Управление раскладкой инвентаря, сохранение конфигов и авто-сортировка.",
   category = ModuleCategory.PLAYER
)
public final class InvManagerModule extends Module {
   private static InvManagerModule instance;

   public final KeybindSetting openKey = this.addSetting(
      new KeybindSetting("Бинд меню", "Клавиша для открытия меню InvManager.")
   );

   public final ActionSetting openButton = this.addSetting(
      new ActionSetting("Открыть меню", "Открыть меню настройки раскладки инвентаря.")
         .withButtonLabel("Открыть")
         .onClick(() -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
               client.setScreen(new InvManagerScreen());
            }
         })
   );

   public final KeybindSetting sortKey = this.addSetting(
      new KeybindSetting("Бинд на раскладку", "Автоматически раскладывать инвентарь по активному конфигу.")
   );

   public final NumberSetting delaySetting = this.addSetting(
      new NumberSetting("Задержка (мс)", "Интервал между перемещением предметов в инвентаре.")
         .withRange(0.0F, 200.0F)
         .withStep(5.0F)
         .withSuffix(" мс")
         .setValue(40.0F)
   );

   private final Queue<InvSortEngine.SwapOperation> swapQueue = new ArrayDeque<>();
   private long lastActionTime = 0L;
   private boolean openKeyPressed = false;
   private boolean sortKeyPressed = false;

   private final EventListener<PlayerTickEvent> tickListener = event -> {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == null || client.getWindow() == null) return;

      long windowHandle = client.getWindow().getHandle();

      // 1. Open Menu Keybind
      boolean isDownOpen = this.openKey.isBound() && this.openKey.getBinding().isPressed(windowHandle);
      if (isDownOpen && !this.openKeyPressed && client.currentScreen == null) {
         client.setScreen(new InvManagerScreen());
      }
      this.openKeyPressed = isDownOpen;

      // 2. Quick Sort Keybind
      boolean isDownSort = this.sortKey.isBound() && this.sortKey.getBinding().isPressed(windowHandle);
      if (isDownSort && !this.sortKeyPressed && client.currentScreen == null) {
         this.startSorting(null);
      }
      this.sortKeyPressed = isDownSort;

      // 3. Process sorting queue
      if (!this.swapQueue.isEmpty()) {
         long now = System.currentTimeMillis();
         long delay = (long)this.delaySetting.getValue();

         if (delay <= 0L) {
            // Instant batch sort
            while (!this.swapQueue.isEmpty()) {
               InvSortEngine.SwapOperation op = this.swapQueue.poll();
               if (op != null) {
                  InvSortEngine.executeSwap(op);
               }
            }
         } else if (now - this.lastActionTime >= delay) {
            InvSortEngine.SwapOperation op = this.swapQueue.poll();
            if (op != null) {
               InvSortEngine.executeSwap(op);
               this.lastActionTime = now;
            }
         }
      }
   };

   public static InvManagerModule getInstance() {
      if (instance == null) {
         instance = new InvManagerModule();
      }
      return instance;
   }

   public InvManagerModule() {
      instance = this;
      this.setEnabled(true);
   }

   public void startSorting(InvLayoutConfig layout) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == null) return;

      InvLayoutConfig config = layout != null ? layout : InvConfigManager.getInstance().getActiveConfig();
      if (config == null) return;

      List<InvSortEngine.SwapOperation> plan = InvSortEngine.planSorting(config);
      this.swapQueue.clear();
      this.swapQueue.addAll(plan);
      this.lastActionTime = 0L;

      if (!plan.isEmpty()) {
         client.player.sendMessage(
            Text.literal("§d[Horus] §fРаскладка инвентаря: §a" + plan.size() + "§f действий (" + config.getName() + ")."),
            false
         );
      } else {
         client.player.sendMessage(
            Text.literal("§d[Horus] §aИнвентарь уже соответствует конфигу '" + config.getName() + "'!"),
            false
         );
      }
   }

   public int getPendingActionsCount() {
      return this.swapQueue.size();
   }

   public void cancelSorting() {
      this.swapQueue.clear();
   }
}

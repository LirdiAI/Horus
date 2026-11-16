package horus.client.module.misc;

import horus.client.event.ClientTickEvent;
import horus.client.event.MovementInputEvent;
import horus.client.storage.AltStore;
import horus.client.event.EventListener;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.multiplayer.AltBot;
import horus.client.setting.BooleanSetting;
import horus.client.setting.KeybindSetting;
import horus.client.util.InputBinding;
import horus.client.ui.MultiAccountScreen;
import horus.client.ui.hud.NotificationHud;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Мультиаккаунт — несколько аккаунтов на одном сервере из одного окна.
 * Правый Ctrl (по умолчанию) открывает панель: вводишь ник, второй аккаунт
 * сам заходит на тот же сервер, виден его статус (ХП/голод/пинг),
 * при смерти сам возрождается. Чат можно писать от всех сразу.
 */
@ModuleInfo(name = "MultiAccount", description = "Несколько аккаунтов на одном сервере из одного окна.", category = ModuleCategory.MISC)
public final class MultiAccountModule extends Module {
   private static MultiAccountModule multiAccountModule;
   public KeybindSetting keybindSetting;
   public BooleanSetting followSetting;
   public final horus.client.setting.TextSetting passwordSetting;
   public final horus.client.setting.TextSetting modeCommandSetting;
   private final List<AltBot> bots = new ArrayList<>();
   private volatile java.net.InetSocketAddress resolvedServerAddress;
   private volatile AltBot driveTarget;
   public boolean attackFlag;
   private final EventListener<MovementInputEvent> driveMovementListener = movementInputEvent -> {
      // в режиме посадки свой персонаж стоит на месте
      if (this.driveTarget != null) {
         movementInputEvent.cancel();
      }
   };
   private final EventListener<ClientTickEvent> clientTickListener = clientTickEvent -> this.onTick();
   private boolean flag;

   public static MultiAccountModule getInstance() {
      if (multiAccountModule == null) {
         multiAccountModule = new MultiAccountModule();
      }

      return multiAccountModule;
   }

   private MultiAccountModule() {
      this.keybindSetting = this.addSetting(
         new KeybindSetting("Открыть панель", "Открывает окно мультиаккаунта.", InputBinding.keyboardKey(345))
      );
      this.followSetting = this.addSetting(new BooleanSetting("Следовать за игроком", "Альт держится рядом с тобой.", true));
      this.passwordSetting = this.addSetting(new horus.client.setting.TextSetting("Пароль альтов", "Пароль для /register и /login на оффлайн-сервере."));
      this.passwordSetting.setValue("123456");
      this.modeCommandSetting = this.addSetting(new horus.client.setting.TextSetting("Команда режима", "Команда входа в режим из лобби, например play или mode anarchy. Без слэша. Пусто = не отправлять."));
      this.modeCommandSetting.setValue("");
   }

   public void setDriveTarget(AltBot bot) {
      if (this.driveTarget == bot) {
         if (bot != null) {
            bot.setDriven(false);
         }

         this.driveTarget = null;
         horus.client.ui.hud.NotificationHud.notifyWithText("★", "Мультиаккаунт", Text.literal("Ты вернулся за свой аккаунт"), true);
         return;
      }

      if (this.driveTarget != null) {
         this.driveTarget.setDriven(false);
      }

      this.driveTarget = bot;
      if (bot != null) {
         bot.setDriven(true);
         horus.client.ui.hud.NotificationHud.notifyWithText("★", "Мультиаккаунт", Text.literal("Управляешь: " + bot.name + " — WASD идёт, ЛКМ бьёт, встать — кнопка в панели"), true);
      }
   }

   public AltBot getDriveTarget() {
      return this.driveTarget;
   }

   public String getModeCommand() {
      String cmd = this.modeCommandSetting.getValue();
      if (cmd == null) return "";
      cmd = cmd.trim();
      while (cmd.startsWith("/")) {
         cmd = cmd.substring(1);
      }
      return cmd;
   }

   public String getPassword() {
      String pw = this.passwordSetting.getValue();
      return pw == null ? "" : pw.trim();
   }

   public List<AltBot> getBots() {
      synchronized (this.bots) {
         return new ArrayList<>(this.bots);
      }
   }

   public boolean isFollowEnabled() {
      return this.followSetting.isChecked();
   }

   public void addBot(String name, String password) {
      java.net.InetSocketAddress resolved = this.resolvedServerAddress;
      if (resolved == null) {
         NotificationHud.notifyWithText("★", "Мультиаккаунт", Text.literal("Нет данных о текущем сервере"), false);
         return;
      }

      for (AltBot bot : this.getBots()) {
         if (bot.name.equalsIgnoreCase(name)) {
            NotificationHud.notifyWithText("★", "Мультиаккаунт", Text.literal("Такой альт уже есть"), false);
            return;
         }
      }

      if (!AltBot.isNickValid(name)) {
         NotificationHud.notifyWithText("★", "Мультиаккаунт", Text.literal("Ник: 3-16 символов, латиница/цифры/_"), false);
         return;
      }

      AltStore.addAlt(name, password);
      AltStore.setModeCommand(this.modeCommandSetting.getValue());
      AltBot bot = new AltBot(name, password, this.getModeCommand(), resolved);
      synchronized (this.bots) {
         this.bots.add(bot);
      }

      bot.start();
      horus.client.HorusClient.logger().info("[MultiAccount] Alt {} connecting to {}:{}", name, resolved.getHostString(), resolved.getPort());
   }

   public void removeBot(AltBot bot) {
      if (this.driveTarget == bot) {
         this.driveTarget = null;
      }

      bot.disconnect();
      AltStore.removeAlt(bot.name);
      synchronized (this.bots) {
         this.bots.remove(bot);
      }
   }

   public void restoreSavedAlts() {
      java.net.InetSocketAddress resolved = this.resolvedServerAddress;
      if (resolved == null) {
         NotificationHud.notifyWithText("★", "Мультиаккаунт", Text.literal("Сначала зайди на сервер"), false);
         return;
      }

      String modeCommand = this.getModeCommand();
      int restored = 0;

      for (String name : AltStore.getAltNames()) {
         boolean exists = false;

         for (AltBot bot : this.getBots()) {
            if (bot.name.equalsIgnoreCase(name)) {
               exists = true;
               break;
            }
         }

         if (!exists && AltBot.isNickValid(name)) {
            AltBot bot = new AltBot(name, AltStore.getPassword(name), modeCommand, resolved);
            synchronized (this.bots) {
               this.bots.add(bot);
            }

            bot.start();
            restored++;
         }
      }

      NotificationHud.notifyWithText("★", "Мультиаккаунт", Text.literal(restored > 0 ? "Подключаю альтов: " + restored : "Все сохранённые альты уже в списке"), true);
   }

   public void sendChatToBot(AltBot target, String message) {
      if (target != null) {
         target.sendChat(message);
      }
   }

   public void sendChatFromAll(String message) {
      for (AltBot bot : this.getBots()) {
         bot.sendChat(message);
      }

      if (mc.player != null && mc.getNetworkHandler() != null) {
         mc.getNetworkHandler().sendChatMessage(message);
      }
   }

   public String getCurrentServerAddress() {
      if (mc.getCurrentServerEntry() != null) {
         return mc.getCurrentServerEntry().address;
      }

      return null;
   }

   private void onTick() {
      // === режим посадки за альта ===
      AltBot driver = this.driveTarget;
      if (driver != null) {
         if (!driver.isAlive() || driver.getStatus() == AltBot.Status.CLOSED) {
            this.setDriveTarget(null);
            NotificationHud.notifyWithText("★", "Мультиаккаунт", Text.literal("Альт отключился — ты снова за своим"), false);
         } else if (driver.isOnServer() && mc.player != null) {
            horus.client.util.PlayerInputAccess.handleDrive(this, driver, mc);
         }
      }

      // берём уже разрешённый адрес из текущего подключения (без DNS)
      if (mc.getNetworkHandler() != null && mc.getNetworkHandler().getConnection() != null) {
         java.net.SocketAddress socketAddress = mc.getNetworkHandler().getConnection().getAddress();
         if (socketAddress instanceof java.net.InetSocketAddress inetSocketAddress && !inetSocketAddress.isUnresolved()) {
            this.resolvedServerAddress = inetSocketAddress;
         }
      }

      // тикаем ботов
      for (AltBot bot : this.getBots()) {
         if (bot.isAlive()) {
            bot.tick(mc);
         }
      }

      // чистим отключённых — оставляем в списке, чтобы была видна причина
      // (убирает пользователь крестиком или при закрытии панели)

      // кейбинд открытия панели
      if (this.keybindSetting.isBound()) {
         boolean pressed = this.keybindSetting.getBinding().getDevice() == InputBinding.DeviceType.MOUSE
            ? GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), this.keybindSetting.getBinding().getCode()) == 1
            : GLFW.glfwGetKey(mc.getWindow().getHandle(), this.keybindSetting.getBinding().getCode()) == 1;
         if (pressed && !this.flag && mc.currentScreen == null && this.hasServerContext()) {
            mc.setScreen(new MultiAccountScreen(null));
         }

         this.flag = pressed;
      }
   }

   private boolean hasServerContext() {
      return mc.player != null && this.getCurrentServerAddress() != null;
   }

   public static MultiAccountModule instance() {
      return getInstance();
   }
}

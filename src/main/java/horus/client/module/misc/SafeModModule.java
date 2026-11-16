package horus.client.module.misc;

import horus.client.WindowAppearance;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.module.ModuleManager;
import horus.client.setting.BooleanSetting;
import horus.client.setting.ModeSetting;
import horus.client.ui.ClickGuiScreen;
import horus.client.util.ClientChat;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(
   name = "SafeMod",
   description = "Режим проверки на читы / Panic: моментально скрывает интерфейс чита и отключает модули.",
   category = ModuleCategory.MISC
)
public final class SafeModModule extends Module {
   private static SafeModModule instance;

   public final ModeSetting mode = this.addSetting(
      new ModeSetting("Режим", "Область отключения", "Full", "Full", "Combat", "Visuals")
   );
   public final BooleanSetting hideHud = this.addSetting(
      new BooleanSetting("Скрыть HUD", "Полностью скрывать все элементы HUD клиента", true)
   );
   public final BooleanSetting disableCheats = this.addSetting(
      new BooleanSetting("Отключить модули", "Деактивировать активные модули", true)
   );
   public final BooleanSetting lockGui = this.addSetting(
      new BooleanSetting("Блокировать ClickGUI", "Запретить открытие меню читов", true)
   );
   public final BooleanSetting restoreOnDisable = this.addSetting(
      new BooleanSetting("Восстанавливать", "Возвращать включенные модули при выключении SafeMod", true)
   );
   public final BooleanSetting restoreTitle = this.addSetting(
      new BooleanSetting("Очистить заголовок", "Возвращать стандартное название окна Minecraft", true)
   );
   public final BooleanSetting clearChat = this.addSetting(
      new BooleanSetting("Очистить чат", "Очищать историю чата при включении", false)
   );
   public final BooleanSetting notify = this.addSetting(
      new BooleanSetting("Уведомления", "Оповещения в чат о статусе SafeMod", true)
   );

   private final List<Module> savedModules = new ArrayList<>();

   private SafeModModule() {
   }

   public static SafeModModule getInstance() {
      if (instance == null) {
         instance = new SafeModModule();
      }
      return instance;
   }

   public boolean isHideHud() {
      return this.isEnabled() && this.hideHud.getValue();
   }

   public boolean isLockGui() {
      return this.isEnabled() && this.lockGui.getValue();
   }

   public boolean isRestoreTitle() {
      return this.isEnabled() && this.restoreTitle.getValue();
   }

   @Override
   protected void onEnable() {
      this.savedModules.clear();

      // Close ClickGui if currently open
      if (mc.currentScreen instanceof ClickGuiScreen) {
         mc.setScreen(null);
      }

      // Disable modules
      if (this.disableCheats.getValue()) {
         String selectedMode = this.mode.getSelectedOption();
         for (Module mod : ModuleManager.getModules()) {
            if (mod == this || mod == null) {
               continue;
            }
            if (!mod.isEnabled()) {
               continue;
            }

            boolean shouldDisable = false;
            if ("Full".equalsIgnoreCase(selectedMode)) {
               shouldDisable = true;
            } else if ("Combat".equalsIgnoreCase(selectedMode)) {
               if (mod.category == ModuleCategory.COMBAT || mod.category == ModuleCategory.MOVEMENT || mod.category == ModuleCategory.PLAYER) {
                  shouldDisable = true;
               }
            } else if ("Visuals".equalsIgnoreCase(selectedMode)) {
               if (mod.category == ModuleCategory.RENDER) {
                  shouldDisable = true;
               }
            }

            if (shouldDisable) {
               this.savedModules.add(mod);
               mod.setEnabled(false);
            }
         }
      }

      // Clear chat history if requested
      if (this.clearChat.getValue() && mc.inGameHud != null && mc.inGameHud.getChatHud() != null) {
         mc.inGameHud.getChatHud().clear(false);
      }

      // Restore vanilla window title
      if (this.restoreTitle.getValue() && mc.getWindow() != null) {
         try {
            org.lwjgl.glfw.GLFW.glfwSetWindowTitle(
               mc.getWindow().getHandle(),
               "Minecraft " + WindowAppearance.formatText2()
            );
         } catch (Throwable ignored) {
         }
      }

      // Chat notification
      if (this.notify.getValue()) {
         ClientChat.processData("&c[SafeMod] &fБезопасный режим &aВКЛЮЧЕН&f. Сохранено и отключено модулей: &e" + this.savedModules.size());
      }
   }

   @Override
   protected void onDisable() {
      // Restore previously saved modules
      if (this.restoreOnDisable.getValue() && !this.savedModules.isEmpty()) {
         int restoredCount = 0;
         for (Module mod : this.savedModules) {
            if (mod != null && !mod.isEnabled()) {
               mod.setEnabled(true);
               restoredCount++;
            }
         }
         this.savedModules.clear();

         if (this.notify.getValue()) {
            ClientChat.processData("&c[SafeMod] &fБезопасный режим &cВЫКЛЮЧЕН&f. Восстановлено модулей: &a" + restoredCount);
         }
      } else {
         this.savedModules.clear();
         if (this.notify.getValue()) {
            ClientChat.processData("&c[SafeMod] &fБезопасный режим &cВЫКЛЮЧЕН&f.");
         }
      }

      // Restore client window title
      if (this.restoreTitle.getValue() && mc.getWindow() != null) {
         try {
            org.lwjgl.glfw.GLFW.glfwSetWindowTitle(
               mc.getWindow().getHandle(),
               WindowAppearance.formatText().concat(" | DLC")
            );
         } catch (Throwable ignored) {
         }
      }
   }
}

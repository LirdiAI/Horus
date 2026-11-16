package horus.client.command;

import horus.client.storage.ConfigStore;
import horus.client.util.ClientChat;
import horus.client.util.CloudConfigManager;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Arrays;

public class CloudCommand implements ClientCommand {
   private final String name;

   public CloudCommand(String name) {
      this.name = name;
   }

   public CloudCommand() {
      this("cloud");
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public String getDescription() {
      return "Обмен конфигами через облако (.cloud share [имя] / .cloud load <код>)";
   }

   @Override
   public ArrayList<String> suggestArguments(String[] args) {
      if (args.length <= 1) {
         return new ArrayList<>(Arrays.asList("share", "load", "help"));
      }
      if (args.length == 2 && args[0].equalsIgnoreCase("share")) {
         return ConfigStore.listConfigs();
      }
      return new ArrayList<>();
   }

   @Override
   public void execute(String[] args) {
      if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
         ClientChat.processData(Formatting.AQUA + "☁ [Horus Cloud Share]:");
         ClientChat.processData(Formatting.GRAY + " • " + Formatting.WHITE + ".cloud share [конфиг] " + Formatting.GRAY + "- сгенерировать код и поделиться");
         ClientChat.processData(Formatting.GRAY + " • " + Formatting.WHITE + ".cloud load <H-XXXX> " + Formatting.GRAY + "- загрузить и применить конфиг");
         return;
      }

      if (args[0].equalsIgnoreCase("share")) {
         String target = args.length > 1 ? args[1] : ConfigStore.getActiveConfig();
         ClientChat.processData(Formatting.YELLOW + "☁ Отправка конфига '" + target + "' в облако Supabase...");
         CloudConfigManager.shareConfigAsync(target).thenAccept(code -> {
            if (code != null) {
               ClientChat.processData(Formatting.GREEN + "✔ Конфиг '" + target + "' загружен! Код: " + Formatting.BOLD + Formatting.AQUA + code);
               ClientChat.processData(Formatting.GRAY + "Код скопирован в буфер обмена. Отправьте его друзьям!");
            } else {
               ClientChat.processData(Formatting.RED + "✖ Ошибка: " + CloudConfigManager.getLastStatusMessage());
            }
         });
         return;
      }

      if (args[0].equalsIgnoreCase("load") || args[0].equalsIgnoreCase("get")) {
         if (args.length < 2) {
            ClientChat.processData(Formatting.RED + "✖ Укажите код конфига: .cloud load <H-XXXX>");
            return;
         }
         String code = args[1];
         ClientChat.processData(Formatting.YELLOW + "☁ Получение конфига " + code + " из облака...");
         CloudConfigManager.loadConfigAsync(code).thenAccept(success -> {
            if (success) {
               ClientChat.processData(Formatting.GREEN + "✔ " + CloudConfigManager.getLastStatusMessage());
            } else {
               ClientChat.processData(Formatting.RED + "✖ " + CloudConfigManager.getLastStatusMessage());
            }
         });
         return;
      }

      // If user typed: .cloud H-XXXX directly
      if (args[0].toUpperCase().startsWith("H-") || (args[0].length() == 4 && args[0].matches("[A-Za-z0-9]+"))) {
         String code = args[0];
         ClientChat.processData(Formatting.YELLOW + "☁ Получение конфига " + code + " из облака...");
         CloudConfigManager.loadConfigAsync(code).thenAccept(success -> {
            if (success) {
               ClientChat.processData(Formatting.GREEN + "✔ " + CloudConfigManager.getLastStatusMessage());
            } else {
               ClientChat.processData(Formatting.RED + "✖ " + CloudConfigManager.getLastStatusMessage());
            }
         });
         return;
      }

      ClientChat.processData(Formatting.RED + "✖ Неизвестный аргумент. Используйте .cloud help");
   }
}

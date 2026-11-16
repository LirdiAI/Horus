package horus.client.command;

import horus.client.module.Module;
import horus.client.module.ModuleManager;
import horus.client.storage.ConfigStore;
import horus.client.util.ClientChat;
import horus.client.util.InputBinding;

import java.util.ArrayList;
import java.util.Arrays;

public class BindCommand implements ClientCommand {
   @Override
   public void execute(String[] args) {
      if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
         ClientChat.processData8("Использование: &7.bind <модуль> <клавиша>&r, &7.bind <модуль> none&r, &7.bind list&r, &7.bind clear");
         return;
      }

      if (args[0].equalsIgnoreCase("list")) {
         ArrayList<String> boundList = new ArrayList<>();
         for (Module m : ModuleManager.getModules()) {
            if (m.hasKeybind()) {
               boundList.add("&7" + m.name + " &8— &f[" + m.getKeyName() + "]");
            }
         }
         if (boundList.isEmpty()) {
            ClientChat.processData8("Нет привязанных клавиш у модулей.");
         } else {
            ClientChat.processData9("&fСписок биндов (" + boundList.size() + "):", boundList.toArray(new String[0]));
         }
         return;
      }

      if (args[0].equalsIgnoreCase("clear")) {
         int count = 0;
         for (Module m : ModuleManager.getModules()) {
            if (m.hasKeybind()) {
               m.withKeyCode(-1);
               count++;
            }
         }
         ConfigStore.saveActiveConfig();
         ClientChat.processData8("Все бинды очищены (&7" + count + "&r шт). Сохранено в конфиг &7" + ConfigStore.getActiveConfig() + "&r.");
         return;
      }

      String moduleName = args[0];
      Module module = ModuleManager.findByName(moduleName);
      if (module == null) {
         ClientChat.processData8("Модуль &7" + moduleName + "&r не найден. Проверь правильность названия.");
         return;
      }

      if (args.length < 2) {
         String currentKey = module.hasKeybind() ? module.getKeyName() : "нет";
         ClientChat.processData8("Модуль &7" + module.name + "&r привязан к клавише &7" + currentKey + "&r. Для смены: &7.bind " + module.name + " <клавиша>");
         return;
      }

      String keyName = args[1].trim();
      if (keyName.equalsIgnoreCase("none") || keyName.equalsIgnoreCase("clear") || keyName.equalsIgnoreCase("-1") || keyName.equalsIgnoreCase("null") || keyName.equalsIgnoreCase("del") || keyName.equalsIgnoreCase("off")) {
         module.withKeyCode(-1);
         ConfigStore.saveActiveConfig();
         ClientChat.processData8("Бинд с модуля &7" + module.name + "&r удалён (сохранено в конфиг &7" + ConfigStore.getActiveConfig() + "&r).");
         return;
      }

      int keyCode = parseKey(keyName);
      if (keyCode == -1) {
         ClientChat.processData8("Клавиша &7" + keyName + "&r не распознана. Пример: &7R&r, &7F&r, &7V&r, &7F5&r, &7SPACE&r, &7LSHIFT");
         return;
      }

      module.withKeyCode(keyCode);
      ConfigStore.saveActiveConfig();
      ClientChat.processData8("Модуль &7" + module.name + "&r привязан к клавише &7" + module.getKeyName() + "&r (сохранено в конфиг &7" + ConfigStore.getActiveConfig() + "&r).");
   }

   public static int parseKey(String name) {
      if (name == null || name.isEmpty()) {
         return -1;
      }
      for (int code = 32; code <= 348; code++) {
         if (InputBinding.keyboardKey(code).getDisplayName().equalsIgnoreCase(name)) {
            return code;
         }
      }
      if (name.length() == 1) {
         char c = Character.toUpperCase(name.charAt(0));
         if (c >= 'A' && c <= 'Z') return (int) c;
         if (c >= '0' && c <= '9') return (int) c;
      }
      return -1;
   }

   @Override
   public ArrayList<String> suggestArguments(String[] args) {
      ArrayList<String> suggestions = new ArrayList<>();
      if (args.length <= 1) {
         suggestions.add("list");
         suggestions.add("clear");
         for (Module m : ModuleManager.getModules()) {
            suggestions.add(m.name);
         }
      } else if (args.length == 2 && !args[0].equalsIgnoreCase("list") && !args[0].equalsIgnoreCase("clear")) {
         suggestions.add("none");
         for (int code = 65; code <= 90; code++) {
            suggestions.add(String.valueOf((char) code));
         }
         suggestions.add("SPACE");
         suggestions.add("LSHIFT");
         suggestions.add("RSHIFT");
         suggestions.add("LCONTROL");
         suggestions.add("TAB");
         suggestions.add("CAPSLOCK");
         for (int i = 1; i <= 12; i++) {
            suggestions.add("F" + i);
         }
      }
      return suggestions;
   }

   @Override
   public String getName() {
      return "bind";
   }

   @Override
   public String getDescription() {
      return "Привязка клавиши к модулю (.bind <модуль> <клавиша>/none/list/clear)";
   }
}

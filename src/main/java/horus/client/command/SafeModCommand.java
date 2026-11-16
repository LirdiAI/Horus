package horus.client.command;

import horus.client.module.misc.SafeModModule;
import horus.client.util.ClientChat;

import java.util.ArrayList;
import java.util.Arrays;

public class SafeModCommand implements ClientCommand {
   private final String name;

   public SafeModCommand(String name) {
      this.name = name;
   }

   public SafeModCommand() {
      this("safemod");
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public String getDescription() {
      return "Управление безопасным режимом (.safe / .safemod [on/off/status])";
   }

   @Override
   public ArrayList<String> suggestArguments(String[] args) {
      if (args.length <= 1) {
         return new ArrayList<>(Arrays.asList("on", "off", "toggle", "status"));
      }
      return new ArrayList<>();
   }

   @Override
   public void execute(String[] args) {
      SafeModModule safeMod = SafeModModule.getInstance();
      if (args.length > 0) {
         if (args[0].equalsIgnoreCase("on")) {
            if (!safeMod.isEnabled()) {
               safeMod.setEnabled(true);
            } else {
               ClientChat.processData("&c[SafeMod] &fБезопасный режим уже включен!");
            }
            return;
         } else if (args[0].equalsIgnoreCase("off")) {
            if (safeMod.isEnabled()) {
               safeMod.setEnabled(false);
            } else {
               ClientChat.processData("&c[SafeMod] &fБезопасный режим уже выключен!");
            }
            return;
         } else if (args[0].equalsIgnoreCase("status")) {
            ClientChat.processData("&c[SafeMod] &fСтатус: " + (safeMod.isEnabled() ? "&aВКЛЮЧЕН" : "&cВЫКЛЮЧЕН"));
            ClientChat.processData("&7Режим: &e" + safeMod.mode.getSelectedOption()
               + " &7| Скрыть HUD: &e" + safeMod.hideHud.getValue()
               + " &7| Блок GUI: &e" + safeMod.lockGui.getValue()
               + " &7| Восстановление: &e" + safeMod.restoreOnDisable.getValue());
            return;
         }
      }

      safeMod.toggle();
   }
}

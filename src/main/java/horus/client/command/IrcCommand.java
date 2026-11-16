package horus.client.command;

import horus.client.util.ClientChat;
import horus.client.util.IrcClient;
import ru.dreamix.protection.api.impl.profile.ProfileAPI;
import ru.dreamix.protection.api.impl.profile.objects.RoleObject;

import java.util.ArrayList;
import java.util.Arrays;

public class IrcCommand implements ClientCommand {
   @Override
   public ArrayList<String> suggestArguments(String[] stringArray) {
      if (stringArray.length > 1) {
         return new ArrayList<>();
      }
      return new ArrayList<>(Arrays.asList("on", "off", "online", "url"));
   }

   private boolean checkState() {
      return ProfileAPI.get().getModel().getRole().priorityHigherOrEquals(RoleObject.ADMIN);
   }

   @Override
   public String getName() {
      return "irc";
   }

   @Override
   public String getDescription() {
      return "IRC чат клиента (.irc on/off/online/url, писать через @сообщение)";
   }

   @Override
   public void execute(String[] stringArray) {
      switch (stringArray.length == 0 ? "" : stringArray[0].toLowerCase()) {
         case "on":
            IrcClient.getInstance().processData2(true);
            ClientChat.processData8("&aIRC чат включён! &rСервер: &7" + IrcClient.getInstance().getServerUrl());
            ClientChat.processData8("Отправляй сообщения в чат через: &e@сообщение &7(например: @всем ку)");
            break;
         case "off":
            IrcClient.getInstance().processData2(false);
            ClientChat.processData8("&cIRC чат выключен.");
            break;
         case "online":
            if (!IrcClient.getInstance().checkState()) {
               ClientChat.processData8("IRC выключен, сначала включи через: &7.irc on");
            } else {
               IrcClient.getInstance().updateState();
            }
            break;
         case "url":
            if (stringArray.length > 1) {
               String newUrl = stringArray[1].trim();
               IrcClient.getInstance().setServerUrl(newUrl);
               ClientChat.processData8("&aАдрес IRC сервера успешно установлен на: &7" + newUrl);
            } else {
               ClientChat.processData8("Текущий сервер IRC: &7" + IrcClient.getInstance().getServerUrl());
               ClientChat.processData8("Чтобы сменить, используй: &7.irc url <адрес>");
            }
            break;
         default:
            boolean active = IrcClient.getInstance().checkState();
            ClientChat.processData8("Статус IRC: " + (active ? "&aВКЛЮЧЁН" : "&cВЫКЛЮЧЕН") + "&r | Сервер: &7" + IrcClient.getInstance().getServerUrl());
            ClientChat.processData8("&7.irc on &r— включить IRC чат");
            ClientChat.processData8("&7.irc off &r— выключить IRC чат");
            ClientChat.processData8("&7.irc online &r— список игроков в IRC");
            ClientChat.processData8("&7.irc url <адрес> &r— сменить сервер IRC");
            ClientChat.processData8("Писать в чат: &e@сообщение &r(например: &7@привет всем&r)");
      }
   }
}

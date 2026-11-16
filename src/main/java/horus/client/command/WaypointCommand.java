package horus.client.command;

import horus.client.util.WaypointManager;
import horus.client.util.ClientChat;
import horus.client.command.ClientCommand;
import horus.client.MinecraftAccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WaypointCommand implements MinecraftAccess, ClientCommand {
   private final String commandName;

   public WaypointCommand() {
      this("gps");
   }

   public WaypointCommand(String commandName) {
      this.commandName = commandName;
   }

   @Override
   public ArrayList<String> suggestArguments(String[] stringArray) {
      if (stringArray.length <= 1) {
         ArrayList<String> suggestions = new ArrayList<>(Arrays.asList("del", "clear", "add"));
         if (mc.player != null) {
            suggestions.add(mc.player.getBlockX() + " " + mc.player.getBlockY() + " " + mc.player.getBlockZ());
            suggestions.add(mc.player.getBlockX() + " " + mc.player.getBlockZ());
         }
         return suggestions;
      } else if (stringArray[0].equalsIgnoreCase("del") || stringArray[0].equalsIgnoreCase("remove")) {
         return WaypointManager.getInstance().getArrayList();
      } else if (stringArray[0].equalsIgnoreCase("add") && stringArray.length == 2 && mc.player != null) {
         return new ArrayList<>(Arrays.asList(mc.player.getBlockX() + " " + mc.player.getBlockY() + " " + mc.player.getBlockZ()));
      }
      return new ArrayList<>();
   }

   @Override
   public String getName() {
      return this.commandName;
   }

   @Override
   public void execute(String[] stringArray) {
      if (stringArray.length == 0) {
         ClientChat.processData8("Использование: &7." + this.commandName + " <корды>&r, &7." + this.commandName + " [название] <корды>&r, &7." + this.commandName + " del <название>&r, &7." + this.commandName + " clear");
         return;
      }

      String firstArg = stringArray[0].toLowerCase();

      // 1. Очистка всех меток: .gps clear / .gps reset
      if (firstArg.equals("clear") || firstArg.equals("reset")) {
         int count = WaypointManager.getInstance().computeInt();
         ClientChat.processData8(count == 0 ? "Меток нет." : "Удалено меток: &7" + count);
         WaypointManager.getInstance().updateState();
         return;
      }

      // 2. Удаление метки: .gps del <название> / .gps remove <название>
      if (firstArg.equals("del") || firstArg.equals("remove") || firstArg.equals("delete")) {
         if (stringArray.length < 2) {
            ClientChat.processData8("Использование: &7." + this.commandName + " del <название>");
            return;
         }
         String targetName = String.join(" ", Arrays.copyOfRange(stringArray, 1, stringArray.length));
         boolean removed = WaypointManager.getInstance().matchesCriteria(targetName);
         if (!removed && stringArray.length > 2) {
            removed = WaypointManager.getInstance().matchesCriteria(stringArray[1]);
         }
         ClientChat.processData8(
            removed ? "Метка &7" + targetName + "&r удалена." : "Метка &7" + targetName + "&r не найдена."
         );
         return;
      }

      // 3. Добавление метки: .gps <корды> / .gps [название] <корды> / .gps add ...
      List<String> rawTokens = new ArrayList<>(Arrays.asList(stringArray));
      if (firstArg.equals("add")) {
         rawTokens.remove(0);
         if (rawTokens.isEmpty()) {
            if (mc.player != null) {
               int px = mc.player.getBlockX();
               int py = mc.player.getBlockY();
               int pz = mc.player.getBlockZ();
               WaypointManager.getInstance().processData("GPS", px, py, pz);
               ClientChat.processData8("Метка &7GPS&r поставлена на &7" + px + " " + py + " " + pz);
            } else {
               ClientChat.processData8("Использование: &7." + this.commandName + " <корды>");
            }
            return;
         }
      }

      // Чистим токены от запятых, скобок, префиксов x:, y:, z:
      List<String> cleanedTokens = new ArrayList<>();
      for (String token : rawTokens) {
         String clean = token.replace(",", " ").replace("[", " ").replace("]", " ").replace(";", " ").trim();
         for (String part : clean.split("\\s+")) {
            if (!part.isEmpty()) {
               String low = part.toLowerCase();
               if (low.startsWith("x:") || low.startsWith("y:") || low.startsWith("z:")) {
                  part = part.substring(2);
               } else if (low.equals("x") || low.equals("y") || low.equals("z") || low.equals("x=") || low.equals("y=") || low.equals("z=")) {
                  continue;
               }
               if (!part.isEmpty()) {
                  cleanedTokens.add(part);
               }
            }
         }
      }

      if (cleanedTokens.isEmpty()) {
         ClientChat.processData8("Использование: &7." + this.commandName + " <корды>&r (например, &7." + this.commandName + " 100 64 200&r или &7." + this.commandName + " 100 200&r)");
         return;
      }

      // Извлекаем числа / координаты с конца списка
      List<Integer> numbers = new ArrayList<>();
      int playerX = mc.player != null ? mc.player.getBlockX() : 0;
      int playerY = mc.player != null ? mc.player.getBlockY() : 70;
      int playerZ = mc.player != null ? mc.player.getBlockZ() : 0;

      int numIdx = cleanedTokens.size() - 1;
      while (numIdx >= 0) {
         String val = cleanedTokens.get(numIdx);
         Integer parsed = parseCoord(val, playerX, playerY, playerZ, numbers.size());
         if (parsed != null) {
            numbers.add(0, parsed);
            numIdx--;
         } else {
            break;
         }
      }

      String waypointName = "GPS";
      if (numIdx >= 0) {
         waypointName = String.join(" ", cleanedTokens.subList(0, numIdx + 1));
      }

      if (numbers.size() >= 3) {
         int x = numbers.get(numbers.size() - 3);
         int y = numbers.get(numbers.size() - 2);
         int z = numbers.get(numbers.size() - 1);
         WaypointManager.getInstance().processData(waypointName, x, y, z);
         ClientChat.processData8("Метка &7" + waypointName + "&r поставлена на &7" + x + " " + y + " " + z);
      } else if (numbers.size() == 2) {
         int x = numbers.get(0);
         int y = playerY;
         int z = numbers.get(1);
         WaypointManager.getInstance().processData(waypointName, x, y, z);
         ClientChat.processData8("Метка &7" + waypointName + "&r поставлена на &7" + x + " " + y + " " + z);
      } else {
         ClientChat.processData8("Не удалось определить координаты. Пример: &7." + this.commandName + " 100 64 200&r или &7." + this.commandName + " База 100 200");
      }
   }

   private static Integer parseCoord(String token, int px, int py, int pz, int alreadyFoundCount) {
      // Поддержка относительных координат ~ и ~10 / ~-5
      if (token.equals("~")) {
         // alreadyFoundCount: 0 -> Z, 1 -> Y, 2 -> X (так как парсим с конца)
         if (alreadyFoundCount == 0) return pz;
         if (alreadyFoundCount == 1) return py;
         return px;
      }
      if (token.startsWith("~")) {
         try {
            int delta = Integer.parseInt(token.substring(1));
            if (alreadyFoundCount == 0) return pz + delta;
            if (alreadyFoundCount == 1) return py + delta;
            return px + delta;
         } catch (NumberFormatException e) {
            return null;
         }
      }
      try {
         return Integer.parseInt(token);
      } catch (NumberFormatException e) {
         return null;
      }
   }

   @Override
   public String getDescription() {
      return this.commandName.equals("way")
         ? "Алиас для .gps"
         : "GPS метки (.gps <корды> / .gps [название] <корды> / del / clear)";
   }
}

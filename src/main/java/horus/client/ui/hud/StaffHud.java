package horus.client.ui.hud;

import horus.client.render.FontText;
import horus.client.render.ThemeColors;
import horus.client.ui.UiTheme;

import horus.client.util.Animation;

import horus.client.util.Easings;
import horus.client.render.FontFace;
import horus.client.render.Render2D;
import horus.client.storage.StaffStore;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

public final class StaffHud {
   public static final Pattern pattern = Pattern.compile("^\\w{3,16}$");
   public static final Pattern pattern2 = Pattern.compile(
      ".*(mod|der|adm|help|wne|мод|хелп|помо|адм|владе|отри|таф|taf|curat|курато|dev|раз|supp|сапп|yt|ютуб).*"
   );
   private static final String[][] strValArray = new String[][]{
      {"karandash", "Helper", "26:08", "ι"}, {"smdxyxz", "Tester", "08:56", "κ"}, {"Кирейко", "Moder", "10:34", "ι"}
   };
   private static final Map<String, StaffHud.SubContext2> map = new LinkedHashMap<>();
   private static final HashMap<String, Long> map2 = new HashMap<>();
   private static final List<StaffHud.SubContext2> list = new ArrayList<>();
   private static final Animation animator = new Animation();
   private static final Animation animator2 = new Animation();
   private static final Animation animator3 = new Animation();
   private static final Animation animator4 = new Animation();
   private static float floatVal;
   private static float floatVal2;
   private static long timeMillis;
   private static final IdentityHashMap<PlayerListEntry, StaffHud.DataRecord> map3 = new IdentityHashMap<>();
   private static final HashSet<String> set = new HashSet<>();
   private static Object object;
   private static long timeMillis2;

   private static float computeFloat(StaffHud.SubContext2 local3482_0) {
      return local3482_0.strVal2.isEmpty() ? 0.0F : FontText.computeFloat(FontFace.SEMI, local3482_0.strVal2, 6.0F) + 3.0F;
   }

   private StaffHud() {
   }

   private static String formatText(PlayerListEntry playerListEntry, String local3485_1, Team newTeam) {
      if (!StaffStore.matchesCriteria3(local3485_1) && !pattern.matcher(local3485_1).matches()) {
         return null;
      }

      String local3485_3 = newTeam == null ? "" : newTeam.getPrefix().getString().trim();
      String local3485_4 = !local3485_3.isEmpty()
         ? local3485_3
         : (playerListEntry.getDisplayName() == null ? "" : playerListEntry.getDisplayName().getString().replace(local3485_1, "").trim());
      return StaffStore.matchesCriteria3(local3485_1)
            || StaffStore.matchesCriteria2(local3485_4)
            || !local3485_4.isEmpty() && pattern2.matcher(local3485_4.toLowerCase(Locale.ROOT)).matches()
         ? local3485_4
         : null;
   }

   static {
      animator2.resetTo(1.0);
   }

   private static void processData(
      MinecraftClient minecraftClient, float local3487_1, float local3487_2, float local3487_3, StaffHud.SubContext2 local3487_4
   ) {
      float local3487_5 = local3487_4.visibilityAnimation.floatValue() * local3487_3;
      if (!(local3487_5 <= 0.001F)) {
         float local3487_6 = HudStyle.computeFloat2(local3487_5);
         float local3487_7 = computeFloat4(local3487_4);
         float local3487_8 = HudStyle.computeFloat(6.0F, local3487_7);
         float local3487_9 = computeFloat2(local3487_4);
         float local3487_10 = local3487_1 - local3487_6;
         float local3487_11 = local3487_1 + animator4.floatValue() + 2.0F - local3487_6;
         local3487_9 = Math.max(local3487_9, animator3.floatValue() - animator4.floatValue() - 2.0F);
         float local3487_12 = HudStyle.computeFloat3(local3487_2);
         HudStyle.processData6(local3487_11, local3487_2, local3487_9, 11.0F, local3487_5);
         String local3487_13 = formatText2(minecraftClient, local3487_4.strVal);
         float local3487_14 = local3487_2 + 5.5F - 4.0F;
         int local3487_15 = UiTheme.computeInt(-1, local3487_5);
         Render2D.processData26(local3487_13, local3487_11 + 4.0F, local3487_14, 8.0F, 8.0F, 3.5F, 0.25F, 0.125F, 0.125F, 0.25F, 0.25F, local3487_15);
         Render2D.processData26(local3487_13, local3487_11 + 4.0F, local3487_14, 8.0F, 8.0F, 3.5F, 0.25F, 0.625F, 0.125F, 0.75F, 0.25F, local3487_15);
         float local3487_16 = local3487_11 + 4.0F + 8.0F + 3.0F;
         if (local3487_4.text2 != null) {
            FontText.processData5(
               FontFace.SEMI,
               local3487_4.text2,
               local3487_16,
               local3487_12,
               6.0F,
               UiTheme.computeInt(HudStyle.computeInt4(1.0F), local3487_5)
            );
         } else if (!local3487_4.strVal2.isEmpty()) {
            FontText.processData3(
               FontFace.SEMI,
               local3487_4.strVal2,
               local3487_16,
               local3487_12,
               6.0F,
               UiTheme.computeInt(HudStyle.computeInt2(1.0F), local3487_5)
            );
         }

         FontText.processData3(
            FontFace.SEMI,
            local3487_4.strVal,
            local3487_16 + computeFloat(local3487_4),
            local3487_12,
            6.0F,
            UiTheme.computeInt(HudStyle.computeInt4(1.0F), local3487_5)
         );
         HudStyle.processData6(local3487_10, local3487_2, local3487_8, 11.0F, local3487_5);
         float local3487_17 = local3487_10 + 2.0F + 6.0F + 1.0F;
         float local3487_18 = local3487_17 + 0.25F;
         HudStyle.processData(local3487_17, local3487_2 + 5.5F, local3487_5, 3.0F);
         ThemeColors.processData(
            local3487_4.strVal4,
            local3487_10 + (local3487_18 - local3487_10 - 6.0F) / 2.0F + 3.0F,
            local3487_2 + 5.5F,
            6.0F,
            UiTheme.computeInt(HudStyle.computeInt2(1.0F), local3487_5)
         );
         FontText.processData3(
            FontFace.SEMI,
            local3487_4.strVal3,
            local3487_18 + (local3487_10 + local3487_8 - local3487_18 - local3487_7) / 2.0F,
            local3487_12,
            6.0F,
            UiTheme.computeInt(HudStyle.computeInt4(1.0F), local3487_5)
         );
      }
   }

   private static float computeFloat2(StaffHud.SubContext2 local3488_0) {
      return 15.0F + computeFloat(local3488_0) + FontText.computeFloat(FontFace.SEMI, local3488_0.strVal, 6.0F) + 4.0F;
   }

   private static void updateStaffList(MinecraftClient minecraftClient) {
      list.clear();
      long local3490_1 = System.currentTimeMillis();
      if (local3490_1 - timeMillis >= 250L) {
         processData2(minecraftClient);
         timeMillis = local3490_1;
      }

      boolean local3490_3 = map.values().stream().anyMatch(local3484_0 -> local3484_0.visible && !local3484_0.flag);
      if (!local3490_3 && HudRenderContext.checkState()) {
         for (String[] stringArray : strValArray) {
            StaffHud.SubContext2 local3490_8 = map.computeIfAbsent(stringArray[0], StaffHud.SubContext2::new);
            local3490_8.strVal2 = stringArray[1];
            local3490_8.text2 = null;
            local3490_8.strVal3 = stringArray[2];
            local3490_8.strVal4 = stringArray[3];
            local3490_8.flag = true;
            local3490_8.visible = true;
         }
      }

      for (StaffHud.SubContext2 local3490_5 : map.values()) {
         if (local3490_5.visible || local3490_5.visibilityAnimation.floatValue() > 0.001) {
            list.add(local3490_5);
         }
      }

      map.entrySet()
         .removeIf(
            local3489_0 -> !local3489_0.getValue().visible
               && local3489_0.getValue().visibilityAnimation.floatValue() <= 0.001
               && local3489_0.getValue().visibilityAnimation.isFinished()
         );

      for (StaffHud.SubContext2 local3490_5 : list) {
         local3490_5.visibilityAnimation.update();
         local3490_5.visibilityAnimation.animateTo(local3490_5.visible ? 1.0 : 0.0, 0.6, Easings.EASE_OUT_QUART);
      }
   }

   public static float computeFloat3(float local3491_0, float local3491_1) {
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      updateStaffList(minecraftClient);
      float local3491_3 = HudStyle.computeFloat5("Staffs");
      float local3491_4 = 0.0F;
      float local3491_5 = 0.0F;
      float local3491_6 = 13.0F;

      for (StaffHud.SubContext2 local3491_8 : list) {
         local3491_4 = Math.max(local3491_4, HudStyle.computeFloat(6.0F, computeFloat4(local3491_8)));
         local3491_5 = Math.max(local3491_5, computeFloat2(local3491_8));
         local3491_6 += 14.0F * local3491_8.visibilityAnimation.floatValue();
      }

      float local3491_7 = local3491_4 + (local3491_4 > 0.0F && local3491_5 > 0.0F ? 2.0F : 0.0F) + local3491_5;
      float local3491_8 = Math.max(local3491_3, Math.max(local3491_7, 1.0F));
      boolean local3491_9 = !list.isEmpty() || HudRenderContext.checkState();
      animator.update();
      animator.animateTo(local3491_9 ? 1.0 : 0.0, 0.4, Easings.EASE_OUT_QUART);
      animator2.update();
      if (!list.isEmpty()) {
         animator2.animateTo(Math.max(local3491_8, 1.0F), 0.6, Easings.EASE_OUT_QUART);
      }

      animator3.update();
      if (!list.isEmpty()) {
         animator3.animateTo(Math.max(local3491_8, 1.0F), 0.6, Easings.EASE_OUT_QUART);
      }

      animator4.update();
      if (!list.isEmpty()) {
         animator4.animateTo(Math.max(local3491_4, 0.0F), 0.6, Easings.EASE_OUT_QUART);
      }

      floatVal = animator2.floatValue();
      floatVal2 = Math.max(local3491_6, 1.0F);
      float local3491_10 = animator.floatValue();
      if (local3491_10 <= 0.001F) {
         return 0.0F;
      }

      float[] floatArray = HudManager.computeFloat5("staffs", local3491_0 - floatVal, local3491_1);
      float local3491_12 = floatArray[0];
      float local3491_13 = floatArray[1];
      HudManager.processData9("staffs", local3491_12, local3491_13, floatVal, floatVal2);
      HudRenderContext.processData4(local3491_12 + floatVal / 2.0F, local3491_13 + floatVal2 / 2.0F, local3491_10);
      HudStyle.processData7(local3491_12, local3491_13, Math.max(animator3.floatValue(), 1.0F), local3491_10, HudIcons.STAFF, "Staffs");
      float local3491_14 = local3491_13 + 15.0F;

      for (StaffHud.SubContext2 local3491_16 : list) {
         processData(minecraftClient, local3491_12, local3491_14, local3491_10, local3491_16);
         local3491_14 += 13.0F * local3491_16.visibilityAnimation.floatValue();
      }

      HudRenderContext.updateState();
      return floatVal2 * local3491_10;
   }

   private static float computeFloat4(StaffHud.SubContext2 local3492_0) {
      return FontText.computeFloat(FontFace.SEMI, local3492_0.strVal3, 6.0F);
   }

   private static void processData2(MinecraftClient minecraftClient) {
      for (StaffHud.SubContext2 local3493_2 : map.values()) {
         local3493_2.visible = false;
      }

      if (minecraftClient.getNetworkHandler() != null && minecraftClient.world != null) {
         long local3493_1 = System.currentTimeMillis();
         if (minecraftClient.world != object || local3493_1 - timeMillis2 >= 5000L) {
            map3.clear();
            object = minecraftClient.world;
            timeMillis2 = local3493_1;
         }

         set.clear();
         Collection<PlayerListEntry> playerList = minecraftClient.getNetworkHandler().getPlayerList();
         if (map3.size() > playerList.size() * 2 + 16) {
            map3.clear();
         }

         for (PlayerListEntry playerListEntry : playerList) {
            String local3493_6 = playerListEntry.getProfile().name();
            if (local3493_6 != null && !local3493_6.isEmpty()) {
               Team scoreHolderTeam = minecraftClient.world.getScoreboard().getScoreHolderTeam(local3493_6);
               Object local3493_8 = scoreHolderTeam == null ? null : scoreHolderTeam.getPrefix();
               Object displayName = playerListEntry.getDisplayName();
               StaffHud.DataRecord local3493_10 = map3.get(playerListEntry);
               if (local3493_10 == null
                  || local3493_10.team != scoreHolderTeam
                  || local3493_10.prefix != local3493_8
                  || local3493_10.display != displayName
                  || !local3493_6.equals(local3493_10.name)) {
                  local3493_10 = new StaffHud.DataRecord(
                     local3493_6, scoreHolderTeam, local3493_8, displayName, formatText(playerListEntry, local3493_6, scoreHolderTeam)
                  );
                  map3.put(playerListEntry, local3493_10);
               }

               String local3493_11 = local3493_10.rankText;
               if (local3493_11 != null) {
                  set.add(local3493_6);
                  long local3493_12 = map2.computeIfAbsent(local3493_6, local3494_0 -> System.currentTimeMillis());
                  StaffHud.SubContext2 local3493_14 = map.computeIfAbsent(local3493_6, StaffHud.SubContext2::new);
                  local3493_14.strVal2 = local3493_11.isEmpty() ? "Staff" : local3493_11;
                  local3493_14.text2 = scoreHolderTeam != null && !scoreHolderTeam.getPrefix().getString().trim().isEmpty() ? scoreHolderTeam.getPrefix() : null;
                  local3493_14.strVal3 = String.format(
                     "%02d:%02d", (System.currentTimeMillis() - local3493_12) / 60000L, (System.currentTimeMillis() - local3493_12) / 1000L % 60L
                  );
                  local3493_14.strVal4 = playerListEntry.getGameMode() == GameMode.SPECTATOR ? "ι" : "κ";
                  local3493_14.flag = false;
                  local3493_14.visible = true;
               }
            }
         }

         map2.keySet().retainAll(set);
      } else {
         map2.clear();
      }
   }

   private static String formatText2(MinecraftClient minecraftClient, String local3495_1) {
      if (minecraftClient.getNetworkHandler() != null) {
         PlayerListEntry playerListEntry = minecraftClient.getNetworkHandler().getPlayerListEntry(local3495_1);
         if (playerListEntry != null) {
            return playerListEntry.getSkinTextures().body().texturePath().toString();
         }
      }

      UUID uUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + local3495_1).getBytes(StandardCharsets.UTF_8));
      return DefaultSkinHelper.getSkinTextures(uUID).body().texturePath().toString();
   }

   private record DataRecord(String name, Object team, Object prefix, Object display, String rankText) {
   }

   private static final class SubContext2 extends DraggableHudElement {
      final String strVal;
      String strVal2 = "";
      Text text2;
      String strVal3 = "";
      String strVal4 = "κ";
      boolean flag;

      SubContext2(String local3505_1) {
         this.strVal = local3505_1;
      }
   }
}

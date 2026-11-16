package horus.client.ui.hud;

import horus.client.render.FontText;
import horus.client.util.ItemCooldownAccess;
import horus.client.render.ItemRenderCache;
import horus.client.util.PotionPreset;
import horus.client.ui.UiTheme;

import horus.client.util.Animation;

import horus.client.util.Colors;
import horus.mixin.ContainerComponentMixin;
import horus.client.util.Easings;
import horus.client.render.FontFace;
import horus.client.util.ItemDrawOptions;
import horus.client.module.misc.PotionCombinerModule;
import horus.client.module.player.ServerHelperModule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class ServerKeybindsHud {
   private static final Map<String, ServerKeybindsHud.SubContext> map = new HashMap<>();
   private static final List<ServerKeybindsHud.SubContext> list = new ArrayList<>();
   private static final Animation animation = new Animation();
   private static float floatVal;
   private static float floatVal2;

   private static float computeFloat(ServerKeybindsHud.SubContext local6665_0) {
      float local6665_1 = local6665_0.animation.floatValue();
      float local6665_2 = FontText.computeFloat(FontFace.SEMI, local6665_0.strVal, 6.0F);
      float local6665_3 = local6665_0.strVal2.isEmpty() ? 0.0F : FontText.computeFloat(FontFace.SEMI, local6665_0.strVal2, 6.0F);
      return local6665_2 * (1.0F - local6665_1) + local6665_3 * local6665_1;
   }

   private static void processData(MinecraftClient minecraftClient, ServerKeybindsHud.SubContext local6666_1, ServerHelperModule.SubContext local6666_2) {
      int local6666_3 = 0;
      ItemStack itemStack = ItemStack.EMPTY;

      for (int index = 0; index < minecraftClient.player.getInventory().size(); index++) {
         ItemStack stack = minecraftClient.player.getInventory().getStack(index);
         if (local6666_2.matchesCriteria2(stack)) {
            local6666_3 += stack.getCount();
            if (itemStack.isEmpty()) {
               itemStack = stack;
            }
         }

         if (stack.get(DataComponentTypes.BUNDLE_CONTENTS) != null) {
            for (ItemStack itemStack2 : stack.get(DataComponentTypes.BUNDLE_CONTENTS).iterate()) {
               if (local6666_2.matchesCriteria2(itemStack2)) {
                  local6666_3 += itemStack2.getCount();
                  if (itemStack.isEmpty()) {
                     itemStack = itemStack2;
                  }
               }
            }
         }

         if (Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock && stack.get(DataComponentTypes.CONTAINER) != null) {
            for (ItemStack itemStack3 : ((ContainerComponentMixin)(Object)stack.get(DataComponentTypes.CONTAINER)).getItems()) {
               if (local6666_2.matchesCriteria2(itemStack3)) {
                  local6666_3 += itemStack3.getCount();
                  if (itemStack.isEmpty()) {
                     itemStack = itemStack3;
                  }
               }

               if (itemStack3.get(DataComponentTypes.BUNDLE_CONTENTS) != null) {
                  for (ItemStack itemStack4 : itemStack3.get(DataComponentTypes.BUNDLE_CONTENTS).iterate()) {
                     if (local6666_2.matchesCriteria2(itemStack4)) {
                        local6666_3 += itemStack4.getCount();
                        if (itemStack.isEmpty()) {
                           itemStack = itemStack4;
                        }
                     }
                  }
               }
            }
         }
      }

      if (!itemStack.isEmpty()) {
         local6666_1.itemStack6 = itemStack.copy();
      }

      local6666_1.intVal = local6666_3;
      local6666_1.flag = false;
      ItemStack itemStack5 = !itemStack.isEmpty()
         ? itemStack
         : (local6666_2.getItem2() != null ? new ItemStack(local6666_2.getItem2()) : ItemStack.EMPTY);
      if (!itemStack5.isEmpty() && minecraftClient.player.getItemCooldownManager().getCooldownProgress(itemStack5, 0.0F) > 0.001F) {
         int local6666_6 = ((ItemCooldownAccess)minecraftClient.player.getItemCooldownManager()).remainingCooldownTicks(itemStack5);
         local6666_1.floatVal3 = minecraftClient.player.getItemCooldownManager().getCooldownProgress(itemStack5, 0.0F);
         int local6666_7 = Math.max(0, Math.round(local6666_6 / 2.0F));
         local6666_1.strVal2 = local6666_7 / 10 + "." + local6666_7 % 10 + "с";
         local6666_1.flag = true;
         if (itemStack.isEmpty()) {
            local6666_1.itemStack6 = itemStack5.copy();
         }
      }

      local6666_1.visible = local6666_3 > 0 || local6666_1.flag;
   }

   private ServerKeybindsHud() {
   }

   private static float computeFloat2(ServerKeybindsHud.SubContext local6671_0) {
      return HudStyle.computeFloat(8.0F + (local6671_0.list2 == null ? 0.0F : (local6671_0.list2.size() - 1) * 9.5F), computeFloat(local6671_0));
   }

   public static float computeFloat3(MinecraftClient minecraftClient, float local6675_1, float local6675_2) {
      processData3(minecraftClient);
      float local6675_3 = 0.0F;
      float local6675_4 = 0.0F;
      int local6675_5 = 1;
      boolean local6675_6 = false;

      for (ServerKeybindsHud.SubContext local6675_8 : list) {
         float local6675_9 = computeFloat2(local6675_8);
         if (local6675_4 > 0.0F && local6675_4 + local6675_9 > 150.0F) {
            local6675_5++;
            local6675_4 = 0.0F;
         }

         local6675_8.sortIndex = local6675_5 - 1;
         local6675_8.yOffset = local6675_4;
         local6675_4 += local6675_9 + 2.0F;
         local6675_3 = Math.max(local6675_3, local6675_4 - 2.0F);
         local6675_6 = true;
      }

      float local6675_7 = local6675_6 ? 2.0F + local6675_5 * 11.0F + (local6675_5 - 1) * 2.0F : 0.0F;
      animation.update();
      animation.animateTo(list.isEmpty() ? 0.0 : 1.0, 0.4, Easings.EASE_OUT_QUART);
      if (local6675_6) {
         floatVal = Math.max(local6675_3, 1.0F);
         floatVal2 = Math.max(local6675_7, 1.0F);
      }

      float local6675_8 = animation.floatValue();
      if (local6675_8 <= 0.001F) {
         return 0.0F;
      }

      float[] floatArray = HudManager.computeFloat5("serverbinds", local6675_1, local6675_2);
      float local6675_10 = floatArray[0] - floatVal / 2.0F;
      float local6675_11 = floatArray[1] - floatVal2;
      HudManager.processData6("serverbinds", local6675_10, local6675_11, floatVal, floatVal2, floatArray[0], floatArray[1]);
      HudRenderContext.processData4(local6675_10 + floatVal / 2.0F, local6675_11 + floatVal2 / 2.0F, local6675_8);

      for (ServerKeybindsHud.SubContext local6675_13 : list) {
         float local6675_14 = local6675_13.visibilityAnimation.floatValue() * local6675_8;
         if (!(local6675_14 <= 0.001F)) {
            processData2(local6675_10 + local6675_13.yOffset, local6675_11 + local6675_13.sortIndex * 13.0F, local6675_14, local6675_13);
         }
      }

      HudRenderContext.updateState();
      return floatVal2 * local6675_8;
   }

   private static void processData2(float local6676_0, float local6676_1, float local6676_2, ServerKeybindsHud.SubContext local6676_3) {
      float local6676_4 = computeFloat2(local6676_3);
      float local6676_5 = computeFloat(local6676_3);
      HudStyle.processData6(local6676_0, local6676_1, local6676_4, 11.0F, local6676_2);
      float local6676_6 = local6676_0 + 2.0F + 8.0F + (local6676_3.list2 == null ? 0.0F : (local6676_3.list2.size() - 1) * 9.5F) + 1.0F;
      float local6676_7 = local6676_6 + 0.25F;
      HudStyle.processData(local6676_6, local6676_1 + 5.5F, local6676_2, 3.0F);
      float local6676_8 = local6676_0
         + (local6676_7 - local6676_0 - 8.0F - (local6676_3.list2 == null ? 0.0F : (local6676_3.list2.size() - 1) * 9.5F)) / 2.0F;
      if (local6676_3.list2 != null) {
         for (int index = 0; index < local6676_3.list2.size(); index++) {
            ItemRenderCache.processData(
               local6676_3.list2.get(index),
               Math.round((local6676_8 + index * 9.5F) * 2.0F) / 2.0F,
               Math.round((local6676_1 + 5.5F - 4.0F) * 2.0F) / 2.0F,
               8.0F,
               ItemDrawOptions.noDecorations(local6676_2)
            );
         }
      } else if (local6676_3.itemStack6 != null && !local6676_3.itemStack6.isEmpty()) {
         float local6676_9 = Math.round(local6676_8 * 2.0F) / 2.0F;
         float local6676_10 = Math.round((local6676_1 + 5.5F - 4.0F) * 2.0F) / 2.0F;
         ItemRenderCache.processData(local6676_3.itemStack6, local6676_9, local6676_10, 8.0F, ItemDrawOptions.noDecorations(local6676_2));
      }

      if (local6676_3.intVal > 0) {
         String local6676_9 = Integer.toString(local6676_3.intVal);
         float local6676_10 = 5.0F;
         float local6676_11 = FontText.computeFloat(FontFace.SEMI, local6676_9, local6676_10);
         float local6676_12 = local6676_8 + 8.0F - local6676_11;
         float local6676_13 = local6676_1 + 5.5F + 4.0F - local6676_10;
         int local6676_14 = Colors.computeInt2(HudStyle.computeInt4(1.0F), -16777216, 0.1F);
         FontText.processData3(
            FontFace.SEMI, local6676_9, local6676_12, local6676_13, local6676_10, UiTheme.computeInt(local6676_14, local6676_2)
         );
      }

      float local6676_9 = local6676_7 + (local6676_0 + local6676_4 - local6676_7 - local6676_5) / 2.0F;
      float local6676_10 = HudStyle.computeFloat3(local6676_1);
      float local6676_11 = local6676_3.animation.floatValue();
      if (local6676_11 < 0.999F) {
         FontText.processData3(
            FontFace.SEMI,
            local6676_3.strVal,
            local6676_9,
            local6676_10,
            6.0F,
            UiTheme.computeInt(HudStyle.computeInt4(1.0F), local6676_2 * (1.0F - local6676_11))
         );
      }

      if (local6676_11 > 0.001F && !local6676_3.strVal2.isEmpty()) {
         int local6676_12 = Colors.computeInt2(-16711936, -65536, local6676_3.floatVal3);
         FontText.processData3(
            FontFace.SEMI,
            local6676_3.strVal2,
            local6676_9,
            local6676_10,
            6.0F,
            UiTheme.computeInt(local6676_12, local6676_2 * local6676_11)
         );
      }
   }

   private static void processData3(MinecraftClient minecraftClient) {
      list.clear();
      map.values().forEach(local6670_0 -> local6670_0.visible = false);
      if (minecraftClient.player != null) {
         for (ServerHelperModule.SubContext local6677_2 : ServerHelperModule.getInstance().getArrayList()) {
            if (local6677_2.getKeybindSetting().isBound() && local6677_2.getKeybindSetting().isVisible()) {
               ServerKeybindsHud.SubContext local6677_3 = map.computeIfAbsent(local6677_2.getKeybindSetting().getKeyName(), ServerKeybindsHud.SubContext::new);
               processData(minecraftClient, local6677_3, local6677_2);
            }
         }

         for (PotionPreset.SubContext local6677_2 : PotionPreset.getInstance().list) {
            if (local6677_2.flag && local6677_2.keybindSetting.isBound() && PotionCombinerModule.getInstance().isEnabled()) {
               processData4(
                  minecraftClient,
                  map.computeIfAbsent(
                     "combo:" + local6677_2.strVal + ":" + local6677_2.keybindSetting.getKeyName(),
                     local6673_1 -> new ServerKeybindsHud.SubContext(local6677_2.keybindSetting.getKeyName())
                  ),
                  local6677_2
               );
            }
         }
      }

      boolean local6677_1 = map.values().stream().anyMatch(local6678_0 -> local6678_0.visible);
      if (!local6677_1 && HudRenderContext.checkState()) {
         Object[][] objectArrayArray = new Object[][]{
            {"R", Items.NETHERITE_SCRAP, 4}, {"F", Items.ENDER_EYE, 16}, {"V", Items.PHANTOM_MEMBRANE, 8}, {"X", Items.FIRE_CHARGE, 12}
         };

         for (Object[] objectArray : objectArrayArray) {
            ServerKeybindsHud.SubContext local6677_7 = map.computeIfAbsent(
               "preview:" + objectArray[0], local6672_1 -> new ServerKeybindsHud.SubContext((String)objectArray[0])
            );
            local6677_7.itemStack6 = new ItemStack((Item)objectArray[1]);
            local6677_7.intVal = (Integer)objectArray[2];
            local6677_7.flag = false;
            local6677_7.visible = true;
         }
      }

      for (ServerKeybindsHud.SubContext local6677_3 : map.values()) {
         if (local6677_3.visible || local6677_3.visibilityAnimation.floatValue() > 0.001) {
            list.add(local6677_3);
         }
      }

      map.entrySet()
         .removeIf(
            local6680_0 -> !local6680_0.getValue().visible
               && local6680_0.getValue().visibilityAnimation.floatValue() <= 0.001
               && local6680_0.getValue().visibilityAnimation.isFinished()
         );
      list.sort(
         Comparator.<ServerKeybindsHud.SubContext>comparingInt(local6674_0 -> local6674_0.flag ? 1 : 0)
            .thenComparingDouble(local6667_0 -> -(8.0F + FontText.computeFloat(FontFace.SEMI, local6667_0.strVal, 6.0F)))
      );

      for (ServerKeybindsHud.SubContext local6677_3 : list) {
         local6677_3.visibilityAnimation.update();
         local6677_3.visibilityAnimation.animateTo(local6677_3.visible ? 1.0 : 0.0, 0.6, Easings.EASE_OUT_QUART);
         local6677_3.animation.update();
         local6677_3.animation.animateTo(local6677_3.flag ? 1.0 : 0.0, 0.35, Easings.EASE_OUT_QUART);
      }
   }

   private static void processData4(MinecraftClient minecraftClient, ServerKeybindsHud.SubContext local6679_1, PotionPreset.SubContext local6679_2) {
      int local6679_3 = 0;

      for (int index = 0; index < minecraftClient.player.getInventory().size(); index++) {
         ItemStack stack = minecraftClient.player.getInventory().getStack(index);
         if (!stack.isEmpty()) {
            for (ServerHelperModule.SubContext local6679_7 : ServerHelperModule.getInstance().getArrayList()) {
               if (local6679_7.getItem2() == null
                  && local6679_7.matchesCriteria2(stack)
                  && !local6679_2.list2.stream().noneMatch(local6669_1 -> local6669_1.strVal3.equals(local6679_7.getKeybindSetting().name))) {
                  local6679_3 += stack.getCount();
                  break;
               }
            }
         }
      }

      local6679_1.list2 = new ArrayList<>();

      for (PotionPreset.SubContext2 local6679_5 : local6679_2.list2) {
         local6679_1.list2.add(PotionPreset.getItemStack(local6679_5.strVal3));
      }

      local6679_1.itemStack6 = local6679_1.list2.isEmpty() ? ItemStack.EMPTY : local6679_1.list2.get(0);
      local6679_1.intVal = local6679_3;
      local6679_1.flag = false;
      local6679_1.visible = local6679_3 > 0;
   }

   private static final class SubContext extends DraggableHudElement {
      final String strVal;
      ItemStack itemStack6;
      ArrayList<ItemStack> list2;
      int intVal;
      boolean flag;
      float floatVal3;
      String strVal2 = "";
      final Animation animation = new Animation();

      SubContext(String local6682_1) {
         this.strVal = local6682_1;
      }
   }
}

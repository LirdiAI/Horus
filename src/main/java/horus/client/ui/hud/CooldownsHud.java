package horus.client.ui.hud;

import horus.client.render.FontText;
import horus.client.util.GuiScale;
import horus.client.util.ItemCooldownAccess;
import horus.client.render.ItemRenderCache;
import horus.client.ui.UiTheme;

import horus.client.util.Animation;

import horus.client.util.Easings;
import horus.client.render.FontFace;
import horus.client.module.render.InterfaceModule;
import horus.client.util.ItemDrawOptions;
import horus.client.render.Render2D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public final class CooldownsHud {
   private static final Item[] itemArray = new Item[]{
      Items.ENDER_EYE,
      Items.ENDER_PEARL,
      Items.SUGAR,
      Items.MACE,
      Items.ENCHANTED_GOLDEN_APPLE,
      Items.TRIDENT,
      Items.CROSSBOW,
      Items.DRIED_KELP,
      Items.NETHERITE_SCRAP
   };
   private static final CooldownsHud.SubContext nested0531 = new CooldownsHud.SubContext(Items.ENDER_EYE);
   private static long timeMillis;
   private static int intVal;
   private static final Map<Item, CooldownsHud.SubContext> map = new LinkedHashMap<>();
   private static final Map<Item, CooldownsHud.SubContext2> map2 = new LinkedHashMap<>();
   private static final List<CooldownsHud.SubContext> list = new ArrayList<>();
   private static final Animation animator = new Animation();
   private static final Animation animator2 = new Animation();
   private static final Animation animator3 = new Animation();
   private static final Animation animator4 = new Animation();
   private static float floatVal;
   private static float floatVal2;

   static float computeFloat(Item item) {
      CooldownsHud.SubContext2 local7670_1 = map2.get(item);
      return local7670_1 == null ? 0.0F : Math.max(0.0F, local7670_1.floatVal5 - (float)(System.currentTimeMillis() - local7670_1.timeMillis2) / 1000.0F);
   }

   private static TextColor getTextColor(ItemStack itemStack) {
      TextColor[] textColorArray = new TextColor[]{null};
      itemStack.getName().visit((style, local7674_2) -> {
         if (textColorArray[0] == null && style.getColor() != null && !local7674_2.isBlank()) {
            textColorArray[0] = style.getColor();
         }

         return Optional.empty();
      }, Style.EMPTY);
      return textColorArray[0] != null ? textColorArray[0] : TextColor.fromFormatting(itemStack.getRarity().getFormatting());
   }

   private static void processData(CooldownsHud.SubContext local7673_0, int local7673_1) {
      if (local7673_1 != local7673_0.intVal2) {
         local7673_0.intVal2 = local7673_1;
         int local7673_2 = (local7673_1 + 9) / 10;
         int local7673_3 = local7673_2 / 60;
         int local7673_4 = local7673_2 % 60;
         local7673_0.strVal2 = local7673_3 > 0 ? local7673_3 + "м " + local7673_4 + "с" : local7673_4 + "с";
         local7673_0.floatVal3 = FontText.computeFloat(FontFace.SEMI_BOLD, local7673_0.strVal2, 6.0F);
      }
   }

   private static ItemStack getItemStack(MinecraftClient minecraftClient, Item item, CooldownsHud.SubContext2 local7675_2) {
      int changeCount = minecraftClient.player.getInventory().getChangeCount();
      long local7675_4 = System.currentTimeMillis();
      if (local7675_2.itemStack4 == null
         || local7675_2.object != minecraftClient.player
         || local7675_2.intVal3 != changeCount
         || local7675_4 - local7675_2.timeMillis3 >= 1000L) {
         local7675_2.itemStack4 = getItemStack2(minecraftClient, item, local7675_2.strVal3);
         local7675_2.object = minecraftClient.player;
         local7675_2.intVal3 = changeCount;
         local7675_2.timeMillis3 = local7675_4;
      }

      return local7675_2.itemStack4;
   }

   private CooldownsHud() {
   }

   public static void processData2(String local7680_0, Item item, float local7680_2) {
      CooldownsHud.SubContext2 local7680_3 = map2.get(item);
      if (local7680_3 == null || !((float)(System.currentTimeMillis() - local7680_3.timeMillis2) < local7680_3.floatVal5 * 1000.0F)) {
         CooldownsHud.SubContext2 local7680_4 = new CooldownsHud.SubContext2();
         local7680_4.strVal3 = local7680_0;
         local7680_4.timeMillis2 = System.currentTimeMillis();
         local7680_4.floatVal5 = local7680_2;
         map2.put(item, local7680_4);
      }
   }

   private static void processData3(MinecraftClient minecraftClient) {
      list.clear();
      map.values().forEach(local7692_0 -> local7692_0.visible = false);
      if (minecraftClient.player != null) {
         for (int index = 0; index < minecraftClient.player.getInventory().size(); index++) {
            processData4(minecraftClient, minecraftClient.player.getInventory().getStack(index));
         }

         processData4(minecraftClient, minecraftClient.player.getOffHandStack());
         map2.values().removeIf(local7687_0 -> (float)(System.currentTimeMillis() - local7687_0.timeMillis2) >= local7687_0.floatVal5 * 1000.0F);

         for (Entry<Item, CooldownsHud.SubContext2> local7681_2 : map2.entrySet()) {
            CooldownsHud.SubContext2 value = local7681_2.getValue();
            float local7681_4 = Math.max(0.0F, value.floatVal5 - (float)(System.currentTimeMillis() - value.timeMillis2) / 1000.0F);
            CooldownsHud.SubContext local7681_5 = map.computeIfAbsent(local7681_2.getKey(), CooldownsHud.SubContext::new);
            local7681_5.itemStack3 = getItemStack(minecraftClient, local7681_2.getKey(), value);
            local7681_5.strVal = value.strVal3;
            local7681_5.flag = true;
            processData(local7681_5, Math.round(local7681_4 * 10.0F));
            local7681_5.visible = true;
         }
      }

      for (CooldownsHud.SubContext local7681_2 : map.values()) {
         if (local7681_2.visible || local7681_2.visibilityAnimation.floatValue() > 0.001) {
            list.add(local7681_2);
         }
      }

      map.entrySet()
         .removeIf(
            local7689_0 -> !local7689_0.getValue().visible
               && local7689_0.getValue().visibilityAnimation.floatValue() <= 0.001
               && local7689_0.getValue().visibilityAnimation.isFinished()
         );
      nested0531.visible = list.isEmpty() && HudRenderContext.checkState();
      if (nested0531.visible) {
         long local7681_1 = System.currentTimeMillis();
         float local7681_3 = (float)(10000L - (local7681_1 - timeMillis)) / 1000.0F;
         if (local7681_3 <= 0.0F) {
            timeMillis = local7681_1;
            intVal++;
            local7681_3 = 10.0F;
         }

         Item item = itemArray[intVal % itemArray.length];
         if (nested0531.itemStack3 == null || !nested0531.itemStack3.isOf(item)) {
            nested0531.itemStack3 = new ItemStack(item);
         }

         nested0531.strVal = "Example Cooldown";
         nested0531.flag = true;
         processData(nested0531, Math.round(Math.max(1.0F, local7681_3) * 10.0F));
      } else {
         timeMillis = 0L;
      }

      if (nested0531.visible || nested0531.visibilityAnimation.floatValue() > 0.001) {
         list.add(nested0531);
      }

      for (CooldownsHud.SubContext local7681_2 : list) {
         local7681_2.floatVal4 = computeFloat2(local7681_2);
      }

      list.sort(Comparator.<CooldownsHud.SubContext>comparingDouble(local7676_0 -> local7676_0.floatVal4).reversed());

      for (CooldownsHud.SubContext local7681_2 : list) {
         local7681_2.visibilityAnimation.update();
         local7681_2.visibilityAnimation.animateTo(local7681_2.visible ? 1.0 : 0.0, 0.6, Easings.EASE_OUT_QUART);
      }
   }

   static {
      animator2.resetTo(1.0);
   }

   private static ItemStack getItemStack2(MinecraftClient minecraftClient, Item item, String local7683_2) {
      ItemStack itemStack = ItemStack.EMPTY;
      String[] stringArray = local7683_2.toLowerCase(Locale.ROOT).split("/");

      for (int index = 0; index < minecraftClient.player.getInventory().size() + 1; index++) {
         ItemStack itemStack2 = index < minecraftClient.player.getInventory().size()
            ? minecraftClient.player.getInventory().getStack(index)
            : minecraftClient.player.getOffHandStack();
         if (!itemStack2.isEmpty() && itemStack2.isOf(item)) {
            if (itemStack.isEmpty()) {
               itemStack = itemStack2;
            }

            String local7683_7 = Formatting.strip(itemStack2.getName().getString());
            if (local7683_7 != null) {
               local7683_7 = local7683_7.toLowerCase(Locale.ROOT);

               for (String local7683_11 : stringArray) {
                  if (!local7683_11.isBlank() && local7683_7.contains(local7683_11.trim())) {
                     return itemStack2.copy();
                  }
               }
            }
         }
      }

      return itemStack.isEmpty() ? new ItemStack(item) : itemStack.copy();
   }

   private static float computeFloat2(CooldownsHud.SubContext local7684_0) {
      float[] floatArray = new float[]{0.0F};
      getText(local7684_0).visit((style, local7677_2) -> {
         floatArray[0] += FontText.computeFloat(FontFace.SEMI_BOLD, local7677_2, 6.0F);
         return Optional.empty();
      }, Style.EMPTY);
      return floatArray[0];
   }

   public static float computeFloat3(MinecraftClient minecraftClient, float local7686_1, float local7686_2) {
      processData3(minecraftClient);
      float local7686_3 = HudStyle.computeFloat5("Cooldowns");
      float local7686_4 = 0.0F;
      float local7686_5 = 0.0F;
      float local7686_6 = 13.0F;

      for (CooldownsHud.SubContext local7686_8 : list) {
         local7686_4 = Math.max(local7686_4, HudStyle.computeFloat(8.0F, local7686_8.floatVal3));
         local7686_5 = Math.max(local7686_5, HudStyle.computeFloat4(local7686_8.floatVal4));
         local7686_6 += 14.0F * local7686_8.visibilityAnimation.floatValue();
      }

      float local7686_7 = local7686_4 + (local7686_4 > 0.0F && local7686_5 > 0.0F ? 2.0F : 0.0F) + local7686_5;
      float local7686_8 = Math.max(local7686_3, Math.max(local7686_7, 1.0F));
      boolean local7686_9 = !list.isEmpty() || HudRenderContext.checkState();
      animator.update();
      animator.animateTo(local7686_9 ? 1.0 : 0.0, 0.4, Easings.EASE_OUT_QUART);
      animator2.update();
      if (!list.isEmpty()) {
         animator2.animateTo(Math.max(local7686_8, 1.0F), 0.6, Easings.EASE_OUT_QUART);
      }

      animator3.update();
      if (!list.isEmpty()) {
         animator3.animateTo(Math.max(local7686_8, 1.0F), 0.6, Easings.EASE_OUT_QUART);
      }

      animator4.update();
      if (!list.isEmpty()) {
         animator4.animateTo(Math.max(local7686_4, 0.0F), 0.6, Easings.EASE_OUT_QUART);
      }

      floatVal = animator2.floatValue();
      floatVal2 = Math.max(local7686_6, 1.0F);
      float local7686_10 = animator.floatValue();
      if (local7686_10 <= 0.001F) {
         return 0.0F;
      }

      float[] floatArray = HudManager.computeFloat5("cooldowns", local7686_1 - floatVal, local7686_2);
      float local7686_12 = floatArray[0];
      float local7686_13 = floatArray[1];
      HudManager.processData9("cooldowns", local7686_12, local7686_13, floatVal, floatVal2);
      HudRenderContext.processData4(local7686_12 + floatVal / 2.0F, local7686_13 + floatVal2 / 2.0F, local7686_10);
      HudStyle.processData7(
         local7686_12, local7686_13, Math.max(animator3.floatValue(), 1.0F), local7686_10, HudIcons.COOLDOWNS, "Cooldowns"
      );
      float local7686_14 = local7686_13 + 15.0F;

      for (CooldownsHud.SubContext local7686_16 : list) {
         processData6(local7686_12, local7686_14, local7686_10, local7686_16);
         local7686_14 += 13.0F * local7686_16.visibilityAnimation.floatValue();
      }

      HudRenderContext.updateState();
      return floatVal2 * local7686_10 + 8.0F * local7686_10;
   }

   private static void processData4(MinecraftClient minecraftClient, ItemStack itemStack) {
      if (!itemStack.isEmpty() && !itemStack.isOf(Items.BOW) && !itemStack.isOf(Items.CROSSBOW) && !itemStack.isOf(Items.TRIDENT)) {
         if (!(minecraftClient.player.getItemCooldownManager().getCooldownProgress(itemStack, 0.0F) <= 0.001F)) {
            CooldownsHud.SubContext local7688_2 = map.computeIfAbsent(itemStack.getItem(), CooldownsHud.SubContext::new);
            if (!local7688_2.visible) {
               int local7688_3 = ((ItemCooldownAccess)minecraftClient.player.getItemCooldownManager()).remainingCooldownTicks(itemStack);
               processData(local7688_2, Math.max(0, Math.round(local7688_3 / 2.0F)));
               if (local7688_2.itemStack3 == null || local7688_2.itemStack3.getItem() != itemStack.getItem()) {
                  local7688_2.itemStack3 = itemStack.copy();
                  local7688_2.strVal = itemStack.getName().getString();
               }

               local7688_2.flag = false;
               local7688_2.visible = true;
            }
         }
      }
   }

   public static void processData5(MinecraftClient minecraftClient, float local7690_1, float local7690_2) {
      if (minecraftClient.player != null) {
         float local7690_3 = GuiScale.computeInt() / GuiScale.computeFloat();

         for (int index = 0; index < 9; index++) {
            ItemStack stack = minecraftClient.player.getInventory().getStack(index);
            if (!stack.isEmpty() && !stack.isOf(Items.BOW) && !stack.isOf(Items.CROSSBOW) && !stack.isOf(Items.TRIDENT)) {
               float local7690_6 = Math.max(
                  ((ItemCooldownAccess)minecraftClient.player.getItemCooldownManager()).remainingCooldownTicks(stack) / 20.0F,
                  computeFloat(stack.getItem())
               );
               if (!(local7690_6 <= 0.05F)) {
                  float local7690_7 = local7690_1 / 2.0F - 91.0F + 3.0F + index * 20.0F;
                  Render2D.processData51(
                     local7690_7 * local7690_3,
                     (local7690_2 - 19.0F) * local7690_3,
                     16.0F * local7690_3,
                     16.0F * local7690_3,
                     2.0F * local7690_3,
                     Math.round(InterfaceModule.getInstance().cooldownSlotDarkening.floatValue() * 2.55F) << 24
                  );
                  String local7690_8 = String.valueOf((int)Math.ceil(local7690_6));
                  float local7690_9 = 5.0F * local7690_3;
                  float local7690_10 = 0.7F * local7690_3;
                  float local7690_11 = (local7690_7 + 1.0F) * local7690_3;
                  float local7690_12 = (local7690_2 - 19.0F + 2.5F) * local7690_3 - local7690_9 / 2.0F;
                  int local7690_13 = local7690_6 > 30.0F ? -50384 : (local7690_6 <= 5.0F ? -12517568 : (local7690_6 < 15.0F ? -20440 : -34264));
                  FontText.processData3(FontFace.BOLD, local7690_8, local7690_11 - local7690_10, local7690_12, local7690_9, -16777216);
                  FontText.processData3(FontFace.BOLD, local7690_8, local7690_11 + local7690_10, local7690_12, local7690_9, -16777216);
                  FontText.processData3(FontFace.BOLD, local7690_8, local7690_11, local7690_12 - local7690_10, local7690_9, -16777216);
                  FontText.processData3(FontFace.BOLD, local7690_8, local7690_11, local7690_12 + local7690_10, local7690_9, -16777216);
                  FontText.processData3(FontFace.BOLD, local7690_8, local7690_11, local7690_12, local7690_9, local7690_13);
               }
            }
         }
      }
   }

   private static Text getText(CooldownsHud.SubContext local7691_0) {
      if (local7691_0.itemStack3 == null || local7691_0.itemStack3.isEmpty()) {
         return Text.literal(local7691_0.strVal);
      } else {
         return local7691_0.flag
            ? Text.literal(local7691_0.strVal).styled(style -> style.withColor(getTextColor(local7691_0.itemStack3)))
            : local7691_0.itemStack3
               .getName()
               .copy()
               .styled(style2 -> style2.getColor() == null ? style2.withColor(local7691_0.itemStack3.getRarity().getFormatting()) : style2);
      }
   }

   private static void processData6(float local7693_0, float local7693_1, float local7693_2, CooldownsHud.SubContext local7693_3) {
      float local7693_4 = local7693_3.visibilityAnimation.floatValue() * local7693_2;
      if (!(local7693_4 <= 0.001F)) {
         float local7693_5 = HudStyle.computeFloat2(local7693_4);
         float local7693_6 = local7693_3.floatVal3;
         float local7693_7 = HudStyle.computeFloat(8.0F, local7693_6);
         float local7693_8 = HudStyle.computeFloat4(local7693_3.floatVal4);
         float local7693_9 = local7693_0 - local7693_5;
         float local7693_10 = local7693_0 + animator4.floatValue() + 2.0F - local7693_5;
         float local7693_11 = HudStyle.computeFloat3(local7693_1);
         HudStyle.processData6(local7693_9, local7693_1, local7693_7, 11.0F, local7693_4);
         float local7693_12 = local7693_9 + 2.0F + 8.0F + 1.0F;
         float local7693_13 = local7693_12 + 0.25F;
         HudStyle.processData(local7693_12, local7693_1 + 5.5F, local7693_4, 3.0F);
         if (local7693_3.itemStack3 != null && !local7693_3.itemStack3.isEmpty()) {
            float local7693_14 = Math.round((local7693_9 + (local7693_13 - local7693_9 - 8.0F) / 2.0F) * 2.0F) / 2.0F;
            float local7693_15 = Math.round((local7693_1 + 5.5F - 4.0F) * 2.0F) / 2.0F;
            ItemRenderCache.processData(local7693_3.itemStack3, local7693_14, local7693_15, 8.0F, ItemDrawOptions.noDecorations(local7693_4));
         }

         FontText.processData3(
            FontFace.SEMI_BOLD,
            local7693_3.strVal2,
            local7693_13 + (local7693_9 + local7693_7 - local7693_13 - local7693_6) / 2.0F,
            local7693_11,
            6.0F,
            UiTheme.computeInt(HudStyle.computeInt4(1.0F), local7693_4)
         );
         HudStyle.processData6(local7693_10, local7693_1, local7693_8, 11.0F, local7693_4);
         int local7693_14 = UiTheme.computeInt(HudStyle.computeInt4(1.0F), local7693_4);
         float[] floatArray = new float[]{local7693_10 + 4.0F};
         getText(local7693_3).visit((style, local7678_4) -> {
            if (!local7678_4.isEmpty()) {
               int local7678_5 = style.getColor() != null ? style.getColor().getRgb() & 16777215 | local7693_14 & 0xFF000000 : local7693_14;
               FontText.processData3(FontFace.SEMI_BOLD, local7678_4, floatArray[0], local7693_11, 6.0F, local7678_5);
               floatArray[0] += FontText.computeFloat(FontFace.SEMI_BOLD, local7678_4, 6.0F);
            }

            return Optional.empty();
         }, Style.EMPTY);
      }
   }

   private static final class SubContext extends DraggableHudElement {
      String strVal = "";
      String strVal2 = "";
      float floatVal3;
      float floatVal4;
      int intVal2 = -1;
      boolean flag;
      ItemStack itemStack3;

      SubContext(Item item) {
      }
   }

   private static final class SubContext2 {
      String strVal3 = "";
      long timeMillis2;
      float floatVal5;
      ItemStack itemStack4;
      Object object;
      int intVal3;
      long timeMillis3;
   }
}

package horus.client.invmanager;

import horus.client.MinecraftAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InvSortEngine implements MinecraftAccess {

   public record SwapOperation(int invSlotA, int invSlotB) {}

   public static int getContainerSlotId(int invSlot) {
      if (invSlot >= 0 && invSlot <= 8) {
         return 36 + invSlot;
      } else if (invSlot >= 9 && invSlot <= 35) {
         return invSlot;
      } else if (invSlot == 36) {
         return 8; // Boots
      } else if (invSlot == 37) {
         return 7; // Leggings
      } else if (invSlot == 38) {
         return 6; // Chestplate
      } else if (invSlot == 39) {
         return 5; // Helmet
      } else if (invSlot == 40) {
         return 45; // Offhand
      }
      return -1;
   }

   public static List<SwapOperation> planSorting(InvLayoutConfig layout) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == null || layout == null) {
         return Collections.emptyList();
      }

      String[] current = new String[41];
      for (int i = 0; i <= 40; i++) {
         ItemStack stack = client.player.getInventory().getStack(i);
         current[i] = stack.isEmpty() ? "" : Registries.ITEM.getId(stack.getItem()).toString();
      }

      List<SwapOperation> swaps = new ArrayList<>();

      for (int target = 0; target <= 40; target++) {
         String wanted = layout.getItem(target);
         if (wanted == null || wanted.isBlank()) continue;
         if (wanted.equals(current[target])) continue;

         int source = -1;
         // First pass: find a slot that has wanted item AND is not already satisfying its own target
         for (int s = 0; s <= 40; s++) {
            if (s == target) continue;
            if (wanted.equals(current[s])) {
               String sWanted = layout.getItem(s);
               if (!wanted.equals(sWanted)) {
                  source = s;
                  break;
               }
            }
         }

         // Second pass: if none found, take from any slot holding wanted item
         if (source == -1) {
            for (int s = 0; s <= 40; s++) {
               if (s == target) continue;
               if (wanted.equals(current[s])) {
                  source = s;
                  break;
               }
            }
         }

         if (source != -1) {
            swaps.add(new SwapOperation(source, target));
            String temp = current[target];
            current[target] = current[source];
            current[source] = temp;
         }
      }

      return swaps;
   }

   public static void executeSwap(SwapOperation op) {
      if (op == null) return;
      executeSwap(op.invSlotA(), op.invSlotB());
   }

   public static void executeSwap(int invSlotA, int invSlotB) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == null || client.interactionManager == null) return;
      if (invSlotA < 0 || invSlotA > 40 || invSlotB < 0 || invSlotB > 40 || invSlotA == invSlotB) return;

      int cSlotA = getContainerSlotId(invSlotA);
      int cSlotB = getContainerSlotId(invSlotB);
      if (cSlotA == -1 || cSlotB == -1 || cSlotA == cSlotB) return;

      int syncId = client.player.playerScreenHandler.syncId;

      // Fast swap if one is hotbar (container slots 36..44)
      if (cSlotB >= 36 && cSlotB <= 44) {
         client.interactionManager.clickSlot(syncId, cSlotA, cSlotB - 36, SlotActionType.SWAP, client.player);
         return;
      }
      if (cSlotA >= 36 && cSlotA <= 44) {
         client.interactionManager.clickSlot(syncId, cSlotB, cSlotA - 36, SlotActionType.SWAP, client.player);
         return;
      }

      // Fast swap if one is offhand (container slot 45)
      if (cSlotB == 45) {
         client.interactionManager.clickSlot(syncId, cSlotA, 40, SlotActionType.SWAP, client.player);
         return;
      }
      if (cSlotA == 45) {
         client.interactionManager.clickSlot(syncId, cSlotB, 40, SlotActionType.SWAP, client.player);
         return;
      }

      // Standard 3-step pickup swap for storage/armor
      client.interactionManager.clickSlot(syncId, cSlotA, 0, SlotActionType.PICKUP, client.player);
      client.interactionManager.clickSlot(syncId, cSlotB, 0, SlotActionType.PICKUP, client.player);
      client.interactionManager.clickSlot(syncId, cSlotA, 0, SlotActionType.PICKUP, client.player);
   }
}

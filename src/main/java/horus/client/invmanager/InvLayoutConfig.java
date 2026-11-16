package horus.client.invmanager;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class InvLayoutConfig {
   private String name;
   private final Map<Integer, String> slots = new HashMap<>();

   public InvLayoutConfig() {
      this("Default");
   }

   public InvLayoutConfig(String name) {
      this.name = name == null || name.isBlank() ? "Default" : name.trim();
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name == null || name.isBlank() ? "Default" : name.trim();
   }

   public Map<Integer, String> getSlots() {
      return this.slots;
   }

   public void setItem(int slot, String itemId) {
      if (slot < 0 || slot > 40) return;
      if (itemId == null || itemId.isBlank()) {
         this.slots.remove(slot);
      } else {
         this.slots.put(slot, itemId.trim());
      }
   }

   public String getItem(int slot) {
      return this.slots.getOrDefault(slot, "");
   }

   public boolean hasItem(int slot) {
      String id = this.slots.get(slot);
      return id != null && !id.isBlank();
   }

   public void clear() {
      this.slots.clear();
   }

   public InvLayoutConfig copy() {
      InvLayoutConfig copy = new InvLayoutConfig(this.name);
      copy.slots.putAll(this.slots);
      return copy;
   }

   public static ItemStack getItemStack(String itemId) {
      if (itemId == null || itemId.isBlank()) {
         return ItemStack.EMPTY;
      }
      Identifier identifier = itemId.indexOf(':') >= 0
         ? Identifier.tryParse(itemId.trim())
         : Identifier.ofVanilla(itemId.trim());
      if (identifier == null) {
         return ItemStack.EMPTY;
      }
      Item item = Registries.ITEM.getOptionalValue(identifier).orElse(null);
      return item == null ? ItemStack.EMPTY : item.getDefaultStack();
   }

   public static String getItemId(ItemStack stack) {
      if (stack == null || stack.isEmpty()) {
         return "";
      }
      return Registries.ITEM.getId(stack.getItem()).toString();
   }

   public static InvLayoutConfig fromPlayerInventory(PlayerInventory inv, String name) {
      InvLayoutConfig config = new InvLayoutConfig(name);
      if (inv == null) return config;

      for (int i = 0; i <= 40; i++) {
         ItemStack stack = inv.getStack(i);
         if (!stack.isEmpty()) {
            config.setItem(i, getItemId(stack));
         }
      }
      return config;
   }

   public static InvLayoutConfig createDefaultPvpPreset() {
      InvLayoutConfig config = new InvLayoutConfig("PvP");
      // Armor (39: Helmet, 38: Chestplate, 37: Leggings, 36: Boots)
      config.setItem(39, "minecraft:netherite_helmet");
      config.setItem(38, "minecraft:netherite_chestplate");
      config.setItem(37, "minecraft:netherite_leggings");
      config.setItem(36, "minecraft:netherite_boots");

      // Offhand (40)
      config.setItem(40, "minecraft:totem_of_undying");

      // Hotbar (0..8)
      config.setItem(0, "minecraft:netherite_sword");
      config.setItem(1, "minecraft:netherite_axe");
      config.setItem(2, "minecraft:enchanted_golden_apple");
      config.setItem(3, "minecraft:ender_pearl");
      config.setItem(4, "minecraft:totem_of_undying");
      config.setItem(5, "minecraft:totem_of_undying");
      config.setItem(6, "minecraft:chorus_fruit");
      config.setItem(7, "minecraft:firework_rocket");
      config.setItem(8, "minecraft:water_bucket");

      // Main inventory (9..35)
      for (int i = 9; i <= 26; i++) {
         config.setItem(i, "minecraft:totem_of_undying");
      }
      for (int i = 27; i <= 35; i++) {
         config.setItem(i, "minecraft:golden_apple");
      }

      return config;
   }

   public static InvLayoutConfig createTotemKitPreset() {
      InvLayoutConfig config = new InvLayoutConfig("Totem Kit");
      // Armor
      config.setItem(39, "minecraft:netherite_helmet");
      config.setItem(38, "minecraft:netherite_chestplate");
      config.setItem(37, "minecraft:netherite_leggings");
      config.setItem(36, "minecraft:netherite_boots");

      // Offhand
      config.setItem(40, "minecraft:totem_of_undying");

      // Hotbar
      config.setItem(0, "minecraft:netherite_sword");
      for (int i = 1; i <= 8; i++) {
         config.setItem(i, "minecraft:totem_of_undying");
      }

      // Full 27 slots of totems
      for (int i = 9; i <= 35; i++) {
         config.setItem(i, "minecraft:totem_of_undying");
      }

      return config;
   }
}

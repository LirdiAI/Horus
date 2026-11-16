package horus.client.module.combat;

import horus.client.event.EventListener;
import horus.client.event.PlayerTickEvent;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.KeybindSetting;
import horus.client.setting.ModeSetting;
import horus.client.ui.hud.NotificationHud;
import horus.client.util.InventoryUtils;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.PiercingWeaponComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * AutoLunge: по нажатию бинда достаёт копьё (хотбар или инвентарь), бьёт им тем же
 * пакетом, что и ванильный клик, и возвращает прежний слот.
 *
 * Удар отправляется сразу после свапа в том же тике: после свапа тикер заряда
 * сброшен, поэтому ванильный клик копьём отклоняется проверкой
 * MINIMUM_ATTACK_CHARGE — бьём напрямую пакетом атаки.
 */
@ModuleInfo(name = "AutoLunge", description = "Достаёт копьё, бьёт им и возвращает прежний слот.", category = ModuleCategory.COMBAT)
public final class AutoLungeModule extends Module {
   private static AutoLungeModule autoLungeModule;

   private static final int IDLE = 0;
   private static final int ATTACKED = 1;

   private final KeybindSetting keybindSetting = this.addSetting(
      new KeybindSetting("Клавиша", "Свап на копьё → удар → возврат прежнего слота.")
   );
   private final ModeSetting modeSetting = this.addSetting(
      new ModeSetting(
         "Режим",
         "Пакетный: удар и возврат слота в одном тике. Легитный: возврат через тик, как у живого игрока.",
         "Пакетный",
         "Пакетный",
         "Легитный"
      )
   );

   private final EventListener<PlayerTickEvent> playerTickListener = event -> this.onTick();

   private boolean wasPressed;
   private int stage = IDLE;
   private int waitTicks;
   private int spearSlot = -1;     // слот хотбара, куда положили копьё
   private int inventorySlot = -1; // исходный слот инвентаря (>8), если копьё взяли оттуда
   private int restoreSlot = -1;   // прежний выбранный слот хотбара

   private AutoLungeModule() {
   }

   public static AutoLungeModule getInstance() {
      if (autoLungeModule == null) {
         autoLungeModule = new AutoLungeModule();
      }

      return autoLungeModule;
   }

   @Override
   protected void onEnable() {
      // Включение из ClickGUI без назначенного бинда: сработать один раз и выключиться
      if (!this.keybindSetting.isBound()) {
         if (mc.player != null && mc.currentScreen == null) {
            this.start();
         }

         this.setEnabled(false);
      }
   }

   @Override
   protected void onDisable() {
      this.reset();
      this.wasPressed = false;
   }

   private void onTick() {
      if (mc.player == null || mc.world == null || mc.interactionManager == null) {
         this.reset();
         this.wasPressed = false;
         return;
      }

      if (this.stage == ATTACKED) {
         if (this.waitTicks > 0) {
            this.waitTicks--;
         }

         if (this.waitTicks <= 0) {
            this.restore();
         }

         return;
      }

      boolean pressed = mc.currentScreen == null
         && this.keybindSetting.isBound()
         && this.keybindSetting.getBinding().isPressed(mc.getWindow().getHandle());

      if (!pressed) {
         this.wasPressed = false;
         return;
      }

      if (!this.wasPressed) {
         this.wasPressed = true;

         if (!mc.player.isUsingItem() && mc.player.currentScreenHandler == mc.player.playerScreenHandler) {
            this.start();
         }
      }
   }

   private void start() {
      if (this.stage != IDLE || mc.player == null || mc.interactionManager == null || mc.currentScreen != null) {
         return;
      }

      int spear = this.findSpear();
      if (spear == -1) {
         this.notify("В инвентаре нет копья");
         return;
      }

      int selected = InventoryUtils.getSelectedSlot();
      this.restoreSlot = selected;

      if (spear <= 8) {
         // копьё уже в хотбаре — просто выбираем слот
         this.inventorySlot = -1;
         this.spearSlot = spear;

         if (spear != selected) {
            this.switchSlot(spear);
         }
      } else {
         // копьё в инвентаре — молча меняем с текущим слотом хотбара
         this.inventorySlot = spear;
         this.spearSlot = selected;
         InventoryUtils.swapSlotWithHotbar(spear, selected);
      }

      if (!AutoSpearModule.isSpear(mc.player.getMainHandStack())) {
         this.reset();
         this.notify("Не удалось взять копьё в руку");
         return;
      }

      this.attackPacket();

      if (this.modeSetting.isSelected("Легитный")) {
         // копьё остаётся в руке ещё один тик, чтобы возврат слота выглядел как у живого игрока
         this.stage = ATTACKED;
         this.waitTicks = 1;
         return;
      }

      this.restore();
   }

   /** Отправляет удар копьём напрямую, не дожидаясь ванильного hitResult. */
   private void attackPacket() {
      ItemStack held = mc.player.getMainHandStack();
      PiercingWeaponComponent weapon = held.get(DataComponentTypes.PIERCING_WEAPON);

      if (weapon != null && mc.interactionManager != null) {
         mc.interactionManager.attackWithPiercingWeapon(weapon);
      } else if (mc.getNetworkHandler() != null) {
         mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STAB, BlockPos.ORIGIN, Direction.DOWN));
      }

      mc.player.swingHand(Hand.MAIN_HAND);
   }

   private void restore() {
      if (this.inventorySlot > 8) {
         // вернуть прежний предмет обратно в хотбар, а копьё — в инвентарь
         InventoryUtils.swapSlotWithHotbar(this.inventorySlot, this.spearSlot);
      } else if (this.restoreSlot >= 0 && this.restoreSlot != InventoryUtils.getSelectedSlot()) {
         this.switchSlot(this.restoreSlot);
      }

      this.reset();
   }

   private void switchSlot(int slot) {
      if (slot >= 0 && slot <= 8) {
         mc.player.getInventory().setSelectedSlot(slot);

         if (mc.player.networkHandler != null) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
         }
      }
   }

   private void reset() {
      this.stage = IDLE;
      this.waitTicks = 0;
      this.spearSlot = -1;
      this.inventorySlot = -1;
      this.restoreSlot = -1;
   }

   /** Ищет копьё во всём инвентаре; при равном тире побеждает хотбар. */
   private int findSpear() {
      int best = -1;
      int bestScore = -1;

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = mc.player.getInventory().getStack(slot);
         if (stack.isEmpty() || !AutoSpearModule.isSpear(stack)) {
            continue;
         }

         int score = this.spearScore(stack);
         if (score > bestScore) {
            bestScore = score;
            best = slot;
         }
      }

      return best;
   }

   /** Уровень зачарования «Рывок» (minecraft:lunge); 0 — если его нет. */
   private int getLungeLevel(ItemStack stack) {
      if (stack.isEmpty() || mc.world == null) {
         return 0;
      }

      try {
         RegistryEntry<Enchantment> lunge = mc.world.getRegistryManager()
            .getOptional(RegistryKeys.ENCHANTMENT)
            .flatMap(registry -> registry.getOptional(Enchantments.LUNGE))
            .orElse(null);

         if (lunge != null) {
            int level = EnchantmentHelper.getLevel(lunge, stack);
            if (level > 0) {
               return level;
            }
         }
      } catch (Exception ignored) {
      }

      try {
         ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.ENCHANTMENTS);

         if (enchants != null) {
            for (RegistryEntry<Enchantment> entry : enchants.getEnchantments()) {
               String id = entry.getIdAsString().toLowerCase();
               if (id.contains("lunge") || id.contains("рывок") || id.contains("выпад")) {
                  return enchants.getLevel(entry);
               }
            }
         }
      } catch (Exception ignored) {
      }

      return 0;
   }

   /** Сначала копья с «Рывком» (чем выше уровень, тем лучше), затем по материалу. */
   private int spearScore(ItemStack stack) {
      int material = this.materialScore(stack);
      int lunge = this.getLungeLevel(stack);
      return lunge > 0 ? 100 + lunge * 10 + material : material;
   }

   private int materialScore(ItemStack stack) {
      String id = Registries.ITEM.getId(stack.getItem()).getPath();
      if (id.contains("netherite")) {
         return 7;
      }

      if (id.contains("diamond")) {
         return 6;
      }

      if (id.contains("iron")) {
         return 5;
      }

      if (id.contains("gold")) {
         return 4;
      }

      if (id.contains("copper")) {
         return 3;
      }

      if (id.contains("stone")) {
         return 2;
      }

      return 1;
   }

   private void notify(String message) {
      NotificationHud.notifyWithText(
         this.category.getIcon(), "AutoLunge", Text.literal(message).setStyle(Style.EMPTY.withColor(Formatting.RED)), true
      );
   }
}

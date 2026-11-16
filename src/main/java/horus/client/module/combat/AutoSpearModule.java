package horus.client.module.combat;

import horus.client.event.EventListener;
import horus.client.event.PlayerTickEvent;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.BooleanSetting;
import horus.client.setting.KeybindSetting;
import horus.client.setting.ModeSetting;
import horus.client.setting.NumberSetting;
import horus.client.ui.hud.NotificationHud;
import horus.client.util.InventoryUtils;
import horus.client.util.EntityUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.PiercingWeaponComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "AutoSpear", description = "Мгновенно использует копьё с зачарованием «Рывок» из хотбара.", category = ModuleCategory.COMBAT)
public final class AutoSpearModule extends Module {
   private static AutoSpearModule autoSpearModule;

   private final KeybindSetting keybindSetting = this.addSetting(
      new KeybindSetting("Рывок копьём", "Кнопка активации рывка (если не задана, работает бинд самого модуля).")
   );
   private final ModeSetting serverSetting = this.addSetting(
      new ModeSetting(
         "Сервер",
         "Режим работы: Авто подходит для большинства серверов (ReallyWorld, FunTime, SpookyTime).",
         "Авто",
         "Авто",
         "ReallyWorld",
         "FunTime",
         "SpookyTime",
         "Ванила / Other"
      )
   );
   private final ModeSetting attackModeSetting = this.addSetting(
      new ModeSetting(
         "Тип действия",
         "Какое сетевое действие отправлять на сервер при рывке.",
         "Все (STAB + ПКМ + Взмах)",
         "Все (STAB + ПКМ + Взмах)",
         "STAB + Взмах",
         "ПКМ (Использовать)",
         "ЛКМ (Атака)",
         "Только STAB",
         "Только импульс"
      )
   );
   private final ModeSetting impulseModeSetting = this.addSetting(
      new ModeSetting(
         "Режим рывка",
         "Заменять: резкий моментальный рывок. Прибавлять: плавное сложение со скоростью бега.",
         "Заменять",
         "Заменять",
         "Прибавлять"
      )
   );
   private final NumberSetting forceSetting = this.addSetting(
      new NumberSetting("Сила рывка", "Скорость рывка вперед. 0 — авто (по уровню зачарования), либо вручную от 0.5 до 4.0.")
         .withRange(0.0F, 4.0F)
         .withStep(0.1F)
         .setValue(0.0F)
   );
   private final BooleanSetting verticalBoostSetting = this.addSetting(
      new BooleanSetting("Учитывать наклон", "Учитывать ли наклон прицела вверх/вниз при рывке.", false)
   );
   private final BooleanSetting checkEnchantSetting = this.addSetting(
      new BooleanSetting("Требовать «Рывок»", "Искать копьё только с зачарованием «Рывок»/«Выпад». Если выключено — подходит любое копьё.", false)
   );
   private final BooleanSetting revertSlotSetting = this.addSetting(
      new BooleanSetting("Возвращать слот", "Возвращать ли прежний выбранный слот после выполнения рывка.", true)
   );
   private final NumberSetting delaySetting = this.addSetting(
      new NumberSetting("Задержка возврата", "Задержка в тиках перед возвратом прежнего слота (для стабильности на серверах)")
         .withIntegerRange(1, 10)
         .withIntegerStep(1)
         .withSuffix(" т.")
         .setValue(2.0F)
   );
   private final BooleanSetting repeatSetting = this.addSetting(
      new BooleanSetting("Повторять при удержании", "Повторять попытку рывка, пока кнопка бинда удерживается.", true)
   );
   private final NumberSetting repeatDelaySetting = this.addSetting(
      new NumberSetting("Интервал рывка", "Интервал между попытками рывка при удержании бинда.")
         .withIntegerRange(1, 20)
         .withIntegerStep(1)
         .withSuffix(" т.")
         .setValue(1.0F)
   );

   private boolean keybindPressed;
   private int pendingRevertSlot = -1;
   private int revertTicksRemaining = 0;
   private int repeatTicksRemaining = 0;

   private final EventListener<PlayerTickEvent> playerTickListener = event -> this.onTick();

   private AutoSpearModule() {
   }

   public static AutoSpearModule getInstance() {
      if (autoSpearModule == null) {
         autoSpearModule = new AutoSpearModule();
      }

      return autoSpearModule;
   }

   @Override
   protected void onEnable() {
      // If the module is toggled via the module's own keybind and no inner keybind is configured:
      if (!this.keybindSetting.isBound()) {
         if (mc.player != null && mc.currentScreen == null) {
            this.useSpear();
         }
         this.setEnabled(false);
      }
   }

   @Override
   protected void onDisable() {
      this.pendingRevertSlot = -1;
      this.revertTicksRemaining = 0;
      this.keybindPressed = false;
      this.repeatTicksRemaining = 0;
   }

   private void onTick() {
      if (mc.player == null || mc.world == null) {
         this.pendingRevertSlot = -1;
         this.revertTicksRemaining = 0;
         this.keybindPressed = false;
         this.repeatTicksRemaining = 0;
         return;
      }

      // Delayed slot revert for server stability
      if (this.revertTicksRemaining > 0) {
         this.revertTicksRemaining--;
         if (this.revertTicksRemaining == 0 && this.pendingRevertSlot != -1) {
            if (this.revertSlotSetting.getValue() && InventoryUtils.getSelectedSlot() != this.pendingRevertSlot) {
               this.switchSlot(this.pendingRevertSlot);
            }
            this.pendingRevertSlot = -1;
         }
      }

      this.handleKeybind();
   }

   private void handleKeybind() {
      if (mc.interactionManager == null) {
         this.keybindPressed = false;
         this.repeatTicksRemaining = 0;
         return;
      }

      boolean pressed = mc.currentScreen == null
         && this.keybindSetting.isBound()
         && this.keybindSetting.getBinding().isPressed(mc.getWindow().getHandle());

      if (!pressed) {
         this.keybindPressed = false;
         this.repeatTicksRemaining = 0;
         return;
      }

      if (!this.keybindPressed) {
         this.keybindPressed = true;
         if (!mc.player.isUsingItem()) {
            this.useSpear();
         }
         this.repeatTicksRemaining = Math.max(1, Math.round(this.repeatDelaySetting.getValue()));
         return;
      }

      if (this.repeatSetting.isChecked() && !mc.player.isUsingItem() && --this.repeatTicksRemaining <= 0) {
         this.useSpear();
         this.repeatTicksRemaining = Math.max(1, Math.round(this.repeatDelaySetting.getValue()));
      }
   }

   private void switchSlot(int slot) {
      if (mc.player != null && slot >= 0 && slot <= 8) {
         mc.player.getInventory().setSelectedSlot(slot);
         if (mc.player.networkHandler != null) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
         }
      }
   }

   public boolean useSpear() {
      return this.useSpear(false, null);
   }

   public boolean useSpear(boolean silent) {
      return this.useSpear(silent, null);
   }

   public boolean useSpear(boolean silent, Vec3d targetPos) {
      if (mc.player == null || mc.world == null || mc.interactionManager == null) {
         return false;
      }

      boolean hasSpearInInventory = false;
      for (int slot = 0; slot < mc.player.getInventory().size(); slot++) {
         if (isSpear(mc.player.getInventory().getStack(slot))) {
            hasSpearInInventory = true;
            break;
         }
      }
      if (!hasSpearInInventory && !isSpear(mc.player.getOffHandStack())) {
         if (!silent) {
            this.notifyUnavailable("В инвентаре нет копья");
         }
         return false;
      }

      boolean checkEnchant = this.checkEnchantSetting.isChecked() && !this.serverSetting.isSelected("ReallyWorld");
      int spearSlot = -1;
      int bestLevel = 0;
      boolean foundAnySpearInHotbar = false;

      // Check hotbar slots 0..8
      for (int slot = 0; slot < 9; slot++) {
         ItemStack stack = mc.player.getInventory().getStack(slot);
         if (!isSpear(stack)) {
            continue;
         }
         foundAnySpearInHotbar = true;
         int level = this.getLungeLevel(stack);

         if (checkEnchant) {
            if (level > bestLevel && level > 0) {
               bestLevel = level;
               spearSlot = slot;
            }
         } else {
            if (level > bestLevel) {
               bestLevel = level;
               spearSlot = slot;
            } else if (spearSlot == -1) {
               spearSlot = slot;
            }
         }
      }

      // Check offhand if not in hotbar
      boolean isOffhand = false;
      if (spearSlot == -1 && isSpear(mc.player.getOffHandStack())) {
         int offLevel = this.getLungeLevel(mc.player.getOffHandStack());
         if (!checkEnchant || offLevel > 0) {
            isOffhand = true;
            bestLevel = Math.max(bestLevel, offLevel);
         }
      }

      if (spearSlot == -1 && !isOffhand) {
         int invSlot = findSpearInventorySlot();
         if (invSlot > 8) {
            int selected = InventoryUtils.getSelectedSlot();
            InventoryUtils.swapSlotWithHotbar(invSlot, selected);
            spearSlot = selected;
         }
      }

      if (spearSlot == -1 && !isOffhand) {
         if (!silent) {
            if (!foundAnySpearInHotbar) {
               this.notifyUnavailable("Копьё должно быть в хотбаре или левой руке");
            } else {
               this.notifyUnavailable("На копье нет зачарования «Рывок»/«Выпад»");
            }
         }
         return false;
      }

      // Preserve true original slot before slot swaps, especially when repeat is active
      int originalSlot = (this.pendingRevertSlot != -1) ? this.pendingRevertSlot : InventoryUtils.getSelectedSlot();
      if (!isOffhand && spearSlot != InventoryUtils.getSelectedSlot()) {
         this.switchSlot(spearSlot);
      }

      ItemStack spear = isOffhand ? mc.player.getOffHandStack() : mc.player.getInventory().getStack(spearSlot);
      PiercingWeaponComponent piercingWeapon = spear.get(DataComponentTypes.PIERCING_WEAPON);
      Hand hand = isOffhand ? Hand.OFF_HAND : Hand.MAIN_HAND;

      // 1. Actions / Network packets
      String actionType = this.attackModeSetting.getSelectedOption();
      switch (actionType) {
         case "Только STAB":
            if (mc.getNetworkHandler() != null) {
               mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STAB, BlockPos.ORIGIN, Direction.DOWN));
            }
            if (piercingWeapon != null) {
               piercingWeapon.playSound(mc.player);
            }
            break;
         case "ЛКМ (Атака)":
            if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY && mc.crosshairTarget instanceof EntityHitResult entityHit) {
               mc.interactionManager.attackEntity(mc.player, entityHit.getEntity());
            }
            mc.player.swingHand(hand);
            break;
         case "ПКМ (Использовать)":
            mc.interactionManager.interactItem(mc.player, hand);
            mc.player.swingHand(hand);
            break;
         case "STAB + Взмах":
            if (piercingWeapon != null) {
               mc.interactionManager.attackWithPiercingWeapon(piercingWeapon);
            } else {
               if (mc.getNetworkHandler() != null) {
                  mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STAB, BlockPos.ORIGIN, Direction.DOWN));
               }
               mc.player.beforePlayerAttack();
            }
            mc.player.swingHand(hand);
            break;
         case "Только импульс":
            mc.player.swingHand(hand);
            break;
         case "Все (STAB + ПКМ + Взмах)":
         default:
            if (mc.getNetworkHandler() != null) {
               mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STAB, BlockPos.ORIGIN, Direction.DOWN));
            }
            mc.interactionManager.interactItem(mc.player, hand);
            mc.player.swingHand(hand);
            mc.player.beforePlayerAttack();
            break;
      }

      // 2. Client-side physical velocity impulse (Instant lunge)
      Vec3d look;
      if (targetPos != null) {
         look = targetPos.subtract(mc.player.getEyePos()).normalize();
         float[] rots = EntityUtils.calculateRotationsToVec(targetPos, mc.player.getEyePos());
         mc.player.setYaw(rots[0]);
         mc.player.setPitch(rots[1]);
      } else {
         look = mc.player.getRotationVector();
      }
      double hLen = Math.hypot(look.x, look.z);
      double normX = hLen > 1.0E-4 ? look.x / hLen : 0.0;
      double normZ = hLen > 1.0E-4 ? look.z / hLen : 0.0;

      double force;
      float manualForce = this.forceSetting.getValue();
      if (manualForce > 0.05F) {
         force = manualForce;
      } else {
         int effLevel = Math.max(1, bestLevel);
         // Vanilla formula: 0.458 * level. If bestLevel is 0 (no enchant found), default to 1.37 (Level 3)
         force = (bestLevel > 0) ? (0.458 * effLevel) : 1.37;
      }
      force = Math.min(force, 4.0);

      double yBoost;
      if (targetPos != null) {
         yBoost = MathHelper.clamp(look.y * force, -2.5, 1.5);
      } else if (this.verticalBoostSetting.isChecked()) {
         double pitchRad = Math.toRadians(-mc.player.getPitch());
         yBoost = MathHelper.clamp(Math.sin(pitchRad) * force, -1.0, 1.5);
      } else {
         yBoost = mc.player.isOnGround() ? 0.2 : mc.player.getVelocity().y;
      }

      if (this.impulseModeSetting.isSelected("Заменять")) {
         mc.player.setVelocity(normX * force, yBoost, normZ * force);
      } else {
         mc.player.addVelocity(normX * force, yBoost, normZ * force);
      }
      mc.player.velocityDirty = true;

      // 3. Audio feedback
      try {
         mc.player.playSound(SoundEvents.ITEM_SPEAR_LUNGE_1.value(), 1.0F, 1.0F);
      } catch (Exception ignored) {
      }

      // 4. Delayed slot revert for server stability
      if (!isOffhand && this.revertSlotSetting.getValue() && spearSlot != originalSlot) {
         this.pendingRevertSlot = originalSlot;
         this.revertTicksRemaining = Math.max(1, Math.round(this.delaySetting.getValue()));
      }
      return true;
   }

   public void cancelPendingRevert() {
      this.pendingRevertSlot = -1;
      this.revertTicksRemaining = 0;
   }

   private int getLungeLevel(ItemStack stack) {
      if (stack.isEmpty() || mc.world == null) {
         return 0;
      }

      // 1. Try registry-based enchantment (vanilla / experimental snapshot)
      try {
         RegistryEntry<Enchantment> lunge = mc.world.getRegistryManager()
            .getOptional(RegistryKeys.ENCHANTMENT)
            .flatMap(r -> r.getOptional(Enchantments.LUNGE))
            .orElse(null);
         if (lunge != null) {
            int lvl = EnchantmentHelper.getLevel(lunge, stack);
            if (lvl > 0) {
               return lvl;
            }
         }
      } catch (Exception ignored) {
      }

      // 2. Check enchantments component by id/path
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

      // 3. Check lore and custom name (for servers like ReallyWorld, FunTime, SpookyTime)
      try {
         LoreComponent lore = stack.get(DataComponentTypes.LORE);
         if (lore != null) {
            for (Text line : lore.lines()) {
               int lvl = this.parseLevelFromText(line.getString());
               if (lvl > 0) {
                  return lvl;
               }
            }
         }
         Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
         if (customName != null) {
            int lvl = this.parseLevelFromText(customName.getString());
            if (lvl > 0) {
               return lvl;
            }
         }
      } catch (Exception ignored) {
      }

      return 0;
   }

   private int parseLevelFromText(String raw) {
      if (raw == null || raw.isEmpty()) {
         return 0;
      }
      String s = Formatting.strip(raw).toLowerCase();
      if (!s.contains("рывок") && !s.contains("lunge") && !s.contains("выпад")) {
         return 0;
      }
      Pattern p = Pattern.compile("(?i)(?:рывок|lunge|выпад)[^0-9a-zа-яё]*([0-9]+|[ivxlcdmії]+)?");
      Matcher m = p.matcher(s);
      if (m.find()) {
         String num = m.group(1);
         if (num != null && !num.isEmpty()) {
            try {
               return Integer.parseInt(num);
            } catch (NumberFormatException e) {
               return this.parseRoman(num.toUpperCase());
            }
         }
         return 1;
      }
      return 1;
   }

   private int parseRoman(String roman) {
      int result = 0;
      int prev = 0;
      for (int i = roman.length() - 1; i >= 0; i--) {
         char c = roman.charAt(i);
         int val = switch (c) {
            case 'I', 'І', 'i', 'і', '1' -> 1;
            case 'V', 'v' -> 5;
            case 'X', 'Х', 'x', 'х' -> 10;
            case 'L', 'l' -> 50;
            case 'C', 'С', 'c', 'с' -> 100;
            default -> 0;
         };
         if (val < prev) {
            result -= val;
         } else {
            result += val;
         }
         prev = val;
      }
      return Math.max(1, result);
   }

   private void notifyUnavailable(String message) {
      NotificationHud.notifyWithText(
         this.category.getIcon(), "AutoSpear", Text.literal(message).setStyle(Style.EMPTY.withColor(Formatting.RED)), true
      );
   }

   public static boolean hasSpearInInventory() {
      if (mc.player == null) {
         return false;
      }
      if (isSpear(mc.player.getOffHandStack())) {
         return true;
      }
      for (int i = 0; i < mc.player.getInventory().size(); i++) {
         if (isSpear(mc.player.getInventory().getStack(i))) {
            return true;
         }
      }
      return false;
   }

   public static int findSpearHotbarSlot() {
      if (mc.player == null) {
         return -1;
      }
      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (isSpear(stack) && !mc.player.getItemCooldownManager().isCoolingDown(stack)) {
            return i;
         }
      }
      return -1;
   }

   public static int findSpearInventorySlot() {
      if (mc.player == null) {
         return -1;
      }
      int hotbar = findSpearHotbarSlot();
      if (hotbar != -1) {
         return hotbar;
      }
      for (int i = 9; i < 36; i++) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (isSpear(stack) && !mc.player.getItemCooldownManager().isCoolingDown(stack)) {
            return i;
         }
      }
      return -1;
   }

   public static boolean isSpear(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      }
      if (stack.isOf(Items.WOODEN_SPEAR)
         || stack.isOf(Items.STONE_SPEAR)
         || stack.isOf(Items.COPPER_SPEAR)
         || stack.isOf(Items.IRON_SPEAR)
         || stack.isOf(Items.GOLDEN_SPEAR)
         || stack.isOf(Items.DIAMOND_SPEAR)
         || stack.isOf(Items.NETHERITE_SPEAR)
         || stack.isOf(Items.TRIDENT)) {
         return true;
      }
      if (stack.get(DataComponentTypes.PIERCING_WEAPON) != null) {
         return true;
      }
      String name = stack.getName().getString().toLowerCase();
      if (name.contains("копь") || name.contains("spear")) {
         return true;
      }
      LoreComponent lore = stack.get(DataComponentTypes.LORE);
      if (lore != null) {
         for (Text line : lore.lines()) {
            String s = line.getString().toLowerCase();
            if (s.contains("рывок") || s.contains("выпад") || s.contains("lunge") || s.contains("копь")) {
               return true;
            }
         }
      }
      return false;
   }
}

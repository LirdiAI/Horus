package horus.client.module.combat;

import horus.client.event.EventListener;
import horus.client.event.PlayerTickEvent;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.BooleanSetting;
import horus.client.setting.NumberSetting;
import horus.client.ui.hud.NotificationHud;
import horus.client.util.InventoryActionQueue;
import horus.client.util.InventorySlotManager;
import horus.client.util.InventoryUtils;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

@ModuleInfo(name = "AutoBuff", description = "Автоматически использует взрывные зелья полезных эффектов.", category = ModuleCategory.COMBAT)
public final class AutoBuffModule extends Module {
   private static AutoBuffModule autoBuffModule;

   private final BooleanSetting strengthSetting = this.addSetting(
      new BooleanSetting("Сила", "Автоматически кидать зелье Силы при отсутствии эффекта.", true)
   );
   private final BooleanSetting speedSetting = this.addSetting(
      new BooleanSetting("Скорость", "Автоматически кидать зелье Скорости при отсутствии эффекта.", true)
   );
   private final BooleanSetting fireResSetting = this.addSetting(
      new BooleanSetting("Огнестойкость", "Автоматически кидать зелье Огнестойкости при отсутствии эффекта.", true)
   );
   private final BooleanSetting regenSetting = this.addSetting(
      new BooleanSetting("Регенерация", "Автоматически кидать зелье Регенерации при отсутствии эффекта.", false)
   );
   private final BooleanSetting healSetting = this.addSetting(
      new BooleanSetting("Исцеление (Хил)", "Кидать зелье моментального исцеления при низком здоровье.", false)
   );
   private final NumberSetting healHpSetting = this.addSetting(
      new NumberSetting("Порог HP для хила", "Уровень здоровья (в HP из 20), при котором экстренно бросается зелье исцеления.")
         .withIntegerRange(1, 19)
         .withIntegerStep(1)
         .withSuffix(" HP")
         .setValue(10.0F)
   );

   private final NumberSetting minDurationSetting = this.addSetting(
      new NumberSetting("Обновлять за (сек)", "Обновлять эффект, если до окончания осталось меньше указанных секунд.")
         .withIntegerRange(0, 30)
         .withIntegerStep(1)
         .withSuffix(" с.")
         .setValue(3.0F)
   );
   private final NumberSetting delaySetting = this.addSetting(
      new NumberSetting("Задержка (тики)", "Минимальная задержка между бросками зелий.")
         .withIntegerRange(1, 40)
         .withIntegerStep(1)
         .withSuffix(" т.")
         .setValue(10.0F)
   );
   private final BooleanSetting onlyOnGround = this.addSetting(
      new BooleanSetting("Только на земле", "Кидать зелье только когда персонаж стоит на земле (чтобы эффект точно попал).", true)
   );
   private final BooleanSetting onlyInCombat = this.addSetting(
      new BooleanSetting("Только в бою", "Баффаться только во время боя (активна Аура, получен урон или рядом враги).", false)
   );
   private final BooleanSetting pullFromInventory = this.addSetting(
      new BooleanSetting("Брать из инвентаря", "Если нужного зелья нет в хотбаре, автоматически брать его из инвентаря.", true)
   );
   private final BooleanSetting revertSlot = this.addSetting(
      new BooleanSetting("Возвращать слот", "Возвращать прежний выбранный слот оружия после броска зелья.", true)
   );
   private final NumberSetting revertDelaySetting = this.addSetting(
      new NumberSetting("Задержка возврата", "Задержка в тиках перед возвратом прежнего слота (для стабильности серверов).")
         .withIntegerRange(1, 5)
         .withIntegerStep(1)
         .withSuffix(" т.")
         .setValue(2.0F)
   );
   private final BooleanSetting notifications = this.addSetting(
      new BooleanSetting("Уведомления", "Показывать всплывающее уведомление при использовании зелья.", true)
   );

   private int cooldownTicks = 0;
   private int pendingRevertSlot = -1;
   private int revertTicksRemaining = 0;

   private final EventListener<PlayerTickEvent> playerTickListener = event -> this.onTick();

   private AutoBuffModule() {
   }

   public static AutoBuffModule getInstance() {
      if (autoBuffModule == null) {
         autoBuffModule = new AutoBuffModule();
      }
      return autoBuffModule;
   }

   @Override
   protected void onDisable() {
      this.pendingRevertSlot = -1;
      this.revertTicksRemaining = 0;
      this.cooldownTicks = 0;
   }

   private void onTick() {
      if (mc.player == null || mc.world == null) {
         this.pendingRevertSlot = -1;
         this.revertTicksRemaining = 0;
         this.cooldownTicks = 0;
         return;
      }

      // Revert slot handling
      if (this.revertTicksRemaining > 0) {
         this.revertTicksRemaining--;
         if (this.revertTicksRemaining == 0 && this.pendingRevertSlot != -1) {
            if (this.revertSlot.isChecked() && InventoryUtils.getSelectedSlot() != this.pendingRevertSlot) {
               this.switchSlot(this.pendingRevertSlot);
            }
            this.pendingRevertSlot = -1;
         }
      }

      if (this.cooldownTicks > 0) {
         this.cooldownTicks--;
         return;
      }

      if (mc.currentScreen != null) {
         return;
      }

      if (mc.player.isUsingItem() || mc.player.isDead()) {
         return;
      }

      if (this.onlyOnGround.isChecked() && !mc.player.isOnGround()) {
         return;
      }

      if (this.onlyInCombat.isChecked() && !this.isInCombat()) {
         return;
      }

      this.processAutoBuff();
   }

   private void processAutoBuff() {
      int minDurationTicks = Math.round(this.minDurationSetting.getValue() * 20.0F);

      for (BuffType type : BuffType.values()) {
         if (!this.isBuffEnabled(type)) {
            continue;
         }

         if (!this.needsBuff(type, minDurationTicks)) {
            continue;
         }

         // 1. Check offhand
         if (this.matchesPotion(mc.player.getOffHandStack(), type)) {
            this.throwPotionFromOffhand(type);
            return;
         }

         // 2. Check hotbar (0..8)
         int hotbarSlot = this.findPotionInHotbar(type);
         if (hotbarSlot != -1) {
            this.throwPotionFromHotbar(hotbarSlot, type);
            return;
         }

         // 3. Check inventory if pulling enabled
         if (this.pullFromInventory.isChecked()) {
            int invSlot = this.findPotionInInventory(type);
            if (invSlot != -1) {
               if (this.pullPotionToHotbar(invSlot)) {
                  this.cooldownTicks = 2;
                  return;
               }
            }
         }
      }
   }

   private void throwPotionFromHotbar(int slot, BuffType type) {
      if (mc.player == null || mc.interactionManager == null) {
         return;
      }

      int originalSlot = (this.pendingRevertSlot != -1) ? this.pendingRevertSlot : InventoryUtils.getSelectedSlot();
      if (slot != InventoryUtils.getSelectedSlot()) {
         this.switchSlot(slot);
      }

      float prevPitch = mc.player.getPitch();
      mc.player.setPitch(90.0F);

      mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
      mc.player.swingHand(Hand.MAIN_HAND);

      mc.player.setPitch(prevPitch);

      if (this.revertSlot.isChecked() && slot != originalSlot) {
         this.pendingRevertSlot = originalSlot;
         this.revertTicksRemaining = Math.max(1, Math.round(this.revertDelaySetting.getValue()));
      }

      this.cooldownTicks = Math.max(1, Math.round(this.delaySetting.getValue()));

      if (this.notifications.isChecked()) {
         NotificationHud.notifyWithText(
            this.category.getIcon(),
            "AutoBuff",
            Text.literal("Использовано: " + type.displayName).setStyle(Style.EMPTY.withColor(Formatting.GREEN)),
            true
         );
      }
   }

   private void throwPotionFromOffhand(BuffType type) {
      if (mc.player == null || mc.interactionManager == null) {
         return;
      }

      float prevPitch = mc.player.getPitch();
      mc.player.setPitch(90.0F);

      mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
      mc.player.swingHand(Hand.OFF_HAND);

      mc.player.setPitch(prevPitch);

      this.cooldownTicks = Math.max(1, Math.round(this.delaySetting.getValue()));

      if (this.notifications.isChecked()) {
         NotificationHud.notifyWithText(
            this.category.getIcon(),
            "AutoBuff",
            Text.literal("Использовано: " + type.displayName).setStyle(Style.EMPTY.withColor(Formatting.GREEN)),
            true
         );
      }
   }

   private boolean pullPotionToHotbar(int invSlot) {
      if (mc.player == null || mc.player.currentScreenHandler != mc.player.playerScreenHandler) {
         return false;
      }
      if (InventoryActionQueue.getInstance().hasPendingActions() || InventorySlotManager.getInstance().isLocked()) {
         return false;
      }

      int targetHotbarSlot = this.findBestHotbarSlotForPotion();
      if (targetHotbarSlot != -1) {
         InventoryActionQueue.getInstance().queueSlotSwap(invSlot, targetHotbarSlot, 1);
         return true;
      }
      return false;
   }

   private int findBestHotbarSlotForPotion() {
      // 1. Look for an empty slot in hotbar (0..8)
      for (int i = 0; i < 9; i++) {
         if (mc.player.getInventory().getStack(i).isEmpty()) {
            return i;
         }
      }
      // 2. Look for a slot that is not currently selected, and not sword/mace/axe/totem/gapple
      int current = InventoryUtils.getSelectedSlot();
      for (int i = 8; i >= 0; i--) {
         if (i == current) {
            continue;
         }
         ItemStack s = mc.player.getInventory().getStack(i);
         if (!s.isOf(Items.TOTEM_OF_UNDYING)
            && !s.isOf(Items.NETHERITE_SWORD)
            && !s.isOf(Items.DIAMOND_SWORD)
            && !s.isOf(Items.NETHERITE_AXE)
            && !s.isOf(Items.DIAMOND_AXE)
            && !s.isOf(Items.MACE)
            && !s.isOf(Items.ENCHANTED_GOLDEN_APPLE)
            && !s.isOf(Items.GOLDEN_APPLE)) {
            return i;
         }
      }
      return (current == 8) ? 7 : 8;
   }

   private void switchSlot(int slot) {
      if (mc.player != null && slot >= 0 && slot <= 8) {
         mc.player.getInventory().setSelectedSlot(slot);
         if (mc.player.networkHandler != null) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
         }
      }
   }

   private boolean isBuffEnabled(BuffType type) {
      return switch (type) {
         case HEAL -> this.healSetting.isChecked();
         case STRENGTH -> this.strengthSetting.isChecked();
         case SPEED -> this.speedSetting.isChecked();
         case REGEN -> this.regenSetting.isChecked();
         case FIRE_RES -> this.fireResSetting.isChecked();
      };
   }

   private boolean needsBuff(BuffType type, int minDurationTicks) {
      if (type == BuffType.HEAL) {
         return mc.player.getHealth() <= this.healHpSetting.getValue();
      }

      StatusEffectInstance instance = mc.player.getStatusEffect(type.effect);
      return instance == null || instance.getDuration() <= minDurationTicks;
   }

   private int findPotionInHotbar(BuffType type) {
      int bestSlot = -1;
      int bestAmp = -1;
      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (this.matchesPotion(stack, type)) {
            int amp = this.getPotionAmplifier(stack, type);
            if (amp > bestAmp) {
               bestAmp = amp;
               bestSlot = i;
            }
         }
      }
      return bestSlot;
   }

   private int findPotionInInventory(BuffType type) {
      for (int i = 9; i < 36; i++) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (this.matchesPotion(stack, type)) {
            return i;
         }
      }
      return -1;
   }

   private int getPotionAmplifier(ItemStack stack, BuffType type) {
      try {
         PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
         if (contents != null) {
            for (StatusEffectInstance effectInst : contents.getEffects()) {
               if (effectInst.getEffectType().equals(type.effect)) {
                  return effectInst.getAmplifier();
               }
            }
         }
      } catch (Exception ignored) {
      }
      String name = Formatting.strip(stack.getName().getString()).toLowerCase();
      if (name.contains(" ii") || name.contains(" 2") || name.contains("два") || name.contains("втор")) {
         return 1;
      }
      return 0;
   }

   private boolean matchesPotion(ItemStack stack, BuffType type) {
      if (stack.isEmpty() || !this.isThrowablePotion(stack)) {
         return false;
      }

      // 1. Check DataComponentTypes.POTION_CONTENTS
      try {
         PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
         if (contents != null) {
            for (StatusEffectInstance effectInst : contents.getEffects()) {
               if (effectInst.getEffectType().equals(type.effect)) {
                  return true;
               }
            }
         }
      } catch (Exception ignored) {
      }

      // 2. Check lore and custom name for server potions (ReallyWorld, FunTime, SpookyTime)
      String name = Formatting.strip(stack.getName().getString()).toLowerCase();
      if (this.matchesTextForType(name, type)) {
         return true;
      }

      LoreComponent lore = stack.get(DataComponentTypes.LORE);
      if (lore != null) {
         for (Text line : lore.lines()) {
            String lineStr = Formatting.strip(line.getString()).toLowerCase();
            if (this.matchesTextForType(lineStr, type)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean matchesTextForType(String s, BuffType type) {
      return switch (type) {
         case STRENGTH -> s.contains("сил") || s.contains("strength");
         case SPEED -> s.contains("скорост") || s.contains("стремительн") || s.contains("speed") || s.contains("быстрот");
         case FIRE_RES -> s.contains("огнестойк") || s.contains("fire_res") || s.contains("огн");
         case REGEN -> s.contains("реген") || s.contains("regen");
         case HEAL -> s.contains("исцелен") || s.contains("лечени") || s.contains("хил") || s.contains("heal");
      };
   }

   private boolean isThrowablePotion(ItemStack stack) {
      if (stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION)) {
         return true;
      }
      String name = stack.getName().getString().toLowerCase();
      if (name.contains("взрывн") || name.contains("туманн") || name.contains("всплеск") || name.contains("splash")) {
         return true;
      }
      LoreComponent lore = stack.get(DataComponentTypes.LORE);
      if (lore != null) {
         for (Text line : lore.lines()) {
            String l = line.getString().toLowerCase();
            if (l.contains("взрывн") || l.contains("splash")) {
               return true;
            }
         }
      }
      return false;
   }

   private boolean isInCombat() {
      if (mc.player == null) {
         return false;
      }
      try {
         if (AttackAuraModule.getInstance().isEnabled() && AttackAuraModule.getInstance().getTarget() != null) {
            return true;
         }
      } catch (Exception ignored) {
      }
      if (mc.player.hurtTime > 0 || (mc.player.age - mc.player.getLastAttackedTime() < 100)) {
         return true;
      }
      if (mc.world != null) {
         for (PlayerEntity other : mc.world.getPlayers()) {
            if (other != mc.player && other.isAlive() && !other.isSpectator() && mc.player.distanceTo(other) <= 10.0F) {
               return true;
            }
         }
      }
      return false;
   }

   private enum BuffType {
      HEAL("Исцеление", StatusEffects.INSTANT_HEALTH),
      STRENGTH("Сила", StatusEffects.STRENGTH),
      SPEED("Скорость", StatusEffects.SPEED),
      REGEN("Регенерация", StatusEffects.REGENERATION),
      FIRE_RES("Огнестойкость", StatusEffects.FIRE_RESISTANCE);

      final String displayName;
      final RegistryEntry<StatusEffect> effect;

      BuffType(String displayName, RegistryEntry<StatusEffect> effect) {
         this.displayName = displayName;
         this.effect = effect;
      }
   }
}

package horus.client.module.player;

import horus.client.setting.BooleanSetting;
import horus.client.event.EventListener;
import horus.client.setting.KeybindSetting;
import horus.client.setting.ModeSetting;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.event.MovementInputEvent;
import horus.client.event.PacketEvent;
import horus.client.event.PlayerTickEvent;
import horus.client.util.InventoryUtils;
import horus.client.util.InventoryActionQueue;
import horus.client.ui.hud.NotificationHud;
import horus.client.util.InventorySlotManager;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.AttributeModifiersComponent.Entry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "ElytraHelper", description = "Выполняет действия с элитрой по нажатию клавиши с обходом античитов.", category = ModuleCategory.PLAYER)
public final class ElytraHelperModule extends Module {
   private static ElytraHelperModule elytraHelperModule;
   public KeybindSetting toggleKey;
   public KeybindSetting fireworkKey;
   public ModeSetting bypassMode;
   public BooleanSetting swapOnUse;
   public BooleanSetting autoStartAfterSwap;
   public BooleanSetting grimTakeoff;
   public BooleanSetting pitchOnTakeoff;
   public BooleanSetting useFlyOnSwap;
   public BooleanSetting autoFirework;

   private boolean flag;
   private boolean flag2;
   private int intVal;
   private boolean flag3;
   private boolean flag4;
   private int grimTakeoffTicks;
   private boolean grimTakingOff;

   private final EventListener<PlayerTickEvent> playerTickListener = playerTickEvent -> {
      if (mc.player != null && mc.interactionManager != null) {
         if (mc.currentScreen != null) {
            this.flag3 = this.toggleKey.getBinding().isPressed(mc.getWindow().getHandle());
            this.flag4 = this.fireworkKey.getBinding().isPressed(mc.getWindow().getHandle());
         } else {
            if (this.toggleKey.getBinding().isPressed(mc.getWindow().getHandle()) && !this.flag3) {
               this.updateState();
            }

            this.flag3 = this.toggleKey.getBinding().isPressed(mc.getWindow().getHandle());
            if (this.fireworkKey.getBinding().isPressed(mc.getWindow().getHandle()) && !this.flag4) {
               this.updateState2();
            }

            this.flag4 = this.fireworkKey.getBinding().isPressed(mc.getWindow().getHandle());
         }

         if (this.grimTakingOff && (this.bypassMode.isSelected("Grim") || this.bypassMode.isSelected("FunTime"))) {
            this.handleGrimTakeoff();
         }
      }
   };

   private final EventListener<MovementInputEvent> movementInputListener = event -> {
      if (mc.player != null) {
         if (mc.player.age < 5) {
            this.flag = false;
         } else if (this.flag && this.checkState()) {
            this.onMovementInput(event);
         }
      }
   };

   public final EventListener<PacketEvent> packetListener = packetEvent -> {
      if (packetEvent.isIncoming()
         && packetEvent.getPacket() instanceof GameMessageS2CPacket gameMessageS2CPacket
         && this.flag
         && gameMessageS2CPacket.content().getString().contains("Эту команду могут писать только донатеры выше рангом")) {
         this.flag2 = true;
      }
   };

   private boolean matchesCriteria(int local1336_1) {
      return this.intVal + local1336_1 < mc.player.age;
   }

   private void handleGrimTakeoff() {
      if (mc.player == null || mc.world == null) {
         this.grimTakingOff = false;
         return;
      }

      boolean hasElytra = mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA);
      if (!hasElytra) {
         this.grimTakeoffTicks++;
         if (this.grimTakeoffTicks > 20) {
            this.grimTakingOff = false;
         }
         return;
      }

      // If already gliding, deploy firework if needed and finish
      if (mc.player.isGliding()) {
         if (this.autoFirework.isChecked()) {
            this.updateState2();
         }
         this.grimTakingOff = false;
         this.grimTakeoffTicks = 0;
         this.flag = false;
         return;
      }

      // Phase 1: On ground -> clean vanilla jump
      if (mc.player.isOnGround()) {
         mc.player.jump();
         this.grimTakeoffTicks = 1;
         return;
      }

      // Phase 2: Airborne -> at peak of jump (ticks 2 to 4), deploy fall flying cleanly
      this.grimTakeoffTicks++;
      if (this.grimTakeoffTicks >= 2 && !mc.player.isGliding()) {
         if (this.pitchOnTakeoff.isChecked() && mc.player.getPitch() > 0.0F) {
            mc.player.setPitch(-20.0F);
         }

         if (mc.player.networkHandler != null) {
            mc.player.networkHandler.sendPacket(
               new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING)
            );
         }
         mc.player.startGliding();

         if (this.autoFirework.isChecked()) {
            this.updateState2();
         }
         this.grimTakingOff = false;
         this.grimTakeoffTicks = 0;
         this.flag = false;
      }

      if (this.grimTakeoffTicks > 25) {
         this.grimTakingOff = false;
         this.grimTakeoffTicks = 0;
      }
   }

   private void onMovementInput(MovementInputEvent event) {
      if (this.bypassMode.isSelected("Grim") || this.bypassMode.isSelected("FunTime")) {
         return;
      }

      boolean local1337_2 = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
      if (!mc.player.isGliding() && local1337_2) {
         event.setJumping(mc.player.age % 2 == 0);
         this.intVal = mc.player.age;
         if (!this.autoFirework.isChecked()) {
            this.flag = false;
         }
      }

      if (mc.player.isGliding()
         && this.autoFirework.isChecked()
         && !mc.player.isTouchingWater()
         && !mc.player.isOnGround()
         && this.matchesCriteria(2)) {
         this.updateState2();
         this.flag = false;
      }

      if (this.matchesCriteria(10)) {
         this.flag = false;
      }
   }

   private boolean checkState() {
      if (this.bypassMode.isSelected("Grim") || this.bypassMode.isSelected("FunTime")) {
         return false;
      }

      if (this.useFlyOnSwap.isChecked() && !this.flag2) {
         if (this.intVal + 1 == mc.player.age) {
            mc.player.networkHandler.sendChatCommand("fly");
         }

         if (mc.player.getAbilities().allowFlying && this.intVal <= mc.player.age && mc.player.isOnGround()) {
            double x = mc.player.getX();
            double local1338_3 = mc.player.getY() + 0.19999997F;
            double z = mc.player.getZ();
            mc.player
               .networkHandler
               .sendPacket(new PositionAndOnGround(x, local1338_3, z, false, mc.player.horizontalCollision));
            mc.player.setPosition(x, local1338_3, z);
            this.intVal = mc.player.age + 9;
         }

         if (mc.player.getAbilities().allowFlying && !mc.player.isOnGround()) {
            mc.player.getAbilities().flying = true;
            mc.player.setVelocity(Vec3d.ZERO);
            mc.player.sendAbilitiesUpdate();
            this.flag = false;
         }

         if (!this.matchesCriteria(15) && (!this.matchesCriteria(3) || mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA)) {
            return false;
         }

         this.flag = false;
         return false;
      } else {
         return true;
      }
   }

   private void updateState() {
      if (!InventoryActionQueue.getInstance().hasPendingActions() && !InventorySlotManager.getInstance().isLocked()) {
         boolean wearingElytra = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
         int targetSlot = wearingElytra ? this.computeInt() : InventoryUtils.findInventorySlot(Items.ELYTRA);
         if (targetSlot == -1) {
            NotificationHud.notifyWithText(
               this.category.getIcon(),
               (wearingElytra ? Items.NETHERITE_CHESTPLATE : Items.ELYTRA).getDefaultStack().getName().getString(),
               Text.literal("нет в инвентаре").setStyle(Style.EMPTY.withColor(Formatting.RED)),
               true
            );
         } else {
            if (this.swapOnUse.isChecked()) {
               InventorySlotManager.getInstance().processData3(mc.player.getInventory().getStack(targetSlot).copy(), true);
            } else {
               InventoryActionQueue.getInstance().processData8(targetSlot, 6, 1);
            }

            if (this.autoStartAfterSwap.isChecked() && !wearingElytra) {
               this.intVal = mc.player.age;
               this.flag = true;
               if (this.bypassMode.isSelected("Grim") || this.bypassMode.isSelected("FunTime")) {
                  this.grimTakingOff = true;
                  this.grimTakeoffTicks = 0;
               }
            } else if (wearingElytra) {
               this.grimTakingOff = false;
               this.grimTakeoffTicks = 0;
            }
         }
      }
   }

   private void updateState2() {
      if (mc.player == null) return;
      if (mc.player.isGliding() || !this.bypassMode.isSelected("Grim")) {
         // 1. Check offhand first (instant, 0 inventory sync)
         if (mc.player.getOffHandStack().isOf(Items.FIREWORK_ROCKET)) {
            if (mc.interactionManager != null) {
               mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
               mc.player.swingHand(Hand.OFF_HAND);
            }
            return;
         }

         // 2. Check hotbar
         int hotbarSlot = InventoryUtils.findHotbarSlot(Items.FIREWORK_ROCKET);
         if (hotbarSlot != -1) {
            int prevSlot = InventoryUtils.getSelectedSlot();
            InventoryUtils.setSelectedSlot(hotbarSlot, false);
            if (mc.interactionManager != null) {
               mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
               mc.player.swingHand(Hand.MAIN_HAND);
            }
            InventoryUtils.setSelectedSlot(prevSlot, false);
            return;
         }

         // 3. Fallback to InventorySlotManager
         if (!InventorySlotManager.getInstance().matchesCriteria2(Items.FIREWORK_ROCKET.getDefaultStack())) {
            NotificationHud.notifyWithText(
               this.category.getIcon(),
               Items.FIREWORK_ROCKET.getDefaultStack().getName().getString(),
               Text.literal("нет в инвентаре").setStyle(Style.EMPTY.withColor(Formatting.RED)),
               true
            );
         } else {
            InventorySlotManager.getInstance().processData3(Items.FIREWORK_ROCKET.getDefaultStack(), true);
         }
      }
   }

   public static ElytraHelperModule getInstance() {
      if (elytraHelperModule == null) {
         elytraHelperModule = new ElytraHelperModule();
      }

      return elytraHelperModule;
   }

   @Override
   protected void onDisable() {
      this.flag = false;
      this.grimTakingOff = false;
      this.grimTakeoffTicks = 0;
   }

   private int computeInt() {
      RegistryEntry<Enchantment> orThrow = mc.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION);
      int bestSlot = -1;
      double bestScore = 0.0;

      for (int index = 0; index < 36; index++) {
         ItemStack stack = mc.player.getInventory().getStack(index);
         if (!stack.isEmpty()
            && stack.getItem() != Items.ELYTRA
            && stack.get(DataComponentTypes.EQUIPPABLE) != null
            && stack.get(DataComponentTypes.EQUIPPABLE).slot() == EquipmentSlot.CHEST) {
            double level = EnchantmentHelper.getLevel(orThrow, stack);
            AttributeModifiersComponent attributeModifiersComponent = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            if (attributeModifiersComponent != null) {
               for (Entry entry : attributeModifiersComponent.modifiers()) {
                  if (entry.attribute() == EntityAttributes.ARMOR || entry.attribute() == EntityAttributes.ARMOR_TOUGHNESS) {
                     level += entry.modifier().value();
                  }
               }
            }

            if (level > bestScore) {
               bestScore = level;
               bestSlot = index;
            }
         }
      }

      return bestSlot;
   }

   private ElytraHelperModule() {
      this.toggleKey = this.addSetting(new KeybindSetting("Кнопка переключения", "Надевает элитры вместо нагрудника и обратно."));
      this.fireworkKey = this.addSetting(new KeybindSetting("Кнопка фейерверка", "Использует фейерверк в полёте по нажатию."));
      this.bypassMode = this.addSetting(
         new ModeSetting("Режим обхода", "Метод обхода античита при взлёте.", "Grim", "Grim", "Vanilla", "FunTime")
      );
      this.swapOnUse = this.addSetting(
         new BooleanSetting(
            "Свап использованием", "Меняет нагрудник правым кликом по надеваемому предмету (1.21.2+) вместо трёх кликов по слотам брони.", true
         )
      );
      this.autoStartAfterSwap = this.addSetting(new BooleanSetting("Автостарт после свапа", "Сам взлетает после свапа на элитры.", true));
      this.grimTakeoff = this.addSetting(
         new BooleanSetting("Grim Взлёт", "Идеальный тайминг прыжка и деплоя элитр без киков за ElytraFly.", true)
            .withVisibility(() -> this.autoStartAfterSwap.isChecked() && (this.bypassMode.isSelected("Grim") || this.bypassMode.isSelected("FunTime")))
      );
      this.pitchOnTakeoff = this.addSetting(
         new BooleanSetting("Угол при взлёте", "Автоматически приподнимает угол прицела (-20°) для плавного разгона на Grim.", true)
            .withVisibility(() -> this.autoStartAfterSwap.isChecked() && (this.bypassMode.isSelected("Grim") || this.bypassMode.isSelected("FunTime")))
      );
      this.useFlyOnSwap = this.addSetting(
         new BooleanSetting("Использовать /fly при свапе", "Взлетает через /fly, пока сервер разрешает.", false)
            .withVisibility(() -> this.autoStartAfterSwap.isChecked() && this.bypassMode.isSelected("Vanilla"))
      );
      this.autoFirework = this.addSetting(
         new BooleanSetting("Автофейерверк", "Запускает фейерверк, как только полёт начался.", true).withVisibility(() -> this.autoStartAfterSwap.isChecked())
      );
   }
}

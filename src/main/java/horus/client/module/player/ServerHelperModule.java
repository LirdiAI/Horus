package horus.client.module.player;

import horus.client.util.TargetHudStyle;
import horus.client.util.EventSearchManager;

import horus.client.setting.BooleanSetting;
import horus.client.setting.NumberSetting;
import horus.mixin.ClientPlayerInteractionManagerMixin2;
import horus.mixin.ContainerComponentMixin;
import horus.client.event.EventListener;
import horus.client.render.FontFace;
import horus.client.event.HudRenderEvent;
import horus.client.util.ItemCooldownAccess;
import horus.client.setting.KeybindSetting;
import horus.client.setting.ModeSetting;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.MultiSelectSetting;
import horus.client.event.PacketEvent;
import horus.client.event.PlayerTickEvent;
import horus.client.util.InventoryUtils;
import horus.client.util.GuiScale;
import horus.client.util.WaypointManager;
import horus.client.render.FontText;
import horus.client.util.InventoryActionQueue;
import horus.client.ui.hud.NotificationHud;
import horus.client.module.movement.MovementPauseController;
import horus.client.util.TargetHudStyle;
import horus.client.util.EventSearchManager;
import horus.client.util.InventorySlotManager;
import horus.client.ui.hud.CooldownsHud;
import horus.client.RotationAngles;
import horus.client.RotationManager;
import horus.client.RotationSpeed;
import horus.client.module.movement.ScreenWalkModule;
import horus.client.setting.SectionSetting;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import java.util.function.Predicate;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.BundleItemSelectedC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

   @ModuleInfo(name = "ServerHelper", description = "Бинды предметов для SpookyTime, FunTime, ReallyWorld, LonyGrief и HolyWorld.", category = ModuleCategory.PLAYER)
   public final class ServerHelperModule extends Module {
      private static ServerHelperModule serverHelperModule;
      public ModeSetting modeSetting;
      public BooleanSetting smartScroll;
      public MultiSelectSetting multiSelectSetting;
      public BooleanSetting autoPitch;
      public BooleanSetting showArrows;
      public BooleanSetting eventWaypoint;
      public BooleanSetting autoPlast;
      public BooleanSetting eventSearch;
      public KeybindSetting eventSearchKey;
      public BooleanSetting eventSearchAutoCapture;
      public BooleanSetting eventSearchTracers;
      public BooleanSetting eventSearchBoxes;
      public NumberSetting eventSearchDistance;
      public BooleanSetting clearEventTarget;
      private static final Pattern pattern = Pattern.compile("(?m)\\|\\|\\|\\s+\\[([^\\]]+)]|^\\s*\\[\\d+]([^:\\n]+):");
      private static final Pattern pattern2 = Pattern.compile("(?:Координаты: \\[|/gps )(-?\\d{1,7})[ ,]+(-?\\d{1,7})[ ,]+(-?\\d{1,7})");
      private static final Pattern pattern3 = Pattern.compile("Призван игроком: ([^\\n|]+)");
      private static final Pattern pattern4 = Pattern.compile("Уровень лута: ([^\\n|]+)");
      private static final java.util.Set<String> BAD_EFFECTS = java.util.Set.of(
         "poison", "wither", "slowness", "hunger", "instant_damage",
         "weakness", "mining_fatigue", "blindness", "radiation", "glowing"
      );
      private final ArrayList<ServerHelperModule.SubContext> list = new ArrayList<>();
      private long timeMillis;
      private long timeMillis2;
      private ServerHelperModule.SubContext nested0360;
      private int intVal;
      private int intVal2;
      private int intVal3;
      private int autoPlastCooldown;
      private double plastPlaceX;
      private double plastPlaceY;
      private double plastPlaceZ;
      private float plastTargetYaw;
      private float plastTargetPitch;
      private long plastRotateTime;
    private final EventListener<PlayerTickEvent> playerTickListener = playerTickEvent -> {
       this.updateState();
       if (this.autoPlast.isChecked() && mc.player != null && mc.world != null && mc.interactionManager != null) {
          this.handleAutoPlast();
       }
    };
   private final EventListener<HudRenderEvent> hudRenderListener = hudRenderEvent -> this.updateState();
    private final EventListener<PacketEvent> packetListener = packetEvent -> {
       if (packetEvent.isOutgoing()
          && mc.player != null
          && packetEvent.getPacket() instanceof PlayerInteractItemC2SPacket playerInteractItemC2SPacket) {
          if (System.currentTimeMillis() < this.plastRotateTime) {
             packetEvent.setPacket(new PlayerInteractItemC2SPacket(playerInteractItemC2SPacket.getHand(), playerInteractItemC2SPacket.getSequence(), this.plastTargetYaw, this.plastTargetPitch));
             this.plastRotateTime = 0L;
          } else if (ScreenWalkModule.getInstance().checkState() && System.currentTimeMillis() < this.timeMillis2) {
             packetEvent.setPacket(new PlayerInteractItemC2SPacket(playerInteractItemC2SPacket.getHand(), playerInteractItemC2SPacket.getSequence(), playerInteractItemC2SPacket.getYaw(), 90.0F));
             this.timeMillis2 = 0L;
          }
       } else {
         if (packetEvent.isIncoming() && this.eventWaypoint.isChecked() && packetEvent.getPacket() instanceof GameMessageS2CPacket gameMessageS2CPacket) {
             Matcher matcher = pattern.matcher(Formatting.strip(gameMessageS2CPacket.content().getString()));
             Matcher matcher2 = pattern2.matcher(Formatting.strip(gameMessageS2CPacket.content().getString() + gameMessageS2CPacket.content().toString()));
             Matcher matcher3 = pattern3.matcher(Formatting.strip(gameMessageS2CPacket.content().getString()));
             Matcher matcher4 = pattern4.matcher(Formatting.strip(gameMessageS2CPacket.content().getString()));
             if (matcher.find()) {
                mc.execute(
                   () -> this.processData(
                      (matcher.group(1) == null ? matcher.group(2) : matcher.group(1)).trim(),
                      matcher2.find()
                         ? new BlockPos(Integer.parseInt(matcher2.group(1)), Integer.parseInt(matcher2.group(2)), Integer.parseInt(matcher2.group(3)))
                         : null,
                      matcher3.find() ? "Призван: " + matcher3.group(1).trim() : null,
                      matcher4.find() ? "Лут: " + matcher4.group(1).trim() : null
                   )
                );
             }
          }

          if (packetEvent.isIncoming()
             && packetEvent.getPacket() instanceof OverlayMessageS2CPacket overlayMessageS2CPacket
             && overlayMessageS2CPacket.text().getString().contains("будет доступн")) {
             Matcher matcher5 = Pattern.compile("(.*через )(\\d[\\d.,]*)( ?сек.*)").matcher(Formatting.strip(overlayMessageS2CPacket.text().getString()));
             if (matcher5.matches() && System.currentTimeMillis() - this.timeMillis >= 2000L) {
                this.timeMillis = System.currentTimeMillis();
                mc.execute(
                   () -> {
                      NotificationHud.notifyWithText(
                         this.category.getIcon(),
                         "Кулдаун",
                         Text.literal(matcher5.group(1))
                            .setStyle(Style.EMPTY.withColor(Formatting.WHITE))
                            .append(Text.literal(matcher5.group(2) + matcher5.group(3)).setStyle(Style.EMPTY.withColor(Formatting.RED))),
                         true
                      );
                      CooldownsHud.processData2("Исцел/Святая вода", Items.SPLASH_POTION, Float.parseFloat(matcher5.group(2).replace(',', '.')));
                   }
                );
             }
          }
       }
    };
   private final EventListener<HudRenderEvent> hudRenderListener2 = hudRenderEvent2 -> {
      if (mc.player != null && this.showArrows.isChecked()) {
         int local905_2 = 0;

         for (int index = 0; index < mc.player.getInventory().size(); index++) {
            if (mc.player.getInventory().getStack(index).isIn(ItemTags.ARROWS)) {
               local905_2 += mc.player.getInventory().getStack(index).getCount();
            }
         }

         String local905_3 = String.valueOf(local905_2);
         float local905_4 = GuiScale.computeInt() / GuiScale.computeFloat();
         float local905_5 = 6.0F * local905_4;
         float local905_6 = 0.7F * local905_4;

         for (int index2 = 0; index2 < 9; index2++) {
            if (mc.player.getInventory().getStack(index2).isOf(Items.BOW)
               || mc.player.getInventory().getStack(index2).isOf(Items.CROSSBOW)) {
               float local905_8 = (hudRenderEvent2.computeInt2() / 2.0F - 96.0F + index2 * 20.0F + 16.0F) * local905_4
                  - FontText.computeFloat(FontFace.BOLD, local905_3, local905_5);
               float local905_9 = (hudRenderEvent2.computeInt() - 19.0F + 6.0F) * local905_4 - local905_5;
               FontText.processData3(FontFace.BOLD, local905_3, local905_8 - local905_6, local905_9, local905_5, -16777216);
               FontText.processData3(FontFace.BOLD, local905_3, local905_8 + local905_6, local905_9, local905_5, -16777216);
               FontText.processData3(FontFace.BOLD, local905_3, local905_8, local905_9 - local905_6, local905_5, -16777216);
               FontText.processData3(FontFace.BOLD, local905_3, local905_8, local905_9 + local905_6, local905_5, -16777216);
               FontText.processData3(FontFace.BOLD, local905_3, local905_8, local905_9, local905_5, -1);
            }
         }
      }
   };
   private final EventListener<PlayerTickEvent> playerTickListener2 = playerTickEvent2 -> {
      if (this.nested0360 != null && mc.player != null && mc.interactionManager != null) {
         this.intVal3++;
         switch (this.intVal2) {
            case 0:
               if (MovementPauseController.getInstance().getRemainingTicks() >= 1) {
                  return;
               }

               if (mc.player.currentScreenHandler != mc.player.playerScreenHandler
                  || !this.matchesCriteria(mc.player.getInventory().getStack(this.intVal), this.nested0360)) {
                  this.nested0360 = null;
                  return;
               }

               mc.interactionManager
                  .clickSlot(
                     mc.player.playerScreenHandler.syncId,
                     this.intVal < 9 ? this.intVal + 36 : this.intVal,
                     1,
                     SlotActionType.PICKUP,
                     mc.player
                  );
               this.intVal2 = 1;
               this.intVal3 = 0;
               break;
            case 1: {
               ScreenHandler screenHandler = mc.player.currentScreenHandler;
               if (screenHandler != mc.player.playerScreenHandler && screenHandler.slots.size() > 36) {
                  if (!mc.player.playerScreenHandler.getCursorStack().isEmpty() && mc.player.getInventory().getStack(this.intVal).isEmpty()
                     )
                   {
                     mc.player.getInventory().setStack(this.intVal, mc.player.playerScreenHandler.getCursorStack());
                     mc.player.playerScreenHandler.setCursorStack(ItemStack.EMPTY);
                  }

                  if (this.computeInt3(screenHandler) == -1) {
                     if (this.intVal3 <= 5) {
                        return;
                     }

                     mc.player.closeHandledScreen();
                     this.nested0360 = null;
                     return;
                  }

                  MovementPauseController.getInstance().pauseForTicks(1);
                  this.intVal2 = 2;
                  break;
               }

               if (this.intVal3 <= 20) {
                  return;
               }

               if (!mc.player.playerScreenHandler.getCursorStack().isEmpty()) {
                  mc.interactionManager
                     .clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        this.intVal < 9 ? this.intVal + 36 : this.intVal,
                        0,
                        SlotActionType.PICKUP,
                        mc.player
                     );
               }

               NotificationHud.notifyWithText(
                  this.category.getIcon(), "Шалкер", Text.literal("не открылся").setStyle(Style.EMPTY.withColor(Formatting.RED)), true
               );
               this.nested0360 = null;
               return;
            }
            case 2: {
               if (MovementPauseController.getInstance().getRemainingTicks() >= 1) {
                  return;
               }

               ScreenHandler screenHandler2 = mc.player.currentScreenHandler;
               int local913_3 = screenHandler2.slots.size() - 36;
               int local913_4 = this.computeInt3(screenHandler2);
               int local913_5 = InventoryUtils.findEmptySlot(9, 35) != -1 ? InventoryUtils.findEmptySlot(9, 35) : InventoryUtils.findEmptySlot(0, 8);
               if (screenHandler2 != mc.player.playerScreenHandler && local913_4 != -1 && local913_5 != -1 && screenHandler2.getCursorStack().isEmpty()) {
                  boolean local913_6 = this.nested0360.matchesCriteria2(screenHandler2.getSlot(local913_4).getStack());
                  int local913_7 = local913_6 ? -1 : this.computeInt(screenHandler2.getSlot(local913_4).getStack(), this.nested0360);
                  if (!local913_6 && BundleItem.getSelectedStackIndex(screenHandler2.getSlot(local913_4).getStack()) != local913_7) {
                     screenHandler2.selectBundleStack(local913_4, local913_7);
                     mc.player.networkHandler.sendPacket(new BundleItemSelectedC2SPacket(local913_4, local913_7));
                  }

                  mc.interactionManager.clickSlot(screenHandler2.syncId, local913_4, local913_6 ? 0 : 1, SlotActionType.PICKUP, mc.player);
                  screenHandler2.nextRevision();
                  mc.interactionManager
                     .clickSlot(
                        screenHandler2.syncId,
                        local913_5 < 9 ? local913_3 + 27 + local913_5 : local913_3 + local913_5 - 9,
                        0,
                        SlotActionType.PICKUP,
                        mc.player
                     );
                  mc.player.closeHandledScreen();
                  this.intVal2 = 3;
                  break;
               }

               if (screenHandler2 != mc.player.playerScreenHandler) {
                  mc.player.closeHandledScreen();
               }

               this.nested0360 = null;
               return;
            }
            default: {
               ServerHelperModule.SubContext local913_2 = this.nested0360;
               this.nested0360 = null;
               this.processData9(local913_2);
            }
         }
      }
   };

   private int computeInt(ItemStack itemStack, ServerHelperModule.SubContext local903_2) {
      if (itemStack.get(DataComponentTypes.BUNDLE_CONTENTS) == null) {
         return -1;
      }

      for (int index = 0; index < itemStack.get(DataComponentTypes.BUNDLE_CONTENTS).size(); index++) {
         if (local903_2.matchesCriteria2(itemStack.get(DataComponentTypes.BUNDLE_CONTENTS).get(index))) {
            return index;
         }
      }

      return -1;
   }

   private void processData(String local904_1, BlockPos blockPos, String stringArray, String local904_4) {
      if (blockPos == null) {
         switch (local904_1.toLowerCase()) {
            case "сундук смерти":
               WaypointManager.getInstance().processData2(local904_1, new BlockPos(-155, 64, 205), 300, 0, stringArray, local904_4);
               break;
            case "адская резня":
               WaypointManager.getInstance().processData2(local904_1, new BlockPos(48, 87, 73), 180, 120, stringArray, local904_4);
         }
      } else {
         switch (local904_1.toLowerCase()) {
            case "мистический сундук":
               WaypointManager.getInstance().processData2(local904_1, blockPos, 300, 0, stringArray, local904_4);
               break;
            case "вулкан":
               WaypointManager.getInstance().processData2(local904_1, blockPos, 300, 120, stringArray, local904_4);
               break;
            case "метеоритный дождь":
               WaypointManager.getInstance().processData2(local904_1, blockPos, 360, 0, stringArray, local904_4);
               break;
            case "маяк убийца":
               WaypointManager.getInstance().processData2(local904_1, blockPos, 360, 0, stringArray, local904_4);
               break;
            case "мистический алтарь":
               WaypointManager.getInstance().processData2(local904_1, blockPos, 360, 0, stringArray, local904_4);
               break;
            case "загадочный маяк":
               WaypointManager.getInstance().processData2(local904_1, blockPos, 60, 180, stringArray, local904_4);
         }
      }
   }

   public int computeInt2(int local906_1, int local906_2) {
      if (this.isEnabled() && this.smartScroll.isChecked()) {
         int local906_3 = Math.floorMod(local906_2 - local906_1, 9) <= 4 ? 1 : -1;
         int local906_4 = local906_2;

         for (int index = 0; index < 9; index++) {
            if (!this.multiSelectSetting.isIndexSelected(local906_4)) {
               return local906_4;
            }

            local906_4 = Math.floorMod(local906_4 + local906_3, 9);
         }

         return local906_1;
      } else {
         return local906_2;
      }
   }

   private void processData2(ItemStack itemStack) {
      NotificationHud.notifyWithText(
         this.category.getIcon(),
         Formatting.strip(itemStack.getName().getString()),
         Text.literal("в кд ещё ")
            .setStyle(Style.EMPTY.withColor(Formatting.GRAY))
            .append(
               Text.literal(
                     String.format(
                        Locale.US, "%.1fс", ((ItemCooldownAccess)mc.player.getItemCooldownManager()).remainingCooldownTicks(itemStack) / 20.0F
                     )
                  )
                  .setStyle(Style.EMPTY.withColor(Formatting.RED))
            ),
         true
      );
   }

   private int computeInt3(ScreenHandler screenHandler) {
      for (int index = 0; index < screenHandler.slots.size() - 36; index++) {
         if (this.nested0360.matchesCriteria2(screenHandler.getSlot(index).getStack())
            || this.computeInt(screenHandler.getSlot(index).getStack(), this.nested0360) != -1) {
            return index;
         }
      }

      return -1;
   }

   public void processData3(String local911_1) {
      if (this.isEnabled() && this.autoPitch.isChecked() && mc.player != null && !local911_1.toLowerCase().matches(".*(радиац|снотворн).*")) {
         this.timeMillis2 = System.currentTimeMillis() + (ScreenWalkModule.getInstance().checkState() ? 500 : 200);
         if (!ScreenWalkModule.getInstance().checkState()) {
            RotationManager.getRotationManager()
               .setRotationWithTimeout(new RotationAngles(mc.player.getYaw(), 90.0F), new RotationSpeed(355.0F, 355.0F, 28.0F, 28.0F), 300L);
         }
      }
   }

   private void processData4(ServerHelperModule.SubContext local912_1) {
      if (local912_1.strVal != null && local912_1.item2 == null) {
         this.processData3(local912_1.strVal);
      }
   }

   private void updateState() {
      if (mc.player != null && mc.interactionManager != null && mc.currentScreen == null) {
         for (ServerHelperModule.SubContext local914_2 : this.list) {
            if (local914_2.keybindSetting.isVisible()
               && local914_2.keybindSetting.getBinding().isPressed(mc.getWindow().getHandle())
               && !local914_2.flag) {
               this.processData9(local914_2);
            }

            local914_2.flag = local914_2.keybindSetting.getBinding().isPressed(mc.getWindow().getHandle());
         }
      } else {
         for (ServerHelperModule.SubContext local914_2 : this.list) {
            local914_2.flag = local914_2.keybindSetting.getBinding().isPressed(mc.getWindow().getHandle());
         }
      }
   }

   public ArrayList<ServerHelperModule.SubContext> getArrayList() {
      return this.list;
   }

   private void processData5(ItemStack itemStack) {
      NotificationHud.notifyWithText(
         this.category.getIcon(),
         Formatting.strip(itemStack.getName().getString()),
         Text.literal("использовано").setStyle(Style.EMPTY.withColor(Formatting.GREEN)),
         true
      );
   }

   private void processData6(String newName, String... stringArray) {
      KeybindSetting currentKeybindSetting = this.addSetting(new KeybindSetting(newName, "Использует предмет по нажатию кнопки."));
      currentKeybindSetting.withVisibility(() -> !this.modeSetting.isSelected("HolyWorld") && !this.modeSetting.isSelected("ReallyWorld") && !this.modeSetting.isSelected("LonyGrief"));
      this.list.add(new ServerHelperModule.SubContext(newName, currentKeybindSetting, stringArray));
   }

   private void processData7(String newName, Item item, String local925_3) {
      KeybindSetting currentKeybindSetting = this.addSetting(new KeybindSetting(newName, "Использует предмет по нажатию кнопки."));
      currentKeybindSetting.withVisibility(() -> this.modeSetting.isSelected("HolyWorld"));
      this.list.add(new ServerHelperModule.SubContext(local925_3, item, currentKeybindSetting));
   }

   private void processData8(String newName, Item item, String local926_3) {
      KeybindSetting currentKeybindSetting = this.addSetting(new KeybindSetting(newName, "Использует предмет по нажатию кнопки."));
      currentKeybindSetting.withVisibility(() -> !this.modeSetting.isSelected("HolyWorld") && !this.modeSetting.isSelected("ReallyWorld") && !this.modeSetting.isSelected("LonyGrief"));
      this.list.add(new ServerHelperModule.SubContext(item, currentKeybindSetting));
   }

   private void processDataReallyWorld(String newName, Item item, Predicate<ItemStack> matcher) {
      KeybindSetting currentKeybindSetting = this.addSetting(new KeybindSetting(newName, "Использует предмет по нажатию кнопки."));
      currentKeybindSetting.withVisibility(() -> this.modeSetting.isSelected("ReallyWorld"));
      this.list.add(new ServerHelperModule.SubContext(newName, item, currentKeybindSetting, matcher));
   }

   private void processDataLonyGrief(String newName, Item item, Predicate<ItemStack> matcher) {
      KeybindSetting currentKeybindSetting = this.addSetting(new KeybindSetting(newName, "Использует предмет по нажатию кнопки."));
      currentKeybindSetting.withVisibility(() -> this.modeSetting.isSelected("LonyGrief"));
      this.list.add(new ServerHelperModule.SubContext(newName, item, currentKeybindSetting, matcher));
   }

   public void processData9(ServerHelperModule.SubContext local927_1) {
      if (!InventorySlotManager.getInstance().isLocked() && !InventoryActionQueue.getInstance().hasPendingActions()) {
         if (mc.player != null && mc.interactionManager != null && local927_1.matchesCriteria2(mc.player.getOffHandStack())) {
            ItemStack offStack = mc.player.getOffHandStack();
            if (mc.player.getItemCooldownManager().isCoolingDown(offStack)) {
               this.processData2(offStack);
            } else {
               this.processData4(local927_1);
               TargetHudStyle.processData2(offStack.getItem());
               this.processData5(offStack);
               ((ClientPlayerInteractionManagerMixin2)mc.interactionManager)
                  .invokeStartPrediction(
                     mc.world,
                     seq -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, seq, mc.player.getYaw(), mc.player.getPitch())
                  );
            }
            return;
         }
         int local927_2 = InventoryUtils.findOffhandSlot(itemStack -> local927_1.matchesCriteria2(itemStack));
         if (local927_2 != -1) {
            if (mc.player.getItemCooldownManager().isCoolingDown(mc.player.getInventory().getStack(local927_2))) {
               this.processData2(mc.player.getInventory().getStack(local927_2));
            } else {
               this.processData4(local927_1);
               TargetHudStyle.processData2(mc.player.getInventory().getStack(local927_2).getItem());
               this.processData5(mc.player.getInventory().getStack(local927_2));
               InventorySlotManager.getInstance().processData3(mc.player.getInventory().getStack(local927_2).copy(), true);
            }
         } else {
            int local927_3 = InventoryUtils.findOffhandSlot(itemStack2 -> this.computeInt(itemStack2, local927_1) != -1);
            if (local927_3 == -1) {
               int local927_4 = InventoryUtils.findOffhandSlot(itemStack3 -> this.matchesCriteria(itemStack3, local927_1));
               if (local927_4 != -1) {
                  if (InventoryUtils.findEmptySlot(0, 35) == -1) {
                     NotificationHud.notifyWithText(
                        this.category.getIcon(),
                        "Шалкер",
                        Text.literal("Нет свободного слота под предмет из мешка").setStyle(Style.EMPTY.withColor(Formatting.RED)),
                        true
                     );
                  } else if (this.nested0360 == null && mc.player.currentScreenHandler == mc.player.playerScreenHandler) {
                     this.nested0360 = local927_1;
                     this.intVal = local927_4;
                     this.intVal2 = 0;
                     this.intVal3 = 0;
                     MovementPauseController.getInstance().pauseForTicks(1);
                  }
               } else {
                  NotificationHud.notifyWithText(
                     this.category.getIcon(),
                     local927_1.getKeybindSetting().name,
                     Text.literal("нет в инвентаре").setStyle(Style.EMPTY.withColor(Formatting.RED)),
                     true
                  );
               }
            } else {
               ItemStack itemStack4 = mc.player
                  .getInventory()
                  .getStack(local927_3)
                  .get(DataComponentTypes.BUNDLE_CONTENTS)
                  .get(this.computeInt(mc.player.getInventory().getStack(local927_3), local927_1));
               if (mc.player.getItemCooldownManager().isCoolingDown(itemStack4)) {
                  this.processData2(itemStack4);
               } else if (InventoryUtils.findEmptySlot(0, 35) == -1) {
                  NotificationHud.notifyWithText(
                     this.category.getIcon(),
                     "Мешок",
                     Text.literal("Нет свободного слота под предмет из мешка").setStyle(Style.EMPTY.withColor(Formatting.RED)),
                     true
                  );
               } else {
                  this.processData4(local927_1);
                  TargetHudStyle.processData2(itemStack4.getItem());
                  this.processData5(itemStack4);
                  InventorySlotManager.getInstance().processData3(itemStack4.copy(), true);
               }
            }
         }
      }
   }

   public void updateState2() {
      if (mc.player != null) {
         this.timeMillis2 = System.currentTimeMillis() + (ScreenWalkModule.getInstance().checkState() ? 500 : 200);
         if (!ScreenWalkModule.getInstance().checkState()) {
            RotationManager.getRotationManager()
               .setRotationWithTimeout(new RotationAngles(mc.player.getYaw(), 90.0F), new RotationSpeed(355.0F, 355.0F, 28.0F, 28.0F), 300L);
         }
      }
   }

    private boolean matchesCriteria(ItemStack itemStack, ServerHelperModule.SubContext local930_2) {
       if (Block.getBlockFromItem(itemStack.getItem()) instanceof ShulkerBoxBlock && itemStack.get(DataComponentTypes.CONTAINER) != null) {
          for (ItemStack itemStack2 : ((ContainerComponentMixin)(Object)itemStack.get(DataComponentTypes.CONTAINER)).getItems()) {
             if (local930_2.matchesCriteria2(itemStack2) || this.computeInt(itemStack2, local930_2) != -1) {
                return true;
             }
          }

          return false;
       } else {
          return false;
       }
    }

    private boolean hasSubContextItem(ServerHelperModule.SubContext sub) {
       if (sub == null || mc.player == null) return false;
       if (InventoryUtils.findOffhandSlot(sub::matchesCriteria2) != -1) return true;
       if (InventoryUtils.findOffhandSlot(item -> this.computeInt(item, sub) != -1) != -1) return true;
       return InventoryUtils.findOffhandSlot(item -> this.matchesCriteria(item, sub)) != -1;
    }

    private boolean isPotionThreatening(net.minecraft.entity.projectile.thrown.PotionEntity potion) {
       if (mc.player == null || mc.world == null) return false;
       Vec3d currentPos = potion.getEntityPos();
       Vec3d velocity = potion.getVelocity();
       if (velocity.lengthSquared() < 0.01 || Double.isNaN(velocity.x)) {
          return false;
       }
       Vec3d playerPos = mc.player.getEntityPos();
       Vec3d playerEye = mc.player.getEyePos();
       Box playerHitbox = mc.player.getBoundingBox().expand(0.5);
       for (int tick = 0; tick < 35; tick++) {
          Vec3d nextPos = currentPos.add(velocity);
          BlockHitResult blockHit = mc.world.raycast(new RaycastContext(
             currentPos,
             nextPos,
             RaycastContext.ShapeType.COLLIDER,
             RaycastContext.FluidHandling.NONE,
             potion
          ));
          if (blockHit.getType() != HitResult.Type.MISS) {
             Vec3d hitPos = blockHit.getPos();
             double distToEye = hitPos.distanceTo(playerEye);
             double distToFeet = hitPos.distanceTo(playerPos);
             if (distToEye <= 4.0 || distToFeet <= 4.0) {
                return tick >= 2;
             }
             return false;
          }
          if (playerHitbox.intersects(currentPos, nextPos)
             || nextPos.distanceTo(playerEye) <= 3.0
             || nextPos.distanceTo(playerPos) <= 3.0) {
             return tick >= 2;
          }
          currentPos = nextPos;
          velocity = new Vec3d(velocity.x * 0.99, (velocity.y - 0.05) * 0.99, velocity.z * 0.99);
       }
       return false;
    }

    private void handleAutoPlast() {
       if (this.autoPlastCooldown > 0) {
          this.autoPlastCooldown--;
          return;
       }
       if (mc.player == null || mc.world == null || mc.interactionManager == null) {
          return;
       }
       if (mc.player.getItemCooldownManager().isCoolingDown(Items.DRIED_KELP.getDefaultStack())) {
          return;
       }
       ServerHelperModule.SubContext plastSub = null;
       for (ServerHelperModule.SubContext sub : this.list) {
          if (sub.getItem2() == Items.DRIED_KELP) {
             plastSub = sub;
             break;
          }
       }
       if (plastSub == null || !this.hasSubContextItem(plastSub)) {
          return;
       }
       for (net.minecraft.entity.Entity entity : mc.world.getEntities()) {
          if (entity instanceof net.minecraft.entity.projectile.thrown.PotionEntity thrownPotion && thrownPotion.getOwner() != mc.player) {
             ItemStack potionStack = thrownPotion.getStack();
             if (potionStack != null && !potionStack.isEmpty() && this.hasBadEffects(thrownPotion)) {
                if (this.isPotionThreatening(thrownPotion)) {
                   double dx = thrownPotion.getX() - mc.player.getX();
                   double dy = thrownPotion.getY() - mc.player.getEyeY();
                   double dz = thrownPotion.getZ() - mc.player.getZ();
                   double distXZ = Math.hypot(dx, dz);
                   float targetYaw = MathHelper.wrapDegrees((float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
                   float targetPitch = MathHelper.clamp((float)(-Math.toDegrees(Math.atan2(dy, distXZ))), -89.0F, 89.0F);
                   this.plastPlaceX = thrownPotion.getX();
                   this.plastPlaceY = thrownPotion.getY();
                   this.plastPlaceZ = thrownPotion.getZ();
                   this.autoPlastCooldown = 25;
                   this.plastTargetYaw = targetYaw;
                   this.plastTargetPitch = targetPitch;
                   this.plastRotateTime = System.currentTimeMillis() + 300L;
                   RotationManager.getRotationManager().setRotationWithTimeout(
                      new RotationAngles(targetYaw, targetPitch),
                      new RotationSpeed(360.0F, 360.0F, 30.0F, 30.0F),
                      250L
                   );
                   NotificationHud.notifyWithText(
                      this.category.getIcon(),
                      "\u0410\u0432\u0442\u043e \u041f\u043b\u0430\u0441\u0442",
                      Text.literal("\u0441\u0440\u0430\u0431\u043e\u0442\u0430\u043b").setStyle(Style.EMPTY.withColor(Formatting.GREEN)),
                      true
                   );
                   this.processData9(plastSub);
                   return;
                }
             }
          }
       }
    }

    private boolean hasBadEffects(net.minecraft.entity.projectile.thrown.PotionEntity thrownPotion) {
       ItemStack stack = thrownPotion.getStack();
       if (stack.isEmpty()) return false;
       net.minecraft.component.type.PotionContentsComponent potion = stack.get(DataComponentTypes.POTION_CONTENTS);
       if (potion == null) return false;
       for (net.minecraft.entity.effect.StatusEffectInstance effect : potion.getEffects()) {
          String id = effect.getEffectType().getIdAsString();
          if (id.startsWith("minecraft:")) {
             id = id.substring("minecraft:".length());
          }
          if (BAD_EFFECTS.contains(id) || effect.getEffectType().value().getCategory() == net.minecraft.entity.effect.StatusEffectCategory.HARMFUL) {
             return true;
          }
       }
       return false;
    }

    private ServerHelperModule() {
      this.modeSetting = this.addSetting(
         new ModeSetting("Сервер", "Для какого сервера предметы.", "SpookyTime", "SpookyTime", "FunTime", "ReallyWorld", "LonyGrief", "HolyWorld")
      );
      this.smartScroll = this.addSetting(
         new BooleanSetting("Умный скролл", "Скролл колёсиком пропускает выбранные слоты, выбор цифрами работает как обычно.", false)
      );
      this.multiSelectSetting = this.addSetting(
         new MultiSelectSetting("Слоты без скролла", "Какие слоты хотбара пропускать при скролле.", new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9"})
      );
      this.multiSelectSetting.withVisibility(() -> this.smartScroll.isChecked());
      this.autoPitch = this.addSetting(
         new BooleanSetting("Авто питч", "Кидает зелья по бинду себе под ноги, глядя вниз. Радиацию и Снотворное не трогает.", false)
      );
      this.autoPitch.withVisibility(() -> !this.modeSetting.isSelected("HolyWorld") && !this.modeSetting.isSelected("ReallyWorld") && !this.modeSetting.isSelected("LonyGrief"));
      this.showArrows = this.addSetting(new BooleanSetting("Показывать стрелы", "У лука и арбалета в хотбаре рисует общее число стрел в инвентаре.", false));
      this.eventWaypoint = this.addSetting(new BooleanSetting("Точка на ивент", "Ставит метку по координатам ивента из чата.", false));
      this.autoPlast = this.addSetting(new BooleanSetting("Авто Пласт", "Если на тебя летит зелье с плохими эффектами, автоматически ставит пласт (сушёные водоросли) в точку попадания."));
      this.autoPlast.withVisibility(() -> !this.modeSetting.isSelected("HolyWorld") && !this.modeSetting.isSelected("ReallyWorld") && !this.modeSetting.isSelected("LonyGrief"));
      this.addSetting(new SectionSetting("Предметы"));
      this.processData8("Снежок", Items.SNOWBALL, "snowball");
      this.processData8("Божья аура", Items.PHANTOM_MEMBRANE, "phantom membrane");
      this.processData8("Трапка", Items.NETHERITE_SCRAP, "netherite scrap");
      this.processData8("Пласт", Items.DRIED_KELP, "dried kelp");
      this.processData8("Явная пыль", Items.SUGAR, "sugar");
      this.processData8("Огненный смерч", Items.FIRE_CHARGE, "fire charge");
      this.processData8("Дезориентация", Items.ENDER_EYE, "ender eye");
      this.processData7("Взрывная трапка", Items.PRISMARINE_SHARD, "взрывная трапка");
      this.processData7("Обычная трапка", Items.POPPED_CHORUS_FRUIT, "трапка");
      this.processData7("Стан", Items.NETHER_STAR, "стан");
      this.processData7("Взрывная штучка", Items.FIRE_CHARGE, "взрывная штучка");
      this.processData7("Ком снега", Items.SNOWBALL, "ком снега");
      this.processDataReallyWorld("Темный Пульс", Items.ECHO_SHARD, itemStack -> {
         String name = Formatting.strip(itemStack.getName().getString()).toLowerCase().replace('ё', 'е');
         if (name.contains("темный пульс") || name.contains("темный-пульс") || name.contains("пульс")) {
            return true;
         }
         LoreComponent lore = itemStack.get(DataComponentTypes.LORE);
         if (lore != null) {
            for (Text line : lore.lines()) {
               String lineStr = Formatting.strip(line.getString()).toLowerCase().replace('ё', 'е');
               if (lineStr.contains("темный пульс") || lineStr.contains("темный-пульс") || lineStr.contains("пульс")) {
                  return true;
               }
            }
         }
         return false;
      });
      this.processDataReallyWorld("Ловушка", Items.NETHERITE_SCRAP, itemStack -> {
         String name = Formatting.strip(itemStack.getName().getString()).toLowerCase().replace('ё', 'е');
         if (!name.contains("эндер") && (name.contains("ловушк") || name.contains("трапк"))) {
            return true;
         }
         LoreComponent lore = itemStack.get(DataComponentTypes.LORE);
         if (lore != null) {
            for (Text line : lore.lines()) {
               String lineStr = Formatting.strip(line.getString()).toLowerCase().replace('ё', 'е');
               if (!lineStr.contains("эндер") && (lineStr.contains("ловушк") || lineStr.contains("трапк"))) {
                  return true;
               }
            }
         }
         return false;
      });
      this.processDataReallyWorld("Эндер-Ловушка", Items.ENDER_EYE, itemStack -> {
         String name = Formatting.strip(itemStack.getName().getString()).toLowerCase().replace('ё', 'е');
         if (name.contains("эндер") && (name.contains("ловушк") || name.contains("трапк"))) {
            return true;
         }
         LoreComponent lore = itemStack.get(DataComponentTypes.LORE);
         if (lore != null) {
            for (Text line : lore.lines()) {
               String lineStr = Formatting.strip(line.getString()).toLowerCase().replace('ё', 'е');
               if (lineStr.contains("эндер") && (lineStr.contains("ловушк") || lineStr.contains("трапк"))) {
                  return true;
               }
            }
         }
         return false;
      });
      this.processDataReallyWorld("Анти Полет", Items.FIREWORK_STAR, itemStack -> {
         if (itemStack.isOf(Items.FIREWORK_STAR)) {
            return true;
         }
         String name = Formatting.strip(itemStack.getName().getString()).toLowerCase().replace('ё', 'е');
         if (name.contains("анти полет") || name.contains("анти-полет") || name.contains("антиполет")) {
            return true;
         }
         LoreComponent lore = itemStack.get(DataComponentTypes.LORE);
         if (lore != null) {
            for (Text line : lore.lines()) {
               String lineStr = Formatting.strip(line.getString()).toLowerCase().replace('ё', 'е');
               if (lineStr.contains("анти полет") || lineStr.contains("анти-полет") || lineStr.contains("антиполет")) {
                  return true;
               }
            }
         }
         return false;
      });

      // Предметы активации LonyGrief
      this.processDataLonyGrief("Обычная Ливалка", Items.NETHER_STAR, itemStack -> {
         String name = Formatting.strip(itemStack.getName().getString()).toLowerCase().replace('ё', 'е');
         return name.contains("ливалка") && !name.contains("платформ");
      });
      this.processDataLonyGrief("Ливалка с платформой", Items.NETHER_STAR, itemStack -> {
         String name = Formatting.strip(itemStack.getName().getString()).toLowerCase().replace('ё', 'е');
         if (name.contains("ливалка") && name.contains("платформ")) {
            return true;
         }
         LoreComponent lore = itemStack.get(DataComponentTypes.LORE);
         if (lore != null) {
            for (Text line : lore.lines()) {
               String lineStr = Formatting.strip(line.getString()).toLowerCase().replace('ё', 'е');
               if (lineStr.contains("ливалка") && lineStr.contains("платформ")) {
                  return true;
               }
            }
         }
         return false;
      });
      this.processDataLonyGrief("Уникальное Пёрышко", Items.NETHER_STAR, itemStack -> {
         String name = Formatting.strip(itemStack.getName().getString()).toLowerCase().replace('ё', 'е');
         if (name.contains("перы") || name.contains("перышк")) {
            return true;
         }
         LoreComponent lore = itemStack.get(DataComponentTypes.LORE);
         if (lore != null) {
            for (Text line : lore.lines()) {
               String lineStr = Formatting.strip(line.getString()).toLowerCase().replace('ё', 'е');
               if (lineStr.contains("перы") || lineStr.contains("перышк")) {
                  return true;
               }
            }
         }
         return false;
      });
      this.processDataLonyGrief("Уникальная Гранка", Items.NETHER_STAR, itemStack -> {
         String name = Formatting.strip(itemStack.getName().getString()).toLowerCase().replace('ё', 'е');
         if (name.contains("гранка") || name.contains("гранк")) {
            return true;
         }
         LoreComponent lore = itemStack.get(DataComponentTypes.LORE);
         if (lore != null) {
            for (Text line : lore.lines()) {
               String lineStr = Formatting.strip(line.getString()).toLowerCase().replace('ё', 'е');
               if (lineStr.contains("гранка") || lineStr.contains("гранк")) {
                  return true;
               }
            }
         }
         return false;
      });

      // EventSearch for ReallyWorld
      this.addSetting(new SectionSetting("EventSearch (ReallyWorld)")).withVisibility(() -> this.modeSetting.isSelected("ReallyWorld"));
      this.eventSearch = this.addSetting(new BooleanSetting("EventSearch", "Поиск ивентовых голов по совпадению скина/модели.", false));
      this.eventSearch.withVisibility(() -> this.modeSetting.isSelected("ReallyWorld"));

      this.eventSearchKey = this.addSetting(new KeybindSetting("Захватить цель", "Захватывает голову, на которую вы смотрите."));
      this.eventSearchKey.withVisibility(() -> this.modeSetting.isSelected("ReallyWorld") && this.eventSearch.isChecked());

      this.eventSearchAutoCapture = this.addSetting(new BooleanSetting("Авто-захват", "Автоматически захватывает голову при первом наведении.", true));
      this.eventSearchAutoCapture.withVisibility(() -> this.modeSetting.isSelected("ReallyWorld") && this.eventSearch.isChecked());

      this.eventSearchTracers = this.addSetting(new BooleanSetting("Трассера", "Линия от центра экрана к найденным головам.", true));
      this.eventSearchTracers.withVisibility(() -> this.modeSetting.isSelected("ReallyWorld") && this.eventSearch.isChecked());

      this.eventSearchBoxes = this.addSetting(new BooleanSetting("3D Боксы", "Подсветка голов 3D-боксом сквозь стены.", true));
      this.eventSearchBoxes.withVisibility(() -> this.modeSetting.isSelected("ReallyWorld") && this.eventSearch.isChecked());

      this.eventSearchDistance = this.addSetting(new NumberSetting("Дистанция", "Радиус поиска голов в блоках.").withRange(16.0F, 256.0F).withStep(8.0F).setValue(96.0F));
      this.eventSearchDistance.withVisibility(() -> this.modeSetting.isSelected("ReallyWorld") && this.eventSearch.isChecked());

      this.clearEventTarget = this.addSetting(new BooleanSetting("Сбросить цель", "Сбрасывает текущую цель поиска.", false));
      this.clearEventTarget.withVisibility(() -> this.modeSetting.isSelected("ReallyWorld") && this.eventSearch.isChecked());

      this.addSetting(new SectionSetting("Зелья")).withVisibility(() -> !this.modeSetting.isSelected("HolyWorld") && !this.modeSetting.isSelected("ReallyWorld") && !this.modeSetting.isSelected("LonyGrief"));
      this.processData6("Зелье Палладина", "minecraft:resistance", "minecraft:fire_resistance", "minecraft:health_boost", "minecraft:invisibility");
      this.processData6("Зелье Гнева", "minecraft:strength", "minecraft:slowness");
      this.processData6("Хлопушка", "minecraft:slowness", "minecraft:speed", "minecraft:blindness", "minecraft:glowing");
      this.processData6("Святая вода", "minecraft:regeneration", "minecraft:invisibility", "minecraft:instant_health");
      this.processData6("Зелье Ассасина", "minecraft:strength", "minecraft:speed", "minecraft:haste", "minecraft:instant_damage");
      this.processData6("Зелье Радиации", "minecraft:poison", "minecraft:wither", "minecraft:slowness", "minecraft:hunger", "minecraft:glowing");
      this.processData6("Снотворное", "minecraft:weakness", "minecraft:mining_fatigue", "minecraft:wither", "minecraft:blindness");
      this.processData6("Зелье Прыгучести");
   }

   public static ServerHelperModule getInstance() {
      if (serverHelperModule == null) {
         serverHelperModule = new ServerHelperModule();
      }

      return serverHelperModule;
   }

   public static final class SubContext {
      private final Item item2;
      private final KeybindSetting keybindSetting;
      private final String strVal;
      private String[] strValArray;
      private boolean flag;
      private final Predicate<ItemStack> customMatcher;

      private SubContext(Item item, KeybindSetting initialKeybindSetting) {
         this.item2 = item;
         this.keybindSetting = initialKeybindSetting;
         this.strVal = null;
         this.customMatcher = null;
      }

      public Item getItem2() {
         return this.item2;
      }

      public KeybindSetting getKeybindSetting() {
         return this.keybindSetting;
      }

      public boolean matchesCriteria2(ItemStack itemStack) {
         if (itemStack.isEmpty()) {
            return false;
         } else if (this.customMatcher != null) {
            return this.customMatcher.test(itemStack);
         } else if (this.item2 != null && itemStack.getItem() != this.item2) {
            return false;
         } else {
            return this.strValArray != null && this.strValArray.length > 0
               ? this.matchesCriteria3(itemStack)
               : this.strVal == null || Formatting.strip(itemStack.getName().getString()).toLowerCase().replace('ё', 'е').contains(this.strVal.replace('ё', 'е'));
         }
      }

      private SubContext(String local941_1, Item item, KeybindSetting initialKeybindSetting) {
         this.item2 = item;
         this.keybindSetting = initialKeybindSetting;
         this.strVal = local941_1.toLowerCase();
         this.customMatcher = null;
      }

      private SubContext(String local941_1, Item item, KeybindSetting initialKeybindSetting, Predicate<ItemStack> customMatcher) {
         this.item2 = item;
         this.keybindSetting = initialKeybindSetting;
         this.strVal = local941_1.toLowerCase();
         this.customMatcher = customMatcher;
      }

      private boolean matchesCriteria3(ItemStack itemStack) {
         for (String local942_5 : this.strValArray) {
            boolean local942_6 = false;

            for (StatusEffectInstance statusEffectInstance : itemStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).getEffects()) {
               if (statusEffectInstance.getEffectType().getIdAsString().equals(local942_5)) {
                  local942_6 = true;
               }
            }

            if (!local942_6) {
               return false;
            }
         }

         return true;
      }

      private SubContext(String local943_1, KeybindSetting initialKeybindSetting, String... stringArray) {
         this.item2 = null;
         this.keybindSetting = initialKeybindSetting;
         this.strVal = local943_1.toLowerCase();
         this.strValArray = stringArray;
         this.customMatcher = null;
      }
   }
}

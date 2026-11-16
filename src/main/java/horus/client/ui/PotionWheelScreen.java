package horus.client.ui;

import horus.client.util.Easings;
import horus.client.render.FontFace;
import horus.client.util.ItemDrawOptions;
import horus.client.module.player.PotionThrowerModule;
import horus.client.util.GuiScale;
import horus.client.render.FontText;
import horus.client.ui.ScreenClosingTransition;
import horus.client.render.ItemRenderCache;
import horus.client.util.ServerItemType;
import horus.client.util.Animation;
import horus.client.ui.UiTheme;
import horus.client.render.ShapeDrawing;
import horus.client.storage.AutoBuyItem;
import horus.client.ui.hud.HudRenderContext;
import horus.client.ui.hud.HudStyle;
import horus.client.render.Render2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class PotionWheelScreen extends Screen implements ScreenClosingTransition.SubInterface {
   private final ArrayList<Animation> list = new ArrayList<>();
   private final Animation animator = new Animation();
   private final Animation animator2 = new Animation();
   private final ArrayList<Animation> list2 = new ArrayList<>();
   private final HashMap<String, ItemStack> map = new HashMap<>();
   private int intVal = -1;
   private int intVal2 = -1;
   private int intVal3 = -1;
   private int intVal4 = -1;
   private float floatVal = 92.0F;
   private float floatVal2;
   private float floatVal3;
   private float floatVal4;
   private float floatVal5;
   private float floatVal6;
   private float floatVal7;
   private boolean flag;

   private static PotionThrowerModule thrower() {
      return PotionThrowerModule.getInstance();
   }

   @Override
   protected void init() {
      ScreenClosingTransition.processData3(this);
      this.flag = false;
      this.intVal = -1;
      this.intVal2 = -1;
      this.intVal3 = -1;
      this.animator.animateTo(1.0, 0.22, Easings.EASE_OUT_QUAD);
   }

   @Override
   public void renderBackground(DrawContext drawContext, int local6362_2, int local6362_3, float local6362_4) {
   }

   private void drawSlot(int local6363_1, float local6363_2, float local6363_3, float local6363_4, float local6363_5, float local6363_6) {
      AutoBuyItem autoBuyItem = thrower().getAutoBuyItem(local6363_1);
      if (autoBuyItem == null) {
         float local6363_8 = 20.0F * local6363_4;
         FontText.processData9(
            FontFace.BOLD,
            "+",
            local6363_2,
            local6363_3 - local6363_8 / 2.0F,
            local6363_8,
            UiTheme.computeInt(HudStyle.computeInt2(0.4F + 0.3F * local6363_6), local6363_5)
         );
      } else {
         float local6363_8 = 20.0F * local6363_4;
         float local6363_9 = Math.round((local6363_2 - local6363_8 / 2.0F) * 2.0F) / 2.0F;
         float local6363_10 = Math.round((local6363_3 - local6363_8 / 2.0F) * 2.0F) / 2.0F;
         ItemRenderCache.processData(this.potionStack(autoBuyItem), local6363_9, local6363_10, local6363_8, ItemDrawOptions.noDecorations(local6363_5));
      }
   }

   private void drawPicker(float local6364_1, float local6364_2, float local6364_3, float local6364_4) {
      ArrayList<AutoBuyItem> local6364_5 = thrower().getArrayList2();
      ArrayList<String> local6364_6 = this.pickerRows(local6364_5);
      float local6364_7 = 0.0F;

      for (String local6364_9 : local6364_6) {
         local6364_7 = Math.max(local6364_7, FontText.computeFloat(FontFace.SEMI_BOLD, local6364_9, 6.0F));
      }

      this.floatVal6 = 16.0F + local6364_7 + 6.0F;
      this.floatVal7 = local6364_6.size() * 13.0F + 4.0F;
      this.floatVal4 = local6364_1;
      this.floatVal5 = Math.max(4.0F, Math.min(local6364_3 - this.floatVal7 - 4.0F, local6364_2 - this.floatVal7 / 2.0F));
      float local6364_8 = 6.0F * (1.0F - local6364_4);
      float local6364_9 = this.floatVal4 + local6364_8;
      HudStyle.processData4(local6364_9, this.floatVal5, this.floatVal6, this.floatVal7, local6364_4, 6.0F);
      this.intVal4 = -1;

      while (this.list2.size() < local6364_6.size()) {
         this.list2.add(new Animation());
      }

      float local6364_10 = this.animator2.floatValue();

      for (int index = 0; index < local6364_6.size(); index++) {
         float local6364_12 = this.floatVal5 + 2.0F + index * 13.0F;
         boolean local6364_13 = this.pickerOpen()
            && this.floatVal2 >= local6364_9
            && this.floatVal2 <= local6364_9 + this.floatVal6
            && this.floatVal3 >= local6364_12
            && this.floatVal3 < local6364_12 + 13.0F;
         if (local6364_13) {
            this.intVal4 = index;
         }

         Animation animation = this.list2.get(index);
         animation.update();
         animation.animateTo(local6364_13 ? 1.0 : 0.0, 0.16, Easings.EASE_OUT_QUAD);
         float local6364_15 = animation.floatValue();
         float local6364_16 = (float)Easings.EASE_OUT_QUART
            .apply(Math.max(0.0F, Math.min(1.0F, local6364_10 * (1.0F + local6364_6.size() * 0.12F) - index * 0.12F)));
         float local6364_17 = local6364_4 * local6364_16 * (0.72F + 0.28F * local6364_15);
         float local6364_18 = local6364_9 + 4.0F + 6.0F * (1.0F - local6364_16) + 2.0F * local6364_15;
         boolean local6364_19 = index >= local6364_5.size();
         if (!local6364_19) {
            ItemRenderCache.processData(
               this.potionStack(local6364_5.get(index)),
               local6364_18,
               local6364_12 + 2.5F,
               8.0F,
               ItemDrawOptions.noDecorations(local6364_4 * local6364_16)
            );
         }

         if (local6364_19) {
            FontText.processData3(
               FontFace.SEMI_BOLD,
               local6364_6.get(index),
               local6364_18 + 8.0F + 4.0F,
               local6364_12 + 6.5F - 3.0F,
               6.0F,
               UiTheme.computeInt(UiTheme.computeInt2(HudStyle.computeInt2(1.0F), -42386, local6364_15), local6364_17)
            );
         } else {
            FontText.processData5(
               FontFace.SEMI_BOLD,
               this.potionName(local6364_5.get(index)),
               local6364_18 + 8.0F + 4.0F,
               local6364_12 + 6.5F - 3.0F,
               6.0F,
               UiTheme.computeInt(HudStyle.computeInt4(1.0F), local6364_17)
            );
         }
      }
   }

   private ArrayList<String> pickerRows(ArrayList<AutoBuyItem> local6365_1) {
      ArrayList<String> local6365_2 = new ArrayList<>();

      for (AutoBuyItem autoBuyItem : local6365_1) {
         local6365_2.add(autoBuyItem.name());
      }

      if (this.intVal3 != -1 && this.intVal3 < slots().size() && !slots().get(this.intVal3).isEmpty()) {
         local6365_2.add("Очистить");
      }

      return local6365_2;
   }

   @Override
   public boolean shouldPause() {
      return false;
   }

   @Override
   public boolean renderClosing(DrawContext drawContext) {
      this.drawWheel(drawContext, false);
      return this.animator.floatValue() > 0.01F;
   }

   private void closePicker() {
      this.intVal3 = -1;
      this.intVal4 = -1;
      if (!thrower().checkState()) {
         this.startClosing();
      }
   }

   private ItemStack potionStack(AutoBuyItem autoBuyItem) {
      return this.map
         .computeIfAbsent(
            autoBuyItem.name(),
            local6368_1 -> {
               ServerItemType serverItemType = ServerItemType.getServerItemType(local6368_1);
               ItemStack itemStack = autoBuyItem.displayStack().copy();
               if (serverItemType != null) {
                  itemStack.set(
                     DataComponentTypes.POTION_CONTENTS,
                     new PotionContentsComponent(Optional.empty(), Optional.of(serverItemType.computeInt() | 0xFF000000), List.of(), Optional.empty())
                  );
               }

               return itemStack;
            }
         );
   }

   private void clampCursor(MinecraftClient minecraftClient, float local6371_2, float local6371_3) {
      if (minecraftClient.isWindowFocused()) {
         Window window = minecraftClient.getWindow();
         double local6371_5 = (double)window.getFramebufferWidth() / local6371_2;
         double local6371_7 = (double)window.getFramebufferHeight() / local6371_3;
         double local6371_9 = local6371_2 / 2.0 * local6371_5;
         double local6371_11 = local6371_3 / 2.0 * local6371_7;
         double local6371_13 = minecraftClient.mouse.getX() - local6371_9;
         double local6371_15 = minecraftClient.mouse.getY() - local6371_11;
         double local6371_17 = (this.floatVal - 1.0) * local6371_5;
         double local6371_19 = Math.hypot(local6371_13, local6371_15);
         if (local6371_19 > local6371_17) {
            double local6371_21 = local6371_17 / local6371_19;
            GLFW.glfwSetCursorPos(window.getHandle(), local6371_9 + local6371_13 * local6371_21, local6371_11 + local6371_15 * local6371_21);
         }
      }
   }

   public boolean closing() {
      return this.flag;
   }

   private void openPicker(int local6373_1) {
      this.intVal3 = local6373_1;
      this.intVal2 = -1;
   }

   private static float space() {
      return GuiScale.computeInt() / GuiScale.computeFloat();
   }

   public PotionWheelScreen() {
      super(Text.literal("PotionWheel"));
   }

   @Override
   public void render(DrawContext drawContext, int local6376_2, int local6376_3, float local6376_4) {
      if (!this.flag) {
         this.drawWheel(drawContext, true);
      }
   }

   public void releaseHeldSlot(int local6377_1) {
      if (this.intVal2 != -1) {
         if (local6377_1 != -1 && local6377_1 != this.intVal2 && local6377_1 < slots().size() && this.intVal2 < slots().size()) {
            Collections.swap(slots(), this.intVal2, local6377_1);
            thrower().updateState();
         }

         this.intVal2 = -1;
      }
   }

   public int draggedSlot() {
      return this.intVal2;
   }

   private Text potionName(AutoBuyItem autoBuyItem) {
      ServerItemType serverItemType = ServerItemType.getServerItemType(autoBuyItem.name());
      return serverItemType != null ? serverItemType.getText() : Text.literal(autoBuyItem.name());
   }

   public boolean pickerOpen() {
      return this.intVal3 != -1;
   }

   private int hoveredAt(float local6381_1, float local6381_2, float local6381_3, float local6381_4, int local6381_5) {
      float local6381_6 = local6381_1 - local6381_3;
      float local6381_7 = local6381_2 - local6381_4;
      if (Math.hypot(local6381_6, local6381_7) > this.floatVal * 1.25F) {
         return -1;
      }

      double local6381_8 = 360.0 / local6381_5;
      double local6381_10 = Math.toDegrees(Math.atan2(local6381_7, local6381_6)) + 90.0 + local6381_8 / 2.0;

      while (local6381_10 < 0.0) {
         local6381_10 += 360.0;
      }

      local6381_10 %= 360.0;
      return Math.min(local6381_5 - 1, (int)(local6381_10 / local6381_8));
   }

   public void startClosing() {
      if (!this.flag) {
         this.flag = true;
         this.intVal = -1;
         this.intVal2 = -1;
         this.intVal3 = -1;
         this.animator.animateTo(0.0, 0.22, Easings.EASE_OUT_QUAD);
         ScreenClosingTransition.processData(this);
         if (this.client != null && this.client.currentScreen == this) {
            this.client.setScreen(null);
         }
      }
   }

   private static ArrayList<String> slots() {
      return thrower().getArrayList();
   }

   private void drawWheel(DrawContext drawContext, boolean local6384_2) {
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      if (minecraftClient.player != null) {
         Render2D.renderElement5(drawContext);
         HudRenderContext.renderElement(drawContext);
         float local6384_4 = this.width * space();
         float local6384_5 = this.height * space();
         float local6384_6 = local6384_4 / 2.0F;
         float local6384_7 = local6384_5 / 2.0F;
         this.floatVal = Math.min(140.0F, Math.max(56.0F, Math.min(local6384_4, local6384_5) * 0.17F));
         if (local6384_2 && !this.pickerOpen()) {
            this.clampCursor(minecraftClient, local6384_4, local6384_5);
         }

         this.animator.update();
         this.animator2.update();
         this.animator2.animateTo(this.pickerOpen() ? 1.0 : 0.0, this.pickerOpen() ? 0.32 : 0.16, Easings.EASE_OUT_QUAD);
         this.floatVal2 = (float)minecraftClient.mouse.getScaledX(minecraftClient.getWindow()) * space();
         this.floatVal3 = (float)minecraftClient.mouse.getScaledY(minecraftClient.getWindow()) * space();
         int local6384_8 = slots().size();

         while (this.list.size() < local6384_8) {
            this.list.add(new Animation());
         }

         this.intVal = local6384_2 && !this.flag && !this.pickerOpen()
            ? this.hoveredAt(this.floatVal2, this.floatVal3, local6384_6, local6384_7, local6384_8)
            : -1;
         float local6384_9 = this.animator.floatValue();
         if (!(local6384_9 <= 0.004F)) {
            float local6384_10 = this.floatVal * (0.88F + 0.12F * local6384_9);
            float local6384_11 = local6384_10 / 92.0F;
            float local6384_12 = 360.0F / local6384_8;

            for (int index = 0; index < local6384_8; index++) {
               Animation animation = this.list.get(index);
               animation.update();
               animation.animateTo(
                  index != this.intVal && index != this.intVal2 && index != this.intVal3 ? 0.0 : 1.0,
                  0.18,
                  Easings.EASE_OUT_QUAD
               );
               float local6384_15 = animation.floatValue();
               float local6384_16 = -90.0F - local6384_12 / 2.0F + local6384_12 * index;
               double local6384_17 = Math.toRadians(local6384_16 + local6384_12 / 2.0F);
               float local6384_19 = 1.0F + 0.03F * local6384_15;
               HudRenderContext.processData3(local6384_6, local6384_7, local6384_19);
               ShapeDrawing.processData3(
                  ShapeDrawing.TypeOption.MIDDLE,
                  local6384_6,
                  local6384_7,
                  local6384_10,
                  local6384_16,
                  local6384_12,
                  6.0F * local6384_11 / local6384_19,
                  10.0F * local6384_11,
                  index == this.intVal2
                     ? UiTheme.computeInt2(HudStyle.computeInt3(), HudStyle.computeInt(1.0F), 0.35F)
                     : HudStyle.computeInt3(),
                  0.8F,
                  local6384_9
               );
               float local6384_20 = local6384_6 + (float)Math.cos(local6384_17) * local6384_10 * 0.62F;
               float local6384_21 = local6384_7 + (float)Math.sin(local6384_17) * local6384_10 * 0.62F;
               if (index != this.intVal2) {
                  this.drawSlot(index, local6384_20, local6384_21, local6384_11, local6384_9, local6384_15);
               }

               HudRenderContext.updateState();
            }

            if (this.intVal2 != -1) {
               AutoBuyItem autoBuyItem = thrower().getAutoBuyItem(this.intVal2);
               if (autoBuyItem != null) {
                  float local6384_14 = 20.0F * local6384_11;
                  ItemRenderCache.processData(
                     this.potionStack(autoBuyItem),
                     this.floatVal2 - local6384_14 / 2.0F,
                     this.floatVal3 - local6384_14 / 2.0F,
                     local6384_14,
                     ItemDrawOptions.noDecorations(local6384_9)
                  );
               }
            }

            int local6384_13 = this.intVal2 != -1 ? this.intVal2 : this.intVal;
            AutoBuyItem autoBuyItem2 = thrower().getAutoBuyItem(local6384_13);
            if (autoBuyItem2 != null) {
               Text text = this.potionName(autoBuyItem2);
               FontText.processData5(
                  FontFace.SEMI_BOLD,
                  text,
                  local6384_6 - FontText.computeFloat(FontFace.SEMI_BOLD, text.getString(), 6.0F) / 2.0F,
                  local6384_7 + local6384_10 + 6.0F,
                  6.0F,
                  UiTheme.computeInt(HudStyle.computeInt4(1.0F), local6384_9)
               );
            }

            float local6384_15 = this.animator2.floatValue();
            if (local6384_15 > 0.004F) {
               this.drawPicker(local6384_6 + local6384_10 + 12.0F, local6384_7, local6384_5, local6384_9 * local6384_15);
            } else {
               this.intVal4 = -1;
            }

            Render2D.updateState6();
         }
      }
   }

   @Override
   public void close() {
      if (this.pickerOpen()) {
         this.closePicker();
      } else {
         this.startClosing();
      }
   }

   @Override
   public boolean mouseReleased(Click click) {
      if (click.button() == 0 && this.intVal2 != -1) {
         this.releaseHeldSlot(this.intVal);
         return true;
      } else {
         return super.mouseReleased(click);
      }
   }

   public int hoveredSlot() {
      return this.intVal;
   }

   @Override
   public boolean mouseClicked(Click click, boolean local6388_2) {
      if (this.flag) {
         return true;
      }

      if (this.pickerOpen()) {
         if (click.button() == 0 && this.intVal4 != -1) {
            ArrayList<AutoBuyItem> local6388_3 = thrower().getArrayList2();
            ArrayList<String> local6388_4 = this.pickerRows(local6388_3);
            if (this.intVal4 < local6388_3.size()) {
               slots().set(this.intVal3, local6388_3.get(this.intVal4).name());
            } else if (local6388_4.get(this.intVal4).equals("Очистить")) {
               slots().set(this.intVal3, "");
            }

            thrower().updateState();
         }

         this.closePicker();
         return true;
      } else {
         if (this.intVal != -1) {
            if (click.button() == 1) {
               this.openPicker(this.intVal);
               return true;
            }

            if (click.button() == 0) {
               if (thrower().getAutoBuyItem(this.intVal) != null) {
                  this.intVal2 = this.intVal;
               } else {
                  this.openPicker(this.intVal);
               }

               return true;
            }
         }

         return super.mouseClicked(click, local6388_2);
      }
   }
}

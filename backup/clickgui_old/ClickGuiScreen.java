package horus.client.ui;

import horus.client.util.Colors;
import horus.client.render.FontFace;
import horus.client.util.InputBinding;
import horus.client.module.render.InterfaceModule;
import horus.client.module.ModuleCategory;
import horus.client.util.GuiScale;
import horus.client.render.FontText;
import horus.client.ui.ScreenClosingTransition;
import horus.client.ui.UiTheme;
import horus.client.ui.ClickGuiStyle;
import horus.client.ui.panel.ModuleCategoryPanel;
import horus.client.ui.setting.SettingRenderContext;
import horus.client.render.GuiEffectsState;
import horus.client.ui.SmoothScroll;
import horus.client.render.Render2D;
import horus.client.setting.TextSetting;

import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ClickGuiScreen extends Screen implements ScreenClosingTransition.SubInterface {
   private final SettingRenderContext settingRenderContext = new SettingRenderContext();
   private final ModuleCategory[] moduleCategoryArray = ModuleCategory.values();
   private final SmoothScroll[] recoveredClass0320Array;
   private final float[][] floatValArray;
   private float floatVal;
   private float floatVal2;
   private long timeMillis;
   private boolean flag;
   private boolean flag2;
   private boolean flag3;
   private boolean flag4;
   private long timeMillis2 = System.nanoTime() / 1000000L;
   private boolean flag5;
   private final TextSetting textSetting = new TextSetting("Поиск", "").withLengthRange(0, 28);
   private boolean cloudModalOpen = false;
   private final TextSetting cloudCodeInput = new TextSetting("Код", "").withLengthRange(0, 16);
   private boolean cloudInputActive = false;
   private String cloudStatusText = "";
   private long cloudStatusTime = 0L;
   private long timeMillis3;
   private static ClickGuiScreen clickGuiScreen;
   private Screen parentScreen;

   public void setParentScreen(Screen parent) {
      this.parentScreen = parent;
   }

   @Override
   public void close() {
      if (!this.flag2) {
         this.flag2 = true;
         this.settingRenderContext.textSetting = null;
         this.settingRenderContext.nested0492 = null;
         this.settingRenderContext.keybindSetting = null;
         this.settingRenderContext.object = null;
         this.settingRenderContext.module = null;
         this.flag4 = true;
         ScreenClosingTransition.processData(this);
         if (MinecraftClient.getInstance().currentScreen == this) {
            Screen back = this.parentScreen;
            this.parentScreen = null;
            MinecraftClient.getInstance().setScreen(back);
         }
      }
   }

   private void drawSearchBar(float local1247_1, float local1247_2, float local1247_3) {
      float local1247_4 = this.settingRenderContext.scale;
      float local1247_5 = 74.8F * local1247_4;
      float local1247_6 = 19.31F * local1247_4;
      float local1247_7 = (local1247_1 - local1247_5) / 2.0F;
      float local1247_8 = Math.min(local1247_3 + 30.8F * local1247_4, local1247_2 - local1247_6 - 5.0F * local1247_4);
      Render2D.processData60(
         local1247_7 + 0.5F * local1247_4,
         local1247_8 + 0.5F * local1247_4,
         local1247_5 - 1.0F * local1247_4,
         local1247_6 - 1.0F * local1247_4,
         local1247_6 / 2.26F - 0.5F * local1247_4,
         30.0F,
         1.0F,
         -1
      );
      Render2D.processData51(local1247_7, local1247_8, local1247_5, local1247_6, local1247_6 / 2.26F, ClickGuiStyle.intVal2);
      Render2D.processData86(local1247_7, local1247_8, local1247_5, local1247_6, local1247_6 / 2.26F, 0.5F, ClickGuiStyle.intVal3);
      Render2D.processData84(
         local1247_7, local1247_8, local1247_6, local1247_6, local1247_6 / 2.26F, 0.0F, 0.0F, local1247_6 / 2.26F, ClickGuiStyle.intVal39
      );
      Render2D.processData89(
         "assets/horus/textures/gui/mainmenu/icons/logo.png",
         local1247_7 + local1247_6 / 2.0F - 3.4F * local1247_4,
         local1247_8 + local1247_6 / 2.0F - 2.65F * local1247_4,
         6.8F * local1247_4,
         5.3F * local1247_4,
         0.0F,
         ClickGuiStyle.intVal
      );
      Render2D.processData89(
         "assets/horus/textures/gui/clickgui/search.png",
         local1247_7 + local1247_5 - 10.4F * local1247_4,
         local1247_8 + local1247_6 / 2.0F - 2.65F * local1247_4,
         5.3F * local1247_4,
         5.3F * local1247_4,
         0.0F,
         ClickGuiStyle.intVal4
      );
      String local1247_9 = this.textSetting.getText();
      String local1247_10 = local1247_9.isEmpty() ? (this.flag5 ? "" : "Search...") : local1247_9;
      float local1247_11 = local1247_7 + 25.4F * local1247_4;
      float local1247_12 = local1247_8 + (local1247_6 - 6.0F * local1247_4) / 2.0F;
      float local1247_13 = local1247_5 - 25.4F * local1247_4 - 12.0F * local1247_4;
      Render2D.renderElement2(this.settingRenderContext.drawContext, local1247_11 - 1.0F * local1247_4, local1247_8, local1247_13 + 2.0F * local1247_4, local1247_6);
      float local1247_14 = FontText.computeFloat(FontFace.SF, local1247_10, 6.0F * local1247_4);
      float local1247_15 = Math.max(0.0F, local1247_14 - local1247_13);
      if (this.textSetting.hasSelection() && !local1247_9.isEmpty()) {
         Render2D.processData51(
            local1247_11
               - local1247_15
               + FontText.computeFloat(FontFace.SF, local1247_9.substring(0, this.textSetting.getSelectionStart()), 6.0F * local1247_4),
            local1247_12 - 1.0F * local1247_4,
            FontText.computeFloat(
               FontFace.SF, local1247_9.substring(this.textSetting.getSelectionStart(), this.textSetting.getSelectionEnd()), 6.0F * local1247_4
            ),
            8.0F * local1247_4,
            1.0F * local1247_4,
            Colors.computeInt7(-1, 56)
         );
      }

      if (!local1247_10.isEmpty()) {
         FontText.processData3(
            FontFace.SF,
            local1247_10,
            local1247_11 - local1247_15,
            local1247_12,
            6.0F * local1247_4,
            local1247_9.isEmpty() ? ClickGuiStyle.intVal7 : Colors.computeInt7(-1, 210)
         );
      }

      if (this.flag5 && System.nanoTime() / 1000000L / 530L % 2L == 0L) {
         float local1247_16 = local1247_11
            - local1247_15
            + FontText.computeFloat(FontFace.SF, local1247_9.substring(0, this.textSetting.getCaret()), 6.0F * local1247_4);
         Render2D.processData51(
            local1247_16 + 0.4F * local1247_4,
            local1247_12 - 0.5F * local1247_4,
            0.6F * local1247_4,
            7.0F * local1247_4,
            0.3F * local1247_4,
            Colors.computeInt7(-1, 200)
         );
      }

      Render2D.renderElement(this.settingRenderContext.drawContext);
      this.settingRenderContext.processData7(local1247_7, local1247_8, local1247_5, local1247_6, null, () -> {
         this.flag5 = true;
         this.cloudInputActive = false;
         this.textSetting.moveToEnd(false);
         this.timeMillis3 = System.nanoTime() / 1000000L;
      });

      // Cloud Share Button beside search
      float cloudBtnW = 32.0F * local1247_4;
      float cloudBtnX = local1247_7 + local1247_5 + 4.0F * local1247_4;
      float cloudBtnY = local1247_8;
      float cloudBtnH = local1247_6;

      Render2D.processData60(cloudBtnX + 0.5F * local1247_4, cloudBtnY + 0.5F * local1247_4, cloudBtnW - 1.0F * local1247_4, cloudBtnH - 1.0F * local1247_4, cloudBtnH / 2.26F - 0.5F * local1247_4, 25.0F, 1.0F, -1);
      Render2D.processData51(cloudBtnX, cloudBtnY, cloudBtnW, cloudBtnH, cloudBtnH / 2.26F, this.cloudModalOpen ? ClickGuiStyle.intVal : ClickGuiStyle.intVal2);
      Render2D.processData86(cloudBtnX, cloudBtnY, cloudBtnW, cloudBtnH, cloudBtnH / 2.26F, 0.5F, this.cloudModalOpen ? -1 : ClickGuiStyle.intVal3);
      FontText.processData3(FontFace.SEMI_BOLD, "☁ CLOUD", cloudBtnX + 4.5F * local1247_4, cloudBtnY + (cloudBtnH - 5.5F * local1247_4) / 2.0F, 5.2F * local1247_4, this.cloudModalOpen ? -16777216 : Colors.computeInt7(-1, 230));

      this.settingRenderContext.processData7(cloudBtnX, cloudBtnY, cloudBtnW, cloudBtnH, null, () -> {
         this.cloudModalOpen = !this.cloudModalOpen;
         if (!this.cloudModalOpen) {
            this.cloudInputActive = false;
         }
      });
   }

   public static ClickGuiScreen instance() {
      if (clickGuiScreen == null) {
         clickGuiScreen = new ClickGuiScreen();
      }

      return clickGuiScreen;
   }

   @Override
   public void renderBackground(DrawContext drawContext, int local1249_2, int local1249_3, float local1249_4) {
   }

   @Override
   public void render(DrawContext drawContext, int local1250_2, int local1250_3, float local1250_4) {
      float local1250_5 = sf();
      float mouseX = local1250_2 * local1250_5;
      float mouseY = local1250_3 * local1250_5;
      this.settingRenderContext
         .renderElement(
            drawContext, 1.13F * Math.min(1.0F, Math.min(this.width * local1250_5 / 640.0F, this.height * local1250_5 / 460.0F)), mouseX, mouseY
         );
      local1250_5 = Math.min(0.05F, (float)(System.nanoTime() / 1000000L - this.timeMillis2) / 1000.0F);
      this.timeMillis2 = System.nanoTime() / 1000000L;
      InterfaceModule.getInstance().updateState();
      Render2D.renderElement5(drawContext);
      boolean local1250_8 = !this.flag2;
      if (local1250_8 != this.flag) {
         this.floatVal2 = this.floatVal;
         this.timeMillis = System.nanoTime() / 1000000L;
         this.flag = local1250_8;
      }

      mouseX = Math.min(1.0F, (float)(System.nanoTime() / 1000000L - this.timeMillis) / 300.0F);
      mouseY = mouseX * mouseX * mouseX * (mouseX * (mouseX * 6.0F - 15.0F) + 10.0F);
      this.floatVal = this.floatVal2 + ((local1250_8 ? 1.0F : 0.0F) - this.floatVal2) * mouseY;
      if (this.flag2 && this.floatVal <= 0.003F) {
         GuiEffectsState.processData(false);
         Render2D.updateState6();
         if (!this.flag3) {
            this.flag3 = true;
            MinecraftClient.getInstance().execute(() -> {
               if (MinecraftClient.getInstance().currentScreen == this) {
                  MinecraftClient.getInstance().setScreen(null);
               }
            });
         }
      } else {
         float local1250_9 = this.width * sf();
         float local1250_10 = this.height * sf();
         GuiEffectsState.processData(true);
         GuiEffectsState.processData2(8.0F);
         GuiEffectsState.processData4(0.0F);
         drawContext.createNewRootLayer();
         if (this.flag4) {
            try {
               drawContext.applyBlur();
            } catch (IllegalStateException exception) {
            }
         }

         Render2D.processData46(this.floatVal);
         float local1250_11 = Math.min(266.5F * this.settingRenderContext.scale, local1250_10 - 60.0F * this.settingRenderContext.scale);
         float local1250_12 = (local1250_10 - local1250_11) / 2.0F;

         for (int index = 0; index < this.moduleCategoryArray.length; index++) {
            this.floatValArray[index][0] = (
                     local1250_9
                        - (this.moduleCategoryArray.length * 112.5F * this.settingRenderContext.scale + (this.moduleCategoryArray.length - 1) * 9.0F * this.settingRenderContext.scale)
                  )
                  / 2.0F
               + index * 121.5F * this.settingRenderContext.scale;
            this.floatValArray[index][1] = local1250_12;
            this.floatValArray[index][2] = 112.5F * this.settingRenderContext.scale;
            this.floatValArray[index][3] = local1250_11;
            ModuleCategoryPanel.processData(
               this.settingRenderContext,
               this.moduleCategoryArray[index],
               this.recoveredClass0320Array[index],
               this.floatValArray[index][0],
               this.floatValArray[index][1],
               this.floatValArray[index][2],
               this.floatValArray[index][3],
               local1250_5
            );
         }

         this.drawTip(local1250_9, local1250_12);
         this.drawSearchBar(local1250_9, local1250_10, local1250_12 + local1250_11);
         if (this.cloudModalOpen) {
            this.drawCloudModal(local1250_9, local1250_10);
         }
         this.settingRenderContext.updateState();
         Render2D.updateState();
         this.updateDrag(local1250_2 * sf(), local1250_3 * sf());
         Render2D.updateState6();
      }
   }

   private void finishOverlay() {
      GuiEffectsState.processData(false);
      this.flag4 = false;
      this.flag2 = false;
      this.flag3 = false;
      this.floatVal = 0.0F;
      this.floatVal2 = 0.0F;
      this.flag = false;
      this.timeMillis = 0L;
   }

   @Override
   public boolean shouldPause() {
      return false;
   }

   private int searchWordHop(int local1253_1) {
      String local1253_2 = this.textSetting.getText();
      int local1253_3 = this.textSetting.getCaret();
      if (local1253_1 < 0) {
         while (local1253_3 > 0 && local1253_2.charAt(local1253_3 - 1) == ' ') {
            local1253_3--;
         }

         while (local1253_3 > 0 && local1253_2.charAt(local1253_3 - 1) != ' ') {
            local1253_3--;
         }

         return local1253_3 - this.textSetting.getCaret();
      } else {
         while (local1253_3 < local1253_2.length() && local1253_2.charAt(local1253_3) != ' ') {
            local1253_3++;
         }

         while (local1253_3 < local1253_2.length() && local1253_2.charAt(local1253_3) == ' ') {
            local1253_3++;
         }

         return local1253_3 - this.textSetting.getCaret();
      }
   }

   @Override
   public void removed() {
      GuiEffectsState.processData(false);
      if (!this.flag4) {
         this.flag2 = false;
         this.flag3 = false;
         this.floatVal = 0.0F;
         this.floatVal2 = 0.0F;
         this.flag = false;
         this.timeMillis = 0L;
      }

      this.settingRenderContext.textSetting = null;
      this.settingRenderContext.nested0492 = null;
      this.settingRenderContext.keybindSetting = null;
      this.settingRenderContext.object = null;
      this.settingRenderContext.module = null;
      this.flag5 = false;
      this.textSetting.setValue("");
      this.settingRenderContext.strVal = "";
      super.removed();
   }

   @Override
   public boolean charTyped(CharInput charInput) {
      if (this.settingRenderContext.nested0492 != null) {
         if (charInput.codepoint() >= 32) {
            this.settingRenderContext.nested0492.processData20((char)charInput.codepoint());
         }

         return true;
      } else if (this.cloudInputActive) {
         if (charInput.codepoint() >= 32) {
            this.cloudCodeInput.insertText(String.valueOf((char)charInput.codepoint()));
         }
         return true;
      } else if (this.settingRenderContext.textSetting != null) {
         if (charInput.codepoint() >= 32) {
            this.settingRenderContext.textSetting.insertText(String.valueOf((char)charInput.codepoint()));
         }

         return true;
      } else if (this.flag5) {
         if (charInput.codepoint() >= 32) {
            this.textSetting.insertText(String.valueOf((char)charInput.codepoint()));
            this.settingRenderContext.strVal = this.textSetting.getText().toLowerCase();
            this.timeMillis3 = System.nanoTime() / 1000000L;
         }

         return true;
      } else {
         return super.charTyped(charInput);
      }
   }

   private static float sf() {
      return GuiScale.computeInt() / GuiScale.computeFloat();
   }

   private void updateDrag(float local1259_1, float local1259_2) {
      if (this.settingRenderContext.numberSetting != null || this.settingRenderContext.colorSetting != null) {
         float local1259_3 = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), 0) == 1 ? 1.0F : 0.0F;
         if (this.settingRenderContext.numberSetting != null) {
            if (local1259_3 < 0.5F) {
               this.settingRenderContext.numberSetting = null;
            } else if (this.settingRenderContext.map7.get(this.settingRenderContext.numberSetting) != null) {
               this.settingRenderContext
                  .numberSetting
                  .setFraction(
                     SettingRenderContext.computeFloat(
                        (local1259_1 - this.settingRenderContext.map7.get(this.settingRenderContext.numberSetting)[0])
                           / this.settingRenderContext.map7.get(this.settingRenderContext.numberSetting)[1],
                        0.0F,
                        1.0F
                     )
                  );
            }
         }

         if (this.settingRenderContext.colorSetting != null) {
            if (local1259_3 < 0.5F) {
               this.settingRenderContext.colorSetting = null;
            } else if (this.settingRenderContext.map8.get(this.settingRenderContext.colorSetting) != null) {
               float[] floatArray = this.settingRenderContext.computeFloat5(this.settingRenderContext.colorSetting);
               float[] floatArray2 = this.settingRenderContext.map8.get(this.settingRenderContext.colorSetting);
               if (this.settingRenderContext.intVal2 == 0) {
                  floatArray[1] = SettingRenderContext.computeFloat((local1259_1 - floatArray2[0]) / floatArray2[2], 0.0F, 1.0F);
                  floatArray[2] = SettingRenderContext.computeFloat(1.0F - (local1259_2 - floatArray2[1]) / floatArray2[3], 0.0F, 1.0F);
               } else {
                  floatArray[0] = SettingRenderContext.computeFloat((local1259_1 - floatArray2[4]) / floatArray2[6], 0.0F, 1.0F);
               }

               this.settingRenderContext.processData10(this.settingRenderContext.colorSetting, floatArray);
            }
         }
      }
   }

   @Override
   public boolean keyPressed(KeyInput keyInput) {
      if (this.flag2) {
         return true;
      }

      int local1260_2 = keyInput.key();
      if (this.cloudInputActive) {
         if (local1260_2 == 256) {
            this.cloudInputActive = false;
            return true;
         } else if (local1260_2 == 257) {
            String cd = this.cloudCodeInput.getText();
            if (!cd.trim().isEmpty()) {
               this.cloudStatusText = "Загрузка...";
               horus.client.util.CloudConfigManager.loadConfigAsync(cd).thenAccept(ok -> {
                  this.cloudStatusText = horus.client.util.CloudConfigManager.getLastStatusMessage();
               });
            }
            return true;
         } else if (local1260_2 == 259) {
            this.cloudCodeInput.backspace();
            return true;
         } else if ((keyInput.modifiers() & 2) != 0 && local1260_2 == 86) {
            this.cloudCodeInput.insertText(MinecraftClient.getInstance().keyboard.getClipboard());
            return true;
         }
         return true;
      }
      if (this.settingRenderContext.module != null) {
         this.settingRenderContext.module.withKeyCode(local1260_2 != 256 && local1260_2 != 261 && local1260_2 != 344 ? local1260_2 : -1);
         this.settingRenderContext.module = null;
         return true;
      }

      if (this.settingRenderContext.keybindSetting != null) {
         this.settingRenderContext.keybindSetting.withKeyCode(local1260_2 != 256 && local1260_2 != 261 ? local1260_2 : -1);
         this.settingRenderContext.keybindSetting = null;
         return true;
      }

      if (this.settingRenderContext.nested0492 != null) {
         if (local1260_2 == 256) {
            this.settingRenderContext.nested0492 = null;
         } else {
            this.settingRenderContext.nested0492.processData19(local1260_2);
         }

         return true;
      } else if (this.settingRenderContext.textSetting != null) {
         if ((keyInput.modifiers() & 2) != 0 && local1260_2 == 65) {
            this.settingRenderContext.textSetting.selectAll();
         } else if ((keyInput.modifiers() & 2) != 0 && local1260_2 == 67 && this.settingRenderContext.textSetting.hasSelection()) {
            MinecraftClient.getInstance().keyboard.setClipboard(this.settingRenderContext.textSetting.getSelectedText());
         } else if ((keyInput.modifiers() & 2) != 0 && local1260_2 == 88 && this.settingRenderContext.textSetting.hasSelection()) {
            MinecraftClient.getInstance().keyboard.setClipboard(this.settingRenderContext.textSetting.getSelectedText());
            this.settingRenderContext.textSetting.backspace();
         } else if ((keyInput.modifiers() & 2) != 0 && local1260_2 == 86) {
            this.settingRenderContext.textSetting.insertText(MinecraftClient.getInstance().keyboard.getClipboard());
         } else if (local1260_2 == 259) {
            this.settingRenderContext.textSetting.backspace();
         } else if (local1260_2 == 261) {
            this.settingRenderContext.textSetting.deleteForward();
         } else if (local1260_2 == 263) {
            this.settingRenderContext.textSetting.moveCaret(-1, (keyInput.modifiers() & 1) != 0);
         } else if (local1260_2 == 262) {
            this.settingRenderContext.textSetting.moveCaret(1, (keyInput.modifiers() & 1) != 0);
         } else if (local1260_2 == 268) {
            this.settingRenderContext.textSetting.moveToStart((keyInput.modifiers() & 1) != 0);
         } else if (local1260_2 == 269) {
            this.settingRenderContext.textSetting.moveToEnd((keyInput.modifiers() & 1) != 0);
         } else if (local1260_2 == 257 || local1260_2 == 256) {
            this.settingRenderContext.textSetting = null;
         }

         return true;
      } else {
         if (this.settingRenderContext.object != null && local1260_2 == 256) {
            this.settingRenderContext.object = null;
            return true;
         }

         if ((keyInput.modifiers() & 2) != 0 && local1260_2 == 70) {
            this.flag5 = !this.flag5;
            if (!this.flag5) {
               this.textSetting.setValue("");
               this.settingRenderContext.strVal = "";
            }

            this.timeMillis3 = System.nanoTime() / 1000000L;
            return true;
         } else if (!this.flag5) {
            if (local1260_2 != 344 && local1260_2 != 256) {
               return super.keyPressed(keyInput);
            }

            this.close();
            return true;
         } else if (local1260_2 != 256 && local1260_2 != 257) {
            boolean local1260_3 = (keyInput.modifiers() & 2) != 0;
            boolean local1260_4 = (keyInput.modifiers() & 1) != 0;
            String local1260_5 = this.textSetting.getText();
            if (local1260_3 && local1260_2 == 65) {
               this.textSetting.selectAll();
            } else if (local1260_3 && local1260_2 == 67 && this.textSetting.hasSelection()) {
               MinecraftClient.getInstance().keyboard.setClipboard(this.textSetting.getSelectedText());
            } else if (local1260_3 && local1260_2 == 88 && this.textSetting.hasSelection()) {
               MinecraftClient.getInstance().keyboard.setClipboard(this.textSetting.getSelectedText());
               this.textSetting.backspace();
            } else if (local1260_3 && local1260_2 == 86) {
               this.textSetting.insertText(MinecraftClient.getInstance().keyboard.getClipboard());
            } else if (local1260_2 == 259) {
               if (local1260_3 && !this.textSetting.hasSelection()) {
                  this.textSetting.moveCaret(this.searchWordHop(-1), true);
               }

               this.textSetting.backspace();
            } else if (local1260_2 == 261) {
               if (local1260_3 && !this.textSetting.hasSelection()) {
                  this.textSetting.moveCaret(this.searchWordHop(1), true);
               }

               this.textSetting.deleteForward();
            } else if (local1260_2 == 263) {
               this.textSetting.moveCaret(local1260_3 ? this.searchWordHop(-1) : -1, local1260_4);
            } else if (local1260_2 == 262) {
               this.textSetting.moveCaret(local1260_3 ? this.searchWordHop(1) : 1, local1260_4);
            } else if (local1260_2 == 268) {
               this.textSetting.moveToStart(local1260_4);
            } else if (local1260_2 == 269) {
               this.textSetting.moveToEnd(local1260_4);
            }

            if (!local1260_5.equals(this.textSetting.getText())) {
               this.settingRenderContext.strVal = this.textSetting.getText().toLowerCase();
               this.timeMillis3 = System.nanoTime() / 1000000L;
            }

            return true;
         } else {
            this.flag5 = false;
            this.textSetting.setValue("");
            this.settingRenderContext.strVal = "";
            return true;
         }
      }
   }

   private void drawTip(float local1261_1, float local1261_2) {
      if (this.settingRenderContext.strVal2 != null) {
         this.settingRenderContext.strVal3 = this.settingRenderContext.strVal2;
      }

      float local1261_3 = this.settingRenderContext.computeFloat7("tip.show", this.settingRenderContext.strVal2 != null ? 1.0F : 0.0F);
      if (this.settingRenderContext.strVal2 != null) {
         local1261_3 = 1.0F;
      }

      if (!(local1261_3 < 0.01F) && !this.settingRenderContext.strVal3.isEmpty()) {
         float local1261_4 = this.settingRenderContext.scale;
         List<String> local1261_5 = UiTheme.getList(FontFace.SF, this.settingRenderContext.strVal3, 6.0F * local1261_4, 240.0F * local1261_4);
         float local1261_6 = local1261_5.size() * 6.95F * local1261_4;
         float local1261_7 = Math.max(5.0F * local1261_4, local1261_2 - 29.6F * local1261_4 + (13.9F * local1261_4 - local1261_6) / 2.0F);
         Render2D.processData46(local1261_3);

         for (String local1261_9 : local1261_5) {
            FontText.processData3(
               FontFace.SF,
               local1261_9,
               (local1261_1 - FontText.computeFloat(FontFace.SF, local1261_9, 6.0F * local1261_4)) / 2.0F,
               local1261_7,
               6.0F * local1261_4,
               ClickGuiStyle.intVal40
            );
            local1261_7 += 6.95F * local1261_4;
         }

         Render2D.updateState();
      }
   }

   @Override
   public boolean mouseScrolled(double local1262_1, double local1262_3, double local1262_5, double local1262_7) {
      if (this.flag2) {
         return true;
      }

      float local1262_9 = sf();
      float local1262_10 = (float)local1262_1 * local1262_9;
      float local1262_11 = (float)local1262_3 * local1262_9;
      if (this.settingRenderContext.floatValArray != null
         && UiTheme.matchesCriteria(
            local1262_10,
            local1262_11,
            this.settingRenderContext.floatValArray[0],
            this.settingRenderContext.floatValArray[1],
            this.settingRenderContext.floatValArray[2],
            this.settingRenderContext.floatValArray[3]
         )) {
         this.settingRenderContext
            .map10
            .put(
               this.settingRenderContext.object2,
               SettingRenderContext.computeFloat(
                  this.settingRenderContext.map10.getOrDefault(this.settingRenderContext.object2, 0.0F) - (float)local1262_7 * 17.86F * this.settingRenderContext.scale,
                  0.0F,
                  this.settingRenderContext.floatVal2
               )
            );
         return true;
      }

      if (this.settingRenderContext.intVal > 0) {
         return true;
      }

      for (int index = 0; index < this.moduleCategoryArray.length; index++) {
         if (this.floatValArray[index] != null
            && UiTheme.matchesCriteria(
               local1262_10,
               local1262_11,
               this.floatValArray[index][0],
               this.floatValArray[index][1],
               this.floatValArray[index][2],
               this.floatValArray[index][3]
            )) {
            this.recoveredClass0320Array[index].targetOffset = SettingRenderContext.computeFloat(
               this.recoveredClass0320Array[index].targetOffset - (float)local1262_7 * 24.0F * this.settingRenderContext.scale,
               0.0F,
               this.recoveredClass0320Array[index].maximumOffset
            );
            return true;
         }
      }

      return super.mouseScrolled(local1262_1, local1262_3, local1262_5, local1262_7);
   }

   private ClickGuiScreen() {
      super(Text.literal("Horus ClickGUI"));
      this.recoveredClass0320Array = new SmoothScroll[this.moduleCategoryArray.length];
      this.floatValArray = new float[this.moduleCategoryArray.length][4];

      for (int index = 0; index < this.moduleCategoryArray.length; index++) {
         this.recoveredClass0320Array[index] = new SmoothScroll();
      }
   }

   public SettingRenderContext getCtx() {
      return this.settingRenderContext;
   }

   @Override
   protected void init() {
      super.init();
      if (horus.client.module.misc.SafeModModule.getInstance().isLockGui()) {
         this.close();
         return;
      }
      if (this.flag4) {
         ScreenClosingTransition.processData3(this);
         this.flag4 = false;
         this.flag2 = false;
         this.flag3 = false;
      }
   }

   @Override
   public boolean renderClosing(DrawContext drawContext) {
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      if (this.flag4 && minecraftClient.player != null) {
         this.render(drawContext, (int)minecraftClient.mouse.getScaledX(minecraftClient.getWindow()), (int)minecraftClient.mouse.getScaledY(minecraftClient.getWindow()), 0.0F);
         if (this.floatVal <= 0.003F) {
            this.finishOverlay();
            return false;
         } else {
            return true;
         }
      } else {
         this.finishOverlay();
         return false;
      }
   }

   @Override
   public boolean mouseClicked(Click click, boolean local1267_2) {
      if (this.flag2) {
         return true;
      }

      if (this.settingRenderContext.keybindSetting != null) {
         if (click.button() != 0) {
            this.settingRenderContext.keybindSetting.setBinding(InputBinding.mouseButton(click.button()));
         }

         this.settingRenderContext.keybindSetting = null;
         return true;
      } else {
         if (click.button() == 0 || click.button() == 1 || click.button() == 2) {
            float local1267_3 = sf();
            float local1267_4 = (float)click.x() * local1267_3;
            float local1267_5 = (float)click.y() * local1267_3;
            if (click.button() == 0) {
               this.settingRenderContext.textSetting = null;
               this.settingRenderContext.nested0492 = null;
               this.settingRenderContext.module = null;
            }

            for (int index = this.settingRenderContext.list.size() - 1; index >= this.settingRenderContext.intVal; index--) {
               SettingRenderContext.DataRecord2 local1267_7 = this.settingRenderContext.list.get(index);
               if (UiTheme.matchesCriteria(local1267_4, local1267_5, local1267_7.x(), local1267_7.y(), local1267_7.w(), local1267_7.h())) {
                  if (click.button() == 0) {
                     local1267_7.action().run();
                     return true;
                  }

                  if (click.button() == 1 && local1267_7.rightAction() != null) {
                     local1267_7.rightAction().run();
                     return true;
                  }

                  if (click.button() == 2 && local1267_7.middleAction() != null) {
                     local1267_7.middleAction().run();
                     return true;
                  }
               }
            }

            if (this.settingRenderContext.intVal > 0) {
               return true;
            }
         }

         return super.mouseClicked(click, local1267_2);
      }
   }
   private void drawCloudModal(float screenW, float screenH) {
      float scale = this.settingRenderContext.scale;
      float modalW = 205.0F * scale;
      float modalH = 118.0F * scale;
      float modalX = (screenW - modalW) / 2.0F;
      float modalY = (screenH - modalH) / 2.0F;

      Render2D.processData60(modalX, modalY, modalW, modalH, 8.0F * scale, 35.0F, 1.0F, Colors.computeInt7(0x00F5C4, 60));
      Render2D.processData51(modalX, modalY, modalW, modalH, 8.0F * scale, 0xF008080E);
      Render2D.processData86(modalX, modalY, modalW, modalH, 8.0F * scale, 1.0F, Colors.computeInt7(0x00F5C4, 200));

      FontText.processData3(FontFace.SEMI_BOLD, "☁ ОБМЕН КОНФИГАМИ (SUPABASE)", modalX + 12.0F * scale, modalY + 10.0F * scale, 6.0F * scale, -1);

      float closeX = modalX + modalW - 16.0F * scale;
      float closeY = modalY + 8.0F * scale;
      FontText.processData3(FontFace.SEMI_BOLD, "✕", closeX, closeY, 7.0F * scale, Colors.computeInt7(-1, 180));
      this.settingRenderContext.processData7(closeX - 2.0F * scale, closeY - 2.0F * scale, 14.0F * scale, 14.0F * scale, null, () -> {
         this.cloudModalOpen = false;
         this.cloudInputActive = false;
      });

      String activeCfg = horus.client.storage.ConfigStore.getActiveConfig();
      FontText.processData3(FontFace.SF, "Поделиться: " + activeCfg, modalX + 12.0F * scale, modalY + 26.0F * scale, 5.0F * scale, Colors.computeInt7(-1, 200));

      float shareBtnX = modalX + 12.0F * scale;
      float shareBtnY = modalY + 36.0F * scale;
      float shareBtnW = modalW - 24.0F * scale;
      float shareBtnH = 15.0F * scale;

      Render2D.processData51(shareBtnX, shareBtnY, shareBtnW, shareBtnH, 4.0F * scale, 0xFF0D2E28);
      Render2D.processData86(shareBtnX, shareBtnY, shareBtnW, shareBtnH, 4.0F * scale, 0.8F, 0xFF00F5C4);
      FontText.processData3(FontFace.SEMI_BOLD, "✦ СГЕНЕРИРОВАТЬ КОД (ПОДЕЛИТЬСЯ)", shareBtnX + 14.0F * scale, shareBtnY + 4.5F * scale, 5.0F * scale, 0xFF00F5C4);

      this.settingRenderContext.processData7(shareBtnX, shareBtnY, shareBtnW, shareBtnH, null, () -> {
         this.cloudStatusText = "Загрузка в Supabase...";
         horus.client.util.CloudConfigManager.shareConfigAsync(activeCfg).thenAccept(code -> {
            if (code != null) {
               this.cloudStatusText = "Код " + code + " скопирован!";
            } else {
               this.cloudStatusText = horus.client.util.CloudConfigManager.getLastStatusMessage();
            }
            this.cloudStatusTime = System.currentTimeMillis();
         });
      });

      FontText.processData3(FontFace.SF, "Импорт по коду:", modalX + 12.0F * scale, modalY + 59.0F * scale, 5.0F * scale, Colors.computeInt7(-1, 200));

      float inputX = modalX + 12.0F * scale;
      float inputY = modalY + 68.0F * scale;
      float inputW = modalW - 74.0F * scale;
      float inputH = 16.0F * scale;

      Render2D.processData51(inputX, inputY, inputW, inputH, 4.0F * scale, 0xFF040407);
      Render2D.processData86(inputX, inputY, inputW, inputH, 4.0F * scale, 0.8F, this.cloudInputActive ? 0xFF00F5C4 : Colors.computeInt7(-1, 80));

      String codeText = this.cloudCodeInput.getText();
      String displayText = codeText.isEmpty() ? (this.cloudInputActive ? "" : "H-XXXX") : codeText;
      FontText.processData3(FontFace.SF, displayText, inputX + 6.0F * scale, inputY + 5.0F * scale, 5.5F * scale, codeText.isEmpty() ? Colors.computeInt7(-1, 100) : -1);

      this.settingRenderContext.processData7(inputX, inputY, inputW, inputH, null, () -> {
         this.cloudInputActive = true;
         this.flag5 = false;
         this.cloudCodeInput.moveToEnd(false);
      });

      float loadBtnX = inputX + inputW + 6.0F * scale;
      float loadBtnY = inputY;
      float loadBtnW = 44.0F * scale;
      float loadBtnH = inputH;

      Render2D.processData51(loadBtnX, loadBtnY, loadBtnW, loadBtnH, 4.0F * scale, 0xFF141420);
      Render2D.processData86(loadBtnX, loadBtnY, loadBtnW, loadBtnH, 4.0F * scale, 0.8F, Colors.computeInt7(-1, 120));
      FontText.processData3(FontFace.SEMI_BOLD, "ЗАГРУЗИТЬ", loadBtnX + 5.0F * scale, loadBtnY + 5.0F * scale, 5.0F * scale, -1);

      this.settingRenderContext.processData7(loadBtnX, loadBtnY, loadBtnW, loadBtnH, null, () -> {
         String cd = this.cloudCodeInput.getText();
         if (!cd.trim().isEmpty()) {
            this.cloudStatusText = "Загрузка...";
            horus.client.util.CloudConfigManager.loadConfigAsync(cd).thenAccept(ok -> {
               this.cloudStatusText = horus.client.util.CloudConfigManager.getLastStatusMessage();
               this.cloudStatusTime = System.currentTimeMillis();
            });
         } else {
            this.cloudStatusText = "Введите код!";
            this.cloudStatusTime = System.currentTimeMillis();
         }
      });

      if (!this.cloudStatusText.isEmpty()) {
         FontText.processData3(FontFace.SEMI_BOLD, this.cloudStatusText, modalX + 12.0F * scale, modalY + 95.0F * scale, 5.0F * scale, 0xFF00F5C4);
      }
   }

}

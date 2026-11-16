package horus.client.ui;

import horus.client.storage.AccountEntry;
import horus.client.render.AccountPreviewRenderer;
import horus.client.render.FontFace;
import horus.client.module.render.InterfaceModule;
import ru.dreamix.protection.api.impl.profile.ProfileAPI;
import horus.client.util.GuiScale;
import horus.client.render.FontText;
import horus.client.ui.UiTheme;
import horus.client.ui.AnimatedValue;
import horus.client.render.Render2D;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class WelcomeScreen extends Screen {
   private final long timeMillis = System.currentTimeMillis();
   private long timeMillis2;
   private long timeMillis3;
   private final AnimatedValue animatedValue = new AnimatedValue(160L);
   private float floatVal;
   private float floatVal2;
   private float floatVal3;
   private float floatVal4;

   @Override
   public boolean mouseClicked(Click click, boolean local5201_2) {
      if (click.button() == 0) {
         long local5201_3 = System.currentTimeMillis();
         if (this.timeMillis2 == 0L) {
            if (local5201_3 - this.timeMillis > 400L) {
               this.timeMillis2 = local5201_3;
            }

            return true;
         }

         float local5201_5 = spaceFactor();
         if (this.timeMillis3 == 0L
            && UiTheme.matchesCriteria(
               (float)click.x() * local5201_5, (float)click.y() * local5201_5, this.floatVal, this.floatVal2, this.floatVal3, this.floatVal4
            )) {
            this.timeMillis3 = local5201_3;
            return true;
         }
      }

      return super.mouseClicked(click, local5201_2);
   }

   @Override
   public boolean shouldCloseOnEsc() {
      return false;
   }

   @Override
   public boolean shouldPause() {
      return false;
   }

   @Override
   public void renderBackground(DrawContext drawContext, int local5204_2, int local5204_3, float local5204_4) {
   }

   private static float spaceFactor() {
      return GuiScale.computeInt() / GuiScale.computeFloat();
   }

   private void drawSessionChip(float local5206_1, float local5206_2, float local5206_3) {
      MinecraftClient minecraftClient = this.client != null ? this.client : MinecraftClient.getInstance();
      String local5206_5 = minecraftClient != null && minecraftClient.getSession() != null ? minecraftClient.getSession().getUsername() : "Player";
      String local5206_6 = AccountPreviewRenderer.getSkinTextures(local5206_5, AccountEntry.getUUID(local5206_5)).body().texturePath().toString();
      Render2D.processData26(
         local5206_6,
         local5206_1,
         local5206_2,
         15.0F * local5206_3,
         15.0F * local5206_3,
         5.0F * local5206_3,
         1.0F,
         0.125F,
         0.125F,
         0.25F,
         0.25F,
         UiTheme.intVal
      );
      Render2D.processData26(
         local5206_6,
         local5206_1,
         local5206_2,
         15.0F * local5206_3,
         15.0F * local5206_3,
         5.0F * local5206_3,
         1.0F,
         0.625F,
         0.125F,
         0.75F,
         0.25F,
         UiTheme.intVal
      );
      UiTheme.processData3(
         "assets/horus/textures/gui/mainmenu/icons/person.png",
         local5206_1 + 22.2F * local5206_3,
         local5206_2 + 3.2F * local5206_3,
         3.4F * local5206_3,
         4.5F * local5206_3,
         UiTheme.intVal29
      );
      FontText.processData3(
         FontFace.SEMI,
         "Активная сессия",
         local5206_1 + 26.5F * local5206_3,
         local5206_2 + 0.5F * local5206_3,
         5.0F * local5206_3,
         UiTheme.intVal29
      );
      FontText.processData3(
         FontFace.SEMI_BOLD,
         ProfileAPI.get() != null
               && ProfileAPI.get().getModel() != null
               && ProfileAPI.get().getModel().getName() != null
               && !ProfileAPI.get().getModel().getName().isBlank()
            ? ProfileAPI.get().getModel().getName()
            : local5206_5,
         local5206_1 + 20.0F * local5206_3,
         local5206_2 + 7.5F * local5206_3,
         6.5F * local5206_3,
         UiTheme.intVal
      );
   }

   @Override
   public void render(DrawContext drawContext, int local5207_2, int local5207_3, float local5207_4) {
      InterfaceModule.getInstance().updateState();
      float local5207_5 = this.width * spaceFactor();
      float local5207_6 = this.height * spaceFactor();
      float local5207_7 = Math.min(local5207_5 / 960.0F, local5207_6 / 540.0F);
      float local5207_8 = (local5207_6 - 540.0F * local5207_7) / 2.0F;
      long local5207_9 = System.currentTimeMillis();
      UiTheme.flag = true;
      Render2D.renderElement5(drawContext);
      if (this.timeMillis2 == 0L && local5207_9 - this.timeMillis >= 2700L) {
         this.timeMillis2 = local5207_9;
      }

      float local5207_11 = this.timeMillis2 == 0L ? 0.0F : UiTheme.computeFloat2(UiTheme.computeFloat3(this.timeMillis2, 0L, 450L));
      UiTheme.processData6("assets/horus/textures/gui/mainmenu/background/loading.png", local5207_5, local5207_6, 1.0F);
      if (local5207_11 > 0.001F) {
         UiTheme.processData6("assets/horus/textures/gui/mainmenu/background/welcome.png", local5207_5, local5207_6, local5207_11);
      }

      if (local5207_11 < 0.999F) {
         Render2D.processData46(1.0F - local5207_11);
         UiTheme.processData4(this.timeMillis, local5207_5, local5207_6);
         Render2D.updateState();
      }

      if (local5207_11 > 0.001F) {
         float local5207_12 = this.timeMillis3 == 0L ? 0.0F : UiTheme.computeFloat2(UiTheme.computeFloat3(this.timeMillis3, 0L, 320L));
         Render2D.processData46(local5207_11 * (1.0F - local5207_12));
         this.drawWelcomePhase(
            local5207_6, local5207_7, local5207_8, local5207_2 * spaceFactor(), local5207_3 * spaceFactor(), local5207_12 * 26.0F * local5207_7
         );
         Render2D.updateState();
      }

      Render2D.updateState6();
   }

   @Override
   public void tick() {
      if (this.timeMillis3 != 0L && System.currentTimeMillis() - this.timeMillis3 >= 340L) {
         MinecraftClient minecraftClient = this.client != null ? this.client : MinecraftClient.getInstance();
         minecraftClient.setScreen(new MainMenuScreen(true));
      }
   }

   public WelcomeScreen() {
      super(Text.literal("Horus"));
   }

   private void drawWelcomePhase(float local5210_1, float local5210_2, float local5210_3, float local5210_4, float local5210_5, float local5210_6) {
      long local5210_7 = this.timeMillis2;
      float centerX = this.width * spaceFactor() / 2.0F;

      float local5210_10 = UiTheme.computeFloat2(UiTheme.computeFloat3(local5210_7, 0L, 420L));
      float local5210_9 = 51.5F * local5210_2 - local5210_6;
      Render2D.processData46(local5210_10);
      Render2D.processData89(
         "assets/horus/textures/gui/mainmenu/icons/logo.png",
         centerX - 6.75F * local5210_2,
         local5210_3 + 168.0F * local5210_2 + (1.0F - local5210_10) * 8.0F * local5210_2,
         13.5F * local5210_2,
         10.5F * local5210_2,
         0.0F,
         UiTheme.intVal7
      );
      Render2D.updateState();

      float local5210_11 = UiTheme.computeFloat2(UiTheme.computeFloat3(local5210_7, 90L, 460L));
      Render2D.processData46(local5210_11);
      float local5210_12 = centerX - (1.0F - local5210_11) * 14.0F * local5210_2;
      FontText.processData3(
         FontFace.SEMI, "Добро пожаловать в", local5210_12, local5210_3 + 190.0F * local5210_2, 20.0F * local5210_2, UiTheme.intVal
      );
      Render2D.updateState();

      float local5210_13 = UiTheme.computeFloat2(UiTheme.computeFloat3(local5210_7, 170L, 460L));
      Render2D.processData46(local5210_13);
      Render2D.processData89(
         "assets/horus/textures/gui/mainmenu/icons/horus_serif.png",
         centerX - 34.0F * local5210_2,
         local5210_3 + 216.0F * local5210_2 + (1.0F - local5210_13) * 10.0F * local5210_2,
         68.04957F * local5210_2,
         15.4F * local5210_2,
         0.0F,
         UiTheme.intVal7
      );
      Render2D.updateState();

      float local5210_14 = local5210_3 + 248.5F * local5210_2;
      float local5210_15 = UiTheme.computeFloat2(UiTheme.computeFloat3(local5210_7, 170L, 460L));
      Render2D.processData46(local5210_15);
      for (String local5210_16 : UiTheme.getList(
         FontFace.SEMI,
         "Всё готово для комфортной игры. Настройте клиент под себя, используйте любимые функции и откройте больше возможностей вместе с Horus.",
         7.5F * local5210_2,
         246.0F * local5210_2
      )) {
         float lineW = FontText.computeFloat(FontFace.SEMI, local5210_16, 7.5F * local5210_2);
         FontText.processData3(FontFace.SEMI, local5210_16, centerX - lineW / 2.0F, local5210_14, 7.5F * local5210_2, UiTheme.intVal28);
         local5210_14 += 9.0F * local5210_2;
      }

      Render2D.updateState();
      float local5210_16a = UiTheme.computeFloat2(UiTheme.computeFloat3(local5210_7, 250L, 460L));
      Render2D.processData46(local5210_16a);
      float btnW = 220.0F * local5210_2;
      float btnH = 24.0F * local5210_2;
      this.floatVal = centerX - btnW / 2.0F + (1.0F - local5210_16a) * 14.0F * local5210_2;
      this.floatVal2 = local5210_14 + 10.0F * local5210_2;
      this.floatVal3 = btnW;
      this.floatVal4 = btnH;
      this.animatedValue
         .processData(
            this.timeMillis3 == 0L
                  && UiTheme.matchesCriteria(local5210_4, local5210_5, this.floatVal, this.floatVal2, this.floatVal3, this.floatVal4)
               ? 1.0
               : 0.0
         );
      float local5210_16 = (float)this.animatedValue.computeDouble();
      boolean isWhite = (UiTheme.intVal7 & 0x00FFFFFF) >= 0xE0E0E0;
      Render2D.processData51(
         this.floatVal,
         this.floatVal2,
         this.floatVal3,
         this.floatVal4,
         12.0F * local5210_2,
         isWhite
            ? UiTheme.computeInt(-1, 0.16F + 0.12F * local5210_16)
            : UiTheme.computeInt2(UiTheme.intVal7, UiTheme.intVal8, local5210_16 * 0.65F)
      );
      Render2D.processData86(
         this.floatVal,
         this.floatVal2,
         this.floatVal3,
         this.floatVal4,
         12.0F * local5210_2,
         0.8F,
         isWhite ? UiTheme.computeInt(-1, 0.55F + 0.35F * local5210_16) : UiTheme.intVal34
      );
      UiTheme.processData3(
         "assets/horus/textures/gui/mainmenu/icons/arrow.png",
         this.floatVal + 11.0F * local5210_2 + local5210_16 * 2.0F * local5210_2,
         this.floatVal2 + this.floatVal4 / 2.0F,
         6.5F * local5210_2,
         6.5F * local5210_2,
         UiTheme.intVal10
      );
      FontText.processData3(
         FontFace.SEMI,
         "Перейти в главное меню",
         this.floatVal + 22.0F * local5210_2,
         this.floatVal2 + this.floatVal4 / 2.0F - 3.5F * local5210_2,
         7.0F * local5210_2,
         UiTheme.intVal10
      );
      Render2D.updateState();
      float local5210_17 = UiTheme.computeFloat2(UiTheme.computeFloat3(local5210_7, 330L, 460L));
      Render2D.processData46(local5210_17);
      this.drawSessionChip(local5210_9, local5210_1 - 51.0F * local5210_2 + (1.0F - local5210_17) * 10.0F * local5210_2, local5210_2);
      Render2D.updateState();
   }
}

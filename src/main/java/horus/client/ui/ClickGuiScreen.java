package horus.client.ui;

import horus.client.util.Colors;
import horus.client.render.FontFace;
import ru.dreamix.protection.api.impl.profile.ProfileAPI;
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

import java.util.ArrayList;
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

   public static class ProxyConfig {
      public String host;
      public String port;
      public String type;
      public String username;
      public String password;
      public boolean isWorking;

      public ProxyConfig(String host, String port, String type, String username, String password, boolean isWorking) {
         this.host = host;
         this.port = port;
         this.type = type;
         this.username = username;
         this.password = password;
         this.isWorking = isWorking;
      }

      public String getHostPort() {
         return this.host + ":" + this.port;
      }
   }

   private final List<ProxyConfig> savedProxies = new ArrayList<>();
   private boolean isProxyServerSectionVisible = false;
   private float proxySectionAnim = 0.0F;
   private boolean isProxyChecking = false;
   private String proxyCheckResult = "";

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
   private final float[] searchHitbox = new float[4];
   private final TextSetting textSetting = new TextSetting("Поиск", "").withLengthRange(0, 28);
   private boolean cloudModalOpen = false;
   private float cloudModalX = -1.0F;
   private float cloudModalY = -1.0F;
   private boolean cloudDragging = false;
   private float cloudDragOffX = 0.0F;
   private float cloudDragOffY = 0.0F;
   private final TextSetting cloudCodeInput = new TextSetting("Код", "").withLengthRange(0, 16);
   private boolean cloudInputActive = false;
   private String cloudStatusText = "";
   private long cloudStatusTime = 0L;
   private long timeMillis3;
   private static ClickGuiScreen clickGuiScreen;
   private Screen parentScreen;
   private int currentCategoryIndex;
   private float windowX;
   private float windowY;
   private float windowW;
   private float windowH;

   public void checkAndSaveProxy(String type, String host, String port, String user, String pass) {
      if (host == null || host.isEmpty() || port == null || port.isEmpty()) {
         this.proxyCheckResult = "Укажите хост и порт";
         return;
      }

      this.isProxyChecking = true;
      this.proxyCheckResult = "Проверка...";
      
      boolean isValid = testProxyConnection(host, port);
      
      if (isValid) {
         this.savedProxies.add(new ProxyConfig(host, port, type, user, pass, true));
         this.proxyCheckResult = "Прокси сохранен и работает";
         this.isProxyServerSectionVisible = true;
         this.proxySectionAnim = 0.0F;
      } else {
         this.proxyCheckResult = "Ошибка соединения";
      }
      
      this.isProxyChecking = false;
   }

   private boolean testProxyConnection(String host, String port) {
      try {
         int portNum = Integer.parseInt(port);
         if (portNum < 0 || portNum > 65535) return false;
         try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, portNum), 3000);
            return true;
         }
      } catch (Exception e) {
         return false;
      }
   }

   public void removeProxy(int index) {
      if (index >= 0 && index < this.savedProxies.size()) {
         this.savedProxies.remove(index);
         if (this.savedProxies.isEmpty()) {
            this.isProxyServerSectionVisible = false;
         }
      }
   }

   public void connectProxy(int index) {
      if (index >= 0 && index < this.savedProxies.size()) {
         ProxyConfig config = this.savedProxies.get(index);
         this.proxyCheckResult = "Подключено к " + config.getHostPort();
      }
   }

   private void updateProxyAnimation() {
      if (this.isProxyServerSectionVisible) {
         if (this.proxySectionAnim < 1.0F) {
            this.proxySectionAnim += 0.1F;
            if (this.proxySectionAnim > 1.0F) this.proxySectionAnim = 1.0F;
         }
      } else {
         if (this.proxySectionAnim > 0.0F) {
            this.proxySectionAnim -= 0.1F;
            if (this.proxySectionAnim < 0.0F) this.proxySectionAnim = 0.0F;
         }
      }
   }

   private void renderProxySection(float winX, float winY, float winW, float winH, float scale) {
      if (this.proxySectionAnim <= 0.01F) return;

      float anim = this.proxySectionAnim;
      float slide = (1.0F - anim) * 20.0F * scale;

      float panelX = winX + 10.0F * scale;
      float panelY = winY + 10.0F * scale + slide;
      float panelW = 170.0F * scale;
      float panelH = 25.0F * scale + this.savedProxies.size() * 20.0F * scale;

      int alpha = (int) (anim * 255.0F);

      Render2D.processData51(panelX, panelY, panelW, panelH, 8.0F * scale, Colors.computeInt7(0xFF181921, alpha));
      Render2D.processData86(panelX, panelY, panelW, panelH, 8.0F * scale, 0.5F, Colors.computeInt7(0xFF2B2E4A, alpha));

      FontText.processData3(
         FontFace.SF, "Сервера", panelX + 10.0F * scale, panelY + 10.0F * scale, 6.0F * scale, Colors.computeInt7(-1, alpha)
      );

      float itemY = panelY + 25.0F * scale;
      for (int i = 0; i < this.savedProxies.size(); i++) {
         ProxyConfig config = this.savedProxies.get(i);

         FontText.processData3(
            FontFace.SF,
            config.getHostPort(),
            panelX + 10.0F * scale,
            itemY + 2.0F * scale,
            5.0F * scale,
            Colors.computeInt7(-1, (int) (alpha * 0.8F))
         );

         float connectX = panelX + panelW - 50.0F * scale;
         Render2D.processData51(
            connectX, itemY - 1.0F * scale, 20.0F * scale, 12.0F * scale, 3.0F * scale,
            Colors.computeInt7(0xFF2B2E4A, alpha)
         );
         FontText.processData3(
            FontFace.SF, "Подкл.", connectX + 2.0F * scale, itemY + 2.0F * scale, 4.0F * scale,
            Colors.computeInt7(-1, alpha)
         );
         final int connectIndex = i;
         this.settingRenderContext.processData7(
            connectX, itemY - 1.0F * scale, 20.0F * scale, 12.0F * scale, null, () -> {
               this.proxyCheckResult = "Подключено: " + this.savedProxies.get(connectIndex).getHostPort();
            }
         );

         float deleteX = connectX + 23.0F * scale;
         Render2D.processData51(
            deleteX, itemY - 1.0F * scale, 20.0F * scale, 12.0F * scale, 3.0F * scale,
            Colors.computeInt7(0xFF4A2B2B, alpha)
         );
         FontText.processData3(
            FontFace.SF, "Удалить", deleteX + 1.0F * scale, itemY + 2.0F * scale, 4.0F * scale,
            Colors.computeInt7(-1, alpha)
         );
         final int deleteIndex = i;
         this.settingRenderContext.processData7(
            deleteX, itemY - 1.0F * scale, 20.0F * scale, 12.0F * scale, null, () -> {
               this.savedProxies.remove(deleteIndex);
               if (this.savedProxies.isEmpty()) {
                  this.isProxyServerSectionVisible = false;
               }
            }
         );

         itemY += 20.0F * scale;
      }

      if (this.proxyCheckResult != null && !this.proxyCheckResult.isEmpty()) {
         FontText.processData3(
            FontFace.SF,
            this.proxyCheckResult,
            panelX + 10.0F * scale,
            itemY + 2.0F * scale,
            4.0F * scale,
            Colors.computeInt7(0xFF2EAA4E, alpha)
         );
      }
   }

   private String profileName() {
      try {
         if (ProfileAPI.get() != null
            && ProfileAPI.get().getModel() != null
            && ProfileAPI.get().getModel().getName() != null
            && !ProfileAPI.get().getModel().getName().isBlank()) {
            return ProfileAPI.get().getModel().getName();
         }
      } catch (Exception ignored) {
      }

      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      return minecraftClient.getSession() != null ? minecraftClient.getSession().getUsername() : "Player";
   }

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
      float local1247_7 = local1247_1 - (local1247_5 + 36.0F * local1247_4) / 2.0F;
      float local1247_8 = local1247_2 + 2.8F * local1247_4;
      this.searchHitbox[0] = local1247_7;
      this.searchHitbox[1] = local1247_8;
      this.searchHitbox[2] = local1247_5;
      this.searchHitbox[3] = local1247_6;
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
         float scale = this.settingRenderContext.scale;
         float winW = Math.min(480.0F * scale, local1250_9 - 20.0F * scale);
         float winH = Math.min(270.0F * scale, local1250_10 - 20.0F * scale);
         float winX = (local1250_9 - winW) / 2.0F;
         float winY = (local1250_10 - winH) / 2.0F;
         this.windowX = winX;
         this.windowY = winY;
         this.windowW = winW;
         this.windowH = winH;
         float bar = 108.0F * scale;

         Render2D.processData51(winX, winY, winW, winH, 8.5F * scale, 0xFF181921);
         Render2D.processData51(winX + bar, winY + 25.0F * scale, winW - bar, 0.5F, 0.0F, 0xFF292A37);
         Render2D.processData51(winX + bar, winY, 0.5F, winH, 0.0F, 0xFF292A37);

         Render2D.processData89(
            "assets/horus/textures/gui/clickgui/cloud.png",
            winX + 23.0F * scale,
            winY + 10.0F * scale,
            14.0F * scale,
            14.0F * scale,
            0.0F,
            -1
         );
         FontText.processData3(FontFace.SF, "Horus", winX + 42.0F * scale, winY + 17.0F * scale, 7.0F * scale, -1);
         FontText.processData3(FontFace.SF, "build version: 38.1.5", winX + bar + 7.0F * scale, winY + 11.0F * scale, 5.0F * scale, Colors.computeInt7(-1, 120));

         float catLen = 0.0F;
         for (int index = 0; index < this.moduleCategoryArray.length; index++) {
            ModuleCategory moduleCategory = this.moduleCategoryArray[index];
            float rowY = winY + 53.0F * scale + catLen;
            boolean selected = index == this.currentCategoryIndex;
            boolean hovered = UiTheme.matchesCriteria(
               mouseX, mouseY, winX + 5.0F * scale, rowY, bar - 20.0F * scale, 18.0F * scale
            );
            float anim = this.settingRenderContext.computeFloat7("cat." + moduleCategory.name(), selected ? 1.0F : hovered ? 0.5F : 0.0F);
            if (anim > 0.001F) {
               Render2D.processData51(
                  winX + 5.0F * scale,
                  rowY,
                  bar - 20.0F * scale,
                  18.0F * scale,
                  3.0F * scale,
                  Colors.computeInt7(0xFF2B2E4A, (int)(150.0F * anim))
               );
               Render2D.processData51(
                  winX + 5.0F * scale,
                  rowY + 4.0F * scale,
                  1.5F * scale,
                  10.0F * scale,
                  0.75F * scale,
                  Colors.computeInt7(0xFF5D5F7C, (int)(255.0F * anim))
               );
            }

            Render2D.processData89(
               ClickGuiStyle.formatText(moduleCategory),
               winX + 15.0F * scale,
               rowY + 5.0F * scale,
               6.4F * scale,
               6.4F * scale,
               0.0F,
               selected ? -1 : Colors.computeInt7(-1, 200)
            );
            FontText.processData3(
               FontFace.SF,
               moduleCategory.getDisplayName(),
               winX + 26.0F * scale,
               rowY + 5.5F * scale,
               5.8F * scale,
               selected ? -1 : Colors.computeInt7(-1, 220)
            );
            catLen += 20.0F * scale;
         }

         Render2D.processData51(winX + 10.0F * scale, winY + winH - 30.0F * scale, bar - 20.0F * scale, 20.0F * scale, 3.0F * scale, 0xFF1D1F27);
         Render2D.processData89(
            "assets/horus/textures/gui/clickgui/ava.png",
            winX + 13.0F * scale,
            winY + winH - 27.0F * scale,
            14.0F * scale,
            14.0F * scale,
            0.0F,
            -1
         );
         Render2D.processData86(
            winX + 13.0F * scale,
            winY + winH - 27.0F * scale,
            14.0F * scale,
            14.0F * scale,
            7.0F * scale,
            1.0F,
            0xFF2EAA4E
         );
         FontText.processData3(FontFace.SF, this.profileName(), winX + 30.0F * scale, winY + winH - 25.0F * scale, 5.0F * scale, -1);
         FontText.processData3(FontFace.SF, "Till: 01.01.2057", winX + 30.0F * scale, winY + winH - 18.0F * scale, 4.2F * scale, Colors.computeInt7(-1, 120));

         float panelX = winX + bar + 7.0F * scale;
         float panelY = winY + 30.0F * scale;
         float panelW = winW - bar - 14.0F * scale;
         float panelH = winH - 30.0F * scale - 7.0F * scale;

         for (int index = 0; index < this.moduleCategoryArray.length; index++) {
            if (index == this.currentCategoryIndex) {
               if (this.floatValArray[index] == null) {
                  this.floatValArray[index] = new float[4];
               }
               this.floatValArray[index][0] = panelX;
               this.floatValArray[index][1] = panelY;
               this.floatValArray[index][2] = panelW;
               this.floatValArray[index][3] = panelH;
            } else {
               this.floatValArray[index] = null;
            }
         }

         ModuleCategoryPanel.processData(
            this.settingRenderContext,
            this.moduleCategoryArray[this.currentCategoryIndex],
            this.recoveredClass0320Array[this.currentCategoryIndex],
            panelX,
            panelY,
            panelW,
            panelH,
            local1250_5
         );

         this.drawTip(this.windowX + winW / 2.0F, panelY);
         this.drawSearchBar(winX + winW / 2.0F, winY + winH + 5.0F * scale, winW);

         if (!this.savedProxies.isEmpty()) {
            this.updateProxyAnimation();
            this.renderProxySection(winX, winY, winW, winH, scale);
         }
         this.drawCloudModal(winX, winY, winW, winH, local1250_9, local1250_10);
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
      this.cloudModalOpen = false;
      this.cloudModalX = -1.0F;
      this.cloudModalY = -1.0F;
      this.cloudDragging = false;
      this.cloudInputActive = false;
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
      this.cloudModalOpen = false;
      this.cloudModalX = -1.0F;
      this.cloudModalY = -1.0F;
      this.cloudDragging = false;
      this.cloudInputActive = false;
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

         if (!this.flag5) {
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
            // Keep the filter, but stop accepting text until the next search click.
            this.flag5 = false;
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
         float local1261_7 = this.windowY - local1261_6 - 10.0F * local1261_4;
         Render2D.processData46(local1261_3);

         float maxLineW = 0.0F;
         for (String local1261_10 : local1261_5) {
            maxLineW = Math.max(maxLineW, FontText.computeFloat(FontFace.SF, local1261_10, 6.0F * local1261_4));
         }

         float tipW = Math.max(60.0F * local1261_4, maxLineW + 14.0F * local1261_4);
         float tipH = local1261_6 + 8.0F * local1261_4;
         float tipX = local1261_1 - tipW / 2.0F;
         float tipY = local1261_7 - 4.0F * local1261_4;
         Render2D.processData60(tipX, tipY, tipW, tipH, 6.0F * local1261_4, 35.0F, 1.0F, -1);
         Render2D.processData51(tipX, tipY, tipW, tipH, 6.0F * local1261_4, 0xE0131319);
         Render2D.processData86(tipX, tipY, tipW, tipH, 6.0F * local1261_4, 1.0F, Colors.computeInt7(ClickGuiStyle.intVal3, 90));

         for (String local1261_9 : local1261_5) {
            FontText.processData3(
               FontFace.SF,
               local1261_9,
               local1261_1 - FontText.computeFloat(FontFace.SF, local1261_9, 6.0F * local1261_4) / 2.0F,
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

            // Клик мимо строки поиска снимает фокус: клавиши снова идут биндам,
            // а не тексту поиска. Поиск снова активируется кликом по нему.
            if (this.flag5 && !UiTheme.matchesCriteria(
               local1267_4, local1267_5, this.searchHitbox[0], this.searchHitbox[1], this.searchHitbox[2], this.searchHitbox[3]
            )) {
               this.flag5 = false;
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

            float sidebarS = this.settingRenderContext.scale;
            float sidebarRowY = this.windowY + 53.0F * sidebarS;
            for (int index = 0; index < this.moduleCategoryArray.length; index++) {
               if (UiTheme.matchesCriteria(
                  local1267_4,
                  local1267_5,
                  this.windowX + 5.0F * sidebarS,
                  sidebarRowY,
                  88.0F * sidebarS,
                  18.0F * sidebarS
               )) {
                  if (this.currentCategoryIndex != index) {
                     this.currentCategoryIndex = index;
                  }
                  return true;
               }
               sidebarRowY += 20.0F * sidebarS;
            }
         }

         return super.mouseClicked(click, local1267_2);
      }
   }
   private void drawCloudModal(float winX, float winY, float winW, float winH, float screenW, float screenH) {
      float scale = this.settingRenderContext.scale;
      float modalW = 205.0F * scale;
      float modalH = 118.0F * scale;

      float cloudAnim = this.settingRenderContext.computeFloat7("cloud.modal", this.cloudModalOpen ? 1.0F : 0.0F);
      if (cloudAnim < 0.01F) {
         return;
      }

      float modalX = winX + winW + 10.0F * scale;
      if (modalX + modalW > screenW - 5.0F * scale) {
         modalX = winX - modalW - 10.0F * scale;
      }
      float modalY = winY + (winH - modalH) / 2.0F;

      if (this.cloudModalX < 0.0F) {
         this.cloudModalX = modalX;
         this.cloudModalY = modalY;
      }

      if (this.cloudDragging) {
         if (GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), 0) != 1) {
            this.cloudDragging = false;
         } else {
            this.cloudModalX = this.settingRenderContext.mouseX - this.cloudDragOffX;
            this.cloudModalY = this.settingRenderContext.mouseY - this.cloudDragOffY;
         }
      }

      float slide = (1.0F - cloudAnim) * 14.0F * scale;
      float modalX2 = this.cloudModalX + slide;

      Render2D.processData46(cloudAnim);
      Render2D.processData60(modalX2, modalY, modalW, modalH, 8.0F * scale, 35.0F, 1.0F, Colors.computeInt7(0x00F5C4, 60));
      Render2D.processData51(modalX2, modalY, modalW, modalH, 8.0F * scale, 0xF008080E);
      Render2D.processData86(modalX2, modalY, modalW, modalH, 8.0F * scale, 1.0F, Colors.computeInt7(0x00F5C4, 200));

      FontText.processData3(FontFace.SEMI_BOLD, "☁ ОБМЕН КОНФИГАМИ (SUPABASE)", modalX2 + 12.0F * scale, modalY + 10.0F * scale, 6.0F * scale, -1);

      this.settingRenderContext.processData7(modalX2, modalY, modalW - 22.0F * scale, 22.0F * scale, null, () -> {
         this.cloudDragOffX = this.settingRenderContext.mouseX - this.cloudModalX;
         this.cloudDragOffY = this.settingRenderContext.mouseY - this.cloudModalY;
         this.cloudDragging = true;
      });

      float closeX = modalX2 + modalW - 16.0F * scale;
      float closeY = modalY + 8.0F * scale;
      FontText.processData3(FontFace.SEMI_BOLD, "✕", closeX, closeY, 7.0F * scale, Colors.computeInt7(-1, 180));
      this.settingRenderContext.processData7(closeX - 2.0F * scale, closeY - 2.0F * scale, 14.0F * scale, 14.0F * scale, null, () -> {
         this.cloudModalOpen = false;
         this.cloudInputActive = false;
      });

      String activeCfg = horus.client.storage.ConfigStore.getActiveConfig();
      FontText.processData3(FontFace.SF, "Поделиться: " + activeCfg, modalX2 + 12.0F * scale, modalY + 26.0F * scale, 5.0F * scale, Colors.computeInt7(-1, 200));

      float shareBtnX = modalX2 + 12.0F * scale;
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

      FontText.processData3(FontFace.SF, "Импорт по коду:", modalX2 + 12.0F * scale, modalY + 59.0F * scale, 5.0F * scale, Colors.computeInt7(-1, 200));

      float inputX = modalX2 + 12.0F * scale;
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
         FontText.processData3(FontFace.SEMI_BOLD, this.cloudStatusText, modalX2 + 12.0F * scale, modalY + 95.0F * scale, 5.0F * scale, 0xFF00F5C4);
      }

      Render2D.updateState();
   }

}

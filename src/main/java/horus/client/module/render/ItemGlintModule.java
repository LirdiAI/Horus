package horus.client.module.render;

import horus.client.event.EventListener;
import horus.client.event.HudRenderEvent;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.BooleanSetting;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.ResourceTexture;

@ModuleInfo(name = "GlintAura", description = "Шейдерная аура духов на предметах в руках: светящиеся волны движутся и мерцают. Работает на своих и чужих руках.", category = ModuleCategory.RENDER)
public final class ItemGlintModule extends Module {
   private static final int SIZE = 128;
   private static ItemGlintModule itemGlintModule;
   public BooleanSetting animationSetting;
   private NativeImageBackedTexture auraTexture;
   private NativeImage auraImage;
   private long lastWispUpdate;
   private Double savedGlintSpeed;
   private boolean applied;

   private ItemGlintModule() {
      this.animationSetting = this.addSetting(new BooleanSetting("Анимация", "Движущиеся духи в ауре. Выкл — статичная дымка без движения.", true));
   }

   public static ItemGlintModule getInstance() {
      if (itemGlintModule == null) {
         itemGlintModule = new ItemGlintModule();
      }

      return itemGlintModule;
   }

   @Override
   protected void onEnable() {
      // Текстуру подменяем лениво: модуль может включиться до готовности TextureManager
      this.applied = false;
      this.applyIfNeeded();
   }

   @Override
   protected void onDisable() {
      this.restoreVanillaGlint();
   }

   private void applyIfNeeded() {
      if (this.applied || mc.getTextureManager() == null) {
         return;
      }

      try {
         if (this.savedGlintSpeed == null) {
            this.savedGlintSpeed = mc.options.getGlintSpeed().getValue();
         }

         if (this.auraImage == null) {
            this.auraImage = new NativeImage(SIZE, SIZE, false);
         }

         if (this.auraTexture == null) {
            this.auraTexture = new NativeImageBackedTexture(() -> "horus_glint_aura", this.auraImage);
            this.drawWisps(System.currentTimeMillis());
         }

         mc.getTextureManager().registerTexture(ItemRenderer.ITEM_ENCHANTMENT_GLINT, this.auraTexture);
         this.applied = true;
      } catch (Exception exception) {
         horus.client.HorusClient.logger().warn("[GlintAura] Could not override glint texture", exception);
      }
   }

   private void restoreVanillaGlint() {
      if (!this.applied) {
         return;
      }

      try {
         mc.getTextureManager().registerTexture(ItemRenderer.ITEM_ENCHANTMENT_GLINT, new ResourceTexture(ItemRenderer.ITEM_ENCHANTMENT_GLINT));
      } catch (Exception exception) {
         horus.client.HorusClient.logger().warn("[GlintAura] Could not restore glint texture", exception);
      }

      if (this.savedGlintSpeed != null) {
         mc.options.getGlintSpeed().setValue(this.savedGlintSpeed);
      }

      this.auraTexture = null;
      this.auraImage = null;
      this.applied = false;
   }

   private final EventListener<HudRenderEvent> hudRenderListener = hudRenderEvent -> {
      if (!this.isEnabled()) {
         return;
      }

      this.applyIfNeeded();
      if (!this.applied || this.auraImage == null || this.auraTexture == null) {
         return;
      }

      boolean animate = this.animationSetting.isChecked();
      // Анимация: обычная скорость прокрутки; статика: полностью замирает
      Double target = animate ? this.savedGlintSpeed : Double.valueOf(0.0);
      if (target != null && Math.abs(mc.options.getGlintSpeed().getValue() - target) > 1.0E-4) {
         mc.options.getGlintSpeed().setValue(target);
      }

      if (animate && System.currentTimeMillis() - this.lastWispUpdate >= 40L) {
         this.lastWispUpdate = System.currentTimeMillis();
         this.drawWisps(this.lastWispUpdate);
      }
   };

   // Рисуем «духов»: живые фиолетово-белые волны, которые текут по текстуре блеска
   private void drawWisps(long timeMillis) {
      NativeImage image = this.auraImage;
      if (image == null) {
         return;
      }

      float t = (timeMillis % 1000000L) / 1000.0F;
      float step = (float) (Math.PI * 2.0) / SIZE;

      for (int y = 0; y < SIZE; y++) {
         float v = y * step;

         for (int x = 0; x < SIZE; x++) {
            float u = x * step;
            double warp = Math.sin(v * 2.0 + t * 0.9) * 1.2;
            double n = Math.sin(u * 2.0 + t * 1.4 + warp)
               + Math.sin(u * 1.5 + v * 3.0 - t * 1.8) * 0.8
               + Math.sin(v * 5.0 + u * 0.5 + t * 0.6) * 0.45;
            double wisp = Math.pow(Math.max(0.0, n * 0.5 + 0.62), 2.6);
            wisp = Math.min(1.0, wisp);
            int alpha = (int) (wisp * 240.0);
            float mix = 0.5F + 0.5F * (float) Math.sin(u * 1.5 - t * 0.7 + v * 0.5);
            int red = (int) (150.0F + 105.0F * mix);
            int green = (int) (110.0F + 80.0F * mix);
            int blue = 255;
            // NativeImage.setColor ждёт порядок ABGR
            image.setColor(x, y, alpha << 24 | blue << 16 | green << 8 | red);
         }
      }

      this.auraTexture.upload();
   }
}

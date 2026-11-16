package horus.client.module.player;

import horus.client.event.EventListener;
import horus.client.event.PlayerTickEvent;
import horus.client.event.WorldRenderEvent;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.BooleanSetting;
import horus.client.setting.ModeSetting;
import horus.client.setting.NumberSetting;
import horus.client.util.InputBinding;
import horus.mixin.KeyBindingMixin;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.hit.HitResult;

@ModuleInfo(
   name = "TapeMouse",
   description = "Автоматически зажимает или кликает выбранную кнопку мыши с заданной скоростью.",
   category = ModuleCategory.PLAYER
)
public final class TapeMouseModule extends Module {
   private static TapeMouseModule instance;

   private final ModeSetting buttonSetting = this.addSetting(
      new ModeSetting("Кнопка", "Какую кнопку мыши нажимать или удерживать.", "Левый", "Левый", "Правый")
   );

   private final ModeSetting modeSetting = this.addSetting(
      new ModeSetting("Режим", "Способ нажатия кнопки.", "Клик", "Клик", "Зажатие")
   );

   private final NumberSetting speedSetting = this.addSetting(
      new NumberSetting("Скорость", "Задержка между кликами в миллисекундах.")
         .withRange(10.0F, 1000.0F)
         .withStep(5.0F)
         .withSuffix(" мс")
         .setValue(50.0F)
   );

   private final BooleanSetting pauseInScreensSetting = this.addSetting(
      new BooleanSetting("Пауза в меню", "Приостанавливать работу в инвентаре, чате и других экранах.", true)
   );

   private final BooleanSetting onlyWhenHoldingSetting = this.addSetting(
      new BooleanSetting("Только при зажатии", "Работать только пока физически зажата соответствующая кнопка мыши.", false)
   );

   private final BooleanSetting targetOnlySetting = this.addSetting(
      new BooleanSetting("Только на цель", "Нажимать только когда прицел наведён на блок или существо.", false)
   );

   private long lastClickTime;
   private long lastPressTime;
   private boolean isCurrentlyPressed;
   private KeyBinding lastActiveKeyBinding;

   private final EventListener<PlayerTickEvent> tickListener = event -> this.updateAction();
   private final EventListener<WorldRenderEvent> renderListener = event -> this.updateAction();

   private TapeMouseModule() {
      this.speedSetting.withVisibility(() -> this.modeSetting.isSelected("Клик"));
   }

   public static TapeMouseModule getInstance() {
      if (instance == null) {
         instance = new TapeMouseModule();
      }
      return instance;
   }

   @Override
   protected void onEnable() {
      this.lastClickTime = 0L;
      this.lastPressTime = 0L;
      this.isCurrentlyPressed = false;
      this.lastActiveKeyBinding = null;
   }

   @Override
   protected void onDisable() {
      this.releaseKeys();
   }

   private void updateAction() {
      if (mc.player == null || mc.world == null || mc.options == null) {
         this.releaseKeys();
         return;
      }

      if (this.pauseInScreensSetting.isChecked() && mc.currentScreen != null) {
         this.releaseKeys();
         return;
      }

      if (this.onlyWhenHoldingSetting.isChecked()) {
         int mouseButtonCode = this.buttonSetting.isSelected("Левый") ? 0 : 1;
         if (!InputBinding.mouseButton(mouseButtonCode).isPressed(mc.getWindow().getHandle())) {
            this.releaseKeys();
            return;
         }
      }

      if (this.targetOnlySetting.isChecked()) {
         if (mc.crosshairTarget == null || mc.crosshairTarget.getType() == HitResult.Type.MISS) {
            this.releaseKeys();
            return;
         }
      }

      KeyBinding targetKey = this.buttonSetting.isSelected("Левый") ? mc.options.attackKey : mc.options.useKey;
      KeyBinding otherKey = this.buttonSetting.isSelected("Левый") ? mc.options.useKey : mc.options.attackKey;

      if (this.lastActiveKeyBinding != null && this.lastActiveKeyBinding != targetKey) {
         this.releaseKey(this.lastActiveKeyBinding);
      }
      this.lastActiveKeyBinding = targetKey;
      this.releaseKey(otherKey);

      long currentTime = System.currentTimeMillis();

      if (this.modeSetting.isSelected("Зажатие")) {
         this.pressKey(targetKey);
         this.isCurrentlyPressed = true;
         return;
      }

      // Mode: "Клик"
      long speed = Math.max(5L, Math.round(this.speedSetting.getValue()));
      long holdDuration = Math.max(5L, Math.min(25L, speed / 2L));

      if (this.isCurrentlyPressed) {
         if (currentTime - this.lastPressTime >= holdDuration) {
            this.releaseKey(targetKey);
            this.isCurrentlyPressed = false;
         }
      } else {
         if (currentTime - this.lastClickTime >= speed) {
            this.pressKey(targetKey);
            this.isCurrentlyPressed = true;
            this.lastClickTime = currentTime;
            this.lastPressTime = currentTime;
         }
      }
   }

   private void pressKey(KeyBinding keyBinding) {
      if (keyBinding == null) {
         return;
      }
      InputUtil.Key key = ((KeyBindingMixin) keyBinding).getKey();
      if (key != null) {
         KeyBinding.setKeyPressed(key, true);
         KeyBinding.onKeyPressed(key);
      }
      keyBinding.setPressed(true);
   }

   private void releaseKey(KeyBinding keyBinding) {
      if (keyBinding == null) {
         return;
      }
      InputUtil.Key key = ((KeyBindingMixin) keyBinding).getKey();
      if (key != null) {
         KeyBinding.setKeyPressed(key, false);
      }
      keyBinding.setPressed(false);
   }

   private void releaseKeys() {
      if (mc.options != null) {
         this.releaseKey(mc.options.attackKey);
         this.releaseKey(mc.options.useKey);
      }
      this.isCurrentlyPressed = false;
      this.lastClickTime = 0L;
      this.lastPressTime = 0L;
   }
}

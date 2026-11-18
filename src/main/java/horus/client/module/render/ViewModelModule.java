package horus.client.module.render;

import horus.client.module.combat.AttackAuraModule;
import horus.client.setting.BooleanSetting;
import horus.client.event.EventListener;
import horus.client.event.HeldItemRenderEvent;
import horus.client.setting.LegacyNumberSetting;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.SectionSetting;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.util.Arm;

@ModuleInfo(name = "ViewModel", description = "Позиция рук в кадре и поведение вьюмодели.", category = ModuleCategory.RENDER)
public final class ViewModelModule extends Module {
   private static ViewModelModule viewModelModule;
   public LegacyNumberSetting rightX;
   public LegacyNumberSetting rightY;
   public LegacyNumberSetting rightZ;
   public LegacyNumberSetting leftX;
   public LegacyNumberSetting leftY;
   public LegacyNumberSetting leftZ;
   public BooleanSetting removeUseAnimation;
   public BooleanSetting freezeHands;
   public BooleanSetting onlyWithKillaura;
   public BooleanSetting handWave;
   public LegacyNumberSetting handWaveAmount;
   public LegacyNumberSetting handWaveSpeed;
   private final EventListener<HeldItemRenderEvent> heldItemRenderListener = heldItemRenderEvent -> {
      if (this.checkState() && !heldItemRenderEvent.getItemStack().isEmpty() && !heldItemRenderEvent.getItemStack().contains(DataComponentTypes.MAP_ID)) {
         this.renderElement(heldItemRenderEvent.getMatrixStack(), heldItemRenderEvent.getArm());
      }

      if (this.isEnabled() && this.handWave != null && this.handWave.isChecked()) {
         this.applyHandWave(heldItemRenderEvent.getMatrixStack(), heldItemRenderEvent.getArm());
      }
   };

   private ViewModelModule() {
      this.addSetting(new SectionSetting("Правая рука"));
      this.rightX = this.addSetting(new LegacyNumberSetting("Правая X", "Смещение правой руки по X.", 0.0, -1.0, 1.0, 0.05));
      this.rightY = this.addSetting(new LegacyNumberSetting("Правая Y", "Смещение правой руки по Y.", 0.0, -1.0, 1.0, 0.05));
      this.rightZ = this.addSetting(new LegacyNumberSetting("Правая Z", "Смещение правой руки по Z.", 0.0, -1.0, 1.0, 0.05));
      this.addSetting(new SectionSetting("Левая рука"));
      this.leftX = this.addSetting(new LegacyNumberSetting("Левая X", "Смещение левой руки по X.", 0.0, -1.0, 1.0, 0.05));
      this.leftY = this.addSetting(new LegacyNumberSetting("Левая Y", "Смещение левой руки по Y.", 0.0, -1.0, 1.0, 0.05));
      this.leftZ = this.addSetting(new LegacyNumberSetting("Левая Z", "Смещение левой руки по Z.", 0.0, -1.0, 1.0, 0.05));
      this.removeUseAnimation = this.addSetting(new BooleanSetting("Убрать анимацию юза", "Убирает тряску предмета при еде и питье.", false));
      this.freezeHands = this.addSetting(new BooleanSetting("Не двигать руки", "Отключает отставание рук при повороте камеры.", false));
      this.onlyWithKillaura = this.addSetting(new BooleanSetting("Только с киллаурой", "Смещения рук работают только при включённой AttackAura.", false));
      this.addSetting(new SectionSetting("Анимация рук"));
      this.handWave = this.addSetting(new BooleanSetting("Анимация рук", "Руки плавно парят и покачиваются, как в визуалах ChaosWorld.", false));
      this.handWaveAmount = this.addSetting(new LegacyNumberSetting("Сила волн", "Насколько сильно машут руки.", 0.5, 0.0, 1.0, 0.05));
      this.handWaveAmount.withVisibility(() -> this.handWave.isChecked());
      this.handWaveSpeed = this.addSetting(new LegacyNumberSetting("Скорость волн", "Темп покачивания рук.", 1.0, 0.25, 3.0, 0.05));
      this.handWaveSpeed.withVisibility(() -> this.handWave.isChecked());
   }

   public boolean checkState() {
      return !this.onlyWithKillaura.isChecked() || AttackAuraModule.getInstance().isEnabled();
   }

   public void renderElement(MatrixStack matrixStack, Arm arm) {
      if (arm == Arm.RIGHT) {
         matrixStack.translate(this.rightX.getValue(), this.rightY.getValue(), this.rightZ.getValue());
      } else {
         matrixStack.translate(-this.leftX.getValue(), this.leftY.getValue(), this.leftZ.getValue());
      }
   }

   // Живая анимация рук: руки парят, качаются и слегка наклоняются в противофазе
   private void applyHandWave(MatrixStack matrixStack, Arm arm) {
      double t = System.currentTimeMillis() / 1000.0 * this.handWaveSpeed.getValue();
      float amp = (float) this.handWaveAmount.getValue();
      float phase = arm == Arm.RIGHT ? 0.0F : 1.9F;
      float swayX = (float) Math.sin(t * 1.1 + phase) * 0.045F * amp;
      float swayY = (float) Math.sin(t * 0.85 + phase * 2.0) * 0.05F * amp;
      float swayZ = (float) Math.sin(t * 0.65 + phase) * 0.03F * amp;
      float roll = (float) Math.sin(t * 0.9 + phase) * 7.0F * amp;
      float tilt = (float) Math.sin(t * 0.7 + phase + 0.6F) * 5.5F * amp;
      matrixStack.translate(swayX, swayY, swayZ);
      matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));
      matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(tilt));
   }

   public static ViewModelModule getInstance() {
      if (viewModelModule == null) {
         viewModelModule = new ViewModelModule();
      }

      return viewModelModule;
   }

   public boolean checkState2() {
      return this.isEnabled() && this.removeUseAnimation.isChecked();
   }

   public boolean checkState3() {
      return this.isEnabled() && this.freezeHands.isChecked();
   }
}

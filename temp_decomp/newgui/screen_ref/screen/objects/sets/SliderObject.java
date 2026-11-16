package cloud.client.ui.screen.objects.sets;

import com.mojang.blaze3d.matrix.MatrixStack;
import cloud.client.modules.api.imp.SliderSetting;
import cloud.client.util.math.MathUtil;
import cloud.client.util.render.ColorUtil;
import cloud.client.util.render.RenderUtil;
import cloud.client.util.render.animation.AnimationMath;
import cloud.client.ui.screen.objects.ModuleObject;
import cloud.client.ui.screen.objects.Object;
import cloud.client.util.font.Fonts;

public class SliderObject extends Object {

    public ModuleObject object;
    public SliderSetting set;
    public boolean sliding;

    public float animatedVal;

    public SliderObject(ModuleObject object, SliderSetting set) {
        this.object = object;
        this.set = set;
        setting = set;
    }

    @Override
    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        super.draw(stack, mouseX, mouseY);
        y -= 1;
        if (sliding) {
            float value = (float) ((mouseX - x) / (width - 8) * (set.getMax() - set.getMin()) + set.getMin());
            value = (float) MathUtil.round(value, set.getIncrement());
            set.setValue(value);
        }
        float sliderWidth = ((set.getValue().floatValue() - set.getMin()) / (set.getMax() - set.getMin()) + 0.8F) * (width - 90);
        animatedVal = AnimationMath.fast(animatedVal, sliderWidth, 15);
        RenderUtil.Render2D.drawRoundedRect(x + 65  , y + height / 2f + 2, width - 85, 2, 1, ColorUtil.rgba(42,42,54,255));
        RenderUtil.Render2D.drawRoundedRect(x + 65, y + height / 2f + 2, animatedVal - 55, 2, 1, ColorUtil.rgba(87,90,198, 255));


        RenderUtil.Render2D.drawRoundCircle(x + 10 + animatedVal, y + height / 2f + 2.5f, 5.5f, ColorUtil.rgba(244,244,247, 255));

        Fonts.sfmedium[12].drawString(stack, set.getName(), x + 10, y + height / 2f + 1.5f, ColorUtil.rgba(161, 164, 177, 255));
        Fonts.sfmedium[12].drawString(stack, String.valueOf(set.getValue().floatValue()), x + width + 5 - Fonts.msLight[12].getWidth(String.valueOf(set.getValue().floatValue())), y + height / 2f + 1.5, ColorUtil.rgba(161, 164, 177, 255));

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(mouseX,mouseY)) {
            sliding = true;
        }
    }

    @Override
    public void exit() {
        super.exit();
        sliding = false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        sliding = false;
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }
}

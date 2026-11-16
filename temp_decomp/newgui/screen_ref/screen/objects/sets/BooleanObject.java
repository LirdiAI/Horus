package cloud.client.ui.screen.objects.sets;

import com.mojang.blaze3d.matrix.MatrixStack;
import cloud.client.modules.api.imp.BooleanOption;
import cloud.client.util.render.ColorUtil;
import cloud.client.util.render.RenderUtil;
import cloud.client.util.render.animation.AnimationMath;
import cloud.client.ui.screen.objects.ModuleObject;
import cloud.client.ui.screen.objects.Object;
import cloud.client.util.font.Fonts;

public class BooleanObject extends Object {

    public ModuleObject object;
    public BooleanOption set;
    public float enabledAnimation;

    public BooleanObject(ModuleObject object, BooleanOption set) {
        this.object = object;
        this.set = set;
        setting = set;
    }

    @Override
    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        super.draw(stack, mouseX, mouseY);
        y -= 1;
        double max = !set.get() ? 0 : 4f;
        this.enabledAnimation = AnimationMath.fast(enabledAnimation, (float) max, 10);

        Fonts.sfmedium[13].drawString(stack, set.getName(), x + 10, y + height / 2f - Fonts.msLight[13].getFontHeight() / 2f + 2, ColorUtil.rgba(161, 166, 179, 255));
        int color = ColorUtil.interpolateColor(ColorUtil.rgba(148,148,152, 255), ColorUtil.rgba(255, 255, 255, 255), enabledAnimation / 6.5f);
        int color2 = ColorUtil.interpolateColor(ColorUtil.rgba(42,42,54, 255), ColorUtil.rgba(87,90,198, 255), enabledAnimation / 6.5f);

        RenderUtil.Render2D.drawRoundedRect(x + width - 11f, y + 4.5f,  13.5f, 9, 4, color2);
        RenderUtil.Render2D.drawRoundCircle(x + width - 6f + enabledAnimation,  y + 9f, 7, color);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (isHovered(mouseX, mouseY)) {
                set.toggle();
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }


}

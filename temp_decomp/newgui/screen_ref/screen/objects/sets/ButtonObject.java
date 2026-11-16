package cloud.client.ui.screen.objects.sets;

import com.mojang.blaze3d.matrix.MatrixStack;
import cloud.client.modules.api.imp.ButtonSetting;
import cloud.client.util.render.ColorUtil;
import cloud.client.util.render.RenderUtil;
import cloud.client.ui.screen.objects.ModuleObject;
import cloud.client.ui.screen.objects.Object;
import cloud.client.util.font.Fonts;

public class ButtonObject extends Object {

    public ButtonSetting set;
    public ModuleObject object;




    public ButtonObject(ModuleObject object, ButtonSetting set) {
        this.object = object;
        this.set = set;
        setting = set;

    }
    @Override
    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        super.draw(stack, mouseX, mouseY);
        Fonts.msLight[13].drawString(stack, set.getName(), x + 10, y + height / 2f - Fonts.msLight[13].getFontHeight() / 2f + 2, ColorUtil.rgba(161, 166, 179,255));

        float wwidth = Math.max(10, Fonts.msLight[13].getWidth("Открыть") + 4);
        RenderUtil.Render2D.drawRoundedRect(x + width - wwidth - 10,y + 2, wwidth,10, 2,ColorUtil.rgba(20, 21, 24, 255));

        Fonts.msLight[13].drawCenteredString(stack, "Открыть", x + width - wwidth - 10  + wwidth / 2f, y + height / 2f - Fonts.msLight[13].getFontHeight() / 2f + 2, ColorUtil.rgba(255, 255, 255,255));

    }


    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(mouseX,mouseY)) {
            float wwidth = Math.max(10, Fonts.msLight[13].getWidth("Открыть") + 4);
            if (RenderUtil.isInRegion(mouseX,mouseY,x + width - wwidth - 10,y + 2, wwidth,10)) {
                set.getRun().run();
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

package cloud.client.ui.screen.objects.sets;

import com.mojang.blaze3d.matrix.MatrixStack;
import cloud.client.modules.api.imp.ModeSetting;
import cloud.client.util.render.ColorUtil;
import cloud.client.util.render.RenderUtil;
import cloud.client.ui.screen.objects.ModuleObject;
import cloud.client.ui.screen.objects.Object;
import cloud.client.util.font.Fonts;
import org.joml.Vector4i;

import java.awt.*;

public class ModeObject extends Object {

    public ModeSetting set;
    public ModuleObject object;

    public ModeObject(ModuleObject object, ModeSetting set) {
        this.object = object;
        this.set = set;
        setting = set;
    }

    @Override
    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        super.draw(stack, mouseX, mouseY);
        int offset = 0;
        float offsetY = 0;
        int lines = 1;
        float size = 0;
        for (String mode : set.modes) {

            float preOffset = size + Fonts.msSemiBold[11].getWidth(mode) + 3;
            if (preOffset > width - 20) {
                break;
            }
            size += Fonts.msSemiBold[11].getWidth(mode) + 3;
        }

        for (String mode : set.modes) {
            float preOffset = offset + Fonts.msSemiBold[11].getWidth(mode) + 3;
            if (preOffset > size) {
                lines++;
                offset = 0;
            }
            offset += Fonts.msSemiBold[11].getWidth(mode) + 3;
        }

        height += 8;
        Fonts.sfmedium[12].drawString(stack, set.getName(), x + 10, y + height / 2f - 8, ColorUtil.rgba(150, 150, 150, 255));

        RenderUtil.Render2D.drawRoundOutline(x + 10, y + 9, size + 7, 11 * lines, 2,0f, ColorUtil.rgba(15, 15, 15, 0), new Vector4i(new Color(41,42,55, 255).getRGB()));
        height += 11 * (lines - 1);
        offset = 0;
        offsetY = 0;
        int i = 0;
        for (String mode : set.modes) {

            float preOff = offset + Fonts.msSemiBold[11].getWidth(mode) + 3;
            if (preOff > size) {
                offset = 0;
                offsetY += 11;
            }
            if (set.getIndex() == i) {
                int finalOffset = offset;
                float finalOffsetY = offsetY;
                    Fonts.gilroy[11].drawString(stack, mode, x + 15 + finalOffset, y + 14f + finalOffsetY, ColorUtil.rgba(119, 121, 134, 255));
                Fonts.gilroy[11].drawString(stack, mode, x + 15 + offset, y + 14f + offsetY, ColorUtil.rgba(255, 255, 255, 255));
            } else
                Fonts.gilroy[11].drawString(stack, mode, x + 15 + offset, y + 14f + offsetY, ColorUtil.rgba(26, 30, 41, 255));
            offset += Fonts.gilroy[11].getWidth(mode) + 3;
            i++;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float offset = 0;
        float offsetY = 0;
        int i = 0;
        float size = 0;
        for (String mode : set.modes) {

            float preOffset = size + Fonts.msSemiBold[11].getWidth(mode) + 3;
            if (preOffset > width - 20) {
                break;
            }
            size += Fonts.msSemiBold[11].getWidth(mode) + 3;
        }

        for (String mode : set.modes) {
            float preOff = offset + Fonts.msSemiBold[11].getWidth(mode) + 3;
            if (preOff > size) {
                offset = 0;
                offsetY += 11;
            }
            if (RenderUtil.isInRegion(mouseX, mouseY, x + 15 + offset, y + 12f + offsetY, Fonts.msSemiBold[11].getWidth(mode), Fonts.msSemiBold[11].getFontHeight() / 2f + 3)) {
                set.setIndex(i);
            }

            offset += Fonts.msSemiBold[11].getWidth(mode) + 3;
            i++;
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

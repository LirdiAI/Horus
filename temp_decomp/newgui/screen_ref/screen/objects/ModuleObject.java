package cloud.client.ui.screen.objects;

import cloud.client.modules.api.imp.*;
import cloud.client.ui.screen.objects.sets.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import cloud.client.util.managment.Managment;
import cloud.client.modules.api.Setting;
import cloud.client.modules.api.imp.*;
import cloud.client.ui.screen.binds.BindWindow;
import cloud.client.ui.screen.objects.sets.*;
import cloud.client.util.font.Fonts;
import cloud.client.util.render.ColorUtil;
import cloud.client.util.render.RenderUtil;
import cloud.client.util.render.animation.AnimationMath;
import cloud.client.modules.Function;
import org.joml.Vector4i;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class ModuleObject extends Object {

    public Function function;
    public ArrayList<Object> object = new ArrayList<>();
    public float animation;
    public BindWindow bindWindow;
    public BooleanOption set;

    @Override
    public void exit() {
        super.exit();
        for (Object object1 : object) {
            object1.exit();
        }
    }
    public ModuleObject(Function function) {
        this.function = function;
        for (Setting setting : function.settingList) {
            if (setting instanceof BooleanOption option) {
                object.add(new BooleanObject(this, option));
            }
            if (setting instanceof SliderSetting option) {
                object.add(new SliderObject(this, option));
            }
            if (setting instanceof ModeSetting option) {
                object.add(new ModeObject(this, option));
            }
            if (setting instanceof MultiBoxSetting option) {
                object.add(new MultiObject(this, option));
            }
            if (setting instanceof BindSetting option) {
                object.add(new BindObject(this, option));
            }
            if (setting instanceof ButtonSetting option) {
                object.add(new ButtonObject(this, option));
            }
        }
        bindWindow = new BindWindow(this);
        bindWindow.x = 10 + ThreadLocalRandom.current().nextFloat(0, 200);
        bindWindow.y = 10 + ThreadLocalRandom.current().nextFloat(0, 200);
        bindWindow.width = 178 / 2f;
        bindWindow.height = 73 / 2f;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        for (Object object1 : object) {
            object1.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }

    @Override
    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        super.draw(stack, mouseX, mouseY);
        animation = AnimationMath.fast(animation, function.state ? 4 : 0, 16);
        RenderUtil.Render2D.drawRoundOutline(x, y, width, height, 3, 0f, ColorUtil.rgba(15, 15, 15, 0), new Vector4i(new Color(41,42,55, 255).getRGB()));
        //ce
        Fonts.sfmedium[13].drawString(stack, "Enabled", x + 10, y + 26, ColorUtil.rgba(170, 170, 170, 255));
        RenderUtil.Render2D.drawRoundedRect(x + width - 21f, y + 22f, 13.5f, 9, 4, function.state ? new Color(87,90,198).getRGB() : new Color(42,42,54).getRGB());
        RenderUtil.Render2D.drawRoundCircle(x + width - 16f + animation, y + 26.5f, 7, function.state ? new Color(255, 255, 255).getRGB() : new Color(148,148,152).getRGB());

        Fonts.sfmedium[14].drawString(stack, function.name, x + 10, y + 13, -1);
        if (!function.settingList.isEmpty()) {
        }
        drawObjects(stack, mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        Managment.CLICK_GUI.searching = false;
        for (Object object1 : object) {
            object1.mouseClicked(mouseX, mouseY, mouseButton);
        }
            if (RenderUtil.isInRegion(mouseX, mouseY,x + width - 21f, y + 23, 13.5f, 7)) {
                if (mouseButton == 0)
                    function.toggle();
            if (mouseButton == 2)
                bindWindow.openAnimation = !bindWindow.openAnimation;
        }
    }

    public void drawObjects(MatrixStack stack, int mouseX, int mouseY) {
        float offset = 2;
        for (Object object : object) {
            if (object.setting.visible()) {
                object.x = x;
                object.y = y + 30 + offset;
                object.width = 163;
                object.height = 16;
                object.draw(stack, mouseX, mouseY);
                offset += object.height;
            }
        }
    }
}

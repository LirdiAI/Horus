package cloud.client.ui.screen;

import cloud.client.util.render.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import cloud.client.Client;
import cloud.client.util.managment.Managment;
import cloud.client.modules.Type;
import cloud.client.ui.screen.configs.ConfigDrawing;
import cloud.client.ui.screen.objects.sets.BindObject;
import cloud.client.ui.screen.theme.ThemeDrawing;
import cloud.client.util.utils.IMinecraft;
import cloud.client.util.font.Fonts;
import cloud.client.util.render.*;
import cloud.client.util.render.animation.AnimationMath;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import org.joml.Vector4i;
import org.lwjgl.glfw.GLFW;
import cloud.client.modules.Function;
import cloud.client.ui.screen.objects.ModuleObject;
import cloud.client.ui.screen.objects.Object;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClickUI extends Screen {
    private Vector2f position = new Vector2f(0, 0);
    public static Vector2f size = new Vector2f(450, 260);
    public static int dark = new Color(18, 19, 25).getRGB();
    public static int black = new Color(0, 0, 0).getRGB();
    public static int light = new Color(150, 150, 150).getRGB();
    public static int sett = new Color(69,72,129).getRGB();
    public static int newcol2 = new Color(90, 90, 90).getRGB();
    public static int white = new Color(36,35,43).getRGB();
    private Type currentCategory;
    private ThemeDrawing themeDrawing = new ThemeDrawing();
    private ConfigDrawing configDrawing = new ConfigDrawing();
    private ArrayList<ModuleObject> objects = new ArrayList<>();
    float len = 0;

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrolling += (float) (delta * 30);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    public ClickUI(ITextComponent titleIn) {
        super(titleIn);
        scrolling = 0;
        for (Function function : Managment.FUNCTION_MANAGER.getFunctions()) {
            objects.add(new ModuleObject(function));
        }

    }

    @Override
    protected void init() {
        size = new Vector2f(480, 270);
        position = new Vector2f(IMinecraft.mc.getMainWindow().scaledWidth() / 2f - size.x / 2f, IMinecraft.mc.getMainWindow().scaledHeight() / 2f - size.y / 2f);
        super.init();
    }

    public static float scrolling;
    public static float scrollingOut;

    public boolean searching;
    private String searchText = "";

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        if (Managment.FUNCTION_MANAGER.clickGui.blur.get()) {
            GaussianBlur.startBlur();
            RenderUtil.Render2D.drawRect(0, 0, width, height, -1);
            GaussianBlur.endBlur(Managment.FUNCTION_MANAGER.clickGui.blurVal.getValue().floatValue(), 1);
        }
        float bar = 108;
        IMinecraft.mc.gameRenderer.setupOverlayRendering(2);
        RenderUtil.Render2D.drawRoundedCorner(position.x + bar, position.y, size.x - bar, size.y, new Vector4f(0, 0, 5, 5), ColorUtil.rgba(24, 25, 33, 255));
        RenderUtil.Render2D.drawRect(position.x + 109, position.y + 25, 371, 0.5f, ColorUtil.rgba(41,42,55, 255));

        RenderUtil.Render2D.drawRoundedCorner(position.x, position.y, bar, size.y, new Vector4f(5, 5, 0, 0), ColorUtil.rgba(24, 25, 33, 255));
        RenderUtil.Render2D.drawRect(position.x + bar, position.y, 0.5f, size.y, ColorUtil.rgba(41,42,55, 255));

        Fonts.sfbold[21].drawString(matrixStack, Client.client, position.x + 46, position.y + 25.5f, ColorUtil.rgba(255, 255, 255, 255));
        Fonts.sfmedium[14].drawString(matrixStack, "build version: " + Client.version + Client.build, position.x + 115, position.y + 11, ColorUtil.rgba(100, 100, 100,200));
        RenderUtil.Render2D.drawImage(new ResourceLocation("cheat/images/cloud.png"),position.x + 23, position.y + 18, 20, 20,  -1);

        RenderUtil.Render2D.drawRoundedCorner(position.x + 10, position.y + 240f, (float) bar - 18, 20, 3, ColorUtil.rgba(29,31,39, 255));
        Fonts.sfbold[12].drawString(matrixStack, Client.owner, position.x + 30, position.y + size.y - 25, ColorUtil.rgba(255, 255, 255, 255));
        Fonts.sfmedium[11].drawString(matrixStack, "Till: 01.01.2057", position.x + 30, position.y + size.y - 17, ColorUtil.rgba(255, 255, 255,120));
        RenderUtil.Render2D.drawImage(new ResourceLocation("cheat/images/ava.png"),position.x + 12, position.y + size.y - 28,  16, 16,  -1);
        RenderUtil.Render2D.drawRoundOutline(position.x + 390, position.y + 5,80, 15, 2, 0f, ColorUtil.rgba(15, 15, 15, 0), new Vector4i(new Color(41,42,55, 255).getRGB()));
        RenderUtil.Render2D.drawRoundCircle(position.x + 25, position.y + size.y - 15,  4, ColorUtil.rgba(50, 150, 50,255));

        SmartScissor.push();
        SmartScissor.setFromComponentCoordinates(position.x + 390, position.y + size.y - 270,  bar - 15, 17);
        if (!searching && searchText.isEmpty()) {
            Fonts.gilroy[14].drawString(matrixStack, "Search", position.x + 403, position.y + size.y - 259, ColorUtil.rgba(100, 100, 100, 200));
            Fonts.wildfont[14].drawString(matrixStack, "a",position.x + 395, position.y + size.y - 258.5, ColorUtil.rgba(100, 100, 100, 200));
        } else {
            Fonts.gilroy[14].drawString(matrixStack, searchText + (searching ? (System.currentTimeMillis() % 1000 > 500 ? "_" : "") : ""), position.x + 395, position.y + size.y - 260, RenderUtil.reAlphaInt(light, 200));
        }
        SmartScissor.setFromComponentCoordinates(position.x + 10f, position.y + size.y - 50,  bar - 15, 17);
        SmartScissor.unset();
        SmartScissor.pop();
        for (Type type : Type.values()) {
            len = type.ordinal() * 20;

            if (type == Type.Theme) {
            }
            Fonts.iconswex[20].drawString(matrixStack, type.image, position.x + 15f, position.y + 60  + len, type == currentCategory ? new Color(93,96,124).getRGB() : new Color(255, 255, 255,255).getRGB());
            Fonts.sfbold[14].drawString(matrixStack, type.name(), position.x + 10f + 16, position.y + 60 + len, type == currentCategory ? new Color(93,96,124).getRGB() : new Color(255, 255, 255,255).getRGB());

            type.anim = AnimationMath.fast((float) type.anim, type == currentCategory ? 1 : 0, 5);
            if (type.anim > 0.001) {
                RenderUtil.Render2D.drawRoundOutline(position.x + 5, position.y + 53f + len, (float) bar - 20, 18, 3, 0f, ColorUtil.rgba(15, 15, 15, 0), new Vector4i(RenderUtil.reAlphaInt(new Color(43,46,74).getRGB(), (int) (150 * type.anim))));
                Fonts.wildfont[14].drawString(matrixStack, "p", position.x + 8, position.y + 61.5f + len, RenderUtil.reAlphaInt(new Color(93,96,124).getRGB(), (int) (150 * type.anim)));
            }
        }
        if (currentCategory == Type.Theme) {
            themeDrawing.draw(matrixStack, mouseX, mouseY, position.x + 100, position.y, size.x - bar, size.y);
            BloomHelper.draw(25, 1.5f, false);
        }

        if (currentCategory != Type.Theme)
            drawObjects(matrixStack, mouseX, mouseY);
        if (currentCategory != Type.Theme) {
            StencilUtil.initStencilToWrite();
            RenderUtil.Render2D.drawRoundedCorner(position.x, position.y, size.x, size.y, new Vector4f(8.5f, 8.5f, 8.5f, 8.5f), -1);
            StencilUtil.readStencilBuffer(1);
            StencilUtil.uninitStencilBuffer();
        }



        scrollingOut = AnimationMath.fast(scrollingOut, scrolling, 20);

        StencilUtil.initStencilToWrite();
        RenderUtil.Render2D.drawRoundedCorner(position.x, position.y, size.x, size.y, new Vector4f(8.5f, 8.5f, 8.5f, 8.5f), -1);
        StencilUtil.readStencilBuffer(0);
        StencilUtil.uninitStencilBuffer();

        for (Object object : objects) {
            if (object instanceof ModuleObject m) {
                m.bindWindow.render(matrixStack, mouseX, mouseY);
                for (Object object1 : m.object) {
                    if (object1 instanceof BindObject no) {
                        no.bindWindow.render(matrixStack, mouseX, mouseY);
                    }
                }
            }
        }
        IMinecraft.mc.gameRenderer.setupOverlayRendering();

    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
            configDrawing.charTyped(codePoint);
        if (configDrawing.searching && configDrawing.search.length() < 23) {
            configDrawing.search += codePoint;
        }
        if (searching && searchText.length() < 13) {
            searchText += codePoint;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (ModuleObject m : objects) {
            if (m.function.category == currentCategory)
                m.keyTyped(keyCode, scanCode, modifiers);
        }
            configDrawing.keyTyped(keyCode);
        if (configDrawing.searching) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!configDrawing.search.isEmpty())
                    configDrawing.search = configDrawing.search.substring(0, configDrawing.search.length() - 1);
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                configDrawing.searching = false;
            }
        }
        if (searching) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchText.isEmpty())
                    searchText = searchText.substring(0, searchText.length() - 1);
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                searching = false;
            }
        }
        for (Object object : objects) {
            if (object instanceof ModuleObject m) {
                m.bindWindow.keyPress(keyCode);
                for (Object object1 : m.object) {
                    if (object1 instanceof BindObject no) {
                        no.bindWindow.keyPress(keyCode);

                        if (no.bindWindow.binding && keyCode == GLFW.GLFW_KEY_ESCAPE) {
                            return false;
                        }
                    }
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Vec2i fixed = ScaleMath.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();
        for (ModuleObject m : objects) {
            if (searching || !searchText.isEmpty()) {
                if (!searchText.isEmpty())
                    if (!m.function.name.toLowerCase().contains(searchText.toLowerCase())) continue;
                m.mouseReleased((int) mouseX, (int) mouseY, button);
            } else {
                if (m.function.category == currentCategory)
                    m.mouseReleased((int) mouseX, (int) mouseY, button);
            }
        }
        for (Object object : objects) {
            if (object instanceof ModuleObject m) {
                m.bindWindow.mouseUn();
                for (Object object1 : m.object) {
                    if (object1 instanceof BindObject no) {
                        no.bindWindow.mouseUn();
                    }
                }
            }

        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        super.onClose();
        searching = false;
        for (ModuleObject m : objects) {
            m.exit();
        }
    }

    public void drawObjects(MatrixStack stack, int mouseX, int mouseY) {
        Vec2i fixed = ScaleMath.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();
        List<ModuleObject> first = objects.stream()
                .filter(moduleObject -> (!searchText.isEmpty()) || moduleObject.function.category == currentCategory)
                .filter(moduleObject -> objects.indexOf(moduleObject) % 2 == 0)
                .toList();

        List<ModuleObject> second = objects.stream()
                .filter(moduleObject -> (!searchText.isEmpty()) || moduleObject.function.category == currentCategory)
                .filter(moduleObject -> objects.indexOf(moduleObject) % 2 != 0)
                .toList();


        RenderUtil.SmartScissor.push();
        RenderUtil.SmartScissor.setFromComponentCoordinates(position.x, position.y + 25, size.x, size.y - 25);
        float offset = scrollingOut;
        float sizePanel = 0;
        for (ModuleObject object : first) {
            if (!searchText.isEmpty())
                if (!object.function.name.toLowerCase().contains(searchText.toLowerCase())) continue;
            object.x = position.x + 115;
            object.y = position.y + 10 + offset + 28;
            object.width = 173;
            object.height = 38;
            for (Object object1 : object.object) {
                if (object1.setting.visible()) {
                    object.height += object1.height;
                }
            }
            object.height += 3;
            if (!(object.y - object.height - 50 > size.y))
                object.draw(stack, mouseX, mouseY);
            offset += object.height += 5;
            sizePanel += object.height += 5;
        }
        float sizePanel1 = 0;
        offset = scrollingOut;
        for (ModuleObject object : second) {
            if (!searchText.isEmpty())
                if (!object.function.name.toLowerCase().contains(searchText.toLowerCase())) continue;
            object.x = position.x + 298;
            object.y = position.y + 10 + offset + 28;
            object.width = 173;
            object.height = 38;
            for (Object object1 : object.object) {
                if (object1.setting.visible()) {
                    object.height += object1.height;
                }
            }
            object.height += 3;
            if (!(object.y - object.height - 50 > size.y))
                object.draw(stack, mouseX, mouseY);
            offset += object.height += 20;
            sizePanel1 += object.height += 5;
        }
        float max = Math.max(sizePanel, sizePanel1);
        if (max < size.y) {
            scrolling = 0;
        } else {
            scrolling = MathHelper.clamp(scrolling, -(max - size.y), 0);
        }
        BloomHelper.draw(6, 1.5f, false);
        RenderUtil.SmartScissor.unset();
        RenderUtil.SmartScissor.pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2i fixed = ScaleMath.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();
        if (currentCategory == Type.Theme) {
            themeDrawing.click((int) mouseX, (int) mouseY, button);
        }

        float bar = 100;

        float len = 0;
        for (Type type : Type.values()) {
            len = type.ordinal() * 20;
            if (type == Type.Theme) {
                len += 10;
            }
            if (RenderUtil.isInRegion(mouseX, mouseY, position.x, position.y + 53f + len, (float) bar, 18)) {
                currentCategory = type;
                searching = false;
            }
        }

        for (ModuleObject m : objects) {
            if (searching || !searchText.isEmpty()) {
                if (!searchText.isEmpty())
                    if (!m.function.name.toLowerCase().contains(searchText.toLowerCase())) continue;
                m.mouseClicked((int) mouseX, (int) mouseY, button);
            } else {
                if (m.function.category == currentCategory)
                    m.mouseClicked((int) mouseX, (int) mouseY, button);
            }

        }
        for (Object object : objects) {
            if (object instanceof ModuleObject m) {
                m.bindWindow.mouseClick((int) mouseX, (int) mouseY, button);
                for (Object object1 : m.object) {
                    if (object1 instanceof BindObject no) {
                        no.bindWindow.mouseClick((int) mouseX, (int) mouseY, button);
                    }
                }
            }
        }
        if (RenderUtil.isInRegion(mouseX, mouseY, position.x + 400, position.y + 5,70, 15)) {
            searching = !searching;
        }


        return super.mouseClicked(mouseX, mouseY, button);
    }
}

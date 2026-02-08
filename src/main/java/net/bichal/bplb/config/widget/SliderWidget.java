package net.bichal.bplb.config.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public abstract class SliderWidget extends AnimatedWidget {
    public double value;
    private boolean sliderFocused;
    private static final int TRACK_HEIGHT = 2;
    private static final int HANDLE_WIDTH = 6;
    private static final int HANDLE_HEIGHT = 16;

    public SliderWidget(int x, int y, int width, int height, Text text, double value) {
        super(x, y, width, height, text);
        this.value = value;
    }

    @Override
    protected MutableText getNarrationMessage() {
        return Text.translatable("gui.narrate.slider", this.getMessage());
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, this.getNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                builder.put(NarrationPart.USAGE, Text.translatable("narration.slider.usage.focused"));
            } else {
                builder.put(NarrationPart.USAGE, Text.translatable("narration.slider.usage.hovered"));
            }
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        updateHoverAnimation(mouseX, mouseY, 0.2f);
        int trackY = this.getY() + (this.height - TRACK_HEIGHT) / 2;
        int trackColor = getBorderColor();
        context.fill(this.getX() + 2, trackY, this.getX() + this.width - 2, trackY + TRACK_HEIGHT, trackColor);
        int filledWidth = (int) ((this.width - 4) * this.value);
        context.fill(this.getX() + 2, trackY, this.getX() + 2 + filledWidth, trackY + TRACK_HEIGHT, trackColor);
        int handleX = this.getX() + (int) ((this.width - HANDLE_WIDTH) * this.value);
        int handleY = this.getY() + (this.height - HANDLE_HEIGHT) / 2;
        int handleColor = getHandleColor();
        context.fill(handleX, handleY, handleX + HANDLE_WIDTH, handleY + HANDLE_HEIGHT, handleColor);
    }

    private int getHandleColor() {
        return 0xFF000000 | ((int) (0xC0 * borderBrightness) << 16 | (int) (0xC0 * borderBrightness) << 8) | (int) (0xC0 * borderBrightness);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.setValueFromMouse(mouseX);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.sliderFocused = false;
        } else {
            GuiNavigationType guiNavigationType = MinecraftClient.getInstance().getNavigationType();
            if (guiNavigationType == GuiNavigationType.MOUSE || guiNavigationType == GuiNavigationType.KEYBOARD_TAB) {
                this.sliderFocused = true;
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyCodes.isToggle(keyCode)) {
            this.sliderFocused = !this.sliderFocused;
            return true;
        } else {
            if (this.sliderFocused) {
                boolean bl = keyCode == GLFW.GLFW_KEY_LEFT;
                if (bl || keyCode == GLFW.GLFW_KEY_RIGHT) {
                    float f = bl ? -1.0F : 1.0F;
                    this.setValue(this.value + (double) (f / (float) (this.width - 8)));
                    return true;
                }
            }
            return false;
        }
    }

    private void setValueFromMouse(double mouseX) {
        this.setValue((mouseX - (double) (this.getX() + 4)) / (double) (this.width - 8));
    }

    private void setValue(double value) {
        double d = this.value;
        this.value = MathHelper.clamp(value, 0.0, 1.0);
        if (d != this.value) {
            this.applyValue();
        }
        this.updateMessage();
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        this.setValueFromMouse(mouseX);
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
    }
    public abstract void updateMessage();
    protected abstract void applyValue();
}

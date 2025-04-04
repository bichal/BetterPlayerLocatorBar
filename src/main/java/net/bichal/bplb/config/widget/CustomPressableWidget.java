// CustomPressableWidget.java
package net.bichal.bplb.config.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public abstract class CustomPressableWidget extends ClickableWidget {
    private long lastClickTime = 0;
    private float hoverProgress = 0f;
    private float clickProgress = 0f;
    private float borderBrightness = 0.3f;
    private float pulseScale = 1f;

    public CustomPressableWidget(int i, int j, int k, int l, Text text) {
        super(i, j, k, l, text);
    }

    public abstract void onPress();

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();

        updateAnimations(mouseX, mouseY);

        if (clickProgress > 0) {
            renderPulseEffect(context);
        }

        renderButtonBase(context);

        context.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int color = this.active ? 16777215 : 10526880;
        this.drawMessage(context, minecraftClient.textRenderer, color | MathHelper.ceil(this.alpha * 255.0F) << 24);
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void updateAnimations(int mouseX, int mouseY) {
        float baseBorderBrightness = 0.3f;
        float hoverBorderBrightness = 0.6f;
        float clickBorderBrightness = 0.9f;

        float targetHover = this.isMouseOver(mouseX, mouseY) ? 1f : 0f;
        hoverProgress = MathHelper.lerp(0.2f, hoverProgress, targetHover);

        long clickTime = System.currentTimeMillis() - lastClickTime;
        if (clickTime > 300) {
            clickProgress = MathHelper.lerp(0.15f, clickProgress, 0f);
        } else if (clickTime < 100) {
            clickProgress = MathHelper.lerp(0.3f, clickProgress, 1f);
        }

        float targetBorderBrightness = clickProgress > 0.1f ? clickBorderBrightness : (hoverProgress > 0.5f ? hoverBorderBrightness : baseBorderBrightness);
        borderBrightness = MathHelper.lerp(0.15f, borderBrightness, targetBorderBrightness);

        if (clickProgress > 0) {
            float pulsePhase = clickTime / 300f;
            pulseScale = 0.8f + (float) (Math.sin(pulsePhase * Math.PI * 2) * 0.2 * clickProgress);
        } else {
            pulseScale = 1f;
        }
    }

    private void renderPulseEffect(DrawContext context) {
        float pulseAlpha = clickProgress * 0.7f;
        float minAlpha = 0f;

        long clickTime = System.currentTimeMillis() - lastClickTime;
        float alphaProgress = Math.min(clickTime / 150f, 1f);
        float currentAlpha = minAlpha + (pulseAlpha - minAlpha) * (1f - Math.abs(alphaProgress));

        context.getMatrices().push();

        float centerX = this.getX() + this.width / 2f;
        float centerY = this.getY() + this.height / 2f;

        context.getMatrices().translate(centerX, centerY, 0);
        context.getMatrices().scale(pulseScale * 1.1f, pulseScale * 1.1f, 1f);
        context.getMatrices().translate(-centerX, -centerY, 0);

        int backgroundColor = ((int) (currentAlpha * 0x80) << 24) | 0x202020;
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, backgroundColor);

        int borderAlpha = (int) (currentAlpha * 0xFF);
        int borderColor = (borderAlpha << 24) | 0x808080;

        renderBorderButton(context, borderColor);

        context.getMatrices().pop();
    }

    private void renderButtonBase(DrawContext context) {
        int backgroundColor = 0x80000000 | ((int) (0x20 * borderBrightness) << 16) | ((int) (0x20 * borderBrightness) << 8 | (int) (0x20 * borderBrightness));
        int borderColor = 0xFF000000 | ((int) (0x80 * borderBrightness) << 16 | ((int) (0x80 * borderBrightness) << 8) | (int) (0x80 * borderBrightness));
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, backgroundColor);

        renderBorderButton(context, borderColor);

        int cornerColor = 0xFF000000 | ((int) (0xA0 * borderBrightness) << 16 | ((int) (0xA0 * borderBrightness) << 8) | (int) (0xA0 * borderBrightness));
        context.fill(this.getX(), this.getY(), this.getX() + 2, this.getY() + 2, cornerColor);
        context.fill(this.getX() + this.width - 2, this.getY(), this.getX() + this.width, this.getY() + 2, cornerColor);
        context.fill(this.getX(), this.getY() + this.height - 2, this.getX() + 2, this.getY() + this.height, cornerColor);
        context.fill(this.getX() + this.width - 2, this.getY() + this.height - 2, this.getX() + this.width, this.getY() + this.height, cornerColor);
    }

    private void renderBorderButton(DrawContext context, int borderColor) {
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, borderColor);
        context.fill(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, borderColor);

        context.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height, borderColor);
        context.fill(this.getX() + this.width - 1, this.getY(), this.getX() + this.width, this.getY() + this.height, borderColor);
    }

    public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
        int x = this.getX() + (this.width - textRenderer.getWidth(this.getMessage())) / 2;
        int y = this.getY() + (this.height - 8) / 2;
        context.drawText(textRenderer, this.getMessage(), x, y, color, true);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.lastClickTime = System.currentTimeMillis();
        this.onPress();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.active || !this.visible) {
            return false;
        } else if (KeyCodes.isToggle(keyCode)) {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            this.onClick(0, 0);
            return true;
        } else {
            return false;
        }
    }
}

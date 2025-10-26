package net.bichal.bplb.config.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.bichal.bplb.util.Constants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public abstract class PressableWidget extends AnimatedWidget {
    public PressableWidget(int i, int j, int k, int l, Text text) {
        super(i, j, k, l, text);
    }
    public abstract void onPress();

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        updateHoverAnimation(mouseX, mouseY, 0.2f);
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        renderButtonBase(context);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int color = this.active ? Constants.WHITE_COLOR : Constants.GRAY_COLOR;
        this.drawMessage(context, minecraftClient.textRenderer, color | MathHelper.ceil(this.alpha * 255.0F) << 24);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderButtonBase(DrawContext context) {
        int backgroundColor = this.active ? 0x80000000 | ((int) (0x20 * borderBrightness) << 16) | ((int) (0x20 * borderBrightness) << 8 | (int) (0x20 * borderBrightness)) : 0x803D3D3D | ((int) (0x80 * borderBrightness) << 16) | ((int) (0x80 * borderBrightness) << 8 | (int) (0x80 * borderBrightness));
        int borderColor = this.active ? Constants.BLACK_COLOR | ((int) (0x80 * borderBrightness) << 16 | ((int) (0x80 * borderBrightness) << 8) | (int) (0x80 * borderBrightness)) : 0x20000000 | ((int) (0x20 * borderBrightness) << 16 | ((int) (0x20 * borderBrightness) << 8) | (int) (0x20 * borderBrightness));
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, backgroundColor);
        renderBorderButton(context, borderColor);
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

package net.bichal.bplb.config.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class TextInputWidget extends TextFieldWidget {
    private Text placeholder;
    private final MinecraftClient client;
    private final boolean drawsBackground;

    public TextInputWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
        this.placeholder = text;
        this.drawsBackground = false;
        this.client = MinecraftClient.getInstance();
    }

    @Override
    public void setPlaceholder(Text placeholder) {
        this.placeholder = placeholder;
        super.setPlaceholder(placeholder);
    }

    @Override
    public boolean drawsBackground() {
        return this.drawsBackground;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.isVisible()) {
            int backgroundColor = this.isFocused() ? 0xA0282828 : 0x80101010;
            int borderColor = this.isFocused() ? 0xFF909090 : 0xFF505050;
            context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, backgroundColor);
            context.drawBorder(this.getX(), this.getY(), this.width, this.height, borderColor);
        }
        int k = this.getX() + 4;
        int l = this.getY() + (this.height - 8) / 2;
        int m = k;
        int j = l;

        if (!this.drawsBackground) {
            k = this.getX() + 4 * 2;
            l = this.getY() + (this.height - 8 * 2) / 2;
        }

        if (this.placeholder != null && this.getText().isEmpty() && !this.isFocused()) {
            context.drawTextWithShadow(this.client.textRenderer, this.placeholder, !this.drawsBackground ? m : k, !this.drawsBackground ? j : l, 0xFF808080);
        }

        super.renderWidget(context, mouseX, mouseY, delta);
    }
}

package net.bichal.bplb.config.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class TextInputWidget extends TextFieldWidget {
    public TextInputWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    @Override public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.isVisible()) {
            int backgroundColor = this.isFocused() ? 0xA0282828 : 0x80101010;
            int borderColor = this.isFocused() ? 0xFF909090 : 0xFF505050;
            context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, backgroundColor);
            context.drawBorder(this.getX(), this.getY(), this.width, this.height, borderColor);
        }
        super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        if (result) {
            this.setFocused(true);
        }
        return result;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
    }
}

package net.bichal.bplb.config.widget;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public abstract class AnimatedWidget extends ClickableWidget {
    protected float hoverProgress = 0f;
    protected float borderBrightness = 0.3f;

    public AnimatedWidget(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    protected void updateHoverAnimation(int mouseX, int mouseY, float hoverSpeed) {
        float targetHover = this.isMouseOver(mouseX, mouseY) ? 1f : 0f;
        hoverProgress = MathHelper.lerp(hoverSpeed, hoverProgress, targetHover);

        float targetBrightness = hoverProgress > 0.5f ? 0.6f : 0.3f;
        borderBrightness = MathHelper.lerp(0.15f, borderBrightness, targetBrightness);
    }

    protected int getBorderColor() {
        int value = (int) (0x80 * borderBrightness);
        return 0xFF000000 | (value << 16) | (value << 8) | value;
    }
}
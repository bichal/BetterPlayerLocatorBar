package net.bichal.bplb.gui.widget;

import net.bichal.bplb.gui.animation.Transition;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class CompactButton extends AnimatedWidget {
    private final Transition hoverTransition = Constants.createHoverTransition();
    private final PressAction onPress;
    private boolean hasTexture = false, showBackground = true;
    private Identifier texture1, texture2;
    private int textureX, textureY, u, v, texWidth, texHeight;

    public CompactButton(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message);
        this.onPress = onPress;
    }

    public CompactButton(Identifier texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, boolean showBackground, PressAction onPress) {
        this(texture, null, x, y, u, v, width, height, textureWidth, textureHeight, showBackground, onPress);
    }

    public CompactButton(Identifier texture1, Identifier texture2, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, boolean showBackground, PressAction onPress) {
        super(x, y, width, height, Text.empty());
        this.onPress = onPress;
        this.hasTexture = texture1 != null || texture2 != null;
        this.texture1 = texture1;
        this.texture2 = texture2;
        this.textureX = x;
        this.textureY = y;
        this.u = u;
        this.v = v;
        this.texWidth = textureWidth;
        this.texHeight = textureHeight;
        this.showBackground = showBackground;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = isMouseOver(mouseX, mouseY);
        hoverTransition.setTarget(hovered ? 1f : 0f);
        float hoverAlpha = hoverTransition.update();

        if (showBackground) {
            int bgColor = active ? 0xFF2A2A2A : 0xFF1A1A1A;
            int brightness = (int) (hoverAlpha * 30);
            bgColor = (bgColor & 0xFF000000) |
                    Math.min(255, ((bgColor >> 16) & 0xFF) + brightness) << 16 |
                    Math.min(255, ((bgColor >> 8) & 0xFF) + brightness) << 8 |
                    Math.min(255, (bgColor & 0xFF) + brightness);

            context.fill(getX(), getY(), getX() + width, getY() + height, bgColor);
            context.drawBorder(getX(), getY(), width, height, active ? 0xFF505050 : 0xFF303030);
        }

        int textColor = active ? 0xFFFFFF : 0x808080;
        MinecraftClient mc = MinecraftClient.getInstance();
        int textX = getX() + (width - mc.textRenderer.getWidth(getMessage())) / 2;
        int textY = getY() + (height - 8) / 2;
        int texX = getX() + textureX;
        int texY = getY() + textureY;
        if (hasTexture) {
            if (texture2 != null)
                context.drawTexture(active ? texture1 : texture2, texX, texY, u, v, width, height, texWidth, texHeight);
            else context.drawTexture(texture1, texX, texY, u, v, width, height, texWidth, texHeight);
        } else {
            context.drawText(mc.textRenderer, getMessage(), textX, textY, textColor, true);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (active) onPress.onPress(this);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    public interface PressAction {
        void onPress(CompactButton button);
    }
}
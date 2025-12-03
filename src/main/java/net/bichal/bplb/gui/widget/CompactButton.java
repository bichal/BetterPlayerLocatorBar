package net.bichal.bplb.gui.widget;

import net.bichal.bichalutils.client.render.RenderUtil;
import net.bichal.bichalutils.util.ModIdentifier;
import net.bichal.bplb.gui.animation.Transition;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class CompactButton extends AnimatedWidget {
    private final Transition hoverTransition = Constants.createHoverTransition();
    public final PressAction onPress;
    private final ButtonStyle style;
    private final boolean showBackground;

    private CompactButton(int x, int y, int width, int height, Text message, ButtonStyle style, boolean showBackground, PressAction onPress) {
        super(x, y, width, height, message);
        this.style = style;
        this.onPress = onPress;
        this.showBackground = showBackground;
    }

    public static CompactButton text(int x, int y, int width, int height, Text message, boolean showBackground, PressAction onPress) {
        return new CompactButton(x, y, width, height, message, new TextStyle(), showBackground, onPress);
    }

    public static CompactButton text(int x, int y, int width, int height, Text message, PressAction onPress) {
        return new CompactButton(x, y, width, height, message, new TextStyle(), true, onPress);
    }

    public static CompactButton texture(int x, int y, int size, int atlasU, int atlasV, boolean showBackground, PressAction onPress) {
        return new CompactButton(x, y, size, size, Text.empty(), new TextureStyle(atlasU, atlasV), showBackground, onPress);
    }

    public static CompactButton texture(int x, int y, int size, int u, int v, PressAction onPress) {
        return new CompactButton(x, y, size, size, Text.empty(), new TextureStyle(u, v), true, onPress);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        hoverTransition.setTarget(isMouseOver(mouseX, mouseY) && active ? 1f : 0f);
        float hoverAlpha = hoverTransition.update();
        style.render(context, getX(), getY(), width, height, hoverAlpha, active, showBackground, getMessage());
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

    private static void renderBackground(DrawContext context, int x, int y, int width, int height, float hoverAlpha, boolean active, boolean showBackground) {
        if (!showBackground) return;
        int bgColor;
        if (active) {
            bgColor = 0xFF2A2A2A;
            int brightness = (int) (hoverAlpha * 30);
            bgColor = (bgColor & 0xFF000000) | Math.min(255, ((bgColor >> 16) & 0xFF) + brightness) << 16 | Math.min(255, ((bgColor >> 8) & 0xFF) + brightness) << 8 | Math.min(255, (bgColor & 0xFF) + brightness);
        } else {
            bgColor = 0xFF151515;
        }

        context.fill(x, y, x + width, y + height, bgColor);
        context.drawBorder(x, y, width, height, active ? 0xFF505050 : 0xFF252525);
    }

    private interface ButtonStyle {
        void render(DrawContext context, int x, int y, int width, int height, float hoverAlpha, boolean active, boolean showBackground, Text text);
    }

    private static class TextStyle implements ButtonStyle {
        @Override
        public void render(DrawContext context, int x, int y, int width, int height, float hoverAlpha, boolean showBackground, boolean active, Text text) {
            renderBackground(context, x, y, width, height, hoverAlpha, showBackground, active);
            MinecraftClient mc = MinecraftClient.getInstance();
            int textColor = active ? 0xFFFFFF : 0x808080;
            int textX = x + (width - mc.textRenderer.getWidth(text)) / 2;
            int textY = y + (height - 8) / 2;
            context.drawText(mc.textRenderer, text, textX, textY, textColor, true);
        }
    }

    private record TextureStyle(int u, int v) implements ButtonStyle {
        private static final Identifier ATLAS = ModIdentifier.ofMod("textures/sprites/hud/atlas/buttons.png");

        @Override
        public void render(DrawContext context, int x, int y, int width, int height, float hoverAlpha, boolean active, boolean showBackground, Text text) {
            renderBackground(context, x, y, width, height, hoverAlpha, active, showBackground);
            float alpha = active ? 1.0f : 0.4f;
            RenderUtil.setShaderColorRGBA(0xFFFFFF, alpha);
            context.drawTexture(ATLAS, x, y, u, v, width, height, 200, 200);
            RenderUtil.setShaderColorRGBA(0xFFFFFF, 1.0f);
        }
    }
}

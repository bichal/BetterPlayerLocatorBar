package net.bichal.bplb.gui.widget;

import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

public class TooltipWidget {
    private static final long HOVER_DELAY_MS = 500;
    private final TextRenderer textRenderer;
    private Text currentTooltip;
    private long hoverStartTime;
    private boolean isVisible;
    private int x, y, width;

    public TooltipWidget(MinecraftClient client) {
        this.textRenderer = client.textRenderer;
    }

    public void setHoveredTooltip(Text tooltip, int x, int y, int width) {
        if (tooltip == null) {
            clearTooltip();
            return;
        }

        if (!tooltip.equals(currentTooltip)) {
            currentTooltip = tooltip;
            hoverStartTime = System.currentTimeMillis();
            isVisible = false;
            this.x = x;
            this.y = y;
            this.width = width;
        } else {
            long elapsed = System.currentTimeMillis() - hoverStartTime;
            if (elapsed >= HOVER_DELAY_MS) {
                isVisible = true;
            }
        }
    }

    public void clearTooltip() {
        currentTooltip = null;
        isVisible = false;
    }

    public void tick() {
        if (currentTooltip != null && !isVisible) {
            if (System.currentTimeMillis() - hoverStartTime >= HOVER_DELAY_MS) {
                isVisible = true;
            }
        }
    }

    public void render(DrawContext context) {
        if (!isVisible || currentTooltip == null) return;

        List<OrderedText> lines = textRenderer.wrapLines(currentTooltip, width - Constants.CONFIG_PADDING * 2);
        int tooltipHeight = lines.size() * (textRenderer.fontHeight + 2) + Constants.CONFIG_PADDING;

        int bgColor = 0xE0000000;
        int borderColor = 0xFF505050;

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 400);

        context.fill(x, y, x + width, y + tooltipHeight, bgColor);
        context.drawBorder(x, y, width, tooltipHeight, borderColor);

        int textY = y + Constants.CONFIG_PADDING / 2;
        for (OrderedText line : lines) {
            context.drawText(textRenderer, line, x + Constants.CONFIG_PADDING, textY, Constants.WHITE_COLOR, true);
            textY += textRenderer.fontHeight + 2;
        }

        context.getMatrices().pop();
    }
}

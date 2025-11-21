package net.bichal.bplb.gui.widget;

import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

public class TooltipWidget {
    private final TextRenderer textRenderer;
    private Text currentTooltip;
    private int x, y, width;
    private int maxHeight;

    public TooltipWidget(MinecraftClient client) {
        this.textRenderer = client.textRenderer;
    }

    public void setHoveredTooltip(Text tooltip, int x, int y, int width) {
        this.currentTooltip = tooltip;
        this.x = x;
        this.width = width;

        List<OrderedText> lines = textRenderer.wrapLines(tooltip, width - Constants.CONFIG_PADDING * 2);
        int calculatedHeight = lines.size() * (textRenderer.fontHeight + 2) + Constants.CONFIG_PADDING;

        MinecraftClient client = MinecraftClient.getInstance();
        int screenHeight = client.getWindow().getScaledHeight();

        if (y + calculatedHeight > screenHeight - 30) {
            this.y = Math.max(85, y - calculatedHeight);
        } else {
            this.y = y;
        }

        this.maxHeight = calculatedHeight;
    }

    public void clearTooltip() {
        currentTooltip = null;
    }

    public void render(DrawContext context) {
        if (currentTooltip == null) return;

        List<OrderedText> lines = textRenderer.wrapLines(currentTooltip, width - Constants.CONFIG_PADDING * 2);
        int tooltipHeight = Math.min(maxHeight, lines.size() * (textRenderer.fontHeight + 2) + Constants.CONFIG_PADDING);

        int bgColor = 0xE0000000;
        int borderColor = 0xFF505050;

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 400);

        context.fill(x, y, x + width, y + tooltipHeight, bgColor);
        context.drawBorder(x, y, width, tooltipHeight, borderColor);

        int textY = y + Constants.CONFIG_PADDING / 2;
        for (OrderedText line : lines) {
            if (textY + textRenderer.fontHeight > y + tooltipHeight) break;
            context.drawText(textRenderer, line, x + Constants.CONFIG_PADDING, textY, Constants.WHITE_COLOR, true);
            textY += textRenderer.fontHeight + 2;
        }

        context.getMatrices().pop();
    }
}

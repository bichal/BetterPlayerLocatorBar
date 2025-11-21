package net.bichal.bplb.gui.widget;

import net.bichal.bplb.gui.animation.Transition;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

public final class TooltipRenderer {
    private final Transition fadeTransition = Constants.createTransition();
    private Text currentTooltip;
    private int tooltipX, tooltipY, tooltipWidth, tooltipMaxHeight;
    private long showTime;
    private boolean isFooterTooltip;

    public void setTooltip(Text tooltip, int x, int y, int width, int maxHeight, boolean isFooterTooltip) {
        if (tooltip != currentTooltip) {
            currentTooltip = tooltip;
            tooltipX = x;
            tooltipY = y;
            tooltipWidth = width;
            tooltipMaxHeight = maxHeight;
            this.isFooterTooltip = isFooterTooltip;
            showTime = System.currentTimeMillis();
            fadeTransition.setTarget(0);
        }
    }

    public void clear() {
        currentTooltip = null;
        fadeTransition.setTarget(0);
    }

    public void render(DrawContext context, int screenHeight, int footerY) {
        if (currentTooltip == null) return;

        long elapsed = System.currentTimeMillis() - showTime;
        if (elapsed > Constants.TOOLTIP_FADE_DELAY_MS) {
            fadeTransition.setTarget(1f);
        }

        float alpha = fadeTransition.update();
        if (alpha < 0.01f) return;

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        List<OrderedText> lines = textRenderer.wrapLines(currentTooltip, tooltipWidth - 16);
        int contentHeight = lines.size() * (textRenderer.fontHeight + 2) + 8;
        int actualHeight = Math.min(contentHeight, tooltipMaxHeight);

        int finalY = tooltipY;
        if (!isFooterTooltip) {
            if (finalY + actualHeight > footerY) {
                finalY = footerY - actualHeight - 4;
            }
            if (finalY < Constants.HEADER_HEIGHT) {
                finalY = Constants.HEADER_HEIGHT;
            }
        } else {
            finalY = footerY - actualHeight - 4;
        }

        int bgAlpha = (int) (alpha * 224);
        int bgColor = (bgAlpha << 24);
        int borderAlpha = (int) (alpha * 128);
        int borderColor = 0x808080 | (borderAlpha << 24);

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 400);

        context.fill(tooltipX, finalY, tooltipX + tooltipWidth, finalY + actualHeight, bgColor);
        context.drawBorder(tooltipX, finalY, tooltipWidth, actualHeight, borderColor);

        context.enableScissor(tooltipX, finalY, tooltipX + tooltipWidth, finalY + actualHeight);

        int textY = finalY + 4;
        int textAlpha = (int) (alpha * 255);
        int textColor = 0xFFFFFF | (textAlpha << 24);

        for (OrderedText line : lines) {
            if (textY + textRenderer.fontHeight > finalY + actualHeight) break;
            context.drawText(textRenderer, line, tooltipX + 8, textY, textColor, true);
            textY += textRenderer.fontHeight + 2;
        }

        context.disableScissor();
        context.getMatrices().pop();
    }
}
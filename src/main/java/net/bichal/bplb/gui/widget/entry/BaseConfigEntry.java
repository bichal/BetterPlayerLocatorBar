package net.bichal.bplb.gui.widget.entry;

import net.bichal.bplb.gui.ConfigScreen;
import net.bichal.bplb.gui.animation.Transition;
import net.bichal.bplb.gui.widget.ScrollableListWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

public abstract class BaseConfigEntry extends ScrollableListWidget.Entry {
    protected final MinecraftClient client;
    protected final Text label;
    protected final String configKey;
    protected final Transition hoverTransition = Constants.createHoverTransition();
    protected final Transition highlightTransition = Constants.createTransition();
    protected Text highlightedText;
    protected Text tooltip;
    private long highlightStartTime = -1;
    private long tooltipHoverTime = -1;
    private boolean highlighted = false;
    protected boolean persistentHighlight = false;
    private boolean tooltipShown = false;

    protected BaseConfigEntry(MinecraftClient client, String key, Text label) {
        this.client = client;
        this.configKey = key;
        this.label = label;
        this.highlightedText = label;
    }

    public BaseConfigEntry setTooltip(Text tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public void setHighlightedText(Text text) {
        this.highlightedText = text;
    }

    protected Text getDisplayLabel() {
        return highlighted ? highlightedText : label;
    }

    public void setHighlighted(boolean highlighted) {
        if (highlighted && !this.highlighted) {
            highlightStartTime = System.currentTimeMillis();
            highlightTransition.setTarget(1f);
        }
        this.highlighted = highlighted;
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        boolean isHovered = mouseX >= x && mouseX <= x + entryWidth && mouseY >= y && mouseY <= y + entryHeight;

        hoverTransition.setTarget(isHovered ? 1f : 0f);
        float hoverAlpha = hoverTransition.update();

        float highlightAlpha = 0f;
        if (highlighted) {
            long elapsed = System.currentTimeMillis() - highlightStartTime;
            if (elapsed > Constants.HIGHLIGHT_DURATION_MS && !persistentHighlight) {
                highlighted = false;
                highlightTransition.setTarget(0f);
            } else {
                if (persistentHighlight) {
                    highlightAlpha = 0.3f;
                } else {
                    float pulse = (float) Math.sin(elapsed * 0.008) * 0.5f + 0.5f;
                    highlightAlpha = highlightTransition.update() * pulse * 0.4f;
                }
            }
        }

        float combinedAlpha = Math.max(hoverAlpha * 0.2f, highlightAlpha);
        if (combinedAlpha > 0.01f) {
            int bgAlpha = (int) (combinedAlpha * 255);
            int bgColor = persistentHighlight ? 0xFFFF00 : 0x808080;
            bgColor = bgColor | (bgAlpha << 24);
            context.fill(x + 4, y, x + entryWidth - 4, y + entryHeight, bgColor);
        }

        renderContent(context, x, y, entryWidth, entryHeight, mouseX, mouseY, tickDelta);

        if (isHovered && tooltip != null && client.currentScreen instanceof ConfigScreen screen) {
            if (tooltipHoverTime < 0) tooltipHoverTime = System.currentTimeMillis();

            if (System.currentTimeMillis() - tooltipHoverTime > Constants.TOOLTIP_FADE_DELAY_MS) {
                showTooltipNow(screen, x, y, entryWidth, entryHeight);
            }
        } else {
            tooltipHoverTime = -1;
            if (client.currentScreen instanceof ConfigScreen screen) {
                screen.clearTooltip();
            }
        }
    }

    private void showTooltipNow(ConfigScreen screen, int x, int y, int width, int height) {
        List<OrderedText> lines = client.textRenderer.wrapLines(tooltip, width - 16);
        int tooltipHeight = lines.size() * (client.textRenderer.fontHeight + 2) + 8;
        int screenHeight = client.getWindow().getScaledHeight();
        int footerY = screenHeight - 30;

        int tooltipY = y + height + 2;
        if (tooltipY + tooltipHeight > footerY) {
            tooltipY = y - tooltipHeight - 2;
        }

        screen.showTooltip(tooltip, x + 4, tooltipY, width - 8, tooltipHeight);
    }

    public void setPersistentHighlight(boolean persistent) {
        this.persistentHighlight = persistent;
    }

    protected abstract void renderContent(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta);

    public String getConfigKey() {
        return configKey;
    }

    public Text getLabel() {
        return label;
    }
}

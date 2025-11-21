package net.bichal.bplb.gui.widget.entry;

import net.bichal.bplb.gui.animation.Transition;
import net.bichal.bplb.gui.widget.ScrollableListWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public abstract class BaseConfigEntry extends ScrollableListWidget.Entry {
    protected final MinecraftClient client;
    protected final Text label;
    protected final String configKey;
    protected final Transition hoverTransition = Constants.createHoverTransition();
    protected final Transition highlightTransition = Constants.createTransition();
    protected Text highlightedText;
    private long highlightStartTime = -1;
    private boolean highlighted = false;

    protected BaseConfigEntry(MinecraftClient client, String key, Text label) {
        this.client = client;
        this.configKey = key;
        this.label = label;
        this.highlightedText = label;
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
            if (elapsed > Constants.HIGHLIGHT_DURATION_MS) {
                highlighted = false;
                highlightTransition.setTarget(0f);
            } else {
                float progress = 1f - (elapsed / (float) Constants.HIGHLIGHT_DURATION_MS);
                float pulse = (float) Math.sin(elapsed * 0.008) * 0.5f + 0.5f;
                highlightAlpha = highlightTransition.update() * pulse * 0.4f;
            }
        }

        float combinedAlpha = Math.max(hoverAlpha * 0.2f, highlightAlpha);
        if (combinedAlpha > 0.01f) {
            int bgAlpha = (int) (combinedAlpha * 255);
            int bgColor = 0x808080 | (bgAlpha << 24);
            context.fill(x + 4, y, x + entryWidth - 4, y + entryHeight, bgColor);
        }

        renderContent(context, x, y, entryWidth, entryHeight, mouseX, mouseY, tickDelta);

        if (isHovered && hoverAlpha > 0.5f) {
            checkTooltip(mouseX, mouseY);
        }
    }

    protected void checkTooltip(int mouseX, int mouseY) {}

    protected abstract void renderContent(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta);

    public String getConfigKey() {
        return configKey;
    }

    public Text getLabel() {
        return label;
    }
}

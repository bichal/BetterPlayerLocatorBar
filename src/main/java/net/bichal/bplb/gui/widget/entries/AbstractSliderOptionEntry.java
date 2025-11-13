package net.bichal.bplb.gui.widget.entries;

import net.bichal.bplb.gui.ConfigScreen;
import net.bichal.bplb.gui.widget.ScrollableListWidget;
import net.bichal.bplb.gui.widget.SliderWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.text.Text;

import java.util.List;

public abstract class AbstractSliderOptionEntry extends ScrollableListWidget.Entry {
    protected final SliderWidget slider;
    protected final Text label;
    private final MinecraftClient client;
    private final String configKey;

    public AbstractSliderOptionEntry(MinecraftClient client, String key, SliderWidget slider) {
        this.client = client;
        this.label = Text.translatable(Constants.CONFIG_KEY_PREFIX + key);
        this.slider = slider;
        this.slider.updateMessage();
        this.configKey = key;
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        slider.setX(x + entryWidth - Constants.CONFIG_SLIDER_WIDTH - Constants.CONFIG_PADDING);
        slider.setY(y + 2);

        if (hovered && this.client.currentScreen instanceof ConfigScreen screen) {
            screen.showTooltip(configKey, y, entryHeight);
        } else if (this.client.currentScreen instanceof ConfigScreen screen) {
            screen.clearTooltip();
        }

        if (this.client.currentScreen instanceof ConfigScreen screen) {
            screen.drawLabelWithHighlight(context, label, x + Constants.CONFIG_PADDING, y + 6, extractKeyFromLabel());
        } else {
            context.drawTextWithShadow(this.client.textRenderer, label, x + Constants.CONFIG_PADDING, y + 6, Constants.WHITE_COLOR);
        }

        context.drawTextWithShadow(this.client.textRenderer, slider.getMessage(), slider.getX() - this.client.textRenderer.getWidth(slider.getMessage()) - 5, y + 6, Constants.WHITE_COLOR);
        slider.render(context, mouseX, mouseY, tickDelta);
    }

    private String extractKeyFromLabel() {
        String fullKey = label.getString();
        if (fullKey.startsWith(Constants.CONFIG_KEY_PREFIX)) {
            return fullKey.substring(Constants.CONFIG_KEY_PREFIX.length());
        }
        return fullKey;
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return List.of(new Selectable() {
            @Override
            public SelectionType getType() {
                return SelectionType.FOCUSED;
            }

            @Override
            public void appendNarrations(NarrationMessageBuilder builder) {
                builder.put(NarrationPart.TITLE, label);
                builder.put(NarrationPart.USAGE, slider.getMessage());
            }
        });
    }

    @Override
    public List<? extends Element> children() {
        return List.of(this.slider);
    }
}

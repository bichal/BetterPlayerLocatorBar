package net.bichal.bplb.config.widget.entries;

import net.bichal.bplb.config.widget.CustomSliderWidget;
import net.bichal.bplb.config.widget.ScrollableListWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;

import java.util.List;

public abstract class AbstractSliderOptionEntry extends ScrollableListWidget.Entry {
    protected final CustomSliderWidget slider;
    protected final Text label;
    private final MinecraftClient client;

    public AbstractSliderOptionEntry(MinecraftClient client, String key, CustomSliderWidget slider) {
        this.client = client;
        this.label = Text.translatable(Constants.CONFIG_KEY_PREFIX + key);
        this.slider = slider;
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        slider.setX(x + entryWidth - Constants.CONFIG_SLIDER_WIDTH - Constants.CONFIG_PADDING);
        slider.setY(y + 2);
        context.drawTextWithShadow(this.client.textRenderer, label, x + Constants.CONFIG_PADDING, y + 6, 0xFFFFFF);
        context.drawTextWithShadow(this.client.textRenderer, slider.getMessage(), slider.getX() - this.client.textRenderer.getWidth(slider.getMessage()) - 5, y + 6, 0xFFFFFF);
        slider.render(context, mouseX, mouseY, tickDelta);
    }

    @Override public List<? extends Selectable> selectableChildren() {
        return List.of(this.slider);
    }

    @Override public List<? extends Element> children() {
        return List.of(this.slider);
    }
}

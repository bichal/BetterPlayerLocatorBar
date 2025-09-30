package net.bichal.bplb.config.entries;

import net.bichal.bplb.config.widget.ButtonWidget;
import net.bichal.bplb.config.widget.ScrollableListWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class ToggleOptionEntry extends ScrollableListWidget.Entry {
    private final ButtonWidget button;
    private final Text label;
    private final MinecraftClient client;
    private boolean value;

    public ToggleOptionEntry(MinecraftClient client, String key, boolean initialValue, Consumer<Boolean> valueConsumer, Runnable onDirty) {
        this.client = client;
        this.label = Text.translatable(Constants.CONFIG_KEY_PREFIX + key);
        this.value = initialValue;
        this.button = ButtonWidget.builder(Text.empty(), button -> {
            this.value = !this.value;
            valueConsumer.accept(this.value);
            onDirty.run();
        }).dimensions(0, 0, Constants.CONFIG_TOGGLE_WIDTH, 20).build();
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        context.drawTextWithShadow(this.client.textRenderer, this.label, x + Constants.CONFIG_PADDING, y + 6, 0xFFFFFF);
        button.setX(x + entryWidth - Constants.CONFIG_TOGGLE_WIDTH - Constants.CONFIG_PADDING);
        button.setY(y + 2);

        Text toggleText = value ? Text.translatable("gui.yes") : Text.translatable("gui.no");
        int color = button.active ? (value ? 0x55FF55 : 0xFF5555) : 0xAAAAAA;

        button.render(context, mouseX, mouseY, tickDelta);
        context.drawCenteredTextWithShadow(this.client.textRenderer, toggleText, button.getX() + button.getWidth() / 2, y + 8, color);
    }

    @Override public List<? extends Selectable> selectableChildren() {
        return List.of(this.button);
    }

    @Override public List<? extends Element> children() {
        return List.of(this.button);
    }
}

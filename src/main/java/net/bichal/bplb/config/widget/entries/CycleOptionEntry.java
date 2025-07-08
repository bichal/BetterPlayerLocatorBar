package net.bichal.bplb.config.widget.entries;

import net.bichal.bplb.config.widget.CustomButtonWidget;
import net.bichal.bplb.config.widget.ScrollableListWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CycleOptionEntry<T> extends ScrollableListWidget.Entry {
    private final CustomButtonWidget button;
    private final Text label;
    private final List<T> options;
    private final String key;
    private T value;
    private final Function<T, Text> textProvider;
    private final Consumer<T> valueConsumer;
    private final Runnable onDirty;
    private final MinecraftClient client;

    public CycleOptionEntry(MinecraftClient client, String key, T initialValue, List<T> options, Function<T, Text> textProvider, Consumer<T> valueConsumer, Runnable onDirty) {
        this.client = client;
        this.key = key;
        this.label = Text.translatable(Constants.CONFIG_KEY_PREFIX + key);
        this.value = initialValue;
        this.options = options;
        this.textProvider = textProvider;
        this.valueConsumer = valueConsumer;
        this.onDirty = onDirty;
        this.button = CustomButtonWidget.builder(textProvider.apply(this.value), button -> {
        }).dimensions(0, 0, Constants.CONFIG_TOGGLE_WIDTH, 20).build();
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        button.active = this.options != null && !this.options.isEmpty();
        button.setX(x + entryWidth - Constants.CONFIG_TOGGLE_WIDTH - Constants.CONFIG_PADDING);
        button.setY(y + 2);
        context.drawTextWithShadow(this.client.textRenderer, label, x + Constants.CONFIG_PADDING, y + 6, 0xFFFFFF);
        button.render(context, mouseX, mouseY, tickDelta);
    }

    @Override @SuppressWarnings("unchecked") public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
        if (this.button.isMouseOver(mouseX, mouseY) && this.button.active) {
            this.button.playDownSound(MinecraftClient.getInstance().getSoundManager());

            if (this.key.equals("dot_type") && buttonId == 0 && Screen.hasShiftDown()) {
                this.value = (T) "bowtie";
                this.button.setMessage(this.textProvider.apply(this.value));
                this.valueConsumer.accept(this.value);
                this.onDirty.run();
                return true;
            }

            if (this.options == null || this.options.isEmpty()) return false;

            int currentIndex = this.options.indexOf(this.value);
            int nextIndex = -1;

            if (buttonId == 0) {
                nextIndex = (currentIndex + 1) % this.options.size();
            } else if (buttonId == 1) {
                nextIndex = (currentIndex - 1 + this.options.size()) % this.options.size();
            }

            if (nextIndex != -1) {
                this.value = this.options.get(nextIndex);
                this.button.setMessage(this.textProvider.apply(this.value));
                this.valueConsumer.accept(this.value);
                this.onDirty.run();
                return true;
            }
        }
        return false;
    }

    @Override public List<? extends Selectable> selectableChildren() {
        return List.of(this.button);
    }

    @Override public List<? extends Element> children() {
        return List.of(this.button);
    }
}

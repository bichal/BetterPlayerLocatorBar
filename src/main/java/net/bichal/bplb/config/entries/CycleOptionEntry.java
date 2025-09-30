package net.bichal.bplb.config.entries;

import net.bichal.bplb.config.widget.ButtonWidget;
import net.bichal.bplb.config.widget.ScrollableListWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CycleOptionEntry<T> extends ScrollableListWidget.Entry {
    private final ButtonWidget button;
    private final Text label;
    private final List<T> options;
    private final String key;
    private T value;
    private final Function<T, Text> textProvider;
    private final Consumer<T> valueConsumer;
    private final Runnable onDirty;
    private final MinecraftClient client;
    private Text hintText = null;

    public CycleOptionEntry(MinecraftClient client, String key, T initialValue, List<T> options,
                            Function<T, Text> textProvider, Consumer<T> valueConsumer, Runnable onDirty) {
        this.client = client;
        this.key = key;
        this.label = Text.translatable(Constants.CONFIG_KEY_PREFIX + key);
        this.value = initialValue;
        this.options = options;
        this.textProvider = textProvider;
        this.valueConsumer = valueConsumer;
        this.onDirty = onDirty;

        // Add hint text for dot type
        if (key.equals("dot_type")) {
            this.hintText = Text.literal("Shift+Click for secret").formatted(Formatting.GRAY, Formatting.ITALIC);
        }

        this.button = ButtonWidget.builder(textProvider.apply(this.value), button -> { })
                .dimensions(0, 0, Constants.CONFIG_TOGGLE_WIDTH, 20).build();
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean hovered, float tickDelta) {
        button.active = this.options != null && !this.options.isEmpty();
        button.setX(x + entryWidth - Constants.CONFIG_TOGGLE_WIDTH - Constants.CONFIG_PADDING);
        button.setY(y + 2);

        context.drawTextWithShadow(this.client.textRenderer, label, x + Constants.CONFIG_PADDING, y + 6, 0xFFFFFF);

        // Draw hint text for dot type
        if (this.key.equals("dot_type") && hintText != null && button.isMouseOver(mouseX, mouseY)) {
            int hintX = button.getX() - client.textRenderer.getWidth(hintText) - 5;
            context.drawTextWithShadow(this.client.textRenderer, hintText, hintX, y + 6, 0x808080);
        }

        button.render(context, mouseX, mouseY, tickDelta);
    }

    private int calculateNextIndex(int currentIndex, int buttonId) {
        if (currentIndex == -1 && this.key.equals("dot_type") && this.value.equals("bowtie")) {
            return 0;
        }
        if (buttonId == 0) {
            return (currentIndex + 1) % this.options.size();
        }
        if (buttonId == 1) {
            return (currentIndex - 1 + this.options.size()) % this.options.size();
        }
        return -1;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
        if (this.button.isMouseOver(mouseX, mouseY) && this.button.active) {
            this.button.playDownSound(MinecraftClient.getInstance().getSoundManager());

            if (this.key.equals("dot_type") && buttonId == 0 && Screen.hasShiftDown()) {
                @SuppressWarnings("unchecked")
                T bowtieValue = (T) "bowtie";
                this.value = bowtieValue;
                this.button.setMessage(this.textProvider.apply(this.value));
                this.valueConsumer.accept(this.value);
                this.onDirty.run();
                return true;
            }

            if (this.options == null || this.options.isEmpty()) return false;

            int nextIndex = calculateNextIndex(this.options.indexOf(this.value), buttonId);

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

    @Override
    public List<? extends Selectable> selectableChildren() {
        return List.of(this.button);
    }

    @Override
    public List<? extends Element> children() {
        return List.of(this.button);
    }
}
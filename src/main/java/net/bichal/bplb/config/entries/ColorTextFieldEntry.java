package net.bichal.bplb.config.entries;

import net.bichal.bplb.config.ConfigScreen;
import net.bichal.bplb.config.widget.ScrollableListWidget;
import net.bichal.bplb.config.widget.TextInputWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class ColorTextFieldEntry extends ScrollableListWidget.Entry {
    private final TextInputWidget textField;
    private final Text label;
    private final MinecraftClient client;
    private final Consumer<Integer> valueConsumer;
    private final ConfigScreen parent;
    private Integer currentColor;

    public ColorTextFieldEntry(MinecraftClient client, String key, int initialColor, Consumer<Integer> valueConsumer, ConfigScreen parent) {
        this.client = client;
        this.parent = parent;
        this.label = Text.translatable(Constants.CONFIG_KEY_PREFIX + key);
        this.currentColor = initialColor;
        this.valueConsumer = valueConsumer;
        String hexValue = "#" + Integer.toHexString(initialColor & Constants.WHITE_COLOR).toUpperCase();
        this.textField = new TextInputWidget(client.textRenderer, 0, 0, 80, 20, Text.literal(hexValue));
        this.textField.setText(hexValue);
        this.textField.setMaxLength(7);
        this.textField.setChangedListener(this::onTextChanged);
    }

    private void onTextChanged(String text) {
        if (text.startsWith("#") && text.length() == 7) {
            try {
                int color = Integer.parseInt(text.substring(1), 16);
                this.currentColor = Constants.BLACK_COLOR | color;
                this.valueConsumer.accept(this.currentColor);
                this.parent.markDirty();
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        context.drawTextWithShadow(this.client.textRenderer, this.label, x + Constants.CONFIG_PADDING, y + 6, Constants.WHITE_COLOR);
        this.textField.setX(x + entryWidth - 80 - Constants.CONFIG_PADDING - 24);
        this.textField.setY(y + 2);
        this.textField.render(context, mouseX, mouseY, tickDelta);
        int previewX = x + entryWidth - 20 - Constants.CONFIG_PADDING;
        int previewY = y + 2;
        context.fill(previewX, previewY, previewX + 20, previewY + 20, this.currentColor);
        context.drawBorder(previewX, previewY, 20, 20, Constants.WHITE_COLOR);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.textField.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return this.textField.charTyped(chr, modifiers);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (focused) {
            this.textField.setFocused(true);
        }
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return List.of(this.textField);
    }

    @Override
    public List<? extends Element> children() {
        return List.of(this.textField);
    }
}

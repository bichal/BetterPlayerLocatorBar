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

public class TextFieldOptionEntry extends ScrollableListWidget.Entry {
    private final TextInputWidget textField;
    private final Text label;
    private final MinecraftClient client;
    private final Consumer<String> valueConsumer;
    private final ConfigScreen parent;

    public TextFieldOptionEntry(MinecraftClient client, ConfigScreen parent, String key, String initialValue, Consumer<String> valueConsumer) {
        this.client = client;
        this.parent = parent;
        this.label = Text.translatable(Constants.CONFIG_KEY_PREFIX + key);
        this.valueConsumer = valueConsumer;

        this.textField = new TextInputWidget(client.textRenderer, 0, 0, 150, 20, Text.literal(initialValue));
        this.textField.setText(initialValue);
        this.textField.setMaxLength(32);
        this.textField.setChangedListener(text -> {
            this.valueConsumer.accept(text);
            this.parent.markDirty();
        });
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        context.drawTextWithShadow(this.client.textRenderer, this.label, x + Constants.CONFIG_PADDING, y + 6, 0xFFFFFF);

        this.textField.setX(x + entryWidth - 150 - Constants.CONFIG_PADDING);
        this.textField.setY(y + 2);
        this.textField.render(context, mouseX, mouseY, tickDelta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.textField.mouseClicked(mouseX, mouseY, button)) {
            this.parent.setFocused(this.textField);
            this.setFocused(true);
            return true;
        }
        return false;
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
        if (!focused) {
            this.textField.setFocused(false);
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

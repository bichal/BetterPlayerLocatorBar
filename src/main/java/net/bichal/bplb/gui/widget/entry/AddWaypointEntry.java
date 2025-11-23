package net.bichal.bplb.gui.widget.entry;

import net.bichal.bplb.gui.widget.CompactButton;
import net.bichal.bplb.gui.widget.TextInputWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public final class AddWaypointEntry extends BaseConfigEntry {
    private final TextInputWidget inputField;
    private final CompactButton addButton;

    public AddWaypointEntry(MinecraftClient client, Text placeholder, Consumer<String> onAdd) {
        super(client, "add_waypoint", placeholder);

        this.inputField = new TextInputWidget(client.textRenderer, 0, 0, 200, 20, placeholder);
        this.inputField.setMaxLength(16);
        this.inputField.setPlaceholder(placeholder);

        this.addButton = CompactButton.text(0, 0, 30, 20, Text.literal("+"), b -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                onAdd.accept(text);
                inputField.setText("");
            }
        });
    }

    @Override
    protected void renderContent(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta) {
        inputField.setX(x + 10);
        inputField.setY(y + (height - 20) / 2);
        inputField.render(context, mouseX, mouseY, delta);

        addButton.setX(x + 220);
        addButton.setY(y + (height - 20) / 2);
        addButton.active = !inputField.getText().trim().isEmpty();
        addButton.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return inputField.mouseClicked(mouseX, mouseY, button) ||
                addButton.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return inputField.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return inputField.charTyped(chr, modifiers);
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return List.of(inputField, addButton);
    }

    @Override
    public List<? extends Element> children() {
        return List.of(inputField, addButton);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
    }
}

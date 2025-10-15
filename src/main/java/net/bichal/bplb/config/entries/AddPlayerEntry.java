package net.bichal.bplb.config.entries;

import net.bichal.bplb.config.Config;
import net.bichal.bplb.config.ConfigScreen;
import net.bichal.bplb.config.widget.ButtonWidget;
import net.bichal.bplb.config.widget.ScrollableListWidget;
import net.bichal.bplb.config.widget.TextInputWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;

public class AddPlayerEntry extends ScrollableListWidget.Entry {
    private final ConfigScreen parent;
    private final Config workingConfig;
    private final TextInputWidget inputField;
    private final ButtonWidget addButton;

    public AddPlayerEntry(MinecraftClient client, ConfigScreen parent, Config workingConfig) {
        this.parent = parent;
        this.workingConfig = workingConfig;
        this.inputField = new TextInputWidget(client.textRenderer, 0, 0, 150, 20, Text.literal(""));
        this.inputField.setMaxLength(16);
        this.inputField.setPlaceholder(Text.translatable("bplb.config.player_appearance.add_player").formatted(Formatting.GRAY));
        this.addButton = ButtonWidget.builder(Text.literal("+"), btn -> addPlayer()).dimensions(0, 0, 20, 20).build();
    }

    private void addPlayer() {
        String name = inputField.getText().trim();
        if (!name.isEmpty() && !workingConfig.getPlayerConfigs().containsKey(name)) {
            workingConfig.getPlayerConfigs().put(name, new Config.PlayerAppearance());
            parent.markDirty();
            inputField.setText("");
            inputField.setFocused(false);
            parent.rebuildList();
        }
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        int inputX = x + Constants.CONFIG_PADDING;
        int inputY = y + 2;
        int buttonX = inputX + inputField.getWidth() + 5;
        int buttonY = y + 2;

        inputField.setX(inputX);
        inputField.setY(inputY);
        inputField.render(context, mouseX, mouseY, tickDelta);
        addButton.active = !inputField.getText().trim().isEmpty() && !workingConfig.getPlayerConfigs().containsKey(inputField.getText().trim());
        addButton.setX(buttonX);
        addButton.setY(buttonY);
        addButton.render(context, mouseX, mouseY, tickDelta);
    }

    @Override
    public List<? extends Element> children() {
        return Arrays.asList(inputField, addButton);
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return Arrays.asList(inputField, addButton);
    }
}

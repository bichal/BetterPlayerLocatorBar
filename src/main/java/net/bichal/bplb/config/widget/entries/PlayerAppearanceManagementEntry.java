package net.bichal.bplb.config.widget.entries;

import net.bichal.bplb.config.Config;
import net.bichal.bplb.config.ConfigScreen;
import net.bichal.bplb.config.PlayerAppearance;
import net.bichal.bplb.config.widget.CustomTextInputWidget;
import net.bichal.bplb.config.widget.ScrollableListWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

public class PlayerAppearanceManagementEntry extends ScrollableListWidget.Entry {
    private final TextFieldWidget nameInputField;
    private final ButtonWidget addButton;
    private final ConfigScreen parent;

    public PlayerAppearanceManagementEntry(MinecraftClient client, ConfigScreen parent, Config workingConfig) {
        this.parent = parent;

        this.nameInputField = new CustomTextInputWidget(client.textRenderer, 0, 0, 150, 20, Text.translatable(Constants.CONFIG_KEY_PREFIX + "player_appearance.add_player"));
        this.addButton = ButtonWidget.builder(Text.literal("+"), button -> {
            String playerName = this.nameInputField.getText();
            if (!playerName.isBlank() && !workingConfig.getPlayerAppearances().containsKey(playerName)) {
                workingConfig.getPlayerAppearances().put(playerName, new PlayerAppearance());
                this.parent.markDirty();
                this.nameInputField.setText("");
                this.parent.rebuildList();
            }
        }).dimensions(0, 0, 20, 20).build();
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        this.nameInputField.setX(x + Constants.CONFIG_PADDING);
        this.nameInputField.setY(y + 2);
        this.addButton.setX(this.nameInputField.getX() + this.nameInputField.getWidth() + 5);
        this.addButton.setY(y + 2);

        this.nameInputField.render(context, mouseX, mouseY, tickDelta);
        this.addButton.render(context, mouseX, mouseY, tickDelta);
    }

    @Override public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.nameInputField.setFocused(false);
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.nameInputField.mouseClicked(mouseX, mouseY, button)) {
            this.parent.setFocused(this.nameInputField);
            this.setFocused(true);
            return true;
        }
        if (this.addButton.mouseClicked(mouseX, mouseY, button)) {
            this.parent.setFocused(null);
            this.nameInputField.setFocused(false);
            return true;
        }
        return false;
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.nameInputField.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean charTyped(char chr, int modifiers) {
        return this.nameInputField.charTyped(chr, modifiers);
    }

    @Override public List<? extends Selectable> selectableChildren() {
        return List.of(this.nameInputField, this.addButton);
    }

    @Override public List<? extends Element> children() {
        return List.of(this.nameInputField, this.addButton);
    }
}

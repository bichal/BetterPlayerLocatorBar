package net.bichal.bplb.config.entries;

import net.bichal.bplb.config.Config;
import net.bichal.bplb.config.ConfigScreen;
import net.bichal.bplb.config.PlayerConfig;
import net.bichal.bplb.config.widget.ScrollableListWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class PlayerListEntry extends ScrollableListWidget.Entry {
    private final ButtonWidget removeButton;
    private final ButtonWidget editButton;
    private final String playerName;
    private final MinecraftClient client;

    public PlayerListEntry(MinecraftClient client, ConfigScreen parent, Config workingConfig, String playerName) {
        this.client = client;
        this.playerName = playerName;
        this.removeButton = ButtonWidget.builder(Text.literal("-"), button -> {
            workingConfig.getPlayerConfigs().remove(playerName);
            parent.markDirty();
            parent.rebuildList();
        }).dimensions(0, 0, 20, 20).build();

        this.editButton = ButtonWidget.builder(Text.translatable("bplb.config.player_appearance.edit"), button -> {
            PlayerConfig appearance = workingConfig.getPlayerConfigs().get(playerName);
//            if (appearance != null) {
//                this.client.setScreen(new PlayerAppearanceConfigScreen(parent, playerName, appearance, updatedAppearance -> parent.markDirty()));
//            }
        }).dimensions(0, 0, 50, 20).build();
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        context.drawTextWithShadow(this.client.textRenderer, Text.literal(this.playerName), x + Constants.CONFIG_PADDING, y + 6, 0xFFFFFF);

        this.removeButton.setX(x + entryWidth - Constants.CONFIG_PADDING - 20);
        this.removeButton.setY(y + 2);
        this.removeButton.render(context, mouseX, mouseY, tickDelta);

        this.editButton.setX(this.removeButton.getX() - 5 - 50);
        this.editButton.setY(y + 2);
        this.editButton.render(context, mouseX, mouseY, tickDelta);
    }

    @Override public List<? extends Selectable> selectableChildren() {
        return List.of(this.removeButton, this.editButton);
    }

    @Override public List<? extends Element> children() {
        return List.of(this.removeButton, this.editButton);
    }
}

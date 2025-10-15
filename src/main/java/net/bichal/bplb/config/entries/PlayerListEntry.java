package net.bichal.bplb.config.entries;

import net.bichal.bplb.config.Config;
import net.bichal.bplb.config.ConfigScreen;
import net.bichal.bplb.config.widget.ButtonWidget;
import net.bichal.bplb.config.widget.ScrollableListWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;

import java.util.List;

public class PlayerListEntry extends ScrollableListWidget.Entry {
    private final ButtonWidget removeButton;
    private final String playerName;
    private final MinecraftClient client;
    private final Config workingConfig;
    private final ConfigScreen parent;
    private int lastX, lastY, lastWidth, lastHeight;
    private boolean expanded;

    public PlayerListEntry(MinecraftClient client, ConfigScreen parent, Config workingConfig, String playerName, boolean expanded) {
        this.client = client;
        this.parent = parent;
        this.playerName = playerName;
        this.workingConfig = workingConfig;
        this.expanded = expanded;

        this.removeButton = ButtonWidget.builder(Text.literal("-"), button -> {
            workingConfig.getPlayerConfigs().remove(playerName);
            parent.markDirty();
            parent.rebuildList();
        }).dimensions(0, 0, 20, 20).build();
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        this.lastX = x;
        this.lastY = y;
        this.lastWidth = entryWidth;
        this.lastHeight = entryHeight;

        int bgColor = hovered ? 0xA0303030 : 0x80202020;
        context.fill(x + Constants.CONFIG_PADDING, y, x + entryWidth - Constants.CONFIG_PADDING * 4 + 5, y + entryHeight, bgColor);

        String arrow = expanded ? "▼" : "▶";
        context.drawTextWithShadow(this.client.textRenderer, Text.literal(arrow), x + Constants.CONFIG_PADDING * 2, y + 6, 0xFF808080);
        context.drawTextWithShadow(this.client.textRenderer, Text.literal(this.playerName), x + Constants.CONFIG_PADDING * 4, y + 6, Constants.WHITE_COLOR);

        this.removeButton.setX(x + entryWidth - Constants.CONFIG_PADDING - 20);
        this.removeButton.setY(y);
        this.removeButton.render(context, mouseX, mouseY, tickDelta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.removeButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (button == 0 && mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() &&
                mouseY >= this.getY() && mouseY <= this.getY() + this.getHeight()) {
            this.expanded = !this.expanded;
            workingConfig.setPlayerExpanded(playerName, this.expanded);
            parent.rebuildList();
            return true;
        }
        return false;
    }

    @SuppressWarnings("unused") // Temporal
    public boolean isExpanded() {
        return expanded;
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return List.of(this.removeButton);
    }

    @Override
    public List<? extends Element> children() {
        return List.of(this.removeButton);
    }

    private int getX() { return lastX; }
    private int getY() { return lastY; }
    private int getWidth() { return lastWidth; }
    private int getHeight() { return lastHeight; }
}

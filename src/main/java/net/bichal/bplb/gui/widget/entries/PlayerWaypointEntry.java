package net.bichal.bplb.gui.widget.entries;

import net.bichal.bplb.client.render.RenderAddons;
import net.bichal.bplb.gui.Config;
import net.bichal.bplb.gui.ConfigScreen;
import net.bichal.bplb.gui.widget.ButtonWidget;
import net.bichal.bplb.gui.widget.ScrollableListWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerWaypointEntry extends ScrollableListWidget.Entry {
    private final ButtonWidget removeButton;
    private final String playerName;
    private final MinecraftClient client;
    private final Config workingConfig;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final ConfigScreen parent;
    private final boolean expanded;
    private final Consumer<String> onToggle;
    private int lastX, lastY, lastWidth, lastHeight;
    
    public PlayerWaypointEntry(MinecraftClient client, ConfigScreen parent, Config workingConfig, String playerName, boolean expanded, Consumer<String> onToggle) {
        this.client = client;
        this.parent = parent;
        this.playerName = playerName;
        this.workingConfig = workingConfig;
        this.expanded = expanded;
        this.onToggle = onToggle;
        
        this.removeButton = ButtonWidget.builder(Text.literal("-"), button -> {
            workingConfig.getPlayerConfigs().remove(playerName);
            parent.markDirty();
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
        
        UUID uuid = RenderAddons.getUuidFromCache(playerName);
        if (uuid != null) {
            context.getMatrices().push();
            context.getMatrices().translate(x + Constants.CONFIG_PADDING * 4, y + 2, 0);
            RenderAddons.renderPlayerIcon(context, playerName, uuid, 0, 0, 0, false, workingConfig, 1.0f);
            context.getMatrices().pop();
        }
        
        context.drawTextWithShadow(this.client.textRenderer, Text.literal(this.playerName), x + Constants.CONFIG_PADDING * 4 + 12, y + 6, Constants.WHITE_COLOR);
        
        this.removeButton.setX(x + entryWidth - Constants.CONFIG_PADDING - 20);
        this.removeButton.setY(y);
        this.removeButton.render(context, mouseX, mouseY, tickDelta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.removeButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        
        if (button == 0 && mouseX >= lastX && mouseX <= lastX + lastWidth && mouseY >= lastY && mouseY <= lastY + lastHeight) {
            onToggle.accept(playerName);
            return true;
        }
        return false;
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return List.of(this.removeButton);
    }

    @Override
    public List<? extends Element> children() {
        return List.of(this.removeButton);
    }
}
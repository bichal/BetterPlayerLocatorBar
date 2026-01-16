package net.bichal.bplb.gui.widget.entry;

import net.bichal.bplb.gui.Config;
import net.bichal.bplb.gui.widget.CompactButton;
import net.bichal.bplb.gui.widget.DotPreviewWidget;
import net.bichal.bplb.util.ColorConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class PlayerWaypointEntry extends BaseConfigEntry {
    private final Config.PlayerAppearance appearance;
    private final BiConsumer<String, Action> actionHandler;
    private final List<CompactButton> buttons = new ArrayList<>();
    private final UUID playerUuid;
    private boolean visible = true;
    private static Config.PlayerAppearance clipboard = null;

    public PlayerWaypointEntry(MinecraftClient client, String playerName, Config.PlayerAppearance appearance, BiConsumer<String, Action> actionHandler) {
        super(client, playerName, Text.literal(playerName));
        this.appearance = appearance;
        this.actionHandler = actionHandler;
        this.playerUuid = getPlayerUuid(playerName);
        initButtons();
    }

    private UUID getPlayerUuid(String playerName) {
        if (client.world != null) {
            for (var player : client.world.getPlayers()) {
                if (player.getName().getString().equals(playerName)) {
                    return player.getUuid();
                }
            }
        }
        return UUID.nameUUIDFromBytes(playerName.getBytes());
    }

    private void initButtons() {
        buttons.add(CompactButton.text(0, 0, 20, 20, Text.literal("📋"), b -> {
            clipboard = copyAppearance(appearance);
            actionHandler.accept(configKey, Action.COPY_STYLES);
        }));

        buttons.add(CompactButton.text(0, 0, 20, 20, Text.literal("📄"), b -> {
            if (clipboard != null) {
                pasteAppearance(clipboard, appearance);
                actionHandler.accept(configKey, Action.PASTE_STYLES);
            }
        }));

        buttons.add(CompactButton.text(0, 0, 20, 20, Text.literal(visible ? "👁" : "🚫"), b -> {
            visible = !visible;
            b.setMessage(Text.literal(visible ? "👁" : "🚫"));
            actionHandler.accept(configKey, Action.TOGGLE_VISIBILITY);
        }));

        buttons.add(CompactButton.text(0, 0, 20, 20, Text.literal("🗑"), b -> {
            actionHandler.accept(configKey, Action.DELETE);
        }));
    }

    private Config.PlayerAppearance copyAppearance(Config.PlayerAppearance source) {
        Config.PlayerAppearance copy = new Config.PlayerAppearance();
        copy.dotType = source.dotType;
        copy.iconBorderStyle = source.iconBorderStyle;
        copy.iconBorderType = source.iconBorderType;
        copy.arrowType = source.arrowType;
        copy.color = source.color;
        copy.textureHeadOverride = source.textureHeadOverride;
        return copy;
    }

    private void pasteAppearance(Config.PlayerAppearance source, Config.PlayerAppearance target) {
        target.dotType = source.dotType;
        target.iconBorderStyle = source.iconBorderStyle;
        target.iconBorderType = source.iconBorderType;
        target.arrowType = source.arrowType;
        target.color = source.color;
        target.textureHeadOverride = source.textureHeadOverride;
    }

    @Override
    protected void renderContent(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta) {
        DotPreviewWidget.render(context, x + 10, y + (height - 9) / 2, appearance, label.getString(), playerUuid);

        Text displayName = label;
        if (Screen.hasShiftDown()) {
            displayName = Text.literal(label.getString() + " (" + playerUuid.toString().substring(0, 8) + "...)");
        }
        context.drawText(client.textRenderer, displayName, x + 25, y + (height - 8) / 2, ColorConstants.COLOR_WHITE, true);

        int buttonX = x + width - 10;
        for (int i = buttons.size() - 1; i >= 0; i--) {
            buttonX -= 24;
            CompactButton btn = buttons.get(i);
            btn.setX(buttonX);
            btn.setY(y + (height - 20) / 2);
            if (i == 1) {
                btn.active = clipboard != null;
            }
            btn.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (CompactButton btn : buttons) {
            if (btn.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return false;
    }

    @Override
    public List<? extends Element> children() {
        return buttons;
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return buttons;
    }

    public enum Action {
        COPY_STYLES, PASTE_STYLES, TOGGLE_VISIBILITY, DELETE
    }
}

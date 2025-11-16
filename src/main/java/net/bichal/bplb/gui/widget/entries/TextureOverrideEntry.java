package net.bichal.bplb.gui.widget.entries;

import net.bichal.bplb.gui.Config;
import net.bichal.bplb.gui.ConfigScreen;
import net.bichal.bplb.gui.widget.ScrollableListWidget;
import net.bichal.bplb.gui.widget.TextInputWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;

import java.util.List;

public class TextureOverrideEntry extends ScrollableListWidget.Entry {
    private final TextInputWidget textField;
    private final Text label;
    private final MinecraftClient client;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final Config.PlayerAppearance appearance;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final ConfigScreen parent;

    public TextureOverrideEntry(MinecraftClient client, Config.PlayerAppearance appearance, ConfigScreen parent) {
        this.client = client;
        this.appearance = appearance;
        this.parent = parent;
        this.label = Text.translatable(Constants.CONFIG_KEY_PREFIX + "player_appearance.texture_override");

        String initialValue = appearance.textureHeadOverride != null ? appearance.textureHeadOverride : "";
        this.textField = new TextInputWidget(client.textRenderer, 0, 0, 150, 20, Text.literal(initialValue));
        this.textField.setText(initialValue);
        this.textField.setMaxLength(36);
        this.textField.setPlaceholder(Text.translatable("bplb.config.player_appearance.texture_override.placeholder"));
        this.textField.setChangedListener(text -> {
            appearance.textureHeadOverride = text.isEmpty() ? null : text;
            parent.markDirty();
        });
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        context.drawTextWithShadow(this.client.textRenderer, this.label, x + Constants.CONFIG_PADDING, y + 6, Constants.WHITE_COLOR);

        int fieldX = x + entryWidth - 150 - Constants.CONFIG_PADDING;
        this.textField.setX(fieldX);
        this.textField.setY(y + 2);
        this.textField.render(context, mouseX, mouseY, tickDelta);
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
    public List<? extends Selectable> selectableChildren() {
        return List.of(this.textField);
    }

    @Override
    public List<? extends Element> children() {
        return List.of(this.textField);
    }
}
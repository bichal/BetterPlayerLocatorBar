package net.bichal.bplb.gui.widget.entries;

import net.bichal.bplb.gui.Config;
import net.bichal.bplb.gui.ConfigScreen;
import net.bichal.bplb.gui.widget.ButtonWidget;
import net.bichal.bplb.gui.widget.ScrollableListWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public class ResetSettingsEntry extends ScrollableListWidget.Entry {
    private final MinecraftClient client;
    private final ButtonWidget resetButton;
    private final ConfigScreen parentScreen;

    public ResetSettingsEntry(MinecraftClient client, ConfigScreen parentScreen, Runnable onDirty, Config workingConfig) {
        this.client = client;
        this.parentScreen = parentScreen;
        this.resetButton = ButtonWidget.builder(Text.translatable("bplb.config.reset_settings"), button -> {
            if (this.client != null) {
                this.client.setScreen(new ConfirmScreen(confirmed -> {
                    if (confirmed) {
                        workingConfig.resetToDefaults();
                        onDirty.run();
                        this.parentScreen.rebuildList();
                        this.client.setScreen(this.parentScreen);
                    } else {
                        this.client.setScreen(this.parentScreen);
                    }
                }, Text.translatable("bplb.config.reset_confirm.title"), Text.translatable("bplb.config.reset_confirm.message")));
            }
        }).dimensions(0, 0, Constants.CONFIG_BUTTON_WIDTH + 20, 20).build();
    }

    @Override
    public void tick() {
        this.resetButton.active = Screen.hasShiftDown();
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        this.resetButton.setX(x + (entryWidth - this.resetButton.getWidth()) / 2);
        this.resetButton.setY(y + 2);
        this.resetButton.render(context, mouseX, mouseY, tickDelta);
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return List.of(this.resetButton);
    }

    @Override
    public List<? extends Element> children() {
        return List.of(this.resetButton);
    }
}

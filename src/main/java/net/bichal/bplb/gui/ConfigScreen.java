package net.bichal.bplb.gui;

import net.bichal.bplb.gui.screen.BaseTabScreen;
import net.bichal.bplb.gui.screen.tabs.DotOptionsTab;
import net.bichal.bplb.gui.screen.tabs.GeneralOptionsTab;
import net.bichal.bplb.gui.screen.tabs.ServerOptionsTab;
import net.bichal.bplb.gui.screen.tabs.WaypointsTab;
import net.bichal.bplb.gui.widget.ButtonWidget;
import net.bichal.bplb.gui.widget.TooltipWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class ConfigScreen extends BaseTabScreen {
    private final Config workingConfig;
    private boolean hasChanges = false;
    private Screen currentTabScreen;
    private ButtonWidget applyButton, doneButton;
    private TooltipWidget tooltipWidget;

    public ConfigScreen(Screen parent) {
        super(Text.translatable(Constants.CONFIG_KEY_PREFIX + "title"), parent);
        this.workingConfig = new Config();
        Config.copy(Config.getInstance(), this.workingConfig);
    }

    @Override
    protected void init() {
        if (this.client == null) return;
        this.tooltipWidget = new TooltipWidget(this.client);

        addTab("general", () -> switchToTab(new GeneralOptionsTab(this, workingConfig)));
        addTab("dots", () -> switchToTab(new DotOptionsTab(this, workingConfig)));

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.getServer() != null && client.getServer().getPlayerManager().isOperator(client.player.getGameProfile())) {
            addTab("server", () -> switchToTab(new ServerOptionsTab(this, workingConfig)));
        }

        addTab("waypoints", () -> switchToTab(new WaypointsTab(this, workingConfig)));
        
        super.init();

        switchToTab(new GeneralOptionsTab(this, workingConfig));
        
        int startX = (int) ((double) this.width / 2 - Constants.CONFIG_BUTTON_WIDTH * 1.5 - Constants.CONFIG_BUTTON_SPACING * 1.5);
        int buttonY = this.height - 27;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), button -> this.close()).dimensions(startX, buttonY, Constants.CONFIG_BUTTON_WIDTH, 20).build());

        this.applyButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.bplb.config.apply"), button -> applyChanges()).dimensions(startX + Constants.CONFIG_BUTTON_WIDTH + Constants.CONFIG_BUTTON_SPACING, buttonY, Constants.CONFIG_BUTTON_WIDTH, 20).build());

        this.doneButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            if (hasChanges) applyChanges();
            this.close();
        }).dimensions(startX + (Constants.CONFIG_BUTTON_WIDTH + Constants.CONFIG_BUTTON_SPACING) * 2, buttonY, Constants.CONFIG_BUTTON_WIDTH, 20).build());

        updateButtons();
    }

    public void showTooltip(String configKey, int y, int entryHeight) {
        if (tooltipWidget != null) {
            Text tooltipText = Text.translatable(Constants.CONFIG_KEY_PREFIX + configKey + ".tooltip");
            int tooltipX = this.width / 2 - 230;
            int tooltipY = y + entryHeight + 2;
            int tooltipWidth = 460;
            tooltipWidget.setHoveredTooltip(tooltipText, tooltipX, tooltipY, tooltipWidth);
        }
    }

    public void clearTooltip() {
        if (tooltipWidget != null) {
            tooltipWidget.clearTooltip();
        }
    }

    private void switchToTab(Screen newTab) {
        if (currentTabScreen != null) {
            this.remove(currentTabScreen);
        }
        currentTabScreen = newTab;
        if (currentTabScreen != null && this.client != null) {
            currentTabScreen.init(this.client, this.width, this.height);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (currentTabScreen != null) currentTabScreen.render(context, mouseX, mouseY, delta);
    }

    public void drawLabelWithHighlight(DrawContext context, Text label, int x, int y, String key) {
        int color = Constants.WHITE_COLOR;
        context.drawTextWithShadow(this.textRenderer, label, x, y, color);
    }

    public void markDirty() {
        hasChanges = true;
        updateButtons();
    }

    private void updateButtons() {
        if (applyButton != null) applyButton.active = hasChanges;
        if (doneButton != null) doneButton.active = !hasChanges;
    }

    private void applyChanges() {
        Config.copy(workingConfig, Config.getInstance());
        Config.getInstance().save();
        hasChanges = false;
        updateButtons();
    }

    public void rebuildList() {
        if (currentTabScreen instanceof GeneralOptionsTab) {
            switchToTab(new GeneralOptionsTab(this, workingConfig));
        } else if (currentTabScreen instanceof DotOptionsTab) {
            switchToTab(new DotOptionsTab(this, workingConfig));
        } else if (currentTabScreen instanceof ServerOptionsTab) {
            switchToTab(new ServerOptionsTab(this, workingConfig));
        } else if (currentTabScreen instanceof WaypointsTab) {
            switchToTab(new WaypointsTab(this, workingConfig));
        }
    }

    public Config getWorkingConfig() {
        return workingConfig;
    }
}
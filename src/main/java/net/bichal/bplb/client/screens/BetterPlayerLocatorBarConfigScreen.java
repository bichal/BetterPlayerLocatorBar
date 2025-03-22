package net.bichal.bplb.client.screens;

import net.bichal.bplb.config.BetterPlayerLocatorBarConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

import java.util.Objects;

public class BetterPlayerLocatorBarConfigScreen extends Screen {
    private final Screen parent;
    private BetterPlayerLocatorBarConfig configCopy;

    public BetterPlayerLocatorBarConfigScreen(Screen parent) {
        super(Text.translatable("screen.bplb.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.configCopy = BetterPlayerLocatorBarConfig.getInstance().copy();

        int buttonWidth = 150;
        int buttonHeight = 20;
        int buttonSpacing = 5;
        int leftColumn = width / 2 - buttonWidth - buttonSpacing;
        int rightColumn = width / 2 + buttonSpacing;
        int startY = 50;
        int rowHeight = 25;

        addDrawableChild(new TextWidget(width / 2, startY - 20, 200, 20,
                Text.translatable("screen.bplb.config.basic_settings"), textRenderer).alignCenter());

        int currentY = startY;

        addSlider("minAlpha", leftColumn, currentY, buttonWidth, buttonHeight, 0.0f, 1.0f, configCopy.getMinAlpha());
        currentY += rowHeight;

        addSlider("maxFadeDistance", leftColumn, currentY, buttonWidth, buttonHeight, 100f, 10000f, configCopy.getMaxFadeDistance());
        currentY += rowHeight;

        addSlider("fadeStartDistance", leftColumn, currentY, buttonWidth, buttonHeight, 10f, 1000f, configCopy.getFadeStartDistance());
        currentY += rowHeight;

        addSlider("lerpSpeed", leftColumn, currentY, buttonWidth, buttonHeight, 0.01f, 1.0f, configCopy.getLerpSpeed());
        currentY += rowHeight;

        addSlider("iconSize", leftColumn, currentY, buttonWidth, buttonHeight, 1, 10, configCopy.getIconSize());

        currentY = startY;

        addSlider("iconOpacity", rightColumn, currentY, buttonWidth, buttonHeight, 0.0f, 1.0f, configCopy.getIconOpacity());
        currentY += rowHeight;

        addSlider("playerHeadSize", rightColumn, currentY, buttonWidth, buttonHeight, 1f, 10f, configCopy.getPlayerHeadSize());
        currentY += rowHeight;

        addSlider("playerHeadOpacity", rightColumn, currentY, buttonWidth, buttonHeight, 0.0f, 1.0f, configCopy.getPlayerHeadOpacity());
        currentY += rowHeight;

        addToggle("alwaysShowPlayerHeads", rightColumn, currentY, buttonWidth, buttonHeight, configCopy.isAlwaysShowPlayerHeads());
        currentY += rowHeight;

        addToggle("alwaysShowPlayerNames", rightColumn, currentY, buttonWidth, buttonHeight, configCopy.isAlwaysShowPlayerNames());

        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.bplb.config.experimental_features"), button -> Objects.requireNonNull(client).setScreen(new BetterPlayerLocatorBarWarningScreen(this))).dimensions(width / 2 - 100, height - 80, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.bplb.config.save"), button -> {
            BetterPlayerLocatorBarConfig.getInstance().copyFrom(configCopy);
            BetterPlayerLocatorBarConfig.getInstance().saveConfig();
            Objects.requireNonNull(client).setScreen(parent);
        }).dimensions(width / 2 - 100, height - 50, 95, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.bplb.config.cancel"), button -> Objects.requireNonNull(client).setScreen(parent)).dimensions(width / 2 + 5, height - 50, 95, 20).build());
    }

    private void addSlider(String settingName, int x, int y, int width, int height, float min, float max, float value) {
        SliderWidget slider = new SliderWidget(
                x, y, width, height,
                Text.translatable("setting.bplb." + settingName, String.format("%.2f", value)),
                (value - min) / (max - min)
        ) {
            @Override
            protected void updateMessage() {
                float value = min + (max - min) * (float) this.value;
                setMessage(Text.translatable("setting.bplb." + settingName, String.format("%.2f", value)));

                switch (settingName) {
                    case "minAlpha" -> configCopy.setMinAlpha(value);
                    case "maxFadeDistance" -> configCopy.setMaxFadeDistance(value);
                    case "fadeStartDistance" -> configCopy.setFadeStartDistance(value);
                    case "lerpSpeed" -> configCopy.setLerpSpeed(value);
                    case "iconSize" -> configCopy.setIconSize((int) value);
                    case "iconOpacity" -> configCopy.setIconOpacity(value);
                    case "playerHeadSize" -> configCopy.setPlayerHeadSize(value);
                    case "playerHeadOpacity" -> configCopy.setPlayerHeadOpacity(value);
                }
            }

            @Override
            protected void applyValue() {
                this.updateMessage();
            }
        };

        addDrawableChild(slider);
    }

    private void addToggle(String settingName, int x, int y, int width, int height, boolean initialValue) {
        ButtonWidget toggle = ButtonWidget.builder(
                Text.translatable("setting.bplb." + settingName)
                        .append(": ")
                        .append(Text.translatable(initialValue ? "options.on" : "options.off")),
                button -> {
                    boolean newValue = false;
                    switch (settingName) {
                        case "alwaysShowPlayerHeads":
                            newValue = !configCopy.isAlwaysShowPlayerHeads();
                            configCopy.setAlwaysShowPlayerHeads(newValue);
                            break;
                        case "alwaysShowPlayerNames":
                            newValue = !configCopy.isAlwaysShowPlayerNames();
                            configCopy.setAlwaysShowPlayerNames(newValue);
                            break;
                    }
                    button.setMessage(Text.translatable("setting.bplb." + settingName)
                            .append(": ")
                            .append(Text.translatable(newValue ? "options.on" : "options.off")));
                }
        ).dimensions(x, y, width, height).build();

        addDrawableChild(toggle);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
    }
}

package net.bichal.bplb.config;

import net.bichal.bplb.config.widget.CustomButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class BetterPlayerLocatorBarConfigScreen extends Screen {
    private final Screen parent;
    private final BetterPlayerLocatorBarConfig config;
    private ScrollableListWidget scrollableList;
    private static final int ITEM_HEIGHT = 24;
    private static final int PADDING = 10;
    private static final int SLIDER_WIDTH = 150;
    private static final int TOGGLE_WIDTH = 80;

    public BetterPlayerLocatorBarConfigScreen(Screen parent) {
        super(Text.translatable("bplb.config.title"));
        this.parent = parent;
        this.config = BetterPlayerLocatorBarConfig.getInstance();
    }

    @Override
    protected void init() {
        this.scrollableList = new ScrollableListWidget(
                this.client,
                this.width,
                32,
                this.height - 32,
                ITEM_HEIGHT
        );

        addSection(Text.translatable("bplb.config.section.general"));
        addSpacing();

        addSliderOption("bplb.config.min_alpha", config.getMinAlpha(), 0.0f, 1.0f, value -> {
            config.setMinAlpha(value);
            return Text.translatable("bplb.config.min_alpha", String.format("%.2f", value));
        });

        addSliderOption("bplb.config.max_fade_distance", config.getMaxFadeDistance(), 10.0f, 200.0f, value -> {
            config.setMaxFadeDistance(value);
            return Text.translatable("bplb.config.max_fade_distance", String.format("%.0f", value));
        });

        addSliderOption("bplb.config.fade_start_distance", config.getFadeStartDistance(), 5.0f, 100.0f, value -> {
            config.setFadeStartDistance(value);
            return Text.translatable("bplb.config.fade_start_distance", String.format("%.0f", value));
        });

        addSliderOption("bplb.config.lerp_speed", config.getLerpSpeed(), 0.01f, 1.0f, value -> {
            config.setLerpSpeed(value);
            return Text.translatable("bplb.config.lerp_speed", String.format("%.2f", value));
        });

        addToggleOption("bplb.config.apply_hotbar_offset", config.isApplyHotbarOffset(), config::setApplyHotbarOffset);

        addToggleOption("bplb.config.always_show_player_heads", config.isAlwaysShowPlayerHeads(), config::setAlwaysShowPlayerHeads);

        addToggleOption("bplb.config.always_show_player_names", config.isAlwaysShowPlayerNames(), config::setAlwaysShowPlayerNames);

        addToggleOption("bplb.config.toggle_tab", config.isToggleTab(), config::setToggleTab);

        addSliderOption("bplb.config.fade_alpha_max", config.getFadeAlphaMax(), 0.1f, 1.0f, value -> {
            config.setFadeAlphaMax(value);
            return Text.translatable("bplb.config.fade_alpha_max", String.format("%.2f", value));
        });

        addSliderOption("bplb.config.fade_alpha_min", config.getFadeAlphaMin(), 0.0f, 0.5f, value -> {
            config.setFadeAlphaMin(value);
            return Text.translatable("bplb.config.fade_alpha_min", String.format("%.2f", value));
        });

        addSliderOption("bplb.config.fade_scale_max", config.getFadeScaleMax(), 0.5f, 1.0f, value -> {
            config.setFadeScaleMax(value);
            return Text.translatable("bplb.config.fade_scale_max", String.format("%.2f", value));
        });

        addSliderOption("bplb.config.fade_scale_min", config.getFadeScaleMin(), 0.25f, 0.75f, value -> {
            config.setFadeScaleMin(value);
            return Text.translatable("bplb.config.fade_scale_min", String.format("%.2f", value));
        });

        addSection(Text.translatable("bplb.config.section.icon"));
        addSpacing();

        addSliderOption("bplb.config.icon_size", config.getIconSize(), 3.0f, 10.0f, value -> {
            config.setIconSize((int) value);
            return Text.translatable("bplb.config.icon_size", String.format("%d", (int) value));
        });

        addSliderOption("bplb.config.icon_opacity", config.getIconOpacity(), 0.0f, 1.0f, value -> {
            config.setIconOpacity(value);
            return Text.translatable("bplb.config.icon_opacity", String.format("%.2f", value));
        });

        addBorderStyleOption("bplb.config.icon_border_style", config.getIconBorderStyle(), config::setIconBorderStyle);

        addSection(Text.translatable("bplb.config.section.player_head"));
        addSpacing();

        addSliderOption("bplb.config.head_size", config.getHeadSize(), 3.0f, 10.0f, value -> {
            config.setHeadSize((int) value);
            return Text.translatable("bplb.config.head_size", String.format("%d", (int) value));
        });

        addSliderOption("bplb.config.head_opacity", config.getHeadOpacity(), 0.0f, 1.0f, value -> {
            config.setHeadOpacity(value);
            return Text.translatable("bplb.config.head_opacity", String.format("%.2f", value));
        });

        addBorderStyleOption("bplb.config.head_border_style", config.getHeadBorderStyle(), config::setHeadBorderStyle);

        addToggleOption("bplb.config.inherit_border_color", config.isInheritBorderColor(), config::setInheritBorderColor);

        addSection(Text.translatable("bplb.config.section.player_name"));
        addSpacing();

        addBorderStyleOption("bplb.config.name_border_style", config.getNameBorderStyle(), config::setNameBorderStyle);

        this.addDrawableChild(this.scrollableList);

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).dimensions(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    private void addSpacing() {
        this.scrollableList.addPublicEntry(new SpacingHeaderEntry());
    }

    private void addSection(Text title) {
        this.scrollableList.addPublicEntry(new SectionHeaderEntry(title));
    }

    private void addSliderOption(String key, float initialValue, float min, float max, SliderTextProvider textProvider) {
        this.scrollableList.addPublicEntry(new SliderOptionEntry(key, initialValue, min, max, textProvider));
    }

    private void addToggleOption(String key, boolean initialValue, ToggleValueConsumer valueConsumer) {
        this.scrollableList.addPublicEntry(new ToggleOptionEntry(key, initialValue, valueConsumer));
    }

    private void addBorderStyleOption(String key, String initialValue, Consumer<String> valueConsumer) {
        this.scrollableList.addPublicEntry(new BorderStyleOptionEntry(Text.translatable(key), initialValue, valueConsumer));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void close() {
        config.save();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    private interface SliderTextProvider {
        Text getText(float value);
    }

    private interface ToggleValueConsumer {
        void accept(boolean value);
    }

    private static class SpacingHeaderEntry extends ScrollableListWidget.Entry {
        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.fill(x + PADDING, y + 9, x + entryWidth - PADDING, y + 10, 0x30FFFFFF);
            context.fill(x + PADDING, y + 8, x + entryWidth - PADDING, y + 9, 0x30000000);
        }
    }

    private class SectionHeaderEntry extends ScrollableListWidget.Entry {
        private final Text text;

        public SectionHeaderEntry(Text text) {
            this.text = text;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawCenteredTextWithShadow(textRenderer, text, x + entryWidth / 2, y + 8, 0xFFFFFF);
        }
    }

    private class SliderOptionEntry extends ScrollableListWidget.Entry {
        private final Text label;
        private final SliderWidget slider;

        public SliderOptionEntry(String key, float initialValue, float min, float max, SliderTextProvider textProvider) {
            this.label = Text.translatable(key);

            float normalizedValue = MathHelper.clamp((initialValue - min) / (max - min), 0.0f, 1.0f);

            this.slider = new SliderWidget(0, 0, SLIDER_WIDTH, 20, textProvider.getText(initialValue), normalizedValue) {
                @Override
                protected void updateMessage() {
                    float value = min + (max - min) * (float) this.value;
                    this.setMessage(textProvider.getText(value));
                }

                @Override
                protected void applyValue() {
                    float value = min + (max - min) * (float) this.value;
                    textProvider.getText(value);
                }
            };
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawTextWithShadow(textRenderer, label, x + PADDING, y + 6, 0xFFFFFF);

            slider.setX(x + entryWidth - SLIDER_WIDTH - PADDING);
            slider.setY(y + 2);
            slider.render(context, mouseX, mouseY, tickDelta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return slider.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            return slider.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return slider.mouseReleased(mouseX, mouseY, button);
        }
    }

    private class BorderStyleOptionEntry extends ScrollableListWidget.Entry {
        private final Text label;
        private final CustomButtonWidget button;
        private String value;

        public BorderStyleOptionEntry(Text label, String initialValue, Consumer<String> valueConsumer) {
            this.label = label;
            this.value = initialValue;

            this.button = CustomButtonWidget.builder(Text.translatable("bplb.config.border_style." + value), button -> {
                this.value = this.value.equals("rounded") ? "squared" : "rounded";
                button.setMessage(Text.translatable("bplb.config.border_style." + value));
                valueConsumer.accept(this.value);
            }).dimensions(0, 0, TOGGLE_WIDTH, 20).build();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawTextWithShadow(textRenderer, label, x + PADDING, y + 6, 0xFFFFFF);

            button.setX(x + entryWidth - TOGGLE_WIDTH - PADDING);
            button.setY(y + 2);
            button.render(context, mouseX, mouseY, tickDelta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.button.mouseClicked(mouseX, mouseY, button);
        }
    }

    private class ToggleOptionEntry extends ScrollableListWidget.Entry {
        private final Text label;
        private final CustomToggleButtonWidget toggleButton;
        private boolean value;

        public ToggleOptionEntry(String key, boolean initialValue, ToggleValueConsumer valueConsumer) {
            this.label = Text.translatable(key);
            this.value = initialValue;

            this.toggleButton = new CustomToggleButtonWidget(0, 0, TOGGLE_WIDTH, 20, initialValue, button -> {
                value = !value;
                valueConsumer.accept(value);
            });
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawTextWithShadow(textRenderer, label, x + PADDING, y + 6, 0xFFFFFF);

            toggleButton.setX(x + entryWidth - TOGGLE_WIDTH - PADDING);
            toggleButton.setY(y + 2);

            context.drawTextWithShadow(textRenderer, value ? Text.translatable("gui.yes") : Text.translatable("gui.no"),
                    toggleButton.getX() + toggleButton.getWidth() / 2 - textRenderer.getWidth(value ? Text.translatable("gui.yes") : Text.translatable("gui.no")) / 2,
                    toggleButton.getY() + 6, 0xFFFFFF);

            toggleButton.render(context, mouseX, mouseY, tickDelta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return toggleButton.mouseClicked(mouseX, mouseY, button);
        }
    }

    private static class CustomToggleButtonWidget extends CustomButtonWidget {
        private boolean isToggled;

        public CustomToggleButtonWidget(int x, int y, int width, int height, boolean initialValue, CustomButtonWidget.PressAction onPress) {
            super(x, y, width, height, Text.empty(), onPress, CustomButtonWidget.DEFAULT_NARRATION_SUPPLIER);
            this.isToggled = initialValue;
        }

        public void toggle() {
            this.isToggled = !this.isToggled;
        }

        @Override
        public void onPress() {
            super.onPress();
            toggle();
        }
    }
}

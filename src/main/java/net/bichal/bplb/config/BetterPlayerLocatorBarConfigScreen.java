package net.bichal.bplb.config;

import net.bichal.bplb.config.widget.CustomButtonWidget;
import net.bichal.bplb.config.widget.CustomSliderWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class BetterPlayerLocatorBarConfigScreen extends Screen {
    private final Screen parent;
    private final BetterPlayerLocatorBarConfig config;
    private ScrollableListWidget scrollableList;
    private boolean hasChanges = false;
    private static final int ITEM_HEIGHT = 24;
    private static final int PADDING = 10;
    private static final int SLIDER_WIDTH = 80;
    private static final int TOGGLE_WIDTH = 80;

    public BetterPlayerLocatorBarConfigScreen(Screen parent) {
        super(Text.translatable("bplb.config.title"));
        this.parent = parent;
        this.config = BetterPlayerLocatorBarConfig.getInstance();
    }

    @Override
    protected void init() {
        this.scrollableList = new ScrollableListWidget(this.client, this.width, 35, this.height - 35, ITEM_HEIGHT);

        int buttonWidth = 100;
        int buttonSpacing = 5;
        int totalWidth = buttonWidth * 3 + buttonSpacing * 2;
        int startX = this.width / 2 - totalWidth / 2;
        int buttonY = this.height - 27;

        addSection(Text.translatable("bplb.config.section.general"));

        addSliderOption("bplb.config.min_alpha", config.getMinAlpha(), 0.0f, 1.0f, config::setMinAlpha);

        addSliderOption("bplb.config.max_fade_distance", config.getMaxFadeDistance(), 10.0f, 200.0f, config::setMaxFadeDistance);

        addSliderOption("bplb.config.fade_start_distance", config.getFadeStartDistance(), 5.0f, 100.0f, config::setFadeStartDistance);

        addSliderOption("bplb.config.lerp_speed", config.getLerpSpeed(), 0.01f, 1.0f, config::setLerpSpeed);

        addToggleOption("bplb.config.apply_hotbar_offset", config.isApplyHotbarOffset(), config::setApplyHotbarOffset);

        addToggleOption("bplb.config.always_show_player_heads", config.isAlwaysShowPlayerHeads(), config::setAlwaysShowPlayerHeads);

        addToggleOption("bplb.config.always_show_player_names", config.isAlwaysShowPlayerNames(), config::setAlwaysShowPlayerNames);

        addToggleOption("bplb.config.toggle_tab", config.isToggleTab(), config::setToggleTab);

        addSliderOption("bplb.config.fade_alpha_max", config.getFadeAlphaMax(), 0.1f, 1.0f, config::setFadeAlphaMax);

        addSliderOption("bplb.config.fade_alpha_min", config.getFadeAlphaMin(), 0.0f, 0.5f, config::setFadeAlphaMin);

        addSliderOption("bplb.config.fade_scale_max", config.getFadeScaleMax(), 0.5f, 1.0f, config::setFadeScaleMax);

        addSliderOption("bplb.config.fade_scale_min", config.getFadeScaleMin(), 0.25f, 0.75f, config::setFadeScaleMin);

        addSection(Text.translatable("bplb.config.section.icon"));

        addSliderOption("bplb.config.icon_size", config.getIconSize(), 3.0f, 10.0f, value -> config.setIconSize((int) value));

        addSliderOption("bplb.config.icon_opacity", config.getIconOpacity(), 0.0f, 1.0f, config::setIconOpacity);

        addBorderStyleOption("bplb.config.icon_border_style", config.getIconBorderStyle(), config::setIconBorderStyle);

        addSection(Text.translatable("bplb.config.section.player_head"));

        addSliderOption("bplb.config.head_size", config.getHeadSize(), 3.0f, 10.0f, value -> config.setHeadSize((int) value));

        addSliderOption("bplb.config.head_opacity", config.getHeadOpacity(), 0.0f, 1.0f, config::setHeadOpacity);

        addBorderStyleOption("bplb.config.head_border_style", config.getHeadBorderStyle(), config::setHeadBorderStyle);

        addToggleOption("bplb.config.inherit_border_color", config.isInheritBorderColor(), config::setInheritBorderColor);

        addSection(Text.translatable("bplb.config.section.player_name"));

        addBorderStyleOption("bplb.config.name_border_style", config.getNameBorderStyle(), config::setNameBorderStyle);

        addSection(Text.translatable("bplb.config.section.visuals"));

        addSliderOption("bplb.config.max_visible_icons", config.getMaxVisibleIcons(), 1, 45, value -> {
            config.setMaxVisibleIcons((int) value);
            markDirty();
        });

        addCycleOption("bplb.config.icon_type", config.getIconType(), Arrays.asList("Normal", "Mojang", "Bare Bones"), value -> {
            config.setIconType(value);
            markDirty();
        });

        addCycleOption("bplb.config.arrow_type", config.getArrowType(), Arrays.asList("Normal", "Mojang", "Bare Bones"), value -> {
            config.setArrowType(value);
            markDirty();
        });

        addCycleOption("bplb.config.bar_type", config.getBarType(), Arrays.asList("Normal", "Mojang", "Bare Bones"), value -> {
            config.setBarType(value);
            markDirty();
        });

        this.addDrawableChild(CustomButtonWidget.builder(Text.literal("↷"), button -> {
            config.resetToDefaults();
            markDirty();
        }).dimensions(this.width - 40 - buttonSpacing * 2, 5, 20, 20).build());

        this.addDrawableChild(CustomButtonWidget.builder(Text.literal(config.isModEnabled() ? "✕" : "✔"), button -> {
            config.setModEnabled(!config.isModEnabled());
            button.setMessage(Text.translatable(config.isModEnabled() ? "✕" : "✔"));
            markDirty();
        }).dimensions(this.width - 20 - buttonSpacing, 5, 20, 20).build());

        this.addDrawableChild(this.scrollableList);

        this.addDrawableChild(CustomButtonWidget.builder(Text.translatable("gui.cancel"), button -> this.closeWithoutSaving()).dimensions(startX, buttonY, buttonWidth, 20).build());

        this.addDrawableChild(CustomButtonWidget.builder(Text.translatable("screen.bplb.config.apply"), button -> {
            config.save();
            hasChanges = false;
            updateButtons();
        }).dimensions(startX + buttonWidth + buttonSpacing, buttonY, buttonWidth, 20).build());

        this.addDrawableChild(CustomButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).dimensions(startX + (buttonWidth + buttonSpacing) * 2, buttonY, buttonWidth, 20).build());
    }

    private void closeWithoutSaving() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    private void markDirty() {
        hasChanges = true;
    }

    private void updateButtons() {
    }

    private void addSection(Text title) {
        this.scrollableList.addPublicEntry(new SectionHeaderEntry(title));
    }

    private void addCycleOption(String key, String initialValue, List<String> options, Consumer<String> valueConsumer) {
        this.scrollableList.addPublicEntry(new CycleOptionEntry(Text.translatable(key), initialValue, options, valueConsumer));
    }

    private void addSliderOption(String key, float initialValue, float min, float max, SliderValueConsumer valueConsumer) {
        this.scrollableList.addPublicEntry(new SliderOptionEntry(key, initialValue, min, max, valueConsumer));
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
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    private interface SliderValueConsumer {
        void accept(float value);
    }

    private interface ToggleValueConsumer {
        void accept(boolean value);
    }

    private class SectionHeaderEntry extends ScrollableListWidget.Entry {
        private final Text text;

        public SectionHeaderEntry(Text text) {
            this.text = text;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawCenteredTextWithShadow(textRenderer, text, x + entryWidth / 2, y + 6, 0xFFFFFF);

            int textWidth = text.getString().length() * 2 + 10;

            context.fill(x + PADDING, y + 9, x + entryWidth / 2 - textWidth - 10, y + 10, 0x30FFFFFF);
            context.fill(x + PADDING, y + 10, x + entryWidth / 2 - textWidth - 10, y + 11, 0x40000000);
            context.fill(x + entryWidth / 2 - textWidth - 8, y + 7, x + entryWidth / 2 - textWidth - 7, y + 13, 0x30FFFFFF);

            context.fill(x + entryWidth / 2 + textWidth + 10, y + 9, x + entryWidth - PADDING, y + 10, 0x30FFFFFF);
            context.fill(x + entryWidth / 2 + textWidth + 10, y + 10, x + entryWidth - PADDING, y + 11, 0x40000000);
            context.fill(x + entryWidth / 2 + textWidth + 8, y + 7, x + entryWidth / 2 + textWidth + 7, y + 13, 0x30FFFFFF);
        }
    }

    private class SliderOptionEntry extends ScrollableListWidget.Entry {
        private final Text label;
        private final CustomSliderWidget slider;
        private final float min;
        private final float max;
        private static final int VALUE_SLIDER_SPACING = 5;
        private final String format;

        public SliderOptionEntry(String key, float initialValue, float min, float max, SliderValueConsumer valueConsumer) {
            this.label = Text.translatable(key);
            this.min = min;
            this.max = max;
            this.format = (min == (int) min && max == (int) max) ? "%.0f" : "%.2f";

            float normalizedValue = MathHelper.clamp((initialValue - min) / (max - min), 0.0f, 1.0f);

            this.slider = new CustomSliderWidget(50, 0, SLIDER_WIDTH, 20, Text.empty(), normalizedValue) {
                @Override
                protected void updateMessage() {
                }

                @Override
                protected void applyValue() {
                    float value = min + (max - min) * (float) this.value;
                    valueConsumer.accept(value);
                }
            };
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawTextWithShadow(textRenderer, label, x + PADDING, y + 6, 0xFFFFFF);

            float currentValue = min + (max - min) * (float) slider.value;
            Text valueText = Text.literal(String.format(format, currentValue));

            int valueX = x + entryWidth - SLIDER_WIDTH - textRenderer.getWidth(valueText) - VALUE_SLIDER_SPACING - PADDING;
            context.drawTextWithShadow(textRenderer, valueText, valueX, y + 6, 0xFFFFFF);

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

    private class CycleOptionEntry extends ScrollableListWidget.Entry {
        private final Text label;
        private final CustomButtonWidget button;
        private String value;

        public CycleOptionEntry(Text label, String initialValue, List<String> options, Consumer<String> valueConsumer) {
            this.label = label;
            this.value = initialValue;

            this.button = CustomButtonWidget.builder(Text.translatable("bplb.config." + value), button -> {
                int currentIndex = options.indexOf(value);
                int nextIndex = (currentIndex + 1) % options.size();
                this.value = options.get(nextIndex);
                button.setMessage(Text.translatable("bplb.config." + value));
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
        private final CustomButtonWidget toggleButton;
        private boolean value;

        public ToggleOptionEntry(String key, boolean initialValue, ToggleValueConsumer valueConsumer) {
            this.label = Text.translatable(key);
            this.value = initialValue;

            this.toggleButton = CustomButtonWidget.builder(Text.empty(), button -> {
                value = !value;
                valueConsumer.accept(value);
            }).dimensions(0, 0, TOGGLE_WIDTH, 20).build();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawTextWithShadow(textRenderer, label, x + PADDING, y + 6, 0xFFFFFF);

            toggleButton.setX(x + entryWidth - TOGGLE_WIDTH - PADDING);
            toggleButton.setY(y + 2);
            toggleButton.render(context, mouseX, mouseY, tickDelta);

            Text toggleText = value ? Text.translatable("gui.yes") : Text.translatable("gui.no");
            int color = value ? 0x55FF55 : 0xFF5555;

            context.drawTextWithShadow(textRenderer, toggleText, toggleButton.getX() + toggleButton.getWidth() / 2 - textRenderer.getWidth(toggleText) / 2, toggleButton.getY() + 6, color);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return toggleButton.mouseClicked(mouseX, mouseY, button);
        }
    }
}

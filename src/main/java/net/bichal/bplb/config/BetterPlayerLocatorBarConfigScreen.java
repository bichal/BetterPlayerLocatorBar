package net.bichal.bplb.config;

import net.bichal.bplb.config.widget.CustomButtonWidget;
import net.bichal.bplb.config.widget.CustomSliderWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class BetterPlayerLocatorBarConfigScreen extends Screen {
    private final Screen parent;
    private final BetterPlayerLocatorBarConfig originalConfig;
    private final BetterPlayerLocatorBarConfig workingConfig;
    private ScrollableListWidget scrollableList;
    private boolean hasChanges = false;
    private static final int ITEM_HEIGHT = 24;
    private static final int PADDING = 10;
    private static final int SLIDER_WIDTH = 80;
    private static final int TOGGLE_WIDTH = 80;
    private CustomButtonWidget applyButton;
    private CustomButtonWidget doneButton;
    private ToggleOptionEntry alwaysShowPlayerHeadsEntry;
    private ToggleOptionEntry alwaysShowPlayerNamesEntry;

    public BetterPlayerLocatorBarConfigScreen(Screen parent) {
        super(Text.translatable("bplb.config.title"));
        this.parent = parent;
        this.originalConfig = BetterPlayerLocatorBarConfig.getInstance();
        this.workingConfig = new BetterPlayerLocatorBarConfig();
        copyConfig(originalConfig, workingConfig);
    }

    private void copyConfig(BetterPlayerLocatorBarConfig source, BetterPlayerLocatorBarConfig target) {
        target.setMinAlpha(source.getMinAlpha());
        target.setMaxFadeDistance(source.getMaxFadeDistance());
        target.setFadeStartDistance(source.getFadeStartDistance());
        target.setLerpSpeed(source.getLerpSpeed());
        target.setApplyHotbarOffset(source.isApplyHotbarOffset());
        target.setAlwaysShowPlayerHeads(source.isAlwaysShowPlayerHeads());
        target.setAlwaysShowPlayerNames(source.isAlwaysShowPlayerNames());
        target.setToggleTab(source.isToggleTab());
        target.setFadeAlphaMax(source.getFadeAlphaMax());
        target.setFadeAlphaMin(source.getFadeAlphaMin());
        target.setFadeScaleMax(source.getFadeScaleMax());
        target.setFadeScaleMin(source.getFadeScaleMin());
        target.setIconSize(source.getIconSize());
        target.setIconOpacity(source.getIconOpacity());
        target.setHeadSize(source.getHeadSize());
        target.setHeadOpacity(source.getHeadOpacity());
        target.setInheritBorderColor(source.isInheritBorderColor());
        target.setNameBorderStyle(source.getNameBorderStyle());
        target.setIconBorderStyle(source.getIconBorderStyle());
        target.setHeadBorderStyle(source.getHeadBorderStyle());
        target.setMaxVisibleIcons(source.getMaxVisibleIcons());
        target.setIconType(source.getIconType());
        target.setArrowType(source.getArrowType());
        target.setBarType(source.getBarType());
        target.setModEnabled(source.isModEnabled());
    }

    @Override
    protected void init() {
        this.scrollableList = new ScrollableListWidget(this.client, this.width, 35, this.height - 35, ITEM_HEIGHT);

        int buttonWidth = 100;
        int buttonSpacing = 5;
        int totalWidth = buttonWidth * 3 + buttonSpacing * 2;
        int startX = this.width / 2 - totalWidth / 2;
        int buttonY = this.height - 27;

        addText(Text.translatable("bplb.config.welcome", "Better Player Locator Bar"), Text.translatable("bplb.config.introduction"), Text.translatable("bplb.config.footer"), 8, 16, 0x76AF83);
        addSection(Text.translatable("bplb.config.section.general"));
        addSliderOption("bplb.config.min_alpha", workingConfig.getMinAlpha(), 0.0f, 1.0f, value -> {
            workingConfig.setMinAlpha(value);
            markDirty();
        });
        addSliderOption("bplb.config.max_fade_distance", workingConfig.getMaxFadeDistance(), 10.0f, 200.0f, value -> {
            workingConfig.setMaxFadeDistance(value);
            markDirty();
        });
        addSliderOption("bplb.config.fade_start_distance", workingConfig.getFadeStartDistance(), 5.0f, 100.0f, value -> {
            workingConfig.setFadeStartDistance(value);
            markDirty();
        });
        addSliderOption("bplb.config.lerp_speed", workingConfig.getLerpSpeed(), 0.01f, 1.0f, value -> {
            workingConfig.setLerpSpeed(value);
            markDirty();
        });
        addToggleOption("bplb.config.apply_hotbar_offset", workingConfig.isApplyHotbarOffset(), value -> {
            workingConfig.setApplyHotbarOffset(value);
            markDirty();
        });
        addToggleOption("bplb.config.toggle_tab", workingConfig.isToggleTab(), value -> {
            workingConfig.setToggleTab(value);
            updateToggleDependencies();
            markDirty();
        });
        alwaysShowPlayerHeadsEntry = addToggleOption("bplb.config.always_show_player_heads", workingConfig.isAlwaysShowPlayerHeads(), value -> {
            workingConfig.setAlwaysShowPlayerHeads(value);
            markDirty();
        });
        alwaysShowPlayerNamesEntry = addToggleOption("bplb.config.always_show_player_names", workingConfig.isAlwaysShowPlayerNames(), value -> {
            workingConfig.setAlwaysShowPlayerNames(value);
            markDirty();
        });
        addSliderOption("bplb.config.fade_alpha_max", workingConfig.getFadeAlphaMax(), 0.1f, 1.0f, value -> {
            workingConfig.setFadeAlphaMax(value);
            markDirty();
        });
        addSliderOption("bplb.config.fade_alpha_min", workingConfig.getFadeAlphaMin(), 0.0f, 0.5f, value -> {
            workingConfig.setFadeAlphaMin(value);
            markDirty();
        });
        addSliderOption("bplb.config.fade_scale_max", workingConfig.getFadeScaleMax(), 0.5f, 1.0f, value -> {
            workingConfig.setFadeScaleMax(value);
            markDirty();
        });
        addSliderOption("bplb.config.fade_scale_min", workingConfig.getFadeScaleMin(), 0.25f, 0.75f, value -> {
            workingConfig.setFadeScaleMin(value);
            markDirty();
        });
        addSliderOption("bplb.config.max_visible_icons", workingConfig.getMaxVisibleIcons(), 1, 45, value -> {
            workingConfig.setMaxVisibleIcons((int) value);
            markDirty();
        });

        addSection(Text.translatable("bplb.config.section.icon"));
        addSliderOption("bplb.config.icon_size", workingConfig.getIconSize(), 3.0f, 10.0f, value -> {
            workingConfig.setIconSize((int) value);
            markDirty();
        });
        addSliderOption("bplb.config.icon_opacity", workingConfig.getIconOpacity(), 0.0f, 1.0f, value -> {
            workingConfig.setIconOpacity(value);
            markDirty();
        });
        addBorderStyleOption("bplb.config.icon_border_style", workingConfig.getIconBorderStyle(), value -> {
            workingConfig.setIconBorderStyle(value);
            markDirty();
        });

        addSection(Text.translatable("bplb.config.section.player_head"));
        addSliderOption("bplb.config.head_size", workingConfig.getHeadSize(), 3.0f, 10.0f, value -> {
            workingConfig.setHeadSize((int) value);
            markDirty();
        });
        addSliderOption("bplb.config.head_opacity", workingConfig.getHeadOpacity(), 0.0f, 1.0f, value -> {
            workingConfig.setHeadOpacity(value);
            markDirty();
        });
        addBorderStyleOption("bplb.config.head_border_style", workingConfig.getHeadBorderStyle(), value -> {
            workingConfig.setHeadBorderStyle(value);
            markDirty();
        });
        addToggleOption("bplb.config.inherit_border_color", workingConfig.isInheritBorderColor(), value -> {
            workingConfig.setInheritBorderColor(value);
            markDirty();
        });

        addSection(Text.translatable("bplb.config.section.player_name"));
        addBorderStyleOption("bplb.config.name_border_style", workingConfig.getNameBorderStyle(), value -> {
            workingConfig.setNameBorderStyle(value);
            markDirty();
        });

        addText(Text.empty(), Text.translatable("bplb.config.reset_info"), Text.literal("./config/Better Player Locator Bar/options.json"), 0, 8, 0xAAAAAA);

        this.addDrawableChild(this.scrollableList);
        this.addDrawableChild(CustomButtonWidget.builder(Text.translatable("gui.cancel"), button -> this.closeWithoutSaving()).dimensions(startX, buttonY, buttonWidth, 20).build());

        applyButton = CustomButtonWidget.builder(Text.translatable("screen.bplb.config.apply"), button -> {
            applyChanges();
            updateButtons();
        }).dimensions(startX + buttonWidth + buttonSpacing, buttonY, buttonWidth, 20).build();

        doneButton = CustomButtonWidget.builder(ScreenTexts.DONE, button -> {
            if (hasChanges) {
                applyChanges();
            }
            this.close();
        }).dimensions(startX + (buttonWidth + buttonSpacing) * 2, buttonY, buttonWidth, 20).build();

        this.addDrawableChild(applyButton);
        this.addDrawableChild(doneButton);
        updateButtons();
        updateToggleDependencies();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    private void applyChanges() {
        copyConfig(workingConfig, originalConfig);
        originalConfig.save();
        hasChanges = false;
        updateButtons();
    }

    private void closeWithoutSaving() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    private void markDirty() {
        hasChanges = true;
        updateButtons();
    }

    private void updateButtons() {
        applyButton.active = hasChanges;
        doneButton.active = !hasChanges;
    }

    private void updateToggleDependencies() {
        boolean toggleTabActive = workingConfig.isToggleTab();

        alwaysShowPlayerHeadsEntry.setActive(!toggleTabActive);
        alwaysShowPlayerNamesEntry.setActive(!toggleTabActive);

        if (toggleTabActive) {
            workingConfig.setAlwaysShowPlayerHeads(false);
            workingConfig.setAlwaysShowPlayerNames(false);
            markDirty();
        }
    }

    private void addSection(Text title) {
        this.scrollableList.addPublicEntry(new SectionHeaderEntry(title));
    }

    private void addText(Text textTop, Text textMiddle, Text textBottom, int yMiddleOffset, int yBottomOffset, int color) {
        this.scrollableList.addPublicEntry(new TextEntry(textTop, textMiddle, textBottom, 0, yMiddleOffset, yBottomOffset, color));
    }

    private void addSliderOption(String key, float initialValue, float min, float max, SliderValueConsumer valueConsumer) {
        this.scrollableList.addPublicEntry(new SliderOptionEntry(key, initialValue, min, max, valueConsumer));
    }

    private ToggleOptionEntry addToggleOption(String key, boolean initialValue, ToggleValueConsumer valueConsumer) {
        ToggleOptionEntry entry = new ToggleOptionEntry(key, initialValue, valueConsumer);
        this.scrollableList.addPublicEntry(entry);
        return entry;
    }

    private void addBorderStyleOption(String key, String initialValue, Consumer<String> valueConsumer) {
        this.scrollableList.addPublicEntry(new BorderStyleOptionEntry(Text.translatable(key), initialValue, valueConsumer));
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
        private final Text title;

        public SectionHeaderEntry(Text title) {
            this.title = title;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawCenteredTextWithShadow(textRenderer, title, x + entryWidth / 2, y + 6, 0xFFFFFF);
            int textWidth = title.getString().length() * 2 + 10;
            context.fill(x + PADDING, y + 9, x + entryWidth / 2 - textWidth - 10, y + 10, 0x30FFFFFF);
            context.fill(x + PADDING, y + 10, x + entryWidth / 2 - textWidth - 10, y + 11, 0x40000000);
            context.fill(x + entryWidth / 2 - textWidth - 8, y + 7, x + entryWidth / 2 - textWidth - 7, y + 13, 0x30FFFFFF);
            context.fill(x + entryWidth / 2 + textWidth + 10, y + 9, x + entryWidth - PADDING, y + 10, 0x30FFFFFF);
            context.fill(x + entryWidth / 2 + textWidth + 10, y + 10, x + entryWidth - PADDING, y + 11, 0x40000000);
            context.fill(x + entryWidth / 2 + textWidth + 8, y + 7, x + entryWidth / 2 + textWidth + 7, y + 13, 0x30FFFFFF);
        }
    }

    private class TextEntry extends ScrollableListWidget.Entry {
        private final Text textTop;
        private final Text textMiddle;
        private final Text textBottom;
        private final int yTopOffset;
        private final int yMiddleOffset;
        private final int yBottomOffset;
        private final int color;

        public TextEntry(Text textTop, Text textMiddle, Text textBottom, int yTopOffset, int yMiddleOffset, int yBottomOffset, int color) {
            this.textTop = textTop;
            this.textMiddle = textMiddle;
            this.textBottom = textBottom;
            this.yTopOffset = yTopOffset;
            this.yMiddleOffset = yMiddleOffset;
            this.yBottomOffset = yBottomOffset;
            this.color = color;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawCenteredTextWithShadow(textRenderer, textTop, x + entryWidth / 2, y + yTopOffset, color);
            context.drawCenteredTextWithShadow(textRenderer, textMiddle, x + entryWidth / 2, y + yMiddleOffset, color);
            context.drawCenteredTextWithShadow(textRenderer, textBottom, x + entryWidth / 2, y + yBottomOffset, color);
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

        public void setActive(boolean active) {
            this.toggleButton.active = active;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawTextWithShadow(textRenderer, label, x + PADDING, y + 6, 0xFFFFFF);
            toggleButton.setX(x + entryWidth - TOGGLE_WIDTH - PADDING);
            toggleButton.setY(y + 2);
            toggleButton.render(context, mouseX, mouseY, tickDelta);
            Text toggleText = value ? Text.translatable("gui.yes") : Text.translatable("gui.no");
            int color = toggleButton.active ? (value ? 0x55FF55 : 0xFF5555) : 0xAAAAAA;
            context.drawTextWithShadow(textRenderer, toggleText, toggleButton.getX() + toggleButton.getWidth() / 2 - textRenderer.getWidth(toggleText) / 2, toggleButton.getY() + 6, color);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return toggleButton.mouseClicked(mouseX, mouseY, button);
        }
    }
}

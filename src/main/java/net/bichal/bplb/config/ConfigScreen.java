package net.bichal.bplb.config;

import net.bichal.bplb.client.Client;
import net.bichal.bplb.client.render.RenderUtils;
import net.bichal.bplb.config.widget.CustomButtonWidget;
import net.bichal.bplb.config.widget.ScrollableListWidget;
import net.bichal.bplb.config.widget.entries.*;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConfigScreen extends Screen {
    private static final int PREVIEW_BASE_SIZE = 40;
    private final Screen parent;
    private final Config workingConfig;
    private ScrollableListWidget scrollableList;
    private boolean hasChanges = false;
    private CustomButtonWidget applyButton, doneButton;

    public ConfigScreen(Screen parent) {
        super(Text.translatable(Constants.CONFIG_KEY_PREFIX + "title"));
        this.parent = parent;
        this.workingConfig = new Config();
        Config.copy(Config.getInstance(), this.workingConfig);
    }

    public void rebuildList() {
        if (this.client != null) {
            double scroll = this.scrollableList.getScrollAmount();
            this.init(this.client, this.width, this.height);
            this.scrollableList.setScrollAmount(scroll);
        }
    }

    @Override protected void init() {
        super.init();
        int uiWidth = Math.min(this.width - (10 * 5) * 2 - 25 * 2, 900);
        this.scrollableList = new ScrollableListWidget(this.client, this.width, 35, this.height - 35, 24);
        this.scrollableList.setRowWidth(uiWidth);
        this.scrollableList.setRowLeft(25);
        this.scrollableList.setScrollbarX(this.width - 6);
        populateOptions();
        this.addDrawableChild(this.scrollableList);
        int startX = (int) ((double) this.width / 2 - Constants.CONFIG_BUTTON_WIDTH * 1.5 - Constants.CONFIG_BUTTON_SPACING * 1.5);
        int buttonY = this.height - 27;
        this.addDrawableChild(CustomButtonWidget.builder(Text.translatable("gui.cancel"), button -> this.close()).dimensions(startX, buttonY, Constants.CONFIG_BUTTON_WIDTH, 20).build());
        this.applyButton = this.addDrawableChild(CustomButtonWidget.builder(Text.translatable("screen.bplb.config.apply"), button -> applyChanges()).dimensions(startX + Constants.CONFIG_BUTTON_WIDTH + Constants.CONFIG_BUTTON_SPACING, buttonY, Constants.CONFIG_BUTTON_WIDTH, 20).build());
        this.doneButton = this.addDrawableChild(CustomButtonWidget.builder(ScreenTexts.DONE, button -> {
            if (hasChanges) applyChanges();
            this.close();
        }).dimensions(startX + (Constants.CONFIG_BUTTON_WIDTH + Constants.CONFIG_BUTTON_SPACING) * 2, buttonY, Constants.CONFIG_BUTTON_WIDTH, 20).build());
        updateButtons();
    }

    private void addSection(String key) {
        scrollableList.addPublicEntry(new SectionHeaderEntry(this.client, Text.translatable(Constants.CONFIG_KEY_PREFIX + "section." + key)));
    }

    private void addToggle(String key, boolean initialValue, Consumer<Boolean> setter) {
        scrollableList.addPublicEntry(new ToggleOptionEntry(this.client, key, initialValue, setter, this::markDirty));
    }

    private void addIntSlider(String key, int initialValue, int min, int max, Consumer<Integer> setter) {
        scrollableList.addPublicEntry(new IntegerSliderOptionEntry(this.client, key, initialValue, min, max, setter, this::markDirty));
    }

    private void addFloatSlider(String key, float initialValue, float min, float max, Consumer<Float> setter) {
        scrollableList.addPublicEntry(new FloatSliderOptionEntry(this.client, key, initialValue, min, max, setter, this::markDirty));
    }

    private <T> void addCycle(String key, T initialValue, List<T> options, Function<T, Text> textSupplier, Consumer<T> setter) {
        scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, key, initialValue, options, textSupplier, setter, this::markDirty));
    }

    private void populateOptions() {
        List<String> borderStyles = Arrays.asList(Constants.BORDER_STYLE_ROUNDED, Constants.BORDER_STYLE_SQUARED);
        List<String> heightModes = Arrays.asList("PLAYER", "CAMERA");
        List<String> markerTypes = Arrays.asList("default", "minimal");
        Function<String, Text> borderStyleText = value -> Text.translatable(Constants.CONFIG_KEY_PREFIX + "border_style." + value.toLowerCase());
        Function<String, Text> heightModeText = value -> Text.translatable(Constants.CONFIG_KEY_PREFIX + "height_difference_mode." + value.toLowerCase());

        addSection("general");
        addToggle("modEnabled", workingConfig.isModEnabled(), workingConfig::setModEnabled);
        addToggle("apply_hotbar_offset", workingConfig.isApplyHotbarOffset(), workingConfig::setApplyHotbarOffset);
        addToggle("toggle_tab", workingConfig.isToggleTab(), workingConfig::setToggleTab);
        addToggle("always_show_player_heads", workingConfig.isAlwaysShowPlayerHeads(), workingConfig::setAlwaysShowPlayerHeads);
        addToggle("always_show_player_names", workingConfig.isAlwaysShowPlayerNames(), workingConfig::setAlwaysShowPlayerNames);
        addIntSlider("max_visible_icons", workingConfig.getMaxVisibleIcons(), 1, 45, workingConfig::setMaxVisibleIcons);
        addIntSlider("position_update_rate_ticks", workingConfig.getPositionUpdateRateTicks(), 1, 20, workingConfig::setPositionUpdateRateTicks);
        addFloatSlider("lerp_speed", workingConfig.getLerpSpeed(), 0.01f, 1.0f, workingConfig::setLerpSpeed);

        addSection("icon");
        addIntSlider("icon_size", workingConfig.getIconSize(), 1, 4, workingConfig::setIconSize);
        addCycle("dot_type", workingConfig.getDotType(), Client.availableDots, Text::literal, workingConfig::setDotType);
        addCycle("icon_border_style", workingConfig.getIconBorderStyle(), borderStyles, borderStyleText, workingConfig::setIconBorderStyle);
        addCycle("height_difference_mode", workingConfig.getHeightDifferenceMode(), heightModes, heightModeText, workingConfig::setHeightDifferenceMode);
        addCycle("arrow_type", workingConfig.getArrowType(), Client.availableArrows, Text::literal, workingConfig::setArrowType);
        addIntSlider("vertical_padding", workingConfig.getVerticalPadding(), 0, 10, workingConfig::setVerticalPadding);

        addSection("markers");
        addCycle("death_marker_type", workingConfig.getDeathMarkerType(), markerTypes, Text::literal, workingConfig::setDeathMarkerType);

        addSection("player_name");
        addFloatSlider("nameplate_scale", workingConfig.getNameplateScale(), 0.5f, 1.5f, workingConfig::setNameplateScale);
        addCycle("name_border_style", workingConfig.getNameBorderStyle(), borderStyles, borderStyleText, workingConfig::setNameBorderStyle);

        addSection("fading");
        addFloatSlider("min_alpha", workingConfig.getMinAlpha(), 0.0f, 1.0f, workingConfig::setMinAlpha);
        addFloatSlider("max_fade_distance", workingConfig.getMaxFadeDistance(), 10.0f, 200.0f, workingConfig::setMaxFadeDistance);
        addFloatSlider("fade_start_distance", workingConfig.getFadeStartDistance(), 5.0f, 100.0f, workingConfig::setFadeStartDistance);
        addFloatSlider("fade_alpha_max", workingConfig.getFadeAlphaMax(), 0.1f, 1.0f, workingConfig::setFadeAlphaMax);
        addFloatSlider("fade_alpha_min", workingConfig.getFadeAlphaMin(), 0.0f, 0.5f, workingConfig::setFadeAlphaMin);

        addSection("player_appearance");
        scrollableList.addPublicEntry(new PlayerAppearanceManagementEntry(Objects.requireNonNull(this.client), this, workingConfig));
        workingConfig.getPlayerAppearances().keySet().stream().sorted().forEach(name -> scrollableList.addPublicEntry(new PlayerListEntry(this.client, this, workingConfig, name)));
        scrollableList.addPublicEntry(new ResetSettingsEntry(this.client, this, this::markDirty, workingConfig));
    }

    @Override public void tick() {
        super.tick();
        if (this.scrollableList != null) this.scrollableList.tick();
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        renderGlobalPreview(context);
        if (Client.isLocalMode()) {
            Text warningText = Text.translatable("bplb.config.local_mode_warning").formatted(Formatting.RED);
            context.drawTextWithShadow(this.textRenderer, warningText, this.width - this.textRenderer.getWidth(warningText) - 5, this.height - 15, 0xFFFFFF);
        }
    }

    private void renderGlobalPreview(DrawContext context) {
        int previewCenterX = this.width - 70;
        int previewCenterY = this.height / 2;

        context.getMatrices().push();
        context.getMatrices().translate(previewCenterX, previewCenterY, 0);

        int textureIndex = RenderUtils.getTextureIndexForSize(workingConfig.getIconSize());
        String dotId = workingConfig.getDotType();
        String borderStyle = workingConfig.getIconBorderStyle();

        Identifier dotTexture = RenderUtils.getBplbTexture(String.format("dots/%s/%s_%d.png", dotId, dotId, textureIndex));
        Identifier outlineTexture = RenderUtils.getBplbTexture(String.format("outlines/%s/default_%d.png", borderStyle, textureIndex));

        RenderUtils.renderTintedTexture(context, outlineTexture, -PREVIEW_BASE_SIZE / 2f, -PREVIEW_BASE_SIZE / 2f, PREVIEW_BASE_SIZE, PREVIEW_BASE_SIZE, 0x808080, 1.0f);
        RenderUtils.renderTintedTexture(context, dotTexture, -PREVIEW_BASE_SIZE / 2f, -PREVIEW_BASE_SIZE / 2f, PREVIEW_BASE_SIZE, PREVIEW_BASE_SIZE, 0xFFFFFF, 1.0f);

        context.getMatrices().pop();
        Text dotLabel = Text.translatable("bplb.config.player_appearance.dot_preview").formatted(Formatting.BOLD, Formatting.GRAY);
        context.drawTextWithShadow(this.textRenderer, dotLabel, previewCenterX - this.textRenderer.getWidth(dotLabel) / 2, previewCenterY - PREVIEW_BASE_SIZE / 2 - 15, 0xFFFFFF);
    }

    private void applyChanges() {
        Config.copy(workingConfig, Config.getInstance());
        Config.getInstance().save();
        hasChanges = false;
        updateButtons();
    }

    public void markDirty() {
        hasChanges = true;
        updateButtons();
    }

    private void updateButtons() {
        if (applyButton != null) applyButton.active = hasChanges;
        if (doneButton != null) doneButton.active = !hasChanges;
    }

    @Override public void close() {
        if (this.client != null) this.client.setScreen(this.parent);
    }
}

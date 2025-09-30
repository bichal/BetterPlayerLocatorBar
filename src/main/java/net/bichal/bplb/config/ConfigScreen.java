package net.bichal.bplb.config;

import net.bichal.bplb.client.Client;
import net.bichal.bplb.client.render.RenderAddons;
import net.bichal.bplb.client.render.RenderUtils;
import net.bichal.bplb.client.render.TextureManager;
import net.bichal.bplb.config.entries.*;
import net.bichal.bplb.config.widget.ButtonWidget;
import net.bichal.bplb.config.widget.ScrollableListWidget;
import net.bichal.bplb.config.widget.TextInputWidget;
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
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.bichal.bplb.client.render.RenderAddons.getUuid;
import static net.bichal.bplb.util.ColorUtils.generateColorFromUUID;

public class ConfigScreen extends Screen {
    private static final int PREVIEW_BASE_SIZE = 40;
    private static final int preview_close_size = 0;
    private static final int MAX_UI_WIDTH = 460;
    private final Screen parent;
    private final Config workingConfig;
    private ScrollableListWidget scrollableList;
    private boolean hasChanges = false;
    private ButtonWidget applyButton, doneButton;
    private String searchQuery = "";

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

    private void rebuildListWithoutScroll() {
        if (this.client != null) {
            scrollableList.clearEntries();
            populateOptions();
        }
    }

    private boolean matchesSearch(String key) {
        if (searchQuery.isEmpty()) return true;
        String translatedKey = Text.translatable(Constants.CONFIG_KEY_PREFIX + key).getString().toLowerCase();
        if (translatedKey.contains(searchQuery)) return true;
        return key.toLowerCase().contains(searchQuery);
    }


    @Override
    protected void init() {
        super.init();
        int uiWidth = Math.min(this.width - (10 * 5) * 2 - 25 * 2, MAX_UI_WIDTH);
        int scrollListLeftOffset = 0;

        if (this.width < MAX_UI_WIDTH) {
            uiWidth = this.width - 25 * 2 - PREVIEW_BASE_SIZE - PREVIEW_BASE_SIZE / 2;
            scrollListLeftOffset = -10;
        }
        TextInputWidget searchField = new TextInputWidget(
                this.textRenderer,
                this.width / 2 - 100,
                12,
                200,
                20,
                Text.literal("Search...")
        );
        searchField.setChangedListener(text -> {
            this.searchQuery = text.toLowerCase();
            this.scrollableList.setScrollAmount(0);
            rebuildListWithoutScroll();
        });
        this.addDrawableChild(searchField);
        this.scrollableList = new ScrollableListWidget(this.client, this.width, 40, this.height - 35, 24);
        this.scrollableList.setRowWidth(uiWidth);
        this.scrollableList.setRowLeft(this.width / 2 - uiWidth / 2 + scrollListLeftOffset);
        this.scrollableList.setScrollbarX(this.width - 6);
        populateOptions();
        this.addSelectableChild(this.scrollableList);
        this.addDrawableChild(this.scrollableList);
        int startX = (int) ((double) this.width / 2 - Constants.CONFIG_BUTTON_WIDTH * 1.5 - Constants.CONFIG_BUTTON_SPACING * 1.5);
        int buttonY = this.height - 27;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), button -> this.close())
                .dimensions(startX, buttonY, Constants.CONFIG_BUTTON_WIDTH, 20).build());
        this.applyButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.bplb.config.apply"), button -> applyChanges())
                .dimensions(startX + Constants.CONFIG_BUTTON_WIDTH + Constants.CONFIG_BUTTON_SPACING, buttonY, Constants.CONFIG_BUTTON_WIDTH, 20).build());
        this.doneButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
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
        List<String> borderTypes = Arrays.asList("default", "minimal");
        List<String> heightModes = Arrays.asList("player", "camera");
        List<String> markerTypes = Arrays.asList("default", "minimal");

        Function<String, Text> borderStyleText = value -> Text.translatable(Constants.CONFIG_KEY_PREFIX + "border_style." + value.toLowerCase());
        Function<String, Text> borderTypeText = value -> Text.translatable(Constants.CONFIG_KEY_PREFIX + "border_type." + value.toLowerCase());
        Function<String, Text> heightModeText = value -> Text.translatable(Constants.CONFIG_KEY_PREFIX + "height_difference_mode." + value.toLowerCase());

        boolean hasGeneralMatches = matchesSearch("section.general") || matchesSearch("modEnabled") || matchesSearch("apply_hotbar_offset") || matchesSearch("toggle_tab") || matchesSearch("always_show_player_heads") || matchesSearch("always_show_player_names") || matchesSearch("max_visible_icons") || matchesSearch("position_update_rate_ticks") || matchesSearch("lerp_speed");
        if (hasGeneralMatches) {
            addSection("general");
            if (matchesSearch("modEnabled"))
                addToggle("modEnabled", workingConfig.isModEnabled(), workingConfig::setModEnabled);
            if (matchesSearch("apply_hotbar_offset"))
                addToggle("apply_hotbar_offset", workingConfig.isApplyHotbarOffset(), workingConfig::setApplyHotbarOffset);
            if (matchesSearch("toggle_tab"))
                addToggle("toggle_tab", workingConfig.isToggleTab(), workingConfig::setToggleTab);
            if (matchesSearch("always_show_player_heads"))
                addToggle("always_show_player_heads", workingConfig.isAlwaysShowPlayerHeads(), workingConfig::setAlwaysShowPlayerHeads);
            if (matchesSearch("always_show_player_names"))
                addToggle("always_show_player_names", workingConfig.isAlwaysShowPlayerNames(), workingConfig::setAlwaysShowPlayerNames);
            if (matchesSearch("max_visible_icons"))
                addIntSlider("max_visible_icons", workingConfig.getMaxVisibleIcons(), 1, 45, workingConfig::setMaxVisibleIcons);
            if (matchesSearch("position_update_rate_ticks"))
                addIntSlider("position_update_rate_ticks", workingConfig.getPositionUpdateRateTicks(), 1, 20, workingConfig::setPositionUpdateRateTicks);
            if (matchesSearch("lerp_speed"))
                addFloatSlider("lerp_speed", workingConfig.getLerpSpeed(), 0.01f, 1.0f, workingConfig::setLerpSpeed);
        }

        boolean hasIconMatches = matchesSearch("section.icon") || matchesSearch("icon_size") || matchesSearch("dot_type") || matchesSearch("icon_border_style") || matchesSearch("icon_border_type") || matchesSearch("height_difference_mode") || matchesSearch("arrow_type") || matchesSearch("vertical_padding") || matchesSearch("inherit_border_color");
        if (hasIconMatches) {
            addSection("icon");
            if (matchesSearch("icon_size"))
                addIntSlider("icon_size", workingConfig.getIconSize(), 1, 4, workingConfig::setIconSize);

            if (matchesSearch("dot_type")) {
                List<String> dotsToShow = Client.availableDots.stream().filter(d -> !d.equals("bowtie")).toList();
                scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "dot_type",
                        workingConfig.getDotType(), dotsToShow, Text::literal,
                        val -> {
                            workingConfig.setDotType(val);
                            rebuildListWithoutScroll();
                        }, this::markDirty));
            }

            boolean isBowtie = workingConfig.getDotType().equals("bowtie");

            if (matchesSearch("icon_border_style") && !isBowtie) {
                addCycle("icon_border_style", workingConfig.getIconBorderStyle(), borderStyles, borderStyleText, workingConfig::setIconBorderStyle);
            }
            if (matchesSearch("icon_border_type") && !isBowtie) {
                addCycle("icon_border_type", workingConfig.getIconBorderType(), borderTypes, borderTypeText, workingConfig::setIconBorderType);
            }
            if (matchesSearch("inherit_border_color"))
                addToggle("inherit_border_color", workingConfig.isInheritBorderColor(), workingConfig::setInheritBorderColor);
            if (matchesSearch("height_difference_mode"))
                addCycle("height_difference_mode", workingConfig.getHeightDifferenceMode(), heightModes, heightModeText, workingConfig::setHeightDifferenceMode);
            if (matchesSearch("arrow_type"))
                addCycle("arrow_type", workingConfig.getArrowType(), Client.availableArrows, Text::literal, workingConfig::setArrowType);
            if (matchesSearch("vertical_padding"))
                addIntSlider("vertical_padding", workingConfig.getVerticalPadding(), 0, 10, workingConfig::setVerticalPadding);
        }

        boolean hasMarkersMatches = matchesSearch("section.markers") || matchesSearch("death_marker_type") || matchesSearch("death_marker_border_type") || matchesSearch("death_marker_inherit_border_color");
        if (hasMarkersMatches) {
            addSection("markers");
            if (matchesSearch("death_marker_type"))
                addCycle("death_marker_type", workingConfig.getDeathMarkerType(), markerTypes, Text::literal, workingConfig::setDeathMarkerType);
            if (matchesSearch("death_marker_border_type"))
                addCycle("death_marker_border_type", workingConfig.getDeathMarkerBorderType(), borderTypes, borderTypeText, workingConfig::setDeathMarkerBorderType);
            if (matchesSearch("death_marker_inherit_border_color"))
                addToggle("death_marker_inherit_border_color", workingConfig.isDeathMarkerInheritBorderColor(), workingConfig::setDeathMarkerInheritBorderColor);
        }

        boolean hasPlayerNameMatches = matchesSearch("section.player_name") || matchesSearch("nameplate_scale") || matchesSearch("name_border_style");
        if (hasPlayerNameMatches) {
            addSection("player_name");
            if (matchesSearch("nameplate_scale"))
                addFloatSlider("nameplate_scale", workingConfig.getNameplateScale(), 0.5f, 1.5f, workingConfig::setNameplateScale);
            if (matchesSearch("name_border_style"))
                addCycle("name_border_style", workingConfig.getNameBorderStyle(), borderStyles, borderStyleText, workingConfig::setNameBorderStyle);
        }

        boolean hasFadingMatches = matchesSearch("section.fading") || matchesSearch("min_alpha") || matchesSearch("max_fade_distance") || matchesSearch("fade_start_distance") || matchesSearch("fade_alpha_max") || matchesSearch("fade_alpha_min");
        if (hasFadingMatches) {
            addSection("fading");
            if (matchesSearch("min_alpha"))
                addFloatSlider("min_alpha", workingConfig.getMinAlpha(), 0.0f, 1.0f, workingConfig::setMinAlpha);
            if (matchesSearch("max_fade_distance"))
                addFloatSlider("max_fade_distance", workingConfig.getMaxFadeDistance(), 10.0f, 200.0f, workingConfig::setMaxFadeDistance);
            if (matchesSearch("fade_start_distance"))
                addFloatSlider("fade_start_distance", workingConfig.getFadeStartDistance(), 5.0f, 100.0f, workingConfig::setFadeStartDistance);
            if (matchesSearch("fade_alpha_max"))
                addFloatSlider("fade_alpha_max", workingConfig.getFadeAlphaMax(), 0.1f, 1.0f, workingConfig::setFadeAlphaMax);
            if (matchesSearch("fade_alpha_min"))
                addFloatSlider("fade_alpha_min", workingConfig.getFadeAlphaMin(), 0.0f, 0.5f, workingConfig::setFadeAlphaMin);
        }

        addSection("player_appearance");
        scrollableList.addPublicEntry(new AddPlayerEntry(Objects.requireNonNull(this.client), this, workingConfig));

        workingConfig.getPlayerConfigs().keySet().stream().sorted().forEach(name -> {
            scrollableList.addPublicEntry(new PlayerListEntry(this.client, this, workingConfig, name));

            PlayerConfig pc = workingConfig.getPlayerConfigs().get(name);
            if (pc != null) {
                if (matchesSearch("player." + name + ".color")) {
                    UUID uuid = getUuid(name);
                    int defaultColor = uuid != null ? generateColorFromUUID(uuid) : 0xFF808080;
                    scrollableList.addPublicEntry(new ColorTextFieldEntry(this.client, "player." + name + ".color",
                            pc.color != null ? pc.color : defaultColor, color -> {
                        pc.color = color;
                        markDirty();
                    }, this));
                }

                // Dot Type (Icon)
                if (matchesSearch("player." + name + ".dot_type")) {
                    List<String> dotsToShow = Client.availableDots.stream().filter(d -> !d.equals("bowtie")).toList();
                    scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "player." + name + ".dot_type",
                            pc.dotType != null ? pc.dotType : "default", dotsToShow, Text::literal, val -> {
                        pc.dotType = val;
                        markDirty();
                    }, this::markDirty));
                }

                // Icon Border Style
                if (matchesSearch("player." + name + ".icon_border_style")) {
                    scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "player." + name + ".icon_border_style",
                            pc.iconBorderStyle != null ? pc.iconBorderStyle : "rounded", borderStyles, borderStyleText, val -> {
                        pc.iconBorderStyle = val;
                        markDirty();
                    }, this::markDirty));
                }

                // Icon Border Type
                if (matchesSearch("player." + name + ".icon_border_type")) {
                    scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "player." + name + ".icon_border_type",
                            pc.iconBorderType != null ? pc.iconBorderType : "default", borderTypes, borderTypeText, val -> {
                        pc.iconBorderType = val;
                        markDirty();
                    }, this::markDirty));
                }

                // Arrow Type
                if (matchesSearch("player." + name + ".arrow_type")) {
                    scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "player." + name + ".arrow_type",
                            pc.arrowType != null ? pc.arrowType : "default", Client.availableArrows, Text::literal, val -> {
                        pc.arrowType = val;
                        markDirty();
                    }, this::markDirty));
                }

                // Texture Override
                if (matchesSearch("player." + name + ".textureHeadOverride")) {
                    scrollableList.addPublicEntry(new TextFieldOptionEntry(this.client, this, "player." + name + ".textureHeadOverride",
                            pc.textureHeadOverride != null ? pc.textureHeadOverride : "", value -> {
                        pc.textureHeadOverride = value.isEmpty() ? null : value;
                        markDirty();
                    }));
                }
            }
        });
        scrollableList.addPublicEntry(new ResetSettingsEntry(this.client, this, this::markDirty, workingConfig));
    }

    @Override public void tick() {
        super.tick();
        if (this.scrollableList != null) this.scrollableList.tick();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 2, 0xFFFFFF);
        renderGlobalPreview(context);

        if (Client.isLocalMode()) {
            Text warningText = Text.translatable("bplb.config.local_mode_warning").formatted(Formatting.RED);
            context.drawTextWithShadow(this.textRenderer, warningText, this.width - this.textRenderer.getWidth(warningText) - 5, this.height - 15, 0xFFFFFF);
        }
    }

    private void renderGlobalPreview(DrawContext context) {
        int previewOffset = this.width < MAX_UI_WIDTH ? 20 : 0;
        int previewCenterX = this.width - 70 + preview_close_size + previewOffset;
        int previewCenterY = this.height / 2;

        renderArrowPreview(context, previewCenterX, (int) ((previewCenterY + PREVIEW_BASE_SIZE * -1.5 - 5) - workingConfig.getVerticalPadding()), true);
        renderIconPreview(context, previewCenterX, (int) (previewCenterY + PREVIEW_BASE_SIZE * -.5 - 5));
        renderArrowPreview(context, previewCenterX, (int) ((previewCenterY + PREVIEW_BASE_SIZE * .5 - 5) + workingConfig.getVerticalPadding()), false);
        renderDeathMarkerPreview(context, previewCenterX, (int) (previewCenterY + PREVIEW_BASE_SIZE * 1.5 + 5));
    }

    private void renderIconPreview(DrawContext context, int centerX, int centerY) {
        context.getMatrices().push();
        context.getMatrices().translate(centerX - PREVIEW_BASE_SIZE / 2f, centerY - PREVIEW_BASE_SIZE / 2f, 0);

        UUID playerUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        RenderAddons.renderPlayerIcon(context, "Preview", playerUuid, 32.0, 0, 0,
                PREVIEW_BASE_SIZE, 1.0f, false, workingConfig);
        context.getMatrices().pop();
    }

    private void renderArrowPreview(DrawContext context, int centerX, int centerY, boolean isUp) {
        context.getMatrices().push();
        context.getMatrices().translate(centerX, centerY, 0);

        Identifier arrowTexture = TextureManager.getArrowTexture(workingConfig.getArrowType());
        float u = isUp ? 0 : Constants.ICON_BASE_SIZE;
        int textureHeight = "mojang".equals(workingConfig.getArrowType()) ? Constants.ICON_BASE_SIZE * 2 : Constants.ICON_BASE_SIZE;

        context.drawTexture(arrowTexture, -PREVIEW_BASE_SIZE / 2, -PREVIEW_BASE_SIZE / 2,
                PREVIEW_BASE_SIZE, PREVIEW_BASE_SIZE, u, 0,
                Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE,
                Constants.ICON_BASE_SIZE * 2, textureHeight);
        context.getMatrices().pop();
    }

    private void renderDeathMarkerPreview(DrawContext context, int centerX, int centerY) {
        RenderUtils.withMatrixPush(context, centerX - PREVIEW_BASE_SIZE / 2f, centerY - PREVIEW_BASE_SIZE / 2f, () -> RenderAddons.renderDeathMarker(context, 0, 0, PREVIEW_BASE_SIZE, 1.0f, workingConfig));
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

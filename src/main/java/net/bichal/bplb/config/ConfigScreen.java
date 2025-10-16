package net.bichal.bplb.config;

import net.bichal.bplb.client.Client;
import net.bichal.bplb.client.render.RenderAddons;
import net.bichal.bplb.client.render.RenderUtils;
import net.bichal.bplb.client.render.TextureAnimator;
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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.bichal.bplb.client.render.RenderAddons.getUuidFromCache;
import static net.bichal.bplb.util.ColorUtils.generateColorFromUUID;

public class ConfigScreen extends Screen {
    private static final int PREVIEW_BASE_SIZE = 45;
    private static final int preview_close_size = 0;
    private static final int MAX_UI_WIDTH = 460;
    private final Screen parent;
    private TextureAnimator previewArrowAnimator = null;
    private final Config workingConfig;
    private ScrollableListWidget scrollableList;
    private boolean hasChanges = false;
    private final Map<String, SearchMatch> searchMatches = new HashMap<>();
    private ButtonWidget applyButton, doneButton;
    private String searchQuery = "";
    private boolean isRebuilding = false;

    public ConfigScreen(Screen parent) {
        super(Text.translatable(Constants.CONFIG_KEY_PREFIX + "title"));
        this.parent = parent;
        this.workingConfig = new Config();
        Config.copy(Config.getInstance(), this.workingConfig);
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

        TextInputWidget searchField = new TextInputWidget(this.textRenderer, this.width / 2 - 100, 30, 200, 20, Text.translatable("bplb.config.search"));
        searchField.setChangedListener(text -> {
            this.searchQuery = text.toLowerCase();
            this.scrollableList.setScrollAmount(0);
            rebuildListWithoutScroll();
        });
        this.addDrawableChild(searchField);

        this.scrollableList = new ScrollableListWidget(this.client, this.width, 60, this.height - 35, 24);
        this.scrollableList.setRowWidth(uiWidth);
        this.scrollableList.setRowLeft(this.width / 2 - uiWidth / 2 + scrollListLeftOffset);
        this.scrollableList.setScrollbarX(this.width - 6);
        populateOptions();
        this.addSelectableChild(this.scrollableList);
        this.addDrawableChild(this.scrollableList);

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

    public void rebuildList() {
        if (this.client != null) {
            double scroll = this.scrollableList.getScrollAmount();
            this.init(this.client, this.width, this.height);
            this.scrollableList.setScrollAmount(scroll);
        }
    }

    private void rebuildListWithoutScroll() {
        if (isRebuilding || this.client == null) return;

        isRebuilding = true;
        try {
            scrollableList.clearEntries();
            populateOptions();
        } finally {
            isRebuilding = false;
        }
    }

    private void addIntSlider(String key, int actualValue, int min, int max, Consumer<Integer> setter) {
        scrollableList.addPublicEntry(new IntegerSliderOptionEntry(this.client, key, actualValue, min, max, setter, this::markDirty));
    }

    private void addSection(String key) {
        scrollableList.addPublicEntry(new SectionHeaderEntry(this.client, Text.translatable(Constants.CONFIG_KEY_PREFIX + "section." + key)));
    }

    private void addToggle(String key, boolean initialValue, Consumer<Boolean> setter) {
        scrollableList.addPublicEntry(new ToggleOptionEntry(this.client, key, initialValue, setter, this::markDirty));
    }

    private void updateSearchMatches() {
        searchMatches.clear();
        if (searchQuery.isEmpty()) return;

        List<String> allKeys = List.of("modEnabled", "apply_hotbar_offset", "always_show_player_heads", "always_show_player_names", "max_visible_icons", "position_update_rate_ticks", "lerp_speed", "icon_size", "dot_type", "icon_border_style", "icon_border_type", "inherit_border_color", "height_difference_mode", "arrow_type", "vertical_padding", "adjust_to_fov", "fov_multiplier", "death_marker_type", "death_marker_border_type", "death_marker_inherit_border_color", "nameplate_scale", "name_border_style", "fade_end_distance", "fade_start_distance", "fade_alpha_max", "fade_alpha_min");

        for (String key : allKeys) {
            String translated = Text.translatable(Constants.CONFIG_KEY_PREFIX + key).getString().toLowerCase();
            List<Integer> indices = findMatchIndices(translated, searchQuery);
            if (!indices.isEmpty()) {
                searchMatches.put(key, new SearchMatch(translated, indices));
            }
        }
    }

    private void addFloatSlider(String key, float initialValue, float min, float max, Consumer<Float> setter) {
        scrollableList.addPublicEntry(new FloatSliderOptionEntry(this.client, key, initialValue, min, max, setter, this::markDirty));
    }

    private <T> void addCycle(String key, T initialValue, List<T> options, Function<T, Text> textSupplier, Consumer<T> setter, boolean active) {
        scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, key, initialValue, options, textSupplier, setter, this::markDirty, active));
    }

    private List<Integer> findMatchIndices(String text, String query) {
        List<Integer> indices = new ArrayList<>();
        int index = 0;
        while ((index = text.indexOf(query, index)) != -1) {
            for (int i = 0; i < query.length(); i++) {
                indices.add(index + i);
            }
            index++;
        }
        return indices;
    }

    private boolean matchesSearch(String key) {
        return searchQuery.isEmpty() || searchMatches.containsKey(key);
    }

    private Text getTranslatedAssetName(String assetId, String category) {
        String key = "bplb.config.asset." + category + "." + assetId;
        Text translated = Text.translatable(key);
        return translated.getString().equals(key) ? Text.literal(assetId) : translated;
    }

    private void populateOptions() {
        if (isRebuilding) return;
        updateSearchMatches();

        boolean isBowtie = workingConfig.getDotType().equals("bowtie");

        List<String> borderStyles = Arrays.asList(Constants.BORDER_STYLE_ROUNDED, Constants.BORDER_STYLE_SQUARED);
        List<String> borderTypes = Arrays.asList("default", "minimal");
        List<String> heightModes = Arrays.asList("player", "camera");
        List<String> markerTypes = Arrays.asList("default", "minimal");

        Function<String, Text> borderStyleText = value -> Text.translatable(Constants.CONFIG_KEY_PREFIX + "border_style." + value.toLowerCase());
        Function<String, Text> borderTypeText = value -> Text.translatable(Constants.CONFIG_KEY_PREFIX + "border_type." + value.toLowerCase());
        Function<String, Text> heightModeText = value -> Text.translatable(Constants.CONFIG_KEY_PREFIX + "height_difference_mode." + value.toLowerCase());

        if (hasAnyMatch("modEnabled", "apply_hotbar_offset", "always_show_player_heads", "always_show_player_names", "max_visible_icons", "position_update_rate_ticks", "lerp_speed")) {
            addSection("general");
            addIfMatch("modEnabled", () -> addToggle("modEnabled", workingConfig.isModEnabled(), workingConfig::setModEnabled));
            addIfMatch("apply_hotbar_offset", () -> addToggle("apply_hotbar_offset", workingConfig.isApplyHotbarOffset(), workingConfig::setApplyHotbarOffset));
            addIfMatch("always_show_player_heads", () -> addToggle("always_show_player_heads", workingConfig.isAlwaysShowPlayerHeads(), workingConfig::setAlwaysShowPlayerHeads));
            addIfMatch("always_show_player_names", () -> addToggle("always_show_player_names", workingConfig.isAlwaysShowPlayerNames(), workingConfig::setAlwaysShowPlayerNames));
            addIfMatch("max_visible_icons", () -> addIntSlider("max_visible_icons", workingConfig.getMaxVisibleIcons(), 1, 200, workingConfig::setMaxVisibleIcons));
            addIfMatch("position_update_rate_ticks", () -> addIntSlider("position_update_rate_ticks", workingConfig.getPositionUpdateRateTicks(), 1, 20, workingConfig::setPositionUpdateRateTicks));
            addIfMatch("lerp_speed", () -> addFloatSlider("lerp_speed", workingConfig.getLerpSpeed(), 0.1f, 1.0f, workingConfig::setLerpSpeed));
        }

        if (hasAnyMatch("icon_size", "dot_type", "icon_border_style", "icon_border_type", "height_difference_mode", "arrow_type", "vertical_padding", "inherit_border_color", "adjust_to_fov", "fov_multiplier")) {
            addSection("icon");
            addIfMatch("icon_size", () -> addIntSlider("icon_size", workingConfig.getIconSize(), 1, 4, workingConfig::setIconSize));

            if (matchesSearch("dot_type")) {
                List<String> dotsToShow = Client.availableDots.stream().filter(d -> !d.equals("bowtie")).toList();
                scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "dot_type", workingConfig.getDotType(), dotsToShow, id -> getTranslatedAssetName(id, "dot"), val -> {
                    workingConfig.setDotType(val);
                    markDirty();
                    rebuildList();
                }, this::markDirty, true));
            }

            addIfMatch("icon_border_style", () -> addCycle("icon_border_style", workingConfig.getIconBorderStyle(), borderStyles, borderStyleText, workingConfig::setIconBorderStyle, !isBowtie));
            addIfMatch("icon_border_type", () -> addCycle("icon_border_type", workingConfig.getIconBorderType(), borderTypes, borderTypeText, workingConfig::setIconBorderType, !isBowtie));
            addIfMatch("inherit_border_color", () -> addToggle("inherit_border_color", workingConfig.isInheritBorderColor(), workingConfig::setInheritBorderColor));
            addIfMatch("height_difference_mode", () -> addCycle("height_difference_mode", workingConfig.getHeightDifferenceMode(), heightModes, heightModeText, workingConfig::setHeightDifferenceMode, true));

            if (matchesSearch("arrow_type")) {
                List<String> arrows = Client.availableArrows.isEmpty() ? List.of("default") : Client.availableArrows;
                scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "arrow_type", workingConfig.getArrowType(), arrows, id -> getTranslatedAssetName(id, "arrow"), val -> {
                    workingConfig.setArrowType(val);
                    previewArrowAnimator = null;
                    markDirty();
                }, this::markDirty, true));
            }

            addIfMatch("vertical_padding", () -> addIntSlider("vertical_padding", workingConfig.getVerticalPadding(), 0, 4, workingConfig::setVerticalPadding));
            addIfMatch("adjust_to_fov", () -> addToggle("adjust_to_fov", workingConfig.isAdjustToFov(), workingConfig::setAdjustToFov));
            addIfMatch("fov_multiplier", () -> addFloatSlider("fov_multiplier", workingConfig.getFovMultiplier(), 0.5f, 2.0f, workingConfig::setFovMultiplier));
        }

        if (hasAnyMatch("death_marker_type", "death_marker_border_type", "death_marker_inherit_border_color")) {
            addSection("markers");
            addIfMatch("death_marker_type", () -> addCycle("death_marker_type", workingConfig.getDeathMarkerType(), markerTypes, id -> getTranslatedAssetName(id, "marker"), workingConfig::setDeathMarkerType, true));
            addIfMatch("death_marker_border_type", () -> addCycle("death_marker_border_type", workingConfig.getDeathMarkerBorderType(), borderTypes, borderTypeText, workingConfig::setDeathMarkerBorderType, true));
            addIfMatch("death_marker_inherit_border_color", () -> addToggle("death_marker_inherit_border_color", workingConfig.isDeathMarkerInheritBorderColor(), workingConfig::setDeathMarkerInheritBorderColor));
        }

        if (hasAnyMatch("nameplate_scale", "name_border_style")) {
            addSection("player_name");
            addIfMatch("nameplate_scale", () -> addFloatSlider("nameplate_scale", workingConfig.getNameplateScale(), 0.5f, 1.5f, workingConfig::setNameplateScale));
            addIfMatch("name_border_style", () -> addCycle("name_border_style", workingConfig.getNameBorderStyle(), borderStyles, borderStyleText, workingConfig::setNameBorderStyle, true));
        }

        if (hasAnyMatch("fade_start_distance", "fade_end_distance", "fade_alpha_max", "fade_alpha_min")) {
            addSection("fading");
            addIfMatch("fade_start_distance", () -> addIntSlider("fade_start_distance", workingConfig.getFadeStartDistance(), 5, 9995, workingConfig::setFadeStartDistance));
            addIfMatch("fade_end_distance", () -> addIntSlider("fade_end_distance", workingConfig.getFadeEndDistance(), 10, 10000, workingConfig::setFadeEndDistance));
            addIfMatch("fade_alpha_max", () -> addFloatSlider("fade_alpha_max", workingConfig.getFadeAlphaMax(), workingConfig.getFadeAlphaMin(), 1.0f, workingConfig::setFadeAlphaMax));
            addIfMatch("fade_alpha_min", () -> addFloatSlider("fade_alpha_min", workingConfig.getFadeAlphaMin(), 0.0f, workingConfig.getFadeAlphaMax(), workingConfig::setFadeAlphaMin));
        }

        if (searchQuery.isEmpty() || workingConfig.getPlayerConfigs().keySet().stream().anyMatch(name -> matchesSearch("player." + name))) {
            addSection("player_appearance");
            scrollableList.addPublicEntry(new AddPlayerEntry(Objects.requireNonNull(this.client), this, workingConfig));

            workingConfig.getPlayerConfigs().keySet().stream().sorted().forEach(name -> {
                boolean expanded = workingConfig.isPlayerExpanded(name);
                scrollableList.addPublicEntry(new PlayerListEntry(this.client, this, workingConfig, name, expanded));

                if (expanded) {
                    Config.PlayerAppearance pc = workingConfig.getPlayerConfigs().get(name);
                    if (pc != null) {
                        addPlayerOption(name, "color", () -> {
                            UUID uuid = getUuidFromCache(name);
                            int defaultColor = uuid != null ? generateColorFromUUID(uuid) : 0xFF808080;
                            scrollableList.addPublicEntry(new ColorTextFieldEntry(this.client, "player_appearance.color", pc.color != null ? pc.color : defaultColor, color -> {
                                pc.color = color;
                                markDirty();
                            }, this));
                        });

                        addPlayerOption(name, "dot_type", () -> {
                            List<String> dotsToShow = Client.availableDots.stream().filter(d -> !d.equals("bowtie")).toList();
                            scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "player_appearance.dot_type", pc.dotType != null ? pc.dotType : "default", dotsToShow,
                                    id -> getTranslatedAssetName(id, "dot"),
                                    val -> {
                                        pc.dotType = val;
                                        markDirty();
                                    }, this::markDirty, true));
                        });

                        addPlayerOption(name, "icon_border_style", () -> scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "player_appearance.icon_border_style", pc.iconBorderStyle != null ? pc.iconBorderStyle : "rounded", borderStyles, borderStyleText, val -> {
                            pc.iconBorderStyle = val;
                            markDirty();
                        }, this::markDirty, true)));

                        addPlayerOption(name, "icon_border_type", () -> scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "player_appearance.icon_border_type", pc.iconBorderType != null ? pc.iconBorderType : "default", borderTypes, borderTypeText, val -> {
                            pc.iconBorderType = val;
                            markDirty();
                        }, this::markDirty, true)));

                        addPlayerOption(name, "arrow_type", () -> scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "player_appearance.arrow_type", pc.arrowType != null ? pc.arrowType : "default", Client.availableArrows,
                                id -> getTranslatedAssetName(id, "arrow"),
                                val -> {
                                    pc.arrowType = val;
                                    markDirty();
                                }, this::markDirty, true)));
                    }
                }
            });
        }

        scrollableList.addPublicEntry(new ResetSettingsEntry(this.client, this, this::markDirty, workingConfig));
    }

    private boolean hasAnyMatch(String... keys) {
        if (searchQuery.isEmpty()) return true;
        for (String key : keys) {
            if (matchesSearch(key)) return true;
        }
        return false;
    }

    private void addIfMatch(String key, Runnable action) {
        if (matchesSearch(key)) {
            action.run();
        }
    }

    private void addPlayerOption(String playerName, String option, Runnable action) {
        if (matchesSearch("player." + playerName + "." + option)) {
            action.run();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.scrollableList != null) this.scrollableList.tick();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, Constants.WHITE_COLOR);
        renderGlobalPreview(context);

        if (Client.isLocalMode()) {
            Text warningText = Text.translatable("bplb.config.local_mode_warning").formatted(Formatting.RED);
            context.drawTextWithShadow(this.textRenderer, warningText, this.width - this.textRenderer.getWidth(warningText) - 5, this.height - 15, Constants.WHITE_COLOR);
        }
    }

    public void drawLabelWithHighlight(DrawContext context, Text label, int x, int y, String key) {
        if (searchQuery.isEmpty() || !searchMatches.containsKey(key)) {
            context.drawTextWithShadow(this.textRenderer, label, x, y, Constants.WHITE_COLOR);
            return;
        }

        String text = label.getString();
        String textLower = text.toLowerCase();
        String query = searchQuery.toLowerCase();
        int queryPos = textLower.indexOf(query);

        if (queryPos == -1) {
            context.drawTextWithShadow(this.textRenderer, label, x, y, Constants.WHITE_COLOR);
            return;
        }

        String before = text.substring(0, queryPos);
        String match = text.substring(queryPos, queryPos + query.length());
        String after = text.substring(queryPos + query.length());

        int currentX = x;
        if (!before.isEmpty()) {
            context.drawTextWithShadow(this.textRenderer, Text.literal(before), currentX, y, Constants.WHITE_COLOR);
            currentX += this.textRenderer.getWidth(before);
        }

        context.drawTextWithShadow(this.textRenderer, Text.literal(match), currentX, y, 0xFFFFFF00);
        currentX += this.textRenderer.getWidth(match);

        if (!after.isEmpty()) {
            context.drawTextWithShadow(this.textRenderer, Text.literal(after), currentX, y, Constants.WHITE_COLOR);
        }
    }

    private void renderGlobalPreview(DrawContext context) {
        int previewOffset = this.width < MAX_UI_WIDTH ? 20 : 0;
        int previewCenterX = this.width - 70 + preview_close_size + previewOffset;
        int previewCenterY = this.height / 2 + 30;
        int previewAdjustmentY = Constants.ICON_BASE_SIZE * 2;
        int verticalPadding = workingConfig.getVerticalPadding() * 5 + 3;

        renderArrowPreview(context, previewCenterX, previewCenterY - PREVIEW_BASE_SIZE * 2 - verticalPadding + previewAdjustmentY, true);
        renderIconPreview(context, previewCenterX, previewCenterY - PREVIEW_BASE_SIZE + 1);
        renderArrowPreview(context, previewCenterX, previewCenterY + verticalPadding - previewAdjustmentY + 1, false);
        renderDeathMarkerPreview(context, previewCenterX, previewCenterY + PREVIEW_BASE_SIZE);
    }

    private void renderIconPreview(DrawContext context, int centerX, int centerY) {
        context.getMatrices().push();
        context.getMatrices().translate(centerX - PREVIEW_BASE_SIZE / 2f, centerY - PREVIEW_BASE_SIZE / 2f, 0);
        int textureIndex = workingConfig.getIconSize() == 4 ? 0 : 4 - workingConfig.getIconSize();

        String dotId = workingConfig.getDotType();
        String borderStyle = workingConfig.getIconBorderStyle();
        String borderType = workingConfig.getIconBorderType();
        int color = 0xFFFFFFFF;

        Identifier dotTexture = net.bichal.bplb.client.render.TextureManager.getPlayerDotTexture(dotId, textureIndex);
        Identifier outlineTexture = net.bichal.bplb.client.render.TextureManager.getPlayerDotOutlineTexture(dotId, borderStyle, borderType, textureIndex);

        int borderColor = workingConfig.isInheritBorderColor() ? net.bichal.bplb.util.ColorUtils.darkerColoring(color) : Constants.BLACK_COLOR;
        RenderUtils.renderTintedTexture(context, outlineTexture, 0, 0, PREVIEW_BASE_SIZE, PREVIEW_BASE_SIZE, borderColor, 1.0f);
        RenderUtils.renderTintedTexture(context, dotTexture, 0, 0, PREVIEW_BASE_SIZE, PREVIEW_BASE_SIZE, color, 1.0f);

        context.getMatrices().pop();
    }

    private void renderArrowPreview(DrawContext context, int centerX, int centerY, boolean isUp) {
        context.getMatrices().push();
        context.getMatrices().translate(centerX, centerY, 0);
        if (previewArrowAnimator == null) previewArrowAnimator = new TextureAnimator(10, 4);
        Identifier arrowTexture = TextureManager.getArrowTexture(workingConfig.getArrowType());
        int frame = previewArrowAnimator.getCurrentFrame();
        float u = isUp ? 0 : Constants.ICON_BASE_SIZE;
        float v = frame * Constants.ICON_BASE_SIZE;
        context.drawTexture(arrowTexture, -PREVIEW_BASE_SIZE / 2, -PREVIEW_BASE_SIZE / 2, PREVIEW_BASE_SIZE, PREVIEW_BASE_SIZE, u, v, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE * 2, Constants.ICON_BASE_SIZE * 2);
        context.getMatrices().pop();
    }

    private void renderDeathMarkerPreview(DrawContext context, int centerX, int centerY) {
        RenderUtils.withMatrixPush(context, centerX - PREVIEW_BASE_SIZE / 2f, centerY - PREVIEW_BASE_SIZE / 2f, () -> RenderAddons.renderDeathMarker(context, 0, 0, PREVIEW_BASE_SIZE, 1.0f, workingConfig));
    }

    private void applyChanges() {
        for (Map.Entry<String, Config.PlayerAppearance> entry : workingConfig.getPlayerConfigs().entrySet()) {
            UUID uuid = getUuidFromCache(entry.getKey());
            if (uuid != null) {
                Config.PlayerAppearance pc = entry.getValue();
                pc.playerUuid = uuid;
                pc.playerName = entry.getKey();
            }
        }

        Config.copy(workingConfig, Config.getInstance());
        Config.getInstance().save();
        hasChanges = false;
        updateButtons();
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(this.parent);
    }

    public void markDirty() {
        hasChanges = true;
        updateButtons();
    }

    private void updateButtons() {
        if (applyButton != null) applyButton.active = hasChanges;
        if (doneButton != null) doneButton.active = !hasChanges;
    }

    private record SearchMatch(String key, List<Integer> matchIndices) {
    }
}

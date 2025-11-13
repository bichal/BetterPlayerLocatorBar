package net.bichal.bplb.gui;

import net.bichal.bichalutils.util.ColorUtil;
import net.bichal.bplb.client.Client;
import net.bichal.bplb.client.render.RenderAddons;
import net.bichal.bplb.gui.widget.ButtonWidget;
import net.bichal.bplb.gui.widget.ScrollableListWidget;
import net.bichal.bplb.gui.widget.TextInputWidget;
import net.bichal.bplb.gui.widget.TooltipWidget;
import net.bichal.bplb.gui.widget.entries.*;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.bichal.bplb.client.render.RenderAddons.getUuidFromCache;

public class ConfigScreen extends Screen {
    private static final int MAX_UI_WIDTH = 460;
    private final Screen parent;
    private final Config workingConfig;
    private ScrollableListWidget scrollableList;
    private boolean hasChanges = false;
    private final Map<String, SearchMatch> searchMatches = new HashMap<>();
    private ButtonWidget applyButton, doneButton;
    private String searchQuery = "";
    private final UUID previewUuid = UUID.randomUUID();
    private final Map<String, Text> tooltipTexts = new HashMap<>();
    private TooltipWidget tooltipWidget;

    public ConfigScreen(Screen parent) {
        super(Text.translatable(Constants.CONFIG_KEY_PREFIX + "title"));
        this.parent = parent;
        this.workingConfig = new Config();
        Config.copy(Config.getInstance(), this.workingConfig);
    }

    @Override
    protected void init() {
        super.init();
        this.tooltipWidget = new TooltipWidget(Objects.requireNonNull(this.client));
        initializeTooltips();
        int uiWidth = Math.min(this.width - (10 * 5) * 2 - 25 * 2, MAX_UI_WIDTH);
        int scrollListLeftOffset = 0;
        TextInputWidget searchField = new TextInputWidget(this.textRenderer, this.width / 2 - 100, 30, 200, 20, Text.translatable("bplb.config.search"));
        searchField.setChangedListener(text -> {
            this.searchQuery = text.toLowerCase();
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

    private void initializeTooltips() {
        tooltipTexts.put("modEnabled", Text.translatable("bplb.config.tooltip.modEnabled"));
        tooltipTexts.put("apply_hotbar_offset", Text.translatable("bplb.config.tooltip.apply_hotbar_offset"));
        tooltipTexts.put("always_show_player_heads", Text.translatable("bplb.config.tooltip.always_show_player_heads"));
        tooltipTexts.put("always_show_player_names", Text.translatable("bplb.config.tooltip.always_show_player_names"));
        tooltipTexts.put("max_visible_icons", Text.translatable("bplb.config.tooltip.max_visible_icons"));
        tooltipTexts.put("lerp_speed", Text.translatable("bplb.config.tooltip.lerp_speed"));
        tooltipTexts.put("icon_size", Text.translatable("bplb.config.tooltip.icon_size"));
        tooltipTexts.put("dot_type", Text.translatable("bplb.config.tooltip.dot_type"));
        tooltipTexts.put("icon_border_style", Text.translatable("bplb.config.tooltip.icon_border_style"));
        tooltipTexts.put("icon_border_type", Text.translatable("bplb.config.tooltip.icon_border_type"));
        tooltipTexts.put("inherit_border_color", Text.translatable("bplb.config.tooltip.inherit_border_color"));
        tooltipTexts.put("height_difference_mode", Text.translatable("bplb.config.tooltip.height_difference_mode"));
        tooltipTexts.put("arrow_type", Text.translatable("bplb.config.tooltip.arrow_type"));
        tooltipTexts.put("vertical_padding", Text.translatable("bplb.config.tooltip.vertical_padding"));
        tooltipTexts.put("adjust_to_fov", Text.translatable("bplb.config.tooltip.adjust_to_fov"));
        tooltipTexts.put("fov_multiplier", Text.translatable("bplb.config.tooltip.fov_multiplier"));
        tooltipTexts.put("death_marker_type", Text.translatable("bplb.config.tooltip.death_marker_type"));
        tooltipTexts.put("death_marker_border_type", Text.translatable("bplb.config.tooltip.death_marker_border_type"));
        tooltipTexts.put("death_marker_inherit_border_color", Text.translatable("bplb.config.tooltip.death_marker_inherit_border_color"));
        tooltipTexts.put("nameplate_scale", Text.translatable("bplb.config.tooltip.nameplate_scale"));
        tooltipTexts.put("name_border_style", Text.translatable("bplb.config.tooltip.name_border_style"));
        tooltipTexts.put("fade_start_distance", Text.translatable("bplb.config.tooltip.fade_start_distance"));
        tooltipTexts.put("fade_end_distance", Text.translatable("bplb.config.tooltip.fade_end_distance"));
        tooltipTexts.put("fade_alpha_max", Text.translatable("bplb.config.tooltip.fade_alpha_max"));
        tooltipTexts.put("fade_alpha_min", Text.translatable("bplb.config.tooltip.fade_alpha_min"));
    }

    public void rebuildList() {
        if (this.client != null) {
            double scroll = this.scrollableList.getScrollAmount();
            this.init(this.client, this.width, this.height);
            this.scrollableList.setScrollAmount(scroll);
        }
    }

    private void rebuildListWithoutScroll() {
        if (this.client == null) return;

        double scroll = this.scrollableList.getScrollAmount();
        scrollableList.clearEntries();
        populateOptions();
        this.scrollableList.setScrollAmount(scroll);
    }

    @Override
    public void setFocused(Element focused) {
        Element oldFocused = this.getFocused();
        super.setFocused(focused);
        if (oldFocused != focused && !(focused instanceof TextInputWidget)) {
            rebuildListWithoutScroll();
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
        this.scrollableList.setScrollAmount(0);
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
            addIfMatch("global_hud_y_offset", () -> addIntSlider("global_hud_y_offset", workingConfig.getGlobalHudYOffset(), -100, 100, workingConfig::setGlobalHudYOffset));
            addIfMatch("apply_hotbar_offset", () -> addToggle("apply_hotbar_offset", workingConfig.isApplyHotbarOffset(), workingConfig::setApplyHotbarOffset));
            addIfMatch("always_show_player_heads", () -> addToggle("always_show_player_heads", workingConfig.isAlwaysShowPlayerHeads(), workingConfig::setAlwaysShowPlayerHeads));
            addIfMatch("always_show_player_names", () -> addToggle("always_show_player_names", workingConfig.isAlwaysShowPlayerNames(), workingConfig::setAlwaysShowPlayerNames));
            addIfMatch("max_visible_icons", () -> addIntSlider("max_visible_icons", workingConfig.getMaxVisibleIcons(), 1, 200, workingConfig::setMaxVisibleIcons));
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
                    markDirty();
                }, this::markDirty, true));
            }

            addIfMatch("vertical_padding", () -> addIntSlider("vertical_padding", workingConfig.getVerticalPadding(), 0, 4, workingConfig::setVerticalPadding));
            addIfMatch("adjust_to_fov", () -> addToggle("adjust_to_fov", workingConfig.isAdjustToFov(), workingConfig::setAdjustToFov));
            addIfMatch("fov_multiplier", () -> addFloatSlider("fov_multiplier", workingConfig.getFovMultiplier(), 0.5f, 2.0f, workingConfig::setFovMultiplier));
        }

        if (hasAnyMatch("enable_icon_clustering", "enable_cluster_size_scaling")) {
            addSection("experimental");
            addIfMatch("enable_icon_clustering", () -> addToggle("enable_icon_clustering", workingConfig.isEnableIconClustering(), workingConfig::setEnableIconClustering));
            addIfMatch("enable_cluster_size_scaling", () -> addToggle("enable_cluster_size_scaling", workingConfig.isEnableClusterSizeScaling(), workingConfig::setEnableClusterSizeScaling));
        }

        if (hasAnyMatch("death_marker_type", "death_marker_border_type", "death_marker_inherit_border_color")) {
            addSection("markers");
            addIfMatch("death_marker_type", () -> addCycle("death_marker_type", workingConfig.getDeathMarkerType(), markerTypes, id -> getTranslatedAssetName(id, "marker"), workingConfig::setDeathMarkerType, true));
            addIfMatch("death_marker_border_type", () -> addCycle("death_marker_border_type", workingConfig.getDeathMarkerBorderType(), borderTypes, borderTypeText, workingConfig::setDeathMarkerBorderType, true));
            addIfMatch("death_marker_inherit_border_color", () -> addToggle("death_marker_inherit_border_color", workingConfig.isDeathMarkerInheritBorderColor(), workingConfig::setDeathMarkerInheritBorderColor));
        }

        if (hasAnyMatch("lodestone_marker_type", "lodestone_marker_border_type", "lodestone_marker_inherit_border_color")) {
            addSection("lodestone_markers");
            addIfMatch("lodestone_marker_type", () -> addCycle("lodestone_marker_type", workingConfig.getLodestoneMarkerType(), markerTypes, id -> getTranslatedAssetName(id, "marker"), workingConfig::setLodestoneMarkerType, true));
            addIfMatch("lodestone_marker_border_type", () -> addCycle("lodestone_marker_border_type", workingConfig.getLodestoneMarkerBorderType(), borderTypes, borderTypeText, workingConfig::setLodestoneMarkerBorderType, true));
            addIfMatch("lodestone_marker_inherit_border_color", () -> addToggle("lodestone_marker_inherit_border_color", workingConfig.isLodestoneMarkerInheritBorderColor(), workingConfig::setLodestoneMarkerInheritBorderColor));
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
            addIfMatch("fade_alpha_max", () -> scrollableList.addPublicEntry(new FloatSliderOptionEntry(this.client, "fade_alpha_max", workingConfig.getFadeAlphaMax(), workingConfig.getFadeAlphaMin(), 1.0f, val -> {
                workingConfig.setFadeAlphaMax(val);
                rebuildListWithoutScroll();
            }, this::markDirty)));
            addIfMatch("fade_alpha_min", () -> scrollableList.addPublicEntry(new FloatSliderOptionEntry(this.client, "fade_alpha_min", workingConfig.getFadeAlphaMin(), 0.0f, workingConfig.getFadeAlphaMax(), val -> {
                workingConfig.setFadeAlphaMin(val);
                rebuildListWithoutScroll();
            }, this::markDirty)));
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
                            int defaultColor = uuid != null ? ColorUtil.generateColorFromUUID(uuid) : 0xFF808080;
                            scrollableList.addPublicEntry(new ColorTextFieldEntry(this.client, "player_appearance.color", pc.color != null ? pc.color : defaultColor, color -> {
                                pc.color = color;
                                markDirty();
                            }, this));
                        });
                        addPlayerOption(name, "dot_type", () -> {
                            List<String> dotsToShow = Client.availableDots.stream().filter(d -> !d.equals("bowtie")).toList();
                            scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "player_appearance.dot_type", pc.dotType != null ? pc.dotType : "default", dotsToShow, id -> getTranslatedAssetName(id, "dot"), val -> {
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
                        addPlayerOption(name, "arrow_type", () -> scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "player_appearance.arrow_type", pc.arrowType != null ? pc.arrowType : "default", Client.availableArrows, id -> getTranslatedAssetName(id, "arrow"), val -> {
                            pc.arrowType = val;
                            markDirty();
                        }, this::markDirty, true)));
                    }
                }
            });
        }

        if (searchQuery.isEmpty()) scrollableList.addPublicEntry(new ResetSettingsEntry(this.client, this, this::markDirty, workingConfig));
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
        if (this.tooltipWidget != null) this.tooltipWidget.tick();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (this.tooltipWidget != null) this.tooltipWidget.render(context);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, Constants.WHITE_COLOR);
        renderGlobalPreview(context);

        if (Client.isLocalMode()) {
            Text warningText = Text.translatable("bplb.config.local_mode_warning").formatted(Formatting.RED);
            context.drawTextWithShadow(this.textRenderer, warningText, this.width - this.textRenderer.getWidth(warningText) - 5, this.height - 15, Constants.WHITE_COLOR);
        }
    }

    public void drawLabelWithHighlight(DrawContext context, Text label, int x, int y, String key) {
        String text = label.getString();

        if (searchQuery.isEmpty() || !searchMatches.containsKey(key)) {
            context.drawTextWithShadow(this.textRenderer, label, x, y, Constants.WHITE_COLOR);
            return;
        }

        String textLower = text.toLowerCase();
        String query = searchQuery.toLowerCase();

        boolean[] highlighted = new boolean[text.length()];
        int pos = 0;
        while ((pos = textLower.indexOf(query, pos)) != -1) {
            for (int i = pos; i < pos + query.length() && i < text.length(); i++) {
                highlighted[i] = true;
            }
            pos++;
        }

        int currentX = x;
        int start = 0;

        while (start < text.length()) {
            int end = start;
            boolean isHighlighted = highlighted[start];

            while (end < text.length() && highlighted[end] == isHighlighted) {
                end++;
            }

            String segment = text.substring(start, end);
            int color = isHighlighted ? 0xFFFFFF00 : Constants.WHITE_COLOR;
            context.drawTextWithShadow(this.textRenderer, Text.literal(segment), currentX, y, color);
            currentX += this.textRenderer.getWidth(segment);
            start = end;
        }
    }

    public void showTooltip(String key, int entryY, int entryHeight) {
        Text tooltip = tooltipTexts.get(key);
        if (tooltip != null) {
            int uiWidth = Math.min(this.width - (10 * 5) * 2 - 25 * 2, MAX_UI_WIDTH);
            int tooltipY = entryY + entryHeight;
            tooltipWidget.setHoveredTooltip(tooltip, this.width / 2 - uiWidth / 2, tooltipY, uiWidth);
        }
    }

    public void clearTooltip() {
        if (tooltipWidget != null) {
            tooltipWidget.clearTooltip();
        }
    }

    private void renderGlobalPreview(DrawContext context) {
        int previewBaseX = this.width / 2 + Math.min(this.width - (10 * 5) * 2 - 25 * 2, MAX_UI_WIDTH) / 2 + 10;
        int previewBaseY = this.height / 2;

        context.getMatrices().push();
        context.getMatrices().translate(previewBaseX, previewBaseY, 0);
        context.getMatrices().scale(5, 5, 1.0f);

        RenderAddons.renderArrow(context, workingConfig.getArrowType(), true, 1, (-workingConfig.getVerticalPadding() - Constants.ARROW_BASE_SIZE_HEIGHT - Constants.ICON_BASE_SIZE) + 1, 0/*Constants.ARROW_ANIMATOR*/);
        RenderAddons.renderPlayerIcon(context, "Preview", previewUuid, 0, 0, -Constants.ICON_BASE_SIZE, false, workingConfig);
        RenderAddons.renderArrow(context, workingConfig.getArrowType(), false, 1, (workingConfig.getVerticalPadding() + Constants.ARROW_BASE_SIZE_HEIGHT - Math.round((float) Constants.ICON_BASE_SIZE / 2)) - 1, 0/*Constants.ARROW_ANIMATOR*/);
        RenderAddons.renderDeathMarker(context, 0, (float) (Constants.ICON_BASE_SIZE), workingConfig);

        context.getMatrices().pop();
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

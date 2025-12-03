package net.bichal.bplb.gui;

import net.bichal.bplb.client.Client;
import net.bichal.bplb.gui.animation.Transition;
import net.bichal.bplb.gui.config.ConfigState;
import net.bichal.bplb.gui.config.SearchEngine;
import net.bichal.bplb.gui.widget.*;
import net.bichal.bplb.gui.widget.entry.*;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class ConfigScreen extends Screen {
    private final Screen parent;
    private final Config workingConfig;
    private final ConfigState state;
    private final SearchEngine searchEngine = new SearchEngine();
    private final TooltipRenderer tooltipRenderer = new TooltipRenderer();
    private final Transition scrollTransition = Constants.createScrollTransition();
    private final Transition tabVisibilityTransition = Constants.createTransition();
    private SearchField searchField;
    private TabBar tabBar;
    private SideNavigation sideNav;
    private ScrollableListWidget contentList;
    private int currentTab = 0;
    private boolean globalSearch = false;
    private String currentQuery = "";
    private boolean tabsVisible = true;
    private int dynamicTopOffset = 85;
    private int globalU = 0;
    private boolean isInGame = false;

    public ConfigScreen(Screen parent) {
        super(Text.translatable("bplb.config.title"));
        this.parent = parent;
        this.workingConfig = new Config();
        Config.copy(Config.getInstance(), workingConfig);
        this.state = new ConfigState(workingConfig);
    }

    @Override
    public void init() {
        if (client == null) return;

        isInGame = client.world != null;
        clearChildren();
        updateDynamicOffsets();

        searchField = new SearchField(textRenderer, width / 2 - 130, 30, 245, 20);
        searchField.setPlaceholder(Text.translatable("bplb.config.search"));
        addDrawableChild(searchField);

        CompactButton searchModeBtn = CompactButton.texture(width / 2 + 120, 30, 20, globalSearch ? 20 : 0, 0, b -> {
            globalSearch = !globalSearch;
            globalU = globalSearch ? 20 : 0;
            onSearchChanged(currentQuery);
        });
        addDrawableChild(searchModeBtn);

        tabBar = new TabBar(width / 2 - 230, 59, 460, this::onTabChanged);
        tabBar.addTab("general", Text.translatable("bplb.config.tab.general"));
        tabBar.addTab("dots", Text.translatable("bplb.config.tab.dots"));

        boolean isSingleplayer = client.getServer() != null && client.getServer().isSingleplayer();
        boolean hasOp = client.player != null && client.getServer() != null &&
                client.getServer().getPlayerManager().isOperator(client.player.getGameProfile());

        if (isSingleplayer || hasOp) {
            String tabKey = isSingleplayer ? "bplb.config.tab.world" : "bplb.config.tab.server";
            tabBar.addTab("server", Text.translatable(tabKey));
        }

        tabBar.addTab("waypoints", Text.translatable("bplb.config.tab.waypoints"));
        addDrawableChild(tabBar);

        contentList = new ScrollableListWidget(client, width, dynamicTopOffset, height, 24);
        updateContentListLayout();
        addSelectableChild(contentList);
        addDrawableChild(contentList);

        sideNav = new SideNavigation(0, dynamicTopOffset, height - dynamicTopOffset, this::scrollToSection);
        addDrawableChild(sideNav);

        ConfigFooter footer = new ConfigFooter(height - 30, width, state, this::handleFooterAction);
        addDrawableChild(footer);

        indexAllEntries();
        searchField.setChangedListener(this::onSearchChanged);
        searchField.setText(currentQuery);

        if (currentQuery.isEmpty()) {
            populateTab(currentTab);
        }
    }

    private void updateDynamicOffsets() {
        tabsVisible = currentQuery.isEmpty() || !globalSearch;
        tabVisibilityTransition.setTarget(tabsVisible ? 1f : 0f);
        float progress = tabVisibilityTransition.update();
        dynamicTopOffset = 60 + (int) (25 * progress);
    }

    private void updateContentListLayout() {
        if (client == null) return;
        int rowWidth = Math.min(width - 20, 460);
        contentList.setRowWidth(rowWidth);
        contentList.setRowLeft((width - rowWidth) / 2);
        contentList.setScrollbarX(width - 6);
    }

    private boolean sideNavHasSpace() {
        return width > 800;
    }

    private void indexAllEntries() {
        indexTab("general", 0, "modEnabled", "global_hud_y_offset", "apply_hotbar_offset", "max_visible_icons", "lerp_speed", "fade_start_distance", "fade_end_distance", "fade_alpha_max", "fade_alpha_min", "enable_icon_clustering", "enable_cluster_size_scaling", "enable_bouncing_animation");

        indexTab("dots", 1, "always_show_player_heads", "always_show_player_names", "icon_size", "nameplate_scale", "vertical_padding", "adjust_to_fov", "fov_multiplier", "dot_type", "icon_border_style", "icon_border_type", "inherit_border_color", "arrow_type", "death_marker_type", "death_marker_inherit_color", "lodestone_marker_type", "lodestone_marker_inherit_color", "lodestone_icon_size");

        for (String name : workingConfig.getPlayerConfigs().keySet()) {
            searchEngine.index("player_" + name, Text.literal(name), 3);
        }
    }

    @SuppressWarnings("unused")
    private void indexTab(String tab, int index, String... keys) {
        for (String key : keys) {
            searchEngine.index(key, Text.translatable("bplb.config." + key), index);
        }
    }

    private void onSearchChanged(String query) {
        if (contentList == null) return;
        currentQuery = query.toLowerCase();
        updateDynamicOffsets();

        if (currentQuery.isEmpty()) {
            populateTab(currentTab);
            clearHighlights();
            return;
        }

        if (globalSearch) {
            List<SearchEngine.SearchResult> results = searchEngine.search(currentQuery);
            populateSearchResults(results);
        } else {
            populateTab(currentTab);
            highlightInCurrentTab(currentQuery);
        }
    }

    private void populateSearchResults(List<SearchEngine.SearchResult> results) {
        contentList.clearEntries();
        sideNav.setSections(List.of());
        contentList.setScrollAmount(0);

        Set<String> addedKeys = new HashSet<>();
        for (SearchEngine.SearchResult result : results) {
            if (!addedKeys.add(result.key())) continue;
            Text highlighted = searchField.highlightMatches(result.label(), currentQuery);
            contentList.addPublicEntry(new SearchResultEntry(client, result, highlighted, this::jumpToEntry));
        }
    }

    private void highlightInCurrentTab(String query) {
        for (var entry : contentList.children()) {
            if (entry instanceof BaseConfigEntry baseEntry) {
                String label = baseEntry.getLabel().getString().toLowerCase();
                if (label.contains(query)) {
                    baseEntry.setHighlighted(true);
                    baseEntry.setHighlightedText(searchField.highlightMatches(baseEntry.getLabel(), query));
                    baseEntry.setPersistentHighlight(true);
                } else {
                    baseEntry.setHighlighted(false);
                    baseEntry.setPersistentHighlight(false);
                }
            }
        }
    }

    private void clearHighlights() {
        for (var entry : contentList.children()) {
            if (entry instanceof BaseConfigEntry baseEntry) {
                baseEntry.setHighlighted(false);
                baseEntry.setPersistentHighlight(false);
            }
        }
    }

    private void populateTab(int tabIndex) {
        contentList.clearEntries();
        currentTab = tabIndex;
        switch (tabIndex) {
            case 0 -> populateGeneralTab();
            case 1 -> populateDotsTab();
        case 2 -> populateServerTab();
        case 3 -> populateWaypointsTab();
        }
    }

    private void populateGeneralTab() {
        List<SideNavigation.NavSection> sections = List.of(new SideNavigation.NavSection(Text.translatable("bplb.config.section.general"), 0), new SideNavigation.NavSection(Text.translatable("bplb.config.section.experience"), 250), new SideNavigation.NavSection(Text.translatable("bplb.config.section.fading"), 400), new SideNavigation.NavSection(Text.translatable("bplb.config.section.experimental"), 600)
        );
        sideNav.setSections(sections);

        addSection("general", () -> {
            addToggle("modEnabled", workingConfig.isModEnabled(), workingConfig::setModEnabled).setTooltip(Text.translatable("bplb.config.modEnabled.tooltip"));
            addIntSlider("global_hud_y_offset", workingConfig.getGlobalHudYOffset(), -100, 100, workingConfig::setGlobalHudYOffset).setTooltip(Text.translatable("bplb.config.global_hud_y_offset.tooltip"));
            addToggle("apply_hotbar_offset", workingConfig.isApplyHotbarOffset(), workingConfig::setApplyHotbarOffset).setTooltip(Text.translatable("bplb.config.apply_hotbar_offset.tooltip"));
            addIntSlider("max_visible_icons", workingConfig.getMaxVisibleIcons(), 1, 200, workingConfig::setMaxVisibleIcons).setTooltip(Text.translatable("bplb.config.max_visible_icons.tooltip"));
            addFloatSlider("lerp_speed", workingConfig.getLerpSpeed(), 0.1f, 1.0f, workingConfig::setLerpSpeed).setTooltip(Text.translatable("bplb.config.lerp_speed.tooltip"));
        });

        addSection("experience", () -> {
            addToggle("show_experience_bar", workingConfig.isShowExperienceBar(), workingConfig::setShowExperienceBar).setTooltip(Text.translatable("bplb.config.show_experience_bar.tooltip"));
            addCycleOption("experience_bar_background", workingConfig.getExperienceBarBackground(), List.of("mojang", "custom"), workingConfig::setExperienceBarBackground).setTooltip(Text.translatable("bplb.config.experience_bar_background.tooltip"));
        });

        addSection("fading", () -> {
            addIntSlider("fade_start_distance", workingConfig.getFadeStartDistance(), 5, 9995, workingConfig::setFadeStartDistance).setTooltip(Text.translatable("bplb.config.fade_start_distance.tooltip"));
            addIntSlider("fade_end_distance", workingConfig.getFadeEndDistance(), 10, 10000, workingConfig::setFadeEndDistance).setTooltip(Text.translatable("bplb.config.fade_end_distance.tooltip"));
            addFloatSlider("fade_alpha_max", workingConfig.getFadeAlphaMax(), 0.01f, 1.0f, workingConfig::setFadeAlphaMax).setTooltip(Text.translatable("bplb.config.fade_alpha_max.tooltip"));
            addFloatSlider("fade_alpha_min", workingConfig.getFadeAlphaMin(), 0.0f, 1.0f, workingConfig::setFadeAlphaMin).setTooltip(Text.translatable("bplb.config.fade_alpha_min.tooltip"));
        });

        addSection("experimental", () -> {
            addToggle("enable_icon_clustering", workingConfig.isEnableIconClustering(), workingConfig::setEnableIconClustering).setTooltip(Text.translatable("bplb.config.enable_icon_clustering.tooltip"));
            addToggle("enable_cluster_size_scaling", workingConfig.isEnableClusterSizeScaling(), workingConfig::setEnableClusterSizeScaling).setTooltip(Text.translatable("bplb.config.enable_cluster_size_scaling.tooltip"));
            addToggle("enable_bouncing_animation", workingConfig.isEnableBouncingAnimation(), workingConfig::setEnableBouncingAnimation).setTooltip(Text.translatable("bplb.config.enable_bouncing_animation.tooltip"));
        });
    }

    private void populateDotsTab() {
        List<SideNavigation.NavSection> sections = List.of(new SideNavigation.NavSection(Text.translatable("bplb.config.section.player_dots"), 0), new SideNavigation.NavSection(Text.translatable("bplb.config.section.appearance"), 300), new SideNavigation.NavSection(Text.translatable("bplb.config.section.death_marker"), 600), new SideNavigation.NavSection(Text.translatable("bplb.config.section.lodestone_marker"), 900));
        sideNav.setSections(sections);

        addSection("player_dots", () -> {
            addToggle("always_show_player_heads", workingConfig.isAlwaysShowPlayerHeads(), workingConfig::setAlwaysShowPlayerHeads).setTooltip(Text.translatable("bplb.config.always_show_player_heads.tooltip"));
            addToggle("always_show_player_names", workingConfig.isAlwaysShowPlayerNames(), workingConfig::setAlwaysShowPlayerNames).setTooltip(Text.translatable("bplb.config.always_show_player_names.tooltip"));
            addIntSlider("icon_size", workingConfig.getIconSize(), 1, 4, workingConfig::setIconSize).setTooltip(Text.translatable("bplb.config.icon_size.tooltip"));
            addFloatSlider("nameplate_scale", workingConfig.getNameplateScale(), 0.5f, 1.5f, workingConfig::setNameplateScale).setTooltip(Text.translatable("bplb.config.nameplate_scale.tooltip"));
            addIntSlider("vertical_padding", workingConfig.getVerticalPadding(), 0, 10, workingConfig::setVerticalPadding).setTooltip(Text.translatable("bplb.config.vertical_padding.tooltip"));
            addToggle("adjust_to_fov", workingConfig.isAdjustToFov(), workingConfig::setAdjustToFov).setTooltip(Text.translatable("bplb.config.adjust_to_fov.tooltip"));
            addFloatSlider("fov_multiplier", workingConfig.getFovMultiplier(), 0.5f, 2.0f, workingConfig::setFovMultiplier).setTooltip(Text.translatable("bplb.config.fov_multiplier.tooltip"));
        });

        addSection("appearance", () -> {
            addCycleOption("dot_type", workingConfig.getDotType(), net.bichal.bplb.client.Client.availableDots, workingConfig::setDotType).setTooltip(Text.translatable("bplb.config.dot_type.tooltip"));
            addCycleOption("icon_border_style", workingConfig.getIconBorderStyle(), List.of("rounded", "squared"), workingConfig::setIconBorderStyle).setTooltip(Text.translatable("bplb.config.icon_border_style.tooltip"));
            addCycleOption("icon_border_type", workingConfig.getIconBorderType(), List.of("default", "minimal"), workingConfig::setIconBorderType).setTooltip(Text.translatable("bplb.config.icon_border_type.tooltip"));
            addToggle("inherit_border_color", workingConfig.isInheritBorderColor(), workingConfig::setInheritBorderColor).setTooltip(Text.translatable("bplb.config.inherit_border_color.tooltip"));
            addCycleOption("arrow_type", workingConfig.getArrowType(), net.bichal.bplb.client.Client.availableArrows, workingConfig::setArrowType).setTooltip(Text.translatable("bplb.config.arrow_type.tooltip"));
        });

        addSection("death_marker", () -> {
            addCycleOption("death_marker_type", workingConfig.getDeathMarkerType(), net.bichal.bplb.client.Client.availableDeathMarkers, workingConfig::setDeathMarkerType).setTooltip(Text.translatable("bplb.config.death_marker_type.tooltip"));
            addToggle("death_marker_inherit_color", workingConfig.isDeathMarkerInheritBorderColor(), workingConfig::setDeathMarkerInheritBorderColor).setTooltip(Text.translatable("bplb.config.death_marker_inherit_color.tooltip"));
            addCycleOption("death_marker_border_style", workingConfig.getDeathMarkerBorderStyle(), List.of("rounded", "squared"), workingConfig::setDeathMarkerBorderStyle).setTooltip(Text.translatable("bplb.config.death_marker_border_style.tooltip"));
            addCycleOption("death_marker_border_type", workingConfig.getDeathMarkerBorderType(), List.of("default", "minimal"), workingConfig::setDeathMarkerBorderType).setTooltip(Text.translatable("bplb.config.death_marker_border_type.tooltip"));
        });

        addSection("lodestone_marker", () -> {
            addCycleOption("lodestone_marker_type", workingConfig.getLodestoneMarkerType(), net.bichal.bplb.client.Client.availableDots, workingConfig::setLodestoneMarkerType).setTooltip(Text.translatable("bplb.config.lodestone_marker_type.tooltip"));
            addToggle("lodestone_marker_inherit_color", workingConfig.isLodestoneMarkerInheritBorderColor(), workingConfig::setLodestoneMarkerInheritBorderColor).setTooltip(Text.translatable("bplb.config.lodestone_marker_inherit_color.tooltip"));
            addCycleOption("lodestone_marker_border_style", workingConfig.getLodestoneMarkerBorderStyle(), List.of("rounded", "squared"), workingConfig::setLodestoneMarkerBorderStyle).setTooltip(Text.translatable("bplb.config.lodestone_marker_border_style.tooltip"));
            addCycleOption("lodestone_marker_border_type", workingConfig.getLodestoneMarkerBorderType(), List.of("default", "minimal"), workingConfig::setLodestoneMarkerBorderType).setTooltip(Text.translatable("bplb.config.lodestone_marker_border_type.tooltip"));
            addIntSlider("lodestone_icon_size", workingConfig.getLodestoneIconSize(), 1, 4, workingConfig::setLodestoneIconSize).setTooltip(Text.translatable("bplb.config.lodestone_icon_size.tooltip"));
        });
    }

    private void populateServerTab() {
        sideNav.setSections(List.of());

        addSection("server_general", () -> {
            addToggle("show_experience_bar", workingConfig.isShowExperienceBar(), workingConfig::setShowExperienceBar).setTooltip(Text.translatable("bplb.config.show_experience_bar.tooltip"));

            addCycleOption("experience_bar_background", workingConfig.getExperienceBarBackground(), List.of("mojang", "custom"), workingConfig::setExperienceBarBackground).setTooltip(Text.translatable("bplb.config.experience_bar_background.tooltip"));
        });
    }

    private void populateWaypointsTab() {
        List<SideNavigation.NavSection> sections = List.of(new SideNavigation.NavSection(Text.translatable("bplb.config.section.players"), 0));
        sideNav.setSections(sections);

        addSection("players", () -> {
            workingConfig.getPlayerConfigs().forEach((name, appearance) -> {
                contentList.addPublicEntry(new PlayerWaypointEntry(client, name, appearance, this::handleWaypointAction));
            });

            contentList.addPublicEntry(new AddWaypointEntry(client, Text.translatable("bplb.config.waypoint.add_player"), playerName -> {
                workingConfig.getPlayerConfigs().put(playerName, new Config.PlayerAppearance());
                markDirty();
                populateTab(3);
            }));
        });
    }

    private void handleWaypointAction(String playerName, PlayerWaypointEntry.Action action) {
        switch (action) {
        case DELETE -> {
            workingConfig.getPlayerConfigs().remove(playerName);
            markDirty();
            populateTab(3);
        }
        case TOGGLE_VISIBILITY, PASTE_STYLES -> markDirty();
        case COPY_STYLES -> {
        }
        }
    }

    private void addSection(String key, Runnable contentAdder) {
        SectionEntry section = new SectionEntry(client, Text.translatable("bplb.config.section." + key), workingConfig.isSectionExpanded(key), expanded -> {
            workingConfig.setSectionExpanded(key, expanded);
            updateSectionContent(key, expanded, contentAdder);
        });
        section.setCollapsible(true);
        contentList.addPublicEntry(section);

        if (workingConfig.isSectionExpanded(key)) {
            contentAdder.run();
        }
    }

    private void updateSectionContent(String key, boolean expanded, Runnable contentAdder) {
        int sectionIndex = -1;
        for (int i = 0; i < contentList.children().size(); i++) {
            var entry = contentList.children().get(i);
            if (entry instanceof SectionEntry se && se.getConfigKey().equals(key)) {
                sectionIndex = i;
                break;
            }
        }

        if (sectionIndex == -1) return;

        List<ScrollableListWidget.Entry> toRemove = new ArrayList<>();
        for (int i = sectionIndex + 1; i < contentList.children().size(); i++) {
            var entry = contentList.children().get(i);
            if (entry instanceof SectionEntry) break;
            toRemove.add(entry);
        }

        contentList.children().removeAll(toRemove);

        if (expanded) {
            int insertIndex = sectionIndex + 1;
            List<ScrollableListWidget.Entry> newEntries = new ArrayList<>();
            ScrollableListWidget tempList = new ScrollableListWidget(client, width, dynamicTopOffset, height, 24);

            Runnable oldAdder = () -> contentList.addPublicEntry(null);
            contentAdder.run();

            for (int i = insertIndex; i < contentList.children().size(); i++) {
                if (contentList.children().get(i) instanceof SectionEntry) break;
            }
        }
    }

    private BaseConfigEntry addToggle(String key, boolean value, Consumer<Boolean> setter) {
        ToggleEntry entry = new ToggleEntry(client, key, Text.translatable("bplb.config." + key), value, v -> {
            setter.accept(v);
            markDirty();
        });
        contentList.addPublicEntry(entry);
        return entry;
    }

    private BaseConfigEntry addIntSlider(String key, int value, int min, int max, Consumer<Integer> setter) {
        SliderEntry entry = new SliderEntry(client, key, Text.translatable("bplb.config." + key), value, min, max, v -> {
            setter.accept(v.intValue());
            markDirty();
        }, true);
        contentList.addPublicEntry(entry);
        return entry;
    }

    private BaseConfigEntry addFloatSlider(String key, float value, float min, float max, Consumer<Float> setter) {
        SliderEntry entry = new SliderEntry(client, key, Text.translatable("bplb.config." + key), value, min, max, v -> {
            setter.accept(v);
            markDirty();
        }, false);
        contentList.addPublicEntry(entry);
        return entry;
    }

    private BaseConfigEntry addCycleOption(String key, String current, List<String> options, Consumer<String> setter) {
        CycleEntry entry = new CycleEntry(client, key, Text.translatable("bplb.config." + key), current, options, v -> {
            setter.accept(v);
            markDirty();
        });
        contentList.addPublicEntry(entry);
        return entry;
    }

    public void markDirty() {
        state.pushChange(workingConfig);
    }

    private void onTabChanged(int index) {
        currentTab = index;
        contentList.setScrollAmount(0);
        populateTab(index);
    }

    private void scrollToSection(int targetY) {
        scrollTransition.setTarget(targetY);
    }

    private void jumpToEntry(SearchEngine.SearchResult result) {
        currentQuery = "";
        searchField.setText("");
        globalSearch = false;
        globalU = 0;
        updateDynamicOffsets();

        int targetTab = result.tabIndex();
        currentTab = targetTab;
        tabBar.selectedTab = targetTab;
        onTabChanged(targetTab);

        contentList.setScrollAmount(0);

        if (client != null) {
            client.execute(() -> {
                int entryIndex = 0;
                for (var entry : contentList.children()) {
                    if (entry instanceof BaseConfigEntry baseEntry && baseEntry.getConfigKey().equals(result.key())) {
                        baseEntry.setHighlighted(true);
                        baseEntry.setPersistentHighlight(true);
                        int targetY = Math.max(0, entryIndex * 24 - (height - Constants.HEADER_HEIGHT - Constants.FOOTER_HEIGHT) / 2);
                        scrollTransition.setTarget(targetY);
                        break;
                    }
                    entryIndex++;
                }
            });
        }
    }

    private void handleFooterAction(ConfigFooter.Action action) {
        switch (action) {
            case UNDO -> {
                if (state.undo(workingConfig)) populateTab(currentTab);
            }
            case REDO -> {
                if (state.redo(workingConfig)) populateTab(currentTab);
            }
            case APPLY -> {
                Config.copy(workingConfig, Config.getInstance());
                Config.getInstance().save();
                state.markClean();
            }
        case DONE -> close();
            case CANCEL -> {
                if (state.isDirty()) {
                    state.undo(workingConfig);
                    Config.copy(Config.getInstance(), workingConfig);
                    state.markClean();
                } else {
                    close();
                }
            }
            case RESET -> {
                workingConfig.resetToDefaults();
                markDirty();
                populateTab(currentTab);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updateDynamicOffsets();

        if (sideNav != null) {
            sideNav.setY(dynamicTopOffset);
            sideNav.setHeight(height - dynamicTopOffset - 30);
            sideNav.visible = sideNavHasSpace();
        }

        if (contentList != null) {
            contentList.setY(dynamicTopOffset);
            contentList.setHeight(height - dynamicTopOffset - 35);
            updateContentListLayout();
        }

        if (tabBar != null) {
            tabBar.visible = tabsVisible;
            tabBar.active = tabsVisible;
        }

        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFF);

        float targetScroll = scrollTransition.update();
        if (scrollTransition.isAnimating()) contentList.setScrollAmount(targetScroll);

        if (isInGame) renderModeIndicator(context);

        tooltipRenderer.render(context, height, height - 30);
    }

    private void renderModeIndicator(DrawContext context) {
        boolean isLocal = Client.isLocalMode();
        String modeText = isLocal ? "LOCAL" : "SERVER";
        int color = isLocal ? 0xFFAA00 : 0x00FF00;

        int textWidth = textRenderer.getWidth(modeText);
        int padding = 6;
        int boxWidth = textWidth + padding * 2;
        int boxHeight = 16;
        int x = width - boxWidth - 10;
        int y = height - boxHeight - 10;

        context.fill(x, y, x + boxWidth, y + boxHeight, 0xE0000000);
        context.drawBorder(x, y, boxWidth, boxHeight, 0xFF404040);
        context.drawText(textRenderer, modeText, x + padding, y + 4, color, true);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    public void showTooltip(Text tooltip, int x, int y, int width, int maxHeight) {
        tooltipRenderer.setTooltip(tooltip, x, y, width, maxHeight, false);
    }

    public void clearTooltip() {
        tooltipRenderer.clear();
    }
}

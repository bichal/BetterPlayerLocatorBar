package net.bichal.bplb.gui;

import net.bichal.bplb.gui.animation.Transition;
import net.bichal.bplb.gui.config.ConfigState;
import net.bichal.bplb.gui.config.SearchEngine;
import net.bichal.bplb.gui.widget.*;
import net.bichal.bplb.gui.widget.entry.*;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

        updateDynamicOffsets();

        searchField = new SearchField(textRenderer, width / 2 - 135, 35, 250, 20);
        searchField.setPlaceholder(Text.translatable("bplb.config.search"));
        searchField.setChangedListener(this::onSearchChanged);
        addDrawableChild(searchField);

        CompactButton searchModeBtn = new CompactButton(width / 2 + 120, 35, 25, 20, Text.literal(globalSearch ? "🌐" : "📄"), b -> {
            globalSearch = !globalSearch;
            b.setMessage(Text.literal(globalSearch ? "🌐" : "📄"));
            onSearchChanged(currentQuery);
        });
        addDrawableChild(searchModeBtn);

        tabBar = new TabBar(width / 2 - 230, 60, 460, this::onTabChanged);
        tabBar.addTab("general", Text.translatable("bplb.config.tab.general"));
        tabBar.addTab("dots", Text.translatable("bplb.config.tab.dots"));

        boolean isSingleplayer = client.getServer() != null && client.getServer().isSingleplayer();
        boolean hasOp = client.player != null && client.getServer() != null && client.getServer().getPlayerManager().isOperator(client.player.getGameProfile());
        if (isSingleplayer || hasOp) {
            String tabKey = isSingleplayer ? "bplb.config.tab.world" : "bplb.config.tab.server";
            tabBar.addTab("server", Text.translatable(tabKey));
        }
        tabBar.addTab("waypoints", Text.translatable("bplb.config.tab.waypoints"));
        addDrawableChild(tabBar);

        sideNav = new SideNavigation(0, dynamicTopOffset, height - dynamicTopOffset - 30, this::scrollToSection);
        addDrawableChild(sideNav);

        contentList = new ScrollableListWidget(client, width, dynamicTopOffset, height - 35, 24);
        updateContentListLayout();
        addSelectableChild(contentList);
        addDrawableChild(contentList);

        ConfigFooter footer = new ConfigFooter(height - 30, width, state, this::handleFooterAction);
        addDrawableChild(footer);

        indexAllEntries();
        populateTab(currentTab);
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
        contentList.setScrollbarX(width - 10);
    }

    private boolean sideNavHasSpace() {
        return width > 800;
    }

    private void indexAllEntries() {
        indexTab("general", 0, "modEnabled", "global_hud_y_offset", "apply_hotbar_offset", "max_visible_icons", "lerp_speed", "fade_start_distance", "fade_end_distance", "fade_alpha_max", "fade_alpha_min", "enable_icon_clustering", "enable_cluster_size_scaling", "enable_bouncing_animation");
        indexTab("dots", 1, "always_show_player_heads", "always_show_player_names", "icon_size", "dot_type", "icon_border_style", "arrow_type", "death_marker_type", "lodestone_marker_type");

        for (String name : workingConfig.getPlayerConfigs().keySet()) {
            searchEngine.index("player_" + name, Text.literal(name), 3);
        }
    }

    private void indexTab(String tab, int index, String... keys) {
        for (String key : keys) {
            searchEngine.index(key, Text.translatable("bplb.config." + key), index);
        }
    }

    private void onSearchChanged(String query) {
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
            highlightInCurrentTab(currentQuery);
        }
    }

    private void populateSearchResults(List<SearchEngine.SearchResult> results) {
        contentList.clearEntries();
        sideNav.setSections(List.of());
        contentList.setScrollAmount(0);

        contentList.addPublicEntry(new SectionEntry(client, Text.translatable("bplb.config.search.results", results.size()), true, e -> {}));

        Set<String> addedKeys = new HashSet<>();
        for (SearchEngine.SearchResult result : results) {
            if (!addedKeys.add(result.key())) continue;

            Text highlighted = searchField.highlightMatches(result.label(), currentQuery);
            contentList.addPublicEntry(new SearchResultEntry(client, result, highlighted, this::jumpToEntry));
        }
    }

    private void highlightInCurrentTab(String query) {
        clearHighlights();
        for (var entry : contentList.children()) {
            if (entry instanceof BaseConfigEntry baseEntry) {
                String label = baseEntry.getLabel().getString().toLowerCase();
                if (label.contains(query)) {
                    baseEntry.setHighlighted(true);
                    baseEntry.setHighlightedText(searchField.highlightMatches(baseEntry.getLabel(), query));
                }
            }
        }
    }

    private void clearHighlights() {
        for (var entry : contentList.children()) {
            if (entry instanceof BaseConfigEntry baseEntry) {
                baseEntry.setHighlighted(false);
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
        List<SideNavigation.NavSection> sections = List.of(new SideNavigation.NavSection(Text.translatable("bplb.config.section.general"), 0), new SideNavigation.NavSection(Text.translatable("bplb.config.section.fading"), 200), new SideNavigation.NavSection(Text.translatable("bplb.config.section.experimental"), 400));
        sideNav.setSections(sections);

        addSection("general");
        addToggle("modEnabled", workingConfig.isModEnabled(), workingConfig::setModEnabled);
        addIntSlider("global_hud_y_offset", workingConfig.getGlobalHudYOffset(), -100, 100, workingConfig::setGlobalHudYOffset);
        addToggle("apply_hotbar_offset", workingConfig.isApplyHotbarOffset(), workingConfig::setApplyHotbarOffset);
        addIntSlider("max_visible_icons", workingConfig.getMaxVisibleIcons(), 1, 200, workingConfig::setMaxVisibleIcons);
        addFloatSlider("lerp_speed", workingConfig.getLerpSpeed(), 0.1f, 1.0f, workingConfig::setLerpSpeed);
        addSection("fading");
        addIntSlider("fade_start_distance", workingConfig.getFadeStartDistance(), 5, 9995, workingConfig::setFadeStartDistance);
        addIntSlider("fade_end_distance", workingConfig.getFadeEndDistance(), 10, 10000, workingConfig::setFadeEndDistance);
        addFloatSlider("fade_alpha_max", workingConfig.getFadeAlphaMax(), 0.01f, 1.0f, workingConfig::setFadeAlphaMax);
        addFloatSlider("fade_alpha_min", workingConfig.getFadeAlphaMin(), 0.0f, 1.0f, workingConfig::setFadeAlphaMin);
        addSection("experimental");
        addToggle("enable_icon_clustering", workingConfig.isEnableIconClustering(), workingConfig::setEnableIconClustering);
        addToggle("enable_cluster_size_scaling", workingConfig.isEnableClusterSizeScaling(), workingConfig::setEnableClusterSizeScaling);
        addToggle("enable_bouncing_animation", workingConfig.isEnableBouncingAnimation(), workingConfig::setEnableBouncingAnimation);
    }

    private void populateDotsTab() {
        List<SideNavigation.NavSection> sections = List.of(new SideNavigation.NavSection(Text.translatable("bplb.config.section.player_dots"), 0), new SideNavigation.NavSection(Text.translatable("bplb.config.section.death_marker"), 300), new SideNavigation.NavSection(Text.translatable("bplb.config.section.lodestone_marker"), 500));
        sideNav.setSections(sections);

        addSection("player_dots");
        addToggle("always_show_player_heads", workingConfig.isAlwaysShowPlayerHeads(), workingConfig::setAlwaysShowPlayerHeads);
        addToggle("always_show_player_names", workingConfig.isAlwaysShowPlayerNames(), workingConfig::setAlwaysShowPlayerNames);
        addIntSlider("icon_size", workingConfig.getIconSize(), 1, 4, workingConfig::setIconSize);
    }

    private void populateServerTab() {
        sideNav.setSections(List.of());
        addSection("server_general");
    }

    private void populateWaypointsTab() {
        List<SideNavigation.NavSection> sections = List.of(new SideNavigation.NavSection(Text.translatable("bplb.config.section.players"), 0), new SideNavigation.NavSection(Text.translatable("bplb.config.section.death_markers"), 300), new SideNavigation.NavSection(Text.translatable("bplb.config.section.lodestones"), 600));
        sideNav.setSections(sections);

        addSection("players");
        workingConfig.getPlayerConfigs().forEach((name, appearance) -> {
            contentList.addPublicEntry(new WaypointEntry(client, name, UUID.randomUUID(), appearance, WaypointEntry.WaypointType.PLAYER, action -> markDirty()));
        });
        contentList.addPublicEntry(new AddWaypointEntry(client, Text.translatable("bplb.config.waypoint.add_player"), playerName -> {
            workingConfig.getPlayerConfigs().put(playerName, new Config.PlayerAppearance());
            markDirty();
            populateTab(3);
        }));

        addSection("death_markers");
        addSection("lodestones");
    }

    private void addSection(String key) {
        contentList.addPublicEntry(new SectionEntry(client, Text.translatable("bplb.config.section." + key), true, e -> {}));
    }

    private void addToggle(String key, boolean value, java.util.function.Consumer<Boolean> setter) {
        contentList.addPublicEntry(new ToggleEntry(client, key, Text.translatable("bplb.config." + key), value, v -> {
            setter.accept(v);
            markDirty();
        }));
    }

    private void addIntSlider(String key, int value, int min, int max, java.util.function.Consumer<Integer> setter) {
        contentList.addPublicEntry(new SliderEntry(client, key, Text.translatable("bplb.config." + key), value, min, max, v -> {
            setter.accept(v.intValue());
            markDirty();
        }, true));
    }

    private void addFloatSlider(String key, float value, float min, float max, java.util.function.Consumer<Float> setter) {
        contentList.addPublicEntry(new SliderEntry(client, key, Text.translatable("bplb.config." + key), value, min, max, v -> {
            setter.accept(v);
            markDirty();
        }, false));
    }

    public void markDirty() {
        state.pushChange(workingConfig);
    }

    private void onTabChanged(int index) {
        currentTab = index;
        populateTab(index);
    }

    private void scrollToSection(int targetY) {
        scrollTransition.setTarget(targetY);
    }

    private void jumpToEntry(SearchEngine.SearchResult result) {
        currentQuery = "";
        searchField.setText("");
        globalSearch = false;
        updateDynamicOffsets();

        onTabChanged(result.tabIndex());

        contentList.setScrollAmount(0);
        if (client != null) {
            client.execute(() -> {
                int entryIndex = 0;
                for (var entry : contentList.children()) {
                    if (entry instanceof BaseConfigEntry baseEntry && baseEntry.getConfigKey().equals(result.key())) {
                        baseEntry.setHighlighted(true);
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
            case DONE -> {
                if (state.isDirty()) handleFooterAction(ConfigFooter.Action.APPLY);
                close();
            }
            case CANCEL -> close();
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
        if (scrollTransition.isAnimating()) {
            contentList.setScrollAmount(targetScroll);
        }

        tooltipRenderer.render(context, height, height - 30);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    public void showTooltip(Text tooltip, int x, int y, int width, int maxHeight) {
        tooltipRenderer.setTooltip(tooltip, x, y, width, maxHeight, true);
    }

    public void clearTooltip() {
        tooltipRenderer.clear();
    }
}

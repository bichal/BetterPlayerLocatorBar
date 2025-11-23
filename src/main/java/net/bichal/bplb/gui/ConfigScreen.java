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

        searchField = new SearchField(textRenderer, width / 2 - 130, 30, 245, 20);
        searchField.setPlaceholder(Text.translatable("bplb.config.search"));
        searchField.setChangedListener(this::onSearchChanged);
        addDrawableChild(searchField);

        final CompactButton[] searchModeBtn = new CompactButton[1];
        searchModeBtn[0] = CompactButton.texture(width / 2 + 120, 30, 20, globalU, 0, b -> {
            globalSearch = !globalSearch;
            globalU = globalSearch ? 20 : 0;
            onSearchChanged(currentQuery);
            init();
        });
        addDrawableChild(searchModeBtn[0]);

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

    @SuppressWarnings("unused")
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
            case 2 -> populateWaypointsTab();
            case 3 -> populateServerTab();
        }
    }

    private void populateGeneralTab() {
        List<SideNavigation.NavSection> sections = List.of(
                new SideNavigation.NavSection(Text.translatable("bplb.config.section.general"), 0),
                new SideNavigation.NavSection(Text.translatable("bplb.config.section.fading"), 200),
                new SideNavigation.NavSection(Text.translatable("bplb.config.section.experimental"), 400)
        );
        sideNav.setSections(sections);

        SectionEntry generalSection = new SectionEntry(client, Text.translatable("bplb.config.section.general"), true, expanded -> {});
        contentList.addPublicEntry(generalSection);

        if (generalSection.isExpanded()) {
            addToggle("modEnabled", workingConfig.isModEnabled(), workingConfig::setModEnabled);
            addIntSlider("global_hud_y_offset", workingConfig.getGlobalHudYOffset(), -100, 100, workingConfig::setGlobalHudYOffset);
            addToggle("apply_hotbar_offset", workingConfig.isApplyHotbarOffset(), workingConfig::setApplyHotbarOffset);
            addIntSlider("max_visible_icons", workingConfig.getMaxVisibleIcons(), 1, 200, workingConfig::setMaxVisibleIcons);
            addFloatSlider("lerp_speed", workingConfig.getLerpSpeed(), 0.1f, 1.0f, workingConfig::setLerpSpeed);
        }

        SectionEntry fadingSection = new SectionEntry(client, Text.translatable("bplb.config.section.fading"), true, expanded -> {});
        contentList.addPublicEntry(fadingSection);

        if (fadingSection.isExpanded()) {
            addIntSlider("fade_start_distance", workingConfig.getFadeStartDistance(), 5, 9995, workingConfig::setFadeStartDistance);
            addIntSlider("fade_end_distance", workingConfig.getFadeEndDistance(), 10, 10000, workingConfig::setFadeEndDistance);
            addFloatSlider("fade_alpha_max", workingConfig.getFadeAlphaMax(), 0.01f, 1.0f, workingConfig::setFadeAlphaMax);
            addFloatSlider("fade_alpha_min", workingConfig.getFadeAlphaMin(), 0.0f, 1.0f, workingConfig::setFadeAlphaMin);
        }

        SectionEntry experimentalSection = new SectionEntry(client, Text.translatable("bplb.config.section.experimental"), true, expanded -> {});
        contentList.addPublicEntry(experimentalSection);

        if (experimentalSection.isExpanded()) {
            addToggle("enable_icon_clustering", workingConfig.isEnableIconClustering(), workingConfig::setEnableIconClustering);
            addToggle("enable_cluster_size_scaling", workingConfig.isEnableClusterSizeScaling(), workingConfig::setEnableClusterSizeScaling);
            addToggle("enable_bouncing_animation", workingConfig.isEnableBouncingAnimation(), workingConfig::setEnableBouncingAnimation);
        }
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
        workingConfig.getPlayerConfigs().forEach((name, appearance) -> contentList.addPublicEntry(new WaypointEntry(client, name, UUID.randomUUID(), appearance, WaypointEntry.WaypointType.PLAYER, action -> markDirty())));
        contentList.addPublicEntry(new AddWaypointEntry(client, Text.translatable("bplb.config.waypoint.add_player"), playerName -> {
            workingConfig.getPlayerConfigs().put(playerName, new Config.PlayerAppearance());
            markDirty();
            populateTab(3);
        }));

        addSection("death_markers");
        addSection("lodestones");
    }

    private void addSection(String key) {
        contentList.addPublicEntry(new SectionEntry(client, Text.translatable("bplb.config.section." + key), true, expanded -> populateTab(currentTab)));
    }

    private void addToggle(String key, boolean value, Consumer<Boolean> setter) {
        contentList.addPublicEntry(new ToggleEntry(client, key, Text.translatable("bplb.config." + key), value, v -> {
            setter.accept(v);
            markDirty();
        }));
    }

    private void addIntSlider(String key, int value, int min, int max, Consumer<Integer> setter) {
        contentList.addPublicEntry(new SliderEntry(client, key, Text.translatable("bplb.config." + key), value, min, max, v -> {
            setter.accept(v.intValue());
            markDirty();
        }, true));
    }

    @SuppressWarnings("SameParameterValue")
    private void addFloatSlider(String key, float value, float min, float max, Consumer<Float> setter) {
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
        scrollToSection(0);
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
            case DONE -> {
                if (state.isDirty()) handleFooterAction(ConfigFooter.Action.APPLY);
                close();
            }
            case CANCEL -> {
                state.undo(workingConfig);
                Config.copy(Config.getInstance(), workingConfig);
                state.markClean();
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
        if (scrollTransition.isAnimating()) {
            contentList.setScrollAmount(targetScroll);
        }

        tooltipRenderer.render(context, height, height - 30);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    @SuppressWarnings("unused")
    public void showTooltip(Text tooltip, int x, int y, int width, int maxHeight) {
        tooltipRenderer.setTooltip(tooltip, x, y, width, maxHeight, true);
    }

    public void clearTooltip() {
        tooltipRenderer.clear();
    }
}

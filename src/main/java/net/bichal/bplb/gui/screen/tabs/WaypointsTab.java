package net.bichal.bplb.gui.screen.tabs;

import net.bichal.bichalutils.util.ColorUtil;
import net.bichal.bplb.client.Client;
import net.bichal.bplb.gui.Config;
import net.bichal.bplb.gui.ConfigScreen;
import net.bichal.bplb.gui.widget.SideNavigationWidget;
import net.bichal.bplb.gui.widget.entries.*;
import net.minecraft.text.Text;

import java.util.*;

import static net.bichal.bplb.client.render.RenderAddons.getUuidFromCache;

public class WaypointsTab extends BaseConfigTab {
    private final Map<String, Boolean> expandedPlayers = new HashMap<>();
    
    public WaypointsTab(ConfigScreen parent, Config workingConfig) {
        super(parent, workingConfig);
    }

    @Override
    protected void init() {
        super.init();
        if (this.client == null) return;
        
        List<SideNavigationWidget.SectionDefinition> sections = new ArrayList<>();
        sections.add(new SideNavigationWidget.SectionDefinition(Text.translatable("bplb.config.section.death_markers"), 60));
        sections.add(new SideNavigationWidget.SectionDefinition(Text.translatable("bplb.config.section.lodestone_markers"), 200));
        sections.add(new SideNavigationWidget.SectionDefinition(Text.translatable("bplb.config.section.player_waypoints"), 400));
        
        initSideNav(sections);
        initScrollableList();
        populateOptions();
        
        this.addSelectableChild(this.scrollableList);
        this.addDrawableChild(this.scrollableList);
    }

    @Override
    protected void populateOptions() {
        scrollableList.addPublicEntry(new SectionHeaderEntry(this.client, Text.translatable("bplb.config.section.death_markers")));
        
        scrollableList.addPublicEntry(new SectionHeaderEntry(this.client, Text.translatable("bplb.config.section.lodestone_markers")));
        
        scrollableList.addPublicEntry(new SectionHeaderEntry(this.client, Text.translatable("bplb.config.section.player_waypoints")));
        scrollableList.addPublicEntry(new AddPlayerEntry(this.client, parent, workingConfig));
        
        workingConfig.getPlayerConfigs().keySet().stream().sorted().forEach(name -> {
            boolean expanded = expandedPlayers.getOrDefault(name, false);
            scrollableList.addPublicEntry(new PlayerWaypointEntry(this.client, parent, workingConfig, name, expanded, this::togglePlayer));
            
            if (expanded) {
                Config.PlayerAppearance pc = workingConfig.getPlayerConfigs().get(name);
                if (pc != null) {
                    addPlayerOptions(name, pc);
                }
            }
        });
    }

    private void addPlayerOptions(String name, Config.PlayerAppearance pc) {
        UUID uuid = getUuidFromCache(name);
        int defaultColor = uuid != null ? ColorUtil.generateColorFromUUID(uuid) : 0xFF808080;
        
        scrollableList.addPublicEntry(new ColorTextFieldEntry(this.client, "player_appearance.color", pc.color != null ? pc.color : defaultColor, color -> {
            pc.color = color;
            parent.markDirty();
        }, parent));
        
        List<String> dotsToShow = Client.availableDots.stream().filter(d -> !d.equals("bowtie")).toList();
        scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "player_appearance.dot_type", pc.dotType != null ? pc.dotType : "default", dotsToShow, id -> Text.translatable("bplb.config.asset.dot." + id), val -> {
            pc.dotType = val;
            parent.markDirty();
        }, parent::markDirty, true));
        
        scrollableList.addPublicEntry(new TextureOverrideEntry(this.client, pc, parent));
    }

    private void togglePlayer(String playerName) {
        expandedPlayers.compute(playerName, (k, v) -> v == null || !v);
        rebuildList();
    }

    private void rebuildList() {
        if (this.client == null) return;
        double scroll = this.scrollableList.getScrollAmount();
        scrollableList.clearEntries();
        populateOptions();
        this.scrollableList.setScrollAmount(scroll);
    }
}
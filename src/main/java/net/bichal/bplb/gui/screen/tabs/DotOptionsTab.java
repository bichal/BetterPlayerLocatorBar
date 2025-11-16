package net.bichal.bplb.gui.screen.tabs;

import net.bichal.bplb.client.Client;
import net.bichal.bplb.gui.Config;
import net.bichal.bplb.gui.ConfigScreen;
import net.bichal.bplb.gui.widget.SideNavigationWidget;
import net.bichal.bplb.gui.widget.entries.*;
import net.bichal.bplb.util.Constants;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class DotOptionsTab extends BaseConfigTab {
    
    public DotOptionsTab(ConfigScreen parent, Config workingConfig) {
        super(parent, workingConfig);
    }

    @Override
    protected void init() {
        super.init();
        if (this.client == null) return;
        
        List<SideNavigationWidget.SectionDefinition> sections = new ArrayList<>();
        sections.add(new SideNavigationWidget.SectionDefinition(Text.translatable("bplb.config.section.player_dots"), 60));
        sections.add(new SideNavigationWidget.SectionDefinition(Text.translatable("bplb.config.section.death_marker"), 300));
        sections.add(new SideNavigationWidget.SectionDefinition(Text.translatable("bplb.config.section.lodestone_marker"), 500));
        sections.add(new SideNavigationWidget.SectionDefinition(Text.translatable("bplb.config.section.nameplates"), 700));
        
        initSideNav(sections);
        initScrollableList();
        populateOptions();
        
        this.addSelectableChild(this.scrollableList);
        this.addDrawableChild(this.scrollableList);
    }

    @Override
    protected void populateOptions() {
        List<String> borderStyles = Arrays.asList(Constants.BORDER_STYLE_ROUNDED, Constants.BORDER_STYLE_SQUARED);
        List<String> borderTypes = Arrays.asList("default", "minimal");
        List<String> heightModes = Arrays.asList("player", "camera");
        
        Function<String, Text> borderStyleText = value -> Text.translatable(Constants.CONFIG_KEY_PREFIX + "border_style." + value.toLowerCase());
        Function<String, Text> borderTypeText = value -> Text.translatable(Constants.CONFIG_KEY_PREFIX + "border_type." + value.toLowerCase());
        Function<String, Text> heightModeText = value -> Text.translatable(Constants.CONFIG_KEY_PREFIX + "height_difference_mode." + value.toLowerCase());
        Function<String, Function<String, Text>> assetText = cat -> id -> Text.translatable("bplb.config.asset." + cat + "." + id);
        
        scrollableList.addPublicEntry(new SectionHeaderEntry(this.client, Text.translatable("bplb.config.section.player_dots")));
        scrollableList.addPublicEntry(new ToggleOptionEntry(this.client, "always_show_player_heads", workingConfig.isAlwaysShowPlayerHeads(), workingConfig::setAlwaysShowPlayerHeads, parent::markDirty));
        scrollableList.addPublicEntry(new ToggleOptionEntry(this.client, "always_show_player_names", workingConfig.isAlwaysShowPlayerNames(), workingConfig::setAlwaysShowPlayerNames, parent::markDirty));
        scrollableList.addPublicEntry(new IntegerSliderOptionEntry(this.client, "icon_size", workingConfig.getIconSize(), 1, 4, workingConfig::setIconSize, parent::markDirty));
        
        List<String> dotsToShow = Client.availableDots.stream().filter(d -> !d.equals("bowtie")).toList();
        scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "dot_type", workingConfig.getDotType(), dotsToShow, assetText.apply("dot"), val -> {
            workingConfig.setDotType(val);
            parent.markDirty();
        }, parent::markDirty, true));
        
        boolean isBowtie = workingConfig.getDotType().equals("bowtie");
        scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "icon_border_style", workingConfig.getIconBorderStyle(), borderStyles, borderStyleText, workingConfig::setIconBorderStyle, parent::markDirty, !isBowtie));
        scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "icon_border_type", workingConfig.getIconBorderType(), borderTypes, borderTypeText, workingConfig::setIconBorderType, parent::markDirty, !isBowtie));
        scrollableList.addPublicEntry(new ToggleOptionEntry(this.client, "inherit_border_color", workingConfig.isInheritBorderColor(), workingConfig::setInheritBorderColor, parent::markDirty));
        
        scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "height_difference_mode", workingConfig.getHeightDifferenceMode(), heightModes, heightModeText, workingConfig::setHeightDifferenceMode, parent::markDirty, true));
        
        List<String> arrows = Client.availableArrows.isEmpty() ? List.of("default") : Client.availableArrows;
        scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "arrow_type", workingConfig.getArrowType(), arrows, assetText.apply("arrow"), workingConfig::setArrowType, parent::markDirty, true));
        scrollableList.addPublicEntry(new IntegerSliderOptionEntry(this.client, "vertical_padding", workingConfig.getVerticalPadding(), 0, 10, workingConfig::setVerticalPadding, parent::markDirty));
        
        scrollableList.addPublicEntry(new ToggleOptionEntry(this.client, "adjust_to_fov", workingConfig.isAdjustToFov(), workingConfig::setAdjustToFov, parent::markDirty));
        scrollableList.addPublicEntry(new FloatSliderOptionEntry(this.client, "fov_multiplier", workingConfig.getFovMultiplier(), 0.5f, 2.0f, workingConfig::setFovMultiplier, parent::markDirty));
        
        scrollableList.addPublicEntry(new SectionHeaderEntry(this.client, Text.translatable("bplb.config.section.death_marker")));
        
        List<String> markerTypes = Arrays.asList("default", "minimal");
        scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "death_marker_type", workingConfig.getDeathMarkerType(), markerTypes, assetText.apply("marker"), workingConfig::setDeathMarkerType, parent::markDirty, true));
        scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "death_marker_border_style", workingConfig.getDeathMarkerBorderStyle(), borderStyles, borderStyleText, workingConfig::setDeathMarkerBorderStyle, parent::markDirty, true));
        scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "death_marker_border_type", workingConfig.getDeathMarkerBorderType(), borderTypes, borderTypeText, workingConfig::setDeathMarkerBorderType, parent::markDirty, true));
        scrollableList.addPublicEntry(new ToggleOptionEntry(this.client, "death_marker_inherit_border_color", workingConfig.isDeathMarkerInheritBorderColor(), workingConfig::setDeathMarkerInheritBorderColor, parent::markDirty));
        
        scrollableList.addPublicEntry(new SectionHeaderEntry(this.client, Text.translatable("bplb.config.section.lodestone_marker")));
        scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "lodestone_marker_type", workingConfig.getLodestoneMarkerType(), markerTypes, assetText.apply("marker"), workingConfig::setLodestoneMarkerType, parent::markDirty, true));
        scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "lodestone_marker_border_style", workingConfig.getLodestoneMarkerBorderStyle(), borderStyles, borderStyleText, workingConfig::setLodestoneMarkerBorderStyle, parent::markDirty, true));
        scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "lodestone_marker_border_type", workingConfig.getLodestoneMarkerBorderType(), borderTypes, borderTypeText, workingConfig::setLodestoneMarkerBorderType, parent::markDirty, true));
        scrollableList.addPublicEntry(new ToggleOptionEntry(this.client, "lodestone_marker_inherit_border_color", workingConfig.isLodestoneMarkerInheritBorderColor(), workingConfig::setLodestoneMarkerInheritBorderColor, parent::markDirty));
        scrollableList.addPublicEntry(new IntegerSliderOptionEntry(this.client, "lodestone_icon_size", workingConfig.getLodestoneIconSize(), 1, 4, workingConfig::setLodestoneIconSize, parent::markDirty));
        
        scrollableList.addPublicEntry(new SectionHeaderEntry(this.client, Text.translatable("bplb.config.section.nameplates")));
        scrollableList.addPublicEntry(new FloatSliderOptionEntry(this.client, "nameplate_scale", workingConfig.getNameplateScale(), 0.5f, 1.5f, workingConfig::setNameplateScale, parent::markDirty));
        scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "name_border_style", workingConfig.getNameBorderStyle(), borderStyles, borderStyleText, workingConfig::setNameBorderStyle, parent::markDirty, true));
    }
}
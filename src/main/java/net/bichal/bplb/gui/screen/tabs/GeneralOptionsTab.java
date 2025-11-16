package net.bichal.bplb.gui.screen.tabs;

import net.bichal.bplb.gui.Config;
import net.bichal.bplb.gui.ConfigScreen;
import net.bichal.bplb.gui.widget.SideNavigationWidget;
import net.bichal.bplb.gui.widget.entries.*;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class GeneralOptionsTab extends BaseConfigTab {
    
    public GeneralOptionsTab(ConfigScreen parent, Config workingConfig) {
        super(parent, workingConfig);
    }

    @Override
    protected void init() {
        super.init();
        if (this.client == null) return;
        
        List<SideNavigationWidget.SectionDefinition> sections = new ArrayList<>();
        sections.add(new SideNavigationWidget.SectionDefinition(Text.translatable("bplb.config.section.general"), 60));
        sections.add(new SideNavigationWidget.SectionDefinition(Text.translatable("bplb.config.section.fading"), 300));
        sections.add(new SideNavigationWidget.SectionDefinition(Text.translatable("bplb.config.section.experimental"), 500));
        
        initSideNav(sections);
        initScrollableList();
        populateOptions();
        
        this.addSelectableChild(this.scrollableList);
        this.addDrawableChild(this.scrollableList);
    }

    @Override
    protected void populateOptions() {
        scrollableList.addPublicEntry(new SectionHeaderEntry(this.client, Text.translatable("bplb.config.section.general")));
        scrollableList.addPublicEntry(new ToggleOptionEntry(this.client, "modEnabled", workingConfig.isModEnabled(), workingConfig::setModEnabled, parent::markDirty));
        scrollableList.addPublicEntry(new IntegerSliderOptionEntry(this.client, "global_hud_y_offset", workingConfig.getGlobalHudYOffset(), -100, 100, workingConfig::setGlobalHudYOffset, parent::markDirty));
        scrollableList.addPublicEntry(new ToggleOptionEntry(this.client, "apply_hotbar_offset", workingConfig.isApplyHotbarOffset(), workingConfig::setApplyHotbarOffset, parent::markDirty));
        scrollableList.addPublicEntry(new IntegerSliderOptionEntry(this.client, "max_visible_icons", workingConfig.getMaxVisibleIcons(), 1, 200, workingConfig::setMaxVisibleIcons, parent::markDirty));
        scrollableList.addPublicEntry(new FloatSliderOptionEntry(this.client, "lerp_speed", workingConfig.getLerpSpeed(), 0.1f, 1.0f, workingConfig::setLerpSpeed, parent::markDirty));
        
        scrollableList.addPublicEntry(new SectionHeaderEntry(this.client, Text.translatable("bplb.config.section.fading")));
        scrollableList.addPublicEntry(new IntegerSliderOptionEntry(this.client, "fade_start_distance", workingConfig.getFadeStartDistance(), 5, 9995, workingConfig::setFadeStartDistance, parent::markDirty));
        scrollableList.addPublicEntry(new IntegerSliderOptionEntry(this.client, "fade_end_distance", workingConfig.getFadeEndDistance(), 10, 10000, workingConfig::setFadeEndDistance, parent::markDirty));
        scrollableList.addPublicEntry(new FloatSliderOptionEntry(this.client, "fade_alpha_max", workingConfig.getFadeAlphaMax(), workingConfig.getFadeAlphaMin(), 1.0f, workingConfig::setFadeAlphaMax, parent::markDirty));
        scrollableList.addPublicEntry(new FloatSliderOptionEntry(this.client, "fade_alpha_min", workingConfig.getFadeAlphaMin(), 0.0f, workingConfig.getFadeAlphaMax(), workingConfig::setFadeAlphaMin, parent::markDirty));
        
        scrollableList.addPublicEntry(new SectionHeaderEntry(this.client, Text.translatable("bplb.config.section.experimental")));
        scrollableList.addPublicEntry(new ToggleOptionEntry(this.client, "enable_icon_clustering", workingConfig.isEnableIconClustering(), workingConfig::setEnableIconClustering, parent::markDirty));
        scrollableList.addPublicEntry(new ToggleOptionEntry(this.client, "enable_cluster_size_scaling", workingConfig.isEnableClusterSizeScaling(), workingConfig::setEnableClusterSizeScaling, parent::markDirty));
        scrollableList.addPublicEntry(new ToggleOptionEntry(this.client, "enable_bouncing_animation", workingConfig.isEnableBouncingAnimation(), workingConfig::setEnableBouncingAnimation, parent::markDirty));
    }
}
package net.bichal.bplb.gui.screen.tabs;

import net.bichal.bplb.gui.Config;
import net.bichal.bplb.gui.ConfigScreen;
import net.bichal.bplb.gui.widget.entries.*;
import net.bichal.bplb.server.ServerConfig;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class ServerOptionsTab extends BaseConfigTab {
    private final ServerConfig serverConfig;
    
    public ServerOptionsTab(ConfigScreen parent, Config workingConfig) {
        super(parent, workingConfig);
        this.serverConfig = ServerConfig.getInstance();
    }

    @Override
    protected void init() {
        super.init();
        if (this.client == null) return;
        
        initScrollableList();
        populateOptions();
        
        this.addSelectableChild(this.scrollableList);
        this.addDrawableChild(this.scrollableList);
    }

    @Override
    protected void populateOptions() {
        scrollableList.addPublicEntry(new SectionHeaderEntry(this.client, Text.translatable("bplb.config.section.server_general")));
        
        List<String> presets = Arrays.asList("minimal", "recommended", "insane", "custom");
        Function<String, Text> presetText = value -> Text.translatable("bplb.config.server.preset." + value);
        
        scrollableList.addPublicEntry(new CycleOptionEntry<>(this.client, "server.preset", serverConfig.preset(), presets, presetText, preset -> {
            serverConfig.applyPreset(preset);
            serverConfig.save();
            rebuildList();
        }, () -> {}, true));
        
        scrollableList.addPublicEntry(new ToggleOptionEntry(this.client, "server.use_datapack_fallback", serverConfig.useDatapackFallback(), val -> {
            serverConfig.setUseDatapackFallback(val);
            serverConfig.save();
        }, () -> {}));
        
        scrollableList.addPublicEntry(new SectionHeaderEntry(this.client, Text.translatable("bplb.config.section.server_performance")));
        
        scrollableList.addPublicEntry(new IntegerSliderOptionEntry(this.client, "server.update_rate", serverConfig.positionUpdateRateTicks(), 1, 100, val -> {
            serverConfig.setPositionUpdateRateTicks(val);
            serverConfig.save();
        }, () -> {}));
        
        scrollableList.addPublicEntry(new ServerDoubleSliderEntry(this.client, "server.position_threshold", serverConfig.positionChangeThreshold(), 0.01, 10.0, val -> {
            serverConfig.setPositionChangeThreshold(val);
            serverConfig.save();
        }));
        
        scrollableList.addPublicEntry(new ServerDoubleSliderEntry(this.client, "server.max_distance", serverConfig.maxRelevantDistance(), 64.0, 2048.0, val -> {
            serverConfig.setMaxRelevantDistance(val);
            serverConfig.save();
        }));
        
        scrollableList.addPublicEntry(new IntegerSliderOptionEntry(this.client, "server.thread_pool_size", serverConfig.threadPoolSize(), 1, Runtime.getRuntime().availableProcessors(), val -> {
            serverConfig.setThreadPoolSize(val);
            serverConfig.save();
        }, () -> {}));
        
        scrollableList.addPublicEntry(new SectionHeaderEntry(this.client, Text.translatable("bplb.config.section.server_info")));
        
        scrollableList.addPublicEntry(new InfoTextEntry(this.client, 
            Text.translatable("bplb.config.server.info.description"),
            Text.translatable("bplb.config.server.info.warning").styled(style -> style.withColor(0xFFFF5555))
        ));
    }

    private void rebuildList() {
        if (this.client == null) return;
        double scroll = this.scrollableList.getScrollAmount();
        scrollableList.clearEntries();
        populateOptions();
        this.scrollableList.setScrollAmount(scroll);
    }
}
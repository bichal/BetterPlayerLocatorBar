package net.bichal.bplb.gui.config;

import net.bichal.bplb.gui.Config;

import java.util.HashMap;
import java.util.Map;

final class ConfigSnapshot {
    private final Map<String, Object> values = new HashMap<>(64);

    ConfigSnapshot(Config config) {
        values.put("modEnabled", config.isModEnabled());
        values.put("globalHudYOffset", config.getGlobalHudYOffset());
        values.put("applyHotbarOffset", config.isApplyHotbarOffset());
        values.put("maxVisibleIcons", config.getMaxVisibleIcons());
        values.put("lerpSpeed", config.getLerpSpeed());
        values.put("fadeStartDistance", config.getFadeStartDistance());
        values.put("fadeEndDistance", config.getFadeEndDistance());
        values.put("fadeAlphaMax", config.getFadeAlphaMax());
        values.put("fadeAlphaMin", config.getFadeAlphaMin());
        values.put("iconSize", config.getIconSize());
        values.put("dotType", config.getDotType());
        values.put("arrowType", config.getArrowType());
        values.put("enableIconClustering", config.isEnableIconClustering());
        values.put("enableClusterSizeScaling", config.isEnableClusterSizeScaling());
        values.put("enableBouncingAnimation", config.isEnableBouncingAnimation());
        values.put("playerConfigs", new HashMap<>(config.getPlayerConfigs()));
    }

    void applyTo(Config config) {
        config.setModEnabled((Boolean) values.get("modEnabled"));
        config.setGlobalHudYOffset((Integer) values.get("globalHudYOffset"));
        config.setApplyHotbarOffset((Boolean) values.get("applyHotbarOffset"));
        config.setMaxVisibleIcons((Integer) values.get("maxVisibleIcons"));
        config.setLerpSpeed((Float) values.get("lerpSpeed"));
        config.setFadeStartDistance((Integer) values.get("fadeStartDistance"));
        config.setFadeEndDistance((Integer) values.get("fadeEndDistance"));
        config.setFadeAlphaMax((Float) values.get("fadeAlphaMax"));
        config.setFadeAlphaMin((Float) values.get("fadeAlphaMin"));
        config.setIconSize((Integer) values.get("iconSize"));
        config.setDotType((String) values.get("dotType"));
        config.setArrowType((String) values.get("arrowType"));
        config.setEnableIconClustering((Boolean) values.get("enableIconClustering"));
        config.setEnableClusterSizeScaling((Boolean) values.get("enableClusterSizeScaling"));
        config.setEnableBouncingAnimation((Boolean) values.get("enableBouncingAnimation"));
        Map<String, Config.PlayerAppearance> playerConfigs = (Map<String, Config.PlayerAppearance>) values.get("playerConfigs");
        config.getPlayerConfigs().clear();
        config.getPlayerConfigs().putAll(playerConfigs);
    }
}
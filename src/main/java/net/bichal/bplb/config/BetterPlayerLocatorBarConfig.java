package net.bichal.bplb.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.bichal.bplb.BetterPlayerLocatorBar;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BetterPlayerLocatorBarConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("bplb.json").toFile();
    private static BetterPlayerLocatorBarConfig INSTANCE;

    private float minAlpha = 0.1f;
    private float maxFadeDistance = 5000f;
    private float fadeStartDistance = 100f;
    private float lerpSpeed = 0.15f;
    private boolean applyHotbarOffset = true;
    private boolean alwaysShowPlayerHeads = false;
    private boolean alwaysShowPlayerNames = false;
    private boolean toggleTab = false;
    private float minFadeAlpha = 0.1f;
    private float maxFadeAlpha = 1.0f;
    private float minFadeScale = 0.75f;
    private float maxFadeScale = 1.0f;

    private int iconSize = 5;
    private float iconOpacity = 1.0f;

    private Float playerHeadSize = 5F;
    private float playerHeadOpacity = 1.0f;
    private boolean inheritBorderColor = true;
    private boolean setHideStatusBars = false;

    private final Map<UUID, PlayerSettings> playerSettings = new HashMap<>();

    public static class PlayerSettings {
        private boolean enabled = true;
        private int color = -1;
        private boolean customSettings = false;
        private float minAlpha = 0.1f;
        private float playerHeadOpacity = 1.0f;
        private boolean showHead = true;
        private boolean showName = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public boolean hasCustomSettings() {
            return customSettings;
        }

        public void setCustomSettings(boolean customSettings) {
            this.customSettings = customSettings;
        }

        public float getMinAlpha() {
            return minAlpha;
        }

        public void setMinAlpha(float minAlpha) {
            this.minAlpha = minAlpha;
        }

        public float getPlayerHeadOpacity() {
            return playerHeadOpacity;
        }

        public void setPlayerHeadOpacity(float playerHeadOpacity) {
            this.playerHeadOpacity = playerHeadOpacity;
        }

        public boolean isShowHead() {
            return showHead;
        }

        public void setShowHead(boolean showHead) {
            this.showHead = showHead;
        }

        public boolean isShowName() {
            return showName;
        }

        public void setShowName(boolean showName) {
            this.showName = showName;
        }
    }

    private BetterPlayerLocatorBarConfig() {
    }

    public static BetterPlayerLocatorBarConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = loadConfig();
        }
        return INSTANCE;
    }

    private static BetterPlayerLocatorBarConfig loadConfig() {
        BetterPlayerLocatorBarConfig config = new BetterPlayerLocatorBarConfig();

        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, BetterPlayerLocatorBarConfig.class);
                BetterPlayerLocatorBar.LOGGER.info("[{}] Config loaded successfully", BetterPlayerLocatorBar.MOD_SHORT_NAME);
            } catch (IOException e) {
                BetterPlayerLocatorBar.LOGGER.error("[{}] Failed to load config file, using defaults", BetterPlayerLocatorBar.MOD_SHORT_NAME);
            }
        }

        return config != null ? config : new BetterPlayerLocatorBarConfig();
    }

    public void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
            BetterPlayerLocatorBar.LOGGER.info("[{}] Config saved successfully", BetterPlayerLocatorBar.MOD_SHORT_NAME);
        } catch (IOException e) {
            BetterPlayerLocatorBar.LOGGER.error("[{}] Failed to save config file", BetterPlayerLocatorBar.MOD_SHORT_NAME);
        }
    }

    public BetterPlayerLocatorBarConfig copy() {
        BetterPlayerLocatorBarConfig copy = new BetterPlayerLocatorBarConfig();
        copy.minAlpha = this.minAlpha;
        copy.maxFadeDistance = this.maxFadeDistance;
        return copy;
    }

    public void copyFrom(BetterPlayerLocatorBarConfig other) {
        this.minAlpha = other.minAlpha;
        this.maxFadeDistance = other.maxFadeDistance;
    }

    public float getMinAlpha() {
        return minAlpha;
    }

    public void setMinAlpha(float minAlpha) {
        this.minAlpha = minAlpha;
    }

    public float getMaxFadeDistance() {
        return maxFadeDistance;
    }

    public void setMaxFadeDistance(float maxFadeDistance) {
        this.maxFadeDistance = maxFadeDistance;
    }

    public float getFadeStartDistance() {
        return fadeStartDistance;
    }

    public void setFadeStartDistance(float fadeStartDistance) {
        this.fadeStartDistance = fadeStartDistance;
    }

    public float getLerpSpeed() {
        return lerpSpeed;
    }

    public void setLerpSpeed(float lerpSpeed) {
        this.lerpSpeed = lerpSpeed;
    }

    public boolean isApplyHotbarOffset() {
        return applyHotbarOffset;
    }

    public void setApplyHotbarOffset(boolean applyHotbarOffset) {
        this.applyHotbarOffset = applyHotbarOffset;
    }

    public boolean isAlwaysShowPlayerHeads() {
        return alwaysShowPlayerHeads;
    }

    public void setAlwaysShowPlayerHeads(boolean alwaysShowPlayerHeads) {
        this.alwaysShowPlayerHeads = alwaysShowPlayerHeads;
    }

    public boolean isAlwaysShowPlayerNames() {
        return alwaysShowPlayerNames;
    }

    public void setAlwaysShowPlayerNames(boolean alwaysShowPlayerNames) {
        this.alwaysShowPlayerNames = alwaysShowPlayerNames;
    }

    public boolean isToggleTab() {
        return toggleTab;
    }

    public void setToggleTab(boolean toggleTab) {
        this.toggleTab = toggleTab;
    }

    public float getMinFadeAlpha() {
        return minFadeAlpha;
    }

    public void setMinFadeAlpha(float minFadeAlpha) {
        this.minFadeAlpha = minFadeAlpha;
    }

    public float getMaxFadeAlpha() {
        return maxFadeAlpha;
    }

    public void setMaxFadeAlpha(float maxFadeAlpha) {
        this.maxFadeAlpha = maxFadeAlpha;
    }

    public float getMinFadeScale() {
        return minFadeScale;
    }

    public void setMinFadeScale(float minFadeScale) {
        this.minFadeScale = minFadeScale;
    }

    public float getMaxFadeScale() {
        return maxFadeScale;
    }

    public void setMaxFadeScale(float maxFadeScale) {
        this.maxFadeScale = maxFadeScale;
    }

    public int getIconSize() {
        return iconSize;
    }

    public void setIconSize(int iconSize) {
        this.iconSize = iconSize;
    }

    public float getIconOpacity() {
        return iconOpacity;
    }

    public void setIconOpacity(float iconOpacity) {
        this.iconOpacity = iconOpacity;
    }

    public Float getPlayerHeadSize() {
        return playerHeadSize;
    }

    public void setPlayerHeadSize(Float playerHeadSize) {
        this.playerHeadSize = playerHeadSize;
    }

    public float getPlayerHeadOpacity() {
        return playerHeadOpacity;
    }

    public void setPlayerHeadOpacity(float playerHeadOpacity) {
        this.playerHeadOpacity = playerHeadOpacity;
    }

    public boolean isInheritBorderColor() {
        return inheritBorderColor;
    }

    public void setInheritBorderColor(boolean inheritBorderColor) {
        this.inheritBorderColor = inheritBorderColor;
    }

    public boolean isHideStatusBars() {
        return setHideStatusBars;
    }

    public void setHideStatusBars(boolean setHideStatusBars) {
        this.setHideStatusBars = setHideStatusBars;
    }

    public Map<UUID, PlayerSettings> getPlayerSettings() {
        return playerSettings;
    }

    public PlayerSettings getPlayerSetting(UUID playerUuid) {
        return playerSettings.computeIfAbsent(playerUuid, uuid -> new PlayerSettings());
    }
}
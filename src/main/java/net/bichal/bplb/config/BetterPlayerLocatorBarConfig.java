package net.bichal.bplb.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class BetterPlayerLocatorBarConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(BetterPlayerLocatorBarConfig.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("better_player_locator_bar.json").toFile();
    private static BetterPlayerLocatorBarConfig instance;

    private float minAlpha = 0.25f;
    private float maxFadeDistance = 100.0f;
    private float fadeStartDistance = 30.0f;
    private float lerpSpeed = 0.12f;
    private boolean applyHotbarOffset = true;
    private boolean alwaysShowPlayerHeads = false;
    private boolean alwaysShowPlayerNames = false;
    private boolean toggleTab = false;
    private float fadeAlphaMax = 1.0f;
    private float fadeAlphaMin = 0.1f;
    private float fadeScaleMax = 1.0f;
    private float fadeScaleMin = 0.5f;

    private int iconSize = 5;
    private float iconOpacity = 0.8f;

    private int headSize = 5;
    private float headOpacity = 1.0f;
    private boolean inheritBorderColor = true;

    public static BetterPlayerLocatorBarConfig getInstance() {
        if (instance == null) {
            instance = loadConfig();
        }
        return instance;
    }

    private static BetterPlayerLocatorBarConfig loadConfig() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                return GSON.fromJson(reader, BetterPlayerLocatorBarConfig.class);
            } catch (IOException e) {
                LOGGER.error("Error loading config file", e);
            }
        }
        return new BetterPlayerLocatorBarConfig();
    }

    public void save() {
        try {
            if (!CONFIG_FILE.exists()) {
                if (!CONFIG_FILE.getParentFile().mkdirs() && !CONFIG_FILE.getParentFile().exists()) {
                    LOGGER.error("Failed to create config directory");
                    return;
                }
                if (!CONFIG_FILE.createNewFile()) {
                    LOGGER.error("Failed to create config file");
                    return;
                }
            }

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Error saving config file", e);
        }
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

    public float getFadeAlphaMax() {
        return fadeAlphaMax;
    }

    public void setFadeAlphaMax(float fadeAlphaMax) {
        this.fadeAlphaMax = fadeAlphaMax;
    }

    public float getFadeAlphaMin() {
        return fadeAlphaMin;
    }

    public void setFadeAlphaMin(float fadeAlphaMin) {
        this.fadeAlphaMin = fadeAlphaMin;
    }

    public float getFadeScaleMax() {
        return fadeScaleMax;
    }

    public void setFadeScaleMax(float fadeScaleMax) {
        this.fadeScaleMax = fadeScaleMax;
    }

    public float getFadeScaleMin() {
        return fadeScaleMin;
    }

    public void setFadeScaleMin(float fadeScaleMin) {
        this.fadeScaleMin = fadeScaleMin;
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

    public int getHeadSize() {
        return headSize;
    }

    public void setHeadSize(int headSize) {
        this.headSize = headSize;
    }

    public float getHeadOpacity() {
        return headOpacity;
    }

    public void setHeadOpacity(float headOpacity) {
        this.headOpacity = headOpacity;
    }

    public boolean isInheritBorderColor() {
        return inheritBorderColor;
    }

    public void setInheritBorderColor(boolean inheritBorderColor) {
        this.inheritBorderColor = inheritBorderColor;
    }
}

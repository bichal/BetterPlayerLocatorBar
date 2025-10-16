package net.bichal.bplb.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.bichal.bplb.client.render.RenderAddons;
import net.bichal.bplb.util.Constants;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("BetterPlayerLocatorBar/options.json").toFile();
    private static final File PLAYERS_FILE = FabricLoader.getInstance().getConfigDir().resolve("BetterPlayerLocatorBar/players.json").toFile();
    private final Map<String, PlayerAppearance> playerConfigs = new HashMap<>();
    private static Config instance;
    private final Map<String, Boolean> playerExpandedStates = new HashMap<>();
    private float lerpSpeed = 0.65f;
    private boolean applyHotbarOffset = true;
    private boolean alwaysShowPlayerHeads = false;
    private boolean alwaysShowPlayerNames = false;
    private int fadeStartDistance = 512;
    private int fadeEndDistance = 4096;
    private float fadeAlphaMax = 1.0f;
    private float fadeAlphaMin = 0.25f;
    private String nameBorderStyle = "rounded";
    private String iconBorderStyle = "rounded";
    private String iconBorderType = "default";
    private boolean inheritBorderColor = true;
    private int iconSize = 4;
    private String dotType = "default";
    private String arrowType = "default";
    private String deathMarkerType = "default";
    private int deathMarkerColor = 0xFF4c4c;
    private String heightDifferenceMode = "PLAYER";
    private boolean modEnabled = true;
    private int maxVisibleIcons = 100;
    private int positionUpdateRateTicks = 2;
    private float nameplateScale = 1.0f;
    private String deathMarkerBorderStyle = "rounded";
    private String deathMarkerBorderType = "default";
    private boolean deathMarkerInheritBorderColor = true;
    private int verticalPadding = 2;
    private boolean adjustToFov = false;
    private float fovMultiplier = 1.0f;

    public static void copy(Config source, Config target) {
        target.fadeEndDistance = source.fadeEndDistance;
        target.fadeStartDistance = source.fadeStartDistance;
        target.lerpSpeed = source.lerpSpeed;
        target.applyHotbarOffset = source.applyHotbarOffset;
        target.alwaysShowPlayerHeads = source.alwaysShowPlayerHeads;
        target.alwaysShowPlayerNames = source.alwaysShowPlayerNames;
        target.fadeAlphaMax = source.fadeAlphaMax;
        target.fadeAlphaMin = source.fadeAlphaMin;
        target.iconSize = source.iconSize;
        target.nameBorderStyle = source.nameBorderStyle;
        target.iconBorderStyle = source.iconBorderStyle;
        target.iconBorderType = source.iconBorderType;
        target.inheritBorderColor = source.inheritBorderColor;
        target.maxVisibleIcons = source.maxVisibleIcons;
        target.dotType = source.dotType;
        target.arrowType = source.arrowType;
        target.deathMarkerType = source.deathMarkerType;
        target.deathMarkerColor = source.deathMarkerColor;
        target.heightDifferenceMode = source.heightDifferenceMode;
        target.modEnabled = source.modEnabled;
        target.verticalPadding = source.verticalPadding;
        target.positionUpdateRateTicks = source.positionUpdateRateTicks;
        target.nameplateScale = source.nameplateScale;
        target.deathMarkerBorderStyle = source.deathMarkerBorderStyle;
        target.deathMarkerBorderType = source.deathMarkerBorderType;
        target.deathMarkerInheritBorderColor = source.deathMarkerInheritBorderColor;
        target.adjustToFov = source.adjustToFov;
        target.fovMultiplier = source.fovMultiplier;
        target.playerConfigs.clear();
        target.playerConfigs.putAll(source.playerConfigs);
        target.playerExpandedStates.clear();
        target.playerExpandedStates.putAll(source.playerExpandedStates);
    }

    private static Config loadConfig() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                Config loaded = GSON.fromJson(reader, Config.class);
                if (loaded != null) {
                    loadPlayers(loaded);
                    Constants.LOGGER.info("[{}] Config loaded successfully", Constants.MOD_NAME_SHORT);
                    return loaded;
                }
            } catch (IOException e) {
                Constants.LOGGER.error("Error loading config file", e);
            }
        }
        Config newConfig = new Config();
        newConfig.save();
        Constants.LOGGER.info("[{}] Created new config with defaults", Constants.MOD_NAME_SHORT);
        return newConfig;
    }

    private static void loadPlayers(Config config) {
        if (!PLAYERS_FILE.exists()) return;
        try (FileReader reader = new FileReader(PLAYERS_FILE)) {
            Type type = new com.google.gson.reflect.TypeToken<Map<String, PlayerAppearance>>() {
            }.getType();
            Map<String, PlayerAppearance> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                config.playerConfigs.putAll(loaded);
            }
        } catch (IOException e) {
            Constants.LOGGER.error("Error loading players config", e);
        }
    }

    public static Config getInstance() {
        if (instance == null) instance = loadConfig();
        return instance;
    }

    private static UUID getUuidFromCache(String name) {
        return RenderAddons.getUuidFromCache(name);
    }

    public void setPlayerExpanded(String playerName, boolean expanded) {
        playerExpandedStates.put(playerName, expanded);
        save();
    }

    public boolean isPlayerExpanded(String playerName) {
        return playerExpandedStates.getOrDefault(playerName, false);
    }

    public void resetToDefaults() {
        copy(new Config(), this);
    }

    private <T> void set(T value, Consumer<T> setter) {
        setter.accept(value);
        save();
    }

    public Map<String, PlayerAppearance> getPlayerConfigs() {
        return this.playerConfigs;
    }

    public void save() {
        try {
            File parentDir = CONFIG_FILE.getParentFile();
            if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                Constants.LOGGER.error("Failed to create config directory");
                return;
            }

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
            }

            savePlayers();
        } catch (IOException e) {
            Constants.LOGGER.error("Error saving config file", e);
        }
    }

    private void savePlayers() {
        try {
            File parent = PLAYERS_FILE.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                Constants.LOGGER.error("Failed to create players config directory");
                return;
            }

            Map<String, PlayerAppearance> toSave = new HashMap<>();
            for (Map.Entry<String, PlayerAppearance> entry : playerConfigs.entrySet()) {
                UUID uuid = getUuidFromCache(entry.getKey());
                if (uuid != null) {
                    PlayerAppearance pa = entry.getValue();
                    pa.playerUuid = uuid;
                    pa.playerName = entry.getKey();
                    toSave.put(entry.getKey(), pa);
                }
            }

            try (FileWriter writer = new FileWriter(PLAYERS_FILE)) {
                GSON.toJson(toSave, writer);
            }
        } catch (IOException e) {
            Constants.LOGGER.error("Error saving players config", e);
        }
    }

    @Nullable
    public PlayerAppearance getPlayerConfig(String playerName) { return playerConfigs.get(playerName); }
    public void setMaxVisibleIcons(int value) { set(MathHelper.clamp(value, 1, 200), v -> this.maxVisibleIcons = v); }
    public int getFadeEndDistance() { return fadeEndDistance; }
    public void setFadeEndDistance(int value) { set(value, v -> this.fadeEndDistance = v); }
    public int getFadeStartDistance() { return fadeStartDistance; }
    public void setFadeStartDistance(int value) { set(value, v -> this.fadeStartDistance = v); }
    public float getLerpSpeed() { return lerpSpeed; }
    public void setLerpSpeed(float value) { set(value, v -> this.lerpSpeed = v); }
    public boolean isApplyHotbarOffset() { return applyHotbarOffset; }
    public void setApplyHotbarOffset(boolean value) { set(value, v -> this.applyHotbarOffset = v); }
    public boolean isAlwaysShowPlayerHeads() { return alwaysShowPlayerHeads; }
    public void setAlwaysShowPlayerHeads(boolean value) { set(value, v -> this.alwaysShowPlayerHeads = v); }
    public boolean isAlwaysShowPlayerNames() { return alwaysShowPlayerNames; }
    public void setAlwaysShowPlayerNames(boolean value) { set(value, v -> this.alwaysShowPlayerNames = v); }
    public float getFadeAlphaMax() { return fadeAlphaMax; }
    public void setFadeAlphaMax(float value) { set(value, v -> this.fadeAlphaMax = v); }
    public float getFadeAlphaMin() { return fadeAlphaMin; }
    public void setFadeAlphaMin(float value) { set(value, v -> this.fadeAlphaMin = v); }
    public int getIconSize() { return iconSize; }
    public void setIconSize(int value) { set(value, v -> this.iconSize = v); }
    public String getNameBorderStyle() { return nameBorderStyle; }
    public void setNameBorderStyle(String value) { set(value, v -> this.nameBorderStyle = v); }
    public String getIconBorderStyle() { return iconBorderStyle; }
    public void setIconBorderStyle(String value) { set(value, v -> this.iconBorderStyle = v); }
    public String getIconBorderType() { return iconBorderType; }
    public void setIconBorderType(String value) { set(value, v -> this.iconBorderType = v); }
    public boolean isInheritBorderColor() { return inheritBorderColor; }
    public void setInheritBorderColor(boolean value) { set(value, v -> this.inheritBorderColor = v); }
    public int getMaxVisibleIcons() { return maxVisibleIcons; }
    @SuppressWarnings("unused") /* Future implementation */ public void setDeathMarkerColor(int value) { set(value, v -> this.deathMarkerColor = v); }
    public String getDotType() { return dotType; }
    public void setDotType(String value) { set(value, v -> this.dotType = v); }
    public String getArrowType() { return arrowType; }
    public void setArrowType(String value) { set(value, v -> this.arrowType = v); }
    public String getDeathMarkerType() { return deathMarkerType; }
    public void setDeathMarkerType(String value) { set(value, v -> this.deathMarkerType = v); }
    public int getDeathMarkerColor() { return deathMarkerColor; }
    @SuppressWarnings("unused") /* Future implementation */ public void setDeathMarkerBorderStyle(String value) { set(value, v -> this.deathMarkerBorderStyle = v); }
    public String getHeightDifferenceMode() { return heightDifferenceMode; }
    public void setHeightDifferenceMode(String value) { set(value, v -> this.heightDifferenceMode = v); }
    public boolean isModEnabled() { return modEnabled; }
    public void setModEnabled(boolean value) { set(value, v -> this.modEnabled = v); }
    public int getVerticalPadding() { return verticalPadding; }
    public void setVerticalPadding(int value) { set(value, v -> this.verticalPadding = v); }
    public int getPositionUpdateRateTicks() { return positionUpdateRateTicks; }
    public void setPositionUpdateRateTicks(int value) { set(value, v -> this.positionUpdateRateTicks = v); }
    public float getNameplateScale() { return nameplateScale; }
    public void setNameplateScale(float value) { set(value, v -> this.nameplateScale = v); }
    public String getDeathMarkerBorderStyle() { return deathMarkerBorderStyle; }
    public boolean isAdjustToFov() { return adjustToFov; }
    public String getDeathMarkerBorderType() { return deathMarkerBorderType; }
    public void setDeathMarkerBorderType(String value) { set(value, v -> this.deathMarkerBorderType = v); }
    public boolean isDeathMarkerInheritBorderColor() { return deathMarkerInheritBorderColor; }
    public void setDeathMarkerInheritBorderColor(boolean value) { set(value, v -> this.deathMarkerInheritBorderColor = v); }
    public void setAdjustToFov(boolean value) { set(value, v -> this.adjustToFov = v); }
    public float getFovMultiplier() { return fovMultiplier; }
    public void setFovMultiplier(float value) { set(MathHelper.clamp(value, 0.5f, 2.0f), v -> this.fovMultiplier = v); }

    @SuppressWarnings("unused")
    public static class PlayerAppearance {
        public String playerName;
        public UUID playerUuid;
        public String dotType;
        public String iconBorderStyle;
        public String iconBorderType;
        public String arrowType;
        public Integer color;
        public String textureHeadOverride /* Future implementation */;
    }
}

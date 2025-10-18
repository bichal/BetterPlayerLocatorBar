package net.bichal.bplb.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
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
import java.util.*;
import java.util.function.Consumer;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("BetterPlayerLocatorBar/options.json").toFile();
    private static final File PLAYERS_FILE = FabricLoader.getInstance().getConfigDir().resolve("BetterPlayerLocatorBar/players.json").toFile();
    private final Map<String, PlayerAppearance> playerConfigs = new HashMap<>();
    private static Config instance;
    private final Map<String, Boolean> playerExpandedStates = new HashMap<>();
    private float lerpSpeed;
    private boolean applyHotbarOffset;
    private boolean alwaysShowPlayerHeads;
    private boolean alwaysShowPlayerNames;
    private int fadeStartDistance;
    private int fadeEndDistance;
    private float fadeAlphaMax;
    private float fadeAlphaMin;
    private String nameBorderStyle;
    private String iconBorderStyle;
    private String iconBorderType;
    private boolean inheritBorderColor;
    private int iconSize;
    private String dotType;
    private String arrowType;
    private String deathMarkerType;
    private int deathMarkerColor;
    private String heightDifferenceMode;
    private boolean modEnabled;
    private int maxVisibleIcons;
    private float nameplateScale;
    private String deathMarkerBorderStyle;
    private String deathMarkerBorderType;
    private boolean deathMarkerInheritBorderColor;
    private int verticalPadding;
    private boolean adjustToFov;
    private float fovMultiplier;

    public Config() {
        this.lerpSpeed = 0.65f;
        this.applyHotbarOffset = true;
        this.alwaysShowPlayerHeads = false;
        this.alwaysShowPlayerNames = false;
        this.fadeStartDistance = 512;
        this.fadeEndDistance = 4096;
        this.fadeAlphaMax = 1.0f;
        this.fadeAlphaMin = 0.25f;
        this.nameBorderStyle = "rounded";
        this.iconBorderStyle = "rounded";
        this.iconBorderType = "default";
        this.inheritBorderColor = true;
        this.iconSize = 4;
        this.dotType = "default";
        this.arrowType = "default";
        this.deathMarkerType = "default";
        this.deathMarkerColor = 0xFF4c4c;
        this.heightDifferenceMode = "PLAYER";
        this.modEnabled = true;
        this.maxVisibleIcons = 100;
        this.nameplateScale = 1.0f;
        this.deathMarkerBorderStyle = "rounded";
        this.deathMarkerBorderType = "default";
        this.deathMarkerInheritBorderColor = true;
        this.verticalPadding = 0;
        this.adjustToFov = false;
        this.fovMultiplier = 1.0f;
    }

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
                    loaded.validate();
                    loadPlayers(loaded);
                    loaded.save();
                    Constants.LOGGER.info("[{}] Config loaded successfully", Constants.MOD_NAME_SHORT);
                    return loaded;
                }
            } catch (IOException e) {
                Constants.LOGGER.error("Error loading config file", e);
            } catch (JsonParseException e) {
                Constants.LOGGER.error("Config file is corrupted, creating backup and resetting to defaults", e);
                backupAndDelete();
            } catch (Exception e) {
                Constants.LOGGER.error("Unexpected error loading config, resetting to defaults", e);
                backupAndDelete();
            }
        }
        Config newConfig = new Config();
        newConfig.save();
        Constants.LOGGER.info("[{}] Created new config with defaults", Constants.MOD_NAME_SHORT);
        return newConfig;
    }

    private static void backupAndDelete() {
        try {
            File backup = new File(CONFIG_FILE.getParent(), "options.json.backup." + System.currentTimeMillis());
            java.nio.file.Files.copy(CONFIG_FILE.toPath(), backup.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            if (!CONFIG_FILE.delete()) Constants.LOGGER.warn("Failed to delete corrupted config file");
            Constants.LOGGER.info("Backup saved to: {}", backup.getAbsolutePath());
        } catch (IOException ex) {
            Constants.LOGGER.error("Failed to backup corrupted config", ex);
        }
    }

    private void validate() {
        List<String> correctedFields = new ArrayList<>();

        correctedFields.addAll(validateFloat("lerpSpeed", v -> lerpSpeed = v, lerpSpeed, 0.1f, 1.0f, 0.65f));
        correctedFields.addAll(validateInt("fadeStartDistance", v -> fadeStartDistance = v, fadeStartDistance, 5, 9995, 512));
        correctedFields.addAll(validateInt("fadeEndDistance", v -> fadeEndDistance = v, fadeEndDistance, 10, 10000, 4096));
        correctedFields.addAll(validateFloat("fadeAlphaMax", v -> fadeAlphaMax = v, fadeAlphaMax, 0.01f, 1.0f, 1.0f));
        correctedFields.addAll(validateFloat("fadeAlphaMin", v -> fadeAlphaMin = v, fadeAlphaMin, 0.0f, 1.0f, 0.25f));
        correctedFields.addAll(validateInt("iconSize", v -> iconSize = v, iconSize, 1, 4, 4));
        correctedFields.addAll(validateInt("maxVisibleIcons", v -> maxVisibleIcons = v, maxVisibleIcons, 1, 200, 100));
        correctedFields.addAll(validateFloat("nameplateScale", v -> nameplateScale = v, nameplateScale, 0.5f, 1.5f, 1.0f));
        correctedFields.addAll(validateInt("verticalPadding", v -> verticalPadding = v, verticalPadding, 0, 10, 2));
        correctedFields.addAll(validateFloat("fovMultiplier", v -> fovMultiplier = v, fovMultiplier, 0.5f, 2.0f, 1.0f));

        if (fadeStartDistance >= fadeEndDistance) {
            fadeStartDistance = 512;
            fadeEndDistance = 4096;
            correctedFields.add("fadeStartDistance/fadeEndDistance (range)");
        }

        if (fadeAlphaMin >= fadeAlphaMax) {
            fadeAlphaMin = 0.25f;
            fadeAlphaMax = 1.0f;
            correctedFields.add("fadeAlphaMin/fadeAlphaMax (range)");
        }

        correctedFields.addAll(validateString("nameBorderStyle", v -> nameBorderStyle = v, nameBorderStyle, List.of("rounded", "squared"), "rounded"));
        correctedFields.addAll(validateString("iconBorderStyle", v -> iconBorderStyle = v, iconBorderStyle, List.of("rounded", "squared"), "rounded"));
        correctedFields.addAll(validateString("iconBorderType", v -> iconBorderType = v, iconBorderType, List.of("default", "minimal"), "default"));
        correctedFields.addAll(validateString("deathMarkerBorderStyle", v -> deathMarkerBorderStyle = v, deathMarkerBorderStyle, List.of("rounded", "squared"), "rounded"));
        correctedFields.addAll(validateString("deathMarkerBorderType", v -> deathMarkerBorderType = v, deathMarkerBorderType, List.of("default", "minimal"), "default"));
        correctedFields.addAll(validateString("heightDifferenceMode", v -> heightDifferenceMode = v, heightDifferenceMode != null ? heightDifferenceMode.toLowerCase() : null, List.of("player", "camera"), "player"));

        correctedFields.addAll(validateNotEmpty("dotType", v -> dotType = v, dotType));
        correctedFields.addAll(validateNotEmpty("arrowType", v -> arrowType = v, arrowType));
        correctedFields.addAll(validateNotEmpty("deathMarkerType", v -> deathMarkerType = v, deathMarkerType));

        boolean removed = playerConfigs.entrySet().removeIf(entry -> entry.getKey() == null || entry.getKey().isEmpty() || entry.getValue() == null);
        if (removed) correctedFields.add("playerConfigs (removed invalid entries)");

        if (!correctedFields.isEmpty()) Constants.LOGGER.warn("Corrected invalid config fields: {}", String.join(", ", correctedFields));
    }

    private List<String> validateFloat(String name, Consumer<Float> setter, float value, float min, float max, float defaultValue) {
        if (value < min || value > max || Float.isNaN(value) || Float.isInfinite(value)) {
            setter.accept(defaultValue);
            return List.of(name);
        }
        return List.of();
    }

    private List<String> validateInt(String name, Consumer<Integer> setter, int value, int min, int max, int defaultValue) {
        if (value < min || value > max) {
            setter.accept(defaultValue);
            return List.of(name);
        }
        return List.of();
    }

    private List<String> validateString(String name, Consumer<String> setter, String value, List<String> validValues, String defaultValue) {
        if (value == null || !validValues.contains(value)) {
            setter.accept(defaultValue);
            return List.of(name);
        }
        return List.of();
    }

    private List<String> validateNotEmpty(String name, Consumer<String> setter, String value) {
        if (value == null || value.isEmpty()) {
            setter.accept("default");
            return List.of(name);
        }
        return List.of();
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
    public void setFadeAlphaMax(float value) { float clampedValue = MathHelper.clamp(value, this.fadeAlphaMin + 0.01f, 1.0f); set(clampedValue, v -> this.fadeAlphaMax = v); }
    public float getFadeAlphaMin() { return fadeAlphaMin; }
    public void setFadeAlphaMin(float value) { float clampedValue = MathHelper.clamp(value, 0.0f, this.fadeAlphaMax - 0.01f); set(clampedValue, v -> this.fadeAlphaMin = v); }
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

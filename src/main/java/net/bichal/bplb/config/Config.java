package net.bichal.bplb.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.bichal.bplb.util.Constants;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static net.bichal.bplb.client.render.RenderAddons.getUuid;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("Better Player Locator Bar/options.json").toFile();
    private static Config instance;
    private final Map<String, PlayerConfig> playerConfigs = new HashMap<>();
    private float minAlpha = 0.25f;
    private float maxFadeDistance = 50.0f;
    private float fadeStartDistance = 30.0f;
    private float lerpSpeed = 0.4f;
    private boolean applyHotbarOffset = true;
    private boolean alwaysShowPlayerHeads = false;
    private boolean alwaysShowPlayerNames = false;
    private boolean toggleTab = false;
    private float fadeAlphaMax = 1.0f;
    private float fadeAlphaMin = 0.1f;
    private int iconSize = 6;
    private String nameBorderStyle = "rounded";
    private String iconBorderStyle = "rounded";
    private String iconBorderType = "default";
    private boolean inheritBorderColor = true;
    private int maxVisibleIcons = 15;
    private String dotType = "default";
    private String arrowType = "default";
    private String deathMarkerType = "default";
    private int deathMarkerColor = 0xFF4c4c;
    private String heightDifferenceMode = "PLAYER";
    private boolean modEnabled = true;
    private int verticalPadding = 0;
    private int positionUpdateRateTicks = 2;
    private float nameplateScale = 0.875f;
    private String deathMarkerBorderStyle = "rounded";
    private String deathMarkerBorderType = "default";
    private boolean deathMarkerInheritBorderColor = true;

    public static Config getInstance() {
        if (instance == null) instance = loadConfig();
        return instance;
    }

    public static void copy(Config source, Config target) {
        target.minAlpha = source.minAlpha;
        target.maxFadeDistance = source.maxFadeDistance;
        target.fadeStartDistance = source.fadeStartDistance;
        target.lerpSpeed = source.lerpSpeed;
        target.applyHotbarOffset = source.applyHotbarOffset;
        target.alwaysShowPlayerHeads = source.alwaysShowPlayerHeads;
        target.alwaysShowPlayerNames = source.alwaysShowPlayerNames;
        target.toggleTab = source.toggleTab;
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
        target.getPlayerConfigs().clear();
//        source.getPlayerConfigs().forEach((name, playerConfig) -> target.getPlayerConfigs().put(name, playerConfig.deepCopy()));
    }

    private static Config loadConfig() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                Config loaded = GSON.fromJson(reader, Config.class);
                if (loaded != null) {
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

    public void resetToDefaults() {
        copy(new Config(), this);
    }

    private <T> void set(T value, Consumer<T> setter) {
        setter.accept(value);
        save();
    }

    @Nullable
    public PlayerConfig getPlayerConfig(String playerName) {
        PlayerConfig legacy = playerConfigs.get(playerName);
        if (legacy != null) return legacy;

        UUID uuid = findPlayerUUID(playerName);
        if (uuid != null) {
            PlayerConfig pc = PlayerConfig.load(uuid);
            if (pc != null) {
                PlayerConfig pa = new PlayerConfig();
                pa.color = pc.color;
                pa.dotType = pc.dotType;
                pa.iconBorderStyle = pc.iconBorderStyle;
                pa.arrowType = pc.arrowType;
                return pa;
            }
        }
        return null;
    }

    private UUID findPlayerUUID(String name) {
        return getUuid(name);
    }

    public void save() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
            }

            // Guardar configs de jugadores individuales
            for (Map.Entry<String, PlayerConfig> entry : playerConfigs.entrySet()) {
                UUID uuid = getUuid(entry.getKey());
                if (uuid != null) {
                    PlayerConfig pc = entry.getValue();
                    pc.playerUuid = uuid;
                    pc.playerName = entry.getKey();
                    pc.save();
                }
            }
        } catch (IOException e) {
            Constants.LOGGER.error("Error saving config file", e);
        }
    }

    public Map<String, PlayerConfig> getPlayerConfigs() { return this.playerConfigs; }
    public float getMinAlpha() { return minAlpha; }
    public void setMinAlpha(float value) { set(value, v -> this.minAlpha = v); }
    public float getMaxFadeDistance() { return maxFadeDistance; }
    public void setMaxFadeDistance(float value) { set(value, v -> this.maxFadeDistance = v); }
    public float getFadeStartDistance() { return fadeStartDistance; }
    public void setFadeStartDistance(float value) { set(value, v -> this.fadeStartDistance = v); }
    public float getLerpSpeed() { return lerpSpeed; }
    public void setLerpSpeed(float value) { set(value, v -> this.lerpSpeed = v); }
    public boolean isApplyHotbarOffset() { return applyHotbarOffset; }
    public void setApplyHotbarOffset(boolean value) { set(value, v -> this.applyHotbarOffset = v); }
    public boolean isAlwaysShowPlayerHeads() { return alwaysShowPlayerHeads; }
    public void setAlwaysShowPlayerHeads(boolean value) { set(value, v -> this.alwaysShowPlayerHeads = v); }
    public boolean isAlwaysShowPlayerNames() { return alwaysShowPlayerNames; }
    public void setAlwaysShowPlayerNames(boolean value) { set(value, v -> this.alwaysShowPlayerNames = v); }
    public boolean isToggleTab() { return toggleTab; }
    public void setToggleTab(boolean value) { set(value, v -> this.toggleTab = v); }
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
    public void setMaxVisibleIcons(int value) { set(MathHelper.clamp(value, 1, 45), v -> this.maxVisibleIcons = v); }
    public String getDotType() { return dotType; }
    public void setDotType(String value) { set(value, v -> this.dotType = v); }
    public String getArrowType() { return arrowType; }
    public void setArrowType(String value) { set(value, v -> this.arrowType = v); }
    public String getDeathMarkerType() { return deathMarkerType; }
    public void setDeathMarkerType(String value) { set(value, v -> this.deathMarkerType = v); }
    public int getDeathMarkerColor() { return deathMarkerColor; }
    public void setDeathMarkerColor(int value) { set(value, v -> this.deathMarkerColor = v); }
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
    public void setDeathMarkerBorderStyle(String value) { set(value, v -> this.deathMarkerBorderStyle = v); }
    public String getDeathMarkerBorderType() { return deathMarkerBorderType; }
    public void setDeathMarkerBorderType(String value) { set(value, v -> this.deathMarkerBorderType = v); }
    public boolean isDeathMarkerInheritBorderColor() { return deathMarkerInheritBorderColor; }
    public void setDeathMarkerInheritBorderColor(boolean value) { set(value, v -> this.deathMarkerInheritBorderColor = v); }
}

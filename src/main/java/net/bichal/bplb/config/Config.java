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

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("Better Player Locator Bar/options.json").toFile();
    private static Config instance;
    private final Map<String, PlayerAppearance> playerAppearances = new HashMap<>();
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
        target.getPlayerAppearances().clear();
        source.getPlayerAppearances().forEach((name, appearance) -> target.getPlayerAppearances().put(name, appearance.deepCopy()));
    }

    private static Config loadConfig() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                return GSON.fromJson(reader, Config.class);
            } catch (IOException e) {
                Constants.LOGGER.error("Error loading config file", e);
            }
        }
        return new Config();
    }

    public void resetToDefaults() {
        copy(new Config(), this);
    }

    public void save() {
        try {
            if (!CONFIG_FILE.getParentFile().exists() && !CONFIG_FILE.getParentFile().mkdirs()) {
                Constants.LOGGER.warn("Could not create config directory for BPLB");
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            Constants.LOGGER.error("Error saving config file", e);
        }
    }

    public Map<String, PlayerAppearance> getPlayerAppearances() { return this.playerAppearances; }
    @Nullable public PlayerAppearance getPlayerAppearance(String playerName) { return playerAppearances.get(playerName); }
    public float getMinAlpha() { return minAlpha; }
    public void setMinAlpha(float minAlpha) { this.minAlpha = minAlpha; }
    public float getMaxFadeDistance() { return maxFadeDistance; }
    public void setMaxFadeDistance(float maxFadeDistance) { this.maxFadeDistance = maxFadeDistance; }
    public float getFadeStartDistance() { return fadeStartDistance; }
    public void setFadeStartDistance(float fadeStartDistance) { this.fadeStartDistance = fadeStartDistance; }
    public float getLerpSpeed() { return lerpSpeed; }
    public void setLerpSpeed(float lerpSpeed) { this.lerpSpeed = lerpSpeed; }
    public boolean isApplyHotbarOffset() { return applyHotbarOffset; }
    public void setApplyHotbarOffset(boolean applyHotbarOffset) { this.applyHotbarOffset = applyHotbarOffset; }
    public boolean isAlwaysShowPlayerHeads() { return alwaysShowPlayerHeads; }
    public void setAlwaysShowPlayerHeads(boolean alwaysShowPlayerHeads) { this.alwaysShowPlayerHeads = alwaysShowPlayerHeads; }
    public boolean isAlwaysShowPlayerNames() { return alwaysShowPlayerNames; }
    public void setAlwaysShowPlayerNames(boolean alwaysShowPlayerNames) { this.alwaysShowPlayerNames = alwaysShowPlayerNames; }
    public boolean isToggleTab() { return toggleTab; }
    public void setToggleTab(boolean toggleTab) { this.toggleTab = toggleTab; }
    public float getFadeAlphaMax() { return fadeAlphaMax; }
    public void setFadeAlphaMax(float fadeAlphaMax) { this.fadeAlphaMax = fadeAlphaMax; }
    public float getFadeAlphaMin() { return fadeAlphaMin; }
    public void setFadeAlphaMin(float fadeAlphaMin) { this.fadeAlphaMin = fadeAlphaMin; }
    public int getIconSize() { return iconSize; }
    public void setIconSize(int iconSize) { this.iconSize = iconSize; }
    public String getNameBorderStyle() { return nameBorderStyle; }
    public void setNameBorderStyle(String nameBorderStyle) { this.nameBorderStyle = nameBorderStyle; }
    public String getIconBorderStyle() { return iconBorderStyle; }
    public void setIconBorderStyle(String iconBorderStyle) { this.iconBorderStyle = iconBorderStyle; }
    public int getMaxVisibleIcons() { return maxVisibleIcons; }
    public void setMaxVisibleIcons(int maxVisibleIcons) { this.maxVisibleIcons = MathHelper.clamp(maxVisibleIcons, 1, 45); }
    public String getDotType() { return dotType; }
    public void setDotType(String dotType) { this.dotType = dotType; }
    public String getArrowType() { return arrowType; }
    public void setArrowType(String arrowType) { this.arrowType = arrowType; }
    public String getDeathMarkerType() { return deathMarkerType; }
    public void setDeathMarkerType(String deathMarkerType) { this.deathMarkerType = deathMarkerType; }
    public int getDeathMarkerColor() { return deathMarkerColor; }
    public void setDeathMarkerColor(int deathMarkerColor) { this.deathMarkerColor = deathMarkerColor; }
    public String getHeightDifferenceMode() { return heightDifferenceMode; }
    public void setHeightDifferenceMode(String heightDifferenceMode) { this.heightDifferenceMode = heightDifferenceMode; }
    public boolean isModEnabled() { return modEnabled; }
    public void setModEnabled(boolean modEnabled) { this.modEnabled = modEnabled; }
    public int getVerticalPadding() { return verticalPadding; }
    public void setVerticalPadding(int verticalPadding) { this.verticalPadding = verticalPadding; }
    public int getPositionUpdateRateTicks() { return positionUpdateRateTicks; }
    public void setPositionUpdateRateTicks(int positionUpdateRateTicks) { this.positionUpdateRateTicks = positionUpdateRateTicks; }
    public float getNameplateScale() { return nameplateScale; }
    public void setNameplateScale(float nameplateScale) { this.nameplateScale = nameplateScale; }
}

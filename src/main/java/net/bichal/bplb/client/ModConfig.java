package net.bichal.bplb.client;

import com.teamresourceful.resourcefulconfig.api.annotations.Config;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption;
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType;
import net.bichal.bichalutils.util.Logger;
import net.bichal.bplb.util.Constants;

@ConfigInfo.Provider(ModInfoProvider.class) @Config(value = Constants.MOD_ID, version = 1)
public final class ModConfig {
    @ConfigEntry(id = "modEnabled", type = EntryType.BOOLEAN, translation = "bplb.config.modEnabled")
    public static boolean modEnabled = true;

    @ConfigEntry(id = "globalHudYOffset", type = EntryType.INTEGER, translation = "bplb.config.global_hud_y_offset")
    @ConfigOption.Range(min = -100, max = 100)
    public static int globalHudYOffset = 0;

    @ConfigEntry(id = "applyHotbarOffset", type = EntryType.BOOLEAN, translation = "bplb.config.apply_hotbar_offset")
    public static boolean applyHotbarOffset = true;

    @ConfigEntry(id = "maxVisibleIcons", type = EntryType.INTEGER, translation = "bplb.config.max_visible_icons")
    @ConfigOption.Range(min = 1, max = 200)
    public static int maxVisibleIcons = 100;

    @ConfigEntry(id = "lerpSpeed", type = EntryType.FLOAT, translation = "bplb.config.lerp_speed")
    @ConfigOption.Range(min = 0.1, max = 1.0)
    @ConfigOption.Slider
    public static float lerpSpeed = 0.65f;

    @ConfigEntry(id = "showExperienceBar", type = EntryType.BOOLEAN, translation = "bplb.config.show_experience_bar")
    public static boolean showExperienceBar = true;

    @ConfigEntry(id = "experienceBarBackground", type = EntryType.ENUM, translation = "bplb.config.experience_bar_background")
    public static ExperienceBarBackground experienceBarBackground = ExperienceBarBackground.MOJANG;

    @ConfigEntry(id = "fadeStartDistance", type = EntryType.INTEGER, translation = "bplb.config.fade_start_distance")
    @ConfigOption.Range(min = 5, max = 9995)
    @ConfigOption.Slider
    public static int fadeStartDistance = 512;

    @ConfigEntry(id = "fadeEndDistance", type = EntryType.INTEGER, translation = "bplb.config.fade_end_distance")
    @ConfigOption.Range(min = 10, max = 10000)
    @ConfigOption.Slider
    public static int fadeEndDistance = 4096;

    @ConfigEntry(id = "fadeAlphaMax", type = EntryType.FLOAT, translation = "bplb.config.fade_alpha_max")
    @ConfigOption.Range(min = 0.01, max = 1.0)
    @ConfigOption.Slider
    public static float fadeAlphaMax = 1.0f;

    @ConfigEntry(id = "fadeAlphaMin", type = EntryType.FLOAT, translation = "bplb.config.fade_alpha_min")
    @ConfigOption.Range(min = 0.0, max = 1.0)
    @ConfigOption.Slider
    public static float fadeAlphaMin = 0.25f;

    @ConfigEntry(id = "enableIconClustering", type = EntryType.BOOLEAN, translation = "bplb.config.enable_icon_clustering")
    public static boolean enableIconClustering = false;

    @ConfigEntry(id = "enableClusterSizeScaling", type = EntryType.BOOLEAN, translation = "bplb.config.enable_cluster_size_scaling")
    public static boolean enableClusterSizeScaling = false;

    @ConfigEntry(id = "enableBouncingAnimation", type = EntryType.BOOLEAN, translation = "bplb.config.enable_bouncing_animation")
    public static boolean enableBouncingAnimation = true;

    @ConfigEntry(id = "alwaysShowPlayerHeads", type = EntryType.BOOLEAN, translation = "bplb.config.always_show_player_heads")
    public static boolean alwaysShowPlayerHeads = false;

    @ConfigEntry(id = "alwaysShowPlayerNames", type = EntryType.BOOLEAN, translation = "bplb.config.always_show_player_names")
    public static boolean alwaysShowPlayerNames = false;

    @ConfigEntry(id = "iconSize", type = EntryType.INTEGER, translation = "bplb.config.icon_size")
    @ConfigOption.Range(min = 1, max = 4)
    @ConfigOption.Slider
    public static int iconSize = 4;

    @ConfigEntry(id = "nameplateScale", type = EntryType.FLOAT, translation = "bplb.config.nameplate_scale")
    @ConfigOption.Range(min = 0.5, max = 1.5)
    @ConfigOption.Slider
    public static float nameplateScale = 1.0f;

    @ConfigEntry(id = "verticalPadding", type = EntryType.INTEGER, translation = "bplb.config.vertical_padding")
    @ConfigOption.Range(min = 0, max = 10)
    @ConfigOption.Slider
    public static int verticalPadding = 0;

    @ConfigEntry(id = "adjustToFov", type = EntryType.BOOLEAN, translation = "bplb.config.adjust_to_fov")
    public static boolean adjustToFov = false;

    @ConfigEntry(id = "fovMultiplier", type = EntryType.FLOAT, translation = "bplb.config.fov_multiplier")
    @ConfigOption.Range(min = 0.5, max = 2.0)
    @ConfigOption.Slider
    public static float fovMultiplier = 1.0f;

    @ConfigEntry(id = "dotType", type = EntryType.STRING, translation = "bplb.config.dot_type")
    public static String dotType = "default";

    @ConfigEntry(id = "iconBorderStyle", type = EntryType.ENUM, translation = "bplb.config.icon_border_style")
    public static BorderStyle iconBorderStyle = BorderStyle.ROUNDED;

    @ConfigEntry(id = "iconBorderType", type = EntryType.ENUM, translation = "bplb.config.icon_border_type")
    public static BorderType iconBorderType = BorderType.DEFAULT;

    @ConfigEntry(id = "inheritBorderColor", type = EntryType.BOOLEAN, translation = "bplb.config.inherit_border_color")
    public static boolean inheritBorderColor = true;

    @ConfigEntry(id = "arrowType", type = EntryType.STRING, translation = "bplb.config.arrow_type")
    public static String arrowType = "default";

    @ConfigEntry(id = "deathMarkerType", type = EntryType.STRING, translation = "bplb.config.death_marker_type")
    public static String deathMarkerType = "default";

    @ConfigEntry(id = "deathMarkerColor", type = EntryType.INTEGER, translation = "bplb.config.death_marker_color")
    public static int deathMarkerColor = 0xFF4c4c;

    @ConfigEntry(id = "deathMarkerInheritBorderColor", type = EntryType.BOOLEAN, translation = "bplb.config.death_marker_inherit_color")
    public static boolean deathMarkerInheritBorderColor = true;

    @ConfigEntry(id = "deathMarkerBorderStyle", type = EntryType.ENUM, translation = "bplb.config.death_marker_border_style")
    public static BorderStyle deathMarkerBorderStyle = BorderStyle.ROUNDED;

    @ConfigEntry(id = "deathMarkerBorderType", type = EntryType.ENUM, translation = "bplb.config.death_marker_border_type")
    public static BorderType deathMarkerBorderType = BorderType.DEFAULT;

    @ConfigEntry(id = "lodestoneMarkerType", type = EntryType.STRING, translation = "bplb.config.lodestone_marker_type")
    public static String lodestoneMarkerType = "default";

    @ConfigEntry(id = "lodestoneMarkerColor", type = EntryType.INTEGER, translation = "bplb.config.lodestone_marker_color")
    public static int lodestoneMarkerColor = 0x00FFFF;

    @ConfigEntry(id = "lodestoneMarkerInheritBorderColor", type = EntryType.BOOLEAN, translation = "bplb.config.lodestone_marker_inherit_color")
    public static boolean lodestoneMarkerInheritBorderColor = true;

    @ConfigEntry(id = "lodestoneMarkerBorderStyle", type = EntryType.ENUM, translation = "bplb.config.lodestone_marker_border_style")
    public static BorderStyle lodestoneMarkerBorderStyle = BorderStyle.ROUNDED;

    @ConfigEntry(id = "lodestoneMarkerBorderType", type = EntryType.ENUM, translation = "bplb.config.lodestone_marker_border_type")
    public static BorderType lodestoneMarkerBorderType = BorderType.DEFAULT;

    @ConfigEntry(id = "lodestoneIconSize", type = EntryType.INTEGER, translation = "bplb.config.lodestone_icon_size")
    @ConfigOption.Range(min = 1, max = 4)
    @ConfigOption.Slider
    public static int lodestoneIconSize = 4;

    @ConfigEntry(id = "heightDifferenceMode", type = EntryType.ENUM, translation = "bplb.config.height_difference_mode")
    public static HeightMode heightDifferenceMode = HeightMode.PLAYER;

    @ConfigEntry(id = "animateModMenuIcon", type = EntryType.BOOLEAN, translation = "bplb.config.animate_modmenu_icon")
    public static boolean animateModMenuIcon = true;

    public enum BorderStyle {
        ROUNDED("rounded"), SQUARED("squared");

        private final String id;

        BorderStyle(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    public enum BorderType {
        DEFAULT("default"), MINIMAL("minimal");

        private final String id;

        BorderType(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    public enum ExperienceBarBackground {
        MOJANG("mojang"), CUSTOM("custom");

        private final String id;

        ExperienceBarBackground(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    public enum HeightMode {
        PLAYER("player"), CAMERA("camera");

        private final String id;

        HeightMode(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    private static final java.util.Map<String, PlayerAppearance> playerConfigs = new java.util.HashMap<>();

    public static java.util.Map<String, PlayerAppearance> getPlayerConfigs() {
        return playerConfigs;
    }

    public static PlayerAppearance getPlayerConfig(String playerName) {
        return playerConfigs.get(playerName);
    }

    public static class PlayerAppearance {
        public String playerName;
        public java.util.UUID playerUuid;
        public String dotType;
        public String iconBorderStyle;
        public String iconBorderType;
        public String arrowType;
        public Integer color;
        public String textureHeadOverride;
    }

    public static String getDeathMarkerBorderStyle() {
        return deathMarkerBorderStyle.getId();
    }

    public static String getLodestoneMarkerBorderStyle() {
        return lodestoneMarkerBorderStyle.getId();
    }

    public static String getIconBorderStyle() {
        return iconBorderStyle.getId();
    }

    public static String getIconBorderType() {
        return iconBorderType.getId();
    }

    public static String getDeathMarkerBorderType() {
        return deathMarkerBorderType.getId();
    }

    public static String getLodestoneMarkerBorderType() {
        return lodestoneMarkerBorderType.getId();
    }

    public static String getNameBorderStyle() {
        return iconBorderStyle.getId();
    }

    public static String getHeightDifferenceMode() {
        return heightDifferenceMode.getId();
    }

    public static String getDotType() {
        return dotType;
    }

    public static String getDeathMarkerType() {
        return deathMarkerType;
    }

    public static String getLodestoneMarkerType() {
        return lodestoneMarkerType;
    }

    public static int getIconSize() {
        return iconSize;
    }

    public static int getDeathMarkerColor() {
        return deathMarkerColor;
    }

    public static boolean isInheritBorderColor() {
        return inheritBorderColor;
    }

    public static boolean isDeathMarkerInheritBorderColor() {
        return deathMarkerInheritBorderColor;
    }

    public static boolean isLodestoneMarkerInheritBorderColor() {
        return lodestoneMarkerInheritBorderColor;
    }

    public static void init() {
        Logger.info("Resourceful Config initialized for BPLB");
    }
}

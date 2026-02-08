package net.bichal.bplb.util;

import net.bichal.bplb.config.Config;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Constants {
    public static final String MOD_ID = "bplb";
    public static final String MOD_NAME_SHORT = "BPLB";
    public static final String MOD_NAME_LARGE = "Better Player Locator Bar";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME_LARGE);
    public static final Config CONFIG = Config.getInstance();
    public static final Identifier STEVE_SKIN_TEXTURE = Identifier.of("minecraft", "textures/entity/player/wide/steve.png");
    public static final int BAR_Y_OFFSET = -26;
    public static final int BAR_WIDTH = 182;
    public static final int EDGE_ALPHA_FADE_MARGIN = 10;
    public static final int ICON_BASE_SIZE = 9;
    public static final float HIGH_Z_DEPTH_START = 1000;
    public static final float Z_DEPTH_INCREMENT = 1;

    public static final int CONFIG_PADDING = 10;
    public static final int CONFIG_SLIDER_WIDTH = 80;
    public static final int CONFIG_TOGGLE_WIDTH = 100;
    public static final int CONFIG_BUTTON_WIDTH = 100;
    public static final int CONFIG_BUTTON_SPACING = 5;

    public static final String CONFIG_KEY_PREFIX = "bplb.config.";
    public static final String BORDER_STYLE_ROUNDED = "rounded";
    public static final String BORDER_STYLE_SQUARED = "squared";

    public static final int BLACK_COLOR = 0xFF1A1A1A;
    public static final int GRAY_COLOR = 0xFF6B6B6B;
    public static final int WHITE_COLOR = 0xFFFAFAFA;
}

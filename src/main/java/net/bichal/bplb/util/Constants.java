package net.bichal.bplb.util;

import net.bichal.bichalutils.client.render.AtlasAnimator;
import net.bichal.bplb.gui.Config;
import net.minecraft.util.Identifier;

public final class Constants {
    private Constants() {}

    public static final String MOD_ID = "bplb";
    public static final String MOD_NAME_SHORT = "BPLB";
    public static final String MOD_NAME_LARGE = "Better Player Locator Bar";

    public static final Config CONFIG = Config.getInstance();

    public static final Identifier STEVE_SKIN_TEXTURE = Identifier.of("minecraft", "textures/entity/player/wide/steve.png");

    public static final int BAR_Y_OFFSET = -26;
    public static final int BAR_WIDTH = 182;
    public static final int EDGE_ALPHA_FADE_MARGIN = 10;
    public static final int ICON_BASE_SIZE = 9;
    public static final int ARROW_BASE_SIZE_WIDTH = 7;
    public static final int ARROW_BASE_SIZE_HEIGHT = 5;

    public static final int CONFIG_PADDING = 10;
    public static final int CONFIG_SLIDER_WIDTH = 80;
    public static final int CONFIG_TOGGLE_WIDTH = 100;
    public static final int CONFIG_BUTTON_WIDTH = 100;
    public static final int CONFIG_BUTTON_SPACING = 5;

    private static final int[] ARROW_FRAME_DURATIONS = {1000, 200};
    private static final int[] ICON_FRAME_DURATIONS = {1000, 1000, 1000, 1000, 1000, 1000};

    private static volatile AtlasAnimator arrowAnimator;
    private static volatile AtlasAnimator iconAnimator;

//    public static final AtlasAnimator ARROW_ANIMATOR = getArrowAnimator();
//    public static final AtlasAnimator ICON_ANIMATOR = getIconAnimator();
//
//    private static AtlasAnimator getArrowAnimator() {
//        if (arrowAnimator == null) {
//            synchronized (Constants.class) {
//                if (arrowAnimator == null) {
//                    arrowAnimator = new AtlasAnimator(
//                            ofMod("arrow_anim"),
//                            2,
//                            ICON_BASE_SIZE,
//                            ICON_BASE_SIZE,
//                            ARROW_FRAME_DURATIONS,
//                            100
//                    );
//                }
//            }
//        }
//        return arrowAnimator;
//    }
//
//    private static AtlasAnimator getIconAnimator() {
//        if (iconAnimator == null) {
//            synchronized (Constants.class) {
//                if (iconAnimator == null) {
//                    iconAnimator = new AtlasAnimator(
//                            ofMod("icon_anim"),
//                            6,
//                            16,
//                            16,
//                            ICON_FRAME_DURATIONS,
//                            50
//                    );
//                }
//            }
//        }
//        return iconAnimator;
//    }

    public static final String CONFIG_KEY_PREFIX = "bplb.config.";
    public static final String BORDER_STYLE_ROUNDED = "rounded";
    public static final String BORDER_STYLE_SQUARED = "squared";

    public static final int BLACK_COLOR = 0xFF1A1A1A;
    public static final int GRAY_COLOR = 0xFF6B6B6B;
    public static final int WHITE_COLOR = 0xFAFAFA;

    public static Identifier ofMod(String path) {
        return Identifier.of(MOD_ID, path);
    }
}

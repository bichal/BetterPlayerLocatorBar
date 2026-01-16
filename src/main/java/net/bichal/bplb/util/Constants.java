package net.bichal.bplb.util;

import net.bichal.bichalutils.client.render.AtlasAnimator;
import net.bichal.bichalutils.util.Logger;
import net.bichal.bichalutils.util.ModIdentifier;
import net.bichal.bplb.gui.Config;
import net.bichal.bplb.gui.animation.Transition;
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
    private static final int MAX_OFFSET_THRESHOLD = 40;
    public static final int EDGE_ALPHA_FADE_MARGIN = 10;

    public static final int ICON_BASE_SIZE = 9;
    public static final int ARROW_BASE_SIZE_WIDTH = 7;
    public static final int ARROW_BASE_SIZE_HEIGHT = 5;

    public static final int CONFIG_PADDING = 10;
    public static final int HEADER_HEIGHT = 65;
    public static final int FOOTER_HEIGHT = 30;
    public static final int TAB_BAR_HEIGHT = 24;
    public static final int ASIDE_COLLAPSED_WIDTH = 6;
    public static final int ASIDE_EXPANDED_WIDTH = 150;
    public static final int SEARCH_FIELD_WIDTH = 250;
    public static final int SEARCH_BUTTON_SIZE = 25;

    public static final int HIGHLIGHT_DURATION_MS = 1000;
    public static final int TOOLTIP_FADE_DELAY_MS = 200;
    public static final int SECTION_FLASH_DURATION_MS = 1000;

    public static Transition createTransition() {
        return new Transition(0, 120f, 29f);
    }

    private static final int[] MOD_MENU_ICON_DURATIONS = {200, 200, 200, 200, 200, 200};
    private static volatile AtlasAnimator modMenuIconAnimator;

    public static AtlasAnimator getIconAnimator() {
        if (modMenuIconAnimator == null) {
            synchronized (Constants.class) {
                if (modMenuIconAnimator == null) {
                    try {
                        modMenuIconAnimator = new AtlasAnimator(
                                ModIdentifier.ofMod("sprites/icons/frame"),
                                6, 16, 16,
                                MOD_MENU_ICON_DURATIONS,
                                50
                        );
                    } catch (Exception e) {
                        Logger.error("Failed to initialize ModMenu icon animator", e);
                    }
                }
            }
        }
        return modMenuIconAnimator;
    }

    public static final String CONFIG_KEY_PREFIX = "bplb.config.";
    public static final String BORDER_STYLE_ROUNDED = "rounded";
    public static final String BORDER_STYLE_SQUARED = "squared";

    public static final int BLACK_COLOR = 0xFF1A1A1A;
    public static final int BLACK_BORDER_COLOR = 0xFF0E1110;
    public static final int GRAY_COLOR = 0xFF6B6B6B;
    public static final int WHITE_COLOR = 0xFAFAFA;

    public static Transition createHoverTransition() {
        return new Transition(0, 100f, 32.5f);
    }

    public static Transition createFlashTransition() {
        return new Transition(0, 200f, 40f);
    }

    public static Transition createToggleTransition() {
        return new Transition(0, 260f, 32f);
    }

    public static Transition createScrollTransition() {
        return new Transition(0, 280f, 35f);
    }

    public static int calculateTotalIconHeight() {
        int iconSize = CONFIG.getIconSize() * ICON_BASE_SIZE / 4;
        int verticalPadding = CONFIG.getVerticalPadding();
        return iconSize + (ARROW_BASE_SIZE_HEIGHT + verticalPadding) * 2;
    }

    public static int calculateNameplateHeight() {
        return (int) (12 * CONFIG.getNameplateScale());
    }

    public static int calculateTabOffset() {
        return -(18 + calculateNameplateHeight());
    }

    public static boolean shouldElevateElement(int currentOffset) {
        return currentOffset > -MAX_OFFSET_THRESHOLD;
    }
}

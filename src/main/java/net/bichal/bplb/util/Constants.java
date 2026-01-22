package net.bichal.bplb.util;

import net.bichal.bichalutils.client.render.AtlasAnimator;
import net.bichal.bichalutils.util.Logger;
import net.bichal.bichalutils.util.ModIdentifier;
import net.minecraft.util.Identifier;

public final class Constants {
    private Constants() {}

    public static final String MOD_ID = "bplb";
    public static final String MOD_NAME_SHORT = "BPLB";
    public static final String MOD_NAME_LARGE = "Better Player Locator Bar";
    public static final Identifier STEVE_SKIN_TEXTURE = Identifier.of("minecraft", "textures/entity/player/wide/steve.png");

    public static final int BAR_Y_OFFSET = -26;
    public static final int BAR_WIDTH = 182;
    public static final int EDGE_ALPHA_FADE_MARGIN = 10;
    public static final int ICON_BASE_SIZE = 9;
    public static final int ARROW_BASE_SIZE_WIDTH = 7;
    public static final int ARROW_BASE_SIZE_HEIGHT = 5;

    public static final String BORDER_STYLE_ROUNDED = "rounded";
    public static final String BORDER_STYLE_SQUARED = "squared";

    private static volatile AtlasAnimator modMenuIconAnimator;

    public static AtlasAnimator getIconAnimator() {
        if (modMenuIconAnimator == null) {
            synchronized (Constants.class) {
                if (modMenuIconAnimator == null) {
                    try {
                        modMenuIconAnimator = new AtlasAnimator(
                                ModIdentifier.ofMod("sprites/icons/frame"),
                                6, 16, 16,
                                new int[]{200, 200, 200, 200, 200, 200},
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
}

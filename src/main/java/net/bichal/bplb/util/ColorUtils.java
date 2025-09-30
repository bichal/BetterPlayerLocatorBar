package net.bichal.bplb.util;

import java.util.UUID;

public class ColorUtils {
    @SuppressWarnings("unused")
    public static int darkerColoring(int color, float darkness) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (Math.max(0, ((color >> 16) & 0xFF) * (1.0f - darkness)));
        int g = (int) (Math.max(0, ((color >> 8) & 0xFF) * (1.0f - darkness)));
        int b = (int) (Math.max(0, (color & 0xFF) * (1.0f - darkness)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int darkerColoring(int color) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (Math.max(0, ((color >> 16) & 0xFF) * (1.0f - 0.45f)));
        int g = (int) (Math.max(0, ((color >> 8) & 0xFF) * (1.0f - 0.45f)));
        int b = (int) (Math.max(0, (color & 0xFF) * (1.0f - 0.45f)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int fromRGB(int r, int g, int b) {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    public static float[] getRGBComponents(int color) {
        return new float[] {
                ((color >> 16) & 0xFF) / 255.0f,
                ((color >> 8) & 0xFF) / 255.0f,
                (color & 0xFF) / 255.0f
        };
    }

    public static int generateColorFromUUID(UUID uuid) {
        java.util.Random random = new java.util.Random(uuid.getLeastSignificantBits() ^ uuid.getMostSignificantBits());
        return 0xFF000000 |
                ((random.nextInt(206) + 50) << 16) |
                ((random.nextInt(206) + 50) << 8) |
                (random.nextInt(206) + 50);
    }
}

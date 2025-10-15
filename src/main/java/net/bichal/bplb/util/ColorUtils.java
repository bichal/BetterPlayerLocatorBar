package net.bichal.bplb.util;

import java.util.Random;
import java.util.UUID;

public class ColorUtils {
    public static int darkerColoring(int color) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (Math.max(0, ((color >> 16) & 0xFF) * (1.0f - 0.45f)));
        int g = (int) (Math.max(0, ((color >> 8) & 0xFF) * (1.0f - 0.45f)));
        int b = (int) (Math.max(0, (color & 0xFF) * (1.0f - 0.45f)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int generateColorFromUUID(UUID uuid) {
        Random random = new Random(uuid.getLeastSignificantBits() ^ uuid.getMostSignificantBits());
        return 0xFF000000 | ((random.nextInt(206) + 50) << 16) | ((random.nextInt(206) + 50) << 8) | (random.nextInt(206) + 50);
    }
}

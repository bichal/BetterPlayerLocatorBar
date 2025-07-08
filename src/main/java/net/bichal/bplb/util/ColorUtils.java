package net.bichal.bplb.util;

public class ColorUtils {
    public static int rgba(int color, float darkness) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (Math.max(0, ((color >> 16) & 0xFF) * (1.0f - darkness)));
        int g = (int) (Math.max(0, ((color >> 8) & 0xFF) * (1.0f - darkness)));
        int b = (int) (Math.max(0, (color & 0xFF) * (1.0f - darkness)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}

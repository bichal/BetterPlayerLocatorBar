package net.bichal.bplb.util;

import net.bichal.bplb.config.Config;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilsTest {
    @Test
    void easingHitsItsEndpointsAndIsMonotonic() {
        assertEquals(0f, MathUtils.easeInOutQuad(0f), 1e-6);
        assertEquals(0.5f, MathUtils.easeInOutQuad(0.5f), 1e-6);
        assertEquals(1f, MathUtils.easeInOutQuad(1f), 1e-6);
        float previous = -1f;
        for (int i = 0; i <= 100; i++) {
            float value = MathUtils.easeInOutQuad(i / 100f);
            assertTrue(value >= previous);
            previous = value;
        }
    }

    @Test
    void distanceIsEuclidean() {
        assertEquals(5.0, DistanceUtils.calculateDistance(0, 0, 0, 3, 0, 4), 1e-9);
        assertEquals(0.0, DistanceUtils.calculateDistance(7, 8, 9, 7, 8, 9), 1e-9);
    }

    @Test
    void darkerColoringKeepsAlphaAndDimsChannels() {
        int dark = ColorUtils.darkerColoring(0xFFC8C8C8);
        assertEquals(0xFF, dark >>> 24);
        assertEquals((int) (0xC8 * 0.55f), (dark >> 16) & 0xFF);
        assertEquals(0xFF000000, ColorUtils.darkerColoring(0xFF000000));
    }

    @Test
    void uuidColorIsStableOpaqueAndNotTooDark() {
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        int color = ColorUtils.generateColorFromUUID(id);
        assertEquals(color, ColorUtils.generateColorFromUUID(id));
        assertEquals(0xFF, color >>> 24);
        for (int shift : new int[]{16, 8, 0}) assertTrue(((color >> shift) & 0xFF) >= 50);
        assertNotEquals(color, ColorUtils.generateColorFromUUID(new UUID(1, 2)));
    }

    @Test
    void configCopyAndResetRoundTrip() {
        Config source = new Config();
        source.setIconSize(2);
        source.setLerpSpeed(0.3f);
        source.setModEnabled(false);

        Config target = new Config();
        Config.copy(source, target);
        assertEquals(2, target.getIconSize());
        assertEquals(0.3f, target.getLerpSpeed(), 1e-6);
        assertEquals(false, target.isModEnabled());

        target.resetToDefaults();
        Config defaults = new Config();
        assertEquals(defaults.getIconSize(), target.getIconSize());
        assertEquals(defaults.getLerpSpeed(), target.getLerpSpeed(), 1e-6);
        assertEquals(defaults.isModEnabled(), target.isModEnabled());
    }
}

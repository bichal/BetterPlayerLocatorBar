package net.bichal.bplb.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigScreenRenderTest {
    /**
     * Screen.render already draws the background (and, from 1.21.2 on, blurs it, which may only
     * happen once per frame). ConfigScreen must therefore never call renderBackground itself.
     */
    @Test
    void screenNeverDrawsTheBackgroundItself() throws IOException {
        try (InputStream in = ConfigScreen.class.getResourceAsStream("ConfigScreen.class")) {
            assertNotNull(in);
            String constants = new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
            assertFalse(constants.contains("renderBackground"), "ConfigScreen references renderBackground");
            assertFalse(constants.contains("renderBlurredBackground"), "ConfigScreen references renderBlurredBackground");
        }
    }
}

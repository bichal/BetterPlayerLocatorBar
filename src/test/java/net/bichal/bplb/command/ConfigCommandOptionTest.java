package net.bichal.bplb.command;

import net.bichal.bplb.config.Config;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigCommandOptionTest {
    @Test
    void intOptionParsesAndBoundsChecks() {
        Config config = new Config();
        ConfigCommand.IntOption option = new ConfigCommand.IntOption(Config::getIconSize, Config::setIconSize, 1, 4);

        assertTrue(option.setValue(config, "3"));
        assertEquals(3, config.getIconSize());
        assertTrue(option.setValue(config, " 2 "));
        assertEquals(2, config.getIconSize());

        assertFalse(option.setValue(config, "9"));
        assertFalse(option.setValue(config, "abc"));
        assertFalse(option.setValue(config, ""));
        assertEquals(2, config.getIconSize(), "rejected values leave the setting alone");
    }

    @Test
    void floatOptionBoundsAndFormat() {
        Config config = new Config();
        ConfigCommand.FloatOption option = new ConfigCommand.FloatOption(Config::getLerpSpeed, Config::setLerpSpeed, 0.1f, 1.0f);

        assertTrue(option.setValue(config, "0.5"));
        assertEquals(0.5f, config.getLerpSpeed(), 1e-6);
        assertFalse(option.setValue(config, "5"));
        assertFalse(option.setValue(config, "fast"));
        assertEquals("0.50", option.getValue(config));
    }

    @Test
    void booleanOptionOnlyAcceptsTrueAndFalse() {
        Config config = new Config();
        ConfigCommand.BooleanOption option = new ConfigCommand.BooleanOption("modEnabled", Config::isModEnabled, Config::setModEnabled);

        assertTrue(option.setValue(config, "false"));
        assertFalse(config.isModEnabled());
        assertTrue(option.setValue(config, "TRUE"));
        assertTrue(config.isModEnabled());
        assertFalse(option.setValue(config, "yes"));
        assertEquals("true", option.getValue(config));
    }

    @Test
    void stringOptionRejectsEmpty() {
        Config config = new Config();
        ConfigCommand.StringOption option = new ConfigCommand.StringOption(Config::getDotType, Config::setDotType);

        assertTrue(option.setValue(config, "minimal"));
        assertEquals("minimal", config.getDotType());
        assertFalse(option.setValue(config, ""));
        assertEquals("minimal", config.getDotType());
    }
}

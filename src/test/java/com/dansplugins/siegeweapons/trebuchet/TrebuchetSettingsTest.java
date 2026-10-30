package com.dansplugins.siegeweapons.trebuchet;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrebuchetSettingsTest {

    private final List<String> warnings = new ArrayList<>();

    private TrebuchetSettings read(String yaml) throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(yaml);
        return TrebuchetSettings.from(config, warnings::add);
    }

    @Test
    void theBundledConfigParsesWithoutWarnings() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
            assertNotNull(in, "config.yml must be on the classpath");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(in, StandardCharsets.UTF_8));

            TrebuchetSettings settings = TrebuchetSettings.from(config, warnings::add);

            assertEquals(TrebuchetSettings.DEFAULT_EXPLOSION_POWER, settings.explosionPower());
            assertEquals(Duration.ofSeconds(TrebuchetSettings.DEFAULT_SHOT_COOLDOWN_SECONDS), settings.shotCooldown());
            assertEquals(TrebuchetSettings.DEFAULT_MIN_RANGE, settings.minRange());
            assertEquals(TrebuchetSettings.DEFAULT_MAX_RANGE, settings.maxRange());
            assertEquals(TrebuchetSettings.DEFAULT_RANGE_STEP, settings.rangeStep());
        }
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void readsTheExplosionPower() throws Exception {
        assertEquals(5.5f, read("trebuchet:\n  explosion-power: 5.5\n").explosionPower());
        assertEquals(2.0f, read("trebuchet:\n  explosion-power: 2\n").explosionPower());
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void aMissingExplosionPowerUsesTheDefaultSilently() throws Exception {
        assertEquals(TrebuchetSettings.DEFAULT_EXPLOSION_POWER, read("").explosionPower());
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void anInvalidExplosionPowerUsesTheDefaultAndWarns() throws Exception {
        for (String value : List.of("0", "-1", "lots", ".nan", "1e300")) {
            warnings.clear();

            TrebuchetSettings settings = read("trebuchet:\n  explosion-power: " + value + "\n");

            assertEquals(TrebuchetSettings.DEFAULT_EXPLOSION_POWER, settings.explosionPower(), value);
            assertEquals(1, warnings.size(), value);
            assertTrue(warnings.get(0).contains(TrebuchetSettings.EXPLOSION_POWER_KEY), warnings::toString);
        }
    }

    @Test
    void readsTheShotCooldown() throws Exception {
        assertEquals(Duration.ofSeconds(30), read("trebuchet:\n  shot-cooldown-seconds: 30\n").shotCooldown());
        assertEquals(Duration.ZERO, read("trebuchet:\n  shot-cooldown-seconds: 0\n").shotCooldown());
        assertEquals(Duration.ofSeconds(TrebuchetSettings.MAX_SHOT_COOLDOWN_SECONDS),
                read("trebuchet:\n  shot-cooldown-seconds: 86400\n").shotCooldown());
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void aMissingShotCooldownUsesTheDefaultSilently() throws Exception {
        assertEquals(Duration.ofSeconds(TrebuchetSettings.DEFAULT_SHOT_COOLDOWN_SECONDS), read("").shotCooldown());
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void anInvalidShotCooldownUsesTheDefaultAndWarns() throws Exception {
        for (String value : List.of("-1", "2.5", "lots", "86401", "99999999999")) {
            warnings.clear();

            TrebuchetSettings settings = read("trebuchet:\n  shot-cooldown-seconds: " + value + "\n");

            assertEquals(Duration.ofSeconds(TrebuchetSettings.DEFAULT_SHOT_COOLDOWN_SECONDS), settings.shotCooldown(),
                    value);
            assertEquals(1, warnings.size(), value);
            assertTrue(warnings.get(0).contains(TrebuchetSettings.SHOT_COOLDOWN_KEY), warnings::toString);
        }
    }

    @Test
    void readsTheRange() throws Exception {
        TrebuchetSettings settings = read("trebuchet:\n  min-range: 5\n  max-range: 150\n  range-step: 25\n");

        assertEquals(5, settings.minRange());
        assertEquals(150, settings.maxRange());
        assertEquals(25, settings.rangeStep());
        assertEquals(40, read("trebuchet:\n  min-range: 40\n  max-range: 40\n").maxRange());
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void aMissingRangeUsesTheDefaultsSilently() throws Exception {
        TrebuchetSettings settings = read("");

        assertEquals(TrebuchetSettings.DEFAULT_MIN_RANGE, settings.minRange());
        assertEquals(TrebuchetSettings.DEFAULT_MAX_RANGE, settings.maxRange());
        assertEquals(TrebuchetSettings.DEFAULT_RANGE_STEP, settings.rangeStep());
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void anInvalidRangeValueUsesItsDefaultAndWarns() throws Exception {
        for (String key : List.of("min-range", "max-range", "range-step")) {
            for (String value : List.of("0", "-5", "151", "12.5", "far")) {
                warnings.clear();

                TrebuchetSettings settings = read("trebuchet:\n  " + key + ": " + value + "\n");

                assertEquals(TrebuchetSettings.DEFAULT_MIN_RANGE, settings.minRange(), key + "=" + value);
                assertEquals(TrebuchetSettings.DEFAULT_MAX_RANGE, settings.maxRange(), key + "=" + value);
                assertEquals(TrebuchetSettings.DEFAULT_RANGE_STEP, settings.rangeStep(), key + "=" + value);
                assertEquals(1, warnings.size(), key + "=" + value);
                assertTrue(warnings.get(0).contains("trebuchet." + key), warnings::toString);
            }
        }
    }

    @Test
    void aMinRangeAboveTheMaxRangeUsesBothDefaultsAndWarns() throws Exception {
        TrebuchetSettings settings = read("trebuchet:\n  min-range: 90\n  max-range: 30\n  range-step: 5\n");

        assertEquals(TrebuchetSettings.DEFAULT_MIN_RANGE, settings.minRange());
        assertEquals(TrebuchetSettings.DEFAULT_MAX_RANGE, settings.maxRange());
        assertEquals(5, settings.rangeStep());
        assertEquals(1, warnings.size(), warnings::toString);
        assertTrue(warnings.get(0).contains(TrebuchetSettings.MIN_RANGE_KEY), warnings::toString);
        assertTrue(warnings.get(0).contains(TrebuchetSettings.MAX_RANGE_KEY), warnings::toString);
    }

    @Test
    void noConfigKeyMakesAShotBreakBlocks() throws Exception {
        TrebuchetSettings settings = read("""
                trebuchet:
                  explosion-power: 4
                  break-blocks: true
                  block-damage: true
                """);

        assertFalse(settings.breaksBlocks());
    }
}

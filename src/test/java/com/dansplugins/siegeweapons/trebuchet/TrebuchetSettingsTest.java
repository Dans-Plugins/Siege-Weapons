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

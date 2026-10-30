package com.dansplugins.siegeweapons.trebuchet;

import org.bukkit.configuration.ConfigurationSection;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * The configurable values a trebuchet shot uses, read from {@code config.yml}.
 *
 * <p>There is deliberately no setting for block damage. A shot never breaks blocks until the
 * Medieval Factions war gate exists, and once it does, only that gate decides (MVP.md: block
 * damage outside a declared war is never configurable).
 */
public record TrebuchetSettings(float explosionPower, Duration shotCooldown, int minRange, int maxRange,
                                int rangeStep) {

    public static final String EXPLOSION_POWER_KEY = "trebuchet.explosion-power";
    public static final float DEFAULT_EXPLOSION_POWER = 3.0f;

    public static final String SHOT_COOLDOWN_KEY = "trebuchet.shot-cooldown-seconds";
    public static final int DEFAULT_SHOT_COOLDOWN_SECONDS = 10;
    /** A day: long enough for any real cooldown, short enough that no arithmetic overflows. */
    public static final int MAX_SHOT_COOLDOWN_SECONDS = 86_400;

    public static final String MIN_RANGE_KEY = "trebuchet.min-range";
    public static final int DEFAULT_MIN_RANGE = 20;
    public static final String MAX_RANGE_KEY = "trebuchet.max-range";
    public static final int DEFAULT_MAX_RANGE = 80;
    public static final String RANGE_STEP_KEY = "trebuchet.range-step";
    public static final int DEFAULT_RANGE_STEP = 10;
    /** The longest range any setting allows, in blocks. */
    public static final int RANGE_LIMIT = 150;

    /**
     * Reads the settings, falling back to the default for any value that is missing or invalid.
     * An invalid value is reported through {@code warn}; a missing one is not.
     */
    public static TrebuchetSettings from(ConfigurationSection config, Consumer<String> warn) {
        float explosionPower = DEFAULT_EXPLOSION_POWER;
        Object raw = config.get(EXPLOSION_POWER_KEY);
        if (raw != null) {
            if (raw instanceof Number number
                    && Float.isFinite(number.floatValue()) && number.floatValue() > 0) {
                explosionPower = number.floatValue();
            } else {
                warn.accept(EXPLOSION_POWER_KEY + " must be a number greater than 0, but is '" + raw
                        + "'; using " + DEFAULT_EXPLOSION_POWER + ".");
            }
        }
        int shotCooldownSeconds = wholeNumber(config, SHOT_COOLDOWN_KEY, 0, MAX_SHOT_COOLDOWN_SECONDS,
                DEFAULT_SHOT_COOLDOWN_SECONDS, warn);
        int minRange = wholeNumber(config, MIN_RANGE_KEY, 1, RANGE_LIMIT, DEFAULT_MIN_RANGE, warn);
        int maxRange = wholeNumber(config, MAX_RANGE_KEY, 1, RANGE_LIMIT, DEFAULT_MAX_RANGE, warn);
        if (minRange > maxRange) {
            warn.accept(MIN_RANGE_KEY + " (" + minRange + ") is greater than " + MAX_RANGE_KEY + " (" + maxRange
                    + "); using " + DEFAULT_MIN_RANGE + " and " + DEFAULT_MAX_RANGE + ".");
            minRange = DEFAULT_MIN_RANGE;
            maxRange = DEFAULT_MAX_RANGE;
        }
        int rangeStep = wholeNumber(config, RANGE_STEP_KEY, 1, RANGE_LIMIT, DEFAULT_RANGE_STEP, warn);
        return new TrebuchetSettings(explosionPower, Duration.ofSeconds(shotCooldownSeconds), minRange, maxRange,
                rangeStep);
    }

    private static int wholeNumber(ConfigurationSection config, String key, int min, int max, int fallback,
                                   Consumer<String> warn) {
        Object raw = config.get(key);
        if (raw == null) {
            return fallback;
        }
        if ((raw instanceof Integer || raw instanceof Long)
                && ((Number) raw).longValue() >= min && ((Number) raw).longValue() <= max) {
            return ((Number) raw).intValue();
        }
        warn.accept(key + " must be a whole number from " + min + " to " + max + ", but is '" + raw
                + "'; using " + fallback + ".");
        return fallback;
    }

    /** Whether a shot's explosion breaks blocks. Always {@code false} until the war gate exists. */
    public boolean breaksBlocks() {
        return false;
    }
}

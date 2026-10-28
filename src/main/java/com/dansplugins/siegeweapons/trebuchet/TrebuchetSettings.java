package com.dansplugins.siegeweapons.trebuchet;

import org.bukkit.configuration.ConfigurationSection;

import java.util.function.Consumer;

/**
 * The configurable values a trebuchet shot uses, read from {@code config.yml}.
 *
 * <p>There is deliberately no setting for block damage. A shot never breaks blocks until the
 * Medieval Factions war gate exists, and once it does, only that gate decides (MVP.md: block
 * damage outside a declared war is never configurable).
 */
public record TrebuchetSettings(float explosionPower) {

    public static final String EXPLOSION_POWER_KEY = "trebuchet.explosion-power";
    public static final float DEFAULT_EXPLOSION_POWER = 3.0f;

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
        return new TrebuchetSettings(explosionPower);
    }

    /** Whether a shot's explosion breaks blocks. Always {@code false} until the war gate exists. */
    public boolean breaksBlocks() {
        return false;
    }
}

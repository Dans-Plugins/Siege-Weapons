package com.dansplugins.siegeweapons.trebuchet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Range. An engine always throws along its facing, which the structure fixes; its range is the
 * one thing a player aims. A new engine starts at the shortest range, and each adjustment
 * lengthens it by one step until the longest range, after which it starts again at the shortest.
 *
 * <p>Ranges are kept in memory only, so a server restart puts every engine back at the shortest
 * range. A destroyed engine's range is lost with it.
 */
public final class TrebuchetAiming {

    /** The range {@code trebuchet} was set to by an adjustment. */
    public record Aim(Trebuchet trebuchet, int range) {
    }

    private final TrebuchetRegistry registry;
    private final int minRange;
    private final int maxRange;
    private final int step;
    private final Map<UUID, Integer> ranges = new HashMap<>();

    /**
     * @throws IllegalArgumentException unless {@code 1 <= minRange <= maxRange} and {@code step >= 1}
     */
    public TrebuchetAiming(TrebuchetRegistry registry, int minRange, int maxRange, int step) {
        if (minRange < 1 || minRange > maxRange || step < 1) {
            throw new IllegalArgumentException("invalid range " + minRange + ".." + maxRange + " step " + step);
        }
        this.registry = registry;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.step = step;
    }

    /**
     * Adjusts the range of the engine whose load block is at ({@code x}, {@code y}, {@code z}).
     *
     * @return empty if the block is not the load block of a registered trebuchet
     */
    public Optional<Aim> adjust(String world, int x, int y, int z) {
        return registry.findByAnchor(world, x, y - TrebuchetFiring.LOAD_BLOCK_UP, z).map(trebuchet -> {
            int current = rangeOf(trebuchet);
            int next = current >= maxRange ? minRange : Math.min(current + step, maxRange);
            ranges.put(trebuchet.id(), next);
            return new Aim(trebuchet, next);
        });
    }

    /** The range {@code trebuchet} is aimed at, in blocks. */
    public int rangeOf(Trebuchet trebuchet) {
        return ranges.getOrDefault(trebuchet.id(), minRange);
    }
}

package com.dansplugins.siegeweapons.trebuchet;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * Loading and firing. The centre of the axle is the load block: using it with ammunition in hand
 * loads the engine, and using it again fires. An engine holds one shot at a time, and after firing
 * it cannot fire again until its cooldown has passed. It can be reloaded during the cooldown.
 *
 * <p>Loads are kept in memory only, so a loaded engine is unloaded by a server restart. A
 * destroyed engine's load and cooldown are lost with it: rebuilding it makes a new, unloaded
 * engine that can fire as soon as it is loaded.
 */
public final class TrebuchetFiring {

    /** Height of the load block, the centre of the axle, above the anchor. */
    public static final int LOAD_BLOCK_UP = 2;

    /** What using a load block did. */
    public enum Result {
        /** The engine had no shot, and one was loaded from the player's hand. */
        LOADED,
        /** The engine's shot was fired. */
        FIRED,
        /** The engine had no shot, and the player held no ammunition. */
        NOT_LOADED,
        /** The engine is loaded but fired too recently to fire again; it keeps its shot. */
        COOLING_DOWN
    }

    /**
     * The outcome of using the load block of {@code trebuchet}.
     *
     * @param cooldownRemaining how long until the engine can fire again; zero unless the result is
     *                          {@link Result#COOLING_DOWN}
     */
    public record Interaction(Trebuchet trebuchet, Result result, Duration cooldownRemaining) {

        Interaction(Trebuchet trebuchet, Result result) {
            this(trebuchet, result, Duration.ZERO);
        }
    }

    private final TrebuchetRegistry registry;
    private final long cooldownNanos;
    private final LongSupplier nanoClock;
    private final Set<UUID> loaded = new HashSet<>();
    private final Map<UUID, Long> lastFired = new HashMap<>();

    /**
     * @param cooldown  the shortest time between two shots of one engine
     * @param nanoClock a monotonic clock in nanoseconds, such as {@link System#nanoTime()}
     */
    public TrebuchetFiring(TrebuchetRegistry registry, Duration cooldown, LongSupplier nanoClock) {
        this.registry = registry;
        this.cooldownNanos = cooldown.toNanos();
        this.nanoClock = nanoClock;
    }

    /**
     * Handles a player using the block at ({@code x}, {@code y}, {@code z}).
     *
     * @param holdingAmmunition whether the player holds ammunition; on {@link Result#LOADED} the
     *                          caller takes one from them
     * @return empty if the block is not the load block of a registered trebuchet
     */
    public Optional<Interaction> use(String world, int x, int y, int z, boolean holdingAmmunition) {
        return engineWithLoadBlockAt(world, x, y, z).map(trebuchet -> {
            if (loaded.contains(trebuchet.id())) {
                long now = nanoClock.getAsLong();
                Long firedAt = lastFired.get(trebuchet.id());
                if (firedAt != null && now - firedAt < cooldownNanos) {
                    return new Interaction(trebuchet, Result.COOLING_DOWN,
                            Duration.ofNanos(cooldownNanos - (now - firedAt)));
                }
                loaded.remove(trebuchet.id());
                lastFired.put(trebuchet.id(), now);
                return new Interaction(trebuchet, Result.FIRED);
            }
            if (holdingAmmunition) {
                loaded.add(trebuchet.id());
                return new Interaction(trebuchet, Result.LOADED);
            }
            return new Interaction(trebuchet, Result.NOT_LOADED);
        });
    }

    /** {@code duration} in whole seconds, rounded up, so a player is never told to wait 0s. */
    public static long wholeSecondsUp(Duration duration) {
        long seconds = duration.getSeconds();
        return duration.getNano() > 0 ? seconds + 1 : seconds;
    }

    public boolean isLoaded(Trebuchet trebuchet) {
        return loaded.contains(trebuchet.id());
    }

    private Optional<Trebuchet> engineWithLoadBlockAt(String world, int x, int y, int z) {
        // The axle centre sits straight above the anchor whichever way the engine faces.
        return registry.findByAnchor(world, x, y - LOAD_BLOCK_UP, z);
    }
}

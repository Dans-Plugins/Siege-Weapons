package com.dansplugins.siegeweapons.trebuchet;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Loading and firing. The centre of the axle is the load block: using it with ammunition in hand
 * loads the engine, and using it again fires. An engine holds one shot at a time.
 *
 * <p>Loads are kept in memory only, so a loaded engine is unloaded by a server restart. A
 * destroyed engine's load is lost with it: rebuilding it makes a new, unloaded engine.
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
        NOT_LOADED
    }

    /** The outcome of using the load block of {@code trebuchet}. */
    public record Interaction(Trebuchet trebuchet, Result result) {
    }

    private final TrebuchetRegistry registry;
    private final Set<UUID> loaded = new HashSet<>();

    public TrebuchetFiring(TrebuchetRegistry registry) {
        this.registry = registry;
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
            if (loaded.remove(trebuchet.id())) {
                return new Interaction(trebuchet, Result.FIRED);
            }
            if (holdingAmmunition) {
                loaded.add(trebuchet.id());
                return new Interaction(trebuchet, Result.LOADED);
            }
            return new Interaction(trebuchet, Result.NOT_LOADED);
        });
    }

    public boolean isLoaded(Trebuchet trebuchet) {
        return loaded.contains(trebuchet.id());
    }

    private Optional<Trebuchet> engineWithLoadBlockAt(String world, int x, int y, int z) {
        // The axle centre sits straight above the anchor whichever way the engine faces.
        return registry.findByAnchor(world, x, y - LOAD_BLOCK_UP, z);
    }
}

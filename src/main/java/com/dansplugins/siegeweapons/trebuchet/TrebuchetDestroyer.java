package com.dansplugins.siegeweapons.trebuchet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Destroys the engines a removed block belonged to. Every block of the pattern is a key block:
 * a trebuchet missing any part is no longer a trebuchet, so losing one unregisters the engine.
 * Rebuilding the missing block registers it again as a new engine.
 */
public final class TrebuchetDestroyer {

    private final TrebuchetRegistry registry;
    private final TrebuchetStorage storage;
    private final Consumer<String> warn;

    public TrebuchetDestroyer(TrebuchetRegistry registry, TrebuchetStorage storage, Consumer<String> warn) {
        this.registry = registry;
        this.storage = storage;
        this.warn = warn;
    }

    /**
     * Unregisters every trebuchet that has a part at ({@code x}, {@code y}, {@code z}), and saves
     * the registry if any were removed.
     *
     * @return the trebuchets destroyed by losing this block
     */
    public List<Trebuchet> blockRemoved(String world, int x, int y, int z) {
        List<Trebuchet> destroyed = new ArrayList<>();
        for (Trebuchet trebuchet : registry.all()) {
            if (trebuchet.world().equals(world)
                    && TrebuchetPattern.isPartAt(
                            trebuchet.x(), trebuchet.y(), trebuchet.z(), trebuchet.facing(), x, y, z)
                    && registry.unregister(trebuchet)) {
                destroyed.add(trebuchet);
            }
        }
        if (!destroyed.isEmpty()) {
            save();
        }
        return destroyed;
    }

    /**
     * Checks the trebuchet whose load block is at ({@code x}, {@code y}, {@code z}) against the
     * world, and destroys it if any part is missing. This catches a part lost in a way no event
     * reported, so an engine that has lost one can never be loaded, aimed or fired.
     *
     * @return the trebuchet destroyed; empty if this is not a load block or the engine is whole
     */
    public Optional<Trebuchet> loadBlockUsed(String world, TrebuchetPattern.BlockLookup blocks, int x, int y, int z) {
        Optional<Trebuchet> destroyed = registry.findByAnchor(world, x, y - TrebuchetFiring.LOAD_BLOCK_UP, z)
                .filter(trebuchet -> !TrebuchetPattern.isCompleteAt(
                        blocks, trebuchet.x(), trebuchet.y(), trebuchet.z(), trebuchet.facing()))
                .filter(registry::unregister);
        if (destroyed.isPresent()) {
            save();
        }
        return destroyed;
    }

    private void save() {
        try {
            storage.save(registry.all());
        } catch (IOException e) {
            warn.accept("Could not save trebuchets: " + e.getMessage());
        }
    }
}

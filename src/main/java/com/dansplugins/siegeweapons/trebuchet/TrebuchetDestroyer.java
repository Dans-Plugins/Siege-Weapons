package com.dansplugins.siegeweapons.trebuchet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
            try {
                storage.save(registry.all());
            } catch (IOException e) {
                warn.accept("Could not save trebuchets: " + e.getMessage());
            }
        }
        return destroyed;
    }
}

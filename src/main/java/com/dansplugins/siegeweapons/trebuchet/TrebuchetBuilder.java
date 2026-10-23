package com.dansplugins.siegeweapons.trebuchet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/** Turns a placed block that completes a trebuchet into a registered, saved engine. */
public final class TrebuchetBuilder {

    private final TrebuchetRegistry registry;
    private final TrebuchetStorage storage;
    private final Consumer<String> warn;

    public TrebuchetBuilder(TrebuchetRegistry registry, TrebuchetStorage storage, Consumer<String> warn) {
        this.registry = registry;
        this.storage = storage;
        this.warn = warn;
    }

    /**
     * Registers every not-yet-registered trebuchet the block at ({@code x}, {@code y}, {@code z})
     * completes, and saves the registry if any were added.
     *
     * @return the trebuchets newly built by this block
     */
    public List<Trebuchet> blockPlaced(String world, TrebuchetPattern.BlockLookup blocks, int x, int y, int z) {
        List<Trebuchet> built = new ArrayList<>();
        for (TrebuchetPattern.Match match : TrebuchetPattern.findCompletedBy(blocks, x, y, z)) {
            Trebuchet trebuchet = new Trebuchet(
                    UUID.randomUUID(), world, match.x(), match.y(), match.z(), match.facing());
            if (registry.register(trebuchet)) {
                built.add(trebuchet);
            }
        }
        if (!built.isEmpty()) {
            try {
                storage.save(registry.all());
            } catch (IOException e) {
                warn.accept("Could not save trebuchets: " + e.getMessage());
            }
        }
        return built;
    }
}

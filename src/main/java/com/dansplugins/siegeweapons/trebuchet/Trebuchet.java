package com.dansplugins.siegeweapons.trebuchet;

import java.util.Objects;
import java.util.UUID;

/**
 * A built trebuchet. The anchor is the centre block of the base layer; the structure is
 * emplaced and never moves, so the anchor identifies the engine within its world.
 */
public record Trebuchet(UUID id, String world, int x, int y, int z, Facing facing) {

    public Trebuchet {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(facing, "facing");
    }
}

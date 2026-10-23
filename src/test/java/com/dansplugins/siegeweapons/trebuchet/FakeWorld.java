package com.dansplugins.siegeweapons.trebuchet;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** An in-memory block grid for tests; every unset position is air. */
final class FakeWorld implements TrebuchetPattern.BlockLookup {

    private final Map<List<Integer>, Material> blocks = new HashMap<>();

    void set(int x, int y, int z, Material material) {
        blocks.put(List.of(x, y, z), material);
    }

    @Override
    public Material typeAt(int x, int y, int z) {
        return blocks.getOrDefault(List.of(x, y, z), Material.AIR);
    }

    /** Builds every part of the trebuchet pattern around the anchor, facing the given way. */
    void buildTrebuchet(int anchorX, int anchorY, int anchorZ, Facing facing) {
        for (TrebuchetPattern.Part part : TrebuchetPattern.PARTS) {
            set(worldX(anchorX, part, facing), anchorY + part.up(), worldZ(anchorZ, part, facing), part.material());
        }
    }

    static int worldX(int anchorX, TrebuchetPattern.Part part, Facing facing) {
        return anchorX + part.right() * facing.rightX() + part.forward() * facing.forwardX();
    }

    static int worldZ(int anchorZ, TrebuchetPattern.Part part, Facing facing) {
        return anchorZ + part.right() * facing.rightZ() + part.forward() * facing.forwardZ();
    }
}

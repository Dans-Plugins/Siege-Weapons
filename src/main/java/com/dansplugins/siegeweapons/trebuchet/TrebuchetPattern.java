package com.dansplugins.siegeweapons.trebuchet;

import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The block arrangement a player builds to make a trebuchet, and the check for whether a
 * newly placed block completed one. Kept free of the running server so it can be unit tested:
 * the world is read through {@link BlockLookup}.
 *
 * <p>Offsets are relative to the anchor (the centre of the base) in the engine's own frame:
 * {@code right} across the engine, {@code up}, and {@code forward} in the direction it fires.
 * Blocks not listed here are ignored, so decoration around the engine does not stop detection.
 */
public final class TrebuchetPattern {

    /** Reads the block type at a world position. */
    @FunctionalInterface
    public interface BlockLookup {
        Material typeAt(int x, int y, int z);
    }

    /** One required block of the pattern. */
    public record Part(int right, int up, int forward, Material material) {
    }

    /** Where a completed trebuchet was found. */
    public record Match(int x, int y, int z, Facing facing) {
    }

    public static final List<Part> PARTS = List.of(
            // Base: a 3x3 platform of oak planks.
            new Part(-1, 0, -1, Material.OAK_PLANKS),
            new Part(0, 0, -1, Material.OAK_PLANKS),
            new Part(1, 0, -1, Material.OAK_PLANKS),
            new Part(-1, 0, 0, Material.OAK_PLANKS),
            new Part(0, 0, 0, Material.OAK_PLANKS),
            new Part(1, 0, 0, Material.OAK_PLANKS),
            new Part(-1, 0, 1, Material.OAK_PLANKS),
            new Part(0, 0, 1, Material.OAK_PLANKS),
            new Part(1, 0, 1, Material.OAK_PLANKS),
            // Uprights either side of the centre.
            new Part(-1, 1, 0, Material.OAK_FENCE),
            new Part(1, 1, 0, Material.OAK_FENCE),
            // Axle across the top of the uprights.
            new Part(-1, 2, 0, Material.OAK_LOG),
            new Part(0, 2, 0, Material.OAK_LOG),
            new Part(1, 2, 0, Material.OAK_LOG),
            // Counterweight hanging behind the axle; it is what gives the engine a facing.
            new Part(0, 1, -1, Material.IRON_BLOCK)
    );

    private TrebuchetPattern() {
    }

    /**
     * Finds every complete trebuchet that the block at ({@code x}, {@code y}, {@code z}) is part
     * of, one per anchor. Usually there is at most one; a block shared by two engines built side
     * by side can complete both. If an anchor reads as complete in more than one facing (another
     * counterweight placed in front of the engine), the first in {@link Facing} order wins.
     */
    public static List<Match> findCompletedBy(BlockLookup world, int x, int y, int z) {
        Material placed = world.typeAt(x, y, z);
        Map<List<Integer>, Match> byAnchor = new LinkedHashMap<>();
        for (Facing facing : Facing.values()) {
            for (Part part : PARTS) {
                if (part.material() != placed) {
                    continue;
                }
                int anchorX = x - offsetX(part, facing);
                int anchorY = y - part.up();
                int anchorZ = z - offsetZ(part, facing);
                List<Integer> anchor = List.of(anchorX, anchorY, anchorZ);
                if (!byAnchor.containsKey(anchor) && isCompleteAt(world, anchorX, anchorY, anchorZ, facing)) {
                    byAnchor.put(anchor, new Match(anchorX, anchorY, anchorZ, facing));
                }
            }
        }
        return List.copyOf(byAnchor.values());
    }

    /** Whether every part of the pattern is present around the given anchor and facing. */
    public static boolean isCompleteAt(BlockLookup world, int anchorX, int anchorY, int anchorZ, Facing facing) {
        for (Part part : PARTS) {
            Material actual = world.typeAt(
                    anchorX + offsetX(part, facing),
                    anchorY + part.up(),
                    anchorZ + offsetZ(part, facing));
            if (actual != part.material()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether ({@code x}, {@code y}, {@code z}) is the position of one of the pattern's parts for
     * an engine anchored at the given position and facing.
     */
    public static boolean isPartAt(int anchorX, int anchorY, int anchorZ, Facing facing, int x, int y, int z) {
        for (Part part : PARTS) {
            if (anchorX + offsetX(part, facing) == x
                    && anchorY + part.up() == y
                    && anchorZ + offsetZ(part, facing) == z) {
                return true;
            }
        }
        return false;
    }

    private static int offsetX(Part part, Facing facing) {
        return part.right() * facing.rightX() + part.forward() * facing.forwardX();
    }

    private static int offsetZ(Part part, Facing facing) {
        return part.right() * facing.rightZ() + part.forward() * facing.forwardZ();
    }
}

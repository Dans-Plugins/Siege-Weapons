package com.dansplugins.siegeweapons.trebuchet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * Holds a shot's explosion to what the {@link WarGate} allows. The gate is asked twice: once for
 * the point of impact, which decides whether the explosion breaks blocks at all, and again for
 * each block the explosion is about to break, because a blast landing beside a chunk border
 * reaches into the chunk next door and that chunk has to pass in its own right.
 *
 * <p>The gate's answer is the same for every block of one chunk, so it is asked once per chunk a
 * blast reaches and the answer is reused for the rest of that blast.
 *
 * <p>A block that would set off an explosion of its own, such as TNT, is never broken by a shot,
 * even in enemy land: its explosion would come later, unattributed, and could reach terrain the
 * gate refuses.
 *
 * <p>The server reports an explosion's blocks in an event raised while the explosion runs, so the
 * shot is remembered for the length of {@link #detonate} and {@link #keepAllowed} applies to it.
 */
public final class ShotBlast {

    private record Detonation(UUID firer, String world, Map<Long, Boolean> allowedByChunk) {
    }

    private static final int BLOCKS_PER_CHUNK_SHIFT = 4;

    private final WarGate warGate;
    private Detonation detonating;

    public ShotBlast(WarGate warGate) {
        this.warGate = warGate;
    }

    /**
     * Sets off a shot that landed at ({@code landedX}, {@code landedZ}).
     *
     * @param firer     the player who fired, or {@code null} if that is no longer known
     * @param explosion creates the explosion, breaking blocks only if it is given {@code true}
     */
    public void detonate(UUID firer, String world, int landedX, int landedZ, Consumer<Boolean> explosion) {
        Detonation shot = new Detonation(firer, world, new HashMap<>());
        boolean breaksBlocks = isAllowed(shot, landedX, landedZ);
        detonating = shot;
        try {
            explosion.accept(breaksBlocks);
        } finally {
            detonating = null;
        }
    }

    /**
     * Removes from {@code blocks} every block the gate refuses, and every block that would set off
     * an explosion of its own, if a shot is detonating. Any other explosion is left alone: it is
     * not this plugin's to judge.
     *
     * @param setsOffExplosion whether breaking the block would set off a further explosion
     */
    public <B> void keepAllowed(List<B> blocks, ToIntFunction<B> blockX, ToIntFunction<B> blockZ,
                                Predicate<B> setsOffExplosion) {
        Detonation shot = detonating;
        if (shot == null) {
            return;
        }
        blocks.removeIf(block -> setsOffExplosion.test(block)
                || !isAllowed(shot, blockX.applyAsInt(block), blockZ.applyAsInt(block)));
    }

    private boolean isAllowed(Detonation shot, int blockX, int blockZ) {
        int chunkX = blockX >> BLOCKS_PER_CHUNK_SHIFT;
        int chunkZ = blockZ >> BLOCKS_PER_CHUNK_SHIFT;
        long chunk = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
        return shot.allowedByChunk().computeIfAbsent(chunk,
                key -> warGate.mayBreakBlockAt(shot.firer(), shot.world(), blockX, blockZ));
    }
}

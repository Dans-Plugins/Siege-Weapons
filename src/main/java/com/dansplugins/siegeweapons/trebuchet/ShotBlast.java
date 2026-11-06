package com.dansplugins.siegeweapons.trebuchet;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

/**
 * Holds a shot's explosion to what the {@link WarGate} allows. The gate is asked twice: once for
 * the point of impact, which decides whether the explosion breaks blocks at all, and again for
 * each block the explosion is about to break, because a blast landing beside a chunk border
 * reaches into the chunk next door and that chunk has to pass in its own right.
 *
 * <p>The server reports an explosion's blocks in an event raised while the explosion runs, so the
 * shot is remembered for the length of {@link #detonate} and {@link #keepAllowed} applies to it.
 */
public final class ShotBlast {

    private record Detonation(UUID firer, String world) {
    }

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
        boolean breaksBlocks = warGate.mayBreakBlockAt(firer, world, landedX, landedZ);
        detonating = new Detonation(firer, world);
        try {
            explosion.accept(breaksBlocks);
        } finally {
            detonating = null;
        }
    }

    /**
     * Removes from {@code blocks} every block the gate refuses, if a shot is detonating. Any other
     * explosion is left alone: it is not this plugin's to judge.
     */
    public <B> void keepAllowed(List<B> blocks, ToIntFunction<B> blockX, ToIntFunction<B> blockZ) {
        Detonation shot = detonating;
        if (shot == null) {
            return;
        }
        blocks.removeIf(block -> !warGate.mayBreakBlockAt(
                shot.firer(), shot.world(), blockX.applyAsInt(block), blockZ.applyAsInt(block)));
    }
}

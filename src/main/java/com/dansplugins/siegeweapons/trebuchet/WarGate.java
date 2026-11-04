package com.dansplugins.siegeweapons.trebuchet;

import java.util.Optional;
import java.util.UUID;

/**
 * The one place that decides whether a shot may break blocks, and the plugin's most important
 * rule (MVP.md): only in a chunk claimed by a faction at war with the firer's faction. Every other
 * case leaves terrain alone, and nothing in {@code config.yml} can change the answer.
 *
 * <p>The answer is for a single block position, so it holds for each block a blast reaches and
 * not only for the point of impact: a shot landing beside a chunk border must not reach into the
 * chunk next door unless that chunk passes too.
 */
public final class WarGate {

    /** What the gate needs to know about factions, which Medieval Factions will answer. */
    public interface Factions {

        /** The faction {@code player} belongs to, if any. */
        Optional<String> factionOf(UUID player);

        /** The faction that claims the chunk, if any. */
        Optional<String> claimOwner(String world, int chunkX, int chunkZ);

        boolean atWar(String faction, String otherFaction);
    }

    private static final int BLOCKS_PER_CHUNK_SHIFT = 4;

    private static final Factions NO_FACTIONS = new Factions() {
        @Override
        public Optional<String> factionOf(UUID player) {
            return Optional.empty();
        }

        @Override
        public Optional<String> claimOwner(String world, int chunkX, int chunkZ) {
            return Optional.empty();
        }

        @Override
        public boolean atWar(String faction, String otherFaction) {
            return false;
        }
    };

    private final Factions factions;

    public WarGate(Factions factions) {
        this.factions = factions;
    }

    /** A gate that knows of no factions, so it never lets a shot break a block. */
    public static WarGate closed() {
        return new WarGate(NO_FACTIONS);
    }

    /**
     * Whether a shot may break the block at ({@code blockX}, {@code blockZ}).
     *
     * @param firer the player who fired, or {@code null} if that is no longer known; a shot with
     *              no known firer breaks nothing
     */
    public boolean mayBreakBlockAt(UUID firer, String world, int blockX, int blockZ) {
        if (firer == null) {
            return false;
        }
        Optional<String> firerFaction = factions.factionOf(firer);
        if (firerFaction.isEmpty()) {
            return false;
        }
        Optional<String> claimOwner = factions.claimOwner(
                world, blockX >> BLOCKS_PER_CHUNK_SHIFT, blockZ >> BLOCKS_PER_CHUNK_SHIFT);
        if (claimOwner.isEmpty() || claimOwner.equals(firerFaction)) {
            return false;
        }
        return factions.atWar(firerFaction.get(), claimOwner.get());
    }
}

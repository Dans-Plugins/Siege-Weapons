package com.dansplugins.siegeweapons.trebuchet;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShotBlastTest {

    private static final String WORLD = "world";
    private static final String ATTACKERS = "attackers";
    private static final String ENEMIES = "enemies";
    private static final String NEUTRALS = "neutrals";

    /** Chunk (0, 0) is enemy land, chunk (1, 0) is neutral land and chunk (2, 0) is unclaimed. */
    private static final BlockAt IN_ENEMY_LAND = new BlockAt(15, 5);
    private static final BlockAt IN_NEUTRAL_LAND = new BlockAt(16, 5);
    private static final BlockAt IN_UNCLAIMED_LAND = new BlockAt(32, 5);

    private static final BlockAt TNT_IN_ENEMY_LAND = new BlockAt(14, 5, true);
    private static final BlockAt ANOTHER_IN_ENEMY_LAND = new BlockAt(3, 9);

    private record BlockAt(int x, int z, boolean tnt) {
        BlockAt(int x, int z) {
            this(x, z, false);
        }
    }

    private final UUID firer = UUID.randomUUID();
    private final Map<UUID, String> members = new HashMap<>(Map.of(firer, ATTACKERS));
    private final Map<Integer, String> claimsByChunkX = new HashMap<>(Map.of(0, ENEMIES, 1, NEUTRALS));
    private boolean warLookupFailsForNeutrals;
    private int factionLookups;
    private int claimLookups;

    private final ShotBlast blast = new ShotBlast(new WarGate(new WarGate.Factions() {
        @Override
        public Optional<String> factionOf(UUID player) {
            factionLookups++;
            return Optional.ofNullable(members.get(player));
        }

        @Override
        public Optional<String> claimOwner(String world, int chunkX, int chunkZ) {
            claimLookups++;
            return WORLD.equals(world) ? Optional.ofNullable(claimsByChunkX.get(chunkX)) : Optional.empty();
        }

        @Override
        public boolean atWar(String faction, String otherFaction) {
            if (warLookupFailsForNeutrals && NEUTRALS.equals(otherFaction)) {
                throw new IllegalStateException("war lookup failed");
            }
            return ATTACKERS.equals(faction) && ENEMIES.equals(otherFaction);
        }
    }, message -> { }));

    private List<BlockAt> blocksLeftAfterAShot(UUID shooter, String world, BlockAt landed, BlockAt... reached) {
        List<BlockAt> blocks = new ArrayList<>(List.of(reached));
        blast.detonate(shooter, world, landed.x(), landed.z(),
                breaksBlocks -> blast.keepAllowed(blocks, BlockAt::x, BlockAt::z, BlockAt::tnt));
        return blocks;
    }

    private boolean breaksBlocksOnLanding(UUID shooter, BlockAt landed) {
        List<Boolean> answers = new ArrayList<>();
        blast.detonate(shooter, WORLD, landed.x(), landed.z(), answers::add);
        assertEquals(1, answers.size(), "the explosion is created once");
        return answers.get(0);
    }

    @Test
    void aShotLandingInEnemyLandBreaksBlocks() {
        assertTrue(breaksBlocksOnLanding(firer, IN_ENEMY_LAND));
    }

    @Test
    void aShotLandingOutsideEnemyLandBreaksNoBlocks() {
        assertFalse(breaksBlocksOnLanding(firer, IN_NEUTRAL_LAND));
        assertFalse(breaksBlocksOnLanding(firer, IN_UNCLAIMED_LAND));
    }

    @Test
    void aBlastInEnemyLandKeepsOnlyTheBlocksInEnemyLand() {
        assertEquals(List.of(IN_ENEMY_LAND), blocksLeftAfterAShot(firer, WORLD, IN_ENEMY_LAND,
                IN_NEUTRAL_LAND, IN_ENEMY_LAND, IN_UNCLAIMED_LAND));
    }

    /** Its explosion would come later, unattributed, and could reach land the gate refuses. */
    @Test
    void aBlastInEnemyLandLeavesTntIntact() {
        assertEquals(List.of(IN_ENEMY_LAND), blocksLeftAfterAShot(firer, WORLD, IN_ENEMY_LAND,
                TNT_IN_ENEMY_LAND, IN_ENEMY_LAND));
    }

    /** Medieval Factions scans every faction to find a player's, so a blast must not ask per block. */
    @Test
    void factionsAreAskedOncePerChunkABlastReaches() {
        blocksLeftAfterAShot(firer, WORLD, IN_ENEMY_LAND,
                IN_ENEMY_LAND, ANOTHER_IN_ENEMY_LAND, IN_NEUTRAL_LAND, IN_NEUTRAL_LAND, IN_UNCLAIMED_LAND);

        assertEquals(3, factionLookups, "once for each of the three chunks");
        assertEquals(3, claimLookups, "once for each of the three chunks");
    }

    @Test
    void eachShotAsksAfresh() {
        blocksLeftAfterAShot(firer, WORLD, IN_ENEMY_LAND, IN_ENEMY_LAND);
        claimsByChunkX.remove(0);

        assertEquals(List.of(), blocksLeftAfterAShot(firer, WORLD, IN_ENEMY_LAND, IN_ENEMY_LAND));
    }

    /** If this threw, the event handler would stop and the rest of the list would be broken unjudged. */
    @Test
    void aLookupFailingPartWayThroughABlastLeavesTheBlockItWasAskedAboutIntact() {
        warLookupFailsForNeutrals = true;

        assertEquals(List.of(IN_ENEMY_LAND), blocksLeftAfterAShot(firer, WORLD, IN_ENEMY_LAND,
                IN_NEUTRAL_LAND, IN_ENEMY_LAND));
    }

    @Test
    void aFirerWithNoFactionLeavesEveryBlockIntact() {
        members.remove(firer);

        assertEquals(List.of(), blocksLeftAfterAShot(firer, WORLD, IN_ENEMY_LAND, IN_ENEMY_LAND));
    }

    @Test
    void aShotWithNoKnownFirerLeavesEveryBlockIntact() {
        assertEquals(List.of(), blocksLeftAfterAShot(null, WORLD, IN_ENEMY_LAND, IN_ENEMY_LAND));
    }

    @Test
    void blocksAreJudgedInTheWorldTheShotLandedIn() {
        assertEquals(List.of(), blocksLeftAfterAShot(firer, "world_nether", IN_ENEMY_LAND, IN_ENEMY_LAND));
    }

    @Test
    void anExplosionThatIsNotAShotIsLeftAlone() {
        List<BlockAt> blocks = new ArrayList<>(List.of(IN_NEUTRAL_LAND, IN_UNCLAIMED_LAND));

        blast.keepAllowed(blocks, BlockAt::x, BlockAt::z, BlockAt::tnt);

        assertEquals(List.of(IN_NEUTRAL_LAND, IN_UNCLAIMED_LAND), blocks);
    }

    @Test
    void anExplosionAfterAShotIsLeftAlone() {
        blocksLeftAfterAShot(firer, WORLD, IN_ENEMY_LAND);
        List<BlockAt> blocks = new ArrayList<>(List.of(IN_NEUTRAL_LAND));

        blast.keepAllowed(blocks, BlockAt::x, BlockAt::z, BlockAt::tnt);

        assertEquals(List.of(IN_NEUTRAL_LAND), blocks);
    }

    @Test
    void anExplosionAfterAShotThatFailedIsLeftAlone() {
        Consumer<Boolean> failing = breaksBlocks -> {
            throw new IllegalStateException("no explosion");
        };
        assertThrows(IllegalStateException.class,
                () -> blast.detonate(firer, WORLD, IN_ENEMY_LAND.x(), IN_ENEMY_LAND.z(), failing));
        List<BlockAt> blocks = new ArrayList<>(List.of(IN_NEUTRAL_LAND));

        blast.keepAllowed(blocks, BlockAt::x, BlockAt::z, BlockAt::tnt);

        assertEquals(List.of(IN_NEUTRAL_LAND), blocks);
    }

    @Test
    void aClosedGateLeavesEveryBlockIntact() {
        ShotBlast closed = new ShotBlast(WarGate.closed());
        List<BlockAt> blocks = new ArrayList<>(List.of(IN_ENEMY_LAND));

        closed.detonate(firer, WORLD, IN_ENEMY_LAND.x(), IN_ENEMY_LAND.z(), breaksBlocks -> {
            assertFalse(breaksBlocks);
            closed.keepAllowed(blocks, BlockAt::x, BlockAt::z, BlockAt::tnt);
        });

        assertEquals(List.of(), blocks);
    }

    /**
     * The destroy listener reads the block list at MONITOR, so a part the filter saved must be gone
     * from the list by then, or the engine would be destroyed by a block that was never broken.
     * Nothing later than HIGHEST may still change the list, which leaves the filter the last word.
     */
    @Test
    void theListenerFiltersEveryKindOfExplosionAtTheLastPriorityThatMayChangeIt() throws NoSuchMethodException {
        assertEquals(EventPriority.HIGHEST, TrebuchetFireListener.class
                .getMethod("onEntityExplode", EntityExplodeEvent.class)
                .getAnnotation(EventHandler.class).priority());
        assertEquals(EventPriority.HIGHEST, TrebuchetFireListener.class
                .getMethod("onBlockExplode", BlockExplodeEvent.class)
                .getAnnotation(EventHandler.class).priority());
    }
}

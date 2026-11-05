package com.dansplugins.siegeweapons.trebuchet;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrebuchetDestroyerTest {

    @TempDir
    Path dir;

    private final List<String> warnings = new ArrayList<>();
    private final TrebuchetRegistry registry = new TrebuchetRegistry();

    private TrebuchetStorage storage() {
        return new TrebuchetStorage(dir.resolve("trebuchets.yml").toFile(), warnings::add);
    }

    private List<Trebuchet> remove(String world, int x, int y, int z) {
        return new TrebuchetDestroyer(registry, storage(), warnings::add).blockRemoved(world, x, y, z);
    }

    private Trebuchet built(String world, int x, int y, int z, Facing facing) throws Exception {
        Trebuchet trebuchet = new Trebuchet(UUID.randomUUID(), world, x, y, z, facing);
        registry.register(trebuchet);
        storage().save(registry.all());
        return trebuchet;
    }

    @Test
    void losingAnyPartDestroysTheEngineAndSavesTheRegistry() throws Exception {
        for (TrebuchetPattern.Part part : TrebuchetPattern.PARTS) {
            Trebuchet trebuchet = built("world", 10, 64, -20, Facing.WEST);

            List<Trebuchet> destroyed = remove("world",
                    FakeWorld.worldX(10, part, Facing.WEST), 64 + part.up(), FakeWorld.worldZ(-20, part, Facing.WEST));

            assertEquals(List.of(trebuchet), destroyed, "removed " + part);
            assertTrue(registry.all().isEmpty(), "removed " + part);
            assertTrue(storage().load().isEmpty(), "removed " + part);
        }
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void aBlockOutsideThePatternLeavesTheEngineStanding() throws Exception {
        Trebuchet trebuchet = built("world", 0, 64, 0, Facing.NORTH);

        // In front of the axle, an empty corner of layer 2, and below the anchor.
        assertTrue(remove("world", 0, 65, -1).isEmpty());
        assertTrue(remove("world", -1, 65, -1).isEmpty());
        assertTrue(remove("world", 0, 63, 0).isEmpty());

        assertEquals(List.of(trebuchet), List.copyOf(registry.all()));
        assertEquals(List.of(trebuchet), storage().load());
    }

    @Test
    void theSamePositionInAnotherWorldLeavesTheEngineStanding() throws Exception {
        Trebuchet trebuchet = built("world", 0, 64, 0, Facing.NORTH);

        assertTrue(remove("world_nether", 0, 64, 0).isEmpty());

        assertEquals(List.of(trebuchet), List.copyOf(registry.all()));
    }

    @Test
    void onlyTheEngineThatOwnsTheBlockIsDestroyed() throws Exception {
        Trebuchet first = built("world", 0, 64, 0, Facing.NORTH);
        Trebuchet second = built("world", 20, 64, 0, Facing.SOUTH);

        assertEquals(List.of(second), remove("world", 20, 66, 0));

        assertEquals(List.of(first), List.copyOf(registry.all()));
        assertEquals(List.of(first), storage().load());
    }

    @Test
    void aBlockSharedByTwoEnginesDestroysBoth() throws Exception {
        // Side by side, facing north: the right edge of the first base is the left edge of the second.
        Trebuchet first = built("world", 0, 64, 0, Facing.NORTH);
        Trebuchet second = built("world", 2, 64, 0, Facing.NORTH);

        assertEquals(List.of(first, second), remove("world", 1, 64, 0));

        assertTrue(registry.all().isEmpty());
    }

    private Optional<Trebuchet> useLoadBlock(FakeWorld blocks, String world, int x, int y, int z) {
        return new TrebuchetDestroyer(registry, storage(), warnings::add).loadBlockUsed(world, blocks, x, y, z);
    }

    @Test
    void usingTheLoadBlockOfAWholeEngineLeavesItStanding() throws Exception {
        FakeWorld blocks = new FakeWorld();
        blocks.buildTrebuchet(0, 64, 0, Facing.EAST);
        Trebuchet trebuchet = built("world", 0, 64, 0, Facing.EAST);

        assertEquals(Optional.empty(), useLoadBlock(blocks, "world", 0, 66, 0));

        assertEquals(List.of(trebuchet), List.copyOf(registry.all()));
    }

    @Test
    void usingTheLoadBlockDestroysAnEngineMissingAnyPartAndSavesTheRegistry() throws Exception {
        for (TrebuchetPattern.Part part : TrebuchetPattern.PARTS) {
            FakeWorld blocks = new FakeWorld();
            blocks.buildTrebuchet(10, 64, -20, Facing.WEST);
            blocks.set(FakeWorld.worldX(10, part, Facing.WEST), 64 + part.up(),
                    FakeWorld.worldZ(-20, part, Facing.WEST), Material.AIR);
            Trebuchet trebuchet = built("world", 10, 64, -20, Facing.WEST);

            assertEquals(Optional.of(trebuchet), useLoadBlock(blocks, "world", 10, 66, -20), "lost " + part);

            assertTrue(registry.all().isEmpty(), "lost " + part);
            assertTrue(storage().load().isEmpty(), "lost " + part);
        }
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void aStrippedAxleCountsAsAMissingPart() throws Exception {
        FakeWorld blocks = new FakeWorld();
        blocks.buildTrebuchet(0, 64, 0, Facing.NORTH);
        blocks.set(1, 66, 0, Material.STRIPPED_OAK_LOG);
        Trebuchet trebuchet = built("world", 0, 64, 0, Facing.NORTH);

        assertEquals(Optional.of(trebuchet), useLoadBlock(blocks, "world", 0, 66, 0));
    }

    @Test
    void usingABlockThatIsNotALoadBlockChecksNothing() throws Exception {
        // The world holds none of the engine, so anything that did check it would destroy it.
        Trebuchet trebuchet = built("world", 0, 64, 0, Facing.NORTH);

        assertEquals(Optional.empty(), useLoadBlock(new FakeWorld(), "world", 1, 66, 0));
        assertEquals(Optional.empty(), useLoadBlock(new FakeWorld(), "world_nether", 0, 66, 0));

        assertEquals(List.of(trebuchet), List.copyOf(registry.all()));
    }

    @Test
    void onlyTheEngineWhoseLoadBlockWasUsedIsChecked() throws Exception {
        FakeWorld blocks = new FakeWorld();
        blocks.buildTrebuchet(0, 64, 0, Facing.NORTH);
        Trebuchet whole = built("world", 0, 64, 0, Facing.NORTH);
        Trebuchet broken = built("world", 20, 64, 0, Facing.SOUTH);

        assertEquals(Optional.empty(), useLoadBlock(blocks, "world", 0, 66, 0));
        assertEquals(Optional.of(broken), useLoadBlock(blocks, "world", 20, 66, 0));

        assertEquals(List.of(whole), storage().load());
    }
}

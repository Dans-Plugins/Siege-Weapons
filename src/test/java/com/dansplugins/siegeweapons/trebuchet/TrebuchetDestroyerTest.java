package com.dansplugins.siegeweapons.trebuchet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
}

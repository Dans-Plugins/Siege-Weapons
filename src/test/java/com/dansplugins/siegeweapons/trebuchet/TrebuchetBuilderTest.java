package com.dansplugins.siegeweapons.trebuchet;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrebuchetBuilderTest {

    @TempDir
    Path dir;

    private final FakeWorld blocks = new FakeWorld();
    private final List<String> warnings = new ArrayList<>();
    private final TrebuchetRegistry registry = new TrebuchetRegistry();

    private TrebuchetStorage storage() {
        return new TrebuchetStorage(dir.resolve("trebuchets.yml").toFile(), warnings::add);
    }

    private List<Trebuchet> place(int x, int y, int z) {
        return new TrebuchetBuilder(registry, storage(), warnings::add).blockPlaced("world", blocks, x, y, z);
    }

    @Test
    void completingATrebuchetRegistersAndSavesIt() {
        blocks.buildTrebuchet(0, 64, 0, Facing.EAST);

        List<Trebuchet> built = place(0, 65, -1);

        assertEquals(1, built.size());
        Trebuchet trebuchet = built.get(0);
        assertEquals(new Trebuchet(trebuchet.id(), "world", 0, 64, 0, Facing.EAST), trebuchet);
        assertEquals(trebuchet, registry.findByAnchor("world", 0, 64, 0).orElseThrow());
        assertEquals(List.of(trebuchet), storage().load());
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void placingMoreBlocksOnABuiltTrebuchetDoesNotRegisterItAgain() {
        blocks.buildTrebuchet(0, 64, 0, Facing.EAST);
        place(0, 66, 0);

        assertTrue(place(0, 64, 0).isEmpty());

        assertEquals(1, registry.all().size());
    }

    @Test
    void anIncompleteStructureRegistersAndSavesNothing() {
        blocks.buildTrebuchet(0, 64, 0, Facing.EAST);
        blocks.set(0, 66, 0, Material.AIR);

        assertTrue(place(0, 64, 0).isEmpty());

        assertTrue(registry.all().isEmpty());
        assertTrue(storage().load().isEmpty());
    }

    @Test
    void aSecondEngineIsSavedAlongsideTheFirst() {
        blocks.buildTrebuchet(0, 64, 0, Facing.NORTH);
        Trebuchet first = place(0, 66, 0).get(0);
        blocks.buildTrebuchet(20, 64, 0, Facing.SOUTH);

        Trebuchet second = place(20, 66, 0).get(0);

        assertEquals(List.of(first, second), storage().load());
    }
}

package com.dansplugins.siegeweapons.trebuchet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrebuchetStorageTest {

    @TempDir
    Path dir;

    private final List<String> warnings = new ArrayList<>();

    private TrebuchetStorage storage() {
        return new TrebuchetStorage(dir.resolve("trebuchets.yml").toFile(), warnings::add);
    }

    @Test
    void savedTrebuchetsLoadBackUnchanged() throws IOException {
        List<Trebuchet> saved = List.of(
                new Trebuchet(UUID.randomUUID(), "world", 10, 64, -20, Facing.EAST),
                new Trebuchet(UUID.randomUUID(), "world_nether", -3, 40, 7, Facing.WEST));

        storage().save(saved);

        assertEquals(saved, storage().load());
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void missingFileLoadsNothing() {
        assertTrue(storage().load().isEmpty());
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void unreadableEntriesAreSkippedWithAWarning() throws IOException {
        UUID good = UUID.randomUUID();
        File file = dir.resolve("trebuchets.yml").toFile();
        Files.writeString(file.toPath(), String.join("\n",
                "trebuchets:",
                "  " + good + ":",
                "    world: world",
                "    x: 1",
                "    y: 2",
                "    z: 3",
                "    facing: SOUTH",
                "  not-a-uuid:",
                "    world: world",
                "    x: 1",
                "    y: 2",
                "    z: 3",
                "    facing: SOUTH",
                "  " + UUID.randomUUID() + ":",
                "    world: world",
                "    x: 1",
                "    y: 2",
                "    z: 3",
                "    facing: UP",
                "  " + UUID.randomUUID() + ":",
                "    world: world",
                "    x: 1",
                "    z: 3",
                "    facing: NORTH",
                ""));

        List<Trebuchet> loaded = storage().load();

        assertEquals(List.of(new Trebuchet(good, "world", 1, 2, 3, Facing.SOUTH)), loaded);
        assertEquals(3, warnings.size(), warnings::toString);
    }

    @Test
    void savingAgainDropsEnginesNoLongerPassed() throws IOException {
        Trebuchet kept = new Trebuchet(UUID.randomUUID(), "world", 10, 64, -20, Facing.EAST);
        Trebuchet destroyed = new Trebuchet(UUID.randomUUID(), "world", 40, 70, 5, Facing.NORTH);
        storage().save(List.of(kept, destroyed));

        storage().save(List.of(kept));

        assertEquals(List.of(kept), storage().load());
    }

    @Test
    void savingNoEnginesLoadsNothing() throws IOException {
        storage().save(List.of(new Trebuchet(UUID.randomUUID(), "world", 1, 2, 3, Facing.SOUTH)));

        storage().save(List.of());

        assertTrue(storage().load().isEmpty());
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void aFileWithoutATrebuchetsSectionLoadsNothing() throws IOException {
        Files.writeString(dir.resolve("trebuchets.yml"), "something-else: 1\n");

        assertTrue(storage().load().isEmpty());
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void entriesMissingTheirWorldOrFacingOrNotASectionAreSkippedWithAWarningNamingThem() throws IOException {
        UUID noWorld = UUID.randomUUID();
        UUID noFacing = UUID.randomUUID();
        UUID notASection = UUID.randomUUID();
        Files.writeString(dir.resolve("trebuchets.yml"), String.join("\n",
                "trebuchets:",
                "  " + noWorld + ":",
                "    x: 1",
                "    y: 2",
                "    z: 3",
                "    facing: SOUTH",
                "  " + noFacing + ":",
                "    world: world",
                "    x: 1",
                "    y: 2",
                "    z: 3",
                "  " + notASection + ": 5",
                ""));

        List<Trebuchet> loaded = storage().load();

        assertTrue(loaded.isEmpty(), loaded::toString);
        assertEquals(3, warnings.size(), warnings::toString);
        for (UUID key : List.of(noWorld, noFacing, notASection)) {
            String prefix = "Skipping unreadable trebuchet '" + key + "' in trebuchets.yml: ";
            assertTrue(warnings.stream().anyMatch(warning -> warning.startsWith(prefix)), warnings::toString);
        }
    }
}

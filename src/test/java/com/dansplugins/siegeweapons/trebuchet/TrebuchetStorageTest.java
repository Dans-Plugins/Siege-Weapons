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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void aFileWithoutATrebuchetsSectionLoadsNothingAndIsNeverSavedOver() throws IOException {
        Path file = dir.resolve("trebuchets.yml");
        Files.writeString(file, "something-else: 1\n");
        TrebuchetStorage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertTrue(storage.isLoadFailed());
        assertEquals(1, warnings.size(), warnings::toString);
        assertThrows(IOException.class, () -> storage.save(List.of()));
        assertEquals("something-else: 1\n", Files.readString(file));
    }

    @Test
    void anEmptyFileLoadsNothingAndCanBeSaved() throws IOException {
        Files.writeString(dir.resolve("trebuchets.yml"), "");
        TrebuchetStorage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertFalse(storage.isLoadFailed());
        storage.save(List.of());
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void aFileThatCannotBeParsedLoadsNothingAndIsNeverSavedOver() throws IOException {
        // prepare - a good save, then the file cut off part way through an entry
        TrebuchetStorage storage = storage();
        storage.save(List.of(
                new Trebuchet(UUID.randomUUID(), "world", 10, 64, -20, Facing.EAST),
                new Trebuchet(UUID.randomUUID(), "world_nether", -3, 40, 7, Facing.WEST)));
        Path file = dir.resolve("trebuchets.yml");
        String full = Files.readString(file);
        Files.writeString(file, full.substring(0, full.indexOf("facing")) + "facing: [EAST\n  :");
        byte[] broken = Files.readAllBytes(file);
        TrebuchetStorage restarted = storage();

        // execute - load on the next start, then a build/destroy save or the save on shutdown
        List<Trebuchet> loaded = restarted.load();
        IOException refused = assertThrows(IOException.class, () -> restarted.save(List.of()));

        // verify
        assertTrue(loaded.isEmpty());
        assertTrue(restarted.isLoadFailed());
        assertTrue(refused.getMessage().contains("not saved over"), refused::getMessage);
        assertArrayEquals(broken, Files.readAllBytes(file));
    }

    @Test
    void aSuccessfulLoadClearsAnEarlierFailure() throws IOException {
        Path file = dir.resolve("trebuchets.yml");
        Files.writeString(file, "trebuchets: [\n");
        TrebuchetStorage storage = storage();
        storage.load();
        Files.delete(file);

        storage.load();

        assertFalse(storage.isLoadFailed());
        storage.save(List.of());
    }

    @Test
    void savingLeavesNoTemporaryFileBehind() throws IOException {
        storage().save(List.of(new Trebuchet(UUID.randomUUID(), "world", 1, 2, 3, Facing.SOUTH)));

        try (var files = Files.list(dir)) {
            assertTrue(files.noneMatch(f -> f.getFileName().toString().endsWith(".tmp")));
        }
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

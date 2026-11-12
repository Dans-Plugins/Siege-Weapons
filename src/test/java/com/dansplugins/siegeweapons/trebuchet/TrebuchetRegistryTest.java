package com.dansplugins.siegeweapons.trebuchet;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrebuchetRegistryTest {

    private static Trebuchet at(String world, int x, int y, int z) {
        return new Trebuchet(UUID.randomUUID(), world, x, y, z, Facing.NORTH);
    }

    @Test
    void registeredTrebuchetIsFoundByItsAnchor() {
        TrebuchetRegistry registry = new TrebuchetRegistry();
        Trebuchet trebuchet = at("world", 1, 2, 3);

        assertTrue(registry.register(trebuchet));

        assertEquals(trebuchet, registry.findByAnchor("world", 1, 2, 3).orElseThrow());
        assertTrue(registry.findByAnchor("world_nether", 1, 2, 3).isEmpty());
        assertTrue(registry.findByAnchor("world", 1, 2, 4).isEmpty());
    }

    @Test
    void secondTrebuchetAtTheSameAnchorIsRejected() {
        TrebuchetRegistry registry = new TrebuchetRegistry();
        Trebuchet first = at("world", 0, 64, 0);

        assertTrue(registry.register(first));
        assertFalse(registry.register(at("world", 0, 64, 0)));

        assertEquals(List.of(first), List.copyOf(registry.all()));
    }

    @Test
    void unregisteredTrebuchetIsNoLongerFound() {
        TrebuchetRegistry registry = new TrebuchetRegistry();
        Trebuchet kept = at("world", 9, 64, 9);
        Trebuchet removed = at("world", 0, 64, 0);
        registry.register(kept);
        registry.register(removed);

        assertTrue(registry.unregister(removed));

        assertTrue(registry.findByAnchor("world", 0, 64, 0).isEmpty());
        assertEquals(List.of(kept), List.copyOf(registry.all()));
        assertFalse(registry.unregister(removed));
    }

    @Test
    void unregisterLeavesADifferentEngineAtTheSameAnchor() {
        TrebuchetRegistry registry = new TrebuchetRegistry();
        Trebuchet registered = at("world", 0, 64, 0);
        registry.register(registered);

        assertFalse(registry.unregister(at("world", 0, 64, 0)));

        assertEquals(List.of(registered), List.copyOf(registry.all()));
    }

    @Test
    void replaceAllDiscardsPreviousContents() {
        TrebuchetRegistry registry = new TrebuchetRegistry();
        registry.register(at("world", 0, 64, 0));
        Trebuchet loaded = at("world", 9, 64, 9);

        registry.replaceAll(List.of(loaded));

        assertEquals(List.of(loaded), List.copyOf(registry.all()));
    }

    @Test
    void replaceAllKeepsTheFirstOfTwoEnginesAtOneAnchor() {
        TrebuchetRegistry registry = new TrebuchetRegistry();
        Trebuchet first = at("world", 0, 64, 0);
        Trebuchet second = at("world", 0, 64, 0);

        registry.replaceAll(List.of(first, second));

        assertEquals(List.of(first), List.copyOf(registry.all()));
        assertEquals(first, registry.findByAnchor("world", 0, 64, 0).orElseThrow());
    }

    @Test
    void theSameAnchorInAnotherWorldIsADifferentEngine() {
        TrebuchetRegistry registry = new TrebuchetRegistry();
        Trebuchet overworld = at("world", 0, 64, 0);
        Trebuchet nether = at("world_nether", 0, 64, 0);

        assertTrue(registry.register(overworld));
        assertTrue(registry.register(nether));

        assertEquals(overworld, registry.findByAnchor("world", 0, 64, 0).orElseThrow());
        assertEquals(nether, registry.findByAnchor("world_nether", 0, 64, 0).orElseThrow());
    }

    @Test
    void allListsEnginesInTheOrderTheyWereRegistered() {
        TrebuchetRegistry registry = new TrebuchetRegistry();
        Trebuchet third = at("world", 30, 64, 30);
        Trebuchet first = at("world", -10, 64, -10);
        Trebuchet second = at("world", 0, 64, 0);
        registry.register(third);
        registry.register(first);
        registry.register(second);

        assertEquals(List.of(third, first, second), List.copyOf(registry.all()));
    }

    @Test
    void allIsAnUnmodifiableSnapshot() {
        TrebuchetRegistry registry = new TrebuchetRegistry();
        Trebuchet before = at("world", 0, 64, 0);
        registry.register(before);

        Collection<Trebuchet> snapshot = registry.all();
        registry.register(at("world", 9, 64, 9));

        assertEquals(List.of(before), List.copyOf(snapshot));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(at("world", 5, 64, 5)));
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertEquals(2, registry.all().size());
    }

    @Test
    void nullIsRejected() {
        TrebuchetRegistry registry = new TrebuchetRegistry();

        assertThrows(NullPointerException.class, () -> registry.register(null));
        assertThrows(NullPointerException.class, () -> registry.unregister(null));
    }
}

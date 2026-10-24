package com.dansplugins.siegeweapons.trebuchet;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}

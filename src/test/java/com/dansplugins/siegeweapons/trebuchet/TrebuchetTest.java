package com.dansplugins.siegeweapons.trebuchet;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrebuchetTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void anEngineNeedsAnIdAWorldAndAFacing() {
        assertEquals("id", assertThrows(NullPointerException.class,
                () -> new Trebuchet(null, "world", 0, 64, 0, Facing.NORTH)).getMessage());
        assertEquals("world", assertThrows(NullPointerException.class,
                () -> new Trebuchet(ID, null, 0, 64, 0, Facing.NORTH)).getMessage());
        assertEquals("facing", assertThrows(NullPointerException.class,
                () -> new Trebuchet(ID, "world", 0, 64, 0, null)).getMessage());
    }

    @Test
    void enginesAreEqualOnlyWhenEveryComponentIsEqual() {
        Trebuchet engine = new Trebuchet(ID, "world", 1, 64, -3, Facing.EAST);

        assertEquals(engine, new Trebuchet(ID, "world", 1, 64, -3, Facing.EAST));
        assertEquals(engine.hashCode(), new Trebuchet(ID, "world", 1, 64, -3, Facing.EAST).hashCode());
        assertNotEquals(engine, new Trebuchet(UUID.randomUUID(), "world", 1, 64, -3, Facing.EAST));
        assertNotEquals(engine, new Trebuchet(ID, "world_nether", 1, 64, -3, Facing.EAST));
        assertNotEquals(engine, new Trebuchet(ID, "world", 1, 65, -3, Facing.EAST));
        assertNotEquals(engine, new Trebuchet(ID, "world", 1, 64, -3, Facing.WEST));
    }
}

package com.dansplugins.siegeweapons.trebuchet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FacingTest {

    @Test
    void forwardMatchesMinecraftAxes() {
        assertEquals(-1, Facing.NORTH.forwardZ());
        assertEquals(1, Facing.EAST.forwardX());
        assertEquals(1, Facing.SOUTH.forwardZ());
        assertEquals(-1, Facing.WEST.forwardX());
    }

    @Test
    void rightIsTheNextFacingClockwise() {
        Facing[] clockwise = Facing.values();
        for (int i = 0; i < clockwise.length; i++) {
            Facing facing = clockwise[i];
            Facing next = clockwise[(i + 1) % clockwise.length];
            assertEquals(next.forwardX(), facing.rightX(), facing + " right x");
            assertEquals(next.forwardZ(), facing.rightZ(), facing + " right z");
        }
    }
}

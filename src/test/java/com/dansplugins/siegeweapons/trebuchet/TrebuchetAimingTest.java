package com.dansplugins.siegeweapons.trebuchet;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrebuchetAimingTest {

    private final TrebuchetRegistry registry = new TrebuchetRegistry();
    private final TrebuchetAiming aiming = new TrebuchetAiming(registry, 20, 45, 10);

    private Trebuchet built(String world, int x, int y, int z, Facing facing) {
        Trebuchet trebuchet = new Trebuchet(UUID.randomUUID(), world, x, y, z, facing);
        registry.register(trebuchet);
        return trebuchet;
    }

    private int adjust(Trebuchet trebuchet) {
        TrebuchetAiming.Aim aim = aiming.adjust(trebuchet.world(),
                trebuchet.x(), trebuchet.y() + TrebuchetFiring.LOAD_BLOCK_UP, trebuchet.z()).orElseThrow();
        assertEquals(trebuchet, aim.trebuchet());
        assertEquals(aim.range(), aiming.rangeOf(trebuchet));
        return aim.range();
    }

    @Test
    void aNewEngineIsAimedAtTheShortestRange() {
        assertEquals(20, aiming.rangeOf(built("world", 0, 64, 0, Facing.NORTH)));
    }

    @Test
    void eachAdjustmentLengthensTheRangeAndWrapsPastTheLongest() {
        Trebuchet trebuchet = built("world", 10, 64, -20, Facing.EAST);
        List<Integer> ranges = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ranges.add(adjust(trebuchet));
        }

        // The last step is cut short at the longest range rather than overshooting it.
        assertEquals(List.of(30, 40, 45, 20, 30), ranges);
    }

    @Test
    void enginesAreAimedIndependently() {
        Trebuchet first = built("world", 0, 64, 0, Facing.NORTH);
        Trebuchet second = built("world", 100, 64, 0, Facing.SOUTH);

        adjust(first);
        adjust(first);

        assertEquals(40, aiming.rangeOf(first));
        assertEquals(20, aiming.rangeOf(second));
    }

    @Test
    void onlyTheLoadBlockOfARegisteredEngineAdjustsIt() {
        Trebuchet trebuchet = built("world", 0, 64, 0, Facing.NORTH);

        assertTrue(aiming.adjust("world", 0, 64, 0).isEmpty(), "anchor");
        assertTrue(aiming.adjust("world", 0, 67, 0).isEmpty(), "above the load block");
        assertTrue(aiming.adjust("world", 1, 66, 0).isEmpty(), "side of the axle");
        assertTrue(aiming.adjust("other", 0, 66, 0).isEmpty(), "other world");
        assertEquals(20, aiming.rangeOf(trebuchet));
    }

    @Test
    void aDestroyedAndRebuiltEngineStartsAtTheShortestRange() {
        Trebuchet trebuchet = built("world", 0, 64, 0, Facing.NORTH);
        adjust(trebuchet);
        registry.unregister(trebuchet);

        assertTrue(aiming.adjust("world", 0, 66, 0).isEmpty());
        assertEquals(20, aiming.rangeOf(built("world", 0, 64, 0, Facing.NORTH)));
    }

    @Test
    void aSingleRangeStaysPut() {
        TrebuchetAiming fixed = new TrebuchetAiming(registry, 50, 50, 10);
        Trebuchet trebuchet = built("world", 0, 64, 0, Facing.NORTH);

        assertEquals(50, fixed.adjust("world", 0, 66, 0).orElseThrow().range());
        assertEquals(50, fixed.rangeOf(trebuchet));
    }

    @Test
    void rejectsAnInvalidRange() {
        assertThrows(IllegalArgumentException.class, () -> new TrebuchetAiming(registry, 0, 10, 1));
        assertThrows(IllegalArgumentException.class, () -> new TrebuchetAiming(registry, 30, 20, 1));
        assertThrows(IllegalArgumentException.class, () -> new TrebuchetAiming(registry, 10, 20, 0));
    }
}

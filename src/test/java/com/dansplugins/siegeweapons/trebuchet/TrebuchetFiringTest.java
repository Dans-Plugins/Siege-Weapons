package com.dansplugins.siegeweapons.trebuchet;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrebuchetFiringTest {

    private final TrebuchetRegistry registry = new TrebuchetRegistry();
    private final TrebuchetFiring firing = new TrebuchetFiring(registry);

    private Trebuchet built(String world, int x, int y, int z, Facing facing) {
        Trebuchet trebuchet = new Trebuchet(UUID.randomUUID(), world, x, y, z, facing);
        registry.register(trebuchet);
        return trebuchet;
    }

    private TrebuchetFiring.Result use(Trebuchet trebuchet, boolean holdingAmmunition) {
        TrebuchetFiring.Interaction interaction = firing.use(trebuchet.world(),
                trebuchet.x(), trebuchet.y() + TrebuchetFiring.LOAD_BLOCK_UP, trebuchet.z(), holdingAmmunition)
                .orElseThrow();
        assertEquals(trebuchet, interaction.trebuchet());
        return interaction.result();
    }

    @Test
    void theLoadBlockIsTheCentreOfTheAxleForEveryFacing() {
        for (Facing facing : Facing.values()) {
            FakeWorld world = new FakeWorld();
            world.buildTrebuchet(0, 64, 0, facing);
            built("world_" + facing, 0, 64, 0, facing);

            assertEquals(Material.OAK_LOG, world.typeAt(0, 64 + TrebuchetFiring.LOAD_BLOCK_UP, 0));
            assertTrue(firing.use("world_" + facing, 0, 66, 0, false).isPresent(), facing.toString());
        }
    }

    @Test
    void loadingThenUsingAgainFires() {
        Trebuchet trebuchet = built("world", 10, 64, -20, Facing.EAST);

        assertEquals(TrebuchetFiring.Result.LOADED, use(trebuchet, true));
        assertTrue(firing.isLoaded(trebuchet));

        assertEquals(TrebuchetFiring.Result.FIRED, use(trebuchet, false));
        assertFalse(firing.isLoaded(trebuchet));
    }

    @Test
    void aLoadedEngineFiresEvenWhenThePlayerHoldsMoreAmmunition() {
        Trebuchet trebuchet = built("world", 0, 64, 0, Facing.NORTH);
        use(trebuchet, true);

        assertEquals(TrebuchetFiring.Result.FIRED, use(trebuchet, true));
        assertFalse(firing.isLoaded(trebuchet));
    }

    @Test
    void anUnloadedEngineWithoutAmmunitionInHandDoesNothing() {
        Trebuchet trebuchet = built("world", 0, 64, 0, Facing.NORTH);

        assertEquals(TrebuchetFiring.Result.NOT_LOADED, use(trebuchet, false));
        assertFalse(firing.isLoaded(trebuchet));
    }

    @Test
    void otherBlocksAreNotTheLoadBlock() {
        built("world", 0, 64, 0, Facing.NORTH);

        // The anchor, the ends of the axle, above the axle, and the axle centre in another world.
        assertEquals(Optional.empty(), firing.use("world", 0, 64, 0, true));
        assertEquals(Optional.empty(), firing.use("world", -1, 66, 0, true));
        assertEquals(Optional.empty(), firing.use("world", 1, 66, 0, true));
        assertEquals(Optional.empty(), firing.use("world", 0, 67, 0, true));
        assertEquals(Optional.empty(), firing.use("world_nether", 0, 66, 0, true));
    }

    @Test
    void eachEngineHoldsItsOwnLoad() {
        Trebuchet first = built("world", 0, 64, 0, Facing.NORTH);
        Trebuchet second = built("world", 20, 64, 0, Facing.SOUTH);

        use(first, true);

        assertTrue(firing.isLoaded(first));
        assertFalse(firing.isLoaded(second));
        assertEquals(TrebuchetFiring.Result.NOT_LOADED, use(second, false));
    }

    @Test
    void aDestroyedEngineCanNoLongerBeUsed() {
        Trebuchet trebuchet = built("world", 0, 64, 0, Facing.NORTH);
        use(trebuchet, true);

        registry.unregister(trebuchet);

        assertEquals(Optional.empty(), firing.use("world", 0, 66, 0, false));
    }
}

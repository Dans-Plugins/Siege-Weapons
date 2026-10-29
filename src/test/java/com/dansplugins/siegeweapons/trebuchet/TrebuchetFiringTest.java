package com.dansplugins.siegeweapons.trebuchet;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrebuchetFiringTest {

    private static final Duration COOLDOWN = Duration.ofSeconds(10);

    private final TrebuchetRegistry registry = new TrebuchetRegistry();
    // Starts far from zero so that nothing depends on the clock's origin, as with System.nanoTime().
    private long now = -5_000_000_000L;
    private final TrebuchetFiring firing = new TrebuchetFiring(registry, COOLDOWN, () -> now);

    private Trebuchet built(String world, int x, int y, int z, Facing facing) {
        Trebuchet trebuchet = new Trebuchet(UUID.randomUUID(), world, x, y, z, facing);
        registry.register(trebuchet);
        return trebuchet;
    }

    private TrebuchetFiring.Result use(Trebuchet trebuchet, boolean holdingAmmunition) {
        return interact(trebuchet, holdingAmmunition).result();
    }

    private TrebuchetFiring.Interaction interact(Trebuchet trebuchet, boolean holdingAmmunition) {
        TrebuchetFiring.Interaction interaction = firing.use(trebuchet.world(),
                trebuchet.x(), trebuchet.y() + TrebuchetFiring.LOAD_BLOCK_UP, trebuchet.z(), holdingAmmunition)
                .orElseThrow();
        assertEquals(trebuchet, interaction.trebuchet());
        return interaction;
    }

    private void advance(Duration duration) {
        now += duration.toNanos();
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

    @Test
    void anEngineCannotFireAgainUntilItsCooldownHasPassed() {
        Trebuchet trebuchet = built("world", 0, 64, 0, Facing.NORTH);
        use(trebuchet, true);
        assertEquals(TrebuchetFiring.Result.FIRED, use(trebuchet, false));

        assertEquals(TrebuchetFiring.Result.LOADED, use(trebuchet, true));
        advance(Duration.ofMillis(3_500));
        TrebuchetFiring.Interaction early = interact(trebuchet, false);

        assertEquals(TrebuchetFiring.Result.COOLING_DOWN, early.result());
        assertEquals(Duration.ofMillis(6_500), early.cooldownRemaining());
        assertTrue(firing.isLoaded(trebuchet), "a cooling-down engine keeps its shot");

        advance(Duration.ofMillis(6_499));
        assertEquals(TrebuchetFiring.Result.COOLING_DOWN, use(trebuchet, false));

        advance(Duration.ofMillis(1));
        assertEquals(TrebuchetFiring.Result.FIRED, use(trebuchet, false));
        assertFalse(firing.isLoaded(trebuchet));
    }

    @Test
    void theCooldownRestartsWithEachShot() {
        Trebuchet trebuchet = built("world", 0, 64, 0, Facing.NORTH);
        use(trebuchet, true);
        use(trebuchet, false);
        advance(COOLDOWN);
        use(trebuchet, true);
        assertEquals(TrebuchetFiring.Result.FIRED, use(trebuchet, false));

        use(trebuchet, true);
        advance(Duration.ofSeconds(1));

        assertEquals(TrebuchetFiring.Result.COOLING_DOWN, use(trebuchet, false));
    }

    @Test
    void anUnloadedEngineInItsCooldownStillAsksForAmmunition() {
        Trebuchet trebuchet = built("world", 0, 64, 0, Facing.NORTH);
        use(trebuchet, true);
        use(trebuchet, false);

        TrebuchetFiring.Interaction interaction = interact(trebuchet, false);

        assertEquals(TrebuchetFiring.Result.NOT_LOADED, interaction.result());
        assertEquals(Duration.ZERO, interaction.cooldownRemaining());
    }

    @Test
    void eachEngineHasItsOwnCooldown() {
        Trebuchet first = built("world", 0, 64, 0, Facing.NORTH);
        Trebuchet second = built("world", 20, 64, 0, Facing.SOUTH);
        use(first, true);
        use(first, false);

        use(second, true);

        assertEquals(TrebuchetFiring.Result.FIRED, use(second, false));
    }

    @Test
    void aZeroCooldownLetsAnEngineFireAsSoonAsItIsReloaded() {
        TrebuchetFiring noCooldown = new TrebuchetFiring(registry, Duration.ZERO, () -> now);
        built("world", 0, 64, 0, Facing.NORTH);

        for (int shot = 0; shot < 3; shot++) {
            assertEquals(TrebuchetFiring.Result.LOADED, noCooldown.use("world", 0, 66, 0, true).orElseThrow().result());
            assertEquals(TrebuchetFiring.Result.FIRED, noCooldown.use("world", 0, 66, 0, false).orElseThrow().result());
        }
    }

    @Test
    void remainingCooldownIsReportedInWholeSecondsRoundedUp() {
        assertEquals(0, TrebuchetFiring.wholeSecondsUp(Duration.ZERO));
        assertEquals(1, TrebuchetFiring.wholeSecondsUp(Duration.ofNanos(1)));
        assertEquals(1, TrebuchetFiring.wholeSecondsUp(Duration.ofSeconds(1)));
        assertEquals(7, TrebuchetFiring.wholeSecondsUp(Duration.ofMillis(6_500)));
    }
}

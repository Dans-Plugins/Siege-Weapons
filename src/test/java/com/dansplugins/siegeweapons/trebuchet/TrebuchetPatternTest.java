package com.dansplugins.siegeweapons.trebuchet;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrebuchetPatternTest {

    @ParameterizedTest
    @EnumSource(Facing.class)
    void placingAnyPartOfACompleteTrebuchetFindsItsAnchorAndFacing(Facing facing) {
        FakeWorld world = new FakeWorld();
        world.buildTrebuchet(10, 64, -20, facing);

        for (TrebuchetPattern.Part part : TrebuchetPattern.PARTS) {
            List<TrebuchetPattern.Match> matches = TrebuchetPattern.findCompletedBy(world,
                    FakeWorld.worldX(10, part, facing), 64 + part.up(), FakeWorld.worldZ(-20, part, facing));

            assertEquals(List.of(new TrebuchetPattern.Match(10, 64, -20, facing)), matches, "placed " + part);
        }
    }

    @Test
    void missingAnyPartMeansNoTrebuchet() {
        for (TrebuchetPattern.Part missing : TrebuchetPattern.PARTS) {
            FakeWorld world = new FakeWorld();
            world.buildTrebuchet(0, 64, 0, Facing.EAST);
            world.set(FakeWorld.worldX(0, missing, Facing.EAST), 64 + missing.up(),
                    FakeWorld.worldZ(0, missing, Facing.EAST), Material.AIR);

            for (Facing facing : Facing.values()) {
                assertFalse(TrebuchetPattern.isCompleteAt(world, 0, 64, 0, facing),
                        "complete facing " + facing + " without " + missing);
            }
        }
    }

    @Test
    void wrongMaterialMeansNoTrebuchet() {
        FakeWorld world = new FakeWorld();
        world.buildTrebuchet(0, 64, 0, Facing.SOUTH);
        world.set(0, 66, 0, Material.SPRUCE_LOG);

        assertFalse(TrebuchetPattern.isCompleteAt(world, 0, 64, 0, Facing.SOUTH));
        assertTrue(TrebuchetPattern.findCompletedBy(world, 0, 64, 0).isEmpty());
    }

    @Test
    void counterweightDecidesTheFacing() {
        FakeWorld world = new FakeWorld();
        world.buildTrebuchet(0, 64, 0, Facing.NORTH);

        assertTrue(TrebuchetPattern.isCompleteAt(world, 0, 64, 0, Facing.NORTH));
        // The counterweight sits behind the axle, so the same frame does not read as facing south.
        assertFalse(TrebuchetPattern.isCompleteAt(world, 0, 64, 0, Facing.SOUTH));
        assertFalse(TrebuchetPattern.isCompleteAt(world, 0, 64, 0, Facing.EAST));
    }

    @Test
    void extraBlocksAroundTheEngineDoNotPreventDetection() {
        FakeWorld world = new FakeWorld();
        world.buildTrebuchet(0, 64, 0, Facing.WEST);
        world.set(0, 65, 0, Material.TORCH);
        world.set(5, 64, 5, Material.STONE);

        assertEquals(List.of(new TrebuchetPattern.Match(0, 64, 0, Facing.WEST)),
                TrebuchetPattern.findCompletedBy(world, 0, 66, 0));
    }

    @Test
    void aBlockSharedByTwoAdjacentTrebuchetsCompletesBoth() {
        FakeWorld world = new FakeWorld();
        // Two north-facing engines, the second directly in front of the first, so the first's
        // front row of planks is also the second's back row.
        world.buildTrebuchet(0, 64, 0, Facing.NORTH);
        world.buildTrebuchet(0, 64, -2, Facing.NORTH);

        List<TrebuchetPattern.Match> matches = TrebuchetPattern.findCompletedBy(world, 1, 64, -1);

        assertEquals(List.of(
                new TrebuchetPattern.Match(0, 64, -2, Facing.NORTH),
                new TrebuchetPattern.Match(0, 64, 0, Facing.NORTH)), matches);
    }

    @Test
    void anAnchorThatReadsAsTwoFacingsIsReportedOnceWithTheFirst() {
        FakeWorld world = new FakeWorld();
        world.buildTrebuchet(0, 64, 0, Facing.SOUTH);
        // A second counterweight in front makes the same frame read as facing north too.
        world.set(0, 65, 1, Material.IRON_BLOCK);

        assertEquals(List.of(new TrebuchetPattern.Match(0, 64, 0, Facing.NORTH)),
                TrebuchetPattern.findCompletedBy(world, 0, 66, 0));
    }

    @Test
    void anUnrelatedBlockFindsNothing() {
        FakeWorld world = new FakeWorld();
        world.buildTrebuchet(0, 64, 0, Facing.NORTH);
        world.set(0, 70, 0, Material.STONE);

        assertTrue(TrebuchetPattern.findCompletedBy(world, 0, 70, 0).isEmpty());
    }

    @ParameterizedTest
    @EnumSource(Facing.class)
    void isPartAtMatchesExactlyTheBlocksOfTheBuiltPattern(Facing facing) {
        FakeWorld world = new FakeWorld();
        world.buildTrebuchet(5, 64, 5, facing);

        for (int x = 2; x <= 8; x++) {
            for (int y = 62; y <= 68; y++) {
                for (int z = 2; z <= 8; z++) {
                    assertEquals(world.typeAt(x, y, z) != Material.AIR,
                            TrebuchetPattern.isPartAt(5, 64, 5, facing, x, y, z),
                            "at " + x + "," + y + "," + z);
                }
            }
        }
    }
}

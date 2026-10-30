package com.dansplugins.siegeweapons.trebuchet;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrebuchetTrajectoryTest {

    private static final double EPSILON = 1e-9;

    private static TrebuchetTrajectory.Launch launch(Facing facing) {
        return TrebuchetTrajectory.launchFrom(new Trebuchet(UUID.randomUUID(), "world", 10, 64, -20, facing), 50);
    }

    @Test
    void launchesFromAboveTheAxleOneBlockForward() {
        TrebuchetTrajectory.Launch north = launch(Facing.NORTH);
        assertEquals(10.5, north.x(), EPSILON);
        assertEquals(67.5, north.y(), EPSILON);
        assertEquals(-20.5, north.z(), EPSILON);

        TrebuchetTrajectory.Launch east = launch(Facing.EAST);
        assertEquals(11.5, east.x(), EPSILON);
        assertEquals(-19.5, east.z(), EPSILON);
    }

    @Test
    void throwsAlongTheFacingAndUpwards() {
        for (Facing facing : Facing.values()) {
            TrebuchetTrajectory.Launch launch = launch(facing);
            double forward = launch.velocityX() * facing.forwardX() + launch.velocityZ() * facing.forwardZ();
            double sideways = launch.velocityX() * facing.rightX() + launch.velocityZ() * facing.rightZ();

            assertTrue(forward > 0, facing + " forward");
            assertEquals(0, sideways, EPSILON, facing + " sideways");
            assertTrue(launch.velocityY() > 0, facing + " upward");
        }
    }

    @Test
    void launchesAtTheElevation() {
        TrebuchetTrajectory.Launch launch = launch(Facing.SOUTH);
        double horizontal = Math.hypot(launch.velocityX(), launch.velocityZ());

        assertEquals(TrebuchetTrajectory.ELEVATION_DEGREES,
                Math.toDegrees(Math.atan2(launch.velocityY(), horizontal)), EPSILON);
    }

    @Test
    void theLaunchSpeedLandsTheShotAtTheRange() {
        for (int range = 1; range <= TrebuchetSettings.RANGE_LIMIT; range++) {
            double speed = TrebuchetTrajectory.launchSpeedFor(range);

            assertTrue(speed >= 0 && speed < TrebuchetTrajectory.MAX_LAUNCH_SPEED, range + ": " + speed);
            assertEquals(range, TrebuchetTrajectory.distanceAt(speed), 1e-6, Integer.toString(range));
        }
    }

    @Test
    void aLongerRangeThrowsFaster() {
        TrebuchetTrajectory.Launch near = TrebuchetTrajectory.launchFrom(
                new Trebuchet(UUID.randomUUID(), "world", 0, 64, 0, Facing.NORTH), 20);
        TrebuchetTrajectory.Launch far = TrebuchetTrajectory.launchFrom(
                new Trebuchet(UUID.randomUUID(), "world", 0, 64, 0, Facing.NORTH), 80);

        assertTrue(Math.abs(far.velocityZ()) > Math.abs(near.velocityZ()));
        assertTrue(far.velocityY() > near.velocityY());
    }

    @Test
    void theArcFollowsThrownProjectilePhysics() {
        // A thrown snowball keeps 99% of its speed each tick and falls a further 0.03 blocks per tick.
        // Launched at 1.5 blocks per tick from 3.5 blocks up, it lands about 54 blocks from the anchor.
        assertEquals(54.0, TrebuchetTrajectory.distanceAt(1.5), 0.5);
        // With no speed it drops straight down from the launch point, one block forward of the anchor.
        assertEquals(1.0, TrebuchetTrajectory.distanceAt(0), 1e-9);
    }
}

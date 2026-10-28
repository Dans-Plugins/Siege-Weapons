package com.dansplugins.siegeweapons.trebuchet;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrebuchetTrajectoryTest {

    private static final double EPSILON = 1e-9;

    private static TrebuchetTrajectory.Launch launch(Facing facing) {
        return TrebuchetTrajectory.launchFrom(new Trebuchet(UUID.randomUUID(), "world", 10, 64, -20, facing));
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
    void launchSpeedAndElevationMatchTheConstants() {
        TrebuchetTrajectory.Launch launch = launch(Facing.SOUTH);
        double horizontal = Math.hypot(launch.velocityX(), launch.velocityZ());

        assertEquals(TrebuchetTrajectory.LAUNCH_SPEED, Math.hypot(horizontal, launch.velocityY()), EPSILON);
        assertEquals(TrebuchetTrajectory.ELEVATION_DEGREES,
                Math.toDegrees(Math.atan2(launch.velocityY(), horizontal)), EPSILON);
    }
}

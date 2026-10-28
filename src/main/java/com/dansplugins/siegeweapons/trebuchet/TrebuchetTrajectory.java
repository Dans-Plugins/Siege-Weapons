package com.dansplugins.siegeweapons.trebuchet;

/**
 * Where a trebuchet's projectile starts and how fast it leaves. The arc itself is the server's
 * own projectile physics (gravity and drag); this only sets the launch.
 *
 * <p>Every shot is thrown at {@link #ELEVATION_DEGREES} and {@link #LAUNCH_SPEED} along the
 * engine's facing, which lands it roughly 50 blocks out on level ground. Aiming is not
 * implemented yet, so the range is fixed.
 */
public final class TrebuchetTrajectory {

    /** Launch speed in blocks per tick. */
    public static final double LAUNCH_SPEED = 1.5;
    /** Launch angle above the horizontal. */
    public static final double ELEVATION_DEGREES = 45.0;

    /** A launch position (world coordinates) and velocity (blocks per tick). */
    public record Launch(double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
    }

    private TrebuchetTrajectory() {
    }

    /**
     * The launch for a shot from {@code trebuchet}: from the centre of the block above the
     * axle and one block forward of it, so the projectile clears the engine's own frame.
     */
    public static Launch launchFrom(Trebuchet trebuchet) {
        Facing facing = trebuchet.facing();
        double elevation = Math.toRadians(ELEVATION_DEGREES);
        double horizontal = LAUNCH_SPEED * Math.cos(elevation);
        return new Launch(
                trebuchet.x() + 0.5 + facing.forwardX(),
                trebuchet.y() + 3.5,
                trebuchet.z() + 0.5 + facing.forwardZ(),
                horizontal * facing.forwardX(),
                LAUNCH_SPEED * Math.sin(elevation),
                horizontal * facing.forwardZ());
    }
}

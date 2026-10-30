package com.dansplugins.siegeweapons.trebuchet;

/**
 * Where a trebuchet's projectile starts and how fast it leaves. The arc itself is the server's
 * own projectile physics (gravity and drag); this only sets the launch.
 *
 * <p>Every shot is thrown at {@link #ELEVATION_DEGREES} along the engine's facing. Its speed is
 * chosen so that, on level ground, it lands the engine's range away from the anchor. The speed
 * is found by stepping the same gravity and drag a thrown snowball has, so the landing point is
 * close to, not exactly, the range: the server's arc and the ground it lands on decide the rest.
 */
public final class TrebuchetTrajectory {

    /** Launch angle above the horizontal. */
    public static final double ELEVATION_DEGREES = 45.0;
    /** Height of the launch point above the anchor. */
    public static final double LAUNCH_HEIGHT = 3.5;

    /** Downward speed a thrown projectile gains each tick, in blocks per tick. */
    static final double GRAVITY = 0.03;
    /** Fraction of its speed a thrown projectile keeps each tick in air. */
    static final double DRAG = 0.99;
    /** The fastest launch the speed search considers, in blocks per tick; it reaches over 200 blocks. */
    static final double MAX_LAUNCH_SPEED = 4.0;
    private static final int MAX_TICKS = 1_000;

    /** A launch position (world coordinates) and velocity (blocks per tick). */
    public record Launch(double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
    }

    private TrebuchetTrajectory() {
    }

    /**
     * The launch for a shot from {@code trebuchet} aimed {@code range} blocks out: from the
     * centre of the block above the axle and one block forward of it, so the projectile clears
     * the engine's own frame.
     */
    public static Launch launchFrom(Trebuchet trebuchet, int range) {
        Facing facing = trebuchet.facing();
        double speed = launchSpeedFor(range);
        double elevation = Math.toRadians(ELEVATION_DEGREES);
        double horizontal = speed * Math.cos(elevation);
        return new Launch(
                trebuchet.x() + 0.5 + facing.forwardX(),
                trebuchet.y() + LAUNCH_HEIGHT,
                trebuchet.z() + 0.5 + facing.forwardZ(),
                horizontal * facing.forwardX(),
                speed * Math.sin(elevation),
                horizontal * facing.forwardZ());
    }

    /** The launch speed, in blocks per tick, that lands a shot {@code range} blocks from the anchor. */
    static double launchSpeedFor(int range) {
        double low = 0;
        double high = MAX_LAUNCH_SPEED;
        for (int i = 0; i < 60; i++) {
            double middle = (low + high) / 2;
            if (distanceAt(middle) < range) {
                low = middle;
            } else {
                high = middle;
            }
        }
        return (low + high) / 2;
    }

    /**
     * How far from the anchor, horizontally, a shot launched at {@code speed} comes back down to
     * the anchor's height. Each tick the projectile moves, then slows by drag, then falls by
     * gravity, as a thrown snowball does.
     */
    static double distanceAt(double speed) {
        double elevation = Math.toRadians(ELEVATION_DEGREES);
        double distance = 1;
        double height = LAUNCH_HEIGHT;
        double horizontal = speed * Math.cos(elevation);
        double vertical = speed * Math.sin(elevation);
        for (int tick = 0; tick < MAX_TICKS; tick++) {
            double nextDistance = distance + horizontal;
            double nextHeight = height + vertical;
            if (nextHeight <= 0) {
                // Interpolate to the point within this tick where the shot reaches the ground.
                return distance + horizontal * (height / (height - nextHeight));
            }
            distance = nextDistance;
            height = nextHeight;
            horizontal *= DRAG;
            vertical = vertical * DRAG - GRAVITY;
        }
        return distance;
    }
}

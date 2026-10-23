package com.dansplugins.siegeweapons.trebuchet;

/**
 * The horizontal direction a siege engine fires in. Engines are only ever built square to the
 * block grid, so the four cardinal directions are the only facings.
 */
public enum Facing {
    NORTH(0, -1),
    EAST(1, 0),
    SOUTH(0, 1),
    WEST(-1, 0);

    private final int forwardX;
    private final int forwardZ;

    Facing(int forwardX, int forwardZ) {
        this.forwardX = forwardX;
        this.forwardZ = forwardZ;
    }

    public int forwardX() {
        return forwardX;
    }

    public int forwardZ() {
        return forwardZ;
    }

    /** X step to the engine's right, looking along its facing (the facing turned clockwise). */
    public int rightX() {
        return -forwardZ;
    }

    /** Z step to the engine's right, looking along its facing (the facing turned clockwise). */
    public int rightZ() {
        return forwardX;
    }
}

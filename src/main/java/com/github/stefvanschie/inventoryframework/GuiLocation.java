package com.github.stefvanschie.inventoryframework;

/**
 * A location inside a GUI
 *
 * @since 5.6.0
 */
public class GuiLocation {

    /**
     * The x and y coordinate
     */
    private final int x, y;

    /**
     * Creates a new location for in a gui
     *
     * @param x the x coordinate in range 0..9.
     * @param y the y coordinate in range 0..9.
     */
    public GuiLocation(int x, int y) {
        if (!(x >= 0 && x <= 9)) {
            throw new IllegalArgumentException("x coordinate outside GUI");
        }
        if (!(y >= 0 && y <= 9)) {
            throw new IllegalArgumentException("y coordinate outside GUI");
        }

        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x coordinate
     *
     * @return the x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y coordinate
     *
     * @return the y coordinate
     */
    public int getY() {
        return y;
    }
}

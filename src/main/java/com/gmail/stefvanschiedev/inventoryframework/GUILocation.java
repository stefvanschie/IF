package com.gmail.stefvanschiedev.inventoryframework;

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
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public GuiLocation(int x, int y) {
        assert x >= 0 && x <= 9 : "x coordinate outside GUI";
        assert y >= 0 && y <= 9 : "y coordinate outside GUI";

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
package com.gmail.stefvanschiedev.inventoryframework;

/**
 * A location inside a GUI
 */
public class GUILocation {

    /**
     * The x and y coordinate
     */
    private int x, y;

    /**
     * Creates a new location for in a gui
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public GUILocation(int x, int y) {
        assert x >= 0 && x <= 9 : "x coordinate outside GUI";
        assert y >= 0 && y <= 9 : "y coordinate outside GUI";

        this.x = x;
        this.y = y;
    }

    /**
     * @return the x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y coordinate
     */
    public int getY() {
        return y;
    }
}
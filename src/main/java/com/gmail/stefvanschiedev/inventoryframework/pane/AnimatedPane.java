package com.gmail.stefvanschiedev.inventoryframework.pane;

import com.gmail.stefvanschiedev.inventoryframework.GUI;
import com.gmail.stefvanschiedev.inventoryframework.GUILocation;
import com.gmail.stefvanschiedev.inventoryframework.pane.util.Pane;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class AnimatedPane extends Pane {

    /**
     * A set of frames for the different pages
     */
    private Pane[] frames;

    /**
     * The current frame
     */
    private int frame;

    /**
     * The current animation timer
     */
    private BukkitRunnable runnable;

    /**
     * Constructs a new default pane
     *
     * @param start  the upper left corner of the pane
     * @param length the length of the pane
     * @param width  the width of the pane
     */
    public AnimatedPane(@NotNull GUILocation start, int length, int width, int frames) {
        super(start, length, width);

        this.frames = new Pane[frames];
        frame = 0;
    }

    /**
     * Starts this animation
     */
    public void start(Plugin plugin, GUI gui, long delay) {
        assert runnable == null : "Animation is already started";

        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                frame++;

                if (frame == frames.length)
                    frame = 0;

                gui.update();
            }
        };
        runnable.runTaskTimer(plugin, 0L, delay);
    }

    /**
     * Stops this animation
     */
    public void stop() {
        runnable.cancel();
        runnable = null;
    }

    /**
     * Sets a frame to a pane
     *
     * @param frame the current frame
     * @param pane the current pane
     */
    public void setFrame(int frame, Pane pane) {
        assert pane.getLength() == length && pane.getWidth() == width : "Panes length and/or width mismatch";

        frames[frame] = pane;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void display(Inventory inventory) {
        frames[frame].display(inventory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean click(@NotNull InventoryClickEvent event) {
        return frames[frame].click(event);
    }
}

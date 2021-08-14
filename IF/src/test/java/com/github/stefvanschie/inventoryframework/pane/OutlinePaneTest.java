package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.pane.util.Mask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OutlinePaneTest {

    @Test
    void testApplyMaskInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () ->
            new OutlinePane(3, 7).applyMask(new Mask("0", "1")));
    }

    @Test
    void testCopy() {
        OutlinePane original = new OutlinePane(8, 5, 1, 1, Pane.Priority.HIGHEST);
        original.setVisible(false);
        original.setOrientation(Orientable.Orientation.VERTICAL);
        original.setRotation(180);
        original.setGap(0);
        original.setRepeat(false);
        original.flipHorizontally(true);
        original.flipVertically(true);
        original.applyMask(new Mask("0"));
        original.align(OutlinePane.Alignment.CENTER);

        OutlinePane copy = original.copy();

        assertNotSame(original, copy);

        assertEquals(original.getX(), copy.getX());
        assertEquals(original.getY(), copy.getY());
        assertEquals(original.getLength(), copy.getLength());
        assertEquals(original.getHeight(), copy.getHeight());
        assertEquals(original.getPriority(), copy.getPriority());
        assertEquals(original.isVisible(), copy.isVisible());
        assertEquals(original.getOrientation(), copy.getOrientation());
        assertEquals(original.getRotation(), copy.getRotation());
        assertEquals(original.getGap(), copy.getGap());
        assertEquals(original.doesRepeat(), copy.doesRepeat());
        assertEquals(original.isFlippedHorizontally(), copy.isFlippedHorizontally());
        assertEquals(original.isFlippedVertically(), copy.isFlippedVertically());
        assertEquals(original.getMask(), copy.getMask());
        assertEquals(original.getAlignment(), copy.getAlignment());
        assertEquals(original.getUUID(), copy.getUUID());
    }
}

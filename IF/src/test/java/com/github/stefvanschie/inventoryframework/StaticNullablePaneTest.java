package com.github.stefvanschie.inventoryframework;

import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticNullablePane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class StaticNullablePaneTest {

    @Test
    void testCopy() {
        StaticNullablePane original = new StaticNullablePane(5, 1, 1, 1, Pane.Priority.MONITOR);
        original.setVisible(false);
        original.setRotation(90);
        original.flipHorizontally(false);
        original.flipVertically(true);

        StaticPane copy = original.copy();

        assertNotSame(original, copy);

        assertEquals(original.getX(), copy.getX());
        assertEquals(original.getY(), copy.getY());
        assertEquals(original.getLength(), copy.getLength());
        assertEquals(original.getHeight(), copy.getHeight());
        assertEquals(original.getPriority(), copy.getPriority());
        assertEquals(original.isVisible(), copy.isVisible());
        assertEquals(original.getRotation(), copy.getRotation());
        assertEquals(original.isFlippedHorizontally(), copy.isFlippedHorizontally());
        assertEquals(original.isFlippedVertically(), copy.isFlippedVertically());
        assertEquals(original.getUUID(), copy.getUUID());
    }
}

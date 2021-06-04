package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PatternPaneTest {

    @Test
    void testApplyPatternInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () ->
            new PatternPane(3, 7, new Pattern("0", "1")));

        PatternPane pane = new PatternPane(3, 7, new Pattern(
            "000",
            "000",
            "000",
            "000",
            "000",
            "000",
            "000"
        ));

        assertThrows(IllegalArgumentException.class, () -> pane.setPattern(new Pattern("0")));
    }

    @Test
    void testCopy() {
        PatternPane original = new PatternPane(8, 5, 1, 1, Pane.Priority.HIGHEST, new Pattern("1"));
        original.setVisible(false);
        original.setRotation(180);
        original.flipHorizontally(true);
        original.flipVertically(true);

        PatternPane copy = original.copy();

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
        assertEquals(original.getPattern(), copy.getPattern());
        assertEquals(original.getUUID(), copy.getUUID());
    }
}

package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.font.util.Font;
import com.github.stefvanschie.inventoryframework.pane.component.Label;
import com.github.stefvanschie.inventoryframework.pane.component.PercentageBar;
import com.github.stefvanschie.inventoryframework.pane.component.ToggleButton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PaginatedPaneTest {

    @Test
    void testCopy() {
        PaginatedPane original = new PaginatedPane(5, 5, 4, 1, Pane.Priority.NORMAL);
        original.setVisible(false);

        original.addPane(0, new OutlinePane(1, 1));
        original.addPane(1, new OutlinePane(1, 1));
        original.addPane(2, new PaginatedPane(1, 1));
        original.addPane(3, new PaginatedPane(1, 1));
        original.addPane(4, new OutlinePane(1, 1));

        original.setPage(4);

        PaginatedPane copy = original.copy();

        assertNotSame(original, copy);

        assertEquals(original.getX(), copy.getX());
        assertEquals(original.getY(), copy.getY());
        assertEquals(original.getLength(), copy.getLength());
        assertEquals(original.getHeight(), copy.getHeight());
        assertEquals(original.getPriority(), copy.getPriority());
        assertEquals(original.isVisible(), copy.isVisible());
        assertEquals(original.getPage(), copy.getPage());
        assertEquals(original.getPages(), copy.getPages());
    }
}

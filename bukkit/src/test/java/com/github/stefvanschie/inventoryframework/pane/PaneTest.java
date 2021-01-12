package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.bukkit.pane.Pane;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PaneTest {

    @Test
    void testPriorityIsGreaterThan() {
        assertTrue(Pane.Priority.MONITOR.isGreaterThan(Pane.Priority.HIGH));
        assertFalse(Pane.Priority.NORMAL.isGreaterThan(Pane.Priority.MONITOR));
    }

    @Test
    void testPriorityIsLessThan() {
        assertTrue(Pane.Priority.HIGHEST.isLessThan(Pane.Priority.MONITOR));
        assertFalse(Pane.Priority.NORMAL.isLessThan(Pane.Priority.LOWEST));
    }
}

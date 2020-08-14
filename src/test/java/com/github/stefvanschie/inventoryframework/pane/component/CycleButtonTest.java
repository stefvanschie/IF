package com.github.stefvanschie.inventoryframework.pane.component;

import com.github.stefvanschie.inventoryframework.pane.Pane;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CycleButtonTest {

    @Test
    void testCopy() {
        CycleButton original = new CycleButton(6, 2, 2, 3, Pane.Priority.HIGH);
        original.setVisible(true);

        original.addPane(new CycleButton(1, 1));

        CycleButton copy = original.copy();

        assertNotSame(original, copy);

        assertEquals(original.getX(), copy.getX());
        assertEquals(original.getY(), copy.getY());
        assertEquals(original.getLength(), copy.getLength());
        assertEquals(original.getHeight(), copy.getHeight());
        assertEquals(original.getPriority(), copy.getPriority());
        assertEquals(original.isVisible(), copy.isVisible());
        assertEquals(original.getPanes().size(), copy.getPanes().size());
        assertEquals(original.getUUID(), copy.getUUID());
    }
}

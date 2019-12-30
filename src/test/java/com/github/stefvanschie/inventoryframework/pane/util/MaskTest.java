package com.github.stefvanschie.inventoryframework.pane.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MaskTest {

    @Test
    void testConstructorIncorrectCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new Mask("missingno."));
    }

    @Test
    void testConstructorInvalidLengths() {
        assertThrows(IllegalArgumentException.class, () -> new Mask(
            "0",
            "0000000000"
        ));
    }

    @Test
    void testAmountOfEnabledSlots() {
        assertEquals(4, new Mask(
            "1001",
            "1001"
        ).amountOfEnabledSlots());
    }

    @Test
    void testGetColumn() {
        assertArrayEquals(new boolean[] {true, false}, new Mask(
            "10",
            "00"
        ).getColumn(0));
    }

    @Test
    void testGetRow() {
        assertArrayEquals(new boolean[] {true, false}, new Mask(
            "10",
            "00"
        ).getRow(0));
    }

    @Test
    void testIsEnabled() {
        assertTrue(new Mask(
            "10",
            "00"
        ).isEnabled(0, 0));
    }

    @Test
    void testGetLength() {
        assertEquals(2, new Mask(
            "10",
            "00",
            "01"
        ).getLength());
    }

    @Test
    void testGetHeight() {
        assertEquals(3, new Mask(
            "10",
            "00",
            "01"
        ).getHeight());
    }
}

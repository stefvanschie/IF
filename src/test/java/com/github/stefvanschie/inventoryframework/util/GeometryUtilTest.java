package com.github.stefvanschie.inventoryframework.util;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GeometryUtilTest {

    @Test
    void testClockwiseRotations() {
        testRotation(3, 2, GeometryUtil.processClockwiseRotation(3, 2, 5, 5, 0));
        testRotation(4, 1, GeometryUtil.processClockwiseRotation(1, 1, 6, 6, 90));
        testRotation(3, 5, GeometryUtil.processClockwiseRotation(2, 0, 6, 6, 180));
        testRotation(0, 1, GeometryUtil.processClockwiseRotation(0, 0, 2, 2, 270));
    }

    @Test
    void testCounterClockwiseRotations() {
        testRotation(0, 0, GeometryUtil.processCounterClockwiseRotation(0, 0, 6, 6, 0));
        testRotation(2, 2, GeometryUtil.processCounterClockwiseRotation(0, 2, 3, 3, 90));
        testRotation(1, 0, GeometryUtil.processCounterClockwiseRotation(2, 3, 4, 4, 180));
        testRotation(3, 1, GeometryUtil.processCounterClockwiseRotation(1, 0, 4, 4, 270));
    }

    void testRotation(int expectedX, int expectedY, @NotNull Map.Entry<Integer, Integer> coordinates) {
        assertEquals(expectedX, (int) coordinates.getKey());
        assertEquals(expectedY, (int) coordinates.getValue());
    }
}

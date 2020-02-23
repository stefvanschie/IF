package com.github.stefvanschie.inventoryframework.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CSVUtilTest {

    @Test
    void testCsvReading() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/test-csv-file.csv")) {
            List<String[]> strings = CSVUtil.readAll(inputStream);

            assertEquals("a", strings.get(0)[0]);
            assertEquals("b", strings.get(0)[1]);
            assertEquals("c", strings.get(1)[0]);
            assertEquals("d", strings.get(1)[1]);
            assertEquals("\"", strings.get(2)[0]);
            assertEquals("e", strings.get(2)[1]);
            assertEquals("f", strings.get(3)[0]);
            assertEquals("g", strings.get(3)[1]);
            assertEquals(",", strings.get(4)[0]);
            assertEquals(",", strings.get(4)[1]);
        }
    }
}

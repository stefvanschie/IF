package com.github.stefvanschie.inventoryframework.util;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class for reading csv files
 *
 * @since 0.5.0
 */
public final class CSVUtil {

    /**
     * A private constructor to ensure this utility class is never instantiated
     *
     * @since 0.5.0
     */
    private CSVUtil() {}

    private static final Pattern UNICODE_CHARACTER_PATTERN = Pattern.compile("\\\\u([0-9A-Fa-f]{4})");

    /**
     * Reads the entire file and returns it as a list of strings.
     *
     * @param inputStream the input stream to read from
     * @return a list of strings containing the values inside the file
     * @throws IOException when reading fails for any reason
     * @since 0.5.0
     */
    @NotNull
    public static List<String[]> readAll(@NotNull InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            List<String[]> strings = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }

                List<Integer> splittingIndices = new ArrayList<>();
                char[] chars = line.toCharArray();
                boolean quote = false;

                for (int i = 0; i < chars.length; i++) {
                    if (chars[i] == '"') {
                        quote = !quote;
                    } else if (chars[i] == ',' && !quote) {
                        splittingIndices.add(i);
                    }
                }

                String[] array = new String[splittingIndices.size() + 1];

                for (int i = 0; i < splittingIndices.size() + 1; i++) {
                    array[i] = line.substring(i - 1 < 0 ? 0 : splittingIndices.get(i - 1) + 1, i == splittingIndices.size() ? line.length() : splittingIndices.get(i));
                }

                for (int i = 0; i < array.length; i++) {
                    array[i] = array[i].trim();

                    if (array[i].startsWith("\"") && array[i].endsWith("\"")) {
                        array[i] = array[i].substring(1, array[i].length() - 1);
                    }

                    array[i] = StringUtils.replace(array[i], "\"\"", "\"");
                    //Restore original code (array[i] = array[i].replace("\"\"", "\""))
                    //once we update to Java 11, where it receives the current, faster implementation

                    //replace unicode characters
                    Matcher matcher = UNICODE_CHARACTER_PATTERN.matcher(array[i]);
                    StringBuffer buf = new StringBuffer(array[i].length());

                    while (matcher.find()) {
                        String character = String.valueOf((char) Integer.parseInt(matcher.group(1), 16));
                        matcher.appendReplacement(buf, Matcher.quoteReplacement(character));
                    }

                    matcher.appendTail(buf);

                    array[i] = buf.toString();
                }

                strings.add(array);
            }
            
            return strings;
        }
    }
}

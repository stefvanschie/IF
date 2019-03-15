package com.github.stefvanschie.inventoryframework.font.util;

import com.github.stefvanschie.inventoryframework.font.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * An interface for fonts
 *
 * @since 0.5.0
 */
public abstract class Font {

    /**
     * A map containing font names and mapping them to the fonts
     */
    @NotNull
    private static final Map<String, Font> FONT_BY_NAME = new HashMap<>();

    /**
     * The birch planks font
     */
    public static final Font BIRCH_PLANKS = new CSVFont(' ', "/fonts/birch-planks.csv");

    /**
     * The black font
     */
    public static final Font BLACK = new CSVFont(' ', "/fonts/black.csv");

    /**
     * The blue font
     */
    public static final Font BLUE = new CSVFont(' ', "/fonts/blue.csv");

    /**
     * The brown font
     */
    public static final Font BROWN = new CSVFont(' ', "/fonts/brown.csv");

    /**
     * The cobblestone font
     */
    public static final Font COBBLESTONE = new CSVFont(' ', "/fonts/cobblestone.csv");

    /**
     * The cyan font
     */
    public static final Font CYAN = new CSVFont(' ', "/fonts/cyan.csv");

    /**
     * The diamond font
     */
    public static final Font DIAMOND = new CSVFont(' ', "/fonts/diamond.csv");

    /**
     * The dirt font
     */
    public static final Font DIRT = new CSVFont(' ', "/fonts/dirt.csv");

    /**
     * The gold font
     */
    public static final Font GOLD = new CSVFont(' ', "/fonts/gold.csv");

    /**
     * The gray font
     */
    public static final Font GRAY = new CSVFont(' ', "/fonts/gray.csv");

    /**
     * The green font
     */
    public static final Font GREEN = new CSVFont(' ', "/fonts/green.csv");

    /**
     * The jungle planks font
     */
    public static final Font JUNGLE_PLANKS = new CSVFont(' ', "/fonts/jungle-planks.csv");

    /**
     * The letter cube font
     */
    public static final Font LETTER_CUBE = new CSVFont(' ', "/fonts/letter-cube.csv");

    /**
     * The light blue font
     */
    public static final Font LIGHT_BLUE = new CSVFont(' ', "/fonts/light-blue.csv");

    /**
     * The light gray font
     */
    public static final Font LIGHT_GRAY = new CSVFont(' ', "/fonts/light-gray.csv");

    /**
     * The lime font
     */
    public static final Font LIME = new CSVFont(' ', "/fonts/lime.csv");

    /**
     * The magenta font
     */
    public static final Font MAGENTA = new CSVFont(' ', "/fonts/magenta.csv");

    /**
     * The monitor font
     */
    public static final Font MONITOR = new CSVFont(' ', "/fonts/monitor.csv");

    /**
     * The oak log font
     */
    public static final Font OAK_LOG = new CSVFont(' ', "/fonts/oak-log.csv");

    /**
     * The oak planks font
     */
    public static final Font OAK_PLANKS = new CSVFont(' ', "/fonts/oak-planks.csv");

    /**
     * The orange font
     */
    public static final Font ORANGE = new CSVFont(' ', "/fonts/orange.csv");

    /**
     * The pink font
     */
    public static final Font PINK = new CSVFont(' ', "/fonts/pink.csv");

    /**
     * The plush font
     */
    public static final Font PLUSH = new CSVFont(' ', "/fonts/plush.csv");

    /**
     * The pumpkin font
     */
    public static final Font PUMPKIN = new CSVFont('_', "/fonts/pumpkin.csv");

    /**
     * The purple font
     */
    public static final Font PURPLE = new CSVFont(' ', "/fonts/purple.csv");

    /**
     * The quartz font
     */
    public static final Font QUARTZ = new CSVFont(' ', "/fonts/quartz.csv");

    /**
     * The rainbow font
     */
    public static final Font RAINBOW = new CSVFont(' ', "/fonts/rainbow.csv");

    /**
     * The red font
     */
    public static final Font RED = new CSVFont(' ', "/fonts/red.csv");

    /**
     * The spruce planks font
     */
    public static final Font SPRUCE_PLANKS = new CSVFont(' ', "/fonts/spruce-planks.csv");

    /**
     * The stone font
     */
    public static final Font STONE = new CSVFont(' ', "/fonts/stone.csv");

    /**
     * The watermelon font
     */
    public static final Font WATERMELON = new CSVFont(' ', "/fonts/watermelon.csv");

    /**
     * The white font
     */
    public static final Font WHITE = new CSVFont(' ', "/fonts/white.csv");

    /**
     * The yellow font
     */
    public static final Font YELLOW = new CSVFont(' ', "/fonts/yellow.csv");

    /**
     * Gets a default item for characters that do not have a dedicated item
     *
     * @return the default item
     * @since 0.5.0
     */
    @NotNull
    @Contract(pure = true)
    public abstract ItemStack getDefaultItem();

    /**
     * Turns the specified character into an {@link ItemStack} representing the specified character. If there is no item
     * for the specified character this will return null.
     *
     * @param character the character to get an item from
     * @return the item
     * @since 0.5.0
     */
    @Nullable
    @Contract(pure = true)
    public abstract ItemStack toItem(char character);

    /**
     * Gets a font by its name. The name will be made uppercase and spaces will be replaced with underscore before
     * trying to access it.
     *
     * @param name the name of the font
     * @return the font
     * @since 0.5.0
     */
    @Nullable
    @Contract(pure = true)
    public static Font fromName(@NotNull String name) {
        return FONT_BY_NAME.get(name.replace(' ', '_').toUpperCase(Locale.getDefault()));
    }

    /**
     * Registers a custom font so it can be used in lookups and XML files. The name will be made upper case and spaces
     * will be turned into underscores to ensure a standardized format for all font names.
     *
     * @param name the font name
     * @param font the font
     * @since 0.5.0
     */
    @Contract(pure = true)
    public static void registerFont(@NotNull String name, @NotNull Font font) {
        FONT_BY_NAME.put(name.replace(' ', '_').toUpperCase(Locale.getDefault()), font);
    }

    static {
        FONT_BY_NAME.put("BIRCH_PLANKS", BIRCH_PLANKS);
        FONT_BY_NAME.put("BLACK", BLACK);
        FONT_BY_NAME.put("BLUE", BLUE);
        FONT_BY_NAME.put("BROWN", BROWN);
        FONT_BY_NAME.put("COBBLESTONE", COBBLESTONE);
        FONT_BY_NAME.put("CYAN", CYAN);
        FONT_BY_NAME.put("DIAMOND", DIAMOND);
        FONT_BY_NAME.put("DIRT", DIRT);
        FONT_BY_NAME.put("GOLD", GOLD);
        FONT_BY_NAME.put("GRAY", GRAY);
        FONT_BY_NAME.put("GREEN", GREEN);
        FONT_BY_NAME.put("JUNGLE_PLANKS", JUNGLE_PLANKS);
        FONT_BY_NAME.put("LETTER_CUBE", LETTER_CUBE);
        FONT_BY_NAME.put("LIGHT_BLUE", LIGHT_BLUE);
        FONT_BY_NAME.put("LIGHT_GRAY", LIGHT_GRAY);
        FONT_BY_NAME.put("LIME", LIME);
        FONT_BY_NAME.put("MAGENTA", MAGENTA);
        FONT_BY_NAME.put("MONITOR", MONITOR);
        FONT_BY_NAME.put("OAK_LOG", OAK_LOG);
        FONT_BY_NAME.put("OAK_PLANKS", OAK_PLANKS);
        FONT_BY_NAME.put("ORANGE", ORANGE);
        FONT_BY_NAME.put("PINK", PINK);
        FONT_BY_NAME.put("PLUSH", PLUSH);
        FONT_BY_NAME.put("PUMPKIN", PUMPKIN);
        FONT_BY_NAME.put("PURPLE", PURPLE);
        FONT_BY_NAME.put("QUARTZ", QUARTZ);
        FONT_BY_NAME.put("RAINBOW", RAINBOW);
        FONT_BY_NAME.put("RED", RED);
        FONT_BY_NAME.put("SPRUCE_PLANKS", SPRUCE_PLANKS);
        FONT_BY_NAME.put("STONE", STONE);
        FONT_BY_NAME.put("WATERMELON", WATERMELON);
        FONT_BY_NAME.put("WHITE", WHITE);
        FONT_BY_NAME.put("YELLOW", YELLOW);
    }
}

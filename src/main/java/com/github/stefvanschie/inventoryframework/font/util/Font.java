package com.github.stefvanschie.inventoryframework.font.util;

import com.github.stefvanschie.inventoryframework.font.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface for fonts
 *
 * @since 0.5.0
 */
public interface Font {

    /**
     * The birch planks font
     */
    Font BIRCH_PLANKS = new CSVFont(' ', "/fonts/birch-planks.csv");

    /**
     * The black font
     */
    Font BLACK = new CSVFont(' ', "/fonts/black.csv");

    /**
     * The blue font
     */
    Font BLUE = new CSVFont(' ', "/fonts/blue.csv");

    /**
     * The brown font
     */
    Font BROWN = new CSVFont(' ', "/fonts/brown.csv");

    /**
     * The cobblestone font
     */
    Font COBBLESTONE = new CSVFont(' ', "/fonts/cobblestone.csv");

    /**
     * The cyan font
     */
    Font CYAN = new CSVFont(' ', "/fonts/cyan.csv");

    /**
     * The diamond font
     */
    Font DIAMOND = new CSVFont(' ', "/fonts/diamond.csv");

    /**
     * The dirt font
     */
    Font DIRT = new CSVFont(' ', "/fonts/dirt.csv");

    /**
     * The gold font
     */
    Font GOLD = new CSVFont(' ', "/fonts/gold.csv");

    /**
     * The gray font
     */
    Font GRAY = new CSVFont(' ', "/fonts/gray.csv");

    /**
     * The green font
     */
    Font GREEN = new CSVFont(' ', "/fonts/green.csv");

    /**
     * The jungle planks font
     */
    Font JUNGLE_PLANKS = new CSVFont(' ', "/fonts/jungle-planks.csv");

    /**
     * The letter cube font
     */
    Font LETTER_CUBE = new CSVFont(' ', "/fonts/letter-cube.csv");

    /**
     * The light blue font
     */
    Font LIGHT_BLUE = new CSVFont(' ', "/fonts/light-blue.csv");

    /**
     * The light gray font
     */
    Font LIGHT_GRAY = new CSVFont(' ', "/fonts/light-gray.csv");

    /**
     * The lime font
     */
    Font LIME = new CSVFont(' ', "/fonts/lime.csv");

    /**
     * The magenta font
     */
    Font MAGENTA = new CSVFont(' ', "/fonts/magenta.csv");

    /**
     * The monitor font
     */
    Font MONITOR = new CSVFont(' ', "/fonts/monitor.csv");

    /**
     * The oak log font
     */
    Font OAK_LOG = new CSVFont(' ', "/fonts/oak-log.csv");

    /**
     * The oak planks font
     */
    Font OAK_PLANKS = new CSVFont(' ', "/fonts/oak-planks.csv");

    /**
     * The orange font
     */
    Font ORANGE = new CSVFont(' ', "/fonts/orange.csv");

    /**
     * The pink font
     */
    Font PINK = new CSVFont(' ', "/fonts/pink.csv");

    /**
     * The plush font
     */
    Font PLUSH = new CSVFont(' ', "/fonts/plush.csv");

    /**
     * The pumpkin font
     */
    Font PUMPKIN = new CSVFont('_', "/fonts/pumpkin.csv");

    /**
     * The purple font
     */
    Font PURPLE = new CSVFont(' ', "/fonts/purple.csv");

    /**
     * The quartz font
     */
    Font QUARTZ = new CSVFont(' ', "/fonts/quartz.csv");

    /**
     * The rainbow font
     */
    Font RAINBOW = new CSVFont(' ', "/fonts/rainbow.csv");

    /**
     * The red font
     */
    Font RED = new CSVFont(' ', "/fonts/red.csv");

    /**
     * The spruce planks font
     */
    Font SPRUCE_PLANKS = new CSVFont(' ', "/fonts/spruce-planks.csv");

    /**
     * The stone font
     */
    Font STONE = new CSVFont(' ', "/fonts/stone.csv");

    /**
     * The watermelon font
     */
    Font WATERMELON = new CSVFont(' ', "/fonts/watermelon.csv");

    /**
     * The white font
     */
    Font WHITE = new CSVFont(' ', "/fonts/white.csv");

    /**
     * The yellow font
     */
    Font YELLOW = new CSVFont(' ', "/fonts/yellow.csv");

    /**
     * Gets a default item for characters that do not have a dedicated item
     *
     * @return the default item
     * @since 0.5.0
     */
    @NotNull
    ItemStack getDefaultItem();

    /**
     * Turns the specified character into an {@link ItemStack} representing the specified character. If there is no item
     * for the specified character this will return null.
     *
     * @param character the character to get an item from
     * @return the item
     * @since 0.5.0
     */
    @Nullable
    ItemStack toItem(char character);

}

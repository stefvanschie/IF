package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.github.stefvanschie.inventoryframework.util.GeometryUtil;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * A pattern pane allows you to specify a textual pattern and assign items to individual characters.
 *
 * @since 0.9.8
 */
public class PatternPane extends Pane implements Flippable, Rotatable {

    /**
     * The pattern of this pane.
     */
    @NotNull
    private Pattern pattern;

    /**
     * The bindings between the characters in the pattern and the gui item. Not every character in the pattern has to be
     * present in this map and this map may contain characters that are not present in the pattern.
     */
    @NotNull
    private final Map<Integer, GuiItem> bindings = new HashMap<>();

    /**
     * The amount of degrees this pane is rotated by. This will always be between [0,360) and a multiple of 90.
     */
    private int rotation;

    /**
     * Whether this pane is flipped horizontally.
     */
    private boolean flippedHorizontally;

    /**
     * Whether this pane is flipped vertically.
     */
    private boolean flippedVertically;

    /**
     * Constructs a new pattern pane.
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param priority the priority of the pane
     * @param pattern the pattern of the pane
     * @throws IllegalArgumentException when the pane and pattern dimensions don't match
     * @since 0.10.8
     */
    public PatternPane(@NotNull Slot slot, int length, int height, @NotNull Priority priority, @NotNull Pattern pattern) {
        super(slot, length, height, priority);

        if (pattern.getLength() != length || pattern.getHeight() != height) {
            throw new IllegalArgumentException(
                    "Dimensions of the provided pattern do not match the dimensions of the pane"
            );
        }

        this.pattern = pattern;
    }

    /**
     * Constructs a new pattern pane.
     *
     * @param x the upper left x coordinate of the pane
     * @param y the upper left y coordinate of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param priority the priority of the pane
     * @param pattern the pattern of the pane
     * @throws IllegalArgumentException when the pane and pattern dimensions don't match
     * @since 0.9.8
     */
    public PatternPane(int x, int y, int length, int height, @NotNull Priority priority, @NotNull Pattern pattern) {
        this(Slot.fromXY(x, y), length, height, priority, pattern);
    }

    /**
     * Constructs a new pattern pane, with no position.
     *
     * @param length the length of the pane
     * @param height the height of the pane
     * @param pattern the pattern of the pane
     * @throws IllegalArgumentException when the pane and pattern dimensions don't match
     * @since 0.9.8
     */
    public PatternPane(int length, int height, @NotNull Pattern pattern) {
        this(0, 0, length, height, pattern);
    }

    /**
     * Constructs a new pattern pane.
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param pattern the pattern of the pane
     * @throws IllegalArgumentException when the pane and pattern dimensions don't match
     * @since 0.10.8
     */
    public PatternPane(@NotNull Slot slot, int length, int height, @NotNull Pattern pattern) {
        this(slot, length, height, Priority.NORMAL, pattern);
    }

    /**
     * Constructs a new pattern pane.
     *
     * @param x the upper left x coordinate of the pane
     * @param y the upper left y coordinate of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param pattern the pattern of the pane
     * @throws IllegalArgumentException when the pane and pattern dimensions don't match
     * @since 0.9.8
     */
    public PatternPane(int x, int y, int length, int height, @NotNull Pattern pattern) {
        this(x, y, length, height, Priority.NORMAL, pattern);
    }

    @Override
    public void display(@NotNull InventoryComponent inventoryComponent, int paneOffsetX, int paneOffsetY, int maxLength,
                        int maxHeight) {
        int length = Math.min(this.length, maxLength);
        int height = Math.min(this.height, maxHeight);

        for (int x = 0; x < length; x++) {
            for (int y = 0; y < height; y++) {
                GuiItem item = this.bindings.get(pattern.getCharacter(x, y));

                if (item == null || !item.isVisible()) {
                    continue;
                }

                int newX = x, newY = y;

                if (isFlippedHorizontally()) {
                    newX = length - x - 1;
                }

                if (isFlippedVertically()) {
                    newY = height - y - 1;
                }

                Map.Entry<Integer, Integer> coordinates = GeometryUtil.processClockwiseRotation(newX, newY, length,
                    height, rotation);

                newX = coordinates.getKey();
                newY = coordinates.getValue();

                Slot slot = getSlot();

                int finalRow = slot.getY(maxLength) + newY + paneOffsetY;
                int finalColumn = slot.getX(maxLength) + newX + paneOffsetX;

                inventoryComponent.setItem(item, finalColumn, finalRow);
            }
        }
    }

    @Override
    public boolean click(@NotNull Gui gui, @NotNull InventoryComponent inventoryComponent,
                         @NotNull InventoryClickEvent event, int slot, int paneOffsetX, int paneOffsetY, int maxLength,
                         int maxHeight) {
        int length = Math.min(this.length, maxLength);
        int height = Math.min(this.height, maxHeight);

        Slot paneSlot = getSlot();

        int xPosition = paneSlot.getX(maxLength);
        int yPosition = paneSlot.getY(maxLength);

        int totalLength = inventoryComponent.getLength();

        int adjustedSlot = slot - (xPosition + paneOffsetX) - totalLength * (yPosition + paneOffsetY);

        int x = adjustedSlot % totalLength;
        int y = adjustedSlot / totalLength;

        //this isn't our item
        if (x < 0 || x >= length || y < 0 || y >= height) {
            return false;
        }

        callOnClick(event);

        ItemStack itemStack = event.getCurrentItem();

        if (itemStack == null) {
            return false;
        }

        GuiItem clickedItem = findMatchingItem(getItems(), itemStack);

        if (clickedItem == null) {
            return false;
        }

        clickedItem.callAction(event);

        return true;
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public PatternPane copy() {
        PatternPane patternPane = new PatternPane(getSlot(), getLength(), getHeight(), getPriority(), getPattern());

        patternPane.setVisible(isVisible());
        patternPane.onClick = onClick;

        patternPane.uuid = uuid;

        patternPane.rotation = rotation;
        patternPane.flippedHorizontally = flippedHorizontally;
        patternPane.flippedVertically = flippedVertically;

        return patternPane;
    }

    @Override
    public void setRotation(int rotation) {
        if (getLength() != getHeight()) {
            throw new IllegalArgumentException("Rotations can only be applied to square panes");
        }

        if (rotation >= 0 && rotation % 90 != 0) {
            throw new IllegalArgumentException("Rotation must be non-negative and be a multiple of 90");
        }

        this.rotation = rotation % 360;
    }

    /**
     * {@inheritDoc}
     *
     * This only returns the items for which their binding also appears in the given pattern. An item bound to 'x',
     * where 'x' does not appear in the pattern will not be returned.
     *
     * @return the bounded and used items
     * @since 0.9.8
     */
    @NotNull
    @Override
    public Collection<GuiItem> getItems() {
        Set<GuiItem> items = new HashSet<>();

        for (Map.Entry<Integer, GuiItem> binding : bindings.entrySet()) {
            if (pattern.contains(binding.getKey())) {
                items.add(binding.getValue());
            }
        }

        return Collections.unmodifiableCollection(items);
    }

    /**
     * Overrides the pattern set on this pane.
     *
     * @param pattern the new pattern to set
     * @throws IllegalArgumentException when the pane and pattern dimensions don't match
     * @since 0.9.8
     */
    public void setPattern(@NotNull Pattern pattern) {
        if (pattern.getLength() != getLength() || pattern.getHeight() != getHeight()) {
            throw new IllegalArgumentException(
                "Dimensions of the provided pattern do not match the dimensions of the pane"
            );
        }

        this.pattern = pattern;
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);

        this.pattern = this.pattern.setHeight(height);
    }

    @Override
    public void setLength(int length) {
        super.setLength(length);

        this.pattern = this.pattern.setLength(length);
    }

    /**
     * Binds a character to a specific item or if the character was already bound, this overwrites the previously
     * binding with the provided one. To bind characters above the 16-bit range, see {@link #bindItem(int, GuiItem)}.
     *
     * @param character the character
     * @param item the item this represents
     * @since 0.9.8
     */
    public void bindItem(char character, @NotNull GuiItem item) {
        this.bindings.put((int) character, item);
    }

    /**
     * Binds a character to a specific item or if the character was already bound, this overwrites the previously
     * binding with the provided one.
     *
     * @param character the character
     * @param item the item this represents
     * @since 0.9.8
     * @see PatternPane#bindItem(char, GuiItem)
     */
    public void bindItem(int character, @NotNull GuiItem item) {
        this.bindings.put(character, item);
    }

    @Override
    public void clear() {
        this.bindings.clear();
    }

    @Override
    public void flipHorizontally(boolean flipHorizontally) {
        this.flippedHorizontally = flipHorizontally;
    }

    @Override
    public void flipVertically(boolean flipVertically) {
        this.flippedVertically = flipVertically;
    }

    @NotNull
    @Override
    public Collection<Pane> getPanes() {
        return Collections.emptySet();
    }

    /**
     * Gets the pattern.
     *
     * @return the pattern
     * @since 0.9.8
     */
    @NotNull
    @Contract(pure = true)
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public boolean isFlippedHorizontally() {
        return this.flippedHorizontally;
    }

    @Override
    public boolean isFlippedVertically() {
        return this.flippedVertically;
    }

    @Override
    public int getRotation() {
        return this.rotation;
    }

    /**
     * Loads a pattern pane from a given element
     *
     * @param instance the instance class
     * @param element the element
     * @param plugin the plugin that will own the underlying items
     * @return the pattern pane
     * @since 0.10.8
     */
    @NotNull
    public static PatternPane load(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
        try {
            NodeList childNodes = element.getChildNodes();

            Pattern pattern = null;
            Map<Integer, GuiItem> bindings = new HashMap<>();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);

                if (item.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element child = (Element) item;
                String name = item.getNodeName();

                if (name.equals("pattern")) {
                    pattern = Pattern.load(child);
                } else if (name.equals("binding")) {
                    String character = child.getAttribute("char");

                    if (character == null) {
                        throw new XMLLoadException("Missing char attribute on binding");
                    }

                    if (character.codePointCount(0, character.length()) != 1) {
                        throw new XMLLoadException("Char attribute doesn't have one character");
                    }

                    NodeList children = child.getChildNodes();
                    GuiItem guiItem = null;

                    for (int index = 0; index < children.getLength(); index++) {
                        Node guiItemNode = children.item(index);

                        if (guiItemNode.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }

                        if (guiItem != null) {
                            throw new XMLLoadException("Binding has multiple inner tags, one expected");
                        }

                        guiItem = Pane.loadItem(instance, (Element) guiItemNode, plugin);
                    }

                    //guaranteed to only be a single code point
                    bindings.put(character.codePoints().toArray()[0], guiItem);
                } else {
                    throw new XMLLoadException("Unknown tag " + name + " in pattern pane");
                }
            }

            if (pattern == null) {
                throw new XMLLoadException("Pattern pane doesn't have a pattern");
            }

            PatternPane patternPane = new PatternPane(
                Integer.parseInt(element.getAttribute("length")),
                Integer.parseInt(element.getAttribute("height")),
                pattern
            );

            Pane.load(patternPane, instance, element);
            Flippable.load(patternPane, element);
            Rotatable.load(patternPane, element);

            if (!element.hasAttribute("populate")) {
                for (Map.Entry<Integer, GuiItem> entry : bindings.entrySet()) {
                    patternPane.bindItem(entry.getKey(), entry.getValue());
                }
            }

            return patternPane;
        } catch (NumberFormatException exception) {
            throw new XMLLoadException(exception);
        }
    }

    /**
     * Loads a pattern pane from a given element
     *
     * @param instance the instance class
     * @param element the element
     * @return the pattern pane
     * @deprecated this method is no longer used internally and has been superseded by
     *             {@link #load(Object, Element, Plugin)}
     */
    @NotNull
    @Deprecated
    public static PatternPane load(@NotNull Object instance, @NotNull Element element) {
        return load(instance, element, JavaPlugin.getProvidingPlugin(PatternPane.class));
    }
}

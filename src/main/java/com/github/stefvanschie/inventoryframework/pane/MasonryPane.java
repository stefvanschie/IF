package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This pane holds panes and decides itself where every pane should go. It tries to put every pane in the top left
 * corner and will move rightwards and downwards respectively if the top left corner is already in use. Depending on the
 * order and size of the panes, this may leave empty spaces in certain spots. Do note however that the order of panes
 * isn't always preserved. If there is a gap left in which a pane with a higher index can fit, it will be put there,
 * even if there are panes with a lower index after it. Panes that do not fit will not be displayed.
 *
 * @since 0.3.0
 */
public class MasonryPane extends Pane implements Orientable {

    /**
     * A list of panes that should be displayed
     */
    @NotNull
    private final List<Pane> panes = new ArrayList<>();

    /**
     * The orientation of the items in this pane
     */
    @NotNull
    private Orientation orientation = Orientation.HORIZONTAL;

    public MasonryPane(int x, int y, int length, int height, @NotNull Priority priority) {
        super(x, y, length, height, priority);
    }

    public MasonryPane(int x, int y, int length, int height) {
        super(x, y, length, height);
    }

    public MasonryPane(int length, int height) {
        super(length, height);
    }

    @Override
    public void display(@NotNull Gui gui, @NotNull Inventory inventory, @NotNull PlayerInventory playerInventory,
                        int paneOffsetX, int paneOffsetY, int maxLength, int maxHeight) {
        int length = Math.min(this.length, maxLength) - paneOffsetX;
        int height = Math.min(this.height, maxHeight) - paneOffsetY;

        int[][] positions = new int[length][height];

        for (int[] array : positions) {
            Arrays.fill(array, -1);
        }

        for (int paneIndex = 0; paneIndex < panes.size(); paneIndex++) {
            Pane pane = panes.get(paneIndex);

            if (orientation == Orientation.HORIZONTAL) {
                outerLoop:
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < length; x++) {
                        //check whether the pane fits
                        boolean fits = true;

                        paneFits:
                        for (int i = 0; i < pane.getLength(); i++) {
                            for (int j = 0; j < pane.getHeight(); j++) {
                                if (x + i >= positions.length || y + j >= positions[x + i].length || positions[x + i][y + j] != -1) {
                                    fits = false;
                                    break paneFits;
                                }
                            }
                        }

                        if (fits) {
                            for (int i = 0; i < pane.getLength(); i++) {
                                for (int j = 0; j < pane.getHeight(); j++) {
                                    positions[x + i][y + j] = paneIndex;
                                }
                            }

                            pane.setX(x);
                            pane.setY(y);

                            pane.display(
                                gui,
                                inventory,
                                playerInventory,
                                paneOffsetX + getX(),
                                paneOffsetY + getY(),
                                Math.min(this.length, maxLength),
                                Math.min(this.height, maxHeight)
                            );
                            break outerLoop;
                        }
                    }
                }
            } else if (orientation == Orientation.VERTICAL) {
                outerLoop:
                for (int x = 0; x < length; x++) {
                    for (int y = 0; y < height; y++) {
                        //check whether the pane fits
                        boolean fits = true;

                        paneFits:
                        for (int i = 0; i < pane.getHeight(); i++) {
                            for (int j = 0; j < pane.getLength(); j++) {
                                if (x + j >= positions.length || y + i >= positions[x + j].length || positions[x + j][y + i] != -1) {
                                    fits = false;
                                    break paneFits;
                                }
                            }
                        }

                        if (fits) {
                            for (int i = 0; i < pane.getLength(); i++) {
                                for (int j = 0; j < pane.getHeight(); j++) {
                                    positions[x + i][y + j] = paneIndex;
                                }
                            }

                            pane.setX(x);
                            pane.setY(y);

                            pane.display(
                                gui,
                                inventory,
                                playerInventory,
                                paneOffsetX + getX(),
                                paneOffsetY + getY(),
                                Math.min(this.length, maxLength),
                                Math.min(this.height, maxHeight)
                            );
                            break outerLoop;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean click(@NotNull Gui gui, @NotNull InventoryClickEvent event, int paneOffsetX, int paneOffsetY,
                         int maxLength, int maxHeight) {
        int length = Math.min(this.length, maxLength);
        int height = Math.min(this.height, maxHeight);

        int slot = event.getSlot();

        int x, y;

        if (Gui.getInventory(event.getView(), event.getRawSlot()).equals(event.getView().getBottomInventory())) {
            x = (slot % 9) - getX() - paneOffsetX;
            y = ((slot / 9) + gui.getRows() - 1) - getY() - paneOffsetY;

            if (slot / 9 == 0) {
                y = (gui.getRows() + 3) - getY() - paneOffsetY;
            }
        } else {
            x = (slot % 9) - getX() - paneOffsetX;
            y = (slot / 9) - getY() - paneOffsetY;
        }

        if (x < 0 || x >= length || y < 0 || y >= height)
            return false;

        if (onClick != null)
            onClick.accept(event);

        boolean success = false;

        for (Pane pane : panes) {
            success = success || pane.click(gui, event, paneOffsetX + getX(),
                paneOffsetY + getY(), length, height);
        }

        return success;
    }

    /**
     * Adds a pane to this masonry pane
     *
     * @param pane the pane to add
     * @since 0.3.0
     */
    public void addPane(@NotNull Pane pane) {
        panes.add(pane);
    }

    @NotNull
    @Override
    public Collection<GuiItem> getItems() {
        return getPanes().stream().flatMap(pane -> pane.getItems().stream()).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public Collection<Pane> getPanes() {
        Collection<Pane> panes = new HashSet<>();

        this.panes.forEach(p -> {
            panes.addAll(p.getPanes());
            panes.add(p);
        });

        return panes;
    }

    @Override
    public void clear() {
        panes.clear();
    }

    @NotNull
    @Override
    public Orientation getOrientation() {
        return orientation;
    }

    @Override
    public void setOrientation(@NotNull Orientation orientation) {
        this.orientation = orientation;
    }

    /**
     * Loads a masonry pane from a given element
     *
     * @param instance the instance class
     * @param element the element
     * @return the masonry pane
     */
    @NotNull
    public static MasonryPane load(@NotNull Object instance, @NotNull Element element) {
        try {
            MasonryPane masonryPane = new MasonryPane(
                Integer.parseInt(element.getAttribute("length")),
                Integer.parseInt(element.getAttribute("height"))
            );

            Pane.load(masonryPane, instance, element);
            Orientable.load(masonryPane, element);

            if (element.hasAttribute("populate")) {
                return masonryPane;
            }

            NodeList childNodes = element.getChildNodes();

            for (int j = 0; j < childNodes.getLength(); j++) {
                Node pane = childNodes.item(j);

                if (pane.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                masonryPane.addPane(Gui.loadPane(instance, pane));
            }

            return masonryPane;
        } catch (NumberFormatException exception) {
            throw new XMLLoadException(exception);
        }
    }
}

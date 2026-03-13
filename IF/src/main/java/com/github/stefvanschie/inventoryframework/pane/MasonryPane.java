package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.gui.GuiComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.pane.util.GuiItemContainer;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
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

    /**
     * The cached positions from the last display.
     */
    private int[][] cachedPositions;

    /**
     * Creates a new masonry pane
     *
     * @param length the length of the pane
     * @param height the height of the pane
     * @param priority the priority of the pane
     * @since 0.12.0
     */
    public MasonryPane(int length, int height, @NotNull Priority priority) {
        super(length, height, priority);
    }

    /**
     * Creates a new masonry pane
     *
     * @param length the length of the pane
     * @param height the height of the pane
     * @since 0.12.0
     */
    public MasonryPane(int length, int height) {
        super(length, height);
    }

    @NotNull
    @Override
    public GuiItemContainer display() {
        GuiItemContainer container = new GuiItemContainer(getLength(), getHeight());

        int[][] positions = new int[getLength()][getHeight()];

        for (int[] array : positions) {
            Arrays.fill(array, -1);
        }

        for (int paneIndex = 0; paneIndex < panes.size(); paneIndex++) {
            Pane pane = panes.get(paneIndex);

            if (!pane.isVisible()) {
                continue;
            }

            if (orientation == Orientation.HORIZONTAL) {
                outerLoop:
                for (int y = 0; y < getHeight(); y++) {
                    for (int x = 0; x < getLength(); x++) {
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

                            container.apply(pane.display(), x, y);
                            break outerLoop;
                        }
                    }
                }
            } else if (orientation == Orientation.VERTICAL) {
                outerLoop:
                for (int x = 0; x < getLength(); x++) {
                    for (int y = 0; y < getHeight(); y++) {
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

                            container.apply(pane.display(), x, y);
                            break outerLoop;
                        }
                    }
                }
            }
        }

        this.cachedPositions = positions;

        return container;
    }

    @Override
    public boolean click(@NotNull Gui gui, @NotNull GuiComponent guiComponent, @NotNull InventoryClickEvent event,
                         @NotNull Slot slot) {
        int x = slot.getX(getLength());
        int y = slot.getY(getLength());

        if (x < 0 || x >= getLength() || y < 0 || y >= getHeight()) {
            return false;
        }

        callOnClick(event);

        boolean success = false;

        for (int index = 0; index < this.panes.size(); index++) {
            Pane pane = this.panes.get(index);

            if (!pane.isVisible()) {
                continue;
            }

            outer:
            for (int column = 0; column < this.cachedPositions.length; column++) {
                for (int row = 0; row < this.cachedPositions[column].length; row++) {
                    if (this.cachedPositions[column][row] != index) {
                        continue;
                    }

                    success = success || pane.click(gui, guiComponent, event, Slot.fromXY(x - column, y - row));

                    break outer;
                }
            }
        }

        return success;
    }

    @NotNull
	@Contract(pure = true)
	@Override
    public MasonryPane copy() {
		MasonryPane masonryPane = new MasonryPane(getLength(), getHeight(), getPriority());

		for (Pane pane : panes) {
            masonryPane.addPane(pane.copy());
        }

        masonryPane.setVisible(isVisible());
		masonryPane.onClick = onClick;
		masonryPane.orientation = orientation;

		masonryPane.uuid = uuid;

		return masonryPane;
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
     * @param plugin the plugin that will be the owner of the created items
     * @return the masonry pane
     * @since 0.10.8
     */
    @NotNull
    public static MasonryPane load(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
        if (!element.hasAttribute("length")) {
            throw new XMLLoadException("Masonry pane XML tag does not have the mandatory length attribute");
        }

        if (!element.hasAttribute("height")) {
            throw new XMLLoadException("Masonry pane XML tag does not have the mandatory height attribute");
        }

        int length;
        int height;

        try {
            length = Integer.parseInt(element.getAttribute("length"));
        } catch (NumberFormatException exception) {
            throw new XMLLoadException("Length attribute is not an integer", exception);
        }

        try {
            height = Integer.parseInt(element.getAttribute("height"));
        } catch (NumberFormatException exception) {
            throw new XMLLoadException("Height attribute is not an integer", exception);
        }

        MasonryPane masonryPane = new MasonryPane(length, height);

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

            masonryPane.addPane(Gui.loadPane(instance, pane, plugin));
        }

        return masonryPane;
    }
}

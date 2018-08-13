package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.GuiLocation;
import com.github.stefvanschie.inventoryframework.pane.util.Pane;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A pane for panes that should be spread out over multiple pages
 */
public class PaginatedPane extends Pane {

    /**
     * A set of panes for the different pages
     */
    private final Map<Integer, List<Pane>> panes = new HashMap<>();

    /**
     * The current page
     */
    private int page;

    /**
     * {@inheritDoc}
     */
    public PaginatedPane(@NotNull GuiLocation start, int length, int height) {
        super(start, length, height);
    }

    /**
     * Returns the current page
     *
     * @return the current page
     */
    public int getPage() {
        return page;
    }

    /**
     * Returns the amount of pages
     *
     * @return the amount of pages
     */
    public int getPages() {
        return panes.size();
    }
    /**
     * Assigns a pane to a selected page
     *
     * @param page the page to assign the pane to
     * @param pane the new pane
     */
    public void addPane(int page, Pane pane) {
        if (!this.panes.containsKey(page))
            this.panes.put(page, new ArrayList<>());

        this.panes.get(page).add(pane);

        this.panes.get(page).sort(Comparator.comparing(Pane::getPriority));
    }

    /**
     * Sets the current displayed page
     *
     * @param page the page
     */
    public void setPage(int page) {
        if (!panes.containsKey(page)) {
            throw new ArrayIndexOutOfBoundsException("page outside range");
        }

        this.page = page;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void display(Inventory inventory, int paneOffsetX, int paneOffsetY, int maxLength, int maxHeight) {
        this.panes.get(page).forEach(pane -> pane.display(inventory, paneOffsetX + start.getX(),
                paneOffsetY + start.getY(), Math.min(length, maxLength), Math.min(height, maxHeight)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean click(@NotNull InventoryClickEvent event, int paneOffsetX, int paneOffsetY, int maxLength,
                         int maxHeight) {
        int length = Math.min(this.length, maxLength);
        int height = Math.min(this.height, maxHeight);

        int slot = event.getSlot();

        int x = (slot % 9) - start.getX();
        int y = (slot / 9) - start.getY();

        if (x < 0 || x > length || y < 0 || y > height)
            return false;

        if (onLocalClick != null)
            onLocalClick.accept(event);

        boolean success = false;

        for (Pane pane : this.panes.get(page))
            success = success || pane.click(event, paneOffsetX + start.getX(),
                    paneOffsetY + start.getY(), length, height);

        return success;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Contract(pure = true)
    @Override
    public Collection<Pane> getPanes() {
        Collection<Pane> panes = new HashSet<>();

        this.panes.forEach((integer, p) -> {
            p.forEach(pane -> panes.addAll(pane.getPanes()));
            panes.addAll(p);
        });

        return panes;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Contract(pure = true)
    @Override
    public Collection<GuiItem> getItems() {
        return getPanes().stream().flatMap(pane -> pane.getItems().stream()).collect(Collectors.toList());
    }

    /**
     * Loads a paginated pane from a given element
     *
     * @param instance the instance class
     * @param element the element
     * @return the paginated pane
     */
    @Nullable
    @Contract("_, null -> fail")
    public static PaginatedPane load(Object instance, @NotNull Element element) {
        try {
            PaginatedPane paginatedPane = new PaginatedPane(new GuiLocation(
                    Integer.parseInt(element.getAttribute("x")),
                    Integer.parseInt(element.getAttribute("y"))),
                    Integer.parseInt(element.getAttribute("length")),
                    Integer.parseInt(element.getAttribute("height"))
            );

            Pane.load(paginatedPane, instance, element);

            if (element.hasAttribute("populate"))
                return paginatedPane;

            int pageCount = 0;

            NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);

                if (item.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                NodeList innerNodes = item.getChildNodes();

                List<Pane> panes = new ArrayList<>();

                for (int j = 0; j < innerNodes.getLength(); j++) {
                    Node pane = innerNodes.item(j);

                    if (pane.getNodeType() != Node.ELEMENT_NODE)
                        continue;

                    panes.add(Gui.loadPane(instance, pane));
                }

                for (Pane pane : panes)
                    paginatedPane.addPane(pageCount, pane);

                pageCount++;
            }

            return paginatedPane;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return null;
    }
}

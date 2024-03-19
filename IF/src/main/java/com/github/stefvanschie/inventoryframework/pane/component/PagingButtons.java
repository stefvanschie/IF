package com.github.stefvanschie.inventoryframework.pane.component;

import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * An interface for interacting with {@link PaginatedPane}s. This gives two buttons for navigating backwards and
 * forwards through the pages of the {@link PaginatedPane}. The backward button will be displayed at (0, 0) of this pane
 * and the forward button will be displayed at (length - 1, 0) of this pane. If the paginated pane is at the first page
 * or the last page, the backwards respectively the forward button will not show. This does not display the
 * {@link PaginatedPane} itself, but is merely an interface for interacting with it.
 * 
 * @since 0.10.14
 */
public class PagingButtons extends Pane {

    /**
     * The paginated pane.
     */
    @NotNull
    private final PaginatedPane pages;

    /**
     * The backwards button.
     */
    @NotNull
    private GuiItem backwardButton;

    /**
     * The forwards button.
     */
    @NotNull
    private GuiItem forwardButton;

    /**
     * The plugin with which the items were created.
     */
    @NotNull
    private final Plugin plugin;

    /**
     * Creates a new PagingButtons instance, which controls the provided {@link PaginatedPane}. The backward and forward
     * item will be an arrow. If the length provided is less than 2, this will throw an
     * {@link IllegalArgumentException}.
     *
     * @param slot the position of this interface
     * @param length the length of this interface
     * @param priority the priority of this interface
     * @param pages the pages to interact with
     * @param plugin the plugin that will be the owner of this interface's items
     * @since 0.10.14
     * @throws IllegalArgumentException if the length is less than 2
     */
    public PagingButtons(
        @NotNull Slot slot,
        int length,
        @NotNull Priority priority,
        @NotNull PaginatedPane pages,
        @NotNull Plugin plugin
    ) {
        super(slot, length, 1, priority);

        if (length < 2) {
            throw new IllegalArgumentException("Length of paging buttons must be at least 2");
        }
        
        this.pages = pages;
        this.plugin = plugin;
        
        this.backwardButton = new GuiItem(new ItemStack(Material.ARROW), plugin);
        this.forwardButton = new GuiItem(new ItemStack(Material.ARROW), plugin);
    }

    /**
     * Creates a new PagingButtons instance, which controls the provided {@link PaginatedPane}. The backward and forward
     * item will be an arrow. If the length provided is less than 2, this will throw an
     * {@link IllegalArgumentException}.
     *
     * @param slot the position of this interface
     * @param length the length of this interface
     * @param priority the priority of this interface
     * @param pages the pages to interact with
     * @since 0.10.14
     * @throws IllegalArgumentException if the length is less than 2
     */
    public PagingButtons(@NotNull Slot slot, int length, @NotNull Priority priority, @NotNull PaginatedPane pages) {
        this(slot, length, priority, pages, JavaPlugin.getProvidingPlugin(PagingButtons.class));
    }

    /**
     * Creates a new PagingButtons instance, which controls the provided {@link PaginatedPane}. The backward and forward
     * item will be an arrow. If the length provided is less than 2, this will throw an
     * {@link IllegalArgumentException}.
     *
     * @param slot the position of this interface
     * @param length the length of this interface
     * @param pages the pages to interact with
     * @param plugin the plugin that will be the owner of this interface's items
     * @since 0.10.14
     * @throws IllegalArgumentException if the length is less than 2
     */
    public PagingButtons(@NotNull Slot slot, int length, @NotNull PaginatedPane pages, @NotNull Plugin plugin) {
        this(slot, length, Priority.NORMAL, pages, plugin);
    }

    /**
     * Creates a new PagingButtons instance, which controls the provided {@link PaginatedPane}. The backward and forward
     * item will be an arrow. If the length provided is less than 2, this will throw an
     * {@link IllegalArgumentException}.
     *
     * @param slot the position of this interface
     * @param length the length of this interface
     * @param pages the pages to interact with
     * @since 0.10.14
     * @throws IllegalArgumentException if the length is less than 2
     */
    public PagingButtons(@NotNull Slot slot, int length, @NotNull PaginatedPane pages) {
        this(slot, length, Priority.NORMAL, pages);
    }

    /**
     * Creates a new PagingButtons instance, which controls the provided {@link PaginatedPane}. The backward and forward
     * item will be an arrow. If the length provided is less than 2, this will throw an
     * {@link IllegalArgumentException}.
     *
     * @param length the length of this interface
     * @param priority the priority of this interface
     * @param pages the pages to interact with
     * @param plugin the plugin that will be the owner of this interface's items
     * @since 0.10.14
     * @throws IllegalArgumentException if the length is less than 2
     */
    public PagingButtons(int length, @NotNull Priority priority, @NotNull PaginatedPane pages, @NotNull Plugin plugin) {
        this(Slot.fromXY(0, 0), length, priority, pages, plugin);
    }

    /**
     * Creates a new PagingButtons instance, which controls the provided {@link PaginatedPane}. The backward and forward
     * item will be an arrow. If the length provided is less than 2, this will throw an
     * {@link IllegalArgumentException}.
     *
     * @param length the length of this interface
     * @param priority the priority of this interface
     * @param pages the pages to interact with
     * @since 0.10.14
     * @throws IllegalArgumentException if the length is less than 2
     */
    public PagingButtons(int length, @NotNull Priority priority, @NotNull PaginatedPane pages) {
        this(Slot.fromXY(0, 0), length, priority, pages, JavaPlugin.getProvidingPlugin(PagingButtons.class));
    }

    /**
     * Creates a new PagingButtons instance, which controls the provided {@link PaginatedPane}. The backward and forward
     * item will be an arrow. If the length provided is less than 2, this will throw an
     * {@link IllegalArgumentException}.
     *
     * @param length the length of this interface
     * @param pages the pages to interact with
     * @param plugin the plugin that will be the owner of this interface's items
     * @since 0.10.14
     * @throws IllegalArgumentException if the length is less than 2
     */
    public PagingButtons(int length, @NotNull PaginatedPane pages, @NotNull Plugin plugin) {
        this(Slot.fromXY(0, 0), length, Priority.NORMAL, pages, plugin);
    }

    /**
     * Creates a new PagingButtons instance, which controls the provided {@link PaginatedPane}. The backward and forward
     * item will be an arrow. If the length provided is less than 2, this will throw an
     * {@link IllegalArgumentException}.
     *
     * @param length the length of this interface
     * @param pages the pages to interact with
     * @since 0.10.14
     * @throws IllegalArgumentException if the length is less than 2
     */
    public PagingButtons(int length, @NotNull PaginatedPane pages) {
        this(Slot.fromXY(0, 0), length, Priority.NORMAL, pages);
    }

    @Override
    public boolean click(
        @NotNull Gui gui,
        @NotNull InventoryComponent inventoryComponent,
        @NotNull InventoryClickEvent event,
        int slot,
        int paneOffsetX,
        int paneOffsetY,
        int maxLength,
        int maxHeight
    ) {
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

        if (matchesItem(this.backwardButton, itemStack)) {
            this.pages.setPage(this.pages.getPage() - 1);

            this.backwardButton.callAction(event);

            gui.update();

            return true;
        }

        if (matchesItem(this.forwardButton, itemStack)) {
            this.pages.setPage(this.pages.getPage() + 1);

            this.forwardButton.callAction(event);

            gui.update();

            return true;
        }

        return false;
    }

    @Override
    public void display(
            @NotNull InventoryComponent inventoryComponent,
            int paneOffsetX,
            int paneOffsetY,
            int maxLength,
            int maxHeight
    ) {
        int length = Math.min(getLength(), maxLength);

        int x = super.slot.getX(length) + paneOffsetX;
        int y = super.slot.getY(length) + paneOffsetY;

        if (this.pages.getPage() > 0) {
            inventoryComponent.setItem(this.backwardButton, x, y);
        }

        if (this.pages.getPage() < this.pages.getPages() - 1) {
            inventoryComponent.setItem(this.forwardButton, x + length - 1, y);
        }
    }

    /**
     * {@inheritDoc}
     *
     * This does not make a copy of the {@link PaginatedPane} that is being controlled by this interface.
     */
    @NotNull
    @Contract(pure = true)
    @Override
    public PagingButtons copy() {
        PagingButtons pagingButtons = new PagingButtons(getSlot(), getLength(), getPriority(), this.pages, this.plugin);

        pagingButtons.setVisible(isVisible());
        pagingButtons.onClick = super.onClick;

        pagingButtons.uuid = super.uuid;

        pagingButtons.backwardButton = this.backwardButton.copy();
        pagingButtons.forwardButton = this.forwardButton.copy();

        return pagingButtons;
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public Collection<GuiItem> getItems() {
        Collection<GuiItem> items = new HashSet<>();

        items.add(this.backwardButton);
        items.add(this.forwardButton);

        return Collections.unmodifiableCollection(items);
    }

    /**
     * Sets the item to be used for navigating backwards. If an event is attached to the item, this event will be called
     * after the page has been changed.
     *
     * @param item the new backward item
     * @since 0.10.14
     */
    public void setBackwardButton(@NotNull GuiItem item) {
        this.backwardButton = item;
    }

    /**
     * Sets the item to be used for navigating forwards. If an event is attached to the item, this event will be called
     * after the page has been changed.
     *
     * @param item the new forward item
     * @since 0.10.14
     */
    public void setForwardButton(@NotNull GuiItem item) {
        this.forwardButton = item;
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public Collection<Pane> getPanes() {
        return Collections.emptySet();
    }

    /**
     * This is a no-op.
     *
     * @since 0.10.14
     */
    @Override
    public void clear() {}

    /**
     * Loads a paging buttons pane from an XML element.
     *
     * @param instance the instance class
     * @param element the element
     * @param plugin the plugin that will be the owner of the underlying items
     * @return the paging buttons pane
     * @since 0.10.14
     */
    @NotNull
    @Contract(pure = true)
    public static PagingButtons load(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
        int length;

        try {
            length = Integer.parseInt(element.getAttribute("length"));
        } catch (NumberFormatException exception) {
            throw new XMLLoadException(exception);
        }

        if (!element.hasAttribute("pages")) {
            throw new XMLLoadException("Paging buttons does not have pages attribute");
        }

        Element paginatedPaneElement = element.getOwnerDocument().getElementById(element.getAttribute("pages"));

        if (paginatedPaneElement == null) {
            throw new XMLLoadException("Paging buttons pages reference is invalid");
        }

        Object paginatedPane = paginatedPaneElement.getUserData("pane");

        if (!(paginatedPane instanceof PaginatedPane)) {
            throw new XMLLoadException("Retrieved data is not a paginated pane");
        }

        PagingButtons pagingButtons = new PagingButtons(length, (PaginatedPane) paginatedPane);

        Pane.load(pagingButtons, instance, element);

        return pagingButtons;
    }
}

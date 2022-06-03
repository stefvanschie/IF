package com.github.stefvanschie.inventoryframework.gui.type;

import com.github.stefvanschie.inventoryframework.abstraction.MerchantInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.StringHolder;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.util.version.Version;
import com.github.stefvanschie.inventoryframework.util.version.VersionMatcher;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a gui in the form of a merchant.
 *
 * @since 0.10.0
 */
public class MerchantGui extends NamedGui {

    /**
     * Represents the inventory component for the input
     */
    @NotNull
    private InventoryComponent inputComponent = new InventoryComponent(2, 1);

    /**
     * Represents the inventory component for the player inventory
     */
    @NotNull
    private InventoryComponent playerInventoryComponent = new InventoryComponent(9, 4);

    /**
     * The merchant holding the trades and inventory
     */
    @NotNull
    private Merchant merchant;

    /**
     * The human entities viewing this gui
     */
    @NotNull
    private final List<HumanEntity> viewers = new ArrayList<>();

    /**
     * The trades of this merchant with their price differences. The differences are the difference between the new
     * price and the original price.
     */
    @NotNull
    private final List<Map.Entry<MerchantRecipe, Integer>> trades = new ArrayList<>();

    /**
     * The experience of this merchant. Values below zero indicate that the experience should be hidden.
     */
    private int experience = -1;

    /**
     * The level of this merchant. A value of zero indicates this villager doesn't have a level.
     */
    private int level = 0;

    /**
     * The internal merchant inventory
     */
    @NotNull
    private final MerchantInventory merchantInventory = VersionMatcher.newMerchantInventory(Version.getVersion());

    /**
     * Creates a merchant gui with the given title.
     *
     * @param title the title
     * @since 0.10.0
     */
    public MerchantGui(@NotNull String title) {
        this(StringHolder.of(title));
    }

    /**
     * Creates a merchant gui with the given title.
     *
     * @param title the title
     * @since 0.10.0
     */
    public MerchantGui(@NotNull TextHolder title) {
        super(title);

        this.merchant = getTitleHolder().asMerchantTitle();
    }

    @Override
    protected void show(@NotNull HumanEntity humanEntity, boolean reopen) {
        if (!(humanEntity instanceof Player)) {
            throw new IllegalArgumentException("Merchants can only be opened by players");
        }

        if (isDirty()) {
            this.merchant = getTitleHolder().asMerchantTitle();
            markChanges();
        }

        InventoryView view = humanEntity.openMerchant(merchant, true);

        if (view == null) {
            throw new IllegalStateException("Merchant could not be opened");
        }

        Inventory inventory = view.getTopInventory();

        addInventory(inventory, this);

        inventory.clear();

        getInputComponent().display(inventory, 0);
        getPlayerInventoryComponent().display();

        if (getPlayerInventoryComponent().hasItem()) {
            getHumanEntityCache().storeAndClear(humanEntity);

            getPlayerInventoryComponent().placeItems(humanEntity.getInventory(), 0);
        }

        this.viewers.add(humanEntity);

        Player player = (Player) humanEntity;

        if (this.experience >= 0 || this.level > 0) {
            this.merchantInventory.sendMerchantOffers(player, this.trades, this.level, this.experience);

            return;
        }

        boolean discount = false;

        for (Map.Entry<MerchantRecipe, Integer> trade : this.trades) {
            if (trade.getValue() != 0) {
                this.merchantInventory.sendMerchantOffers(player, this.trades, this.level, this.experience);

                break;
            }
        }
    }

    @NotNull
    @Override
    public Gui copy() {
        MerchantGui gui = new MerchantGui(getTitleHolder());

        gui.inputComponent = inputComponent.copy();
        gui.playerInventoryComponent = playerInventoryComponent.copy();

        gui.setOnTopClick(this.onTopClick);
        gui.setOnBottomClick(this.onBottomClick);
        gui.setOnGlobalClick(this.onGlobalClick);
        gui.setOnOutsideClick(this.onOutsideClick);
        gui.setOnClose(this.onClose);

        return gui;
    }

    @Override
    public void click(@NotNull InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();

        if (rawSlot >= 0 && rawSlot <= 1) {
            getInputComponent().click(this, event, rawSlot);
        } else if (rawSlot != 2) {
            getPlayerInventoryComponent().click(this, event, rawSlot - 3);
        }
    }

    /**
     * Adds a trade to this gui. The specified discount is the difference between the old price and the new price. For
     * example, if a price was decreased from five to two, the discount would be three.
     *
     * @param recipe the recipe to add
     * @param discount the discount
     * @since 0.10.1
     */
    public void addTrade(@NotNull MerchantRecipe recipe, int discount) {
        this.trades.add(new AbstractMap.SimpleImmutableEntry<>(recipe, -discount));

        List<MerchantRecipe> recipes = new ArrayList<>(this.merchant.getRecipes());

        recipes.add(recipe);

        this.merchant.setRecipes(recipes);
    }

    /**
     * Sets the experience of this merchant gui. Setting the experience will make the experience bar visible, even if
     * the amount of experience is zero. Note that if the level of this merchant gui has not been set via
     * {@link #setLevel(int)} that the experience will always show as zero even when set to something else. Experience
     * must be greater than or equal to zero. Attempting to set the experience to below zero will throw an
     * {@link IllegalArgumentException}.
     *
     * @param experience the experience to set
     * @since 0.10.1
     * @throws IllegalArgumentException when the experience is below zero
     */
    public void setExperience(int experience) {
        if (experience < 0) {
            throw new IllegalArgumentException("Experience must be greater than or equal to zero");
        }

        this.experience = experience;
    }

    /**
     * Sets the level of this merchant gui. This is a value between one and five and will visibly change the gui by
     * appending the level of the villager to the title. These are displayed as "Novice", "Apprentice", "Journeyman",
     * "Expert" and "Master" respectively (when the player's locale is set to English). When an argument is supplied
     * that is not within one and five, an {@link IllegalArgumentException} will be thrown.
     *
     * @param level the numeric level
     * @since 0.10.1
     * @throws IllegalArgumentException when the level is not between one and five
     */
    public void setLevel(int level) {
        if (level < 0 || level > 5) {
            throw new IllegalArgumentException("Level must be between one and five");
        }

        this.level = level;
    }

    /**
     * Adds a trade to this gui. This will not set a discount on the trade. For specifiying discounts, see
     * {@link #addTrade(MerchantRecipe, int)}.
     *
     * @param recipe the recipe to add
     * @since 0.10.0
     */
    public void addTrade(@NotNull MerchantRecipe recipe) {
        addTrade(recipe, 0);
    }

    /**
     * Handles a human entity closing this gui.
     *
     * @param humanEntity the human entity who's closing this gui
     * @since 0.10.0
     */
    public void handleClose(@NotNull HumanEntity humanEntity) {
        this.viewers.remove(humanEntity);
    }

    @Override
    public boolean isPlayerInventoryUsed() {
        return getPlayerInventoryComponent().hasItem();
    }

    @Contract(pure = true)
    @Override
    public int getViewerCount() {
        return this.viewers.size();
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public List<HumanEntity> getViewers() {
        return new ArrayList<>(this.viewers);
    }

    /**
     * Gets the inventory component representing the input
     *
     * @return the input component
     * @since 0.10.0
     */
    @NotNull
    @Contract(pure = true)
    public InventoryComponent getInputComponent() {
        return inputComponent;
    }

    /**
     * Gets the inventory component representing the player inventory
     *
     * @return the player inventory component
     * @since 0.10.0
     */
    @NotNull
    @Contract(pure = true)
    public InventoryComponent getPlayerInventoryComponent() {
        return playerInventoryComponent;
    }

    /**
     * Loads a merchant gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @return the loaded merchant gui
     * @since 0.10.0
     */
    @Nullable
    @Contract(pure = true)
    public static MerchantGui load(@NotNull Object instance, @NotNull InputStream inputStream) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            Element documentElement = document.getDocumentElement();

            documentElement.normalize();

            return load(instance, documentElement);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads a merchant gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @return the loaded merchant gui
     * @since 0.10.0
     */
    @NotNull
    @Contract(pure = true)
    public static MerchantGui load(@NotNull Object instance, @NotNull Element element) {
        if (!element.hasAttribute("title")) {
            throw new XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set");
        }

        MerchantGui merchantGui = new MerchantGui(element.getAttribute("title"));
        merchantGui.initializeOrThrow(instance, element);

        if (element.hasAttribute("populate")) {
            return merchantGui;
        }

        NodeList childNodes = element.getChildNodes();

        for (int index = 0; index < childNodes.getLength(); index++) {
            Node item = childNodes.item(index);

            if (item.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element nestedElement = (Element) item;
            String tagName = nestedElement.getTagName();

            if (tagName.equalsIgnoreCase("component")) {
                if (!nestedElement.hasAttribute("name")) {
                    throw new XMLLoadException("Component tag does not have a name specified");
                }

                InventoryComponent component;

                switch (nestedElement.getAttribute("name")) {
                    case "input":
                        component = merchantGui.getInputComponent();
                        break;
                    case "player-inventory":
                        component = merchantGui.getPlayerInventoryComponent();
                        break;
                    default:
                        throw new XMLLoadException("Unknown component name");
                }

                component.load(instance, nestedElement);
            } else if (tagName.equalsIgnoreCase("trade")) {
                NodeList tradeNodes = nestedElement.getChildNodes();

                List<ItemStack> ingredients = new ArrayList<>(2);
                ItemStack result = null;

                for (int tradeIndex = 0; tradeIndex < tradeNodes.getLength(); tradeIndex++) {
                    Node tradeNode = tradeNodes.item(tradeIndex);

                    if (tradeNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    Element tradeElement = (Element) tradeNode;

                    if (tradeElement.getTagName().equalsIgnoreCase("ingredient")) {
                        if (ingredients.size() >= 2) {
                            throw new XMLLoadException("Too many ingredients specified, must be no more than two");
                        }

                        NodeList ingredientNodes = tradeElement.getChildNodes();

                        for (int ingredientIndex = 0; ingredientIndex < ingredientNodes.getLength(); ingredientIndex++) {
                            Node ingredientNode = ingredientNodes.item(ingredientIndex);

                            if (ingredientNode.getNodeType() != Node.ELEMENT_NODE) {
                                continue;
                            }

                            ingredients.add(Pane.loadItem(instance, (Element) ingredientNode).getItem());
                        }
                    } else if (tradeElement.getTagName().equalsIgnoreCase("result")) {
                        NodeList resultNodes = tradeElement.getChildNodes();

                        for (int resultIndex = 0; resultIndex < resultNodes.getLength(); resultIndex++) {
                            Node resultNode = resultNodes.item(resultIndex);

                            if (resultNode.getNodeType() != Node.ELEMENT_NODE) {
                                continue;
                            }

                            if (result != null) {
                                throw new XMLLoadException("Multiple results specified for the same trade");
                            }

                            result = Pane.loadItem(instance, (Element) resultNode).getItem();
                        }
                    } else {
                        throw new XMLLoadException("Trade element is neither an ingredient nor a result");
                    }
                }

                if (result == null) {
                    throw new XMLLoadException("Trade must have a result specified");
                }

                if (ingredients.size() < 1) {
                    throw new XMLLoadException("Trade must have at least one ingredient");
                }

                MerchantRecipe recipe = new MerchantRecipe(result, Integer.MAX_VALUE);

                recipe.setIngredients(ingredients);

                merchantGui.addTrade(recipe);
            } else {
                throw new XMLLoadException("Nested element is neither a component nor a trade");
            }
        }

        return merchantGui;
    }
}

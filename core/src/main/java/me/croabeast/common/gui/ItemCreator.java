package me.croabeast.common.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import me.croabeast.common.util.ArrayUtils;
import me.croabeast.prismatic.PrismaticAPI;
import me.croabeast.takion.TakionLib;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A fluent builder for creating and customizing {@link ItemStack} instances,
 * and converting them into {@link GuiItem} objects for GUI implementations.
 * <p>
 * The {@code ItemCreator} provides methods to modify the underlying item,
 * adjust its metadata (such as display name and lore), and attach click actions.
 * This utility simplifies item customization and integrates with external APIs
 * (e.g., {@link PrismaticAPI} for colorization) to produce visually appealing GUI items.
 * </p>
 * <p>
 * <strong>Example usage:</strong>
 * <pre><code>
 * // Create an item with a custom name and lore, and set a click action
 * GuiItem guiItem = ItemCreator.of(Material.DIAMOND)
 *     .modifyName("&bShiny Diamond")
 *     .modifyLore("&7This is a very shiny diamond.", "&eRight-click to use!")
 *     .setAction(event -&gt; {
 *         event.setCancelled(true);
 *         // Additional click handling code...
 *     })
 *     .create();
 * </code></pre>
 * </p>
 */
public final class ItemCreator {

    private Consumer<InventoryClickEvent> consumer;
    private final ItemStack item;

    /**
     * Private constructor that initializes the {@code ItemCreator} with the specified {@link ItemStack}.
     *
     * @param stack the item stack to customize (must not be {@code null})
     */
    private ItemCreator(ItemStack stack) {
        item = Objects.requireNonNull(stack);
    }

    /**
     * Applies a modification to the underlying {@link ItemStack}.
     * <p>
     * This method accepts a {@link Consumer} that performs operations on the item.
     * </p>
     *
     * @param consumer the consumer that modifies the item stack
     * @return this {@code ItemCreator} instance for fluent chaining
     */
    public ItemCreator modifyItem(Consumer<ItemStack> consumer) {
        Objects.requireNonNull(consumer).accept(item);
        return this;
    }

    /**
     * Sets the click action to be performed when the item is clicked in a GUI.
     *
     * @param consumer the click event consumer (must not be {@code null})
     * @return this {@code ItemCreator} instance for fluent chaining
     */
    public ItemCreator setAction(Consumer<InventoryClickEvent> consumer) {
        this.consumer = Objects.requireNonNull(consumer);
        return this;
    }

    /**
     * Sets a default action for the item that cancels any click event.
     * <p>
     * This is useful when you want the item to be non-interactive.
     * </p>
     *
     * @return this {@code ItemCreator} instance for fluent chaining
     */
    public ItemCreator setActionToEmpty() {
        return setAction(e -> e.setCancelled(true));
    }

    /**
     * Applies modifications to the {@link ItemMeta} of the underlying item.
     * <p>
     * The provided consumer will be executed on the current {@code ItemMeta},
     * and the updated meta will be set back to the item.
     * </p>
     *
     * @param consumer the consumer that modifies the item meta (must not be {@code null})
     * @return this {@code ItemCreator} instance for fluent chaining
     */
    public ItemCreator modifyMeta(Consumer<ItemMeta> consumer) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Objects.requireNonNull(consumer).accept(meta);
            item.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Sets the display name of the item.
     * <p>
     * The provided name is colorized using {@link PrismaticAPI#colorize(String)}.
     * </p>
     *
     * @param name the new display name for the item
     * @return this {@code ItemCreator} instance for fluent chaining
     */
    public ItemCreator modifyName(String name) {
        return modifyMeta(m -> m.setDisplayName(PrismaticAPI.colorize(name)));
    }

    /**
     * Sets the lore (description) of the item using a list of strings.
     * <p>
     * Each line of the lore is processed and colorized via {@link PrismaticAPI#colorize(String)}.
     * </p>
     *
     * @param lore a list of strings representing the item's lore
     * @return this {@code ItemCreator} instance for fluent chaining
     */
    public ItemCreator modifyLore(List<String> lore) {
        lore.replaceAll(PrismaticAPI::colorize);
        return modifyMeta(m -> m.setLore(lore));
    }

    /**
     * Sets the lore (description) of the item using an array of strings.
     *
     * @param lore an array of strings representing the item's lore
     * @return this {@code ItemCreator} instance for fluent chaining
     */
    public ItemCreator modifyLore(String... lore) {
        return modifyLore(ArrayUtils.toList(lore));
    }

    /**
     * Finalizes the creation of the GUI item.
     * <p>
     * This method wraps the customized {@link ItemStack} in a {@link GuiItem} and assigns the configured
     * click action (if any) to it.
     * </p>
     *
     * @return the constructed {@link GuiItem}
     */
    public GuiItem create() {
        GuiItem guiItem = new GuiItem(this.item, TakionLib.getLib().getPlugin());
        if (consumer != null)
            guiItem.setAction(consumer);
        return guiItem;
    }

    /**
     * Creates a new {@code ItemCreator} for the given {@link ItemStack}.
     *
     * @param stack the item stack to customize
     * @return a new {@code ItemCreator} instance
     */
    public static ItemCreator of(ItemStack stack) {
        return new ItemCreator(stack);
    }

    /**
     * Creates a new {@code ItemCreator} for the specified {@link Material}.
     * <p>
     * A new {@code ItemStack} is created from the material.
     * </p>
     *
     * @param material the material to create the item stack from (must not be {@code null})
     * @return a new {@code ItemCreator} instance
     */
    public static ItemCreator of(Material material) {
        return of(new ItemStack(Objects.requireNonNull(material)));
    }
}

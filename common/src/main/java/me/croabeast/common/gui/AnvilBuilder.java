package me.croabeast.common.gui;

import com.github.stefvanschie.inventoryframework.exception.UnsupportedVersionException;
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.croabeast.common.CommonServices;
import me.croabeast.prismatic.PrismaticAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A builder for a single-line text input backed by an anvil GUI.
 * <p>
 * The input slot shows the current value, and clicking the result slot passes the typed text
 * to {@link #setOnConfirm(BiConsumer)}. When InventoryFramework cannot open an anvil on this
 * server version, {@link #setOnUnsupported(Consumer)} runs instead, so callers can fall back
 * to another input method such as chat.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre><code>
 * AnvilBuilder.of(plugin, "&amp;8Permission")
 *         .setInput(current)
 *         .setOnConfirm((player, text) -&gt; save(text))
 *         .setOnUnsupported(player -&gt; startChatEditor(player))
 *         .showGui(player);
 * </code></pre>
 * </p>
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnvilBuilder {

    private final Plugin plugin;
    private final String title;

    /**
     * The current value shown on the input item.
     */
    @Setter
    @Accessors(chain = true)
    private String input = "";

    /**
     * Called with the player and the typed text when the result slot is clicked.
     */
    @Setter
    @Accessors(chain = true)
    private BiConsumer<Player, String> onConfirm;

    /**
     * Called instead of opening the anvil when this server version is not supported.
     */
    @Setter
    @Accessors(chain = true)
    private Consumer<Player> onUnsupported;

    /**
     * Opens the anvil for the given player, or runs the unsupported callback if it cannot be opened.
     *
     * @param player the player to show the anvil to
     */
    public void showGui(Player player) {
        try {
            AnvilGui gui = new AnvilGui(PrismaticAPI.colorize(title), plugin);
            gui.setCost((short) 0);

            OutlinePane inputPane = new OutlinePane(1, 1);
            inputPane.addItem(ItemCreator.of(Material.PAPER)
                    .modifyName("&f" + (StringUtils.isBlank(input) ? "<empty>" : input))
                    .create(plugin));
            gui.getFirstItemComponent().addPane(Slot.fromXY(0, 0), inputPane);

            OutlinePane resultPane = new OutlinePane(1, 1);
            resultPane.addItem(ItemCreator.of(Material.LIME_STAINED_GLASS_PANE)
                    .modifyName("&a&lSave")
                    .modifyLore("&7Click to save the value.")
                    .setAction(click -> {
                        click.setCancelled(true);
                        if (onConfirm != null) onConfirm.accept(player, gui.getRenameText());
                    })
                    .create(plugin));
            gui.getResultComponent().addPane(Slot.fromXY(0, 0), resultPane);

            gui.show(player);
        } catch (UnsupportedVersionException | LinkageError e) {
            if (onUnsupported != null) onUnsupported.accept(player);
        }
    }

    /**
     * Creates a new {@code AnvilBuilder} for the given plugin and title.
     *
     * @param plugin the plugin owning this GUI
     * @param title  the title shown in the anvil GUI
     * @return a new {@code AnvilBuilder} instance
     */
    @NotNull
    public static AnvilBuilder of(Plugin plugin, String title) {
        return new AnvilBuilder(Objects.requireNonNull(plugin), Objects.requireNonNull(title));
    }

    /**
     * Creates a new {@code AnvilBuilder} using the common plugin from {@link CommonServices}.
     *
     * @param title the title shown in the anvil GUI
     * @return a new {@code AnvilBuilder} instance
     */
    @NotNull
    public static AnvilBuilder of(String title) {
        return of(CommonServices.getPlugin(), title);
    }
}

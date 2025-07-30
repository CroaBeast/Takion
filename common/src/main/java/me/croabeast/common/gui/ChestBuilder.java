package me.croabeast.common.gui;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import me.croabeast.prismatic.PrismaticAPI;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A builder for creating and managing a chest GUI with paginated panes.
 * <p>
 * The {@code ChestBuilder} provides a fluent API to configure a chest GUI by setting the number of rows,
 * the display name, and the currently visible page. It extends {@link GuiBuilder} and uses a {@link PaginatedPane}
 * to manage multiple pages of content. The builder also supports showing the GUI to a human entity.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre><code>
 * ChestBuilder chest = new ChestBuilder(4, "&aMy Chest GUI");
 * chest.setDisplayedPage(2).showGui(player);
 * </code></pre>
 * </p>
 *
 * @see ChestGui
 * @see GuiBuilder
 */
public final class ChestBuilder extends GuiBuilder<ChestGui, ChestBuilder> {

    private boolean loaded = false;

    /**
     * Constructs a new {@code ChestBuilder} with the specified number of rows and GUI title.
     * <p>
     * The title is colorized using {@link PrismaticAPI#colorize(String)}.
     * </p>
     *
     * @param rows the number of rows in the chest GUI
     * @param name the display name of the GUI
     */
    ChestBuilder(int rows, String name) {
        super(new PaginatedPane(0, 0, 9, rows), new ChestGui(rows, PrismaticAPI.colorize(name)));
    }

    /**
     * Sets the displayed page for the chest GUI.
     * <p>
     * If a valid row count is provided (greater than 0), the pane's height and the GUI's row count
     * are updated accordingly. Then, the specified page is set and the GUI is refreshed.
     * </p>
     *
     * @param rows  the number of rows to display (or -1 to use the default)
     * @param index the index of the page to display
     */
    @Override
    public void setDisplayedPage(int rows, int index) {
        if (rows > 0) {
            pane.setHeight(rows);
            value.setRows(rows);
        }
        pane.setPage(index);
        value.update();
    }

    /**
     * Displays the chest GUI to the specified human entity.
     * <p>
     * The GUI is added only once. On first display, the builder adds the paginated pane to the GUI,
     * and subsequent calls simply show the GUI.
     * </p>
     *
     * @param entity the human entity (typically a player) to show the GUI to
     */
    @Override
    public void showGui(HumanEntity entity) {
        if (!loaded) {
            value.addPane(pane);
            loaded = true;
        }
        value.show(entity);
    }

    /**
     * Returns this instance of {@code ChestBuilder} for fluent chaining.
     *
     * @return this builder instance
     */
    @NotNull
    public ChestBuilder instance() {
        return this;
    }

    @NotNull
    public static ChestBuilder of(int rows, String name) {
        return new ChestBuilder(rows, Objects.requireNonNull(name));
    }
}

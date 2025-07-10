package me.croabeast.common.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.croabeast.common.builder.Builder;
import me.croabeast.common.util.ArrayUtils;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * An abstract builder for creating and managing GUIs with paginated panes.
 * <p>
 * The {@code GuiBuilder} provides a fluent API to build complex GUIs by allowing the addition
 * of panes and items in a paginated layout. It implements {@link Builder} to allow chainable
 * modifications to the underlying GUI value.
 * </p>
 * <p>
 * Typical usage involves adding panes, adding individual items to specific panes,
 * and displaying the GUI to a human entity.
 * </p>
 *
 * @param <G> the type of GUI that this builder creates
 * @param <B> the concrete type of this GUI builder for fluent chaining
 * @see Builder
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class GuiBuilder<G extends Gui, B extends GuiBuilder<G, B>> implements Builder<G, B> {

    /**
     * The paginated pane that serves as the container for all added panes.
     */
    protected final PaginatedPane pane;

    /**
     * The underlying GUI object managed by this builder.
     */
    @Getter
    protected final G value;

    /**
     * Adds a pane to the paginated pane at the specified index.
     * <p>
     * Optional consumers can be provided to further configure the pane before it is added.
     * </p>
     *
     * @param index     the index at which to add the pane
     * @param pane      the pane to add
     * @param consumers optional consumers to modify the pane
     * @param <P>       the type of the pane being added
     * @return this builder instance for fluent chaining
     * @throws NullPointerException if the provided pane is {@code null}
     */
    @SafeVarargs
    public final <P extends Pane> B addPane(int index, P pane, Consumer<P>... consumers) {
        Objects.requireNonNull(pane);
        for (Consumer<P> c : ArrayUtils.toList(consumers))
            if (c != null) c.accept(pane);
        this.pane.addPane(index, pane);
        return instance();
    }

    /**
     * Adds a single GUI item to a newly created {@link OutlinePane} at the specified position
     * and adds it to the paginated pane at the given index.
     * <p>
     * Optional consumers can be provided to further configure the outline pane.
     * </p>
     *
     * @param index     the index at which to add the pane containing the item
     * @param x         the x-coordinate within the pane where the item will be placed
     * @param y         the y-coordinate within the pane where the item will be placed
     * @param item      the GUI item to add
     * @param consumers optional consumers to modify the outline pane
     * @return this builder instance for fluent chaining
     */
    @SafeVarargs
    public final B addSingleItem(int index, int x, int y, GuiItem item, Consumer<OutlinePane>... consumers) {
        OutlinePane pane = new OutlinePane(x, y, 1, 1);
        for (Consumer<OutlinePane> c : ArrayUtils.toList(consumers))
            if (c != null) c.accept(pane);
        pane.addItem(item);
        return addPane(index, pane);
    }

    /**
     * Retrieves all panes at the specified index within the paginated pane.
     *
     * @param index the index from which to retrieve panes
     * @return a collection of panes at the given index
     */
    @NotNull
    public final Collection<Pane> getPanes(int index) {
        return pane.getPanes(index);
    }

    /**
     * Sets the displayed page for the GUI based on the number of rows and the page index.
     * <p>
     * This abstract method should be implemented to adjust the GUI display according to the specific layout.
     * </p>
     *
     * @param rows  the number of rows in the display (or -1 to use a default setting)
     * @param index the index of the page to display
     */
    public abstract void setDisplayedPage(int rows, int index);

    /**
     * Sets the displayed page for the GUI using the provided page index and default row settings.
     *
     * @param index the index of the page to display
     */
    public void setDisplayedPage(int index) {
        setDisplayedPage(-1, index);
    }

    /**
     * Displays the constructed GUI to the specified human entity.
     * <p>
     * This abstract method should be implemented to handle the specifics of how the GUI is shown to the player,
     * including any necessary event handling or animations.
     * </p>
     *
     * @param entity the human entity (typically a player) to display the GUI to
     */
    public abstract void showGui(HumanEntity entity);
}

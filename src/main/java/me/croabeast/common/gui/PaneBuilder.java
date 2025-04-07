package me.croabeast.common.gui;

import com.github.stefvanschie.inventoryframework.pane.Pane;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.croabeast.common.builder.Builder;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An abstract builder for constructing and configuring {@link Pane} objects in a fluent manner.
 * <p>
 * The {@code PaneBuilder} simplifies the process of setting up and modifying panes used in GUI frameworks,
 * allowing the registration of click actions and other pane-specific configurations. It implements the {@link Builder}
 * interface to provide a fluent API for chaining modifications to the underlying pane.
 * </p>
 * <p>
 * Example usage:
 * <pre><code>
 * Pane myPane = ...; // obtain or create a pane instance
 * PaneBuilder&lt;Pane, ?&gt; builder = MyPaneBuilder.of(myPane);
 * builder.setAction(event -&gt; {
 *     // Handle click event
 * }).getValue(); // returns the configured pane
 * </code></pre>
 * </p>
 *
 * @param <P> the type of the pane being built
 * @param <B> the concrete type of this builder for fluent method chaining
 * @see Builder
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class PaneBuilder<P extends Pane, B extends PaneBuilder<P, B>> implements Builder<P, B> {

    /**
     * The underlying pane instance that is being built and configured.
     */
    protected final P value;

    /**
     * Sets the action to be executed when the pane is clicked.
     * <p>
     * The provided consumer will be registered as the click handler for the pane.
     * </p>
     *
     * @param consumer the click event consumer to set
     * @return the current builder instance for fluent chaining
     */
    @NotNull
    public B setAction(Consumer<InventoryClickEvent> consumer) {
        value.setOnClick(consumer);
        return instance();
    }

    /**
     * Sets the click action for the pane using a function that maps the pane to a click event consumer.
     * <p>
     * This overload allows for dynamic determination of the click action based on the current state of the pane.
     * </p>
     *
     * @param function a function that takes the pane and returns a click event consumer
     * @return the current builder instance for fluent chaining
     */
    @NotNull
    public B setAction(Function<P, Consumer<InventoryClickEvent>> function) {
        return setAction(function.apply(value));
    }

    /**
     * Compares this builder's underlying pane with another pane by comparing their UUIDs.
     *
     * @param pane the pane to compare with
     * @return {@code true} if both panes have the same UUID; {@code false} otherwise
     */
    public boolean compare(P pane) {
        return Objects.equals(this.value.getUUID(), pane.getUUID());
    }
}

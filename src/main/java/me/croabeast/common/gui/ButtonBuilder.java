package me.croabeast.common.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.component.ToggleButton;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import org.jetbrains.annotations.NotNull;

/**
 * A builder for creating and configuring a toggle button in a GUI.
 * <p>
 * The {@code ButtonBuilder} simplifies the process of creating a toggle button using an
 * {@link ToggleButton} wrapped in a {@link PaneBuilder}. It provides methods to set the enabled
 * and disabled items, as well as convenient factory methods to create a new builder instance from
 * a {@link Slot} or by x and y coordinates.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre><code>
 * ButtonBuilder button = ButtonBuilder.of(Slot.fromXY(3, 2), true)
 *     .setItem(new GuiItem(myItemStack), true);
 * </code></pre>
 * </p>
 *
 * @see ToggleButton
 * @see PaneBuilder
 */
public final class ButtonBuilder extends PaneBuilder<ToggleButton, ButtonBuilder> {

    /**
     * Constructs a new {@code ButtonBuilder} with the given slot and initial toggle value.
     *
     * @param slot  the slot position for the button
     * @param value the initial state of the toggle button (true for enabled, false for disabled)
     */
    ButtonBuilder(Slot slot, boolean value) {
        super(new ToggleButton(slot, 1, 1, value));
    }

    /**
     * Sets the GUI item for the button.
     * <p>
     * If {@code isEnabled} is {@code true}, the provided item is set as the enabled item;
     * otherwise, it is set as the disabled item.
     * </p>
     *
     * @param item      the {@link GuiItem} to set
     * @param isEnabled flag indicating whether to set the item as enabled or disabled
     * @return this {@code ButtonBuilder} instance for chaining
     */
    @NotNull
    public ButtonBuilder setItem(GuiItem item, boolean isEnabled) {
        if (isEnabled) {
            value.setEnabledItem(item);
            return this;
        }
        value.setDisabledItem(item);
        return this;
    }

    /**
     * Returns this instance of {@code ButtonBuilder}.
     *
     * @return this builder instance for fluent chaining
     */
    @NotNull
    public ButtonBuilder instance() {
        return this;
    }

    /**
     * Creates a new {@code ButtonBuilder} using the specified slot and initial value.
     *
     * @param slot  the slot where the button will be placed
     * @param value the initial toggle state
     * @return a new {@code ButtonBuilder} instance
     */
    public static ButtonBuilder of(Slot slot, boolean value) {
        return new ButtonBuilder(slot, value);
    }

    /**
     * Creates a new {@code ButtonBuilder} using the specified x and y coordinates and initial value.
     *
     * @param x     the x-coordinate of the slot
     * @param y     the y-coordinate of the slot
     * @param value the initial toggle state
     * @return a new {@code ButtonBuilder} instance
     */
    public static ButtonBuilder of(int x, int y, boolean value) {
        return of(Slot.fromXY(x, y), value);
    }
}

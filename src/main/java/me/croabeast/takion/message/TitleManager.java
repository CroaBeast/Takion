package me.croabeast.takion.message;

import lombok.Setter;
import lombok.experimental.Accessors;
import me.croabeast.common.util.Exceptions;
import org.bukkit.entity.Player;

/**
 * Manages the timing and display of titles for players.
 * <p>
 * A {@code TitleManager} controls the timing parameters (fade in, stay, and fade out ticks)
 * for title messages displayed to players. It provides methods to get and set these tick values,
 * as well as a builder pattern to conveniently construct and send title messages.
 * </p>
 * <p>
 * The built-in {@link Builder} class allows for fluent customization of title and subtitle messages,
 * including the configuration of timing and the dispatch of the title to players.
 * </p>
 *
 * @see Player#sendTitle(String, String, int, int, int)
 */
public interface TitleManager {

    /**
     * Gets the number of ticks for the title fade-in animation.
     *
     * @return the fade-in ticks
     */
    int getFadeInTicks();

    /**
     * Gets the number of ticks that the title remains on screen.
     *
     * @return the stay ticks
     */
    int getStayTicks();

    /**
     * Gets the number of ticks for the title fade-out animation.
     *
     * @return the fade-out ticks
     */
    int getFadeOutTicks();

    /**
     * Sets the number of ticks for the title fade-in animation.
     *
     * @param fadeIn the fade-in ticks (must be &ge; 0)
     */
    void setFadeInTicks(int fadeIn);

    /**
     * Sets the number of ticks that the title remains on screen.
     *
     * @param stay the stay ticks (must be &gt; 0)
     */
    void setStayTicks(int stay);

    /**
     * Sets the number of ticks for the title fade-out animation.
     *
     * @param fadeOut the fade-out ticks (must be &ge; 0)
     */
    void setFadeOutTicks(int fadeOut);

    /**
     * Sets the fade in, stay, and fade out ticks for the title simultaneously.
     * <p>
     * Each parameter is validated: fade-in and fade-out must be non-negative, and stay must be positive.
     * Invalid values are ignored.
     * </p>
     *
     * @param fadeIn  the fade-in ticks
     * @param stay    the stay ticks
     * @param fadeOut the fade-out ticks
     */
    default void setTicks(int fadeIn, int stay, int fadeOut) {
        try {
            setFadeInTicks(Exceptions.validate(i -> i >= 0, fadeIn));
        } catch (Exception ignored) {}
        try {
            setStayTicks(Exceptions.validate(i -> i > 0, stay));
        } catch (Exception ignored) {}
        try {
            setFadeOutTicks(Exceptions.validate(i -> i >= 0, fadeOut));
        } catch (Exception ignored) {}
    }

    /**
     * Creates a new {@link Builder} for constructing a title message with the specified title and subtitle.
     * <p>
     * The builder is pre-populated with the current tick settings from this manager.
     * </p>
     *
     * @param title    the main title text
     * @param subtitle the subtitle text
     * @return a new {@link Builder} instance for creating and sending the title
     */
    default Builder builder(String title, String subtitle) {
        return new Builder(this, title, subtitle);
    }

    /**
     * Creates a new {@link Builder} for constructing a title message using a single combined message.
     * <p>
     * The interpretation of a single message is implementation-dependent.
     * </p>
     *
     * @param message the combined title message
     * @return a new {@link Builder} instance
     */
    Builder builder(String message);

    /**
     * A builder class for configuring and sending title messages to players.
     * <p>
     * The {@code Builder} allows you to fluently set the title, subtitle, and timing parameters
     * (fade in, stay, fade out) for a title display. It then provides a method to send the configured
     * title to a specified player.
     * </p>
     *
     * @see Player#sendTitle(String, String, int, int, int)
     */
    @Accessors(chain = true)
    @Setter
    class Builder {

        private final String title;
        private final String subtitle;
        private int fadeIn;
        private int stay;
        private int fadeOut;

        /**
         * Constructs a new {@code Builder} using the tick settings from the provided {@link TitleManager}.
         *
         * @param manager  the {@code TitleManager} supplying default tick values
         * @param title    the title text to display
         * @param subtitle the subtitle text to display
         */
        private Builder(TitleManager manager, String title, String subtitle) {
            fadeIn = manager.getFadeInTicks();
            stay = manager.getStayTicks();
            fadeOut = manager.getFadeOutTicks();
            this.title = title;
            this.subtitle = subtitle;
        }

        /**
         * Sets the fade in, stay, and fade out ticks for the title.
         * <p>
         * Each value is validated: fade in and fade out must be non-negative, and stay must be positive.
         * </p>
         *
         * @param fadeIn  the fade in ticks
         * @param stay    the stay ticks
         * @param fadeOut the fade out ticks
         * @return this builder instance for method chaining
         */
        public Builder setTicks(int fadeIn, int stay, int fadeOut) {
            try {
                this.fadeIn = Exceptions.validate(i -> i >= 0, fadeIn);
            } catch (Exception ignored) {}
            try {
                this.stay = Exceptions.validate(i -> i > 0, stay);
            } catch (Exception ignored) {}
            try {
                this.fadeOut = Exceptions.validate(i -> i >= 0, fadeOut);
            } catch (Exception ignored) {}
            return this;
        }

        /**
         * Sends the title message to the specified player.
         * <p>
         * If the server version supports it (version 11.0 or higher), the native {@code sendTitle} method is used.
         * Otherwise, legacy packet-based title sending is attempted via reflection.
         * </p>
         *
         * @param player the target player to receive the title; must not be {@code null}
         * @return {@code true} if the title was sent successfully; {@code false} otherwise
         */
        public boolean send(Player player) {
            if (player == null) return false;

            if (ReflectUtils.VERSION >= 11.0) {
                player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
                return true;
            }

            try {
                ReflectUtils.sendPacket(player, ReflectUtils.TIMES_PACKET_INSTANCE.apply(fadeIn, stay, fadeOut));
                ReflectUtils.sendPacket(player, ReflectUtils.LEGACY_PACKET_INSTANCE.apply(true, title));
                ReflectUtils.sendPacket(player, ReflectUtils.LEGACY_PACKET_INSTANCE.apply(false, subtitle));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}

package me.croabeast.takion.channel;

import org.jetbrains.annotations.NotNull;

/**
 * Provides methods to manage and identify communication channels based on delimiters.
 * <p>
 * A {@code ChannelManager} allows you to set and retrieve the start and end delimiters that define
 * a channel within a message, as well as identify the appropriate channel based on an input string.
 * These delimiters can be used to parse and segregate parts of a message for custom formatting or routing.
 * </p>
 *
 * @see Channel
 */
public interface ChannelManager {

    /**
     * Retrieves the start delimiter used to identify a channel in a message.
     *
     * @return the start delimiter as a {@link String}
     */
    @NotNull
    String getStartDelimiter();

    /**
     * Sets the start delimiter to be used for channel identification.
     *
     * @param delimiter the new start delimiter (must not be {@code null})
     */
    void setStartDelimiter(@NotNull String delimiter);

    /**
     * Retrieves the end delimiter used to identify a channel in a message.
     *
     * @return the end delimiter as a {@link String}
     */
    @NotNull
    String getEndDelimiter();

    /**
     * Sets the end delimiter to be used for channel identification.
     *
     * @param delimiter the new end delimiter (must not be {@code null})
     */
    void setEndDelimiter(@NotNull String delimiter);

    /**
     * Identifies and returns the appropriate {@link Channel} based on the provided string.
     * <p>
     * The method uses the configured start and end delimiters to parse the input and determine
     * which channel the message belongs to.
     * </p>
     *
     * @param string the input string from which to identify the channel (must not be {@code null})
     * @return the identified {@link Channel}
     */
    @NotNull
    Channel identify(@NotNull String string);
}

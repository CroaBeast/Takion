package me.croabeast.common.discord;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Discord embed message.
 * <p>
 * The {@code EmbedObject} class provides a fluent API for building rich embed messages,
 * including support for setting the title, description, URL, images, footer, color, and author.
 * It also supports dynamic token replacement within the text fields.
 * </p>
 * <p>
 * Example usage:
 * <pre><code>
 * EmbedObject embed = new EmbedObject("{token}", "Hello, world!")
 *      .setTitle("My Plugin Title")
 *      .setDescription("This is a description with {token} replaced.")
 *      .setUrl("https://example.com")
 *      .setThumbnail("https://example.com/thumbnail.png")
 *      .setImage("https://example.com/image.png")
 *      .setFooter("Footer Text", "https://example.com/footer_icon.png")
 *      .setColor("#FFAA00")
 *      .setAuthor("Author Name", "https://example.com", "https://example.com/author_icon.png");
 *
 * // Add a field to the embed
 * embed.addField("Field Name", "Field Value", true);
 * </code></pre>
 * </p>
 *
 * @since 1.1
 */
@Getter
public class EmbedObject {

    /**
     * The list of fields contained in the embed.
     */
    private final List<Field> fields = new ArrayList<>();

    /**
     * The token to be replaced in text fields.
     */
    @Getter(AccessLevel.PRIVATE)
    private final String token;

    /**
     * The message value that will replace the token.
     */
    @Getter(AccessLevel.PRIVATE)
    private final String message;

    private String title, description, url, image, thumbnail, footerText, footerIcon;

    /**
     * The color of the embed.
     */
    private Color color;

    /**
     * The author of the embed.
     */
    private Author author;

    /**
     * Constructs a new {@code EmbedObject} with the specified token and message.
     *
     * @param token   the token to be replaced in the embed text (must not be {@code null})
     * @param message the message that replaces the token (must not be {@code null})
     */
    public EmbedObject(String token, String message) {
        this.token = token;
        this.message = message;
    }

    /**
     * Replaces occurrences of the token in the given string with the message.
     *
     * @param string the input string
     * @return the resulting string after replacement; if the input is blank, it is returned unchanged.
     */
    @NotNull
    private String replace(String string) {
        if (StringUtils.isBlank(string)) {
            return string;
        }
        // Both token and message must be non-null; if not, return the original string.
        if (token == null || message == null) {
            return string;
        }
        return string.replace(token, message);
    }

    /**
     * Sets the title of the embed.
     *
     * @param text the title text
     * @return this {@code EmbedObject} instance for chaining
     */
    public EmbedObject setTitle(String text) {
        this.title = replace(text);
        return this;
    }

    /**
     * Sets the description of the embed.
     *
     * @param text the description text
     * @return this {@code EmbedObject} instance for chaining
     */
    public EmbedObject setDescription(String text) {
        this.description = replace(text);
        return this;
    }

    /**
     * Sets the URL of the embed.
     *
     * @param text the URL as a string
     * @return this {@code EmbedObject} instance for chaining
     */
    public EmbedObject setUrl(String text) {
        this.url = replace(text);
        return this;
    }

    /**
     * Sets the thumbnail URL for the embed.
     *
     * @param url the thumbnail URL
     * @return this {@code EmbedObject} instance for chaining
     */
    public EmbedObject setThumbnail(String url) {
        this.thumbnail = replace(url);
        return this;
    }

    /**
     * Sets the main image URL for the embed.
     *
     * @param url the image URL
     * @return this {@code EmbedObject} instance for chaining
     */
    public EmbedObject setImage(String url) {
        this.image = replace(url);
        return this;
    }

    /**
     * Sets the footer text and icon for the embed.
     *
     * @param text the footer text
     * @param icon the footer icon URL
     * @return this {@code EmbedObject} instance for chaining
     */
    public EmbedObject setFooter(String text, String icon) {
        this.footerText = replace(text);
        this.footerIcon = replace(icon);
        return this;
    }

    /**
     * Sets the color of the embed.
     * <p>
     * Attempts to decode the provided color string using {@link Color#decode(String)}.
     * If decoding fails, it attempts to retrieve a color by field name from {@link Color}.
     * </p>
     *
     * @param color the color string (e.g., "#FFAA00" or "RED")
     * @return this {@code EmbedObject} instance for chaining
     */
    public EmbedObject setColor(String color) {
        Color c = null;
        try {
            // Try decoding the color from its hexadecimal representation.
            c = Color.decode(color);
        } catch (Exception e) {
            try {
                // Fallback: try to get a color by its name via reflection.
                Class<?> clazz = Class.forName("java.awt.Color");
                c = (Color) clazz.getField(color).get(null);
            } catch (Exception ignored) {}
        }
        // Fallback using Color.getColor may return null.
        this.color = c != null ? c : Color.getColor(color);
        return this;
    }

    /**
     * Sets the author information for the embed.
     *
     * @param name the author's name
     * @param url  the author's URL
     * @param icon the author's icon URL
     * @return this {@code EmbedObject} instance for chaining
     */
    public EmbedObject setAuthor(String name, String url, String icon) {
        this.author = new Author(replace(name), replace(url), replace(icon));
        return this;
    }

    /**
     * Adds a field to the embed.
     *
     * @param name   the name of the field
     * @param value  the value of the field
     * @param inLine {@code true} if the field should be displayed inline; {@code false} otherwise
     */
    public void addField(String name, String value, boolean inLine) {
        this.fields.add(new Field(replace(name), replace(value), inLine));
    }

    /**
     * Represents a field in a Discord embed.
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    static class Field {
        private final String name;
        private final String value;
        private final boolean inLine;
    }

    /**
     * Represents the author information for a Discord embed.
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    static class Author {
        private final String name;
        private final String url;
        private final String iconUrl;
    }
}

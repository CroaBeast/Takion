package me.croabeast.takion.format;

/**
 * Central registry and handler for all text-based formats used within the Takion framework.
 * <p>
 * A {@code FormatManager} allows you to dynamically register new parsing/formatting rules
 * at runtime, keyed by a unique identifier. Once loaded, each format can be retrieved,
 * edited, or removed by its ID. This interface abstracts the lifecycle of
 * {@link Format} instances, which encapsulate:
 * <ul>
 *   <li>A regular expression pattern to locate format markers in strings.</li>
 *   <li>Logic to strip the markers from the text.</li>
 *   <li>Transformation logic to produce a value of type {@code T} from the marked text.</li>
 * </ul>
 * </p>
 *
 * Example Usage:
 * <pre><code>
 * // Create and register a custom format that parses "<uppercase:...>" blocks
 * FormatManager manager = new MyFormatManagerImplementation();
 * manager.load("upper", new Format&lt;String&gt;() {
 *     public @Regex String getRegex() {
 *         return "&lt;uppercase:(.+?)&gt;";
 *     }
 *     public String apply(Collection&lt;? extends Player&gt; players, String input) {
 *         Matcher m = matcher(input);
 *         if (m.find()) return m.group(1).toUpperCase();
 *         return input;
 *     }
 * });
 *
 * // Apply the format
 * String result = manager.get("upper").apply("Hello <uppercase:world>!");
 * // "Hello WORLD!"
 * </code></pre>
 *
 * @see Format
 */
public interface FormatManager {

    /**
     * Register a new format under the specified identifier.
     * <p>
     * If the given {@code id} is already in use, the existing format is not replaced
     * and this method returns {@code false}. Otherwise, the format is stored and will
     * be used for all subsequent retrievals via {@link #get(String)}.
     * </p>
     *
     * @param id     a case-sensitive key under which this format will be stored
     * @param format the {@link Format} implementation encapsulating regex, removal, and transformation logic
     * @param <T>    the output type produced by the format when applied
     * @return {@code true} if the format was successfully registered; {@code false} if the id was already taken
     */
    <T> boolean load(String id, Format<T> format);

    /**
     * Remove the format associated with the given identifier.
     * <p>
     * All future calls to {@link #get(String)} with this id will return {@code null}.
     * </p>
     *
     * @param id the identifier of the format to remove
     * @return {@code true} if a format existed and was removed; {@code false} if no format was mapped to the id
     */
    boolean remove(String id);

    /**
     * Replace the existing format under the specified {@code id} with a new implementation.
     * <p>
     * Allows you to update parsing/transformation logic without changing the format key.
     * Calling this with an {@code id} that does not exist returns {@code false}.
     * </p>
     *
     * @param id        the identifier of the existing format
     * @param newFormat the new {@link Format} instance to use
     * @param <T>       the output type of the new format
     * @return {@code true} if the format was successfully replaced; {@code false} otherwise
     */
    <T> boolean editFormat(String id, Format<T> newFormat);

    /**
     * Change the key under which a format is stored.
     * <p>
     * Useful for renaming a format without losing its existing logic. If {@code oldId}
     * is not found or {@code newId} is already in use, no change is made.
     * </p>
     *
     * @param oldId the current identifier of the format
     * @param newId the desired new identifier
     * @return {@code true} if the key was successfully updated; {@code false} otherwise
     */
    boolean editId(String oldId, String newId);

    /**
     * Retrieve the format instance tied to the given identifier.
     * <p>
     * The returned {@link Format} can be used to detect, strip, or transform
     * strings according to its regex and logic. If no format exists for {@code identifier},
     * this method returns {@code null}.
     * </p>
     *
     * @param identifier the key of the desired format
     * @param <T>        the output type produced by the format
     * @param <F>        the concrete {@link Format} implementation type
     * @return the {@link Format} registered under {@code identifier}, or {@code null} if none exists
     */
    <T, F extends Format<T>> F get(String identifier);
}

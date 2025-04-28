package me.croabeast.takion.chat;

import me.croabeast.common.Copyable;
import me.croabeast.common.Regex;
import me.croabeast.common.util.ArrayUtils;
import me.croabeast.takion.TakionLib;
import me.croabeast.takion.format.Format;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A composite chat component that can parse a formatted string into multiple
 * {@link ChatComponent} segments and allows fluent manipulation of all segments.
 * <p>
 * A {@code MultiComponent} extends {@link ChatComponent} to support
 * complex text containing interleaved click and hover directives. It also
 * implements {@link Copyable} to produce deep copies of itself.
 * </p>
 * <p>
 * Parsing and formatting is driven by a {@link Format} obtained via
 * {@link #getFormat()}, which defaults to {@link #DEFAULT_FORMAT}.
 * New instances can be created via the static {@code fromString(...)} methods.
 * </p>
 *
 * @see ChatComponent
 * @see Copyable
 */
public interface MultiComponent extends ChatComponent<MultiComponent>, Copyable<MultiComponent> {

    /**
     * Regular expression for matching click actions in the format strings.
     * <p>
     * Supports aliases such as "run", "suggest", "url", "file", "page", "copy", etc.
     * </p>
     */
    @Regex
    String CLICK_REGEX = "execute|click|(run|suggest)(_command)?|(open_)?(url|file)|(change_)?page|copy|(copy_to_)?clipboard";

    /**
     * Default format pattern for parsing text with optional hover and click directives.
     * <p>
     * Matches constructs of the form:
     * {@code <action:"argument"[|action:"argument"]>text</text>} (case-insensitive).
     * </p>
     */
    @Regex
    String DEFAULT_REGEX = "(?i)<((hover|" + CLICK_REGEX + "):\"(.[^|]*?)\"([|]((hover|" + CLICK_REGEX + "):\"(.[^|]*?)\"))?)>(.+?)</text>";

    /**
     * The default {@link Format} instance used by new {@code MultiComponent} objects.
     * <p>
     * Uses {@link #DEFAULT_REGEX} and produces individual {@link ChatComponent} segments for each match.
     * </p>
     */
    Format<ChatComponent<?>> DEFAULT_FORMAT = new MultiCompImpl(TakionLib.getLib(), "").getFormat();

    @NotNull
    default MultiComponent setMessage(@NotNull String message) {
        throw new IllegalStateException("Message can not be set");
    }

    /**
     * Gets the {@link Format} used to parse and render this component.
     *
     * @return the current {@link Format} for this multi-component (never {@code null})
     */
    @NotNull
    Format<ChatComponent<?>> getFormat();

    /**
     * Sets a custom {@link Format} for parsing this component.
     * <p>
     * Subsequent invocations of {@link #append(String)} or
     * initial parsing via {@code fromString(...)} will use this format.
     * </p>
     *
     * @param format the new format to apply (must not be {@code null})
     * @return this component, with the format updated
     */
    @NotNull
    MultiComponent setFormat(@NotNull Format<ChatComponent<?>> format);

    /**
     * Appends a raw text segment to this component.
     * <p>
     * The segment is parsed by the current {@linkplain #getFormat() format}
     * and merged with existing segments.
     * </p>
     *
     * @param message the raw text to append
     * @return this component, with the text appended
     */
    @NotNull
    MultiComponent append(String message);

    /**
     * Appends an existing {@link ChatComponent} to this multi-component.
     *
     * @param component the component to append (must not be {@code null})
     * @return this component, with the segment appended
     */
    @NotNull
    MultiComponent append(@NotNull ChatComponent<?> component);

    /**
     * Appends an object's {@code toString()} value as a text segment.
     *
     * @param object the object whose string form will be appended
     * @return this component, with the object's text appended
     */
    @NotNull
    default MultiComponent append(Object object) {
        return append(String.valueOf(object));
    }

    /**
     * Applies a click action uniformly to all segments in this component.
     *
     * @param click the click action type to apply
     * @param input the argument for the click action
     * @return this component, with click events applied to every segment
     */
    @NotNull
    MultiComponent setClickToAll(Click click, String input);

    /**
     * Applies a click action (by name) uniformly to all segments.
     *
     * @param click the click action name or alias
     * @param input the argument for the click action
     * @return this component, with click events applied to every segment
     */
    @NotNull
    default MultiComponent setClickToAll(String click, String input) {
        return setClickToAll(Click.fromName(click), input);
    }

    /**
     * Applies a click action (parsed from a single {@code "action:argument"} string)
     * uniformly to all segments.
     *
     * @param input the combined action and argument string
     * @return this component, with click events applied to every segment
     */
    @NotNull
    default MultiComponent setClickToAll(String input) {
        final String[] parts = input.replace("\"", "").split(":", 2);
        return setClickToAll(parts[0], parts.length == 1 ? "" : parts[1]);
    }

    /**
     * Applies a hover tooltip uniformly to all segments.
     *
     * @param list the lines of hover text
     * @return this component, with hover events applied to every segment
     */
    @NotNull
    MultiComponent setHoverToAll(List<String> list);

    /**
     * Applies a hover tooltip (via varargs) uniformly to all segments.
     *
     * @param strings the lines of hover text
     * @return this component, with hover events applied to every segment
     */
    @NotNull
    default MultiComponent setHoverToAll(String... strings) {
        return setHoverToAll(ArrayUtils.toList(strings));
    }

    /**
     * Applies a single-line hover tooltip uniformly to all segments.
     *
     * @param string the hover text line
     * @return this component, with hover events applied to every segment
     */
    @NotNull
    MultiComponent setHoverToAll(String string);

    /**
     * Creates a new {@link MultiComponent} from the given raw message,
     * using the specified {@link TakionLib} instance for formatting rules.
     *
     * @param lib     the {@link TakionLib} providing configuration
     * @param message the raw chat message (may include formatting tokens)
     * @return a new {@code MultiComponent} parsed from {@code message}
     */
    static MultiComponent fromString(TakionLib lib, String message) {
        return new MultiCompImpl(lib, message);
    }

    /**
     * Creates a new {@link MultiComponent} from the given raw message,
     * using the default {@link TakionLib} singleton.
     *
     * @param message the raw chat message (may include formatting tokens)
     * @return a new {@code MultiComponent} parsed from {@code message}
     */
    static MultiComponent fromString(String message) {
        return fromString(TakionLib.getLib(), message);
    }
}

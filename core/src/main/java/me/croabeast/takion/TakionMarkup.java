package me.croabeast.takion;

import lombok.RequiredArgsConstructor;
import me.croabeast.prismatic.element.Element;
import me.croabeast.prismatic.element.MarkupFormat;
import me.croabeast.prismatic.element.RenderContext;
import me.croabeast.takion.marker.Marker;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bridges Takion's text preparation into the Prismatic element parser.
 *
 * <p>Replaces the old {@code ChatProcessor}: its two responsibilities were split, because they
 * belong to different phases. {@link #prepare(String)} runs once while parsing, and colorization
 * runs per render through {@link #context(Player)}.
 *
 * @since 2.0.0
 */
@RequiredArgsConstructor
public final class TakionMarkup implements MarkupFormat {

    private final TakionLib lib;

    @NotNull
    @Override
    public Pattern getPattern() {
        return MarkupFormat.prismatic().getPattern();
    }

    @NotNull
    @Override
    public Element accept(@NotNull Matcher matcher) {
        return MarkupFormat.prismatic().accept(matcher);
    }

    @NotNull
    @Override
    public String prepare(String raw) {
        return lib.getCharacterManager().align(lib.prepareText(MarkupFormat.prismatic().prepare(raw)));
    }

    /**
     * Returns a render context for a single player acting as both receiver and parser.
     *
     * @param player receiving player, or {@code null}
     * @return the render context
     */
    @NotNull
    public RenderContext context(@Nullable Player player) {
        return context(player, player);
    }

    /**
     * Returns a render context that resolves placeholders by token lookup and colorizes through
     * this library.
     *
     * <p>The tokens were already located while parsing, so resolving them here costs a lookup each
     * instead of scanning the whole message once per registered placeholder. The text formats
     * themselves already ran in {@link #prepare(String)} and are not repeated.
     *
     * @param receiver player receiving the output, or {@code null}
     * @param parser   player used to resolve placeholders, or {@code null}
     * @return the render context
     */
    @NotNull
    public RenderContext context(@Nullable Player receiver, @Nullable Player parser) {
        return RenderContext
                .of(receiver, token -> lib.resolvePlaceholder(parser, token))
                .withFormatter((ignored, text) -> lib.colorizeResolved(receiver, parser, text));
    }

    /**
     * Returns the literal line separator configured through the {@code line_separator} marker.
     *
     * @return the separator literal
     */
    @NotNull
    public String getLineSeparator() {
        Marker marker = lib.getMarkerManager().getMarker("line_separator");
        String regex = marker == null ? Pattern.quote("<n>") : marker.getPattern().pattern();

        return regex.startsWith("\\Q") && regex.endsWith("\\E") ?
                regex.substring(2, regex.length() - 2) :
                regex;
    }
}

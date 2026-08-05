package me.croabeast.takion.marker;

import me.croabeast.takion.token.Token;
import me.croabeast.takion.token.TokenRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Registry of the control markers known to a library instance.
 */
public interface MarkerManager extends TokenRegistry<Marker> {

    @Nullable
    default Marker getMarker(@Nullable String id) {
        return get(id);
    }

    @NotNull
    default Collection<Marker> getMarkers() {
        return getAll();
    }

    default boolean hasMarker(@Nullable String id) {
        return has(id);
    }

    /**
     * Applies one marker to a string.
     *
     * @param id      marker id, or {@code null} to apply every registered marker
     * @param context resolution context
     * @param string  text to transform
     * @return the transformed text
     */
    @NotNull
    String apply(@Nullable String id, @NotNull Token.Context context, @Nullable String string);

    /**
     * Applies every registered marker to a string.
     *
     * @param context resolution context
     * @param string  text to transform
     * @return the transformed text
     */
    @NotNull
    String apply(@NotNull Token.Context context, @Nullable String string);
}

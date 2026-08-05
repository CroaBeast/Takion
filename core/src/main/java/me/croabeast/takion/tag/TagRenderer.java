package me.croabeast.takion.tag;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Converts semantic Takion tags into a target output type.
 *
 * @param <T> output type produced by this renderer
 */
public interface TagRenderer<T> {

    /**
     * Renders the tags contained in the given string.
     *
     * @param receiver player receiving the output
     * @param parser   player used as parse context
     * @param string   input text
     * @return rendered output
     */
    @NotNull
    T render(@Nullable Player receiver, @Nullable Player parser, @Nullable String string);
}

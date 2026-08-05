package me.croabeast.takion.adventure;

import me.croabeast.takion.tag.Tag;
import me.croabeast.takion.token.Token;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.regex.MatchResult;

/**
 * Optional extension point for tags that can render their own Adventure component.
 */
public interface AdventureTag extends Tag {

    @NotNull
    Component renderComponent(@NotNull Token.Context context, @NotNull MatchResult match, @NotNull Token.Rendered result);
}

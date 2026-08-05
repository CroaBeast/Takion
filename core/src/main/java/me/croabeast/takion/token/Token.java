package me.croabeast.takion.token;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.croabeast.takion.TakionLib;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * A pattern located in a message plus the value it resolves to.
 *
 * <p>Tags, markers and actions were three interfaces with this exact shape, each with its own
 * context type and its own registry. They are now one concept parameterized by what a match
 * produces: text for a marker, a rendered result for a tag, a side effect for an action.
 *
 * @param <T> what a match resolves to
 * @since 2.0.0
 */
public interface Token<T> {

    /**
     * Returns the unique identifier.
     *
     * @return token identifier
     */
    @NotNull
    String getId();

    /**
     * Returns the pattern used to locate this token in input text.
     *
     * @return token pattern
     */
    @NotNull
    Pattern getPattern();

    /**
     * Resolves this token for the given context and match.
     *
     * @param context resolution context
     * @param match   matched input
     * @return the resolved value
     */
    @NotNull
    T resolve(@NotNull Context context, @NotNull MatchResult match);

    /**
     * Returns the channel ids where this token applies.
     *
     * <p>An empty set means every channel. Only side-effecting tokens use this.
     *
     * @return allowed channel ids
     */
    @NotNull
    default Set<String> getChannels() {
        return Collections.emptySet();
    }

    /**
     * Everything a token may need while resolving.
     *
     * <p>This is the union of the three context types it replaces. A token reads the fields it
     * cares about and ignores the rest: a marker never looks at the targets, an action never looks
     * at the receiver.
     */
    interface Context {

        @Nullable
        TakionLib getLib();

        /**
         * Player the output is being built for.
         */
        @Nullable
        Player getReceiver();

        /**
         * Player used to resolve placeholders, which is not always the receiver.
         */
        @Nullable
        Player getParser();

        /**
         * Players a side effect applies to.
         */
        @NotNull
        Collection<? extends Player> getTargets();

        /**
         * Channel the message is being sent through, or {@code null}.
         */
        @Nullable
        String getChannel();

        @NotNull
        Map<String, Object> getOptions();

        @Nullable
        default Object getOption(@NotNull String key) {
            return getOptions().get(key);
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    final class SimpleContext implements Context {

        private TakionLib lib;
        private Player receiver;
        private Player parser;
        private String channel;

        @NotNull
        private Collection<? extends Player> targets = Collections.emptyList();

        @NotNull
        private Map<String, Object> options = new HashMap<>();
    }

    /**
     * What a token renders to.
     *
     * <p>{@link #getMeasure()} exists because a token can occupy a different visual width than the
     * text it renders: a player head is one glyph but does not measure like one, and the alignment
     * pass needs the width, not the output.
     */
    interface Rendered {

        @NotNull
        String getLegacy();

        @NotNull
        String getMeasure();

        @NotNull
        Map<String, Object> getData();
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    final class SimpleRendered implements Rendered {

        @NotNull
        private String legacy = "";

        @NotNull
        private String measure = "";

        @NotNull
        private Map<String, Object> data = new HashMap<>();
    }
}

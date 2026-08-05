package me.croabeast.takion;

import me.croabeast.takion.token.Token;
import me.croabeast.takion.marker.Marker;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

final class LangPrefixMarker implements Marker {

    private String prefix;
    private String key;
    private Pattern pattern;

    LangPrefixMarker(String prefix) {
        setPrefix(prefix);
        setKey("<P>");
    }

    @NotNull
    public String getId() {
        return "lang_prefix";
    }

    @NotNull
    public Pattern getPattern() {
        return pattern;
    }

    @NotNull
    public String resolve(@NotNull Token.Context context, @NotNull MatchResult match) {
        return Boolean.TRUE.equals(context.getOption("remove")) ? "" : prefix;
    }

    @NotNull
    String getPrefix() {
        return prefix;
    }

    void setPrefix(String prefix) {
        this.prefix = StringUtils.defaultString(prefix);
    }

    @NotNull
    String getKey() {
        return key;
    }

    void setKey(String key) {
        this.key = StringUtils.defaultIfBlank(key, "<P>");
        this.pattern = Pattern.compile(Pattern.quote(this.key));
    }

}

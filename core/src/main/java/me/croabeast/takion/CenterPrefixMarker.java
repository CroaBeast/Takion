package me.croabeast.takion;

import me.croabeast.takion.token.Token;
import me.croabeast.takion.marker.Marker;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

final class CenterPrefixMarker implements Marker {

    private String prefix;
    private Pattern pattern;

    CenterPrefixMarker() {
        setPrefix("[C]");
    }

    @NotNull
    public String getId() {
        return "center_prefix";
    }

    @NotNull
    public Pattern getPattern() {
        return pattern;
    }

    @NotNull
    public String resolve(@NotNull Token.Context context, @NotNull MatchResult match) {
        return "";
    }

    @NotNull
    String getPrefix() {
        return prefix;
    }

    void setPrefix(String prefix) {
        this.prefix = StringUtils.defaultIfBlank(prefix, "[C]");
        this.pattern = Pattern.compile("^" + Pattern.quote(this.prefix));
    }

}

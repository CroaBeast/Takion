package me.croabeast.takion;

import me.croabeast.takion.token.Token;
import me.croabeast.takion.marker.Marker;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

final class LineSeparatorMarker implements Marker {

    private String separator;
    private Pattern pattern;

    LineSeparatorMarker() {
        setSeparator(Pattern.quote("<n>"));
    }

    @NotNull
    public String getId() {
        return "line_separator";
    }

    @NotNull
    public Pattern getPattern() {
        return pattern;
    }

    @NotNull
    public String resolve(@NotNull Token.Context context, @NotNull MatchResult match) {
        Object prefix = context.getOption("prefix");
        return prefix == null ? "\n" : prefix + getText();
    }

    @NotNull
    String getSeparator() {
        return separator;
    }

    void setSeparator(String separator) {
        this.separator = StringUtils.defaultIfBlank(separator, Pattern.quote("<n>"));
        this.pattern = Pattern.compile(this.separator);
    }

    @NotNull
    String getText() {
        String regex = getSeparator();
        return regex.startsWith("\\Q") && regex.endsWith("\\E") ?
                regex.substring(2, regex.length() - 2) :
                regex;
    }

}

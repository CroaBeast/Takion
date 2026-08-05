package me.croabeast.takion;

import me.croabeast.takion.marker.Marker;
import me.croabeast.takion.marker.MarkerManager;
import me.croabeast.takion.token.Token;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;

final class MarkerManagerImpl extends TokenRegistryImpl<Marker> implements MarkerManager {

    MarkerManagerImpl(String langPrefix) {
        load(new LangPrefixMarker(langPrefix));
        load(new LineSeparatorMarker());
        load(new CenterPrefixMarker());
    }

    @NotNull
    @Override
    public String apply(String id, @NotNull Token.Context context, String string) {
        Marker marker = get(id);
        return marker == null ? StringUtils.defaultString(string) : apply(marker, context, string);
    }

    @NotNull
    @Override
    public String apply(@NotNull Token.Context context, String string) {
        String result = StringUtils.defaultString(string);
        for (Marker marker : getAll())
            result = apply(marker, context, result);

        return result;
    }

    private String apply(Marker marker, Token.Context context, String string) {
        if (StringUtils.isBlank(string)) return StringUtils.defaultString(string);

        Matcher matcher = marker.getPattern().matcher(string);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find())
            matcher.appendReplacement(buffer,
                    Matcher.quoteReplacement(marker.resolve(context, matcher.toMatchResult())));

        matcher.appendTail(buffer);
        return buffer.toString();
    }
}

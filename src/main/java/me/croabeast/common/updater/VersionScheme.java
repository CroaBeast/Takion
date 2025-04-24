package me.croabeast.common.updater;

import org.apache.commons.lang.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interface defining a strategy to compare two version strings and return
 * the "greater" (more recent) of the two, or {@code null} if comparison
 * is not possible under the scheme.
 */
public interface VersionScheme {

    /**
     * A simple decimal-based version scheme: splits on dots and compares each numeric segment.
     * If one array is longer, that version is considered newer.
     */
    VersionScheme DECIMAL_SCHEME = new VersionScheme() {

        @Nullable
        String[] splitVersionInfo(String version) {
            Matcher matcher = Pattern.compile("\\d+(?:\\.\\d+)*").matcher(version);
            return matcher.find() ? matcher.group().split("[.]") : null;
        }

        @Nullable
        public String compare(@NotNull String first, @NotNull String second) {
            String[] firstSplit = splitVersionInfo(first);
            String[] secondSplit = splitVersionInfo(second);

            if (firstSplit == null || secondSplit == null) return null;

            for (int i = 0; i < Math.min(firstSplit.length, secondSplit.length); i++) {
                int currentValue = NumberUtils.toInt(firstSplit[i]),
                        newestValue = NumberUtils.toInt(secondSplit[i]);

                if (newestValue > currentValue) return second;
                else if (newestValue < currentValue) return first;
            }

            return (secondSplit.length > firstSplit.length) ? second : first;
        }
    };


    /**
     * Compares two version identifiers according to a custom scheme.
     *
     * @param first  the currently installed version (non-null)
     * @param second the remote/latest version to compare against (non-null)
     * @return the version string representing the newer version,
     *         or {@code null} if the scheme cannot compare the inputs
     */
    @Nullable
    String compare(@NotNull String first, @NotNull String second);
}

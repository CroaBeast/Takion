package me.croabeast.common.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Platforms supported by an update checker, each with its API URL template.
 */
@RequiredArgsConstructor
@Getter
public enum Platform {
    /**
     * SpigotMC's resource API. Returns a JSON object with "current_version".
     */
    SPIGOT("https://api.spigotmc.org/simple/0.1/index.php?action=getResource&id=%s") {
        @NotNull
        public String extractLatest(JsonElement root) throws RuntimeException {
            if (!root.isJsonObject()) throw new IllegalArgumentException();
            return Objects.requireNonNull(root.getAsJsonObject().get("current_version")).getAsString();
        }
    },

    /**
     * Modrinth's project version API. Returns a JSON array of versions.
     */
    MODRINTH("https://api.modrinth.com/v2/project/%s/version") {
        @NotNull
        public String extractLatest(JsonElement root) throws RuntimeException {
            if (!root.isJsonArray()) throw new IllegalArgumentException();

            JsonArray arr = root.getAsJsonArray();
            if (arr.size() == 0) throw new IndexOutOfBoundsException();

            return Objects.requireNonNull(
                    arr.get(0).getAsJsonObject().get("version_number")
            ).getAsString();
        }
    },

    /**
     * GitHub Releases API. Returns a JSON object for the latest release.
     */
    GITHUB("https://api.github.com/repos/%s/releases/latest") {
        @Override
        public @NotNull String extractLatest(JsonElement root) throws RuntimeException {
            if (!root.isJsonObject()) throw new IllegalArgumentException();

            JsonElement obj = root.getAsJsonObject().get("tag_name");
            if (obj == null || obj.isJsonNull()) throw new NullPointerException();

            return obj.getAsString();
        }
    };

    /**
     * URL template into which the project identifier is substituted.
     */
    private final String urlTemplate;

    /**
     * Extracts the latest version from the JSON returned by this platform's API.
     *
     * @param root the root JSON element returned by the HTTP request
     * @return the latest version string
     * @throws RuntimeException if the response does not match the expected format
     */
    @NotNull
    public abstract String extractLatest(JsonElement root) throws RuntimeException;
}

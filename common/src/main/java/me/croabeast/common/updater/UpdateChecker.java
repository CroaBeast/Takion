package me.croabeast.common.updater;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Checks for plugin updates on supported platforms by querying their respective APIs,
 * parsing the returned JSON, and comparing the latest remote version against the
 * currently installed version.
 * <p>
 * Supports custom version-comparison schemes and platforms: SpigotMC, Modrinth, and GitHub.
 * All network operations are performed asynchronously to avoid blocking the main server thread.
 * </p>
 *
 * @see #requestCheck(String, Platform)
 * @see VersionScheme
 * @see Platform
 * @see UpdateResult
 *
 * @author Parker Hawke - Choco (forked by CroaBeast)
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdateChecker {

    /**
     * User-Agent header identifying this library when making HTTP requests.
     */
    private static final String USER_AGENT = "CHOCO-forked-update-checker";

    /**
     * The plugin whose version is being checked.
     */
    final String pluginVersion;

    /**
     * Strategy for comparing version strings.
     */
    private final VersionScheme scheme;

    /**
     * Performs the HTTP request and JSON parsing asynchronously, then compares
     * the remote version against the installed one.
     *
     * @param id The project identifier, plugged into the {@link Platform#getUrlTemplate()}
     * @param platform The platform from which to fetch version information.
     *
     * @return a {@link CompletableFuture} completing with the {@link UpdateResult}
     * @throws IllegalArgumentException if identifier is blank
     * @throws NullPointerException     if the platform is null
     */
    @NotNull
    public CompletableFuture<@NotNull UpdateResult> requestCheck(String id, Platform platform) {
        Preconditions.checkArgument(StringUtils.isNotBlank(id));
        Objects.requireNonNull(platform);

        return CompletableFuture.supplyAsync(() -> {
            int responseCode = -1;
            String fetched = "";
            Throwable throwable = null;
            Reason reason = null;

            try {
                URL url = new URL(String.format(platform.getUrlTemplate(), id));

                HttpURLConnection connect = (HttpURLConnection) url.openConnection();
                connect.addRequestProperty("User-Agent", USER_AGENT);

                responseCode = connect.getResponseCode();

                JsonReader reader = new JsonReader(
                        new InputStreamReader(connect.getInputStream()));
                JsonElement json = new JsonParser().parse(reader);
                reader.close();

                try {
                    fetched = platform.extractLatest(json);
                } catch (RuntimeException e) {
                    reason = Reason.INVALID_JSON;
                }

                String latest = scheme.compare(pluginVersion, fetched);
                if (latest == null) reason = Reason.UNSUPPORTED_VERSION_SCHEME;

                else if (latest.equals(pluginVersion))
                    reason = pluginVersion.equals(fetched) ?
                            Reason.UP_TO_DATE :
                            Reason.UNRELEASED_VERSION;

                else if (latest.equals(fetched)) reason = Reason.NEW_UPDATE;
            } catch (IOException e) {
                throwable = e;
            }

            if (reason == null)
                switch (responseCode) {
                    case -1:
                        reason = Reason.UNKNOWN_ERROR;
                        break;
                    case 401:
                        reason = Reason.UNAUTHORIZED_QUERY;
                        break;
                    default:
                        reason = Reason.COULD_NOT_CONNECT;
                        break;
                }

            return new UpdateResult(this, reason, fetched, throwable);
        });
    }

    /**
     * Performs the HTTP request and JSON parsing asynchronously, then compares
     * the remote version against the installed one.
     *
     * @param id The project identifier, plugged into the {@link Platform#getUrlTemplate()}
     * @param platform The platform from which to fetch version information.
     *
     * @return a {@link CompletableFuture} completing with the {@link UpdateResult}
     * @throws NullPointerException     if the platform is null
     */
    @NotNull
    public CompletableFuture<@NotNull UpdateResult> requestCheck(int id, Platform platform) {
        return requestCheck(String.valueOf(id), platform);
    }

    /**
     * Factory method to create an {@code UpdateChecker} with all parameters.
     *
     * @param plugin     the plugin instance (non-null)
     * @param scheme     the version comparison scheme (non-null)
     *
     * @return a configured {@code UpdateChecker}
     *
     * @throws NullPointerException     if any required argument is null
     */
    @NotNull
    public static UpdateChecker of(Plugin plugin, VersionScheme scheme) {
        return new UpdateChecker(
                Objects.requireNonNull(plugin).getDescription().getVersion(),
                Objects.requireNonNull(scheme)
        );
    }

    /**
     * Convenience factory using the default decimal {@link VersionScheme#DECIMAL_SCHEME}.
     *
     * @param plugin     the plugin instance
     * @return a configured {@code UpdateChecker}
     *
     * @throws NullPointerException     if the plugin is null
     */
    @NotNull
    public static UpdateChecker of(Plugin plugin) {
        return of(plugin, VersionScheme.DECIMAL_SCHEME);
    }
}

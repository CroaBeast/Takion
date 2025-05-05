package me.croabeast.common.updater;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the outcome of an update check, encapsulating the comparison result
 * between the locally installed plugin version and the fetched remote version.
 * <p>
 * Contains the reason code for the check, the local version, the fetched version,
 * and any exception that occurred during the check.
 * </p>
 *
 * @see UpdateChecker
 * @see Reason
 */
@Getter
public final class UpdateResult {

    /**
     * The reason code describing the outcome of the update check.
     * Indicates whether a new update is available or why the check failed/succeeded.
     *
     * @see Reason
     */
    @NotNull
    private final Reason reason;

    /**
     * The version string of the locally installed plugin, as reported by {@link UpdateChecker}.
     */
    @NotNull
    private final String local;

    /**
     * The version string fetched from the remote platform (e.g., Spigot, GitHub).
     */
    @NotNull
    private final String fetched;

    /**
     * Any exception that was thrown during the update check, or {@code null} if none occurred.
     */
    @Nullable
    private final Throwable throwable;

    /**
     * Constructs a new {@code UpdateResult}.
     *
     * @param checker   the {@link UpdateChecker} that performed the check, used to retrieve the local version
     * @param reason    the outcome reason of the check
     * @param fetched   the version string fetched remotely
     * @param throwable any exception encountered during the check, or {@code null} if the check completed normally
     */
    UpdateResult(
            UpdateChecker checker, @NotNull Reason reason,
            @NotNull String fetched, @Nullable Throwable throwable
    ) {
        this.reason    = reason;
        this.local     = checker.pluginVersion;
        this.fetched   = fetched;
        this.throwable = throwable;
    }

    /**
     * Determines whether the update check has identified a newer version available.
     *
     * @return {@code true} if {@link #getReason()} is {@link Reason#NEW_UPDATE}; {@code false} otherwise.
     */
    public boolean requiresUpdate() {
        return reason == Reason.NEW_UPDATE;
    }

    /**
     * Returns the version string that should be considered the "latest" for this check.
     * <p>
     * If an update is required, returns the fetched remote version; otherwise, returns the local version.
     * </p>
     *
     * @return the fetched version if an update is available, or the local version otherwise
     */
    public String getLatest() {
        return requiresUpdate() ? getFetched() : getLocal();
    }
}

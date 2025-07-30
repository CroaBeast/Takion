package me.croabeast.common.updater;

/**
 * Possible outcomes of an update check, describing why no update may be available
 * or why an error occurred.
 */
public enum Reason {
    /**
     * A newer version is available remotely.
     */
    NEW_UPDATE,
    /**
     * Could not establish a connection to the API endpoint.
     */
    COULD_NOT_CONNECT,
    /**
     * The returned JSON was malformed or missing expected fields.
     */
    INVALID_JSON,
    /**
     * The API responded with HTTP 401 Unauthorized.
     */
    UNAUTHORIZED_QUERY,
    /**
     * The locally installed version is ahead of the remote version.
     */
    UNRELEASED_VERSION,
    /**
     * Some other I/O or parsing error occurred.
     */
    UNKNOWN_ERROR,
    /**
     * The chosen {@link VersionScheme} could not compare the two version strings.
     */
    UNSUPPORTED_VERSION_SCHEME,
    /**
     * The plugin is already at the latest version.
     */
    UP_TO_DATE
}

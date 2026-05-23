package me.croabeast.common.util;

import lombok.experimental.UtilityClass;
import me.croabeast.vnc.VNC;

/**
 * A utility class that stores static server information constants for easy access.
 */
@UtilityClass
public class ServerInfoUtils {

    /**
     * The version number of the Bukkit package that is currently running.
     */
    public static final String BUKKIT_API_VERSION;

    /**
     * The major and minor version of the server in a decimal format.
     * <p>
     * For example, if the server version is {@code 1.16.5}, this field holds {@code 16.5}.
     * </p>
     */
    public final double SERVER_VERSION;

    /**
     * The spigot-format server version string, including the server fork name.
     */
    public final String SERVER_FORK;

    /**
     * {@code true} if the server is running Paper or a Paper-based fork; {@code false} otherwise.
     */
    public final boolean PAPER_ENABLED;

    /**
     * The Java major version running on the server.
     * <p>
     * For example, if the full Java version is {@code 1.8.0_302}, this field holds {@code 8}.
     * </p>
     */
    public final int JAVA_VERSION;
    
    static {
        BUKKIT_API_VERSION = VNC.BUKKIT_API_VERSION;
        SERVER_VERSION = VNC.SERVER_VERSION;
        SERVER_FORK = VNC.SERVER_FORK;
        PAPER_ENABLED = VNC.PAPER_ENABLED;
        JAVA_VERSION = VNC.JAVA_VERSION;
    }
}

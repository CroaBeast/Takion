package me.croabeast.common.util;

import lombok.experimental.UtilityClass;
import me.croabeast.vnc.VNC;

/**
 * The class that stores static keys for easy access and management.
 *
 * @author CroaBeast
 * @since 1.0
 */
@UtilityClass
public class ServerInfoUtils {

    /**
     * Retrieves the version number of the Bukkit package that is currently running.
     */
    public static final String BUKKIT_API_VERSION;

    /**
     * Returns the major and minor version of the server in a double/decimal format.
     *
     * <p> If version is <code>1.16.5</code>, will return <code>16.5</code>.
     */
    public final double SERVER_VERSION;

    /**
     * Returns the spigot-format server version and fork.
     */
    public final String SERVER_FORK;

    /**
     * Returns true if the server is Paper or a fork of it, otherwise false.
     */
    public final boolean PAPER_ENABLED;

    /**
     * Returns the Java major version of the server.
     *
     * <p> Example: if version is <code>1.8.0.302</code>, will return <code>8</code>.
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

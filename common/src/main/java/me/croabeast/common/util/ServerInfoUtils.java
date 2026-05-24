package me.croabeast.common.util;

import lombok.experimental.UtilityClass;
import me.croabeast.vnc.VNC;
import org.bukkit.Bukkit;

import java.util.Locale;

/**
 * A utility class that stores static server information constants for easy access.
 *
 * @deprecated Use {@link me.croabeast.vnc.VNC} directly instead.
 *             This class will be removed in a future version.
 */
@Deprecated
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
        String craftPackage = Bukkit.getServer().getClass().getPackage().getName();
        String apiVersion = craftPackage.substring(craftPackage.lastIndexOf('.') + 1);
        BUKKIT_API_VERSION = apiVersion.matches("v\\d+_\\d+_R\\d+") ? apiVersion : "";
        SERVER_VERSION = VNC.SERVER_VERSION;
        SERVER_FORK = Bukkit.getName() + " " + VNC.SERVER_CLASSIC_VERSION;
        String implementation = (Bukkit.getName() + ' ' + Bukkit.getVersion()).toLowerCase(Locale.ENGLISH);
        PAPER_ENABLED = implementation.contains("paper");
        JAVA_VERSION = VNC.JAVA_VERSION;
    }
}

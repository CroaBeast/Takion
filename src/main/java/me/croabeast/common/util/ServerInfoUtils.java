package me.croabeast.common.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        double main = 0.0;
        
        Pattern p = Pattern.compile("1\\.(\\d+(\\.\\d+)?)");
        Matcher m = p.matcher(Bukkit.getVersion());

        if (m.find()) 
            try {
                main = Double.parseDouble(m.group(1));
            } catch (Exception ignored) {}
        
        SERVER_VERSION = main;

        BUKKIT_API_VERSION = SERVER_VERSION >= 20.5 ?
                "" :
                Bukkit.getServer().getClass()
                        .getPackage()
                        .getName().split("\\.")[3];
        
        SERVER_FORK = WordUtils.capitalize(Bukkit.getName())
                + " 1." +
                SERVER_VERSION;

        boolean isPaper = false;

        if (SERVER_VERSION >= 8) {
            try {
                Class.forName(SERVER_VERSION >= 12.0 ?
                        "com.destroystokyo.paper.ParticleBuilder" :
                        "io.papermc.paperclip.Paperclip"
                );
                isPaper = true;
            } catch (ClassNotFoundException ignored) {}
        }

        PAPER_ENABLED = isPaper;

        String version = SystemUtils.JAVA_VERSION;

        if (!version.startsWith("1.")) {
            int dot = version.indexOf(".");
            if (dot != -1)
                version = version.substring(0, dot);
        }
        else version = version.substring(2, 3);

        JAVA_VERSION = Integer.parseInt(version);
    }
}

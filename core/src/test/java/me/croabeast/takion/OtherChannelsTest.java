package me.croabeast.takion;

import me.croabeast.takion.channel.Channel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Covers the channels other than chat, which share the same per-target loop and prefix stripping.
 */
class OtherChannelsTest {

    private static final String[] MESSAGES = {
            "texto plano",
            "&7con color",
            "<g:ff0000>degradado</g:00ff00>",
            "<sc>small caps</sc> y <u:2764>",
            "&7hola {player}"
    };

    private static TakionLib lib;

    @BeforeAll
    static void setUp() {
        TestServer.install();
        lib = new TakionLib(null);
    }

    @Test
    void actionBar() {
        StringBuilder out = new StringBuilder();
        Channel channel = lib.getChannelManager().identify("[action_bar]x");

        for (String message : MESSAGES) {
            TestPlayer player = new TestPlayer("Steve");
            channel.send(Arrays.asList(player.handle()), null, "[action_bar]" + message);

            out.append("input   : ").append(Snapshots.escape(message)).append('\n');
            for (String line : player.typed())
                out.append("  typed : ").append(Snapshots.escape(line)).append('\n');
            out.append('\n');
        }

        Snapshots.verify("channel-action-bar", out.toString());
    }

    @Test
    void identifyResolvesEveryChannel() {
        assertEquals("chat", lib.getChannelManager().identify("plain").getName());
        assertEquals("action_bar", lib.getChannelManager().identify("[action_bar]x").getName());
        assertEquals("title", lib.getChannelManager().identify("[title]x").getName());
        assertEquals("bossbar", lib.getChannelManager().identify("[bossbar]x").getName());
        assertEquals("tab_header", lib.getChannelManager().identify("[tab_header]x").getName());
        assertEquals("tab_footer", lib.getChannelManager().identify("[tab_footer]x").getName());
        assertEquals("toast", lib.getChannelManager().identify("[toast:minecraft:iron_ingot:challenge]x").getName());
        assertEquals("brand", lib.getChannelManager().identify("[brand]x").getName());
        assertEquals(Channel.Flag.TOAST, lib.getChannelManager().identify("[toast]x").getFlag());
    }

    @Test
    void tabListChannelsSetTheirOwnPart() {
        TestPlayer player = new TestPlayer("Steve");
        lib.getChannelManager().identify("[tab_header]x").send(Arrays.asList(player.handle()), null, "[tab_header]&7Top");
        lib.getChannelManager().identify("[tab_footer]x").send(Arrays.asList(player.handle()), null, "[tab_footer]Bottom");

        assertEquals(Arrays.asList("TAB_HEADER §7Top", "TAB_FOOTER Bottom"), player.typed());
    }

    @Test
    void toastNeedsPacketEvents() {
        TestPlayer player = new TestPlayer("Steve");
        String raw = "[toast:iron_ingot:goal]Hi";

        assertFalse(lib.getChannelManager().identify(raw).send(Arrays.asList(player.handle()), null, raw));
    }

    /**
     * Every channel must drop its own prefix before formatting, or the receiver sees it.
     */
    @Test
    void noChannelLeaksItsPrefix() {
        for (String id : new String[] {"action_bar", "title", "chat"}) {
            TestPlayer player = new TestPlayer("Steve");
            String raw = "[" + id + "]texto";

            Channel channel = lib.getChannelManager().identify(raw);
            channel.send(Arrays.asList(player.handle()), null, raw);

            for (String line : player.plain())
                assertEquals(-1, line.indexOf('['), id + " leaked its prefix: " + line);
            for (String line : player.typed())
                assertEquals(-1, line.indexOf('['), id + " leaked its prefix: " + line);
        }
    }
}

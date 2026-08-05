package me.croabeast.takion;

import me.croabeast.prismatic.element.Element;
import me.croabeast.prismatic.element.RenderContext;
import me.croabeast.takion.channel.Channel;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Freezes what a player actually receives through the chat channel.
 *
 * <p>This is the layer the snapshots did not reach: everything below {@code colorize} needs a real
 * receiver, so the per-target loop, the interactive branch and the click and hover payloads were
 * all unverified.
 */
class ChatChannelTest {

    private static final String[] MESSAGES = {
            "texto plano",
            "&7con color",
            "<g:ff0000>degradado</g:00ff00>",
            "<run:\"/spawn\">&aboton</text>",
            "<run:\"/spawn\"|hover:\"&7pista<n>&8/spawn\">&aboton</text>",
            "&aInicio <run:\"/a\">&bmedio</text> &cfinal",
            "&7visita https://example.com ahora",
            "<sc>small caps</sc> y <u:2764>",
            "&7hola {player}",
            "<run:\"/cmd {player}\">&aplaceholder {player}</text>"
    };

    private static TakionLib lib;
    private static Channel chat;

    @BeforeAll
    static void setUp() {
        TestServer.install();
        lib = new TakionLib(null);
        chat = lib.getChannelManager().identify("texto");
    }

    @Test
    void whatASinglePlayerReceives() {
        StringBuilder out = new StringBuilder();

        for (String message : MESSAGES) {
            TestPlayer player = new TestPlayer("Steve");
            chat.send(Arrays.asList(player.handle()), null, message);

            out.append("input   : ").append(Snapshots.escape(message)).append('\n');
            dump(out, player);
            out.append('\n');
        }

        Snapshots.verify("chat-channel", out.toString());
    }

    /**
     * With an explicit parser every receiver must get identical output. This is the invariant any
     * change to the parse and colorize order has to preserve.
     */
    @Test
    void everyReceiverGetsTheSameOutput() {
        TestPlayer parser = new TestPlayer("Parser");

        for (String message : MESSAGES) {
            TestPlayer first = new TestPlayer("Steve");
            TestPlayer second = new TestPlayer("Alex");

            chat.send(Arrays.asList(first.handle(), second.handle()), parser.handle(), message);

            StringBuilder a = new StringBuilder();
            StringBuilder b = new StringBuilder();
            dump(a, first);
            dump(b, second);

            assertEquals(a.toString(), b.toString(), "message: " + message);
        }
    }

    /**
     * A message without placeholders must render once and be reused across receivers.
     *
     * <p>The render cache keys on the color profile, so it only engages when the context uses the
     * plain pipeline. Supplying a per-receiver formatter silently disabled it, which is invisible
     * in the output and shows up only as repeated work.
     */
    @Test
    void aStaticMessageIsRenderedOnceForEveryReceiver() {
        Element element = Element.parse("&7estatico", lib.getMarkup());

        TestPlayer first = new TestPlayer("Steve");
        TestPlayer second = new TestPlayer("Alex");

        String a = element.legacy(RenderContext.of(first.handle()));
        String b = element.legacy(RenderContext.of(second.handle()));

        assertSame(a, b, "the second receiver must reuse the cached render");
    }

    /**
     * A placeholder whose value carries formatting must still be colorized.
     *
     * <p>Pinned because resolving tokens by lookup instead of scanning moves when the value enters
     * the pipeline, and a value arriving after the color pass would ship raw codes to the client.
     */
    @Test
    void aPlaceholderValueWithFormattingIsStillColorized() {
        TestPlayer plain = new TestPlayer("Steve", "&aSteve");
        chat.send(Arrays.asList(plain.handle()), null, "hola {playerDisplayName}");

        assertEquals(1, plain.plain().size());
        assertEquals("hola §aSteve", plain.plain().get(0));
    }

    /**
     * Text formats apply to placeholder values, not only to the message around them.
     *
     * <p>This is the constraint that decides where placeholder resolution can live: the formats run
     * after the values are substituted, so resolving tokens earlier without keeping that order
     * would stop a value carrying markup from being processed.
     */
    @Test
    void aPlaceholderValueWithATextFormatIsProcessed() {
        TestPlayer tagged = new TestPlayer("Steve", "<sc>steve</sc>");
        chat.send(Arrays.asList(tagged.handle()), null, "hola {playerDisplayName}");

        assertEquals(1, tagged.plain().size());
        assertEquals("hola sᴍᴀʟʟ".replace("sᴍᴀʟʟ", "sᴛᴇᴠᴇ"), tagged.plain().get(0));
    }

    /**
     * Stripping the channel prefix must not undo the alignment pass.
     *
     * <p>Both forms carry the same visible text, so once the prefix is gone they have to produce
     * the same output.
     */
    @Test
    void theChannelPrefixDoesNotDiscardAlignment() {
        TestPlayer withPrefix = new TestPlayer("Steve");
        TestPlayer withoutPrefix = new TestPlayer("Steve");

        chat.send(Arrays.asList(withPrefix.handle()), null, "[chat][C]centrado");
        chat.send(Arrays.asList(withoutPrefix.handle()), null, "[C]centrado");

        assertEquals(withoutPrefix.plain(), withPrefix.plain());
    }

    /**
     * Without a parser each receiver parses for itself, so a placeholder resolves to that
     * receiver's own value. Pinned because it is easy to break by hoisting work out of the loop.
     */
    @Test
    void withoutAParserEachReceiverResolvesItsOwnPlaceholders() {
        TestPlayer first = new TestPlayer("Steve");
        TestPlayer second = new TestPlayer("Alex");

        chat.send(Arrays.asList(first.handle(), second.handle()), null, "&7hola {player}");

        assertEquals(1, first.plain().size());
        assertEquals(1, second.plain().size());
        assertTrue(first.plain().get(0).endsWith("Steve"), first.plain().get(0));
        assertTrue(second.plain().get(0).endsWith("Alex"), second.plain().get(0));
    }

    @SuppressWarnings("deprecation")
    private static void dump(StringBuilder out, TestPlayer player) {
        List<String> plain = player.plain();
        out.append("plain   : ").append(plain.size()).append('\n');
        for (String line : plain)
            out.append("  ").append(Snapshots.escape(line)).append('\n');

        List<BaseComponent[]> components = player.components();
        out.append("compiled: ").append(components.size()).append('\n');

        for (BaseComponent[] array : components) {
            out.append("  parts : ").append(array.length).append('\n');

            for (int i = 0; i < array.length; i++) {
                BaseComponent component = array[i];
                out.append("    [").append(i).append("] text : ")
                        .append(Snapshots.escape(component.toLegacyText())).append('\n');

                ClickEvent click = component.getClickEvent();
                if (click != null)
                    out.append("    [").append(i).append("] click: ")
                            .append(click.getAction()).append(' ')
                            .append(Snapshots.escape(click.getValue())).append('\n');

                HoverEvent hover = component.getHoverEvent();
                if (hover == null) continue;

                StringBuilder content = new StringBuilder();
                for (BaseComponent part : hover.getValue())
                    content.append(part.toLegacyText());

                out.append("    [").append(i).append("] hover: ")
                        .append(hover.getAction()).append(' ')
                        .append(Snapshots.escape(content.toString())).append('\n');
            }
        }
    }
}

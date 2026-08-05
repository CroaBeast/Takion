package me.croabeast.takion;

import me.croabeast.prismatic.element.Element;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Freezes the output of the text pipeline before the rework, so collapsing the two library copies,
 * unifying the token abstractions and routing everything through {@code Element} can be verified
 * against real values instead of intent.
 */
class PipelineSnapshotTest {

    private static final String[] INPUTS = {
            "",
            "texto plano",
            "&7con color",
            "<small_caps>small caps</small_caps>",
            "<sc>alias corto</sc>",
            "<u:2764>char tag",
            "[C]centrado",
            "<P>prefijo de lang",
            "linea uno<n>linea dos",
            "&7mezcla <sc>todo</sc> <u:2764> junto",
            "<g:ff0000>degradado</g:00ff00>",
            "&7hola {player}",
            "&7saldo %vault_eco_balance%",
            "<run:\"/spawn\">&aboton</text>",
            "<run:\"/spawn\"|hover:\"&7pista<n>&8/spawn\">&aboton</text>",
            "texto con https://example.com dentro"
    };

    private static TakionLib lib;

    @BeforeAll
    static void setUp() {
        TestServer.install();
        lib = new TakionLib(null);
    }

    @Test
    void prepareText() {
        StringBuilder out = new StringBuilder();

        for (String input : INPUTS) {
            out.append("input   : ").append(Snapshots.escape(input)).append('\n');
            out.append("prepare : ").append(Snapshots.escape(lib.prepareText(input))).append('\n');
            out.append('\n');
        }

        Snapshots.verify("prepare-text", out.toString());
    }

    @Test
    void colorize() {
        StringBuilder out = new StringBuilder();

        for (String input : INPUTS) {
            out.append("input   : ").append(Snapshots.escape(input)).append('\n');
            out.append("colorize: ").append(Snapshots.escape(lib.colorize(input))).append('\n');
            out.append('\n');
        }

        Snapshots.verify("colorize", out.toString());
    }

    @Test
    void align() {
        StringBuilder out = new StringBuilder();

        for (String input : INPUTS) {
            out.append("input   : ").append(Snapshots.escape(input)).append('\n');
            out.append("align   : ").append(Snapshots.escape(lib.getCharacterManager().align(input))).append('\n');
            out.append('\n');
        }

        Snapshots.verify("align", out.toString());
    }

    @Test
    void markup() {
        StringBuilder out = new StringBuilder();

        for (String input : INPUTS) {
            out.append("input   : ").append(Snapshots.escape(input)).append('\n');
            out.append("isMarkup: ").append(Element.isMarkup(input)).append('\n');
            out.append("strip   : ").append(Snapshots.escape(Element.stripMarkup(input))).append('\n');
            dump(out, Element.parse(input, lib.getMarkup()).bungee(lib.getMarkup().context(null)));
            out.append('\n');
        }

        Snapshots.verify("markup", out.toString());
    }

    @Test
    void registries() {
        StringBuilder out = new StringBuilder();

        out.append("tags    :\n");
        for (me.croabeast.takion.tag.Tag tag : lib.getTagManager().getTags())
            out.append("  ").append(tag.getId()).append(" -> ").append(tag.getPattern()).append('\n');

        out.append("markers :\n");
        for (String id : new String[] {"lang_prefix", "line_separator", "center_prefix"}) {
            me.croabeast.takion.marker.Marker marker = lib.getMarkerManager().getMarker(id);
            out.append("  ").append(id).append(" -> ")
                    .append(marker == null ? "<null>" : marker.getPattern().toString()).append('\n');
        }

        Snapshots.verify("registries", out.toString());
    }

    @SuppressWarnings("deprecation")
    private static void dump(StringBuilder out, BaseComponent[] components) {
        out.append("parts   : ").append(components.length).append('\n');

        for (int i = 0; i < components.length; i++) {
            BaseComponent component = components[i];
            out.append("  [").append(i).append("] text : ")
                    .append(Snapshots.escape(component.toLegacyText())).append('\n');

            ClickEvent click = component.getClickEvent();
            if (click != null)
                out.append("  [").append(i).append("] click: ")
                        .append(click.getAction()).append(' ')
                        .append(Snapshots.escape(click.getValue())).append('\n');

            HoverEvent hover = component.getHoverEvent();
            if (hover == null) continue;

            StringBuilder content = new StringBuilder();
            for (BaseComponent part : hover.getValue())
                content.append(part.toLegacyText());

            out.append("  [").append(i).append("] hover: ")
                    .append(hover.getAction()).append(' ')
                    .append(Snapshots.escape(content.toString())).append('\n');
        }
    }
}

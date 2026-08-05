package me.croabeast.prismatic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins the hex fidelity of the Adventure path.
 *
 * <p>Any string containing a {@code '<'} is routed through the Adventure bridge and comes back out
 * through {@code serialize}. That step used the plain legacy serializer, which cannot write the
 * {@code §x§r§r§g§g§b§b} form, so exact colors were silently downsampled to the nearest of sixteen
 * even for clients that support them.
 *
 * <p>Lives in Takion's test source set because PrismaticAPI has none; the composite build puts both
 * on the same classpath, so package-private access still works.
 */
class AdventureHexTest {

    private final PrismaticCore core = new PrismaticCore();
    private final Adventure adventure = new Adventure(core);

    @Test
    void hexTargetKeepsExactColors() {
        String out = adventure.colorizeLegacy("&aMezcla <#00ffaa>hex", false);

        assertEquals(
                "§aMezcla §x§0§0§f§f§a§ahex",
                out,
                "the exact color must survive the Adventure round trip"
        );
    }

    @Test
    void legacyTargetStillDownsamples() {
        String out = adventure.colorizeLegacy("&aMezcla <#00ffaa>hex", true);
        assertEquals("§aMezcla §3hex", out);
    }

    @Test
    void hexTargetMatchesThePlainPipeline() {
        // Without a '<' the string never reaches Adventure. Both routes must agree on the color.
        String direct = core.applyLegacyPipeline("#00ffaahex", false);
        String viaAdventure = adventure.colorizeLegacy("<#00ffaa>hex", false);

        assertEquals(direct, viaAdventure);
    }
}

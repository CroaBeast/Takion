package me.croabeast.takion;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Establishes whether a library instance can exist without a running server, which decides how much
 * of Takion can be covered before the rework starts.
 */
class HeadlessSmokeTest {

    @BeforeAll
    static void setUp() {
        TestServer.install();
    }

    @Test
    void libraryBuildsWithoutAPlugin() {
        TakionLib lib = new TakionLib(null);

        assertNotNull(lib.getTagManager());
        assertNotNull(lib.getMarkerManager());
        assertNotNull(lib.getFormatManager());
        assertNotNull(lib.getPlaceholderManager());
    }

    @Test
    void prepareTextRuns() {
        TakionLib lib = new TakionLib(null);
        assertNotNull(lib.prepareText("&7hola mundo"));
    }

    @Test
    void colorizeRuns() {
        TakionLib lib = new TakionLib(null);
        assertNotNull(lib.colorize("&7hola <g:ff0000>mundo</g:00ff00>"));
    }
}

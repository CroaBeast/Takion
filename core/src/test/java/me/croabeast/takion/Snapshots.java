package me.croabeast.takion;

import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Approval-style snapshot helper.
 *
 * <p>Snapshots live in {@code core/src/test/resources/snapshots}. A missing file is written on the
 * first run. Run with {@code -Dsnapshot.update=true} to accept an intentional change.
 */
public final class Snapshots {

    private static final Path ROOT = Paths.get("src", "test", "resources", "snapshots");
    private static final boolean UPDATE = Boolean.getBoolean("snapshot.update");

    private Snapshots() {}

    public static void verify(String name, String actual) {
        Path file = ROOT.resolve(name + ".txt");
        String normalized = actual.replace("\r\n", "\n");

        if (UPDATE || !Files.exists(file)) {
            write(file, normalized);
            return;
        }

        compare(name, read(file), normalized);
    }

    private static void compare(String name, String expected, String actual) {
        if (expected.equals(actual)) return;

        String[] expectedLines = expected.split("\n", -1);
        String[] actualLines = actual.split("\n", -1);
        int limit = Math.min(expectedLines.length, actualLines.length);

        for (int i = 0; i < limit; i++) {
            if (expectedLines[i].equals(actualLines[i])) continue;

            Assertions.fail(
                    "Snapshot '" + name + "' changed at line " + (i + 1) + "."
                            + "\n  expected: " + expectedLines[i]
                            + "\n  actual:   " + actualLines[i]
                            + "\nRerun with -Dsnapshot.update=true to accept."
            );
        }

        Assertions.fail(
                "Snapshot '" + name + "' changed length: expected " + expectedLines.length
                        + " lines, got " + actualLines.length + "."
        );
    }

    private static String read(Path file) {
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            return String.join("\n", lines) + "\n";
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void write(Path file, String content) {
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String escape(String value) {
        if (value == null) return "<null>";

        StringBuilder builder = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '§':
                    builder.append("[S]");
                    break;
                case '\n':
                    builder.append("[NL]");
                    break;
                default:
                    builder.append(c);
            }
        }

        return builder.toString();
    }
}

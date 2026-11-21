package ca.askthesops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class AppTest {

    private record Run(int exitCode, String output) {
    }

    private static Run run(String... args) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        int code = new App(SopLibraryTest.library()).run(args, out);
        return new Run(code, bytes.toString(StandardCharsets.UTF_8));
    }

    @Test
    void listShowsCurrentSopsOnly() throws IOException {
        Run r = run("list");
        assertEquals(0, r.exitCode());
        assertTrue(r.output().contains("SOP-CC-01"));
        assertFalse(r.output().contains("retired"));
    }

    @Test
    void listAllIncludesRetiredVersions() throws IOException {
        assertTrue(run("list", "--all").output().contains("retired"));
    }

    @Test
    void showPrintsSections() throws IOException {
        Run r = run("show", "SOP-CC-01");
        assertEquals(0, r.exitCode());
        assertTrue(r.output().contains("§4.2 Overnight excursions"));
    }

    @Test
    void showUnknownOrMissingIdFails() throws IOException {
        assertEquals(1, run("show", "SOP-NOPE-00").exitCode());
        assertEquals(1, run("show").exitCode());
    }

    @Test
    void searchPrintsCitations() throws IOException {
        Run r = run("search", "overnight", "excursion");
        assertEquals(0, r.exitCode());
        assertTrue(r.output().contains("1. SOP-CC-01 v3 §4.2 Overnight excursions"));
    }

    @Test
    void searchWithNoMatchesSaysSo() throws IOException {
        assertTrue(run("search", "fridge").output().contains("No matches"));
    }

    @Test
    void searchWithoutWordsFails() throws IOException {
        assertEquals(1, run("search").exitCode());
    }

    @Test
    void helpAndUnknownCommands() throws IOException {
        assertEquals(0, run().exitCode());
        assertEquals(0, run("help").exitCode());
        Run unknown = run("dance");
        assertEquals(1, unknown.exitCode());
        assertTrue(unknown.output().contains("Unknown command"));
    }

    @Test
    void snippetShortensLongText() {
        String longText = "word ".repeat(100);
        String snippet = App.snippet(longText);
        assertTrue(snippet.length() <= 140);
        assertTrue(snippet.endsWith("..."));
        assertEquals("short text", App.snippet("short\n  text"));
    }
}

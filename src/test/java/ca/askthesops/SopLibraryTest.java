package ca.askthesops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Uses the real SOP files in the project's sops/ folder. */
class SopLibraryTest {

    static SopLibrary library() throws IOException {
        return SopLibrary.load(Path.of("sops"));
    }

    @Test
    void loadsEveryVersion() throws IOException {
        assertEquals(6, library().all().size());
    }

    @Test
    void currentLeavesOutRetiredVersions() throws IOException {
        SopLibrary library = library();
        assertEquals(5, library.current().size());
        assertTrue(library.current().stream().allMatch(Sop::isCurrent));
    }

    @Test
    void findReturnsTheCurrentVersion() throws IOException {
        Sop cleaning = library().find("SOP-CL-03").orElseThrow();
        assertEquals(2, cleaning.version());
    }

    @Test
    void findIgnoresCase() throws IOException {
        assertTrue(library().find("sop-cc-01").isPresent());
    }

    @Test
    void findUnknownIdIsEmpty() throws IOException {
        assertFalse(library().find("SOP-NOPE-00").isPresent());
    }

    @Test
    void missingFolderIsAnError() {
        assertThrows(IOException.class, () -> SopLibrary.load(Path.of("no-such-folder")));
    }

    // SOP-13: French SOPs in sops/fr/, linked to English by id and version (ADR 0003).

    /** Writes a small fictional SOP file with the given sections ("number heading"). */
    private static void writeSop(Path file, String id, int version, String status, String... sections)
            throws IOException {
        StringBuilder md = new StringBuilder("---\nid: " + id + "\ntitle: Sample " + id + "\nversion: " + version
                + "\neffective: 2026-01-01\nstatus: " + status + "\n---\n# Sample\n\n");
        for (String section : sections) {
            md.append("## ").append(section).append("\nBody text.\n\n");
        }
        Files.createDirectories(file.getParent());
        Files.writeString(file, md.toString(), StandardCharsets.UTF_8);
    }

    @Test
    void linksFrenchFileWithSameIdAndVersionAsCurrentEnglish(@TempDir Path dir) throws IOException {
        writeSop(dir.resolve("SOP-XX-01.md"), "SOP-XX-01", 2, "current", "1. Purpose", "2. Steps");
        writeSop(dir.resolve("fr/SOP-XX-01.md"), "SOP-XX-01", 2, "current", "1. Objet", "2. Étapes");

        SopLibrary library = SopLibrary.load(dir);
        Sop english = library.find("SOP-XX-01").orElseThrow();

        Sop french = library.frenchVersionOf(english).orElseThrow();
        assertEquals("SOP-XX-01", french.id());
        assertEquals(2, french.version());
        assertEquals("Étapes", french.sections().get(1).heading());
        assertEquals(List.of(french), library.currentFrench());
        assertTrue(library.warnings().isEmpty());
    }

    @Test
    void frenchFileWithNoMatchingCurrentEnglishIsNotSearchableAndIsReported(@TempDir Path dir)
            throws IOException {
        writeSop(dir.resolve("SOP-XX-01.md"), "SOP-XX-01", 2, "current", "1. Purpose");
        writeSop(dir.resolve("SOP-XX-01-v1.md"), "SOP-XX-01", 1, "retired", "1. Purpose");
        writeSop(dir.resolve("fr/SOP-XX-01-v1.md"), "SOP-XX-01", 1, "current", "1. Objet");
        writeSop(dir.resolve("fr/SOP-ZZ-09.md"), "SOP-ZZ-09", 1, "current", "1. Objet");

        SopLibrary library = SopLibrary.load(dir);

        assertTrue(library.currentFrench().isEmpty());
        assertTrue(library.frenchVersionOf(library.find("SOP-XX-01").orElseThrow()).isEmpty());
        assertEquals(2, library.warnings().size());
        assertTrue(library.warnings().stream().anyMatch(w -> w.contains("SOP-XX-01-v1.md")));
        assertTrue(library.warnings().stream().anyMatch(w -> w.contains("SOP-ZZ-09.md")));
    }

    @Test
    void frenchFileWithDifferentSectionNumbersIsReported(@TempDir Path dir) throws IOException {
        writeSop(dir.resolve("SOP-XX-01.md"), "SOP-XX-01", 2, "current", "1. Purpose", "2. Steps", "3. Records");
        writeSop(dir.resolve("fr/SOP-XX-01.md"), "SOP-XX-01", 2, "current", "1. Objet", "2. Étapes", "4. Registres");

        SopLibrary library = SopLibrary.load(dir);

        assertEquals(1, library.warnings().size());
        String warning = library.warnings().get(0);
        assertTrue(warning.contains("SOP-XX-01.md"), warning);
        assertTrue(warning.contains("3"), warning);
        assertTrue(warning.contains("4"), warning);
    }

    @Test
    void frenchFolderDoesNotChangeEnglishSops(@TempDir Path dir) throws IOException {
        writeSop(dir.resolve("SOP-XX-01.md"), "SOP-XX-01", 2, "current", "1. Purpose");
        SopLibrary englishOnly = SopLibrary.load(dir);

        writeSop(dir.resolve("fr/SOP-XX-01.md"), "SOP-XX-01", 2, "current", "1. Objet");
        SopLibrary withFrench = SopLibrary.load(dir);

        assertEquals(englishOnly.all(), withFrench.all());
        assertEquals(englishOnly.current(), withFrench.current());
        assertEquals("Purpose", withFrench.find("SOP-XX-01").orElseThrow().sections().get(0).heading());
    }

    @Test
    void noFrenchFolderMeansNoFrenchSopsAndNoWarnings(@TempDir Path dir) throws IOException {
        writeSop(dir.resolve("SOP-XX-01.md"), "SOP-XX-01", 2, "current", "1. Purpose");

        SopLibrary library = SopLibrary.load(dir);

        assertTrue(library.currentFrench().isEmpty());
        assertTrue(library.warnings().isEmpty());
        assertEquals(1, library.current().size());
    }
}

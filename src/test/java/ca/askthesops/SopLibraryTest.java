package ca.askthesops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

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
}

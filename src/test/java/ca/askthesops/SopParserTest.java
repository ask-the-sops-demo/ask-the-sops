package ca.askthesops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class SopParserTest {

    private static final String SAMPLE = """
            ---
            id: SOP-XX-99
            title: Sample Procedure
            version: 4
            effective: 2026-05-01
            status: current
            ---
            # Sample Procedure

            > Notice text that belongs to no section.

            ## 1. Purpose
            Why this exists.

            ## 4.2 Overnight checks
            Line one.
            Line two.

            ## Appendix
            Extra notes.
            """;

    @Test
    void readsFrontMatter() {
        Sop sop = SopParser.parse(SAMPLE, "sample.md");
        assertEquals("SOP-XX-99", sop.id());
        assertEquals("Sample Procedure", sop.title());
        assertEquals(4, sop.version());
        assertEquals(LocalDate.of(2026, 5, 1), sop.effectiveDate());
        assertTrue(sop.isCurrent());
    }

    @Test
    void splitsSectionsAndKeepsTheirNumbers() {
        Sop sop = SopParser.parse(SAMPLE, "sample.md");
        assertEquals(3, sop.sections().size());
        assertEquals("1", sop.sections().get(0).number());
        assertEquals("Purpose", sop.sections().get(0).heading());
        assertEquals("4.2", sop.sections().get(1).number());
        assertEquals("Overnight checks", sop.sections().get(1).heading());
        assertEquals("Line one.\nLine two.", sop.sections().get(1).text());
    }

    @Test
    void headingWithoutNumberHasEmptyNumber() {
        Section appendix = SopParser.parse(SAMPLE, "sample.md").sections().get(2);
        assertEquals("", appendix.number());
        assertEquals("Appendix", appendix.label());
    }

    @Test
    void textBeforeFirstSectionIsIgnored() {
        Sop sop = SopParser.parse(SAMPLE, "sample.md");
        assertTrue(sop.sections().stream().noneMatch(s -> s.text().contains("Notice text")));
    }

    @Test
    void handlesWindowsLineEndings() {
        Sop sop = SopParser.parse(SAMPLE.replace("\n", "\r\n"), "sample.md");
        assertEquals("Line one.\nLine two.", sop.sections().get(1).text());
    }

    @Test
    void statusDefaultsToCurrent() {
        Sop sop = SopParser.parse(SAMPLE.replace("status: current\n", ""), "sample.md");
        assertTrue(sop.isCurrent());
    }

    @Test
    void missingIdIsAnError() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> SopParser.parse(SAMPLE.replace("id: SOP-XX-99\n", ""), "broken.md"));
        assertTrue(e.getMessage().contains("broken.md"));
        assertTrue(e.getMessage().contains("'id'"));
    }

    @Test
    void missingFrontMatterIsAnError() {
        assertThrows(IllegalArgumentException.class, () -> SopParser.parse("# Just a title\n", "bad.md"));
    }

    @Test
    void unclosedFrontMatterIsAnError() {
        assertThrows(IllegalArgumentException.class, () -> SopParser.parse("---\nid: X\n", "bad.md"));
    }

    @Test
    void badVersionOrDateIsAnError() {
        assertThrows(IllegalArgumentException.class,
                () -> SopParser.parse(SAMPLE.replace("version: 4", "version: four"), "bad.md"));
        assertThrows(IllegalArgumentException.class,
                () -> SopParser.parse(SAMPLE.replace("2026-05-01", "May 1"), "bad.md"));
    }
}

package ca.askthesops;

import java.time.LocalDate;
import java.util.List;

/**
 * A standard operating procedure (SOP), as loaded from one Markdown file in {@code sops/}.
 *
 * @param id            the SOP's identifier, e.g. "SOP-CC-01"; several versions can share an id
 * @param title         the SOP's title
 * @param version       the version number; higher is newer
 * @param effectiveDate the date this version took effect
 * @param status        "current" or "retired"
 * @param sections      the numbered sections, in document order
 */
public record Sop(String id, String title, int version, LocalDate effectiveDate, String status, List<Section> sections) {

    public Sop {
        sections = List.copyOf(sections);
    }

    /** True if this version is the one staff should follow today. */
    public boolean isCurrent() {
        return "current".equalsIgnoreCase(status);
    }
}

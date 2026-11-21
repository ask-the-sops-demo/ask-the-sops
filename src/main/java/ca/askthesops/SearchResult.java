package ca.askthesops;

/**
 * One section that matched a search, with its score.
 *
 * @param sop     the SOP the section belongs to
 * @param section the matching section
 * @param score   how well it matched; higher is better
 */
public record SearchResult(Sop sop, Section section, double score) {

    /** A short, checkable reference, e.g. "SOP-CC-01 v3 §4.2 Overnight excursions". */
    public String citation() {
        return sop.id() + " v" + sop.version() + " " + section.label();
    }
}

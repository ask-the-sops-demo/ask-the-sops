package ca.askthesops;

/**
 * One numbered section of an SOP, e.g. "4.2 Overnight excursions".
 *
 * @param number  the section number as written in the document, e.g. "4.2"; empty if the heading has no number
 * @param heading the heading text without the number
 * @param text    the body text of the section
 */
public record Section(String number, String heading, String text) {

    /** The heading as it would appear in a citation, e.g. "§4.2 Overnight excursions". */
    public String label() {
        return number.isEmpty() ? heading : "§" + number + " " + heading;
    }
}

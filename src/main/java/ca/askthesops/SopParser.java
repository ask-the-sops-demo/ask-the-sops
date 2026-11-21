package ca.askthesops;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Reads an SOP from Markdown.
 *
 * <p>The expected format is a front-matter block between two {@code ---} lines, followed by the
 * document. Each {@code ## } heading starts a new section. Text before the first {@code ## }
 * heading (the title and the notice) is not part of any section.
 *
 * <pre>
 * ---
 * id: SOP-CC-01
 * title: Refrigerated Medication: Temperature Excursions
 * version: 3
 * effective: 2026-03-01
 * status: current
 * ---
 * # Refrigerated Medication: Temperature Excursions
 *
 * ## 4.2 Overnight excursions
 * Body text...
 * </pre>
 */
public final class SopParser {

    private static final Pattern SECTION_NUMBER = Pattern.compile("\\d+(\\.\\d+)*\\.?");

    private SopParser() {
    }

    /** Parses one SOP file. */
    public static Sop parse(Path file) throws IOException {
        return parse(Files.readString(file, StandardCharsets.UTF_8), file.getFileName().toString());
    }

    /**
     * Parses SOP Markdown.
     *
     * @param markdown   the file contents
     * @param sourceName a name for error messages, usually the file name
     * @throws IllegalArgumentException if the front matter is missing or invalid
     */
    public static Sop parse(String markdown, String sourceName) {
        List<String> lines = markdown.replace("\r\n", "\n").lines().toList();
        if (lines.isEmpty() || !lines.get(0).trim().equals("---")) {
            throw new IllegalArgumentException(sourceName + ": must start with a '---' front-matter line");
        }
        int end = -1;
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).trim().equals("---")) {
                end = i;
                break;
            }
        }
        if (end < 0) {
            throw new IllegalArgumentException(sourceName + ": front matter is not closed with '---'");
        }

        Map<String, String> meta = new HashMap<>();
        for (String line : lines.subList(1, end)) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                meta.put(line.substring(0, colon).trim().toLowerCase(), line.substring(colon + 1).trim());
            }
        }

        String id = required(meta, "id", sourceName);
        String title = required(meta, "title", sourceName);
        int version;
        try {
            version = Integer.parseInt(required(meta, "version", sourceName));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(sourceName + ": 'version' must be a whole number");
        }
        LocalDate effective;
        try {
            effective = LocalDate.parse(required(meta, "effective", sourceName));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(sourceName + ": 'effective' must be a date like 2026-03-01");
        }
        String status = meta.getOrDefault("status", "current");

        return new Sop(id, title, version, effective, status, sections(lines.subList(end + 1, lines.size())));
    }

    private static List<Section> sections(List<String> body) {
        List<Section> sections = new ArrayList<>();
        String number = null;
        String heading = null;
        StringBuilder text = new StringBuilder();
        for (String line : body) {
            if (line.startsWith("## ")) {
                if (heading != null) {
                    sections.add(new Section(number, heading, text.toString().trim()));
                }
                String rest = line.substring(3).trim();
                String first = rest.split("\\s+", 2)[0];
                if (SECTION_NUMBER.matcher(first).matches() && rest.length() > first.length()) {
                    number = first.endsWith(".") ? first.substring(0, first.length() - 1) : first;
                    heading = rest.substring(first.length()).trim();
                } else {
                    number = "";
                    heading = rest;
                }
                text.setLength(0);
            } else if (heading != null) {
                text.append(line).append('\n');
            }
        }
        if (heading != null) {
            sections.add(new Section(number, heading, text.toString().trim()));
        }
        return sections;
    }

    private static String required(Map<String, String> meta, String key, String sourceName) {
        String value = meta.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(sourceName + ": front matter is missing '" + key + "'");
        }
        return value;
    }
}

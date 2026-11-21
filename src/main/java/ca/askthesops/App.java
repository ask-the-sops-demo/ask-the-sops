package ca.askthesops;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Command-line entry point.
 *
 * <pre>
 * java -jar target/ask-the-sops.jar list            current SOPs
 * java -jar target/ask-the-sops.jar list --all      every version, including retired ones
 * java -jar target/ask-the-sops.jar show SOP-CC-01  one SOP, section by section
 * java -jar target/ask-the-sops.jar search fridge   keyword search over current SOPs
 * </pre>
 *
 * <p>SOPs are read from the {@code sops/} folder, or from the folder named by the
 * {@code sops.dir} system property.
 */
public final class App {

    static final int SEARCH_LIMIT = 5;
    private static final int SNIPPET_LENGTH = 140;

    private final SopLibrary library;

    public App(SopLibrary library) {
        this.library = library;
    }

    public static void main(String[] args) throws IOException {
        SopLibrary library = SopLibrary.load(Path.of(System.getProperty("sops.dir", "sops")));
        PrintStream out = new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8);
        int exitCode = new App(library).run(args, out);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    /** Runs one command and returns the process exit code (0 = success). */
    int run(String[] args, PrintStream out) {
        if (args.length == 0) {
            printHelp(out);
            return 0;
        }
        String command = args[0];
        List<String> rest = Arrays.asList(args).subList(1, args.length);
        return switch (command) {
            case "list" -> list(rest.contains("--all"), out);
            case "show" -> show(rest, out);
            case "search" -> search(String.join(" ", rest), out);
            case "help", "--help", "-h" -> {
                printHelp(out);
                yield 0;
            }
            default -> {
                out.println("Unknown command: " + command);
                printHelp(out);
                yield 1;
            }
        };
    }

    private int list(boolean all, PrintStream out) {
        List<Sop> sops = all ? library.all() : library.current();
        for (Sop sop : sops) {
            out.printf("%-10s v%-2d %s  %-8s %s%n", sop.id(), sop.version(), sop.effectiveDate(),
                    sop.status(), sop.title());
        }
        return 0;
    }

    private int show(List<String> rest, PrintStream out) {
        if (rest.isEmpty()) {
            out.println("Usage: show <SOP-ID>");
            return 1;
        }
        Optional<Sop> found = library.find(rest.get(0));
        if (found.isEmpty()) {
            out.println("No SOP with id " + rest.get(0));
            return 1;
        }
        Sop sop = found.get();
        out.println(sop.id() + " v" + sop.version() + ": " + sop.title());
        out.println("Effective " + sop.effectiveDate() + " (" + sop.status() + ")");
        for (Section section : sop.sections()) {
            out.println();
            out.println(section.label());
            out.println(section.text());
        }
        return 0;
    }

    private int search(String query, PrintStream out) {
        if (query.isBlank()) {
            out.println("Usage: search <words>");
            return 1;
        }
        List<SearchResult> results = new KeywordSearch(library.current()).search(query, SEARCH_LIMIT);
        if (results.isEmpty()) {
            out.println("No matches for: " + query);
            return 0;
        }
        int rank = 1;
        for (SearchResult result : results) {
            out.printf("%d. %s  (score %.2f)%n", rank++, result.citation(), result.score());
            out.println("   " + snippet(result.section().text()));
        }
        return 0;
    }

    static String snippet(String text) {
        String flat = text.replaceAll("\\s+", " ").trim();
        return flat.length() <= SNIPPET_LENGTH ? flat : flat.substring(0, SNIPPET_LENGTH - 3) + "...";
    }

    private static void printHelp(PrintStream out) {
        out.println("Ask the SOPs: search fictional pharmacy SOPs (training data only)");
        out.println();
        out.println("Commands:");
        out.println("  list            List the current SOPs");
        out.println("  list --all      List every version, including retired ones");
        out.println("  show <SOP-ID>   Show one SOP, section by section");
        out.println("  search <words>  Keyword search over the current SOPs");
    }
}

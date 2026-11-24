package ca.askthesops;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * All the SOPs loaded from a folder, including retired versions.
 *
 * <p>French translations live in the {@code fr/} subfolder (ADR 0003). A French file is linked to
 * the English SOP with the same id and version, and only while that English version is current.
 * French files that can't be linked, or whose section numbers differ from the English version,
 * are listed in {@link #warnings()}.
 */
public final class SopLibrary {

    static final String FRENCH_FOLDER = "fr";

    private final List<Sop> sops;
    private final Map<String, Sop> frenchByKey;
    private final List<String> warnings;

    public SopLibrary(List<Sop> sops) {
        this(sops, Map.of(), List.of());
    }

    private SopLibrary(List<Sop> sops, Map<String, Sop> frenchByKey, List<String> warnings) {
        this.sops = sops.stream()
                .sorted(Comparator.comparing(Sop::id).thenComparing(Sop::version))
                .toList();
        this.frenchByKey = Map.copyOf(frenchByKey);
        this.warnings = List.copyOf(warnings);
    }

    /**
     * Loads every {@code .md} file in a folder (not its subfolders), plus French translations
     * from its {@code fr/} subfolder if there is one.
     */
    public static SopLibrary load(Path folder) throws IOException {
        if (!Files.isDirectory(folder)) {
            throw new IOException("SOP folder not found: " + folder.toAbsolutePath());
        }
        List<Sop> english = loadFolder(folder).stream().map(Loaded::sop).toList();

        Map<String, Sop> currentEnglish = new HashMap<>();
        for (Sop sop : english) {
            if (sop.isCurrent()) {
                currentEnglish.put(key(sop), sop);
            }
        }

        Map<String, Sop> french = new HashMap<>();
        List<String> warnings = new ArrayList<>();
        Path frenchFolder = folder.resolve(FRENCH_FOLDER);
        if (Files.isDirectory(frenchFolder)) {
            for (Loaded loaded : loadFolder(frenchFolder)) {
                Sop translation = loaded.sop();
                String source = FRENCH_FOLDER + "/" + loaded.file().getFileName();
                Sop original = currentEnglish.get(key(translation));
                if (original == null) {
                    warnings.add(source + ": no current English SOP " + translation.id() + " v"
                            + translation.version() + ", so this French file is not searchable");
                    continue;
                }
                sectionMismatch(original, translation).ifPresent(problem -> warnings.add(source + ": " + problem));
                french.put(key(original), translation);
            }
        }
        return new SopLibrary(english, french, warnings);
    }

    private record Loaded(Path file, Sop sop) {
    }

    private static List<Loaded> loadFolder(Path folder) throws IOException {
        List<Loaded> loaded = new ArrayList<>();
        try (Stream<Path> files = Files.list(folder)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".md") && Files.isRegularFile(f))
                    .sorted().toList()) {
                loaded.add(new Loaded(file, SopParser.parse(file)));
            }
        }
        return loaded;
    }

    private static String key(Sop sop) {
        return sop.id().toLowerCase(Locale.ROOT) + "#" + sop.version();
    }

    private static Optional<String> sectionMismatch(Sop english, Sop french) {
        List<String> englishNumbers = english.sections().stream().map(Section::number).toList();
        List<String> frenchNumbers = french.sections().stream().map(Section::number).toList();
        if (englishNumbers.equals(frenchNumbers)) {
            return Optional.empty();
        }
        Set<String> onlyEnglish = new LinkedHashSet<>(englishNumbers);
        onlyEnglish.removeAll(frenchNumbers);
        Set<String> onlyFrench = new LinkedHashSet<>(frenchNumbers);
        onlyFrench.removeAll(englishNumbers);
        String detail = onlyEnglish.isEmpty() && onlyFrench.isEmpty()
                ? "the same numbers are in a different order"
                : "only in English: " + describe(onlyEnglish) + "; only in French: " + describe(onlyFrench);
        return Optional.of("section numbers differ from English " + english.id() + " v" + english.version()
                + " (" + detail + ")");
    }

    private static String describe(Set<String> numbers) {
        return numbers.isEmpty() ? "none" : String.join(", ", numbers);
    }

    /** Every SOP version, sorted by id then version. */
    public List<Sop> all() {
        return sops;
    }

    /** Only the versions staff should follow today. */
    public List<Sop> current() {
        return sops.stream().filter(Sop::isCurrent).toList();
    }

    /**
     * Finds an SOP by id (ignoring case). Returns the current version if there is one,
     * otherwise the newest version.
     */
    public Optional<Sop> find(String id) {
        List<Sop> matches = sops.stream().filter(s -> s.id().equalsIgnoreCase(id)).toList();
        return matches.stream().filter(Sop::isCurrent).findFirst()
                .or(() -> matches.stream().max(Comparator.comparing(Sop::version)));
    }

    /** The French translation of a current English SOP, if one is linked. */
    public Optional<Sop> frenchVersionOf(Sop english) {
        return Optional.ofNullable(frenchByKey.get(key(english)));
    }

    /** French translations of the current English SOPs, in the same order as {@link #current()}. */
    public List<Sop> currentFrench() {
        return current().stream().map(this::frenchVersionOf).flatMap(Optional::stream).toList();
    }

    /** Problems found while loading French files; each one names the file. */
    public List<String> warnings() {
        return warnings;
    }
}

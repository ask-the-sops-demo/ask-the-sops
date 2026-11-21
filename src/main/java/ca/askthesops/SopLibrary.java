package ca.askthesops;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/** All the SOPs loaded from a folder, including retired versions. */
public final class SopLibrary {

    private final List<Sop> sops;

    public SopLibrary(List<Sop> sops) {
        this.sops = sops.stream()
                .sorted(Comparator.comparing(Sop::id).thenComparing(Sop::version))
                .toList();
    }

    /** Loads every {@code .md} file in a folder (not its subfolders). */
    public static SopLibrary load(Path folder) throws IOException {
        if (!Files.isDirectory(folder)) {
            throw new IOException("SOP folder not found: " + folder.toAbsolutePath());
        }
        List<Sop> loaded = new ArrayList<>();
        try (Stream<Path> files = Files.list(folder)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".md")).sorted().toList()) {
                loaded.add(SopParser.parse(file));
            }
        }
        return new SopLibrary(loaded);
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
}

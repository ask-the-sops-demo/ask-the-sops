package ca.askthesops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * A simple keyword search over SOP sections, scored with TF-IDF.
 *
 * <p>Each section is scored by how often it contains each query word (term frequency),
 * weighted by how rare that word is across all sections (inverse document frequency).
 * Words in a section's heading count twice.
 *
 * <p>Known limitations, on purpose: no stemming ("excursions" does not match "excursion"),
 * no synonyms ("fridge" does not match "refrigerator") and no understanding of meaning.
 * These are good first stories for the workshop, and the reason RAG helps.
 */
public final class KeywordSearch {

    static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "can", "do", "does", "for", "from",
            "how", "i", "if", "in", "is", "it", "my", "of", "on", "or", "our", "should", "that",
            "the", "this", "to", "we", "what", "when", "with");

    private final List<Indexed> index = new ArrayList<>();
    private final Map<String, Integer> documentFrequency = new HashMap<>();

    private record Indexed(Sop sop, Section section, Map<String, Integer> termCounts) {
    }

    /** Builds the index over every section of the given SOPs. */
    public KeywordSearch(List<Sop> sops) {
        for (Sop sop : sops) {
            for (Section section : sop.sections()) {
                Map<String, Integer> counts = new HashMap<>();
                for (String term : tokenize(section.heading())) {
                    counts.merge(term, 2, Integer::sum);
                }
                for (String term : tokenize(section.text())) {
                    counts.merge(term, 1, Integer::sum);
                }
                index.add(new Indexed(sop, section, counts));
                for (String term : counts.keySet()) {
                    documentFrequency.merge(term, 1, Integer::sum);
                }
            }
        }
    }

    /**
     * Returns the best-matching sections, best first.
     *
     * @param query the user's words
     * @param limit the most results to return
     */
    public List<SearchResult> search(String query, int limit) {
        Set<String> terms = new LinkedHashSet<>(tokenize(query));
        if (terms.isEmpty() || limit <= 0) {
            return List.of();
        }
        int sectionCount = index.size();
        List<SearchResult> results = new ArrayList<>();
        for (Indexed entry : index) {
            double score = 0;
            for (String term : terms) {
                int tf = entry.termCounts().getOrDefault(term, 0);
                if (tf > 0) {
                    double idf = Math.log(1.0 + (double) sectionCount / documentFrequency.get(term));
                    score += tf * idf;
                }
            }
            if (score > 0) {
                results.add(new SearchResult(entry.sop(), entry.section(), score));
            }
        }
        results.sort(Comparator.comparingDouble(SearchResult::score).reversed()
                .thenComparing(r -> r.sop().id())
                .thenComparing(r -> r.section().number()));
        return results.subList(0, Math.min(limit, results.size()));
    }

    /** Lower-cases text, splits it into words and numbers, and drops common stop words. */
    static List<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))
                .filter(t -> !t.isEmpty())
                .filter(t -> !STOP_WORDS.contains(t))
                .toList();
    }
}

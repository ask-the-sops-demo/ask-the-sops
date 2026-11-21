package ca.askthesops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class KeywordSearchTest {

    private static KeywordSearch search() throws IOException {
        return new KeywordSearch(SopLibraryTest.library().current());
    }

    @Test
    void tokenizeLowercasesAndDropsStopWords() {
        assertEquals(List.of("overnight", "refrigerator", "excursion"),
                KeywordSearch.tokenize("What is the OVERNIGHT refrigerator excursion?"));
    }

    @Test
    void tokenizeKeepsNumbers() {
        assertEquals(List.of("din", "99990001"), KeywordSearch.tokenize("DIN 99990001"));
    }

    @Test
    void findsTheOvernightExcursionSection() throws IOException {
        SearchResult top = search().search("overnight refrigerator excursion", 5).get(0);
        assertEquals("SOP-CC-01", top.sop().id());
        assertEquals("4.2", top.section().number());
        assertEquals("SOP-CC-01 v3 §4.2 Overnight excursions", top.citation());
    }

    @Test
    void findsAnExactDin() throws IOException {
        SearchResult top = search().search("DIN 99990001", 5).get(0);
        assertEquals("SOP-DE-04", top.sop().id());
    }

    @Test
    void resultsAreBestFirstAndLimited() throws IOException {
        List<SearchResult> results = search().search("pharmacist", 3);
        assertEquals(3, results.size());
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i - 1).score() >= results.get(i).score());
        }
    }

    @Test
    void searchesOnlyTheVersionsItWasGiven() throws IOException {
        List<SearchResult> results = search().search("cleaning", 10);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(r -> r.sop().isCurrent()));
    }

    @Test
    void stopWordsOnlyOrEmptyQueryFindsNothing() throws IOException {
        assertTrue(search().search("what is the", 5).isEmpty());
        assertTrue(search().search("", 5).isEmpty());
        assertTrue(search().search("pharmacist", 0).isEmpty());
    }

    /**
     * Documents a known limitation: there are no synonyms yet, so "fridge" finds nothing
     * even though SOP-CC-01 is about refrigerators. Fixing this is a good first workshop story;
     * when you do, replace this test with one that expects SOP-CC-01.
     */
    @Test
    void knownLimitationFridgeFindsNothingYet() throws IOException {
        assertTrue(search().search("fridge", 5).isEmpty());
    }
}

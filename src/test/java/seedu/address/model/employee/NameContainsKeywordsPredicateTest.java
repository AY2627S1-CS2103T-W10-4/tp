package seedu.address.model.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.EmployeeBuilder;

public class NameContainsKeywordsPredicateTest {

    @Test
    public void equals() {
        List<String> firstPredicateKeywordList = List.of("first");
        List<String> secondPredicateKeywordList = List.of("first", "second");

        NameContainsKeywordsPredicate firstPredicate = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        NameContainsKeywordsPredicate secondPredicate = new NameContainsKeywordsPredicate(secondPredicateKeywordList);

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same values -> returns true
        NameContainsKeywordsPredicate firstPredicateCopy = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        assertTrue(firstPredicate.equals(firstPredicateCopy));

        // different types -> returns false
        assertFalse(firstPredicate.equals(1));

        // null -> returns false
        assertFalse(firstPredicate.equals(null));

        // different employee -> returns false
        assertFalse(firstPredicate.equals(secondPredicate));
    }

    @Test
    public void test_nameContainsKeywords_returnsTrue() {
        // One keyword
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("Alice"));
        assertTrue(predicate.test(new EmployeeBuilder().withName("Alice Bob").build()));

        // Multiple keywords
        predicate = new NameContainsKeywordsPredicate(List.of("Alice", "Bob"));
        assertTrue(predicate.test(new EmployeeBuilder().withName("Alice Bob").build()));

        // Only one matching keyword
        predicate = new NameContainsKeywordsPredicate(List.of("Bob", "Carol"));
        assertTrue(predicate.test(new EmployeeBuilder().withName("Alice Carol").build()));

        // Mixed-case keywords
        predicate = new NameContainsKeywordsPredicate(List.of("aLIce", "bOB"));
        assertTrue(predicate.test(new EmployeeBuilder().withName("Alice Bob").build()));
    }

    @Test
    public void test_nameDoesNotContainKeywords_returnsFalse() {
        // Zero keywords
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of());
        assertFalse(predicate.test(new EmployeeBuilder().withName("Alice").build()));

        // Non-matching keyword
        predicate = new NameContainsKeywordsPredicate(List.of("Carol"));
        assertFalse(predicate.test(new EmployeeBuilder().withName("Alice Bob").build()));

        // Keywords match phone, email and address, but do not match name
        predicate = new NameContainsKeywordsPredicate(List.of("12345", "alice@email.com", "Main", "Street"));
        assertFalse(predicate.test(new EmployeeBuilder().withName("Alice").withPhone("12345")
                .withEmail("alice@email.com").withDepartment("Main Street").build()));
    }

    private static boolean matches(String name, String... keywords) {
        return new NameContainsKeywordsPredicate(List.of(keywords)).test(new EmployeeBuilder().withName(name).build());
    }

    @Test
    public void test_surnameBeforeComma_matchesWithAndWithoutTheComma() {
        assertTrue(matches("Tan, Ah Kow", "Tan"));
        assertTrue(matches("Tan, Ah Kow", "Tan,"));
        assertTrue(matches("Tan, Ah Kow", "Ah"));
        assertTrue(matches("Tan, Ah Kow", "kow"));
        assertFalse(matches("Tan, Ah Kow", "Ta"));
    }

    @Test
    public void test_titleAndSuffixWithFullStops_matchesWithAndWithoutTheFullStop() {
        assertTrue(matches("Dr. John Tan Jr.", "Dr"));
        assertTrue(matches("Dr. John Tan Jr.", "Dr."));
        assertTrue(matches("Dr. John Tan Jr.", "Jr"));
        assertTrue(matches("Dr. John Tan Jr.", "Jr."));
        assertTrue(matches("Dr. John Tan Jr.", "John"));
    }

    @Test
    public void test_hyphenatedAndApostropheNames() {
        assertTrue(matches("Mary-Ann O'Brien", "Mary"));
        assertTrue(matches("Mary-Ann O'Brien", "Ann"));
        assertTrue(matches("Mary-Ann O'Brien", "Mary-Ann"));
        assertTrue(matches("Mary-Ann O'Brien", "O'Brien"));
        // the apostrophe is part of the word, so only a part after it is not a whole word
        assertFalse(matches("Mary-Ann O'Brien", "Brien"));
        // only part of a word
        assertFalse(matches("Mary-Ann O'Brien", "Mar"));
        assertFalse(matches("Mary-Ann O'Brien", "Maryann"));
    }

    @Test
    public void test_keywordWithSeveralWords_allItsWordsMustBeInTheName() {
        assertTrue(matches("Ann Mary Smith", "Mary-Ann"));
        assertFalse(matches("Mary Smith", "Mary-Ann"));
    }

    @Test
    public void test_keywordsSeparatedByCommas_eachKeywordMatchesItsOwnWord() {
        // what the parser makes of "find Ah, Mary-Ann"
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("Ah,", "Mary-Ann"));
        assertTrue(predicate.test(new EmployeeBuilder().withName("Tan, Ah Kow").build()));
        assertTrue(predicate.test(new EmployeeBuilder().withName("Mary-Ann O'Brien").build()));
        assertFalse(predicate.test(new EmployeeBuilder().withName("Bo Yang").build()));
    }

    @Test
    public void test_relationWordsInNames() {
        assertTrue(matches("Ravi s/o Kumar", "s/o"));
        assertTrue(matches("Ravi s/o Kumar", "Kumar"));
        assertTrue(matches("Nur Aisha d/o Ali", "d/o"));
        // the words s and o are not words of this name
        assertFalse(matches("Sam Oliver", "s/o"));
    }

    @Test
    public void test_ignoresAccents() {
        assertTrue(matches("\u00C9mile Roux", "emile"));
        assertTrue(matches("\u00C9mile Roux", "EMILE"));
        assertTrue(matches("\u00C9mile Roux", "\u00C9mile"));
        // a name without the accent is found with the accent too
        assertTrue(matches("Emile Roux", "\u00C9mile"));
        assertTrue(matches("Jos\u00E9 N\u00FA\u00F1ez", "jose", "nunez"));
        assertFalse(matches("\u00C9mile Roux", "emil"));
    }

    @Test
    public void test_decomposedAccentInTheKeyword_sameAsTheAccentOnTheLetter() {
        assertTrue(matches("\u00C9mile Roux", "E\u0301mile"));
        assertTrue(matches("E\u0301mile Roux", "\u00C9mile"));
    }

    @Test
    public void test_keywordWithoutALetterOrDigit_neverMatches() {
        assertFalse(matches("Alice Bob", "-"));
        assertFalse(matches("Alice Bob", "@@@"));
        assertFalse(matches("Alice Bob", "..."));
        assertFalse(new NameContainsKeywordsPredicate(List.of()).test(new EmployeeBuilder().build()));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(keywords);

        String expected = NameContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }
}

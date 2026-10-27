package seedu.address.model.employee;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Employee}'s {@code Name} matches any of the keywords given.
 *
 * <p>The name and each keyword are split into words, see {@link StringUtil#getSearchWords(String)}: spaces and
 * punctuation separate words, and letter case and accents do not matter. A keyword matches when every one of its
 * words is a whole word of the name, so {@code Tan} matches {@code Tan, Ah Kow}, {@code Mary} and {@code Mary-Ann}
 * match {@code Mary-Ann O'Brien}, and {@code Han} does not match {@code Hans}.
 */
public class NameContainsKeywordsPredicate implements Predicate<Employee> {
    private final List<String> keywords;

    // the words of each keyword that has a word, worked out once as the keywords do not change
    private final List<List<String>> wordsOfKeywords;

    /**
     * Creates a predicate that tests for any of the {@code keywords}. A keyword without a word, such as {@code @},
     * never matches.
     */
    public NameContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = keywords;
        this.wordsOfKeywords = keywords.stream()
                .map(StringUtil::getSearchWords)
                .filter(words -> !words.isEmpty())
                .toList();
    }

    @Override
    public boolean test(Employee employee) {
        Set<String> wordsOfName = new HashSet<>(StringUtil.getSearchWords(employee.getName().fullName));
        return wordsOfKeywords.stream().anyMatch(wordsOfName::containsAll);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof NameContainsKeywordsPredicate otherNameContainsKeywordsPredicate)) {
            return false;
        }

        return keywords.equals(otherNameContainsKeywordsPredicate.keywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}

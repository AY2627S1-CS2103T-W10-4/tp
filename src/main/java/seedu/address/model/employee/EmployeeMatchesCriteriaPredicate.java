package seedu.address.model.employee;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that an {@code Employee}'s department and role match all the criteria that were given.
 *
 * <p>Each criterion is a list of words. A criterion is met when every one of its words is a whole word of the
 * corresponding field, ignoring case. A criterion with no words is not applied. All applied criteria must be met
 * (an {@code AND} search).
 */
public class EmployeeMatchesCriteriaPredicate implements Predicate<Employee> {

    private final List<String> departmentWords;
    private final List<String> roleWords;

    /**
     * Creates a predicate from the words of each criterion. At least one criterion must have a word.
     */
    public EmployeeMatchesCriteriaPredicate(List<String> departmentWords, List<String> roleWords) {
        requireNonNull(departmentWords);
        requireNonNull(roleWords);
        assert !(departmentWords.isEmpty() && roleWords.isEmpty()) : "a filter needs at least one criterion";
        this.departmentWords = List.copyOf(departmentWords);
        this.roleWords = List.copyOf(roleWords);
    }

    @Override
    public boolean test(Employee employee) {
        requireNonNull(employee);
        return containsAllWords(employee.getDepartment().value, departmentWords)
                && containsAllWords(employee.getRole().value, roleWords);
    }

    private static boolean containsAllWords(String field, List<String> words) {
        return words.stream().allMatch(word -> StringUtil.containsWordIgnoreCase(field, word));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EmployeeMatchesCriteriaPredicate otherPredicate)) {
            return false;
        }

        return departmentWords.equals(otherPredicate.departmentWords)
                && roleWords.equals(otherPredicate.roleWords);
    }

    @Override
    public int hashCode() {
        return 31 * departmentWords.hashCode() + roleWords.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("departmentWords", departmentWords)
                .add("roleWords", roleWords)
                .toString();
    }
}

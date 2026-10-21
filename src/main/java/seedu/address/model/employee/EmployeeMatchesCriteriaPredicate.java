package seedu.address.model.employee;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that an {@code Employee} matches all the criteria that were given, for its ID, name, department and role.
 *
 * <p>A criterion has one or more values. It is met when any of its values is met (an {@code OR} search):
 * <ul>
 * <li>an ID value is met when it is the employee's ID, ignoring case;</li>
 * <li>a name, department or role value is met when every word of the value is a whole word of that field,
 * ignoring case. Words are separated by spaces and punctuation, see {@link StringUtil#getSearchWords(String)}.</li>
 * </ul>
 * A criterion with no values is not applied. All applied criteria must be met (an {@code AND} search).
 */
public class EmployeeMatchesCriteriaPredicate implements Predicate<Employee> {

    private final List<EmployeeId> ids;
    private final List<String> names;
    private final List<String> departments;
    private final List<String> roles;

    // the words of each value above, in the same order
    private final List<List<String>> nameWords;
    private final List<List<String>> departmentWords;
    private final List<List<String>> roleWords;

    /**
     * Creates a predicate from the values of each criterion. At least one criterion must have a value, and every
     * name, department and role value must have at least one word.
     */
    public EmployeeMatchesCriteriaPredicate(List<EmployeeId> ids, List<String> names, List<String> departments,
            List<String> roles) {
        requireNonNull(ids);
        requireNonNull(names);
        requireNonNull(departments);
        requireNonNull(roles);
        assert !(ids.isEmpty() && names.isEmpty() && departments.isEmpty() && roles.isEmpty())
                : "a filter needs at least one criterion";
        this.ids = List.copyOf(ids);
        this.names = List.copyOf(names);
        this.departments = List.copyOf(departments);
        this.roles = List.copyOf(roles);
        this.nameWords = toWords(this.names);
        this.departmentWords = toWords(this.departments);
        this.roleWords = toWords(this.roles);
    }

    private static List<List<String>> toWords(List<String> values) {
        return values.stream().map(value -> {
            List<String> words = StringUtil.getSearchWords(value);
            assert !words.isEmpty() : "a filter value needs at least one word";
            return words;
        }).toList();
    }

    @Override
    public boolean test(Employee employee) {
        requireNonNull(employee);
        return (ids.isEmpty() || ids.contains(employee.getId()))
                && matchesAny(employee.getName().fullName, nameWords)
                && matchesAny(employee.getDepartment().value, departmentWords)
                && matchesAny(employee.getRole().value, roleWords);
    }

    /** Returns true if there are no {@code alternatives}, or if all words of one of them are in {@code field}. */
    private static boolean matchesAny(String field, List<List<String>> alternatives) {
        if (alternatives.isEmpty()) {
            return true;
        }
        Set<String> wordsOfField = new HashSet<>(StringUtil.getSearchWords(field));
        return alternatives.stream().anyMatch(wordsOfField::containsAll);
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

        return ids.equals(otherPredicate.ids)
                && nameWords.equals(otherPredicate.nameWords)
                && departmentWords.equals(otherPredicate.departmentWords)
                && roleWords.equals(otherPredicate.roleWords);
    }

    @Override
    public int hashCode() {
        return List.of(ids, nameWords, departmentWords, roleWords).hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("ids", ids)
                .add("names", names)
                .add("departments", departments)
                .add("roles", roles)
                .toString();
    }
}

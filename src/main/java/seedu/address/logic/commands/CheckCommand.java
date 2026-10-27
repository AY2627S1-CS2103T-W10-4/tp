package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import seedu.address.model.Model;
import seedu.address.model.employee.Employee;

/**
 * Reports active employees who share an exact phone number or an email that differs only in letter case,
 * without modifying records or the view.
 */
public class CheckCommand extends Command {

    public static final String COMMAND_WORD = "check";
    public static final String MESSAGE_USAGE = "Usage: " + COMMAND_WORD;
    public static final String MESSAGE_NO_ISSUES = "No data-quality issues found.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        List<Employee> employees = model.getAddressBook().getEmployeeList().stream()
                .filter(employee -> !employee.isArchived()).toList();
        Map<String, Long> phoneCounts = countValues(employees, employee -> employee.getPhone().value);
        Map<String, Long> emailCounts = countValues(employees, CheckCommand::emailKey);
        List<String> issues = employees.stream()
                .sorted(Comparator.comparing(employee -> employee.getId().value, String.CASE_INSENSITIVE_ORDER))
                .filter(employee -> phoneCounts.get(employee.getPhone().value) > 1
                        || emailCounts.get(emailKey(employee)) > 1)
                .map(employee -> formatIssue(employee, phoneCounts, emailCounts)).toList();
        if (issues.isEmpty()) {
            return new CommandResult(MESSAGE_NO_ISSUES);
        }
        return new CommandResult("Found " + issues.size() + " active employee(s) with data-quality issues:\n"
                + String.join("\n", issues));
    }

    private Map<String, Long> countValues(List<Employee> employees, Function<Employee, String> classifier) {
        return employees.stream().collect(Collectors.groupingBy(classifier, Collectors.counting()));
    }

    /** Lists each affected employee once, including both reasons when both contact fields are shared. */
    private String formatIssue(Employee employee, Map<String, Long> phoneCounts, Map<String, Long> emailCounts) {
        List<String> reasons = new ArrayList<>();
        if (phoneCounts.get(employee.getPhone().value) > 1) {
            reasons.add("shared phone " + employee.getPhone().value);
        }
        if (emailCounts.get(emailKey(employee)) > 1) {
            reasons.add("shared email " + employee.getEmail().value);
        }
        return employee.getId() + " (" + employee.getName() + "): " + String.join("; ", reasons);
    }

    /** Returns the employee's email in lower case, since emails that differ only in case reach the same inbox. */
    private static String emailKey(Employee employee) {
        return employee.getEmail().value.toLowerCase(Locale.ROOT);
    }
}

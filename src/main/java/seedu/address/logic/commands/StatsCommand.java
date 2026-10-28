package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import seedu.address.logic.commands.SortCommand.SortField;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;

/** Shows workforce totals and breakdowns for all active employees without changing the current view. */
public class StatsCommand extends Command {

    public static final String COMMAND_WORD = "stats";
    public static final String MESSAGE_USAGE = "Usage: " + COMMAND_WORD;

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        List<Employee> employees = model.getAddressBook().getEmployeeList().stream()
                .filter(employee -> !employee.isArchived()).toList();
        StringBuilder summary = new StringBuilder("Total active employees: " + employees.size());
        appendBreakdown(summary, "By department:", employees, employee -> employee.getDepartment().value);
        appendBreakdown(summary, "By role:", employees, employee -> employee.getRole().value);
        return new CommandResult(summary.toString());
    }

    /**
     * Appends counts in descending order, breaking ties by the label in the same order as {@code sort}.
     * Labels that differ only in case or accents are ordered by their characters so that the output stays
     * deterministic.
     */
    private void appendBreakdown(StringBuilder summary, String heading, List<Employee> employees,
            Function<Employee, String> classifier) {
        summary.append("\n").append(heading);
        Map<String, Long> counts = employees.stream()
                .collect(Collectors.groupingBy(classifier, Collectors.counting()));
        if (counts.isEmpty()) {
            summary.append("\n(none)");
            return;
        }
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey(SortField.VALUE_ORDER))
                        .thenComparing(Map.Entry.comparingByKey()))
                .forEach(entry -> summary.append("\n").append(entry.getKey()).append(": ").append(entry.getValue()));
    }
}

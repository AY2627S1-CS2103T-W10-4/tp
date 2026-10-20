package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;

/**
 * Sorts the displayed employees by one field, in ascending and case-insensitive order.
 * Only the displayed order changes; the stored data is not changed. {@code list} restores the saved order.
 */
public class SortCommand extends Command {

    public static final String COMMAND_WORD = "sort";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Sorts the displayed employees in ascending order "
            + "(A to Z, ignoring case) by the given field: id, name, department or role. "
            + "Use list to show the employees in their saved order again.\n"
            + "Parameters: FIELD\n"
            + "Example: " + COMMAND_WORD + " department";

    public static final String MESSAGE_SUCCESS = "Sorted by %1$s in ascending order.";
    public static final String MESSAGE_UNKNOWN_FIELD = "Unknown sort field: %1$s. The field must be one of: %2$s.";

    private static final Logger logger = LogsCenter.getLogger(SortCommand.class);

    private final SortField field;

    /**
     * Creates a command that sorts the displayed employees by {@code field}.
     */
    public SortCommand(SortField field) {
        requireNonNull(field);
        this.field = field;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        logger.fine("Sorting employees by " + field);
        model.updateEmployeeSortOrder(field.getComparator());
        return new CommandResult(String.format(MESSAGE_SUCCESS, field));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof SortCommand otherSortCommand)) {
            return false;
        }

        return field == otherSortCommand.field;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("field", field)
                .toString();
    }

    /**
     * The fields that employees can be sorted by.
     * Employees with an equal value are ordered by their employee ID, so the order is always the same.
     */
    public enum SortField {
        ID(employee -> employee.getId().value),
        NAME(employee -> employee.getName().fullName),
        DEPARTMENT(employee -> employee.getDepartment().value),
        ROLE(employee -> employee.getRole().value);

        private final Comparator<Employee> comparator;

        SortField(Function<Employee, String> keyExtractor) {
            Comparator<Employee> byIdIgnoringCase = Comparator.comparing(
                    employee -> employee.getId().value, String.CASE_INSENSITIVE_ORDER);
            comparator = Comparator.comparing(keyExtractor, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(byIdIgnoringCase);
        }

        public Comparator<Employee> getComparator() {
            return comparator;
        }

        /**
         * Returns the field named {@code text}, ignoring case, or an empty {@code Optional} if there is none.
         */
        public static Optional<SortField> fromString(String text) {
            requireNonNull(text);
            return Arrays.stream(values())
                    .filter(field -> field.toString().equalsIgnoreCase(text.trim()))
                    .findFirst();
        }

        /** Returns the names of all fields, for example in an error message. */
        public static String names() {
            return Arrays.stream(values()).map(SortField::toString).collect(Collectors.joining(", "));
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}

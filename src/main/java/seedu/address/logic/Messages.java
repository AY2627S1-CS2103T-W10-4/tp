package seedu.address.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.employee.Employee;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_EMPLOYEE_DISPLAYED_INDEX = "The employee index provided is invalid.";
    public static final String MESSAGE_EMPLOYEE_NOT_FOUND = "No employee with ID %1$s was found.";
    public static final String MESSAGE_EMPLOYEES_LISTED_OVERVIEW = "%1$d employee(s) listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code employee} for display to the user.
     */
    public static String format(Employee employee) {
        final StringBuilder builder = new StringBuilder();
        builder.append("ID: ")
                .append(employee.getId())
                .append("; Name: ")
                .append(employee.getName())
                .append("; Phone: ")
                .append(employee.getPhone())
                .append("; Email: ")
                .append(employee.getEmail())
                .append("; Department: ")
                .append(employee.getDepartment())
                .append("; Role: ")
                .append(employee.getRole());
        return builder.toString();
    }

}

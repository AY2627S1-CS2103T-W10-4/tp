package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEPARTMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.ReportingStructure;

/**
 * Adds an employee.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds an employee. "
            + "Parameters: "
            + PREFIX_ID + "EMPLOYEE_ID "
            + PREFIX_NAME + "NAME "
            + PREFIX_PHONE + "PHONE "
            + PREFIX_EMAIL + "EMAIL "
            + PREFIX_DEPARTMENT + "DEPARTMENT "
            + PREFIX_ROLE + "ROLE [m/MANAGER_ID]\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_ID + "E0123 "
            + PREFIX_NAME + "John Tan "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_EMAIL + "johntan@example.com "
            + PREFIX_DEPARTMENT + "Engineering "
            + PREFIX_ROLE + "Software Engineer";

    public static final String MESSAGE_SUCCESS = "New employee added: %1$s";
    public static final String MESSAGE_DUPLICATE_EMPLOYEE = "An employee with Employee ID %1$s already exists.";
    public static final String MESSAGE_DUPLICATE_ARCHIVED_EMPLOYEE = "An employee with Employee ID %1$s already "
            + "exists in the archive. Use `list archived` to see it.";
    public static final String MESSAGE_SIMILAR_NAME_WARNING = "Warning: an employee with the same or a very similar "
            + "name already exists (Employee ID %1$s). Please check that this is not a duplicate.";
    public static final String MESSAGE_MANY_SIMILAR_NAMES_WARNING = "Warning: %1$d employees with the same or a very "
            + "similar name already exist (showing first %2$d Employee IDs: %3$s). "
            + "Please check that this is not a duplicate.";

    private static final int MAX_SIMILAR_EMPLOYEE_IDS = 5;

    private final Employee toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Employee}
     */
    public AddCommand(Employee employee) {
        requireNonNull(employee);
        toAdd = employee;
    }

    @Override
    public HistoryAction getHistoryAction() {
        return HistoryAction.COMMIT;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasEmployee(toAdd)) {
            String duplicateMessage = model.hasArchivedEmployee(toAdd)
                    ? MESSAGE_DUPLICATE_ARCHIVED_EMPLOYEE
                    : MESSAGE_DUPLICATE_EMPLOYEE;
            throw new CommandException(String.format(duplicateMessage, toAdd.getId()));
        }

        // Employees may share a name, so a similar name only produces a warning and does not stop the add.
        List<String> similarEmployeeIds = new ArrayList<>(MAX_SIMILAR_EMPLOYEE_IDS);
        int similarEmployeeCount = 0;
        for (Employee employee : model.getAddressBook().getEmployeeList()) {
            if (employee.getName().isSimilarTo(toAdd.getName())) {
                similarEmployeeCount++;
                if (similarEmployeeIds.size() < MAX_SIMILAR_EMPLOYEE_IDS) {
                    similarEmployeeIds.add(employee.getId().toString());
                }
            }
        }

        Employee employeeToAdd;
        try {
            employeeToAdd = ReportingStructure.withCanonicalManager(model.getAddressBook().getEmployeeList(), toAdd);
            model.addEmployee(employeeToAdd);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage());
        }

        String message = String.format(MESSAGE_SUCCESS, Messages.format(employeeToAdd));
        if (similarEmployeeCount > MAX_SIMILAR_EMPLOYEE_IDS) {
            message += "\n" + String.format(MESSAGE_MANY_SIMILAR_NAMES_WARNING, similarEmployeeCount,
                    MAX_SIMILAR_EMPLOYEE_IDS, String.join(", ", similarEmployeeIds));
        } else if (similarEmployeeCount > 0) {
            message += "\n" + String.format(MESSAGE_SIMILAR_NAME_WARNING, String.join(", ", similarEmployeeIds));
        }
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}

package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;

/**
 * Deletes an employee identified by ID from the complete employee roster.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes one employee by employee ID from all stored employee records.\n"
            + "Parameters: id/EMPLOYEE_ID\n"
            + EmployeeId.MESSAGE_CONSTRAINTS + "\n"
            + "Example: " + COMMAND_WORD + " id/EMP-0042";

    public static final String MESSAGE_DELETE_EMPLOYEE_SUCCESS = "Deleted employee: %1$s";
    public static final String MESSAGE_MISSING_ID = "Missing parameter: id/EMPLOYEE_ID.";
    public static final String MESSAGE_BLANK_ID = "Employee ID cannot be blank.";
    public static final String MESSAGE_UNKNOWN_PARAMETER = "Unknown parameter: %1$s. Only id/ is supported.";
    public static final String MESSAGE_UNEXPECTED_ARGUMENTS =
            "Unexpected arguments. Specify exactly one employee ID using id/EMPLOYEE_ID.";

    private final EmployeeId targetId;

    /**
     * Creates a command to delete the employee with {@code targetId}.
     */
    public DeleteCommand(EmployeeId targetId) {
        this.targetId = requireNonNull(targetId);
    }

    public EmployeeId getTargetId() {
        return targetId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Employee employeeToDelete = model.getAddressBook().getEmployeeList().stream()
                .filter(employee -> employee.getId().equals(targetId))
                .findFirst()
                .orElseThrow(() -> new CommandException(String.format(Messages.MESSAGE_EMPLOYEE_NOT_FOUND, targetId)));
        model.deleteEmployee(employeeToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_EMPLOYEE_SUCCESS, Messages.format(employeeToDelete)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return targetId.equals(otherDeleteCommand.targetId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetId", targetId)
                .toString();
    }
}

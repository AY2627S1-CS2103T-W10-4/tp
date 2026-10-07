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
            + "Parameters: id/EMPLOYEE_ID (uppercase E followed by exactly four digits)\n"
            + "Example: " + COMMAND_WORD + " id/E0123";

    public static final String MESSAGE_DELETE_EMPLOYEE_SUCCESS = "Deleted employee: %1$s";
    public static final String MESSAGE_UNEXPECTED_ARGUMENTS = "Unexpected arguments. Specify exactly one employee ID.";

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
    public boolean requiresSave() {
        return true;
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

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
    public static final String MESSAGE_ONE_REPORT_UNASSIGNED = "1 direct report now has no manager.";
    public static final String MESSAGE_MULTIPLE_REPORTS_UNASSIGNED = "%1$d direct reports now have no manager.";

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
    public HistoryAction getHistoryAction() {
        return HistoryAction.COMMIT;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Employee employeeToDelete = EmployeeCommandSupport.find(model, targetId);
        long reports = model.getAddressBook().getEmployeeList().stream()
                .filter(employee -> employee.getManagerId().filter(employeeToDelete.getId()::equals).isPresent())
                .count();
        model.deleteEmployee(employeeToDelete);
        String message = String.format(MESSAGE_DELETE_EMPLOYEE_SUCCESS, Messages.format(employeeToDelete));
        if (reports == 1) {
            message += "\n" + MESSAGE_ONE_REPORT_UNASSIGNED;
        } else if (reports > 1) {
            message += "\n" + String.format(MESSAGE_MULTIPLE_REPORTS_UNASSIGNED, reports);
        }
        return new CommandResult(message);
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
    public int hashCode() {
        return targetId.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetId", targetId)
                .toString();
    }
}

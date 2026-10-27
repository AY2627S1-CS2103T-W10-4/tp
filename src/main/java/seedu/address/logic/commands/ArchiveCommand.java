package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;

/** Archives an active employee. */
public class ArchiveCommand extends Command {
    public static final String COMMAND_WORD = "archive";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Archives an active employee.\n"
            + "Parameters: id/EMPLOYEE_ID\n"
            + "Example: archive id/E0123";
    public static final String MESSAGE_SUCCESS = "Archived employee: %1$s";
    public static final String MESSAGE_ALREADY_ARCHIVED = "Employee with ID %1$s is already archived.";

    private final EmployeeId targetId;

    /** Creates an archive command for an employee ID. */
    public ArchiveCommand(EmployeeId targetId) {
        this.targetId = requireNonNull(targetId);
    }

    @Override
    public HistoryAction getHistoryAction() {
        return HistoryAction.COMMIT;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Employee employee = EmployeeCommandSupport.find(model, targetId);
        if (employee.isArchived()) {
            throw new CommandException(String.format(MESSAGE_ALREADY_ARCHIVED, targetId));
        }
        model.archiveEmployee(employee);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(employee)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ArchiveCommand otherCommand)) {
            return false;
        }
        return targetId.equals(otherCommand.targetId);
    }

    @Override
    public int hashCode() {
        return targetId.hashCode();
    }
}

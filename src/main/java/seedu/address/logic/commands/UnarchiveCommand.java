package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;

/** Restores an archived employee to the active roster. */
public class UnarchiveCommand extends Command {
    public static final String COMMAND_WORD = "unarchive";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Restores an archived employee.\n"
            + "Parameters: id/EMPLOYEE_ID\nExample: unarchive id/E0123";
    public static final String MESSAGE_SUCCESS = "Unarchived employee: %1$s";
    public static final String MESSAGE_ALREADY_ACTIVE = "Employee with ID %1$s is already active.";

    private final EmployeeId targetId;

    public UnarchiveCommand(EmployeeId targetId) {
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
        if (!employee.isArchived()) {
            throw new CommandException(String.format(MESSAGE_ALREADY_ACTIVE, targetId));
        }
        model.unarchiveEmployee(employee);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(employee)));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof UnarchiveCommand otherCommand
                && targetId.equals(otherCommand.targetId);
    }

    @Override
    public int hashCode() {
        return targetId.hashCode();
    }
}

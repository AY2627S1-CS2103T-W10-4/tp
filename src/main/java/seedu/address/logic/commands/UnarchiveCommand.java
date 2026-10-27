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
    public static final String MESSAGE_OUTSIDE_VIEW =
            "The restored employee is outside the current view. Use `list` to see it.";

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
        Employee employee = model.getAddressBook().getEmployeeList().stream()
                .filter(candidate -> candidate.getId().equals(targetId)).findFirst()
                .orElseThrow(() -> new CommandException(String.format(Messages.MESSAGE_EMPLOYEE_NOT_FOUND, targetId)));
        if (!employee.isArchived()) {
            throw new CommandException(String.format(MESSAGE_ALREADY_ACTIVE, targetId));
        }
        model.unarchiveEmployee(employee);
        String message = String.format(MESSAGE_SUCCESS, Messages.format(employee));
        if (model.getFilteredEmployeeList().stream().noneMatch(candidate -> candidate.getId().equals(targetId))) {
            message += "\n" + MESSAGE_OUTSIDE_VIEW;
        }
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof UnarchiveCommand otherCommand
                && targetId.equals(otherCommand.targetId);
    }
}

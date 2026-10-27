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
    public static final String MESSAGE_ACTIVE_REPORT = "1 active employee still reports to this archived manager.";
    public static final String MESSAGE_ACTIVE_REPORTS = "%d active employees still report to this archived manager.";
    public static final String MESSAGE_REVIEW_TEAM = "Use team id/%s to review their assignments.";

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
        String message = String.format(MESSAGE_SUCCESS, Messages.format(employee));
        long activeReports = model.getAddressBook().getEmployeeList().stream()
                .filter(candidate -> !candidate.isArchived())
                .filter(candidate -> candidate.getManagerId().filter(employee.getId()::equals).isPresent())
                .count();
        if (activeReports > 0) {
            String notice = activeReports == 1 ? MESSAGE_ACTIVE_REPORT
                    : String.format(MESSAGE_ACTIVE_REPORTS, activeReports);
            message += "\n" + notice + "\n" + String.format(MESSAGE_REVIEW_TEAM, employee.getId());
        }
        return new CommandResult(message);
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

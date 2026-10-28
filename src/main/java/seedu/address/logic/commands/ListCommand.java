package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_EMPLOYEES;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;

/**
 * Lists all active employees by default, or all archived employees when requested.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_USAGE = "Usage: " + COMMAND_WORD + " [archived]";
    public static final String MESSAGE_SUCCESS = "Listed all active employees.";
    public static final String MESSAGE_ARCHIVED_SUCCESS = "Listed all archived employees.";
    public static final String MESSAGE_EMPTY_ARCHIVE = "There are no archived employees.";
    public static final String MESSAGE_EMPTY_ACTIVE_WITH_ARCHIVED = "There are no active employees. "
            + "Use `list archived` to see the %d archived employee%s.";
    public static final String MESSAGE_EMPTY_LIST = "You don’t have any employees yet.\n"
            + "Add employees by using\n"
            + "add id/EMPLOYEE_ID n/NAME p/PHONE e/EMAIL d/DEPARTMENT r/ROLE\n"
            + "to get a start!";

    private final boolean shouldListArchived;

    public ListCommand() {
        this(false);
    }

    public ListCommand(boolean shouldListArchived) {
        this.shouldListArchived = shouldListArchived;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.resetEmployeeSortOrder();
        if (shouldListArchived) {
            model.showArchivedEmployees();
        } else {
            model.updateFilteredEmployeeList(PREDICATE_SHOW_ALL_EMPLOYEES);
        }
        String feedbackToUser;
        if (!model.getFilteredEmployeeList().isEmpty()) {
            feedbackToUser = shouldListArchived ? MESSAGE_ARCHIVED_SUCCESS : MESSAGE_SUCCESS;
        } else if (shouldListArchived) {
            feedbackToUser = MESSAGE_EMPTY_ARCHIVE;
        } else {
            int archivedCount = model.getArchivedEmployeeCount();
            feedbackToUser = archivedCount == 0
                    ? MESSAGE_EMPTY_LIST
                    : String.format(MESSAGE_EMPTY_ACTIVE_WITH_ARCHIVED, archivedCount, archivedCount == 1 ? "" : "s");
        }
        return new CommandResult(feedbackToUser);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ListCommand otherCommand
                && shouldListArchived == otherCommand.shouldListArchived;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(shouldListArchived);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("shouldListArchived", shouldListArchived)
                .toString();
    }
}

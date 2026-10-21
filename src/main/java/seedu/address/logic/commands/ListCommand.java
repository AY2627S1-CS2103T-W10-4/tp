package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_EMPLOYEES;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;

/**
 * Lists all employees to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_USAGE = "Usage: " + COMMAND_WORD + " [archived]";
    public static final String MESSAGE_SUCCESS = "Listed all employees.";
    public static final String MESSAGE_ARCHIVED_SUCCESS = "Listed all archived employees.";
    public static final String MESSAGE_EMPTY_ARCHIVE = "There are no archived employees.";
    public static final String MESSAGE_EMPTY_ACTIVE_WITH_ARCHIVED = "There are no active employees. "
            + "Use `list archived` to see the %d archived employee%s.";
    public static final String MESSAGE_EMPTY_LIST = "You don’t have any employees yet.\n"
            + "Add employees by using\n"
            + "add id/EMPLOYEE_ID n/NAME p/PHONE e/EMAIL d/DEPARTMENT r/ROLE\n"
            + "to get a start!";

    private final boolean listArchived;

    public ListCommand() {
        this(false);
    }

    public ListCommand(boolean listArchived) {
        this.listArchived = listArchived;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.resetEmployeeSortOrder();
        if (listArchived) {
            model.showArchivedEmployees();
        } else {
            model.updateFilteredEmployeeList(PREDICATE_SHOW_ALL_EMPLOYEES);
        }
        String feedbackToUser;
        if (!model.getFilteredEmployeeList().isEmpty()) {
            feedbackToUser = listArchived ? MESSAGE_ARCHIVED_SUCCESS : MESSAGE_SUCCESS;
        } else if (listArchived) {
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
                && listArchived == otherCommand.listArchived;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(listArchived);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("listArchived", listArchived)
                .toString();
    }
}

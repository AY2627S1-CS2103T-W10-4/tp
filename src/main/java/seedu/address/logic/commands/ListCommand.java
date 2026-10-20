package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_EMPLOYEES;

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
        if (listArchived) {
            model.showArchivedEmployees();
        } else {
            model.updateFilteredEmployeeList(PREDICATE_SHOW_ALL_EMPLOYEES);
        }
        String feedbackToUser = model.getFilteredEmployeeList().isEmpty()
                ? (listArchived ? MESSAGE_EMPTY_ARCHIVE : MESSAGE_EMPTY_LIST)
                : (listArchived ? MESSAGE_ARCHIVED_SUCCESS : MESSAGE_SUCCESS);
        return new CommandResult(feedbackToUser);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ListCommand otherCommand
                && listArchived == otherCommand.listArchived;
    }
}

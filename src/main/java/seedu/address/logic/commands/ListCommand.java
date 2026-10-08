package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_EMPLOYEES;

import seedu.address.model.Model;

/**
 * Lists all employees in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_USAGE = "Usage: " + COMMAND_WORD;
    public static final String MESSAGE_SUCCESS = "Listed all employees.";
    public static final String MESSAGE_EMPTY_LIST = "You don’t have any employees yet.\n"
            + "Add employees by using\n"
            + "add id/EMPLOYEE_ID n/NAME p/PHONE e/EMAIL d/DEPARTMENT r/ROLE\n"
            + "to get a start!";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredEmployeeList(PREDICATE_SHOW_ALL_EMPLOYEES);
        String feedbackToUser = model.getFilteredEmployeeList().isEmpty()
                ? MESSAGE_EMPTY_LIST
                : MESSAGE_SUCCESS;
        return new CommandResult(feedbackToUser);
    }
}

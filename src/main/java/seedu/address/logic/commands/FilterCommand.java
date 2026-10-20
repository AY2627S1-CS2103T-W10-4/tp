package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEPARTMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.employee.EmployeeMatchesCriteriaPredicate;

/**
 * Shows only the employees whose department and role match all the given criteria.
 * The stored data is not changed; {@code list} shows every employee again.
 */
public class FilterCommand extends Command {

    public static final String COMMAND_WORD = "filter";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows the employees whose department and role "
            + "match all the given values (case-insensitive, whole words). Use list to show everyone again.\n"
            + "Parameters: [" + PREFIX_DEPARTMENT + "DEPARTMENT] [" + PREFIX_ROLE + "ROLE] "
            + "(at least one is required)\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_DEPARTMENT + "Engineering " + PREFIX_ROLE + "Manager";

    private static final Logger logger = LogsCenter.getLogger(FilterCommand.class);

    private final EmployeeMatchesCriteriaPredicate predicate;

    /**
     * Creates a command that filters the displayed employees with {@code predicate}.
     */
    public FilterCommand(EmployeeMatchesCriteriaPredicate predicate) {
        requireNonNull(predicate);
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        logger.fine("Filtering employees with " + predicate);
        model.updateFilteredEmployeeList(predicate);
        return new CommandResult(
                Messages.getEmployeesListedOverview(model.getFilteredEmployeeList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FilterCommand otherFilterCommand)) {
            return false;
        }

        return predicate.equals(otherFilterCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}

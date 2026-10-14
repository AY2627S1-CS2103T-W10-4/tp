package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEPARTMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.stream.Collectors;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;

/**
 * Adds an employee to the address book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds an employee to the address book. "
            + "Parameters: "
            + PREFIX_ID + "EMPLOYEE_ID "
            + PREFIX_NAME + "NAME "
            + PREFIX_PHONE + "PHONE "
            + PREFIX_EMAIL + "EMAIL "
            + PREFIX_DEPARTMENT + "DEPARTMENT "
            + PREFIX_ROLE + "ROLE\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_ID + "E0123 "
            + PREFIX_NAME + "John Tan "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_EMAIL + "johntan@example.com "
            + PREFIX_DEPARTMENT + "Engineering "
            + PREFIX_ROLE + "Software Engineer";

    public static final String MESSAGE_SUCCESS = "New employee added: %1$s";
    public static final String MESSAGE_DUPLICATE_EMPLOYEE = "An employee with Employee ID %1$s already exists.";
    public static final String MESSAGE_SIMILAR_NAME_WARNING = "Warning: an employee with the same or a very similar "
            + "name already exists (Employee ID %1$s). Please check that this is not a duplicate.";

    private final Employee toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Employee}
     */
    public AddCommand(Employee employee) {
        requireNonNull(employee);
        toAdd = employee;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasEmployee(toAdd)) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_EMPLOYEE, toAdd.getId()));
        }

        // Employees may share a name, so a similar name only produces a warning and does not stop the add.
        String similarEmployeeIds = model.getAddressBook().getEmployeeList().stream()
                .filter(employee -> employee.getName().isSimilarTo(toAdd.getName()))
                .map(employee -> employee.getId().toString())
                .collect(Collectors.joining(", "));

        model.addEmployee(toAdd);

        String message = String.format(MESSAGE_SUCCESS, Messages.format(toAdd));
        if (!similarEmployeeIds.isEmpty()) {
            message += "\n" + String.format(MESSAGE_SIMILAR_NAME_WARNING, similarEmployeeIds);
        }
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}

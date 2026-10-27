package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.ReportingStructure;

/** Shows managers from the immediate manager to the top. */
public class ChainCommand extends Command {
    public static final String COMMAND_WORD = "chain";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Shows managers from the immediate manager to the top.\n"
            + "Parameters: id/EMPLOYEE_ID\n"
            + "Example: " + COMMAND_WORD + " id/E0002";

    private final EmployeeId employeeId;

    /** Creates the command for the specified employee ID. */
    public ChainCommand(EmployeeId employeeId) {
        this.employeeId = requireNonNull(employeeId);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Employee employee = EmployeeCommandSupport.find(model, employeeId);
        List<Employee> result = ReportingStructure.chain(model.getAddressBook().getEmployeeList(), employee);
        ReportingCommandSupport.show(model, result);
        return new CommandResult("Reporting chain of " + ReportingCommandSupport.label(employee) + ": "
                + ReportingCommandSupport.describe(result, " -> "));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ChainCommand command
                && employeeId.equals(command.employeeId);
    }
}

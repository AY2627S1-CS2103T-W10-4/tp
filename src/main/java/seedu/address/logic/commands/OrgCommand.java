package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.ReportingStructure;

/** Shows all descendants of an employee. */
public class OrgCommand extends Command {
    public static final String COMMAND_WORD = "org";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows all descendants of an employee.\n"
            + "Parameters: id/EMPLOYEE_ID\n"
            + "Example: " + COMMAND_WORD + " id/E0002";

    private final EmployeeId employeeId;

    /** Creates the command for the specified employee ID. */
    public OrgCommand(EmployeeId employeeId) {
        this.employeeId = requireNonNull(employeeId);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Employee employee = EmployeeCommandSupport.find(model, employeeId);
        List<Employee> result = ReportingStructure.reports(model.getAddressBook().getEmployeeList(),
                employee.getId(), true);
        ReportingCommandSupport.show(model, result);
        return new CommandResult("Full team under " + ReportingCommandSupport.label(employee) + ": "
                + ReportingCommandSupport.describe(result, ", "));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof OrgCommand command
                && employeeId.equals(command.employeeId);
    }
}

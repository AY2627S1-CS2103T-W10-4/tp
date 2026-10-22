package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;

/** Removes an employee's manager. */
public class UnassignManagerCommand extends Command {
    public static final String COMMAND_WORD = "unassign-manager";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Removes an employee's manager.\n"
            + "Parameters: id/EMPLOYEE_ID\n"
            + "Example: " + COMMAND_WORD + " id/E0002";

    private final EmployeeId employeeId;

    /** Creates the command for the specified employee ID. */
    public UnassignManagerCommand(EmployeeId employeeId) {
        this.employeeId = requireNonNull(employeeId);
    }

    @Override
    public HistoryAction getHistoryAction() {
        return HistoryAction.COMMIT;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Employee employee = ReportingCommandSupport.find(model, employeeId);
        model.setEmployee(employee, employee.withManager(null));
        return new CommandResult("Manager of " + employee.getId() + " unassigned.");
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof UnassignManagerCommand command
                && employeeId.equals(command.employeeId);
    }
}

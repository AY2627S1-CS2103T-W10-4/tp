package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.ReportingStructure;

/** Assigns an existing manager to an employee. */
public class ManagerCommand extends Command {
    public static final String COMMAND_WORD = "manager";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Assigns an existing manager to an employee.\n"
            + "Parameters: id/EMPLOYEE_ID m/MANAGER_ID\n"
            + "Example: " + COMMAND_WORD + " id/E0002 m/E0001";

    private final EmployeeId employeeId;
    private final EmployeeId managerId;

    /** Creates the command for the specified employee ID. */
    public ManagerCommand(EmployeeId employeeId, EmployeeId managerId) {
        this.employeeId = requireNonNull(employeeId);
        this.managerId = requireNonNull(managerId);
    }

    @Override
    public HistoryAction getHistoryAction() {
        return HistoryAction.COMMIT;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Employee employee = ReportingCommandSupport.find(model, employeeId);
        try {
            Employee updated = ReportingStructure.withCanonicalManager(model.getAddressBook().getEmployeeList(),
                    employee.withManager(managerId));
            model.setEmployee(employee, updated);
            return new CommandResult("Manager of " + employee.getId() + " set to "
                    + updated.getManagerId().orElseThrow() + ".");
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage());
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ManagerCommand command
                && employeeId.equals(command.employeeId) && managerId.equals(command.managerId);
    }
}

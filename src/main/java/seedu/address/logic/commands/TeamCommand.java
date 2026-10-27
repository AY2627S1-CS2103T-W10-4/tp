package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.ReportingStructure;

/** Shows an employee, their manager and direct reports. */
public class TeamCommand extends Command {
    public static final String COMMAND_WORD = "team";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows an employee, their manager and direct reports.\n"
            + "Parameters: id/EMPLOYEE_ID\n"
            + "Example: " + COMMAND_WORD + " id/E0002";

    private final EmployeeId employeeId;

    /** Creates the command for the specified employee ID. */
    public TeamCommand(EmployeeId employeeId) {
        this.employeeId = requireNonNull(employeeId);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Employee employee = EmployeeCommandSupport.find(model, employeeId);
        List<Employee> roster = model.getAddressBook().getEmployeeList();
        List<Employee> reports = ReportingStructure.reports(roster, employee.getId(), false);
        List<Employee> selected = new ArrayList<>(reports);
        selected.add(employee);
        String manager = "None";
        if (employee.getManagerId().isPresent()) {
            Employee supervisor = EmployeeCommandSupport.find(model, employee.getManagerId().orElseThrow());
            selected.add(supervisor);
            manager = ReportingCommandSupport.label(supervisor);
        }
        ReportingCommandSupport.show(model, selected);
        return new CommandResult("Team of " + ReportingCommandSupport.label(employee) + ": Manager: " + manager
                + "; Direct reports: " + reports.size() + ". " + ReportingCommandSupport.describe(reports, ", "));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof TeamCommand command
                && employeeId.equals(command.employeeId);
    }
}

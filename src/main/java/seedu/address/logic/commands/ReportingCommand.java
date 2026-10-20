package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.ReportingStructure;

/** Assigns managers or displays a section of the reporting hierarchy. */
public class ReportingCommand extends Command {
    public static final String MESSAGE_USAGE = "Reporting commands: manager id/EMPLOYEE_ID m/MANAGER_ID; "
            + "unassign-manager id/EMPLOYEE_ID; team id/MANAGER_ID (direct reports); "
            + "org id/MANAGER_ID (all descendants); chain id/EMPLOYEE_ID (reporting chain).";

    /** The supported operations on reporting relationships. */
    public enum Operation { MANAGER, UNASSIGN, TEAM, ORG, CHAIN }

    private final Operation operation;
    private final EmployeeId employeeId;
    private final EmployeeId managerId;

    /** Creates a reporting command. Only MANAGER requires a manager ID. */
    public ReportingCommand(Operation operation, EmployeeId employeeId, EmployeeId managerId) {
        this.operation = requireNonNull(operation);
        this.employeeId = requireNonNull(employeeId);
        this.managerId = operation == Operation.MANAGER ? requireNonNull(managerId) : null;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Employee> roster = model.getAddressBook().getEmployeeList();
        Employee employee = roster.stream().filter(person -> person.getId().equals(employeeId)).findFirst()
                .orElseThrow(() -> new CommandException(
                        String.format(Messages.MESSAGE_EMPLOYEE_NOT_FOUND, employeeId)));
        if (operation == Operation.MANAGER || operation == Operation.UNASSIGN) {
            try {
                model.setEmployee(employee, employee.withManager(managerId));
            } catch (IllegalArgumentException e) {
                throw new CommandException(e.getMessage());
            }
            return new CommandResult(operation == Operation.MANAGER
                    ? "Manager of " + employeeId + " set to " + managerId + "."
                    : "Manager of " + employeeId + " unassigned.");
        }
        List<Employee> result = operation == Operation.CHAIN ? ReportingStructure.chain(roster, employee)
                : ReportingStructure.reports(roster, employeeId, operation == Operation.ORG);
        Set<EmployeeId> selectedIds = new HashSet<>();
        result.forEach(person -> selectedIds.add(person.getId()));
        model.updateFilteredEmployeeList(person -> selectedIds.contains(person.getId()));
        String heading = switch (operation) {
            case TEAM -> "Direct reports of ";
            case ORG -> "Full team under ";
            default -> "Reporting chain of ";
        };
        String details = result.stream().map(person -> person.getName() + " (" + person.getId() + ")")
                .collect(Collectors.joining(operation == Operation.CHAIN ? " -> " : ", "));
        return new CommandResult(heading + employee.getName() + " (" + employeeId + "): "
                + (result.isEmpty() ? "None." : details));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof ReportingCommand command && operation == command.operation
                && employeeId.equals(command.employeeId) && Objects.equals(managerId, command.managerId);
    }
}

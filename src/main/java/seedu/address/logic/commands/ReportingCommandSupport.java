package seedu.address.logic.commands;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;

/** Shared employee lookup and formatting for reporting commands. */
final class ReportingCommandSupport {
    private ReportingCommandSupport() {}

    static Employee find(Model model, EmployeeId id) throws CommandException {
        return model.getAddressBook().getEmployeeList().stream()
                .filter(employee -> employee.getId().equals(id)).findFirst()
                .orElseThrow(() -> new CommandException(String.format(Messages.MESSAGE_EMPLOYEE_NOT_FOUND, id)));
    }

    static String label(Employee employee) {
        return employee.getName() + " (" + employee.getId() + ")";
    }

    static void show(Model model, List<Employee> employees) {
        Set<EmployeeId> ids = new HashSet<>();
        employees.forEach(employee -> ids.add(employee.getId()));
        model.updateReportingEmployeeList(employee -> ids.contains(employee.getId()));
    }

    static String describe(List<Employee> employees, String separator) {
        return employees.isEmpty() ? "None." : employees.stream()
                .map(ReportingCommandSupport::label).collect(Collectors.joining(separator));
    }
}

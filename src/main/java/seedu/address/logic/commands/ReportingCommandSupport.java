package seedu.address.logic.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import seedu.address.model.Model;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.ReportingEmployeePredicate;
import seedu.address.model.employee.ReportingStructure;

/** Shared display and formatting for reporting commands. */
final class ReportingCommandSupport {
    private ReportingCommandSupport() {}

    static String label(Employee employee) {
        return employee.getName() + " (" + employee.getId() + ")";
    }

    static List<Employee> team(Model model, EmployeeId employeeId) {
        Employee employee = findIfPresent(model, employeeId);
        if (employee == null) {
            return List.of();
        }
        List<Employee> selected = new ArrayList<>(ReportingStructure.reports(
                model.getAddressBook().getEmployeeList(), employeeId, false));
        selected.add(employee);
        employee.getManagerId().map(managerId -> findIfPresent(model, managerId)).ifPresent(selected::add);
        return selected;
    }

    static List<Employee> org(Model model, EmployeeId employeeId) {
        return findIfPresent(model, employeeId) == null ? List.of() : ReportingStructure.reports(
                model.getAddressBook().getEmployeeList(), employeeId, true);
    }

    static List<Employee> chain(Model model, EmployeeId employeeId) {
        Employee employee = findIfPresent(model, employeeId);
        return employee == null ? List.of()
                : ReportingStructure.chain(model.getAddressBook().getEmployeeList(), employee);
    }

    private static Employee findIfPresent(Model model, EmployeeId id) {
        return model.getAddressBook().getEmployeeList().stream()
                .filter(employee -> employee.getId().equals(id)).findFirst().orElse(null);
    }

    static void show(Model model, Supplier<List<Employee>> employeesSupplier) {
        model.updateReportingEmployeeList(new ReportingEmployeePredicate(employeesSupplier));
    }

    static String describe(List<Employee> employees, String separator) {
        return employees.isEmpty() ? "None." : employees.stream()
                .map(ReportingCommandSupport::label).collect(Collectors.joining(separator));
    }
}

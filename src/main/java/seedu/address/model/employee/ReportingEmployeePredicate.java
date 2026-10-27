package seedu.address.model.employee;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Selects the employees in a reporting query and can recompute that selection after hierarchy changes. */
public final class ReportingEmployeePredicate implements Predicate<Employee> {
    private final Supplier<List<Employee>> employeesSupplier;
    private final Set<EmployeeId> employeeIds;

    /** Creates a predicate from the current result supplied by a reporting query. */
    public ReportingEmployeePredicate(Supplier<List<Employee>> employeesSupplier) {
        this.employeesSupplier = requireNonNull(employeesSupplier);
        employeeIds = employeesSupplier.get().stream().map(Employee::getId).collect(Collectors.toSet());
    }

    @Override
    public boolean test(Employee employee) {
        return employeeIds.contains(employee.getId());
    }

    /** Returns the same reporting query with its selected employee IDs recomputed. */
    public ReportingEmployeePredicate refreshed() {
        return new ReportingEmployeePredicate(employeesSupplier);
    }
}

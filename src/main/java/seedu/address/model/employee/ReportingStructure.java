package seedu.address.model.employee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Queries and validates reporting relationships using the complete employee roster. */
public final class ReportingStructure {
    public static final String MESSAGE_SELF_REPORTING = "An employee cannot report to themselves.";
    public static final String MESSAGE_MISSING_MANAGER = "Manager does not exist: %s";
    public static final String MESSAGE_CYCLE = "Reporting relationships cannot contain a cycle.";

    private ReportingStructure() {}

    /** Resolves a manager reference to the exact ID stored in the roster. */
    public static Employee withCanonicalManager(List<Employee> roster, Employee employee) {
        if (employee.getManagerId().isEmpty()) {
            return employee;
        }
        EmployeeId id = employee.getManagerId().orElseThrow();
        if (employee.getId().equals(id)) {
            throw new IllegalArgumentException(MESSAGE_SELF_REPORTING);
        }
        Employee manager = roster.stream().filter(candidate -> candidate.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format(MESSAGE_MISSING_MANAGER, id)));
        return employee.withManager(manager.getId());
    }

    /** Rejects missing managers, self-reporting and cycles before data is changed. */
    public static void validate(List<Employee> employees) {
        Map<EmployeeId, Employee> byId = new HashMap<>();
        employees.forEach(employee -> byId.put(employee.getId(), employee));
        Set<EmployeeId> completed = new HashSet<>();
        for (Employee employee : employees) {
            Set<EmployeeId> path = new HashSet<>();
            Employee current = employee;
            while (current != null && !completed.contains(current.getId())) {
                if (!path.add(current.getId())) {
                    throw new IllegalArgumentException(MESSAGE_CYCLE);
                }
                EmployeeId managerId = current.getManagerId().orElse(null);
                if (current.getId().equals(managerId)) {
                    throw new IllegalArgumentException(MESSAGE_SELF_REPORTING);
                }
                if (managerId != null && !byId.containsKey(managerId)) {
                    throw new IllegalArgumentException(String.format(MESSAGE_MISSING_MANAGER, managerId));
                }
                current = byId.get(managerId);
            }
            completed.addAll(path);
        }
    }

    /** Returns managers in order from the immediate manager to the top of the hierarchy. */
    public static List<Employee> chain(List<Employee> employees, Employee employee) {
        Map<EmployeeId, Employee> byId = new HashMap<>();
        employees.forEach(person -> byId.put(person.getId(), person));
        List<Employee> result = new ArrayList<>();
        Set<EmployeeId> visited = new HashSet<>();
        Employee current = employee;
        while (current.getManagerId().isPresent()) {
            current = byId.get(current.getManagerId().get());
            if (current == null || !visited.add(current.getId())) {
                break;
            }
            result.add(current);
        }
        return result;
    }

    /** Returns direct reports, or all descendants in breadth-first order when recursive is true. */
    public static List<Employee> reports(List<Employee> employees, EmployeeId managerId, boolean recursive) {
        List<Employee> result = new ArrayList<>();
        List<EmployeeId> pending = new ArrayList<>();
        Set<EmployeeId> visited = new HashSet<>();
        pending.add(managerId);
        visited.add(managerId);
        for (int i = 0; i < pending.size(); i++) {
            EmployeeId parent = pending.get(i);
            for (Employee employee : employees) {
                if (employee.getManagerId().filter(parent::equals).isPresent() && visited.add(employee.getId())) {
                    result.add(employee);
                    if (recursive) {
                        pending.add(employee.getId());
                    }
                }
            }
        }
        return result;
    }
}

package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import seedu.address.model.employee.Employee;

/** Compares ordered persisted values, including ID spelling, without using identity equality. */
public class EmployeeDataAssertions {
    /** Asserts every stored value and the order of employees. */
    public static void assertStoredEmployees(List<Employee> expected, List<Employee> actual) {
        assertEquals(storedValues(expected), storedValues(actual));
    }

    private static List<List<String>> storedValues(List<Employee> employees) {
        return employees.stream().map(employee -> List.of(employee.getId().value, employee.getName().fullName,
                employee.getPhone().value, employee.getEmail().value, employee.getDepartment().value,
                employee.getRole().value, Boolean.toString(employee.isArchived()))).toList();
    }
}

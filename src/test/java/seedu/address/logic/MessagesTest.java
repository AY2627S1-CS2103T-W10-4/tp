package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.testutil.EmployeeBuilder;

public class MessagesTest {

    private static final String EMPLOYEE_WITHOUT_MANAGER = "ID: E0001; Name: Alice Tan; Phone: 91234567; "
            + "Email: alice@example.com; Department: Engineering; Role: Engineer";

    @Test
    public void format_employeeWithoutManager_omitsManager() {
        assertEquals(EMPLOYEE_WITHOUT_MANAGER, Messages.format(buildEmployee()));
    }

    @Test
    public void format_employeeWithManager_showsManager() {
        Employee employee = buildEmployee().withManager(new EmployeeId("E0004"));
        assertEquals(EMPLOYEE_WITHOUT_MANAGER + "; Manager: E0004", Messages.format(employee));
    }

    private Employee buildEmployee() {
        return new EmployeeBuilder().withId("E0001").withName("Alice Tan").withPhone("91234567")
                .withEmail("alice@example.com").withDepartment("Engineering").withRole("Engineer").build();
    }
}

package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.testutil.EmployeeBuilder;

public class EmployeeCardTest {
    private static final Employee EMPLOYEE = new EmployeeBuilder().build();

    @Test
    public void formatDisplayIndex_singleAndDoubleDigits_usesConsistentGutterText() {
        assertEquals("01", EmployeeCard.formatDisplayIndex(1));
        assertEquals("09", EmployeeCard.formatDisplayIndex(9));
        assertEquals("10", EmployeeCard.formatDisplayIndex(10));
        assertEquals("100", EmployeeCard.formatDisplayIndex(100));
    }

    @Test
    public void getManagerText_employeeWithoutManager_returnsUnassigned() {
        assertEquals("Unassigned", EmployeeCard.getManagerText(EMPLOYEE));
        assertEquals("Unassigned", EmployeeCard.getManagerText(EMPLOYEE.withArchived(true)));
    }

    @Test
    public void getManagerText_employeeWithManager_returnsManagerId() {
        EmployeeId managerId = new EmployeeId("BOSS");

        assertEquals("Reports to BOSS", EmployeeCard.getManagerText(EMPLOYEE.withManager(managerId)));
        assertEquals("Reports to BOSS", EmployeeCard.getManagerText(
                EMPLOYEE.withManager(managerId).withArchived(true)));
    }
}

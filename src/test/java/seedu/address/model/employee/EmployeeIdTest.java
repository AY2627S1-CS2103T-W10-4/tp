package seedu.address.model.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class EmployeeIdTest {
    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EmployeeId(null));
    }

    @Test
    public void constructor_invalidId_throwsIllegalArgumentException() {
        for (String id : new String[] {"", " ", "e0123", "1", "12345", "EMP-0042", "SG_2024_017", "E123",
            "E01234", "E 0123", "E0123!", "E/0123", "E０１２３", "E١٢٣٤", " E0123", "E0123\n"}) {
            assertFalse(EmployeeId.isValidEmployeeId(id), id);
            assertThrows(IllegalArgumentException.class, EmployeeId.MESSAGE_CONSTRAINTS, () -> new EmployeeId(id));
        }
    }

    @Test
    public void isValidEmployeeId_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> EmployeeId.isValidEmployeeId(null));
    }

    @Test
    public void isValidEmployeeId_validFormat_returnsTrue() {
        assertTrue(EmployeeId.isValidEmployeeId("E0000"));
        assertTrue(EmployeeId.isValidEmployeeId("E0123"));
        assertTrue(EmployeeId.isValidEmployeeId("E9999"));
    }

    @Test
    public void equals() {
        EmployeeId id = new EmployeeId("E0123");
        assertTrue(id.equals(new EmployeeId("E0123")));
        assertTrue(id.equals(id));
        assertEquals(id.hashCode(), new EmployeeId("E0123").hashCode());
        assertFalse(id.equals(null));
        assertFalse(id.equals(5.0f));
        assertFalse(id.equals(new EmployeeId("E0124")));
    }

    @Test
    public void toString_preservesLeadingZeroes() {
        assertEquals("E0123", new EmployeeId("E0123").toString());
    }
}

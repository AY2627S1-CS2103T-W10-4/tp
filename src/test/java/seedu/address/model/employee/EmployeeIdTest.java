package seedu.address.model.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class EmployeeIdTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EmployeeId(null));
    }

    @Test
    public void constructor_invalidEmployeeId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new EmployeeId(""));
    }

    @Test
    public void isValidEmployeeId() {
        // null employee ID
        assertThrows(NullPointerException.class, () -> EmployeeId.isValidEmployeeId(null));

        // invalid employee IDs
        assertFalse(EmployeeId.isValidEmployeeId("")); // empty string
        assertFalse(EmployeeId.isValidEmployeeId(" ")); // spaces only
        assertFalse(EmployeeId.isValidEmployeeId("E 0123")); // contains a space
        assertFalse(EmployeeId.isValidEmployeeId("E0123!")); // symbol not allowed
        assertFalse(EmployeeId.isValidEmployeeId("E/0123")); // slash would clash with command prefixes
        assertFalse(EmployeeId.isValidEmployeeId("E".repeat(21))); // longer than 20 characters

        // valid employee IDs
        assertTrue(EmployeeId.isValidEmployeeId("E0123")); // the example format from the spec
        assertTrue(EmployeeId.isValidEmployeeId("e0123")); // lower case
        assertTrue(EmployeeId.isValidEmployeeId("1")); // single character
        assertTrue(EmployeeId.isValidEmployeeId("12345")); // digits only, any length
        assertTrue(EmployeeId.isValidEmployeeId("EMP-0042")); // hyphen
        assertTrue(EmployeeId.isValidEmployeeId("SG_2024_017")); // underscores
        assertTrue(EmployeeId.isValidEmployeeId("E".repeat(20))); // exactly 20 characters
    }

    @Test
    public void equals() {
        EmployeeId id = new EmployeeId("E0123");

        // same values -> returns true
        assertTrue(id.equals(new EmployeeId("E0123")));

        // same object -> returns true
        assertTrue(id.equals(id));

        // differs only in letter case -> returns true
        assertTrue(id.equals(new EmployeeId("e0123")));
        assertEquals(id.hashCode(), new EmployeeId("e0123").hashCode());

        // null -> returns false
        assertFalse(id.equals(null));

        // different types -> returns false
        assertFalse(id.equals(5.0f));

        // different values -> returns false
        assertFalse(id.equals(new EmployeeId("E0124")));
        assertNotEquals(id.hashCode(), new EmployeeId("E0124").hashCode());
    }

    @Test
    public void toString_keepsOriginalCase() {
        assertEquals("e0123", new EmployeeId("e0123").toString());
    }
}

package seedu.address.model.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class DepartmentTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Department(null));
    }

    @Test
    public void constructor_invalidDepartment_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Department(""));
    }

    @Test
    public void isValidDepartment() {
        // null department
        assertThrows(NullPointerException.class, () -> Department.isValidDepartment(null));

        // invalid departments
        assertFalse(Department.isValidDepartment("")); // empty string
        assertFalse(Department.isValidDepartment(" ")); // spaces only
        assertFalse(Department.isValidDepartment("@@@")); // no letter or digit
        assertFalse(Department.isValidDepartment("-Engineering")); // must start with a letter or digit
        assertFalse(Department.isValidDepartment("A".repeat(101))); // longer than 100 characters
        assertFalse(Department.isValidDepartment("\u0301Engineering")); // starts with a mark and not a letter
        assertFalse(Department.isValidDepartment("\u00C9".repeat(101))); // longer than 100 characters, with accents
        assertFalse(Department.isValidDepartment("Caf\u00E9!")); // a symbol that is not allowed, after an accent

        // valid departments
        assertTrue(Department.isValidDepartment("Engineering"));
        assertTrue(Department.isValidDepartment("human resources")); // lower case with space
        assertTrue(Department.isValidDepartment("Level 2 Support")); // with digit
        assertTrue(Department.isValidDepartment("R&D")); // ampersand
        assertTrue(Department.isValidDepartment("Software Engineer (Backend)")); // brackets
        assertTrue(Department.isValidDepartment("Sales - APAC, North")); // hyphen and comma
        assertTrue(Department.isValidDepartment("A".repeat(100))); // exactly 100 characters
        assertTrue(Department.isValidDepartment("Soporte t\u00E9cnico")); // letter with an accent
        assertTrue(Department.isValidDepartment("D\u00E9partement")); // accent on the first letter or later
        assertTrue(Department.isValidDepartment("Caf\u00E9 Operations")); // accent, space and other words
        assertTrue(Department.isValidDepartment("E\u0301cole")); // accent as a separate mark after the letter
        assertTrue(Department.isValidDepartment("\u00C9".repeat(100))); // exactly 100 characters, all with an accent
        assertTrue(Department.isValidDepartment("\u4E2D\u6587 department")); // letters of another script
    }

    @Test
    public void constructor_decomposedAccent_storedInTheSameFormAsTheAccentOnTheLetter() {
        Department decomposed = new Department("E\u0301cole");
        Department precomposed = new Department("\u00C9cole");

        assertEquals("\u00C9cole", decomposed.value);
        assertEquals(precomposed, decomposed);
        assertEquals(precomposed.hashCode(), decomposed.hashCode());
    }

    @Test
    public void equals() {
        Department department = new Department("Engineering");

        // same values -> returns true
        assertTrue(department.equals(new Department("Engineering")));

        // same object -> returns true
        assertTrue(department.equals(department));

        // null -> returns false
        assertFalse(department.equals(null));

        // different types -> returns false
        assertFalse(department.equals(5.0f));

        // different values -> returns false
        assertFalse(department.equals(new Department("Finance")));
    }
}

package seedu.address.model.employee;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RoleTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Role(null));
    }

    @Test
    public void constructor_invalidRole_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Role(""));
    }

    @Test
    public void isValidRole() {
        // null role
        assertThrows(NullPointerException.class, () -> Role.isValidRole(null));

        // invalid roles
        assertFalse(Role.isValidRole("")); // empty string
        assertFalse(Role.isValidRole(" ")); // spaces only
        assertFalse(Role.isValidRole("@@@")); // no letter or digit
        assertFalse(Role.isValidRole("-Engineering")); // must start with a letter or digit
        assertFalse(Role.isValidRole("A".repeat(101))); // longer than 100 characters

        // valid roles
        assertTrue(Role.isValidRole("Engineering"));
        assertTrue(Role.isValidRole("human resources")); // lower case with space
        assertTrue(Role.isValidRole("Level 2 Support")); // with digit
        assertTrue(Role.isValidRole("R&D")); // ampersand
        assertTrue(Role.isValidRole("Software Engineer (Backend)")); // brackets
        assertTrue(Role.isValidRole("Sales - APAC, North")); // hyphen and comma
        assertTrue(Role.isValidRole("A".repeat(100))); // exactly 100 characters
    }

    @Test
    public void equals() {
        Role role = new Role("Engineering");

        // same values -> returns true
        assertTrue(role.equals(new Role("Engineering")));

        // same object -> returns true
        assertTrue(role.equals(role));

        // null -> returns false
        assertFalse(role.equals(null));

        // different types -> returns false
        assertFalse(role.equals(5.0f));

        // different values -> returns false
        assertFalse(role.equals(new Role("Finance")));
    }
}

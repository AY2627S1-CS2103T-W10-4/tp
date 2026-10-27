package seedu.address.model.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertFalse(Role.isValidRole("\u0301Engineering")); // starts with a mark and not a letter
        assertFalse(Role.isValidRole("\u00C9".repeat(101))); // longer than 100 characters, with accents
        assertFalse(Role.isValidRole("Caf\u00E9!")); // a symbol that is not allowed, after an accent

        // valid roles
        assertTrue(Role.isValidRole("Engineering"));
        assertTrue(Role.isValidRole("human resources")); // lower case with space
        assertTrue(Role.isValidRole("Level 2 Support")); // with digit
        assertTrue(Role.isValidRole("R&D")); // ampersand
        assertTrue(Role.isValidRole("Software Engineer (Backend)")); // brackets
        assertTrue(Role.isValidRole("Sales - APAC, North")); // hyphen and comma
        assertTrue(Role.isValidRole("A".repeat(100))); // exactly 100 characters
        assertTrue(Role.isValidRole("Directeur g\u00E9n\u00E9ral")); // letter with an accent
        assertTrue(Role.isValidRole("Ing\u00E9nieur")); // accent on the first letter or later
        assertTrue(Role.isValidRole("Ma\u00EEtre d'h\u00F4tel")); // accent, space and other words
        assertTrue(Role.isValidRole("E\u0301cole")); // accent as a separate mark after the letter
        assertTrue(Role.isValidRole("\u00C9".repeat(100))); // exactly 100 characters, all with an accent
        assertTrue(Role.isValidRole("\u4E2D\u6587 role")); // letters of another script
    }

    @Test
    public void constructor_decomposedAccent_storedInTheSameFormAsTheAccentOnTheLetter() {
        Role decomposed = new Role("E\u0301cole");
        Role precomposed = new Role("\u00C9cole");

        assertEquals("\u00C9cole", decomposed.value);
        assertEquals(precomposed, decomposed);
        assertEquals(precomposed.hashCode(), decomposed.hashCode());
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

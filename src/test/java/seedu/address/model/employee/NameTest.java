package seedu.address.model.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters
        assertFalse(Name.isValidName("peter*")); // contains a symbol that is not allowed
        assertFalse(Name.isValidName("peter&jack")); // ampersand not allowed
        assertFalse(Name.isValidName("-peter")); // must start with a letter or digit
        assertFalse(Name.isValidName("'peter")); // must start with a letter or digit
        assertFalse(Name.isValidName("\u0301peter")); // must not start with a combining mark

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
        assertTrue(Name.isValidName("Ravi s/o Kumar")); // son of
        assertTrue(Name.isValidName("Nur Aisha d/o Ali")); // daughter of
        assertTrue(Name.isValidName("Ravi A/L Kumar")); // Malay-style relation word
        assertTrue(Name.isValidName("Mary-Ann O'Brien")); // hyphen and apostrophe
        assertTrue(Name.isValidName("Dr. Tan, Jr")); // full stop and comma
        assertTrue(Name.isValidName("Jos\u00e9 M\u00fcller")); // accented letters
        assertTrue(Name.isValidName("\u674e\u5c0f\u9f99")); // non-Latin letters
        assertTrue(Name.isValidName("\u0BAE\u0BA4\u0BA9\u0BCD")); // Tamil name, ends with a combining mark
        assertTrue(Name.isValidName("Jose\u0301")); // accent stored as a separate combining character
        assertTrue(Name.isValidName("A".repeat(10000))); // very long input does not overflow the stack
    }

    @Test
    public void normalise_trimsAndCollapsesSpaces() {
        assertEquals("John Doe", Name.normalise("  John    Doe  "));
        assertEquals("John Doe", Name.normalise("John\tDoe"));
    }

    @Test
    public void isSimilarTo() {
        Name name = new Name("John Doe");

        // same name -> returns true
        assertTrue(name.isSimilarTo(new Name("John Doe")));

        // differs only in letter case -> returns true
        assertTrue(name.isSimilarTo(new Name("john doe")));
        assertTrue(name.isSimilarTo(new Name("JOHN DOE")));

        // differs only in spacing -> returns true
        assertTrue(name.isSimilarTo(new Name("John    Doe")));

        // null -> returns false
        assertFalse(name.isSimilarTo(null));

        // different name -> returns false
        assertFalse(name.isSimilarTo(new Name("John Doe Jr")));
        assertFalse(name.isSimilarTo(new Name("Jane Doe")));
    }

    @Test
    public void constructor_decomposedAccent_storedInStandardForm() {
        // "e" followed by a separate acute accent is the same name as the single character "\u00e9"
        assertEquals(new Name("Jos\u00e9"), new Name("Jose\u0301"));
        assertEquals("Jos\u00e9", new Name("Jose\u0301").fullName);
        assertTrue(new Name("Jos\u00e9").isSimilarTo(new Name("JOSE\u0301")));
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}

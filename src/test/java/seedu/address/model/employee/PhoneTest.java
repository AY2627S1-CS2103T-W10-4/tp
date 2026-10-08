package seedu.address.model.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = "";
        assertThrows(IllegalArgumentException.class, () -> new Phone(invalidPhone));
    }

    @Test
    public void isValidPhone() {
        // null phone number
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // invalid phone numbers
        assertFalse(Phone.isValidPhone("")); // empty string
        assertFalse(Phone.isValidPhone(" ")); // spaces only
        assertFalse(Phone.isValidPhone("91")); // less than 3 digits
        assertFalse(Phone.isValidPhone("phone")); // no digits
        assertFalse(Phone.isValidPhone("9011p041")); // letters outside brackets
        assertFalse(Phone.isValidPhone("9312/1534")); // symbol not allowed
        assertFalse(Phone.isValidPhone("12 (ab)")); // fewer than 3 digits even with a label
        assertFalse(Phone.isValidPhone("9312 (HP")); // unbalanced bracket
        assertFalse(Phone.isValidPhone("9312 HP)")); // closing bracket without an opening one
        assertFalse(Phone.isValidPhone("9312 ()")); // empty brackets
        assertFalse(Phone.isValidPhone("9312 ((HP))")); // nested brackets
        assertFalse(Phone.isValidPhone("9312 (H/P)")); // symbol not allowed in a label

        // valid phone numbers
        assertTrue(Phone.isValidPhone("911")); // exactly 3 digits
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("124293842033123")); // long phone numbers
        assertTrue(Phone.isValidPhone("9312 1534")); // spaces within digits
        assertTrue(Phone.isValidPhone("+65 6123-4567")); // country code and hyphen
        assertTrue(Phone.isValidPhone("(+65) 9123 4567")); // brackets around the country code
        assertTrue(Phone.isValidPhone("1234 5678 (HP) 1111-3333 (Office)")); // several numbers with labels
    }

    @Test
    public void isValidPhone_veryLongInput_doesNotOverflowTheStack() {
        assertTrue(Phone.isValidPhone("1".repeat(10000)));
        assertTrue(Phone.isValidPhone("1 (HP) ".repeat(5000)));
        assertFalse(Phone.isValidPhone("1".repeat(10000) + "x"));
        assertFalse(Phone.isValidPhone("(".repeat(10000)));
    }

    @Test
    public void countDigits() {
        assertEquals(0, Phone.countDigits("(HP)"));
        assertEquals(8, Phone.countDigits("9312 1534"));
    }

    @Test
    public void equals() {
        Phone phone = new Phone("999");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("999")));

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("995")));
    }
}

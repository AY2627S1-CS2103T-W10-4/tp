package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void constructor_emptyRemark_isAllowed() {
        assertEquals("", new Remark("").value);
    }

    @Test
    public void equals() {
        Remark remark = new Remark("Hello");

        // same values -> returns true
        assertTrue(remark.equals(new Remark("Hello")));

        // same object -> returns true
        assertTrue(remark.equals(remark));

        // null -> returns false
        assertFalse(remark.equals(null));

        // different types -> returns false
        assertFalse(remark.equals(5.0f));

        // different values -> returns false
        assertFalse(remark.equals(new Remark("Hello1")));
    }

    @Test
    public void hashCode_sameValue_sameHash() {
        assertEquals(new Remark("Hello").hashCode(), new Remark("Hello").hashCode());
        assertNotEquals(new Remark("Hello").hashCode(), new Remark("Bye").hashCode());
    }
}

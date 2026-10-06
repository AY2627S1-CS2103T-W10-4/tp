package seedu.address.model.employee;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents an Employee's ID in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidEmployeeId(String)}
 */
public class EmployeeId {

    public static final String MESSAGE_CONSTRAINTS =
            "Employee ID should be 1 to 20 characters long and contain only letters, digits, hyphens and underscores.";

    /*
     * Company ID schemes vary (e.g. E0123, EMP-0042, 2024-017), so any combination of
     * letters, digits, hyphens and underscores is accepted, up to 20 characters.
     */
    public static final String VALIDATION_REGEX = "[\\p{Alnum}_-]{1,20}";

    public final String value;

    /**
     * Constructs an {@code EmployeeId}.
     *
     * @param employeeId A valid employee ID.
     */
    public EmployeeId(String employeeId) {
        requireNonNull(employeeId);
        checkArgument(isValidEmployeeId(employeeId), MESSAGE_CONSTRAINTS);
        value = employeeId;
    }

    /**
     * Returns true if a given string is a valid employee ID.
     */
    public static boolean isValidEmployeeId(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Two IDs are equal if they differ only in letter case, e.g. E0123 and e0123.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EmployeeId)) {
            return false;
        }

        EmployeeId otherEmployeeId = (EmployeeId) other;
        return value.equalsIgnoreCase(otherEmployeeId.value);
    }

    @Override
    public int hashCode() {
        return value.toLowerCase(Locale.ROOT).hashCode();
    }

}

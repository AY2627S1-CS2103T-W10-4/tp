package seedu.address.model.employee;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents an Employee's ID in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidEmployeeId(String)}
 */
public class EmployeeId {

    public static final String MESSAGE_CONSTRAINTS =
            "Employee ID must be an uppercase E followed by exactly four digits (e.g. E0123).";

    public static final String VALIDATION_REGEX = "E[0-9]{4}";

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
     * Returns true if both IDs have the same validated uppercase value.
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
        return value.equals(otherEmployeeId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}

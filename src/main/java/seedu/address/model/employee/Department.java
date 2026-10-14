package seedu.address.model.employee;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents an Employee's department in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidDepartment(String)}
 */
public class Department {

    public static final int MAX_LENGTH = 100;

    public static final String MESSAGE_BLANK = "Department should not be blank.";

    public static final String MESSAGE_TOO_LONG = "Department should be at most " + MAX_LENGTH + " characters long.";

    public static final String MESSAGE_CONSTRAINTS =
            "Department should not be blank, should start with a letter or digit, and may contain only letters, "
                    + "digits, spaces and the symbols & ' ( ) , . - (up to 100 characters).";

    /*
     * The first character must be alphanumeric, otherwise " " (a blank string) becomes a valid input.
     * Common punctuation is allowed so that entries such as "R&D" or "Software Engineer (Backend)" are accepted.
     */
    public static final String VALIDATION_REGEX = "[\\p{Alnum}][\\p{Alnum} &'(),.\\-]{0,99}";

    public final String value;

    /**
     * Constructs a {@code Department}.
     *
     * @param department A valid department.
     */
    public Department(String department) {
        requireNonNull(department);
        checkArgument(isValidDepartment(department), MESSAGE_CONSTRAINTS);
        value = department;
    }

    /**
     * Returns true if a given string is a valid department.
     */
    public static boolean isValidDepartment(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Department)) {
            return false;
        }

        Department otherDepartment = (Department) other;
        return value.equals(otherDepartment.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}

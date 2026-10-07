package seedu.address.model.employee;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents an Employee's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {


    public static final String MESSAGE_BLANK = "Phone number should not be blank.";

    public static final String MESSAGE_TOO_FEW_DIGITS = "Phone numbers should contain at least 3 digits.";

    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers may only contain digits, spaces and the symbols + - , . ( ), with labels allowed inside "
                    + "brackets (for example 9123 4567 (HP) or +65 6123-4567).";

    public static final int MINIMUM_DIGITS = 3;

    /*
     * Phone numbers come in many layouts, so spaces, +, -, commas, full stops and bracketed labels such as (HP)
     * are accepted as well as plain digits.
     */
    public static final String VALIDATION_REGEX = "(?:[0-9+\\-,. ]|\\([A-Za-z0-9 +\\-]+\\))+";
    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = phone;
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        return test.matches(VALIDATION_REGEX) && countDigits(test) >= MINIMUM_DIGITS;
    }

    /**
     * Returns the number of digits in {@code test}.
     */
    public static long countDigits(String test) {
        return test.chars().filter(Character::isDigit).count();
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
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}

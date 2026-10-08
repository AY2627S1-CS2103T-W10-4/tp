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
        return hasValidLayout(test) && countDigits(test) >= MINIMUM_DIGITS;
    }

    /**
     * Returns true if {@code test} only uses allowed characters and its brackets are used correctly.
     *
     * <p>Phone numbers come in many layouts, so digits, spaces and the symbols + - , . are accepted, as well as
     * labels such as (HP) inside brackets. Brackets must not be nested or empty. This is checked with a loop
     * rather than a regular expression, because a regular expression with a repeated group needs stack space
     * proportional to the input length and overflows the stack on very long input.
     */
    public static boolean hasValidLayout(String test) {
        requireNonNull(test);
        if (test.isEmpty()) {
            return false;
        }
        boolean insideBrackets = false;
        int charactersInsideBrackets = 0;
        for (int i = 0; i < test.length(); i++) {
            char c = test.charAt(i);
            if (insideBrackets) {
                if (c == ')') {
                    if (charactersInsideBrackets == 0) {
                        return false;
                    }
                    insideBrackets = false;
                } else if (isLabelCharacter(c)) {
                    charactersInsideBrackets++;
                } else {
                    return false;
                }
            } else if (c == '(') {
                insideBrackets = true;
                charactersInsideBrackets = 0;
            } else if (!isPlainCharacter(c)) {
                return false;
            }
        }
        return !insideBrackets;
    }

    private static boolean isPlainCharacter(char c) {
        return (c >= '0' && c <= '9') || c == ' ' || c == '+' || c == '-' || c == ',' || c == '.';
    }

    private static boolean isLabelCharacter(char c) {
        return isPlainCharacter(c) || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
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

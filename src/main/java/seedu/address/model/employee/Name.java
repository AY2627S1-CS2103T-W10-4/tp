package seedu.address.model.employee;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.text.Normalizer;

/**
 * Represents an Employee's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_BLANK = "Name should not be blank.";

    public static final String MESSAGE_CONSTRAINTS =
            "Names should start with a letter or digit and may only contain letters, digits, spaces and the symbols "
                    + "' . , / - (for example Mary-Ann, O'Brien, Ravi s/o Kumar).";

    /*
     * The first character of the name must be a letter or digit, otherwise " " (a blank string) becomes
     * a valid input. Apostrophes, hyphens, full stops, commas and slashes are allowed so that real names such as
     * "Ravi s/o Kumar", "Nur Aisha d/o Ali" or "Mary-Ann O'Brien" are not rejected. Combining marks (Unicode
     * category M) are allowed after the first character because many scripts need them, for example Tamil names,
     * or an accent stored as a separate character.
     */
    public static final String VALIDATION_REGEX = "[\\p{L}\\p{N}][\\p{L}\\p{M}\\p{N} '.,/\\-]*";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = Normalizer.normalize(name, Normalizer.Form.NFC);
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    /**
     * Returns {@code name} with leading and trailing spaces removed, runs of whitespace replaced by one space,
     * and characters composed into their standard Unicode form, so that an accented letter typed as one character
     * or as a letter plus a separate accent is the same name.
     */
    public static String normalise(String name) {
        return Normalizer.normalize(name.trim().replaceAll("\\s+", " "), Normalizer.Form.NFC);
    }

    /**
     * Returns true if both names are the same apart from letter case and extra spaces,
     * e.g. "John Doe", "john doe" and "John  Doe".
     */
    public boolean isSimilarTo(Name otherName) {
        return otherName != null
                && normalise(fullName).equalsIgnoreCase(normalise(otherName.fullName));
    }

    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}

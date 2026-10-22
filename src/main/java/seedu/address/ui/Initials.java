package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Makes the initials of a name for the avatar of an employee, for example "AY" for "Alex Yeoh".
 */
final class Initials {

    private Initials() {}

    /**
     * Returns the first letter or digit of the first and of the last word of {@code name}, in upper case.
     * A name with one word has one initial. Words without a letter or digit, such as "-", are skipped.
     * A name without any letter or digit has no initials, and gives an empty string.
     */
    static String of(String name) {
        requireNonNull(name);
        String[] words = Stream.of(name.trim().split("\\s+"))
                .filter(word -> word.codePoints().anyMatch(Character::isLetterOrDigit))
                .toArray(String[]::new);
        if (words.length == 0) {
            return "";
        }
        if (words.length == 1) {
            return firstLetterOrDigit(words[0]);
        }
        return Stream.of(words[0], words[words.length - 1])
                .map(Initials::firstLetterOrDigit)
                .collect(Collectors.joining());
    }

    private static String firstLetterOrDigit(String word) {
        int codePoint = word.codePoints().filter(Character::isLetterOrDigit).findFirst().orElseThrow();
        return new String(Character.toChars(codePoint)).toUpperCase(Locale.ROOT);
    }
}

package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;

/**
 * Compares texts the way a person puts them in order: a number inside a text is compared by its value and not
 * digit by digit, so {@code E2} comes before {@code E10}, and {@code Level 2} before {@code Level 10}.
 * The rest of the text is compared ignoring letter case.
 *
 * <p>The texts are cut into runs of digits and runs of other characters. Two runs of digits are compared by their
 * value (leading zeros do not count, and with the same value the shorter run comes first, so {@code E1} comes
 * before {@code E01}). A run of digits comes before a run of other characters. Only the digits 0 to 9 are numbers.
 */
public final class NaturalOrderComparator implements Comparator<String> {

    public static final NaturalOrderComparator INSTANCE = new NaturalOrderComparator();

    private NaturalOrderComparator() {}

    @Override
    public int compare(String first, String second) {
        requireNonNull(first);
        requireNonNull(second);
        int i = 0;
        int j = 0;
        while (i < first.length() && j < second.length()) {
            boolean firstIsNumber = isDigit(first.charAt(i));
            boolean secondIsNumber = isDigit(second.charAt(j));
            if (firstIsNumber != secondIsNumber) {
                return firstIsNumber ? -1 : 1;
            }
            int endOfFirstRun = endOfRun(first, i, firstIsNumber);
            int endOfSecondRun = endOfRun(second, j, secondIsNumber);
            String firstRun = first.substring(i, endOfFirstRun);
            String secondRun = second.substring(j, endOfSecondRun);
            int result = firstIsNumber
                    ? compareNumbers(firstRun, secondRun)
                    : String.CASE_INSENSITIVE_ORDER.compare(firstRun, secondRun);
            if (result != 0) {
                return result;
            }
            i = endOfFirstRun;
            j = endOfSecondRun;
        }
        // the text that has nothing left comes first
        return Integer.compare(first.length() - i, second.length() - j);
    }

    private static boolean isDigit(char character) {
        return character >= '0' && character <= '9';
    }

    /** Returns the index after the run that starts at {@code start}: digits, or characters that are not digits. */
    private static int endOfRun(String text, int start, boolean isNumber) {
        int end = start;
        while (end < text.length() && isDigit(text.charAt(end)) == isNumber) {
            end++;
        }
        return end;
    }

    /** Compares two runs of digits by their value, without turning them into numbers that could be too large. */
    private static int compareNumbers(String first, String second) {
        String firstWithoutZeros = withoutLeadingZeros(first);
        String secondWithoutZeros = withoutLeadingZeros(second);
        if (firstWithoutZeros.length() != secondWithoutZeros.length()) {
            return Integer.compare(firstWithoutZeros.length(), secondWithoutZeros.length());
        }
        int sameLength = firstWithoutZeros.compareTo(secondWithoutZeros);
        return sameLength != 0 ? sameLength : Integer.compare(first.length(), second.length());
    }

    private static String withoutLeadingZeros(String digits) {
        int start = 0;
        while (start < digits.length() - 1 && digits.charAt(start) == '0') {
            start++;
        }
        return digits.substring(start);
    }
}

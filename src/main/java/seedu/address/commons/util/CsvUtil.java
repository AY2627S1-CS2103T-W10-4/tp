package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import seedu.address.commons.exceptions.IllegalValueException;

/**
 * Converts between a list of values and one line of comma-separated values (CSV).
 * A value that contains a comma, a double quote or a line break is wrapped in double quotes, and any double quote
 * inside it is doubled, as spreadsheet programs such as Excel do.
 */
public class CsvUtil {

    public static final String MESSAGE_UNCLOSED_QUOTE = "A quoted value is missing its closing double quote (\").";

    private static final char SEPARATOR = ',';
    private static final char QUOTE = '"';
    private static final String ESCAPED_QUOTE = "\"\"";
    private static final String CHARACTERS_NEEDING_QUOTES = ",\"\r\n";

    /**
     * Returns {@code values} joined into one CSV line, with values quoted where needed.
     */
    public static String toCsvLine(List<String> values) {
        requireNonNull(values);
        return values.stream()
                .map(CsvUtil::quoteIfNeeded)
                .collect(Collectors.joining(String.valueOf(SEPARATOR)));
    }

    private static String quoteIfNeeded(String value) {
        boolean isQuoteNeeded = value.chars().anyMatch(c -> CHARACTERS_NEEDING_QUOTES.indexOf(c) >= 0);
        if (!isQuoteNeeded) {
            return value;
        }
        return QUOTE + value.replace(String.valueOf(QUOTE), ESCAPED_QUOTE) + QUOTE;
    }

    /**
     * Returns the values in one CSV {@code line}, with surrounding quotes removed and doubled quotes restored.
     *
     * @throws IllegalValueException if a quoted value is not closed before the end of the line.
     */
    public static List<String> parseCsvLine(String line) throws IllegalValueException {
        requireNonNull(line);
        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean isInQuotes = false;
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            boolean isEscapedQuote = isInQuotes && line.startsWith(ESCAPED_QUOTE, i);
            if (isEscapedQuote) {
                currentValue.append(QUOTE);
                i++;
            } else if (c == QUOTE) {
                isInQuotes = !isInQuotes;
            } else if (c == SEPARATOR && !isInQuotes) {
                values.add(currentValue.toString());
                currentValue.setLength(0);
            } else {
                currentValue.append(c);
            }
            i++;
        }
        if (isInQuotes) {
            throw new IllegalValueException(MESSAGE_UNCLOSED_QUOTE);
        }
        values.add(currentValue.toString());
        return values;
    }

}

package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import seedu.address.commons.exceptions.IllegalValueException;

/**
 * Splits CSV text into records and converts between records and lists of values.
 * A value that contains a comma, a double quote or a line break is wrapped in double quotes, and any double quote
 * inside it is doubled, as spreadsheet programs such as Excel do.
 */
public class CsvUtil {

    public static final String MESSAGE_UNCLOSED_QUOTE = "A quoted value is missing its closing double quote (\").";
    public static final String MESSAGE_MISPLACED_QUOTE = "A double quote (\") may only start or end a value, or "
            + "appear doubled (\"\") inside a value that is wrapped in double quotes.";

    private static final char SEPARATOR = ',';
    private static final char QUOTE = '"';
    private static final String ESCAPED_QUOTE = "\"\"";
    private static final String CHARACTERS_NEEDING_QUOTES = ",\"\r\n";

    /** A complete CSV record and its starting physical line number, counting from 1. */
    public record CsvRecord(String text, int lineNumber) {}

    /**
     * Splits CSV text at line endings outside quoted values, retaining line endings inside quoted values.
     * LF, CRLF and CR line endings are supported. Blank records are retained for accurate error locations.
     * Each record must still be validated with {@link #parseCsvLine(String)}, including an unclosed final record.
     */
    public static List<CsvRecord> splitCsvRecords(String text) {
        requireNonNull(text);
        List<CsvRecord> records = new ArrayList<>();
        boolean isInQuotes = false;
        int recordStart = 0;
        int recordLine = 1;
        int lineNumber = 1;
        for (int position = 0; position < text.length(); position++) {
            char character = text.charAt(position);
            if (character == QUOTE) {
                if (isInQuotes && text.startsWith(ESCAPED_QUOTE, position)) {
                    position++;
                } else {
                    isInQuotes = !isInQuotes;
                }
            } else if (character == '\r' || character == '\n') {
                int nextPosition = position + 1;
                if (character == '\r' && nextPosition < text.length() && text.charAt(nextPosition) == '\n') {
                    nextPosition++;
                }
                if (!isInQuotes) {
                    records.add(new CsvRecord(text.substring(recordStart, position), recordLine));
                    recordStart = nextPosition;
                    recordLine = lineNumber + 1;
                }
                lineNumber++;
                position = nextPosition - 1;
            }
        }
        if (recordStart < text.length()) {
            records.add(new CsvRecord(text.substring(recordStart), recordLine));
        }
        return records;
    }

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
     * Returns the values in one complete CSV record, with quotes removed and doubled quotes restored.
     * The {@code line} may contain line endings inside quoted values.
     * A double quote may only open a value at its start, close it just before a comma or the end of the line,
     * or appear doubled inside a quoted value.
     *
     * @throws IllegalValueException if a double quote is anywhere else, or a quoted value is not closed.
     */
    public static List<String> parseCsvLine(String line) throws IllegalValueException {
        requireNonNull(line);
        List<String> values = new ArrayList<>();
        int position = 0;
        do {
            position = line.startsWith(String.valueOf(QUOTE), position)
                    ? readQuotedValue(line, position, values)
                    : readUnquotedValue(line, position, values);
            position++; // skips the comma after the value
        } while (position <= line.length());
        return values;
    }

    /**
     * Adds the unquoted value starting at {@code start} to {@code values}, and returns the index just after it.
     */
    private static int readUnquotedValue(String line, int start, List<String> values) throws IllegalValueException {
        int end = line.indexOf(SEPARATOR, start);
        if (end < 0) {
            end = line.length();
        }
        String value = line.substring(start, end);
        if (value.indexOf(QUOTE) >= 0) {
            throw new IllegalValueException(MESSAGE_MISPLACED_QUOTE);
        }
        values.add(value);
        return end;
    }

    /**
     * Adds the quoted value whose opening quote is at {@code start} to {@code values}, and returns the index just
     * after its closing quote.
     */
    private static int readQuotedValue(String line, int start, List<String> values) throws IllegalValueException {
        StringBuilder value = new StringBuilder();
        int position = start + 1;
        while (position < line.length()) {
            if (line.startsWith(ESCAPED_QUOTE, position)) {
                value.append(QUOTE);
                position += ESCAPED_QUOTE.length();
            } else if (line.charAt(position) == QUOTE) {
                return closeQuotedValue(line, position + 1, value.toString(), values);
            } else {
                value.append(line.charAt(position));
                position++;
            }
        }
        throw new IllegalValueException(MESSAGE_UNCLOSED_QUOTE);
    }

    private static int closeQuotedValue(String line, int afterQuote, String value, List<String> values)
            throws IllegalValueException {
        boolean isEndOfValue = afterQuote == line.length() || line.charAt(afterQuote) == SEPARATOR;
        if (!isEndOfValue) {
            throw new IllegalValueException(MESSAGE_MISPLACED_QUOTE);
        }
        values.add(value);
        return afterQuote;
    }

}

package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Helper functions for handling strings.
 */
public class StringUtil {

    /** Anything that is not a letter, a mark that belongs to a letter, a digit, {@code &} or {@code '}. */
    private static final Pattern NOT_PART_OF_SEARCH_WORD = Pattern.compile("[^\\p{L}\\p{M}\\p{N}&']+");

    private static final Pattern WHITESPACE_RUN = Pattern.compile("\\s+");

    /**
     * Returns {@code text} trimmed, with each run of whitespace inside it replaced by one space,
     * so that a value typed with an accidental double space is stored the same as the intended value.
     *   <br>examples:<pre>
     *       collapseWhitespace("  Human   Resources ") == "Human Resources"
     *       collapseWhitespace("HR\tExecutive") == "HR Executive"
     *       </pre>
     * @param text cannot be null
     */
    public static String collapseWhitespace(String text) {
        requireNonNull(text);
        return WHITESPACE_RUN.matcher(text.trim()).replaceAll(" ");
    }

    /**
     * Returns the lower-case words of {@code text}, to be matched whole against other words.
     * Words are separated by spaces and punctuation, except for {@code &} and {@code '} which stay in a word.
     *   <br>examples:<pre>
     *       getSearchWords("R&amp;D, Asia") == ["r&amp;d", "asia"]
     *       getSearchWords("Engineer (Backend)") == ["engineer", "backend"]
     *       getSearchWords("@@@") == []
     *       </pre>
     * @param text cannot be null
     */
    public static List<String> getSearchWords(String text) {
        requireNonNull(text);
        return NOT_PART_OF_SEARCH_WORD.splitAsStream(text.toLowerCase(Locale.ROOT))
                .filter(word -> !word.isEmpty())
                .toList();
    }

    /**
     * Returns true if the {@code sentence} contains the {@code word}.
     *   Ignores case, but a full word match is required.
     *   <br>examples:<pre>
     *       containsWordIgnoreCase("ABc def", "abc") == true
     *       containsWordIgnoreCase("ABc def", "DEF") == true
     *       containsWordIgnoreCase("ABc def", "AB") == false //not a full word match
     *       </pre>
     * @param sentence cannot be null
     * @param word cannot be null, cannot be empty, must be a single word
     */
    public static boolean containsWordIgnoreCase(String sentence, String word) {
        requireNonNull(sentence);
        requireNonNull(word);

        String preppedWord = word.trim();
        checkArgument(!preppedWord.isEmpty(), "Word parameter cannot be empty");
        checkArgument(preppedWord.split("\\s+").length == 1, "Word parameter should be a single word");

        String preppedSentence = sentence;
        String[] wordsInPreppedSentence = preppedSentence.split("\\s+");

        return Arrays.stream(wordsInPreppedSentence)
                .anyMatch(preppedWord::equalsIgnoreCase);
    }

    /**
     * Returns a detailed message of {@code t}, including the stack trace.
     */
    public static String getDetails(Throwable t) {
        requireNonNull(t);
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return t.getMessage() + "\n" + sw.toString();
    }

    /**
     * Returns true if {@code s} represents a non-zero unsigned integer
     * e.g. 1, 2, 3, ..., {@code Integer.MAX_VALUE} <br>
     * Will return false for any other non-null string input
     * e.g. empty string, "-1", "0", "+1", and " 2 " (untrimmed), "3 0" (contains whitespace), "1 a" (contains letters)
     * @throws NullPointerException if {@code s} is null.
     */
    public static boolean isNonZeroUnsignedInteger(String s) {
        requireNonNull(s);

        try {
            int value = Integer.parseInt(s);
            return value > 0 && !s.startsWith("+"); // "+1" is successfully parsed by Integer#parseInt(String)
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}

package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Locale;

import seedu.address.logic.parser.CommandWords;

/**
 * Completes the command word that the user has started to type, for the Tab key.
 */
final class CommandCompleter {

    /** All the command words, the same as the parser knows. */
    static final List<String> COMMAND_WORDS = CommandWords.ALL;

    private CommandCompleter() {}

    /**
     * Returns {@code text} completed as far as it is clear, or {@code text} itself if it cannot be completed.
     *
     * <ul>
     * <li>If one command word starts with {@code text}, the result is that word and a space, such as
     * {@code "filter "} for {@code "fil"}.</li>
     * <li>If several do, the result is the start that they share, such as {@code "un"} for {@code "u"}.</li>
     * <li>Text that has a space in it, or is empty, or starts no command word, is not changed, because there
     * is nothing to complete.</li>
     * </ul>
     */
    static String complete(String text) {
        requireNonNull(text);
        if (text.isEmpty() || text.chars().anyMatch(Character::isWhitespace)) {
            return text;
        }
        String start = text.toLowerCase(Locale.ROOT);
        List<String> matches = COMMAND_WORDS.stream().filter(word -> word.startsWith(start)).toList();
        if (matches.isEmpty()) {
            return text;
        }
        if (matches.size() == 1) {
            return matches.get(0) + " ";
        }
        String shared = sharedStart(matches);
        return shared.length() > text.length() ? shared : text;
    }

    private static String sharedStart(List<String> words) {
        String shared = words.get(0);
        for (String word : words) {
            int length = 0;
            while (length < shared.length() && length < word.length()
                    && shared.charAt(length) == word.charAt(length)) {
                length++;
            }
            shared = shared.substring(0, length);
        }
        return shared;
    }
}

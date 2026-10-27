package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Locale;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.commands.BackupCommand;
import seedu.address.logic.commands.ChainCommand;
import seedu.address.logic.commands.CheckCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ImportCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.ManagerCommand;
import seedu.address.logic.commands.OrgCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.RestoreCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.StatsCommand;
import seedu.address.logic.commands.TeamCommand;
import seedu.address.logic.commands.UnarchiveCommand;
import seedu.address.logic.commands.UnassignManagerCommand;
import seedu.address.logic.commands.UndoCommand;

/**
 * Completes the command word that the user has started to type, for the Tab key.
 */
final class CommandCompleter {

    /** All the command words. A command that is added to {@code AddressBookParser} must be added here too. */
    static final List<String> COMMAND_WORDS = List.of(
            AddCommand.COMMAND_WORD, ArchiveCommand.COMMAND_WORD, BackupCommand.COMMAND_WORD,
            ChainCommand.COMMAND_WORD, CheckCommand.COMMAND_WORD, ClearCommand.COMMAND_WORD,
            DeleteCommand.COMMAND_WORD, EditCommand.COMMAND_WORD, ExitCommand.COMMAND_WORD,
            ExportCommand.COMMAND_WORD, FilterCommand.COMMAND_WORD, FindCommand.COMMAND_WORD,
            HelpCommand.COMMAND_WORD, ImportCommand.COMMAND_WORD, ListCommand.COMMAND_WORD,
            ManagerCommand.COMMAND_WORD, OrgCommand.COMMAND_WORD, RedoCommand.COMMAND_WORD,
            RestoreCommand.COMMAND_WORD, SortCommand.COMMAND_WORD, StatsCommand.COMMAND_WORD, TeamCommand.COMMAND_WORD,
            UnarchiveCommand.COMMAND_WORD, UnassignManagerCommand.COMMAND_WORD, UndoCommand.COMMAND_WORD);

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

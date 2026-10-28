package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
 * The words of all commands, and a way to find the command that a mistyped word is closest to.
 */
public final class CommandWords {

    /** All the command words. A command that is added to {@code AddressBookParser} must be added here too. */
    public static final List<String> ALL = List.of(
            AddCommand.COMMAND_WORD, ArchiveCommand.COMMAND_WORD, BackupCommand.COMMAND_WORD,
            ChainCommand.COMMAND_WORD, CheckCommand.COMMAND_WORD, ClearCommand.COMMAND_WORD,
            DeleteCommand.COMMAND_WORD, EditCommand.COMMAND_WORD, ExitCommand.COMMAND_WORD,
            ExportCommand.COMMAND_WORD, FilterCommand.COMMAND_WORD, FindCommand.COMMAND_WORD,
            HelpCommand.COMMAND_WORD, ImportCommand.COMMAND_WORD, ListCommand.COMMAND_WORD,
            ManagerCommand.COMMAND_WORD, OrgCommand.COMMAND_WORD, RedoCommand.COMMAND_WORD,
            RestoreCommand.COMMAND_WORD, SortCommand.COMMAND_WORD, StatsCommand.COMMAND_WORD, TeamCommand.COMMAND_WORD,
            UnarchiveCommand.COMMAND_WORD, UnassignManagerCommand.COMMAND_WORD, UndoCommand.COMMAND_WORD);

    /** A word of fewer letters than this must be one change away from a command, a word of this many or more two. */
    private static final int LONGER_WORD_LENGTH = 6;

    private CommandWords() {}

    /**
     * Returns the command that {@code word} is closest to, or an empty {@code Optional} if no command is close.
     * A word that is a command except for its letter case (such as {@code List}) gives that command. Otherwise a
     * command is close when one letter is missing, added or changed, or two neighbouring letters are swapped
     * (two such changes for a word of six letters or more), as in {@code fliter}. A shorter word is only one change
     * away from a command when it is clearly meant to be that command: {@code quit} is not close to {@code exit}.
     */
    public static Optional<String> closestTo(String word) {
        requireNonNull(word);
        String lowerCaseWord = word.toLowerCase(Locale.ROOT);
        if (ALL.contains(lowerCaseWord)) {
            return Optional.of(lowerCaseWord);
        }
        int allowedChanges = lowerCaseWord.length() < LONGER_WORD_LENGTH ? 1 : 2;
        String closest = null;
        int fewestChanges = allowedChanges + 1;
        for (String command : ALL) {
            int changes = changesBetween(lowerCaseWord, command);
            if (changes < fewestChanges) {
                fewestChanges = changes;
                closest = command;
            }
        }
        return Optional.ofNullable(closest);
    }

    /**
     * Returns the number of changes that turn {@code from} into {@code to}, where a change is to add, remove or
     * change a letter, or to swap two neighbouring letters.
     */
    static int changesBetween(String from, String to) {
        int[][] changes = new int[from.length() + 1][to.length() + 1];
        for (int i = 0; i <= from.length(); i++) {
            changes[i][0] = i;
        }
        for (int j = 0; j <= to.length(); j++) {
            changes[0][j] = j;
        }
        for (int i = 1; i <= from.length(); i++) {
            for (int j = 1; j <= to.length(); j++) {
                int substitutionCost = from.charAt(i - 1) == to.charAt(j - 1) ? 0 : 1;
                changes[i][j] = Math.min(Math.min(changes[i - 1][j] + 1, changes[i][j - 1] + 1),
                        changes[i - 1][j - 1] + substitutionCost);
                boolean isSwapped = i > 1 && j > 1 && from.charAt(i - 1) == to.charAt(j - 2)
                        && from.charAt(i - 2) == to.charAt(j - 1);
                if (isSwapped) {
                    changes[i][j] = Math.min(changes[i][j], changes[i - 2][j - 2] + 1);
                }
            }
        }
        return changes[from.length()][to.length()];
    }
}

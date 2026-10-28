package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;

public class CommandCompleterTest {

    @Test
    public void complete_nullText_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> CommandCompleter.complete(null));
    }

    @Test
    public void complete_oneCommandStartsWithTheText_completesTheWordAndAddsASpace() {
        assertEquals("filter ", CommandCompleter.complete("fil"));
        assertEquals("stats ", CommandCompleter.complete("sta"));
        assertEquals("unassign-manager ", CommandCompleter.complete("unas"));
        assertEquals("delete ", CommandCompleter.complete("d"));
        assertEquals("restore ", CommandCompleter.complete("res"));
    }

    @Test
    public void complete_wholeWord_addsASpace() {
        assertEquals("list ", CommandCompleter.complete("list"));
        assertEquals("find ", CommandCompleter.complete("find"));
    }

    @Test
    public void complete_severalCommandsStartWithTheText_completesTheSharedStart() {
        // unarchive, unassign-manager and undo all start with "un"
        assertEquals("un", CommandCompleter.complete("u"));
    }

    @Test
    public void complete_nothingMoreIsShared_isNotChanged() {
        // find and filter
        assertEquals("fi", CommandCompleter.complete("fi"));
        // sort and stats
        assertEquals("s", CommandCompleter.complete("s"));
        // add and archive
        assertEquals("a", CommandCompleter.complete("a"));
        // edit, exit and export share only "e", and exit and export share only "ex"
        assertEquals("e", CommandCompleter.complete("e"));
        assertEquals("ex", CommandCompleter.complete("ex"));
        // unarchive, unassign-manager and undo share only "un"
        assertEquals("un", CommandCompleter.complete("un"));
    }

    @Test
    public void complete_ignoresLetterCaseOfTheText() {
        assertEquals("filter ", CommandCompleter.complete("FIL"));
        assertEquals("filter ", CommandCompleter.complete("Fil"));
    }

    @Test
    public void complete_noCommandStartsWithTheText_isNotChanged() {
        assertEquals("xyz", CommandCompleter.complete("xyz"));
        assertEquals("filters", CommandCompleter.complete("filters"));
    }

    @Test
    public void complete_emptyText_isNotChanged() {
        assertEquals("", CommandCompleter.complete(""));
    }

    @Test
    public void complete_textWithASpace_isNotChanged() {
        assertEquals("filter d/Sa", CommandCompleter.complete("filter d/Sa"));
        assertEquals("fil ", CommandCompleter.complete("fil "));
        assertEquals(" fil", CommandCompleter.complete(" fil"));
    }

    @Test
    public void commandWords_areRealCommands() {
        AddressBookParser parser = new AddressBookParser();
        for (String word : CommandCompleter.COMMAND_WORDS) {
            try {
                parser.parseCommand(word);
            } catch (ParseException e) {
                // a command that needs arguments fails with its own message, but not as an unknown command
                assertFalse(e.getMessage().equals(String.format(Messages.MESSAGE_UNKNOWN_COMMAND, word)),
                        word + " is not a command");
            }
        }
    }

    @Test
    public void commandWords_areAllTheCommandsInTheHelpSummary() {
        Set<String> inHelp = HelpCommand.COMMAND_SUMMARY.lines()
                .skip(1)
                .map(line -> line.split(" ")[0])
                .collect(Collectors.toSet());

        assertTrue(inHelp.containsAll(CommandCompleter.COMMAND_WORDS), "a word is not in the help summary");
        assertEquals(inHelp, new HashSet<>(CommandCompleter.COMMAND_WORDS));
    }
}

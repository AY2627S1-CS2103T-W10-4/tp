package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_COMMAND_WORDS_ARE_LOWERCASE;
import static seedu.address.logic.Messages.MESSAGE_DID_YOU_MEAN;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ID;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.commands.BackupCommand;
import seedu.address.logic.commands.CheckCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditEmployeeDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.StatsCommand;
import seedu.address.logic.commands.UnarchiveCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.EmployeeMatchesCriteriaPredicate;
import seedu.address.model.employee.NameContainsKeywordsPredicate;
import seedu.address.testutil.EditEmployeeDescriptorBuilder;
import seedu.address.testutil.EmployeeBuilder;
import seedu.address.testutil.EmployeeUtil;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_historyCommands_acceptSurroundingSpacesAndTabs() throws Exception {
        for (String padding : List.of("", "  ", "\t", " \t ")) {
            assertTrue(parser.parseCommand(padding + "undo" + padding) instanceof UndoCommand);
            assertTrue(parser.parseCommand(padding + "redo" + padding) instanceof RedoCommand);
        }
    }

    @Test
    public void parseCommand_historyArguments_rejectedWithExactUsage() {
        for (String argument : List.of("2", "id/E0123", "now", "undo", "-1")) {
            assertThrows(ParseException.class,
                    "Invalid command format.\nundo: Reverts the most recent data-changing command.\n"
                            + "Parameters: none\nExample: undo", () -> parser.parseCommand("undo \t" + argument));
            assertThrows(ParseException.class,
                    "Invalid command format.\nredo: Reapplies the most recently undone data-changing command.\n"
                            + "Parameters: none\nExample: redo", () -> parser.parseCommand("redo \t" + argument));
        }
    }

    @Test
    public void parseCommand_historyAliases_preservesUnknownCommandWord() {
        String lowercaseHint = "\nDid you mean %s? Command words are written in lowercase.";
        for (String[] wordAndHint : new String[][] {{"UNDO", String.format(lowercaseHint, "undo")},
            {"Undo", String.format(lowercaseHint, "undo")}, {"REDO", String.format(lowercaseHint, "redo")},
            {"u", ""}, {"r", ""}, {"undo2", "\nDid you mean undo?"}}) {
            String word = wordAndHint[0];
            String expectedMessage = "Unknown command: " + word + "\nType help to see the available commands."
                    + wordAndHint[1];
            assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("  " + word + "  "));
        }
    }

    @Test
    public void parseCommand_commandWordWithOtherLetterCase_saysThatCommandWordsAreLowercase() {
        String lowercaseHint = "\nDid you mean %s? Command words are written in lowercase.";
        String listMessage = "Unknown command: List\nType help to see the available commands."
                + String.format(lowercaseHint, "list");
        assertThrows(ParseException.class, listMessage, () -> parser.parseCommand("List"));
        assertThrows(ParseException.class, listMessage, () -> parser.parseCommand("List archived"));

        String findMessage = "Unknown command: Find\nType help to see the available commands."
                + String.format(lowercaseHint, "find");
        assertThrows(ParseException.class, findMessage, () -> parser.parseCommand("Find alex"));
    }

    @Test
    public void parseCommand_mistypedCommandWord_suggestsTheClosestCommand() {
        String filterMessage = "Unknown command: fliter\nType help to see the available commands.\n"
                + "Did you mean filter?";
        assertThrows(ParseException.class, filterMessage, () -> parser.parseCommand("fliter d/Sales"));

        String sortMessage = "Unknown command: sortt\nType help to see the available commands.\n"
                + "Did you mean sort?";
        assertThrows(ParseException.class, sortMessage, () -> parser.parseCommand("sortt name"));
    }

    @Test
    public void parseCommand_commandWordNotCloseToAnyCommand_hasNoSuggestion() {
        String message = "Unknown command: xyz\nType help to see the available commands.";
        assertThrows(ParseException.class, message, () -> parser.parseCommand("xyz 123"));
    }

    @Test
    public void parseCommand_add() throws Exception {
        Employee employee = new EmployeeBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(EmployeeUtil.getAddCommand(employee));
        assertEquals(new AddCommand(employee), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + " 3") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " id/EMP-0042");
        assertEquals(new DeleteCommand(new EmployeeId("EMP-0042")), command);
    }

    @Test
    public void parseCommand_deleteMixedCaseId_preservesIdAsTyped() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand("delete id/eMp-0042");
        assertEquals("eMp-0042", command.getTargetId().value);
    }

    @Test
    public void parseCommand_archiveAndUnarchive() throws Exception {
        assertEquals(new ArchiveCommand(new EmployeeId("E0123")), parser.parseCommand("archive id/E0123"));
        assertEquals(new UnarchiveCommand(new EmployeeId("E0123")), parser.parseCommand("unarchive id/E0123"));
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Employee employee = new EmployeeBuilder().build();
        EditEmployeeDescriptor descriptor = new EditEmployeeDescriptorBuilder(employee).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + PREFIX_ID + employee.getId().value + " " + EmployeeUtil.getEditEmployeeDescriptorDetails(descriptor));
        assertEquals(new EditCommand(employee.getId(), descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
    }

    @Test
    public void parseCommand_exitWithArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExitCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(ExitCommand.COMMAND_WORD + " hello"));
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(ExitCommand.COMMAND_WORD + " 1"));
    }

    @Test
    public void parseCommand_filter() throws Exception {
        FilterCommand command = (FilterCommand) parser.parseCommand(
                FilterCommand.COMMAND_WORD + " d/Engineering r/Manager");
        assertEquals(new FilterCommand(new EmployeeMatchesCriteriaPredicate(
                List.of(), List.of(), List.of("Engineering"), List.of("Manager"))), command);
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_backup() throws Exception {
        assertTrue(parser.parseCommand(BackupCommand.COMMAND_WORD) instanceof BackupCommand);
        assertTrue(parser.parseCommand("  " + BackupCommand.COMMAND_WORD + "  ") instanceof BackupCommand);
    }

    @Test
    public void parseCommand_backupWithArguments_throwsParseException() {
        assertThrows(ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, BackupCommand.MESSAGE_USAGE), () ->
                parser.parseCommand(BackupCommand.COMMAND_WORD + " now"));
    }

    @Test
    public void parseCommand_statsAndCheck_acceptSurroundingWhitespace() throws Exception {
        assertTrue(parser.parseCommand("stats") instanceof StatsCommand);
        assertTrue(parser.parseCommand(" \tstats \t ") instanceof StatsCommand);
        assertTrue(parser.parseCommand("check") instanceof CheckCommand);
        assertTrue(parser.parseCommand(" \tcheck \t ") instanceof CheckCommand);
    }

    @Test
    public void parseCommand_statsAndCheckWithArguments_throwsParseException() {
        for (String arguments : List.of(" now", " d/Ops", " 1", " archived")) {
            assertThrows(ParseException.class,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, StatsCommand.MESSAGE_USAGE), () ->
                    parser.parseCommand("stats" + arguments));
            assertThrows(ParseException.class,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, CheckCommand.MESSAGE_USAGE), () ->
                    parser.parseCommand("check" + arguments));
        }
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertEquals(new ListCommand(), parser.parseCommand(ListCommand.COMMAND_WORD));
        assertTrue(parser.parseCommand("  " + ListCommand.COMMAND_WORD + "  ") instanceof ListCommand);
        assertEquals(new ListCommand(true), parser.parseCommand("list archived"));
    }

    @Test
    public void parseCommand_listWithArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(ListCommand.COMMAND_WORD + " employee"));
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(ListCommand.COMMAND_WORD + " 1"));
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(ListCommand.COMMAND_WORD + " d/Engineering"));
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        String unknownCommand = "unknownCommand";
        String unknownCommandMessage = String.format(MESSAGE_UNKNOWN_COMMAND, unknownCommand);
        // the command words are in lowercase, and the message says which command was meant
        String uppercaseCommandMessage = String.format(MESSAGE_UNKNOWN_COMMAND, "LIST") + "\n"
                + String.format(MESSAGE_DID_YOU_MEAN, "list") + MESSAGE_COMMAND_WORDS_ARE_LOWERCASE;

        assertThrows(ParseException.class, unknownCommandMessage, () -> parser.parseCommand(unknownCommand));
        assertThrows(ParseException.class, uppercaseCommandMessage, () -> parser.parseCommand("LIST"));
    }

    @Test
    public void parseCommand_sort() throws Exception {
        SortCommand command = (SortCommand) parser.parseCommand(SortCommand.COMMAND_WORD + " department");
        assertEquals(new SortCommand(SortCommand.SortField.DEPARTMENT), command);

        SortCommand descendingCommand = (SortCommand) parser.parseCommand(
                SortCommand.COMMAND_WORD + " department desc");
        assertEquals(new SortCommand(SortCommand.SortField.DEPARTMENT, SortCommand.SortOrder.DESC),
                descendingCommand);
    }
}

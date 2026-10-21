package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.HelpCommand.COMMAND_SUMMARY;
import static seedu.address.logic.commands.HelpCommand.SHOWING_HELP_MESSAGE;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;

public class HelpCommandTest {
    private Model model = new ModelManager();
    private Model expectedModel = new ModelManager();

    @Test
    public void execute_help_success() {
        CommandResult expectedCommandResult = new CommandResult(SHOWING_HELP_MESSAGE, true, false);
        assertCommandSuccess(new HelpCommand(), model, expectedCommandResult, expectedModel);
    }

    @Test
    public void commandSummary_listsEveryCommand() {
        String[] commandWords = {AddCommand.COMMAND_WORD, ListCommand.COMMAND_WORD, FindCommand.COMMAND_WORD,
            EditCommand.COMMAND_WORD, DeleteCommand.COMMAND_WORD, ClearCommand.COMMAND_WORD,
            BackupCommand.COMMAND_WORD, StatsCommand.COMMAND_WORD, CheckCommand.COMMAND_WORD,
            HelpCommand.COMMAND_WORD, ExitCommand.COMMAND_WORD};
            ArchiveCommand.COMMAND_WORD, UnarchiveCommand.COMMAND_WORD, ImportCommand.COMMAND_WORD,
            ExportCommand.COMMAND_WORD, BackupCommand.COMMAND_WORD,
            HelpCommand.COMMAND_WORD, ExitCommand.COMMAND_WORD, UndoCommand.COMMAND_WORD, RedoCommand.COMMAND_WORD};
        for (String commandWord : commandWords) {
            assertTrue(COMMAND_SUMMARY.lines().anyMatch(line -> line.equals(commandWord)
                    || line.startsWith(commandWord + " ")), commandWord + " is missing from the help summary");
        }
    }

    @Test
    public void commandSummary_showsEmployeeIdFormats() {
        assertTrue(COMMAND_SUMMARY.contains("add id/EMPLOYEE_ID n/NAME p/PHONE e/EMAIL d/DEPARTMENT r/ROLE"));
        assertTrue(COMMAND_SUMMARY.contains(
                "edit id/EMPLOYEE_ID [n/NAME] [p/PHONE] [e/EMAIL] [d/DEPARTMENT] [r/ROLE]"));
        assertTrue(COMMAND_SUMMARY.contains("delete id/EMPLOYEE_ID"));
        assertTrue(COMMAND_SUMMARY.contains("archive id/EMPLOYEE_ID"));
        assertTrue(COMMAND_SUMMARY.contains("unarchive id/EMPLOYEE_ID"));
        assertTrue(COMMAND_SUMMARY.lines().anyMatch("list archived"::equals));
    }

    @Test
    public void commandSummary_showsFileCommandFormats() {
        assertTrue(COMMAND_SUMMARY.lines().anyMatch("import FILE_PATH"::equals));
        assertTrue(COMMAND_SUMMARY.lines().anyMatch("export FILE_PATH"::equals));
        assertTrue(COMMAND_SUMMARY.lines().anyMatch("backup"::equals));
    }
}

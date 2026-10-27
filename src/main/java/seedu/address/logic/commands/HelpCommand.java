package seedu.address.logic.commands;

import static seedu.address.logic.parser.CliSyntax.PREFIX_DEPARTMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import seedu.address.model.Model;

/**
 * Opens the help window, which lists the format of every command and links to the user guide.
 */
public class HelpCommand extends Command {

    public static final String COMMAND_WORD = "help";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows program usage instructions.\n"
            + "Example: " + COMMAND_WORD;

    public static final String SHOWING_HELP_MESSAGE = "Opened help window.";

    public static final String COMMAND_SUMMARY = String.join("\n",
            "Commands:",
            AddCommand.COMMAND_WORD + " " + PREFIX_ID + "EMPLOYEE_ID " + PREFIX_NAME + "NAME " + PREFIX_PHONE
                    + "PHONE " + PREFIX_EMAIL + "EMAIL " + PREFIX_DEPARTMENT + "DEPARTMENT " + PREFIX_ROLE
                    + "ROLE [m/MANAGER_ID]",
            ListCommand.COMMAND_WORD,
            SortCommand.COMMAND_WORD + " FIELD [asc|desc]",
            FindCommand.COMMAND_WORD + " KEYWORD [MORE_KEYWORDS]",
            StatsCommand.COMMAND_WORD,
            CheckCommand.COMMAND_WORD,
            FilterCommand.COMMAND_WORD + " [" + PREFIX_ID + "EMPLOYEE_ID] [" + PREFIX_NAME + "NAME] ["
                    + PREFIX_DEPARTMENT + "DEPARTMENT] [" + PREFIX_ROLE + "ROLE]",
            EditCommand.COMMAND_WORD + " " + PREFIX_ID + "EMPLOYEE_ID [" + PREFIX_NAME + "NAME] [" + PREFIX_PHONE
                    + "PHONE] [" + PREFIX_EMAIL + "EMAIL] [" + PREFIX_DEPARTMENT + "DEPARTMENT] [" + PREFIX_ROLE
                    + "ROLE] [m/MANAGER_ID] (empty m/ removes manager)",
            DeleteCommand.COMMAND_WORD + " " + PREFIX_ID + "EMPLOYEE_ID",
            ManagerCommand.COMMAND_WORD + " id/EMPLOYEE_ID m/MANAGER_ID",
            UnassignManagerCommand.COMMAND_WORD + " id/EMPLOYEE_ID",
            TeamCommand.COMMAND_WORD + " id/EMPLOYEE_ID (employee, manager and direct reports)",
            OrgCommand.COMMAND_WORD + " id/EMPLOYEE_ID (full team)",
            ChainCommand.COMMAND_WORD + " id/EMPLOYEE_ID (reporting chain)",
            ArchiveCommand.COMMAND_WORD + " " + PREFIX_ID + "EMPLOYEE_ID",
            UnarchiveCommand.COMMAND_WORD + " " + PREFIX_ID + "EMPLOYEE_ID",
            ListCommand.COMMAND_WORD + " archived",
            ClearCommand.COMMAND_WORD,
            ImportCommand.COMMAND_WORD + " FILE_PATH",
            ExportCommand.COMMAND_WORD + " FILE_PATH",
            BackupCommand.COMMAND_WORD,
            UndoCommand.COMMAND_WORD,
            RedoCommand.COMMAND_WORD,
            COMMAND_WORD,
            ExitCommand.COMMAND_WORD);

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(SHOWING_HELP_MESSAGE, true, false);
    }
}

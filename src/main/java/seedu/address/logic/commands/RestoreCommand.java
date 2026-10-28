package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;

/**
 * Replaces the complete roster with a validated JSON backup as one undoable change.
 */
public class RestoreCommand extends Command {
    public static final String COMMAND_WORD = "restore";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Replaces all employee records with a JSON backup.\n"
            + "Parameters: file/JSON_FILE_PATH\n"
            + "Example: " + COMMAND_WORD + " file/data/backups/huntr-20261020-153005.json";
    public static final String MESSAGE_SUCCESS = "Restored %1$d employee(s) from %2$s";
    public static final String MESSAGE_FILE_NOT_FOUND = "Backup file not found: %s";
    public static final String MESSAGE_READ_FAILED = "Could not restore %s. Check that it is a readable JSON backup "
            + "with valid employee data and reporting relationships. No employee records were changed.";

    private final Path filePath;

    public RestoreCommand(Path filePath) {
        this.filePath = requireNonNull(filePath);
    }

    @Override
    public HistoryAction getHistoryAction() {
        return HistoryAction.COMMIT;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        ReadOnlyAddressBook restored;
        try {
            restored = new JsonAddressBookStorage(filePath).readBackup().orElseThrow(() ->
                    new CommandException(String.format(MESSAGE_FILE_NOT_FOUND, filePath)));
        } catch (DataLoadingException e) {
            throw new CommandException(String.format(MESSAGE_READ_FAILED, filePath), e);
        }
        // The complete backup, including relationships, is validated before the live model is touched.
        model.setAddressBook(restored);
        model.updateFilteredEmployeeList(Model.PREDICATE_SHOW_ALL_EMPLOYEES);
        return new CommandResult(String.format(MESSAGE_SUCCESS, restored.getEmployeeList().size(), filePath));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof RestoreCommand otherCommand
                && filePath.equals(otherCommand.filePath);
    }

    @Override
    public int hashCode() {
        return filePath.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("filePath", filePath).toString();
    }
}

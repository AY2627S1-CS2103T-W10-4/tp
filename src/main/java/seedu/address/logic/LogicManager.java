package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.employee.Employee;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    public static final String DELETE_PERMISSION_ERROR_FORMAT = "Could not delete employee %s: insufficient permission "
            + "to save employee records. No employee records were changed.";
    public static final String DELETE_SAVE_ERROR_FORMAT = "Could not delete employee %s: employee records could not "
            + "be saved. No employee records were changed.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        Command command = addressBookParser.parseCommand(commandText);
        if (!command.requiresSave()) {
            return command.execute(model);
        }
        return executeAndSave(command);
    }

    /**
     * Saves staged employee changes before publishing the records and the command's resulting filter.
     */
    private CommandResult executeAndSave(Command command) throws CommandException {
        Model candidate = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        candidate.updateFilteredEmployeeList(model.getEmployeeFilter());
        CommandResult result = command.execute(candidate);
        try {
            storage.saveAddressBook(candidate.getAddressBook());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not save employee records for " + command.getClass().getSimpleName(), e);
            throw createSaveException(command, e);
        }
        model.setAddressBook(candidate.getAddressBook());
        model.updateFilteredEmployeeList(candidate.getEmployeeFilter());
        return result;
    }

    private CommandException createSaveException(Command command, IOException cause) {
        if (command instanceof DeleteCommand deleteCommand) {
            String messageFormat = cause instanceof AccessDeniedException
                    ? DELETE_PERMISSION_ERROR_FORMAT : DELETE_SAVE_ERROR_FORMAT;
            return new CommandException(String.format(messageFormat, deleteCommand.getTargetId()), cause);
        }
        String messageFormat = cause instanceof AccessDeniedException
                ? FILE_OPS_PERMISSION_ERROR_FORMAT : FILE_OPS_ERROR_FORMAT;
        return new CommandException(String.format(messageFormat, cause.getMessage()), cause);
    }

    @Override
    public ObservableList<Employee> getFilteredEmployeeList() {
        return model.getFilteredEmployeeList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}

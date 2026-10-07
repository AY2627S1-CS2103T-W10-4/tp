package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /**
     * Returns whether this command requires saving employee records before publishing its result.
     * Commands that change employee records must override this method to return true.
     */
    public boolean requiresSave() {
        return false;
    }

    /**
     * Executes the command and returns the result message.
     *
     * @param model {@code Model} which the command should operate on.
     * @return feedback message of the operation result for display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

}

package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /** Describes the command's participation in employee history. */
    public enum HistoryAction {
        /** Leaves employee data and history unchanged. */
        NONE,
        /** Records employee changes after successful execution. */
        COMMIT,
        /** Navigates existing snapshots without recording a new one. */
        NAVIGATE
    }

    /**
     * Returns the history action used by LogicManager for both checkpointing and committing.
     * Modifying commands declare {@code COMMIT}; undo and redo declare {@code NAVIGATE}.
     */
    public HistoryAction getHistoryAction() {
        return HistoryAction.NONE;
    }

    /** Returns whether this command may change employee data or navigate employee history. */
    public final boolean isDataChanging() {
        return getHistoryAction() != HistoryAction.NONE;
    }

    /**
     * Executes the command and returns the result message. LogicManager commits successful
     * modifying commands before saving; commands must not commit their own changes.
     *
     * @param model {@code Model} which the command should operate on.
     * @return feedback message of the operation result for display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

}

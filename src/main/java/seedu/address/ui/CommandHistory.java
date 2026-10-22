package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The commands that were typed in the command box, so that the user can bring one back with the up and down keys.
 *
 * <p>Going back keeps what the user had typed so far, and going forward past the newest command shows it again.
 */
final class CommandHistory {

    static final int MAX_SIZE = 100;

    private final List<String> commands = new ArrayList<>();
    private int position = 0;
    private String draft = "";

    /**
     * Remembers {@code command} as the newest command, and moves back to the end of the history.
     * A blank command, and a command that is the same as the newest one, is not added again.
     */
    void record(String command) {
        requireNonNull(command);
        if (!command.isBlank() && (commands.isEmpty() || !commands.get(commands.size() - 1).equals(command))) {
            commands.add(command);
            if (commands.size() > MAX_SIZE) {
                commands.remove(0);
            }
        }
        reset();
    }

    /**
     * Returns the command before the one that is shown, or {@code currentText} if there is none to go back to.
     * The first step back keeps {@code currentText}, so that {@link #next(String)} can bring it back. If the newest
     * command is already in the command box, the first step goes to the one before it.
     */
    String previous(String currentText) {
        requireNonNull(currentText);
        if (commands.isEmpty()) {
            return currentText;
        }
        int target = position - 1;
        if (position == commands.size()) {
            draft = currentText;
            // the newest command can already be in the box, for example when it failed, so skip it
            if (target > 0 && commands.get(target).equals(currentText)) {
                target--;
            }
        }
        position = Math.max(0, target);
        return commands.get(position);
    }

    /**
     * Returns the command after the one that is shown. After the newest command it returns what the user had
     * typed before going back. When the user is not going through the history, it returns {@code currentText}.
     */
    String next(String currentText) {
        requireNonNull(currentText);
        if (position >= commands.size()) {
            return currentText;
        }
        position++;
        return position == commands.size() ? draft : commands.get(position);
    }

    /** Moves back to the end of the history, and forgets what the user had typed. */
    void reset() {
        position = commands.size();
        draft = "";
    }
}

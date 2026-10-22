package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class CommandHistoryTest {

    private final CommandHistory history = new CommandHistory();

    private void recordAll(String... commands) {
        for (String command : commands) {
            history.record(command);
        }
    }

    @Test
    public void nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> history.record(null));
        assertThrows(NullPointerException.class, () -> history.previous(null));
        assertThrows(NullPointerException.class, () -> history.next(null));
    }

    @Test
    public void previous_emptyHistory_keepsTheText() {
        assertEquals("typing", history.previous("typing"));
        assertEquals("typing", history.next("typing"));
    }

    @Test
    public void previous_goesBackFromTheNewestCommand() {
        recordAll("list", "filter d/Sales", "sort name");

        assertEquals("sort name", history.previous(""));
        assertEquals("filter d/Sales", history.previous("sort name"));
        assertEquals("list", history.previous("filter d/Sales"));
    }

    @Test
    public void previous_newestCommandIsAlreadyInTheBox_skipsIt() {
        recordAll("list", "stats", "bad command");

        // "bad command" failed, so it is still in the box
        assertEquals("stats", history.previous("bad command"));
        assertEquals("list", history.previous("stats"));
        assertEquals("stats", history.next("list"));
        assertEquals("bad command", history.next("stats"));
        assertEquals("bad command", history.next("bad command"));
    }

    @Test
    public void previous_onlyCommandIsAlreadyInTheBox_showsIt() {
        recordAll("list");

        assertEquals("list", history.previous("list"));
    }

    @Test
    public void previous_atTheOldestCommand_staysThere() {
        recordAll("list", "stats");

        history.previous("");
        history.previous("");
        assertEquals("list", history.previous("list"));
        assertEquals("list", history.previous("list"));
    }

    @Test
    public void next_goesForwardAndEndsWithWhatWasTyped() {
        recordAll("list", "filter d/Sales", "sort name");

        history.previous("fil");
        history.previous("sort name");
        assertEquals("sort name", history.next("filter d/Sales"));
        assertEquals("fil", history.next("sort name"));
    }

    @Test
    public void next_notGoingThroughTheHistory_keepsTheText() {
        recordAll("list");

        assertEquals("typing", history.next("typing"));
    }

    @Test
    public void next_afterTheNewest_staysOnTheDraft() {
        recordAll("list");

        history.previous("draft");
        assertEquals("draft", history.next("list"));
        assertEquals("draft", history.next("draft"));
    }

    @Test
    public void record_startsAgainFromTheNewestCommand() {
        recordAll("list", "stats");
        history.previous("");
        history.previous("");

        history.record("check");
        assertEquals("check", history.previous(""));
    }

    @Test
    public void record_blankCommand_notAdded() {
        recordAll("list", "", "   ");

        assertEquals("list", history.previous(""));
        assertEquals("list", history.previous("list"));
    }

    @Test
    public void record_sameAsTheNewest_notAddedAgain() {
        recordAll("list", "stats", "stats", "stats");

        assertEquals("stats", history.previous(""));
        assertEquals("list", history.previous("stats"));
    }

    @Test
    public void record_sameAsAnOlderCommand_addedAgain() {
        recordAll("list", "stats", "list");

        assertEquals("list", history.previous(""));
        assertEquals("stats", history.previous("list"));
        assertEquals("list", history.previous("stats"));
    }

    @Test
    public void record_moreThanTheMaximum_forgetsTheOldest() {
        for (int i = 0; i < CommandHistory.MAX_SIZE + 5; i++) {
            history.record("command " + i);
        }

        String shown = "";
        for (int i = 0; i < CommandHistory.MAX_SIZE + 20; i++) {
            shown = history.previous(shown);
        }
        // command 0 to command 4 are forgotten
        assertEquals("command 5", shown);
    }

    @Test
    public void reset_goesBackToTheEndAndForgetsTheDraft() {
        recordAll("list", "stats");
        history.previous("draft");
        history.previous("stats");

        history.reset();
        assertEquals("", history.next(""));
        assertEquals("stats", history.previous(""));
    }
}

package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class CommandWordsTest {

    @Test
    public void closestTo_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> CommandWords.closestTo(null));
    }

    @Test
    public void allWords_areLowerCaseAndDifferent() {
        for (String word : CommandWords.ALL) {
            assertEquals(word.toLowerCase(Locale.ROOT), word);
        }
        assertEquals(CommandWords.ALL.size(), new HashSet<>(CommandWords.ALL).size());
    }

    @Test
    public void closestTo_sameWordWithOtherLetterCase_isTheCommand() {
        assertEquals(Optional.of("list"), CommandWords.closestTo("List"));
        assertEquals(Optional.of("list"), CommandWords.closestTo("LIST"));
        assertEquals(Optional.of("find"), CommandWords.closestTo("Find"));
        assertEquals(Optional.of("unassign-manager"), CommandWords.closestTo("UNASSIGN-MANAGER"));
    }

    @Test
    public void closestTo_neighbouringLettersSwapped_isTheCommand() {
        assertEquals(Optional.of("filter"), CommandWords.closestTo("fliter"));
        assertEquals(Optional.of("help"), CommandWords.closestTo("hlep"));
        assertEquals(Optional.of("stats"), CommandWords.closestTo("stast"));
    }

    @Test
    public void closestTo_oneLetterMissingAddedOrChanged_isTheCommand() {
        assertEquals(Optional.of("filter"), CommandWords.closestTo("filtr"));
        assertEquals(Optional.of("sort"), CommandWords.closestTo("sortt"));
        assertEquals(Optional.of("stats"), CommandWords.closestTo("stat"));
        assertEquals(Optional.of("delete"), CommandWords.closestTo("delet"));
        assertEquals(Optional.of("archive"), CommandWords.closestTo("archve"));
        assertEquals(Optional.of("list"), CommandWords.closestTo("lisr"));
    }

    @Test
    public void closestTo_twoChangesInALongerWord_isTheCommand() {
        assertEquals(Optional.of("unarchive"), CommandWords.closestTo("unarchve"));
        assertEquals(Optional.of("unassign-manager"), CommandWords.closestTo("unasign-mannager"));
        assertEquals(Optional.of("filter"), CommandWords.closestTo("fliterr"));
    }

    @Test
    public void closestTo_shortWord_needsToBeOneChangeAway() {
        assertEquals(Optional.of("add"), CommandWords.closestTo("ad"));
        assertEquals(Optional.of("org"), CommandWords.closestTo("ogr"));
        assertEquals(Optional.empty(), CommandWords.closestTo("a"));
        assertEquals(Optional.empty(), CommandWords.closestTo("ls"));
        assertEquals(Optional.empty(), CommandWords.closestTo("u"));
    }

    @Test
    public void closestTo_notCloseToAnyCommand_isEmpty() {
        assertEquals(Optional.empty(), CommandWords.closestTo("xyz"));
        assertEquals(Optional.empty(), CommandWords.closestTo("unknownCommand"));
        assertEquals(Optional.empty(), CommandWords.closestTo("remove"));
        assertEquals(Optional.empty(), CommandWords.closestTo(""));
    }

    @Test
    public void closestTo_severalCommandsClose_isTheFewestChanges() {
        // "undo" is one change from "undo2", and "redo" is two
        assertEquals(Optional.of("undo"), CommandWords.closestTo("undo2"));
        assertEquals(Optional.of("redo"), CommandWords.closestTo("rdo"));
    }

    @Test
    public void changesBetween() {
        assertEquals(0, CommandWords.changesBetween("list", "list"));
        assertEquals(1, CommandWords.changesBetween("lisr", "list"));
        assertEquals(1, CommandWords.changesBetween("lst", "list"));
        assertEquals(1, CommandWords.changesBetween("liist", "list"));
        // a swap of two neighbouring letters is one change
        assertEquals(1, CommandWords.changesBetween("fliter", "filter"));
        assertEquals(2, CommandWords.changesBetween("ab", "ba" + "c"));
        assertEquals(4, CommandWords.changesBetween("", "list"));
        assertEquals(4, CommandWords.changesBetween("list", ""));
        assertTrue(CommandWords.changesBetween("sort", "stats") > 1);
    }
}

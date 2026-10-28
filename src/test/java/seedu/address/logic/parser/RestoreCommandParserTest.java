package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RestoreCommand;

public class RestoreCommandParserTest {
    private final RestoreCommandParser parser = new RestoreCommandParser();

    @Test
    public void parse_validPaths_createsRestoreCommand() throws Exception {
        assertParseSuccess(parser, " file/data/backups/staff.json",
                new RestoreCommand(Path.of("data/backups/staff.json")));
        assertParseSuccess(parser, "  file/  backup folder/staff.JSON  ",
                new RestoreCommand(Path.of("backup folder/staff.JSON")));
        Path absolute = Path.of("staff.json").toAbsolutePath();
        assertParseSuccess(parser, " file/" + absolute, new RestoreCommand(absolute));
        assertEquals(new RestoreCommand(Path.of("backup.json")),
                new AddressBookParser().parseCommand("restore file/backup.json"));
    }

    @Test
    public void parse_pathContainingFilePrefix_preservesEntirePath() throws Exception {
        for (String path : List.of("my file/staff.json", "my file/nested file/staff.json", "a.json file/b.json")) {
            RestoreCommand expected = new RestoreCommand(Path.of(path));
            assertParseSuccess(parser, " file/" + path + " ", expected);
            assertEquals(expected, new AddressBookParser().parseCommand("restore file/" + path));
        }
    }

    @Test
    public void parse_invalidArguments_rejected() {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RestoreCommand.MESSAGE_USAGE);
        for (String input : List.of("", " ", "backup.json", "file/", "file/  ", "file/staff.csv",
                "extra file/staff.json", "file/a\u0000.json")) {
            assertParseFailure(parser, input, message);
        }
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}

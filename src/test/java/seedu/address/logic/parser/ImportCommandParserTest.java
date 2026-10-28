package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ImportCommand;

public class ImportCommandParserTest {

    private final ImportCommandParser parser = new ImportCommandParser();

    @Test
    public void parse_validCsvPath_returnsImportCommand() {
        assertParseSuccess(parser, " exports/staff.csv ", new ImportCommand(Paths.get("exports", "staff.csv")));
    }

    @Test
    public void parse_blankArgs_throwsParseException() {
        assertParseFailure(parser, "   ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, ImportCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_filePrefix_resolvesSpecifiedPathWithoutExtraDirectory() throws Exception {
        assertParseSuccess(parser, "file/employees.csv", new ImportCommand(Paths.get("employees.csv")));
        assertParseSuccess(parser, "  file/  my exports/STAFF.CSV  ",
                new ImportCommand(Paths.get("my exports", "STAFF.CSV")));
        assertParseSuccess(parser, "file/file/staff.csv", new ImportCommand(Paths.get("file", "staff.csv")));
        assertParseSuccess(parser, "./file/staff.csv", new ImportCommand(Paths.get("file", "staff.csv")));
        assertEquals(new ImportCommand(Paths.get("employees.csv")),
                new AddressBookParser().parseCommand("import file/employees.csv"));
    }

    @Test
    public void parse_pathContainingFilePrefix_preservesEntirePath() throws Exception {
        for (String path : List.of("my file/staff.csv", "my file/nested file/staff.csv", "a.csv file/b.csv")) {
            ImportCommand expected = new ImportCommand(Paths.get(path));
            assertParseSuccess(parser, " file/" + path + " ", expected);
            assertParseSuccess(parser, " " + path + " ", expected);
            assertEquals(expected, new AddressBookParser().parseCommand("import file/" + path));
        }
    }

    @Test
    public void parse_invalidPrefixedPath_rejectedWithUsage() {
        for (String path : List.of("file/", "file/   ")) {
            assertParseFailure(parser, path, ParserUtil.MESSAGE_BLANK_FILE_PATH + "\n" + ImportCommand.MESSAGE_USAGE);
        }
        assertParseFailure(parser, "file/../staff.csv",
                ParserUtil.MESSAGE_FILE_PATH_OUTSIDE_APP_FOLDER + "\n" + ImportCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "file/staff.txt",
                ParserUtil.MESSAGE_FILE_PATH_NOT_CSV + "\n" + ImportCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "file/" + Paths.get("staff.csv").toAbsolutePath(),
                ParserUtil.MESSAGE_FILE_PATH_OUTSIDE_APP_FOLDER + "\n" + ImportCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_invalidPath_throwsParseExceptionWithUsage() {
        assertParseFailure(parser, " staff.txt",
                ParserUtil.MESSAGE_FILE_PATH_NOT_CSV + "\n" + ImportCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " ../staff.csv",
                ParserUtil.MESSAGE_FILE_PATH_OUTSIDE_APP_FOLDER + "\n" + ImportCommand.MESSAGE_USAGE);
    }

}

package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FILE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.ExportCommand;

public class ExportCommandParserTest {

    private final ExportCommandParser parser = new ExportCommandParser();

    @Test
    public void parse_validCsvPath_returnsExportCommand() {
        assertParseSuccess(parser, " exports/staff.csv ", new ExportCommand(Paths.get("exports", "staff.csv")));
    }

    @Test
    public void parse_blankArgs_throwsParseException() {
        assertParseFailure(parser, "   ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExportCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_filePrefix_resolvesSpecifiedPathWithoutExtraDirectory() throws Exception {
        assertParseSuccess(parser, "file/output.csv", new ExportCommand(Paths.get("output.csv")));
        assertParseSuccess(parser, "  file/  my exports/STAFF.CSV  ",
                new ExportCommand(Paths.get("my exports", "STAFF.CSV")));
        assertParseSuccess(parser, "file/file/staff.csv", new ExportCommand(Paths.get("file", "staff.csv")));
        assertParseSuccess(parser, "./file/staff.csv", new ExportCommand(Paths.get("file", "staff.csv")));
        assertEquals(new ExportCommand(Paths.get("output.csv")),
                new AddressBookParser().parseCommand("export file/output.csv"));
    }

    @Test
    public void parse_invalidPrefixedPath_rejectedWithUsage() {
        for (String path : List.of("file/", "file/   ")) {
            assertParseFailure(parser, path, ParserUtil.MESSAGE_BLANK_FILE_PATH + "\n" + ExportCommand.MESSAGE_USAGE);
        }
        assertParseFailure(parser, "file/a.csv file/b.csv",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_FILE) + "\n" + ExportCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "file/../staff.csv",
                ParserUtil.MESSAGE_FILE_PATH_OUTSIDE_APP_FOLDER + "\n" + ExportCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "file/staff.txt",
                ParserUtil.MESSAGE_FILE_PATH_NOT_CSV + "\n" + ExportCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "file/" + Paths.get("staff.csv").toAbsolutePath(),
                ParserUtil.MESSAGE_FILE_PATH_OUTSIDE_APP_FOLDER + "\n" + ExportCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_invalidPath_throwsParseExceptionWithUsage() {
        assertParseFailure(parser, " staff.txt",
                ParserUtil.MESSAGE_FILE_PATH_NOT_CSV + "\n" + ExportCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " ../staff.csv",
                ParserUtil.MESSAGE_FILE_PATH_OUTSIDE_APP_FOLDER + "\n" + ExportCommand.MESSAGE_USAGE);
    }

}

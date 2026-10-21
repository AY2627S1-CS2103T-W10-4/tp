package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

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
    public void parse_invalidPath_throwsParseExceptionWithUsage() {
        assertParseFailure(parser, " staff.txt",
                ParserUtil.MESSAGE_FILE_PATH_NOT_CSV + "\n" + ExportCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " ../staff.csv",
                ParserUtil.MESSAGE_FILE_PATH_OUTSIDE_APP_FOLDER + "\n" + ExportCommand.MESSAGE_USAGE);
    }

}

package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.model.employee.EmployeeId;

public class ArchiveCommandParserTest {
    private final ArchiveCommandParser parser = new ArchiveCommandParser();

    @Test
    public void parse_validId_success() {
        assertParseSuccess(parser, " id/E0123", new ArchiveCommand(new EmployeeId("E0123")));
    }

    @Test
    public void parse_extraMetadata_failure() {
        assertParseFailure(parser, " id/E0123 date/2026-10-19",
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                        "Unknown parameter: date/\n" + ArchiveCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_duplicateId_failure() {
        assertParseFailure(parser, " id/E0123 id/E9999",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ID));
    }
}

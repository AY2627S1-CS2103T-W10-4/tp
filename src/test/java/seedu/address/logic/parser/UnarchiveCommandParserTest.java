package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.UnarchiveCommand;
import seedu.address.model.employee.EmployeeId;

public class UnarchiveCommandParserTest {
    private final UnarchiveCommandParser parser = new UnarchiveCommandParser();

    @Test
    public void parse_validId_success() {
        assertParseSuccess(parser, " id/e0123", new UnarchiveCommand(new EmployeeId("e0123")));
    }

    @Test
    public void parse_unknownPrefix_failure() {
        assertParseFailure(parser, " x/E0123", String.format(Messages.MESSAGE_UNKNOWN_PARAMETER, "x/")
                + "\n" + UnarchiveCommand.MESSAGE_USAGE);
    }
}

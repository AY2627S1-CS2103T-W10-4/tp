package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.SortCommand.SortField;

public class SortCommandParserTest {

    private SortCommandParser parser = new SortCommandParser();

    private static String unknownFieldMessage(String field) {
        return String.format(SortCommand.MESSAGE_UNKNOWN_FIELD, field, SortField.names())
                + "\n" + SortCommand.MESSAGE_USAGE;
    }

    @Test
    public void parse_validFields_success() {
        assertParseSuccess(parser, " id", new SortCommand(SortField.ID));
        assertParseSuccess(parser, " name", new SortCommand(SortField.NAME));
        assertParseSuccess(parser, " department", new SortCommand(SortField.DEPARTMENT));
        assertParseSuccess(parser, " role", new SortCommand(SortField.ROLE));
    }

    @Test
    public void parse_fieldInAnyCaseWithExtraSpaces_success() {
        assertParseSuccess(parser, "   Department  ", new SortCommand(SortField.DEPARTMENT));
        assertParseSuccess(parser, " ROLE", new SortCommand(SortField.ROLE));
    }

    @Test
    public void parse_noField_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", expected);
        assertParseFailure(parser, "     ", expected);
    }

    @Test
    public void parse_unknownField_failure() {
        assertParseFailure(parser, " salary", unknownFieldMessage("salary"));
        assertParseFailure(parser, " phone", unknownFieldMessage("phone"));
    }

    @Test
    public void parse_moreThanOneWordOrPrefix_failure() {
        assertParseFailure(parser, " name asc", unknownFieldMessage("name asc"));
        assertParseFailure(parser, " name role", unknownFieldMessage("name role"));
        assertParseFailure(parser, " d/department", unknownFieldMessage("d/department"));
    }
}

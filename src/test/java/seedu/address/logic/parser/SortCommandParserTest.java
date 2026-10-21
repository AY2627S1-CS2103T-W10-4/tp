package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.SortCommand.SortField;
import seedu.address.logic.commands.SortCommand.SortOrder;

public class SortCommandParserTest {

    private SortCommandParser parser = new SortCommandParser();

    private static String unknownFieldMessage(String field) {
        return String.format(SortCommand.MESSAGE_UNKNOWN_FIELD, field, SortField.names())
                + "\n" + SortCommand.MESSAGE_USAGE;
    }

    private static String unknownOrderMessage(String order) {
        return String.format(SortCommand.MESSAGE_UNKNOWN_ORDER, order, SortOrder.keywords())
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
    public void parse_fieldWithOrder_success() {
        assertParseSuccess(parser, " name asc", new SortCommand(SortField.NAME, SortOrder.ASC));
        assertParseSuccess(parser, " name desc", new SortCommand(SortField.NAME, SortOrder.DESC));
        assertParseSuccess(parser, " id desc", new SortCommand(SortField.ID, SortOrder.DESC));
        assertParseSuccess(parser, " department desc", new SortCommand(SortField.DEPARTMENT, SortOrder.DESC));
        assertParseSuccess(parser, " role asc", new SortCommand(SortField.ROLE, SortOrder.ASC));
    }

    @Test
    public void parse_noOrder_defaultsToAscending() {
        assertParseSuccess(parser, " role", new SortCommand(SortField.ROLE, SortOrder.ASC));
    }

    @Test
    public void parse_orderInAnyCaseWithExtraSpaces_success() {
        assertParseSuccess(parser, "   NAME    DESC  ", new SortCommand(SortField.NAME, SortOrder.DESC));
        assertParseSuccess(parser, " Role Asc", new SortCommand(SortField.ROLE, SortOrder.ASC));
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
    public void parse_unknownOrder_failure() {
        assertParseFailure(parser, " name ascending", unknownOrderMessage("ascending"));
        assertParseFailure(parser, " name role", unknownOrderMessage("role"));
        assertParseFailure(parser, " name d/desc", unknownOrderMessage("d/desc"));
    }

    @Test
    public void parse_unknownFieldWithOrder_reportsTheField() {
        assertParseFailure(parser, " salary desc", unknownFieldMessage("salary"));
        assertParseFailure(parser, " desc name", unknownFieldMessage("desc"));
    }

    @Test
    public void parse_prefixAsField_failure() {
        assertParseFailure(parser, " d/department", unknownFieldMessage("d/department"));
    }

    @Test
    public void parse_moreThanTwoWords_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " name asc desc", expected);
        assertParseFailure(parser, " name desc extra", expected);
    }
}

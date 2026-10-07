package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteCommand;
import seedu.address.model.employee.EmployeeId;

public class DeleteCommandParserTest {
    private static final String USAGE =
            "delete: Deletes one employee by employee ID from all stored employee records.\n"
            + "Parameters: id/EMPLOYEE_ID (uppercase E followed by exactly four digits)\n"
            + "Example: delete id/E0123";
    private final DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_validIdAndWhitespace_returnsDeleteCommand() {
        for (String input : new String[] {" id/E0123", "id/E0123", "    id/E0123 ", "\tid/\t E0123\t",
            " id/ E0123"}) {
            assertParseSuccess(parser, input, new DeleteCommand(new EmployeeId("E0123")));
        }
        assertParseSuccess(parser, " id/E0000", new DeleteCommand(new EmployeeId("E0000")));
        assertParseSuccess(parser, " id/E9999", new DeleteCommand(new EmployeeId("E9999")));
    }

    @Test
    public void parse_missingOrLegacyArguments_showsFormat() {
        for (String input : new String[] {"", " \t", " id/", " id/ \t", " 1", " E0123", " ID/E0123",
            " extra id/E0123", " id E0123"}) {
            assertParseFailure(parser, input, String.format(MESSAGE_INVALID_COMMAND_FORMAT, USAGE));
        }
    }

    @Test
    public void parse_invalidId_showsConstraintsAndUsage() {
        for (String id : new String[] {"e0123", "EMP-0042", "123", "E123", "E01234", "E 0123", "E\t0123",
            "E0123,E0456", "E0123!", "E０１２３", "*"}) {
            assertParseFailure(parser, " id/" + id,
                    "Employee ID must be an uppercase E followed by exactly four digits (e.g. E0123).\n" + USAGE);
        }
    }

    @Test
    public void parse_extraArguments_showsUnexpectedArguments() {
        for (String input : new String[] {" id/E0123 E0456", " id/E0123 extra", " id/E0123 n/John",
            " id/E0123\tx/value", " id/ n/John"}) {
            assertParseFailure(parser, input, "Unexpected arguments. Specify exactly one employee ID.\n" + USAGE);
        }
    }

    @Test
    public void parse_repeatedPrefix_showsDuplicatePrefix() {
        for (String input : new String[] {" id/E0123 id/E0123", " id/E0123 id/E0456", " id/ id/",
            " id/E0123\tid/E0456", " extra id/bad id/E0123 n/John"}) {
            assertParseFailure(parser, input,
                    "Multiple values specified for the following single-valued field(s): id/\n" + USAGE);
        }
    }
}

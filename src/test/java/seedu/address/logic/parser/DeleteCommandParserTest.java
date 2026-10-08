package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.model.employee.EmployeeId;

public class DeleteCommandParserTest {

    private final DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_validIds_returnsDeleteCommand() {
        String[] validIds = {"E0001", "EMP-0042", "abc", "e0123", "2024-017", "2024_017",
            "1", "_", "-", "A".repeat(20)};
        for (String id : validIds) {
            assertParseSuccess(parser, " id/" + id, new DeleteCommand(new EmployeeId(id)));
        }
    }

    @Test
    public void parse_surroundingWhitespace_returnsDeleteCommand() {
        assertParseSuccess(parser, " \t id/ \tEMP-0042 \t", new DeleteCommand(new EmployeeId("EMP-0042")));
    }

    @Test
    public void parse_mixedCaseId_preservesCase() throws Exception {
        assertEquals("eMp-0042", parser.parse(" id/eMp-0042").getTargetId().value);
    }

    @Test
    public void parse_missingIdParameter_throwsSpecificParseException() {
        String[] missingIds = {"", " \t ", "1", "EMP-0042", "abc"};
        for (String args : missingIds) {
            assertParseFailure(parser, args, withUsage("Missing parameter: id/EMPLOYEE_ID."));
        }
    }

    @Test
    public void parse_blankId_throwsSpecificParseException() {
        assertParseFailure(parser, " id/", withUsage("Employee ID cannot be blank."));
        assertParseFailure(parser, " id/ \t ", withUsage("Employee ID cannot be blank."));
    }

    @Test
    public void parse_unknownParameter_throwsSpecificParseException() {
        String[] unknownParameters = {" n/Alice", " n/Alice id/E0001", " id/E0001 n/Alice",
            " id/E0001\tn/Alice", " id/ n/Alice", " id/E0001 unknown/value"};
        for (String args : unknownParameters) {
            String prefix = args.contains("unknown/") ? "unknown/" : "n/";
            assertParseFailure(parser, args, withUsage("Unknown parameter: " + prefix + ". Only id/ is supported."));
        }
        assertParseFailure(parser, " ID/E0001", withUsage("Unknown parameter: ID/. Only id/ is supported."));
    }

    @Test
    public void parse_duplicateIdParameter_throwsParseException() {
        String expectedMessage = withUsage(Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ID));
        assertParseFailure(parser, " id/E0001 id/E0002", expectedMessage);
        assertParseFailure(parser, " id/E0001\tid/E0001", expectedMessage);
        assertParseFailure(parser, " id/ id/E0001", expectedMessage);
    }

    @Test
    public void parse_unexpectedPreamble_throwsParseException() {
        assertParseFailure(parser, "1 id/E0001", withUsage(DeleteCommand.MESSAGE_UNEXPECTED_ARGUMENTS));
        assertParseFailure(parser, "abc id/E0001", withUsage(DeleteCommand.MESSAGE_UNEXPECTED_ARGUMENTS));
    }

    @Test
    public void parse_invalidIdFormat_throwsConstraintMessage() {
        String[] invalidIds = {"EMP 0042", "EMP\t0042", "EMP\n0042", "EMP/0042", "EMP@0042", "EMP.0042",
            "EMP+0042", "E0001 extra"};
        String expectedMessage = withUsage("Employee ID should be 1 to 20 characters long and contain only "
                + "letters, digits, hyphens and underscores.");
        for (String id : invalidIds) {
            assertParseFailure(parser, " id/" + id, expectedMessage);
        }
    }

    @Test
    public void parse_tooLongId_throwsTooLongMessage() {
        assertParseFailure(parser, " id/" + "A".repeat(21), withUsage(ParserUtil.MESSAGE_EMPLOYEE_ID_TOO_LONG));
    }

    private String withUsage(String message) {
        return message + "\n" + DeleteCommand.MESSAGE_USAGE;
    }
}

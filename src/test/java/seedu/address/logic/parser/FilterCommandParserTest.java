package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEPARTMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.model.employee.Department;
import seedu.address.model.employee.EmployeeMatchesCriteriaPredicate;
import seedu.address.model.employee.Role;

public class FilterCommandParserTest {

    private static final String USAGE = FilterCommand.MESSAGE_USAGE;
    private static final String INVALID_FORMAT = String.format(MESSAGE_INVALID_COMMAND_FORMAT, USAGE);

    private FilterCommandParser parser = new FilterCommandParser();

    private static FilterCommand filterOf(List<String> departmentWords, List<String> roleWords) {
        return new FilterCommand(new EmployeeMatchesCriteriaPredicate(departmentWords, roleWords));
    }

    @Test
    public void parse_departmentOnly_success() {
        assertParseSuccess(parser, " d/Engineering", filterOf(List.of("Engineering"), List.of()));
    }

    @Test
    public void parse_roleOnly_success() {
        assertParseSuccess(parser, " r/Manager", filterOf(List.of(), List.of("Manager")));
    }

    @Test
    public void parse_departmentAndRole_success() {
        FilterCommand expected = filterOf(List.of("Engineering"), List.of("Manager"));

        // either order
        assertParseSuccess(parser, " d/Engineering r/Manager", expected);
        assertParseSuccess(parser, " r/Manager d/Engineering", expected);
    }

    @Test
    public void parse_multipleWordsAndExtraSpaces_success() {
        assertParseSuccess(parser, "  d/  Human   Resources   r/ HR Executive  ",
                filterOf(List.of("Human", "Resources"), List.of("HR", "Executive")));
    }

    @Test
    public void parse_punctuationInValues_success() {
        assertParseSuccess(parser, " d/R&D r/Engineer (Backend)",
                filterOf(List.of("R&D"), List.of("Engineer", "(Backend)")));
    }

    @Test
    public void parse_noCriterion_failure() {
        assertParseFailure(parser, "", INVALID_FORMAT);
        assertParseFailure(parser, "     ", INVALID_FORMAT);
    }

    @Test
    public void parse_valueWithoutPrefix_failure() {
        assertParseFailure(parser, " Engineering", INVALID_FORMAT);
        assertParseFailure(parser, " Engineering d/Sales", INVALID_FORMAT);
    }

    @Test
    public void parse_unknownParameter_failure() {
        String unknownN = String.format(MESSAGE_INVALID_COMMAND_FORMAT, "Unknown parameter: n/\n" + USAGE);
        assertParseFailure(parser, " d/Sales n/John", unknownN);

        String unknownX = String.format(MESSAGE_INVALID_COMMAND_FORMAT, "Unknown parameter: x/\n" + USAGE);
        assertParseFailure(parser, " x/foo", unknownX);
    }

    @Test
    public void parse_blankValue_failure() {
        assertParseFailure(parser, " d/", Department.MESSAGE_BLANK + "\n" + USAGE);
        assertParseFailure(parser, " r/   ", Role.MESSAGE_BLANK + "\n" + USAGE);
        assertParseFailure(parser, " d/Sales r/", Role.MESSAGE_BLANK + "\n" + USAGE);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, " d/@@@", Department.MESSAGE_CONSTRAINTS + "\n" + USAGE);
        assertParseFailure(parser, " r/###", Role.MESSAGE_CONSTRAINTS + "\n" + USAGE);
        assertParseFailure(parser, " d/" + "D".repeat(101), Department.MESSAGE_TOO_LONG + "\n" + USAGE);
    }

    @Test
    public void parse_repeatedPrefix_failure() {
        assertParseFailure(parser, " d/Sales d/Finance",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DEPARTMENT));
        assertParseFailure(parser, " r/Manager r/Analyst",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ROLE));
    }
}

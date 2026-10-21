package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.EmployeeMatchesCriteriaPredicate;

public class FilterCommandParserTest {

    private static final String USAGE = FilterCommand.MESSAGE_USAGE;
    private static final String INVALID_FORMAT = String.format(MESSAGE_INVALID_COMMAND_FORMAT, USAGE);

    private FilterCommandParser parser = new FilterCommandParser();

    private static FilterCommand filterOf(List<String> ids, List<String> names, List<String> departments,
            List<String> roles) {
        return new FilterCommand(new EmployeeMatchesCriteriaPredicate(
                ids.stream().map(EmployeeId::new).toList(), names, departments, roles));
    }

    private static String noSearchWordMessage(String prefix) {
        return String.format(FilterCommand.MESSAGE_NO_SEARCH_WORD, prefix) + "\n" + USAGE;
    }

    @Test
    public void parse_oneCriterion_success() {
        assertParseSuccess(parser, " id/E0001", filterOf(List.of("E0001"), List.of(), List.of(), List.of()));
        assertParseSuccess(parser, " n/John", filterOf(List.of(), List.of("John"), List.of(), List.of()));
        assertParseSuccess(parser, " d/Engineering",
                filterOf(List.of(), List.of(), List.of("Engineering"), List.of()));
        assertParseSuccess(parser, " r/Manager", filterOf(List.of(), List.of(), List.of(), List.of("Manager")));
    }

    @Test
    public void parse_severalCriteria_success() {
        FilterCommand expected = filterOf(List.of(), List.of("John"), List.of("Sales"), List.of());

        // either order
        assertParseSuccess(parser, " n/John d/Sales", expected);
        assertParseSuccess(parser, " d/Sales n/John", expected);

        assertParseSuccess(parser, " id/E0001 n/John d/Sales r/Manager",
                filterOf(List.of("E0001"), List.of("John"), List.of("Sales"), List.of("Manager")));
    }

    @Test
    public void parse_repeatedPrefix_valuesAreAlternatives() {
        assertParseSuccess(parser, " d/Sales d/Finance r/Manager",
                filterOf(List.of(), List.of(), List.of("Sales", "Finance"), List.of("Manager")));
        assertParseSuccess(parser, " r/Manager r/Analyst",
                filterOf(List.of(), List.of(), List.of(), List.of("Manager", "Analyst")));
        assertParseSuccess(parser, " n/John n/Mary",
                filterOf(List.of(), List.of("John", "Mary"), List.of(), List.of()));
        assertParseSuccess(parser, " id/E0001 id/E0002",
                filterOf(List.of("E0001", "E0002"), List.of(), List.of(), List.of()));
    }

    @Test
    public void parse_multipleWordsInOneValue_staysOneValue() {
        // one value with two words is different from two values with one word each
        assertParseSuccess(parser, " r/Software Engineer",
                filterOf(List.of(), List.of(), List.of(), List.of("Software Engineer")));
        assertParseSuccess(parser, "  d/  Human   Resources   r/ HR Executive  ",
                filterOf(List.of(), List.of(), List.of("Human   Resources"), List.of("HR Executive")));
    }

    @Test
    public void parse_punctuationInValues_success() {
        assertParseSuccess(parser, " d/R&D r/(Backend)",
                filterOf(List.of(), List.of(), List.of("R&D"), List.of("(Backend)")));

        // a value that a role or department could not be added with can still be searched for
        assertParseSuccess(parser, " r/Software Engineer (Backend)",
                filterOf(List.of(), List.of(), List.of(), List.of("Software Engineer (Backend)")));
        assertParseSuccess(parser, " d/@Sales", filterOf(List.of(), List.of(), List.of("@Sales"), List.of()));
    }

    @Test
    public void parse_nameWithRelationWord_success() {
        assertParseSuccess(parser, " n/Ravi s/o Kumar d/Sales",
                filterOf(List.of(), List.of("Ravi s/o Kumar"), List.of("Sales"), List.of()));
        assertParseSuccess(parser, " n/Priya d/o Kumar",
                filterOf(List.of(), List.of("Priya d/o Kumar"), List.of(), List.of()));
    }

    @Test
    public void parse_idInAnyCase_success() {
        assertParseSuccess(parser, " id/e0001", filterOf(List.of("e0001"), List.of(), List.of(), List.of()));
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
        String unknownE = String.format(MESSAGE_INVALID_COMMAND_FORMAT, "Unknown parameter: e/\n" + USAGE);
        assertParseFailure(parser, " d/Sales e/john@example.com", unknownE);

        String unknownX = String.format(MESSAGE_INVALID_COMMAND_FORMAT, "Unknown parameter: x/\n" + USAGE);
        assertParseFailure(parser, " x/foo", unknownX);
    }

    @Test
    public void parse_blankValue_failure() {
        assertParseFailure(parser, " n/", noSearchWordMessage("n/"));
        assertParseFailure(parser, " d/", noSearchWordMessage("d/"));
        assertParseFailure(parser, " r/   ", noSearchWordMessage("r/"));
        assertParseFailure(parser, " d/Sales r/", noSearchWordMessage("r/"));
        assertParseFailure(parser, " d/Sales d/ r/Manager", noSearchWordMessage("d/"));
    }

    @Test
    public void parse_valueWithoutLetterOrDigit_failure() {
        assertParseFailure(parser, " d/@@@", noSearchWordMessage("d/"));
        assertParseFailure(parser, " r/() ,", noSearchWordMessage("r/"));
        assertParseFailure(parser, " n/ - ", noSearchWordMessage("n/"));
    }

    @Test
    public void parse_invalidId_failure() {
        assertParseFailure(parser, " id/", ParserUtil.MESSAGE_BLANK_EMPLOYEE_ID + "\n" + USAGE);
        assertParseFailure(parser, " id/E 1", EmployeeId.MESSAGE_CONSTRAINTS + "\n" + USAGE);
        assertParseFailure(parser, " id/E@1", EmployeeId.MESSAGE_CONSTRAINTS + "\n" + USAGE);
        assertParseFailure(parser, " id/" + "E".repeat(21), ParserUtil.MESSAGE_EMPLOYEE_ID_TOO_LONG + "\n" + USAGE);
        assertParseFailure(parser, " id/E0001 id/", ParserUtil.MESSAGE_BLANK_EMPLOYEE_ID + "\n" + USAGE);
    }
}

package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.employee.NameContainsKeywordsPredicate;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);

        // alphanumeric keywords
        FindCommand expectedAlphanumericFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice2", "3Bob")));
        assertParseSuccess(parser, "Alice2 3Bob", expectedAlphanumericFindCommand);

        // keywords containing punctuation and symbols
        FindCommand expectedSymbolsFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(
                        List.of("@@@", "/John", "John!", "s/o", "d/o", "O'Brien")));
        assertParseSuccess(parser, "@@@ /John John! s/o d/o O'Brien", expectedSymbolsFindCommand);
    }

}

package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
    public void parse_invalidKeyword_throwsParseException() {
        assertParseFailure(parser, "@@@", String.format(FindCommand.MESSAGE_INVALID_KEYWORD, "@@@"));
        assertParseFailure(parser, "John valid!", String.format(FindCommand.MESSAGE_INVALID_KEYWORD, "valid!"));
    }

    @Test
    public void parse_invalidKeywordAmongSeveral_messageNamesTheFirstInvalidKeyword() {
        assertParseFailure(parser, "hans @bo %x", String.format(FindCommand.MESSAGE_INVALID_KEYWORD, "@bo"));
        assertParseFailure(parser, "-hans bo", String.format(FindCommand.MESSAGE_INVALID_KEYWORD, "-hans"));
    }

    @Test
    public void parse_invalidKeyword_messageStillExplainsTheRules() {
        assertTrue(String.format(FindCommand.MESSAGE_INVALID_KEYWORD, "@bo")
                .endsWith(FindCommand.MESSAGE_KEYWORD_CONSTRAINTS));
    }

    @Test
    public void parse_otherKindsOfSpaces_separateKeywordsLikeASpace() {
        FindCommand expected = new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        // non-breaking space (as pasted from a web page), ideographic space and a narrow no-break space
        assertParseSuccess(parser, "Alice\u00A0Bob", expected);
        assertParseSuccess(parser, "\u00A0Alice\u3000Bob\u202F", expected);
        assertParseSuccess(parser, "Alice \u00A0 Bob", expected);
    }

    @Test
    public void parse_onlyOtherKindsOfSpaces_throwsParseException() {
        assertParseFailure(parser, "\u00A0\u00A0",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_keywordsWithCommas_keepTheCommas() {
        // the comma is allowed after the first character, and the match ignores it
        assertParseSuccess(parser, "Ah, Mary-Ann",
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Ah,", "Mary-Ann"))));
    }

    @Test
    public void parse_decomposedAccent_isAccepted() {
        assertParseSuccess(parser, "E\u0301mile",
                new FindCommand(new NameContainsKeywordsPredicate(List.of("E\u0301mile"))));
    }

    @Test
    public void parse_unexpectedCommandFormat_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "/John", expectedMessage);
        assertParseFailure(parser, "Alice /John", expectedMessage);
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

        // keywords containing punctuation permitted in names
        FindCommand expectedSymbolsFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(
                        List.of("Mary-Ann", "John.", "s/o", "d/o", "O'Brien")));
        assertParseSuccess(parser, "Mary-Ann John. s/o d/o O'Brien", expectedSymbolsFindCommand);
    }

}

package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.Name;
import seedu.address.model.employee.NameContainsKeywordsPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {

    /** Spaces, tabs, line breaks and other kinds of spaces, such as the non-breaking space of a web page. */
    private static final Pattern SEPARATORS = Pattern.compile("[\\s\\p{Z}]+");
    private static final Pattern SEPARATORS_AT_ENDS = Pattern.compile("^[\\s\\p{Z}]+|[\\s\\p{Z}]+$");

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = SEPARATORS_AT_ENDS.matcher(args).replaceAll("");
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        List<String> nameKeywords = List.of(SEPARATORS.split(trimmedArgs));
        if (nameKeywords.stream().anyMatch(keyword -> keyword.startsWith("/"))) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
        Optional<String> invalidKeyword = nameKeywords.stream()
                .filter(keyword -> !Name.isValidName(keyword)).findFirst();
        if (invalidKeyword.isPresent()) {
            throw new ParseException(String.format(FindCommand.MESSAGE_INVALID_KEYWORD, invalidKeyword.get()));
        }

        return new FindCommand(new NameContainsKeywordsPredicate(nameKeywords));
    }

}

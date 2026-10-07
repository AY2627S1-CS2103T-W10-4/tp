package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.Name;
import seedu.address.model.employee.NameContainsKeywordsPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {

    public static final String MESSAGE_INVALID_KEYWORDS =
            "Keywords should only contain alphanumeric characters and spaces.";

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        List<String> nameKeywords = List.of(trimmedArgs.split("\\s+"));
        if (nameKeywords.stream().anyMatch(keyword -> !Name.isValidName(keyword))) {
            throw new ParseException(MESSAGE_INVALID_KEYWORDS);
        }

        return new FindCommand(new NameContainsKeywordsPredicate(nameKeywords));
    }

}

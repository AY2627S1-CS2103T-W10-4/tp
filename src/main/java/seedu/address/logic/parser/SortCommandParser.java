package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.SortCommand.SortField;
import seedu.address.logic.commands.SortCommand.SortOrder;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new SortCommand object.
 */
public class SortCommandParser implements Parser<SortCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the SortCommand
     * and returns a SortCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public SortCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        String[] words = trimmedArgs.split("\\s+");
        if (trimmedArgs.isEmpty() || words.length > 2) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE));
        }

        SortField field = SortField.fromString(words[0]).orElseThrow(() -> new ParseException(
                String.format(SortCommand.MESSAGE_UNKNOWN_FIELD, words[0], SortField.names())
                        + "\n" + SortCommand.MESSAGE_USAGE));
        if (words.length == 1) {
            return new SortCommand(field);
        }

        SortOrder order = SortOrder.fromString(words[1]).orElseThrow(() -> new ParseException(
                String.format(SortCommand.MESSAGE_UNKNOWN_ORDER, words[1], SortOrder.keywords())
                        + "\n" + SortCommand.MESSAGE_USAGE));
        return new SortCommand(field, order);
    }

}

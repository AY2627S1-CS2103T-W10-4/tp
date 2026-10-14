package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ID;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses exactly one employee ID for deletion.
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    private static final Pattern FIELD_PREFIX = Pattern.compile("(?:^|\\s)([^\\s/]+/)");

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns a DeleteCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        Matcher prefixMatcher = FIELD_PREFIX.matcher(trimmedArgs);
        int idPrefixCount = 0;
        while (prefixMatcher.find()) {
            String prefix = prefixMatcher.group(1);
            if (!PREFIX_ID.toString().equals(prefix)) {
                throw withUsage(String.format(DeleteCommand.MESSAGE_UNKNOWN_PARAMETER, prefix));
            }
            idPrefixCount++;
        }

        if (idPrefixCount == 0) {
            throw withUsage(DeleteCommand.MESSAGE_MISSING_ID);
        }
        if (idPrefixCount > 1) {
            throw withUsage(Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ID));
        }
        if (!trimmedArgs.startsWith(PREFIX_ID.toString())) {
            throw withUsage(DeleteCommand.MESSAGE_UNEXPECTED_ARGUMENTS);
        }

        String value = trimmedArgs.substring(PREFIX_ID.toString().length()).trim();
        if (value.isEmpty()) {
            throw withUsage(DeleteCommand.MESSAGE_BLANK_ID);
        }

        try {
            return new DeleteCommand(ParserUtil.parseEmployeeId(value));
        } catch (ParseException pe) {
            throw new ParseException(pe.getMessage() + "\n" + DeleteCommand.MESSAGE_USAGE, pe);
        }
    }

    private static ParseException withUsage(String message) {
        return new ParseException(message + "\n" + DeleteCommand.MESSAGE_USAGE);
    }

}

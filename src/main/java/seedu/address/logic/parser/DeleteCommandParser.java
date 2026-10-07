package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ID;

import java.util.regex.Pattern;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.EmployeeId;

/**
 * Parses exactly one employee ID for deletion.
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    private static final Pattern ID_PREFIX = Pattern.compile("(?:^|[ \\t])id/");
    private static final Pattern FIELD_PREFIX = Pattern.compile("(?:^|[ \\t])[^ \\t/]+/");
    private static final Pattern EXTRA_ARGUMENTS = Pattern.compile("E[0-9]{4}[ \\t]+.+");

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns a DeleteCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = trimSeparators(args);

        if (ID_PREFIX.matcher(trimmedArgs).results().count() > 1) {
            throw withUsage(Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ID));
        }
        if (!trimmedArgs.startsWith(PREFIX_ID.toString())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        }

        String value = trimSeparators(trimmedArgs.substring(PREFIX_ID.toString().length()));
        if (FIELD_PREFIX.matcher(value).find() || EXTRA_ARGUMENTS.matcher(value).matches()) {
            throw withUsage(DeleteCommand.MESSAGE_UNEXPECTED_ARGUMENTS);
        }
        if (value.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        }
        if (!EmployeeId.isValidEmployeeId(value)) {
            throw withUsage(EmployeeId.MESSAGE_CONSTRAINTS);
        }
        return new DeleteCommand(new EmployeeId(value));
    }

    private static String trimSeparators(String value) {
        return value.replaceAll("^[ \\t]+|[ \\t]+$", "");
    }

    private static ParseException withUsage(String message) {
        return new ParseException(message + "\n" + DeleteCommand.MESSAGE_USAGE);
    }

}

package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ID;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.EmployeeId;

/** Shared parser for commands accepting exactly one id/ parameter. */
final class SingleIdCommandParser {
    private static final Pattern FIELD_PREFIX = Pattern.compile("(?:^|\\s)([^\\s/]+/)");

    private SingleIdCommandParser() {}

    static EmployeeId parse(String args, String usage) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        Matcher prefixMatcher = FIELD_PREFIX.matcher(trimmedArgs);
        int idPrefixCount = 0;
        while (prefixMatcher.find()) {
            String prefix = prefixMatcher.group(1);
            if (!PREFIX_ID.toString().equals(prefix)) {
                throw withUsage(String.format(DeleteCommand.MESSAGE_UNKNOWN_PARAMETER, prefix), usage);
            }
            idPrefixCount++;
        }

        if (idPrefixCount == 0) {
            throw withUsage(DeleteCommand.MESSAGE_MISSING_ID, usage);
        }
        if (idPrefixCount > 1) {
            throw withUsage(Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ID), usage);
        }
        if (!trimmedArgs.startsWith(PREFIX_ID.toString())) {
            throw withUsage(DeleteCommand.MESSAGE_UNEXPECTED_ARGUMENTS, usage);
        }

        String value = trimmedArgs.substring(PREFIX_ID.toString().length()).trim();
        if (value.isEmpty()) {
            throw withUsage(DeleteCommand.MESSAGE_BLANK_ID, usage);
        }

        try {
            return ParserUtil.parseEmployeeId(value);
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + usage, e);
        }
    }

    private static ParseException withUsage(String message, String usage) {
        return new ParseException(message + "\n" + usage);
    }
}

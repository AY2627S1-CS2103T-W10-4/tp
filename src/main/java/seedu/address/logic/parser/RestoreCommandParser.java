package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FILE;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;

import seedu.address.logic.commands.RestoreCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses a single file/ path to a JSON backup, allowing spaces in the path.
 */
public class RestoreCommandParser implements Parser<RestoreCommand> {
    @Override
    public RestoreCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        if (!trimmedArgs.startsWith(PREFIX_FILE.getPrefix())) {
            throw invalidFormat();
        }
        String pathText = trimmedArgs.substring(PREFIX_FILE.getPrefix().length()).trim();
        if (pathText.isBlank()) {
            throw invalidFormat();
        }
        try {
            Path path = Path.of(pathText);
            if (path.getFileName() == null
                    || !path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json")) {
                throw invalidFormat();
            }
            return new RestoreCommand(path);
        } catch (InvalidPathException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RestoreCommand.MESSAGE_USAGE), e);
        }
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RestoreCommand.MESSAGE_USAGE));
    }
}

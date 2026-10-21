package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses an archive command. */
public class ArchiveCommandParser implements Parser<ArchiveCommand> {
    @Override
    public ArchiveCommand parse(String args) throws ParseException {
        requireNonNull(args);
        return new ArchiveCommand(SingleIdCommandParser.parse(args, ArchiveCommand.MESSAGE_USAGE));
    }
}

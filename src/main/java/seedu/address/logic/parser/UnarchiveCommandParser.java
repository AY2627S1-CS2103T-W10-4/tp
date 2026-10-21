package seedu.address.logic.parser;

import seedu.address.logic.commands.UnarchiveCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses an unarchive command. */
public class UnarchiveCommandParser implements Parser<UnarchiveCommand> {
    @Override
    public UnarchiveCommand parse(String args) throws ParseException {
        return new UnarchiveCommand(SingleIdCommandParser.parse(args, UnarchiveCommand.MESSAGE_USAGE));
    }
}

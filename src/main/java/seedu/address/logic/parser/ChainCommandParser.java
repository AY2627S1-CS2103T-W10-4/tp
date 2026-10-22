package seedu.address.logic.parser;

import seedu.address.logic.commands.ChainCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.EmployeeId;

/** Parses arguments for the chain command. */
public class ChainCommandParser implements Parser<ChainCommand> {
    @Override
    public ChainCommand parse(String args) throws ParseException {
        EmployeeId[] ids = ReportingParserSupport.parse(args, ChainCommand.MESSAGE_USAGE, false);
        return new ChainCommand(ids[0]);
    }
}

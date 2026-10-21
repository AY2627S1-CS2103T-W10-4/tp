package seedu.address.logic.parser;

import seedu.address.logic.commands.ManagerCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.EmployeeId;

/** Parses arguments for the manager command. */
public class ManagerCommandParser implements Parser<ManagerCommand> {
    @Override
    public ManagerCommand parse(String args) throws ParseException {
        EmployeeId[] ids = ReportingParserSupport.parse(args, ManagerCommand.MESSAGE_USAGE, true);
        return new ManagerCommand(ids[0], ids[1]);
    }
}

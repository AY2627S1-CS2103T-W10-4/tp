package seedu.address.logic.parser;

import seedu.address.logic.commands.UnassignManagerCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.EmployeeId;

/** Parses arguments for the unassign-manager command. */
public class UnassignManagerCommandParser implements Parser<UnassignManagerCommand> {
    @Override
    public UnassignManagerCommand parse(String args) throws ParseException {
        EmployeeId[] ids = ReportingParserSupport.parse(args, UnassignManagerCommand.MESSAGE_USAGE, false);
        return new UnassignManagerCommand(ids[0]);
    }
}

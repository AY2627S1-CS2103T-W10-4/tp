package seedu.address.logic.parser;

import seedu.address.logic.commands.OrgCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.EmployeeId;

/** Parses arguments for the org command. */
public class OrgCommandParser implements Parser<OrgCommand> {
    @Override
    public OrgCommand parse(String args) throws ParseException {
        EmployeeId[] ids = ReportingParserSupport.parse(args, OrgCommand.MESSAGE_USAGE, false);
        return new OrgCommand(ids[0]);
    }
}

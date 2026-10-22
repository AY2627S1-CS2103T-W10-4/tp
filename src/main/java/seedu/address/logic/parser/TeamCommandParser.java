package seedu.address.logic.parser;

import seedu.address.logic.commands.TeamCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.EmployeeId;

/** Parses arguments for the team command. */
public class TeamCommandParser implements Parser<TeamCommand> {
    @Override
    public TeamCommand parse(String args) throws ParseException {
        EmployeeId[] ids = ReportingParserSupport.parse(args, TeamCommand.MESSAGE_USAGE, false);
        return new TeamCommand(ids[0]);
    }
}

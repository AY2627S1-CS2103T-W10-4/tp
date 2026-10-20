package seedu.address.logic.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.ReportingCommand;
import seedu.address.logic.commands.ReportingCommand.Operation;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.EmployeeId;

/** Parses reporting commands with exactly one ID and, for assignment, one manager ID. */
public class ReportingCommandParser implements Parser<ReportingCommand> {
    private static final Pattern ARGUMENT = Pattern.compile("(id|m)/([^\\s/]+)");
    private final Operation operation;

    public ReportingCommandParser(Operation operation) {
        this.operation = operation;
    }

    @Override
    public ReportingCommand parse(String args) throws ParseException {
        EmployeeId employeeId = null;
        EmployeeId managerId = null;
        for (String argument : args.trim().split("\\s+")) {
            Matcher matcher = ARGUMENT.matcher(argument);
            if (!matcher.matches()) {
                throw invalidFormat();
            }
            EmployeeId value = ParserUtil.parseEmployeeId(matcher.group(2));
            if (matcher.group(1).equals("id") && employeeId == null) {
                employeeId = value;
            } else if (matcher.group(1).equals("m") && operation == Operation.MANAGER && managerId == null) {
                managerId = value;
            } else {
                throw invalidFormat();
            }
        }
        if (employeeId == null || (operation == Operation.MANAGER && managerId == null)) {
            throw invalidFormat();
        }
        return new ReportingCommand(operation, employeeId, managerId);
    }

    private ParseException invalidFormat() {
        return new ParseException(
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, ReportingCommand.MESSAGE_USAGE));
    }
}

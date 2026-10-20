package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ReportingCommand;
import seedu.address.logic.commands.ReportingCommand.Operation;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.EmployeeId;

public class ReportingCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_validCommands_success() throws Exception {
        assertEquals(new ReportingCommand(Operation.MANAGER, new EmployeeId("E1"), new EmployeeId("E2")),
                parser.parseCommand("manager m/E2 id/E1"));
        assertEquals(new ReportingCommand(Operation.UNASSIGN, new EmployeeId("E1"), null),
                parser.parseCommand("unassign-manager id/E1"));
        assertEquals(new ReportingCommand(Operation.TEAM, new EmployeeId("E1"), null),
                parser.parseCommand("team id/E1"));
        assertEquals(new ReportingCommand(Operation.ORG, new EmployeeId("E1"), null),
                parser.parseCommand("org id/E1"));
        assertEquals(new ReportingCommand(Operation.CHAIN, new EmployeeId("E1"), null),
                parser.parseCommand("chain id/E1"));
    }

    @Test
    public void parse_invalidAssignment_throwsParseException() {
        for (String args : List.of("", "id/E1", "m/E2", "id/ m/E2", "id/E1 m/", "id/E1 m/E2 m/E3",
                "id/E1 id/E2 m/E3", "E1 m/E2", "id/E1 m/E2 extra", "id/E1 m/E2 n/Name", "id/E! m/E2")) {
            assertThrows(ParseException.class, () -> parser.parseCommand("manager " + args), args);
        }
    }

    @Test
    public void parse_invalidSingleIdCommands_throwsParseException() {
        for (String command : List.of("team", "org", "chain", "unassign-manager")) {
            for (String args : List.of("", "id/", "id/E1 id/E2", "id/E1 m/E2", "id/E1 extra", "E1", "id/E!")) {
                assertThrows(ParseException.class, () -> parser.parseCommand(command + " " + args), command + args);
            }
        }
    }
}

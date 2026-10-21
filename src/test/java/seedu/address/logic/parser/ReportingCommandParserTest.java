package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ChainCommand;
import seedu.address.logic.commands.ManagerCommand;
import seedu.address.logic.commands.OrgCommand;
import seedu.address.logic.commands.TeamCommand;
import seedu.address.logic.commands.UnassignManagerCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.EmployeeId;

public class ReportingCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_validCommands_success() throws Exception {
        assertEquals(new ManagerCommand(new EmployeeId("E1"), new EmployeeId("E2")),
                parser.parseCommand("manager m/E2 id/E1"));
        assertEquals(new UnassignManagerCommand(new EmployeeId("E1")),
                parser.parseCommand("unassign-manager id/E1"));
        assertEquals(new TeamCommand(new EmployeeId("E1")),
                parser.parseCommand("team id/E1"));
        assertEquals(new OrgCommand(new EmployeeId("E1")),
                parser.parseCommand("org id/E1"));
        assertEquals(new ChainCommand(new EmployeeId("E1")),
                parser.parseCommand("chain id/E1"));
    }

    @Test
    public void parse_errorsIncludeOnlyRelevantUsage() {
        for (String input : List.of("team", "team id/E1 x/foo", "team id/@@@", "team id/E1 id/E2")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand(input));
            assertTrue(error.getMessage().contains(TeamCommand.MESSAGE_USAGE));
            assertFalse(error.getMessage().contains(ManagerCommand.MESSAGE_USAGE));
        }
        ParseException error = assertThrows(ParseException.class, () ->
                parser.parseCommand("manager id/E1 m/@@@"));
        assertTrue(error.getMessage().contains(ManagerCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_whitespaceAccepted() throws Exception {
        assertEquals(new ManagerCommand(new EmployeeId("E1"), new EmployeeId("E2")),
                parser.parseCommand("manager \t m/E2   id/E1  "));
    }

    @Test
    public void parse_invalidManagerOnAddOrEdit_throwsParseException() {
        String add = "add id/NEW n/New Person p/91234567 e/new@example.com d/Engineering r/Engineer ";
        for (String suffix : List.of("m/", "m/@@@", "m/BOSS m/LEAD")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(add + suffix));
        }
        for (String suffix : List.of("m/@@@", "m/BOSS m/LEAD", "m/ m/")) {
            assertThrows(ParseException.class, () -> parser.parseCommand("edit id/NEW " + suffix));
        }
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

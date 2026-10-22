package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.EmployeeDataAssertions.assertStoredEmployees;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.BENSON;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.commands.BackupCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ImportCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.UnarchiveCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.NameContainsKeywordsPredicate;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.EditEmployeeDescriptorBuilder;
import seedu.address.testutil.EmployeeBuilder;

public class HistoryCommandTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_reportingCommands_undoAndRedoRestoreAssignmentsAndSavedData() throws Exception {
        Employee report = BENSON.withArchived(true);
        Employee assigned = report.withManager(ALICE.getId());
        ModelManager model = new ModelManager(new AddressBookBuilder()
                .withEmployee(ALICE).withEmployee(report).build(), new UserPrefs());
        JsonAddressBookStorage employeeStorage = new JsonAddressBookStorage(temporaryFolder.resolve("employees.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(employeeStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        List<String> commands = List.of("manager id/" + report.getId() + " m/" + ALICE.getId(),
                "undo", "redo", "unassign-manager id/" + report.getId(), "undo", "redo");
        List<Employee> expectedReports = List.of(assigned, report, assigned, report, assigned, report);
        for (int i = 0; i < commands.size(); i++) {
            logic.execute(commands.get(i));
            List<Employee> expected = List.of(ALICE, expectedReports.get(i));
            assertStoredEmployees(expected, model.getAddressBook().getEmployeeList());
            assertStoredEmployees(expected, employeeStorage.readAddressBook().orElseThrow().getEmployeeList());
        }
        logic.execute("undo");
        logic.execute("undo");
        assertStoredEmployees(List.of(ALICE, report), model.getAddressBook().getEmployeeList());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_editThenManager_undoRevertsOnlyTheLatestChange() throws Exception {
        ModelManager model = new ModelManager(new AddressBookBuilder()
                .withEmployee(ALICE).withEmployee(BENSON).build(), new UserPrefs());
        LogicManager logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("employees.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        Employee edited = new EmployeeBuilder(ALICE).withRole("Updated role").build();
        logic.execute("edit id/" + ALICE.getId() + " r/Updated role");
        logic.execute("manager id/" + BENSON.getId() + " m/" + ALICE.getId());
        logic.execute("undo");
        assertStoredEmployees(List.of(edited, BENSON), model.getAddressBook().getEmployeeList());
        logic.execute("undo");
        assertStoredEmployees(List.of(ALICE, BENSON), model.getAddressBook().getEmployeeList());
        logic.execute("redo");
        logic.execute("redo");
        assertStoredEmployees(List.of(edited, BENSON.withManager(ALICE.getId())),
                model.getAddressBook().getEmployeeList());
    }

    @Test
    public void execute_allModifyingCommands_commitOnceAndTraverseEveryState() throws Exception {
        Path csv = temporaryFolder.resolve("staff.csv");
        Files.writeString(csv, "id,name,phone,email,department,role,archived\n"
                + "e0123,Chris Tan,91234567,chris@example.com,Engineering,Developer,false\n"
                + "E0124,Dana Tan,92345678,dana@example.com,Sales,Manager,true\n");
        CountingModel model = new CountingModel();
        List<Command> commands = List.of(new AddCommand(ALICE), new AddCommand(BENSON),
                new EditCommand(ALICE.getId(), new EditEmployeeDescriptorBuilder().withName("Changed Name")
                        .withPhone("87654321").withEmail("changed@example.com").withDepartment("Sales")
                        .withRole("Manager").build()),
                new ArchiveCommand(ALICE.getId()), new ClearCommand(), new UnarchiveCommand(ALICE.getId()),
                new DeleteCommand(ALICE.getId()), new ImportCommand(temporaryFolder, Path.of("staff.csv")));
        List<List<Employee>> states = new ArrayList<>();
        states.add(List.of());
        for (Command command : commands) {
            int previousCommits = model.commits;
            assertTrue(command.isDataChanging());
            assertEquals(Command.HistoryAction.COMMIT, command.getHistoryAction());
            execute(command, model);
            assertEquals(previousCommits + 1, model.commits);
            states.add(List.copyOf(model.getAddressBook().getEmployeeList()));
        }
        assertEquals(2, states.getLast().size());
        assertStoredEmployees(List.of(states.get(4).getFirst()), states.get(5));
        assertTrue(states.get(5).getFirst().isArchived());
        assertFalse(states.get(6).getFirst().isArchived());
        Files.writeString(csv, "changed file");
        traverse(model, states);
        Files.delete(csv);
        traverse(model, states);
        assertEquals(commands.size(), model.commits);
    }

    @Test
    public void execute_deleteArchivedThenReuseCaseVariant_preservesRawValuesAndOrder() throws Exception {
        ModelManager model = new ModelManager();
        execute(new AddCommand(ALICE.withArchived(true)), model);
        execute(new AddCommand(BENSON), model);
        execute(new DeleteCommand(ALICE.getId()), model);
        Employee replacement = new EmployeeBuilder(ALICE).withId(ALICE.getId().value.toLowerCase())
                .withName("Replacement Name").build();
        execute(new AddCommand(replacement), model);
        assertStoredEmployees(List.of(BENSON, replacement), model.getAddressBook().getEmployeeList());
        execute(new UndoCommand(), model);
        assertStoredEmployees(List.of(BENSON), model.getAddressBook().getEmployeeList());
        execute(new UndoCommand(), model);
        assertStoredEmployees(List.of(ALICE.withArchived(true), BENSON), model.getAddressBook().getEmployeeList());
        execute(new RedoCommand(), model);
        execute(new RedoCommand(), model);
        assertStoredEmployees(List.of(BENSON, replacement), model.getAddressBook().getEmployeeList());
    }

    @Test
    public void isDataChanging_readOnlyCommandsAndHistoryCommands_classified() {
        List<Command> readOnlyCommands = List.of(new ListCommand(), new ListCommand(true),
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice"))), new HelpCommand(),
                new ExitCommand(), new ExportCommand(temporaryFolder, Path.of("export.csv")),
                new BackupCommand(temporaryFolder, Clock.systemUTC()));
        for (Command command : readOnlyCommands) {
            assertFalse(command.isDataChanging(), command.getClass().getSimpleName());
            assertEquals(Command.HistoryAction.NONE, command.getHistoryAction());
        }
        assertTrue(new UndoCommand().isDataChanging());
        assertTrue(new RedoCommand().isDataChanging());
        assertEquals(Command.HistoryAction.NAVIGATE, new UndoCommand().getHistoryAction());
        assertEquals(Command.HistoryAction.NAVIGATE, new RedoCommand().getHistoryAction());
    }

    private void traverse(CountingModel model, List<List<Employee>> states) throws Exception {
        for (int i = states.size() - 2; i >= 0; i--) {
            assertEquals("Undo successful.", execute(new UndoCommand(), model).getFeedbackToUser());
            assertStoredEmployees(states.get(i), model.getAddressBook().getEmployeeList());
        }
        assertEquals("No more commands to undo.",
                assertThrows(CommandException.class, () -> execute(new UndoCommand(), model)).getMessage());
        for (int i = 1; i < states.size(); i++) {
            assertEquals("Redo successful.", execute(new RedoCommand(), model).getFeedbackToUser());
            assertStoredEmployees(states.get(i), model.getAddressBook().getEmployeeList());
        }
        assertEquals("No more commands to redo.",
                assertThrows(CommandException.class, () -> execute(new RedoCommand(), model)).getMessage());
    }

    private CommandResult execute(Command command, Model model) throws CommandException, ParseException {
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("employees.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        LogicManager logic = new LogicManager(model, storage, new AddressBookParser() {
            @Override
            public Command parseCommand(String text) {
                return command;
            }
        });
        return logic.execute("injected command");
    }

    private static class CountingModel extends ModelManager {
        private int commits;

        @Override
        public void commitAddressBook() {
            commits++;
            super.commitAddressBook();
        }
    }
}

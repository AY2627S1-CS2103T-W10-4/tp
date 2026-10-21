package seedu.address.logic.commands;

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

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.NameContainsKeywordsPredicate;
import seedu.address.testutil.EditEmployeeDescriptorBuilder;
import seedu.address.testutil.EmployeeBuilder;

public class HistoryCommandTest {
    @TempDir
    public Path temporaryFolder;

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
            command.execute(model);
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
        new AddCommand(ALICE.withArchived(true)).execute(model);
        new AddCommand(BENSON).execute(model);
        new DeleteCommand(ALICE.getId()).execute(model);
        Employee replacement = new EmployeeBuilder(ALICE).withId(ALICE.getId().value.toLowerCase())
                .withName("Replacement Name").build();
        new AddCommand(replacement).execute(model);
        assertStoredEmployees(List.of(BENSON, replacement), model.getAddressBook().getEmployeeList());
        new UndoCommand().execute(model);
        assertStoredEmployees(List.of(BENSON), model.getAddressBook().getEmployeeList());
        new UndoCommand().execute(model);
        assertStoredEmployees(List.of(ALICE.withArchived(true), BENSON), model.getAddressBook().getEmployeeList());
        new RedoCommand().execute(model);
        new RedoCommand().execute(model);
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
        }
        assertTrue(new UndoCommand().isDataChanging());
        assertTrue(new RedoCommand().isDataChanging());
    }

    private void traverse(CountingModel model, List<List<Employee>> states) throws Exception {
        for (int i = states.size() - 2; i >= 0; i--) {
            assertEquals("Undo successful.", new UndoCommand().execute(model).getFeedbackToUser());
            assertStoredEmployees(states.get(i), model.getAddressBook().getEmployeeList());
        }
        assertEquals("No more commands to undo.",
                assertThrows(CommandException.class, () -> new UndoCommand().execute(model)).getMessage());
        for (int i = 1; i < states.size(); i++) {
            assertEquals("Redo successful.", new RedoCommand().execute(model).getFeedbackToUser());
            assertStoredEmployees(states.get(i), model.getAddressBook().getEmployeeList());
        }
        assertEquals("No more commands to redo.",
                assertThrows(CommandException.class, () -> new RedoCommand().execute(model)).getMessage());
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

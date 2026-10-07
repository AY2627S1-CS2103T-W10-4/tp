package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.BENSON;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.EmployeeBuilder;
import seedu.address.testutil.EmployeeUtil;

public class DeleteCommandIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_hiddenTarget_savesBeforePublishingAndPreservesFilter() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.updateFilteredEmployeeList(employee -> employee.equals(BENSON));
        ObservableList<Employee> displayed = model.getFilteredEmployeeList();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("employees.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook candidate) throws IOException {
                assertTrue(model.hasEmployee(ALICE));
                assertEquals(List.of(BENSON), displayed);
                assertFalse(candidate.getEmployeeList().contains(ALICE));
                super.saveAddressBook(candidate);
                assertTrue(model.hasEmployee(ALICE));
            }
        };
        Logic logic = createLogic(model, storage);

        CommandResult result = logic.execute(" \tdelete\tid/ E0001\t");

        assertEquals("Deleted employee: ID: E0001; Name: Alice Pauline; Phone: 94351253; Email: alice@example.com; "
                + "Department: Engineering; Role: Software Engineer", result.getFeedbackToUser());
        AddressBook expected = getTypicalAddressBook();
        expected.removeEmployee(ALICE);
        assertEquals(expected, model.getAddressBook());
        assertEquals(expected, storage.readAddressBook().orElseThrow());
        assertSame(displayed, model.getFilteredEmployeeList());
        assertEquals(List.of(BENSON), displayed);
        model.updateFilteredEmployeeList(Model.PREDICATE_SHOW_ALL_EMPLOYEES);
        assertEquals(expected.getEmployeeList(), displayed);
    }

    @Test
    public void execute_visibleTarget_preservesFilterAfterPublishing() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.updateFilteredEmployeeList(employee -> employee.equals(ALICE) || employee.equals(BENSON));
        Logic logic = createLogic(model, new JsonAddressBookStorage(temporaryFolder.resolve("employees.json")));
        logic.execute("delete id/E0001");
        assertEquals(List.of(BENSON), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_lastEmployeeAndReuse_persistsEmptyRosterAndNewRecord() throws Exception {
        Model model = new ModelManager();
        model.addEmployee(ALICE);
        model.updateFilteredEmployeeList(employee -> false);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("employees.json"));
        Logic logic = createLogic(model, storage);

        logic.execute("delete id/E0001");
        assertTrue(model.getFilteredEmployeeList().isEmpty());
        assertTrue(storage.readAddressBook().orElseThrow().getEmployeeList().isEmpty());
        assertEquals("{\"employees\":[]}", Files.readString(storage.getAddressBookFilePath()).replaceAll("\\s", ""));
        assertThrows(CommandException.class, "No employee with ID E0001 was found.", () ->
                logic.execute("delete id/E0001"));

        Employee replacement = new EmployeeBuilder(ALICE).withName("Replacement Employee").build();
        logic.execute(EmployeeUtil.getAddCommand(replacement));
        assertEquals(List.of(replacement), storage.readAddressBook().orElseThrow().getEmployeeList());
    }

    @Test
    public void execute_rejectedCommands_doNotSaveOrChangeRoster() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.updateFilteredEmployeeList(employee -> employee.equals(BENSON));
        Path file = temporaryFolder.resolve("employees.json");
        new JsonAddressBookStorage(file).saveAddressBook(model.getAddressBook());
        byte[] originalBytes = Files.readAllBytes(file);
        Logic logic = createLogic(model, new JsonAddressBookStorage(file) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook candidate) {
                throw new AssertionError("Rejected commands must not save");
            }
        });
        AddressBook original = new AddressBook(model.getAddressBook());
        for (String input : new String[] {"delete 1", "delete", "delete id/", "delete id/e0001",
            "delete id/E0001 n/John", "delete id/E0001 id/E0001"}) {
            assertThrows(ParseException.class, () -> logic.execute(input));
        }
        assertThrows(CommandException.class, "No employee with ID E9999 was found.", () ->
                logic.execute("delete id/E9999"));
        assertEquals(original, model.getAddressBook());
        assertEquals(List.of(BENSON), model.getFilteredEmployeeList());
        assertArrayEquals(originalBytes, Files.readAllBytes(file));
    }

    @Test
    public void execute_addWithLegacyIds_rejectsWithoutSaving() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Path file = temporaryFolder.resolve("employees.json");
        Logic logic = createLogic(model, new JsonAddressBookStorage(file));
        for (String id : new String[] {"e0001", "EMP-0042", "1"}) {
            String add = EmployeeUtil.getAddCommand(ALICE).replace("id/E0001", "id/" + id);
            assertThrows(ParseException.class, EmployeeId.MESSAGE_CONSTRAINTS, () -> logic.execute(add));
        }
        assertEquals(getTypicalAddressBook(), model.getAddressBook());
        assertFalse(Files.exists(file));
    }

    @Test
    public void execute_edit_retainsIdAndRejectsIdEditing() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Logic logic = createLogic(model, new JsonAddressBookStorage(temporaryFolder.resolve("employees.json")));
        logic.execute("edit 1 n/Updated Name");
        assertEquals(ALICE.getId(), model.getAddressBook().getEmployeeList().getFirst().getId());
        assertThrows(ParseException.class, () -> logic.execute("edit 1 id/E9999"));
        assertEquals(ALICE.getId(), model.getAddressBook().getEmployeeList().getFirst().getId());
    }

    private Logic createLogic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}

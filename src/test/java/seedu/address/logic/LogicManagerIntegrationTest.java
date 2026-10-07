package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.BENSON;
import static seedu.address.testutil.TypicalEmployees.HOON;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.EmployeeBuilder;
import seedu.address.testutil.EmployeeUtil;

public class LogicManagerIntegrationTest {
    private static final Predicate<Employee> BENSON_FILTER = employee -> employee.equals(BENSON);

    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_add_savesBeforePublishingAndShowsAllEmployees() throws Exception {
        AddressBook expected = getTypicalAddressBook();
        expected.addEmployee(HOON);
        assertSavedCommand(EmployeeUtil.getAddCommand(HOON), expected, Model.PREDICATE_SHOW_ALL_EMPLOYEES);
    }

    @Test
    public void execute_edit_usesDisplayedIndexAndShowsAllAfterSaving() throws Exception {
        AddressBook expected = getTypicalAddressBook();
        expected.setEmployee(BENSON, new EmployeeBuilder(BENSON).withName("Updated Name").build());
        assertSavedCommand("edit 1 n/Updated Name", expected, Model.PREDICATE_SHOW_ALL_EMPLOYEES);
    }

    @Test
    public void execute_unchangedEdit_stillSavesAndShowsAllEmployees() throws Exception {
        assertSavedCommand("edit 1 n/Benson Meier", getTypicalAddressBook(), Model.PREDICATE_SHOW_ALL_EMPLOYEES);
    }

    @Test
    public void execute_clear_savesBeforePublishingAndPreservesFilter() throws Exception {
        assertSavedCommand("clear", new AddressBook(), BENSON_FILTER);
    }

    @Test
    public void execute_clearEmptyRoster_savesEmptyFile() throws Exception {
        Model model = new ModelManager();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("empty.json"));
        Logic logic = createLogic(model, storage);

        assertEquals("Address book has been cleared!", logic.execute("clear").getFeedbackToUser());

        assertTrue(Files.exists(storage.getAddressBookFilePath()));
        assertEquals(new AddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_readOnlyCommands_neverSaveAndKeepNormalResults() throws Exception {
        for (boolean destinationExists : new boolean[] {true, false}) {
            Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
            Path file = temporaryFolder.resolve("read-only-" + destinationExists + ".json");
            if (destinationExists) {
                new JsonAddressBookStorage(file).saveAddressBook(model.getAddressBook());
            }
            byte[] originalBytes = destinationExists ? Files.readAllBytes(file) : null;
            Logic logic = createLogic(model, storageThatMustNotSave(file));

            assertEquals(new CommandResult("1 employee(s) listed!"), logic.execute("find Benson"));
            assertEquals(List.of(BENSON), model.getFilteredEmployeeList());
            assertEquals(new CommandResult("Opened help window.", true, false), logic.execute("help"));
            assertEquals(List.of(BENSON), model.getFilteredEmployeeList());
            assertEquals(new CommandResult("Listed all employees."), logic.execute("list"));
            assertEquals(getTypicalAddressBook().getEmployeeList(), model.getFilteredEmployeeList());
            assertEquals(new CommandResult("Exiting Address Book as requested ...", false, true),
                    logic.execute("exit"));

            assertEquals(getTypicalAddressBook(), model.getAddressBook());
            if (destinationExists) {
                assertArrayEquals(originalBytes, Files.readAllBytes(file));
            } else {
                assertFalse(Files.exists(file));
            }
        }
    }

    @Test
    public void execute_readOnlyCommands_preserveInvalidFile() throws Exception {
        Path file = temporaryFolder.resolve("invalid.json");
        String original = "invalid employee data";
        Files.writeString(file, original);
        Logic logic = createLogic(new ModelManager(), storageThatMustNotSave(file));

        for (String input : new String[] {"list", "find Nobody", "help", "exit"}) {
            logic.execute(input);
            assertEquals(original, Files.readString(file));
        }
    }

    @Test
    public void execute_rejectedRecordChanges_doNotSaveOrChangeFilter() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.updateFilteredEmployeeList(BENSON_FILTER);
        Logic logic = createLogic(model, storageThatMustNotSave(temporaryFolder.resolve("rejected.json")));

        assertThrows(CommandException.class, () -> logic.execute(EmployeeUtil.getAddCommand(ALICE)));
        assertThrows(CommandException.class, () -> logic.execute("edit 2 n/Invalid Index"));
        assertThrows(ParseException.class, () -> logic.execute("edit 1"));

        assertEquals(getTypicalAddressBook(), model.getAddressBook());
        assertSame(BENSON_FILTER, model.getEmployeeFilter());
        assertEquals(List.of(BENSON), model.getFilteredEmployeeList());
    }

    private void assertSavedCommand(String input, AddressBook expected, Predicate<Employee> expectedFilter)
            throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.updateFilteredEmployeeList(BENSON_FILTER);
        var displayed = model.getFilteredEmployeeList();
        int[] saveCount = {0};
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("employees.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook candidate) throws IOException {
                saveCount[0]++;
                assertEquals(expected, candidate);
                assertEquals(getTypicalAddressBook(), model.getAddressBook());
                assertSame(BENSON_FILTER, model.getEmployeeFilter());
                assertEquals(List.of(BENSON), displayed);
                super.saveAddressBook(candidate);
                assertEquals(getTypicalAddressBook(), model.getAddressBook());
                assertEquals(List.of(BENSON), displayed);
            }
        };

        createLogic(model, storage).execute(input);

        assertEquals(1, saveCount[0]);
        assertEquals(expected, model.getAddressBook());
        assertEquals(expected, storage.readAddressBook().orElseThrow());
        assertSame(displayed, model.getFilteredEmployeeList());
        assertSame(expectedFilter, model.getEmployeeFilter());
        assertEquals(expected.getEmployeeList().stream().filter(expectedFilter).toList(), displayed);
    }

    private JsonAddressBookStorage storageThatMustNotSave(Path file) {
        return new JsonAddressBookStorage(file) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook candidate) {
                throw new AssertionError("This command must not save employee records");
            }
        };
    }

    private Logic createLogic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}

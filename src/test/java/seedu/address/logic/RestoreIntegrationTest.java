package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.EmployeeDataAssertions.assertStoredEmployees;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.BENSON;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.transformation.SortedList;
import seedu.address.logic.commands.BackupCommand;
import seedu.address.logic.commands.RestoreCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.EmployeeBuilder;

public class RestoreIntegrationTest {
    @TempDir
    public Path folder;

    private ModelManager model;
    private JsonAddressBookStorage storage;
    private LogicManager logic;
    private Path backup;
    private AddressBook snapshot;

    @BeforeEach
    public void setUp() throws Exception {
        AddressBook original = new AddressBook();
        original.addEmployee(ALICE);
        model = new ModelManager(original, new UserPrefs());
        storage = new JsonAddressBookStorage(folder.resolve("employees.json"));
        storage.saveAddressBook(original);
        logic = createLogic(storage);
        snapshot = new AddressBook();
        snapshot.setEmployees(List.of(BENSON.withArchived(true).withManager(ALICE.getId()),
                new EmployeeBuilder(ALICE).withRole("Restored Manager").build()));
        backup = folder.resolve("backup with spaces.json");
        new JsonAddressBookStorage(backup).saveAddressBook(snapshot);
    }

    @Test
    public void execute_validBackup_replacesRosterAndSupportsUndoRedoWithoutSource() throws Exception {
        logic.execute("edit id/" + ALICE.getId() + " r/Before restore");
        logic.execute("undo");
        assertTrue(model.canRedoAddressBook());

        assertEquals(String.format(RestoreCommand.MESSAGE_SUCCESS, 2, backup),
                logic.execute("restore file/" + backup).getFeedbackToUser());
        assertStoredEmployees(snapshot.getEmployeeList(), model.getAddressBook().getEmployeeList());
        assertStoredEmployees(List.of(snapshot.getEmployeeList().get(1)), model.getFilteredEmployeeList());
        assertStoredEmployees(snapshot.getEmployeeList(), storage.readAddressBook().orElseThrow().getEmployeeList());
        assertFalse(model.canRedoAddressBook());

        Files.delete(backup);
        logic.execute("undo");
        assertStoredEmployees(List.of(ALICE), model.getAddressBook().getEmployeeList());
        logic.execute("redo");
        assertStoredEmployees(snapshot.getEmployeeList(), model.getAddressBook().getEmployeeList());
        assertStoredEmployees(snapshot.getEmployeeList(), storage.readAddressBook().orElseThrow().getEmployeeList());
    }

    @Test
    public void execute_invalidBackup_preservesRecordsFileViewAndRedo() throws Exception {
        String valid = Files.readString(backup);
        List<String> invalid = List.of("", "{", "null", "{}", "{\"employees\":null}",
                "{\"employees\":[null]}", "{\"employees\":[{}]}", valid + "{}", valid + "trailing",
                valid.replace(ALICE.getPhone().value, "invalid phone"),
                valid.replace("Restored Manager", ""),
                valid.replace("\"managerId\" : \"" + ALICE.getId() + "\"", "\"managerId\" : \"MISSING\""),
                Files.readString(Path.of(
                        "src/test/data/JsonSerializableAddressBookTest/duplicateEmployeeAddressBook.json")));
        logic.execute("edit id/" + ALICE.getId() + " r/Redo me");
        logic.execute("undo");
        model.showArchivedEmployees();
        byte[] saved = Files.readAllBytes(storage.getAddressBookFilePath());
        for (String json : invalid) {
            Files.writeString(backup, json);
            assertThrows(CommandException.class, () -> logic.execute("restore file/" + backup), json);
            assertStoredEmployees(List.of(ALICE), model.getAddressBook().getEmployeeList());
            assertTrue(model.getFilteredEmployeeList().isEmpty());
            assertTrue(model.canRedoAddressBook());
            assertArrayEquals(saved, Files.readAllBytes(storage.getAddressBookFilePath()));
        }
        logic.execute("redo");
        assertEquals("Redo me", model.getAddressBook().getEmployeeList().getFirst().getRole().value);
    }

    @Test
    public void execute_reportingCycle_rejectsBeforeChangingModel() throws Exception {
        String json = Files.readString(backup).replace("\"managerId\" : null",
                "\"managerId\" : \"" + BENSON.getId() + "\"");
        Files.writeString(backup, json);
        assertThrows(CommandException.class, () -> logic.execute("restore file/" + backup));
        assertStoredEmployees(List.of(ALICE), model.getAddressBook().getEmployeeList());
    }

    @Test
    public void execute_missingFileAndDirectory_preservesRoster() throws Exception {
        Path missing = folder.resolve("missing.json");
        assertEquals(String.format(RestoreCommand.MESSAGE_FILE_NOT_FOUND, missing),
                assertThrows(CommandException.class, () -> logic.execute("restore file/" + missing)).getMessage());
        Path directory = Files.createDirectory(folder.resolve("directory.json"));
        assertThrows(CommandException.class, () -> logic.execute("restore file/" + directory));
        assertStoredEmployees(List.of(ALICE), model.getAddressBook().getEmployeeList());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_emptyBackupAndIdenticalRestore_historyMatchesActualChanges() throws Exception {
        new JsonAddressBookStorage(backup).saveAddressBook(model.getAddressBook());
        logic.execute("restore file/" + backup);
        assertFalse(model.canUndoAddressBook());
        new JsonAddressBookStorage(backup).saveAddressBook(new AddressBook());
        logic.execute("restore file/" + backup);
        assertTrue(model.getAddressBook().getEmployeeList().isEmpty());
        logic.execute("undo");
        assertStoredEmployees(List.of(ALICE), model.getAddressBook().getEmployeeList());
        new JsonAddressBookStorage(backup).saveAddressBook(model.getAddressBook());
        logic.execute("restore file/" + backup);
        assertTrue(model.canRedoAddressBook());
        logic.execute("redo");
        assertTrue(model.getAddressBook().getEmployeeList().isEmpty());
    }

    @Test
    public void execute_backupRoundTripAndLegacyBackup_preservesSupportedData() throws Exception {
        model.setAddressBook(snapshot);
        Clock clock = Clock.fixed(Instant.parse("2026-10-20T15:30:05Z"), ZoneOffset.UTC);
        new BackupCommand(folder, clock).execute(model);
        logic.execute("clear");
        logic.execute("restore file/" + folder.resolve("huntr-20261020-153005.json"));
        assertStoredEmployees(snapshot.getEmployeeList(), model.getAddressBook().getEmployeeList());

        String legacy = Files.readString(Path.of(
                "src/test/data/JsonSerializableAddressBookTest/typicalEmployeesAddressBook.json"));
        Files.writeString(backup, legacy);
        logic.execute("restore file/" + backup);
        assertTrue(model.getAddressBook().getEmployeeList().stream()
                .allMatch(employee -> !employee.isArchived() && employee.getManagerId().isEmpty()));
    }

    @Test
    public void execute_saveFailure_rollsBackRosterViewAndHistory() throws Exception {
        logic.execute("edit id/" + ALICE.getId() + " r/Redo me");
        logic.execute("undo");
        model.showArchivedEmployees();
        Comparator<Employee> comparator = Comparator.comparing(employee -> employee.getId().value);
        model.updateEmployeeSortOrder(comparator);
        byte[] saved = Files.readAllBytes(storage.getAddressBookFilePath());
        JsonAddressBookStorage failing = new JsonAddressBookStorage(storage.getAddressBookFilePath()) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("save failed");
            }
        };
        LogicManager failingLogic = createLogic(failing);
        assertThrows(CommandException.class, () -> failingLogic.execute("restore file/" + backup));
        assertStoredEmployees(List.of(ALICE), model.getAddressBook().getEmployeeList());
        assertTrue(model.getFilteredEmployeeList().isEmpty());
        assertSame(comparator, ((SortedList<Employee>) model.getFilteredEmployeeList()).getComparator());
        assertArrayEquals(saved, Files.readAllBytes(storage.getAddressBookFilePath()));
        assertFalse(model.canUndoAddressBook());
        logic.execute("redo");
        assertEquals("Redo me", model.getAddressBook().getEmployeeList().getFirst().getRole().value);
    }

    private LogicManager createLogic(JsonAddressBookStorage employeeStorage) {
        return new LogicManager(model, new StorageManager(employeeStorage,
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
    }
}

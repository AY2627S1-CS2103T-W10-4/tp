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
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.transformation.FilteredList;
import seedu.address.logic.commands.BackupCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.commands.ImportCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
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
import seedu.address.testutil.AddressBookBuilder;

public class AtomicSaveLogicTest {
    @TempDir
    public Path folder;

    @Test
    public void execute_automaticSaveFailures_keepExactMessagesAndCausesWithMemoryRollback() {
        List<IOException> failures = List.of(new IOException("disk full"),
                new IOException("Atomic replacement is not supported for this data file.",
                        new AtomicMoveNotSupportedException("temporary", "target", "unsupported")),
                new IOException("Cannot preserve the required file access protections."),
                new AccessDeniedException("temporary", "configured target", "additional diagnostic text"));
        for (IOException failure : failures) {
            Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
            LogicManager logic = new LogicManager(model, storageThatFails(failure));
            CommandException thrown = assertThrows(CommandException.class, () -> logic.execute("clear"));
            String expected = failure instanceof AccessDeniedException
                    ? "Could not save data to file " + failure.getMessage()
                            + " due to insufficient permissions to write to the file or the folder."
                    : "Could not save data due to the following error: " + failure.getMessage();
            assertEquals(expected, thrown.getMessage());
            assertSame(failure, thrown.getCause());
            assertStoredEmployees(getTypicalAddressBook().getEmployeeList(), model.getAddressBook().getEmployeeList());
            assertFalse(model.canUndoAddressBook());
            assertFalse(model.canRedoAddressBook());
        }
    }

    @Test
    public void execute_backupThenAutomaticSaveFails_keepsBackupAndPresentsAutomaticSaveError() throws Exception {
        Path backupFolder = folder.resolve("backups");
        Path backupPath = backupFolder.resolve("huntr-20261020-153005.json");
        Clock clock = Clock.fixed(Instant.parse("2026-10-20T15:30:05Z"), ZoneOffset.UTC);
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        IOException failure = new IOException("Atomic replacement is not supported for this data file.",
                new AtomicMoveNotSupportedException("temporary", "target", "unsupported"));
        LogicManager logic = new LogicManager(model, storageThatFails(failure), new AddressBookParser() {
            @Override
            public Command parseCommand(String commandText) throws ParseException {
                // Parse real syntax first, then isolate the backup location and clock from the user's files.
                assertTrue(super.parseCommand(commandText) instanceof BackupCommand);
                return new BackupCommand(backupFolder, clock);
            }
        });
        CommandException thrown = assertThrows(CommandException.class, () -> logic.execute("backup"));
        assertEquals("Could not save data due to the following error: "
                + "Atomic replacement is not supported for this data file.", thrown.getMessage());
        assertSame(failure, thrown.getCause());
        assertEquals(getTypicalAddressBook(),
                new AddressBook(new JsonAddressBookStorage(backupPath).readAddressBook().orElseThrow()));
        assertEquals(getTypicalAddressBook(), model.getAddressBook());
        assertFalse(thrown.getMessage().startsWith("Backed up"));
    }

    @Test
    public void execute_realReadOnlySaveFailure_preservesFileAndMemory() throws Exception {
        Path destination = folder.resolve("employees.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(destination);
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        storage.saveAddressBook(model.getAddressBook());
        byte[] original = Files.readAllBytes(destination);
        PosixFileAttributeView posix = Files.getFileAttributeView(destination, PosixFileAttributeView.class);
        DosFileAttributeView dos = Files.getFileAttributeView(destination, DosFileAttributeView.class);
        if (posix != null) {
            posix.setPermissions(PosixFilePermissions.fromString("r--------"));
        } else {
            dos.setReadOnly(true);
        }
        try {
            LogicManager logic = new LogicManager(model, storageManager(storage));
            CommandException failure = assertThrows(CommandException.class, () -> logic.execute("clear"));
            assertTrue(failure.getCause() instanceof AccessDeniedException);
            assertArrayEquals(original, Files.readAllBytes(destination));
            assertStoredEmployees(getTypicalAddressBook().getEmployeeList(), model.getAddressBook().getEmployeeList());
            assertFalse(model.canUndoAddressBook());
            assertFalse(model.canRedoAddressBook());
        } finally {
            if (posix != null) {
                posix.setPermissions(PosixFilePermissions.fromString("rw-------"));
            } else {
                dos.setReadOnly(false);
            }
        }
    }

    @Test
    public void execute_existingCommandMessages_remainUnchangedAlongsideUndo() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        LogicManager logic = new LogicManager(model,
                storageManager(new JsonAddressBookStorage(folder.resolve("employees.json"))));
        assertEquals("Active employee records have been cleared!", logic.execute("clear").getFeedbackToUser());
        assertEquals("No employee with ID MISSING was found.",
                assertThrows(CommandException.class, () -> logic.execute("delete id/MISSING")).getMessage());
        assertEquals("Undo successful.", logic.execute("undo").getFeedbackToUser());
        assertStoredEmployees(getTypicalAddressBook().getEmployeeList(), model.getAddressBook().getEmployeeList());
        Path csv = Path.of("exports", "staff.csv");
        Files.createDirectories(folder.resolve(csv).getParent());
        Files.writeString(folder.resolve(csv), ExportCommand.CSV_HEADER + "\n");
        String displayPath = csv.toString();
        assertEquals(displayPath + " has no employee rows to import.",
                assertThrows(CommandException.class, () -> new ImportCommand(folder, csv).execute(model)).getMessage());
    }

    @Test
    public void execute_successfulReadOnlyAndNoOpCommands_continueAutomaticSaving() throws Exception {
        CountingStorage storage = new CountingStorage(folder.resolve("employees.json"));
        LogicManager logic = new LogicManager(new ModelManager(), storageManager(storage));
        for (String command : List.of("list", "help", "clear", "clear", "exit")) {
            logic.execute(command);
        }
        assertEquals(5, storage.saves);
    }

    @Test
    public void execute_changeUndoAndRedoWithReadOnlyFile_preservesDiskViewAndEntireHistory() throws Exception {
        for (String command : List.of("edit id/" + ALICE.getId() + " r/Failed change", "undo", "redo")) {
            Model model = new ModelManager(new AddressBookBuilder().withEmployee(ALICE)
                    .withEmployee(BENSON.withArchived(true)).build(), new UserPrefs());
            JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("employees.json"));
            LogicManager logic = new LogicManager(model, storageManager(storage));
            List<Employee> initial = List.copyOf(model.getAddressBook().getEmployeeList());
            logic.execute("edit id/" + ALICE.getId() + " r/First edit");
            List<Employee> first = List.copyOf(model.getAddressBook().getEmployeeList());
            logic.execute("edit id/" + ALICE.getId() + " r/Second edit");
            List<Employee> second = List.copyOf(model.getAddressBook().getEmployeeList());
            logic.execute("undo");
            model.showArchivedEmployees();

            assertFailedAtomicSave(logic, model, storage, command, List.of(initial, first, second), 1);
        }
    }

    @Test
    public void execute_noOpAfterUndoWithReadOnlyFile_preservesDiskViewAndRedoSequence() throws Exception {
        for (String command : List.of("edit id/" + ALICE.getId() + " r/" + ALICE.getRole(), "clear")) {
            Model model = new ModelManager(new AddressBookBuilder().withEmployee(ALICE.withArchived(true)).build(),
                    new UserPrefs());
            JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("employees.json"));
            LogicManager logic = new LogicManager(model, storageManager(storage));
            List<Employee> initial = List.copyOf(model.getAddressBook().getEmployeeList());
            logic.execute("edit id/" + ALICE.getId() + " r/First edit");
            List<Employee> first = List.copyOf(model.getAddressBook().getEmployeeList());
            logic.execute("edit id/" + ALICE.getId() + " r/Second edit");
            List<Employee> second = List.copyOf(model.getAddressBook().getEmployeeList());
            logic.execute("undo");
            logic.execute("undo");
            model.showArchivedEmployees();

            assertFailedAtomicSave(logic, model, storage, command, List.of(initial, first, second), 0);
        }
    }

    private void assertFailedAtomicSave(LogicManager logic, Model model, JsonAddressBookStorage storage,
            String command, List<List<Employee>> history, int pointer) throws Exception {
        Path destination = storage.getAddressBookFilePath();
        byte[] original = Files.readAllBytes(destination);
        Object predicate = ((FilteredList<Employee>) model.getFilteredEmployeeList()).getPredicate();
        List<Employee> visible = List.copyOf(model.getFilteredEmployeeList());
        PosixFileAttributeView posix = Files.getFileAttributeView(destination, PosixFileAttributeView.class);
        DosFileAttributeView dos = Files.getFileAttributeView(destination, DosFileAttributeView.class);
        if (posix != null) {
            posix.setPermissions(PosixFilePermissions.fromString("r--------"));
        } else {
            dos.setReadOnly(true);
        }
        try {
            CommandException failure = assertThrows(CommandException.class, () -> logic.execute(command), command);
            assertTrue(failure.getCause() instanceof AccessDeniedException);
            assertArrayEquals(original, Files.readAllBytes(destination));
            assertSame(predicate, ((FilteredList<Employee>) model.getFilteredEmployeeList()).getPredicate());
            assertStoredEmployees(visible, model.getFilteredEmployeeList());
            assertStoredEmployees(history.get(pointer), model.getAddressBook().getEmployeeList());
            Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
            assertStoredEmployees(history.get(pointer), reloaded.getAddressBook().getEmployeeList());
            assertFalse(reloaded.canUndoAddressBook());
            assertFalse(reloaded.canRedoAddressBook());
            for (int i = pointer - 1; i >= 0; i--) {
                model.undoAddressBook();
                assertStoredEmployees(history.get(i), model.getAddressBook().getEmployeeList());
            }
            assertFalse(model.canUndoAddressBook());
            for (int i = 1; i < history.size(); i++) {
                model.redoAddressBook();
                assertStoredEmployees(history.get(i), model.getAddressBook().getEmployeeList());
            }
            assertFalse(model.canRedoAddressBook());
        } finally {
            if (posix != null) {
                posix.setPermissions(PosixFilePermissions.fromString("rw-------"));
            } else {
                dos.setReadOnly(false);
            }
        }
    }

    private StorageManager storageThatFails(IOException failure) {
        return storageManager(new JsonAddressBookStorage(folder.resolve("employees.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw failure;
            }
        });
    }

    private StorageManager storageManager(JsonAddressBookStorage storage) {
        return new StorageManager(storage, new JsonUserPrefsStorage(folder.resolve("prefs.json")));
    }

    private static class CountingStorage extends JsonAddressBookStorage {
        private int saves;

        CountingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            super.saveAddressBook(addressBook);
            saves++;
        }
    }
}

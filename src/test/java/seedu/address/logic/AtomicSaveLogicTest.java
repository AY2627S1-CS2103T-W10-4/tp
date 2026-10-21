package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class AtomicSaveLogicTest {
    @TempDir
    public Path folder;

    @Test
    public void execute_automaticSaveFailures_keepExactMessagesAndCausesWithoutMemoryRollback() {
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
            assertTrue(model.getAddressBook().getEmployeeList().isEmpty());
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
    public void execute_realReadOnlySaveFailure_preservesFileButLeavesMemoryMutation() throws Exception {
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
            assertTrue(model.getAddressBook().getEmployeeList().isEmpty());
        } finally {
            if (posix != null) {
                posix.setPermissions(PosixFilePermissions.fromString("rw-------"));
            } else {
                dos.setReadOnly(false);
            }
        }
    }

    @Test
    public void execute_existingCommandMessagesAndUnknownUndo_remainUnchanged() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        LogicManager logic = new LogicManager(model,
                storageManager(new JsonAddressBookStorage(folder.resolve("employees.json"))));
        assertEquals("Active employee records have been cleared!", logic.execute("clear").getFeedbackToUser());
        assertEquals("No employee with ID MISSING was found.",
                assertThrows(CommandException.class, () -> logic.execute("delete id/MISSING")).getMessage());
        assertEquals("Unknown command: undo\nType help to see the available commands.",
                assertThrows(ParseException.class, () -> logic.execute("undo")).getMessage());
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

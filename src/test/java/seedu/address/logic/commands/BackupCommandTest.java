package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showEmployeeAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_EMPLOYEE;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;

public class BackupCommandTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-20T15:30:05Z"), ZoneOffset.UTC);
    private static final String EXPECTED_FILE_NAME = "huntr-20261020-153005-0000000000.json";

    @TempDir
    public Path testFolder;

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new BackupCommand(null, FIXED_CLOCK));
        assertThrows(NullPointerException.class, () -> new BackupCommand(testFolder, null));
    }

    @Test
    public void execute_typicalEmployees_backupReadsBackAsSameData() throws Exception {
        Path backupFolder = testFolder.resolve("data").resolve("backups");
        Path backupPath = backupFolder.resolve(EXPECTED_FILE_NAME);
        int employeeCount = model.getAddressBook().getEmployeeList().size();
        String expectedMessage = "Backed up " + employeeCount + " employee(s) to " + backupPath;

        assertCommandSuccess(new BackupCommand(backupFolder, FIXED_CLOCK), model, expectedMessage, expectedModel);

        AddressBook backedUp = new AddressBook(new JsonAddressBookStorage(backupPath).readAddressBook().get());
        assertEquals(getTypicalAddressBook(), backedUp);
    }

    @Test
    public void execute_filteredList_backupHasHiddenEmployeesAndFilterKept() throws Exception {
        showEmployeeAtIndex(model, INDEX_FIRST_EMPLOYEE);
        showEmployeeAtIndex(expectedModel, INDEX_FIRST_EMPLOYEE);
        Path backupPath = testFolder.resolve(EXPECTED_FILE_NAME);
        int employeeCount = model.getAddressBook().getEmployeeList().size();
        String expectedMessage = String.format(BackupCommand.MESSAGE_SUCCESS, employeeCount, backupPath);

        assertCommandSuccess(new BackupCommand(testFolder, FIXED_CLOCK), model, expectedMessage, expectedModel);

        AddressBook backedUp = new AddressBook(new JsonAddressBookStorage(backupPath).readAddressBook().get());
        assertEquals(getTypicalAddressBook(), backedUp);
        assertEquals(1, model.getFilteredEmployeeList().size());
    }

    @Test
    public void execute_noEmployees_emptyBackupSaved() throws Exception {
        Path backupPath = testFolder.resolve(EXPECTED_FILE_NAME);
        String expectedMessage = String.format(BackupCommand.MESSAGE_SUCCESS, 0, backupPath);

        assertCommandSuccess(new BackupCommand(testFolder, FIXED_CLOCK), new ModelManager(), expectedMessage,
                new ModelManager());
        assertTrue(Files.isRegularFile(backupPath));
    }

    @Test
    public void execute_sameTimestampDifferentStates_preservesBothSnapshots() throws Exception {
        BackupCommand command = new BackupCommand(testFolder, FIXED_CLOCK);
        Path firstPath = testFolder.resolve(EXPECTED_FILE_NAME);
        Path secondPath = testFolder.resolve("huntr-20261020-153005-0000000001.json");
        AddressBook original = new AddressBook(model.getAddressBook());

        command.execute(model);
        byte[] firstContents = Files.readAllBytes(firstPath);
        model.setAddressBook(new AddressBook());
        CommandResult secondResult = command.execute(model);

        assertEquals(String.format(BackupCommand.MESSAGE_SUCCESS, 0, secondPath), secondResult.getFeedbackToUser());
        assertArrayEquals(firstContents, Files.readAllBytes(firstPath));
        assertEquals(original, new JsonAddressBookStorage(firstPath).readAddressBook().orElseThrow());
        assertEquals(new AddressBook(), new JsonAddressBookStorage(secondPath).readAddressBook().orElseThrow());
        try (var backups = Files.list(testFolder)) {
            assertEquals(2, backups.count());
        }
    }

    @Test
    public void execute_existingBackupAndSuffix_skipsOccupiedNamesAcrossCommandInstances() throws Exception {
        new BackupCommand(testFolder, FIXED_CLOCK).execute(model);
        Path occupiedSuffix = Files.createDirectory(testFolder.resolve("huntr-20261020-153005-0000000001.json"));
        Path backupPath = testFolder.resolve("huntr-20261020-153005-0000000002.json");

        CommandResult result = new BackupCommand(testFolder, FIXED_CLOCK).execute(model);

        assertEquals(String.format(BackupCommand.MESSAGE_SUCCESS,
                model.getAddressBook().getEmployeeList().size(), backupPath), result.getFeedbackToUser());
        assertEquals(model.getAddressBook(), new JsonAddressBookStorage(backupPath).readAddressBook().orElseThrow());
        assertTrue(Files.isDirectory(occupiedSuffix));
    }

    @Test
    public void execute_manyBackupsInSameSecond_nameOrderMatchesCreationOrder() throws Exception {
        List<Path> creationOrder = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            CommandResult result = new BackupCommand(testFolder, FIXED_CLOCK).execute(model);
            try (var entries = Files.list(testFolder)) {
                Path created = entries.filter(path -> !creationOrder.contains(path)).findFirst().orElseThrow();
                creationOrder.add(created);
                assertEquals(String.format(BackupCommand.MESSAGE_SUCCESS,
                        model.getAddressBook().getEmployeeList().size(), created), result.getFeedbackToUser());
            }
        }
        try (var entries = Files.list(testFolder)) {
            assertEquals(creationOrder, entries.sorted().toList());
        }
        Clock nextSecond = Clock.offset(FIXED_CLOCK, java.time.Duration.ofSeconds(1));
        new BackupCommand(testFolder, nextSecond).execute(model);
        try (var entries = Files.list(testFolder)) {
            assertEquals("huntr-20261020-153006-0000000000.json",
                    entries.sorted().toList().getLast().getFileName().toString());
        }
    }

    @Test
    public void execute_backupFolderIsFile_throwsCommandException() throws Exception {
        Path notAFolder = Files.createFile(testFolder.resolve("backups"));

        assertCommandFailure(new BackupCommand(notAFolder, FIXED_CLOCK), model,
                String.format(BackupCommand.MESSAGE_WRITE_FAILED, notAFolder.resolve(EXPECTED_FILE_NAME)));
    }

    @Test
    public void execute_storageIoFailures_keepBackupSpecificMessageAndOriginalCause() {
        Path backupPath = testFolder.resolve(EXPECTED_FILE_NAME);
        List<IOException> failures = List.of(new IOException("write failed"),
                new AccessDeniedException("temporary", "target", "permission diagnostic"),
                new IOException("Atomic replacement is not supported for this data file.",
                        new AtomicMoveNotSupportedException("temporary", "target", "unsupported")),
                new IOException("Cannot preserve the required file access protections."));
        for (IOException failure : failures) {
            BackupCommand command = new BackupCommand(testFolder, FIXED_CLOCK) {
                @Override
                JsonAddressBookStorage createStorage(Path path) {
                    assertEquals(backupPath, path);
                    return new JsonAddressBookStorage(path) {
                        @Override
                        public void saveNewAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                            throw failure;
                        }
                    };
                }
            };
            CommandException thrown = org.junit.jupiter.api.Assertions.assertThrows(CommandException.class, () ->
                    command.execute(model));
            assertEquals("Could not save the backup to " + backupPath
                    + ". Check that you are allowed to write to that folder.", thrown.getMessage());
            assertSame(failure, thrown.getCause());
            assertFalse(Files.exists(backupPath));
        }
    }

    @Test
    public void equals() {
        BackupCommand backup = new BackupCommand(testFolder, FIXED_CLOCK);

        assertTrue(backup.equals(backup));
        assertTrue(backup.equals(new BackupCommand(testFolder, FIXED_CLOCK)));
        assertTrue(new BackupCommand().equals(new BackupCommand()));
        assertFalse(backup.equals(null));
        assertFalse(backup.equals(1));
        assertFalse(backup.equals(new BackupCommand(Paths.get("other"), FIXED_CLOCK)));
        assertFalse(backup.equals(new BackupCommand(testFolder, Clock.systemUTC())));
    }

    @Test
    public void toStringMethod() {
        String expected = BackupCommand.class.getCanonicalName() + "{backupFolder="
                + BackupCommand.DEFAULT_BACKUP_FOLDER + "}";
        assertEquals(expected, new BackupCommand().toString());
    }

}

package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showEmployeeAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_EMPLOYEE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;

public class BackupCommandTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-20T15:30:05Z"), ZoneOffset.UTC);
    private static final String EXPECTED_FILE_NAME = "huntr-20261020-153005.json";

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
        String expectedMessage = String.format(BackupCommand.MESSAGE_SUCCESS, employeeCount, backupPath);

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
    public void execute_backupFolderIsFile_throwsCommandException() throws Exception {
        Path notAFolder = Files.createFile(testFolder.resolve("backups"));

        assertCommandFailure(new BackupCommand(notAFolder, FIXED_CLOCK), model,
                String.format(BackupCommand.MESSAGE_WRITE_FAILED, notAFolder.resolve(EXPECTED_FILE_NAME)));
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

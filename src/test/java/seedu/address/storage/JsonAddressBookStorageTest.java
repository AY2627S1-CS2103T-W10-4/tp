package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.BENSON;
import static seedu.address.testutil.TypicalEmployees.HOON;
import static seedu.address.testutil.TypicalEmployees.IDA;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.testutil.EmployeeBuilder;
import seedu.address.testutil.EmployeeUtil;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidEmployeeAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidEmployeeAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidEmployeeAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidEmployeeAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addEmployee(HOON);
        original.removeEmployee(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addEmployee(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }

    @Test
    public void saveAddressBook_partialWriteFailure_preservesStateAndAllowsRetry() throws Exception {
        assertFailedCommands(true, new IOException("injected partial write failure"));
    }

    @Test
    public void saveAddressBook_permissionFailure_preservesStateAndAllowsRetry() throws Exception {
        assertFailedCommands(true, new AccessDeniedException("injected permission failure"));
    }

    @Test
    public void saveAddressBook_replacementFailure_preservesStateAndAllowsRetry() throws Exception {
        assertFailedCommands(false, new IOException("injected replacement failure"));
    }

    @Test
    public void saveAddressBook_atomicMoveUnsupported_preservesStateAndAllowsRetry() throws Exception {
        assertFailedCommands(false, new AtomicMoveNotSupportedException("temporary", "destination", "unsupported"));
    }

    @Test
    public void saveAddressBook_cleanupFailureAfterCommit_doesNotReportFailure() throws Exception {
        Path file = testFolder.resolve("employees.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file) {
            @Override
            void deleteTemporaryFile(Path temporaryFile) throws IOException {
                super.deleteTemporaryFile(temporaryFile);
                throw new IOException("injected cleanup failure");
            }
        };
        storage.saveAddressBook(getTypicalAddressBook());
        assertEquals(getTypicalAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void readAddressBook_legacyId_rejectsWholeFileWithoutChangingIt() throws Exception {
        Path file = testFolder.resolve("employees.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(getTypicalAddressBook());
        String originalJson = Files.readString(file);
        for (String invalidId : new String[] {"e0001", "EMP-0042", "123"}) {
            String legacyJson = originalJson.replace("E0001", invalidId);
            Files.writeString(file, legacyJson);
            assertThrows(DataLoadingException.class, storage::readAddressBook);
            assertEquals(legacyJson, Files.readString(file));
        }
    }

    private void assertFailedCommands(boolean failDuringWrite, IOException exception) throws Exception {
        AddressBook afterDelete = getTypicalAddressBook();
        afterDelete.removeEmployee(ALICE);
        assertFailedSave("delete id/E0001", afterDelete, List.of(BENSON), failDuringWrite, exception);

        AddressBook afterAdd = getTypicalAddressBook();
        afterAdd.addEmployee(HOON);
        assertFailedSave(EmployeeUtil.getAddCommand(HOON), afterAdd, afterAdd.getEmployeeList(),
                failDuringWrite, exception);

        AddressBook afterEdit = getTypicalAddressBook();
        afterEdit.setEmployee(BENSON, new EmployeeBuilder(BENSON).withName("Updated Name").build());
        assertFailedSave("edit 1 n/Updated Name", afterEdit, afterEdit.getEmployeeList(), failDuringWrite, exception);

        assertFailedSave("clear", new AddressBook(), List.of(), failDuringWrite, exception);
    }

    private void assertFailedSave(String input, AddressBook expected, List<Employee> expectedVisible,
            boolean failDuringWrite, IOException exception) throws Exception {
        for (boolean destinationExists : new boolean[] {true, false}) {
            Path file = testFolder.resolve(input.split(" ")[0] + "-" + destinationExists + ".json");
            AddressBook original = getTypicalAddressBook();
            Model model = new ModelManager(original, new UserPrefs());
            model.updateFilteredEmployeeList(employee -> employee.equals(BENSON));
            var displayed = model.getFilteredEmployeeList();
            var originalFilter = model.getEmployeeFilter();
            if (destinationExists) {
                new JsonAddressBookStorage(file).saveAddressBook(original);
            }
            byte[] originalBytes = destinationExists ? Files.readAllBytes(file) : null;
            FailingStorage storage = new FailingStorage(file, failDuringWrite, exception);
            Logic logic = new LogicManager(model, new StorageManager(storage,
                    new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
            String expectedMessage;
            if (input.startsWith("delete")) {
                expectedMessage = exception instanceof AccessDeniedException
                        ? "Could not delete employee E0001: insufficient permission to save employee records. "
                            + "No employee records were changed."
                        : "Could not delete employee E0001: employee records could not be saved. "
                            + "No employee records were changed.";
            } else {
                expectedMessage = exception instanceof AccessDeniedException
                        ? "Could not save data to file " + exception.getMessage()
                            + " due to insufficient permissions to write to the file or the folder."
                        : "Could not save data due to the following error: " + exception.getMessage();
            }

            assertThrows(CommandException.class, expectedMessage, () -> logic.execute(input));

            assertEquals(original, model.getAddressBook());
            assertSame(displayed, model.getFilteredEmployeeList());
            assertSame(originalFilter, model.getEmployeeFilter());
            assertEquals(List.of(BENSON), displayed);
            if (destinationExists) {
                assertArrayEquals(originalBytes, Files.readAllBytes(file));
            } else {
                assertFalse(Files.exists(file));
            }
            try (var files = Files.list(testFolder)) {
                assertEquals(0, files.filter(path -> path.toString().endsWith(".tmp")).count());
            }

            storage.failure = null;
            logic.execute(input);
            assertEquals(expected, model.getAddressBook());
            assertEquals(expected, storage.readAddressBook().orElseThrow());
            assertSame(displayed, model.getFilteredEmployeeList());
            assertEquals(expectedVisible, displayed);
            if (input.startsWith("delete") || input.equals("clear")) {
                assertSame(originalFilter, model.getEmployeeFilter());
            } else {
                assertSame(Model.PREDICATE_SHOW_ALL_EMPLOYEES, model.getEmployeeFilter());
            }
        }
    }

    private static class FailingStorage extends JsonAddressBookStorage {
        private final boolean failDuringWrite;
        private IOException failure;

        FailingStorage(Path file, boolean failDuringWrite, IOException failure) {
            super(file);
            this.failDuringWrite = failDuringWrite;
            this.failure = failure;
        }

        @Override
        void writeTemporaryFile(Path temporaryFile, String json) throws IOException {
            if (failure != null && failDuringWrite) {
                Files.writeString(temporaryFile, "partial JSON");
                throw failure;
            }
            super.writeTemporaryFile(temporaryFile, json);
        }

        @Override
        void replaceFile(Path temporaryFile, Path destination) throws IOException {
            if (failure != null && !failDuringWrite) {
                throw failure;
            }
            super.replaceFile(temporaryFile, destination);
        }
    }
}

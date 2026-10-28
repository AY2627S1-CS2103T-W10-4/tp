package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.HOON;
import static seedu.address.testutil.TypicalEmployees.IDA;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.employee.Employee;
import seedu.address.testutil.EmployeeBuilder;

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
    public void readAddressBook_missingEmployeesField_throwsDataLoadingException() throws IOException {
        Path filePath = testFolder.resolve("MissingEmployeesAddressBook.json");
        Files.writeString(filePath, "{}");

        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
    }

    @Test
    public void readAddressBook_nullEmployeesField_throwsDataLoadingException() throws IOException {
        Path filePath = testFolder.resolve("NullEmployeesAddressBook.json");
        Files.writeString(filePath, "{\"employees\":null}");

        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
    }

    @Test
    public void readAddressBook_nullEmployeesWithHasEmployeesTrue_throwsDataLoadingException() throws IOException {
        Path filePath = testFolder.resolve("NullEmployeesWithHasEmployeesAddressBook.json");
        Files.writeString(filePath, "{\"employees\":null,\"hasEmployees\":true}");

        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
    }

    @Test
    public void readAddressBook_emptyEmployeesArray_loadsEmptyAddressBook() throws Exception {
        Path filePath = testFolder.resolve("EmptyEmployeesAddressBook.json");
        Files.writeString(filePath, "{\"employees\":[]}");

        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        assertTrue(storage.readAddressBook().orElseThrow().getEmployeeList().isEmpty());
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
    public void readAndSaveAddressBook_withArchivedEmployee_preservesStatus() throws Exception {
        Path filePath = testFolder.resolve("ArchivedAddressBook.json");
        AddressBook original = new AddressBook();
        original.addEmployee(ALICE.withArchived(true));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(original);
        assertEquals(original, new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void readAddressBook_legacyEmployeeWithoutArchivedStatus_loadsAsActive() throws Exception {
        Path filePath = testFolder.resolve("LegacyAddressBook.json");
        Files.writeString(filePath, """
                {"employees":[{"id":"E0001","name":"Alice Pauline",
                "phone":"94351253","email":"alice@example.com","department":"Engineering",
                "role":"Software Engineer"}]}
                """);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        assertFalse(storage.readAddressBook().orElseThrow().getEmployeeList().getFirst().isArchived());
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
    public void saveAddressBook_writerInvoked_receivesCompletePrettyUtf8BeforeAnyFileOperation() throws Exception {
        Path destination = testFolder.resolve("missing/employee.json");
        AddressBook book = orderedBook();
        AtomicBoolean invoked = new AtomicBoolean();
        AtomicFileWriter writer = new AtomicFileWriter() {
            @Override
            void write(Path path, byte[] contents) throws IOException {
                assertEquals(destination, path);
                assertFalse(Files.exists(path.getParent()));
                String json = new String(contents, StandardCharsets.UTF_8);
                String lineSeparator = System.lineSeparator();
                assertTrue(json.startsWith("{" + lineSeparator + "  \"employees\" : [ {" + lineSeparator));
                assertTrue(json.contains("李  Mei"));
                assertArrayEquals(json.getBytes(StandardCharsets.UTF_8), contents);
                assertEquals(rawValues(book), rawValues(assertDoesNotThrow(() ->
                        JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType())));
                JsonNode root = JsonUtil.fromJsonString(json, JsonNode.class);
                assertEquals(1, root.size());
                assertTrue(root.has("employees"));
                root.get("employees").forEach(employee -> {
                    assertEquals(8, employee.size());
                    assertTrue(employee.has("managerId"));
                    assertTrue(employee.get("managerId").isNull());
                });
                invoked.set(true);
            }
        };
        new JsonAddressBookStorage(destination, writer).saveAddressBook(book);
        assertTrue(invoked.get());
        assertFalse(Files.exists(destination.getParent()));
    }

    @Test
    public void saveAddressBook_modelConversionFails_doesNotInvokeWriterOrCreateDirectories() {
        Path destination = testFolder.resolve("missing/employee.json");
        AtomicFileWriter writer = new AtomicFileWriter() {
            @Override
            void write(Path path, byte[] contents) {
                throw new AssertionError("Writer must not run before model conversion and serialization finish");
            }
        };
        IllegalStateException failure = new IllegalStateException("cannot convert model");
        AddressBook brokenBook = new AddressBook() {
            @Override
            public javafx.collections.ObservableList<Employee> getEmployeeList() {
                throw failure;
            }
        };
        IllegalStateException thrown = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                new JsonAddressBookStorage(destination, writer).saveAddressBook(brokenBook));
        assertSame(failure, thrown);
        assertFalse(Files.exists(destination.getParent()));
    }

    @Test
    public void saveAddressBook_writerFailures_preserveExceptionClassificationAndCauses() {
        Path destination = testFolder.resolve("employee.json");
        AtomicMoveNotSupportedException unsupported = new AtomicMoveNotSupportedException("temp", "target", "reason");
        List<IOException> failures = List.of(new IOException("write failed", new IOException("cause")),
                new AccessDeniedException("temp", "target", "additional diagnostic text"),
                new IOException(AtomicFileWriter.ATOMIC_MOVE_UNSUPPORTED, unsupported),
                new IOException(AtomicFileWriter.PROTECTION_UNSUPPORTED));
        for (IOException failure : failures) {
            AtomicFileWriter writer = new AtomicFileWriter() {
                @Override
                void write(Path path, byte[] contents) throws IOException {
                    throw failure;
                }
            };
            IOException thrown = org.junit.jupiter.api.Assertions.assertThrows(IOException.class, () ->
                    new JsonAddressBookStorage(destination, writer).saveAddressBook(new AddressBook()));
            assertSame(failure, thrown);
            assertSame(failure.getCause(), thrown.getCause());
        }
    }

    @Test
    public void saveAddressBook_serializationFails_doesNotInvokeWriterOrTouchDestination() throws Exception {
        Path destination = testFolder.resolve("employee.json");
        byte[] original = "original bytes".getBytes(StandardCharsets.UTF_8);
        Files.write(destination, original);
        AtomicFileWriter writer = new AtomicFileWriter() {
            @Override
            void write(Path path, byte[] contents) {
                throw new AssertionError("Incomplete serialization must not reach the writer");
            }
        };
        // Scope the failing serializer to this test; production JSON utilities remain unchanged.
        Field mapperField = JsonUtil.class.getDeclaredField("objectMapper");
        mapperField.setAccessible(true);
        ObjectMapper originalMapper = (ObjectMapper) mapperField.get(null);
        SimpleModule module = new SimpleModule();
        module.addSerializer(JsonSerializableAddressBook.class, new JsonSerializer<JsonSerializableAddressBook>() {
            @Override
            public void serialize(JsonSerializableAddressBook value, JsonGenerator generator,
                    SerializerProvider provider) throws IOException {
                generator.writeStartObject();
                throw new IOException("serialization failed midway");
            }
        });
        try {
            mapperField.set(null, originalMapper.copy().registerModule(module));
            assertThrows(IOException.class, () ->
                    new JsonAddressBookStorage(destination, writer).saveAddressBook(orderedBook()));
            assertArrayEquals(original, Files.readAllBytes(destination));
            Files.delete(destination);
            assertThrows(IOException.class, () ->
                    new JsonAddressBookStorage(destination, writer).saveAddressBook(orderedBook()));
            assertFalse(Files.exists(destination));
        } finally {
            mapperField.set(null, originalMapper);
        }
    }

    @Test
    public void readAndSaveAddressBook_realCreationAndReplacement_preserveExactOrderedValues() throws Exception {
        Path path = testFolder.resolve("employee.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
        AddressBook book = orderedBook();
        storage.saveAddressBook(book);
        assertEquals(rawValues(book), rawValues(storage.readAddressBook().orElseThrow()));
        book.removeEmployee(HOON);
        book.addEmployee(IDA);
        storage.saveAddressBook(book);
        assertEquals(rawValues(book), rawValues(storage.readAddressBook().orElseThrow()));
        assertEquals(path, storage.getAddressBookFilePath());
    }

    @Test
    public void readAddressBook_unknownPropertiesIgnoredAndOrphanTemporaryNotLoaded() throws Exception {
        Path path = testFolder.resolve("employee.json");
        Files.writeString(testFolder.resolve(".huntr-orphan.tmp"), "{\"employees\":[]}");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
        assertTrue(storage.readAddressBook().isEmpty());
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(orderedBook()));
        Files.writeString(path, json.replaceFirst("\\{", "{\"unknown\":true,")
                .replace("\"archived\" : true", "\"unknownEmployeeField\" : 1, \"archived\" : true"));
        assertEquals(rawValues(orderedBook()), rawValues(storage.readAddressBook().orElseThrow()));
    }

    private AddressBook orderedBook() {
        AddressBook book = new AddressBook();
        book.addEmployee(new EmployeeBuilder().withId("e0123").withName("李  Mei").withPhone("91234567")
                .withEmail("Mei@example.COM").withDepartment("R&D").withRole("Engineer (Senior)")
                .withArchived(true).build());
        book.addEmployee(HOON);
        return book;
    }

    private List<List<Object>> rawValues(ReadOnlyAddressBook book) {
        return book.getEmployeeList().stream().map(employee -> List.<Object>of(employee.getId().value,
                employee.getName().fullName, employee.getPhone().value, employee.getEmail().value,
                employee.getDepartment().value, employee.getRole().value, employee.isArchived())).toList();
    }
}

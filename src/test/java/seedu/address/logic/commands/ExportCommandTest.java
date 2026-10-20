package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestUtil.createSymbolicLinkOrSkip;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.ParserUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.testutil.EmployeeBuilder;

public class ExportCommandTest {

    private static final Path STAFF_CSV = Paths.get("staff.csv");

    @TempDir
    public Path testFolder;

    private Path appFolder;
    private Path outsideFolder;

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @BeforeEach
    public void setUp() throws Exception {
        appFolder = Files.createDirectory(testFolder.resolve("app"));
        outsideFolder = Files.createDirectory(testFolder.resolve("outside"));
    }

    @Test
    public void constructor_nullPath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ExportCommand(null));
        assertThrows(NullPointerException.class, () -> new ExportCommand(null, STAFF_CSV));
    }

    @Test
    public void execute_typicalEmployees_writesHeaderAndOneLinePerEmployee() throws Exception {
        int employeeCount = model.getAddressBook().getEmployeeList().size();
        String expectedMessage = String.format(ExportCommand.MESSAGE_SUCCESS, employeeCount, STAFF_CSV);

        assertCommandSuccess(new ExportCommand(appFolder, STAFF_CSV), model, expectedMessage, expectedModel);

        List<String> lines = Files.readAllLines(appFolder.resolve(STAFF_CSV));
        assertEquals(employeeCount + 1, lines.size());
        assertEquals(ExportCommand.CSV_HEADER, lines.get(0));
        assertEquals("E0001,Alice Pauline,94351253,alice@example.com,Engineering,Software Engineer", lines.get(1));
    }

    @Test
    public void execute_valuesWithCommas_valuesQuoted() throws Exception {
        Employee employee = new EmployeeBuilder(ALICE).withName("Tan, Ah Kow").withDepartment("R&D, Asia").build();
        model.setAddressBook(new AddressBook());
        model.addEmployee(employee);

        new ExportCommand(appFolder, STAFF_CSV).execute(model);

        assertEquals("E0001,\"Tan, Ah Kow\",94351253,alice@example.com,\"R&D, Asia\",Software Engineer",
                Files.readAllLines(appFolder.resolve(STAFF_CSV)).get(1));
    }

    @Test
    public void execute_noEmployees_writesHeaderOnly() throws Exception {
        String expectedMessage = String.format(ExportCommand.MESSAGE_SUCCESS, 0, STAFF_CSV);

        assertCommandSuccess(new ExportCommand(appFolder, STAFF_CSV), new ModelManager(), expectedMessage,
                new ModelManager());
        assertEquals(List.of(ExportCommand.CSV_HEADER), Files.readAllLines(appFolder.resolve(STAFF_CSV)));
    }

    @Test
    public void execute_missingFolders_foldersCreated() throws Exception {
        Path filePath = Paths.get("exports", "2026", "staff.csv");

        new ExportCommand(appFolder, filePath).execute(model);

        assertTrue(Files.isRegularFile(appFolder.resolve(filePath)));
    }

    @Test
    public void execute_existingFile_fileReplaced() throws Exception {
        Files.writeString(appFolder.resolve(STAFF_CSV), "old content");

        new ExportCommand(appFolder, STAFF_CSV).execute(model);

        assertEquals(ExportCommand.CSV_HEADER, Files.readAllLines(appFolder.resolve(STAFF_CSV)).get(0));
    }

    @Test
    public void execute_pathIsFolder_throwsCommandException() throws Exception {
        Path folderPath = Paths.get("folder.csv");
        Files.createDirectory(appFolder.resolve(folderPath));

        assertCommandFailure(new ExportCommand(appFolder, folderPath), model,
                String.format(ExportCommand.MESSAGE_WRITE_FAILED, folderPath));
    }

    @Test
    public void execute_folderLinkedOutsideAppFolder_nothingWritten() {
        createSymbolicLinkOrSkip(appFolder.resolve("exports"), outsideFolder);

        assertCommandFailure(new ExportCommand(appFolder, Paths.get("exports", "staff.csv")), model,
                ParserUtil.MESSAGE_FILE_PATH_OUTSIDE_APP_FOLDER);
        assertFalse(Files.exists(outsideFolder.resolve("staff.csv")));
    }

    @Test
    public void execute_fileLinkedOutsideAppFolder_outsideFileUnchanged() throws Exception {
        Path outsideFile = Files.writeString(outsideFolder.resolve("other.csv"), "outside content");
        createSymbolicLinkOrSkip(appFolder.resolve(STAFF_CSV), outsideFile);

        assertCommandFailure(new ExportCommand(appFolder, STAFF_CSV), model,
                ParserUtil.MESSAGE_FILE_PATH_OUTSIDE_APP_FOLDER);
        assertEquals("outside content", Files.readString(outsideFile));
    }

    @Test
    public void execute_brokenLink_throwsCommandException() {
        createSymbolicLinkOrSkip(appFolder.resolve(STAFF_CSV), outsideFolder.resolve("missing.csv"));

        assertCommandFailure(new ExportCommand(appFolder, STAFF_CSV), model,
                String.format(CsvFileCommand.MESSAGE_CANNOT_CHECK_LOCATION, STAFF_CSV));
        assertFalse(Files.exists(outsideFolder.resolve("missing.csv")));
    }

    @Test
    public void execute_folderLinkedInsideAppFolder_fileWritten() throws Exception {
        Path realFolder = Files.createDirectory(appFolder.resolve("real"));
        createSymbolicLinkOrSkip(appFolder.resolve("exports"), realFolder);

        new ExportCommand(appFolder, Paths.get("exports", "staff.csv")).execute(model);

        assertTrue(Files.isRegularFile(realFolder.resolve("staff.csv")));
    }

    @Test
    public void equals() {
        ExportCommand exportStaff = new ExportCommand(STAFF_CSV);
        ExportCommand exportOther = new ExportCommand(Paths.get("other.csv"));

        assertTrue(exportStaff.equals(exportStaff));
        assertTrue(exportStaff.equals(new ExportCommand(STAFF_CSV)));
        assertFalse(exportStaff.equals(null));
        assertFalse(exportStaff.equals(1));
        assertFalse(exportStaff.equals(exportOther));
        assertFalse(exportStaff.equals(new ExportCommand(appFolder, STAFF_CSV)));
        assertFalse(exportStaff.equals(new ImportCommand(STAFF_CSV)));
    }

    @Test
    public void toStringMethod() {
        String expected = ExportCommand.class.getCanonicalName() + "{filePath=" + STAFF_CSV + "}";
        assertEquals(expected, new ExportCommand(STAFF_CSV).toString());
    }

}

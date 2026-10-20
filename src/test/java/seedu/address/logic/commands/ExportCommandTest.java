package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.testutil.EmployeeBuilder;

public class ExportCommandTest {

    @TempDir
    public Path testFolder;

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullPath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ExportCommand(null));
    }

    @Test
    public void execute_typicalEmployees_writesHeaderAndOneLinePerEmployee() throws Exception {
        Path filePath = testFolder.resolve("staff.csv");
        int employeeCount = model.getAddressBook().getEmployeeList().size();
        String expectedMessage = String.format(ExportCommand.MESSAGE_SUCCESS, employeeCount, filePath);

        assertCommandSuccess(new ExportCommand(filePath), model, expectedMessage, expectedModel);

        List<String> lines = Files.readAllLines(filePath);
        assertEquals(employeeCount + 1, lines.size());
        assertEquals(ExportCommand.CSV_HEADER, lines.get(0));
        assertEquals("E0001,Alice Pauline,94351253,alice@example.com,Engineering,Software Engineer", lines.get(1));
    }

    @Test
    public void execute_valuesWithCommas_valuesQuoted() throws Exception {
        Employee employee = new EmployeeBuilder(ALICE).withName("Tan, Ah Kow").withDepartment("R&D, Asia").build();
        model.setAddressBook(new AddressBook());
        model.addEmployee(employee);
        Path filePath = testFolder.resolve("staff.csv");

        new ExportCommand(filePath).execute(model);

        assertEquals("E0001,\"Tan, Ah Kow\",94351253,alice@example.com,\"R&D, Asia\",Software Engineer",
                Files.readAllLines(filePath).get(1));
    }

    @Test
    public void execute_noEmployees_writesHeaderOnly() throws Exception {
        Model emptyModel = new ModelManager();
        Path filePath = testFolder.resolve("staff.csv");
        String expectedMessage = String.format(ExportCommand.MESSAGE_SUCCESS, 0, filePath);

        assertCommandSuccess(new ExportCommand(filePath), emptyModel, expectedMessage, new ModelManager());
        assertEquals(List.of(ExportCommand.CSV_HEADER), Files.readAllLines(filePath));
    }

    @Test
    public void execute_missingFolders_foldersCreated() throws Exception {
        Path filePath = testFolder.resolve("exports").resolve("2026").resolve("staff.csv");

        new ExportCommand(filePath).execute(model);

        assertTrue(Files.isRegularFile(filePath));
    }

    @Test
    public void execute_existingFile_fileReplaced() throws Exception {
        Path filePath = testFolder.resolve("staff.csv");
        Files.writeString(filePath, "old content");

        new ExportCommand(filePath).execute(model);

        assertEquals(ExportCommand.CSV_HEADER, Files.readAllLines(filePath).get(0));
    }

    @Test
    public void execute_pathIsFolder_throwsCommandException() throws Exception {
        Path folderPath = Files.createDirectory(testFolder.resolve("folder.csv"));

        assertCommandFailure(new ExportCommand(folderPath), model,
                String.format(ExportCommand.MESSAGE_WRITE_FAILED, folderPath));
    }

    @Test
    public void equals() {
        ExportCommand exportStaff = new ExportCommand(Paths.get("staff.csv"));
        ExportCommand exportOther = new ExportCommand(Paths.get("other.csv"));

        assertTrue(exportStaff.equals(exportStaff));
        assertTrue(exportStaff.equals(new ExportCommand(Paths.get("staff.csv"))));
        assertFalse(exportStaff.equals(null));
        assertFalse(exportStaff.equals(1));
        assertFalse(exportStaff.equals(exportOther));
    }

    @Test
    public void toStringMethod() {
        Path filePath = Paths.get("staff.csv");
        String expected = ExportCommand.class.getCanonicalName() + "{filePath=" + filePath + "}";
        assertEquals(expected, new ExportCommand(filePath).toString());
    }

}

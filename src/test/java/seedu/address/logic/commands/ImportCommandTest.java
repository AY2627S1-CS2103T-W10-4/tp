package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestUtil.createSymbolicLinkOrSkip;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.AMY;
import static seedu.address.testutil.TypicalEmployees.BOB;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.util.CsvUtil;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.Phone;
import seedu.address.model.employee.ReportingStructure;
import seedu.address.testutil.EmployeeBuilder;

public class ImportCommandTest {

    private static final Path STAFF_CSV = Paths.get("staff.csv");
    private static final String AMY_ROW = ExportCommand.toCsvLine(AMY);
    private static final String BOB_ROW = ExportCommand.toCsvLine(BOB);

    @TempDir
    public Path testFolder;

    private Path appFolder;
    private Path outsideFolder;

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @BeforeEach
    public void setUp() throws Exception {
        appFolder = Files.createDirectory(testFolder.resolve("app"));
        outsideFolder = Files.createDirectory(testFolder.resolve("outside"));
    }

    @Test
    public void constructor_nullPath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ImportCommand(null));
        assertThrows(NullPointerException.class, () -> new ImportCommand(null, STAFF_CSV));
    }

    @Test
    public void execute_validRows_employeesAdded() throws Exception {
        writeCsv(ExportCommand.CSV_HEADER, AMY_ROW, BOB_ROW);
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.addEmployee(AMY);
        expectedModel.addEmployee(BOB);

        assertCommandSuccess(importStaffCsv(), model, String.format(ImportCommand.MESSAGE_SUCCESS, 2, STAFF_CSV),
                expectedModel);
    }

    @Test
    public void execute_exportedFile_importsBackUnchanged() throws Exception {
        Employee quotedEmployee = new EmployeeBuilder(AMY).withName("Tan, Ah Kow").withDepartment("R&D, Asia")
                .withRole("Engineer (Backend), Senior").build();
        AddressBook original = getTypicalAddressBook();
        original.addEmployee(quotedEmployee);
        new ExportCommand(appFolder, STAFF_CSV).execute(new ModelManager(original, new UserPrefs()));

        Model emptyModel = new ModelManager();
        importStaffCsv().execute(emptyModel);

        assertEquals(original, emptyModel.getAddressBook());
    }

    @Test
    public void execute_exportedArchivedEmployee_importsBackArchived() throws Exception {
        AddressBook original = new AddressBook();
        original.addEmployee(AMY.withArchived(true));
        original.addEmployee(BOB);
        new ExportCommand(appFolder, STAFF_CSV).execute(new ModelManager(original, new UserPrefs()));

        Model emptyModel = new ModelManager();
        importStaffCsv().execute(emptyModel);

        assertEquals(original, emptyModel.getAddressBook());
    }

    @Test
    public void execute_noArchivedColumn_employeesImportedAsActive() throws Exception {
        String header = CsvUtil.toCsvLine(ExportCommand.REQUIRED_CSV_COLUMNS);
        writeCsv(header, CsvUtil.toCsvLine(CsvUtil.parseCsvLine(AMY_ROW).subList(0, 6)));
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.addEmployee(AMY);

        assertCommandSuccess(importStaffCsv(), model, String.format(ImportCommand.MESSAGE_SUCCESS, 1, STAFF_CSV),
                expectedModel);
    }

    @Test
    public void execute_legacySevenColumns_preservesArchiveStatusWithoutManager() throws Exception {
        writeCsv(CsvUtil.toCsvLine(ExportCommand.LEGACY_CSV_COLUMNS),
                CsvUtil.toCsvLine(CsvUtil.parseCsvLine(ExportCommand.toCsvLine(AMY.withArchived(true)))
                        .subList(0, 7)));
        Model imported = new ModelManager();
        importStaffCsv().execute(imported);
        assertEquals(List.of(AMY.withArchived(true)), imported.getAddressBook().getEmployeeList());
    }

    @Test
    public void execute_exportedRelationships_roundTripPreservesOrderAndForwardReferences() throws Exception {
        Employee boss = new EmployeeBuilder(AMY).withId("BOSS").build();
        Employee lead = new EmployeeBuilder(BOB).withId("LEAD").build().withArchived(true)
                .withManager(boss.getId());
        Employee worker = new EmployeeBuilder(ALICE).withId("WORKER").build().withManager(lead.getId());
        AddressBook original = new AddressBook();
        original.setEmployees(List.of(worker, lead, boss));
        new ExportCommand(appFolder, STAFF_CSV).execute(new ModelManager(original, new UserPrefs()));
        assertEquals("id,name,phone,email,department,role,archived,managerId",
                Files.readAllLines(appFolder.resolve(STAFF_CSV)).getFirst());
        Model imported = new ModelManager();
        importStaffCsv().execute(imported);
        assertEquals(original.getEmployeeList(), imported.getAddressBook().getEmployeeList());
    }

    @Test
    public void execute_managerInExistingRoster_canonicalisesReference() throws Exception {
        Employee amy = AMY.withManager(new EmployeeId(ALICE.getId().value.toLowerCase(Locale.ROOT)));
        writeCsv(ExportCommand.CSV_HEADER, ExportCommand.toCsvLine(amy));
        importStaffCsv().execute(model);
        Employee imported = model.getAddressBook().getEmployeeList().getLast();
        assertEquals(ALICE.getId().value, imported.getManagerId().orElseThrow().value);
    }

    @Test
    public void execute_invalidRelationships_nothingAddedAndViewUnchanged() throws Exception {
        model.updateFilteredEmployeeList(employee -> false);
        writeCsv(ExportCommand.CSV_HEADER, BOB_ROW,
                ExportCommand.toCsvLine(AMY.withManager(new EmployeeId("MISSING"))));
        assertCommandFailure(importStaffCsv(), model, String.format(ImportCommand.MESSAGE_INVALID_ROW, 3,
                String.format(ReportingStructure.MESSAGE_MISSING_MANAGER, "MISSING")));
        writeCsv(ExportCommand.CSV_HEADER, ExportCommand.toCsvLine(AMY.withManager(AMY.getId())));
        assertCommandFailure(importStaffCsv(), model, String.format(ImportCommand.MESSAGE_INVALID_ROW, 2,
                ReportingStructure.MESSAGE_SELF_REPORTING));
        writeCsv(ExportCommand.CSV_HEADER, ExportCommand.toCsvLine(AMY.withManager(BOB.getId())),
                ExportCommand.toCsvLine(BOB.withManager(AMY.getId())));
        assertCommandFailure(importStaffCsv(), model, String.format(ImportCommand.MESSAGE_INVALID_RELATIONSHIPS,
                ReportingStructure.MESSAGE_CYCLE));
        writeCsv(ExportCommand.CSV_HEADER, AMY_ROW + "@@@");
        assertCommandFailure(importStaffCsv(), model, String.format(ImportCommand.MESSAGE_INVALID_ROW, 2,
                EmployeeId.MESSAGE_CONSTRAINTS));
        assertTrue(model.getFilteredEmployeeList().isEmpty());
    }

    @Test
    public void execute_archivedValueBlankOrMixedCase_parsed() throws Exception {
        String amyRowBlank = rowWithArchived(AMY, "");
        String bobRowTrue = rowWithArchived(BOB, " TRUE ");
        writeCsv(ExportCommand.CSV_HEADER, amyRowBlank, bobRowTrue);
        Model emptyModel = new ModelManager();

        importStaffCsv().execute(emptyModel);

        assertEquals(List.of(AMY, BOB.withArchived(true)), emptyModel.getAddressBook().getEmployeeList());
    }

    @Test
    public void execute_invalidArchivedValue_nothingAddedAndRowNamed() throws Exception {
        writeCsv(ExportCommand.CSV_HEADER, rowWithArchived(AMY, "yes"));

        assertCommandFailure(importStaffCsv(), model,
                String.format(ImportCommand.MESSAGE_INVALID_ROW, 2, ImportCommand.MESSAGE_INVALID_ARCHIVED));
    }

    @Test
    public void execute_filteredList_showsAllEmployeesAfterImport() throws Exception {
        writeCsv(ExportCommand.CSV_HEADER, AMY_ROW);
        model.updateFilteredEmployeeList(employee -> false);

        importStaffCsv().execute(model);

        assertEquals(model.getAddressBook().getEmployeeList(), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_byteOrderMarkBlankLinesAndHeaderCase_ignored() throws Exception {
        writeCsv("\uFEFFID, Name ,Phone,Email,Department,ROLE, Archived, ManagerID", "", AMY_ROW, "  ");
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.addEmployee(AMY);

        assertCommandSuccess(importStaffCsv(), model, String.format(ImportCommand.MESSAGE_SUCCESS, 1, STAFF_CSV),
                expectedModel);
    }

    @Test
    public void execute_invalidValue_nothingAddedAndRowNamed() throws Exception {
        writeCsv(ExportCommand.CSV_HEADER, AMY_ROW, "", BOB_ROW.replace(VALID_PHONE_BOB, "911a"));

        assertCommandFailure(importStaffCsv(), model,
                String.format(ImportCommand.MESSAGE_INVALID_ROW, 4, Phone.MESSAGE_CONSTRAINTS));
    }

    @Test
    public void execute_blankId_nothingAddedAndRowNamed() throws Exception {
        writeCsv(ExportCommand.CSV_HEADER, AMY_ROW.replace(AMY.getId().toString(), " "));

        assertCommandFailure(importStaffCsv(), model,
                String.format(ImportCommand.MESSAGE_INVALID_ROW, 2, ParserUtil.MESSAGE_BLANK_EMPLOYEE_ID));
    }

    @Test
    public void execute_wrongValueCount_nothingAddedAndRowNamed() throws Exception {
        writeCsv(ExportCommand.CSV_HEADER, AMY_ROW + ",extra");

        assertCommandFailure(importStaffCsv(), model, String.format(ImportCommand.MESSAGE_INVALID_ROW, 2,
                String.format(ImportCommand.MESSAGE_WRONG_VALUE_COUNT, 8, ExportCommand.CSV_HEADER, 9)));
    }

    @Test
    public void execute_unclosedQuote_nothingAddedAndRowNamed() throws Exception {
        writeCsv(ExportCommand.CSV_HEADER, AMY_ROW, "E2000,\"Tan, Ah Kow");

        assertCommandFailure(importStaffCsv(), model,
                String.format(ImportCommand.MESSAGE_INVALID_ROW, 3, CsvUtil.MESSAGE_UNCLOSED_QUOTE));
    }

    @Test
    public void execute_misplacedQuotes_nothingAddedAndRowNamed() throws Exception {
        // without the quotes, the name would be valid, so the row must not be read as "Amy Bee"
        writeCsv(ExportCommand.CSV_HEADER, BOB_ROW, AMY_ROW.replace(VALID_NAME_AMY, "A\"my\" Bee"));

        assertCommandFailure(importStaffCsv(), model,
                String.format(ImportCommand.MESSAGE_INVALID_ROW, 3, CsvUtil.MESSAGE_MISPLACED_QUOTE));
    }

    @Test
    public void execute_duplicateIdInFile_nothingAddedAndBothRowsNamed() throws Exception {
        String sameIdRow = ExportCommand.toCsvLine(new EmployeeBuilder(BOB).withId("e1001").build());
        writeCsv(ExportCommand.CSV_HEADER, AMY_ROW, sameIdRow);

        assertCommandFailure(importStaffCsv(), model, String.format(ImportCommand.MESSAGE_INVALID_ROW, 3,
                String.format(ImportCommand.MESSAGE_DUPLICATE_IN_FILE, "e1001", 2)));
    }

    @Test
    public void execute_idAlreadyInHuntr_nothingAddedAndRowNamed() throws Exception {
        String existingIdRow = ExportCommand.toCsvLine(new EmployeeBuilder(BOB).withId(ALICE.getId().value).build());
        writeCsv(ExportCommand.CSV_HEADER, AMY_ROW, existingIdRow);

        assertCommandFailure(importStaffCsv(), model, String.format(ImportCommand.MESSAGE_INVALID_ROW, 3,
                String.format(AddCommand.MESSAGE_DUPLICATE_EMPLOYEE, ALICE.getId())));
    }

    @Test
    public void execute_missingFile_throwsCommandException() {
        assertCommandFailure(importStaffCsv(), model, String.format(ImportCommand.MESSAGE_FILE_NOT_FOUND, STAFF_CSV));
    }

    @Test
    public void execute_emptyFile_throwsCommandException() throws Exception {
        writeCsv("", " ");

        assertCommandFailure(importStaffCsv(), model, String.format(ImportCommand.MESSAGE_EMPTY_FILE, STAFF_CSV));
    }

    @Test
    public void execute_wrongHeader_throwsCommandException() throws Exception {
        String expectedMessage = String.format(ImportCommand.MESSAGE_WRONG_HEADER, STAFF_CSV);

        writeCsv("id,name,phone,email,department", AMY_ROW);
        assertCommandFailure(importStaffCsv(), model, expectedMessage);
        writeCsv(AMY_ROW, BOB_ROW);
        assertCommandFailure(importStaffCsv(), model, expectedMessage);
        writeCsv("\"id,name", AMY_ROW);
        assertCommandFailure(importStaffCsv(), model, expectedMessage);
    }

    @Test
    public void execute_headerOnly_throwsCommandException() throws Exception {
        writeCsv(ExportCommand.CSV_HEADER, "");

        assertCommandFailure(importStaffCsv(), model, String.format(ImportCommand.MESSAGE_NO_EMPLOYEES, STAFF_CSV));
    }

    @Test
    public void execute_notUtf8_throwsCommandException() throws Exception {
        Files.write(appFolder.resolve(STAFF_CSV), (ExportCommand.CSV_HEADER
                + "\nE2000,Zoë Tan,91234567,zoe@example.com,Sales,Rep").getBytes(StandardCharsets.ISO_8859_1));

        assertCommandFailure(importStaffCsv(), model, String.format(ImportCommand.MESSAGE_NOT_UTF8, STAFF_CSV));
    }

    @Test
    public void execute_folderLinkedOutsideAppFolder_nothingAdded() throws Exception {
        Files.write(outsideFolder.resolve(STAFF_CSV), List.of(ExportCommand.CSV_HEADER, AMY_ROW));
        createSymbolicLinkOrSkip(appFolder.resolve("exports"), outsideFolder);

        assertCommandFailure(new ImportCommand(appFolder, Paths.get("exports", "staff.csv")), model,
                ParserUtil.MESSAGE_FILE_PATH_OUTSIDE_APP_FOLDER);
    }

    @Test
    public void execute_fileLinkedOutsideAppFolder_nothingAdded() throws Exception {
        Path outsideFile = Files.write(outsideFolder.resolve(STAFF_CSV), List.of(ExportCommand.CSV_HEADER, AMY_ROW));
        createSymbolicLinkOrSkip(appFolder.resolve(STAFF_CSV), outsideFile);

        assertCommandFailure(importStaffCsv(), model, ParserUtil.MESSAGE_FILE_PATH_OUTSIDE_APP_FOLDER);
    }

    @Test
    public void equals() {
        ImportCommand importStaff = new ImportCommand(STAFF_CSV);
        ImportCommand importOther = new ImportCommand(Paths.get("other.csv"));

        assertTrue(importStaff.equals(importStaff));
        assertTrue(importStaff.equals(new ImportCommand(STAFF_CSV)));
        assertFalse(importStaff.equals(null));
        assertFalse(importStaff.equals(1));
        assertFalse(importStaff.equals(importOther));
        assertFalse(importStaff.equals(new ImportCommand(appFolder, STAFF_CSV)));
        assertFalse(importStaff.equals(new ExportCommand(STAFF_CSV)));
    }

    @Test
    public void toStringMethod() {
        String expected = ImportCommand.class.getCanonicalName() + "{filePath=" + STAFF_CSV + "}";
        assertEquals(expected, new ImportCommand(STAFF_CSV).toString());
    }

    private ImportCommand importStaffCsv() {
        return new ImportCommand(appFolder, STAFF_CSV);
    }

    private String rowWithArchived(Employee employee, String archived) throws Exception {
        List<String> values = new ArrayList<>(CsvUtil.parseCsvLine(ExportCommand.toCsvLine(employee)));
        values.set(6, archived);
        return CsvUtil.toCsvLine(values);
    }

    /**
     * Writes {@code lines} to staff.csv in the app folder, replacing any earlier content.
     */
    private void writeCsv(String... lines) throws IOException {
        Files.write(appFolder.resolve(STAFF_CSV), List.of(lines));
    }

}

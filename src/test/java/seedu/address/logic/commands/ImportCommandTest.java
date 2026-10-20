package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.AMY;
import static seedu.address.testutil.TypicalEmployees.BOB;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.util.CsvUtil;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.Phone;
import seedu.address.testutil.EmployeeBuilder;

public class ImportCommandTest {

    private static final String AMY_ROW = ExportCommand.toCsvLine(AMY);
    private static final String BOB_ROW = ExportCommand.toCsvLine(BOB);

    @TempDir
    public Path testFolder;

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullPath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ImportCommand(null));
    }

    @Test
    public void execute_validRows_employeesAdded() throws Exception {
        Path filePath = writeCsv(ExportCommand.CSV_HEADER, AMY_ROW, BOB_ROW);
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.addEmployee(AMY);
        expectedModel.addEmployee(BOB);

        assertCommandSuccess(new ImportCommand(filePath), model,
                String.format(ImportCommand.MESSAGE_SUCCESS, 2, filePath), expectedModel);
    }

    @Test
    public void execute_exportedFile_importsBackUnchanged() throws Exception {
        Employee quotedEmployee = new EmployeeBuilder(AMY).withName("Tan, Ah Kow").withDepartment("R&D, Asia")
                .withRole("Engineer (Backend), Senior").build();
        AddressBook original = getTypicalAddressBook();
        original.addEmployee(quotedEmployee);
        Path filePath = testFolder.resolve("staff.csv");
        new ExportCommand(filePath).execute(new ModelManager(original, new UserPrefs()));

        Model emptyModel = new ModelManager();
        new ImportCommand(filePath).execute(emptyModel);

        assertEquals(original, emptyModel.getAddressBook());
    }

    @Test
    public void execute_filteredList_showsAllEmployeesAfterImport() throws Exception {
        model.updateFilteredEmployeeList(employee -> false);

        new ImportCommand(writeCsv(ExportCommand.CSV_HEADER, AMY_ROW)).execute(model);

        assertEquals(model.getAddressBook().getEmployeeList(), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_byteOrderMarkBlankLinesAndHeaderCase_ignored() throws Exception {
        Path filePath = writeCsv("﻿ID, Name ,Phone,Email,Department,ROLE", "", AMY_ROW, "  ");
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.addEmployee(AMY);

        assertCommandSuccess(new ImportCommand(filePath), model,
                String.format(ImportCommand.MESSAGE_SUCCESS, 1, filePath), expectedModel);
    }

    @Test
    public void execute_invalidValue_nothingAddedAndRowNamed() throws Exception {
        String invalidPhoneRow = BOB_ROW.replace(VALID_PHONE_BOB, "911a");
        Path filePath = writeCsv(ExportCommand.CSV_HEADER, AMY_ROW, "", invalidPhoneRow);

        assertCommandFailure(new ImportCommand(filePath), model,
                String.format(ImportCommand.MESSAGE_INVALID_ROW, 4, Phone.MESSAGE_CONSTRAINTS));
    }

    @Test
    public void execute_blankId_nothingAddedAndRowNamed() throws Exception {
        Path filePath = writeCsv(ExportCommand.CSV_HEADER, AMY_ROW.replace(AMY.getId().toString(), " "));

        assertCommandFailure(new ImportCommand(filePath), model,
                String.format(ImportCommand.MESSAGE_INVALID_ROW, 2, ParserUtil.MESSAGE_BLANK_EMPLOYEE_ID));
    }

    @Test
    public void execute_wrongValueCount_nothingAddedAndRowNamed() throws Exception {
        Path filePath = writeCsv(ExportCommand.CSV_HEADER, AMY_ROW + ",extra");

        assertCommandFailure(new ImportCommand(filePath), model, String.format(ImportCommand.MESSAGE_INVALID_ROW, 2,
                String.format(ImportCommand.MESSAGE_WRONG_VALUE_COUNT, 7)));
    }

    @Test
    public void execute_unclosedQuote_nothingAddedAndRowNamed() throws Exception {
        Path filePath = writeCsv(ExportCommand.CSV_HEADER, AMY_ROW, "E2000,\"Tan, Ah Kow");

        assertCommandFailure(new ImportCommand(filePath), model,
                String.format(ImportCommand.MESSAGE_INVALID_ROW, 3, CsvUtil.MESSAGE_UNCLOSED_QUOTE));
    }

    @Test
    public void execute_duplicateIdInFile_nothingAddedAndBothRowsNamed() throws Exception {
        String sameIdRow = ExportCommand.toCsvLine(new EmployeeBuilder(BOB).withId("e1001").build());
        Path filePath = writeCsv(ExportCommand.CSV_HEADER, AMY_ROW, sameIdRow);

        assertCommandFailure(new ImportCommand(filePath), model, String.format(ImportCommand.MESSAGE_INVALID_ROW, 3,
                String.format(ImportCommand.MESSAGE_DUPLICATE_IN_FILE, "e1001", 2)));
    }

    @Test
    public void execute_idAlreadyInHuntr_nothingAddedAndRowNamed() throws Exception {
        String existingIdRow = ExportCommand.toCsvLine(new EmployeeBuilder(BOB).withId(ALICE.getId().value).build());
        Path filePath = writeCsv(ExportCommand.CSV_HEADER, AMY_ROW, existingIdRow);

        assertCommandFailure(new ImportCommand(filePath), model, String.format(ImportCommand.MESSAGE_INVALID_ROW, 3,
                String.format(AddCommand.MESSAGE_DUPLICATE_EMPLOYEE, ALICE.getId())));
    }

    @Test
    public void execute_missingFile_throwsCommandException() {
        Path filePath = testFolder.resolve("missing.csv");

        assertCommandFailure(new ImportCommand(filePath), model,
                String.format(ImportCommand.MESSAGE_FILE_NOT_FOUND, filePath));
    }

    @Test
    public void execute_emptyFile_throwsCommandException() throws Exception {
        Path filePath = writeCsv("", " ");

        assertCommandFailure(new ImportCommand(filePath), model,
                String.format(ImportCommand.MESSAGE_EMPTY_FILE, filePath));
    }

    @Test
    public void execute_wrongHeader_throwsCommandException() throws Exception {
        String expectedMessage = String.format(ImportCommand.MESSAGE_WRONG_HEADER, testFolder.resolve("staff.csv"));

        assertCommandFailure(new ImportCommand(writeCsv("id,name,phone,email,department", AMY_ROW)), model,
                expectedMessage);
        assertCommandFailure(new ImportCommand(writeCsv(AMY_ROW, BOB_ROW)), model, expectedMessage);
        assertCommandFailure(new ImportCommand(writeCsv("\"id,name", AMY_ROW)), model, expectedMessage);
    }

    @Test
    public void execute_headerOnly_throwsCommandException() throws Exception {
        Path filePath = writeCsv(ExportCommand.CSV_HEADER, "");

        assertCommandFailure(new ImportCommand(filePath), model,
                String.format(ImportCommand.MESSAGE_NO_EMPLOYEES, filePath));
    }

    @Test
    public void execute_notUtf8_throwsCommandException() throws Exception {
        Path filePath = testFolder.resolve("staff.csv");
        Files.write(filePath, (ExportCommand.CSV_HEADER + "\nE2000,Zoë Tan,91234567,zoe@example.com,Sales,Rep")
                .getBytes(StandardCharsets.ISO_8859_1));

        assertCommandFailure(new ImportCommand(filePath), model,
                String.format(ImportCommand.MESSAGE_NOT_UTF8, filePath));
    }

    @Test
    public void equals() {
        ImportCommand importStaff = new ImportCommand(Paths.get("staff.csv"));
        ImportCommand importOther = new ImportCommand(Paths.get("other.csv"));

        assertTrue(importStaff.equals(importStaff));
        assertTrue(importStaff.equals(new ImportCommand(Paths.get("staff.csv"))));
        assertFalse(importStaff.equals(null));
        assertFalse(importStaff.equals(1));
        assertFalse(importStaff.equals(importOther));
    }

    @Test
    public void toStringMethod() {
        Path filePath = Paths.get("staff.csv");
        String expected = ImportCommand.class.getCanonicalName() + "{filePath=" + filePath + "}";
        assertEquals(expected, new ImportCommand(filePath).toString());
    }

    /**
     * Writes {@code lines} to staff.csv in the test folder and returns its path.
     */
    private Path writeCsv(String... lines) throws IOException {
        return Files.write(testFolder.resolve("staff.csv"), List.of(lines));
    }

}

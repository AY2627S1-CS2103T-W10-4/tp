package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.BENSON;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.NameContainsKeywordsPredicate;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.EmployeeBuilder;

public class DeleteCommandIntegrationTest {

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage addressBookStorage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        logic = new LogicManager(model, new StorageManager(addressBookStorage, userPrefsStorage));
    }

    @Test
    public void execute_customId_persistsDeletionAndRemainingIdsAsTyped() throws Exception {
        Employee employeeToDelete = new EmployeeBuilder().withId("EMP-0042").build();
        Employee sameNameEmployee = new EmployeeBuilder(employeeToDelete).withId("aBc_0043").build();
        model.setAddressBook(new AddressBook());
        model.addEmployee(employeeToDelete);
        model.addEmployee(sameNameEmployee);
        addressBookStorage.saveAddressBook(model.getAddressBook());

        CommandResult result = logic.execute("delete id/emp-0042");

        assertEquals("Deleted employee: ID: EMP-0042; Name: Amy Bee; Phone: 85355255; "
                + "Email: amy@gmail.com; Department: Engineering; Role: Software Engineer", result.getFeedbackToUser());
        assertEquals(List.of(sameNameEmployee), model.getAddressBook().getEmployeeList());
        ReadOnlyAddressBook savedAddressBook = addressBookStorage.readAddressBook().orElseThrow();
        assertEquals(List.of(sameNameEmployee), savedAddressBook.getEmployeeList());
        assertEquals("aBc_0043", savedAddressBook.getEmployeeList().get(0).getId().value);
    }

    @Test
    public void execute_hiddenEmployee_preservesFilterAndPersistsDeletion() throws Exception {
        model.updateFilteredEmployeeList(new NameContainsKeywordsPredicate(List.of("Alice")));
        addressBookStorage.saveAddressBook(model.getAddressBook());
        AddressBook expectedAddressBook = new AddressBook(model.getAddressBook());
        expectedAddressBook.removeEmployee(BENSON);

        CommandResult result = logic.execute("delete id/e0002");

        assertEquals(String.format(DeleteCommand.MESSAGE_DELETE_EMPLOYEE_SUCCESS, Messages.format(BENSON)),
                result.getFeedbackToUser());
        assertEquals(expectedAddressBook, model.getAddressBook());
        assertEquals(List.of(ALICE), model.getFilteredEmployeeList());
        assertEquals(expectedAddressBook, addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_unknownId_preservesMemoryAndStoredEmployees() throws Exception {
        model.updateFilteredEmployeeList(new NameContainsKeywordsPredicate(List.of("Alice")));
        AddressBook expectedAddressBook = new AddressBook(model.getAddressBook());
        addressBookStorage.saveAddressBook(model.getAddressBook());
        String savedData = Files.readString(addressBookStorage.getAddressBookFilePath());

        assertThrows(CommandException.class, "No employee with ID abc was found.", () ->
                logic.execute("delete id/abc"));

        assertEquals(expectedAddressBook, model.getAddressBook());
        assertEquals(List.of(ALICE), model.getFilteredEmployeeList());
        assertEquals(savedData, Files.readString(addressBookStorage.getAddressBookFilePath()));
    }
}

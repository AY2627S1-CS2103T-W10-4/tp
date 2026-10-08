package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.BENSON;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.NameContainsKeywordsPredicate;
import seedu.address.testutil.EmployeeBuilder;

public class DeleteCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullId_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeleteCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        DeleteCommand deleteCommand = new DeleteCommand(ALICE.getId());
        assertThrows(NullPointerException.class, () -> deleteCommand.execute(null));
    }

    @Test
    public void execute_validIdUnfilteredList_success() {
        DeleteCommand deleteCommand = new DeleteCommand(ALICE.getId());
        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_EMPLOYEE_SUCCESS, Messages.format(ALICE));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deleteEmployee(ALICE);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_customId_success() {
        String[] validIds = {"E0001", "EMP-0042", "abc", "e0123", "2024-017", "EMP_0042", "A", "12345678901234567890"};
        for (String id : validIds) {
            Model customModel = new ModelManager();
            Employee employee = new EmployeeBuilder().withId(id).build();
            customModel.addEmployee(employee);
            DeleteCommand deleteCommand = new DeleteCommand(new EmployeeId(id));
            String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_EMPLOYEE_SUCCESS,
                    Messages.format(employee));

            assertCommandSuccess(deleteCommand, customModel, expectedMessage, new ModelManager());
        }
    }

    @Test
    public void execute_lowerCaseId_matchesUpperCaseEmployee() {
        Employee employee = new EmployeeBuilder().withId("E0123").build();
        Model customModel = new ModelManager();
        customModel.addEmployee(employee);
        DeleteCommand deleteCommand = new DeleteCommand(new EmployeeId("e0123"));
        String expectedMessage = "Deleted employee: ID: E0123; Name: Amy Bee; Phone: 85355255; "
                + "Email: amy@gmail.com; Department: Engineering; Role: Software Engineer";

        assertCommandSuccess(deleteCommand, customModel, expectedMessage, new ModelManager());
    }

    @Test
    public void execute_upperCaseId_matchesLowerCaseEmployeeAndPreservesDisplayedId() {
        Employee employee = new EmployeeBuilder().withId("abc").build();
        Model customModel = new ModelManager();
        customModel.addEmployee(employee);
        DeleteCommand deleteCommand = new DeleteCommand(new EmployeeId("ABC"));
        String expectedMessage = "Deleted employee: ID: abc; Name: Amy Bee; Phone: 85355255; "
                + "Email: amy@gmail.com; Department: Engineering; Role: Software Engineer";

        assertCommandSuccess(deleteCommand, customModel, expectedMessage, new ModelManager());
    }

    @Test
    public void execute_sameNameDifferentIds_deletesOnlyTargetEmployee() {
        Employee sameNameEmployee = new EmployeeBuilder(ALICE).withId("EMP-0042").build();
        model.addEmployee(sameNameEmployee);
        DeleteCommand deleteCommand = new DeleteCommand(sameNameEmployee.getId());
        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_EMPLOYEE_SUCCESS,
                Messages.format(sameNameEmployee));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deleteEmployee(sameNameEmployee);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
        assertTrue(model.hasEmployee(ALICE));
        assertFalse(model.hasEmployee(sameNameEmployee));
    }

    @Test
    public void execute_visibleEmployee_preservesFilter() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("Alice"));
        model.updateFilteredEmployeeList(predicate);
        DeleteCommand deleteCommand = new DeleteCommand(ALICE.getId());
        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_EMPLOYEE_SUCCESS, Messages.format(ALICE));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deleteEmployee(ALICE);
        expectedModel.updateFilteredEmployeeList(predicate);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
        assertTrue(model.getFilteredEmployeeList().isEmpty());
        assertFalse(model.getAddressBook().getEmployeeList().isEmpty());
    }

    @Test
    public void execute_hiddenEmployee_deletesEmployeeAndPreservesFilter() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("Alice"));
        model.updateFilteredEmployeeList(predicate);
        assertFalse(model.getFilteredEmployeeList().contains(BENSON));
        DeleteCommand deleteCommand = new DeleteCommand(BENSON.getId());
        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_EMPLOYEE_SUCCESS, Messages.format(BENSON));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deleteEmployee(BENSON);
        expectedModel.updateFilteredEmployeeList(predicate);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
        assertEquals(List.of(ALICE), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_unknownId_throwsCommandExceptionWithoutChangingModel() {
        DeleteCommand deleteCommand = new DeleteCommand(new EmployeeId("emp-missing"));
        assertCommandFailure(deleteCommand, model, "No employee with ID emp-missing was found.");
    }

    @Test
    public void execute_unknownIdFilteredList_preservesEmployeesAndFilter() {
        model.updateFilteredEmployeeList(new NameContainsKeywordsPredicate(List.of("Alice")));
        DeleteCommand deleteCommand = new DeleteCommand(new EmployeeId("abc"));
        assertCommandFailure(deleteCommand, model, "No employee with ID abc was found.");
    }

    @Test
    public void execute_emptyAddressBook_throwsCommandException() {
        DeleteCommand deleteCommand = new DeleteCommand(new EmployeeId("EMP-0042"));
        assertCommandFailure(deleteCommand, new ModelManager(), "No employee with ID EMP-0042 was found.");
    }

    @Test
    public void getTargetId_mixedCaseId_preservesIdAsTyped() {
        DeleteCommand deleteCommand = new DeleteCommand(new EmployeeId("eMp-0042"));
        assertEquals("eMp-0042", deleteCommand.getTargetId().value);
    }

    @Test
    public void equals() {
        DeleteCommand deleteFirstCommand = new DeleteCommand(new EmployeeId("E0001"));
        DeleteCommand deleteSecondCommand = new DeleteCommand(new EmployeeId("E0002"));

        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));
        assertTrue(deleteFirstCommand.equals(new DeleteCommand(new EmployeeId("E0001"))));
        assertTrue(deleteFirstCommand.equals(new DeleteCommand(new EmployeeId("e0001"))));
        assertFalse(deleteFirstCommand.equals(1));
        assertFalse(deleteFirstCommand.equals(null));
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));
    }

    @Test
    public void toStringMethod() {
        EmployeeId targetId = new EmployeeId("EMP-0042");
        DeleteCommand deleteCommand = new DeleteCommand(targetId);
        String expected = DeleteCommand.class.getCanonicalName() + "{targetId=" + targetId + "}";
        assertEquals(expected, deleteCommand.toString());
    }
}

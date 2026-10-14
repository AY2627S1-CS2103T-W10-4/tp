package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showEmployeeAtIndex;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.BENSON;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_EMPLOYEE;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditEmployeeDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.testutil.EditEmployeeDescriptorBuilder;
import seedu.address.testutil.EmployeeBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        // the employee ID cannot be edited, so the edited employee keeps the original ID
        EmployeeId targetId = model.getFilteredEmployeeList().get(0).getId();
        Employee editedEmployee = new EmployeeBuilder().withId(targetId.value).build();
        EditEmployeeDescriptor descriptor = new EditEmployeeDescriptorBuilder(editedEmployee).build();
        EditCommand editCommand = new EditCommand(targetId, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_EMPLOYEE_SUCCESS,
                Messages.format(editedEmployee));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setEmployee(model.getFilteredEmployeeList().get(0), editedEmployee);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Employee lastEmployee = model.getFilteredEmployeeList().get(model.getFilteredEmployeeList().size() - 1);

        EmployeeBuilder employeeInList = new EmployeeBuilder(lastEmployee);
        Employee editedEmployee = employeeInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB).build();

        EditEmployeeDescriptor descriptor = new EditEmployeeDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).build();
        EditCommand editCommand = new EditCommand(lastEmployee.getId(), descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_EMPLOYEE_SUCCESS,
                Messages.format(editedEmployee));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setEmployee(lastEmployee, editedEmployee);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        Employee editedEmployee = model.getFilteredEmployeeList().get(0);
        EditCommand editCommand = new EditCommand(editedEmployee.getId(), new EditEmployeeDescriptor());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_EMPLOYEE_SUCCESS,
                Messages.format(editedEmployee));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        showEmployeeAtIndex(model, INDEX_FIRST_EMPLOYEE);

        Employee employeeInFilteredList = model.getFilteredEmployeeList().get(INDEX_FIRST_EMPLOYEE.getZeroBased());
        Employee editedEmployee = new EmployeeBuilder(employeeInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(employeeInFilteredList.getId(),
                new EditEmployeeDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_EMPLOYEE_SUCCESS,
                Messages.format(editedEmployee));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setEmployee(model.getFilteredEmployeeList().get(0), editedEmployee);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_idInDifferentCase_success() {
        Employee firstEmployee = model.getFilteredEmployeeList().get(0);
        EmployeeId differentCaseId = new EmployeeId(firstEmployee.getId().value.toLowerCase());
        Employee editedEmployee = new EmployeeBuilder(firstEmployee).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(differentCaseId,
                new EditEmployeeDescriptorBuilder().withName(VALID_NAME_BOB).build());

        // the stored id is kept, not the id as typed
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_EMPLOYEE_SUCCESS,
                Messages.format(editedEmployee));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setEmployee(firstEmployee, editedEmployee);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_employeeIdNotFound_failure() {
        EmployeeId missingId = new EmployeeId("E9999");
        EditEmployeeDescriptor descriptor = new EditEmployeeDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(missingId, descriptor);

        assertCommandFailure(editCommand, model, String.format(Messages.MESSAGE_EMPLOYEE_NOT_FOUND, missingId));
    }

    /**
     * Edits an employee that is in the address book but hidden by the current filter.
     */
    @Test
    public void execute_employeeHiddenByFilter_success() {
        showEmployeeAtIndex(model, INDEX_FIRST_EMPLOYEE);
        Employee hiddenEmployee = model.getAddressBook().getEmployeeList().get(1);
        // ensures that the employee is not in the filtered list
        assertFalse(model.getFilteredEmployeeList().contains(hiddenEmployee));

        Employee editedEmployee = new EmployeeBuilder(hiddenEmployee).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(hiddenEmployee.getId(),
                new EditEmployeeDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_EMPLOYEE_SUCCESS,
                Messages.format(editedEmployee));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setEmployee(hiddenEmployee, editedEmployee);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(ALICE.getId(), DESC_AMY);

        // same values -> returns true
        EditEmployeeDescriptor copyDescriptor = new EditEmployeeDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(ALICE.getId(), copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different id -> returns false
        assertFalse(standardCommand.equals(new EditCommand(BENSON.getId(), DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(ALICE.getId(), DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        EmployeeId targetId = ALICE.getId();
        EditEmployeeDescriptor editEmployeeDescriptor = new EditEmployeeDescriptor();
        EditCommand editCommand = new EditCommand(targetId, editEmployeeDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{targetId=" + targetId
                + ", editEmployeeDescriptor=" + editEmployeeDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }

}

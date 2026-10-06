package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showEmployeeAtIndex;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_EMPLOYEE;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_EMPLOYEE;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.Remark;
import seedu.address.testutil.EmployeeBuilder;

public class RemarkCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_addRemarkUnfilteredList_success() {
        Remark remark = new Remark("Likes to swim");
        Employee employeeToEdit = model.getFilteredEmployeeList().get(INDEX_FIRST_EMPLOYEE.getZeroBased());
        Employee editedEmployee = new EmployeeBuilder(employeeToEdit).withRemark(remark.value).build();
        RemarkCommand remarkCommand = new RemarkCommand(INDEX_FIRST_EMPLOYEE, remark);

        String expectedMessage = String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS,
                Messages.format(editedEmployee));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setEmployee(employeeToEdit, editedEmployee);

        assertCommandSuccess(remarkCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_removeRemarkUnfilteredList_success() {
        Employee employeeWithRemark = new EmployeeBuilder(
                model.getFilteredEmployeeList().get(INDEX_FIRST_EMPLOYEE.getZeroBased()))
                .withRemark("Likes to swim").build();
        model.setEmployee(model.getFilteredEmployeeList().get(INDEX_FIRST_EMPLOYEE.getZeroBased()), employeeWithRemark);

        Remark emptyRemark = new Remark("");
        Employee editedEmployee = new EmployeeBuilder(employeeWithRemark).withRemark("").build();
        RemarkCommand remarkCommand = new RemarkCommand(INDEX_FIRST_EMPLOYEE, emptyRemark);

        String expectedMessage = String.format(RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS,
                Messages.format(editedEmployee));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setEmployee(employeeWithRemark, editedEmployee);

        assertCommandSuccess(remarkCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredEmployeeList().size() + 1);
        RemarkCommand remarkCommand = new RemarkCommand(outOfBoundIndex, new Remark("Likes to swim"));

        assertCommandFailure(remarkCommand, model, Messages.MESSAGE_INVALID_EMPLOYEE_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showEmployeeAtIndex(model, INDEX_FIRST_EMPLOYEE);
        Employee employeeToEdit = model.getFilteredEmployeeList().get(INDEX_FIRST_EMPLOYEE.getZeroBased());
        Employee editedEmployee = new EmployeeBuilder(employeeToEdit).withRemark("Likes to swim").build();
        RemarkCommand remarkCommand = new RemarkCommand(INDEX_FIRST_EMPLOYEE, new Remark("Likes to swim"));

        String expectedMessage = String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS,
                Messages.format(editedEmployee));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setEmployee(employeeToEdit, editedEmployee);

        assertCommandSuccess(remarkCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showEmployeeAtIndex(model, INDEX_FIRST_EMPLOYEE);
        RemarkCommand remarkCommand = new RemarkCommand(INDEX_SECOND_EMPLOYEE, new Remark("Likes to swim"));

        assertCommandFailure(remarkCommand, model, Messages.MESSAGE_INVALID_EMPLOYEE_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        RemarkCommand firstCommand = new RemarkCommand(INDEX_FIRST_EMPLOYEE, new Remark("Likes to swim"));
        RemarkCommand firstCommandCopy = new RemarkCommand(INDEX_FIRST_EMPLOYEE, new Remark("Likes to swim"));
        RemarkCommand secondIndexCommand = new RemarkCommand(INDEX_SECOND_EMPLOYEE, new Remark("Likes to swim"));
        RemarkCommand differentRemarkCommand = new RemarkCommand(INDEX_FIRST_EMPLOYEE, new Remark("Likes to run"));

        assertTrue(firstCommand.equals(firstCommand));
        assertTrue(firstCommand.equals(firstCommandCopy));
        assertFalse(firstCommand.equals(secondIndexCommand));
        assertFalse(firstCommand.equals(differentRemarkCommand));
        assertFalse(firstCommand.equals(null));
        assertFalse(firstCommand.equals(1));
    }

    @Test
    public void toStringMethod() {
        Remark remark = new Remark("Likes to swim");
        RemarkCommand remarkCommand = new RemarkCommand(INDEX_FIRST_EMPLOYEE, remark);
        String expected = RemarkCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_EMPLOYEE
                + ", remark=" + remark + "}";
        assertEquals(expected, remarkCommand.toString());
    }
}

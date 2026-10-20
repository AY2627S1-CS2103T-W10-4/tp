package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.getEmployeesListedOverview;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalEmployees.BENSON;
import static seedu.address.testutil.TypicalEmployees.CARL;
import static seedu.address.testutil.TypicalEmployees.FIONA;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.EmployeeMatchesCriteriaPredicate;

/**
 * Contains integration tests (interaction with the Model) for {@code FilterCommand}.
 */
public class FilterCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private static EmployeeMatchesCriteriaPredicate predicateOf(List<String> departmentWords,
            List<String> roleWords) {
        return new EmployeeMatchesCriteriaPredicate(departmentWords, roleWords);
    }

    @Test
    public void equals() {
        FilterCommand filterFirstCommand = new FilterCommand(predicateOf(List.of("first"), List.of()));
        FilterCommand filterSecondCommand = new FilterCommand(predicateOf(List.of("second"), List.of()));

        // same object -> returns true
        assertTrue(filterFirstCommand.equals(filterFirstCommand));

        // same values -> returns true
        assertTrue(filterFirstCommand.equals(new FilterCommand(predicateOf(List.of("first"), List.of()))));

        // different types -> returns false
        assertFalse(filterFirstCommand.equals(1));

        // null -> returns false
        assertFalse(filterFirstCommand.equals(null));

        // different predicate -> returns false
        assertFalse(filterFirstCommand.equals(filterSecondCommand));
    }

    @Test
    public void execute_departmentOnly_oneEmployeeFound() {
        EmployeeMatchesCriteriaPredicate predicate = predicateOf(List.of("Finance"), List.of());
        expectedModel.updateFilteredEmployeeList(predicate);
        assertCommandSuccess(new FilterCommand(predicate), model, getEmployeesListedOverview(1), expectedModel);
        assertEquals(List.of(CARL), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_roleOnly_multipleEmployeesFound() {
        EmployeeMatchesCriteriaPredicate predicate = predicateOf(List.of(), List.of("Executive"));
        expectedModel.updateFilteredEmployeeList(predicate);
        assertCommandSuccess(new FilterCommand(predicate), model, getEmployeesListedOverview(2), expectedModel);
        assertEquals(List.of(BENSON, FIONA), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_departmentAndRole_onlyEmployeesMatchingBothFound() {
        EmployeeMatchesCriteriaPredicate predicate = predicateOf(List.of("Marketing"), List.of("Executive"));
        expectedModel.updateFilteredEmployeeList(predicate);
        assertCommandSuccess(new FilterCommand(predicate), model, getEmployeesListedOverview(1), expectedModel);
        assertEquals(List.of(FIONA), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_multipleWordDepartment_employeeFound() {
        EmployeeMatchesCriteriaPredicate predicate = predicateOf(List.of("Human", "Resources"), List.of());
        expectedModel.updateFilteredEmployeeList(predicate);
        assertCommandSuccess(new FilterCommand(predicate), model, getEmployeesListedOverview(1), expectedModel);
        assertEquals(List.of(BENSON), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_noMatch_noEmployeeFound() {
        EmployeeMatchesCriteriaPredicate predicate = predicateOf(List.of("Sales"), List.of("Executive"));
        expectedModel.updateFilteredEmployeeList(predicate);
        assertCommandSuccess(new FilterCommand(predicate), model, getEmployeesListedOverview(0), expectedModel);
        assertEquals(List.of(), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_filter_doesNotChangeStoredData() throws Exception {
        new FilterCommand(predicateOf(List.of("Finance"), List.of())).execute(model);
        assertEquals(getTypicalAddressBook(), model.getAddressBook());
    }

    @Test
    public void execute_listAfterFilter_showsEveryEmployeeAgain() throws Exception {
        new FilterCommand(predicateOf(List.of("Finance"), List.of())).execute(model);
        assertEquals(1, model.getFilteredEmployeeList().size());

        new ListCommand().execute(model);
        assertEquals(getTypicalAddressBook().getEmployeeList(), model.getFilteredEmployeeList());
    }

    @Test
    public void toStringMethod() {
        EmployeeMatchesCriteriaPredicate predicate = predicateOf(List.of("Sales"), List.of());
        FilterCommand filterCommand = new FilterCommand(predicate);
        String expected = FilterCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, filterCommand.toString());
    }
}

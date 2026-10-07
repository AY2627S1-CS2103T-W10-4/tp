package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.BENSON;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.testutil.EmployeeBuilder;

public class DeleteCommandTest {
    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_existingId_deletesEmployeeAndReportsAllFields() {
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        expected.deleteEmployee(ALICE);
        assertCommandSuccess(new DeleteCommand(ALICE.getId()), model,
                "Deleted employee: ID: E0001; Name: Alice Pauline; Phone: 94351253; Email: alice@example.com; "
                        + "Department: Engineering; Role: Software Engineer", expected);
    }

    @Test
    public void execute_visibleTarget_preservesFilterAndOrder() throws Exception {
        model.updateFilteredEmployeeList(employee -> employee.equals(ALICE) || employee.equals(BENSON));
        new DeleteCommand(ALICE.getId()).execute(model);
        assertEquals(java.util.List.of(BENSON), model.getFilteredEmployeeList());
        AddressBook expected = getTypicalAddressBook();
        expected.removeEmployee(ALICE);
        assertEquals(expected, model.getAddressBook());
    }

    @Test
    public void execute_hiddenTarget_preservesVisibleResults() throws Exception {
        model.updateFilteredEmployeeList(employee -> employee.equals(BENSON));
        new DeleteCommand(ALICE.getId()).execute(model);
        assertEquals(java.util.List.of(BENSON), model.getFilteredEmployeeList());
        assertFalse(model.hasEmployee(ALICE));
    }

    @Test
    public void execute_emptySearch_deletesHiddenTarget() throws Exception {
        model.updateFilteredEmployeeList(employee -> false);
        new DeleteCommand(ALICE.getId()).execute(model);
        assertTrue(model.getFilteredEmployeeList().isEmpty());
        assertFalse(model.hasEmployee(ALICE));
    }

    @Test
    public void execute_sameNameDifferentId_deletesOnlyRequestedId() throws Exception {
        Employee namesake = new EmployeeBuilder(ALICE).withId("E9999").build();
        model.addEmployee(namesake);
        new DeleteCommand(ALICE.getId()).execute(model);
        assertTrue(model.hasEmployee(namesake));
        assertFalse(model.hasEmployee(ALICE));
    }

    @Test
    public void execute_missingId_leavesModelUnchanged() {
        model.updateFilteredEmployeeList(employee -> employee.equals(BENSON));
        assertCommandFailure(new DeleteCommand(new EmployeeId("E9999")), model,
                "No employee with ID E9999 was found.");
    }

    @Test
    public void execute_lastEmployeeAndRepeatedDeletion_leavesEmptyRoster() throws Exception {
        model = new ModelManager();
        model.addEmployee(ALICE);
        new DeleteCommand(ALICE.getId()).execute(model);
        assertTrue(model.getAddressBook().getEmployeeList().isEmpty());
        assertCommandFailure(new DeleteCommand(ALICE.getId()), model, "No employee with ID E0001 was found.");
    }

    @Test
    public void execute_deletedId_canBeReused() throws Exception {
        new DeleteCommand(ALICE.getId()).execute(model);
        Employee replacement = new EmployeeBuilder(ALICE).withName("Replacement Employee").build();
        model.addEmployee(replacement);
        assertTrue(model.hasEmployee(replacement));
    }

    @Test
    public void equals() {
        DeleteCommand first = new DeleteCommand(ALICE.getId());
        assertTrue(first.equals(first));
        assertTrue(first.equals(new DeleteCommand(new EmployeeId("E0001"))));
        assertFalse(first.equals(new DeleteCommand(BENSON.getId())));
        assertFalse(first.equals(null));
        assertFalse(first.equals(1));
    }

    @Test
    public void toStringMethod() {
        assertEquals(DeleteCommand.class.getCanonicalName() + "{targetId=E0001}",
                new DeleteCommand(ALICE.getId()).toString());
    }
}

package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.BENSON;
import static seedu.address.testutil.TypicalEmployees.CARL;
import static seedu.address.testutil.TypicalEmployees.DANIEL;
import static seedu.address.testutil.TypicalEmployees.ELLE;
import static seedu.address.testutil.TypicalEmployees.FIONA;
import static seedu.address.testutil.TypicalEmployees.GEORGE;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SortCommand.SortField;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.NameContainsKeywordsPredicate;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.EmployeeBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code SortCommand}.
 */
public class SortCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private void assertSortsTo(SortField field, List<Employee> expectedOrder) {
        expectedModel.updateEmployeeSortOrder(field.getComparator());
        assertCommandSuccess(new SortCommand(field), model,
                String.format(SortCommand.MESSAGE_SUCCESS, field), expectedModel);
        assertEquals(expectedOrder, model.getFilteredEmployeeList());
    }

    @Test
    public void equals() {
        SortCommand sortByName = new SortCommand(SortField.NAME);

        // same object -> returns true
        assertTrue(sortByName.equals(sortByName));

        // same value -> returns true
        assertTrue(sortByName.equals(new SortCommand(SortField.NAME)));

        // different types -> returns false
        assertFalse(sortByName.equals(1));

        // null -> returns false
        assertFalse(sortByName.equals(null));

        // different field -> returns false
        assertFalse(sortByName.equals(new SortCommand(SortField.ROLE)));
    }

    @Test
    public void execute_sortById() {
        assertSortsTo(SortField.ID, List.of(ALICE, BENSON, CARL, DANIEL, ELLE, FIONA, GEORGE));
    }

    @Test
    public void execute_sortByName() {
        assertSortsTo(SortField.NAME, List.of(ALICE, BENSON, CARL, DANIEL, ELLE, FIONA, GEORGE));
    }

    @Test
    public void execute_sortByDepartment() {
        // Engineering, Finance, Human Resources, Legal, Marketing, Operations, Sales
        assertSortsTo(SortField.DEPARTMENT, List.of(ALICE, CARL, BENSON, GEORGE, FIONA, ELLE, DANIEL));
    }

    @Test
    public void execute_sortByRole() {
        // Account Manager, Accountant, HR Executive, Legal Counsel, Marketing Executive,
        // Operations Analyst, Software Engineer
        assertSortsTo(SortField.ROLE, List.of(DANIEL, CARL, BENSON, GEORGE, FIONA, ELLE, ALICE));
    }

    @Test
    public void execute_sortIgnoresLetterCaseAndBreaksTiesById() {
        Employee zed = new EmployeeBuilder().withId("E3").withName("Zed").withDepartment("Sales").build();
        Employee lowerCaseAlice = new EmployeeBuilder().withId("E1").withName("alice").withDepartment("sales")
                .build();
        Employee bob = new EmployeeBuilder().withId("E2").withName("Bob").withDepartment("Sales").build();
        Model unsortedModel = new ModelManager(
                new AddressBookBuilder().withEmployee(zed).withEmployee(lowerCaseAlice).withEmployee(bob).build(),
                new UserPrefs());

        new SortCommand(SortField.NAME).execute(unsortedModel);
        assertEquals(List.of(lowerCaseAlice, bob, zed), unsortedModel.getFilteredEmployeeList());

        // all three have the same department when the case is ignored, so the employee ID decides
        new SortCommand(SortField.DEPARTMENT).execute(unsortedModel);
        assertEquals(List.of(lowerCaseAlice, bob, zed), unsortedModel.getFilteredEmployeeList());
    }

    @Test
    public void execute_sort_doesNotChangeStoredData() {
        new SortCommand(SortField.ROLE).execute(model);
        assertEquals(getTypicalAddressBook(), model.getAddressBook());
        assertEquals(getTypicalAddressBook().getEmployeeList(), model.getAddressBook().getEmployeeList());
    }

    @Test
    public void execute_listAfterSort_restoresSavedOrder() {
        new SortCommand(SortField.ROLE).execute(model);
        assertFalse(getTypicalAddressBook().getEmployeeList().equals(model.getFilteredEmployeeList()));

        new ListCommand().execute(model);
        assertEquals(getTypicalAddressBook().getEmployeeList(), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_sortAfterFind_sortsOnlyTheFoundEmployees() {
        // Benson is stored before Daniel
        model.updateFilteredEmployeeList(new NameContainsKeywordsPredicate(List.of("Benson", "Daniel")));

        new SortCommand(SortField.DEPARTMENT).execute(model);
        // Human Resources (Benson) comes before Sales (Daniel)
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredEmployeeList());

        new SortCommand(SortField.ROLE).execute(model);
        // Account Manager (Daniel) comes before HR Executive (Benson)
        assertEquals(List.of(DANIEL, BENSON), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_emptyEmployeeList_succeeds() {
        Model emptyModel = new ModelManager(new AddressBook(), new UserPrefs());
        Model expectedEmptyModel = new ModelManager(new AddressBook(), new UserPrefs());
        expectedEmptyModel.updateEmployeeSortOrder(SortField.NAME.getComparator());

        assertCommandSuccess(new SortCommand(SortField.NAME), emptyModel,
                String.format(SortCommand.MESSAGE_SUCCESS, SortField.NAME), expectedEmptyModel);
    }

    @Test
    public void sortField_fromString_ignoresCaseAndSurroundingSpaces() {
        assertEquals(Optional.of(SortField.DEPARTMENT), SortField.fromString("department"));
        assertEquals(Optional.of(SortField.DEPARTMENT), SortField.fromString("  DePaRtMeNt "));
        assertEquals(Optional.empty(), SortField.fromString("salary"));
        assertEquals(Optional.empty(), SortField.fromString("name asc"));
        assertEquals("id, name, department, role", SortField.names());
    }

    @Test
    public void toStringMethod() {
        assertEquals(SortCommand.class.getCanonicalName() + "{field=name}",
                new SortCommand(SortField.NAME).toString());
    }
}

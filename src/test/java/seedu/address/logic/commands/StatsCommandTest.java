package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.NameContainsKeywordsPredicate;
import seedu.address.testutil.EmployeeBuilder;

public class StatsCommandTest {

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StatsCommand().execute(null));
    }

    @Test
    public void execute_emptyRoster_showsZeroAndEmptyBreakdowns() {
        assertEquals("Total active employees: 0\nBy department:\n(none)\nBy role:\n(none)",
                new StatsCommand().execute(new ModelManager()).getFeedbackToUser());
    }

    @Test
    public void execute_countsAndTies_sortedByDescendingCountThenLabel() {
        Model model = modelWith(
                employee("E3", "Ops", "Engineer"),
                employee("E2", "Sales", "Manager"),
                employee("E1", "Ops", "Manager"),
                employee("E4", "Finance", "Analyst"));

        assertEquals("Total active employees: 4\nBy department:\nOps: 2\nFinance: 1\nSales: 1"
                + "\nBy role:\nManager: 2\nAnalyst: 1\nEngineer: 1",
                new StatsCommand().execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_singleEmployee_showsOneInEachBreakdown() {
        Model model = modelWith(employee("E1", "Ops", "Manager"));
        assertEquals("Total active employees: 1\nBy department:\nOps: 1\nBy role:\nManager: 1",
                new StatsCommand().execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_searchAndArchiveView_usesAllActiveEmployeesAndPreservesModel() {
        Employee alice = new EmployeeBuilder().withId("E1").withName("Alice").withDepartment("Ops").build();
        Employee bob = new EmployeeBuilder().withId("E2").withName("Bob").withDepartment("Sales").build();
        Employee archived = new EmployeeBuilder().withId("E3").withDepartment("Finance")
                .withArchived(true).build();
        Model model = modelWith(alice, bob, archived);
        AddressBook before = new AddressBook(model.getAddressBook());
        model.updateFilteredEmployeeList(new NameContainsKeywordsPredicate(List.of("Alice")));

        String expected = "Total active employees: 2\nBy department:\nOps: 1\nSales: 1"
                + "\nBy role:\nSoftware Engineer: 2";
        assertEquals(expected, new StatsCommand().execute(model).getFeedbackToUser());
        assertEquals(List.of(alice), model.getFilteredEmployeeList());

        model.showArchivedEmployees();
        assertEquals(expected, new StatsCommand().execute(model).getFeedbackToUser());
        assertEquals(List.of(archived), model.getFilteredEmployeeList());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_onlyArchivedEmployees_showsZero() {
        Model model = modelWith(employee("E1", "Ops", "Manager").withArchived(true));
        assertEquals(new StatsCommand().execute(new ModelManager()).getFeedbackToUser(),
                new StatsCommand().execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_differentlyCasedLabels_keepsStoredGroupsDistinct() {
        Model model = modelWith(employee("E1", "Ops", "Manager"), employee("E2", "ops", "manager"));
        assertEquals("Total active employees: 2\nBy department:\nOps: 1\nops: 1"
                + "\nBy role:\nManager: 1\nmanager: 1", new StatsCommand().execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_tiesWithMixedCase_sortedAlphabeticallyIgnoringCase() {
        Model model = modelWith(employee("E1", "Human Resources", "Zebra"), employee("E2", "apple", "analyst"),
                employee("E3", "Banana", "Manager"));
        assertEquals("Total active employees: 3\nBy department:\napple: 1\nBanana: 1\nHuman Resources: 1"
                + "\nBy role:\nanalyst: 1\nManager: 1\nZebra: 1",
                new StatsCommand().execute(model).getFeedbackToUser());
    }

    private Employee employee(String id, String department, String role) {
        return new EmployeeBuilder().withId(id).withDepartment(department).withRole(role).build();
    }

    private Model modelWith(Employee... employees) {
        AddressBook addressBook = new AddressBook();
        addressBook.setEmployees(List.of(employees));
        return new ModelManager(addressBook, new UserPrefs());
    }
}

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

public class CheckCommandTest {

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CheckCommand().execute(null));
    }

    @Test
    public void execute_emptyRoster_reportsNoIssues() {
        assertNoIssues(new ModelManager());
    }

    @Test
    public void execute_uniqueContacts_reportsNoIssues() {
        assertNoIssues(modelWith(employee("E1", "Alice", "111", "alice@example.com"),
                employee("E2", "Bob", "222", "bob@example.com")));
    }

    @Test
    public void execute_singleEmployee_doesNotMatchItself() {
        assertNoIssues(modelWith(employee("E1", "Alice", "111", "alice@example.com")));
    }

    @Test
    public void execute_sharedPhone_reportsBothEmployeesInIdOrder() {
        Model model = modelWith(employee("e2", "Bob", "111", "bob@example.com"),
                employee("E1", "Alice", "111", "alice@example.com"),
                employee("E3", "Carol", "333", "carol@example.com"));
        assertEquals("Found 2 active employee(s) with data-quality issues:\n"
                + "E1 (Alice): shared phone 111\ne2 (Bob): shared phone 111",
                new CheckCommand().execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_sharedEmail_reportsBothEmployees() {
        Model model = modelWith(employee("E1", "Alice", "111", "shared@example.com"),
                employee("E2", "Bob", "222", "shared@example.com"));
        assertEquals("Found 2 active employee(s) with data-quality issues:\n"
                + "E1 (Alice): shared email shared@example.com\nE2 (Bob): shared email shared@example.com",
                new CheckCommand().execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_overlappingGroups_countsEachEmployeeOnceAndReportsBothReasons() {
        Model model = modelWith(employee("E3", "Carol", "333", "shared@example.com"),
                employee("E2", "Bob", "111", "bob@example.com"),
                employee("E1", "Alice", "111", "shared@example.com"));
        assertEquals("Found 3 active employee(s) with data-quality issues:\n"
                + "E1 (Alice): shared phone 111; shared email shared@example.com\n"
                + "E2 (Bob): shared phone 111\nE3 (Carol): shared email shared@example.com",
                new CheckCommand().execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_threeEmployeesSharingBothFields_countsEmployeesRatherThanPairs() {
        Model model = modelWith(employee("E1", "Alice", "111", "shared@example.com"),
                employee("E2", "Bob", "111", "shared@example.com"),
                employee("E3", "Carol", "111", "shared@example.com"));
        assertEquals("Found 3 active employee(s) with data-quality issues:\n"
                + "E1 (Alice): shared phone 111; shared email shared@example.com\n"
                + "E2 (Bob): shared phone 111; shared email shared@example.com\n"
                + "E3 (Carol): shared phone 111; shared email shared@example.com",
                new CheckCommand().execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_phonesDifferOnlyInFormatting_usesExactMatching() {
        assertNoIssues(modelWith(employee("E1", "Alice", "91234567", "alice@example.com"),
                employee("E2", "Bob", "9123 4567", "bob@example.com")));
    }

    @Test
    public void execute_emailsDifferOnlyInCase_reportsBothWithStoredEmails() {
        Model model = modelWith(employee("E1", "Alice", "111", "Alice@example.com"),
                employee("E2", "Bob", "222", "alice@EXAMPLE.com"));
        assertEquals("Found 2 active employee(s) with data-quality issues:\n"
                + "E1 (Alice): shared email Alice@example.com\nE2 (Bob): shared email alice@EXAMPLE.com",
                new CheckCommand().execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_contactSharedOnlyWithArchivedEmployee_reportsNoIssues() {
        Employee alice = employee("E1", "Alice", "111", "shared@example.com");
        Employee archivedBob = employee("E2", "Bob", "111", "shared@example.com").withArchived(true);
        assertNoIssues(modelWith(alice, archivedBob));
        assertNoIssues(modelWith(alice.withArchived(true), archivedBob));
    }

    @Test
    public void execute_searchAndArchiveView_checksFullActiveRosterWithoutChangingModel() {
        Employee alice = employee("E1", "Alice", "111", "alice@example.com");
        Employee bob = employee("E2", "Bob", "111", "bob@example.com");
        Employee archived = employee("E3", "Carol", "111", "alice@example.com").withArchived(true);
        Model model = modelWith(alice, bob, archived);
        AddressBook before = new AddressBook(model.getAddressBook());
        model.updateFilteredEmployeeList(new NameContainsKeywordsPredicate(List.of("Alice")));

        String expected = "Found 2 active employee(s) with data-quality issues:\n"
                + "E1 (Alice): shared phone 111\nE2 (Bob): shared phone 111";
        assertEquals(expected, new CheckCommand().execute(model).getFeedbackToUser());
        assertEquals(List.of(alice), model.getFilteredEmployeeList());

        model.showArchivedEmployees();
        assertEquals(expected, new CheckCommand().execute(model).getFeedbackToUser());
        assertEquals(List.of(archived), model.getFilteredEmployeeList());
        assertEquals(before, model.getAddressBook());
    }

    private void assertNoIssues(Model model) {
        assertEquals(CheckCommand.MESSAGE_NO_ISSUES, new CheckCommand().execute(model).getFeedbackToUser());
    }

    private Employee employee(String id, String name, String phone, String email) {
        return new EmployeeBuilder().withId(id).withName(name).withPhone(phone).withEmail(email).build();
    }

    private Model modelWith(Employee... employees) {
        AddressBook addressBook = new AddressBook();
        addressBook.setEmployees(List.of(employees));
        return new ModelManager(addressBook, new UserPrefs());
    }
}

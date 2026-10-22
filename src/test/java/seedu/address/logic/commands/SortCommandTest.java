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
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SortCommand.SortField;
import seedu.address.logic.commands.SortCommand.SortOrder;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeMatchesCriteriaPredicate;
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
        assertSortsTo(field, SortOrder.ASC, expectedOrder);
    }

    private void assertSortsTo(SortField field, SortOrder order, List<Employee> expectedOrder) {
        expectedModel.updateEmployeeSortOrder(field.getComparator(order));
        assertCommandSuccess(new SortCommand(field, order), model,
                String.format(SortCommand.MESSAGE_SUCCESS, field, order), expectedModel);
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

        // different order -> returns false
        assertFalse(sortByName.equals(new SortCommand(SortField.NAME, SortOrder.DESC)));

        // ascending is the default order -> returns true
        assertTrue(sortByName.equals(new SortCommand(SortField.NAME, SortOrder.ASC)));
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
    public void execute_sortDescending() {
        assertSortsTo(SortField.ID, SortOrder.DESC, List.of(GEORGE, FIONA, ELLE, DANIEL, CARL, BENSON, ALICE));
        assertSortsTo(SortField.NAME, SortOrder.DESC, List.of(GEORGE, FIONA, ELLE, DANIEL, CARL, BENSON, ALICE));
        assertSortsTo(SortField.DEPARTMENT, SortOrder.DESC,
                List.of(DANIEL, ELLE, FIONA, GEORGE, BENSON, CARL, ALICE));
        assertSortsTo(SortField.ROLE, SortOrder.DESC, List.of(ALICE, ELLE, FIONA, GEORGE, BENSON, CARL, DANIEL));
    }

    @Test
    public void execute_sortDescending_stillBreaksTiesByIdFromAToZ() {
        Employee zed = new EmployeeBuilder().withId("E3").withName("Zed").withDepartment("Sales").build();
        Employee alice = new EmployeeBuilder().withId("E1").withName("Alice").withDepartment("Sales").build();
        Employee bob = new EmployeeBuilder().withId("E2").withName("Bob").withDepartment("Sales").build();
        Employee finance = new EmployeeBuilder().withId("E4").withName("Cara").withDepartment("Finance").build();
        Model unsortedModel = new ModelManager(new AddressBookBuilder().withEmployee(zed).withEmployee(finance)
                .withEmployee(bob).withEmployee(alice).build(), new UserPrefs());

        new SortCommand(SortField.DEPARTMENT, SortOrder.DESC).execute(unsortedModel);
        assertEquals(List.of(alice, bob, zed, finance), unsortedModel.getFilteredEmployeeList());
    }

    @Test
    public void execute_sortByNameWithAccents_placesAccentedLettersWithTheirBaseLetter() {
        Employee adam = new EmployeeBuilder().withId("E1").withName("Adam Lim").build();
        Employee zoe = new EmployeeBuilder().withId("E2").withName("Zoe Tan").build();
        Employee emile = new EmployeeBuilder().withId("E3").withName("Émile Roux").build();
        Employee omer = new EmployeeBuilder().withId("E4").withName("Ömer Kaya").build();
        Model unsortedModel = new ModelManager(new AddressBookBuilder().withEmployee(adam).withEmployee(zoe)
                .withEmployee(emile).withEmployee(omer).build(), new UserPrefs());

        new SortCommand(SortField.NAME).execute(unsortedModel);
        assertEquals(List.of(adam, emile, omer, zoe), unsortedModel.getFilteredEmployeeList());

        new SortCommand(SortField.NAME, SortOrder.DESC).execute(unsortedModel);
        assertEquals(List.of(zoe, omer, emile, adam), unsortedModel.getFilteredEmployeeList());
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

    /** Returns a filter for the Finance, Sales and Marketing departments, which are Carl, Daniel and Fiona. */
    private static FilterCommand filterFinanceSalesAndMarketing() {
        return new FilterCommand(new EmployeeMatchesCriteriaPredicate(
                List.of(), List.of(), List.of("Finance", "Sales", "Marketing"), List.of()));
    }

    @Test
    public void execute_filterThenSort_sortsOnlyTheFilteredEmployees() {
        filterFinanceSalesAndMarketing().execute(model);
        // stored order
        assertEquals(List.of(CARL, DANIEL, FIONA), model.getFilteredEmployeeList());

        // Account Manager (Daniel), Accountant (Carl), Marketing Executive (Fiona)
        new SortCommand(SortField.ROLE).execute(model);
        assertEquals(List.of(DANIEL, CARL, FIONA), model.getFilteredEmployeeList());

        new SortCommand(SortField.NAME, SortOrder.DESC).execute(model);
        assertEquals(List.of(FIONA, DANIEL, CARL), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_sortThenFilter_keepsSortOrderOfTheFilteredEmployees() {
        new SortCommand(SortField.ROLE, SortOrder.DESC).execute(model);

        filterFinanceSalesAndMarketing().execute(model);
        // Marketing Executive (Fiona), Accountant (Carl), Account Manager (Daniel)
        assertEquals(List.of(FIONA, CARL, DANIEL), model.getFilteredEmployeeList());

        // a narrower filter keeps the order too
        new FilterCommand(new EmployeeMatchesCriteriaPredicate(
                List.of(), List.of(), List.of("Finance", "Marketing"), List.of())).execute(model);
        assertEquals(List.of(FIONA, CARL), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_sortAndFilterInEitherOrder_showTheSameEmployeesInTheSameOrder() {
        Model filterFirstModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        filterFinanceSalesAndMarketing().execute(filterFirstModel);
        new SortCommand(SortField.ID, SortOrder.DESC).execute(filterFirstModel);

        new SortCommand(SortField.ID, SortOrder.DESC).execute(model);
        filterFinanceSalesAndMarketing().execute(model);

        assertEquals(List.of(FIONA, DANIEL, CARL), filterFirstModel.getFilteredEmployeeList());
        assertEquals(filterFirstModel.getFilteredEmployeeList(), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_listAfterSortAndFilter_restoresAllEmployeesInSavedOrder() {
        new SortCommand(SortField.NAME, SortOrder.DESC).execute(model);
        filterFinanceSalesAndMarketing().execute(model);
        assertEquals(List.of(FIONA, DANIEL, CARL), model.getFilteredEmployeeList());

        new ListCommand().execute(model);
        assertEquals(getTypicalAddressBook().getEmployeeList(), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_filterAndSort_doNotChangeStoredData() {
        filterFinanceSalesAndMarketing().execute(model);
        new SortCommand(SortField.ROLE, SortOrder.DESC).execute(model);
        assertEquals(getTypicalAddressBook(), model.getAddressBook());
        assertEquals(getTypicalAddressBook().getEmployeeList(), model.getAddressBook().getEmployeeList());
    }

    @Test
    public void execute_editSortKeyUnderFilterAndSort_movesEmployeeWithinTheFilteredEmployees() {
        filterFinanceSalesAndMarketing().execute(model);
        new SortCommand(SortField.NAME).execute(model);
        assertEquals(List.of(CARL, DANIEL, FIONA), model.getFilteredEmployeeList());

        Employee renamedCarl = new EmployeeBuilder(CARL).withName("Zara Kurz").build();
        model.setEmployee(CARL, renamedCarl);
        assertEquals(List.of(DANIEL, FIONA, renamedCarl), model.getFilteredEmployeeList());

        // an edit that moves the employee out of the filtered departments removes them from the list
        model.setEmployee(DANIEL, new EmployeeBuilder(DANIEL).withDepartment("Legal").build());
        assertEquals(List.of(FIONA, renamedCarl), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_undoAndRedoAfterAnAddUnderSort_keepSortOrder() {
        new SortCommand(SortField.NAME, SortOrder.DESC).execute(model);
        // "Zed Zimmer" is after every typical name, so it is shown first when the order is descending
        Employee zed = new EmployeeBuilder().withId("E0099").withName("Zed Zimmer").build();

        model.addEmployee(zed);
        model.commitAddressBook();
        assertEquals(List.of(zed, GEORGE, FIONA, ELLE, DANIEL, CARL, BENSON, ALICE),
                model.getFilteredEmployeeList());

        model.undoAddressBook();
        assertEquals(List.of(GEORGE, FIONA, ELLE, DANIEL, CARL, BENSON, ALICE), model.getFilteredEmployeeList());

        model.redoAddressBook();
        assertEquals(List.of(zed, GEORGE, FIONA, ELLE, DANIEL, CARL, BENSON, ALICE),
                model.getFilteredEmployeeList());
    }

    @Test
    public void execute_undoAfterADeleteUnderFilterAndSort_restoresEmployeeInSortedPosition() {
        filterFinanceSalesAndMarketing().execute(model);
        new SortCommand(SortField.ROLE).execute(model);
        assertEquals(List.of(DANIEL, CARL, FIONA), model.getFilteredEmployeeList());

        model.deleteEmployee(CARL);
        model.commitAddressBook();
        assertEquals(List.of(DANIEL, FIONA), model.getFilteredEmployeeList());

        // Carl comes back between Daniel and Fiona, and the filter and the sort are both still applied
        model.undoAddressBook();
        assertEquals(List.of(DANIEL, CARL, FIONA), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_sortThenFind_keepsSortOrder() {
        new SortCommand(SortField.ROLE, SortOrder.DESC).execute(model);

        model.updateFilteredEmployeeList(new NameContainsKeywordsPredicate(List.of("Alice", "Benson", "Daniel")));
        // Software Engineer (Alice), HR Executive (Benson), Account Manager (Daniel)
        assertEquals(List.of(ALICE, BENSON, DANIEL), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_listAfterSortAndFind_restoresAllEmployeesInSavedOrder() {
        new SortCommand(SortField.ROLE).execute(model);
        model.updateFilteredEmployeeList(new NameContainsKeywordsPredicate(List.of("Alice", "Benson")));

        new ListCommand().execute(model);
        assertEquals(getTypicalAddressBook().getEmployeeList(), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_listArchivedAfterSort_restoresSavedOrder() {
        model.setEmployee(CARL, CARL.withArchived(true));
        model.setEmployee(FIONA, FIONA.withArchived(true));
        new SortCommand(SortField.NAME, SortOrder.DESC).execute(model);

        new ListCommand(true).execute(model);
        assertEquals(List.of(CARL.withArchived(true), FIONA.withArchived(true)), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_editSortKey_movesEmployeeToCorrectPosition() {
        new SortCommand(SortField.NAME).execute(model);
        Employee renamedAlice = new EmployeeBuilder(ALICE).withName("Zara Pauline").build();

        model.setEmployee(ALICE, renamedAlice);
        assertEquals(List.of(BENSON, CARL, DANIEL, ELLE, FIONA, GEORGE, renamedAlice),
                model.getFilteredEmployeeList());

        // moves back to the front when the name is changed back
        model.setEmployee(renamedAlice, ALICE);
        assertEquals(List.of(ALICE, BENSON, CARL, DANIEL, ELLE, FIONA, GEORGE), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_deleteEmployee_keepsOrderOfRemainingEmployees() {
        new SortCommand(SortField.ROLE, SortOrder.DESC).execute(model);

        model.deleteEmployee(GEORGE);
        assertEquals(List.of(ALICE, ELLE, FIONA, BENSON, CARL, DANIEL), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_emptyEmployeeList_succeeds() {
        Model emptyModel = new ModelManager(new AddressBook(), new UserPrefs());
        Model expectedEmptyModel = new ModelManager(new AddressBook(), new UserPrefs());
        expectedEmptyModel.updateEmployeeSortOrder(SortField.NAME.getComparator(SortOrder.ASC));

        assertCommandSuccess(new SortCommand(SortField.NAME), emptyModel,
                String.format(SortCommand.MESSAGE_SUCCESS, SortField.NAME, SortOrder.ASC), expectedEmptyModel);
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
    public void sortOrder_fromString_ignoresCaseAndSurroundingSpaces() {
        assertEquals(Optional.of(SortOrder.ASC), SortOrder.fromString("asc"));
        assertEquals(Optional.of(SortOrder.DESC), SortOrder.fromString("  DeSc "));
        assertEquals(Optional.empty(), SortOrder.fromString("ascending"));
        assertEquals(Optional.empty(), SortOrder.fromString(""));
        assertEquals("asc, desc", SortOrder.keywords());
    }

    @Test
    public void sortField_toString_isNotAffectedByDefaultLocale() {
        Locale defaultLocale = Locale.getDefault();
        try {
            // in Turkish, "I".toLowerCase() is a dotless i unless the locale is fixed
            Locale.setDefault(Locale.forLanguageTag("tr"));
            assertEquals("id", SortField.ID.toString());
            assertEquals("id, name, department, role", SortField.names());
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    @Test
    public void toStringMethod() {
        assertEquals(SortCommand.class.getCanonicalName() + "{field=name, order=ascending}",
                new SortCommand(SortField.NAME).toString());
        assertEquals(SortCommand.class.getCanonicalName() + "{field=role, order=descending}",
                new SortCommand(SortField.ROLE, SortOrder.DESC).toString());
    }
}

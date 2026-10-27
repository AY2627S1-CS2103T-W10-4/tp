package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showEmployeeAtIndex;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.BENSON;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_EMPLOYEE;

import java.util.Comparator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showEmployeeAtIndex(model, INDEX_FIRST_EMPLOYEE);
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_listIsSorted_restoresSavedOrder() {
        model.updateEmployeeSortOrder(Comparator.comparing((Employee employee) -> employee.getName().fullName)
                .reversed());
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_emptyEmployeeList_showsEmptyListMessage() {
        model = new ModelManager(new AddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_EMPTY_LIST, expectedModel);
    }

    @Test
    public void execute_allEmployeesArchived_showsSingularArchivedGuidance() throws Exception {
        AddressBook addressBook = new AddressBook();
        addressBook.addEmployee(ALICE.withArchived(true));
        model = new ModelManager(addressBook, new UserPrefs());
        expectedModel = new ModelManager(addressBook, new UserPrefs());

        String expectedMessage = String.format(ListCommand.MESSAGE_EMPTY_ACTIVE_WITH_ARCHIVED, 1, "");
        assertCommandSuccess(new ListCommand(), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_afterClearPreservesArchives_showsPluralArchivedGuidance() throws Exception {
        AddressBook addressBook = getTypicalAddressBook();
        Employee archivedAlice = ALICE.withArchived(true);
        addressBook.setEmployee(ALICE, archivedAlice);
        addressBook.setEmployee(BENSON, BENSON.withArchived(true));
        model = new ModelManager(addressBook, new UserPrefs());
        new ClearCommand().execute(model);
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        String expectedMessage = String.format(ListCommand.MESSAGE_EMPTY_ACTIVE_WITH_ARCHIVED, 2, "s");
        assertCommandSuccess(new ListCommand(), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_listAndListArchived_showOnlyRequestedStatus() throws Exception {
        Employee archivedAlice = ALICE.withArchived(true);
        model.setEmployee(ALICE, archivedAlice);

        new ListCommand().execute(model);
        assertFalse(model.getFilteredEmployeeList().contains(archivedAlice));
        new ListCommand(true).execute(model);
        assertEquals(java.util.List.of(archivedAlice), model.getFilteredEmployeeList());
    }

    @Test
    public void equalsAndHashCode() {
        ListCommand listCommand = new ListCommand();
        ListCommand listCommandCopy = new ListCommand(false);
        ListCommand listArchivedCommand = new ListCommand(true);

        assertTrue(listCommand.equals(listCommand));
        assertTrue(listCommand.equals(listCommandCopy));
        assertEquals(listCommand.hashCode(), listCommandCopy.hashCode());
        assertFalse(listCommand.equals(listArchivedCommand));
        assertNotEquals(listCommand.hashCode(), listArchivedCommand.hashCode());
        assertFalse(listCommand.equals(null));
        assertFalse(listCommand.equals(1));
    }

    @Test
    public void toStringMethod() {
        assertEquals(ListCommand.class.getCanonicalName() + "{shouldListArchived=false}",
                new ListCommand().toString());
        assertEquals(ListCommand.class.getCanonicalName() + "{shouldListArchived=true}",
                new ListCommand(true).toString());
    }
}

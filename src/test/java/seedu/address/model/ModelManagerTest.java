package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_EMPLOYEES;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.BENSON;
import static seedu.address.testutil.TypicalEmployees.CARL;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.NameContainsKeywordsPredicate;
import seedu.address.testutil.AddressBookBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasEmployee_nullEmployee_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasEmployee(null));
    }

    @Test
    public void hasEmployee_employeeNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasEmployee(ALICE));
    }

    @Test
    public void hasEmployee_employeeInAddressBook_returnsTrue() {
        modelManager.addEmployee(ALICE);
        assertTrue(modelManager.hasEmployee(ALICE));
    }

    @Test
    public void hasEmployee_employeeArchived_returnsTrue() {
        modelManager.addEmployee(ALICE);
        modelManager.archiveEmployee(ALICE);
        assertTrue(modelManager.hasEmployee(ALICE));
        assertTrue(modelManager.getFilteredEmployeeList().isEmpty());
    }

    @Test
    public void hasArchivedEmployee_activeAndArchivedEmployees_returnsCorrectResult() {
        modelManager.addEmployee(ALICE);
        modelManager.addEmployee(BENSON.withArchived(true));

        assertFalse(modelManager.hasArchivedEmployee(ALICE));
        assertTrue(modelManager.hasArchivedEmployee(BENSON));
    }

    @Test
    public void getArchivedEmployeeCount_activeAndArchivedEmployees_returnsArchivedCount() {
        modelManager.addEmployee(ALICE);
        modelManager.addEmployee(BENSON.withArchived(true));

        assertEquals(1, modelManager.getArchivedEmployeeCount());
    }

    @Test
    public void getFilteredEmployeeList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredEmployeeList().remove(0));
    }

    @Test
    public void updateEmployeeSortOrder_nullComparator_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.updateEmployeeSortOrder(null));
    }

    @Test
    public void updateEmployeeSortOrder_sortsDisplayedListButNotStoredList() {
        // stored order: Benson, Carl, Alice
        AddressBook addressBook = new AddressBookBuilder().withEmployee(BENSON).withEmployee(CARL)
                .withEmployee(ALICE).build();
        modelManager = new ModelManager(addressBook, new UserPrefs());

        modelManager.updateEmployeeSortOrder(Comparator.comparing(employee -> employee.getName().fullName));
        assertEquals(List.of(ALICE, BENSON, CARL), modelManager.getFilteredEmployeeList());
        assertEquals(List.of(BENSON, CARL, ALICE), modelManager.getAddressBook().getEmployeeList());

        modelManager.resetEmployeeSortOrder();
        assertEquals(List.of(BENSON, CARL, ALICE), modelManager.getFilteredEmployeeList());
    }

    @Test
    public void updateEmployeeSortOrder_newEmployeeIsShownInSortedPosition() {
        modelManager = new ModelManager(new AddressBookBuilder().withEmployee(BENSON).withEmployee(CARL).build(),
                new UserPrefs());
        modelManager.updateEmployeeSortOrder(Comparator.comparing(employee -> employee.getName().fullName));

        modelManager.addEmployee(ALICE);
        assertEquals(List.of(ALICE, BENSON, CARL), modelManager.getFilteredEmployeeList());
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withEmployee(ALICE).withEmployee(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different sort order -> returns false
        ModelManager sortedModelManager = new ModelManager(addressBook, userPrefs);
        sortedModelManager.updateEmployeeSortOrder(
                Comparator.comparing((Employee employee) -> employee.getName().fullName).reversed());
        assertFalse(modelManager.equals(sortedModelManager));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredEmployeeList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredEmployeeList(PREDICATE_SHOW_ALL_EMPLOYEES);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }
}

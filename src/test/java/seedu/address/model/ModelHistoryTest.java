package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.EmployeeDataAssertions.assertStoredEmployees;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.model.employee.Employee;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.EmployeeBuilder;

public class ModelHistoryTest {
    private final AddressBook initial = new AddressBookBuilder().withEmployee(ALICE).withEmployee(BENSON).build();
    private final ModelManager model = new ModelManager(initial, new UserPrefs());

    @Test
    public void constructionAndDirectMutations_requireExplicitCommit() {
        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
        model.deleteEmployee(ALICE);
        assertFalse(model.canUndoAddressBook());
        model.commitAddressBook();
        model.undoAddressBook();
        assertStoredEmployees(List.of(ALICE, BENSON), model.getAddressBook().getEmployeeList());
        assertFalse(model.canUndoAddressBook());
        model.redoAddressBook();
        assertStoredEmployees(List.of(BENSON), model.getAddressBook().getEmployeeList());
        assertFalse(model.canRedoAddressBook());
        assertThrows(IllegalStateException.class, model::redoAddressBook);
        model.undoAddressBook();
        assertThrows(IllegalStateException.class, model::undoAddressBook);
    }

    @Test
    public void setAddressBook_replacesOnlyWorkingData() {
        model.deleteEmployee(ALICE);
        model.commitAddressBook();
        model.undoAddressBook();
        model.showArchivedEmployees();
        Object predicate = filtered().getPredicate();
        model.setAddressBook(new AddressBookBuilder().withEmployee(ALICE.withArchived(true)).build());
        assertSame(predicate, filtered().getPredicate());
        assertStoredEmployees(List.of(ALICE.withArchived(true)), filtered());
        assertFalse(model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());
        model.redoAddressBook();
        assertStoredEmployees(List.of(BENSON), model.getAddressBook().getEmployeeList());
        model.setAddressBook(initial);
        model.commitAddressBook();
        model.undoAddressBook();
        assertStoredEmployees(List.of(BENSON), model.getAddressBook().getEmployeeList());
    }

    @Test
    public void commit_identicalDataPreservesRedoButRawIdOrOrderChangesDiscardIt() {
        model.deleteEmployee(ALICE);
        model.commitAddressBook();
        model.undoAddressBook();
        model.setAddressBook(new AddressBook(initial));
        model.commitAddressBook();
        assertTrue(model.canRedoAddressBook());
        Employee lowercaseAlice = new EmployeeBuilder(ALICE).withId(ALICE.getId().value.toLowerCase()).build();
        model.setEmployee(ALICE, lowercaseAlice);
        model.commitAddressBook();
        assertFalse(model.canRedoAddressBook());
        model.undoAddressBook();
        assertStoredEmployees(List.of(ALICE, BENSON), model.getAddressBook().getEmployeeList());
        model.redoAddressBook();
        assertStoredEmployees(List.of(lowercaseAlice, BENSON), model.getAddressBook().getEmployeeList());
        model.setAddressBook(new AddressBookBuilder().withEmployee(BENSON).withEmployee(lowercaseAlice).build());
        model.commitAddressBook();
        model.undoAddressBook();
        assertStoredEmployees(List.of(lowercaseAlice, BENSON), model.getAddressBook().getEmployeeList());
        model.redoAddressBook();
        assertStoredEmployees(List.of(BENSON, lowercaseAlice), model.getAddressBook().getEmployeeList());
    }

    @Test
    public void checkpoint_laterChangesDoNotAffectWorkingDataOrEntireHistory() {
        model.deleteEmployee(ALICE);
        model.commitAddressBook();
        model.deleteEmployee(BENSON);
        model.commitAddressBook();
        model.undoAddressBook();
        model.showArchivedEmployees();
        model.setEmployee(BENSON, BENSON.withArchived(true)); // Deliberately uncommitted.
        ModelCheckpoint checkpoint = model.createCheckpoint();
        ObservableList<Employee> storedList = model.getAddressBook().getEmployeeList();
        ObservableList<Employee> visibleList = model.getFilteredEmployeeList();
        Object predicate = filtered().getPredicate();
        model.commitAddressBook(); // Discards the redo tail tentatively.
        model.addEmployee(ALICE);
        model.commitAddressBook();
        ModelCheckpoint laterCheckpoint = model.createCheckpoint();

        for (int attempt = 0; attempt < 2; attempt++) {
            model.restoreCheckpoint(checkpoint);
            assertSame(storedList, model.getAddressBook().getEmployeeList());
            assertSame(visibleList, model.getFilteredEmployeeList());
            assertSame(predicate, filtered().getPredicate());
            assertStoredEmployees(List.of(BENSON.withArchived(true)), visibleList);
            assertTrue(model.canUndoAddressBook());
            assertTrue(model.canRedoAddressBook());
            model.redoAddressBook();
            assertStoredEmployees(List.of(), storedList);
            assertFalse(model.canRedoAddressBook());
            model.undoAddressBook();
            assertStoredEmployees(List.of(BENSON), storedList);
            model.undoAddressBook();
            assertStoredEmployees(List.of(ALICE, BENSON), storedList);
            assertFalse(model.canUndoAddressBook());
            model.restoreCheckpoint(laterCheckpoint);
            assertStoredEmployees(List.of(BENSON.withArchived(true), ALICE), storedList);
        }
    }

    @Test
    public void checkpoint_containersAreImmutableAndSnapshotsAreShared() {
        model.deleteEmployee(ALICE);
        model.commitAddressBook();
        ModelCheckpoint first = model.createCheckpoint();
        ModelCheckpoint second = model.createCheckpoint();
        assertSame(first.getHistory().get(0), second.getHistory().get(0));
        assertSame(first.getHistory().get(1), second.getHistory().get(1));
        assertSame(ALICE, first.getHistory().getFirst().getFirst());
        assertThrows(UnsupportedOperationException.class, () -> first.getWorkingEmployees().clear());
        assertThrows(UnsupportedOperationException.class, () -> first.getHistory().clear());
        assertThrows(UnsupportedOperationException.class, () -> first.getHistory().getFirst().clear());
        model.addEmployee(ALICE);
        assertStoredEmployees(List.of(BENSON), first.getWorkingEmployees());
        assertStoredEmployees(List.of(ALICE, BENSON), first.getHistory().getFirst());
    }

    @Test
    public void undo_zeroMatchSearchRevealsRestoredEmployeeWithoutReplacingPredicate() {
        model.deleteEmployee(ALICE);
        model.commitAddressBook();
        model.updateFilteredEmployeeList(employee -> employee.getName().equals(ALICE.getName()));
        Object predicate = filtered().getPredicate();
        assertTrue(filtered().isEmpty());
        model.undoAddressBook();
        assertSame(predicate, filtered().getPredicate());
        assertStoredEmployees(List.of(ALICE), filtered());
        model.redoAddressBook();
        assertSame(predicate, filtered().getPredicate());
        assertTrue(filtered().isEmpty());
    }

    @Test
    public void undoAndRedo_archivedView_preservesPredicateAndObservableLists() {
        model.archiveEmployee(ALICE);
        model.commitAddressBook();
        model.showArchivedEmployees();
        Object predicate = filtered().getPredicate();
        ObservableList<Employee> visible = model.getFilteredEmployeeList();
        ObservableList<Employee> stored = model.getAddressBook().getEmployeeList();
        model.undoAddressBook();
        assertSame(predicate, filtered().getPredicate());
        assertTrue(visible.isEmpty());
        model.redoAddressBook();
        assertSame(predicate, filtered().getPredicate());
        assertSame(visible, model.getFilteredEmployeeList());
        assertSame(stored, model.getAddressBook().getEmployeeList());
        assertStoredEmployees(List.of(ALICE.withArchived(true)), visible);
        assertStoredEmployees(List.of(ALICE.withArchived(true), BENSON), stored);
    }

    @Test
    public void equals_differentHistoriesPreservesExistingEquality() {
        model.deleteEmployee(ALICE);
        model.commitAddressBook();
        model.undoAddressBook();
        assertEquals(new ModelManager(initial, new UserPrefs()), model);
        assertEquals(initial, model.getAddressBook());
        assertEquals(model.getAddressBook(), initial);
        assertEquals(initial.hashCode(), model.getAddressBook().hashCode());
    }

    private FilteredList<Employee> filtered() {
        return (FilteredList<Employee>) model.getFilteredEmployeeList();
    }
}

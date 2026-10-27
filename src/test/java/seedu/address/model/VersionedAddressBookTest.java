package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import seedu.address.model.employee.Employee;
import seedu.address.testutil.EmployeeBuilder;

public class VersionedAddressBookTest {
    @Test
    public void constructor_invalidLimit_rejected() {
        assertThrows(IllegalArgumentException.class, () -> new VersionedAddressBook(new AddressBook(), -2));
    }

    @Test
    @ResourceLock("java.lang.System.properties")
    public void constructor_systemProperty_configuresLimit() {
        String property = VersionedAddressBook.MAX_UNDO_HISTORY_PROPERTY;
        String original = System.getProperty(property);
        try {
            System.setProperty(property, "1");
            VersionedAddressBook book = new VersionedAddressBook(new AddressBook());
            commitState(book, 1);
            commitState(book, 2);
            book.undo();
            assertState(book, 1);
            assertFalse(book.canUndo());
            System.setProperty(property, "invalid");
            assertThrows(IllegalArgumentException.class, () -> new VersionedAddressBook(new AddressBook()));
            System.setProperty(property, "-2");
            assertThrows(IllegalArgumentException.class, () -> new VersionedAddressBook(new AddressBook()));
            System.clearProperty(property);
            VersionedAddressBook unlimited = new VersionedAddressBook(new AddressBook());
            commitState(unlimited, 1);
            commitState(unlimited, 2);
            unlimited.undo();
            unlimited.undo();
            assertTrue(unlimited.getEmployeeList().isEmpty());
        } finally {
            if (original == null) {
                System.clearProperty(property);
            } else {
                System.setProperty(property, original);
            }
        }
    }

    @Test
    public void commit_atCapacity_evictsOnlyOldestState() {
        VersionedAddressBook book = new VersionedAddressBook(new AddressBook(), 2);
        commitState(book, 1);
        commitState(book, 2);
        assertEquals(3, historySize(book));
        commitState(book, 3);
        assertEquals(3, historySize(book));
        book.undo();
        assertState(book, 2);
        book.undo();
        assertState(book, 1);
        assertThrows(IllegalStateException.class, book::undo);
        book.redo();
        book.redo();
        assertState(book, 3);
        assertThrows(IllegalStateException.class, book::redo);
    }

    @Test
    public void commit_zeroLimit_keepsCurrentStateOnly() {
        VersionedAddressBook book = new VersionedAddressBook(new AddressBook(), 0);
        commitState(book, 1);
        commitState(book, 2);
        assertState(book, 2);
        assertEquals(1, historySize(book));
        assertFalse(book.canUndo());
        assertFalse(book.canRedo());
    }

    @Test
    public void commit_afterUndo_noOpPreservesRedoAndChangeDiscardsRedo() {
        VersionedAddressBook book = new VersionedAddressBook(new AddressBook(), 2);
        for (int i = 1; i <= 3; i++) {
            commitState(book, i);
        }
        book.undo();
        book.commit();
        assertTrue(book.canRedo());
        assertEquals(3, historySize(book));
        commitState(book, 4);
        assertFalse(book.canRedo());
        assertEquals(3, historySize(book));
        book.undo();
        assertState(book, 2);
        book.undo();
        assertState(book, 1);
        assertFalse(book.canUndo());
    }

    @Test
    public void checkpoint_afterEviction_restoresOriginalHistoryAndRedo() {
        VersionedAddressBook book = new VersionedAddressBook(new AddressBook(), 2);
        commitState(book, 1);
        commitState(book, 2);
        book.undo();
        ModelCheckpoint checkpoint = book.createCheckpoint(employee -> true);
        for (int attempt = 0; attempt < 2; attempt++) {
            commitState(book, 3);
            commitState(book, 4);
            book.restoreCheckpoint(checkpoint);
            assertState(book, 1);
            assertTrue(book.canUndo());
            assertTrue(book.canRedo());
            book.redo();
            assertState(book, 2);
            book.undo();
            book.undo();
            assertTrue(book.getEmployeeList().isEmpty());
            book.redo();
        }
    }

    @Test
    public void commit_longHistory_retainsBoundedSnapshotsAndCorrectUndoRedo() {
        VersionedAddressBook book = new VersionedAddressBook(new AddressBook(), 25);
        for (int i = 1; i <= 10000; i++) {
            commitState(book, i);
            assertTrue(historySize(book) <= 26);
        }
        assertEquals(26, historySize(book));
        for (int i = 9999; i >= 9975; i--) {
            book.undo();
            assertState(book, i);
        }
        assertFalse(book.canUndo());
        for (int i = 9976; i <= 10000; i++) {
            book.redo();
            assertState(book, i);
        }
        assertFalse(book.canRedo());
    }

    @Test
    public void commit_unlimitedHistory_retainsEveryState() {
        VersionedAddressBook book = new VersionedAddressBook(new AddressBook(), -1);
        for (int i = 1; i <= 1000; i++) {
            commitState(book, i);
        }
        assertEquals(1001, historySize(book));
        for (int i = 0; i < 1000; i++) {
            book.undo();
        }
        assertTrue(book.getEmployeeList().isEmpty());
        assertFalse(book.canUndo());
    }

    private void commitState(VersionedAddressBook book, int number) {
        book.setEmployees(List.of(employee(number)));
        book.commit();
    }

    private Employee employee(int number) {
        return new EmployeeBuilder().withName("Employee " + number).build();
    }

    private void assertState(VersionedAddressBook book, int number) {
        assertEquals(List.of(employee(number)), book.getEmployeeList());
    }

    private int historySize(VersionedAddressBook book) {
        return book.createCheckpoint(employee -> true).getHistory().size();
    }
}

package seedu.address.model;

import java.util.Comparator;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.employee.Employee;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Employee> PREDICATE_SHOW_ALL_EMPLOYEES = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces working data without committing, resetting history or changing the view.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Commits an actual change to the working employee data as one history entry. */
    void commitAddressBook();

    /** Returns whether there is a preceding snapshot. */
    boolean canUndoAddressBook();

    /** Returns whether there is a subsequent snapshot. */
    boolean canRedoAddressBook();

    /** Restores the preceding snapshot, preserving the current view. */
    void undoAddressBook();

    /** Restores the subsequent snapshot, preserving the current view. */
    void redoAddressBook();

    /** Captures working data, complete history and the effective view for rollback. */
    ModelCheckpoint createCheckpoint();

    /** Restores working data, complete history and the saved effective view. */
    void restoreCheckpoint(ModelCheckpoint checkpoint);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if an employee with the same identity as {@code employee} exists in the address book.
     */
    boolean hasEmployee(Employee employee);

    /** Returns true if an archived employee has the same identity as {@code employee}. */
    boolean hasArchivedEmployee(Employee employee);

    /**
     * Deletes the given employee.
     * The employee must exist in the address book.
     */
    void deleteEmployee(Employee target);

    /** Removes all active employees while preserving archived employee records. */
    void clearEmployees();

    /**
     * Adds the given employee.
     * {@code employee} must not already exist in the address book.
     */
    void addEmployee(Employee employee);

    /**
     * Replaces the given employee {@code target} with {@code editedEmployee}.
     * {@code target} must exist in the address book.
     * The employee identity of {@code editedEmployee} must not be the same as another existing employee
     * in the address book.
     */
    void setEmployee(Employee target, Employee editedEmployee);

    /** Replaces an employee with an archived copy. */
    void archiveEmployee(Employee employee);

    /** Replaces an employee with an active copy. */
    void unarchiveEmployee(Employee employee);

    /** Returns an unmodifiable view of the filtered employee list, in the current sort order */
    ObservableList<Employee> getFilteredEmployeeList();

    /** Returns the number of archived employees. */
    int getArchivedEmployeeCount();

    /**
     * Updates the filter of the filtered employee list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredEmployeeList(Predicate<Employee> predicate);

    /** Updates the filter for a reporting query, including matching active and archived employees. */
    void updateReportingEmployeeList(Predicate<Employee> predicate);

    /**
     * Sorts the displayed employee list with the given {@code comparator}. The stored order is not changed.
     * @throws NullPointerException if {@code comparator} is null.
     */
    void updateEmployeeSortOrder(Comparator<Employee> comparator);

    /** Shows the displayed employee list in the order in which the employees are stored. */
    void resetEmployeeSortOrder();

    /** Updates the filtered list to show archived employees only. */
    void showArchivedEmployees();
}

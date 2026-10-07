package seedu.address.model;

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
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if an employee with the same identity as {@code employee} exists in the address book.
     */
    boolean hasEmployee(Employee employee);

    /**
     * Deletes the given employee.
     * The employee must exist in the address book.
     */
    void deleteEmployee(Employee target);

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

    /** Returns an unmodifiable view of the filtered employee list */
    ObservableList<Employee> getFilteredEmployeeList();

    /** Returns the predicate used to filter the employee list. */
    Predicate<? super Employee> getEmployeeFilter();

    /**
     * Updates the filter of the filtered employee list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredEmployeeList(Predicate<? super Employee> predicate);
}

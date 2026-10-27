package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.employee.Employee;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final VersionedAddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Employee> filteredEmployees;
    private final SortedList<Employee> sortedEmployees;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new VersionedAddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredEmployees = new FilteredList<>(this.addressBook.getEmployeeList(), employee -> !employee.isArchived());
        sortedEmployees = new SortedList<>(filteredEmployees);
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public void commitAddressBook() {
        addressBook.commit();
    }

    @Override
    public boolean canUndoAddressBook() {
        return addressBook.canUndo();
    }

    @Override
    public boolean canRedoAddressBook() {
        return addressBook.canRedo();
    }

    @Override
    public void undoAddressBook() {
        addressBook.undo();
    }

    @Override
    public void redoAddressBook() {
        addressBook.redo();
    }

    @Override
    public ModelCheckpoint createCheckpoint() {
        return addressBook.createCheckpoint(filteredEmployees.getPredicate());
    }

    @Override
    public void restoreCheckpoint(ModelCheckpoint checkpoint) {
        requireNonNull(checkpoint);
        addressBook.restoreCheckpoint(checkpoint);
        // Installing the effective predicate directly also preserves archived-only views.
        filteredEmployees.setPredicate(checkpoint.getPredicate());
    }

    @Override
    public boolean hasEmployee(Employee employee) {
        requireNonNull(employee);
        return addressBook.hasEmployee(employee);
    }

    @Override
    public boolean hasArchivedEmployee(Employee employee) {
        requireNonNull(employee);
        return addressBook.getEmployeeList().stream()
                .anyMatch(candidate -> candidate.isArchived() && candidate.isSameEmployee(employee));
    }

    @Override
    public void deleteEmployee(Employee target) {
        addressBook.removeEmployee(target);
    }

    @Override
    public void clearEmployees() {
        addressBook.clearEmployees();
        updateFilteredEmployeeList(PREDICATE_SHOW_ALL_EMPLOYEES);
    }

    @Override
    public void addEmployee(Employee employee) {
        addressBook.addEmployee(employee);
        updateFilteredEmployeeList(PREDICATE_SHOW_ALL_EMPLOYEES);
    }

    @Override
    public void setEmployee(Employee target, Employee editedEmployee) {
        requireAllNonNull(target, editedEmployee);

        addressBook.setEmployee(target, editedEmployee);
    }

    @Override
    public void archiveEmployee(Employee employee) {
        addressBook.setEmployee(employee, employee.withArchived(true));
    }

    @Override
    public void unarchiveEmployee(Employee employee) {
        addressBook.setEmployee(employee, employee.withArchived(false));
    }

    //=========== Filtered Employee List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Employee} backed by the internal list of
     * {@code addressBook}, filtered by the current predicate and in the current sort order.
     */
    @Override
    public ObservableList<Employee> getFilteredEmployeeList() {
        return sortedEmployees;
    }

    @Override
    public int getArchivedEmployeeCount() {
        return (int) addressBook.getEmployeeList().stream().filter(Employee::isArchived).count();
    }

    @Override
    public void updateFilteredEmployeeList(Predicate<Employee> predicate) {
        requireNonNull(predicate);
        filteredEmployees.setPredicate(employee -> !employee.isArchived() && predicate.test(employee));
    }

    @Override
    public void showArchivedEmployees() {
        filteredEmployees.setPredicate(Employee::isArchived);
    }

    @Override
    public void updateReportingEmployeeList(Predicate<Employee> predicate) {
        requireNonNull(predicate);
        filteredEmployees.setPredicate(predicate);
    }

    @Override
    public void updateEmployeeSortOrder(Comparator<Employee> comparator) {
        requireNonNull(comparator);
        sortedEmployees.setComparator(comparator);
    }

    @Override
    public void resetEmployeeSortOrder() {
        sortedEmployees.setComparator(null);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && sortedEmployees.equals(otherModelManager.sortedEmployees);
    }

}

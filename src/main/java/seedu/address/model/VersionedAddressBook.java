package seedu.address.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import seedu.address.model.employee.Employee;

/**
 * Stores working employee data and immutable, ordered snapshots for this session.
 * Ordinary address book equality deliberately excludes history.
 */
public class VersionedAddressBook extends AddressBook {
    private List<List<Employee>> addressBookStateList = new ArrayList<>();
    private boolean historyShared;
    private int currentStatePointer;

    /** Creates working data and exactly one initial snapshot. */
    public VersionedAddressBook(ReadOnlyAddressBook initialState) {
        super(initialState);
        addressBookStateList.add(List.copyOf(getEmployeeList()));
    }

    /** Records an actual stored-value change, discarding redo only when the data differs. */
    public void commit() {
        List<Employee> currentState = addressBookStateList.get(currentStatePointer);
        if (hasSameStoredValues(currentState)) {
            return;
        }
        List<Employee> snapshot = List.copyOf(getEmployeeList());
        if (historyShared) {
            addressBookStateList = new ArrayList<>(addressBookStateList);
            historyShared = false;
        }
        addressBookStateList.subList(currentStatePointer + 1, addressBookStateList.size()).clear();
        addressBookStateList.add(snapshot);
        currentStatePointer++;
    }

    private boolean hasSameStoredValues(List<Employee> snapshot) {
        List<Employee> workingEmployees = getEmployeeList();
        if (snapshot.size() != workingEmployees.size()) {
            return false;
        }
        for (int i = 0; i < snapshot.size(); i++) {
            Employee saved = snapshot.get(i);
            Employee working = workingEmployees.get(i);
            // Employee equality compares IDs without regard to case; storage preserves their spelling.
            if (!saved.equals(working) || !saved.getId().value.equals(working.getId().value)) {
                return false;
            }
        }
        return true;
    }

    /** Returns whether a preceding snapshot exists. */
    public boolean canUndo() {
        return currentStatePointer > 0;
    }

    /** Returns whether a subsequent snapshot exists. */
    public boolean canRedo() {
        return currentStatePointer < addressBookStateList.size() - 1;
    }

    /** Restores the preceding snapshot without recording a new state. */
    public void undo() {
        if (!canUndo()) {
            throw new IllegalStateException("No preceding address book state.");
        }
        setEmployees(addressBookStateList.get(currentStatePointer - 1));
        currentStatePointer--;
    }

    /** Restores the subsequent snapshot without recording a new state. */
    public void redo() {
        if (!canRedo()) {
            throw new IllegalStateException("No subsequent address book state.");
        }
        setEmployees(addressBookStateList.get(currentStatePointer + 1));
        currentStatePointer++;
    }

    /** Captures working data, sharing a frozen history index without copying its entries. */
    ModelCheckpoint createCheckpoint(Predicate<? super Employee> predicate) {
        if (!historyShared) {
            addressBookStateList = Collections.unmodifiableList(addressBookStateList);
            historyShared = true;
        }
        return new ModelCheckpoint(getEmployeeList(), addressBookStateList, currentStatePointer, predicate);
    }

    /** Restores a checkpoint without modifying any container held by that checkpoint. */
    void restoreCheckpoint(ModelCheckpoint checkpoint) {
        setEmployees(checkpoint.getWorkingEmployees());
        addressBookStateList = checkpoint.getHistory();
        historyShared = true;
        currentStatePointer = checkpoint.getCurrentStatePointer();
    }
}

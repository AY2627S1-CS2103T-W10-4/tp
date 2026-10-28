package seedu.address.model;

import static seedu.address.commons.util.AppUtil.checkArgument;

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
    public static final String MAX_UNDO_HISTORY_PROPERTY = "huntr.maxUndoHistory";

    private final int maxUndoHistory;
    private List<List<Employee>> addressBookStateList = new ArrayList<>();
    private boolean historyShared;
    private int currentStatePointer;

    /**
     * Creates working data and one initial snapshot, using the optional JVM history limit.
     * The default is unlimited to preserve existing undo behaviour until a team-wide limit is agreed.
     */
    public VersionedAddressBook(ReadOnlyAddressBook initialState) {
        this(initialState, configuredHistoryLimit());
    }

    /**
     * Creates history retaining at most {@code maxUndoHistory} undo steps plus the current state.
     * Zero disables undo history; -1 retains unlimited history. Other negative limits are invalid.
     */
    public VersionedAddressBook(ReadOnlyAddressBook initialState, int maxUndoHistory) {
        super(initialState);
        checkArgument(maxUndoHistory >= -1, "Maximum undo history must be -1 or a non-negative integer.");
        this.maxUndoHistory = maxUndoHistory;
        addressBookStateList.add(List.copyOf(getEmployeeList()));
    }

    private static int configuredHistoryLimit() {
        String configured = System.getProperty(MAX_UNDO_HISTORY_PROPERTY, "-1");
        try {
            return Integer.parseInt(configured);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(MAX_UNDO_HISTORY_PROPERTY
                    + " must be -1 or a non-negative integer.", e);
        }
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
        if (maxUndoHistory >= 0 && currentStatePointer > maxUndoHistory) {
            int expiredStates = currentStatePointer - maxUndoHistory;
            addressBookStateList.subList(0, expiredStates).clear();
            currentStatePointer -= expiredStates;
        }
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

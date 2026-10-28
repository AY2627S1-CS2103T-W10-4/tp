package seedu.address.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import seedu.address.model.employee.Employee;

/**
 * Captures working data, complete history, pointer and the effective view for rollback.
 * Employee objects, snapshots and the frozen history index are shared. Installed predicates
 * are treated as stable and retained by reference. Capture takes O(employee count), independent of history length.
 */
public final class ModelCheckpoint {
    private final List<Employee> workingEmployees;
    private final List<List<Employee>> history;
    private final int currentStatePointer;
    private final Predicate<? super Employee> predicate;

    /** Creates a checkpoint using the immutable history index supplied by VersionedAddressBook. */
    ModelCheckpoint(List<Employee> workingEmployees, List<List<Employee>> history, int currentStatePointer,
            Predicate<? super Employee> predicate) {
        this.workingEmployees = Collections.unmodifiableList(new ArrayList<>(workingEmployees));
        this.history = history;
        this.currentStatePointer = currentStatePointer;
        this.predicate = predicate;
    }

    List<Employee> getWorkingEmployees() {
        return workingEmployees;
    }

    List<List<Employee>> getHistory() {
        return history;
    }

    int getCurrentStatePointer() {
        return currentStatePointer;
    }

    Predicate<? super Employee> getPredicate() {
        return predicate;
    }
}

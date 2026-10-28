package seedu.address.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.LogicManager;
import seedu.address.model.employee.Department;
import seedu.address.model.employee.Email;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.Name;
import seedu.address.model.employee.Phone;
import seedu.address.model.employee.Role;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Opt-in checkpoint microbenchmark. Keeps the roster fixed to isolate history-index costs.
 * Measures checkpoint capture, the former copying strategy, and undo/redo dispatch without disk I/O.
 * These timings are diagnostic, not a rendered-UI acceptance test or a CI pass/fail threshold.
 */
public final class CheckpointPerformance {
    private static final int REPETITIONS = 10000;
    private static volatile Object sink;

    private CheckpointPerformance() {}

    /** Runs the benchmark using already compiled application classes and runtime dependencies. */
    public static void main(String[] args) throws Exception {
        LogsCenter.getLogger(LogicManager.class).setLevel(Level.OFF);
        System.out.println("history steps,capture ns/op,copying baseline ns/op,undo+redo ns/pair");
        for (int size : List.of(10, 1000, 10000)) {
            ModelManager model = new ModelManager();
            Employee employee = new Employee(new EmployeeId("BENCH"), new Name("Benchmark Employee"),
                    new Phone("91234567"), new Email("bench@example.com"), new Department("QA"), new Role("Initial"));
            model.addEmployee(employee);
            model.commitAddressBook();
            for (int i = 1; i < size; i++) {
                Employee replacement = new Employee(employee.getId(), employee.getName(), employee.getPhone(),
                        employee.getEmail(), employee.getDepartment(), new Role("Role " + i));
                model.setEmployee(employee, replacement);
                model.commitAddressBook();
                employee = replacement;
            }
            JsonAddressBookStorage noDiskStorage = new JsonAddressBookStorage(Path.of("unused-benchmark.json")) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook data) {
                    sink = data;
                }
            };
            LogicManager logic = new LogicManager(model, new StorageManager(noDiskStorage,
                    new JsonUserPrefsStorage(Path.of("unused-benchmark-prefs.json"))));
            ModelCheckpoint checkpoint = model.createCheckpoint();
            Runnable capture = () -> sink = model.createCheckpoint();
            Runnable copying = () -> {
                // Match the former checkpoint's two container copies. Retain both so allocation is observable.
                sink = List.of(new ArrayList<>(model.getAddressBook().getEmployeeList()),
                        new ArrayList<>(checkpoint.getHistory()));
            };
            for (int i = 0; i < 2000; i++) {
                capture.run();
                copying.run();
                logic.execute("undo");
                logic.execute("redo");
            }
            long captureTime = measure(capture);
            long copyingTime = measure(copying);
            long start = System.nanoTime();
            for (int i = 0; i < REPETITIONS; i++) {
                logic.execute("undo");
                logic.execute("redo");
            }
            long navigationTime = (System.nanoTime() - start) / REPETITIONS;
            System.out.printf("%d,%d,%d,%d%n", size, captureTime, copyingTime, navigationTime);
        }
    }

    private static long measure(Runnable operation) {
        long start = System.nanoTime();
        for (int i = 0; i < REPETITIONS; i++) {
            operation.run();
        }
        return (System.nanoTime() - start) / REPETITIONS;
    }
}

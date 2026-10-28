package seedu.address.logic.commands;

import java.util.ArrayList;
import java.util.List;

import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Department;
import seedu.address.model.employee.Email;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.Name;
import seedu.address.model.employee.Phone;
import seedu.address.model.employee.Role;

/**
 * Opt-in similarity microbenchmark and real AddCommand benchmark, excluding fixture setup and disk I/O.
 * Full-roster scanning remains O(N); this measures the cost removed by caching normalised names.
 */
public final class AddPerformance {
    private static final int WARMUP = 30;
    private static final int REPETITIONS = 100;
    private static volatile long sink;

    private AddPerformance() {}

    /** Runs no-match, sparse-match and all-match workloads at increasing roster sizes. */
    public static void main(String[] args) throws Exception {
        System.out.println("roster,scenario,matches,cached scan ns/op,uncached scan ns/op,add command ns/op");
        for (int size : List.of(100, 1000, 10000)) {
            for (String scenario : List.of("none", "sparse", "all")) {
                runScenario(size, scenario);
            }
        }
    }

    private static void runScenario(int size, String scenario) throws Exception {
        List<Employee> roster = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            boolean matches = scenario.equals("all") || scenario.equals("sparse") && (i < 5 || i == size - 1);
            roster.add(employee("PERF-" + i, matches ? "Jos\u00e9    Wei    Tan" : "Other Employee " + i,
                    i % 2 == 0));
        }
        Employee toAdd = employee("NEW", "JOSE\u0301 Wei Tan", false);
        AddressBook book = new AddressBook();
        book.setEmployees(roster);
        ModelManager model = new ModelManager(book, new UserPrefs());
        AddCommand command = new AddCommand(toAdd);
        long expectedMatches = scenario.equals("all") ? size : scenario.equals("sparse") ? 6 : 0;
        if (countMatches(roster, toAdd.getName(), true) != expectedMatches
                || countMatches(roster, toAdd.getName(), false) != expectedMatches) {
            throw new AssertionError("Cached and uncached scans must agree on the match count.");
        }
        for (int i = 0; i < WARMUP; i++) {
            sink = countMatches(roster, toAdd.getName(), true);
            sink = countMatches(roster, toAdd.getName(), false);
            sink = command.execute(model).getFeedbackToUser().length();
            model.deleteEmployee(toAdd);
        }
        long cachedTime = measureScan(roster, toAdd.getName(), true);
        long uncachedTime = measureScan(roster, toAdd.getName(), false);
        long commandTime = 0;
        for (int i = 0; i < REPETITIONS; i++) {
            long start = System.nanoTime();
            CommandResult result = command.execute(model);
            commandTime += System.nanoTime() - start;
            sink = result.getFeedbackToUser().length();
            // Remove the new record outside the timed region to keep every measured add at the same roster size.
            model.deleteEmployee(toAdd);
        }
        System.out.printf("%d,%s,%d,%d,%d,%d%n", size, scenario, expectedMatches,
                cachedTime, uncachedTime, commandTime / REPETITIONS);
    }

    private static long measureScan(List<Employee> roster, Name target, boolean cached) {
        long start = System.nanoTime();
        for (int i = 0; i < REPETITIONS; i++) {
            sink = countMatches(roster, target, cached);
        }
        return (System.nanoTime() - start) / REPETITIONS;
    }

    private static long countMatches(List<Employee> roster, Name target, boolean cached) {
        long matches = 0;
        for (Employee employee : roster) {
            boolean similar = cached ? employee.getName().isSimilarTo(target)
                    : Name.normalise(employee.getName().fullName).equalsIgnoreCase(Name.normalise(target.fullName));
            if (similar) {
                matches++;
            }
        }
        return matches;
    }

    private static Employee employee(String id, String name, boolean archived) {
        return new Employee(new EmployeeId(id), new Name(name), new Phone("91234567"),
                new Email("benchmark@example.com"), new Department("QA"), new Role("Tester"), archived);
    }
}

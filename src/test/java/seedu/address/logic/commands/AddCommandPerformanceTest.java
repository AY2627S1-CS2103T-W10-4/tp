package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.testutil.EmployeeBuilder;

/** Large-roster regressions using the real model, with fixture construction excluded from timing. */
public class AddCommandPerformanceTest {
    private static final int LARGE_ROSTER_SIZE = 10000;
    private static final Duration EXECUTION_BUDGET = Duration.ofSeconds(10);
    private static final Employee TO_ADD = new EmployeeBuilder().withId("NEW")
            .withName("JOSE\u0301 Wei Tan").build();

    @Test
    public void execute_largeRosterWithoutMatches_addsWithoutWarning() {
        ModelManager model = modelWithRoster(LARGE_ROSTER_SIZE, index -> false);
        String feedback = executeWithinBudget(model);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(TO_ADD)), feedback);
        assertAddedWithoutChangingExistingEmployees(model);
    }

    @Test
    public void execute_largeMatchingRoster_boundsIdsAndCountsArchivedMatches() {
        ModelManager model = modelWithRoster(LARGE_ROSTER_SIZE, index -> true);
        String feedback = executeWithinBudget(model);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(TO_ADD)) + "\n"
                + String.format(AddCommand.MESSAGE_MANY_SIMILAR_NAMES_WARNING, LARGE_ROSTER_SIZE, 5,
                        "PERF-0, PERF-1, PERF-2, PERF-3, PERF-4"), feedback);
        assertFalse(feedback.contains("PERF-5"));
        assertAddedWithoutChangingExistingEmployees(model);
    }

    @Test
    public void execute_matchAtEndOfLargeRoster_preservesTotalBeyondPreviewLimit() {
        ModelManager model = modelWithRoster(LARGE_ROSTER_SIZE,
                index -> index < 5 || index == LARGE_ROSTER_SIZE - 1);
        // The warning must use the whole roster even when the current view is empty.
        model.updateFilteredEmployeeList(employee -> false);
        String feedback = executeWithinBudget(model);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(TO_ADD)) + "\n"
                + String.format(AddCommand.MESSAGE_MANY_SIMILAR_NAMES_WARNING, 6, 5,
                        "PERF-0, PERF-1, PERF-2, PERF-3, PERF-4"), feedback);
        assertAddedWithoutChangingExistingEmployees(model);
    }

    @Test
    public void execute_exactlyFiveMatches_displaysAllWithoutTruncation() {
        ModelManager model = modelWithRoster(5, index -> true);
        String feedback = executeWithinBudget(model);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(TO_ADD)) + "\n"
                + String.format(AddCommand.MESSAGE_SIMILAR_NAME_WARNING,
                        "PERF-0, PERF-1, PERF-2, PERF-3, PERF-4"), feedback);
        assertEquals(6, model.getAddressBook().getEmployeeList().size());
    }

    private ModelManager modelWithRoster(int size, IntPredicate matches) {
        List<Employee> employees = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            employees.add(new EmployeeBuilder().withId("PERF-" + i)
                    .withName(matches.test(i) ? "Jos\u00e9    Wei    Tan" : "Other Employee " + i)
                    .withArchived(i % 2 == 0).build());
        }
        AddressBook book = new AddressBook();
        book.setEmployees(employees);
        return new ModelManager(book, new UserPrefs());
    }

    private String executeWithinBudget(ModelManager model) {
        // A generous guard catches severe regressions; benchmark timings are recorded separately, not asserted in CI.
        List<Employee> original = List.copyOf(model.getAddressBook().getEmployeeList());
        CommandResult result = assertTimeout(EXECUTION_BUDGET, () -> new AddCommand(TO_ADD).execute(model));
        assertEquals(original, model.getAddressBook().getEmployeeList().subList(0, original.size()));
        return result.getFeedbackToUser();
    }

    private void assertAddedWithoutChangingExistingEmployees(ModelManager model) {
        List<Employee> employees = model.getAddressBook().getEmployeeList();
        assertEquals(LARGE_ROSTER_SIZE + 1, employees.size());
        assertEquals(TO_ADD, employees.getLast());
        for (int i = 0; i < LARGE_ROSTER_SIZE; i++) {
            assertEquals("PERF-" + i, employees.get(i).getId().value);
            assertEquals(i % 2 == 0, employees.get(i).isArchived());
        }
        assertTrue(model.hasEmployee(TO_ADD));
    }
}

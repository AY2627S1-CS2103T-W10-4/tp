package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.EmployeeBuilder;

public class ArchiveViewIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage employeeStorage;

    @BeforeEach
    public void setUp() {
        Employee firstAlice = new EmployeeBuilder().withId("E0001").withName("Alice Tan")
                .withDepartment("Engineering").withRole("Engineer").build();
        Employee secondAlice = new EmployeeBuilder().withId("E0002").withName("Alice Lim")
                .withDepartment("Engineering").withRole("Manager").build();
        Employee bob = new EmployeeBuilder().withId("E0003").withName("Bob Ng")
                .withDepartment("Sales").withRole("Engineer").build();
        AddressBook data = new AddressBook();
        data.setEmployees(List.of(firstAlice, secondAlice, bob));
        model = new ModelManager(data, new UserPrefs());
        employeeStorage = new JsonAddressBookStorage(temporaryFolder.resolve("employees.json"));
        logic = new LogicManager(model, new StorageManager(employeeStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_searchView_preservesMatchesAndSortOrder() throws Exception {
        logic.execute("find Alice");
        logic.execute("sort id desc");
        logic.execute("archive id/e0001");
        assertVisibleIds("E0002");

        String feedback = logic.execute("unarchive id/E0001").getFeedbackToUser();
        assertVisibleIds("E0002", "E0001");
        assertFalse(feedback.contains(UnarchiveCommand.MESSAGE_OUTSIDE_VIEW));
    }

    @Test
    public void execute_filterView_preservesAllCriteria() throws Exception {
        logic.execute("filter d/Engineering r/Engineer");
        logic.execute("archive id/E0001");
        assertVisibleIds();

        logic.execute("unarchive id/E0001");
        assertVisibleIds("E0001");
    }

    @Test
    public void execute_emptySearch_preservesViewAndExplainsHiddenRestoration() throws Exception {
        logic.execute("find Nobody");
        logic.execute("archive id/E0001");
        assertVisibleIds();

        String feedback = logic.execute("unarchive id/E0001").getFeedbackToUser();
        assertVisibleIds();
        assertTrue(feedback.endsWith("\n" + UnarchiveCommand.MESSAGE_OUTSIDE_VIEW));
        logic.execute("list");
        assertVisibleIds("E0001", "E0002", "E0003");
    }

    @Test
    public void execute_archiveView_preservesRemainingRecordsAndEmptyArchive() throws Exception {
        logic.execute("archive id/E0001");
        logic.execute("archive id/E0002");
        logic.execute("list archived");
        logic.execute("sort id desc");
        assertVisibleIds("E0002", "E0001");

        String firstFeedback = logic.execute("unarchive id/E0001").getFeedbackToUser();
        assertVisibleIds("E0002");
        assertTrue(firstFeedback.contains(UnarchiveCommand.MESSAGE_OUTSIDE_VIEW));
        String lastFeedback = logic.execute("unarchive id/E0002").getFeedbackToUser();
        assertVisibleIds();
        assertTrue(lastFeedback.contains(UnarchiveCommand.MESSAGE_OUTSIDE_VIEW));

        logic.execute("archive id/E0001");
        logic.execute("archive id/E0003");
        assertVisibleIds("E0003", "E0001");
    }

    @Test
    public void execute_activeView_preservesSortAndShowsRestoredEmployee() throws Exception {
        logic.execute("sort id desc");
        logic.execute("archive id/E0002");
        assertVisibleIds("E0003", "E0001");

        String feedback = logic.execute("unarchive id/E0002").getFeedbackToUser();
        assertVisibleIds("E0003", "E0002", "E0001");
        assertFalse(feedback.contains(UnarchiveCommand.MESSAGE_OUTSIDE_VIEW));
    }

    @Test
    public void execute_reportingView_preservesRelatedActiveAndArchivedRecords() throws Exception {
        logic.execute("manager id/E0002 m/E0001");
        logic.execute("team id/E0001");
        logic.execute("archive id/E0001");
        assertVisibleIds("E0001", "E0002");
        assertTrue(model.getFilteredEmployeeList().getFirst().isArchived());

        String feedback = logic.execute("unarchive id/E0001").getFeedbackToUser();
        assertVisibleIds("E0001", "E0002");
        assertFalse(feedback.contains(UnarchiveCommand.MESSAGE_OUTSIDE_VIEW));
    }

    @Test
    public void execute_undoAndRedo_preserveArchiveViewAndSavedStatus() throws Exception {
        logic.execute("archive id/E0001");
        logic.execute("list archived");
        logic.execute("unarchive id/E0001");
        assertVisibleIds();
        assertFalse(employeeStorage.readAddressBook().orElseThrow().getEmployeeList().getFirst().isArchived());

        logic.execute("undo");
        assertVisibleIds("E0001");
        assertTrue(employeeStorage.readAddressBook().orElseThrow().getEmployeeList().getFirst().isArchived());
        logic.execute("redo");
        assertVisibleIds();
        assertFalse(employeeStorage.readAddressBook().orElseThrow().getEmployeeList().getFirst().isArchived());
    }

    @Test
    public void execute_failedStatusChange_preservesViewAndRedo() throws Exception {
        logic.execute("archive id/E0001");
        logic.execute("list archived");
        logic.execute("unarchive id/E0001");
        logic.execute("undo");

        assertThrows(CommandException.class, () -> logic.execute("archive id/E0001"));
        assertThrows(CommandException.class, () -> logic.execute("unarchive id/E0002"));
        assertVisibleIds("E0001");
        logic.execute("redo");
        assertVisibleIds();
    }

    private void assertVisibleIds(String... expected) {
        assertEquals(List.of(expected), model.getFilteredEmployeeList().stream()
                .map(employee -> employee.getId().value).toList());
    }
}

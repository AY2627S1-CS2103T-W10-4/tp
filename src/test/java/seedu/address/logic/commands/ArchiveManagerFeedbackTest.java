package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.EmployeeBuilder;

public class ArchiveManagerFeedbackTest {
    @TempDir
    public Path temporaryFolder;

    private final Employee manager = new EmployeeBuilder().withId("Boss").withName("Morgan Manager").build();
    private final Employee firstReport = new EmployeeBuilder().withId("R001").build().withManager(manager.getId());
    private final Employee secondReport = new EmployeeBuilder().withId("R002").build().withManager(manager.getId());

    @Test
    public void execute_noActiveDirectReports_omitsNotice() throws Exception {
        Employee indirectReport = new EmployeeBuilder().withId("R003").build().withManager(firstReport.getId());
        ModelManager model = modelWith(manager, firstReport.withArchived(true), indirectReport);

        String feedback = new ArchiveCommand(manager.getId()).execute(model).getFeedbackToUser();

        assertEquals(String.format(ArchiveCommand.MESSAGE_SUCCESS, Messages.format(manager)), feedback);
        assertEquals(List.of(manager.withArchived(true), firstReport.withArchived(true), indirectReport),
                model.getAddressBook().getEmployeeList());
    }

    @Test
    public void execute_oneActiveDirectReport_usesSingularAndStoredManagerId() throws Exception {
        ModelManager model = modelWith(manager, firstReport);
        model.updateFilteredEmployeeList(employee -> false);

        String feedback = new ArchiveCommand(new EmployeeId("bOSS")).execute(model).getFeedbackToUser();

        assertEquals(String.format(ArchiveCommand.MESSAGE_SUCCESS, Messages.format(manager))
                + "\n1 active employee still reports to this archived manager."
                + "\nUse team id/Boss to review their assignments.", feedback);
        assertEquals(List.of(manager.withArchived(true), firstReport), model.getAddressBook().getEmployeeList());
    }

    @Test
    public void execute_mixedReports_countsOnlyActiveDirectReports() throws Exception {
        Employee archivedReport = new EmployeeBuilder().withId("R003").withArchived(true).build()
                .withManager(new EmployeeId("boss"));
        Employee indirectReport = new EmployeeBuilder().withId("R004").build().withManager(firstReport.getId());
        Employee unrelated = new EmployeeBuilder().withId("OTHER").build();
        ModelManager model = modelWith(manager, firstReport, secondReport, archivedReport, indirectReport, unrelated);
        List<Employee> originalReports = List.copyOf(model.getAddressBook().getEmployeeList().subList(1, 6));

        String feedback = new ArchiveCommand(manager.getId()).execute(model).getFeedbackToUser();

        assertEquals(String.format(ArchiveCommand.MESSAGE_SUCCESS, Messages.format(manager))
                + "\n2 active employees still report to this archived manager."
                + "\nUse team id/Boss to review their assignments.", feedback);
        assertEquals(originalReports, model.getAddressBook().getEmployeeList().subList(1, 6));
    }

    @Test
    public void execute_archiveRestoreAndHistory_preserveSavedRelationships() throws Exception {
        Employee archivedReport = secondReport.withArchived(true);
        ModelManager model = modelWith(manager, firstReport, archivedReport);
        JsonAddressBookStorage employeeStorage = new JsonAddressBookStorage(temporaryFolder.resolve("employees.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(employeeStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        String feedback = logic.execute("archive id/boss").getFeedbackToUser();
        assertTrue(feedback.contains("1 active employee still reports"));
        List<Employee> archivedState = List.of(manager.withArchived(true), firstReport, archivedReport);
        List<Employee> activeState = List.of(manager, firstReport, archivedReport);
        assertEquals(archivedState, employeeStorage.readAddressBook().orElseThrow().getEmployeeList());

        logic.execute("undo");
        assertEquals(activeState, model.getAddressBook().getEmployeeList());
        logic.execute("redo");
        assertEquals(archivedState, model.getAddressBook().getEmployeeList());
        logic.execute("unarchive id/BOSS");
        assertEquals(activeState, model.getAddressBook().getEmployeeList());
        assertEquals(activeState, employeeStorage.readAddressBook().orElseThrow().getEmployeeList());
        logic.execute("undo");
        assertEquals(archivedState, employeeStorage.readAddressBook().orElseThrow().getEmployeeList());
    }

    private ModelManager modelWith(Employee... employees) {
        AddressBook data = new AddressBook();
        data.setEmployees(List.of(employees));
        return new ModelManager(data, new UserPrefs());
    }
}

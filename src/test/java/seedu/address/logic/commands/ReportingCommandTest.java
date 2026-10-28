package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCommand.EditEmployeeDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.Name;
import seedu.address.testutil.EmployeeBuilder;

/** End-to-end command tests with the real parser and model. */
public class ReportingCommandTest {
    private final Employee boss = new EmployeeBuilder().withId("BOSS").withName("Boss").build();
    private final Employee lead = new EmployeeBuilder().withId("LEAD").withName("Lead").build();
    private final Employee worker = new EmployeeBuilder().withId("WORKER").withName("Worker").build();
    private final Employee outsider = new EmployeeBuilder().withId("OTHER").build();
    private Model model;
    private final AddressBookParser parser = new AddressBookParser();

    @BeforeEach
    public void setUp() throws Exception {
        AddressBook data = new AddressBook();
        data.setEmployees(List.of(boss, lead, worker, outsider));
        model = new ModelManager(data, new UserPrefs());
        run("manager id/LEAD m/BOSS");
        run("manager id/WORKER m/LEAD");
    }

    @Test
    public void execute_assignChangeAndUnassign_success() throws Exception {
        model.updateFilteredEmployeeList(employee -> false);
        run("manager id/worker m/boss");
        assertEquals(boss.getId(), find(worker.getId()).getManagerId().orElseThrow());
        run("unassign-manager id/WORKER");
        assertTrue(find(worker.getId()).getManagerId().isEmpty());
        run("unassign-manager id/WORKER");
        assertTrue(find(worker.getId()).getManagerId().isEmpty());
    }

    @Test
    public void execute_selfReportingRejectedWithoutChanges() {
        assertRejected("manager id/BOSS m/boss");
    }

    @Test
    public void execute_indirectCycleRejectedWithoutChanges() {
        assertRejected("manager id/BOSS m/WORKER");
    }

    @Test
    public void execute_directCycleRejectedWithoutChanges() {
        assertRejected("manager id/LEAD m/WORKER");
    }

    @Test
    public void execute_unknownManagerRejectedWithoutChanges() {
        assertRejected("manager id/WORKER m/MISSING");
    }

    @Test
    public void execute_unknownEmployeeRejectedWithoutChanges() {
        for (String command : List.of("manager id/MISSING m/BOSS", "unassign-manager id/MISSING",
                "team id/MISSING", "org id/MISSING", "chain id/MISSING")) {
            assertRejected(command);
        }
    }

    @Test
    public void execute_queriesUseWholeRosterAndDoNotChangeData() throws Exception {
        AddressBook original = new AddressBook(model.getAddressBook());
        model.updateFilteredEmployeeList(employee -> false);
        run("team id/BOSS");
        assertEquals(List.of(boss, find(lead.getId())), model.getFilteredEmployeeList());
        run("org id/BOSS");
        assertEquals(List.of(find(lead.getId()), find(worker.getId())), model.getFilteredEmployeeList());
        String chain = run("chain id/WORKER").getFeedbackToUser();
        assertTrue(chain.contains("Lead (LEAD) -> Boss (BOSS)"));
        assertEquals(List.of(boss, find(lead.getId())), model.getFilteredEmployeeList());
        assertEquals(original, model.getAddressBook());
        run("list");
        assertEquals(4, model.getFilteredEmployeeList().size());
    }

    @Test
    public void execute_emptyTeamAndTopLevelChain_success() throws Exception {
        assertTrue(run("team id/WORKER").getFeedbackToUser().contains("Direct reports: 0"));
        assertEquals(List.of(find(lead.getId()), find(worker.getId())), model.getFilteredEmployeeList());
        assertTrue(run("org id/WORKER").getFeedbackToUser().contains("None."));
        assertTrue(run("chain id/BOSS").getFeedbackToUser().contains("None."));
    }

    @Test
    public void execute_queriesIncludeArchivedReportsAndManagers_matchingMessagesAndSortedCards() throws Exception {
        for (int archivedMask = 1; archivedMask < 8; archivedMask++) {
            setUp();
            List<Employee> hierarchy = List.of(boss, lead, worker);
            for (int i = 0; i < hierarchy.size(); i++) {
                if ((archivedMask & (1 << i)) != 0) {
                    run("archive id/" + hierarchy.get(i).getId());
                }
            }
            AddressBook original = new AddressBook(model.getAddressBook());
            run("sort id desc");
            String team = run("team id/LEAD").getFeedbackToUser();
            assertEquals("Team of Lead (LEAD): Manager: Boss (BOSS); Direct reports: 1. Worker (WORKER)", team);
            assertEquals(List.of(find(worker.getId()), find(lead.getId()), find(boss.getId())),
                    model.getFilteredEmployeeList());
            assertTrue(run("org id/BOSS").getFeedbackToUser().contains("Lead (LEAD), Worker (WORKER)"));
            assertEquals(List.of(find(worker.getId()), find(lead.getId())), model.getFilteredEmployeeList());
            assertTrue(run("chain id/WORKER").getFeedbackToUser().contains("Lead (LEAD) -> Boss (BOSS)"));
            assertEquals(List.of(find(lead.getId()), find(boss.getId())), model.getFilteredEmployeeList());
            assertEquals(original, model.getAddressBook());
            run("list");
            assertTrue(model.getFilteredEmployeeList().stream().noneMatch(Employee::isArchived));
        }
    }

    @Test
    public void execute_editPreservesManager() throws Exception {
        EditEmployeeDescriptor descriptor = new EditEmployeeDescriptor();
        descriptor.setName(new Name("Updated Worker"));
        new EditCommand(worker.getId(), descriptor).execute(model);
        assertEquals(lead.getId(), find(worker.getId()).getManagerId().orElseThrow());
    }

    @Test
    public void execute_deleteManagerUnassignsOnlyDirectReports() throws Exception {
        assertTrue(run("delete id/LEAD").getFeedbackToUser().contains("1 direct report now has no manager."));
        assertTrue(find(worker.getId()).getManagerId().isEmpty());
        assertEquals(3, model.getAddressBook().getEmployeeList().size());
    }

    @Test
    public void execute_deleteManagerWithMultipleDirectReports_usesPluralFeedback() throws Exception {
        run("manager id/OTHER m/BOSS");

        assertTrue(run("delete id/BOSS").getFeedbackToUser().contains("2 direct reports now have no manager."));
    }

    @Test
    public void execute_deleteEmployeeWithoutReports_omitsReportFeedback() throws Exception {
        String feedback = run("delete id/OTHER").getFeedbackToUser();

        assertFalse(feedback.contains("direct report"));
    }

    @Test
    public void execute_teamView_refreshesAfterRelationshipChangesAndPreservesSort() throws Exception {
        run("sort id desc");
        run("team id/BOSS");
        assertEquals(List.of(find(lead.getId()), boss), model.getFilteredEmployeeList());

        run("manager id/OTHER m/BOSS");
        assertEquals(List.of(outsider.withManager(boss.getId()), find(lead.getId()), boss),
                model.getFilteredEmployeeList());
        run("unassign-manager id/LEAD");
        assertEquals(List.of(find(outsider.getId()), boss), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_orgAndChainViews_refreshAfterRelationshipChanges() throws Exception {
        run("org id/BOSS");
        run("manager id/LEAD m/OTHER");
        assertEquals(List.of(), model.getFilteredEmployeeList());

        run("chain id/WORKER");
        assertEquals(List.of(find(lead.getId()), outsider), model.getFilteredEmployeeList());
        run("unassign-manager id/WORKER");
        assertEquals(List.of(), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_reportingView_refreshesAfterEmployeeDeletion() throws Exception {
        run("chain id/WORKER");

        run("delete id/LEAD");
        assertEquals(List.of(), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_deleteRootPreservesLowerRelationships() throws Exception {
        run("delete id/BOSS");
        assertTrue(find(lead.getId()).getManagerId().isEmpty());
        assertEquals(lead.getId(), find(worker.getId()).getManagerId().orElseThrow());
    }

    @Test
    public void execute_teamIncludesEmployeeManagerAndReports() throws Exception {
        String message = run("team id/lead").getFeedbackToUser();
        assertEquals(List.of(boss, find(lead.getId()), find(worker.getId())), model.getFilteredEmployeeList());
        assertTrue(message.contains("Manager: Boss (BOSS)"));
        assertTrue(message.contains("Direct reports: 1"));
        assertTrue(run("team id/OTHER").getFeedbackToUser().contains("Manager: None; Direct reports: 0"));
        assertEquals(List.of(outsider), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_lowerCaseAssignmentUsesStoredIds() throws Exception {
        assertEquals("Manager of WORKER set to BOSS.", run("manager id/worker m/boss").getFeedbackToUser());
        assertEquals("BOSS", find(worker.getId()).getManagerId().orElseThrow().value);
        assertEquals("Manager of WORKER unassigned.", run("unassign-manager id/worker").getFeedbackToUser());
    }

    @Test
    public void execute_addAndEditManager_success() throws Exception {
        run("add id/NEW n/New Person p/91234567 e/new@example.com d/Engineering r/Engineer m/boss");
        assertEquals("BOSS", find(new EmployeeId("NEW")).getManagerId().orElseThrow().value);
        run("edit id/new m/lead");
        assertEquals("LEAD", find(new EmployeeId("NEW")).getManagerId().orElseThrow().value);
        run("edit id/new m/");
        assertTrue(find(new EmployeeId("NEW")).getManagerId().isEmpty());
    }

    @Test
    public void execute_invalidAddAndEditLeaveRosterAndViewUnchanged() {
        assertRejected("edit id/BOSS m/WORKER");
        assertRejected("edit id/WORKER m/worker");
        assertRejected("edit id/WORKER m/MISSING");
        assertRejected("add id/NEW n/New Person p/91234567 e/new@example.com d/Engineering r/Engineer m/MISSING");
        assertRejected("add id/NEW n/New Person p/91234567 e/new@example.com d/Engineering r/Engineer m/NEW");
    }

    @Test
    public void execute_managerAndArchiveChangesPreserveEachOther() throws Exception {
        Employee assigned = find(worker.getId());
        assertEquals(assigned.withArchived(true), assigned.withArchived(true).withManager(lead.getId()));
        run("archive id/WORKER");
        run("manager id/WORKER m/BOSS");
        Employee archived = find(worker.getId());
        assertEquals(assigned.withManager(boss.getId()).withArchived(true), archived);
        run("edit id/WORKER m/LEAD");
        assertTrue(find(worker.getId()).isArchived());
        run("unarchive id/WORKER");
        assertEquals(assigned, find(worker.getId()));
    }

    @Test
    public void execute_clearUnassignsArchivedReportsOfRemovedManagers() throws Exception {
        run("archive id/WORKER");
        run("clear");
        assertEquals(List.of(worker.withArchived(true)), model.getAddressBook().getEmployeeList());
        run("unarchive id/WORKER");
        assertEquals(List.of(worker), model.getFilteredEmployeeList());
    }

    private CommandResult run(String command) throws Exception {
        return parser.parseCommand(command).execute(model);
    }

    private Employee find(EmployeeId id) {
        return model.getAddressBook().getEmployeeList().stream()
                .filter(employee -> employee.getId().equals(id)).findFirst().orElseThrow();
    }

    private void assertRejected(String command) {
        AddressBook original = new AddressBook(model.getAddressBook());
        List<Employee> originalView = List.copyOf(model.getFilteredEmployeeList());
        assertThrows(CommandException.class, () -> run(command));
        assertEquals(original, model.getAddressBook());
        assertEquals(originalView, model.getFilteredEmployeeList());
    }
}

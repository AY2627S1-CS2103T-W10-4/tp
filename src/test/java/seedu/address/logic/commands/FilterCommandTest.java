package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.getEmployeesListedOverview;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.BENSON;
import static seedu.address.testutil.TypicalEmployees.CARL;
import static seedu.address.testutil.TypicalEmployees.FIONA;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeMatchesCriteriaPredicate;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.EmployeeBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code FilterCommand}.
 */
public class FilterCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private static EmployeeMatchesCriteriaPredicate predicateOf(List<String> departments, List<String> roles) {
        return new EmployeeMatchesCriteriaPredicate(List.of(), List.of(), departments, roles);
    }

    private static EmployeeMatchesCriteriaPredicate predicateOf(List<String> names, List<String> departments,
            List<String> roles) {
        return new EmployeeMatchesCriteriaPredicate(List.of(), names, departments, roles);
    }

    @Test
    public void equals() {
        FilterCommand filterFirstCommand = new FilterCommand(predicateOf(List.of("first"), List.of()));
        FilterCommand filterSecondCommand = new FilterCommand(predicateOf(List.of("second"), List.of()));

        // same object -> returns true
        assertTrue(filterFirstCommand.equals(filterFirstCommand));

        // same values -> returns true
        assertTrue(filterFirstCommand.equals(new FilterCommand(predicateOf(List.of("first"), List.of()))));

        // different types -> returns false
        assertFalse(filterFirstCommand.equals(1));

        // null -> returns false
        assertFalse(filterFirstCommand.equals(null));

        // different predicate -> returns false
        assertFalse(filterFirstCommand.equals(filterSecondCommand));
    }

    @Test
    public void hashCode_equalCommands_sameHashCodeAndFoundInHashSet() {
        FilterCommand filterFirstCommand = new FilterCommand(predicateOf(List.of("first"), List.of()));
        // the case of a word is ignored, so this command is equal
        FilterCommand sameCommand = new FilterCommand(predicateOf(List.of("First"), List.of()));
        FilterCommand filterSecondCommand = new FilterCommand(predicateOf(List.of("second"), List.of()));
        assertEquals(filterFirstCommand, sameCommand);

        assertEquals(filterFirstCommand.hashCode(), sameCommand.hashCode());

        Set<FilterCommand> commands = new HashSet<>(List.of(filterFirstCommand));
        assertTrue(commands.contains(sameCommand));
        commands.add(sameCommand);
        assertEquals(1, commands.size());
        commands.add(filterSecondCommand);
        assertEquals(2, commands.size());
    }

    @Test
    public void execute_departmentOnly_oneEmployeeFound() {
        EmployeeMatchesCriteriaPredicate predicate = predicateOf(List.of("Finance"), List.of());
        expectedModel.updateFilteredEmployeeList(predicate);
        assertCommandSuccess(new FilterCommand(predicate), model, getEmployeesListedOverview(1), expectedModel);
        assertEquals(List.of(CARL), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_roleOnly_multipleEmployeesFound() {
        EmployeeMatchesCriteriaPredicate predicate = predicateOf(List.of(), List.of("Executive"));
        expectedModel.updateFilteredEmployeeList(predicate);
        assertCommandSuccess(new FilterCommand(predicate), model, getEmployeesListedOverview(2), expectedModel);
        assertEquals(List.of(BENSON, FIONA), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_departmentAndRole_onlyEmployeesMatchingBothFound() {
        EmployeeMatchesCriteriaPredicate predicate = predicateOf(List.of("Marketing"), List.of("Executive"));
        expectedModel.updateFilteredEmployeeList(predicate);
        assertCommandSuccess(new FilterCommand(predicate), model, getEmployeesListedOverview(1), expectedModel);
        assertEquals(List.of(FIONA), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_multipleWordDepartment_employeeFound() {
        EmployeeMatchesCriteriaPredicate predicate = predicateOf(List.of("Human Resources"), List.of());
        expectedModel.updateFilteredEmployeeList(predicate);
        assertCommandSuccess(new FilterCommand(predicate), model, getEmployeesListedOverview(1), expectedModel);
        assertEquals(List.of(BENSON), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_idOnly_oneEmployeeFound() {
        EmployeeMatchesCriteriaPredicate predicate = new EmployeeMatchesCriteriaPredicate(
                List.of(CARL.getId()), List.of(), List.of(), List.of());
        expectedModel.updateFilteredEmployeeList(predicate);
        assertCommandSuccess(new FilterCommand(predicate), model, getEmployeesListedOverview(1), expectedModel);
        assertEquals(List.of(CARL), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_severalValuesOfOneCriterion_employeesMatchingAnyValueFound() {
        // (Finance OR Marketing) AND Executive
        EmployeeMatchesCriteriaPredicate predicate = predicateOf(List.of("Finance", "Marketing"),
                List.of("Executive", "Accountant"));
        expectedModel.updateFilteredEmployeeList(predicate);
        assertCommandSuccess(new FilterCommand(predicate), model, getEmployeesListedOverview(2), expectedModel);
        assertEquals(List.of(CARL, FIONA), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_nameAndDepartment_excludesOtherNamesAndOtherDepartments() {
        Employee johnInSales = new EmployeeBuilder().withId("J1").withName("John Tan").withDepartment("Sales").build();
        Employee johnInFinance = new EmployeeBuilder().withId("J2").withName("John Lee")
                .withDepartment("Finance").build();
        Employee maryInSales = new EmployeeBuilder().withId("M1").withName("Mary Tan").withDepartment("Sales").build();
        Model peopleModel = new ModelManager(new AddressBookBuilder().withEmployee(johnInSales)
                .withEmployee(johnInFinance).withEmployee(maryInSales).build(), new UserPrefs());

        new FilterCommand(predicateOf(List.of("John"), List.of("Sales"), List.of())).execute(peopleModel);

        // not Mary, who is in Sales, and not the John in Finance
        assertEquals(List.of(johnInSales), peopleModel.getFilteredEmployeeList());
    }

    @Test
    public void execute_departmentWithPunctuation_employeeFound() {
        Employee researchAndDevelopment = new EmployeeBuilder().withId("R1").withDepartment("R&D, Asia")
                .withRole("Engineer (Backend)").build();
        Model peopleModel = new ModelManager(new AddressBookBuilder().withEmployee(researchAndDevelopment)
                .withEmployee(ALICE).build(), new UserPrefs());

        new FilterCommand(predicateOf(List.of("R&D"), List.of("(Backend)"))).execute(peopleModel);
        assertEquals(List.of(researchAndDevelopment), peopleModel.getFilteredEmployeeList());

        new FilterCommand(predicateOf(List.of(), List.of("Backend"))).execute(peopleModel);
        assertEquals(List.of(researchAndDevelopment), peopleModel.getFilteredEmployeeList());
    }

    @Test
    public void execute_archivedEmployees_areNotShown() {
        model.setEmployee(CARL, CARL.withArchived(true));
        new FilterCommand(predicateOf(List.of("Finance"), List.of())).execute(model);
        assertEquals(List.of(), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_noMatch_noEmployeeFound() {
        EmployeeMatchesCriteriaPredicate predicate = predicateOf(List.of("Sales"), List.of("Executive"));
        expectedModel.updateFilteredEmployeeList(predicate);
        assertCommandSuccess(new FilterCommand(predicate), model, getEmployeesListedOverview(0), expectedModel);
        assertEquals(List.of(), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_filter_doesNotChangeStoredData() throws Exception {
        new FilterCommand(predicateOf(List.of("Finance"), List.of())).execute(model);
        assertEquals(getTypicalAddressBook(), model.getAddressBook());
    }

    @Test
    public void execute_listAfterFilter_showsEveryEmployeeAgain() throws Exception {
        new FilterCommand(predicateOf(List.of("Finance"), List.of())).execute(model);
        assertEquals(1, model.getFilteredEmployeeList().size());

        new ListCommand().execute(model);
        assertEquals(getTypicalAddressBook().getEmployeeList(), model.getFilteredEmployeeList());
    }

    @Test
    public void toStringMethod() {
        EmployeeMatchesCriteriaPredicate predicate = predicateOf(List.of("Sales"), List.of());
        FilterCommand filterCommand = new FilterCommand(predicate);
        String expected = FilterCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, filterCommand.toString());
    }
}

package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.testutil.EmployeeBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_newEmployee_success() {
        Employee validEmployee = new EmployeeBuilder().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addEmployee(validEmployee);

        assertCommandSuccess(new AddCommand(validEmployee), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validEmployee)),
                expectedModel);
    }

    @Test
    public void execute_duplicateEmployee_throwsCommandException() {
        Employee employeeInList = model.getAddressBook().getEmployeeList().get(0);
        assertCommandFailure(new AddCommand(employeeInList), model,
                String.format(AddCommand.MESSAGE_DUPLICATE_EMPLOYEE, employeeInList.getId()));
    }

    @Test
    public void execute_sameIdDifferentCase_throwsCommandException() {
        Employee employeeInList = model.getAddressBook().getEmployeeList().get(0);
        Employee sameIdInLowerCase = new EmployeeBuilder().withName("Someone Else")
                .withId(employeeInList.getId().value.toLowerCase()).build();
        assertCommandFailure(new AddCommand(sameIdInLowerCase), model,
                String.format(AddCommand.MESSAGE_DUPLICATE_EMPLOYEE, sameIdInLowerCase.getId()));
    }

    @Test
    public void execute_similarNameDifferentId_successWithWarning() {
        Employee employeeInList = model.getAddressBook().getEmployeeList().get(0);
        Employee sameNameDifferentCase = new EmployeeBuilder().withId("E7777")
                .withName(employeeInList.getName().fullName.toUpperCase()).build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addEmployee(sameNameDifferentCase);

        String expectedMessage = String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(sameNameDifferentCase))
                + "\n" + String.format(AddCommand.MESSAGE_SIMILAR_NAME_WARNING, employeeInList.getId());
        assertCommandSuccess(new AddCommand(sameNameDifferentCase), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_uniqueName_noWarning() {
        Employee validEmployee = new EmployeeBuilder().withId("E8888").withName("Completely Unique Name").build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addEmployee(validEmployee);

        assertCommandSuccess(new AddCommand(validEmployee), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validEmployee)), expectedModel);
    }

}

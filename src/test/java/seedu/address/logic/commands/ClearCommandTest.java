package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.BENSON;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;

public class ClearCommandTest {

    @Test
    public void execute_emptyAddressBook_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nonEmptyAddressBook_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.clearEmployees();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_activeAndArchivedEmployees_clearsOnlyActiveEmployees() throws Exception {
        AddressBook addressBook = getTypicalAddressBook();
        Employee archivedAlice = ALICE.withArchived(true);
        addressBook.setEmployee(ALICE, archivedAlice);
        Model model = new ModelManager(addressBook, new UserPrefs());

        new ClearCommand().execute(model);

        assertTrue(model.getFilteredEmployeeList().isEmpty());
        assertEquals(java.util.List.of(archivedAlice), model.getAddressBook().getEmployeeList());
    }

    @Test
    public void execute_onlyArchivedEmployees_preservesArchive() throws Exception {
        AddressBook addressBook = new AddressBook();
        Employee archivedBenson = BENSON.withArchived(true);
        addressBook.addEmployee(archivedBenson);
        Model model = new ModelManager(addressBook, new UserPrefs());

        new ClearCommand().execute(model);

        assertTrue(model.getFilteredEmployeeList().isEmpty());
        assertEquals(java.util.List.of(archivedBenson), model.getAddressBook().getEmployeeList());
    }

}

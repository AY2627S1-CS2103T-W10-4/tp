package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.testutil.EmployeeBuilder;

public class EmployeeCommandSupportTest {
    @Test
    public void find_hiddenActiveAndArchivedEmployees_returnsStoredRecordsWithoutChangingView() throws Exception {
        Employee active = new EmployeeBuilder().withId("Active").build();
        Employee archived = new EmployeeBuilder().withId("Archived").withArchived(true).build();
        AddressBook addressBook = new AddressBook();
        addressBook.setEmployees(List.of(active, archived));
        Model model = new ModelManager(addressBook, new UserPrefs());
        model.updateFilteredEmployeeList(employee -> false);

        assertSame(active, EmployeeCommandSupport.find(model, new EmployeeId("ACTIVE")));
        assertSame(archived, EmployeeCommandSupport.find(model, new EmployeeId("archived")));
        assertTrue(model.getFilteredEmployeeList().isEmpty());

        model.showArchivedEmployees();
        assertSame(active, EmployeeCommandSupport.find(model, new EmployeeId("active")));
        assertEquals(List.of(archived), model.getFilteredEmployeeList());
        assertEquals(List.of(active, archived), model.getAddressBook().getEmployeeList());
    }

    @Test
    public void find_unknownId_preservesEnteredSpellingInError() {
        Model model = new ModelManager();
        CommandException exception = assertThrows(CommandException.class, () ->
                EmployeeCommandSupport.find(model, new EmployeeId("Unknown-Id")));

        assertEquals("No employee with ID Unknown-Id was found.", exception.getMessage());
        assertTrue(model.getAddressBook().getEmployeeList().isEmpty());
    }
}

package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalEmployees.ALICE;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.EmployeeId;

public class ArchiveCommandsTest {
    @Test
    public void archiveAndUnarchive_validEmployee_success() throws Exception {
        ModelManager model = modelWithAlice();
        new ArchiveCommand(ALICE.getId()).execute(model);
        assertTrue(model.getAddressBook().getEmployeeList().getFirst().isArchived());
        assertTrue(model.getFilteredEmployeeList().isEmpty());

        new UnarchiveCommand(new EmployeeId("e0001")).execute(model);
        assertFalse(model.getAddressBook().getEmployeeList().getFirst().isArchived());
        assertEquals(ALICE, model.getFilteredEmployeeList().getFirst());
    }

    @Test
    public void archive_unknownEmployee_failure() {
        ModelManager model = modelWithAlice();
        assertThrows(CommandException.class, String.format(Messages.MESSAGE_EMPLOYEE_NOT_FOUND, "MISSING"), () ->
                new ArchiveCommand(new EmployeeId("MISSING")).execute(model));
    }

    @Test
    public void archive_alreadyArchived_failure() throws Exception {
        ModelManager model = modelWithAlice();
        new ArchiveCommand(ALICE.getId()).execute(model);
        assertThrows(CommandException.class,
                String.format(ArchiveCommand.MESSAGE_ALREADY_ARCHIVED, ALICE.getId()), () ->
                new ArchiveCommand(ALICE.getId()).execute(model));
    }

    @Test
    public void unarchive_unknownEmployee_failure() {
        ModelManager model = modelWithAlice();
        assertThrows(CommandException.class, String.format(Messages.MESSAGE_EMPLOYEE_NOT_FOUND, "MISSING"), () ->
                new UnarchiveCommand(new EmployeeId("MISSING")).execute(model));
    }

    @Test
    public void unarchive_activeEmployee_failure() {
        ModelManager model = modelWithAlice();
        assertThrows(CommandException.class,
                String.format(UnarchiveCommand.MESSAGE_ALREADY_ACTIVE, ALICE.getId()), () ->
                new UnarchiveCommand(ALICE.getId()).execute(model));
    }

    private static ModelManager modelWithAlice() {
        AddressBook addressBook = new AddressBook();
        addressBook.addEmployee(ALICE);
        return new ModelManager(addressBook, new UserPrefs());
    }
}

package seedu.address.logic.commands;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;

/** Resolves employee IDs against the complete roster for commands. */
final class EmployeeCommandSupport {
    private EmployeeCommandSupport() {}

    /** Returns the stored employee, including archived employees and employees hidden by the current view. */
    static Employee find(Model model, EmployeeId id) throws CommandException {
        return model.getAddressBook().getEmployeeList().stream()
                .filter(employee -> employee.getId().equals(id)).findFirst()
                .orElseThrow(() -> new CommandException(String.format(Messages.MESSAGE_EMPLOYEE_NOT_FOUND, id)));
    }
}

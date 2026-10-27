package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.employee.Employee;

/**
 * A UI component that displays information of a {@code Employee}.
 */
public class EmployeeCard extends UiPart<Region> {

    private static final String FXML = "EmployeeListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label employeeId;
    @FXML
    private Label contact;
    @FXML
    private Label department;
    @FXML
    private Label role;
    @FXML
    private Label manager;
    @FXML
    private Label archivedBadge;
    @FXML
    private Label avatar;

    /**
     * Creates a {@code EmployeeCard} with the given {@code Employee} and index to display.
     */
    public EmployeeCard(Employee employee, int displayedIndex) {
        super(FXML);
        id.setText(displayedIndex + ". ");
        name.setText(employee.getName().fullName);
        employeeId.setText(employee.getId().value);
        contact.setText(employee.getPhone().value + "  \u00B7  " + employee.getEmail().value);
        avatar.setText(Initials.of(employee.getName().fullName));
        avatar.setStyle(DepartmentColour.chipStyle(employee.getDepartment().value));
        department.setText(employee.getDepartment().value);
        department.setStyle(DepartmentColour.chipStyle(employee.getDepartment().value));
        archivedBadge.setVisible(employee.isArchived());
        archivedBadge.setManaged(employee.isArchived());
        role.setText(employee.getRole().value);
        // a value that is too long for the card is cut short, so the whole of it is shown in a tooltip
        Tooltip.install(name, new Tooltip(employee.getName().fullName));
        Tooltip.install(role, new Tooltip(employee.getRole().value));
        Tooltip.install(contact, new Tooltip("Phone: " + employee.getPhone().value + "\nEmail: "
                + employee.getEmail().value));
        Tooltip.install(department, new Tooltip(employee.getDepartment().value));
        employee.getManagerId().ifPresent(managerId -> {
            manager.setText("Reports to " + managerId);
            manager.setTooltip(new Tooltip(manager.getText()));
            manager.setVisible(true);
            manager.setManaged(true);
        });
    }
}

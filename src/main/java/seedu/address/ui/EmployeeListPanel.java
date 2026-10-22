package seedu.address.ui;

import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.employee.Employee;

/**
 * Panel containing the list of employees.
 */
public class EmployeeListPanel extends UiPart<Region> {
    private static final String FXML = "EmployeeListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(EmployeeListPanel.class);

    @FXML
    private ListView<Employee> employeeListView;

    /**
     * Creates a {@code EmployeeListPanel} with the given {@code ObservableList}.
     */
    public EmployeeListPanel(ObservableList<Employee> employeeList) {
        super(FXML);
        employeeListView.setItems(employeeList);
        employeeListView.setCellFactory(listView -> new EmployeeListViewCell());
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Employee} using a {@code EmployeeCard}.
     */
    class EmployeeListViewCell extends ListCell<Employee> {

        EmployeeListViewCell() {
            // the width of a cell does not depend on its card, so that a long value does not make the list scroll
            setPrefWidth(0);
        }

        @Override
        protected void updateItem(Employee employee, boolean empty) {
            super.updateItem(employee, empty);

            if (empty || employee == null) {
                setGraphic(null);
                setText(null);
            } else {
                Region card = new EmployeeCard(employee, getIndex() + 1).getRoot();
                // as wide as the cell and no wider, so that long text is cut short instead
                card.prefWidthProperty().bind(widthProperty());
                card.setMinWidth(0);
                card.setMaxWidth(Region.USE_PREF_SIZE);
                setGraphic(card);
            }
        }
    }

}

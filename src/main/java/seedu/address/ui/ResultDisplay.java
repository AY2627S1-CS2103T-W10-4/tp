package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";
    private static final String SUCCESS_STYLE_CLASS = "result-success";
    private static final String ERROR_STYLE_CLASS = "result-error";

    @FXML
    private TextArea resultDisplay;

    public ResultDisplay() {
        super(FXML);
    }

    /**
     * Shows the result of a command that worked.
     */
    public void setFeedbackToUser(String feedbackToUser) {
        show(feedbackToUser, false);
    }

    /**
     * Shows the message of a command that failed, in the colour of an error.
     */
    public void setErrorToUser(String errorMessage) {
        show(errorMessage, true);
    }

    private void show(String message, boolean isError) {
        requireNonNull(message);
        resultDisplay.setText(message);
        resultDisplay.getStyleClass().removeAll(SUCCESS_STYLE_CLASS, ERROR_STYLE_CLASS);
        resultDisplay.getStyleClass().add(isError ? ERROR_STYLE_CLASS : SUCCESS_STYLE_CLASS);
    }

}

package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    /**
     * The smallest height of the text area. With the 10 pixels around it in the main window, the result display is
     * at least 100 pixels high, as it was when its height was fixed.
     */
    static final double MIN_HEIGHT = 90;

    /** The largest height of the text area. A longer result is read by scrolling. */
    static final double MAX_HEIGHT = 340;

    /** The space above and below the text in the text area. */
    static final double VERTICAL_SPACE = 40;

    private static final String FXML = "ResultDisplay.fxml";
    private static final String SUCCESS_STYLE_CLASS = "result-success";
    private static final String ERROR_STYLE_CLASS = "result-error";

    /** The space left and right of the text: the padding, the stripe at the left and the scroll bar. */
    private static final double HORIZONTAL_SPACE = 48;

    @FXML
    private TextArea resultDisplay;

    /**
     * Creates a {@code ResultDisplay} that is as high as the text it shows.
     */
    public ResultDisplay() {
        super(FXML);
        resultDisplay.setMinHeight(MIN_HEIGHT);
        // the height depends on how much text fits in a line, and on the font, which are only known after layout
        resultDisplay.widthProperty().addListener((observable, oldWidth, newWidth) -> updateHeight());
        resultDisplay.fontProperty().addListener((observable, oldFont, newFont) -> updateHeight());
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
        updateHeight();
    }

    /**
     * Returns the height of the text area for a text that is {@code textHeight} high: as high as the text, but not
     * lower than {@link #MIN_HEIGHT} and not higher than {@link #MAX_HEIGHT}.
     */
    static double getHeightFor(double textHeight) {
        return Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, textHeight + VERTICAL_SPACE));
    }

    /**
     * Makes the text area as high as its text, so that a result of several lines, such as that of {@code stats},
     * can be read without scrolling in a very small box. Does nothing before the width of the text area is known.
     */
    private void updateHeight() {
        double width = resultDisplay.getWidth();
        if (width <= 0) {
            return;
        }
        Text measuredText = new Text(resultDisplay.getText());
        measuredText.setFont(resultDisplay.getFont());
        measuredText.setWrappingWidth(Math.max(1, width - HORIZONTAL_SPACE));
        // the minimum too, or the box of a crowded window is squeezed by the list below it
        double height = getHeightFor(measuredText.getLayoutBounds().getHeight());
        resultDisplay.setMinHeight(height);
        resultDisplay.setPrefHeight(height);
    }

}

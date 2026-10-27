package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ResultDisplayTest {

    @Test
    public void getHeightFor_shortText_returnsMinimumHeight() {
        assertEquals(ResultDisplay.MIN_HEIGHT, ResultDisplay.getHeightFor(0));
        assertEquals(ResultDisplay.MIN_HEIGHT, ResultDisplay.getHeightFor(20));
    }

    @Test
    public void getHeightFor_textBetweenLimits_fitsText() {
        double heightBetweenLimits = (ResultDisplay.MIN_HEIGHT + ResultDisplay.MAX_HEIGHT) / 2;
        double textHeight = heightBetweenLimits - 40;
        assertEquals(heightBetweenLimits, ResultDisplay.getHeightFor(textHeight));
    }

    @Test
    public void getHeightFor_longText_returnsMaximumHeight() {
        assertEquals(ResultDisplay.MAX_HEIGHT, ResultDisplay.getHeightFor(ResultDisplay.MAX_HEIGHT));
        assertEquals(ResultDisplay.MAX_HEIGHT, ResultDisplay.getHeightFor(5000));
    }
}

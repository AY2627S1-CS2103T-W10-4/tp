package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;

public class CsvUtilTest {

    @Test
    public void toCsvLine_plainValues_joinedWithCommas() {
        assertEquals("E0001,Alice,Engineering", CsvUtil.toCsvLine(List.of("E0001", "Alice", "Engineering")));
    }

    @Test
    public void toCsvLine_valueWithComma_quoted() {
        assertEquals("E0001,\"Tan, Ah Kow\"", CsvUtil.toCsvLine(List.of("E0001", "Tan, Ah Kow")));
    }

    @Test
    public void toCsvLine_valueWithQuote_quotedAndQuoteDoubled() {
        assertEquals("\"say \"\"hi\"\"\"", CsvUtil.toCsvLine(List.of("say \"hi\"")));
    }

    @Test
    public void toCsvLine_emptyValues_keptAsEmpty() {
        assertEquals(",,", CsvUtil.toCsvLine(List.of("", "", "")));
    }

    @Test
    public void parseCsvLine_plainValues_splitOnCommas() throws Exception {
        assertEquals(List.of("E0001", "Alice", "Engineering"), CsvUtil.parseCsvLine("E0001,Alice,Engineering"));
    }

    @Test
    public void parseCsvLine_quotedValueWithComma_oneValue() throws Exception {
        assertEquals(List.of("E0001", "Tan, Ah Kow"), CsvUtil.parseCsvLine("E0001,\"Tan, Ah Kow\""));
    }

    @Test
    public void parseCsvLine_doubledQuoteInsideQuotes_oneQuote() throws Exception {
        assertEquals(List.of("say \"hi\""), CsvUtil.parseCsvLine("\"say \"\"hi\"\"\""));
    }

    @Test
    public void parseCsvLine_emptyValues_keptAsEmpty() throws Exception {
        assertEquals(List.of("", "", ""), CsvUtil.parseCsvLine(",,"));
        assertEquals(List.of(""), CsvUtil.parseCsvLine(""));
    }

    @Test
    public void parseCsvLine_unclosedQuote_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, CsvUtil.MESSAGE_UNCLOSED_QUOTE, () ->
                CsvUtil.parseCsvLine("E0001,\"Tan, Ah Kow"));
    }

    @Test
    public void parseCsvLine_outputOfToCsvLine_sameValues() throws Exception {
        List<String> values = List.of("E0001", "O'Brien, \"Jim\"", "R&D, Asia", "");
        assertEquals(values, CsvUtil.parseCsvLine(CsvUtil.toCsvLine(values)));
    }

    @Test
    public void parseCsvLine_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> CsvUtil.parseCsvLine(null));
    }

}

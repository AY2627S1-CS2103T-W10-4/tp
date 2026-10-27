package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.CsvUtil.CsvRecord;

public class CsvUtilTest {

    @Test
    public void splitCsvRecords_multilineAndEscapedQuotes_retainsTextAndStartingLines() throws Exception {
        for (String ending : List.of("\n", "\r\n", "\r")) {
            String employee = "E1,\"Alice" + ending + "\"\"A\"\" Smith\"";
            String csv = "id,name" + ending + employee + ending + ending + "E2,Bob" + ending;

            List<CsvRecord> records = CsvUtil.splitCsvRecords(csv);

            assertEquals(List.of(new CsvRecord("id,name", 1), new CsvRecord(employee, 2),
                    new CsvRecord("", 4), new CsvRecord("E2,Bob", 5)), records);
            assertEquals(List.of("E1", "Alice" + ending + "\"A\" Smith"),
                    CsvUtil.parseCsvLine(records.get(1).text()));
        }
    }

    @Test
    public void splitCsvRecords_unclosedQuotedField_retainedForValidation() {
        String record = "E1,\"Alice\nSmith\n";
        List<CsvRecord> records = CsvUtil.splitCsvRecords("id,name\n" + record);
        assertEquals(List.of(new CsvRecord("id,name", 1), new CsvRecord(record, 2)), records);
        assertThrows(IllegalValueException.class, CsvUtil.MESSAGE_UNCLOSED_QUOTE, () ->
                CsvUtil.parseCsvLine(records.get(1).text()));
    }

    @Test
    public void splitCsvRecords_emptyAndBlankInput_retainsOnlyActualRecords() {
        assertEquals(List.of(), CsvUtil.splitCsvRecords(""));
        assertEquals(List.of(new CsvRecord("", 1), new CsvRecord("  ", 2)),
                CsvUtil.splitCsvRecords("\n  \r\n"));
        assertThrows(NullPointerException.class, () -> CsvUtil.splitCsvRecords(null));
    }

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
    public void parseCsvLine_quoteInsideUnquotedValue_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, CsvUtil.MESSAGE_MISPLACED_QUOTE, () ->
                CsvUtil.parseCsvLine("E0001,A\"li\"ce"));
        assertThrows(IllegalValueException.class, CsvUtil.MESSAGE_MISPLACED_QUOTE, () ->
                CsvUtil.parseCsvLine("E0001, \"Alice\""));
    }

    @Test
    public void parseCsvLine_textAfterClosingQuote_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, CsvUtil.MESSAGE_MISPLACED_QUOTE, () ->
                CsvUtil.parseCsvLine("E0001,\"Ali\"ce"));
        assertThrows(IllegalValueException.class, CsvUtil.MESSAGE_MISPLACED_QUOTE, () ->
                CsvUtil.parseCsvLine("\"E0001\" ,Alice"));
    }

    @Test
    public void parseCsvLine_quotedEmptyValueAndTrailingComma_emptyValues() throws Exception {
        assertEquals(List.of("", "Alice", ""), CsvUtil.parseCsvLine("\"\",Alice,"));
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

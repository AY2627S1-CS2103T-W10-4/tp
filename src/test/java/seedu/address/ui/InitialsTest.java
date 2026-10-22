package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class InitialsTest {

    @Test
    public void of_nullName_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Initials.of(null));
    }

    @Test
    public void of_twoWords_firstLettersOfBoth() {
        assertEquals("AY", Initials.of("Alex Yeoh"));
    }

    @Test
    public void of_moreWords_firstLettersOfFirstAndLastWord() {
        assertEquals("MT", Initials.of("Mei Ling Tan"));
        assertEquals("RK", Initials.of("Ravi s/o Kumar"));
    }

    @Test
    public void of_oneWord_oneInitial() {
        assertEquals("M", Initials.of("Madonna"));
    }

    @Test
    public void of_lowerCaseName_upperCaseInitials() {
        assertEquals("AY", Initials.of("alex yeoh"));
    }

    @Test
    public void of_extraSpaces_ignored() {
        assertEquals("AY", Initials.of("   Alex    Yeoh  "));
    }

    @Test
    public void of_accentedLetters_keptWithTheirAccent() {
        assertEquals("ÉR", Initials.of("Émile Roux"));
    }

    @Test
    public void of_punctuationAtTheStartOfAWord_skipped() {
        assertEquals("AT", Initials.of("(Ah) 'Tan"));
        assertEquals("OT", Initials.of("O'Neil Tan"));
    }

    @Test
    public void of_wordsWithoutLetterOrDigit_skipped() {
        assertEquals("AY", Initials.of("Alex - Yeoh"));
        assertEquals("A", Initials.of("Alex -"));
    }

    @Test
    public void of_digits_countAsInitials() {
        assertEquals("2B", Initials.of("2nd Bob"));
    }

    @Test
    public void of_noLetterOrDigit_emptyString() {
        assertEquals("", Initials.of(""));
        assertEquals("", Initials.of("   "));
        assertEquals("", Initials.of("- . ,"));
    }
}

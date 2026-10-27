package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

public class NaturalOrderComparatorTest {

    private static final NaturalOrderComparator COMPARATOR = NaturalOrderComparator.INSTANCE;

    private static List<String> sorted(String... texts) {
        List<String> list = new ArrayList<>(List.of(texts));
        list.sort(COMPARATOR);
        return list;
    }

    private static int sign(int number) {
        return Integer.signum(number);
    }

    @Test
    public void compare_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> COMPARATOR.compare(null, "E1"));
        assertThrows(NullPointerException.class, () -> COMPARATOR.compare("E1", null));
    }

    @Test
    public void compare_numberInsideText_comparedByItsValue() {
        assertEquals(List.of("E1", "E2", "E10", "E20", "E100"), sorted("E100", "E10", "E2", "E20", "E1"));
        assertEquals(List.of("Level 2", "Level 10"), sorted("Level 10", "Level 2"));
        assertEquals(List.of("Person 9", "Person 10", "Person 1000"), sorted("Person 1000", "Person 9", "Person 10"));
    }

    @Test
    public void compare_severalNumbers_comparedOneAfterTheOther() {
        assertEquals(List.of("A2-B3", "A2-B10", "A10-B1"), sorted("A10-B1", "A2-B10", "A2-B3"));
        assertEquals(List.of("Level 2 Part 9", "Level 2 Part 10", "Level 10 Part 1"),
                sorted("Level 10 Part 1", "Level 2 Part 10", "Level 2 Part 9"));
    }

    @Test
    public void compare_ignoresLetterCaseOfTheText() {
        assertEquals(0, COMPARATOR.compare("e10", "E10"));
        assertEquals(List.of("e3", "E10"), sorted("E10", "e3"));
        assertEquals(List.of("adam", "Bob", "Chloe"), sorted("Chloe", "adam", "Bob"));
    }

    @Test
    public void compare_leadingZeros_doNotChangeTheValue() {
        assertEquals(List.of("E0001", "E2", "E0010"), sorted("E0010", "E2", "E0001"));
        // the same value: the shorter number comes first, so that the order is always the same
        assertEquals(List.of("E1", "E01", "E001"), sorted("E001", "E1", "E01"));
        assertEquals(List.of("E0", "E00"), sorted("E00", "E0"));
    }

    @Test
    public void compare_veryLongNumbers_doNotOverflow() {
        String small = "ID" + "9".repeat(40);
        String large = "ID1" + "0".repeat(40);
        assertEquals(List.of(small, large), sorted(large, small));
        assertTrue(COMPARATOR.compare("ID" + "1".repeat(50), "ID" + "1".repeat(49) + "2") < 0);
    }

    @Test
    public void compare_numberAgainstText_numberFirst() {
        assertEquals(List.of("1A", "A1", "AB"), sorted("AB", "A1", "1A"));
        assertEquals(List.of("E1", "E1A", "E1B", "E2"), sorted("E2", "E1B", "E1", "E1A"));
    }

    @Test
    public void compare_oneTextIsTheStartOfTheOther_theShorterComesFirst() {
        assertEquals(List.of("E", "E1", "EA"), sorted("EA", "E1", "E"));
        assertEquals(List.of("", "E"), sorted("E", ""));
    }

    @Test
    public void compare_textWithoutNumbers_sameAsComparingIgnoringCase() {
        assertEquals(sign(String.CASE_INSENSITIVE_ORDER.compare("Alice", "alicia")),
                sign(COMPARATOR.compare("Alice", "alicia")));
        assertEquals(sign(String.CASE_INSENSITIVE_ORDER.compare("Mary Ann", "Mary-Ann")),
                sign(COMPARATOR.compare("Mary Ann", "Mary-Ann")));
    }

    @Test
    public void compare_onlyTheDigitsZeroToNineAreNumbers() {
        // an Arabic-Indic digit is a letter for this comparison
        assertEquals(0, COMPARATOR.compare("E١", "E١"));
        assertTrue(COMPARATOR.compare("E9", "E١") < 0);
    }

    @Test
    public void compare_randomTexts_isAConsistentOrder() {
        Random random = new Random(2103);
        String alphabet = "aB1 09-A";
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < 80; i++) {
            StringBuilder text = new StringBuilder();
            int length = random.nextInt(7);
            for (int j = 0; j < length; j++) {
                text.append(alphabet.charAt(random.nextInt(alphabet.length())));
            }
            texts.add(text.toString());
        }

        for (String a : texts) {
            assertEquals(0, COMPARATOR.compare(a, a));
            for (String b : texts) {
                // reversing the texts reverses the result
                assertEquals(-sign(COMPARATOR.compare(a, b)), sign(COMPARATOR.compare(b, a)));
                for (String c : texts) {
                    // if a is not after b, and b is not after c, then a is not after c
                    if (COMPARATOR.compare(a, b) <= 0 && COMPARATOR.compare(b, c) <= 0) {
                        assertTrue(COMPARATOR.compare(a, c) <= 0, a + " / " + b + " / " + c);
                    }
                }
            }
        }

        // sorting does not fail with a message that the comparator breaks its rules
        List<String> many = new ArrayList<>();
        for (int i = 0; i < 3000; i++) {
            many.add(texts.get(random.nextInt(texts.size())) + random.nextInt(50));
        }
        Collections.shuffle(many, random);
        many.sort(COMPARATOR);
    }
}

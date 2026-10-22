package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class DepartmentColourTest {

    private static final List<String> KNOWN_DEPARTMENTS = List.of("Engineering", "Sales", "Finance",
            "Human Resources", "Operations", "Marketing", "Legal");

    @Test
    public void of_nullDepartment_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> DepartmentColour.of(null));
        assertThrows(NullPointerException.class, () -> DepartmentColour.chipStyle(null));
    }

    @Test
    public void of_sameDepartment_sameColour() {
        assertEquals(DepartmentColour.of("Engineering"), DepartmentColour.of("Engineering"));
        assertEquals(DepartmentColour.of("Research and Development"), DepartmentColour.of("Research and Development"));
    }

    @Test
    public void of_ignoresLetterCaseAndSurroundingSpaces() {
        assertEquals(DepartmentColour.of("Sales"), DepartmentColour.of("  SALES "));
        assertEquals(DepartmentColour.of("Human Resources"), DepartmentColour.of("human resources"));
        assertEquals(DepartmentColour.of("R&D"), DepartmentColour.of("r&d"));
    }

    @Test
    public void of_knownDepartments_haveDifferentColours() {
        long distinct = KNOWN_DEPARTMENTS.stream().map(DepartmentColour::of).distinct().count();
        assertEquals(KNOWN_DEPARTMENTS.size(), distinct);
    }

    @Test
    public void of_unknownDepartment_getsAValidColourFromThePalette() {
        for (String department : List.of("R&D", "Customer Support", "O & G", "a", "Facilities (North)")) {
            String colour = DepartmentColour.of(department);
            assertTrue(colour.matches("#[0-9a-f]{6}"), colour);
            assertNotEquals(DepartmentColour.of("Engineering"), colour);
        }
    }

    @Test
    public void chipStyle_usesTheColourOfTheDepartment() {
        // Engineering is #5b8def = rgb(91, 141, 239)
        assertEquals("-fx-text-fill: #5b8def; -fx-background-color: rgba(91,141,239,0.22);",
                DepartmentColour.chipStyle("Engineering"));
    }
}

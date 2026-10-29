package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class DepartmentColourTest {

    @Test
    public void of_nullDepartment_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> DepartmentColour.of(null));
        assertThrows(NullPointerException.class, () -> DepartmentColour.chipStyle(null));
        assertThrows(NullPointerException.class, () -> DepartmentColour.avatarStyle(null));
    }

    @Test
    public void of_knownDepartments_returnsRequestedMutedFamilies() {
        assertEquals("#72a0c4", DepartmentColour.of("Engineering"));
        assertEquals("#c09a52", DepartmentColour.of("Finance"));
        assertEquals("#bb745a", DepartmentColour.of("Operations"));
        assertEquals("#62a07b", DepartmentColour.of("Sales"));
        assertEquals("#9a7ab2", DepartmentColour.of("Human Resources"));
        assertEquals("#8879aa", DepartmentColour.of("Product"));
        assertEquals("#579b9a", DepartmentColour.of("Analytics"));
    }

    @Test
    public void of_aliasesAndFormatting_returnsSameColour() {
        assertEquals(DepartmentColour.of("Human Resources"), DepartmentColour.of("  HR "));
        assertEquals(DepartmentColour.of("Analytics"), DepartmentColour.of("DATA"));
        assertEquals(DepartmentColour.of("Engineering"), DepartmentColour.of(" engineering "));
    }

    @Test
    public void of_unknownDepartment_isDeterministicAndUsesFallbackPalette() {
        assertEquals(DepartmentColour.of("Research"), DepartmentColour.of(" research "));
        assertNotEquals(DepartmentColour.of("Engineering"), DepartmentColour.of("Research"));
    }

    @Test
    public void styles_shareColourFamilyWithDifferentStrengths() {
        assertEquals("-fx-text-fill: #72a0c4; -fx-background-color: rgba(114,160,196,0.22);",
                DepartmentColour.chipStyle("Engineering"));
        assertEquals("-fx-text-fill: #72a0c4; -fx-background-color: rgba(114,160,196,0.34);",
                DepartmentColour.avatarStyle("Engineering"));
    }
}

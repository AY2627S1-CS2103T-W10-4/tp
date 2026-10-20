package seedu.address.model.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.EmployeeBuilder;

public class EmployeeMatchesCriteriaPredicateTest {

    private static final Employee SOFTWARE_ENGINEER = new EmployeeBuilder()
            .withDepartment("Engineering").withRole("Software Engineer").build();

    @Test
    public void equals() {
        EmployeeMatchesCriteriaPredicate departmentPredicate =
                new EmployeeMatchesCriteriaPredicate(List.of("Sales"), List.of());
        EmployeeMatchesCriteriaPredicate rolePredicate =
                new EmployeeMatchesCriteriaPredicate(List.of(), List.of("Sales"));

        // same object -> returns true
        assertTrue(departmentPredicate.equals(departmentPredicate));

        // same values -> returns true
        assertTrue(departmentPredicate.equals(new EmployeeMatchesCriteriaPredicate(List.of("Sales"), List.of())));
        assertEquals(departmentPredicate.hashCode(),
                new EmployeeMatchesCriteriaPredicate(List.of("Sales"), List.of()).hashCode());

        // same words but in a different criterion -> returns false
        assertFalse(departmentPredicate.equals(rolePredicate));

        // different types -> returns false
        assertFalse(departmentPredicate.equals(1));

        // null -> returns false
        assertFalse(departmentPredicate.equals(null));
    }

    @Test
    public void constructor_noCriterion_throwsAssertionError() {
        assertThrows(AssertionError.class, () -> new EmployeeMatchesCriteriaPredicate(List.of(), List.of()));
    }

    @Test
    public void test_departmentOnly() {
        // exact word, and ignoring letter case
        assertTrue(new EmployeeMatchesCriteriaPredicate(List.of("Engineering"), List.of()).test(SOFTWARE_ENGINEER));
        assertTrue(new EmployeeMatchesCriteriaPredicate(List.of("eNGINEERING"), List.of()).test(SOFTWARE_ENGINEER));

        // only part of a word does not match
        assertFalse(new EmployeeMatchesCriteriaPredicate(List.of("Engineer"), List.of()).test(SOFTWARE_ENGINEER));
        assertFalse(new EmployeeMatchesCriteriaPredicate(List.of("Eng"), List.of()).test(SOFTWARE_ENGINEER));

        // a different department does not match
        assertFalse(new EmployeeMatchesCriteriaPredicate(List.of("Finance"), List.of()).test(SOFTWARE_ENGINEER));
    }

    @Test
    public void test_roleOnly() {
        assertTrue(new EmployeeMatchesCriteriaPredicate(List.of(), List.of("Engineer")).test(SOFTWARE_ENGINEER));
        assertTrue(new EmployeeMatchesCriteriaPredicate(List.of(), List.of("software")).test(SOFTWARE_ENGINEER));

        // the department is not searched when only a role is given
        assertFalse(new EmployeeMatchesCriteriaPredicate(List.of(), List.of("Engineering")).test(SOFTWARE_ENGINEER));
    }

    @Test
    public void test_multipleWordsInOneCriterion_allWordsMustMatch() {
        // all words present, in any order
        assertTrue(new EmployeeMatchesCriteriaPredicate(List.of(), List.of("Software", "Engineer"))
                .test(SOFTWARE_ENGINEER));
        assertTrue(new EmployeeMatchesCriteriaPredicate(List.of(), List.of("engineer", "software"))
                .test(SOFTWARE_ENGINEER));

        // one word is missing
        assertFalse(new EmployeeMatchesCriteriaPredicate(List.of(), List.of("Software", "Manager"))
                .test(SOFTWARE_ENGINEER));
    }

    @Test
    public void test_departmentAndRole_bothMustMatch() {
        assertTrue(new EmployeeMatchesCriteriaPredicate(List.of("Engineering"), List.of("Software"))
                .test(SOFTWARE_ENGINEER));

        // the department matches but the role does not, and the other way round
        assertFalse(new EmployeeMatchesCriteriaPredicate(List.of("Engineering"), List.of("Manager"))
                .test(SOFTWARE_ENGINEER));
        assertFalse(new EmployeeMatchesCriteriaPredicate(List.of("Finance"), List.of("Software"))
                .test(SOFTWARE_ENGINEER));
    }

    @Test
    public void test_punctuationInDepartment() {
        Employee researchAndDevelopment = new EmployeeBuilder().withDepartment("R&D").build();
        assertTrue(new EmployeeMatchesCriteriaPredicate(List.of("R&D"), List.of()).test(researchAndDevelopment));

        Employee obstetrics = new EmployeeBuilder().withDepartment("O & G").build();
        assertTrue(new EmployeeMatchesCriteriaPredicate(List.of("O", "&", "G"), List.of()).test(obstetrics));
        assertTrue(new EmployeeMatchesCriteriaPredicate(List.of("G"), List.of()).test(obstetrics));
    }

    @Test
    public void toStringMethod() {
        EmployeeMatchesCriteriaPredicate predicate =
                new EmployeeMatchesCriteriaPredicate(List.of("Sales"), List.of("Manager"));
        String expected = EmployeeMatchesCriteriaPredicate.class.getCanonicalName()
                + "{departmentWords=[Sales], roleWords=[Manager]}";
        assertEquals(expected, predicate.toString());
    }
}

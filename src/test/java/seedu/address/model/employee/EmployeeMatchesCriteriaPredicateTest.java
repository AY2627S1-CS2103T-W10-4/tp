package seedu.address.model.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.EmployeeBuilder;

public class EmployeeMatchesCriteriaPredicateTest {

    private static final Employee SOFTWARE_ENGINEER = new EmployeeBuilder().withId("E0001").withName("John Tan")
            .withDepartment("Engineering").withRole("Software Engineer").build();

    private static EmployeeMatchesCriteriaPredicate idsOf(String... ids) {
        return new EmployeeMatchesCriteriaPredicate(
                Arrays.stream(ids).map(EmployeeId::new).toList(), List.of(), List.of(), List.of());
    }

    private static EmployeeMatchesCriteriaPredicate namesOf(String... names) {
        return new EmployeeMatchesCriteriaPredicate(List.of(), List.of(names), List.of(), List.of());
    }

    private static EmployeeMatchesCriteriaPredicate departmentsOf(String... departments) {
        return new EmployeeMatchesCriteriaPredicate(List.of(), List.of(), List.of(departments), List.of());
    }

    private static EmployeeMatchesCriteriaPredicate rolesOf(String... roles) {
        return new EmployeeMatchesCriteriaPredicate(List.of(), List.of(), List.of(), List.of(roles));
    }

    @Test
    public void equals() {
        EmployeeMatchesCriteriaPredicate departmentPredicate = departmentsOf("Sales");

        // same object -> returns true
        assertTrue(departmentPredicate.equals(departmentPredicate));

        // same values -> returns true
        assertTrue(departmentPredicate.equals(departmentsOf("Sales")));
        assertEquals(departmentPredicate.hashCode(), departmentsOf("Sales").hashCode());

        // same words in a different case or with different punctuation -> returns true
        assertTrue(departmentPredicate.equals(departmentsOf("SALES")));
        assertEquals(departmentPredicate.hashCode(), departmentsOf("(sales)").hashCode());

        // same value in a different criterion -> returns false
        assertFalse(departmentPredicate.equals(rolesOf("Sales")));
        assertFalse(departmentPredicate.equals(namesOf("Sales")));

        // different values -> returns false
        assertFalse(departmentPredicate.equals(departmentsOf("Finance")));
        assertFalse(departmentPredicate.equals(departmentsOf("Sales", "Finance")));
        assertFalse(idsOf("E1").equals(idsOf("E2")));

        // different types -> returns false
        assertFalse(departmentPredicate.equals(1));

        // null -> returns false
        assertFalse(departmentPredicate.equals(null));
    }

    @Test
    public void constructor_noCriterion_throwsAssertionError() {
        assertThrows(AssertionError.class, () ->
                new EmployeeMatchesCriteriaPredicate(List.of(), List.of(), List.of(), List.of()));
    }

    @Test
    public void constructor_valueWithoutWord_throwsAssertionError() {
        assertThrows(AssertionError.class, () -> departmentsOf("@@@"));
    }

    @Test
    public void test_idOnly_matchesWholeIdIgnoringCase() {
        assertTrue(idsOf("E0001").test(SOFTWARE_ENGINEER));
        assertTrue(idsOf("e0001").test(SOFTWARE_ENGINEER));
        assertFalse(idsOf("E0002").test(SOFTWARE_ENGINEER));

        // only part of an ID does not match
        assertFalse(idsOf("E000").test(SOFTWARE_ENGINEER));
        assertFalse(idsOf("E00011").test(SOFTWARE_ENGINEER));
    }

    @Test
    public void test_nameOnly() {
        // a single word of the name, ignoring case
        assertTrue(namesOf("John").test(SOFTWARE_ENGINEER));
        assertTrue(namesOf("tAn").test(SOFTWARE_ENGINEER));
        assertTrue(namesOf("Tan John").test(SOFTWARE_ENGINEER));

        // only part of a word does not match
        assertFalse(namesOf("Joh").test(SOFTWARE_ENGINEER));
        assertFalse(namesOf("Jonathan").test(SOFTWARE_ENGINEER));
    }

    @Test
    public void test_departmentOnly() {
        // exact word, and ignoring letter case
        assertTrue(departmentsOf("Engineering").test(SOFTWARE_ENGINEER));
        assertTrue(departmentsOf("eNGINEERING").test(SOFTWARE_ENGINEER));

        // only part of a word does not match
        assertFalse(departmentsOf("Engineer").test(SOFTWARE_ENGINEER));
        assertFalse(departmentsOf("Eng").test(SOFTWARE_ENGINEER));

        // a different department does not match
        assertFalse(departmentsOf("Finance").test(SOFTWARE_ENGINEER));
    }

    @Test
    public void test_roleOnly() {
        assertTrue(rolesOf("Engineer").test(SOFTWARE_ENGINEER));
        assertTrue(rolesOf("software").test(SOFTWARE_ENGINEER));

        // the department is not searched when only a role is given
        assertFalse(rolesOf("Engineering").test(SOFTWARE_ENGINEER));
    }

    @Test
    public void test_multipleWordsInOneValue_allWordsMustMatch() {
        // all words present, in any order
        assertTrue(rolesOf("Software Engineer").test(SOFTWARE_ENGINEER));
        assertTrue(rolesOf("engineer software").test(SOFTWARE_ENGINEER));

        // one word is missing
        assertFalse(rolesOf("Software Manager").test(SOFTWARE_ENGINEER));
    }

    @Test
    public void test_severalValuesOfOneCriterion_anyValueMayMatch() {
        assertTrue(departmentsOf("Sales", "Engineering").test(SOFTWARE_ENGINEER));
        assertTrue(departmentsOf("Engineering", "Sales").test(SOFTWARE_ENGINEER));
        assertFalse(departmentsOf("Sales", "Finance").test(SOFTWARE_ENGINEER));

        assertTrue(namesOf("Mary", "John").test(SOFTWARE_ENGINEER));
        assertTrue(idsOf("E0009", "E0001").test(SOFTWARE_ENGINEER));
        assertFalse(idsOf("E0009", "E0008").test(SOFTWARE_ENGINEER));

        // the words of one value still all have to match: "Software Manager" is not met even though "Manager" is
        // another employee's role
        assertFalse(rolesOf("Software Manager", "Sales Engineer").test(SOFTWARE_ENGINEER));
        assertTrue(rolesOf("Software Manager", "Software Engineer").test(SOFTWARE_ENGINEER));
    }

    @Test
    public void test_severalCriteria_allMustMatch() {
        assertTrue(new EmployeeMatchesCriteriaPredicate(
                List.of(new EmployeeId("E0001")), List.of("John"), List.of("Engineering"), List.of("Software"))
                .test(SOFTWARE_ENGINEER));

        // each criterion on its own is not enough
        assertFalse(new EmployeeMatchesCriteriaPredicate(
                List.of(new EmployeeId("E0002")), List.of("John"), List.of("Engineering"), List.of("Software"))
                .test(SOFTWARE_ENGINEER));
        assertFalse(new EmployeeMatchesCriteriaPredicate(
                List.of(), List.of("Mary"), List.of("Engineering"), List.of("Software")).test(SOFTWARE_ENGINEER));
        assertFalse(new EmployeeMatchesCriteriaPredicate(
                List.of(), List.of("John"), List.of("Finance"), List.of("Software")).test(SOFTWARE_ENGINEER));
        assertFalse(new EmployeeMatchesCriteriaPredicate(
                List.of(), List.of("John"), List.of("Engineering"), List.of("Manager")).test(SOFTWARE_ENGINEER));
    }

    @Test
    public void test_orWithinOneCriterionAndAcrossCriteria() {
        // (Sales OR Engineering) AND (Manager OR Engineer)
        EmployeeMatchesCriteriaPredicate predicate = new EmployeeMatchesCriteriaPredicate(
                List.of(), List.of(), List.of("Sales", "Engineering"), List.of("Manager", "Engineer"));
        assertTrue(predicate.test(SOFTWARE_ENGINEER));

        Employee salesManager = new EmployeeBuilder().withDepartment("Sales").withRole("Account Manager").build();
        assertTrue(predicate.test(salesManager));

        Employee financeManager = new EmployeeBuilder().withDepartment("Finance").withRole("Manager").build();
        assertFalse(predicate.test(financeManager));

        Employee salesClerk = new EmployeeBuilder().withDepartment("Sales").withRole("Clerk").build();
        assertFalse(predicate.test(salesClerk));
    }

    @Test
    public void test_punctuationInField_doesNotStopAWordFromMatching() {
        Employee researchAndDevelopment = new EmployeeBuilder().withDepartment("R&D, Asia")
                .withRole("Engineer (Backend)").build();

        assertTrue(departmentsOf("R&D").test(researchAndDevelopment));
        assertTrue(departmentsOf("Asia").test(researchAndDevelopment));
        assertTrue(departmentsOf("R&D, Asia").test(researchAndDevelopment));
        assertTrue(rolesOf("Backend").test(researchAndDevelopment));
        assertTrue(rolesOf("(Backend)").test(researchAndDevelopment));
        assertTrue(rolesOf("Engineer (Backend)").test(researchAndDevelopment));

        // "R" is only part of the word "R&D"
        assertFalse(departmentsOf("R").test(researchAndDevelopment));
    }

    @Test
    public void test_otherPunctuationInField() {
        Employee obstetrics = new EmployeeBuilder().withDepartment("O & G").build();
        assertTrue(departmentsOf("O & G").test(obstetrics));
        assertTrue(departmentsOf("G").test(obstetrics));

        Employee hyphenated = new EmployeeBuilder().withName("Mary-Ann O'Neil").withRole("Sr. Manager").build();
        assertTrue(namesOf("Mary").test(hyphenated));
        assertTrue(namesOf("Mary-Ann").test(hyphenated));
        assertTrue(namesOf("O'Neil").test(hyphenated));
        assertFalse(namesOf("Neil").test(hyphenated));
        assertTrue(rolesOf("Sr").test(hyphenated));
        assertTrue(rolesOf("Sr. Manager").test(hyphenated));
    }

    @Test
    public void test_ignoresAccentsInTheName() {
        Employee accented = new EmployeeBuilder().withName("\u00C9mile Roux").build();

        assertTrue(namesOf("emile").test(accented));
        assertTrue(namesOf("\u00C9mile").test(accented));
        assertTrue(namesOf("E\u0301mile").test(accented));
        assertFalse(namesOf("emil").test(accented));
    }

    @Test
    public void test_nameWithRelationWord() {
        Employee ravi = new EmployeeBuilder().withName("Ravi s/o Kumar").build();
        assertTrue(namesOf("Ravi").test(ravi));
        assertTrue(namesOf("Kumar").test(ravi));
        assertTrue(namesOf("Ravi s/o Kumar").test(ravi));
    }

    @Test
    public void toStringMethod() {
        EmployeeMatchesCriteriaPredicate predicate = new EmployeeMatchesCriteriaPredicate(
                List.of(new EmployeeId("E1")), List.of("John"), List.of("Sales"), List.of("Manager"));
        String expected = EmployeeMatchesCriteriaPredicate.class.getCanonicalName()
                + "{ids=[E1], names=[John], departments=[Sales], roles=[Manager]}";
        assertEquals(expected, predicate.toString());
    }
}

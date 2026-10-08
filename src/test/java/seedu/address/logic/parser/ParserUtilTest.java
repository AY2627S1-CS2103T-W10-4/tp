package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_EMPLOYEE;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.Department;
import seedu.address.model.employee.Email;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.Name;
import seedu.address.model.employee.Phone;
import seedu.address.model.employee.Role;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "911a";
    private static final String INVALID_EMPLOYEE_ID = "E 01";
    private static final String INVALID_DEPARTMENT = " ";
    private static final String INVALID_ROLE = " ";
    private static final String INVALID_EMAIL = "example.com";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "123456";
    private static final String VALID_EMPLOYEE_ID = "E0123";
    private static final String VALID_DEPARTMENT = "Engineering";
    private static final String VALID_ROLE = "Software Engineer";
    private static final String VALID_EMAIL = "rachel@example.com";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_EMPLOYEE, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_EMPLOYEE, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parseEmployeeId_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmployeeId((String) null));
    }

    @Test
    public void parseEmployeeId_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmployeeId(INVALID_EMPLOYEE_ID));
    }

    @Test
    public void parseEmployeeId_validValueWithoutWhitespace_returnsEmployeeId() throws Exception {
        EmployeeId expectedEmployeeId = new EmployeeId(VALID_EMPLOYEE_ID);
        assertEquals(expectedEmployeeId, ParserUtil.parseEmployeeId(VALID_EMPLOYEE_ID));
    }

    @Test
    public void parseEmployeeId_validValueWithWhitespace_returnsTrimmedEmployeeId() throws Exception {
        String idWithWhitespace = WHITESPACE + VALID_EMPLOYEE_ID + WHITESPACE;
        EmployeeId expectedEmployeeId = new EmployeeId(VALID_EMPLOYEE_ID);
        assertEquals(expectedEmployeeId, ParserUtil.parseEmployeeId(idWithWhitespace));
    }

    @Test
    public void parseDepartment_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseDepartment((String) null));
    }

    @Test
    public void parseDepartment_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseDepartment(INVALID_DEPARTMENT));
    }

    @Test
    public void parseDepartment_validValueWithoutWhitespace_returnsDepartment() throws Exception {
        Department expectedDepartment = new Department(VALID_DEPARTMENT);
        assertEquals(expectedDepartment, ParserUtil.parseDepartment(VALID_DEPARTMENT));
    }

    @Test
    public void parseDepartment_validValueWithWhitespace_returnsTrimmedDepartment() throws Exception {
        String departmentWithWhitespace = WHITESPACE + VALID_DEPARTMENT + WHITESPACE;
        Department expectedDepartment = new Department(VALID_DEPARTMENT);
        assertEquals(expectedDepartment, ParserUtil.parseDepartment(departmentWithWhitespace));
    }

    @Test
    public void parseRole_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseRole((String) null));
    }

    @Test
    public void parseRole_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseRole(INVALID_ROLE));
    }

    @Test
    public void parseRole_validValueWithoutWhitespace_returnsRole() throws Exception {
        Role expectedRole = new Role(VALID_ROLE);
        assertEquals(expectedRole, ParserUtil.parseRole(VALID_ROLE));
    }

    @Test
    public void parseRole_validValueWithWhitespace_returnsTrimmedRole() throws Exception {
        String roleWithWhitespace = WHITESPACE + VALID_ROLE + WHITESPACE;
        Role expectedRole = new Role(VALID_ROLE);
        assertEquals(expectedRole, ParserUtil.parseRole(roleWithWhitespace));
    }

    @Test
    public void parseEmployeeId_differentCase_returnsEqualId() throws Exception {
        assertEquals(ParserUtil.parseEmployeeId("e0123"), ParserUtil.parseEmployeeId("E0123"));
    }

    @Test
    public void parseEmployeeId_variousSchemes_accepted() throws Exception {
        assertEquals("EMP-0042", ParserUtil.parseEmployeeId("EMP-0042").value);
        assertEquals("2024_017", ParserUtil.parseEmployeeId("2024_017").value);
        assertEquals("1", ParserUtil.parseEmployeeId("1").value);
    }

    @Test
    public void parseEmployeeId_tooLong_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmployeeId("E".repeat(21)));
    }

    @Test
    public void parseDepartmentAndRole_punctuation_accepted() throws Exception {
        assertEquals("R&D", ParserUtil.parseDepartment("R&D").value);
        assertEquals("Software Engineer (Backend)", ParserUtil.parseRole("Software Engineer (Backend)").value);
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseName_extraSpaces_collapsedToSingleSpaces() throws Exception {
        assertEquals(new Name("Rachel Walker"), ParserUtil.parseName("  Rachel     Walker  "));
    }

    @Test
    public void parseName_relationWordsAndPunctuation_accepted() throws Exception {
        assertEquals("Ravi s/o Kumar", ParserUtil.parseName("Ravi s/o Kumar").fullName);
        assertEquals("Nur Aisha d/o Ali", ParserUtil.parseName("Nur Aisha d/o Ali").fullName);
        assertEquals("Mary-Ann O'Brien", ParserUtil.parseName("Mary-Ann O'Brien").fullName);
    }

    @Test
    public void parseName_blankAndInvalid_giveDifferentMessages() {
        assertThrows(ParseException.class, Name.MESSAGE_BLANK, () -> ParserUtil.parseName("   "));
        assertThrows(ParseException.class, Name.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseName("James&"));
    }

    @Test
    public void parsePhone_variousLayouts_accepted() throws Exception {
        assertEquals("9123 4567 (HP)", ParserUtil.parsePhone("  9123 4567 (HP) ").value);
        assertEquals("+65 6123-4567", ParserUtil.parsePhone("+65 6123-4567").value);
    }

    @Test
    public void parsePhone_veryLongInput_doesNotOverflowTheStack() throws Exception {
        String longPhone = "1".repeat(10000);
        assertEquals(longPhone, ParserUtil.parsePhone(longPhone).value);
        assertThrows(ParseException.class, Phone.MESSAGE_CONSTRAINTS, () ->
                ParserUtil.parsePhone(longPhone + "x"));
    }

    @Test
    public void parseName_decomposedAccent_returnsStandardForm() throws Exception {
        assertEquals("Jos\u00e9", ParserUtil.parseName("Jose\u0301").fullName);
        assertEquals("\u0BAE\u0BA4\u0BA9\u0BCD", ParserUtil.parseName("\u0BAE\u0BA4\u0BA9\u0BCD").fullName);
    }

    @Test
    public void parsePhone_blankTooFewDigitsAndInvalid_giveDifferentMessages() {
        assertThrows(ParseException.class, Phone.MESSAGE_BLANK, () -> ParserUtil.parsePhone("  "));
        assertThrows(ParseException.class, Phone.MESSAGE_TOO_FEW_DIGITS, () -> ParserUtil.parsePhone("12"));
        assertThrows(ParseException.class, Phone.MESSAGE_CONSTRAINTS, () -> ParserUtil.parsePhone("hello"));
    }

    @Test
    public void parseEmployeeId_blankTooLongAndInvalid_giveDifferentMessages() {
        assertThrows(ParseException.class, ParserUtil.MESSAGE_BLANK_EMPLOYEE_ID, () ->
                ParserUtil.parseEmployeeId("  "));
        assertThrows(ParseException.class, ParserUtil.MESSAGE_EMPLOYEE_ID_TOO_LONG, () ->
                ParserUtil.parseEmployeeId("E".repeat(21)));
        assertThrows(ParseException.class, EmployeeId.MESSAGE_CONSTRAINTS, () ->
                ParserUtil.parseEmployeeId("E 01"));
    }

    @Test
    public void parseDepartmentAndRole_blankTooLongAndInvalid_giveDifferentMessages() {
        assertThrows(ParseException.class, Department.MESSAGE_BLANK, () -> ParserUtil.parseDepartment(" "));
        assertThrows(ParseException.class, Department.MESSAGE_TOO_LONG, () ->
                ParserUtil.parseDepartment("D".repeat(101)));
        assertThrows(ParseException.class, Department.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseDepartment("@@@"));

        assertThrows(ParseException.class, Role.MESSAGE_BLANK, () -> ParserUtil.parseRole(" "));
        assertThrows(ParseException.class, Role.MESSAGE_TOO_LONG, () -> ParserUtil.parseRole("R".repeat(101)));
        assertThrows(ParseException.class, Role.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseRole("###"));
    }
}

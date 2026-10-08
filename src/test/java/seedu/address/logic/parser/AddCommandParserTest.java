package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.DEPARTMENT_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DEPARTMENT_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.ID_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ID_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_DEPARTMENT_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ID_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ROLE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.address.logic.commands.CommandTestUtil.ROLE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ROLE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_DEPARTMENT_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ID_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ROLE_BOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEPARTMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalEmployees.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.employee.Department;
import seedu.address.model.employee.Email;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.Name;
import seedu.address.model.employee.Phone;
import seedu.address.model.employee.Role;
import seedu.address.testutil.EmployeeBuilder;

public class AddCommandParserTest {

    private static final String REQUIRED_FIELDS_BOB = ID_DESC_BOB + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
            + DEPARTMENT_DESC_BOB + ROLE_DESC_BOB;

    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        Employee expectedEmployee = new EmployeeBuilder(BOB).build();

        // whitespace only preamble
        assertParseSuccess(parser, PREAMBLE_WHITESPACE + REQUIRED_FIELDS_BOB, new AddCommand(expectedEmployee));

        // fields in a different order
        assertParseSuccess(parser, ROLE_DESC_BOB + DEPARTMENT_DESC_BOB + EMAIL_DESC_BOB + PHONE_DESC_BOB
                + NAME_DESC_BOB + ID_DESC_BOB, new AddCommand(expectedEmployee));
    }

    @Test
    public void parse_repeatedValue_failure() {
        String validExpectedEmployeeString = REQUIRED_FIELDS_BOB;

        // multiple IDs
        assertParseFailure(parser, ID_DESC_AMY + validExpectedEmployeeString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ID));

        // multiple names
        assertParseFailure(parser, NAME_DESC_AMY + validExpectedEmployeeString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // multiple phones
        assertParseFailure(parser, PHONE_DESC_AMY + validExpectedEmployeeString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple emails
        assertParseFailure(parser, EMAIL_DESC_AMY + validExpectedEmployeeString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // multiple departments
        assertParseFailure(parser, DEPARTMENT_DESC_AMY + validExpectedEmployeeString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DEPARTMENT));

        // multiple roles
        assertParseFailure(parser, ROLE_DESC_AMY + validExpectedEmployeeString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ROLE));

        // invalid value followed by valid value: reported as a repeated field, not as an invalid value
        assertParseFailure(parser, INVALID_ID_DESC + validExpectedEmployeeString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ID));
        assertParseFailure(parser, INVALID_NAME_DESC + validExpectedEmployeeString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser, INVALID_EMAIL_DESC + validExpectedEmployeeString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));
        assertParseFailure(parser, INVALID_PHONE_DESC + validExpectedEmployeeString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
        assertParseFailure(parser, INVALID_DEPARTMENT_DESC + validExpectedEmployeeString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DEPARTMENT));
        assertParseFailure(parser, INVALID_ROLE_DESC + validExpectedEmployeeString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ROLE));

        // valid value followed by invalid value
        assertParseFailure(parser, validExpectedEmployeeString + INVALID_ID_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ID));
        assertParseFailure(parser, validExpectedEmployeeString + INVALID_NAME_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser, validExpectedEmployeeString + INVALID_DEPARTMENT_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DEPARTMENT));
        assertParseFailure(parser, validExpectedEmployeeString + INVALID_ROLE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ROLE));
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        // missing ID prefix
        assertParseFailure(parser, VALID_ID_BOB + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + DEPARTMENT_DESC_BOB + ROLE_DESC_BOB, expectedMessage);

        // missing name prefix
        assertParseFailure(parser, ID_DESC_BOB + VALID_NAME_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + DEPARTMENT_DESC_BOB + ROLE_DESC_BOB, expectedMessage);

        // missing phone prefix
        assertParseFailure(parser, ID_DESC_BOB + NAME_DESC_BOB + VALID_PHONE_BOB + EMAIL_DESC_BOB
                + DEPARTMENT_DESC_BOB + ROLE_DESC_BOB, expectedMessage);

        // missing email prefix
        assertParseFailure(parser, ID_DESC_BOB + NAME_DESC_BOB + PHONE_DESC_BOB + VALID_EMAIL_BOB
                + DEPARTMENT_DESC_BOB + ROLE_DESC_BOB, expectedMessage);

        // missing department prefix
        assertParseFailure(parser, ID_DESC_BOB + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + VALID_DEPARTMENT_BOB + ROLE_DESC_BOB, expectedMessage);

        // missing role prefix
        assertParseFailure(parser, ID_DESC_BOB + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + DEPARTMENT_DESC_BOB + VALID_ROLE_BOB, expectedMessage);

        // all prefixes missing
        assertParseFailure(parser, VALID_ID_BOB + VALID_NAME_BOB + VALID_PHONE_BOB + VALID_EMAIL_BOB
                + VALID_DEPARTMENT_BOB + VALID_ROLE_BOB, expectedMessage);
    }

    @Test
    public void parse_invalidValue_failure() {
        // invalid ID
        assertParseFailure(parser, INVALID_ID_DESC + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + DEPARTMENT_DESC_BOB + ROLE_DESC_BOB, EmployeeId.MESSAGE_CONSTRAINTS);

        // invalid name
        assertParseFailure(parser, ID_DESC_BOB + INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + DEPARTMENT_DESC_BOB + ROLE_DESC_BOB, Name.MESSAGE_CONSTRAINTS);

        // invalid phone
        assertParseFailure(parser, ID_DESC_BOB + NAME_DESC_BOB + INVALID_PHONE_DESC + EMAIL_DESC_BOB
                + DEPARTMENT_DESC_BOB + ROLE_DESC_BOB, Phone.MESSAGE_CONSTRAINTS);

        // invalid email
        assertParseFailure(parser, ID_DESC_BOB + NAME_DESC_BOB + PHONE_DESC_BOB + INVALID_EMAIL_DESC
                + DEPARTMENT_DESC_BOB + ROLE_DESC_BOB, Email.MESSAGE_CONSTRAINTS);

        // invalid department
        assertParseFailure(parser, ID_DESC_BOB + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + INVALID_DEPARTMENT_DESC + ROLE_DESC_BOB, Department.MESSAGE_BLANK);

        // invalid role
        assertParseFailure(parser, ID_DESC_BOB + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + DEPARTMENT_DESC_BOB + INVALID_ROLE_DESC, Role.MESSAGE_BLANK);

        // two invalid values, only first invalid value reported
        assertParseFailure(parser, ID_DESC_BOB + INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + INVALID_DEPARTMENT_DESC + ROLE_DESC_BOB, Name.MESSAGE_CONSTRAINTS);

        // non-empty preamble
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + REQUIRED_FIELDS_BOB,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_nameWithRelationWords_success() {
        String rest = PHONE_DESC_BOB + EMAIL_DESC_BOB + DEPARTMENT_DESC_BOB + ROLE_DESC_BOB;

        // s/o
        assertParseSuccess(parser, ID_DESC_BOB + " n/Ravi s/o Kumar" + rest,
                new AddCommand(new EmployeeBuilder(BOB).withName("Ravi s/o Kumar").build()));

        // d/o, which looks like the department prefix but is part of the name
        assertParseSuccess(parser, ID_DESC_BOB + " n/Nur Aisha d/o Ali" + rest,
                new AddCommand(new EmployeeBuilder(BOB).withName("Nur Aisha d/o Ali").build()));

        // d/o in the name and the real department prefix afterwards
        assertParseSuccess(parser, " n/Nur Aisha d/o Ali" + ID_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ROLE_DESC_BOB + DEPARTMENT_DESC_BOB,
                new AddCommand(new EmployeeBuilder(BOB).withName("Nur Aisha d/o Ali").build()));

        // upper case, and a/l
        assertParseSuccess(parser, ID_DESC_BOB + " n/Siti D/O Hassan" + rest,
                new AddCommand(new EmployeeBuilder(BOB).withName("Siti D/O Hassan").build()));
        assertParseSuccess(parser, ID_DESC_BOB + " n/Ravi a/l Kumar" + rest,
                new AddCommand(new EmployeeBuilder(BOB).withName("Ravi a/l Kumar").build()));
    }

    @Test
    public void parse_departmentStartingWithRelationWord_notTreatedAsName() {
        // "d/O & G" comes after other fields, so it is the department "O & G" and not part of the name
        Employee expected = new EmployeeBuilder(BOB).withName("Alice").withDepartment("O & G")
                .withRole("Engineer").build();
        assertParseSuccess(parser, ID_DESC_BOB + " n/Alice" + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + " d/O & G" + " r/Engineer", new AddCommand(expected));

        // the same with the department typed before the name
        assertParseSuccess(parser, ID_DESC_BOB + " d/O & G" + " n/Alice" + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + " r/Engineer", new AddCommand(expected));

        // a relation word in the name is still part of the name, followed by a department that starts with "o"
        Employee expectedWithRelation = new EmployeeBuilder(BOB).withName("Nur Aisha d/o Ali")
                .withDepartment("O & G").withRole("Engineer").build();
        assertParseSuccess(parser, ID_DESC_BOB + " n/Nur Aisha d/o Ali" + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + " d/O & G" + " r/Engineer", new AddCommand(expectedWithRelation));
    }

    @Test
    public void parse_relationWordOutsideName_failure() {
        // "s/o" is only accepted inside the name, so elsewhere it is an unknown parameter
        assertParseFailure(parser, REQUIRED_FIELDS_BOB + " s/o", unknownParameterMessage("s/"));
    }

    @Test
    public void parse_extraSpacesInName_collapsed() {
        assertParseSuccess(parser, ID_DESC_BOB + " n/  Bob     Choo  " + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + DEPARTMENT_DESC_BOB + ROLE_DESC_BOB, new AddCommand(new EmployeeBuilder(BOB).build()));
    }

    @Test
    public void parse_lenientPhoneAndPunctuationInFields_success() {
        Employee expected = new EmployeeBuilder(BOB).withPhone("9123 4567 (HP) 1111-3333 (Office)")
                .withDepartment("R&D").withRole("Software Engineer (Backend)").build();
        assertParseSuccess(parser, ID_DESC_BOB + NAME_DESC_BOB + " p/9123 4567 (HP) 1111-3333 (Office)"
                + EMAIL_DESC_BOB + " d/R&D" + " r/Software Engineer (Backend)", new AddCommand(expected));
    }

    @Test
    public void parse_unknownParameter_failure() {
        // an extra parameter
        assertParseFailure(parser, REQUIRED_FIELDS_BOB + " x/foo", unknownParameterMessage("x/"));

        // tags are not part of HuntR
        assertParseFailure(parser, REQUIRED_FIELDS_BOB + " t/friends", unknownParameterMessage("t/"));

        // prefixes are case sensitive
        assertParseFailure(parser, " ID/E5" + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + DEPARTMENT_DESC_BOB + ROLE_DESC_BOB, unknownParameterMessage("ID/"));
    }

    @Test
    public void parse_blankValues_failure() {
        assertParseFailure(parser, " id/" + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + DEPARTMENT_DESC_BOB + ROLE_DESC_BOB, ParserUtil.MESSAGE_BLANK_EMPLOYEE_ID);
        assertParseFailure(parser, ID_DESC_BOB + " n/" + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + DEPARTMENT_DESC_BOB + ROLE_DESC_BOB, Name.MESSAGE_BLANK);
        assertParseFailure(parser, ID_DESC_BOB + NAME_DESC_BOB + " p/" + EMAIL_DESC_BOB
                + DEPARTMENT_DESC_BOB + ROLE_DESC_BOB, Phone.MESSAGE_BLANK);
    }

    @Test
    public void parse_specificErrorMessages_failure() {
        // phone with too few digits is reported differently from a phone with invalid characters
        assertParseFailure(parser, ID_DESC_BOB + NAME_DESC_BOB + " p/12" + EMAIL_DESC_BOB
                + DEPARTMENT_DESC_BOB + ROLE_DESC_BOB, Phone.MESSAGE_TOO_FEW_DIGITS);
        assertParseFailure(parser, ID_DESC_BOB + NAME_DESC_BOB + INVALID_PHONE_DESC + EMAIL_DESC_BOB
                + DEPARTMENT_DESC_BOB + ROLE_DESC_BOB, Phone.MESSAGE_CONSTRAINTS);

        // ID that is too long is reported differently from an ID with invalid characters
        assertParseFailure(parser, " id/" + "E".repeat(21) + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + DEPARTMENT_DESC_BOB + ROLE_DESC_BOB, ParserUtil.MESSAGE_EMPLOYEE_ID_TOO_LONG);

        // department and role: too long, and invalid characters
        assertParseFailure(parser, ID_DESC_BOB + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + " d/" + "D".repeat(101) + ROLE_DESC_BOB, Department.MESSAGE_TOO_LONG);
        assertParseFailure(parser, ID_DESC_BOB + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + " d/@@@" + ROLE_DESC_BOB, Department.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, ID_DESC_BOB + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + DEPARTMENT_DESC_BOB + " r/" + "R".repeat(101), Role.MESSAGE_TOO_LONG);
        assertParseFailure(parser, ID_DESC_BOB + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + DEPARTMENT_DESC_BOB + " r/###", Role.MESSAGE_CONSTRAINTS);
    }

    private static String unknownParameterMessage(String parameter) {
        return String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                "Unknown parameter: " + parameter + "\n" + AddCommand.MESSAGE_USAGE);
    }
}

package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.DEPARTMENT_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DEPARTMENT_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.ID_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_DEPARTMENT_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ID_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ROLE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.ROLE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_DEPARTMENT_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ID_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ROLE_AMY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEPARTMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditEmployeeDescriptor;
import seedu.address.model.employee.Department;
import seedu.address.model.employee.Email;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.Name;
import seedu.address.model.employee.Phone;
import seedu.address.model.employee.Role;
import seedu.address.testutil.EditEmployeeDescriptorBuilder;

public class EditCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE);

    private static final EmployeeId TARGET_ID = new EmployeeId(VALID_ID_AMY);

    private EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_missingParts_failure() {
        // no id specified
        assertParseFailure(parser, NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // no field specified
        assertParseFailure(parser, ID_DESC_AMY, EditCommand.MESSAGE_NOT_EDITED);

        // no id and no field specified
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        // index given instead of id
        assertParseFailure(parser, "1" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // invalid arguments being parsed as preamble
        assertParseFailure(parser, "some random string" + ID_DESC_AMY + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // unknown prefix before the id
        assertParseFailure(parser, "i/ string" + ID_DESC_AMY + NAME_DESC_AMY, unknownParameterMessage("i/"));
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, INVALID_ID_DESC + NAME_DESC_AMY, EmployeeId.MESSAGE_CONSTRAINTS); // invalid id
        assertParseFailure(parser, ID_DESC_AMY + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS); // invalid name
        assertParseFailure(parser, ID_DESC_AMY + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS); // invalid phone
        assertParseFailure(parser, ID_DESC_AMY + INVALID_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS); // invalid email
        // invalid department
        assertParseFailure(parser, ID_DESC_AMY + INVALID_DEPARTMENT_DESC, Department.MESSAGE_BLANK);
        assertParseFailure(parser, ID_DESC_AMY + INVALID_ROLE_DESC, Role.MESSAGE_BLANK); // invalid role

        // invalid phone followed by valid email
        assertParseFailure(parser, ID_DESC_AMY + INVALID_PHONE_DESC + EMAIL_DESC_AMY, Phone.MESSAGE_CONSTRAINTS);

        // multiple invalid values, but only the first invalid value is captured
        assertParseFailure(parser,
                ID_DESC_AMY + INVALID_NAME_DESC + INVALID_EMAIL_DESC + VALID_DEPARTMENT_AMY + VALID_PHONE_AMY,
                Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        String userInput = ID_DESC_AMY + PHONE_DESC_BOB
                + EMAIL_DESC_AMY + DEPARTMENT_DESC_AMY + ROLE_DESC_AMY + NAME_DESC_AMY;

        EditEmployeeDescriptor descriptor = new EditEmployeeDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_AMY).withDepartment(VALID_DEPARTMENT_AMY)
                .withRole(VALID_ROLE_AMY).build();
        EditCommand expectedCommand = new EditCommand(TARGET_ID, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_someFieldsSpecified_success() {
        String userInput = ID_DESC_AMY + PHONE_DESC_BOB + EMAIL_DESC_AMY;

        EditEmployeeDescriptor descriptor = new EditEmployeeDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_AMY).build();
        EditCommand expectedCommand = new EditCommand(TARGET_ID, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_unknownParameter_failure() {
        assertParseFailure(parser, ID_DESC_AMY + " x/foo" + PHONE_DESC_AMY, unknownParameterMessage("x/"));
    }

    @Test
    public void parse_relationWordInName_success() {
        // d/o inside the name is part of the name, not the department prefix
        EditEmployeeDescriptor descriptor = new EditEmployeeDescriptorBuilder().withName("Nur Aisha d/o Ali").build();
        assertParseSuccess(parser, ID_DESC_AMY + " n/Nur Aisha d/o Ali", new EditCommand(TARGET_ID, descriptor));

        // d/ outside the name is still the department prefix
        descriptor = new EditEmployeeDescriptorBuilder().withDepartment("O & G").build();
        assertParseSuccess(parser, ID_DESC_AMY + " d/O & G", new EditCommand(TARGET_ID, descriptor));
    }

    @Test
    public void parse_oneFieldSpecified_success() {
        // name
        String userInput = ID_DESC_AMY + NAME_DESC_AMY;
        EditEmployeeDescriptor descriptor = new EditEmployeeDescriptorBuilder().withName(VALID_NAME_AMY).build();
        EditCommand expectedCommand = new EditCommand(TARGET_ID, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // phone
        userInput = ID_DESC_AMY + PHONE_DESC_AMY;
        descriptor = new EditEmployeeDescriptorBuilder().withPhone(VALID_PHONE_AMY).build();
        expectedCommand = new EditCommand(TARGET_ID, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // email
        userInput = ID_DESC_AMY + EMAIL_DESC_AMY;
        descriptor = new EditEmployeeDescriptorBuilder().withEmail(VALID_EMAIL_AMY).build();
        expectedCommand = new EditCommand(TARGET_ID, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // department
        userInput = ID_DESC_AMY + DEPARTMENT_DESC_AMY;
        descriptor = new EditEmployeeDescriptorBuilder().withDepartment(VALID_DEPARTMENT_AMY).build();
        expectedCommand = new EditCommand(TARGET_ID, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // role
        userInput = ID_DESC_AMY + ROLE_DESC_AMY;
        descriptor = new EditEmployeeDescriptorBuilder().withRole(VALID_ROLE_AMY).build();
        expectedCommand = new EditCommand(TARGET_ID, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_multipleRepeatedFields_failure() {
        // More extensive testing of duplicate parameter detections is done in
        // AddCommandParserTest#parse_repeatedValue_failure()

        // valid followed by invalid
        String userInput = ID_DESC_AMY + INVALID_PHONE_DESC + PHONE_DESC_BOB;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid followed by valid
        userInput = ID_DESC_AMY + PHONE_DESC_BOB + INVALID_PHONE_DESC;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple valid fields repeated
        userInput = ID_DESC_AMY + PHONE_DESC_AMY + DEPARTMENT_DESC_AMY + EMAIL_DESC_AMY
                + PHONE_DESC_AMY + DEPARTMENT_DESC_AMY + EMAIL_DESC_AMY
                + PHONE_DESC_BOB + DEPARTMENT_DESC_BOB + EMAIL_DESC_BOB;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL, PREFIX_DEPARTMENT));

        // multiple invalid values
        userInput = ID_DESC_AMY + INVALID_PHONE_DESC + INVALID_DEPARTMENT_DESC + INVALID_EMAIL_DESC
                + INVALID_PHONE_DESC + INVALID_DEPARTMENT_DESC + INVALID_EMAIL_DESC;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL, PREFIX_DEPARTMENT));

        // repeated id
        userInput = ID_DESC_AMY + ID_DESC_AMY + PHONE_DESC_AMY;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ID));
    }

    private static String unknownParameterMessage(String parameter) {
        return String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                "Unknown parameter: " + parameter + "\n" + EditCommand.MESSAGE_USAGE);
    }
}

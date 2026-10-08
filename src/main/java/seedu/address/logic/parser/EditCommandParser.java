package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEPARTMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditEmployeeDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.EmployeeId;

/**
 * Parses input arguments and creates a new EditCommand object
 */
public class EditCommandParser implements Parser<EditCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the EditCommand
     * and returns an EditCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public EditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_ID, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                        PREFIX_DEPARTMENT, PREFIX_ROLE);

        if (argMultimap.getValue(PREFIX_ID).isEmpty() || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_ID, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_DEPARTMENT, PREFIX_ROLE);

        EmployeeId targetId = ParserUtil.parseEmployeeId(argMultimap.getValue(PREFIX_ID).get());

        EditEmployeeDescriptor editEmployeeDescriptor = new EditEmployeeDescriptor();

        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            editEmployeeDescriptor.setName(ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get()));
        }
        if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            editEmployeeDescriptor.setPhone(ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get()));
        }
        if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            editEmployeeDescriptor.setEmail(ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get()));
        }
        if (argMultimap.getValue(PREFIX_DEPARTMENT).isPresent()) {
            editEmployeeDescriptor.setDepartment(
                    ParserUtil.parseDepartment(argMultimap.getValue(PREFIX_DEPARTMENT).get()));
        }
        if (argMultimap.getValue(PREFIX_ROLE).isPresent()) {
            editEmployeeDescriptor.setRole(ParserUtil.parseRole(argMultimap.getValue(PREFIX_ROLE).get()));
        }

        if (!editEmployeeDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }

        return new EditCommand(targetId, editEmployeeDescriptor);
    }

}

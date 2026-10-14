package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEPARTMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.stream.Stream;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.Department;
import seedu.address.model.employee.Email;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.Name;
import seedu.address.model.employee.Phone;
import seedu.address.model.employee.Role;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        String hiddenArgs = ArgumentPreprocessor.hideNameRelationWords(args);
        ArgumentPreprocessor.rejectUnknownParameters(hiddenArgs, AddCommand.MESSAGE_USAGE,
                PREFIX_ID, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_DEPARTMENT, PREFIX_ROLE);

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(hiddenArgs,
                PREFIX_ID, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_DEPARTMENT, PREFIX_ROLE);

        if (!arePrefixesPresent(argMultimap,
                PREFIX_ID, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_DEPARTMENT, PREFIX_ROLE)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(
                PREFIX_ID, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_DEPARTMENT, PREFIX_ROLE);
        EmployeeId id = ParserUtil.parseEmployeeId(valueOf(argMultimap, PREFIX_ID));
        Name name = ParserUtil.parseName(valueOf(argMultimap, PREFIX_NAME));
        Phone phone = ParserUtil.parsePhone(valueOf(argMultimap, PREFIX_PHONE));
        Email email = ParserUtil.parseEmail(valueOf(argMultimap, PREFIX_EMAIL));
        Department department = ParserUtil.parseDepartment(valueOf(argMultimap, PREFIX_DEPARTMENT));
        Role role = ParserUtil.parseRole(valueOf(argMultimap, PREFIX_ROLE));

        Employee employee = new Employee(id, name, phone, email, department, role);

        return new AddCommand(employee);
    }

    /**
     * Returns the value given for {@code prefix}, with any hidden slashes restored.
     * The prefix must be present.
     */
    private static String valueOf(ArgumentMultimap argMultimap, Prefix prefix) {
        return ArgumentPreprocessor.restoreHiddenSlashes(argMultimap.getValue(prefix).get());
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

}

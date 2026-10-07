package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEPARTMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
     * Words such as "s/o" (son of) and "d/o" (daughter of) look like command prefixes but are part of a name,
     * so they are hidden from the tokenizer while the arguments are split. They are only recognised as whole
     * words. A department that is literally just "o" therefore cannot be entered with {@code d/o}.
     */
    private static final Pattern NAME_RELATION_WORD = Pattern.compile("(?<=\\s)(s/o|d/o|a/l|a/p)(?=\\s|$)",
            Pattern.CASE_INSENSITIVE);
    private static final char HIDDEN_SLASH = '\uE000';

    /** Something that looks like a parameter ("x/"), found at the start of the input or after whitespace. */
    private static final Pattern PARAMETER_LIKE = Pattern.compile("(?:^|\\s)([A-Za-z]+/)");

    private static final Set<String> KNOWN_PREFIXES = Stream.of(PREFIX_ID, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
            PREFIX_DEPARTMENT, PREFIX_ROLE).map(Prefix::getPrefix).collect(Collectors.toSet());

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        String hiddenArgs = hideNameRelationWords(args);
        rejectUnknownParameters(hiddenArgs);

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
     * Returns {@code args} with the slash in words such as "s/o" and "d/o" hidden, so that they are not mistaken
     * for command prefixes.
     */
    private static String hideNameRelationWords(String args) {
        Matcher matcher = NAME_RELATION_WORD.matcher(args);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(
                    matcher.group().replace('/', HIDDEN_SLASH)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Throws a {@code ParseException} if {@code hiddenArgs} contains a parameter that this command does not have,
     * such as {@code x/foo}.
     */
    private static void rejectUnknownParameters(String hiddenArgs) throws ParseException {
        Matcher matcher = PARAMETER_LIKE.matcher(hiddenArgs);
        while (matcher.find()) {
            String parameter = matcher.group(1);
            if (!KNOWN_PREFIXES.contains(parameter)) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                        "Unknown parameter: " + parameter + "\n" + AddCommand.MESSAGE_USAGE));
            }
        }
    }

    /**
     * Returns the value given for {@code prefix}, with any hidden slashes restored.
     * The prefix must be present.
     */
    private static String valueOf(ArgumentMultimap argMultimap, Prefix prefix) {
        return argMultimap.getValue(prefix).get().replace(HIDDEN_SLASH, '/');
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

}

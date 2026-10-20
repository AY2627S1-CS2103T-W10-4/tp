package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEPARTMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.List;
import java.util.Optional;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.EmployeeMatchesCriteriaPredicate;

/**
 * Parses input arguments and creates a new FilterCommand object.
 */
public class FilterCommandParser implements Parser<FilterCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FilterCommand
     * and returns a FilterCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FilterCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentPreprocessor.rejectUnknownParameters(args, FilterCommand.MESSAGE_USAGE,
                PREFIX_DEPARTMENT, PREFIX_ROLE);

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_DEPARTMENT, PREFIX_ROLE);
        Optional<String> department = argMultimap.getValue(PREFIX_DEPARTMENT);
        Optional<String> role = argMultimap.getValue(PREFIX_ROLE);

        if (!argMultimap.getPreamble().isEmpty() || (department.isEmpty() && role.isEmpty())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_DEPARTMENT, PREFIX_ROLE);

        List<String> departmentWords = List.of();
        List<String> roleWords = List.of();
        try {
            if (department.isPresent()) {
                departmentWords = toWords(ParserUtil.parseDepartment(department.get()).value);
            }
            if (role.isPresent()) {
                roleWords = toWords(ParserUtil.parseRole(role.get()).value);
            }
        } catch (ParseException pe) {
            // the specific problem (blank, too long, invalid characters), followed by how to use the command
            throw new ParseException(pe.getMessage() + "\n" + FilterCommand.MESSAGE_USAGE, pe);
        }

        return new FilterCommand(new EmployeeMatchesCriteriaPredicate(departmentWords, roleWords));
    }

    private static List<String> toWords(String value) {
        return List.of(value.trim().split("\\s+"));
    }

}

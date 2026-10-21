package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEPARTMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.EmployeeId;
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
        String hiddenArgs = ArgumentPreprocessor.hideNameRelationWords(args);
        ArgumentPreprocessor.rejectUnknownParameters(hiddenArgs, FilterCommand.MESSAGE_USAGE,
                PREFIX_ID, PREFIX_NAME, PREFIX_DEPARTMENT, PREFIX_ROLE);

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(hiddenArgs,
                PREFIX_ID, PREFIX_NAME, PREFIX_DEPARTMENT, PREFIX_ROLE);
        boolean hasCriterion = !argMultimap.getAllValues(PREFIX_ID).isEmpty()
                || !argMultimap.getAllValues(PREFIX_NAME).isEmpty()
                || !argMultimap.getAllValues(PREFIX_DEPARTMENT).isEmpty()
                || !argMultimap.getAllValues(PREFIX_ROLE).isEmpty();
        if (!argMultimap.getPreamble().isEmpty() || !hasCriterion) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
        }

        List<EmployeeId> ids = parseIds(argMultimap.getAllValues(PREFIX_ID));
        List<String> names = parseSearchValues(PREFIX_NAME, argMultimap.getAllValues(PREFIX_NAME));
        List<String> departments = parseSearchValues(PREFIX_DEPARTMENT, argMultimap.getAllValues(PREFIX_DEPARTMENT));
        List<String> roles = parseSearchValues(PREFIX_ROLE, argMultimap.getAllValues(PREFIX_ROLE));
        return new FilterCommand(new EmployeeMatchesCriteriaPredicate(ids, names, departments, roles));
    }

    /**
     * Parses each of the {@code values} as an employee ID. An ID must be a valid one, as for other commands.
     */
    private static List<EmployeeId> parseIds(List<String> values) throws ParseException {
        List<EmployeeId> ids = new ArrayList<>();
        for (String value : values) {
            try {
                ids.add(ParserUtil.parseEmployeeId(value));
            } catch (ParseException pe) {
                // the specific problem, followed by how to use the command
                throw new ParseException(pe.getMessage() + "\n" + FilterCommand.MESSAGE_USAGE, pe);
            }
        }
        return ids;
    }

    /**
     * Returns the {@code values} given for {@code prefix}, with any hidden slashes restored.
     * These are words to search for, not complete names, departments or roles. So they are not checked against
     * the rules for those fields, and punctuation only separates words. For example, {@code (Backend)} can be
     * searched for although a role cannot start with {@code (}.
     *
     * @throws ParseException if a value has no letter or digit to search for.
     */
    private static List<String> parseSearchValues(Prefix prefix, List<String> values) throws ParseException {
        List<String> searchValues = new ArrayList<>();
        for (String value : values) {
            String restoredValue = ArgumentPreprocessor.restoreHiddenSlashes(value).trim();
            if (StringUtil.getSearchWords(restoredValue).isEmpty()) {
                throw new ParseException(String.format(FilterCommand.MESSAGE_NO_SEARCH_WORD, prefix)
                        + "\n" + FilterCommand.MESSAGE_USAGE);
            }
            searchValues.add(restoredValue);
        }
        return searchValues;
    }

}

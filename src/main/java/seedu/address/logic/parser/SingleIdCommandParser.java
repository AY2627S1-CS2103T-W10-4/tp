package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ID;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.EmployeeId;

/** Shared parser for commands accepting exactly one id/ parameter. */
final class SingleIdCommandParser {
    private SingleIdCommandParser() {}

    static EmployeeId parse(String args, String usage) throws ParseException {
        requireNonNull(args);
        ArgumentPreprocessor.rejectUnknownParameters(args, usage, PREFIX_ID);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_ID);
        if (arguments.getValue(PREFIX_ID).isEmpty() || !arguments.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_ID);
        try {
            return ParserUtil.parseEmployeeId(arguments.getValue(PREFIX_ID).orElseThrow());
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + usage, e);
        }
    }
}

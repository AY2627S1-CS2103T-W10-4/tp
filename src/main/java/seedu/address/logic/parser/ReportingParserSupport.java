package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MANAGER;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.EmployeeId;

/** Shared strict prefix validation for reporting commands. */
final class ReportingParserSupport {
    private ReportingParserSupport() {}

    static EmployeeId[] parse(String args, String usage, boolean requiresManager) throws ParseException {
        try {
            Prefix[] prefixes = requiresManager ? new Prefix[] {PREFIX_ID, PREFIX_MANAGER}
                    : new Prefix[] {PREFIX_ID};
            ArgumentPreprocessor.rejectUnknownParameters(args, usage, prefixes);
            ArgumentMultimap values = ArgumentTokenizer.tokenize(args, prefixes);
            values.verifyNoDuplicatePrefixesFor(prefixes);
            if (!values.getPreamble().isEmpty() || values.getValue(PREFIX_ID).isEmpty()
                    || (requiresManager && values.getValue(PREFIX_MANAGER).isEmpty())) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage));
            }
            EmployeeId employee = ParserUtil.parseEmployeeId(values.getValue(PREFIX_ID).orElseThrow());
            EmployeeId manager = requiresManager
                    ? ParserUtil.parseEmployeeId(values.getValue(PREFIX_MANAGER).orElseThrow()) : null;
            return new EmployeeId[] {employee, manager};
        } catch (ParseException e) {
            if (e.getMessage().contains(usage)) {
                throw e;
            }
            throw new ParseException(e.getMessage() + "\n" + usage, e);
        }
    }
}

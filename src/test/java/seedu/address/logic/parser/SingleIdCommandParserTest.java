package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.UnarchiveCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.EmployeeId;

public class SingleIdCommandParserTest {

    private static final List<ParserCase> PARSER_CASES = List.of(
            new ParserCase(new DeleteCommandParser(), DeleteCommand.MESSAGE_USAGE),
            new ParserCase(new ArchiveCommandParser(), ArchiveCommand.MESSAGE_USAGE),
            new ParserCase(new UnarchiveCommandParser(), UnarchiveCommand.MESSAGE_USAGE));

    @Test
    public void parse_equivalentMalformedInput_usesConsistentValidationMessages() {
        List<MalformedInput> malformedInputs = List.of(
                new MalformedInput("", Messages.MESSAGE_MISSING_ID),
                new MalformedInput(" id/", Messages.MESSAGE_BLANK_ID),
                new MalformedInput(" x/1", String.format(Messages.MESSAGE_UNKNOWN_PARAMETER, "x/")),
                new MalformedInput(" id/E0001 id/E0002",
                        Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ID)),
                new MalformedInput(" id/E0001 unexpectedText", EmployeeId.MESSAGE_CONSTRAINTS));

        for (ParserCase parserCase : PARSER_CASES) {
            for (MalformedInput malformedInput : malformedInputs) {
                ParseException exception = assertThrows(ParseException.class, () ->
                        parserCase.parser.parse(malformedInput.arguments));
                assertEquals(malformedInput.message + "\n" + parserCase.usage, exception.getMessage());
            }
        }
    }

    private record ParserCase(Parser<? extends Command> parser, String usage) {}

    private record MalformedInput(String arguments, String message) {}
}

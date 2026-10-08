package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Prepares the arguments of commands that take employee fields before they are tokenized.
 */
final class ArgumentPreprocessor {

    //@@author cjldylan
    /**
     * Words such as "s/o" (son of) and "d/o" (daughter of) look like command prefixes but are part of a name,
     * so they are hidden from the tokenizer while the arguments are split. They are only treated as name text
     * as whole words that appear inside the name field, i.e. after {@code n/} and before the next prefix, so
     * {@code d/O & G} after the email is still read as the department "O & G". The remaining ambiguous case is a
     * department that starts with the word "o" and is typed straight after the name.
     */
    private static final Pattern NAME_RELATION_WORD = Pattern.compile("(s/o|d/o|a/l|a/p)(?=\\s|$)",
            Pattern.CASE_INSENSITIVE);
    private static final char HIDDEN_SLASH = '\uE000';

    /** Something that looks like a parameter ("x/"), found at the start of the input or after whitespace. */
    private static final Pattern PARAMETER_LIKE = Pattern.compile("(?:^|\\s)([A-Za-z]+/)");

    //@@author
    private ArgumentPreprocessor() {}

    //@@author cjldylan
    /**
     * Returns {@code args} with the slash hidden in relation words such as "s/o" and "d/o" that are part of the
     * name, so that they are not mistaken for command prefixes.
     */
    static String hideNameRelationWords(String args) {
        StringBuilder hidden = new StringBuilder(args);
        Matcher parameterMatcher = PARAMETER_LIKE.matcher(args);
        String currentPrefix = "";
        while (parameterMatcher.find()) {
            String prefix = parameterMatcher.group(1);
            int prefixStart = parameterMatcher.start(1);
            if (currentPrefix.equals(PREFIX_NAME.getPrefix()) && isRelationWordAt(args, prefixStart)) {
                hidden.setCharAt(prefixStart + prefix.length() - 1, HIDDEN_SLASH);
            } else {
                currentPrefix = prefix;
            }
        }
        return hidden.toString();
    }

    private static boolean isRelationWordAt(String args, int start) {
        return NAME_RELATION_WORD.matcher(args).region(start, args.length()).lookingAt();
    }

    //@@author
    /**
     * Throws a {@code ParseException} if {@code hiddenArgs} contains a parameter that is not one of
     * {@code knownPrefixes}, such as {@code x/foo}. The error message ends with {@code usage}.
     */
    static void rejectUnknownParameters(String hiddenArgs, String usage, Prefix... knownPrefixes)
            throws ParseException {
        Set<String> known = Stream.of(knownPrefixes).map(Prefix::getPrefix).collect(Collectors.toSet());
        Matcher matcher = PARAMETER_LIKE.matcher(hiddenArgs);
        while (matcher.find()) {
            String parameter = matcher.group(1);
            if (!known.contains(parameter)) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                        "Unknown parameter: " + parameter + "\n" + usage));
            }
        }
    }

    /**
     * Returns {@code value} with any slashes hidden by {@link #hideNameRelationWords(String)} restored.
     */
    static String restoreHiddenSlashes(String value) {
        return value.replace(HIDDEN_SLASH, '/');
    }

}

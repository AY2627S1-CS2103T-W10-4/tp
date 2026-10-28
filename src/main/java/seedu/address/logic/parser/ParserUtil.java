package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FILE;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.employee.Department;
import seedu.address.model.employee.Email;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.Name;
import seedu.address.model.employee.Phone;
import seedu.address.model.employee.Role;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";
    /** Maximum length of an employee ID; see {@link EmployeeId#VALIDATION_REGEX}. */
    public static final int EMPLOYEE_ID_MAX_LENGTH = 20;
    public static final String MESSAGE_BLANK_EMPLOYEE_ID = "Employee ID should not be blank.";
    public static final String MESSAGE_EMPLOYEE_ID_TOO_LONG = "Employee ID should be at most "
            + EMPLOYEE_ID_MAX_LENGTH + " characters long.";
    public static final String CSV_FILE_EXTENSION = ".csv";
    public static final String MESSAGE_FILE_PATH_NOT_CSV = "File path should end in " + CSV_FILE_EXTENSION
            + ", for example exports/staff.csv.";
    public static final String MESSAGE_FILE_PATH_OUTSIDE_APP_FOLDER = "File path should be relative to the "
            + "folder HuntR runs from and stay inside it, for example exports/staff.csv.";
    public static final String MESSAGE_INVALID_FILE_PATH = "File path contains characters that are not allowed.";
    public static final String MESSAGE_BLANK_FILE_PATH = "File path cannot be blank.";

    /**
     * Parses the file/ argument shared by import and export. Positional paths remain supported for compatibility.
     * Use ./file/ for a positional path whose first directory is literally named file.
     * Only the leading prefix is removed; the remaining text is a single path and may contain spaces or file/.
     */
    public static Path parseCsvFileArgument(String args) throws ParseException {
        requireNonNull(args);
        String pathText = args.trim();
        if (pathText.startsWith(PREFIX_FILE.getPrefix())) {
            pathText = pathText.substring(PREFIX_FILE.getPrefix().length()).trim();
        }
        if (pathText.isBlank()) {
            throw new ParseException(MESSAGE_BLANK_FILE_PATH);
        }
        return parseCsvFilePath(pathText);
    }

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String normalisedName = Name.normalise(name);
        if (normalisedName.isEmpty()) {
            throw new ParseException(Name.MESSAGE_BLANK);
        }
        if (!Name.isValidName(normalisedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(normalisedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (trimmedPhone.isEmpty()) {
            throw new ParseException(Phone.MESSAGE_BLANK);
        }
        if (!Phone.hasValidLayout(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_TOO_FEW_DIGITS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String employeeId} into a {@code EmployeeId}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code employeeId} is invalid.
     */
    public static EmployeeId parseEmployeeId(String employeeId) throws ParseException {
        requireNonNull(employeeId);
        String trimmedEmployeeId = employeeId.trim();
        if (trimmedEmployeeId.isEmpty()) {
            throw new ParseException(MESSAGE_BLANK_EMPLOYEE_ID);
        }
        if (!EmployeeId.isValidEmployeeId(trimmedEmployeeId)) {
            boolean onlyTooLong = trimmedEmployeeId.length() > EMPLOYEE_ID_MAX_LENGTH
                    && EmployeeId.isValidEmployeeId(trimmedEmployeeId.substring(0, EMPLOYEE_ID_MAX_LENGTH));
            throw new ParseException(onlyTooLong ? MESSAGE_EMPLOYEE_ID_TOO_LONG : EmployeeId.MESSAGE_CONSTRAINTS);
        }
        return new EmployeeId(trimmedEmployeeId);
    }

    /**
     * Parses a {@code String department} into a {@code Department}.
     * Leading and trailing whitespaces will be trimmed, and runs of whitespace inside will be reduced to one space.
     *
     * @throws ParseException if the given {@code department} is invalid.
     */
    public static Department parseDepartment(String department) throws ParseException {
        requireNonNull(department);
        String normalisedDepartment = Department.normalise(department);
        if (normalisedDepartment.isEmpty()) {
            throw new ParseException(Department.MESSAGE_BLANK);
        }
        if (!Department.isValidDepartment(normalisedDepartment)) {
            boolean isOnlyTooLong = normalisedDepartment.length() > Department.MAX_LENGTH
                    && Department.isValidDepartment(normalisedDepartment.substring(0, Department.MAX_LENGTH));
            throw new ParseException(isOnlyTooLong ? Department.MESSAGE_TOO_LONG : Department.MESSAGE_CONSTRAINTS);
        }
        return new Department(normalisedDepartment);
    }

    /**
     * Parses a {@code String role} into a {@code Role}.
     * Leading and trailing whitespaces will be trimmed, and runs of whitespace inside will be reduced to one space.
     *
     * @throws ParseException if the given {@code role} is invalid.
     */
    public static Role parseRole(String role) throws ParseException {
        requireNonNull(role);
        String normalisedRole = Role.normalise(role);
        if (normalisedRole.isEmpty()) {
            throw new ParseException(Role.MESSAGE_BLANK);
        }
        if (!Role.isValidRole(normalisedRole)) {
            boolean isOnlyTooLong = normalisedRole.length() > Role.MAX_LENGTH
                    && Role.isValidRole(normalisedRole.substring(0, Role.MAX_LENGTH));
            throw new ParseException(isOnlyTooLong ? Role.MESSAGE_TOO_LONG : Role.MESSAGE_CONSTRAINTS);
        }
        return new Role(normalisedRole);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses {@code filePath} into a {@code Path} to a CSV file inside the folder HuntR runs from.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the path is not valid, is absolute, leads outside the HuntR folder,
     *     or does not end in {@value #CSV_FILE_EXTENSION}.
     */
    public static Path parseCsvFilePath(String filePath) throws ParseException {
        requireNonNull(filePath);
        String trimmedFilePath = filePath.trim();
        Path path;
        try {
            path = Paths.get(trimmedFilePath).normalize();
        } catch (InvalidPathException ipe) {
            throw new ParseException(MESSAGE_INVALID_FILE_PATH, ipe);
        }
        // getRoot() also catches Windows paths such as \staff.csv, which have a root but are not absolute
        boolean isOutsideAppFolder = path.isAbsolute() || path.getRoot() != null || path.startsWith("..");
        if (isOutsideAppFolder) {
            throw new ParseException(MESSAGE_FILE_PATH_OUTSIDE_APP_FOLDER);
        }
        String fileName = path.getFileName().toString();
        boolean isCsvFile = fileName.length() > CSV_FILE_EXTENSION.length()
                && fileName.toLowerCase(Locale.ROOT).endsWith(CSV_FILE_EXTENSION);
        if (!isCsvFile) {
            throw new ParseException(MESSAGE_FILE_PATH_NOT_CSV);
        }
        return path;
    }

}

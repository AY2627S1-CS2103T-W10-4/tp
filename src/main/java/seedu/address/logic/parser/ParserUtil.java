package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

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
        if (!trimmedPhone.matches(Phone.VALIDATION_REGEX)) {
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
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code department} is invalid.
     */
    public static Department parseDepartment(String department) throws ParseException {
        requireNonNull(department);
        String trimmedDepartment = department.trim();
        if (trimmedDepartment.isEmpty()) {
            throw new ParseException(Department.MESSAGE_BLANK);
        }
        if (!Department.isValidDepartment(trimmedDepartment)) {
            boolean onlyTooLong = trimmedDepartment.length() > Department.MAX_LENGTH
                    && Department.isValidDepartment(trimmedDepartment.substring(0, Department.MAX_LENGTH));
            throw new ParseException(onlyTooLong ? Department.MESSAGE_TOO_LONG : Department.MESSAGE_CONSTRAINTS);
        }
        return new Department(trimmedDepartment);
    }

    /**
     * Parses a {@code String role} into a {@code Role}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code role} is invalid.
     */
    public static Role parseRole(String role) throws ParseException {
        requireNonNull(role);
        String trimmedRole = role.trim();
        if (trimmedRole.isEmpty()) {
            throw new ParseException(Role.MESSAGE_BLANK);
        }
        if (!Role.isValidRole(trimmedRole)) {
            boolean onlyTooLong = trimmedRole.length() > Role.MAX_LENGTH
                    && Role.isValidRole(trimmedRole.substring(0, Role.MAX_LENGTH));
            throw new ParseException(onlyTooLong ? Role.MESSAGE_TOO_LONG : Role.MESSAGE_CONSTRAINTS);
        }
        return new Role(trimmedRole);
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

}

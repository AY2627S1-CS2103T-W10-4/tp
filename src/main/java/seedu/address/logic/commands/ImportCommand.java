package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.CsvUtil;
import seedu.address.commons.util.CsvUtil.CsvRecord;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.ReportingStructure;

/**
 * Adds every employee in a CSV file. If any row is invalid, no employee is added.
 */
public class ImportCommand extends CsvFileCommand {

    public static final String COMMAND_WORD = "import";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds every employee in a CSV file. "
            + "If any row is invalid, no employee is added.\n"
            + "Parameters: file/FILE_PATH (relative to the folder HuntR runs from, ending in .csv)\n"
            + "The file must start with the header " + ExportCommand.CSV_HEADER
            + " (legacy six-column files and seven-column files ending in archived are also accepted)\n"
            + "Example: " + COMMAND_WORD + " file/exports/staff.csv";

    public static final String MESSAGE_SUCCESS = "Imported %1$d employee(s) from %2$s";
    public static final String MESSAGE_FILE_NOT_FOUND = "File not found: %1$s";
    public static final String MESSAGE_READ_FAILED = "Could not read %1$s. Check that the file is not open in "
            + "another program (such as Excel) and that you are allowed to read it.";
    public static final String MESSAGE_NOT_UTF8 = "Could not read %1$s because it is not saved as UTF-8 text. "
            + "In Excel, save it as \"CSV UTF-8\".";
    public static final String MESSAGE_EMPTY_FILE = "%1$s is empty.";
    public static final String MESSAGE_WRONG_HEADER = "%1$s should start with the header " + ExportCommand.CSV_HEADER
            + " (legacy six-column files and seven-column files ending in archived are also accepted)";
    public static final String MESSAGE_INVALID_RELATIONSHIPS = "Nothing was imported. %s";
    public static final String MESSAGE_NO_EMPLOYEES = "%1$s has no employee rows to import.";
    public static final String MESSAGE_INVALID_ROW = "Nothing was imported. Row %1$d: %2$s";
    public static final String MESSAGE_WRONG_VALUE_COUNT = "Expected %1$d values (%2$s) but found %3$d.";
    public static final String MESSAGE_INVALID_ARCHIVED = "The archived value should be true or false, or left blank "
            + "for false.";
    public static final String MESSAGE_DUPLICATE_IN_FILE = "Employee ID %1$s is also used on row %2$d.";

    /** Excel adds this invisible character to the start of files saved as "CSV UTF-8". */
    private static final String BYTE_ORDER_MARK = "\uFEFF";

    private static final Logger logger = LogsCenter.getLogger(ImportCommand.class);

    /**
     * Creates an ImportCommand to add the employees in the CSV file at {@code filePath}, relative to the folder
     * HuntR runs from.
     */
    public ImportCommand(Path filePath) {
        this(DEFAULT_APP_FOLDER, filePath);
    }

    /**
     * Creates an ImportCommand to add the employees in the CSV file at {@code filePath}, relative to
     * {@code appFolder}.
     */
    public ImportCommand(Path appFolder, Path filePath) {
        super(appFolder, filePath);
    }

    @Override
    public HistoryAction getHistoryAction() {
        return HistoryAction.COMMIT;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Path resolvedPath = resolveFilePath();
        List<CsvRecord> records = readRecords(resolvedPath);
        List<String> columns = readColumns(records);
        List<Employee> employees = parseEmployees(records.subList(1, records.size()), columns, model);

        // Add every identity before assigning managers, so CSV row order does not restrict references.
        employees.forEach(employee -> model.addEmployee(employee.withManager(null)));
        employees.stream().filter(employee -> employee.getManagerId().isPresent())
                .forEach(employee -> model.setEmployee(employee.withManager(null), employee));
        model.updateFilteredEmployeeList(Model.PREDICATE_SHOW_ALL_EMPLOYEES);

        logger.info("Imported " + employees.size() + " employees from " + resolvedPath);
        return new CommandResult(String.format(MESSAGE_SUCCESS, employees.size(), getFilePath()));
    }

    private List<CsvRecord> readRecords(Path resolvedPath) throws CommandException {
        if (!Files.isRegularFile(resolvedPath)) {
            throw new CommandException(String.format(MESSAGE_FILE_NOT_FOUND, getFilePath()));
        }
        try {
            return CsvUtil.splitCsvRecords(Files.readString(resolvedPath, StandardCharsets.UTF_8));
        } catch (CharacterCodingException cce) {
            throw new CommandException(String.format(MESSAGE_NOT_UTF8, getFilePath()), cce);
        } catch (IOException ioe) {
            logger.warning("Failed to read " + resolvedPath + ": " + ioe);
            throw new CommandException(String.format(MESSAGE_READ_FAILED, getFilePath()), ioe);
        }
    }

    /**
     * Returns recognised column names in their canonical spelling, ignoring header case and surrounding spaces.
     *
     * @throws CommandException unless the header matches the current eight-column format or a legacy
     *     six-column or seven-column format.
     */
    private List<String> readColumns(List<CsvRecord> records) throws CommandException {
        boolean isEmpty = records.stream().allMatch(record -> record.text().isBlank());
        if (isEmpty) {
            throw new CommandException(String.format(MESSAGE_EMPTY_FILE, getFilePath()));
        }

        String headerLine = records.getFirst().text().replace(BYTE_ORDER_MARK, "");
        List<String> columns;
        try {
            columns = CsvUtil.parseCsvLine(headerLine).stream()
                    .map(column -> column.trim().toLowerCase(Locale.ROOT))
                    .toList();
        } catch (IllegalValueException ive) {
            throw new CommandException(String.format(MESSAGE_WRONG_HEADER, getFilePath()), ive);
        }
        boolean isKnownHeader = columns.equals(ExportCommand.CSV_COLUMNS.stream()
                .map(column -> column.toLowerCase(Locale.ROOT)).toList())
                || columns.equals(ExportCommand.LEGACY_CSV_COLUMNS)
                || columns.equals(ExportCommand.REQUIRED_CSV_COLUMNS);
        if (!isKnownHeader) {
            throw new CommandException(String.format(MESSAGE_WRONG_HEADER, getFilePath()));
        }
        return columns.size() == ExportCommand.CSV_COLUMNS.size() ? ExportCommand.CSV_COLUMNS : columns;
    }

    /**
     * Returns the employees in {@code rows}, skipping blank records. Errors use the record's starting physical line.
     *
     * @throws CommandException naming the row number if a row is invalid or its employee ID is already used,
     *     either by an earlier row or by an employee in {@code model}.
     */
    private List<Employee> parseEmployees(List<CsvRecord> rows, List<String> columns, Model model)
            throws CommandException {
        List<Employee> employees = new ArrayList<>();
        Map<EmployeeId, Integer> rowNumbersById = new HashMap<>();
        for (CsvRecord row : rows) {
            if (row.text().isBlank()) {
                continue;
            }
            int rowNumber = row.lineNumber();
            Employee employee = parseRow(row.text(), columns, rowNumber);
            checkNotDuplicate(employee, rowNumber, rowNumbersById, model);
            rowNumbersById.put(employee.getId(), rowNumber);
            employees.add(employee);
        }

        if (employees.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NO_EMPLOYEES, getFilePath()));
        }
        List<Employee> combined = new ArrayList<>(model.getAddressBook().getEmployeeList());
        combined.addAll(employees);
        List<Employee> canonical = new ArrayList<>();
        for (Employee employee : employees) {
            try {
                canonical.add(ReportingStructure.withCanonicalManager(combined, employee));
            } catch (IllegalArgumentException e) {
                throw createRowError(rowNumbersById.get(employee.getId()), e.getMessage());
            }
        }
        try {
            ReportingStructure.validate(combined);
        } catch (IllegalArgumentException e) {
            throw new CommandException(String.format(MESSAGE_INVALID_RELATIONSHIPS, e.getMessage()), e);
        }
        return canonical;
    }

    /**
     * Returns the employee in {@code row}, checked with the same rules as the {@code add} command.
     */
    private static Employee parseRow(String row, List<String> columns, int rowNumber) throws CommandException {
        try {
            List<String> values = CsvUtil.parseCsvLine(row);
            if (values.size() != columns.size()) {
                throw new IllegalValueException(String.format(MESSAGE_WRONG_VALUE_COUNT, columns.size(),
                        CsvUtil.toCsvLine(columns), values.size()));
            }
            boolean isArchived = columns.contains(ExportCommand.ARCHIVED_COLUMN)
                    && parseArchived(values.get(columns.indexOf(ExportCommand.ARCHIVED_COLUMN)));
            int managerIndex = columns.indexOf(ExportCommand.MANAGER_COLUMN);
            EmployeeId managerId = managerIndex < 0 || values.get(managerIndex).isBlank() ? null
                    : ParserUtil.parseEmployeeId(values.get(managerIndex));
            // values are in the order of ExportCommand.CSV_COLUMNS
            return new Employee(
                    ParserUtil.parseEmployeeId(values.get(0)),
                    ParserUtil.parseName(values.get(1)),
                    ParserUtil.parsePhone(values.get(2)),
                    ParserUtil.parseEmail(values.get(3)),
                    ParserUtil.parseDepartment(values.get(4)),
                    ParserUtil.parseRole(values.get(5)),
                    isArchived, managerId);
        } catch (IllegalValueException ive) {
            throw new CommandException(String.format(MESSAGE_INVALID_ROW, rowNumber, ive.getMessage()), ive);
        }
    }

    /**
     * Returns true if {@code value} is "true", and false if it is "false" or blank, ignoring case and spaces.
     */
    private static boolean parseArchived(String value) throws IllegalValueException {
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty() || trimmedValue.equalsIgnoreCase(Boolean.FALSE.toString())) {
            return false;
        }
        if (trimmedValue.equalsIgnoreCase(Boolean.TRUE.toString())) {
            return true;
        }
        throw new IllegalValueException(MESSAGE_INVALID_ARCHIVED);
    }

    private static void checkNotDuplicate(Employee employee, int rowNumber, Map<EmployeeId, Integer> rowNumbersById,
            Model model) throws CommandException {
        EmployeeId id = employee.getId();
        if (rowNumbersById.containsKey(id)) {
            throw createRowError(rowNumber, String.format(MESSAGE_DUPLICATE_IN_FILE, id, rowNumbersById.get(id)));
        }
        if (model.hasEmployee(employee)) {
            throw createRowError(rowNumber, String.format(AddCommand.MESSAGE_DUPLICATE_EMPLOYEE, id));
        }
    }

    private static CommandException createRowError(int rowNumber, String problem) {
        return new CommandException(String.format(MESSAGE_INVALID_ROW, rowNumber, problem));
    }
}

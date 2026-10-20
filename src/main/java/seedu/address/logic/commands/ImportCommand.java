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
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;

/**
 * Adds every employee in a CSV file. If any row is invalid, no employee is added.
 */
public class ImportCommand extends Command {

    public static final String COMMAND_WORD = "import";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds every employee in a CSV file. "
            + "If any row is invalid, no employee is added.\n"
            + "Parameters: FILE_PATH (relative to the folder HuntR runs from, ending in .csv)\n"
            + "The file must start with the header " + ExportCommand.CSV_HEADER + "\n"
            + "Example: " + COMMAND_WORD + " exports/staff.csv";

    public static final String MESSAGE_SUCCESS = "Imported %1$d employee(s) from %2$s";
    public static final String MESSAGE_FILE_NOT_FOUND = "File not found: %1$s";
    public static final String MESSAGE_READ_FAILED = "Could not read %1$s. Check that the file is not open in "
            + "another program (such as Excel) and that you are allowed to read it.";
    public static final String MESSAGE_NOT_UTF8 = "Could not read %1$s because it is not saved as UTF-8 text. "
            + "In Excel, save it as \"CSV UTF-8\".";
    public static final String MESSAGE_EMPTY_FILE = "%1$s is empty.";
    public static final String MESSAGE_WRONG_HEADER = "%1$s should start with the header " + ExportCommand.CSV_HEADER;
    public static final String MESSAGE_NO_EMPLOYEES = "%1$s has no employee rows to import.";
    public static final String MESSAGE_INVALID_ROW = "Nothing was imported. Row %1$d: %2$s";
    public static final String MESSAGE_WRONG_VALUE_COUNT = "Expected " + ExportCommand.CSV_COLUMNS.size()
            + " values (" + ExportCommand.CSV_HEADER + ") but found %1$d.";
    public static final String MESSAGE_DUPLICATE_IN_FILE = "Employee ID %1$s is also used on row %2$d.";

    /** Excel adds this invisible character to the start of files saved as "CSV UTF-8". */
    private static final String BYTE_ORDER_MARK = "﻿";
    /** Row numbers count from 1 and include the header, as a spreadsheet program shows them. */
    private static final int FIRST_DATA_ROW_NUMBER = 2;

    private static final Logger logger = LogsCenter.getLogger(ImportCommand.class);

    private final Path filePath;

    /**
     * Creates an ImportCommand to add the employees in the CSV file at {@code filePath}.
     */
    public ImportCommand(Path filePath) {
        requireNonNull(filePath);
        this.filePath = filePath;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<String> lines = readLines();
        checkHeader(lines);
        List<Employee> employees = parseEmployees(lines.subList(1, lines.size()), model);

        employees.forEach(model::addEmployee);
        model.updateFilteredEmployeeList(Model.PREDICATE_SHOW_ALL_EMPLOYEES);

        logger.info("Imported " + employees.size() + " employees from " + filePath);
        return new CommandResult(String.format(MESSAGE_SUCCESS, employees.size(), filePath));
    }

    private List<String> readLines() throws CommandException {
        if (!Files.isRegularFile(filePath)) {
            throw new CommandException(String.format(MESSAGE_FILE_NOT_FOUND, filePath));
        }
        try {
            return Files.readAllLines(filePath, StandardCharsets.UTF_8);
        } catch (CharacterCodingException cce) {
            throw new CommandException(String.format(MESSAGE_NOT_UTF8, filePath), cce);
        } catch (IOException ioe) {
            logger.warning("Failed to read " + filePath + ": " + ioe);
            throw new CommandException(String.format(MESSAGE_READ_FAILED, filePath), ioe);
        }
    }

    /**
     * Throws a {@code CommandException} unless the first line holds the column names in
     * {@link ExportCommand#CSV_COLUMNS}, ignoring letter case and spaces around each name.
     */
    private void checkHeader(List<String> lines) throws CommandException {
        boolean isEmpty = lines.stream().allMatch(String::isBlank);
        if (isEmpty) {
            throw new CommandException(String.format(MESSAGE_EMPTY_FILE, filePath));
        }

        String headerLine = lines.get(0).replace(BYTE_ORDER_MARK, "");
        List<String> columns;
        try {
            columns = CsvUtil.parseCsvLine(headerLine).stream()
                    .map(column -> column.trim().toLowerCase(Locale.ROOT))
                    .toList();
        } catch (IllegalValueException ive) {
            throw new CommandException(String.format(MESSAGE_WRONG_HEADER, filePath), ive);
        }
        if (!columns.equals(ExportCommand.CSV_COLUMNS)) {
            throw new CommandException(String.format(MESSAGE_WRONG_HEADER, filePath));
        }
    }

    /**
     * Returns the employees in {@code rows}, skipping blank rows.
     *
     * @throws CommandException naming the row number if a row is invalid or its employee ID is already used,
     *     either by an earlier row or by an employee in {@code model}.
     */
    private List<Employee> parseEmployees(List<String> rows, Model model) throws CommandException {
        List<Employee> employees = new ArrayList<>();
        Map<EmployeeId, Integer> rowNumbersById = new HashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).isBlank()) {
                continue;
            }
            int rowNumber = i + FIRST_DATA_ROW_NUMBER;
            Employee employee = parseRow(rows.get(i), rowNumber);
            checkNotDuplicate(employee, rowNumber, rowNumbersById, model);
            rowNumbersById.put(employee.getId(), rowNumber);
            employees.add(employee);
        }

        if (employees.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NO_EMPLOYEES, filePath));
        }
        return employees;
    }

    /**
     * Returns the employee in {@code row}, checked with the same rules as the {@code add} command.
     */
    private static Employee parseRow(String row, int rowNumber) throws CommandException {
        try {
            List<String> values = CsvUtil.parseCsvLine(row);
            if (values.size() != ExportCommand.CSV_COLUMNS.size()) {
                throw new IllegalValueException(String.format(MESSAGE_WRONG_VALUE_COUNT, values.size()));
            }
            // values are in the order of ExportCommand.CSV_COLUMNS
            return new Employee(
                    ParserUtil.parseEmployeeId(values.get(0)),
                    ParserUtil.parseName(values.get(1)),
                    ParserUtil.parsePhone(values.get(2)),
                    ParserUtil.parseEmail(values.get(3)),
                    ParserUtil.parseDepartment(values.get(4)),
                    ParserUtil.parseRole(values.get(5)));
        } catch (IllegalValueException ive) {
            throw new CommandException(String.format(MESSAGE_INVALID_ROW, rowNumber, ive.getMessage()), ive);
        }
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

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ImportCommand otherImportCommand)) {
            return false;
        }

        return filePath.equals(otherImportCommand.filePath);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("filePath", filePath)
                .toString();
    }
}

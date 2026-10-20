package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.CsvUtil;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;

/**
 * Exports all employees to a CSV file.
 */
public class ExportCommand extends Command {

    public static final String COMMAND_WORD = "export";

    /** Column names of the CSV files written by {@code export} and read by {@code import}, in order. */
    public static final List<String> CSV_COLUMNS = List.of("id", "name", "phone", "email", "department", "role");
    public static final String CSV_HEADER = CsvUtil.toCsvLine(CSV_COLUMNS);

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Exports all employees to a CSV file, "
            + "replacing the file if it exists.\n"
            + "Parameters: FILE_PATH (relative to the folder HuntR runs from, ending in .csv)\n"
            + "Example: " + COMMAND_WORD + " exports/staff.csv";

    public static final String MESSAGE_SUCCESS = "Exported %1$d employee(s) to %2$s";
    public static final String MESSAGE_WRITE_FAILED = "Could not write to %1$s. Check that the file is not open in "
            + "another program (such as Excel) and that you are allowed to write to that folder.";

    private static final Logger logger = LogsCenter.getLogger(ExportCommand.class);

    private final Path filePath;

    /**
     * Creates an ExportCommand to write all employees to {@code filePath}.
     */
    public ExportCommand(Path filePath) {
        requireNonNull(filePath);
        this.filePath = filePath;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Employee> employees = model.getAddressBook().getEmployeeList();

        try {
            writeCsvFile(employees);
        } catch (IOException ioe) {
            logger.warning("Failed to export employees to " + filePath + ": " + ioe);
            throw new CommandException(String.format(MESSAGE_WRITE_FAILED, filePath), ioe);
        }

        logger.info("Exported " + employees.size() + " employees to " + filePath);
        return new CommandResult(String.format(MESSAGE_SUCCESS, employees.size(), filePath));
    }

    private void writeCsvFile(List<Employee> employees) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(CSV_HEADER);
        employees.forEach(employee -> lines.add(toCsvLine(employee)));

        Path parentFolder = filePath.getParent();
        if (parentFolder != null) {
            Files.createDirectories(parentFolder);
        }
        FileUtil.writeToFile(filePath, String.join(System.lineSeparator(), lines) + System.lineSeparator());
    }

    /**
     * Returns {@code employee} as one CSV line, with its values in the order of {@link #CSV_COLUMNS}.
     */
    public static String toCsvLine(Employee employee) {
        return CsvUtil.toCsvLine(List.of(
                employee.getId().toString(),
                employee.getName().toString(),
                employee.getPhone().toString(),
                employee.getEmail().toString(),
                employee.getDepartment().toString(),
                employee.getRole().toString()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ExportCommand otherExportCommand)) {
            return false;
        }

        return filePath.equals(otherExportCommand.filePath);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("filePath", filePath)
                .toString();
    }
}

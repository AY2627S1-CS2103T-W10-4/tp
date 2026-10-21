package seedu.address.testutil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assumptions;

import seedu.address.commons.core.index.Index;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;

/**
 * A utility class for test cases.
 */
public class TestUtil {

    /**
     * Folder used for temp files created during testing. Ignored by Git.
     */
    private static final Path SANDBOX_FOLDER = Paths.get("src", "test", "data", "sandbox");

    /**
     * Appends {@code fileName} to the sandbox folder path and returns the resulting path.
     * Creates the sandbox folder if it doesn't exist.
     */
    public static Path getFilePathInSandboxFolder(String fileName) {
        try {
            Files.createDirectories(SANDBOX_FOLDER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return SANDBOX_FOLDER.resolve(fileName);
    }

    /**
     * Creates a symbolic link at {@code link} that points to {@code target}, and returns the link.
     * Skips the calling test if this computer does not allow it, as on Windows without the needed privilege.
     */
    public static Path createSymbolicLinkOrSkip(Path link, Path target) {
        try {
            return Files.createSymbolicLink(link, target);
        } catch (IOException | UnsupportedOperationException e) {
            return Assumptions.abort("Symbolic links are not supported here: " + e);
        }
    }

    /**
     * Returns the middle index of the employee in the {@code model}'s employee list.
     */
    public static Index getMidIndex(Model model) {
        return Index.fromOneBased(model.getFilteredEmployeeList().size() / 2);
    }

    /**
     * Returns the last index of the employee in the {@code model}'s employee list.
     */
    public static Index getLastIndex(Model model) {
        return Index.fromOneBased(model.getFilteredEmployeeList().size());
    }

    /**
     * Returns the employee in the {@code model}'s employee list at {@code index}.
     */
    public static Employee getEmployee(Model model, Index index) {
        return model.getFilteredEmployeeList().get(index.getZeroBased());
    }
}

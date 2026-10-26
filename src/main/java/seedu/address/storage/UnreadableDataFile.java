package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;

/**
 * Keeps a copy of a data file that could not be loaded, because the next automatic save overwrites that file.
 */
public class UnreadableDataFile {

    public static final String COPY_PREFIX = "unreadable-";
    public static final String MESSAGE_COPY_KEPT = "Warning: %1$s could not be loaded, so HuntR started with no "
            + "employees. Your next command will overwrite it.\nA copy was kept at %2$s. To restore your employees, "
            + "close HuntR, fix the copy and rename it to %3$s.";
    public static final String MESSAGE_COPY_FAILED = "Warning: %1$s could not be loaded, so HuntR started with no "
            + "employees, and a copy of it could not be made.\nClose HuntR now and back up %1$s, because your next "
            + "command will overwrite it.";

    private static final Logger logger = LogsCenter.getLogger(UnreadableDataFile.class);

    private UnreadableDataFile() {}

    /**
     * Copies {@code dataFile} into its folder under a name starting with {@link #COPY_PREFIX}, replacing any
     * earlier copy, and returns a warning that tells the user where the copy is.
     * If the copy cannot be made, returns a warning that asks the user to back up the file themselves.
     */
    public static String keepCopy(Path dataFile) {
        requireNonNull(dataFile);
        Path copy = getCopyPath(dataFile);
        try {
            Files.copy(dataFile, copy, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException e) {
            logger.warning("Could not copy unreadable data file " + dataFile + ": " + e);
            return String.format(MESSAGE_COPY_FAILED, dataFile);
        }
        logger.info("Kept a copy of unreadable data file " + dataFile + " at " + copy);
        return String.format(MESSAGE_COPY_KEPT, dataFile, copy, dataFile.getFileName());
    }

    /**
     * Returns the path that {@link #keepCopy(Path)} copies {@code dataFile} to.
     */
    public static Path getCopyPath(Path dataFile) {
        requireNonNull(dataFile);
        return dataFile.resolveSibling(COPY_PREFIX + dataFile.getFileName());
    }
}

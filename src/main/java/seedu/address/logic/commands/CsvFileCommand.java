package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.ParserUtil;

/**
 * Represents a command that reads or writes a CSV file inside the folder HuntR runs from.
 */
public abstract class CsvFileCommand extends Command {

    /** The folder HuntR runs from, which holds the data folder. */
    public static final Path DEFAULT_APP_FOLDER = Paths.get("");

    public static final String MESSAGE_CANNOT_CHECK_LOCATION = "Could not check where %1$s is. Check that it is "
            + "not a shortcut (symbolic link) to a missing file or folder.";

    private static final Logger logger = LogsCenter.getLogger(CsvFileCommand.class);

    private final Path appFolder;
    private final Path filePath;

    /**
     * Creates a command for the CSV file at {@code filePath}, relative to {@code appFolder}.
     */
    protected CsvFileCommand(Path appFolder, Path filePath) {
        requireAllNonNull(appFolder, filePath);
        this.appFolder = appFolder;
        this.filePath = filePath;
    }

    /**
     * Returns the file path as given by the user, for messages.
     */
    protected Path getFilePath() {
        return filePath;
    }

    /**
     * Returns the location of the CSV file to read or write.
     *
     * @throws CommandException if that location is outside the app folder once symbolic links are followed,
     *     for example because a folder in the path is a link to another folder.
     */
    protected Path resolveFilePath() throws CommandException {
        Path resolvedPath = appFolder.resolve(filePath);
        boolean isInsideAppFolder;
        try {
            isInsideAppFolder = FileUtil.isInsideFolder(resolvedPath, appFolder);
        } catch (IOException ioe) {
            logger.warning("Failed to check the location of " + resolvedPath + ": " + ioe);
            throw new CommandException(String.format(MESSAGE_CANNOT_CHECK_LOCATION, filePath), ioe);
        }

        if (!isInsideAppFolder) {
            logger.warning("Rejected " + resolvedPath + " because it leads outside " + appFolder.toAbsolutePath());
            throw new CommandException(ParserUtil.MESSAGE_FILE_PATH_OUTSIDE_APP_FOLDER);
        }
        return resolvedPath;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // handles nulls, and tells apart import and export commands with the same path
        if (other == null || other.getClass() != getClass()) {
            return false;
        }

        CsvFileCommand otherCsvFileCommand = (CsvFileCommand) other;
        return appFolder.equals(otherCsvFileCommand.appFolder)
                && filePath.equals(otherCsvFileCommand.filePath);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("filePath", filePath)
                .toString();
    }
}

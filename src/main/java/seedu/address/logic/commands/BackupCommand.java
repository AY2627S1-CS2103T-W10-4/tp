package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;

/**
 * Saves a copy of the current employee data to a time-stamped file in the backup folder.
 */
public class BackupCommand extends Command {

    public static final String COMMAND_WORD = "backup";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Saves a copy of the current employee data to "
            + "the data/backups folder.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "Backed up %1$d employee(s) to %2$s";
    public static final String MESSAGE_WRITE_FAILED = "Could not save the backup to %1$s. Check that you are "
            + "allowed to write to that folder.";

    public static final Path DEFAULT_BACKUP_FOLDER = Paths.get("data", "backups");

    private static final String FILE_NAME_FORMAT = "huntr-%1$s-%2$010d.json";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private static final Logger logger = LogsCenter.getLogger(BackupCommand.class);

    private final Path backupFolder;
    private final Clock clock;

    /**
     * Creates a BackupCommand that saves backups to {@link #DEFAULT_BACKUP_FOLDER}, named by the current time.
     */
    public BackupCommand() {
        this(DEFAULT_BACKUP_FOLDER, Clock.systemDefaultZone());
    }

    /**
     * Creates a BackupCommand that saves backups to {@code backupFolder}, named by the time given by {@code clock}.
     */
    public BackupCommand(Path backupFolder, Clock clock) {
        requireAllNonNull(backupFolder, clock);
        this.backupFolder = backupFolder;
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        ReadOnlyAddressBook addressBook = model.getAddressBook();
        String timestamp = LocalDateTime.now(clock).format(TIMESTAMP_FORMATTER);
        Path backupPath = backupFolder.resolve(String.format(Locale.ROOT, FILE_NAME_FORMAT, timestamp, 0));

        try {
            int suffix = 0;
            while (true) {
                try {
                    createStorage(backupPath).saveNewAddressBook(addressBook);
                    break;
                } catch (FileAlreadyExistsException collision) {
                    if (!Files.exists(backupPath, LinkOption.NOFOLLOW_LINKS)) {
                        throw collision;
                    }
                    if (suffix == Integer.MAX_VALUE) {
                        throw new IOException("No available backup sequence number for " + timestamp, collision);
                    }
                    suffix++;
                    backupPath = backupFolder.resolve(String.format(Locale.ROOT, FILE_NAME_FORMAT, timestamp, suffix));
                }
            }
        } catch (IOException ioe) {
            logger.warning("Failed to save backup to " + backupPath + ": " + ioe);
            throw new CommandException(String.format(MESSAGE_WRITE_FAILED, backupPath), ioe);
        }

        int employeeCount = addressBook.getEmployeeList().size();
        logger.info("Backed up " + employeeCount + " employees to " + backupPath);
        return new CommandResult(String.format(MESSAGE_SUCCESS, employeeCount, backupPath));
    }

    JsonAddressBookStorage createStorage(Path backupPath) {
        return new JsonAddressBookStorage(backupPath);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof BackupCommand otherBackupCommand)) {
            return false;
        }

        return backupFolder.equals(otherBackupCommand.backupFolder)
                && clock.equals(otherBackupCommand.clock);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("backupFolder", backupFolder)
                .toString();
    }
}

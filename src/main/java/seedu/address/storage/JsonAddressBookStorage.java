package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);

    private Path filePath;
    private final AtomicFileWriter writer;

    public JsonAddressBookStorage(Path filePath) {
        this(filePath, new AtomicFileWriter());
    }

    JsonAddressBookStorage(Path filePath, AtomicFileWriter writer) {
        this.filePath = filePath;
        this.writer = requireNonNull(writer);
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        Optional<JsonSerializableAddressBook> jsonAddressBook = JsonUtil.readJsonFile(
                filePath, JsonSerializableAddressBook.class);
        if (!jsonAddressBook.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonAddressBook.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Reads a backup using strict JSON validation, without changing normal startup loading.
     * Returns {@code Optional.empty()} if the backup does not exist.
     *
     * @throws DataLoadingException if the backup cannot be read or validated.
     */
    public Optional<ReadOnlyAddressBook> readBackup() throws DataLoadingException {
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        try {
            JsonSerializableAddressBook backup = JsonUtil.fromJsonStringStrict(
                    FileUtil.readFromFile(filePath), JsonSerializableAddressBook.class);
            return Optional.of(backup.toModelType());
        } catch (IOException | IllegalValueException e) {
            throw new DataLoadingException(e);
        }
    }
  
    /**
     * Saves a new snapshot without replacing an existing file or symbolic link at this storage path.
     *
     * @throws java.nio.file.FileAlreadyExistsException if the destination already exists.
     * @throws IOException if the snapshot could not be written.
     */
    public void saveNewAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);
        JsonSerializableAddressBook jsonAddressBook = new JsonSerializableAddressBook(addressBook);
        byte[] contents = JsonUtil.toJsonString(jsonAddressBook).getBytes(StandardCharsets.UTF_8);
        writer.writeNew(filePath, contents);
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        JsonSerializableAddressBook jsonAddressBook = new JsonSerializableAddressBook(addressBook);
        byte[] contents = JsonUtil.toJsonString(jsonAddressBook).getBytes(StandardCharsets.UTF_8);
        writer.write(filePath, contents);
    }

}

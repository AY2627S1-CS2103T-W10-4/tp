package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class UnreadableDataFileTest {

    private static final String UNREADABLE_CONTENT = "{ \"employees\" : [ { \"phone\" : \"9123456X\" } ] }";

    @TempDir
    public Path folder;

    @Test
    public void getCopyPath_dataFile_prefixedFileInSameFolder() {
        assertEquals(folder.resolve("unreadable-addressbook.json"),
                UnreadableDataFile.getCopyPath(folder.resolve("addressbook.json")));
    }

    @Test
    public void keepCopy_nullPath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> UnreadableDataFile.keepCopy(null));
    }

    @Test
    public void keepCopy_existingFile_copiesFileAndReturnsCopyKeptWarning() throws Exception {
        Path dataFile = folder.resolve("addressbook.json");
        Files.writeString(dataFile, UNREADABLE_CONTENT);
        Path copy = UnreadableDataFile.getCopyPath(dataFile);

        assertEquals(String.format(UnreadableDataFile.MESSAGE_COPY_KEPT, dataFile, copy),
                UnreadableDataFile.keepCopy(dataFile));
        assertEquals(UNREADABLE_CONTENT, Files.readString(copy));
        assertEquals(UNREADABLE_CONTENT, Files.readString(dataFile));
    }

    @Test
    public void keepCopy_earlierCopyExists_replacesEarlierCopy() throws Exception {
        Path dataFile = folder.resolve("addressbook.json");
        Files.writeString(dataFile, UNREADABLE_CONTENT);
        Files.writeString(UnreadableDataFile.getCopyPath(dataFile), "older copy");

        UnreadableDataFile.keepCopy(dataFile);
        assertEquals(UNREADABLE_CONTENT, Files.readString(UnreadableDataFile.getCopyPath(dataFile)));
    }

    @Test
    public void keepCopy_missingFile_returnsCopyFailedWarning() {
        Path dataFile = folder.resolve("missing.json");

        assertEquals(String.format(UnreadableDataFile.MESSAGE_COPY_FAILED, dataFile),
                UnreadableDataFile.keepCopy(dataFile));
        assertFalse(Files.exists(UnreadableDataFile.getCopyPath(dataFile)));
    }
}

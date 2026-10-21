package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestUtil.createSymbolicLinkOrSkip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileUtilTest {

    @TempDir
    public Path testFolder;

    private Path folder;
    private Path outsideFolder;

    @BeforeEach
    public void setUp() throws Exception {
        folder = Files.createDirectory(testFolder.resolve("app"));
        outsideFolder = Files.createDirectory(testFolder.resolve("outside"));
    }

    @Test
    public void isInsideFolder_pathsInsideFolder_returnsTrue() throws Exception {
        assertTrue(FileUtil.isInsideFolder(folder, folder));
        assertTrue(FileUtil.isInsideFolder(Files.createFile(folder.resolve("staff.csv")), folder));
        assertTrue(FileUtil.isInsideFolder(folder.resolve("missing").resolve("staff.csv"), folder));
    }

    @Test
    public void isInsideFolder_pathsOutsideFolder_returnsFalse() throws Exception {
        assertFalse(FileUtil.isInsideFolder(outsideFolder.resolve("staff.csv"), folder));
        assertFalse(FileUtil.isInsideFolder(folder.resolve("..").resolve("outside"), folder));
        // a sibling folder whose name starts with the same text is still outside
        assertFalse(FileUtil.isInsideFolder(testFolder.resolve("app2").resolve("staff.csv"), folder));
    }

    @Test
    public void isInsideFolder_linkToOutsideFolder_returnsFalse() throws Exception {
        Path link = createSymbolicLinkOrSkip(folder.resolve("exports"), outsideFolder);

        assertFalse(FileUtil.isInsideFolder(link.resolve("staff.csv"), folder));
        assertFalse(FileUtil.isInsideFolder(link.resolve("missing").resolve("staff.csv"), folder));
    }

    @Test
    public void isInsideFolder_linkToInsideFolder_returnsTrue() throws Exception {
        Path realFolder = Files.createDirectory(folder.resolve("real"));
        Path link = createSymbolicLinkOrSkip(folder.resolve("exports"), realFolder);

        assertTrue(FileUtil.isInsideFolder(link.resolve("staff.csv"), folder));
    }

    @Test
    public void isInsideFolder_brokenLink_throwsIoException() {
        createSymbolicLinkOrSkip(folder.resolve("staff.csv"), outsideFolder.resolve("missing.csv"));

        assertThrows(IOException.class, () -> FileUtil.isInsideFolder(folder.resolve("staff.csv"), folder));
    }

    @Test
    public void isInsideFolder_missingFolder_throwsIoException() {
        assertThrows(IOException.class, () ->
                FileUtil.isInsideFolder(folder.resolve("staff.csv"), testFolder.resolve("missing")));
    }

}

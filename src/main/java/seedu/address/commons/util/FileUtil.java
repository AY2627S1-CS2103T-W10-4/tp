package seedu.address.commons.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/**
 * Writes and reads files
 */
public class FileUtil {

    private static final String CHARSET = "UTF-8";

    /**
     * Creates a file if it does not exist along with its missing parent directories.
     * @throws IOException if the file or directory cannot be created.
     */
    public static void createIfMissing(Path file) throws IOException {
        if (Files.exists(file)) {
            return;
        }

        createParentDirsOfFile(file);

        Files.createFile(file);
    }

    /**
     * Creates parent directories of file if it has a parent directory
     */
    private static void createParentDirsOfFile(Path file) throws IOException {
        Path parentDir = file.getParent();

        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Assumes file exists
     */
    public static String readFromFile(Path file) throws IOException {
        return new String(Files.readAllBytes(file), CHARSET);
    }

    /**
     * Writes given string to a file.
     * Will create the file if it does not exist yet.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(CHARSET));
    }

    /**
     * Returns true if {@code path} is inside {@code folder} once symbolic links in both are followed.
     * Parts of {@code path} that do not exist yet are taken as they are written.
     *
     * @throws IOException if {@code folder} does not exist, or a symbolic link in {@code path} cannot be followed.
     */
    public static boolean isInsideFolder(Path path, Path folder) throws IOException {
        Path absolutePath = path.toAbsolutePath().normalize();
        Path existingPart = absolutePath;
        while (!Files.exists(existingPart, LinkOption.NOFOLLOW_LINKS)) {
            existingPart = existingPart.getParent();
            assert existingPart != null : "The root of an absolute path always exists";
        }
        Path realPath = existingPart.toRealPath().resolve(existingPart.relativize(absolutePath));
        return realPath.startsWith(folder.toRealPath());
    }

}

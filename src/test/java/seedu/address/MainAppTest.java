package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonUserPrefsStorage;

public class MainAppTest {

    @TempDir
    public Path testFolder;

    @Test
    public void initPrefs_nullGuiSettings_recoversWithDefaults() throws Exception {
        Path prefsPath = testFolder.resolve("preferences.json");
        Files.writeString(prefsPath, "{\"guiSettings\":null}");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(prefsPath);

        UserPrefs prefs = new MainApp().initPrefs(storage);

        assertEquals(new UserPrefs(), prefs);
        assertEquals(new UserPrefs(), storage.readUserPrefs().orElseThrow());
        assertEquals(new UserPrefs().getGuiSettings(), new ModelManager(new AddressBook(), prefs).getGuiSettings());
    }
}

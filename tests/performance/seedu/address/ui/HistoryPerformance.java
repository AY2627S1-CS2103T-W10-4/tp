package seedu.address.ui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Department;
import seedu.address.model.employee.Email;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.Name;
import seedu.address.model.employee.Phone;
import seedu.address.model.employee.Role;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Runs three opt-in UI acceptance sessions; timing thresholds never run in the JUnit suite. */
public class HistoryPerformance {
    private final Path output;
    private final AddressBook fixture = new AddressBook();
    private final List<String> measurements = new ArrayList<>();
    private ModelManager model;
    private JsonAddressBookStorage storage;
    private Stage stage;
    private Scene scene;
    private TextField commandBox;
    private TextArea resultDisplay;
    private int session;
    private int operation;

    private HistoryPerformance(Path output) {
        this.output = output;
        measurements.add("session,command,step,milliseconds");
        for (int i = 0; i < 1000; i++) {
            fixture.addEmployee(new Employee(new EmployeeId(String.format("E%04d", i)),
                    new Name(padded("Employee " + i)), new Phone("9".repeat(100)),
                    new Email("employee" + String.format("%04d", i) + "a".repeat(76) + "@example.com"),
                    new Department(padded("Engineering")), new Role(role(0))));
        }
    }

    /** Starts the actual application UI with isolated test storage. */
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> fail(error));
        HistoryPerformance runner = new HistoryPerformance(Path.of(args[0]));
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            runner.startSession();
        });
    }

    private void startSession() {
        try {
            if (stage != null) {
                stage.hide();
            }
            session++;
            operation = 0;
            model = new ModelManager(fixture, new UserPrefs());
            storage = new JsonAddressBookStorage(output.resolve("session-" + session + ".json"));
            storage.saveAddressBook(model.getAddressBook());
            LogicManager logic = new LogicManager(model, new StorageManager(storage,
                    new JsonUserPrefsStorage(output.resolve("preferences.json"))));
            stage = new Stage();
            MainWindow window = new MainWindow(stage, logic, storage.getAddressBookFilePath());
            window.fillInnerParts(Optional.empty());
            window.show();
            scene = stage.getScene();
            commandBox = (TextField) scene.lookup("#commandTextField");
            resultDisplay = (TextArea) scene.lookup("#resultDisplay");
            verifyErrorStyling();
            scene.snapshot(snapshot -> {
                Platform.runLater(this::dispatch);
                return null;
            }, null);
        } catch (Throwable error) {
            fail(error);
        }
    }

    private void verifyErrorStyling() {
        for (String command : List.of("undo", "redo", "undo 2", "redo now", "UNDO")) {
            commandBox.setText(command);
            commandBox.fireEvent(new ActionEvent());
            require(commandBox.getText().equals(command), "Error input was cleared");
            require(commandBox.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS), "Missing error styling");
            require(!model.canUndoAddressBook() && !model.canRedoAddressBook(), "Error changed history");
            require(model.getAddressBook().getEmployeeList().size() == 1000, "Error changed data");
        }
    }

    private void dispatch() {
        try {
            boolean isEdit = operation < 100;
            boolean isUndo = operation >= 100 && operation < 200;
            int expectedVersion = isEdit ? operation + 1 : isUndo ? 199 - operation : operation - 199;
            String command = isEdit ? "edit id/E0000 r/" + role(expectedVersion) : isUndo ? "undo" : "redo";
            commandBox.setText(command);
            long start = System.nanoTime();
            commandBox.fireEvent(new ActionEvent());
            // The asynchronous snapshot renders the next frame, including CSS and layout, before its callback.
            scene.snapshot(snapshot -> {
                double milliseconds = (System.nanoTime() - start) / 1_000_000.0;
                try {
                    verifyCompletedFrame(expectedVersion, isEdit, isUndo);
                    if (!isEdit) {
                        measurements.add(String.format(Locale.ROOT, "%d,%s,%d,%.3f", session, command,
                                operation % 100 + 1, milliseconds));
                    }
                    if (session == 3 && operation == 299) {
                        writeImage(snapshot.getImage());
                    }
                    operation++;
                    if (operation < 300) {
                        Platform.runLater(this::dispatch);
                    } else if (session < 3) {
                        Platform.runLater(this::startSession);
                    } else {
                        finish();
                    }
                } catch (Throwable error) {
                    fail(error);
                }
                return null;
            }, null);
        } catch (Throwable error) {
            fail(error);
        }
    }

    private void verifyCompletedFrame(int expectedVersion, boolean isEdit, boolean isUndo) throws Exception {
        require(commandBox.getText().isEmpty(), "Success did not clear the command box");
        require(!commandBox.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS), "Success retained error styling");
        require(model.getAddressBook().getEmployeeList().size() == 1000, "Stored count changed");
        ListView<?> list = (ListView<?>) scene.lookup("#employeeListView");
        require(list.getItems() == model.getFilteredEmployeeList(), "UI list was replaced");
        require(list.getItems().size() == 1000, "Visible count changed");
        require(model.getAddressBook().getEmployeeList().getFirst().getRole().value.equals(role(expectedVersion)),
                "Wrong restored employee value");
        require(scene.getRoot().lookupAll("#role").stream().anyMatch(node -> node instanceof Label label
                && label.getText().equals("Role: " + role(expectedVersion))), "Rendered card did not update");
        if (!isEdit) {
            require(resultDisplay.getText().equals(isUndo ? "Undo successful." : "Redo successful."),
                    "Wrong rendered command result");
        }
        // Read-back assertions are outside the timed interval.
        require(storage.readAddressBook().orElseThrow().getEmployeeList().getFirst().getRole().value
                .equals(role(expectedVersion)), "Saved data did not match rendered data");
    }

    private void finish() throws IOException {
        Files.write(output.resolve("timings.csv"), measurements);
        double maximum = measurements.stream().skip(1).mapToDouble(row -> Double.parseDouble(row.split(",")[3]))
                .max().orElseThrow();
        require(measurements.size() == 601, "Expected exactly 600 measured history operations");
        String summary = String.format(Locale.ROOT,
                "operations=600%nmaximum_ms=%.3f%njava=%s%nos=%s %s %s%nwindow=740x600%n", maximum,
                System.getProperty("java.runtime.version"), System.getProperty("os.name"),
                System.getProperty("os.version"), System.getProperty("os.arch"));
        Files.writeString(output.resolve("summary.txt"), summary);
        System.out.println(summary);
        require(maximum <= 2000, "History operation exceeded two seconds; see timings.csv");
        stage.hide();
        Platform.exit();
    }

    private void writeImage(Image image) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage rendered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rendered.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            }
        }
        ImageIO.write(rendered, "png", output.resolve("completed-frame.png").toFile());
    }

    private static String role(int version) {
        return padded("Role " + version);
    }

    private static String padded(String prefix) {
        return prefix + "a".repeat(100 - prefix.length());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void fail(Throwable error) {
        error.printStackTrace();
        System.exit(1);
    }
}

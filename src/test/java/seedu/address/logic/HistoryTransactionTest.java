package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.EmployeeDataAssertions.assertStoredEmployees;
import static seedu.address.testutil.TypicalEmployees.ALICE;
import static seedu.address.testutil.TypicalEmployees.BENSON;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.logic.commands.BackupCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.commands.ImportCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.EmployeeBuilder;
import seedu.address.testutil.EmployeeUtil;

public class HistoryTransactionTest {
    private static final Employee ARCHIVED = BENSON.withArchived(true);
    private static final Employee FIRST_EDIT = new EmployeeBuilder(ALICE).withRole("First edit").build();
    private static final Employee SECOND_EDIT = new EmployeeBuilder(ALICE).withRole("Second edit").build();

    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private FailingStorage storage;
    private LogicManager logic;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager(initial(), new UserPrefs());
        seedHistory();
        storage = new FailingStorage(temporaryFolder.resolve("employees.json"));
        logic = createLogic(new AddressBookParser());
        Files.writeString(temporaryFolder.resolve("import.csv"),
                "id,name,phone,email,department,role\n"
                        + "E9001,Chris Tan,91234567,chris@example.com,Engineering,Developer\n"
                        + "e9002,Dana Tan,92345678,dana@example.com,Sales,Manager\n");
    }

    @Test
    public void execute_eachDataChangingCommand_saveFailureRestoresAllState() throws Exception {
        List<String> commands = List.of(EmployeeUtil.getAddCommand(new EmployeeBuilder().withId("E9000").build()),
                "edit id/" + ALICE.getId() + " n/Changed Name", "delete id/" + BENSON.getId(), "clear", "import",
                "archive id/" + ALICE.getId(), "unarchive id/" + BENSON.getId(), "undo", "redo");
        for (String command : commands) {
            setUp();
            if (command.equals("import")) {
                logic = logicFor(new ImportCommand(temporaryFolder, Path.of("import.csv")));
            }
            model.showArchivedEmployees();
            Object predicate = filtered().getPredicate();
            ObservableList<Employee> stored = model.getAddressBook().getEmployeeList();
            ObservableList<Employee> visible = model.getFilteredEmployeeList();
            storage.failure = new IOException("save failed");
            CommandException failure = assertThrows(CommandException.class, () -> logic.execute(command), command);
            assertEquals("Could not save data due to the following error: save failed", failure.getMessage());
            assertEquals(1, storage.saves);
            assertSame(stored, model.getAddressBook().getEmployeeList());
            assertSame(visible, model.getFilteredEmployeeList());
            assertSame(predicate, filtered().getPredicate());
            assertStoredEmployees(List.of(ARCHIVED), visible);
            assertEntireHistoryRestored();
        }
    }

    @Test
    public void execute_permissionIoAndRuntimeFailures_restoreThenPreservePresentationOrOriginalException()
            throws Exception {
        List<Exception> failures = List.of(new AccessDeniedException("target", "other", "reason"),
                new IOException("disk error"), new IllegalStateException("serialization or saving failed"));
        for (Exception failure : failures) {
            setUp();
            storage.failure = failure;
            if (failure instanceof RuntimeException) {
                assertSame(failure, assertThrows(RuntimeException.class, () -> logic.execute("clear")));
            } else {
                CommandException actual = assertThrows(CommandException.class, () -> logic.execute("clear"));
                String template = failure instanceof AccessDeniedException
                        ? LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT : LogicManager.FILE_OPS_ERROR_FORMAT;
                assertEquals(String.format(template, failure.getMessage()), actual.getMessage());
                assertSame(failure, actual.getCause());
            }
            assertEntireHistoryRestored();
        }
    }

    @Test
    public void execute_runtimeDuringSerialization_restoresEntireHistory() {
        RuntimeException failure = new IllegalStateException("serialization failed");
        storage.serializationFailure = failure;
        assertSame(failure, assertThrows(RuntimeException.class, () -> logic.execute("clear")));
        assertEntireHistoryRestored();
    }

    @Test
    public void execute_declaredModification_commitsWithoutCommandBookkeeping() throws Exception {
        CommandResult result = new CommandResult("Modified employees");
        logic = logicFor(new Command() {
            @Override
            public HistoryAction getHistoryAction() {
                return HistoryAction.COMMIT;
            }

            @Override
            public CommandResult execute(Model target) {
                target.clearEmployees();
                return result;
            }
        });

        assertSame(result, logic.execute("injected command"));
        assertEquals(1, storage.saves);
        assertStoredEmployees(List.of(ARCHIVED), model.getAddressBook().getEmployeeList());
        assertFalse(model.canRedoAddressBook());
        model.undoAddressBook();
        assertStoredEmployees(List.of(FIRST_EDIT, ARCHIVED), model.getAddressBook().getEmployeeList());
        model.redoAddressBook();
        assertStoredEmployees(List.of(ARCHIVED), model.getAddressBook().getEmployeeList());
    }

    @Test
    public void execute_commitFailure_restoresWorkingDataViewAndDiscardedRedoTail() {
        RuntimeException failure = new IllegalStateException("commit failed");
        model = new ModelManager(initial(), new UserPrefs()) {
            private int commits;

            @Override
            public void commitAddressBook() {
                super.commitAddressBook();
                if (++commits == 3) {
                    throw failure;
                }
            }
        };
        seedHistory();
        model.showArchivedEmployees();
        Object predicate = filtered().getPredicate();
        logic = createLogic(new AddressBookParser());

        assertSame(failure, assertThrows(RuntimeException.class, () -> logic.execute("clear")));
        assertEquals(0, storage.saves);
        assertSame(predicate, filtered().getPredicate());
        assertEntireHistoryRestored();
    }

    @Test
    public void execute_partialImportFailure_restoresWorkingDataViewAndRedoTail() {
        RuntimeException failure = new IllegalStateException("addition failed");
        model = new ModelManager(initial(), new UserPrefs()) {
            private int additions;

            @Override
            public void addEmployee(Employee employee) {
                if (++additions == 2) {
                    throw failure;
                }
                super.addEmployee(employee);
            }
        };
        seedHistory();
        model.showArchivedEmployees();
        Object predicate = filtered().getPredicate();
        logic = logicFor(new ImportCommand(temporaryFolder, Path.of("import.csv")));
        assertSame(failure, assertThrows(RuntimeException.class, () -> logic.execute("import")));
        assertEquals(0, storage.saves);
        assertSame(predicate, filtered().getPredicate());
        assertStoredEmployees(List.of(ARCHIVED), filtered());
        assertEntireHistoryRestored();
    }

    @Test
    public void execute_commandExceptionAfterTentativeCommit_restoresDiscardedRedoTail() {
        CommandException failure = new CommandException("execution failed after commit");
        logic = logicFor(new Command() {
            @Override
            public HistoryAction getHistoryAction() {
                return HistoryAction.COMMIT;
            }

            @Override
            public CommandResult execute(Model target) throws CommandException {
                target.clearEmployees();
                target.commitAddressBook();
                throw failure;
            }
        });
        model.showArchivedEmployees();
        Object predicate = filtered().getPredicate();
        assertSame(failure, assertThrows(CommandException.class, () -> logic.execute("injected command")));
        assertEquals(0, storage.saves);
        assertSame(predicate, filtered().getPredicate());
        assertEntireHistoryRestored();
    }

    @Test
    public void execute_noOpAfterUndoThenSaveFailure_restoresOriginalViewAndRedo() throws Exception {
        for (String command : List.of("edit id/" + ALICE.getId() + " r/Software Engineer", "clear")) {
            Employee archivedAlice = new EmployeeBuilder(ALICE).withRole("Software Engineer")
                    .withArchived(true).build();
            model = new ModelManager(new AddressBookBuilder().withEmployee(archivedAlice).build(), new UserPrefs());
            model.setEmployee(archivedAlice, FIRST_EDIT.withArchived(true));
            model.commitAddressBook();
            model.setEmployee(FIRST_EDIT.withArchived(true), SECOND_EDIT.withArchived(true));
            model.commitAddressBook();
            model.undoAddressBook();
            model.undoAddressBook();
            model.showArchivedEmployees();
            Object predicate = filtered().getPredicate();
            logic = createLogic(new AddressBookParser());
            storage.failure = new IOException("save failed");
            assertThrows(CommandException.class, () -> logic.execute(command));
            assertSame(predicate, filtered().getPredicate());
            assertStoredEmployees(List.of(archivedAlice), filtered());
            assertFalse(model.canUndoAddressBook());
            model.redoAddressBook();
            assertStoredEmployees(List.of(FIRST_EDIT.withArchived(true)), filtered());
            model.redoAddressBook();
            assertStoredEmployees(List.of(SECOND_EDIT.withArchived(true)), filtered());
            assertFalse(model.canRedoAddressBook());
        }
    }

    @Test
    public void execute_noOpsAndReadOnlyCommands_preserveRedoAndStillSave() throws Exception {
        logic.execute("edit id/" + ALICE.getId() + " r/First edit");
        for (String command : List.of("list", "list archived", "find Nobody", "help", "exit")) {
            logic.execute(command);
            assertTrue(model.canRedoAddressBook());
        }
        assertEquals(6, storage.saves);
        Path exported = temporaryFolder.resolve("export.csv");
        logicFor(new ExportCommand(temporaryFolder, Path.of("export.csv"))).execute("export");
        logicFor(new BackupCommand(temporaryFolder.resolve("backups"), Clock.systemUTC())).execute("backup");
        assertTrue(model.canRedoAddressBook());
        logic.execute("redo");
        logic.execute("undo");
        assertTrue(Files.exists(exported));
        try (var backups = Files.list(temporaryFolder.resolve("backups"))) {
            assertEquals(1, backups.count());
        }
        assertEntireHistoryRestored();
    }

    @Test
    public void execute_emptyClearAfterUndo_preservesRedoAndExistingSuccessAndFilter() throws Exception {
        model = new ModelManager(new AddressBookBuilder().withEmployee(ARCHIVED).build(), new UserPrefs());
        model.unarchiveEmployee(ARCHIVED);
        model.commitAddressBook();
        model.undoAddressBook();
        model.showArchivedEmployees();
        logic = createLogic(new AddressBookParser());
        assertEquals("Active employee records have been cleared!", logic.execute("clear").getFeedbackToUser());
        assertTrue(filtered().isEmpty());
        assertFalse(model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());
        assertEquals(1, storage.saves);
        logic.execute("redo");
        assertStoredEmployees(List.of(BENSON), filtered());
    }

    @Test
    public void execute_invalidCommands_preserveViewDataHistoryAndSkipSaving() {
        model.showArchivedEmployees();
        Object predicate = filtered().getPredicate();
        for (String command : List.of("undo 2", "redo now", "UNDO", "edit id/" + ALICE.getId())) {
            assertThrows(ParseException.class, () -> logic.execute(command));
        }
        for (String command : List.of("delete id/missing", EmployeeUtil.getAddCommand(ALICE))) {
            assertThrows(CommandException.class, () -> logic.execute(command));
        }
        assertEquals(0, storage.saves);
        assertSame(predicate, filtered().getPredicate());
        assertEntireHistoryRestored();
    }

    @Test
    public void execute_historySyntaxCheckedBeforeAvailability() {
        model = new ModelManager();
        logic = createLogic(new AddressBookParser());
        assertEquals("Invalid command format.\nundo: Reverts the most recent data-changing command.\n"
                        + "Parameters: none\nExample: undo",
                assertThrows(ParseException.class, () -> logic.execute("undo 2")).getMessage());
        assertEquals("Invalid command format.\nredo: Reapplies the most recently undone data-changing command.\n"
                        + "Parameters: none\nExample: redo",
                assertThrows(ParseException.class, () -> logic.execute("redo 2")).getMessage());
        assertEquals("No more commands to undo.",
                assertThrows(CommandException.class, () -> logic.execute("undo")).getMessage());
        assertEquals("No more commands to redo.",
                assertThrows(CommandException.class, () -> logic.execute("redo")).getMessage());
        assertEquals(0, storage.saves);
    }

    @Test
    public void execute_actualChangeAfterUndo_invalidatesRedo() throws Exception {
        logic.execute("edit id/" + ALICE.getId() + " r/New role");
        assertFalse(model.canRedoAddressBook());
        logic.execute("undo");
        assertStoredEmployees(List.of(FIRST_EDIT, ARCHIVED), model.getAddressBook().getEmployeeList());
        logic.execute("redo");
        assertEquals("New role", model.getAddressBook().getEmployeeList().getFirst().getRole().value);
    }

    @Test
    public void execute_readOnlySaveFailure_keepsViewChangeAndExternalFiles() throws Exception {
        storage.failure = new IOException("save failed");
        assertThrows(CommandException.class, () -> logic.execute("list archived"));
        assertStoredEmployees(List.of(ARCHIVED), filtered());
        LogicManager exportLogic = logicFor(new ExportCommand(temporaryFolder, Path.of("export.csv")));
        assertThrows(CommandException.class, () -> exportLogic.execute("export"));
        assertTrue(Files.exists(temporaryFolder.resolve("export.csv")));
        LogicManager backupLogic = logicFor(new BackupCommand(temporaryFolder.resolve("backups"), Clock.systemUTC()));
        assertThrows(CommandException.class, () -> backupLogic.execute("backup"));
        try (var backups = Files.list(temporaryFolder.resolve("backups"))) {
            assertEquals(1, backups.count());
        }
        assertEntireHistoryRestored();
    }

    @Test
    public void execute_undoAndRedoSaveReload_startsNewSessionWithoutHistory() throws Exception {
        for (String command : List.of("  undo\t ", "\tredo  ")) {
            logic.execute(command);
            ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
            assertStoredEmployees(model.getAddressBook().getEmployeeList(),
                    reloaded.getAddressBook().getEmployeeList());
            assertFalse(reloaded.canUndoAddressBook());
            assertFalse(reloaded.canRedoAddressBook());
            String json = Files.readString(storage.getAddressBookFilePath());
            assertFalse(json.contains("history"));
            assertFalse(json.contains("predicate"));
            assertFalse(json.contains("checkpoint"));
        }
    }

    private AddressBook initial() {
        return new AddressBookBuilder().withEmployee(ALICE).withEmployee(ARCHIVED).build();
    }

    private void seedHistory() {
        model.setEmployee(ALICE, FIRST_EDIT);
        model.commitAddressBook();
        model.setEmployee(FIRST_EDIT, SECOND_EDIT);
        model.commitAddressBook();
        model.undoAddressBook();
    }

    private void assertEntireHistoryRestored() {
        assertStoredEmployees(List.of(FIRST_EDIT, ARCHIVED), model.getAddressBook().getEmployeeList());
        assertTrue(model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());
        model.redoAddressBook();
        assertStoredEmployees(List.of(SECOND_EDIT, ARCHIVED), model.getAddressBook().getEmployeeList());
        assertFalse(model.canRedoAddressBook());
        model.undoAddressBook();
        assertStoredEmployees(List.of(FIRST_EDIT, ARCHIVED), model.getAddressBook().getEmployeeList());
        model.undoAddressBook();
        assertStoredEmployees(List.of(ALICE, ARCHIVED), model.getAddressBook().getEmployeeList());
        assertFalse(model.canUndoAddressBook());
    }

    private FilteredList<Employee> filtered() {
        // the displayed list is a sorted view of the filtered list
        return (FilteredList<Employee>) ((SortedList<Employee>) model.getFilteredEmployeeList()).getSource();
    }

    private LogicManager createLogic(AddressBookParser parser) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))), parser);
    }

    private LogicManager logicFor(Command command) {
        return createLogic(new AddressBookParser() {
            @Override
            public Command parseCommand(String text) {
                return command;
            }
        });
    }

    private static class FailingStorage extends JsonAddressBookStorage {
        private Exception failure;
        private RuntimeException serializationFailure;
        private int saves;

        FailingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saves++;
            if (failure instanceof IOException ioException) {
                throw ioException;
            }
            if (failure instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (serializationFailure != null) {
                super.saveAddressBook(() -> {
                    throw serializationFailure;
                });
            }
            super.saveAddressBook(addressBook);
        }
    }
}

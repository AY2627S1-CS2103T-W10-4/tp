package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.testutil.EmployeeBuilder;

public class ReportingStorageTest {
    @TempDir
    public Path tempDir;

    private final Employee boss = new EmployeeBuilder().withId("BOSS").build();
    private final Employee worker = new EmployeeBuilder().withId("WORKER").build();

    @Test
    public void saveAndRead_relationshipsSurviveWithManagerLaterInFile() throws Exception {
        AddressBook data = new AddressBook();
        data.setEmployees(List.of(worker.withManager(boss.getId()), boss));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(tempDir.resolve("employees.json"));
        storage.saveAddressBook(data);
        assertEquals(data, new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void toModelType_legacyEmployeeHasNoManager() throws Exception {
        JsonAdaptedEmployee legacy = new JsonAdaptedEmployee(worker.getId().value, worker.getName().fullName,
                worker.getPhone().value, worker.getEmail().value, worker.getDepartment().value, worker.getRole().value);
        assertTrue(legacy.toModelType().getManagerId().isEmpty());
    }

    @Test
    public void toModelType_rejectsInvalidManagerId() {
        JsonAdaptedEmployee invalid = new JsonAdaptedEmployee(worker.getId().value, worker.getName().fullName,
                worker.getPhone().value, worker.getEmail().value, worker.getDepartment().value,
                worker.getRole().value, "bad id");
        assertThrows(IllegalValueException.class, invalid::toModelType);
    }

    @Test
    public void toModelType_rejectsMissingManagerSelfReportingAndCycles() {
        assertInvalid(List.of(worker.withManager(new EmployeeId("MISSING"))));
        assertInvalid(List.of(worker.withManager(new EmployeeId("worker"))));
        assertInvalid(List.of(worker.withManager(boss.getId()), boss.withManager(worker.getId())));
    }

    @Test
    public void setEmployees_invalidRelationshipsDoNotReplaceExistingRoster() {
        AddressBook data = new AddressBook();
        data.addEmployee(boss);
        assertThrows(IllegalArgumentException.class, () ->
                data.setEmployees(List.of(worker.withManager(new EmployeeId("MISSING")))));
        assertEquals(List.of(boss), data.getEmployeeList());
    }

    @Test
    public void employeeEquality_includesManagerButIdentityDoesNot() {
        Employee assigned = worker.withManager(boss.getId());
        assertTrue(worker.isSameEmployee(assigned));
        assertFalse(worker.equals(assigned));
        assertEquals(assigned, worker.withManager(boss.getId()));
        assertEquals(assigned.hashCode(), worker.withManager(boss.getId()).hashCode());
    }

    private void assertInvalid(List<Employee> employees) {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                employees.stream().map(JsonAdaptedEmployee::new).toList());
        assertThrows(IllegalValueException.class, data::toModelType);
    }
}

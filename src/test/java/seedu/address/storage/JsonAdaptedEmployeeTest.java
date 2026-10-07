package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedEmployee.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalEmployees.BENSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.employee.Department;
import seedu.address.model.employee.Email;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.Name;
import seedu.address.model.employee.Phone;
import seedu.address.model.employee.Role;

public class JsonAdaptedEmployeeTest {
    private static final String INVALID_ID = "E 01";
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_DEPARTMENT = " ";
    private static final String INVALID_ROLE = " ";

    private static final String VALID_ID = BENSON.getId().toString();
    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_DEPARTMENT = BENSON.getDepartment().toString();
    private static final String VALID_ROLE = BENSON.getRole().toString();

    private static JsonAdaptedEmployee build(String id, String name, String phone, String email, String department,
            String role) {
        return new JsonAdaptedEmployee(id, name, phone, email, department, role);
    }

    @Test
    public void toModelType_validEmployeeDetails_returnsEmployee() throws Exception {
        JsonAdaptedEmployee employee = new JsonAdaptedEmployee(BENSON);
        assertEquals(BENSON, employee.toModelType());
    }

    @Test
    public void toModelType_invalidId_throwsIllegalValueException() {
        JsonAdaptedEmployee employee =
                build(INVALID_ID, VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_DEPARTMENT, VALID_ROLE);
        assertThrows(IllegalValueException.class, EmployeeId.MESSAGE_CONSTRAINTS, employee::toModelType);
    }

    @Test
    public void toModelType_nullId_throwsIllegalValueException() {
        JsonAdaptedEmployee employee =
                build(null, VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_DEPARTMENT, VALID_ROLE);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, EmployeeId.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, employee::toModelType);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedEmployee employee =
                build(VALID_ID, INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_DEPARTMENT, VALID_ROLE);
        assertThrows(IllegalValueException.class, Name.MESSAGE_CONSTRAINTS, employee::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedEmployee employee =
                build(VALID_ID, null, VALID_PHONE, VALID_EMAIL, VALID_DEPARTMENT, VALID_ROLE);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, employee::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedEmployee employee =
                build(VALID_ID, VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_DEPARTMENT, VALID_ROLE);
        assertThrows(IllegalValueException.class, Phone.MESSAGE_CONSTRAINTS, employee::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedEmployee employee =
                build(VALID_ID, VALID_NAME, null, VALID_EMAIL, VALID_DEPARTMENT, VALID_ROLE);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, employee::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedEmployee employee =
                build(VALID_ID, VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_DEPARTMENT, VALID_ROLE);
        assertThrows(IllegalValueException.class, Email.MESSAGE_CONSTRAINTS, employee::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedEmployee employee =
                build(VALID_ID, VALID_NAME, VALID_PHONE, null, VALID_DEPARTMENT, VALID_ROLE);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, employee::toModelType);
    }

    @Test
    public void toModelType_invalidDepartment_throwsIllegalValueException() {
        JsonAdaptedEmployee employee =
                build(VALID_ID, VALID_NAME, VALID_PHONE, VALID_EMAIL, INVALID_DEPARTMENT, VALID_ROLE);
        assertThrows(IllegalValueException.class, Department.MESSAGE_CONSTRAINTS, employee::toModelType);
    }

    @Test
    public void toModelType_nullDepartment_throwsIllegalValueException() {
        JsonAdaptedEmployee employee =
                build(VALID_ID, VALID_NAME, VALID_PHONE, VALID_EMAIL, null, VALID_ROLE);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Department.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, employee::toModelType);
    }

    @Test
    public void toModelType_invalidRole_throwsIllegalValueException() {
        JsonAdaptedEmployee employee =
                build(VALID_ID, VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_DEPARTMENT, INVALID_ROLE);
        assertThrows(IllegalValueException.class, Role.MESSAGE_CONSTRAINTS, employee::toModelType);
    }

    @Test
    public void toModelType_nullRole_throwsIllegalValueException() {
        JsonAdaptedEmployee employee =
                build(VALID_ID, VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_DEPARTMENT, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Role.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, employee::toModelType);
    }

}

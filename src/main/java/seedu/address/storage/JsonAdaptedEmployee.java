package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.employee.Department;
import seedu.address.model.employee.Email;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.Name;
import seedu.address.model.employee.Phone;
import seedu.address.model.employee.Role;

/**
 * Jackson-friendly version of {@link Employee}.
 */
class JsonAdaptedEmployee {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Employee's %s field is missing!";

    private final String id;
    private final String name;
    private final String phone;
    private final String email;
    private final String department;
    private final String role;
    private final Boolean archived;
    private final String managerId;

    /**
     * Constructs a {@code JsonAdaptedEmployee} with the given employee details.
     */
    public JsonAdaptedEmployee(String id, String name, String phone, String email, String department, String role,
            Boolean archived) {
        this(id, name, phone, email, department, role, archived, null);
    }

    /** Creates an adapter with an optional manager for existing callers. */
    public JsonAdaptedEmployee(String id, String name, String phone, String email, String department, String role,
            String managerId) {
        this(id, name, phone, email, department, role, false, managerId);
    }

    @JsonCreator
    public JsonAdaptedEmployee(@JsonProperty("id") String id, @JsonProperty("name") String name,
            @JsonProperty("phone") String phone, @JsonProperty("email") String email,
            @JsonProperty("department") String department, @JsonProperty("role") String role,
            @JsonProperty("archived") Boolean archived, @JsonProperty("managerId") String managerId) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.department = department;
        this.role = role;
        this.archived = archived;
        this.managerId = managerId;
    }

    /** Constructs an active employee adapter for existing callers and tests. */
    public JsonAdaptedEmployee(String id, String name, String phone, String email, String department, String role) {
        this(id, name, phone, email, department, role, false);
    }

    /**
     * Converts a given {@code Employee} into this class for Jackson use.
     */
    public JsonAdaptedEmployee(Employee source) {
        id = source.getId().value;
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        department = source.getDepartment().value;
        role = source.getRole().value;
        archived = source.isArchived();
        managerId = source.getManagerId().map(employeeId -> employeeId.value).orElse(null);
    }

    /**
     * Converts this Jackson-friendly adapted employee object into the model's {@code Employee} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted employee.
     */
    public Employee toModelType() throws IllegalValueException {
        if (id == null) {
            throw new IllegalValueException(
                    String.format(MISSING_FIELD_MESSAGE_FORMAT, EmployeeId.class.getSimpleName()));
        }
        if (!EmployeeId.isValidEmployeeId(id)) {
            throw new IllegalValueException(EmployeeId.MESSAGE_CONSTRAINTS);
        }
        final EmployeeId modelId = new EmployeeId(id);

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        if (department == null) {
            throw new IllegalValueException(
                    String.format(MISSING_FIELD_MESSAGE_FORMAT, Department.class.getSimpleName()));
        }
        String normalisedDepartment = Department.normalise(department);
        if (!Department.isValidDepartment(normalisedDepartment)) {
            throw new IllegalValueException(Department.MESSAGE_CONSTRAINTS);
        }
        final Department modelDepartment = new Department(normalisedDepartment);

        if (role == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Role.class.getSimpleName()));
        }
        String normalisedRole = Role.normalise(role);
        if (!Role.isValidRole(normalisedRole)) {
            throw new IllegalValueException(Role.MESSAGE_CONSTRAINTS);
        }
        final Role modelRole = new Role(normalisedRole);

        if (managerId != null && !EmployeeId.isValidEmployeeId(managerId)) {
            throw new IllegalValueException(EmployeeId.MESSAGE_CONSTRAINTS);
        }
        return new Employee(modelId, modelName, modelPhone, modelEmail, modelDepartment, modelRole,
                archived != null && archived, managerId == null ? null : new EmployeeId(managerId));
    }

}

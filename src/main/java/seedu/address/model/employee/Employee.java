package seedu.address.model.employee;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents an Employee in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Employee {

    // Identity fields
    private final EmployeeId id;

    // Data fields
    private final Name name;
    private final Phone phone;
    private final Email email;
    private final Department department;
    private final Role role;
    private final EmployeeId managerId;

    /**
     * Every field must be present and not null.
     */
    public Employee(EmployeeId id, Name name, Phone phone, Email email, Department department, Role role) {
        this(id, name, phone, email, department, role, null);
    }

    /** Creates an employee with an optional direct manager (null means unassigned). */
    public Employee(EmployeeId id, Name name, Phone phone, Email email, Department department, Role role,
            EmployeeId managerId) {
        requireAllNonNull(id, name, phone, email, department, role);
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.department = department;
        this.role = role;
        this.managerId = managerId;
    }

    public Optional<EmployeeId> getManagerId() {
        return Optional.ofNullable(managerId);
    }

    /** Returns a copy with the given manager, or no manager when null. */
    public Employee withManager(EmployeeId newManagerId) {
        return new Employee(id, name, phone, email, department, role, newManagerId);
    }

    public EmployeeId getId() {
        return id;
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Department getDepartment() {
        return department;
    }

    public Role getRole() {
        return role;
    }

    /**
     * Returns true if both employees have the same employee ID.
     * This defines a weaker notion of equality between two employees.
     */
    public boolean isSameEmployee(Employee otherEmployee) {
        if (otherEmployee == this) {
            return true;
        }

        return otherEmployee != null
                && otherEmployee.getId().equals(getId());
    }

    /**
     * Returns true if both employees have the same identity and data fields.
     * This defines a stronger notion of equality between two employees.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Employee otherEmployee)) {
            return false;
        }

        return id.equals(otherEmployee.id)
                && name.equals(otherEmployee.name)
                && phone.equals(otherEmployee.phone)
                && email.equals(otherEmployee.email)
                && department.equals(otherEmployee.department)
                && role.equals(otherEmployee.role)
                && Objects.equals(managerId, otherEmployee.managerId);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(id, name, phone, email, department, role, managerId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("id", id)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("department", department)
                .add("role", role)
                .add("managerId", managerId)
                .toString();
    }

}

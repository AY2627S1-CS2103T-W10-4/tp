package seedu.address.model.employee;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

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
    private final boolean archived;

    /**
     * Every field must be present and not null.
     */
    public Employee(EmployeeId id, Name name, Phone phone, Email email, Department department, Role role) {
        this(id, name, phone, email, department, role, false);
    }

    /** Creates an employee with an explicit archive status. */
    public Employee(EmployeeId id, Name name, Phone phone, Email email, Department department, Role role,
            boolean archived) {
        requireAllNonNull(id, name, phone, email, department, role);
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.department = department;
        this.role = role;
        this.archived = archived;
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

    public boolean isArchived() {
        return archived;
    }

    /** Returns an otherwise identical employee with the requested archive status. */
    public Employee withArchived(boolean archived) {
        return new Employee(id, name, phone, email, department, role, archived);
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
                && archived == otherEmployee.archived;
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(id, name, phone, email, department, role, archived);
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
                .add("archived", archived)
                .toString();
    }

}

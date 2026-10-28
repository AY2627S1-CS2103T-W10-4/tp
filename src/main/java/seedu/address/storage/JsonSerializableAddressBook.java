package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.employee.Employee;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_EMPLOYEE = "Employees list contains duplicate employee(s).";
    public static final String MESSAGE_MISSING_EMPLOYEES = "Employees field is missing or null.";

    private final List<JsonAdaptedEmployee> employees = new ArrayList<>();
    @JsonIgnore
    private final boolean hasEmployees;

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given employees.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("employees") List<JsonAdaptedEmployee> employees) {
        hasEmployees = employees != null;
        if (employees != null) {
            this.employees.addAll(employees);
        }
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        hasEmployees = true;
        employees.addAll(source.getEmployeeList().stream().map(JsonAdaptedEmployee::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        if (!hasEmployees) {
            throw new IllegalValueException(MESSAGE_MISSING_EMPLOYEES);
        }
        AddressBook addressBook = new AddressBook();
        List<Employee> converted = new ArrayList<>();
        for (JsonAdaptedEmployee jsonAdaptedEmployee : employees) {
            if (jsonAdaptedEmployee == null) {
                throw new IllegalValueException("Employees list contains a null employee.");
            }
            Employee employee = jsonAdaptedEmployee.toModelType();
            if (converted.stream().anyMatch(employee::isSameEmployee)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_EMPLOYEE);
            }
            converted.add(employee);
        }
        try {
            addressBook.setEmployees(converted);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
        return addressBook;
    }

}

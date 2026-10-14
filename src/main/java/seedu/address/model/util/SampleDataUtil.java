package seedu.address.model.util;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.employee.Department;
import seedu.address.model.employee.Email;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.EmployeeId;
import seedu.address.model.employee.Name;
import seedu.address.model.employee.Phone;
import seedu.address.model.employee.Role;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    public static Employee[] getSampleEmployees() {
        return new Employee[] {
            new Employee(new EmployeeId("E0001"), new Name("Alex Yeoh"), new Phone("87438807"),
                new Email("alexyeoh@example.com"), new Department("Engineering"), new Role("Software Engineer")),
            new Employee(new EmployeeId("E0002"), new Name("Bernice Yu"), new Phone("99272758"),
                new Email("berniceyu@example.com"), new Department("Human Resources"), new Role("HR Executive")),
            new Employee(new EmployeeId("E0003"), new Name("Charlotte Oliveiro"), new Phone("93210283"),
                new Email("charlotte@example.com"), new Department("Finance"), new Role("Accountant")),
            new Employee(new EmployeeId("E0004"), new Name("David Li"), new Phone("91031282"),
                new Email("lidavid@example.com"), new Department("Engineering"), new Role("Engineering Manager")),
            new Employee(new EmployeeId("E0005"), new Name("Irfan Ibrahim"), new Phone("92492021"),
                new Email("irfan@example.com"), new Department("Operations"), new Role("Operations Analyst")),
            new Employee(new EmployeeId("E0006"), new Name("Roy Balakrishnan"), new Phone("92624417"),
                new Email("royb@example.com"), new Department("Sales"), new Role("Account Manager"))
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Employee sampleEmployee : getSampleEmployees()) {
            sampleAb.addEmployee(sampleEmployee);
        }
        return sampleAb;
    }

}

package seedu.address.testutil;

import static seedu.address.logic.commands.CommandTestUtil.VALID_DEPARTMENT_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_DEPARTMENT_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ID_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ID_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ROLE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ROLE_BOB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import seedu.address.model.AddressBook;
import seedu.address.model.employee.Employee;

/**
 * A utility class containing a list of {@code Employee} objects to be used in tests.
 */
public class TypicalEmployees {

    public static final Employee ALICE = new EmployeeBuilder().withId("E0001").withName("Alice Pauline")
            .withPhone("94351253").withEmail("alice@example.com")
            .withDepartment("Engineering").withRole("Software Engineer").build();
    public static final Employee BENSON = new EmployeeBuilder().withId("E0002").withName("Benson Meier")
            .withPhone("98765432").withEmail("johnd@example.com")
            .withDepartment("Human Resources").withRole("HR Executive").build();
    public static final Employee CARL = new EmployeeBuilder().withId("E0003").withName("Carl Kurz")
            .withPhone("95352563").withEmail("heinz@example.com")
            .withDepartment("Finance").withRole("Accountant").build();
    public static final Employee DANIEL = new EmployeeBuilder().withId("E0004").withName("Daniel Meier")
            .withPhone("87652533").withEmail("cornelia@example.com")
            .withDepartment("Sales").withRole("Account Manager").build();
    public static final Employee ELLE = new EmployeeBuilder().withId("E0005").withName("Elle Meyer")
            .withPhone("9482224").withEmail("werner@example.com")
            .withDepartment("Operations").withRole("Operations Analyst").build();
    public static final Employee FIONA = new EmployeeBuilder().withId("E0006").withName("Fiona Kunz")
            .withPhone("9482427").withEmail("lydia@example.com")
            .withDepartment("Marketing").withRole("Marketing Executive").build();
    public static final Employee GEORGE = new EmployeeBuilder().withId("E0007").withName("George Best")
            .withPhone("9482442").withEmail("anna@example.com")
            .withDepartment("Legal").withRole("Legal Counsel").build();

    // Manually added
    public static final Employee HOON = new EmployeeBuilder().withId("E0008").withName("Hoon Meier")
            .withPhone("8482424").withEmail("stefan@example.com")
            .withDepartment("Engineering").withRole("Engineering Manager").build();
    public static final Employee IDA = new EmployeeBuilder().withId("E0009").withName("Ida Mueller")
            .withPhone("8482131").withEmail("hans@example.com")
            .withDepartment("Finance").withRole("Financial Analyst").build();

    // Manually added - Employee's details found in {@code CommandTestUtil}
    public static final Employee AMY = new EmployeeBuilder().withId(VALID_ID_AMY).withName(VALID_NAME_AMY)
            .withPhone(VALID_PHONE_AMY).withEmail(VALID_EMAIL_AMY)
            .withDepartment(VALID_DEPARTMENT_AMY).withRole(VALID_ROLE_AMY).build();
    public static final Employee BOB = new EmployeeBuilder().withId(VALID_ID_BOB).withName(VALID_NAME_BOB)
            .withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
            .withDepartment(VALID_DEPARTMENT_BOB).withRole(VALID_ROLE_BOB).build();

    public static final String KEYWORD_MATCHING_MEIER = "Meier"; // A keyword that matches MEIER

    private TypicalEmployees() {} // prevents instantiation

    /**
     * Returns an {@code AddressBook} with all the typical employees.
     */
    public static AddressBook getTypicalAddressBook() {
        AddressBook ab = new AddressBook();
        for (Employee employee : getTypicalEmployees()) {
            ab.addEmployee(employee);
        }
        return ab;
    }

    public static List<Employee> getTypicalEmployees() {
        return new ArrayList<>(Arrays.asList(ALICE, BENSON, CARL, DANIEL, ELLE, FIONA, GEORGE));
    }
}

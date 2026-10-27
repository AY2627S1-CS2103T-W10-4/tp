package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.getEmployeesListedOverview;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalEmployees.CARL;
import static seedu.address.testutil.TypicalEmployees.ELLE;
import static seedu.address.testutil.TypicalEmployees.FIONA;
import static seedu.address.testutil.TypicalEmployees.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.FilterCommandParser;
import seedu.address.logic.parser.FindCommandParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.employee.Employee;
import seedu.address.model.employee.NameContainsKeywordsPredicate;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.EmployeeBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code FindCommand}.
 */
public class FindCommandTest {
    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void equals() {
        NameContainsKeywordsPredicate firstPredicate =
                new NameContainsKeywordsPredicate(List.of("first"));
        NameContainsKeywordsPredicate secondPredicate =
                new NameContainsKeywordsPredicate(List.of("second"));

        FindCommand findFirstCommand = new FindCommand(firstPredicate);
        FindCommand findSecondCommand = new FindCommand(secondPredicate);

        // same object -> returns true
        assertTrue(findFirstCommand.equals(findFirstCommand));

        // same values -> returns true
        FindCommand findFirstCommandCopy = new FindCommand(firstPredicate);
        assertTrue(findFirstCommand.equals(findFirstCommandCopy));

        // different types -> returns false
        assertFalse(findFirstCommand.equals(1));

        // null -> returns false
        assertFalse(findFirstCommand.equals(null));

        // different employee -> returns false
        assertFalse(findFirstCommand.equals(findSecondCommand));
    }

    @Test
    public void execute_zeroKeywords_noEmployeeFound() {
        String expectedMessage = getEmployeesListedOverview(0);
        NameContainsKeywordsPredicate predicate = preparePredicate(" ");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredEmployeeList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_oneKeyword_oneEmployeeFound() {
        String expectedMessage = "1 employee listed!";
        NameContainsKeywordsPredicate predicate = preparePredicate("Fiona");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredEmployeeList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(FIONA), model.getFilteredEmployeeList());
    }

    @Test
    public void execute_multipleKeywords_multipleEmployeesFound() {
        String expectedMessage = getEmployeesListedOverview(3);
        NameContainsKeywordsPredicate predicate = preparePredicate("Kurz Elle Kunz");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredEmployeeList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(CARL, ELLE, FIONA), model.getFilteredEmployeeList());
    }

    @Test
    public void toStringMethod() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("keyword"));
        FindCommand findCommand = new FindCommand(predicate);
        String expected = FindCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, findCommand.toString());
    }

    /**
     * Parses {@code userInput} into a {@code NameContainsKeywordsPredicate}.
     */
    private NameContainsKeywordsPredicate preparePredicate(String userInput) {
        return new NameContainsKeywordsPredicate(List.of(userInput.split("\\s+")));
    }

    /** Runs {@code find} with {@code arguments} on a roster of names with punctuation and accents. */
    private List<Employee> findIn(Model roster, String arguments) throws Exception {
        new FindCommandParser().parse(arguments).execute(roster);
        return roster.getFilteredEmployeeList();
    }

    @Test
    public void execute_namesWithPunctuationAndAccents_foundByTheirWords() throws Exception {
        Employee tan = new EmployeeBuilder().withId("T1").withName("Tan, Ah Kow").build();
        Employee mary = new EmployeeBuilder().withId("T2").withName("Mary-Ann O'Brien").build();
        Employee emile = new EmployeeBuilder().withId("T3").withName("\u00C9mile Roux").build();
        Model roster = new ModelManager(
                new AddressBookBuilder().withEmployee(tan).withEmployee(mary).withEmployee(emile).build(),
                new UserPrefs());

        assertEquals(List.of(tan), findIn(roster, "Tan"));
        assertEquals(List.of(mary), findIn(roster, "Mary"));
        assertEquals(List.of(mary), findIn(roster, "Ann"));
        assertEquals(List.of(emile), findIn(roster, "emile"));
        assertEquals(List.of(emile), findIn(roster, "E\u0301mile"));
        // keywords separated by commas are two keywords, so both names are found
        assertEquals(List.of(tan, mary), findIn(roster, "Ah, Mary-Ann"));
        // only part of a word does not match
        assertEquals(List.of(), findIn(roster, "Ta"));
    }

    @Test
    public void execute_findAndFilterByName_findTheSameEmployees() throws Exception {
        Employee tan = new EmployeeBuilder().withId("T1").withName("Tan, Ah Kow").build();
        Employee mary = new EmployeeBuilder().withId("T2").withName("Mary-Ann O'Brien").build();
        Employee emile = new EmployeeBuilder().withId("T3").withName("\u00C9mile Roux").build();
        Model roster = new ModelManager(
                new AddressBookBuilder().withEmployee(tan).withEmployee(mary).withEmployee(emile).build(),
                new UserPrefs());

        for (String name : List.of("Tan", "Mary", "Ann", "emile")) {
            List<Employee> found = List.copyOf(findIn(roster, name));
            new FilterCommandParser().parse(" n/" + name).execute(roster);
            assertEquals(found, roster.getFilteredEmployeeList(), name);
        }
    }
}

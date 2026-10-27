package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.NaturalOrderComparator;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.employee.Employee;

/**
 * Sorts the displayed employees by one field, in ascending or descending order. Letter case and accents are ignored.
 * Only the displayed order changes; the stored data is not changed. {@code list} restores the saved order.
 */
public class SortCommand extends Command {

    public static final String COMMAND_WORD = "sort";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Sorts the displayed employees by the given field: "
            + "id, name, department or role. The order is ascending (A to Z, ignoring case) unless desc is given. "
            + "Use list to show the employees in their saved order again.\n"
            + "Parameters: FIELD [asc|desc]\n"
            + "Example: " + COMMAND_WORD + " department desc";

    public static final String MESSAGE_SUCCESS = "Sorted by %1$s in %2$s order.";
    public static final String MESSAGE_UNKNOWN_FIELD = "Unknown sort field: %1$s. The field must be one of: %2$s.";
    public static final String MESSAGE_UNKNOWN_ORDER = "Unknown sort order: %1$s. The order must be one of: %2$s.";

    private static final Logger logger = LogsCenter.getLogger(SortCommand.class);

    private final SortField field;
    private final SortOrder order;

    /**
     * Creates a command that sorts the displayed employees by {@code field}, in ascending order.
     */
    public SortCommand(SortField field) {
        this(field, SortOrder.ASC);
    }

    /**
     * Creates a command that sorts the displayed employees by {@code field}, in the given {@code order}.
     */
    public SortCommand(SortField field, SortOrder order) {
        requireNonNull(field);
        requireNonNull(order);
        this.field = field;
        this.order = order;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        logger.fine("Sorting employees by " + field + " in " + order + " order");
        model.updateEmployeeSortOrder(field.getComparator(order));
        return new CommandResult(String.format(MESSAGE_SUCCESS, field, order));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof SortCommand otherSortCommand)) {
            return false;
        }

        return field == otherSortCommand.field && order == otherSortCommand.order;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, order);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("field", field)
                .add("order", order)
                .toString();
    }

    /**
     * The fields that employees can be sorted by.
     * Employees with an equal value are ordered by their employee ID, so the order is always the same.
     */
    public enum SortField {
        ID(employee -> employee.getId().value),
        NAME(employee -> employee.getName().fullName),
        DEPARTMENT(employee -> employee.getDepartment().value),
        ROLE(employee -> employee.getRole().value);

        /**
         * Orders values the way {@code sort} does: ignoring letter case and accents, and comparing a number inside
         * a value by its value.
         */
        public static final Comparator<String> VALUE_ORDER =
                Comparator.comparing(SortField::removeAccents, NaturalOrderComparator.INSTANCE);

        private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");

        private final Function<Employee, String> keyExtractor;

        SortField(Function<Employee, String> keyExtractor) {
            this.keyExtractor = keyExtractor;
        }

        /**
         * Returns a comparator that orders employees by this field in the given {@code order}.
         * Letter case and accents are ignored, so {@code Émile} is placed with the names that start with E.
         * A number inside a value is compared by its value, so {@code E2} comes before {@code E10}.
         * Employees with an equal value are always ordered by ID, from A to Z and from the smallest number up.
         */
        public Comparator<Employee> getComparator(SortOrder order) {
            requireNonNull(order);
            Comparator<Employee> byKey = Comparator.comparing(keyExtractor, VALUE_ORDER);
            Comparator<Employee> byId = Comparator.comparing(
                    employee -> employee.getId().value, NaturalOrderComparator.INSTANCE);
            return (order == SortOrder.DESC ? byKey.reversed() : byKey).thenComparing(byId);
        }

        /** Returns {@code text} without accents, for example {@code Émile} becomes {@code Emile}. */
        private static String removeAccents(String text) {
            return COMBINING_MARKS.matcher(Normalizer.normalize(text, Normalizer.Form.NFD)).replaceAll("");
        }

        /**
         * Returns the field named {@code text}, ignoring case, or an empty {@code Optional} if there is none.
         */
        public static Optional<SortField> fromString(String text) {
            requireNonNull(text);
            return Arrays.stream(values())
                    .filter(field -> field.toString().equalsIgnoreCase(text.trim()))
                    .findFirst();
        }

        /** Returns the names of all fields, for example in an error message. */
        public static String names() {
            return Arrays.stream(values()).map(SortField::toString).collect(Collectors.joining(", "));
        }

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * The direction in which employees are sorted. It is typed as {@code asc} or {@code desc}.
     */
    public enum SortOrder {
        ASC("asc", "ascending"),
        DESC("desc", "descending");

        private final String keyword;
        private final String description;

        SortOrder(String keyword, String description) {
            this.keyword = keyword;
            this.description = description;
        }

        /**
         * Returns the order whose keyword is {@code text}, ignoring case, or an empty {@code Optional} if none.
         */
        public static Optional<SortOrder> fromString(String text) {
            requireNonNull(text);
            return Arrays.stream(values())
                    .filter(order -> order.keyword.equalsIgnoreCase(text.trim()))
                    .findFirst();
        }

        /** Returns the keywords of all orders, for example in an error message. */
        public static String keywords() {
            return Arrays.stream(values()).map(order -> order.keyword).collect(Collectors.joining(", "));
        }

        @Override
        public String toString() {
            return description;
        }
    }
}

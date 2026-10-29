package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.Map;

/**
 * Chooses a restrained colour family for a department, so that the department label and avatar are consistent.
 * Departments are free text, so an unknown department gets a deterministic colour from a small muted palette.
 */
final class DepartmentColour {

    private static final String ENGINEERING = "#72a0c4";
    private static final String FINANCE = "#c09a52";
    private static final String OPERATIONS = "#bb745a";
    private static final String SALES = "#62a07b";
    private static final String HUMAN_RESOURCES = "#9a7ab2";
    private static final String PRODUCT = "#8879aa";
    private static final String DATA = "#579b9a";

    private static final Map<String, String> KNOWN = Map.ofEntries(
            Map.entry("engineering", ENGINEERING),
            Map.entry("finance", FINANCE),
            Map.entry("operations", OPERATIONS),
            Map.entry("sales", SALES),
            Map.entry("human resources", HUMAN_RESOURCES),
            Map.entry("hr", HUMAN_RESOURCES),
            Map.entry("product", PRODUCT),
            Map.entry("analytics", DATA),
            Map.entry("data", DATA),
            Map.entry("data analytics", DATA));

    private static final String[] PALETTE = {
        "#7694aa", "#a28a61", "#739884", "#917fa0", "#a1766a", "#679496"
    };

    private DepartmentColour() {}

    /**
     * Returns the foreground colour for {@code department}. Letter case and surrounding spaces do not matter.
     */
    static String of(String department) {
        requireNonNull(department);
        String key = department.trim().toLowerCase(Locale.ROOT);
        return KNOWN.getOrDefault(key, PALETTE[Math.floorMod(key.hashCode(), PALETTE.length)]);
    }

    /** Returns the subdued style used by a department label. */
    static String chipStyle(String department) {
        return styleFor(department, 0.22);
    }

    /** Returns the slightly stronger matching style used by an employee avatar. */
    static String avatarStyle(String department) {
        return styleFor(department, 0.34);
    }

    private static String styleFor(String department, double opacity) {
        String colour = of(department);
        int red = Integer.parseInt(colour.substring(1, 3), 16);
        int green = Integer.parseInt(colour.substring(3, 5), 16);
        int blue = Integer.parseInt(colour.substring(5, 7), 16);
        return "-fx-text-fill: " + colour + "; -fx-background-color: rgba(" + red + "," + green + "," + blue
                + "," + opacity + ");";
    }
}

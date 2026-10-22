package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.Map;

/**
 * Chooses the colour of a department, so that the same department always looks the same in the list.
 * Departments are free text, so a department without a fixed colour gets one from a small palette, chosen by its name.
 */
final class DepartmentColour {

    private static final Map<String, String> KNOWN = Map.of(
            "engineering", "#5b8def",
            "sales", "#35d49a",
            "finance", "#ffb454",
            "human resources", "#c792ea",
            "operations", "#ff8b6b",
            "marketing", "#ff6bb5",
            "legal", "#4dd0e1");

    private static final String[] PALETTE = {"#9ccc65", "#ffd54f", "#a1887f", "#90a4ae", "#f06292", "#7986cb"};

    private DepartmentColour() {}

    /**
     * Returns the colour of {@code department} as {@code #rrggbb}. Letter case and surrounding spaces do not matter.
     */
    static String of(String department) {
        requireNonNull(department);
        String key = department.trim().toLowerCase(Locale.ROOT);
        String known = KNOWN.get(key);
        return known != null ? known : PALETTE[Math.floorMod(key.hashCode(), PALETTE.length)];
    }

    /**
     * Returns the style of a chip for {@code department}: the colour as the text and a faint version of it behind.
     */
    static String chipStyle(String department) {
        String colour = of(department);
        int red = Integer.parseInt(colour.substring(1, 3), 16);
        int green = Integer.parseInt(colour.substring(3, 5), 16);
        int blue = Integer.parseInt(colour.substring(5, 7), 16);
        return "-fx-text-fill: " + colour + "; -fx-background-color: rgba(" + red + "," + green + "," + blue
                + ",0.22);";
    }
}

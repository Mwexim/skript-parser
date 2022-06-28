package io.github.syst3ms.skriptparser.util.color;

import java.util.Optional;
import java.util.stream.Stream;

public enum ConsoleColors {

    // Reset
    RESET("0", true),

    // Normal Colors
    BLACK("30", false),
    RED("31", false),
    GREEN("32", false, "dark_green"),
    ORANGE("33", false, "orange", "amber"),
    BLUE("34", false, "dark_blue"),
    PURPLE("35", false, "dark_pink"),
    CYAN("36", false, "dark_aqua"),
    WHITE("37", false, "dark_gray"),

    // Bright Colors
    BLACK_BRIGHT("90", false, "gray", "grey"),
    RED_BRIGHT("91", false, "pink"),
    GREEN_BRIGHT("92", false, "lime", "light_green"),
    ORANGE_BRIGHT("93", false, "yellow"),
    BLUE_BRIGHT("94", false, "light_blue"),
    PURPLE_BRIGHT("95", false, "light_purple"),
    CYAN_BRIGHT("96", false, "light_aqua", "aqua"),
    WHITE_BRIGHT("97", false),

    // Colors background
    BLACK_BACKGROUND(BLACK, "40"),
    RED_BACKGROUND(RED, "41"),
    GREEN_BACKGROUND(GREEN, "42"),
    ORANGE_BACKGROUND(ORANGE, "43"),
    BLUE_BACKGROUND(BLUE, "44"),
    PURPLE_BACKGROUND(PURPLE, "45"),
    CYAN_BACKGROUND(CYAN, "46"),
    WHITE_BACKGROUND(WHITE, "47"),

    // Styles
    BOLD("1", true, "b"),
    ITALIC("3", true, "o"),
    UNDERLINE("4", true, "n", "u"),
    STRICKTHROUGH("9", true, "m", "strike"),
    ;

    private static final String ANSI_DELIMITER = "\033[";

    private final String code;
    private final boolean style;
    private final String[] aliases;

    /**
     * Used for backgrounds colors only.
     * It will generate values according to it.
     * @param color
     */
    ConsoleColors(ConsoleColors color, String code) {
        this.code = code;
        this.style = false;
        this.aliases = Stream.of(color.getAliases())
                .map(alias -> alias + "_background")
                .toArray(String[]::new);
    }

    ConsoleColors(String code, boolean style, String... aliases) {
        this.code = code;
        this.style = style;
        this.aliases = aliases;
    }

    public static Optional<ConsoleColors> search(String input) {
        for (ConsoleColors color : values()) {
            if (color.match(input))
                return Optional.of(color);
        }
        return Optional.empty();
    }

    public String format() {
        return ANSI_DELIMITER + code + "m";
    }

    public boolean match(String other) {
        if (other.replace(" ", "_").equalsIgnoreCase(name()))
            return true;
        // Checking for aliases
        for (String alias : aliases) {
            if (other.replace(" ", "_").equalsIgnoreCase(alias))
                return true;
        }

        return false;
    }

    public String toString() {
        return format();
    }

    public String[] getAliases() {
        return aliases;
    }

    public String getCode() {
        return code;
    }

    public boolean isStyle() {
        return style;
    }
}
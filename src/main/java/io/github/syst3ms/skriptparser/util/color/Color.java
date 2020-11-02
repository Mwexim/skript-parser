package io.github.syst3ms.skriptparser.util.color;

import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Map.entry;

/**
 * Represents a color, written like #xxxxxx or with an RGB format.
 * @author Mwexim
 */
public class Color {
    public static final Pattern COLOR_PATTERN = Pattern.compile("#[0-9a-f]{6}");

    private static final Map<String, Color> COLOR_CONSTANTS = Map.ofEntries(
        entry("WHITE", of(0xffffff)),
        entry("SILVER", of(0xc0c0c0)),
        entry("GRAY", of(0x808080)),
        entry("BLACK", of(0x000000)),
        entry("RED", of(0xff0000)),
        entry("DARK_RED", of(0x800000)),
        entry("YELLOW", of(0xffff00)),
        entry("DARK_YELLOW", of(0x808000)),
        entry("LIME", of(0x00ff00)),
        entry("GREEN", of(0x008000)),
        entry("AQUA", of(0x0000ff)),
        entry("CYAN", of(0x008080)),
        entry("BLUE", of(0x0000ff)),
        entry("DARK_BLUE", of(0x000080)),
        entry("PINK", of(0xff00ff)),
        entry("PURPLE", of(0x800080))
    );

    private final int red, green, blue;

    private Color(int r, int g, int b) {
        if (0 <= r && r < 256
                && 0 <= g && g < 256
                && 0 <= b && b < 256) {
            red = r;
            green = g;
            blue = b;
        } else {
            throw new IllegalArgumentException(
                    String.format("Red, green and blue values are not in range 0-255: found %s,%s,%s", r, g, b)
            );
        }
    }

    /**
     * The Color instance of given hex value.
     * @param r the red value
     * @param g the green value
     * @param b the blue value
     * @return a new Color instance
     */
    public static Color of(int r, int g, int b) {
        return new Color(r, g, b);
    }

    /**
     * The Color instance of given hex value.
     * @param hex the hexadecimal value
     * @return a new Color instance
     */
    public static Color of(int hex) {
        int r = (hex & 0xFF0000) >> 16;
        int g = (hex & 0xFF00) >> 8;
        int b = (hex & 0xFF);
        return new Color(r, g, b);
    }

    /**
     * The Color instance of given hex value.
     * @param hex the hexadecimal value in String form (like #xxxxxx)
     * @return a new Color instance
     */
    public static Color of(String hex) {
        return of(Integer.parseInt(hex.substring(1), 16));
    }

    /**
     * The Color instance of given literal.
     * @param literal the literal value, like 'yellow' or 'green'.
     * @return a new Color instance
     */
    public static Color ofLiteral(String literal) {
        String actual = literal.replaceAll(" ", "_").toUpperCase();
        return COLOR_CONSTANTS.get(actual);
    }

    /**
     * @return the red value of this color
     */
    public int getRed() {
        return red;
    }

    /**
     * @return the green value of this color
     */
    public int getGreen() {
        return green;
    }

    /**
     * @return the blue value of this color
     */
    public int getBlue() {
        return blue;
    }

    /**
     * @return the hex value of this color, without the '#' appended
     */
    public String getHex() {
        return String.format("%02x%02x%02x", red, green, blue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Color color = (Color) o;
        return red == color.red && green == color.green && blue == color.blue;
    }

    @Override
    public String toString() {
        return '#' + getHex();
    }
}
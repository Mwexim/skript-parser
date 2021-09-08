package io.github.syst3ms.skriptparser.util.color;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Map.entry;

/**
 * Represents a color, written like #xxxxxx or with an RGB format.
 * @author Mwexim
 */
public class Color {
    public static final Pattern COLOR_PATTERN = Pattern.compile("([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})");
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 255;

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
        entry("AQUA", of(0x00ffff)),
        entry("CYAN", of(0x008080)),
        entry("BLUE", of(0x0000ff)),
        entry("DARK_BLUE", of(0x000080)),
        entry("PINK", of(0xff00ff)),
        entry("PURPLE", of(0x800080))
    );

    private final int red, green, blue, alpha;

    private Color(int r, int g, int b, int a) {
        red = r;
        green = g;
        blue = b;
        alpha = a;
    }

    /**
     * The Color instance of given hex value.
     * @param r the red value
     * @param g the green value
     * @param b the blue value
     * @return a new Color instance
     */
    public static Optional<Color> of(int r, int g, int b) {
        return of(r, g, b, MAX_VALUE);
    }

    /**
     * The Color instance of given hex value.
     * @param r the red value
     * @param g the green value
     * @param b the blue value
     * @param a the alpha value
     * @return a new Color instance
     */
    public static Optional<Color> of(int r, int g, int b, int a) {
        if (0 <= r && r < 256
                && 0 <= g && g < 256
                && 0 <= b && b < 256
                && 0 <= a && a < 256) {
            return Optional.of(new Color(r, g, b, a));
        }
        return Optional.empty();
    }

    /**
     * The Color instance of given hex value.
     * @param hex the hexadecimal value
     * @return a new Color instance
     */
    public static Color of(int hex) {
        return of(hex, false);
    }

    /**
     * The Color instance of given hex value.
     * @param hex the hexadecimal value
     * @param isAlpha whether or not the alpha parameter is present
     * @return a new Color instance
     */
    public static Color of(long hex, boolean isAlpha) {
        int r, g, b, a;
        if (isAlpha) {
            r = (int) ((hex & 0xFF000000) >> 24);
            g = (int) ((hex & 0xFF0000) >> 16);
            b = (int) ((hex & 0xFF00) >> 8);
            a = (int) (hex & 0xFF);
        } else {
            r = (int) ((hex & 0xFF0000) >> 16);
            g = (int) ((hex & 0xFF00) >> 8);
            b = (int) (hex & 0xFF);
            a = MAX_VALUE;
        }
        return Color.of(r, g, b, a).orElseThrow(AssertionError::new);
    }

    /**
     * The Color instance of given hex value.
     * @param hex the hexadecimal value in String form
     * @return a new Color instance
     * @see #COLOR_PATTERN
     */
    public static Optional<Color> ofHex(String hex) {
        if (!hex.matches(COLOR_PATTERN.pattern()))
            return Optional.empty();

        switch (hex.length()) {
            case 3:
                var builder = new StringBuilder();
                for (char c : hex.toCharArray()) {
                    builder.append(c).append(c);
                }
                return Optional.of(of(Integer.parseInt(builder.toString(), 16)));
            case 6:
                return Optional.of(of(Integer.parseInt(hex, 16)));
            case 8:
                return Optional.of(of(Long.parseLong(hex, 16), true));
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * The Color instance of given literal.
     * @param literal the literal value, like 'yellow' or 'green'.
     * @return a new Color instance
     */
    public static Optional<Color> ofLiteral(String literal) {
        String actual = literal.replaceAll(" ", "_").toUpperCase();
        if (COLOR_CONSTANTS.containsKey(actual)) {
            return Optional.of(COLOR_CONSTANTS.get(actual));
        }
        return Optional.empty();
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
     * @return the transparency, ranging from 0 to 1, of this color
     */
    public int getAlpha() {
        return alpha;
    }

    /**
     * @return the hex value of this color, without the '#' appended
     */
    public String getHex() {
        return alpha == 255
                ? String.format("%02x%02x%02x", red, green, blue)
                : String.format("%02x%02x%02x%02x", red, green, blue, alpha);
    }

    public java.awt.Color toJavaColor() {
        return new java.awt.Color(red, green, blue, alpha);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Color color = (Color) o;
        return red == color.red && green == color.green && blue == color.blue && alpha == color.alpha;
    }

    @Override
    public String toString() {
        return '#' + getHex();
    }
}
package io.github.syst3ms.skriptparser.util.color;

import java.util.regex.Pattern;

/**
 * Represents a color, written like #xxxxxx or with an RGB format.
 * @author Mwexim
 */
public class Color {
    public static Pattern COLOR_PATTERN = Pattern.compile("[#&][0-9a-f]{6}");

    public static Color WHITE = of(0xffffff);
    public static Color SILVER = of(0xc0c0c0);
    public static Color GRAY = of(0x808080);
    public static Color BLACK = of(0x000000);
    public static Color RED = of(0xff0000);
    public static Color DARK_RED = of(0x800000);
    public static Color YELLOW = of(0xffff00);
    public static Color DARK_YELLOW = of(0x808000);
    public static Color LIME = of(0x00ff00);
    public static Color GREEN = of(0x008000);
    public static Color AQUA = of(0x0000ff);
    public static Color CYAN = of(0x008080);
    public static Color BLUE = of(0x0000ff);
    public static Color DARK_BLUE = of(0x000080);
    public static Color PINK = of(0xff00ff);
    public static Color PURPLE = of(0x800080);

    private final byte red, green, blue;

    private Color(byte r, byte g, byte b) {
        red = r;
        green = g;
        blue = b;
    }

    /**
     * The Color instance of given hex value.
     * @param r the red value
     * @param g the green value
     * @param b the blue value
     * @return a new Color instance
     */
    public static Color of(byte r, byte g, byte b) {
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
        return new Color((byte) r, (byte) g, (byte) b);
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
        switch (actual) {
            case "WHITE":
                return WHITE;
            case "SILVER":
                return SILVER;
            case "GRAY":
                return GRAY;
            case "BLACK":
                return BLACK;
            case "RED":
                return RED;
            case "DARK_RED":
            case "MAROON":
                return DARK_RED;
            case "YELLOW":
                return YELLOW;
            case "DARK_YELLOW":
            case "OLIVE":
                return DARK_YELLOW;
            case "LIME":
                return LIME;
            case "GREEN":
                return GREEN;
            case "LIGHT_BLUE":
            case "AQUA":
                return AQUA;
            case "CYAN":
            case "TEAL":
                return CYAN;
            case "BLUE":
                return BLUE;
            case "DARK_BLUE":
            case "NAVY":
                return DARK_BLUE;
            case "PINK":
            case "FUCHSIA":
                return PINK;
            case "PURPLE":
                return PURPLE;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * @return the red value of this color
     */
    public byte getRed() {
        return red;
    }

    /**
     * @return the green value of this color
     */
    public byte getGreen() {
        return green;
    }

    /**
     * @return the blue value of this color
     */
    public byte getBlue() {
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
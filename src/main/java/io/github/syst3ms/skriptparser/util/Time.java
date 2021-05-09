package io.github.syst3ms.skriptparser.util;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a time, written as HH:mm:ss.SSS.
 * @author Mwexim
 */
public class Time implements Comparable<Time> {
    // TODO make a config for this
    public final static String TIME_FORMAT = "HH:mm:ss.SSS";
    public final static Locale TIME_LOCALE = Locale.US;

    private final static Pattern DEFAULT_TIME_PATTERN = Pattern.compile("(\\d?\\d)h(\\d\\d)?");
    private final static Pattern BRITISH_TIME_PATTERN = Pattern.compile("(\\d?\\d)(:(\\d\\d))? ?(am|pm)", Pattern.CASE_INSENSITIVE);
    private final static Pattern DETAILED_TIME_PATTERN = Pattern.compile("(\\d?\\d):(\\d\\d)(:((\\d\\d)(\\.(\\d\\d\\d))?))?");

    /**
     * Returns the latest possible time, being 23:59:59.999.
     */
    public final static Time LATEST = Time.of(23, 59, 59, 999);
    /**
     * Represents midnight, being 00:00:00.000.
     */
    public final static Time MIDNIGHT = Time.of(0, 0, 0, 0);
    /**
     * Represents noon, being 12:00:00.000.
     */
    public final static Time NOON = Time.of(12, 0, 0, 0);

    private LocalTime time;

    private Time(LocalTime time) {
        this.time = time;
    }

    /**
     * The current time, as it would be visible on a clock.
     * @return the current time
     */
    public static Time now() {
        return new Time(LocalTime.now());
    }

    /**
     * The time instance from given values.
     * @param hours the hours, between 0 and 23
     * @param minutes the minutes, between 0 and 59
     * @param seconds the seconds, between 0 and 59
     * @param millis the milliseconds, between 0 and 999
     * @return a new time instance
     */
    public static Time of(int hours, int minutes, int seconds, int millis) {
        return new Time(LocalTime.of(hours, minutes, seconds, millis * 1_000_000));
    }

    /**
     * The time instance from a given LocalTime instance.
     * @param time the LocalTime instance
     * @return a new time instance
     */
    public static Time of(LocalTime time) {
        return new Time(time);
    }

    public static Time of(SkriptDate date) {
        var lcd = date.toLocalDateTime();
        return new Time(LocalTime.of(lcd.getHour(), lcd.getMinute(), lcd.getSecond(), lcd.getNano()));
    }

    /**
     * Parses a given string as a time, using one of the three patterns.
     * @param value the string to parse
     * @return an Optional describing the parsed Time instance
     * @see #DEFAULT_TIME_PATTERN
     * @see #BRITISH_TIME_PATTERN
     * @see #DETAILED_TIME_PATTERN
     */
    public static Optional<Time> parse(String value) {
        if (value.isEmpty())
            return Optional.empty();

        // First pattern
        Matcher matcher = DEFAULT_TIME_PATTERN.matcher(value);
        if (matcher.matches()) {
            // Hours
            int hours = Integer.parseInt(matcher.group(1));
            if (hours == 24) {
                hours = 0; // Allows to write 24:00 -> 24:59 instead of 00:00 -> 00:59
            } else if (hours < 0 || 23 < hours) {
                return Optional.empty();
            }

            // Minutes
            int minutes = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
            if (minutes < 0 || 59 < minutes) {
                return Optional.empty();
            }

            return Optional.of(of(hours, minutes, 0, 0));
        }

        // Second pattern
        matcher = BRITISH_TIME_PATTERN.matcher(value);
        if (matcher.matches()) {
            // Hours
            int hours = Integer.parseInt(matcher.group(1));
            if (hours == 12) {
                hours = 0; // 12 AM is 00:00:00.000 for some weird reason
            } else if (hours < 0 || 11 < hours) {
                return Optional.empty();
            }

            // Minutes
            int minutes = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;
            if (minutes < 0 || 59 < minutes) {
                return Optional.empty();
            }

            // AM/PM
            if (matcher.group(4).equalsIgnoreCase("pm"))
                hours += 12;

            return Optional.of(of(hours, minutes, 0, 0));
        }

        // Third pattern
        matcher = DETAILED_TIME_PATTERN.matcher(value);
        if (matcher.matches()) {
            // Hours
            int hours = Integer.parseInt(matcher.group(1));
            if (hours == 24) {
                hours = 0; // Allows to write 24:00 -> 24:59 instead of 00:00 -> 00:59
            } else if (hours < 0 || 23 < hours) {
                return Optional.empty();
            }

            // Minutes
            int minutes = Integer.parseInt(matcher.group(2));
            if (minutes < 0 || 59 < minutes) {
                return Optional.empty();
            }

            // Seconds and milliseconds
            int seconds = matcher.group(5) != null ? Integer.parseInt(matcher.group(5)) : 0;
            int millis = matcher.group(7) != null ? Integer.parseInt(matcher.group(7)) : 0;
            if (seconds < 0 || 59 < seconds || millis < 0 || 999 < millis) {
                return Optional.empty();
            }

            return Optional.of(of(hours,minutes, seconds, millis));
        }
        return Optional.empty();
    }

    public int getHour() {
        return time.getHour();
    }

    public int getMinute() {
        return time.getMinute();
    }

    public int getSecond() {
        return time.getSecond();
    }

    public int getMillis() {
        return time.getNano() / 1_000_000;
    }

    public LocalTime getTime() {
        return time;
    }

    /**
     * @return the amount of milliseconds that passed since the start of the day
     */
    public int toMillis() {
        var millis = MIDNIGHT.getTime().until(time, ChronoUnit.MILLIS);
        assert millis >= 0;
        return (int) millis;
    }

    /**
     * The duration between two given time instances
     * @param other the time instance to compare
     * @return the duration between the instances, or {@link Duration#ZERO ZERO} if one instance is invalid
     */
    public Duration difference(Time other) {
        return Duration.ofMillis(time.until(other.getTime(), ChronoUnit.MILLIS)).abs();
    }

    /**
     * Add a {@link Duration} to this time.
     * @param span {@link Duration} to add
     */
    public void add(Duration span) {
        time = time.plusNanos(span.toMillis() * 1_000_000);
    }

    /**
     * Subtract a {@link Duration} from this time.
     * @param span {@link Duration} to subtract
     */
    public void subtract(Duration span) {
        time = time.minusNanos(span.toMillis() * 1_000_000);
    }

    /**
     * Get a new instance of this time with the added Duration.
     * @param span {@link Duration} to add to this Time
     * @return new {@link Time} with the added Duration
     */
    public Time plus(Duration span) {
        return new Time(time.plusNanos(span.toMillis() * 1_000_000));
    }

    /**
     * Get a new instance of this time with the subtracted Duration.
     * @param span {@link Duration} to subtract from this Time
     * @return new {@link Time} with the subtracted Duration
     */
    public Time minus(Duration span) {
        return new Time(time.minusNanos(span.toMillis() * 1_000_000));
    }

    @Override
    public int compareTo(Time other) {
        return time.compareTo(other.getTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Time time = (Time) o;
        return this.time.equals(time.getTime());
    }

    @Override
    public String toString() {
        return time.format(DateTimeFormatter.ofPattern(TIME_FORMAT, TIME_LOCALE));
    }
}
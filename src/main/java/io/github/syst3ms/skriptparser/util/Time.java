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
 * Represents a time, as one could see on a clock.
 * @author Mwexim
 */
public class Time implements Comparable<Time> {
    // TODO make a config for this
    public final static String TIME_FORMAT = "HH:mm:ss.SSS";
    public final static Locale TIME_LOCALE = Locale.US;

    /**
     * Returns the latest possible time, being 23:59:59.999.
     */
    public final static Time LATEST = Time.of(23, 59, 59, 999);
    /**
     * Represents midnight, at 12 PM.
     */
    public final static Time MIDNIGHT = Time.of(0, 0, 0, 0);
    /**
     * Represents noon, at 12 AM.
     */
    public final static Time NOON = Time.of(12, 0, 0, 0);

    private final static Pattern DEFAULT_TIME_PATTERN = Pattern.compile("(\\d?\\d)h(\\d\\d)?");
    private final static Pattern BRITISH_TIME_PATTERN = Pattern.compile("(\\d?\\d)(?::(\\d\\d))? ?(?:am|pm)", Pattern.CASE_INSENSITIVE);
    private final static Pattern DETAILED_TIME_PATTERN = Pattern.compile("(\\d?\\d):(\\d\\d)(?::(?:(\\d\\d)(?:\\.(\\d\\d\\d))?))?");

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
     * A time instance from given values.
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
     * A time instance from a given LocalTime instance.
     * @param time the time
     * @return a new time instance
     */
    public static Time of(LocalTime time) {
        return new Time(time);
    }

    /**
     * A time instance from a given date.
     * @param date the date
     * @return a new time instance
     */
    public static Time of(SkriptDate date) {
        var lcd = date.toLocalDateTime();
        return new Time(LocalTime.of(lcd.getHour(), lcd.getMinute(), lcd.getSecond(), lcd.getNano()));
    }

    /**
     * Parses a given string as a time, using one of the three patterns.
     * @param toParse the string to parse
     * @return an Optional describing the parsed Time instance,
     * empty if no match was found.
     * @see #DEFAULT_TIME_PATTERN
     * @see #BRITISH_TIME_PATTERN
     * @see #DETAILED_TIME_PATTERN
     */
    public static Optional<Time> parse(String toParse) {
        if (toParse.isEmpty())
            return Optional.empty();

        Matcher matcher;
        if (!(matcher = DEFAULT_TIME_PATTERN.matcher(toParse)).matches()
                && !(matcher = BRITISH_TIME_PATTERN.matcher(toParse)).matches()
                && !(matcher = DETAILED_TIME_PATTERN.matcher(toParse)).matches())
            return Optional.empty();

        int hours = Integer.parseInt(matcher.group(1));
        if (hours == 24)
            hours = 0; // Allows to write 24:00 -> 24:59 instead of 00:00 -> 00:59
        if (toParse.toLowerCase().contains("am") && hours == 12)
            hours = 0; // Apparently 12AM is equal to 0:00.
        if (toParse.toLowerCase().contains("pm") && hours != 12)
            hours += 12;

        int count = matcher.groupCount();
        int minutes = count >= 2 && matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
        int seconds = count >= 3 && matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;
        int millis = count >= 4 && matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : 0;
        if (hours < 0 || 23 < hours
                || minutes < 0 || 59 < minutes
                || seconds < 0 || 59 < seconds
                || millis < 0 || 999 < millis)
            return Optional.empty();

        return Optional.of(of(hours, minutes, seconds, millis));
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
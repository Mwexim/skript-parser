package io.github.syst3ms.skriptparser.util;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.TimeZone;

public class SkriptDate implements Comparable<SkriptDate> {
    // TODO make a config for this
    public final static String DATE_FORMAT = "EEEE dd MMMM yyyy HH:mm:ss.SSS"; // Add 'zzzXXX' to display time zone as well
    public final static Locale DATE_LOCALE = Locale.US;

    private static TimeZone defaultTimeZone = TimeZone.getDefault();

    private long timestamp;

    private SkriptDate(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Creates a new {@link SkriptDate} of the current time.
     * @return the date
     */
    public static SkriptDate now() {
        // System.currentTimeMillis() returns the date in the UTC time zone!
        return new SkriptDate(System.currentTimeMillis());
    }

    /**
     * Creates a new {@link SkriptDate} with a specific timestamp.
     * Note that timestamps are expressed in milliseconds.
     * The timestamp is internally stored as a date in the UTC time zone.
     * Use {@linkplain #of(long, TimeZone) this} method if you wish to
     * take a non-default time zone into account.
     * @param timestamp the timestamp
     * @return the date
     */
    public static SkriptDate of(long timestamp) {
        return new SkriptDate(timestamp);
    }

    /**
     * Creates a new {@link SkriptDate} with a specific timestamp.
     * Note that timestamps are expressed in milliseconds.
     * The zone offset is taken into account when calculating the
     * final timestamp.
     * @param timestamp the timestamp
     * @param zone the time zone
     * @return the date
     */
    public static SkriptDate of(long timestamp, TimeZone zone) {
        return new SkriptDate(timestamp - zone.getOffset(timestamp));
    }

    /**
     * Creates a new {@link SkriptDate} of the current date at midnight.
     * Note that this takes the {@linkplain #defaultTimeZone default time zone}
     * into account. Use {@linkplain #today(TimeZone) this} method if you want
     * to use a non-default time zone.
     * @return the date
     */
	public static SkriptDate today() {
	    return today(defaultTimeZone);
	}

    /**
     * Creates a new {@link SkriptDate} of the current date at midnight
     * in a specific time zone.
     * @param zone the zone
     * @return the date
     */
    public static SkriptDate today(TimeZone zone) {
        long timestamp = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli();
        return new SkriptDate(timestamp - zone.getOffset(timestamp));
    }

    /**
     * @return the default system time zone
     */
    public static TimeZone getDefaultTimeZone() {
        return defaultTimeZone;
    }

    /**
     * @return the amount of milliseconds that have passed
     * since midnight, January 1st, 1970 in the UTC time zone
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the {@linkplain Duration duration} between the
     * given dates.
     * @param other the other date
     * @return the duration
     */
    public Duration difference(SkriptDate other) {
        return Duration.ofMillis(timestamp - other.getTimestamp()).abs();
    }

    /**
     * Adds a specific {@linkplain Duration duration} to this date.
     * A negative duration will subtract instead.
     * @param duration the duration
     */
    public void add(Duration duration) {
        timestamp += duration.toMillis();
    }

    /**
     * Subtracts a specific {@linkplain Duration duration} from this date.
     * A negative duration will add instead.
     * @param duration the duration
     */
    public void subtract(Duration duration) {
        timestamp -= duration.toMillis();
    }

    /**
     * Adds a specific {@linkplain Duration duration} to this date
     * and returns the result of this addition.
     * A negative duration will subtract instead.
     * @param duration the duration
     * @return the date
     */
    public SkriptDate plus(Duration duration) {
        return new SkriptDate(timestamp + duration.toMillis());
    }

    /**
     * Subtracts a specific {@linkplain Duration duration} from this date
     * and returns the result of this subtraction.
     * A negative duration will add instead.
     * @param duration the duration
     * @return the date
     */
    public SkriptDate minus(Duration duration) {
        return new SkriptDate(timestamp - duration.toMillis());
    }

    /**
     * Returns this date as a {@link LocalDate}.
     * @return the {@link LocalDate} of this date
     */
    public LocalDateTime toLocalDateTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), defaultTimeZone.toZoneId());
    }

    @Override
    public int compareTo(@Nullable SkriptDate other) {
        long d = other == null ? timestamp : timestamp - other.timestamp;
        return d < 0 ? -1 : d > 0 ? 1 : 0;
    }

    public String toString() {
        return toString(DATE_FORMAT);
    }

    /**
     * Returns the string representation of this date using a certain format.
     * @param format the format
     * @return the string representation of this date
     */
    public String toString(String format) {
        var sdf = new SimpleDateFormat(format, DATE_LOCALE);
        sdf.setTimeZone(defaultTimeZone);
        return sdf.format(new java.util.Date(timestamp));
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof SkriptDate))
            return false;
        return compareTo((SkriptDate) obj) == 0;
    }
}
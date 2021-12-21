package io.github.syst3ms.skriptparser.util;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.TimeZone;

public class SkriptDate implements Comparable<SkriptDate> {
    // TODO make a config for this
    public final static String DATE_FORMAT = "EEEE dd MMMM yyyy HH:mm:ss.SSS zzzXXX";
    public final static Locale DATE_LOCALE = Locale.US;
    @SuppressWarnings("FieldMayBeFinal")
    private static TimeZone TIME_ZONE = TimeZone.getDefault();

    private long timestamp;

    private SkriptDate(long timestamp, TimeZone zone) {
        this.timestamp = timestamp - zone.getOffset(timestamp);
    }

    /**
     * Creates a new {@link SkriptDate} of the current time.
     * @return the date
     */
    public static SkriptDate now() {
        return of(System.currentTimeMillis());
    }

    /**
     * Creates a new {@link SkriptDate} with a specific timestamp.
     * Note that timestamps are expressed in milliseconds.
     * @param timestamp the timestamp
     * @return the date
     */
    public static SkriptDate of(long timestamp) {
        return of(timestamp, TIME_ZONE);
    }

    /**
     * Creates a new {@link SkriptDate} with a specific timestamp.
     * The zone offset is taken into account when calculating the
     * final timestamp.
     * @param timestamp the timestamp
     * @param zone the time zone
     * @return the date
     */
    public static SkriptDate of(long timestamp, TimeZone zone) {
        return new SkriptDate(timestamp, zone);
    }

    /**
     * Creates a new {@link SkriptDate} of the current date at midnight.
     * @return the date
     */
	public static SkriptDate today() {
	    var local = LocalDate.now(TIME_ZONE.toZoneId()).atStartOfDay(TIME_ZONE.toZoneId());
	    return of(local.toEpochSecond() * 1000);
	}

    /**
     * @return the default system time zone
     */
    public static TimeZone getTimeZone() {
        return TIME_ZONE;
    }

    /**
     * Returns the timestamp of this date, in milliseconds.
     * @return the timestamp
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
        return of(timestamp + duration.toMillis());
    }

    /**
     * Subtracts a specific {@linkplain Duration duration} from this date
     * and returns the result of this subtraction.
     * A negative duration will add instead.
     * @param duration the duration
     * @return the date
     */
    public SkriptDate minus(Duration duration) {
        return of(timestamp - duration.toMillis());
    }

    /**
     * Returns this date as a {@link LocalDate}.
     * @return the {@link LocalDate} of this date
     */
    public LocalDateTime toLocalDateTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TIME_ZONE.toZoneId());
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
        return new SimpleDateFormat(format, DATE_LOCALE).format(new java.util.Date(timestamp));
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof SkriptDate))
            return false;
        return compareTo((SkriptDate) obj) == 0;
    }
}
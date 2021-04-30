package io.github.syst3ms.skriptparser.util.classes;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Represents a date.
 * @author Njol
 */
public class SkriptDate implements Comparable<SkriptDate> {
    // TODO make a config for this
    public final static String DATE_FORMAT = "EEEE dd MMMM yyyy HH:mm:ss.SSS zzzXXX";
    public final static Locale DATE_LOCALE = Locale.US;
    private static ZoneId ZONE_ID = ZoneId.systemDefault();
    public final static int MILLIS_PER_DAY = 86400000;

    private long timestamp;

    private SkriptDate(long timestamp) {
        this.timestamp = timestamp;
    }

    private SkriptDate(long timestamp, TimeZone zone) {
        long offset = zone.getOffset(timestamp);
        this.timestamp = timestamp - offset;
    }

    /**
     * Get a new {@link SkriptDate} with the current time
     * @return new {@link SkriptDate} with the current time
     */
    public static SkriptDate now() {
        return SkriptDate.of(System.currentTimeMillis());
    }

    public static SkriptDate of(long timestamp) {
        return new SkriptDate(timestamp);
    }

    public static SkriptDate of(long timestamp, TimeZone zone) {
        return new SkriptDate(timestamp, zone);
    }

    /**
     * The current day when it started.
     * @return the current day like it would just start
     */
	public static SkriptDate today() {
	    var localDate = LocalDate.now().atStartOfDay(ZONE_ID);
	    return SkriptDate.of(localDate.toEpochSecond() * 1000);
	}

    public static ZoneId getZoneId() {
        return ZONE_ID;
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
     * The String representation of this date using a certain format
     * @param format the format
     * @return the string representation of this date
     */
    public String toString(String format) {
        StringBuilder sb = new StringBuilder();

        SimpleDateFormat formatted = new SimpleDateFormat(
                format,
                DATE_LOCALE);


        String str = formatted.format(new java.util.Date(timestamp));
        sb.append(str);

        return sb.toString();
    }

    /**
     * Get the timestamp of this date.
     * @return The timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the difference between 2 dates.
     * @param other the other date
     * @return the duration between the dates
     */
    public Duration difference(SkriptDate other) {
        return Duration.ofMillis(timestamp - other.getTimestamp()).abs();
    }

    /**
     * Add a {@link Duration} to this date.
     * @param span {@link Duration} to add
     */
    public void add(Duration span) {
        timestamp += span.toMillis();
    }

    /**
     * Subtract a {@link Duration} from this date.
     * @param span {@link Duration} to subtract
     */
    public void subtract(Duration span) {
        timestamp -= span.toMillis();
    }

    /**
     * Get a new instance of this date with the added Duration.
     * @param span {@link Duration} to add to this Date
     * @return new {@link SkriptDate} with the added Duration
     */
    public SkriptDate plus(Duration span) {
        return new SkriptDate(timestamp + span.toMillis());
    }

    /**
     * Get a new instance of this date with the subtracted Duration.
     * @param span {@link Duration} to subtract from this Date
     * @return new {@link SkriptDate} with the subtracted Duration
     */
    public SkriptDate minus(Duration span) {
        return new SkriptDate(timestamp - span.toMillis());
    }

    /**
     * Get the {@link LocalDate} instance of this date.
     * @return the {@link LocalDate} instance of this date
     */
    public LocalDateTime toLocalDateTime() {
        Instant in = new java.util.Date(timestamp).toInstant();
        return in.atOffset(SkriptDate.ZONE_ID.getRules().getOffset(in)).toLocalDateTime();
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof SkriptDate))
            return false;
        SkriptDate other = (SkriptDate) obj;
        return timestamp == other.timestamp;
    }

}
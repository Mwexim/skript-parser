package io.github.syst3ms.skriptparser.util;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Represents a date.
 * @author Njol
 */
public class SkriptDate implements Comparable<SkriptDate> {

    // TODO make a config for this
    public final static String DATE_FORMAT = "EEEE dd MMMM yyyy HH:mm:ss.SSS zzzXXX";
    public final static Locale DATE_LOCALE = new Locale("US");

    public final static int MILLIS_PER_DAY = 86400000;

    /**
     * Timestamp. Should always be in computer time/UTC/GMT+0.
     */
    private long timestamp;

    public SkriptDate() {
        this(System.currentTimeMillis());
    }

    public SkriptDate(long timestamp) {
        this.timestamp = timestamp;
    }

    public SkriptDate(long timestamp, TimeZone zone) {
        long offset = zone.getOffset(timestamp);
        this.timestamp = timestamp - offset;
    }

    /**
     * Get a new {@link SkriptDate} with the current time
     * @return new {@link SkriptDate} with the current time
     */
    public static SkriptDate now() {
        return new SkriptDate(System.currentTimeMillis());
    }

    public Duration difference(SkriptDate other) {
        return Duration.ZERO.plusMillis(Math.abs(timestamp - other.getTimestamp()));
    }

    @Override
    public int compareTo(@Nullable SkriptDate other) {
        long d = other == null ? timestamp : timestamp - other.timestamp;
        return d < 0 ? -1 : d > 0 ? 1 : 0;
    }



    @Nullable
    public String toString() {
        StringBuilder sb = new StringBuilder();

        SimpleDateFormat format = new SimpleDateFormat(
                DATE_FORMAT,
                DATE_LOCALE);


        String str = format.format(new java.util.Date(timestamp));
        sb.append(str);
        if (sb.toString().isEmpty()) return null;

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
        return in.atOffset(ZoneOffset.systemDefault().getRules().getOffset(in)).toLocalDateTime();
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
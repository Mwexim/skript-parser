package io.github.syst3ms.skriptparser.util;

import io.github.syst3ms.skriptparser.util.classes.Time;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

import static io.github.syst3ms.skriptparser.util.math.NumberMath.parseInt;

public class TimeUtils {

    /**
     * A time unit defined to be equal to 50ms.
     */
    public static Duration TICK = Duration.ofMillis(50);

    /**
     * Parses a string as a duration
     * @param str the string to parse
     * @return the parsed duration, empty if no match
     */
    public static Optional<Duration> parseDuration(String str) {
        // TODO parsing gets in trouble with lists
        if (str.isEmpty())
            return Optional.empty();
        Duration dur = Duration.ZERO;

        // If there is a ', and' in the string, like '5 days, and 3 seconds',
        // then we return null since we need to keep consistency with lists.
        if (str.contains(", and"))
            return Optional.empty();

        // Removing all ',' except the last one.
        boolean endsComma = str.endsWith(",");
        str = str.replaceAll(",", "");
        if (endsComma) str = str.concat(",");

        String[] split = str.split("\\s+");

        // Days, hours, minutes, seconds, milliseconds. This is to keep track that they come in the right order.
        // Like '5 days and 3 minutes', not '3 seconds and 5 hours'.
        boolean[] passed = {false, false, false, false, false};

        // Checks how many 'and' occurrences we had. We can only use 'and' once, before our last part.
        // This is, again, to keep consistency with lists.
        int currentAnd = 0;

        for (int i = 0; i < split.length; i++) {
            String s = split[i];

            if (s.equals("and")) {
                currentAnd++;
                if ((split.length > 2 && !split[split.length - 3].equals("and")) || currentAnd > 1)
                    return Optional.empty();
                continue;
            }

            long amount;

            try {
                amount = Long.parseLong(s);
                s = split[++i];
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                return Optional.empty();
            }

            if (s.matches("days?")) {
                if (passed[0] || oneIsTrue(passed[1], passed[2], passed[3], passed[4]))
                    return Optional.empty(); // Days was already used elsewhere and is used again.
                dur = dur.plusDays(amount);
                passed[0] = true;
            } else if (s.matches("hours?")) {
                if (passed[1] || oneIsTrue(passed[2], passed[3], passed[4]))
                    return Optional.empty(); // Hours was already used elsewhere and is used again.
                dur = dur.plusHours(amount);
                passed[1] = true;
            } else if (s.matches("minutes?")) {
                if (passed[2] || oneIsTrue(passed[3], passed[4]))
                    return Optional.empty(); // Minutes was already used elsewhere and is used again.
                dur = dur.plusMinutes(amount);
                passed[2] = true;
            } else if (s.matches("seconds?")) {
                if (passed[3] || oneIsTrue(passed[4]))
                    return Optional.empty(); // Seconds was already used elsewhere and is used again.
                dur = dur.plusSeconds(amount);
                passed[3] = true;
            } else if (s.matches("milli(second)?s?")) {
                if (passed[4]) return Optional.empty(); // Millis was already used elsewhere and is used again.
                dur = dur.plusMillis(amount);
                passed[4] = true;
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(dur);
    }

    public static Optional<Time> parseTime(String str) {
        if (str.isEmpty())
            return Optional.empty();

        var firstMatcher = Pattern.compile("\\d?\\dh(\\d\\d)?").matcher(str);
        var secondMatcher = Pattern.compile("(\\d?\\d)(:(\\d\\d))? ?(am|pm)", Pattern.CASE_INSENSITIVE).matcher(str);
        var thirdMatcher = Pattern.compile("(\\d?\\d):(\\d\\d)(:((\\d\\d)(\\.(\\d\\d\\d))?))?").matcher(str);

        /*
         * We will sort time literals in 3 categories:
         * 1. [#]#h[##], like 18h30
         * 2. [#]#[:##][ ](am|pm), like 11 am or 12:30pm
         * 3. [#]#:##[:##[.###]], like 18:30:15.500
         */
        if (firstMatcher.matches()) {
            // First category
            String[] parts = str.split("h");
            int hours = parseInt(parts[0]);
            if (hours == 24) {
                hours = 0; // Allows to write 24:00 -> 24:59 instead of 00:00 -> 00:59
            } else if (hours < 0 || 23 < hours) {
                return Optional.empty();
            }
            int minutes = (parts.length > 1
                    ? parseInt(parts[1])
                    : 0);
            if (minutes < 0 || 59 < minutes) {
                return Optional.empty();
            }
            return Optional.of(Time.of(hours, minutes, 0, 0));
        } else if (secondMatcher.matches()) {
            // Second category
            int hours = parseInt(secondMatcher.group(1));
            if (hours == 12) {
                hours = 0; // 12 AM is 00:00:00.000 for some weird reason
            } else if (hours < 0 || 11 < hours) {
                return Optional.empty();
            }
            int minutes = 0;
            if (secondMatcher.group(3) != null)
                minutes = parseInt(secondMatcher.group(3));
            if (minutes < 0 || 59 < minutes) {
                return Optional.empty();
            }
            if (secondMatcher.group(4).equalsIgnoreCase("pm"))
                hours += 12;
            return Optional.of(Time.of(hours, minutes, 0, 0));
        } else if (thirdMatcher.matches()) {
            // Third category
            int hours = parseInt(thirdMatcher.group(1));
            if (hours == 24) {
                hours = 0; // Allows to write 24:00 -> 24:59 instead of 00:00 -> 00:59
            } else if (hours < 0 || 23 < hours) {
                return Optional.empty();
            }
            int minutes = parseInt(thirdMatcher.group(2));
            if (minutes < 0 || 59 < minutes) {
                return Optional.empty();
            }
            int seconds = 0, millis = 0;
            if (thirdMatcher.group(5) != null) {
                seconds = parseInt(thirdMatcher.group(5));
            }
            if (thirdMatcher.group(7) != null) {
                millis = parseInt(thirdMatcher.group(7));
            }
            if (seconds < 0 || 59 < seconds || millis < 0 || 999 < millis) {
                return Optional.empty();
            }
            return Optional.of(Time.of(hours,minutes, seconds, millis));
        } else {
            return Optional.empty();
        }
    }

    public static String toStringDuration(Duration dur) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        long days = dur.toDays();
        if (days > 0) {
            sb.append(days)
                    .append(" day")
                    .append(days == 1 ? "" : "s");
            dur = dur.minusDays(days);
            first = false;
        }

        long hours = dur.toHours();
        if (hours > 0) {
            sb.append(first ? "" : ", ")
                    .append(hours)
                    .append(" hour")
                    .append(hours == 1 ? "" : "s");
            dur = dur.minusHours(hours);
            first = false;
        }

        long mins = dur.toMinutes();
        if (mins > 0) {
            sb.append(first ? "" : ", ")
                    .append(mins)
                    .append(" minute")
                    .append(mins == 1 ? "" : "s");
            dur = dur.minusMinutes(mins);
            first = false;
        }

        long secs = dur.getSeconds();
        if (secs > 0) {
            sb.append(first ? "" : ", ")
                    .append(secs)
                    .append(" second")
                    .append(secs == 1 ? "" : "s");
            dur = dur.minusSeconds(secs);
            first = false;
        }

        long millis = dur.toMillis();
        if (millis > 0)
            sb.append(first ? "" : ", ").append(millis).append(" millisecond").append(millis == 1 ? "" : "s");

        String str = sb.toString();

        // Replaces the last ',' with 'and'.
        int i = sb.lastIndexOf(",");
        if (i != -1)
            sb.replace(i, i + 1, " and");

        return str;
    }

    private static boolean oneIsTrue(Boolean... booleans) {
        return Arrays.stream(booleans).anyMatch(Boolean::booleanValue);
    }
}

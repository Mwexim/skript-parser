package io.github.syst3ms.skriptparser.util;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Arrays;

public class TimeUtils {

    @Nullable
    public static Duration parseDuration(String str) {
        // TODO parsing gets in trouble with lists
        if (str.isEmpty())
            return null;
        Duration dur = Duration.ZERO;

        // If there is a ', and' in the string, like '5 days, and 3 seconds',
        // then we return null since we need to keep consistency with lists.
        if (str.contains(", and"))
            return null;

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
                    return null;
                continue;
            }

            long amount;

            try {
                amount = Long.parseLong(s);
                s = split[++i];
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                return null;
            }

            if (s.matches("days?")) {
                if (passed[0] || oneIsTrue(passed[1], passed[2], passed[3], passed[4]))
                    return null; // Days was already used elsewhere and is used again.
                dur = dur.plusDays(amount);
                passed[0] = true;
            } else if (s.matches("hours?")) {
                if (passed[1] || oneIsTrue(passed[2], passed[3], passed[4]))
                    return null; // Hours was already used elsewhere and is used again.
                dur = dur.plusHours(amount);
                passed[1] = true;
            } else if (s.matches("minutes?")) {
                if (passed[2] || oneIsTrue(passed[3], passed[4]))
                    return null; // Minutes was already used elsewhere and is used again.
                dur = dur.plusMinutes(amount);
                passed[2] = true;
            } else if (s.matches("seconds?")) {
                if (passed[3] || oneIsTrue(passed[4]))
                    return null; // Seconds was already used elsewhere and is used again.
                dur = dur.plusSeconds(amount);
                passed[3] = true;
            } else if (s.matches("milliseconds?")) {
                if (passed[4]) return null; // Millis was already used elsewhere and is used again.
                dur = dur.plusMillis(amount);
                passed[4] = true;
            } else {
                return null;
            }
        }
        return dur;
    }

    @Nullable
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
        if (str.isEmpty())
            return null;

        // Replaces the last ',' with 'and'.
        int i = sb.lastIndexOf(",");
        if (i != -1)
            sb.replace(i, i + 1, " and");

        return sb.toString();
    }

    private static boolean oneIsTrue(Boolean... booleans) {
        return Arrays.stream(booleans).anyMatch(Boolean::booleanValue);
    }

}

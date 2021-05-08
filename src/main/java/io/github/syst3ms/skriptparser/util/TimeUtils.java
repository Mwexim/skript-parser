package io.github.syst3ms.skriptparser.util;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static io.github.syst3ms.skriptparser.util.math.NumberMath.parseInt;

public class TimeUtils {
    /**
     * The amount of milliseconds it takes for a tick to pass.
     * A 'tick' is a predetermined time-unit of 50 milliseconds.
     */
    public static final int TICK = 50;

    private static final Pattern DURATION_SPLIT_PATTERN = Pattern.compile("\\s*(,)\\s*|\\s+(and)\\s+", Pattern.CASE_INSENSITIVE);
    private static final Map<String, Integer> DURATION_UNITS = new LinkedHashMap<>();

    static {
        // LinkedHashMap because order is important
        DURATION_UNITS.put("days?", 86_400_000);
        DURATION_UNITS.put("hours?", 3_600_000);
        DURATION_UNITS.put("minutes?", 60_000);
        DURATION_UNITS.put("seconds?", 1000);
        DURATION_UNITS.put("milli(second)?s?", 1);
    }

    public static Optional<Duration> parseDuration(String value) {
        if (value.isEmpty())
            return Optional.empty();

        long duration = 0;
        var split = value.toLowerCase().split("\\s+");
        var usedUnits = new boolean[5]; // Defaults to false

        for (int i = 0; i < split.length; i++) {
            var unit = split[i];
            double delta;

            if (unit.equals("and")) {
                if (i == 0 || i == split.length - 1) {
                    // 'and' in front or at the end
                    return Optional.empty();
                }
                continue;
            } else if (unit.matches("an?")) {
                if (i == split.length - 1) {
                    // 'a' or 'an' at the end
                    return Optional.empty();
                }
                delta = 1;
                unit = split[++i];
            } else if (unit.matches("\\d+(\\.\\d+)?")) {
                if (i == split.length - 1) {
                    // a number at the end
                    return Optional.empty();
                }
                // We do not catch the exception since we checked it earlier.
                delta = Double.parseDouble(unit);
                unit = split[++i];
            } else {
                return Optional.empty();
            }

            // Remove trailing ','
            if (unit.endsWith(",")) {
                if (split[i + 1].equals("and")) {
                    // ', and' in the parsed string
                    return Optional.empty();
                }
                unit = unit.substring(0, unit.length() - 1);
            }

            int millis = -1;
            int iteration = 0;
            for (var entry : DURATION_UNITS.entrySet()) {
                if (unit.matches(entry.getKey()) && !usedUnits[iteration]) {
                    millis = entry.getValue();

                    for (int j = 0; j < iteration + 1; j++)
                        usedUnits[j] = true;
                    break;
                }
                iteration++;
            }
            if (millis == -1)
                return Optional.empty();

            duration += delta * millis;
        }
        return Optional.of(Duration.ofMillis(duration));
    }

    public static String toStringDuration(Duration duration) {
        var builder = new StringBuilder();
        long millis = duration.toMillis();

        boolean first = true;
        String[] unitNames = {"day", "hour", "minute", "second", "millisecond"};
        int[] unitMillis = {86_400_000, 3_600_000, 60_000, 1000, 1};

        for (int i = 0; i < unitMillis.length; i++) {
            long result = Math.floorDiv(millis, unitMillis[i]);
            if (result > 0) {
                builder.append(first ? "" : ", ")
                        .append(result == 1 ? (unitNames[i].equals("hour") ? "an" : "a") : result)
                        .append(" ")
                        .append(unitNames[i])
                        .append(result > 1 ? "s" : "");
                millis -= result * unitMillis[i];
                first = false;
            }
        }
        int i = builder.lastIndexOf(",");
        if (i != -1)
            builder.replace(i, i + 1, " and");

        return builder.toString();
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
}

package io.github.syst3ms.skriptparser.util;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {
    /**
     * The amount of milliseconds it takes for a tick to pass.
     * A 'tick' is a predetermined time-unit of 50 milliseconds.
     */
    public static final int TICK = 50;

    private static final Pattern TICK_PATTERN = Pattern.compile("(a|\\d+) ticks?");
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
        if (value.isEmpty()) {
            return Optional.empty();
        }

        // Tick duration
        Matcher matcher = TICK_PATTERN.matcher(value);
        if (matcher.matches()) {
            var amount = matcher.group(1);
            if (amount.equals("a")) {
                return Optional.of(Duration.ofMillis(TICK));
            } else {
                int delta = Integer.parseInt(matcher.group(1));
                return Optional.of(Duration.ofMillis(TICK * delta));
            }
        }

        // Normal duration
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
}

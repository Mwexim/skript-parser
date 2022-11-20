package io.github.syst3ms.skriptparser.util;

import java.time.Duration;
import java.util.Optional;

public class DurationUtils {
    /**
     * The amount of milliseconds it takes for a tick to pass.
     * A 'tick' is a predetermined time-unit of 50 milliseconds.
     */
    public static final int TICK = 50;

    /*
     * See these arrays as one single array with triplets of information
     * about a certain time unit. Sadly, Java does not allow to create a
     * clean alternative for this.
     */
    private static final String[] unitPatterns = {"days?", "hours?", "minutes?", "seconds?", "milli(second)?s?"};
    private static final String[] unitNames = {"day", "hour", "minute", "second", "millisecond"};
    private static final int[] unitMillis = {86_400_000, 3_600_000, 60_000, 1000, 1};

    public static Optional<Duration> parseDuration(String value) {
        if (value.isBlank())
            return Optional.empty();

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
            for (int j = 0; j < unitPatterns.length; j++) {
                if (unit.matches(unitPatterns[j]) && !usedUnits[iteration]) {
                    millis = unitMillis[j];

                    for (int k = 0; k < iteration + 1; k++)
                        usedUnits[k] = true;
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
        for (int i = 0; i < unitMillis.length; i++) {
            long result = Math.floorDiv(millis, unitMillis[i]);
            if (result > 0) {
                builder.append(first ? "" : ", ")
                        .append(result == 1 ? (unitNames[i].equals("hour") ? "an" : "a") : result)
                        .append(' ')
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

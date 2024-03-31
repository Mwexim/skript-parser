package io.github.syst3ms.skriptparser.registration;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.Arithmetic;
import io.github.syst3ms.skriptparser.types.changers.TypeSerializer;
import io.github.syst3ms.skriptparser.types.comparisons.Comparator;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;
import io.github.syst3ms.skriptparser.types.ranges.Ranges;
import io.github.syst3ms.skriptparser.util.DurationUtils;
import io.github.syst3ms.skriptparser.util.SkriptDate;
import io.github.syst3ms.skriptparser.util.Time;
import io.github.syst3ms.skriptparser.util.color.Color;
import io.github.syst3ms.skriptparser.util.math.BigDecimalMath;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * A class registering features such as types and comparators at startup.
 */
public class DefaultRegistration {
    private static final String INTEGER_PATTERN = "-?[0-9]+";
    private static final String DECIMAL_PATTERN = "-?[0-9]+\\.[0-9]+";

    public static void register() {
        SkriptRegistration registration = Parser.getMainRegistration();

        /*
         * Classes
         */
        registration.addType(
                Object.class,
                "object",
                "object@s"
        );

        registration.newType(Number.class,"number", "number@s")
                .literalParser(s -> {
                    if (s.startsWith("_") || s.endsWith("_"))
                        return null;
                    s = s.replaceAll("_", "");
                    if (s.matches(DECIMAL_PATTERN)) {
                        return new BigDecimal(s);
                    } else if (s.matches(INTEGER_PATTERN)) {
                        return new BigInteger(s);
                    } else {
                        return null;
                    }
                })
                .serializer(new TypeSerializer<Number>() {
                    @Override
                    public JsonElement serialize(Gson gson, Number value) {
                        JsonObject json = new JsonObject();
                        json.addProperty("number", value);
                        return json;
                    }

                    @Override
                    public Number deserialize(Gson gson, JsonElement element) {
                        return element.getAsJsonObject().get("number").getAsNumber();
                    }
                })
                .toStringFunction(o -> {
                    if (o instanceof BigDecimal) {
                        BigDecimal bd = (BigDecimal) o;
                        int significantDigits = bd.scale() <= 0
                                ? bd.precision() + bd.stripTrailingZeros().scale()
                                : bd.precision();
                        return ((BigDecimal) o).setScale(Math.min(10, significantDigits), RoundingMode.HALF_UP)
                                .stripTrailingZeros()
                                .toPlainString();
                    } else {
                        return o.toString();
                    }
                })
                .arithmetic(new Arithmetic<Number, Number>() {
                    @Override
                    public Number difference(Number first, Number second) {
                        if (first instanceof BigDecimal || second instanceof BigDecimal) {
                            var f = BigDecimalMath.getBigDecimal(first);
                            var s = BigDecimalMath.getBigDecimal(second);
                            return f.subtract(s).abs();
                        } else {
                            assert first instanceof BigInteger && second instanceof BigInteger;
                            return ((BigInteger) first).subtract(((BigInteger) second)).abs();
                        }
                    }

                    @Override
                    public Number add(Number value, Number difference) {
                        if (value instanceof BigDecimal || difference instanceof BigDecimal) {
                            var v = BigDecimalMath.getBigDecimal(value);
                            var d = BigDecimalMath.getBigDecimal(difference);
                            return v.add(d);
                        } else {
                            assert value instanceof BigInteger && difference instanceof BigInteger;
                            return ((BigInteger) value).add(((BigInteger) difference));
                        }
                    }

                    @Override
                    public Number subtract(Number value, Number difference) {
                        if (value instanceof BigDecimal || difference instanceof BigDecimal) {
                            var v = BigDecimalMath.getBigDecimal(value);
                            var d = BigDecimalMath.getBigDecimal(difference);
                            return v.subtract(d);
                        } else {
                            assert value instanceof BigInteger && difference instanceof BigInteger;
                            return ((BigInteger) value).subtract(((BigInteger) difference));
                        }
                    }

                    @Override
                    public Class<? extends Number> getRelativeType() {
                        return Number.class;
                    }
                }).register();

        registration.newType(BigInteger.class, "integer", "integer@s")
                .literalParser(s -> {
                    if (s.startsWith("_") || s.endsWith("_"))
                        return null;
                    s = s.replaceAll("_", "");
                    return s.matches(INTEGER_PATTERN) ? new BigInteger(s) : null;
                })
                .serializer(new TypeSerializer<BigInteger>() {
                    @Override
                    public JsonElement serialize(Gson gson, BigInteger value) {
                        JsonObject json = new JsonObject();
                        json.addProperty("number", value);
                        return json;
                    }

                    @Override
                    public BigInteger deserialize(Gson gson, JsonElement element) {
                        return BigInteger.valueOf(element.getAsJsonObject().get("number").getAsNumber().longValue());
                    }
                })
                .arithmetic(new Arithmetic<BigInteger, BigInteger>() {
                    @Override
                    public BigInteger difference(BigInteger first, BigInteger second) {
                        return first.subtract(second).abs();
                    }

                    @Override
                    public BigInteger add(BigInteger value, BigInteger difference) {
                        return value.add(difference);
                    }

                    @Override
                    public BigInteger subtract(BigInteger value, BigInteger difference) {
                        return value.subtract(difference);
                    }

                    @Override
                    public Class<? extends BigInteger> getRelativeType() {
                        return BigInteger.class;
                    }
                })
                .register();

        registration.addType(
                String.class,
                "string",
                "string@s",
                new TypeSerializer<String>() {
                    @Override
                    public JsonElement serialize(Gson gson, String value) {
                        JsonObject json = new JsonObject();
                        json.addProperty("string", value);
                        return json;
                    }

                    @Override
                    public String deserialize(Gson gson, JsonElement element) {
                        return element.getAsJsonObject().get("string").getAsString();
                    }
                }
        );

        registration.newType(Boolean.class, "boolean", "boolean@s")
                .literalParser(s -> {
                    if (s.equalsIgnoreCase("true")) {
                        return true;
                    } else if (s.equalsIgnoreCase("false")) {
                        return false;
                    } else {
                        return null;
                    }
                })
                .serializer(new TypeSerializer<Boolean>() {
                    @Override
                    public JsonElement serialize(Gson gson, Boolean value) {
                        JsonObject json = new JsonObject();
                        json.addProperty("boolean", value);
                        return json;
                    }

                    @Override
                    public Boolean deserialize(Gson gson, JsonElement element) {
                        return element.getAsJsonObject().get("boolean").getAsBoolean();
                    }
                })
                .toStringFunction(String::valueOf)
                .register();

        registration.newType(Type.class, "type", "type@s")
                .literalParser(s -> TypeManager.getByExactName(s.toLowerCase()).orElse(null))
                .toStringFunction(Type::getBaseName)
                .register();

        registration.newType(Color.class, "color", "color@s")
                .literalParser(s -> Color.ofLiteral(s).orElse(null))
                .toStringFunction(Color::toString)
                .register();

        registration.newType(Duration.class, "duration", "duration@s")
                .literalParser(s -> DurationUtils.parseDuration(s).orElse(null))
                .toStringFunction(DurationUtils::toStringDuration)
                .arithmetic(new Arithmetic<Duration, Duration>() {
                    @Override
                    public Duration difference(Duration first, Duration second) {
                        return first.minus(second).abs();
                    }

                    @Override
                    public Duration add(Duration value, Duration difference) {
                        return value.plus(difference);
                    }

                    @Override
                    public Duration subtract(Duration value, Duration difference) {
                        return value.minus(difference);
                    }

                    @Override
                    public Class<? extends Duration> getRelativeType() {
                        return Duration.class;
                    }
                })
                .register();

        registration.newType(SkriptDate.class, "date", "date@s")
                .toStringFunction(SkriptDate::toString)
                .arithmetic(new Arithmetic<SkriptDate, Duration>() {
                    @Override
                    public Duration difference(SkriptDate first, SkriptDate second) {
                        return first.difference(second);
                    }

                    @Override
                    public SkriptDate add(SkriptDate value, Duration difference) {
                        return value.plus(difference);
                    }

                    @Override
                    public SkriptDate subtract(SkriptDate value, Duration difference) {
                        return value.minus(difference);
                    }

                    @Override
                    public Class<? extends Duration> getRelativeType() {
                        return Duration.class;
                    }
                })
                .register();

        registration.newType(Time.class, "time", "time@s")
                .literalParser(s -> Time.parse(s).orElse(null))
                .toStringFunction(Time::toString)
                .arithmetic(new Arithmetic<Time, Duration>() {
                    @Override
                    public Duration difference(Time first, Time second) {
                        return first.difference(second);
                    }

                    @Override
                    public Time add(Time value, Duration difference) {
                        return value.plus(difference);
                    }

                    @Override
                    public Time subtract(Time value, Duration difference) {
                        return value.minus(difference);
                    }

                    @Override
                    public Class<? extends Duration> getRelativeType() {
                        return Duration.class;
                    }
                })
                .register();

        /*
         * Comparators
         */
        Comparators.registerComparator(
                Number.class,
                Number.class,
                new Comparator<>(true) {
                    @Override
                    public Relation apply(Number number, Number number2) {
                        if (number instanceof BigDecimal || number2 instanceof BigDecimal) {
                            BigDecimal bd = BigDecimalMath.getBigDecimal(number).setScale(10, RoundingMode.HALF_UP);
                            BigDecimal bd2 = BigDecimalMath.getBigDecimal(number2).setScale(10, RoundingMode.HALF_UP);
                            return Relation.get(bd.compareTo(bd2));
                        } else {
                            assert number instanceof BigInteger && number2 instanceof BigInteger;
                            return Relation.get(((BigInteger) number).compareTo((BigInteger) number2));
                        }
                    }
                }
        );

        Comparators.registerComparator(
                Duration.class,
                Duration.class,
                new Comparator<>(true) {
                    @Override
                    public Relation apply(Duration duration, Duration duration2) {
                        return Relation.get(duration.compareTo(duration2));
                    }
                }
        );

        /*
         * Ranges
         */
        Ranges.registerRange(
                BigInteger.class,
                BigInteger.class,
                (l, r) -> {
                    if (l.compareTo(r) >= 0) {
                        return new BigInteger[0];
                    } else {
                        List<BigInteger> elements = new ArrayList<>();
                        BigInteger current = l;
                        do {
                            elements.add(current);
                            current = current.add(BigInteger.ONE);
                        } while (current.compareTo(r) <= 0);
                        return elements.toArray(new BigInteger[0]);
                    }
                }
        );

        // Actually a character range
        Ranges.registerRange(
                String.class,
                String.class,
                (l, r) -> {
                    if (l.length() != 1 || r.length() != 1)
                        return new String[0];
                    char leftChar = l.charAt(0), rightChar = r.charAt(0);
                    return IntStream.range(leftChar, rightChar + 1)
                            .mapToObj(i -> Character.toString((char) i))
                            .toArray(String[]::new);
                }
        );

        /*
         * Converters
         */
        registration.addConverter(Number.class, BigInteger.class, n -> {
            if (n instanceof BigInteger) {
                return Optional.of((BigInteger) n);
            } else {
                return Optional.of(BigInteger.valueOf(n.longValue()));
            }
        });

        registration.addConverter(SkriptDate.class, Time.class, da -> Optional.of(Time.of(da)));

        registration.register(true); // Ignoring logs here, we control the input
    }
}
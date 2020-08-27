package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.types.changers.Arithmetic;
import io.github.syst3ms.skriptparser.types.comparisons.Comparator;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.types.ranges.Ranges;
import io.github.syst3ms.skriptparser.util.SkriptDate;
import io.github.syst3ms.skriptparser.util.TimeUtils;
import io.github.syst3ms.skriptparser.util.math.BigDecimalMath;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * A class registering features such as types and comparators at startup.
 */
public class DefaultRegistration {

    public static void register() {
        SkriptRegistration registration = Main.getMainRegistration();
        registration.addType(
                Object.class,
                "object",
                "object@s"
        );
        registration.newType(Number.class,"number", "number@s")
                    .literalParser(s -> {
                        Number n;
                        if (s.endsWith("L") || s.endsWith("l")) {
                            try {
                                n = Long.parseLong(s.substring(0, s.length() - 1));
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        } else if (s.endsWith("D") || s.endsWith("d")) {
                            try {
                                n = Double.parseDouble(s.substring(0, s.length() - 1));
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        } else if (s.contains(".")) {
                            try {
                                n = new BigDecimal(s);
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        } else {
                            try {
                                n = new BigInteger(s);
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        }
                        return n;
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
                        } else if (o instanceof Double) {
                            return Double.toString((Double) o);
                        } else {
                            return o.toString();
                        }
                    })
                    .arithmetic(new Arithmetic<Number, Number>() {
                        @Override
                        public Number difference(Number first, Number second) {
                            // Creating BigDecimals and BigIntegers from strings is costly, so we better make checks before resorting to that
                            if (first instanceof BigDecimal || second instanceof BigDecimal) {
                                // String construction is required for BigDecimal, other methods aren't reliable
                                if (first instanceof BigDecimal && second instanceof BigDecimal) {
                                    return ((BigDecimal) first).subtract((BigDecimal) second).abs();
                                } else if (first instanceof BigDecimal) {
                                    return ((BigDecimal) first).subtract(new BigDecimal(second.toString())).abs();
                                } else {
                                    return ((BigDecimal) second).subtract(new BigDecimal(first.toString())).abs();
                                }
                            } else if (first instanceof Double || second instanceof Double) {
                                return Math.abs(first.doubleValue() - second.doubleValue());
                            } else if (first instanceof BigInteger || second instanceof BigInteger) {
                                if (first instanceof BigInteger && second instanceof BigInteger) {
                                    return ((BigInteger) first).subtract(((BigInteger) second)).abs();
                                } else if (first instanceof BigInteger) {
                                    return ((BigInteger) first).subtract(BigInteger.valueOf(second.longValue())).abs();
                                } else {
                                    return ((BigInteger) second).subtract(BigInteger.valueOf(second.longValue())).abs();
                                }
                            } else {
                                return Math.abs(first.longValue() - second.longValue());
                            }
                        }

                        @Override
                        public Number add(Number value, Number difference) {
                            if (value instanceof BigDecimal || difference instanceof BigDecimal) {
                                if (value instanceof BigDecimal && difference instanceof BigDecimal) {
                                    return ((BigDecimal) value).add(((BigDecimal) difference));
                                } else if (value instanceof BigDecimal) {
                                    return ((BigDecimal) value).add(new BigDecimal(difference.toString()));
                                } else {
                                    return ((BigDecimal) difference).add(new BigDecimal(value.toString()));
                                }
                            } else if (value instanceof Double || difference instanceof Double) {
                                return value.doubleValue() + difference.doubleValue();
                            } else if (value instanceof BigInteger || difference instanceof BigInteger) {
                                if (value instanceof BigInteger && difference instanceof BigInteger) {
                                    return ((BigInteger) value).add(((BigInteger) difference));
                                } else if (value instanceof BigInteger) {
                                    return ((BigInteger) value).add(BigInteger.valueOf(difference.longValue()));
                                } else {
                                    return ((BigInteger) difference).add(BigInteger.valueOf(value.longValue()));
                                }
                            } else {
                                return value.longValue() + difference.longValue();
                            }
                        }

                        @Override
                        public Number subtract(Number value, Number difference) {
                            if (value instanceof BigDecimal || difference instanceof BigDecimal) {
                                if (value instanceof BigDecimal && difference instanceof BigDecimal) {
                                    return ((BigDecimal) value).subtract(((BigDecimal) difference));
                                } else if (value instanceof BigDecimal) {
                                    return ((BigDecimal) value).subtract(new BigDecimal(difference.toString()));
                                } else {
                                    return new BigDecimal(value.toString()).subtract((BigDecimal) difference);
                                }
                            } else if (value instanceof Double || difference instanceof Double) {
                                return value.doubleValue() - difference.doubleValue();
                            } else if (value instanceof BigInteger || difference instanceof BigInteger) {
                                if (value instanceof BigInteger && difference instanceof BigInteger) {
                                    return ((BigInteger) value).subtract(((BigInteger) difference));
                                } else if (value instanceof BigInteger) {
                                    return ((BigInteger) value).subtract(BigInteger.valueOf(difference.longValue()));
                                } else {
                                    return BigInteger.valueOf(value.longValue()).subtract((BigInteger) difference);
                                }
                            } else {
                                return value.longValue() - difference.longValue();
                            }
                        }

                        @Override
                        public Class<? extends Number> getRelativeType() {
                            return Number.class;
                        }
                    }).register();
        registration.newType(Long.class, "integer", "integer@s")
                .literalParser(s -> {
                    try {
                        return Long.parseLong(s);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .arithmetic(new Arithmetic<Long, Long>() {
                    @Override
                    public Long difference(Long first, Long second) {
                        return Math.abs(first - second);
                    }

                    @Override
                    public Long add(Long value, Long difference) {
                        return value + difference;
                    }

                    @Override
                    public Long subtract(Long value, Long difference) {
                        return value - difference;
                    }

                    @Override
                    public Class<? extends Long> getRelativeType() {
                        return Long.class;
                    }
                })
                .register();
        registration.newType(BigInteger.class, "biginteger", "biginteger@s")
                .literalParser(s -> {
                    try {
                        return new BigInteger(s);
                    } catch (NumberFormatException e) {
                        return null;
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
                "string@s"
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
                    .toStringFunction(String::valueOf)
                    .register();
        registration.newType(Duration.class, "duration", "duration@s")
                .literalParser(TimeUtils::parseDuration)
                .toStringFunction(TimeUtils::toStringDuration)
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

        Comparators.registerComparator(
                Number.class,
                Number.class,
                new Comparator<>(true) {
                    @SuppressWarnings("unchecked")
                    @Override
                    public Relation apply(Number number, Number number2) {
                        if (number.getClass() == number2.getClass()) {
                            return Relation.get(((Comparable<? super Number>) number).compareTo(number2));
                        } else if (number instanceof BigDecimal || number2 instanceof BigDecimal) {
                            BigDecimal bd = BigDecimalMath.getBigDecimal(number);
                            BigDecimal bd2 = BigDecimalMath.getBigDecimal(number2);
                            return Relation.get(bd.compareTo(bd2));
                        } else if ((number instanceof BigInteger || number2 instanceof BigInteger) &&
                                (number instanceof Long || number2 instanceof Long)) {
                            BigInteger bi = BigDecimalMath.getBigInteger(number);
                            BigInteger bi2 = BigDecimalMath.getBigInteger(number2);
                            return Relation.get(bi.compareTo(bi2));
                        } else if ((number instanceof Double || number instanceof Long) &&
                                (number2 instanceof Double || number2 instanceof Long)) {
                            double d = number.doubleValue() - number2.doubleValue();
                            return Double.isNaN(d) ? Relation.NOT_EQUAL : Relation.get(d);
                        } else {
                            BigDecimal bd = BigDecimalMath.getBigDecimal(number);
                            BigDecimal bd2 = BigDecimalMath.getBigDecimal(number2);
                            return Relation.get(bd.compareTo(bd2));
                        }
                    }
                }
        );
        /*
         * Ranges
         */
        Ranges.registerRange(
                Long.class,
                Long.class,
                (l, r) -> {
                    if (l.compareTo(r) >= 0) {
                        return new Long[0];
                    } else {
                        return LongStream.range(l, r + 1)
                                .boxed()
                                .toArray(Long[]::new);
                    }
                }
        );
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
        Converters.registerConverter(Number.class, Long.class, n -> Optional.of(n instanceof Long ? (Long) n : n.longValue()));
        Converters.registerConverter(Number.class, BigInteger.class, n -> {
            if (n instanceof BigInteger) {
                return Optional.of((BigInteger) n);
            } else if (n instanceof Long) {
                return Optional.of(BigInteger.valueOf((Long) n));
            } else {
                return Optional.of(BigInteger.valueOf(n.longValue()));
            }
        });
        registration.register(); // Ignoring logs here, we control the input
    }
}

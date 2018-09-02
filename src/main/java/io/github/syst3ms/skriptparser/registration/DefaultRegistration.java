package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.Skript;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.Arithmetic;
import io.github.syst3ms.skriptparser.types.comparisons.Comparator;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;
import io.github.syst3ms.skriptparser.util.math.BigDecimalMath;

import java.math.BigDecimal;
import java.math.BigInteger;

public class DefaultRegistration {
    public static void register() {
        SkriptRegistration registration = Main.getMainRegistration();
        registration.addType(
                Object.class,
                "object",
                "object\u00a6s"
        );
        registration.newType(Number.class,"number", "number\u00a6s")
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
                        if (o == null)
                            return TypeManager.NULL_REPRESENTATION;
                        if (o instanceof Long) {
                            return o.toString();
                        } else if (o instanceof Double) {
                            return Double.toString(o.doubleValue());
                        } else if (o instanceof BigInteger || o instanceof BigDecimal) {
                            return o.toString(); // Both BigInteger and BigDecimal override toString
                        }
                        assert false;
                        return null; // Can't happen, so we don't really have to worry about that
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
        registration.addType(
                String.class,
                "string",
                "string\u00a6s"
        );
        registration.newType(Boolean.class, "boolean", "boolean\u00a6s")
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
        registration.register();
        Comparators.registerComparator(
                Number.class,
                Number.class,
                new Comparator<Number, Number>(true) {
                    @Override
                    public Relation apply(Number number, Number number2) {
                        if (number.getClass() == number2.getClass()) {
                            return Relation.get(((Comparable<? super Number>) number).compareTo(number2));
                        } else if (number instanceof BigDecimal || number2 instanceof BigDecimal) {
                            BigDecimal bd = BigDecimalMath.getBigDecimal(number);
                            BigDecimal bd2 = BigDecimalMath.getBigDecimal(number2);
                            return Relation.get(bd.compareTo(bd2));
                        } else if ((number instanceof BigInteger || number2 instanceof BigInteger) && (number instanceof Long || number2 instanceof Long)) {
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
        registration.register();
    }
}

package io.github.syst3ms.skriptparser.util.math;

import io.github.syst3ms.skriptparser.parsing.SkriptRuntimeException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Utilities for various math operations using the generic {@link Number} type
 */
public class NumberMath {
    private static final BigDecimal RADIANS_TO_DEGREES = new BigDecimal(180).divide(BigDecimalMath.PI, BigDecimalMath.DEFAULT_CONTEXT);
    private static final BigDecimal DEGREES_TO_RADIANS = BigDecimalMath.PI.divide(new BigDecimal(180), BigDecimalMath.DEFAULT_CONTEXT);

    public static Number abs(Number n) {
        if (n instanceof Long) {
            return Math.abs(n.longValue());
        } else if (n instanceof Double) {
            return Math.abs(n.doubleValue());
        } else if (n instanceof BigInteger) {
            return ((BigInteger) n).abs();
        } else if (n instanceof BigDecimal) {
            return ((BigDecimal) n).abs();
        }
        throw new IllegalArgumentException();
    }

    public static Number sqrt(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.sqrt(n.doubleValue());
        } else {
            return BigDecimalMath.sqrt(bigToBigDecimal(n), BigDecimalMath.DEFAULT_CONTEXT);
        }
    }

    public static Number ln(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.log(n.doubleValue());
        } else {
            return BigDecimalMath.log(bigToBigDecimal(n), BigDecimalMath.DEFAULT_CONTEXT);
        }
    }

    public static Number log(Number base, Number n) {
        if ((n instanceof Long || n instanceof Double) && (base instanceof Long || base instanceof Double)) {
            return Math.log(n.doubleValue()) / Math.log(base.doubleValue());
        } else {
            BigDecimal bd = bigToBigDecimal(n);
            BigDecimal bdBase = bigToBigDecimal(base);
            return BigDecimalMath.log(bd, BigDecimalMath.DEFAULT_CONTEXT)
                                 .divide(BigDecimalMath.log(bdBase, BigDecimalMath.DEFAULT_CONTEXT), BigDecimalMath.DEFAULT_CONTEXT.getRoundingMode());
        }
    }

    public static Number factorial(Number n) {
        boolean bigInteger = n instanceof BigInteger ||
                             n.intValue() > 20 ||
                             n instanceof BigDecimal && ((BigDecimal) n).scale() == 0 ||
                             n instanceof Double && n.doubleValue() % 1 == 0;
        if (n.intValue() < 0) {
            throw new SkriptRuntimeException("Cannot compute the factorial of a negative number !");
        } else if (n.intValue() < 2) {
            return bigInteger ? BigInteger.ONE : 1L;
        } else if (bigInteger) {
            BigInteger m = toBigInteger(n);
            BigInteger result = BigInteger.ONE;
            while (!m.equals(BigInteger.ZERO)) {
                result = result.multiply(m);
                m = m.subtract(BigInteger.ONE);
            }
            return result;
        } else {
            long l = n.longValue();
            long result = 1;
            while (l != 0L) {
                result *= l;
                l--;
            }
            return result;
        }
    }

    public static Number floor(Number n) {
        if (n instanceof Long || n instanceof BigInteger) {
            return n;
        } else if (n instanceof Double) {
            return Math.floor(n.doubleValue());
        } else {
            assert n instanceof BigDecimal;
            return ((BigDecimal) n).setScale(0, RoundingMode.FLOOR);
        }
    }

    public static Number ceil(Number n) {
        if (n instanceof Long || n instanceof BigInteger) {
            return n;
        } else if (n instanceof Double) {
            return Math.ceil(n.doubleValue());
        } else {
            assert n instanceof BigDecimal;
            return ((BigDecimal) n).setScale(0, RoundingMode.CEILING);
        }
    }

    public static Number round(Number n) {
        if (n instanceof Long || n instanceof BigInteger) {
            return n;
        } else if (n instanceof Double) {
            return Math.round(n.doubleValue());
        } else {
            assert n instanceof BigDecimal;
            return ((BigDecimal) n).setScale(0, RoundingMode.HALF_EVEN);
        }
    }

    public static Number sin(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.sin(Math.toDegrees(n.doubleValue()));
        } else {
            return BigDecimalMath.sin(bigToBigDecimal(n).multiply(DEGREES_TO_RADIANS));
        }
    }

    public static Number cos(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.cos(Math.toDegrees(n.doubleValue()));
        } else {
            return BigDecimalMath.cos(bigToBigDecimal(n).multiply(DEGREES_TO_RADIANS));
        }
    }

    public static Number tan(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.tan(Math.toDegrees(n.doubleValue()));
        } else {
            return BigDecimalMath.tan(bigToBigDecimal(n).multiply(DEGREES_TO_RADIANS));
        }
    }

    public static Number asin(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.asin(n.doubleValue());
        } else {
            return BigDecimalMath.asin(bigToBigDecimal(n)).multiply(RADIANS_TO_DEGREES);
        }
    }

    public static Number acos(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.acos(n.doubleValue());
        } else {
            return BigDecimalMath.acos(bigToBigDecimal(n)).multiply(RADIANS_TO_DEGREES);
        }
    }

    public static Number atan(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.atan(n.doubleValue());
        } else {
            return BigDecimalMath.atan(bigToBigDecimal(n)).multiply(RADIANS_TO_DEGREES);
        }
    }

    private static BigDecimal bigToBigDecimal(Number n) {
        return n instanceof BigDecimal ? (BigDecimal) n : new BigDecimal((BigInteger) n);
    }

    private static BigInteger toBigInteger(Number n) {
        if (n instanceof Double || n instanceof Long) {
            return BigInteger.valueOf(n.longValue());
        } else if (n instanceof BigDecimal) {
            return ((BigDecimal) n).toBigInteger();
        } else {
            return (BigInteger) n;
        }
    }
}

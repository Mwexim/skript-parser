package io.github.syst3ms.skriptparser.util.math;

import io.github.syst3ms.skriptparser.parsing.SkriptRuntimeException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Utilities for various math operations using the generic {@link Number} type
 */
public class NumberMath {
    private static final int DEFAULT_SCALE = 64;

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
            return BigDecimalFunctions.sqrt(bigToBigDecimal(n), DEFAULT_SCALE);
        }
    }

    public static Number ln(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.log(n.doubleValue());
        } else {
            return BigDecimalFunctions.ln(bigToBigDecimal(n), DEFAULT_SCALE);
        }
    }

    public static Number log(Number base, Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.log(n.doubleValue()) / Math.log(base.doubleValue());
        } else {
            BigDecimal bd = bigToBigDecimal(n);
            BigDecimal bdBase = bigToBigDecimal(base);
            return BigDecimalFunctions.ln(bd, DEFAULT_SCALE)
                                      .divide(BigDecimalFunctions.ln(bdBase, DEFAULT_SCALE), RoundingMode.HALF_UP);
        }
    }

    public static Number factorial(Number n) {
        boolean bigInteger = n instanceof BigInteger || n.intValue() > 20;
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
            return ((BigDecimal) n).round(new MathContext(DEFAULT_SCALE, RoundingMode.FLOOR));
        }
    }

    public static Number ceil(Number n) {
        if (n instanceof Long || n instanceof BigInteger) {
            return n;
        } else if (n instanceof Double) {
            return Math.ceil(n.doubleValue());
        } else {
            assert n instanceof BigDecimal;
            return ((BigDecimal) n).round(new MathContext(DEFAULT_SCALE, RoundingMode.CEILING));
        }
    }

    public static Number round(Number n) {
        if (n instanceof Long || n instanceof BigInteger) {
            return n;
        } else if (n instanceof Double) {
            return Math.round(n.doubleValue());
        } else {
            assert n instanceof BigDecimal;
            return ((BigDecimal) n).round(new MathContext(DEFAULT_SCALE, RoundingMode.HALF_UP));
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
